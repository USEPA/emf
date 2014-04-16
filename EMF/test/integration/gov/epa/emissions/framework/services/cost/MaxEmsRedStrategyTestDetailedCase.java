package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.db.TableModifier;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.csv.CSVImporter;
import gov.epa.emissions.commons.io.importer.ImporterException;
import gov.epa.emissions.commons.io.importer.VersionedDataFormatFactory;
import gov.epa.emissions.commons.io.orl.ORLNonPointImporter;
import gov.epa.emissions.commons.io.orl.ORLNonRoadImporter;
import gov.epa.emissions.commons.io.orl.ORLOnRoadImporter;
import gov.epa.emissions.commons.io.orl.ORLPointImporter;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.ServiceTestCase;
import gov.epa.emissions.framework.services.basic.UserDAO;
import gov.epa.emissions.framework.services.cost.analysis.maxreduction.MaxEmsRedStrategy;
import gov.epa.emissions.framework.services.cost.analysis.maxreduction.StrategyLoader;
import gov.epa.emissions.framework.services.cost.controlStrategy.AbstractControlStrategyInventoryOutput;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyInventoryOutput;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.services.cost.controlmeasure.Scc;
import gov.epa.emissions.framework.services.cost.controlmeasure.io.CMImportTask;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;
import gov.epa.emissions.framework.services.data.DatasetDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;

import java.io.File;

import org.hibernate.Session;

public class MaxEmsRedStrategyTestDetailedCase extends ServiceTestCase {

    protected DbServer dbServer;

    private SqlDataTypes sqlDataTypes;

    // protected ControlStrategyService service;

    protected String tableName = "test" + Math.round(Math.random() * 1000) % 1000;

    protected void doSetUp() throws Exception {
        dbServer = dbServerFactory.getDbServer();
        sqlDataTypes = dbServer.getSqlDataTypes();
        setProperties();
        // import control measures to use...
        importControlMeasures();
        // service = new ControlStrategyServiceImpl(sessionFactory);
    }

    protected ControlStrategyInputDataset setInputDataset(String type) throws Exception {
        EmfDataset inputDataset = new EmfDataset();
        inputDataset.setName("test" + Math.round(Math.random() * 1000) % 1000);
        inputDataset.setCreator(emfUser().getUsername());
        inputDataset.setDatasetType(getDatasetType(type));// orlNonpointDatasetType());

        add(inputDataset);
        session.flush();
        inputDataset = (EmfDataset) load(EmfDataset.class, inputDataset.getName());

        if (type.equalsIgnoreCase("ORL nonpoint"))
            inputDataset = addORLNonpointDataset(inputDataset);
        else if (type.equalsIgnoreCase("ORL point"))
            inputDataset = addORLPointDataset(inputDataset);
        else if (type.equalsIgnoreCase("ORL onroad"))
            inputDataset = addORLOnroadDataset(inputDataset);
        else if (type.equalsIgnoreCase("ORL Nonroad"))
            inputDataset = addORLNonroadDataset(inputDataset);

        addVersionZeroEntryToVersionsTable(inputDataset, dbServer.getEmissionsDatasource());
        inputDataset = (EmfDataset) load(EmfDataset.class, inputDataset.getName());
        ControlStrategyInputDataset controlStrategyInputDataset = new ControlStrategyInputDataset(inputDataset);
        controlStrategyInputDataset.setVersion(inputDataset.getDefaultVersion());
        return controlStrategyInputDataset;
    }

    protected EmfDataset getRegionDataset() throws Exception {
        EmfDataset inputDataset = new EmfDataset();
        inputDataset.setName("test" + Math.round(Math.random() * 1000) % 1000);
        inputDataset.setCreator(emfUser().getUsername());
        inputDataset.setDatasetType(getDatasetType("List of Counties (CSV)"));// orlNonpointDatasetType());

        add(inputDataset);
        session.flush();
        inputDataset = (EmfDataset) load(EmfDataset.class, inputDataset.getName());

        inputDataset = addRegionDataset(inputDataset);

        addVersionZeroEntryToVersionsTable(inputDataset, dbServer.getEmissionsDatasource());
        inputDataset = (EmfDataset) load(EmfDataset.class, inputDataset.getName());
        return inputDataset;
    }

