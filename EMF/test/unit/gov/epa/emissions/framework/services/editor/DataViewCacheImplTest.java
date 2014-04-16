package gov.epa.emissions.framework.services.editor;

import gov.epa.emissions.commons.db.version.ScrollableVersionedRecords;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.VersionedRecordsFactory;
import gov.epa.emissions.framework.services.EmfProperties;
import gov.epa.emissions.framework.services.basic.EmfProperty;

import org.hibernate.Session;
import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;
import org.jmock.core.Constraint;

public class DataViewCacheImplTest extends MockObjectTestCase {

    public void testShouldReinitializeRecordsReaderOnApplyConstraints() throws Exception {
        Mock reader = mock(VersionedRecordsFactory.class);
        
        Session session = (Session) mock(Session.class).proxy();
        
        int batchSize = 10000;
        Mock properties = properties(session,batchSize);
        DataViewCacheImpl cache = new DataViewCacheImpl((VersionedRecordsFactory) reader.proxy(),(EmfProperties) properties.proxy());

        DataAccessToken token = new DataAccessToken();
        token.setVersion(new Version());
        token.setTable("table");

        String columnFilter = "col";
        String rowFilter = "row";
        String sortOrder = "sort";
        
        Constraint[] constraints = new Constraint[] { eq(token.getVersion()), eq(token.getTable()), eq(batchSize), eq(columnFilter),
                eq(rowFilter), eq(sortOrder), same(session) };
        
        Mock records = mock(ScrollableVersionedRecords.class);
        reader.expects(once()).method("optimizedFetch").with(constraints).will(returnValue(records.proxy()));

        cache.init(token, 10, columnFilter, rowFilter, sortOrder, session);
    }
    
    private Mock properties(Session session, int batchSize) {
        Mock properties = mock(EmfProperties.class);

        EmfProperty property = new EmfProperty();
        property.setName("batch-size");
        property.setValue(""+batchSize);
        
        properties.expects(once()).method("getProperty").with(eq("batch-size"), eq(session)).will(returnValue(property));
        
        return properties;
    }
    
    
}
