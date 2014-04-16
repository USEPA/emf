package gov.epa.emissions.framework.client.meta.revisions;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.services.editor.Revision;

import java.util.Date;

import junit.framework.TestCase;

public class RevisionsRowSourceTest extends TestCase {

    public void testShouldFillValuesInAppropriatePositions() {
        Revision revision = new Revision();
        revision.setVersion(123);
        revision.setCreator(new User());
        revision.setDate(new Date());
        revision.setWhat("what");
        revision.setWhy("why");
        revision.setReferences("ref");

        RevisionsRowSource source = new RevisionsRowSource(revision);

        Object[] values = source.values();
        assertEquals(6, values.length);
        assertEquals(revision.getWhat(), values[0]);
        assertEquals(revision.getWhy(), values[1]);
        assertEquals(revision.getReferences(), values[2]);
        assertEquals(revision.getVersion(), ((Long) values[3]).longValue());
        assertEquals(revision.getCreator().getName(), values[4]);

        assertEquals(CustomDateFormat.format_YYYY_MM_DD_HH_MM(revision.getDate()), values[5]);
    }

    public void testShouldTrackOriginalSource() {
        Revision revision = new Revision();
        RevisionsRowSource rowSource = new RevisionsRowSource(revision);

        assertEquals(revision, rowSource.source());
    }
}