    private DatasetType getDatasetType(String type) {
        DatasetType ds = null;
        if (type.equalsIgnoreCase("ORL nonpoint"))
            ds = (DatasetType) load(DatasetType.class, DatasetType.orlNonpointInventory);
        else if (type.equalsIgnoreCase("ORL point"))
            ds = (DatasetType) load(DatasetType.class, DatasetType.orlPointInventory);
        else if (type.equalsIgnoreCase("ORL onroad"))
            ds = (DatasetType) load(DatasetType.class, DatasetType.orlOnroadInventory);
        else if (type.equalsIgnoreCase("ORL Nonroad"))
            ds = (DatasetType) load(DatasetType.class, DatasetType.orlNonroadInventory);
        else if (type.equalsIgnoreCase("List of Counties (CSV)"))
            ds = (DatasetType) load(DatasetType.class, "List of Counties (CSV)");
        return ds;
    }

    // private DatasetType orlNonpointDatasetType() {
    // return (DatasetType) load(DatasetType.class, DatasetType.orlNonpointInventory);
    // }

    private void addVersionZeroEntryToVersionsTable(Dataset dataset, Datasource datasource) throws Exception {
//        TableModifier modifier = new TableModifier(datasource, "versions");
//        String[] data = { null, dataset.getId() + "", "0", "Initial Version", "", "true", null };
//        modifier.insertOneRow(data);
        
        Version defaultZeroVersion = new Version(0);
        defaultZeroVersion.setName("Initial Version");
        defaultZeroVersion.setPath("");
        defaultZeroVersion.setDatasetId(dataset.getId());
        defaultZeroVersion.setFinalVersion(true);
        defaultZeroVersion.setLastModifiedDate(null);

        Session session = sessionFactory.getSession();

        try {
            new DatasetDAO().add(defaultZeroVersion, session);
        } catch (Exception e) {
            throw new EmfException("Could not add default zero version: " + e.getMessage());
        } finally {
            session.close();
        }
    }

    protected void doTearDown() throws Exception {
        // dropTable(tableName, dbServer.getEmissionsDatasource());

        // dropAll(Version.class);
        // dropAll(InternalSource.class);
        // dropAll(QAStepResult.class);
        // dropAll(QAStep.class);
        // dropAll(EmfDataset.class);
        dbServer.disconnect();
    }

    protected ControlStrategy controlStrategy(ControlStrategyInputDataset inputDataset, String name,
            Pollutant pollutant, ControlMeasureClass[] classes) throws Exception {
        ControlStrategy strategy = new ControlStrategy();
        strategy.setName(name);
        strategy.setControlStrategyInputDatasets(new ControlStrategyInputDataset[] { inputDataset });
        strategy.setInventoryYear(2000);
        strategy.setCostYear(2000);
        strategy.setTargetPollutant(pollutant);
        strategy.setStrategyType(maxEmisRedStrategyType());
        strategy.setControlMeasureClasses(classes);
        // strategy.setCountyFile("c:\\cep\\EMF\\test\\data\\cost\\controlStrategy\\070 Run Counties_OTC and West States Statewide.csv");
        // strategy.setFilter("srctype = 2");
        add(strategy);
        return (ControlStrategy) load(ControlStrategy.class, strategy.getName());
    }

    protected ControlStrategy controlStrategy(ControlStrategyInputDataset inputDataset, String name,
            Pollutant pollutant, ControlStrategyMeasure[] measures) {
        ControlStrategy strategy = new ControlStrategy();
        strategy.setName(name);
        strategy.setControlStrategyInputDatasets(new ControlStrategyInputDataset[] { inputDataset });
        strategy.setInventoryYear(2000);
        strategy.setCostYear(2000);
        strategy.setTargetPollutant(pollutant);
        strategy.setStrategyType(maxEmisRedStrategyType());
        strategy.setControlMeasures(measures);
        add(strategy);
        return strategy;
    }

