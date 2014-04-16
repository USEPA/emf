package gov.epa.emissions.framework.services;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.ExternalSource;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.data.KeyVal;
import gov.epa.emissions.commons.data.Keyword;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.DbUpdate;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.basic.LoggingServiceImpl;
import gov.epa.emissions.framework.services.basic.StatusDAO;
import gov.epa.emissions.framework.services.basic.UserService;
import gov.epa.emissions.framework.services.basic.UserServiceImpl;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.services.casemanagement.outputs.CaseOutput;
import gov.epa.emissions.framework.services.data.DataCommonsServiceImpl;
import gov.epa.emissions.framework.services.data.DataService;
import gov.epa.emissions.framework.services.data.DataServiceImpl;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.exim.ExImService;
import gov.epa.emissions.framework.services.exim.ExImServiceImpl;
import gov.epa.emissions.framework.services.exim.ManagedImportService;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ExImServiceImplTestCase extends ExImServiceTestCase {

    private DbServer dbServer;

    protected void doSetUp() throws Exception {
        dbServer = dbServerFactory.getDbServer();
        ExImService eximService = new ExImServiceImpl(dbServerFactory, sessionFactory);
        UserService userService = new UserServiceImpl(sessionFactory);
        DataCommonsServiceImpl commonsService = new DataCommonsServiceImpl(sessionFactory);

        super.setUpService(eximService, userService, commonsService);
    }

    protected void doTearDown() throws Exception {
        dropAll(InternalSource.class);
        dropAll(ExternalSource.class);
        dropAll(Version.class);
        dropAll(EmfDataset.class);
        dropAll(CaseOutput.class);
        dropAll(CaseJob.class);
        dropAll(Case.class);
    }

    private void dropTables(InternalSource[] sources) throws Exception, SQLException {
        Datasource datasource = dbServer.getEmissionsDatasource();
        DbUpdate dbUpdate = dbSetup.dbUpdate(datasource);

        for (InternalSource source : sources) {
            dbUpdate.dropTable(datasource.getName(), source.getTable());
            System.out.println("Table : " + source.getTable() + " dropped.");
        }
    }

    public void testImportOrlNonPoint() throws Exception {
        DataService dataService = new DataServiceImpl(dbServerFactory, sessionFactory);
        User user = userService.getUser("emf");

        File repository = new File(System.getProperty("user.dir"), "test/data/orl/nc/");
        String filename = "small-nonpoint1.txt";

        eximService.importDatasets(user, repository.getAbsolutePath(), new String[] { filename }, dataset
                .getDatasetType());
        Thread.sleep(20000); // so that import thread has enough time to run

        EmfDataset[] imported = dataService.getDatasets("",1);
        EmfDataset localdataset = imported[0];
        assertEquals(filename, imported[0].getName());
        assertEquals(1, imported.length);

        dropTables(localdataset.getInternalSources());
    }

    public void testImportAnExternalDataset() throws Exception {
        DataService dataService = new DataServiceImpl(dbServerFactory, sessionFactory);
        User user = userService.getUser("emf");
        
        File repository = new File(System.getProperty("user.dir"), "test/data/orl/nc/");
        String[] files = new String[] { "file1", "file2", "file3", "file4" };
        eximService.importDataset(user, repository.getAbsolutePath(), files, getDatasetType("External File (External)"), files[0]);
        Thread.sleep(60000); // so that import thread has enough time to run
        
        EmfDataset[] imported = dataService.getDatasets("",1);
        EmfDataset localdataset = imported[0];
        assertEquals(files[0], imported[0].getName());
        assertEquals(1, imported.length);
        
        dropTables(localdataset.getInternalSources());
    }

    public void testImportSingleOrlNonPoint() throws Exception {
        DataService dataService = new DataServiceImpl(dbServerFactory, sessionFactory);
        User user = userService.getUser("emf");

        File repository = new File(System.getProperty("user.dir"), "test/data/orl/nc/");
        String filename = "small-nonpoint1.txt";

        eximService.importDataset(user, repository.getAbsolutePath(), new String[] { filename }, dataset
                .getDatasetType(), "small-nonpoint1.txt");
        Thread.sleep(2000); // so that import thread has enough time to run

        EmfDataset[] imported = dataService.getDatasets("",1);
        EmfDataset localdataset = imported[0];
        assertEquals(filename, imported[0].getName());
        assertEquals(1, imported.length);

        dropTables(localdataset.getInternalSources());
    }

    //NOTE: Worked on Linux platform but hung on Widows platform for the next two cases.
    
    public void testImportMultipleOrlNonPoint() throws Exception {
        DataService dataService = new DataServiceImpl(dbServerFactory, sessionFactory);
        User user = userService.getUser("emf");

        File repository = new File(System.getProperty("user.dir"), "test/data/orl/nc/");
        String filename1 = "small-nonpoint1.txt";
        String filename2 = "small-nonpoint2.txt";
        String filename3 = "small-nonpoint3.txt";
        String filename4 = "small-nonpoint4.txt";

        String[] files = new String[] { filename1, filename2, filename3, filename4 };
        EmfDataset[] imported = null;

        try {
            eximService.importDatasets(user, repository.getAbsolutePath(), files, dataset.getDatasetType());
            Thread.sleep(240000); // so that import thread has enough time to run

            imported = dataService.getDatasets("",1);
            List<String> importedNames = new ArrayList<String>();

            for (int i = 0; i < imported.length; i++)
                importedNames.add(imported[i].getName());

            assertTrue(importedNames.contains(filename1));
            assertTrue(importedNames.contains(filename2));
            assertTrue(importedNames.contains(filename3));
            assertTrue(importedNames.contains(filename4));
            assertEquals(4, imported.length);
        } catch (RuntimeException e) {
            e.printStackTrace();
        } finally {
            InternalSource[] sources = new InternalSource[imported.length];

            for (int i = 0; i < sources.length; i++) {
                sources[i] = imported[i].getInternalSources()[0];
            }

            dropTables(sources);
        }
    }

    public void testImportMultipleLineBasedDatasets() throws Exception {
        DataService dataService = new DataServiceImpl(dbServerFactory, sessionFactory);
        User user = userService.getUser("emf");
        
        File repository = new File(System.getProperty("user.dir"), "test/data/orl/nc/");
        String filename1 = "small-nonpoint1.txt";
        String filename2 = "small-nonpoint2.txt";
        String filename3 = "small-nonpoint3.txt";
        String filename4 = "small-nonpoint4.txt";
        
        String[] files = new String[] { filename1, filename2, filename3, filename4 };
        EmfDataset[] imported = null;
        
        try {
            eximService.importDatasets(user, repository.getAbsolutePath(), files, getDatasetType("Text file (Line-based)"));
            Thread.sleep(240000); // so that import thread has enough time to run
            
            imported = dataService.getDatasets("", 1);
            List<String> importedNames = new ArrayList<String>();

            for (int i = 0; i < imported.length; i++)
                importedNames.add(imported[i].getName());

            assertTrue(importedNames.contains(filename1));
            assertTrue(importedNames.contains(filename2));
            assertTrue(importedNames.contains(filename3));
            assertTrue(importedNames.contains(filename4));
            assertEquals(4, imported.length);
        } catch (RuntimeException e) {
            e.printStackTrace();
        } finally {
            InternalSource[] sources = new InternalSource[imported.length];

            for (int i = 0; i < sources.length; i++) {
                sources[i] = imported[i].getInternalSources()[0];
            }

            dropTables(sources);
        }
    }

    public void testImportMultipleLineBasedDatasetsWithNameCorrections() throws Exception {
        DataService dataService = new DataServiceImpl(dbServerFactory, sessionFactory);
        User user = userService.getUser("emf");
        
        File repository = new File(System.getProperty("user.dir"), "test/data/orl/nc/");
        String filename1 = "small-nonpoint1.txt";
        String filename2 = "small-nonpoint2.txt";
        String filename3 = "small-nonpoint3.txt";
        String filename4 = "small-nonpoint4.txt";
        
        String[] files = new String[] { filename1, filename2, filename3, filename4 };
        EmfDataset[] imported = null;
        
        KeyVal prefixVal = new KeyVal((Keyword)load(Keyword.class, "EXPORT_PREFIX"), "small-");
        KeyVal suffixVal = new KeyVal((Keyword)load(Keyword.class, "EXPORT_SUFFIX"), ".txt");
        DatasetType type = getDatasetType("Text file (Line-based)");
        type.addKeyVal(prefixVal);
        type.addKeyVal(suffixVal);
        
        try {
            eximService.importDatasets(user, repository.getAbsolutePath(), files, type);
            Thread.sleep(240000); // so that import thread has enough time to run
            
            imported = dataService.getDatasets("",1);
            List<String> importedNames = new ArrayList<String>();
            
            for (int i = 0; i < imported.length; i++) {
                importedNames.add(imported[i].getName());
                System.out.println("dataset name" + i + ": " + imported[i].getName());
            }
            
            assertFalse(importedNames.contains(filename1));
            assertTrue(importedNames.contains("nonpoint1"));
            assertFalse(importedNames.contains(filename2));
            assertTrue(importedNames.contains("nonpoint2"));
            assertFalse(importedNames.contains(filename3));
            assertTrue(importedNames.contains("nonpoint3"));
            assertFalse(importedNames.contains(filename4));
            assertTrue(importedNames.contains("nonpoint4"));
            assertEquals(4, imported.length);
        } catch (RuntimeException e) {
            e.printStackTrace();
        } finally {
            InternalSource[] sources = new InternalSource[imported.length];
            
            for (int i = 0; i < sources.length; i++) {
                sources[i] = imported[i].getInternalSources()[0];
            }
            
            dropTables(sources);
        }
    }

    public void testImportCaseOutput() throws Exception {
        ManagedImportService importService = new ManagedImportService(dbServerFactory, sessionFactory);
        
        DataService dataService = new DataServiceImpl(dbServerFactory, sessionFactory);
        User user = userService.getUser("emf");
        
        File repository = new File(System.getProperty("user.dir"), "test/data/orl/nc/");
        String filename = "small-nonpoint1.txt";
        
        Case caseObj = newCase("Test case output case");
        CaseJob job = newCaseJob(caseObj, "Test_Job_Key", user);
        
        CaseOutput output = new CaseOutput("Case Ouput Test");
        
        output.setDatasetType("Text file (Line-based)");
        output.setCaseId(caseObj.getId());
        output.setJobId(job.getId());
        output.setPath(repository.getAbsolutePath());
        output.setDatasetFile(filename);
        output.setDatasetName(filename);
        output.setMessage("Test Registering Case Output");
        
        EmfDataset[] imported = null;
        
        try {
            importService.importDatasetForCaseOutput(user, output, services());
            Thread.sleep(10000); // so that import thread has enough time to run
            
            imported = dataService.getDatasets("",1);
            
            assertEquals(filename, imported[0].getName());
            assertEquals(1, imported.length);
        } catch (RuntimeException e) {
            e.printStackTrace();
        } finally {
            dropTables(imported[0].getInternalSources());
        }
    }

    public void testImportCaseOutputWithNameCorrections() throws Exception {
        ManagedImportService importService = new ManagedImportService(dbServerFactory, sessionFactory);
        
        DataService dataService = new DataServiceImpl(dbServerFactory, sessionFactory);
        User user = userService.getUser("emf");
        
        File repository = new File(System.getProperty("user.dir"), "test/data/orl/nc/");
        String filename = "small-nonpoint1.txt";
        
        Case caseObj = newCase("Test case output case");
        CaseJob job = newCaseJob(caseObj, "Test_Job_Key", user);
        
        CaseOutput output = new CaseOutput("Case Ouput Test");
        
        output.setDatasetType("Text file (Line-based)");
        output.setCaseId(caseObj.getId());
        output.setJobId(job.getId());
        output.setPath(repository.getAbsolutePath());
        output.setDatasetFile(filename);
        output.setDatasetName(filename);
        output.setMessage("Test Registering Case Output");
        
        EmfDataset[] imported = null;
        
        try {
            //Dataset type "Text file (Line-based)" has id# 12
            //KeyWord "EXPORT_PREFIX" has id# 5
            //KeyWord "EXPORT_SUFFIX" has id# 6
            dbServer.getEmfDatasource().query().execute("insert into emf.dataset_types_keywords values (DEFAULT,12,0,5,'small-')");
            dbServer.getEmfDatasource().query().execute("insert into emf.dataset_types_keywords values (DEFAULT,12,1,6,'.txt')");
            importService.importDatasetForCaseOutput(user, output, services());
            Thread.sleep(10000); // so that import thread has enough time to run
            
            imported = dataService.getDatasets("", 1);
            
            assertEquals("nonpoint1", imported[0].getName());
            assertEquals(1, imported.length);
        } catch (RuntimeException e) {
            e.printStackTrace();
        } finally {
            dbServer.getEmfDatasource().query().execute("delete from emf.dataset_types_keywords");
            dropTables(imported[0].getInternalSources());
        }
    }
    
    public void testImportExternalTypeCaseOutput() throws Exception {
        ManagedImportService importService = new ManagedImportService(dbServerFactory, sessionFactory);
        
        DataService dataService = new DataServiceImpl(dbServerFactory, sessionFactory);
        User user = userService.getUser("emf");
        
        File repository = new File(System.getProperty("user.dir"), "test/data/orl/nc/");
        
        Case caseObj = newCase("Test case output case");
        CaseJob job = newCaseJob(caseObj, "Test_Job_Key", user);
        
        CaseOutput output = new CaseOutput("Case Ouput Test");
        
        output.setDatasetType("External File (External)");
        output.setCaseId(caseObj.getId());
        output.setJobId(job.getId());
        output.setPath(repository.getAbsolutePath());
        output.setPattern("*");
        output.setMessage("Test Registering Case Output");
        
        EmfDataset[] imported = null;
        
        try {
            importService.importDatasetForCaseOutput(user, output, services());
            Thread.sleep(2000); // so that import thread has enough time to run
            
            imported = dataService.getDatasets("",1);
            
            assertEquals(1, imported.length);
        } catch (RuntimeException e) {
            e.printStackTrace();
        } 
    }
 
    public void testImportMultipleCaseOutputs() throws Exception {
        ManagedImportService importService = new ManagedImportService(dbServerFactory, sessionFactory);
        
        DataService dataService = new DataServiceImpl(dbServerFactory, sessionFactory);
        User user = userService.getUser("emf");
        
        File repository = new File(System.getProperty("user.dir"), "test/data/orl/nc/");
        
        Case caseObj = newCase("Test case output case");
        CaseJob job = newCaseJob(caseObj, "Test_Job_Key", user);
        
        CaseOutput output = new CaseOutput("Case Ouput Test");
        
        output.setDatasetType("Text file (Line-based)");
        output.setCaseId(caseObj.getId());
        output.setJobId(job.getId());
        output.setPath(repository.getAbsolutePath());
        output.setPattern("small-non*.txt");
        output.setMessage("Test Registering Case Output");
        
        EmfDataset[] imported = null;
        
        try {
            importService.importDatasetForCaseOutput(user, output, services());
            Thread.sleep(180000); // so that import thread has enough time to run
            
            imported = dataService.getDatasets("", 1);
            assertEquals(4, imported.length);
        } catch (RuntimeException e) {
            e.printStackTrace();
        } finally {
            InternalSource[] sources = new InternalSource[imported.length];
            
            for (int i = 0; i < sources.length; i++) {
                sources[i] = imported[i].getInternalSources()[0];
            }
            
            dropTables(sources);
        }
    }

    public void testImportCaseOutputShouldFail() {
        ManagedImportService importService = new ManagedImportService(dbServerFactory, sessionFactory);
        
        File repository = new File(System.getProperty("user.dir"), "test/data/orl/nc/");
        String filename = "small-nonpoint1.txt";
        
        CaseOutput output = new CaseOutput("Case Ouput Test");
        output.setDatasetType("xxx"); //no such dataset type 
        output.setCaseId(0);
        output.setJobId(0);
        output.setPath(repository.getAbsolutePath());
        output.setDatasetFile(filename);
        output.setDatasetName(filename);
        output.setMessage("Test Registering Case Output");
        
        try {
            User user = userService.getUser("emf");
            importService.importDatasetForCaseOutput(user, output, services());
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Dataset type 'xxx' does not exist"));
        } 
    }

    public void testExportWithOverwrite() throws Exception {
        DataService dataService = new DataServiceImpl(dbServerFactory, sessionFactory);
        User user = userService.getUser("emf");

        dataset.setDescription("description");
        dataset.setStatus("imported");

        // import
        File repository = new File(System.getProperty("user.dir"), "test/data/orl/nc/");
        String filename = "small-nonpoint1.txt";
        eximService.importDataset(user, repository.getAbsolutePath(), new String[] { filename }, dataset
                .getDatasetType(), filename);

        Thread.sleep(2000); // so that import thread has enough time to run

        EmfDataset[] imported = dataService.getDatasets("", 1);
        assertEquals(filename, imported[0].getName());

        Version version = new Version();
        version.setDatasetId(imported[0].getId());

        // export
        File outputFile = new File(System.getProperty("java.io.tmpdir"));
        outputFile.deleteOnExit();
        if (!outputFile.exists())
            outputFile.mkdir();

        eximService.exportDatasets(user, new EmfDataset[] { imported[0] }, new Version[] { version },
                outputFile.getAbsolutePath(), null, true, "", null, null, null, "", "Exporting NonPoint file");

        Thread.sleep(2000);// wait, until the export is complete

        dropTables(imported[0].getInternalSources());
    }

    public void testExport() throws Exception {
        DataService dataService = new DataServiceImpl(dbServerFactory, sessionFactory);
        User user = userService.getUser("emf");

        dataset.setDescription("description");
        dataset.setStatus("imported");

        // import
        File repository = new File(System.getProperty("user.dir"), "test/data/orl/nc/");
        String filename = "small-nonpoint1.txt";
        eximService.importDatasets(user, repository.getAbsolutePath(), new String[] { filename }, dataset
                .getDatasetType());

        Thread.sleep(2000); // so that import thread has enough time to run

        EmfDataset[] imported = dataService.getDatasets("", user.getId());
        assertEquals(filename, imported[0].getName());

        Version version = new Version();
        version.setDatasetId(imported[0].getId());

        // export
        File outputFile = new File(System.getProperty("java.io.tmpdir"));
        outputFile.deleteOnExit();
        if (!outputFile.exists())
            outputFile.mkdir();

        eximService.exportDatasets(user, new EmfDataset[] { imported[0] }, new Version[] { version }, 
                outputFile.getAbsolutePath(), null, false, "", null, null, null,"", "Exporting NonPoint file");

        // FIXME: verify the exported file exists
        Thread.sleep(2000);// wait, until the export is complete

        dropTables(imported[0].getInternalSources());
    }
    
    private Case newCase(String name) {
        session.clear();
        Case element = new Case(name);
        add(element);
        return (Case) load(Case.class, element.getName());
    }
    
    private CaseJob newCaseJob(Case caseObj, String jobkey, User user) {
        CaseJob element = new CaseJob("test" + Math.random());
        element.setCaseId(caseObj.getId());
        element.setJobkey(jobkey);
        element.setUser(user);
        add(element);

        return loadNewCaseJob(element, caseObj);
    }
    
    private CaseJob loadNewCaseJob(CaseJob job, Case caseObj) {
        /**
         * Takes a new job that and adds case ID and adds to db
         */
        // adds the element to the db and then reloads it from the db
        // ensures that it has an id
        job.setCaseId(caseObj.getId());
        add(job);
        return (CaseJob) load(CaseJob.class, job.getName());
    }
    
    private Services services() {
        Services services = new Services();
        services.setLoggingService(new LoggingServiceImpl(sessionFactory));
        services.setStatusService(new StatusDAO(sessionFactory));
        services.setDataService(new DataServiceImpl(dbServerFactory, sessionFactory));

        return services;
    }

}
