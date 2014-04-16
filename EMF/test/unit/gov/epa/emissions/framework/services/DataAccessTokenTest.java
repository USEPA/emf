package gov.epa.emissions.framework.services;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.services.editor.DataAccessToken;

import java.util.Date;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;

public class DataAccessTokenTest extends MockObjectTestCase {

    public void testShouldReturnVersionLockDateAsLockStartDate() {
        Mock version = mock(Version.class);
        Date lockDate = new Date();
        version.stubs().method("getLockDate").withNoArguments().will(returnValue(lockDate));
        
        DataAccessToken token = new DataAccessToken((Version)version.proxy(), null);
        
        assertEquals(lockDate, token.lockStart());
    }
    
    public void testShouldCalculateEndDateBasedOnLockIntervalAndVersionLockDate() {
        Mock version = mock(Version.class);
        Date start = new Date();
        version.stubs().method("getLockDate").withNoArguments().will(returnValue(start));
        
        DataAccessToken token = new DataAccessToken((Version)version.proxy(), null);
        token.setLockTimeInterval(3600000);
        Date end = new Date(start.getTime() + 3600000);
        
        assertEquals(end, token.lockEnd());
    }
    
    public void testNoLockEndIfLockStartIsNotSet() {
        Mock version = mock(Version.class);
        Date start = null;
        version.stubs().method("getLockDate").withNoArguments().will(returnValue(start));
        
        DataAccessToken token = new DataAccessToken((Version)version.proxy(), null);
        
        assertNull("Lock End should be null if Lock Start is unavailable", token.lockEnd());
    }
}
