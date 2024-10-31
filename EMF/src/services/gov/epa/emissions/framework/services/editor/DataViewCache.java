package gov.epa.emissions.framework.services.editor;

import gov.epa.emissions.commons.db.PageReader;

import javax.persistence.EntityManager;

public interface DataViewCache {

    PageReader reader(DataAccessToken token);

    void init(DataAccessToken token, EntityManager entityManager) throws Exception;

    void applyConstraints(DataAccessToken token, String columnFilter, String rowFilter, String sortOrder, EntityManager entityManager)
            throws Exception;

    int defaultPageSize(EntityManager entityManager);

    int pageSize(DataAccessToken token);

    void init(DataAccessToken token, int pageSize, EntityManager entityManager) throws Exception;

    void invalidate() throws Exception;

    void reload(DataAccessToken token, EntityManager entityManager) throws Exception;

    void close(DataAccessToken token, EntityManager entityManager) throws Exception;

}