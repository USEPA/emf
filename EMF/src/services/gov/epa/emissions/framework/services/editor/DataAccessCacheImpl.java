package gov.epa.emissions.framework.services.editor;

import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.PageReader;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.db.version.ChangeSet;
import gov.epa.emissions.commons.db.version.VersionedRecordsFactory;
import gov.epa.emissions.framework.services.persistence.EmfPropertiesDAO;

import org.hibernate.Session;

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

    public void init(DataAccessToken token, Session session) throws Exception {
        init(token, defaultPageSize(session), session);
    }

    public void applyConstraints(DataAccessToken token, String columnFilter, String rowFilter, String sortOrder,
            Session session) throws Exception {
        view.applyConstraints(token, columnFilter, rowFilter, sortOrder, session);
    }

    public void init(DataAccessToken token, int pageSize, Session session) throws Exception {
        view.init(token, pageSize, session);
        updates.init(token, session);
    }

    public PageReader reader(DataAccessToken token) {
        return view.reader(token);
    }

    /*
     * Keeps a two-level mapping. First map, ChangeSetMap is a map of tokens and PageChangeSetMap. PageChangeSetMap maps
     * Page Number to Change Sets (of that Page)
     */
    public ChangeSets changesets(DataAccessToken token, int pageNumber, Session session) throws Exception {
        return updates.changesets(token, pageNumber, session);
    }

    public void submitChangeSet(DataAccessToken token, ChangeSet changeset, int pageNumber, Session session)
            throws Exception {
        updates.submitChangeSet(token, changeset, pageNumber, session);
    }

    public void discardChangeSets(DataAccessToken token, Session session) throws Exception {
        updates.discardChangeSets(token, session);
    }

    public ChangeSets changesets(DataAccessToken token, Session session) throws Exception {
        return updates.changesets(token, session);
    }

    public int defaultPageSize(Session session) {
        return view.defaultPageSize(session);
    }

    public int pageSize(DataAccessToken token) {
        return view.pageSize(token);
    }

    public void invalidate() throws Exception {
        view.invalidate();
        updates.invalidate();
    }

    public void reload(DataAccessToken token, Session session) throws Exception {
        close(token, session);
        init(token, session);
    }

    public void close(DataAccessToken token, Session session) throws Exception {
        view.close(token, session);
        updates.close(token, session);
    }

    public void save(DataAccessToken token, Session session) throws Exception {
        updates.save(token, session);
        reload(token, session);
    }

    public boolean hasChanges(DataAccessToken token, Session session) throws Exception {
        return updates.hasChanges(token, session);
    }

}
