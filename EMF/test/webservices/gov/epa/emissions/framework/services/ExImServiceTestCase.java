package gov.epa.emissions.framework.services;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.services.basic.UserService;
import gov.epa.emissions.framework.services.data.DataCommonsService;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.exim.ExImService;

import java.util.Random;

public abstract class ExImServiceTestCase extends ServiceTestCase {

    protected ExImService eximService;

    protected UserService userService;

    protected EmfDataset dataset;
    
    protected DataCommonsService commonsService;

    protected void setUpService(ExImService eximService, UserService userService, DataCommonsService commonsService)
            throws Exception {
        this.eximService = eximService;
        this.userService = userService;
        this.commonsService = commonsService;
        dataset = new EmfDataset();
        Random random = new Random();
        dataset.setName("ORL NonPoint - ExImServicesTest" + random.nextInt());
        dataset.setCreator("creator");

        DatasetType datasetType = orlNonPointType(commonsService);
        dataset.setDatasetType(datasetType);
    }

    private DatasetType orlNonPointType(DataCommonsService service) throws Exception {
        DatasetType[] types = service.getDatasetTypes();
        for (int i = 0; i < types.length; i++) {
            if (types[i].getName().startsWith("ORL Nonpoint Inventory"))
                return types[i];
        }

        return null;
    }

    protected DatasetType getDatasetType(String type) throws Exception {
        DatasetType[] types = commonsService.getDatasetTypes();
        for (int i = 0; i < types.length; i++) {
            if (types[i].getName().startsWith(type))
                return types[i];
        }
        
        return null;
    }

    protected void doTearDown() throws Exception {
        //ExImDbUpdate dbUpdate = new ExImDbUpdate();
        dropAll(InternalSource.class);
        dropAll(Version.class);
        dropAll(EmfDataset.class);
        //dbUpdate.deleteAllDatasets();
    }

}
