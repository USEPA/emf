package gov.epa.emissions.framework.services.cost;

import java.util.Date;

import gov.epa.emissions.framework.services.ServiceTestCase;
import gov.epa.emissions.framework.services.cost.controlmeasure.Scc;

import org.hibernate.HibernateException;
import org.hibernate.Session;

public class RetrieveSCCTest extends ServiceTestCase {

    protected void doSetUp() throws Exception {
        // no op
    }

    protected void doTearDown() throws Exception {
        // no op
    }

    // setup the reference schema before running this test
    public void testShouldGetCorrectSCCs() throws Exception {
        ControlMeasure cm = new ControlMeasure();
        cm.setEquipmentLife(Float.valueOf(12));
        cm.setName("cm test added" + Math.random());
        cm.setAbbreviation("12345678");
        cm.setLastModifiedBy("emf");
        cm.setLastModifiedTime(new Date());
        Scc scc0 = new Scc("0ABBBBBB", "");
        Scc scc1 = new Scc("10100101", "");
        Scc scc2 = new Scc("10100225", "");
        Scc scc3 = new Scc("10100226", "");
        Scc scc4 = new Scc("4BBBBBBB", "");

        // These scc numbers have to exist in the reference.scc table
        cm.setSccs(new Scc[] { scc0, scc1, scc2, scc3, scc4 });

        ControlMeasureDAO dao = null;
        Scc[] sccs = null;
        try {
            dao = new ControlMeasureDAO();
            addMeasure(cm, dao);
            RetrieveSCC retreiveSCC = new RetrieveSCC(cm.getId(), dbServer());
            sccs = retreiveSCC.sccs();
        } finally {
            dropAll(Scc.class);
            removeMeasure(cm, dao);
        }
        assertEquals(5, sccs.length);
        assertEquals("0ABBBBBB", sccs[0].getCode());
        assertEquals("10100101", sccs[1].getCode());
        assertEquals("10100225", sccs[2].getCode());
        assertEquals("10100226", sccs[3].getCode());
        assertEquals("4BBBBBBB", sccs[4].getCode());
        assertEquals("The SCC entry is not found in the reference.scc table", sccs[0].getDescription());
        assertEquals("External Combustion Boilers;Electric Generation;Anthracite Coal;Pulverized Coal", sccs[1]
                .getDescription());
        assertEquals(
                "External Combustion Boilers;Electric Generation;Bituminous/Subbituminous Coal;Traveling Grate (Overfeed) Stoker (Subbituminous Coal)",
                sccs[2].getDescription());
        assertEquals(
                "External Combustion Boilers;Electric Generation;Bituminous/Subbituminous Coal;Pulverized Coal: Dry Bottom Tangential (Subbituminous Coal)",
                sccs[3].getDescription());
        assertEquals("The SCC entry is not found in the reference.scc table", sccs[4].getDescription());

    }

    private void addMeasure(ControlMeasure measure, ControlMeasureDAO dao) throws HibernateException, Exception {
        Session session = sessionFactory().getSession();
        try {
            dao.add(measure, measure.getSccs(), session);
        } finally {
            session.close();
        }
    }

    private void removeMeasure(ControlMeasure measure, ControlMeasureDAO dao) throws HibernateException, Exception {
        Session session = sessionFactory().getSession();
        try {
            dao.remove(measure.getId(), session);
        } finally {
            session.close();
        }
    }

}
