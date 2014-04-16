package gov.epa.emissions.framework.services.data;

import gov.epa.emissions.commons.data.Country;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.TableCreator;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.TableFormat;
import gov.epa.emissions.commons.io.csv.CSVImporter;
import gov.epa.emissions.commons.io.importer.Importer;
import gov.epa.emissions.commons.io.importer.VersionedDataFormatFactory;
import gov.epa.emissions.commons.io.importer.VersionedImporter;
import gov.epa.emissions.commons.io.reference.CSVFileFormat;
import gov.epa.emissions.commons.io.temporal.TemporalProfileImporter;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.ServiceTestCase;
import gov.epa.emissions.framework.services.basic.AccessLog;
import gov.epa.emissions.framework.services.basic.UserDAO;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseDAO;
import gov.epa.emissions.framework.services.casemanagement.CaseInput;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.services.casemanagement.outputs.CaseOutput;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.ControlStrategyDAO;
import gov.epa.emissions.framework.services.cost.ControlStrategyInputDataset;
import gov.epa.emissions.framework.services.editor.Revision;

import java.io.File;
import java.util.Date;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

public class DatasetDaoTest extends ServiceTestCase {

    private DatasetDAO dao;

    private DataCommonsDAO dcDao;

    private UserDAO userDAO;
    
    private DataCommonsDAO dataDAO;
    
    private ControlStrategyDAO strategyDao;
    
    private CaseDAO caseDao;
    
    private DatasetType[] types;

    protected void doSetUp() throws Exception {
        deleteAllDatasets();
        dao = new DatasetDAO();
        dcDao = new DataCommonsDAO();
        userDAO = new UserDAO();
        dataDAO = new DataCommonsDAO();
        strategyDao = new ControlStrategyDAO();
        caseDao = new CaseDAO();
        types = (DatasetType[])dataDAO.getDatasetTypes(session).toArray(new DatasetType[0]);

    }

    protected void doTearDown() throws Exception {// no op
    }

    public void testShouldGetAll() {
        List all = dao.all(session);
        assertEquals(0, all.size());
    }

    public void testShouldAddDatasetToDatabaseOnAdd() throws Exception {
        EmfDataset dataset = newDataset("dataset-dao-test");
        try {
            dao.add(dataset, session);
            EmfDataset result = (EmfDataset) load(EmfDataset.class, dataset.getName());

            assertEquals(dataset.getId(), result.getId());
            assertEquals(dataset.getName(), result.getName());
        } finally {
            remove(dataset);
        }
    }

