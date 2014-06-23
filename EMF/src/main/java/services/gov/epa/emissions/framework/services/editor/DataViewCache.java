package gov.epa.emissions.framework.services.editor;

import gov.epa.emissions.commons.db.PageReader;

import org.hibernate.Session;

public interface DataViewCache {

    PageReader reader(DataAccessToken token);

    void init(DataAccessToken token, Session session) throws Exception;

    void applyConstraints(DataAccessToken token, String columnFilter, String rowFilter, String sortOrder, Session session)
            throws Exception;

    int defaultPageSize(Session session);

    int pageSize(DataAccessToken token);

    void init(DataAccessToken token, int pageSize, Session session) throws Exception;

    void invalidate() throws Exception;

    void reload(DataAccessToken token, Session session) throws Exception;

    void close(DataAccessToken token, Session session) throws Exception;

}