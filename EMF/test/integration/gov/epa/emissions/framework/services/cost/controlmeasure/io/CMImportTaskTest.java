package gov.epa.emissions.framework.services.cost.controlmeasure.io;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.ServiceTestCase;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.basic.UserDAO;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.ControlMeasureDAO;
import gov.epa.emissions.framework.services.cost.controlmeasure.Scc;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;

import java.io.File;

public class CMImportTaskTest extends ServiceTestCase {

    private ControlMeasureDAO dao;

    public CMImportTaskTest() {
        this.dao = new ControlMeasureDAO();
    }

    protected void doSetUp() throws Exception {
        //
    }

    protected void doTearDown() throws Exception {
        //
    }

    public void testShouldImportControlMeasureFiles() throws Exception {
        try {
            File folder = new File("test/data/cost/controlMeasure");
            String[] fileNames = { "CMSummary_2020_ON_Controls.csv", "CMSCCs_2020_ON_Controls.csv", "CMEfficiencies_2020_ON_Controls_NC_SC_VA.csv" };
//            String[] fileNames = { "CMSummary.csv", "CMSCCs.csv", "CMEfficiencies.csv", "CMReferences.csv" };
            CMImportTask task = new CMImportTask(folder, fileNames, emfUser(), false, new int[] {}, sessionFactory, dbServerFactory());
            task.run();
            ControlMeasure[] measures = measures();
//            assertEquals(32, measures.length);
//            assertEquals(1135, noOfRecords(measures));
//            assertEquals(126, noOfScc());
            assertEquals(132, measures.length);
            assertEquals(40800, noOfRecords(measures));
            assertEquals(132, noOfScc());
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            dropAll(Scc.class);
            dropData("aggregrated_efficiencyrecords", dbServer().getEmfDatasource());
            dropAll(EfficiencyRecord.class);
            dropAll(ControlMeasure.class);
            dropAll(Status.class);
        }
    }
    
    public void testShouldImportControlMeasureFilesTwice() throws Exception {
        try {
            File folder = new File("test/data/cost/controlMeasure");
            String[] fileNames = { "CMSummary.csv", "CMSCCs.csv", "CMEfficiencies.csv", "CMReferences.csv" };
            CMImportTask task = new CMImportTask(folder, fileNames, emfUser(), false, new int[] {}, sessionFactory, dbServerFactory());
            task.run();
            ControlMeasure[] measures = measures();
            assertEquals(32, measures.length);
            assertEquals(1135, noOfRecords(measures));
            assertEquals(126, noOfScc());
            
            task.run();
            
            measures = measures();
            assertEquals(32, measures.length);
            assertEquals(1135, noOfRecords(measures));
            assertEquals(126, noOfScc());
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            dropAll(Scc.class);
            dropAll(EfficiencyRecord.class);
            dropAll(ControlMeasure.class);
            dropAll(Status.class);
        }
    }

    private ControlMeasure[] measures() {
        return (ControlMeasure[]) dao.all(session).toArray(new ControlMeasure[0]);
    }

    private User emfUser() {
        return new UserDAO().get("emf", session);
    }

    private int noOfScc() {
        HibernateFacade facade = new HibernateFacade();
        return facade.getAll(Scc.class, session).size();
    }

    private int noOfRecords(ControlMeasure[] measures) {
        int count = 0;
        for (int i = 0; i < measures.length; i++) {
            count += dao.getEfficiencyRecords(measures[i].getId(), session).size();
        }
        return count;
    }

}
