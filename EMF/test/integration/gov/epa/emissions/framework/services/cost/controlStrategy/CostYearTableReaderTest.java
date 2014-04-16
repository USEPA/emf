package gov.epa.emissions.framework.services.cost.controlStrategy;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.ServiceTestCase;

public class CostYearTableReaderTest extends ServiceTestCase {

    protected void doSetUp() throws Exception {
        // NOTE Auto-generated method stub
        
    }

    protected void doTearDown() throws Exception {
        // NOTE Auto-generated method stub
    }
    
    public void testShouldReadCostYearTable() throws EmfException{
        CostYearTableReader reader = new CostYearTableReader(dbServer(),2000);
        CostYearTable table = reader.costYearTable();
        assertEquals(77,table.size());
        assertEquals(2.0665193,table.factor(1977),0.0000001);
    }
    

}