    public void testShouldDeleteDatasetRefsFromVersionTable() throws Exception {
        try {
            newVersion(1, 0, true);
            newVersion(2, 0, true);
            newVersion(3, 0, true);
            newVersion(4, 0, true);
            
            int deletedItems = dao.deleteFromObjectTable(new int[]{1,2,3,4}, Version.class, "datasetId", session);
            
            assertEquals(deletedItems, 4);
            assertEquals(0, countRecords("versions"));
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void testShouldDeleteDatasetFromAllReferencedTables() throws Exception {
        DbServer dbServer = dbServerFactory.getDbServer();
        
        try {
            User owner = userDAO.get("emf", session);
            
            EmfDataset dataset = newDataset("dataset-dao-test");
            dataset.setCreator("emf");
            add(dataset);
            EmfDataset toBeDelete = (EmfDataset) load(EmfDataset.class, dataset.getName());
            
            Version version = new Version();
            version.setVersion(0);
            
            VersionedDataFormatFactory dataformatFactory = new VersionedDataFormatFactory(version, dataset);
            File folder = new File("test/data/ref");
            Importer importer = new CSVImporter(folder, new String[] { "pollutants.txt" }, dataset, dbServer,
                    dbServer.getSqlDataTypes(), dataformatFactory);
            VersionedImporter importerv = new VersionedImporter(importer, dataset, dbServer, new Date());
            importerv.run();
            
            AccessLog log = new AccessLog(owner.getUsername(), toBeDelete.getId(), new Date(), "initial version", "test", "not real");
            add(log);
            
            Note note = new Note(owner, toBeDelete.getId(), new Date(), "testing", "test note", 
                    (NoteType)dataDAO.getNoteTypes(session).get(0), "no reference", 0);
            add(note);
            
            Revision revision = new Revision(owner, toBeDelete.getId(), new Date(), 
                    0, "what what?", "test", "no reference");
            add(revision);
            
            QAStep step = new QAStep();
            step.setName("test dataset deletion - qa step");
            step.setDatasetId(toBeDelete.getId());
            add(step);
            QAStep loadedStep = (QAStep) load(QAStep.class, step.getName());
            
            Datasource datasource = dbServer.getEmissionsDatasource();
            TableCreator emissionTableTool = new TableCreator(datasource);

            CSVFileFormat fileFormat = new CSVFileFormat(dbServer.getSqlDataTypes(), new String[]{"col1", "col2", "col3"});
            TableFormat tableFormat = dataformatFactory.tableFormat(fileFormat, dbServer.getSqlDataTypes());
            QAStepResult stepResult = new QAStepResult(loadedStep);
            emissionTableTool.create("test_step_result_table", tableFormat, toBeDelete.getId());
            stepResult.setTable("test_step_result_table");
            add(stepResult);
            
            Case caseObj = load(newCase());
            CaseJob job = load(newCaseJob(caseObj));
            CaseOutput output = new CaseOutput("Test Case Output");
            output.setCaseId(caseObj.getId());
            output.setJobId(job.getId());
            output.setDatasetId(toBeDelete.getId());
            add(output);
            
            dao.deleteDatasets(new EmfDataset[]{dataset}, dbServer, session);
            
            assertEquals(0, countRecords("versions"));
  
            CaseOutput output2 = new CaseOutput("Test Case Output");
            output2.setCaseId(caseObj.getId());
            output2.setJobId(job.getId());
            output2.setDatasetId(0);
            session.flush();
            session.clear();
            CaseOutput loadedOutput = (CaseOutput)caseDao.loadCaseOutput(output2, session);
  
            assertEquals("Test Case Output", loadedOutput.getName());
            assertEquals("Associated dataset deleted", loadedOutput.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error: " + e.getMessage());
        } finally {
            if (dbServer != null && dbServer.isConnected())
                dbServer.disconnect();
        }
    }
    
    //NOTE: for this test case, one has to manually remove the remaining items from db tables, namely,
    //  emf.internal_sources, emf.datasets, emf.versions and drop table emissions.diurnal_weekend,
    //  emissions.diurnal_weekday, emissions.weekly, and emissions.monthly.
    public void testShouldDeleteDatasetDataRecordsFromCOSTYandPTPROTables() throws Exception {
        DbServer dbServer = dbServerFactory.getDbServer();
        
        try {
            DatasetType type = dataDAO.getDatasetType("Temporal Profile (A/M/PTPRO)", session);
            System.out.println("dataset type: " + type.getName().toUpperCase());
            EmfDataset dataset = newDataset("dataset-dao-test-one");
            dataset.setCreator("emf");
            dataset.setDatasetType(type);
            add(dataset);
            EmfDataset toBeDelete = (EmfDataset) load(EmfDataset.class, dataset.getName());
            
            Version version = new Version();
            version.setVersion(0);
            
            VersionedDataFormatFactory dataformatFactory = new VersionedDataFormatFactory(version, dataset);
            File folder = new File("test/data/temporal-profiles");
            Importer importer = new TemporalProfileImporter(folder, new String[] { "amptpro.m3.us+can.txt" }, dataset, dbServer,
                    dbServer.getSqlDataTypes(), dataformatFactory);
            VersionedImporter importerv = new VersionedImporter(importer, dataset, dbServer, new Date());
            importerv.run();
            
            EmfDataset dataset2 = newDataset("dataset-dao-test-two");
            dataset2.setCreator("emf");
            dataset2.setDatasetType(type);
            add(dataset2);
            
            Version version2 = new Version();
            version2.setVersion(0);
            
            VersionedDataFormatFactory dataformatFactory2 = new VersionedDataFormatFactory(version2, dataset2);
            File folder2 = new File("test/data/temporal-profiles");
            Importer importer2 = new TemporalProfileImporter(folder2, new String[] { "diurnal-weekend.txt" }, dataset2, dbServer,
                    dbServer.getSqlDataTypes(), dataformatFactory2);
            VersionedImporter importerv2 = new VersionedImporter(importer2, dataset2, dbServer, new Date());
            importerv2.run();
            
            
            dao.deleteDatasets(new EmfDataset[]{toBeDelete}, dbServer, session);
            
            assertEquals(1, countRecords("versions"));
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error: " + e.getMessage());
        } finally {
            if (dbServer != null && dbServer.isConnected())
                dbServer.disconnect();
        }
    }

    public void testShouldGetDatasetsForaDatasetType() throws Exception {
        DatasetType datasetType1 = newDatasetType("type1");
        DatasetType datasetType2 = newDatasetType("type2");

        EmfDataset dataset1 = newDataset("name1");
        EmfDataset dataset2 = newDataset("name2");
        EmfDataset dataset3 = newDataset("name3");
        
        dataset1.setDatasetType(datasetType1);
        dataset2.setDatasetType(datasetType1);
        dataset3.setDatasetType(datasetType2);

        try {
            dao.updateWithoutLocking(dataset1,session);
            dao.updateWithoutLocking(dataset2,session);
            dao.updateWithoutLocking(dataset3,session);
            
            List datasets = dao.getDatasets(session, datasetType1);
            assertEquals(2, datasets.size());
            assertEquals(dataset1.getId(), ((EmfDataset) datasets.get(0)).getId());
            assertEquals(dataset1.getName(), ((EmfDataset) datasets.get(0)).getName());
            assertEquals(dataset2.getId(), ((EmfDataset) datasets.get(1)).getId());
            assertEquals(dataset2.getName(), ((EmfDataset) datasets.get(1)).getName());
        } finally {
            remove(dataset1);
            remove(dataset2);
            remove(dataset3);
            remove(datasetType1);
            remove(datasetType2);
        }
    }

    public void testShouldUpdateDatasetOnUpdate() throws Exception {
        EmfDataset dataset = newDataset("dataset-dao-test");
        Country country = new Country("test-country");

        try {
            dcDao.add(country, session);
            dataset.setCountry(country);

            dao.updateWithoutLocking(dataset, session);
            EmfDataset result = (EmfDataset) load(EmfDataset.class, dataset.getName());

            assertEquals(dataset.getId(), result.getId());
            assertEquals("test-country", result.getCountry().getName());
        } finally {
            remove(dataset);
            remove(country);
        }
    }


    public void testShouldRemoveDatasetFromDatabaseOnRemove() throws Exception {
        EmfDataset dataset = newDataset("dataset-dao-test");
        User owner = userDAO.get("emf", session);
        
        dao.remove(owner, dataset, session);
        EmfDataset result = (EmfDataset) load(EmfDataset.class, dataset.getName());

        assertNull("Should be removed from the database on 'remove'", result);
    }

    public void testShouldConfirmDatasetExistsWhenQueriedByName() throws Exception {
        EmfDataset dataset = newDataset("dataset-dao-test");

        try {
            assertTrue("Should be able to confirm existence of dataset", dao.exists(dataset.getName(), session));

        } finally {
            remove(dataset);
        }
    }

    public void testShouldObtainLockedDatasetForUpdate() throws EmfException {
        User owner = userDAO.get("emf", session);
        EmfDataset dataset = newDataset("dataset-dao-test");

        try {
            EmfDataset locked = dao.obtainLocked(owner, dataset, session);
            assertEquals(locked.getLockOwner(), owner.getUsername());

            EmfDataset loadedFromDb = (EmfDataset) load(EmfDataset.class, dataset.getName());
            assertEquals(loadedFromDb.getLockOwner(), owner.getUsername());
        } finally {
            remove(dataset);
        }
    }

    public void testShouldUpdateDatasetAfterObtainingLock() throws Exception {
        UserDAO userDao = new UserDAO();
        User owner = userDao.get("emf", session);
        EmfDataset dataset = newDataset("dataset-dao-test");

        try {
            EmfDataset locked = dao.obtainLocked(owner, dataset, session);
            assertEquals(locked.getLockOwner(), owner.getUsername());
            locked.setName("TEST");

            EmfDataset modified = dao.update(locked, session);
            assertEquals("TEST", locked.getName());
            assertEquals(modified.getLockOwner(), null);
        } finally {
            remove(dataset);
        }
    }

    public void testShouldFailToGetLockWhenAlreadyLockedByAnotherUser() throws EmfException {
        UserDAO userDao = new UserDAO();
        User owner = userDao.get("emf", session);
        EmfDataset dataset = newDataset("dataset-dao-test");

        try {
            dao.obtainLocked(owner, dataset, session);

            User user = userDao.get("admin", session);
            EmfDataset result = dao.obtainLocked(user, dataset, session);

            assertFalse("Should have failed to obtain lock as it's already locked by another user", result
                    .isLocked(user));// failed to obtain lock for another user
        } finally {
            remove(dataset);
        }
    }

    public void testShouldReleaseLock() throws EmfException {
        UserDAO userDao = new UserDAO();
        User owner = userDao.get("emf", session);
        EmfDataset dataset = newDataset("dataset-dao-test");

        try {
            EmfDataset locked = dao.obtainLocked(owner, dataset, session);
            EmfDataset released = dao.releaseLocked(owner, locked, session);
            assertFalse("Should have released lock", released.isLocked());

            EmfDataset loadedFromDb = (EmfDataset) load(EmfDataset.class, dataset.getName());
            assertFalse("Should have released lock", loadedFromDb.isLocked());
        } finally {
            remove(dataset);
        }
    }

    public void testShouldGetNonDeletedDatasets() throws Exception {
        EmfDataset dataset = newDataset("dataset-dao-test");
        EmfDataset dataset2 = newDataset("test2");
        dataset2.setStatus("Deleted");
        EmfDataset dataset3 = newDataset("test3");
        dataset3.setStatus("Deleted");
        
        try {
            EmfDataset[] loadedFromDb = (EmfDataset[])dao.allNonDeleted(session, 1).toArray(new EmfDataset[0]);
            assertEquals(1, loadedFromDb.length);
            assertEquals("dataset-dao-test", loadedFromDb[0].getName());
        } finally {
            remove(dataset);
            remove(dataset2);
            remove(dataset3);
        }
    }

    public void testShouldGetNonDeletedDatasetsWithSpecifiedDatasettype() throws Exception {
        EmfDataset dataset = newDataset("dataset-dao-test");
        dataset.setDatasetType(types[0]);
        EmfDataset dataset2 = newDataset("test2");
        dataset2.setDatasetType(types[1]);
        EmfDataset dataset3 = newDataset("test3");
        dataset3.setDatasetType(types[0]);
        dataset3.setStatus("Deleted");
        
        try {
            EmfDataset[] loadedFromDb = (EmfDataset[])dao.getDatasets(session, types[0]).toArray(new EmfDataset[0]);
            assertEquals(1, loadedFromDb.length);
            assertEquals("dataset-dao-test", loadedFromDb[0].getName());
        } finally {
            remove(dataset);
            remove(dataset2);
            remove(dataset3);
        }
    }

    public void testShouldGetNonDeletedDatasetsWithSpecifiedName() throws Exception {
        EmfDataset dataset = newDataset("dataset-dao-test");
        EmfDataset dataset2 = newDataset("test2");
        dataset2.setStatus("Deleted");
        EmfDataset dataset3 = newDataset("test3");
        dataset3.setStatus("Deleted");
        
        try {
            EmfDataset loadedFromDb = dao.getDataset(session, "dataset-dao-test");
            assertEquals("dataset-dao-test", loadedFromDb.getName());
        } finally {
            remove(dataset);
            remove(dataset2);
            remove(dataset3);
        }
    }

    public void testShouldGetNonDeletedDatasetsWithSpecifiedID() throws Exception {
        EmfDataset dataset = newDataset("dataset-dao-test");
        EmfDataset dataset2 = newDataset("test2");
        dataset2.setStatus("Deleted");
        EmfDataset dataset3 = newDataset("test3");
        dataset3.setStatus("Deleted");
        
        try {
            EmfDataset loadedFromDb = dao.getDataset(session, dataset.getId());
            assertEquals("dataset-dao-test", loadedFromDb.getName());

            EmfDataset loadedFromDb2 = dao.getDataset(session, dataset2.getId());
            assertNull(loadedFromDb2);

            EmfDataset loadedFromDb3 = dao.getDataset(session, dataset3.getId());
            assertNull(loadedFromDb3);
        } finally {
            remove(dataset);
            remove(dataset2);
            remove(dataset3);
        }
    }

    public void testGetDatasetsByNameContaining() throws Exception {
        DatasetType datasetType1 = newDatasetType("type1");
        DatasetType datasetType2 = newDatasetType("type2");
        
        EmfDataset dataset = newDataset("dataset-dao-test");
        EmfDataset dataset2 = newDataset("test2");
        EmfDataset dataset3 = newDataset("test3");
        
        dataset.setDatasetType(datasetType1);
        dataset2.setDatasetType(datasetType1);
        dataset3.setDatasetType(datasetType2);

        dao.updateWithoutLocking(dataset, session);
        dao.updateWithoutLocking(dataset2, session);
        dao.updateWithoutLocking(dataset3, session);

        try {
            List list = dao.getDatasets(session, datasetType1.getId(), "test");

            System.out.println(list.size());
            System.out.println(((EmfDataset)list.get(1)).getName());
        } finally {
            remove(dataset);
            remove(dataset2);
            remove(dataset3);
            remove(datasetType1);
            remove(datasetType2);
        }
    }

    public void testShouldDetectWhetherUsedByControlStrategiesOrCases() throws EmfException {
        EmfDataset dataset = newDataset("dataset-dao-test");
        EmfDataset dataset2 = newDataset("test2");
        EmfDataset dataset3 = newDataset("test3");
        
        ControlStrategy strategy = newControlStrategy();
        strategy.setControlStrategyInputDatasets(new ControlStrategyInputDataset[]{new ControlStrategyInputDataset(dataset)});
        
        CaseInput input = new CaseInput();
        input.setDataset(dataset2);
        Case caseObj = newCase();
        input.setCaseID(caseObj.getId());
        caseDao.add(input, session);
        session.clear();
        
        try {
            assertTrue(dao.isUsedByControlStrategies(session, dataset));
            assertFalse(dao.isUsedByControlStrategies(session, dataset2));
            assertFalse(dao.isUsedByControlStrategies(session, dataset3));
            
            assertTrue(dao.isUsedByCases(session, dataset2));
            assertFalse(dao.isUsedByCases(session, dataset));
            assertFalse(dao.isUsedByCases(session, dataset3));
        } finally {
            remove(strategy);
            remove(input);
            remove(dataset);
            remove(dataset2);
            remove(dataset3);
            remove(caseObj);
        }
    }
    
    private EmfDataset newDataset(String name) throws EmfException {
        User owner = userDAO.get("emf", session);

        EmfDataset dataset = new EmfDataset();
        
        String newName = name;
        if ( newName != null) {
            newName = newName.trim();
        } else {
            throw new EmfException("Dataset name is null");
        }
        dataset.setName(newName);

        dataset.setCreator(owner.getUsername());

        save(dataset);
        return (EmfDataset) load(EmfDataset.class, dataset.getName());
    }

    private DatasetType newDatasetType(String name) {
        DatasetType datasetType = new DatasetType(name);
        datasetType.setDescription("no description");
        save(datasetType);

        return (DatasetType) load(DatasetType.class, datasetType.getName());
    }
    
    private ControlStrategy newControlStrategy() {
        ControlStrategy element = new ControlStrategy("test" + Math.random());
        strategyDao.add(element, session);
        return element;
    }
    
    private Case newCase() {
        Case element = new Case("test" + Math.random());
        caseDao.add(element, session);

        return element;
    }
    
    private Version newVersion(int datasetId, int versionNum, boolean isFinal) {
        /**
         * Associates dataset id w/ a new version adds version to db and returns new version
         */
        // Create version db
        Version version = new Version();
        version.setDatasetId(datasetId);
        version.setVersion(versionNum);
        version.setPath("");
        version.setFinalVersion(isFinal);
        Date lastModifiedDate = new Date(); // initialized to now
        version.setLastModifiedDate(lastModifiedDate);

        version.setName("test_version" + Math.random());
        add(version);
        return (version);
    }
    
    private CaseJob newCaseJob(Case caseObj) {
        CaseJob job = new CaseJob("test" + Math.random());
        job.setCaseId(caseObj.getId());
        add(job);
        
        return job;
    }

    private Case load(Case caseObj) {
        Transaction tx = null;

        session.clear();
        try {
            tx = session.beginTransaction();
            Criteria crit = session.createCriteria(Case.class).add(Restrictions.eq("name", caseObj.getName()));
            tx.commit();

            return (Case) crit.uniqueResult();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    private CaseJob load(CaseJob job) {
        Transaction tx = null;
        
        session.clear();
        try {
            tx = session.beginTransaction();
            Criteria crit = session.createCriteria(CaseJob.class).add(Restrictions.eq("name", job.getName()));
            tx.commit();
            
            return (CaseJob) crit.uniqueResult();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

}

