package com.netflix.vms.transformer.hollowinput;

import com.netflix.hollow.objects.delegate.HollowObjectAbstractDelegate;
import com.netflix.hollow.read.dataaccess.HollowObjectTypeDataAccess;
import com.netflix.hollow.HollowObjectSchema;

public class RolloutPhaseArtworkSourceFileIdDelegateLookupImpl extends HollowObjectAbstractDelegate implements RolloutPhaseArtworkSourceFileIdDelegate {

    private final RolloutPhaseArtworkSourceFileIdTypeAPI typeAPI;

    public RolloutPhaseArtworkSourceFileIdDelegateLookupImpl(RolloutPhaseArtworkSourceFileIdTypeAPI typeAPI) {
        this.typeAPI = typeAPI;
    }

    public int getValueOrdinal(int ordinal) {
        return typeAPI.getValueOrdinal(ordinal);
    }

    public RolloutPhaseArtworkSourceFileIdTypeAPI getTypeAPI() {
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