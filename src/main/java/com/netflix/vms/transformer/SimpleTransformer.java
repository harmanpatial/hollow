package com.netflix.vms.transformer;

import com.netflix.hollow.read.engine.HollowReadStateEngine;
import com.netflix.hollow.util.SimultaneousExecutor;
import com.netflix.hollow.write.HollowWriteStateEngine;
import com.netflix.hollow.write.objectmapper.HollowObjectMapper;
import com.netflix.vms.transformer.hollowinput.VMSHollowVideoInputAPI;
import com.netflix.vms.transformer.hollowinput.VideoDisplaySetHollow;
import com.netflix.vms.transformer.hollowoutput.CompleteVideo;
import com.netflix.vms.transformer.hollowoutput.CompleteVideoFacetData;
import com.netflix.vms.transformer.hollowoutput.DeploymentIntent;
import com.netflix.vms.transformer.hollowoutput.ISOCountry;
import com.netflix.vms.transformer.hollowoutput.Video;
import com.netflix.vms.transformer.hollowoutput.VideoCollectionsData;
import com.netflix.vms.transformer.hollowoutput.VideoMetaData;
import com.netflix.vms.transformer.index.VMSTransformerIndexer;
import com.netflix.vms.transformer.misc.TopNVideoDataModule;
import com.netflix.vms.transformer.modules.TransformModule;
import com.netflix.vms.transformer.modules.collections.VideoCollectionsDataHierarchy;
import com.netflix.vms.transformer.modules.collections.VideoCollectionsModule;
import com.netflix.vms.transformer.modules.deploymentintent.CacheDeploymentIntentModule;
import com.netflix.vms.transformer.modules.meta.VideoMetaDataModule;
import com.netflix.vms.transformer.modules.mpl.DrmSystemModule;
import com.netflix.vms.transformer.modules.mpl.EncodingProfileModule;
import com.netflix.vms.transformer.modules.mpl.OriginServerModule;
import com.netflix.vms.transformer.modules.passthrough.artwork.ArtworkFormatModule;
import com.netflix.vms.transformer.modules.passthrough.artwork.ArtworkImageRecipeModule;
import com.netflix.vms.transformer.modules.passthrough.artwork.ArtworkTypeModule;
import com.netflix.vms.transformer.modules.passthrough.artwork.DefaultExtensionRecipeModule;
import com.netflix.vms.transformer.modules.passthrough.beehive.RolloutCharacterModule;
import com.netflix.vms.transformer.modules.person.GlobalPersonModule;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleTransformer {

    private final ThreadLocal<VideoCollectionsModule> collectionsModuleRef = new ThreadLocal<VideoCollectionsModule>();
    private final ThreadLocal<VideoMetaDataModule> metadataModuleRef = new ThreadLocal<VideoMetaDataModule>();

    private final VMSHollowVideoInputAPI api;
    private VMSTransformerIndexer indexer;

    public SimpleTransformer(VMSHollowVideoInputAPI api) {
        this.api = api;
    }

    public HollowWriteStateEngine transform() throws Exception {
        indexer = new VMSTransformerIndexer((HollowReadStateEngine)api.getDataAccess(), new SimultaneousExecutor());

        final ShowHierarchyInitializer hierarchyInitializer = new ShowHierarchyInitializer(api, indexer);

        HollowWriteStateEngine writeStateEngine = new HollowWriteStateEngine();  //TODO: Need to define a HashCodeFinder.
        final HollowObjectMapper objectMapper = new HollowObjectMapper(writeStateEngine);

        SimultaneousExecutor executor = new SimultaneousExecutor();

        long startTime = System.currentTimeMillis();

        for(final VideoDisplaySetHollow displaySet : api.getAllVideoDisplaySetHollow()) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    VideoCollectionsModule collectionsModule = getVideoCollectionsModule();
                    VideoMetaDataModule metadataModule = getVideoMetaDataModule();

                    Map<String, ShowHierarchy> showHierarchiesByCountry = hierarchyInitializer.getShowHierarchiesByCountry(displaySet);

                    if(showHierarchiesByCountry != null) {
                        Map<String, VideoCollectionsDataHierarchy> vcdByCountry = collectionsModule.buildVideoCollectionsDataByCountry(showHierarchiesByCountry);
                        Map<String, Map<Integer, VideoMetaData>> vmdByCountry = metadataModule.buildVideoMetaDataByCountry(showHierarchiesByCountry);

                        if(vcdByCountry != null)
                            writeJustTheCurrentData(vcdByCountry, vmdByCountry, objectMapper);
                    }
                }
            });
        }

        objectMapper.addObject(new DeploymentIntent());

        // Register Transform Modules
        List<TransformModule> moduleList = Arrays.<TransformModule>asList(
                new DrmSystemModule(api, objectMapper),
                new OriginServerModule(api, objectMapper, indexer),
                new EncodingProfileModule(api, objectMapper, indexer),

                new ArtworkFormatModule(api, objectMapper),
                new CacheDeploymentIntentModule(api, objectMapper),
                new ArtworkTypeModule(api, objectMapper),
                new ArtworkImageRecipeModule(api, objectMapper),
                new DefaultExtensionRecipeModule(api, objectMapper),
                new RolloutCharacterModule(api, objectMapper),
                new GlobalPersonModule(api, objectMapper, indexer),
                new TopNVideoDataModule(api, objectMapper)
                );

        // Execute Transform Modules
        for(TransformModule m : moduleList) {
            long tStart = System.currentTimeMillis();
            m.transform();
            long tDuration = System.currentTimeMillis() - tStart;
            System.out.println(String.format("Finished Trasform for module=%s, duration=%s", m.getName(), tDuration));
        }


        executor.awaitSuccessfulCompletion();

        long endTime = System.currentTimeMillis();
        System.out.println("Processed all videos in " + (endTime - startTime) + "ms");

        return writeStateEngine;
    }

    private VideoCollectionsModule getVideoCollectionsModule() {
        VideoCollectionsModule module = collectionsModuleRef.get();
        if(module == null) {
            module = new VideoCollectionsModule(api, indexer);
            collectionsModuleRef.set(module);
        }
        return module;
    }

    private VideoMetaDataModule getVideoMetaDataModule() {
        VideoMetaDataModule module = metadataModuleRef.get();
        if(module == null) {
            module = new VideoMetaDataModule(api, indexer);
            metadataModuleRef.set(module);
        }
        return module;
    }

    private void writeJustTheCurrentData(Map<String, VideoCollectionsDataHierarchy> vcdByCountry,
            Map<String, Map<Integer, VideoMetaData>> vmdByCountry,
            HollowObjectMapper objectMapper) {

        for(Map.Entry<String, VideoCollectionsDataHierarchy> countryHierarchyEntry : vcdByCountry.entrySet()) {
            String countryId = countryHierarchyEntry.getKey();
            ISOCountry country = getCountry(countryId);
            VideoCollectionsDataHierarchy hierarchy = countryHierarchyEntry.getValue();

            CompleteVideo topNode = new CompleteVideo();
            topNode.country = country;
            topNode.facetData = new CompleteVideoFacetData();
            topNode.facetData.videoCollectionsData = hierarchy.getTopNode();
            topNode.id = topNode.facetData.videoCollectionsData.topNode;
            topNode.facetData.videoMetaData = vmdByCountry.get(countryId).get(topNode.id.value);

            objectMapper.addObject(topNode);

            if(topNode.facetData.videoCollectionsData.nodeType == VideoCollectionsDataHierarchy.SHOW) {
                int sequenceNumber = 0;

                for(Map.Entry<Integer, VideoCollectionsData> showEntry : hierarchy.getOrderedSeasons().entrySet()) {
                    CompleteVideo showNode = new CompleteVideo();
                    showNode.country = country;
                    showNode.id = new Video(showEntry.getKey().intValue());
                    showNode.facetData = new CompleteVideoFacetData();
                    showNode.facetData.videoCollectionsData = showEntry.getValue();
                    showNode.facetData.videoMetaData = vmdByCountry.get(countryId).get(showNode.id.value);

                    objectMapper.addObject(showNode);

                    for(Map.Entry<Integer, VideoCollectionsData> episodeEntry : hierarchy.getOrderedSeasonEpisodes(++sequenceNumber).entrySet()) {
                        CompleteVideo episodeNode = new CompleteVideo();
                        episodeNode.country = country;
                        episodeNode.id = new Video(episodeEntry.getKey().intValue());
                        episodeNode.facetData = new CompleteVideoFacetData();
                        episodeNode.facetData.videoCollectionsData = episodeEntry.getValue();
                        episodeNode.facetData.videoMetaData = vmdByCountry.get(countryId).get(episodeNode.id.value);

                        objectMapper.addObject(episodeNode);
                    }
                }
            }

            for(Map.Entry<Integer, VideoCollectionsData> supplementalEntry : hierarchy.getSupplementalVideosCollectionsData().entrySet()) {
                CompleteVideo supplementalNode = new CompleteVideo();
                supplementalNode.country = country;
                supplementalNode.id = new Video(supplementalEntry.getKey().intValue());
                supplementalNode.facetData = new CompleteVideoFacetData();
                supplementalNode.facetData.videoCollectionsData = supplementalEntry.getValue();
                supplementalNode.facetData.videoMetaData = vmdByCountry.get(countryId).get(supplementalNode.id.value);

                objectMapper.addObject(supplementalNode);
            }
        }
    }

    private Map<String, ISOCountry> countries = new HashMap<String, ISOCountry>();
    private ISOCountry getCountry(String id) {
        ISOCountry country = countries.get(id);
        if(country == null) {
            country = new ISOCountry(id);
            countries.put(id, country);
        }
        return country;
    }

}
