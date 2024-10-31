package gov.epa.emissions.framework.services.editor;

import gov.epa.emissions.commons.db.version.ChangeSet;

import javax.persistence.EntityManager;

public interface DataUpdatesCache {

    void init(DataAccessToken token, EntityManager entityManager) throws Exception;

    void invalidate() throws Exception;

    void reload(DataAccessToken token, EntityManager entityManager) throws Exception;

    void close(DataAccessToken token, EntityManager entityManager) throws Exception;

    /*
     * Keeps a two-level mapping. First map, ChangeSetMap is a map of tokens and PageChangeSetMap. PageChangeSetMap maps
     * Page Number to Change Sets (of that Page)
     */
    ChangeSets changesets(DataAccessToken token, int pageNumber, EntityManager entityManager) throws Exception;

    void submitChangeSet(DataAccessToken token, ChangeSet changeset, int pageNumber, EntityManager entityManager) throws Exception;

    void discardChangeSets(DataAccessToken token, EntityManager entityManager) throws Exception;

    ChangeSets changesets(DataAccessToken token, EntityManager entityManager) throws Exception;

    boolean hasChanges(DataAccessToken token, EntityManager entityManager) throws Exception;

    void save(DataAccessToken token, EntityManager entityManager) throws Exception;

}