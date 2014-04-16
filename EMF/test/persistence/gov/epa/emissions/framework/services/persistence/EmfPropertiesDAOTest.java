package gov.epa.emissions.framework.services.persistence;

import gov.epa.emissions.framework.services.ServiceTestCase;
import gov.epa.emissions.framework.services.basic.EmfProperty;

public class EmfPropertiesDAOTest extends ServiceTestCase {

    private EmfPropertiesDAO dao;

    protected void doSetUp() throws Exception {
        dao = new EmfPropertiesDAO();
    }

    protected void doTearDown() {// no op
    }

    public void testFetchPageSize() {
        EmfProperty pageSize = dao.getProperty("page-size", session);
        assertEquals(100, Integer.parseInt(pageSize.getValue()));
    }
    
    public void testFetchTimeInterval() {
        EmfProperty timeInteval = dao.getProperty("lock.time-interval", session);
        assertEquals(7200000, Integer.parseInt(timeInteval.getValue()));
    }
    
    public void testFetchBatchSize() {
        EmfProperty timeInteval = dao.getProperty("batch-size", session);
        assertEquals(10000, Integer.parseInt(timeInteval.getValue()));
    }
    
    public void testFetchExportBatchSize() {
        EmfProperty timeInteval = dao.getProperty("export-batch-size", session);
        assertEquals(150000, Integer.parseInt(timeInteval.getValue()));
    }

}
