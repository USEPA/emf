package gov.epa.emissions.framework.services.editor;

import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.db.version.ChangeSet;
import gov.epa.emissions.commons.db.version.VersionedRecordsWriter;
import gov.epa.emissions.framework.services.EmfProperties;
import gov.epa.emissions.framework.services.basic.EmfProperty;
import gov.epa.emissions.framework.services.editor.ChangeSets.ChangeSetsIterator;
import gov.epa.emissions.framework.services.persistence.EmfPropertiesDAO;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.hibernate.Session;

public class DataUpdatesCacheImpl implements DataUpdatesCache {

    private Map writersMap;

    private Datasource datasource;

    private SqlDataTypes sqlTypes;

    private Map changesetsMap;

    private VersionedRecordsWriterFactory writerFactory;

    private EmfProperties properties;

    public DataUpdatesCacheImpl(VersionedRecordsWriterFactory writerFactory, Datasource datasource,
            SqlDataTypes sqlTypes) {
        this(writerFactory, datasource, sqlTypes, new EmfPropertiesDAO());
    }

    public DataUpdatesCacheImpl(VersionedRecordsWriterFactory writerFactory, Datasource datasource,
            SqlDataTypes sqlTypes, EmfProperties properties) {
        this.properties = properties;

        this.writerFactory = writerFactory;
        this.datasource = datasource;
        this.sqlTypes = sqlTypes;

        writersMap = new HashMap();
        changesetsMap = new HashMap();
    }

    public VersionedRecordsWriter writer(DataAccessToken token) {
        return (VersionedRecordsWriter) writersMap.get(token.key());
    }

    /*
     * Keeps a two-level mapping. First map, ChangeSetMap is a map of tokens and PageChangeSetMap. PageChangeSetMap maps
     * Page Number to Change Sets (of that Page)
     */
    public ChangeSets changesets(DataAccessToken token, int pageNumber, Session session) throws Exception {
        Map map = pageChangesetsMap(token);
        Integer pageKey = pageChangesetsKey(pageNumber);
        if (!map.containsKey(pageKey)) {
            map.put(pageKey, new ChangeSets());
        }

        return (ChangeSets) map.get(pageKey);
    }

    public void submitChangeSet(DataAccessToken token, ChangeSet changeset, int pageNumber, Session session)
            throws Exception {
        ChangeSets sets = changesets(token, pageNumber, session);
        sets.add(changeset);
    }

    public void discardChangeSets(DataAccessToken token, Session session) throws SQLException {
        Map pageChangsetsMap = pageChangesetsMap(token);
        pageChangsetsMap.clear();
    }

    public ChangeSets changesets(DataAccessToken token, Session session) throws SQLException {
        ChangeSets all = new ChangeSets();

        Map pageChangesetsMap = pageChangesetsMap(token);
        Set keys = new TreeSet(pageChangesetsMap.keySet());
        for (Iterator iter = keys.iterator(); iter.hasNext();) {
            ChangeSets sets = (ChangeSets) pageChangesetsMap.get(iter.next());
            all.add(sets);
        }

        return all;
    }

    public int defaultPageSize(Session session) {
        EmfProperty pageSize = properties.getProperty("page-size", session);
        return Integer.parseInt(pageSize.getValue());
    }

    public void init(DataAccessToken token, Session session) throws SQLException {
        initChangesetsMap(token);
        initWriter(token);
    }

    public void invalidate() throws SQLException {
        closeWriters();
        changesetsMap.clear();
    }

    public void reload(DataAccessToken token, Session session) throws SQLException {
        close(token, session);
        init(token, session);
    }

    public void close(DataAccessToken token, Session session) throws SQLException {
        removeChangesets(token, session);
        closeWriter(token);
    }

    private void closeWriter(DataAccessToken token) throws SQLException {
        VersionedRecordsWriter writer = (VersionedRecordsWriter) writersMap.remove(token.key());
        writer.close();
    }

    private void removeChangesets(DataAccessToken token, Session session) throws SQLException {
        discardChangeSets(token, session);
        changesetsMap.remove(token.key());
    }

    private void closeWriters() throws SQLException {
        Collection all = writersMap.values();
        for (Iterator iter = all.iterator(); iter.hasNext();) {
            VersionedRecordsWriter writer = (VersionedRecordsWriter) iter.next();
            writer.close();
        }

        writersMap.clear();
    }

    private Integer pageChangesetsKey(int pageNumber) {
        return new Integer(pageNumber);
    }

    private Map pageChangesetsMap(DataAccessToken token) throws SQLException {
        init(token, null);
        return (Map) changesetsMap.get(token.key());
    }

    private void initWriter(DataAccessToken token) throws SQLException {
        if (!writersMap.containsKey(token.key())) {
            VersionedRecordsWriter writer = writerFactory.create(datasource, token.getTable(), sqlTypes);
            writersMap.put(token.key(), writer);
        }
    }

    private void initChangesetsMap(DataAccessToken token) {
        if (!changesetsMap.containsKey(token.key())) {
            changesetsMap.put(token.key(), new HashMap());
        }
    }

    public void save(DataAccessToken token, Session session) throws Exception {
        VersionedRecordsWriter writer = writer(token);
        ChangeSets sets = changesets(token, session);
        for (ChangeSetsIterator iter = sets.iterator(); iter.hasNext();) {
            ChangeSet element = iter.next();
            writer.update(element);
        }

        reload(token, session);
    }

    public boolean hasChanges(DataAccessToken token, Session session) throws Exception {
        return changesets(token, session).hasChanges();
    }

}
