package com.netflix.vms.transformer.hollowinput;

import com.netflix.hollow.objects.HollowObject;
import com.netflix.hollow.HollowObjectSchema;

public class PersonArtworkHollow extends HollowObject {

    public PersonArtworkHollow(PersonArtworkDelegate delegate, int ordinal) {
        super(delegate, ordinal);
    }

    public ArtworkDerivativeListHollow _getDerivatives() {
        int refOrdinal = delegate().getDerivativesOrdinal(ordinal);
        if(refOrdinal == -1)
            return null;
        return  api().getArtworkDerivativeListHollow(refOrdinal);
    }

    public ArtworkLocaleListHollow _getLocales() {
        int refOrdinal = delegate().getLocalesOrdinal(ordinal);
        if(refOrdinal == -1)
            return null;
        return  api().getArtworkLocaleListHollow(refOrdinal);
    }

    public long _getSeqNum() {
        return delegate().getSeqNum(ordinal);
    }

    public Long _getSeqNumBoxed() {
        return delegate().getSeqNumBoxed(ordinal);
    }

    public long _getOrdinalPriority() {
        return delegate().getOrdinalPriority(ordinal);
    }

    public Long _getOrdinalPriorityBoxed() {
        return delegate().getOrdinalPriorityBoxed(ordinal);
    }

    public StringHollow _getSourceFileId() {
        int refOrdinal = delegate().getSourceFileIdOrdinal(ordinal);
        if(refOrdinal == -1)
            return null;
        return  api().getStringHollow(refOrdinal);
    }

    public ArtworkAttributesHollow _getAttributes() {
        int refOrdinal = delegate().getAttributesOrdinal(ordinal);
        if(refOrdinal == -1)
            return null;
        return  api().getArtworkAttributesHollow(refOrdinal);
    }

    public long _getPersonId() {
        return delegate().getPersonId(ordinal);
    }

    public Long _getPersonIdBoxed() {
        return delegate().getPersonIdBoxed(ordinal);
    }

    public StringHollow _getFileImageType() {
        int refOrdinal = delegate().getFileImageTypeOrdinal(ordinal);
        if(refOrdinal == -1)
            return null;
        return  api().getStringHollow(refOrdinal);
    }

    public VMSHollowInputAPI api() {
        return typeApi().getAPI();
    }

    public PersonArtworkTypeAPI typeApi() {
        return delegate().getTypeAPI();
    }

    protected PersonArtworkDelegate delegate() {
        return (PersonArtworkDelegate)delegate;
    }

}