package gov.epa.emissions.framework.services.editor;

import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.PageReader;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.db.version.ChangeSet;
import gov.epa.emissions.commons.db.version.VersionedRecordsFactory;
import gov.epa.emissions.framework.services.persistence.EmfPropertiesDAO;

import javax.persistence.EntityManager;

public class DataAccessCacheImpl implements DataAccessCache {

    private DataViewCache view;

    private DataUpdatesCache updates;

    public DataAccessCacheImpl(VersionedRecordsFactory reader, VersionedRecordsWriterFactory writerFactory,
            Datasource datasource, SqlDataTypes sqlTypes) {
        this(reader, writerFactory, datasource, sqlTypes, new EmfPropertiesDAO());
    }

    public DataAccessCacheImpl(VersionedRecordsFactory reader, VersionedRecordsWriterFactory writerFactory,
            Datasource datasource, SqlDataTypes sqlTypes, EmfPropertiesDAO properties) {
        view = new DataViewCacheImpl(reader, properties);
        updates = new DataUpdatesCacheImpl(writerFactory, datasource, sqlTypes, properties);
    }

    public DataAccessCacheImpl(DataViewCache view, DataUpdatesCache updates) {
        this.view = view;
        this.updates = updates;
    }

    public void init(DataAccessToken token, EntityManager entityManager) throws Exception {
        init(token, defaultPageSize(entityManager), entityManager);
    }

    public void applyConstraints(DataAccessToken token, String columnFilter, String rowFilter, String sortOrder,
            EntityManager entityManager) throws Exception {
        view.applyConstraints(token, columnFilter, rowFilter, sortOrder, entityManager);
    }

    public void init(DataAccessToken token, int pageSize, EntityManager entityManager) throws Exception {
        view.init(token, pageSize, entityManager);
        updates.init(token, entityManager);
    }

    public PageReader reader(DataAccessToken token) {
        return view.reader(token);
    }

    /*
     * Keeps a two-level mapping. First map, ChangeSetMap is a map of tokens and PageChangeSetMap. PageChangeSetMap maps
     * Page Number to Change Sets (of that Page)
     */
    public ChangeSets changesets(DataAccessToken token, int pageNumber, EntityManager entityManager) throws Exception {
        return updates.changesets(token, pageNumber, entityManager);
    }

    public void submitChangeSet(DataAccessToken token, ChangeSet changeset, int pageNumber, EntityManager entityManager)
            throws Exception {
        updates.submitChangeSet(token, changeset, pageNumber, entityManager);
    }

    public void discardChangeSets(DataAccessToken token, EntityManager entityManager) throws Exception {
        updates.discardChangeSets(token, entityManager);
    }

    public ChangeSets changesets(DataAccessToken token, EntityManager entityManager) throws Exception {
        return updates.changesets(token, entityManager);
    }

    public int defaultPageSize(EntityManager entityManager) {
        return view.defaultPageSize(entityManager);
    }

    public int pageSize(DataAccessToken token) {
        return view.pageSize(token);
    }

    public void invalidate() throws Exception {
        view.invalidate();
        updates.invalidate();
    }

    public void reload(DataAccessToken token, EntityManager entityManager) throws Exception {
        close(token, entityManager);
        init(token, entityManager);
    }

    public void close(DataAccessToken token, EntityManager entityManager) throws Exception {
        view.close(token, entityManager);
        updates.close(token, entityManager);
    }

    public void save(DataAccessToken token, EntityManager entityManager) throws Exception {
        updates.save(token, entityManager);
        reload(token, entityManager);
    }

    public boolean hasChanges(DataAccessToken token, EntityManager entityManager) throws Exception {
        return updates.hasChanges(token, entityManager);
    }

}
