package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.db.postgres.PostgresSqlDataTypes;
import gov.epa.emissions.framework.services.cost.analysis.maxreduction.ORLNonpointInventoryOutputQuery;
import junit.framework.TestCase;

public class ORLNonpointInventoryOutputQueryTest extends TestCase {

    public void testSelectClauseQuery() {
        ORLNonpointInventoryOutputQuery query = new ORLNonpointInventoryOutputQuery(new PostgresSqlDataTypes());
        String expected = "a.FIPS, a.SCC, a.SIC, a.MACT, a.SRCTYPE, a.NAICS, a.POLL, b.final_emissions, a.AVD_EMIS, b.control_eff, b.rule_pen, b.rule_eff, a.PRIMARY_DEVICE_TYPE_CODE, a.SECONDARY_DEVICE_TYPE_CODE, a.DATA_SOURCE, a.YEAR, a.TRIBAL_CODE, a.MACT_FLAG, a.PROCESS_MACT_COMPLIANCE_STATUS, a.START_DATE, a.END_DATE, a.WINTER_THROUGHPUT_PCT, a.SPRING_THROUGHPUT_PCT, a.SUMMER_THROUGHPUT_PCT, a.FALL_THROUGHPUT_PCT, a.ANNUAL_AVG_DAYS_PER_WEEK, a.ANNUAL_AVG_WEEKS_PER_YEAR, a.ANNUAL_AVG_HOURS_PER_DAY, a.ANNUAL_AVG_HOURS_PER_YEAR, a.PERIOD_DAYS_PER_WEEK, a.PERIOD_WEEKS_PER_PERIOD, a.PERIOD_HOURS_PER_DAY, PERIOD_HOURS_PER_PERIOD";
        assertEquals(expected, query.selectClause("a", "b"));
    }

    public void testSelectContditionalClause() {
        ORLNonpointInventoryOutputQuery query = new ORLNonpointInventoryOutputQuery(new PostgresSqlDataTypes());
        String expected = "a.dataset_id=b.input_ds_id AND a.FIPS=b.FIPS AND a.SCC=b.SCC AND a.POLL=b.POLLUTANT";
        assertEquals(expected, query.conditionalClause("a", "b"));
    }

}
