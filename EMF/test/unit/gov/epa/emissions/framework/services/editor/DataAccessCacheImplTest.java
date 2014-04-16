package gov.epa.emissions.framework.services.editor;


import org.hibernate.Session;
import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;
import org.jmock.core.Constraint;

public class DataAccessCacheImplTest extends MockObjectTestCase {

    public void testShouldApplyConstraintsToViewCacheOnApplyConstraints() throws Exception {
        Mock view = mock(DataViewCache.class);
        Mock update = mock(DataUpdatesCache.class);

        DataAccessCache cache = new DataAccessCacheImpl((DataViewCache) view.proxy(), (DataUpdatesCache) update.proxy());

        DataAccessToken token = new DataAccessToken();
        String columnFilter = "col";
        String rowFilter = "row";
        String sortOrder = "sort";
        Session session = (Session) mock(Session.class).proxy();
        
        Constraint[] constraints = new Constraint[] { same(token), eq(columnFilter), eq(rowFilter), eq(sortOrder),
                same(session) };
        view.expects(once()).method("applyConstraints").with(constraints);

        cache.applyConstraints(token, columnFilter, rowFilter, sortOrder, session);
    }
}
