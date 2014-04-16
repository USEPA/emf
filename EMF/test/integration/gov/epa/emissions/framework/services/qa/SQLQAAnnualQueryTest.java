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
import gov.epa.emissions.framework.services.ServiceTestCase;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;
//import gov.epa.emissions.framework.services.exim.ExImServiceImpl;
//import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;
//import gov.epa.emissions.framework.services.qa.SQLAnnualQueryRunner;

public class SQLQAAnnualQueryTest extends ServiceTestCase {
    
    //private String emissioDatasourceName;
    
    /*public static void main (String args []) {
        SQLQAAnnualQuery annQuery = new SQLQAAnnualQuery();
        annQuery.createAnnualQuery();
    }*/
    
    //private DbServer dbServer;
    //private String tableName;
    //private EmfDataset dataset;
    //private Version version;
    //private SqlDataTypes sqlDataTypes;
    //private HibernateSessionFactory sessionFactory;

    public void testShoudParseTheQueryWhichContainsManyTags() throws Exception {
        QAStep qaStep = new QAStep();
        //qaStep.setName("Step1");
        //String userQuery = "SELECT * FROM reference.pollutants";
        //qaStep.setProgramArguments(userQuery);
        //SQLQAAnnualQuery annQuery = new SQLQAAnnualQuery(dbServer, qaStep, tableName, dataset,
               // version, sessionFactory );
        
        /*localDbServer = dbSetup.getNewPostgresDbServerInstance();
        version = new Version();
        version.setVersion(0);

        File file = new File("C:\\Documents and Settings\\rva\\My Documents\\data\\onroad_to_calculate", "onroad_calif_hap2002v2_jan_noRFL_12apr2007_orl.txt");
        Importer orlImporter = new ORLOnRoadImporter(file.getParentFile(), new String[] { file.getName() }, dataset,
                localDbServer, sqlDataTypes, new VersionedDataFormatFactory(version, dataset));
        VersionedImporter importer = new VersionedImporter(orlImporter, dataset, localDbServer, lastModifiedDate(file.getParentFile(),file.getName()));
        importer.run();*/
        
        
        String tableName = "table1";
        EmfDataset dataset = dataset(0, tableName);
        add(dataset);
        dataset = (EmfDataset)load(EmfDataset.class, dataset.getName());
        Version version = version(dataset.getId());
        String emissioDatasourceName = dbServer().getEmissionsDatasource().getName();
        addVersionEntryToVersionsTable(version);
        
        SQLQAAnnualQuery annQuery = new SQLQAAnnualQuery(sessionFactory, emissioDatasourceName, tableName, qaStep);
        //SQLQueryParser parser = new SQLQueryParser(qaStep, "table1", emissioDatasourceName, null, null, sessionFactory);
        String query = annQuery.createAnnualQuery();
        System.out.println("The final query is : " + query);
        dropVersionDataFromTable();
        remove(dataset);
        //String expected = "CREATE TABLE emissions.table1 AS " + userQuery.toUpperCase();
        
        //assertEquals(expected, query);
    }
    
    public void testOfQueryThroughRunner() throws Exception {
        QAStep qaStep = new QAStep();
        qaStep.setName("Step1");
        DbServer dbServer = dbServerFactory.getDbServer();
        //String emissioDatasourceName = dbServer().getEmissionsDatasource().getName();
        //this will set the properties...
        //new ExImServiceImpl(emf(), dbServer, sessionFactory);
        SQLAnnualQueryRunner runner = new SQLAnnualQueryRunner(dbServer, sessionFactory, qaStep);
        //runner.query();
        runner.run();
        //runner.query(dbServer, qaStep, tableName);
        dbServer.disconnect();
    }
    
    /*public void testOfNonsummaryQueryThroughRunner() throws Exception {
        QAStep qaStep = new QAStep();
        qaStep.setName("Step1");
        DbServer dbServer = dbServerFactory.getDbServer();
        //String emissioDatasourceName = dbServer().getEmissionsDatasource().getName();
        //this will set the properties...
        //new ExImServiceImpl(emf(), dbServer, sessionFactory);
        SQLQAAnnualNonsummaryQueryRunner runner = new SQLQAAnnualNonsummaryQueryRunner(dbServer, sessionFactory, qaStep);
        //runner.query();
        runner.run();
        //runner.query(dbServer, qaStep, tableName);
        dbServer.disconnect();
    }*/
    
    private EmfDataset dataset(int datasetId, String tableName) {
        EmfDataset dataset = new EmfDataset();
        dataset.setName("TEST");
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