    protected ControlStrategy controlStrategy(ControlStrategyInputDataset inputDataset, String name, Pollutant pollutant) {
        ControlStrategy strategy = new ControlStrategy();
        strategy.setName(name);
        strategy.setControlStrategyInputDatasets(new ControlStrategyInputDataset[] { inputDataset });
        strategy.setInventoryYear(2000);
        strategy.setCostYear(2000);
        strategy.setTargetPollutant(pollutant);
        strategy.setStrategyType(maxEmisRedStrategyType());
        add(strategy);
        return strategy;
    }

    protected User emfUser() {
        return new UserDAO().get("emf", session);
    }

    private StrategyType maxEmisRedStrategyType() {
        return (StrategyType) load(StrategyType.class, "Max Emissions Reduction");
    }

    private EmfDataset addORLPointDataset(EmfDataset inputDataset) throws ImporterException {
        Version version = new Version();
        version.setVersion(0);
        version.setDatasetId(inputDataset.getId());

        File folder = new File("test/data/orl/nc");
        String[] fileNames = { "ptinv.nti99_NC-with-matching_values.txt" };
        ORLPointImporter importer = new ORLPointImporter(folder, fileNames, inputDataset, dbServer, sqlDataTypes,
                new VersionedDataFormatFactory(version, inputDataset));
        importer.run();
        add(inputDataset);
        session.flush();
        return (EmfDataset) load(EmfDataset.class, inputDataset.getName());
    }

    private EmfDataset addORLNonpointDataset(EmfDataset inputDataset) throws ImporterException {
        Version version = new Version();
        version.setVersion(0);
        version.setDatasetId(inputDataset.getId());

        File folder = new File("test/data/cost");
        String[] fileNames = { "orl-nonpoint-for_cs_testing.txt" };
        ORLNonPointImporter importer = new ORLNonPointImporter(folder, fileNames, inputDataset, dbServer, sqlDataTypes,
                new VersionedDataFormatFactory(version, inputDataset));
        importer.run();
        add(inputDataset);
        session.flush();
        return (EmfDataset) load(EmfDataset.class, inputDataset.getName());
    }

    private EmfDataset addORLOnroadDataset(EmfDataset inputDataset) throws ImporterException {
        Version version = new Version();
        version.setVersion(0);
        version.setDatasetId(inputDataset.getId());

        File folder = new File("test/data/orl/nc");
        String[] fileNames = { "orl_onroad_with_poll_name_txt_17aug2006.txt" };
        ORLOnRoadImporter importer = new ORLOnRoadImporter(folder, fileNames, inputDataset, dbServer, sqlDataTypes,
                new VersionedDataFormatFactory(version, inputDataset));
        importer.run();
        add(inputDataset);
        session.flush();
        return (EmfDataset) load(EmfDataset.class, inputDataset.getName());
    }

    private EmfDataset addORLNonroadDataset(EmfDataset inputDataset) throws ImporterException {
        Version version = new Version();
        version.setVersion(0);
        version.setDatasetId(inputDataset.getId());

        File folder = new File("test/data/orl/nc");
        String[] fileNames = { "arinv.nonroad.nti99d_NC-with-matching_values.txt" };
        ORLNonRoadImporter importer = new ORLNonRoadImporter(folder, fileNames, inputDataset, dbServer, sqlDataTypes,
                new VersionedDataFormatFactory(version, inputDataset));
        importer.run();
        add(inputDataset);
        session.flush();
        return (EmfDataset) load(EmfDataset.class, inputDataset.getName());
    }

    private EmfDataset addRegionDataset(EmfDataset inputDataset) throws ImporterException {
        Version version = new Version();
        version.setVersion(0);
        version.setDatasetId(inputDataset.getId());

        File folder = new File("test/data/cost/regionFile");
        String[] fileNames = { "regions.csv" };
        CSVImporter importer = new CSVImporter(folder, fileNames, inputDataset, dbServer, sqlDataTypes,
                new VersionedDataFormatFactory(version, inputDataset));
        importer.run();
        add(inputDataset);
        session.flush();
        return (EmfDataset) load(EmfDataset.class, inputDataset.getName());
    }

