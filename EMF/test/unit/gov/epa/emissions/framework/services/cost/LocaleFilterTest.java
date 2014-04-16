package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.framework.services.cost.controlStrategy.LocaleFilter;
import junit.framework.TestCase;

public class LocaleFilterTest extends TestCase {

    public void testShouldPassTheLocalForTheFips() {
        String fips = "101033";
        LocaleFilter filter = new LocaleFilter();
        assertTrue(filter.acceptLocale("", fips));
        assertTrue(filter.acceptLocale("10", fips));
        assertTrue(filter.acceptLocale("101", fips));
        assertTrue(filter.acceptLocale("101033", fips));
    }
    
    public void testShouldNotPassTheLocalForTheFips() {
        String fips = "101033";
        LocaleFilter filter = new LocaleFilter();
        assertFalse(filter.acceptLocale("20", fips));
        assertFalse(filter.acceptLocale("30000", fips));
        assertFalse(filter.acceptLocale("101037", fips));
        
        assertFalse(filter.acceptLocale("101031","10103"));
    }

}
