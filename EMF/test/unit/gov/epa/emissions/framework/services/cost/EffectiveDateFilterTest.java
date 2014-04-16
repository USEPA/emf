package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.services.cost.analysis.common.EffectiveDateFilter;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;

import java.text.ParseException;

import junit.framework.TestCase;

public class EffectiveDateFilterTest extends TestCase {

    public void testShouldGiveClosestEffectiveDate() throws ParseException {
        EfficiencyRecord[] records = {record("1999"),record("2000"),record("1999")};
        EffectiveDateFilter filter = new EffectiveDateFilter(records,1999);
        EfficiencyRecord[] filterRecords = filter.filter();
        assertEquals(2, filterRecords.length);
        assertEquals(filterRecords[0].getEffectiveDate(), records[0].getEffectiveDate());
        assertEquals(filterRecords[1].getEffectiveDate(), records[2].getEffectiveDate());
    }
    
    
    public void testShouldGiveClosestEffectiveDateWithSomeRecordsHaveNullEffectiveDates() throws ParseException {
        EfficiencyRecord[] records = {record(null),record("2000"),record("1988"),record("2001"),record("1999")};
        EffectiveDateFilter filter = new EffectiveDateFilter(records,1999);
        EfficiencyRecord[] filterRecords = filter.filter();
        assertEquals(1, filterRecords.length);
        assertEquals(filterRecords[0].getEffectiveDate(), records[4].getEffectiveDate());
    }
    
    
    public void testShouldGiveNullEffectiveDates() throws ParseException {
        EfficiencyRecord[] records = {record(null),record("2000"),record("1988"),record("2001"),record("1999")};
        EffectiveDateFilter filter = new EffectiveDateFilter(records,1980);
        EfficiencyRecord[] filterRecords = filter.filter();
        assertEquals(1, filterRecords.length);
        assertNull(filterRecords[0].getEffectiveDate());
    }

    private EfficiencyRecord record(String year) throws ParseException {
        EfficiencyRecord record = new EfficiencyRecord();
        if (year != null)
            record.setEffectiveDate(CustomDateFormat.parse_YYYY(year));
        return record;
    }

}
