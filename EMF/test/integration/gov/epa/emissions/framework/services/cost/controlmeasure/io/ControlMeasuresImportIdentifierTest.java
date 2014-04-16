package gov.epa.emissions.framework.services.cost.controlmeasure.io;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.ServiceTestCase;
import gov.epa.emissions.framework.services.basic.UserDAO;

import java.io.File;

public class ControlMeasuresImportIdentifierTest extends ServiceTestCase{
    
    protected void doSetUp() throws Exception {
        // NOTE Auto-generated method stub
        
    }

    protected void doTearDown() throws Exception {
        // NOTE Auto-generated method stub
        
    }


    public void testShouldIdentifyAllTheImportersCorrectly() throws Exception {
        String folder = "test/data/cost/controlMeasure";
        String[] fileNames = { "CMSummary.csv", "CMSCCs.csv", "CMEfficiencies.csv", "CMReferences.csv" };
        File[] files = new File[fileNames.length];
        for (int i = 0; i < files.length; i++) {
            files[i] = new File(folder, fileNames[i]);
        }

        ControlMeasuresImportIdentifier identifier = new ControlMeasuresImportIdentifier(files,emfUser(), sessionFactory(), dbServer());
        CMImporters cmImporters = identifier.cmImporters();
        assertNotNull(cmImporters.summaryImporter());
        assertNotNull(cmImporters.efficiencyImporter());
        assertNotNull(cmImporters.sccImporter());
        assertNotNull(cmImporters.referenceImporter());

    }
    
    private User emfUser() {
        return new UserDAO().get("emf",session);
    }


}
