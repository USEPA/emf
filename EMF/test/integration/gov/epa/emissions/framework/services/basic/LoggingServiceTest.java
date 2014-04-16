package gov.epa.emissions.framework.services.basic;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.ServiceTestCase;
import gov.epa.emissions.framework.services.data.DataCommonsService;
import gov.epa.emissions.framework.services.data.DataCommonsServiceImpl;
import gov.epa.emissions.framework.services.data.DataServiceImpl;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.util.Date;
import java.util.Random;

public class LoggingServiceTest extends ServiceTestCase {

    private LoggingServiceImpl logService;

    private DataCommonsService dcService;

    private UserService userService;

    private DataServiceImpl dataService;

    protected void doSetUp() throws Exception {
        HibernateSessionFactory sessionFactory = sessionFactory(configFile());

        dcService = new DataCommonsServiceImpl(sessionFactory);
        userService = new UserServiceImpl(sessionFactory);
        logService = new LoggingServiceImpl(sessionFactory);
        dataService = new DataServiceImpl(dbServerFactory, sessionFactory);

    }

    protected void doTearDown() throws Exception {// no op
    }

    public void testVerifyOneAccessLogLogged() throws Exception {
        Random rando = new Random();
        long id = Math.abs(rando.nextInt());

        User user = null;
        EmfDataset dataset = null;
        EmfDataset datasetFromDB = null;
        AccessLog alog = null;
        AccessLog returnAlog = null;

        try {
            user = user(id);
            userService.createUser(user);

            dataset = dataset(id, user);
            super.add(dataset);

            datasetFromDB = getDataset(dataset);

            alog = accessLog(user, datasetFromDB, "folderPath");
            alog.setDatasetname("");
            alog.setEnddate(new Date());
            alog.setTimereqrd(12345.0);

            logService.setAccessLog(alog);

            AccessLog[] allLogs = logService.getAccessLogs(datasetFromDB.getId());
            for (int i = 0; i < allLogs.length; i++) {
                returnAlog = allLogs[i];
                assertEquals(returnAlog, alog);
            }
        } finally {
            remove(alog);
            remove(dataset);
            remove(user);
        }
    }

    private User user(long id) {
        User user;
        user = new User("Giovanni Falcone", "UNC", "919-966-9572", "falcone@unc.edu", "falcone" + id, "falcone123",
                false, false);
        return user;
    }


    public void testShouldGiveLastExportedFileName() throws Exception {
        Random rando = new Random();
        long id = Math.abs(rando.nextInt());
        
        User user = null;
        EmfDataset dataset = null;
        EmfDataset datasetFromDB = null;
        AccessLog alog1 = null;
        AccessLog alog2 = null;
        try {
            user = user(id);
            userService.createUser(user);

            dataset = dataset(id, user);
            super.add(dataset);

            datasetFromDB = getDataset(dataset);

            alog1 = accessLog(user, datasetFromDB, "folderPath1");
            alog1.setDatasetname("");
            alog1.setEnddate(new Date());
            alog1.setTimereqrd(12345.0);
            
            alog2 = accessLog(user, datasetFromDB, "folderPath2");
            alog2.setDatasetname("");
            alog2.setEnddate(new Date());
            alog2.setTimereqrd(12345.0);

            logService.setAccessLog(alog1);
            logService.setAccessLog(alog2);

            String folderName = logService.getLastExportedFileName(datasetFromDB.getId());
            assertEquals("folderPath1", folderName);
        } finally {
            remove(alog1);
            remove(alog2);
            remove(dataset);
            remove(user);
        }
            
        
    }

    private EmfDataset getDataset(EmfDataset dataset) throws EmfException {
        EmfDataset datasetFromDB = null;

        EmfDataset[] allDatasets = dataService.getDatasets("", 1);

        for (int i = 0; i < allDatasets.length; i++) {
            datasetFromDB = allDatasets[i];

            if (datasetFromDB.getName().equals(dataset.getName()))
                break;
        }

        return datasetFromDB;
    }
    
    private AccessLog accessLog(User user, EmfDataset datasetFromDB, String folder) {
        AccessLog alog;
        alog = new AccessLog(user.getUsername(), datasetFromDB.getId(), new Date(), "0", "description", folder);
        return alog;
    }

    private EmfDataset dataset(long id, User user) throws EmfException {
        EmfDataset dataset;
        dataset = new EmfDataset();
        
        String newName = user.getUsername() + "_" + id;
        if ( newName != null) {
            newName = newName.trim();
        } else {
            throw new EmfException("Dataset name is null");
        }
        dataset.setName(newName);

        dataset.setAccessedDateTime(new Date());
        dataset.setCreatedDateTime(new Date());
        dataset.setCreator(user.getUsername());
        dataset.setDatasetType(getDatasetType("External File (External)"));
        dataset.setDescription("DESCRIPTION");
        dataset.setModifiedDateTime(new Date());
        dataset.setStartDateTime(new Date());
        dataset.setStatus("imported");
        dataset.setYear(42);
        dataset.setUnits("orl");
        dataset.setTemporalResolution("t1");
        dataset.setStopDateTime(new Date());
        return dataset;
    }


    private DatasetType getDatasetType(String string) throws EmfException {
        DatasetType aDST = null;

        DatasetType[] allDST = dcService.getDatasetTypes();
        for (int i = 0; i < allDST.length; i++) {
            aDST = allDST[i];

            if (aDST.getName().equals(string)) {
                break;
            }
        }
        return aDST;
    }

}
