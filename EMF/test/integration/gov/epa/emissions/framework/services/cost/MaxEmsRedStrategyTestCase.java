package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.db.TableModifier;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.importer.ImporterException;
import gov.epa.emissions.commons.io.importer.VersionedDataFormatFactory;
import gov.epa.emissions.commons.io.orl.ORLNonPointImporter;
import gov.epa.emissions.commons.io.orl.ORLOnRoadImporter;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.ServiceTestCase;
import gov.epa.emissions.framework.services.basic.UserDAO;
import gov.epa.emissions.framework.services.cost.controlmeasure.Scc;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;
import gov.epa.emissions.framework.services.data.DatasetDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;

import java.io.File;
import java.util.Date;

import org.hibernate.Session;

public class MaxEmsRedStrategyTestCase extends ServiceTestCase {

    private DbServer dbServer;

    private SqlDataTypes sqlDataTypes;

    protected String tableName = "test" + Math.round(Math.random() * 1000) % 1000;

    protected void doSetUp() throws Exception {
        dbServer = dbServer();
        sqlDataTypes = dbServer.getSqlDataTypes();
    }

    protected EmfDataset setInputDataset(String type) throws Exception {
        EmfDataset inputDataset = new EmfDataset();
        
        String newName = tableName;
        if ( newName != null) {
            newName = newName.trim();
        } else {
            throw new EmfException("Dataset name is null");
        }
        inputDataset.setName(newName);
       
        inputDataset.setCreator(emfUser().getUsername());
        inputDataset.setDatasetType(orlNonpointDatasetType());

        if (type.equalsIgnoreCase("ORL nonpoint"))
            inputDataset = addORLNonpointDataset(inputDataset);
        
        if (type.equalsIgnoreCase("ORL onroad"))
            inputDataset = addORLOnroadDataset(inputDataset);
        
        addVersionZeroEntryToVersionsTable(inputDataset, dbServer.getEmissionsDatasource());
        return inputDataset;
    }

    private DatasetType orlNonpointDatasetType() {
        return (DatasetType) load(DatasetType.class, DatasetType.orlNonpointInventory);
    }

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
        dropTable(tableName, dbServer.getEmissionsDatasource());
        dropAll(Version.class);
        dropAll(InternalSource.class);
        dropAll(EmfDataset.class);
    }

    protected ControlStrategy controlStrategy(EmfDataset inputDataset, String name, Pollutant pollutant) {
        ControlStrategy strategy = new ControlStrategy();
        strategy.setName(name);
        strategy.setControlStrategyInputDatasets(new ControlStrategyInputDataset[] { new ControlStrategyInputDataset(inputDataset) });
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

    private EmfDataset addORLNonpointDataset(EmfDataset inputDataset) throws ImporterException {
        Version version = new Version();
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
        return (EmfDataset) load(EmfDataset.class, tableName);
    }

    protected ControlMeasure addControlMeasure(String name, String abbr, Scc[] sccs, EfficiencyRecord[] records) {
        ControlMeasure measure = new ControlMeasure();
        measure.setName(name);
        measure.setAbbreviation(abbr);
        measure.setLastModifiedBy(emfUser().getName());
        measure.setLastModifiedTime(new Date());
        measure.setEfficiencyRecords(records);
        measure.setSccs(sccs);
        add(measure);
        ControlMeasure load = (ControlMeasure) load(ControlMeasure.class, measure.getName());
        for (int i = 0; i < sccs.length; i++) {
            sccs[i].setControlMeasureId(load.getId());
            add(sccs[i]);
        }
        for (int i = 0; i < records.length; i++) {
            records[i].setControlMeasureId(load.getId());
            records[i].setLastModifiedBy(emfUser().getName());
            records[i].setLastModifiedTime(new Date());
            add(records[i]);
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

}
