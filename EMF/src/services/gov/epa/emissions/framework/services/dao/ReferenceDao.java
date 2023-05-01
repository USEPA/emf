package gov.epa.emissions.framework.services.dao;

import java.util.List;

import gov.epa.emissions.commons.data.Reference;

public interface ReferenceDao {

    void addReference(Reference reference);

    List<Reference> getReferences();

    List<Reference> getReferences(String textContains);

    boolean canUpdate(Reference reference);

    Long getReferenceCount();

    Long getReferenceCount(String text);

    boolean descriptionUsed(String description);

}