package gov.epa.emissions.framework.services.qa;

//import java.io.File;
//import java.util.Date;

import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.db.DbServer;
//import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.db.TableModifier;
import gov.epa.emissions.commons.db.version.Version;
//import gov.epa.emissions.commons.io.importer.Importer;
//import gov.epa.emissions.commons.io.importer.VersionedDataFormatFactory;
//import gov.epa.emissions.commons.io.importer.VersionedImporter;
//import gov.epa.emissions.commons.io.orl.ORLOnRoadImporter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.ServiceTestCase;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;
//import gov.epa.emissions.framework.services.exim.ExImServiceImpl;
//import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;
//import gov.epa.emissions.framework.services.qa.SQLAnnualQueryRunner;

public class SQLMultiInvProgramQueryTest extends ServiceTestCase {
    
    public void testOfQueryThroughRunner() throws Exception {
        EmfDataset dataset = null;
        EmfDataset dataset2 = null;
        EmfDataset dataset3 = null;
        EmfDataset dataset4 = null;
        DbServer dbServer = null;
        String emissioDatasourceName = dbServer().getEmissionsDatasource().getName();
        try {
            String tableName = "table1";
            dataset = dataset(0, "test", tableName);
            add(dataset);
            dataset = (EmfDataset) load(EmfDataset.class, dataset.getName());
            Version version = version(dataset.getId());
            addVersionEntryToVersionsTable(version);

            tableName = "table2";
            dataset2 = dataset(1, "test2", tableName);
            add(dataset2);
            dataset2 = (EmfDataset) load(EmfDataset.class, dataset2.getName());
            version = version(dataset2.getId());
            addVersionEntryToVersionsTable(version);

            tableName = "table3";
            dataset3 = dataset(1, "test3", tableName);
            add(dataset3);
            dataset3 = (EmfDataset) load(EmfDataset.class, dataset3.getName());
            version = version(dataset3.getId());
            addVersionEntryToVersionsTable(version);

            tableName = "invtable";
            dataset4 = dataset(1, "invtable", tableName);
            add(dataset4);
            dataset4 = (EmfDataset) load(EmfDataset.class, dataset4.getName());
            version = version(dataset4.getId());
            addVersionEntryToVersionsTable(version);

            tableName = "newtable";
            QAStep qaStep = new QAStep();
            qaStep.setName("Step1");
            //qaStep.setProgramArguments("-inv\ntest\ntest2\ntest3\n-invtable\ninvtable\n-summaryType\n");
            qaStep.setProgramArguments("-inv\ntest\ntest2\ntest3\n-invtable\ninvtable\n-summaryType\nState");

            dbServer = dbServerFactory.getDbServer();
            //String emissioDatasourceName = dbServer().getEmissionsDatasource().getName();
            //this will set the properties...
            //new ExImServiceImpl(emf(), dbServer, sessionFactory);
            SQLMultiInvSumProgramQuery query = new SQLMultiInvSumProgramQuery(sessionFactory, emissioDatasourceName, tableName,
                    qaStep);
            //runner.query();
            System.out.println(query.createInvSumProgramQuery());
        } catch (Exception e) {
            // NOTE: handle exception
            e.printStackTrace();
        }    finally {
            //runner.query(dbServer, qaStep, tableName);
            dbServer.disconnect();
            dropVersionDataFromTable();
            remove(dataset);
            remove(dataset2);
            remove(dataset3);
            remove(dataset4);
        }
    }
    
    private EmfDataset dataset(int datasetId, String name, String tableName) throws EmfException {
        EmfDataset dataset = new EmfDataset();
        
        String newName = name;
        if ( newName != null) {
            newName = newName.trim();
        } else {
            throw new EmfException("Dataset name is null");
        }
        dataset.setName(newName);
        
        dataset.setId(datasetId);
        dataset.setCreator("emf");
        InternalSource source = new InternalSource();
        source.setTable(tableName);
        source.setSource("test");
        source.setCols(new String[] {});
        source.setType("TEST TYPE");
        source.setSourceSize(123456);
        dataset.setInternalSources(new InternalSource[] { source });

        return dataset;
    }
    
    private Version version(int datasetId) {
        Version version = new Version();
        version.setId(0);
        version.setDatasetId(datasetId);
        version.setVersion(0);
        version.setName("Initial Version");
        version.setPath("");
        return version;
    }
    private void addVersionEntryToVersionsTable(Version version) throws Exception {
        TableModifier modifier = new TableModifier(dbSetup.getDbServer().getEmissionsDatasource(), "versions");
        modifier.insertOneRow(createVersionData(version));
    }
    
    private String[] createVersionData(Version version) {
        return new String[] {
            "", "" + version.getDatasetId(), "" + version.getVersion(), version.getName(), version.getPath()   
        };
    }
    
   private void dropVersionDataFromTable() throws Exception {
        TableModifier modifier = new TableModifier(dbSetup.getDbServer().getEmissionsDatasource(), "versions");
        modifier.dropAllData();
   }
    
   protected void doSetUp() throws Exception {
       //Nothing
    }
    
    protected void doTearDown() throws Exception {
       //Nothing 
    }

}
