package gov.epa.emissions.framework.services;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.DefaultEmfSession;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.transport.RemoteServiceLocator;
import gov.epa.emissions.framework.services.basic.UserService;
import gov.epa.emissions.framework.services.data.DataCommonsService;
import gov.epa.emissions.framework.services.exim.ExImService;

public class ExportImportServiceTransportTest extends ServiceTestCase {
    private static final String DEFAULT_URL = "http://localhost:8080/emf/services";// default

    private ExImService transport = null;

    private UserService userService;

    private EmfSession emfSession;

    private DataCommonsService dataCommonsService;

    protected void doSetUp() throws Exception {

        RemoteServiceLocator remoteServiceLocator = new RemoteServiceLocator(DEFAULT_URL);
        transport = remoteServiceLocator.eximService();

        userService = remoteServiceLocator.userService();
        User user = userService.getUser("emf");
        emfSession = new DefaultEmfSession(user, remoteServiceLocator);
        dataCommonsService = remoteServiceLocator.dataCommonsService();

    }

    public void testShouldImport100DatasetWithoutAProblem() throws EmfException {
        String folderPath = "D:/CEP/Commons/test/data/orl/nc";
        String[] fileNames = { "small-nonpoint.txt" };
        DatasetType datasetType = orlNonpointDatasetType();
        try {
            for (int i = 0; i < 100; i++) {
                transport.importDataset(emfSession.user(), folderPath, fileNames, datasetType, ("test" + i));
            }
        } catch (EmfException e) {
            e.printStackTrace();
        }
    }

    private DatasetType orlNonpointDatasetType() throws EmfException {
        DatasetType[] datasetTypes = dataCommonsService.getDatasetTypes();
        for (int i = 0; i < datasetTypes.length; i++) {
            String name = datasetTypes[i].getName();
            if (name.startsWith(DatasetType.orlNonpointInventory))
                return datasetTypes[i];
        }
        return null;
    }

    protected void doTearDown() throws Exception {// no op
    }

}
