package com.netflix.vms.transformer.hollowinput;

import com.netflix.hollow.objects.delegate.HollowObjectDelegate;

public interface AssetMetaDatasDelegate extends HollowObjectDelegate {

    public int getAssetIdOrdinal(int ordinal);

    public int getTrackLabelsOrdinal(int ordinal);

    public AssetMetaDatasTypeAPI getTypeAPI();

}