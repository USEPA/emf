package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.postgres.PostgresSqlDataTypes;
import gov.epa.emissions.framework.EmfMockObjectTestCase;
import gov.epa.emissions.framework.services.cost.analysis.maxreduction.InventoryOutputQuery;
import gov.epa.emissions.framework.services.cost.analysis.maxreduction.ORLNonpointInventoryOutputQuery;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.services.data.EmfDataset;

import org.jmock.Mock;

public class ControlStrategyInventoryOuputQueryTest extends EmfMockObjectTestCase {
    
    public void testShouldCreateACorrectQuery(){
        
        Mock inputDataset = dataset("inputTable");
        Mock dbServer = mock(DbServer.class);
        Mock datasource = mock(Datasource.class);
        expects(datasource,1,"getName","emissions");
        dbServer.expects(once()).method("getEmissionsDatasource").withNoArguments().will(returnValue(datasource.proxy()));
        InventoryOutputQuery outputQuery = new ORLNonpointInventoryOutputQuery(new PostgresSqlDataTypes());
        ControlStrategyInventoryOuputQuery inventoryOutput = new ControlStrategyInventoryOuputQuery((EmfDataset) inputDataset.proxy(),outputQuery, (DbServer) dbServer.proxy());
        
        Mock result = mock(ControlStrategyResult.class);
        Mock detailResultDataset = dataset("detailResultTable");
        expects(result,1,"getDetailedResultDataset",detailResultDataset.proxy());
        String query = inventoryOutput.query((ControlStrategyResult) result.proxy());
        String expected="SELECT a.FIPS, a.SCC, a.SIC, a.MACT, a.SRCTYPE, a.NAICS, a.POLL, b.final_emissions, a.AVD_EMIS, b.control_eff, b.rule_pen, b.rule_eff, a.PRIMARY_DEVICE_TYPE_CODE, a.SECONDARY_DEVICE_TYPE_CODE, a.DATA_SOURCE, a.YEAR, a.TRIBAL_CODE, a.MACT_FLAG, a.PROCESS_MACT_COMPLIANCE_STATUS, a.START_DATE, a.END_DATE, a.WINTER_THROUGHPUT_PCT, a.SPRING_THROUGHPUT_PCT, a.SUMMER_THROUGHPUT_PCT, a.FALL_THROUGHPUT_PCT, a.ANNUAL_AVG_DAYS_PER_WEEK, a.ANNUAL_AVG_WEEKS_PER_YEAR, a.ANNUAL_AVG_HOURS_PER_DAY, a.ANNUAL_AVG_HOURS_PER_YEAR, a.PERIOD_DAYS_PER_WEEK, a.PERIOD_WEEKS_PER_PERIOD, a.PERIOD_HOURS_PER_DAY, PERIOD_HOURS_PER_PERIOD FROM emissions.inputTable AS a LEFT JOIN emissions.detailResultTable AS b  ON a.dataset_id=b.input_ds_id AND a.FIPS=b.FIPS AND a.SCC=b.SCC AND a.POLL=b.POLLUTANT";
        assertEquals(expected,query);
    }

    private Mock dataset(String tableName) {
        Mock dataset = mock(EmfDataset.class);
        expects(dataset,1,"getInternalSources",new InternalSource[]{internalSource(tableName)});
        return dataset;
    }

    private InternalSource internalSource(String tableName) {
        InternalSource source = new InternalSource();
        source.setTable(tableName);
        return source;
    }

}
