package gov.epa.emissions.framework.services.qa;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.db.TableModifier;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.importer.ImporterException;
import gov.epa.emissions.commons.io.importer.VersionedDataFormatFactory;
import gov.epa.emissions.commons.io.orl.ORLNonPointImporter;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.QAStepTask;
import gov.epa.emissions.framework.services.ServiceTestCase;
import gov.epa.emissions.framework.services.basic.UserDAO;
import gov.epa.emissions.framework.services.data.DataCommonsDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.data.QAStepResult;

import java.io.File;
import java.util.List;

public class QAStepTaskTest extends ServiceTestCase {

    private UserDAO userDAO;

    private DbServer dbserver;

    private String tableName = "test" + Math.round(Math.random() * 1000) % 1000;

    protected void doSetUp() throws Exception {
        userDAO = new UserDAO();
        dbserver = dbServer();
    }

    protected void doTearDown() throws Exception {
        dropAll(QAStepResult.class);
        dropAll(QAStep.class);
        dropAll(Version.class);
        dropAll(InternalSource.class);
        dropAll(EmfDataset.class);
    }

    private void dropTables(EmfDataset dataset) throws Exception {
        dropTable(tableName, dbServer().getEmissionsDatasource());
        dropTable("qasummarize_by_county_and_pollutant_dsid" + dataset.getId() + "_v0", dbServer()
                .getEmissionsDatasource());
        dropTable("qasummarize_by_scc_and_pollutant_dsid" + dataset.getId() + "_v0", dbServer()
                .getEmissionsDatasource());
        dropTable("qasummarize_by_pollutant_dsid" + dataset.getId() + "_v0", dbServer().getEmissionsDatasource());
    }

    public void testShouldGetDefaultSummaryQANames() throws Exception {
        EmfDataset dataset = newDataset(tableName, "");
        User user = userDAO.get("emf", session);

        QAStepTask qaTask = new QAStepTask(dataset, 0, user, sessionFactory(), dbServerFactory());
        String[] summaryQANames = qaTask.getDefaultSummaryQANames();

        assertEquals(4, summaryQANames.length);
        assertEquals("Summarize by Pollutant", summaryQANames[0]);
        assertEquals("Summarize by SCC and Pollutant", summaryQANames[1]);
        assertEquals("Summarize by County and Pollutant", summaryQANames[2]);
        assertEquals("Summarize by US State and Pollutant", summaryQANames[3]);
    }

    public void testShouldCheckAndRunSummaryQASteps() throws Exception {
        EmfDataset inputDataset = new EmfDataset();
        
        String newName = tableName;
        if ( newName != null) {
            newName = newName.trim();
        } else {
            throw new EmfException("Dataset name is null");
        }
        inputDataset.setName(newName);
        
        inputDataset.setCreator(userDAO.get("emf", session).getUsername());
        inputDataset.setDatasetType(getDatasetType(DatasetType.orlNonpointInventory));
        inputDataset = addORLNonpointDataset(inputDataset);

        addVersionZeroEntryToVersionsTable(inputDataset, dbserver.getEmissionsDatasource());

        try {
            QAStepTask qaTask = new QAStepTask(inputDataset, 0, userDAO.get("emf", session), sessionFactory(), dbServerFactory());
            String[] summaryQANames = qaTask.getDefaultSummaryQANames();
            qaTask.runSummaryQASteps(summaryQANames);
        } finally {
            dropTables(inputDataset);
        }
    }

    private void addVersionZeroEntryToVersionsTable(Dataset dataset, Datasource datasource) throws Exception {
        TableModifier modifier = new TableModifier(datasource, "versions");
        String[] data = { null, dataset.getId() + "", "0", "Initial Version", "", "true", null };
        modifier.insertOneRow(data);
    }

    private EmfDataset newDataset(String name, String type) throws EmfException{
        User owner = userDAO.get("emf", session);

        if (type.equals(""))
            type = DatasetType.orlNonpointInventory;

        EmfDataset dataset = new EmfDataset();
        
        String newName = name;
        if ( newName != null) {
            newName = newName.trim();
        } else {
            throw new EmfException("Dataset name is null");
        }
        dataset.setName(newName);
        
        
        dataset.setCreator(owner.getUsername());
        dataset.setDatasetType(getDatasetType(type));

        return (EmfDataset) load(EmfDataset.class, dataset.getName());
    }

    private DatasetType getDatasetType(String type) {
        DataCommonsDAO dcDao = new DataCommonsDAO();
        List types = dcDao.getDatasetTypes(session);

        for (int i = 0; i < types.size(); i++)
            if (((DatasetType) types.get(i)).getName().equalsIgnoreCase(type))
                return (DatasetType) types.get(i);

        return null;
    }

    private EmfDataset addORLNonpointDataset(EmfDataset inputDataset) throws ImporterException {
        DbServer dbServer = dbServer();
        SqlDataTypes sqlDataTypes = dbServer.getSqlDataTypes();
        Version version = new Version();
        version.setName("Initial Version");
        version.setVersion(0);
        version.setDatasetId(inputDataset.getId());

        File folder = new File("test/data/cost");
        String[] fileNames = { "orl-nonpoint-with-larger_values.txt" };
        ORLNonPointImporter importer = new ORLNonPointImporter(folder, fileNames, inputDataset, dbServer, sqlDataTypes,
                new VersionedDataFormatFactory(version, inputDataset));
        importer.run();
        add(inputDataset);
        session.flush();
        return (EmfDataset) load(EmfDataset.class, tableName);
    }
}
