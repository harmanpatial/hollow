package com.netflix.vms.transformer.hollowinput;

import com.netflix.hollow.api.objects.delegate.HollowObjectAbstractDelegate;
import com.netflix.hollow.core.read.dataaccess.HollowObjectTypeDataAccess;
import com.netflix.hollow.core.schema.HollowObjectSchema;

@SuppressWarnings("all")
public class ArtworkRecipeDelegateLookupImpl extends HollowObjectAbstractDelegate implements ArtworkRecipeDelegate {

    private final ArtworkRecipeTypeAPI typeAPI;

    public ArtworkRecipeDelegateLookupImpl(ArtworkRecipeTypeAPI typeAPI) {
        this.typeAPI = typeAPI;
    }

    public int getRecipeNameOrdinal(int ordinal) {
        return typeAPI.getRecipeNameOrdinal(ordinal);
    }

    public int getCdnFolderOrdinal(int ordinal) {
        return typeAPI.getCdnFolderOrdinal(ordinal);
    }

    public int getExtensionOrdinal(int ordinal) {
        return typeAPI.getExtensionOrdinal(ordinal);
    }

    public int getHostNameOrdinal(int ordinal) {
        return typeAPI.getHostNameOrdinal(ordinal);
    }

    public ArtworkRecipeTypeAPI getTypeAPI() {
        return typeAPI;
    }

    @Override
    public HollowObjectSchema getSchema() {
        return typeAPI.getTypeDataAccess().getSchema();
    }

    @Override
    public HollowObjectTypeDataAccess getTypeDataAccess() {
        return typeAPI.getTypeDataAccess();
    }

}