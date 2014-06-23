package gov.epa.emissions.framework.services.editor;

import gov.epa.emissions.commons.db.version.ChangeSet;

import org.hibernate.Session;

public interface DataUpdatesCache {

    void init(DataAccessToken token, Session session) throws Exception;

    void invalidate() throws Exception;

    void reload(DataAccessToken token, Session session) throws Exception;

    void close(DataAccessToken token, Session session) throws Exception;

    /*
     * Keeps a two-level mapping. First map, ChangeSetMap is a map of tokens and PageChangeSetMap. PageChangeSetMap maps
     * Page Number to Change Sets (of that Page)
     */
    ChangeSets changesets(DataAccessToken token, int pageNumber, Session session) throws Exception;

    void submitChangeSet(DataAccessToken token, ChangeSet changeset, int pageNumber, Session session) throws Exception;

    void discardChangeSets(DataAccessToken token, Session session) throws Exception;

    ChangeSets changesets(DataAccessToken token, Session session) throws Exception;

    boolean hasChanges(DataAccessToken token, Session session) throws Exception;

    void save(DataAccessToken token, Session session) throws Exception;

}