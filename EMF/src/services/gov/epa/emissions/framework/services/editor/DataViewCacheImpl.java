package gov.epa.emissions.framework.services.editor;

import gov.epa.emissions.commons.db.PageReader;
import gov.epa.emissions.commons.db.version.ScrollableVersionedRecords;
import gov.epa.emissions.commons.db.version.VersionedRecordsFactory;
import gov.epa.emissions.framework.services.basic.EmfProperty;
import gov.epa.emissions.framework.services.persistence.EmfPropertiesDAO;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.persistence.EntityManager;

public class DataViewCacheImpl implements DataViewCache {

    private Map readersMap;

    private VersionedRecordsFactory recordsReader;

    private EmfPropertiesDAO properties;

    public DataViewCacheImpl(VersionedRecordsFactory reader) {
        this(reader, new EmfPropertiesDAO());
    }

    public DataViewCacheImpl(VersionedRecordsFactory reader, EmfPropertiesDAO properties) {
        this.properties = properties;
        recordsReader = reader;
        readersMap = new HashMap();
    }

    public PageReader reader(DataAccessToken token) {
        return (PageReader) readersMap.get(token.key());

    }

    public void init(DataAccessToken token, EntityManager entityManager) throws SQLException {
        init(token, defaultPageSize(entityManager), entityManager);
    }

    public void init(DataAccessToken token, int pageSize, EntityManager entityManager) throws SQLException {
        initReader(token, pageSize, entityManager);
    }

    public void init(DataAccessToken token, int pageSize, String columnFilter, String rowFilter, String sortOrder,
            EntityManager entityManager) throws Exception {
        reinitialize(token, pageSize, columnFilter, rowFilter, sortOrder, entityManager);
    }

    public void applyConstraints(DataAccessToken token, String columnFilter, String rowFilter, String sortOrder,
            EntityManager entityManager) throws Exception {
        reinitialize(token, defaultPageSize(entityManager), columnFilter, rowFilter, sortOrder, entityManager);
    }

    public int defaultPageSize(EntityManager entityManager) {
        EmfProperty pageSize = properties.getProperty("page-size", entityManager);
        return Integer.parseInt(pageSize.getValue());
    }

    public int pageSize(DataAccessToken token) {
        return reader(token).pageSize();
    }

    public void invalidate() throws SQLException {
        closeReaders();
    }

    public void reload(DataAccessToken token, EntityManager entityManager) throws SQLException {
        close(token, entityManager);
        init(token, entityManager);
    }

    public void close(DataAccessToken token, EntityManager entityManager) throws SQLException {
        PageReader reader = (PageReader) readersMap.remove(token.key());
        if (reader != null)
            reader.close();
    }

    void closeReaders() throws SQLException {
        Collection all = readersMap.values();
        for (Iterator iter = all.iterator(); iter.hasNext();) {
            PageReader pageReader = (PageReader) iter.next();
            if (pageReader != null)
               pageReader.close();
        }

        readersMap.clear();
    }

    private void initReader(DataAccessToken token, int pageSize, EntityManager entityManager) throws SQLException {
        if (readersMap.containsKey(token.key()))
            return;
        int batchSize = batchSize(properties, entityManager);
        ScrollableVersionedRecords records = recordsReader.optimizedFetch(token.getVersion(), token.getTable(),
                batchSize, pageSize, entityManager);
        
        PageReader reader = new PageReader(pageSize, records);

        cacheReader(token, reader);
    }

    private void cacheReader(DataAccessToken token, PageReader reader) throws SQLException {
        if (readersMap.containsKey(token.key())) {// close old/stale reader - performance enhancement
            PageReader oldReader = reader(token);
            if (oldReader != null)
               oldReader.close();
        }

        readersMap.put(token.key(), reader);
    }

    private void reinitialize(DataAccessToken token, int pageSize, String columnFilter, String rowFilter,
            String sortOrder, EntityManager entityManager) throws Exception {
        int batchSize = batchSize(properties, entityManager);
        ScrollableVersionedRecords records = recordsReader.optimizedFetch(token.getVersion(), token.getTable(),
                batchSize, pageSize, columnFilter, rowFilter, sortOrder, entityManager);

        PageReader reader = new PageReader(pageSize, records);
        cacheReader(token, reader);
    }

    private int batchSize(EmfPropertiesDAO properties, EntityManager entityManager) {
        EmfProperty batchSize = properties.getProperty("batch-size", entityManager);
        return Integer.parseInt(batchSize.getValue());
    }
}
