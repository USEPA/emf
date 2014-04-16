package gov.epa.emissions.framework.services.cost.controlmeasure.io;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.ServiceTestCase;
import gov.epa.emissions.framework.services.basic.UserDAO;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.ControlMeasureDAO;
import gov.epa.emissions.framework.services.cost.controlmeasure.Scc;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;

import java.io.File;

public class ControlMeasuresImporterTest extends ServiceTestCase {

    protected void doSetUp() throws Exception {
        // NOTE Auto-generated method stub

    }

    protected void doTearDown() throws Exception {
        // NOTE Auto-generated method stub

    }

    public void testShouldImportControlMeasureFiles() throws EmfException, Exception {
        File folder = new File("test/data/cost/controlMeasure");
        String[] fileNames = { "CMSummary.csv", "CMSCCs.csv", "CMEfficiencies.csv", "CMReferences.csv" };
        ControlMeasuresImporter importer = new ControlMeasuresImporter(folder, fileNames, emfUser(), false, new int[] {}, sessionFactory(), dbServerFactory());
        importer.run();

        ControlMeasure[] measures = importer.controlMeasures();
        assertEquals(32, measures.length);
        assertEquals(1132, noOfRecords(measures));
        assertEquals(126, noOfScc(measures));
        
        dropAll(EfficiencyRecord.class);
        dropAll(Scc.class);
        dropAll(ControlMeasure.class);
    }

    public void testShouldImportControlMeasureFiles2() throws EmfException, Exception {
        File folder = new File("D:/CEP");
        String[] fileNames = { "Summary.csv", "SCCs.csv", "Efficiencies.csv"};
        ControlMeasuresImporter importer = new ControlMeasuresImporter(folder, fileNames, emfUser(), false, new int[] {}, sessionFactory(), dbServerFactory());
        importer.run();

        ControlMeasure[] measures = importer.controlMeasures();
        assertEquals(32, measures.length);
        assertEquals(1132, noOfRecords(measures));
        assertEquals(126, noOfScc(measures));
        
        dropAll(EfficiencyRecord.class);
        dropAll(Scc.class);
        dropAll(ControlMeasure.class);
    }

    private User emfUser() {
        return new UserDAO().get("emf",session);
    }

    private int noOfScc(ControlMeasure[] measures) {
        int count = 0;
        for (int i = 0; i < measures.length; i++) {
            count += measures[i].getSccs().length;
        }
        return count;
    }

    private int noOfRecords(ControlMeasure[] measures) {
        int count = 0;
        ControlMeasureDAO dao = new ControlMeasureDAO();
        for (int i = 0; i < measures.length; i++) {
            count += dao.getEfficiencyRecords(measures[i].getId(), session).size();
        }
        return count;
    }

}