    protected ControlMeasure addControlMeasure(String name, String abbr, Scc[] sccs, EfficiencyRecord[] records) {
        ControlMeasure measure = new ControlMeasure();
        measure.setName(name);
        measure.setAbbreviation(abbr);
        measure.setEfficiencyRecords(records);
        measure.setSccs(sccs);
        add(measure);
        ControlMeasure load = (ControlMeasure) load(ControlMeasure.class, measure.getName());
        for (int i = 0; i < sccs.length; i++) {
            sccs[i].setControlMeasureId(load.getId());
            add(sccs[i]);
        }
        return load;
    }

    protected EfficiencyRecord record(Pollutant pollutant, String locale, float efficiency, double cost, int costYear) {
        EfficiencyRecord record = new EfficiencyRecord();
        record.setPollutant(pollutant);
        record.setLocale(locale);
        record.setEfficiency((double)efficiency);
        record.setRuleEffectiveness(100);
        record.setRulePenetration(100);
        record.setCostPerTon(cost);
        record.setCostYear(costYear);
        return record;
    }

    protected String detailResultDatasetTableName(ControlStrategy strategy, int index) throws Exception {
        try {
            ControlStrategyResult result = getControlStrategyResult(strategy, index);
            Dataset detailedResultDataset = result.getDetailedResultDataset();
            return detailedResultDataset.getInternalSources()[0].getTable();
        } finally {
            //
        }
    }

    protected String detailResultDatasetTableName(ControlStrategy strategy) throws Exception {
        return detailResultDatasetTableName(strategy, 0);
    }

    protected ControlStrategyResult getControlStrategyResult(ControlStrategy controlStrategy, int index) {
        Session session = sessionFactory.getSession();
        try {
            // return new ControlStrategyDAO().getControlStrategyResults(controlStrategy.getId(), session)[index];
            return (new ControlStrategyDAO()).getControlStrategyResults(controlStrategy.getId(), session).toArray(
                    new ControlStrategyResult[0])[index];
        } finally {
            session.close();
        }
    }

    protected ControlStrategyResult getControlStrategyResult(ControlStrategy controlStrategy) {
        return getControlStrategyResult(controlStrategy, 0);
    }

    private void importControlMeasures() throws EmfException, Exception {
        File folder = new File("test/data/cost/controlMeasure");
        String[] fileNames = { "CMSummary.csv", "CMSCCs.csv", "CMEfficiencies.csv", "CMReferences.csv" };
        CMImportTask task = new CMImportTask(folder, fileNames, emfUser(), false, new int[] {}, sessionFactory, dbServerFactory());
        task.run();
    }

    protected void runStrategy(ControlStrategy strategy) throws EmfException {

        StrategyLoader loader = new StrategyLoader(emfUser(), dbServerFactory, sessionFactory, strategy);
        MaxEmsRedStrategy strategyTask = new MaxEmsRedStrategy(strategy, emfUser(), dbServerFactory, sessionFactory,
                loader);
        strategyTask.run();
    }

    protected void createControlledInventory(ControlStrategy strategy, ControlStrategyResult controlStrategyResult)
            throws Exception {
        // create the controlled inventory for this strategy run....
        ControlStrategyInventoryOutput output = new AbstractControlStrategyInventoryOutput(emfUser(), strategy,
                controlStrategyResult, "", sessionFactory, dbServerFactory);
        output.create();
    }

    protected String getExportDirectory() {
        // TBD: make this use the new temp dir
        String tempDir = System.getProperty("IMPORT_EXPORT_TEMP_DIR");

        if (tempDir == null || tempDir.isEmpty())
            tempDir = System.getProperty("java.io.tmpdir");
        return tempDir;
    }
}
