package gov.epa.emissions.framework.services.qa;

import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.data.QAProgram;
import gov.epa.emissions.commons.db.DataModifier;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.TableModifier;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.ServiceTestCase;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.data.QAStepResult;

import java.sql.SQLException;

public class SQLQueryParserTest extends ServiceTestCase {

    //Made extensive changes to test the new query with multiple versioned datasets.
    
    private String emissioDatasourceName;
    
    protected void addRecord(Datasource datasource, String table, String[] data) throws SQLException {
        DataModifier modifier = datasource.dataModifier();
        modifier.insertRow(table, data);
    }

    public void testShoudParseTheQueryWhichDoesNotContailTags() throws Exception {
        QAStep qaStep = new QAStep();
        qaStep.setName("Step1");
        String userQuery = "SELECT * FROM reference.pollutants";
        qaStep.setProgramArguments(userQuery);

        SQLQueryParser parser = new SQLQueryParser(qaStep, "table1", emissioDatasourceName, null, null, sessionFactory);
        String query = parser.parse();
        String expected = "CREATE TABLE emissions.table1 AS " + userQuery;
        assertEquals(expected, query);
    }

//    public void testShoudParseTheQueryWhichDoesNotContainMultipleTags() throws Exception {
//       QAStep qaStep = new QAStep();
//        qaStep.setName("Step1");
//        String userQuery = "SELECT * FROM reference.pollutants";
//        qaStep.setProgramArguments(userQuery);
//
//        SQLQueryParser parser = new SQLQueryParser(qaStep, "table1", emissioDatasourceName, null, null);
//        String query = parser.parse();
//        String expected = "CREATE TABLE emissions.table1 AS " + userQuery.toUpperCase();
//        
//        assertEquals(expected, query);
//    }
//    
    public void testShoudParseTheQueryWhichDoesContainsSimpleTag() throws Exception {
        String tableName = "table1";
        EmfDataset dataset = dataset(0, tableName);
        add(dataset);
        dataset = (EmfDataset)load(EmfDataset.class, dataset.getName());
        Version version = version(dataset.getId());
        Version version1 = version1(dataset.getId());
        addVersionEntryToVersionsTable(version);
        addVersionEntryToVersionsTable(version1);
        QAStep qaStep = new QAStep();
        qaStep.setName("Step1");
        qaStep.setDatasetId(dataset.getId());
        
        String userQuery = "SELECT * FROM $TABLE[1] e group by";
        
        // Alternate query to use in the client.
        // String userQuery = "select coalesce(fips.state_abbr,'AN UNSPECIFIED STATE ABBREVIATION') as state_abbr, coalesce(fips.state_name, 'AN UNSPECIFIED STATE NAME') as state_name, e.POLL, coalesce(e.mact, 'AN UNSPECIFIED MACT CODE') as mact_code, coalesce(m.mact_source_category, 'AN UNSPECIFIED CATEGORY NAME') as mact_source_category,e.scc, coalesce(scc.scc_description,'AN UNSPECIFIED DESCRIPTION') as scc_description, e.sic, coalesce(r.description,'AN UNSPECIFIED DESCRIPTION') as description, coalesce(p.pollutant_code_desc,'AN UNSPECIFIED DESCRIPTION') as pollutant_code_desc, coalesce(p.smoke_name,'AN UNSPECIFIED SMOKE NAME') as smoke_name, p.factor,p.voctog, p.species, coalesce(sum(ann_emis), 0) as ann_emis, coalesce(sum(avd_emis), 0) as avd_emis from $TABLE[1] e left outer join $DATASET_TABLE[TEST, 1] p on e.POLL=p.pollutant_code left outer join reference.fips on fips.state_county_fips = e.FIPS left outer join reference.mact_codes m on m.mact_code = e.mact left outer join reference.scc on scc.scc = e.scc left outer join reference.sic_codes r on r.sic = e.sic where fips.country_num = '0' and  e.POLL in ('593748','7487947','7439976','22967926','62384','202','201','200','199') group by fips.state_name, fips.state_abbr, e.POLL, p.pollutant_code_desc, p.smoke_name, p.factor, p.voctog, p.species, scc.scc_description, r.description, e.sic, e.scc, e.mact, m.mact_source_category order by e.mact, e.sic, e.scc, e.POLL";

        
        qaStep.setProgramArguments(userQuery);
        String qaStepOutputTable = "qa_table1";
        SQLQueryParser parser = new SQLQueryParser(qaStep, qaStepOutputTable, emissioDatasourceName, dataset, version, sessionFactory);
        String query = parser.parse();
        System.out.println("The input query is: " + userQuery);
        System.out.println("The final query is : " + query);
        String expected = "CREATE TABLE " + qualfiedName(qaStepOutputTable) + " AS SELECT * FROM "
                + qualfiedName(tableName)
                + " E  WHERE  (E.version IN (0) AND E.dataset_id="
                + dataset.getId() + ") " + " group by";
        dropVersionDataFromTable();
        remove(dataset);
        assertEquals(expected.toUpperCase(), query.toUpperCase());
    }

    public void testShoudParseTheQueryWhichDoesContainsTwoTagsWithComma() throws Exception {
        String tableName = "table1";
        EmfDataset dataset = dataset(0, tableName);
        add(dataset);
        dataset = (EmfDataset)load(EmfDataset.class, dataset.getName());
        Version version = version(dataset.getId());
        Version version1 = version1(dataset.getId());
        addVersionEntryToVersionsTable(version);
        addVersionEntryToVersionsTable(version1);
        QAStep qaStep = new QAStep();
        qaStep.setName("Step1");
        qaStep.setDatasetId(dataset.getId());
        
        String userQuery = "SELECT * FROM $TABLE[1] e, $DATASET_TABLE[TEST, 1] f WHERE POLL=p.cas group by POLL, P.descrptn ORDER BY POLL";
        
        qaStep.setProgramArguments(userQuery);
        String qaStepOutputTable = "qa_table1";
        SQLQueryParser parser = new SQLQueryParser(qaStep, qaStepOutputTable, emissioDatasourceName, dataset, version, sessionFactory);
        String query = parser.parse();
        System.out.println("The input query is: " + userQuery);
        System.out.println("The final query is : " + query);
        String expected = "CREATE TABLE " + qualfiedName(qaStepOutputTable) + " AS SELECT * FROM "
                + qualfiedName(tableName)
                + " E, emissions.table1 F WHERE POLL=P.CAS  AND  (E.version IN (0) AND E.dataset_id="
                + dataset.getId() + ") AND  (F.version IN (0) AND F.dataset_id=" + dataset.getId() + ")" + "  group by POLL, P.DESCRPTN ORDER BY POLL";

        dropVersionDataFromTable();
        remove(dataset);
        assertEquals(expected.toUpperCase(), query.toUpperCase());
    }
   

    public void testShoudParseTheQueryWhichDoesContainsTwoTagsWithoutComma() throws Exception {
        String tableName = "table1";
        EmfDataset dataset = dataset(0, tableName);
        add(dataset);
        dataset = (EmfDataset)load(EmfDataset.class, dataset.getName());
        Version version = version(dataset.getId());
        Version version1 = version1(dataset.getId());
        addVersionEntryToVersionsTable(version);
        addVersionEntryToVersionsTable(version1);
        QAStep qaStep = new QAStep();
        qaStep.setName("Step1");
        qaStep.setDatasetId(dataset.getId());
        
        String userQuery = "SELECT * FROM $TABLE[1] e $DATASET_TABLE[TEST, 1] f WHERE POLL=p.cas group by POLL, P.descrptn ORDER BY POLL";
        
        qaStep.setProgramArguments(userQuery);
        String qaStepOutputTable = "qa_table1";
        SQLQueryParser parser = new SQLQueryParser(qaStep, qaStepOutputTable, emissioDatasourceName, dataset, version, sessionFactory);
        String query = parser.parse();
        System.out.println("The input query is: " + userQuery);
        System.out.println("The final query is : " + query);
        String expected = "CREATE TABLE " + qualfiedName(qaStepOutputTable) + " AS SELECT * FROM "
                + qualfiedName(tableName)
                + " E emissions.table1 F WHERE POLL=P.CAS  AND  (E.version IN (0) AND E.dataset_id="
                + dataset.getId() + ") AND  (F.version IN (0) AND F.dataset_id=" + dataset.getId() + ")" + "  group by POLL, P.DESCRPTN ORDER BY POLL";

        dropVersionDataFromTable();
        remove(dataset);
        assertEquals(expected.toUpperCase(), query.toUpperCase());
    }

    
    public void testShoudParseTheQueryWhichDoesContainsThreeTags() throws Exception {
        String tableName = "table1";
        EmfDataset dataset = dataset(0, tableName);
        add(dataset);
        dataset = (EmfDataset)load(EmfDataset.class, dataset.getName());
        Version version = version(dataset.getId());
        Version version1 = version1(dataset.getId());
        addVersionEntryToVersionsTable(version);
        addVersionEntryToVersionsTable(version1);
        QAStep qaStep = new QAStep();
        qaStep.setName("Step1");
        qaStep.setDatasetId(dataset.getId());
        
        String userQuery = "SELECT * FROM $TABLE[1] e left outer join $DATASET_TABLE[TEST, 1] f inner join $DATASET_TABLE_VERSION[TEST, 1, 0] k group by";

        // Alternate query to use in the client.
        //String userQuery = "SELECT * FROM $TABLE[1] e left outer join $DATASET_TABLE[TEST, 1] f inner join $DATASET_TABLE_VERSION[TEST, 1, 1] k group by";
        
        qaStep.setProgramArguments(userQuery);
        String qaStepOutputTable = "qa_table1";
        SQLQueryParser parser = new SQLQueryParser(qaStep, qaStepOutputTable, emissioDatasourceName, dataset, version, sessionFactory);
        String query = parser.parse();
        System.out.println("The input query is: " + userQuery);
        System.out.println("The final query is : " + query);
        String expected = "CREATE TABLE " + qualfiedName(qaStepOutputTable) + " AS SELECT * FROM "
                + qualfiedName(tableName)
                + " E left outer join emissions.table1 F INNER JOIN emissions.table1 K  WHERE  (E.version IN (0) AND E.dataset_id="
                + dataset.getId() + ") AND  (K.version IN (0) AND K.dataset_id=" + dataset.getId() + ") AND  (F.version IN (0) AND F.dataset_id=" + dataset.getId() + ")  group by";
  
        dropVersionDataFromTable();
        remove(dataset);
        assertEquals(expected.toUpperCase(), query.toUpperCase());
    }
    
    public void testShoudParseTheQueryWhichContainsQAStepTags() throws Exception {
        String tableName = "table1";
        EmfDataset dataset = dataset(0, tableName);
        add(dataset);
        dataset = (EmfDataset)load(EmfDataset.class, dataset.getName());
        Version version = version(dataset.getId());
        Version version1 = version1(dataset.getId());
        addVersionEntryToVersionsTable(version);
        addVersionEntryToVersionsTable(version1);
        
        QAProgram qaProgram = new QAProgram("Average day to Annual Summary");
        
        QAStep qaStep = new QAStep();
        qaStep.setName("Step1");
        qaStep.setProgram(qaProgram);
        qaStep.setDatasetId(dataset.getId());
        
        QAStepResult qaStepResult = new QAStepResult(qaStep);
        qaStepResult.setTable("QA_DSID603_V0_20070827122550Test1");
        qaStepResult.getTable();
        
        String userQuery = "SELECT fipsst, sum(ann_emis) as ann_emis, poll FROM ((select br.fipsst, be.poll, be.ann_emis from $DATASET_QASTEP[TEST, Average day to Annual Summary] as be) union all (select te.fipsst, te.name, te.ann_emis from $DATASET_QASTEP[TEST, Average day to Annual Summary] as te)) as ab group by ab.fipsst, ab.ann_emis, ab.poll order by ab.fipsst";
        
        //emissions.QA_DSID135_V0_20070824135517Summarize_by_US_State_and_Pollutant 
        //emissions.QA_DSID603_V0_20070827122550Test1

        // Alternate query to use in the client.
        // String userQuery = "select coalesce(fips.state_abbr,'AN UNSPECIFIED STATE ABBREVIATION') as state_abbr, coalesce(fips.state_name, 'AN UNSPECIFIED STATE NAME') as state_name, e.POLL, coalesce(e.mact, 'AN UNSPECIFIED MACT CODE') as mact_code, coalesce(m.mact_source_category, 'AN UNSPECIFIED CATEGORY NAME') as mact_source_category,e.scc, coalesce(scc.scc_description,'AN UNSPECIFIED DESCRIPTION') as scc_description, e.sic, coalesce(r.description,'AN UNSPECIFIED DESCRIPTION') as description, coalesce(p.pollutant_code_desc,'AN UNSPECIFIED DESCRIPTION') as pollutant_code_desc, coalesce(p.smoke_name,'AN UNSPECIFIED SMOKE NAME') as smoke_name, p.factor,p.voctog, p.species, coalesce(sum(ann_emis), 0) as ann_emis, coalesce(sum(avd_emis), 0) as avd_emis from $TABLE[1] e left outer join $DATASET_TABLE[TEST, 1] p on e.POLL=p.pollutant_code left outer join reference.fips on fips.state_county_fips = e.FIPS left outer join reference.mact_codes m on m.mact_code = e.mact left outer join reference.scc on scc.scc = e.scc left outer join reference.sic_codes r on r.sic = e.sic where fips.country_num = '0' and  e.POLL in ('593748','7487947','7439976','22967926','62384','202','201','200','199') group by fips.state_name, fips.state_abbr, e.POLL, p.pollutant_code_desc, p.smoke_name, p.factor, p.voctog, p.species, scc.scc_description, r.description, e.sic, e.scc, e.mact, m.mact_source_category order by e.mact, e.sic, e.scc, e.POLL";

        
        qaStep.setProgramArguments(userQuery);
        String qaStepOutputTable = "qa_step_table1";
        //String qaStepOutputTable = "qa_table1";
        SQLQueryParser parser = new SQLQueryParser(qaStep, qaStepOutputTable, emissioDatasourceName, dataset, version, sessionFactory);
        String query = parser.parse();
        System.out.println("The input query is: " + userQuery);
        System.out.println("The final query is : " + query);
        String expected = "CREATE TABLE " + qualfiedName(qaStepOutputTable) + " AS SELECT * FROM "
                + qualfiedName(tableName)
                + " E  WHERE  (E.version IN (0) AND E.dataset_id="
                + dataset.getId() + ") " + " group by";
        dropVersionDataFromTable();
        remove(dataset);
        assertEquals(expected.toUpperCase(), query.toUpperCase());
    }
    
  //Commented out for now until I get this combination working
    
   public void testShoudParseTheQueryWhichDoesContainsTagsOneThree() throws Exception {
        String tableName = "table1";
        EmfDataset dataset = dataset(0, tableName);
        add(dataset);
        dataset = (EmfDataset)load(EmfDataset.class, dataset.getName());
        Version version = version(dataset.getId());
        Version version1 = version1(dataset.getId());
        addVersionEntryToVersionsTable(version);
        addVersionEntryToVersionsTable(version1);
        QAStep qaStep = new QAStep();
        qaStep.setName("Step1");
        qaStep.setDatasetId(dataset.getId());
        
        String userQuery = "SELECT * FROM $TABLE[1] e left outer join $DATASET_TABLE_VERSION[TEST, 1, 1] k group by";
        
        //Test of query with alias removed
        //String userQuery = "SELECT * FROM $TABLE[1] group by";
        
        //Test of query with end tag removed
        //String userQuery = "SELECT * FROM $TABLE[1 e group by";
        
        qaStep.setProgramArguments(userQuery);
        String qaStepOutputTable = "qa_table1";
        SQLQueryParser parser = new SQLQueryParser(qaStep, qaStepOutputTable, emissioDatasourceName, dataset, version, sessionFactory);
        String query = parser.parse();
        System.out.println("The input query is: " + userQuery);
        System.out.println("The final query is : " + query);
        String expected = "CREATE TABLE " + qualfiedName(qaStepOutputTable) + " AS SELECT * FROM "
                + qualfiedName(tableName)
                + " E left outer join emissions.table1 k  WHERE  (E.version IN (0) AND E.dataset_id="
                + dataset.getId() + ") AND  (K.version IN (0,1) AND K.delete_versions NOT SIMILAR TO '(1|1,%|%,1,%|%,1)' AND K.dataset_id=" + dataset.getId() + ")" + "  group by";

          //(K.version IN (0,1) AND K.delete_versions NOT SIMILAR TO '(1|1,%|%,1,%|%,1)'

        
        dropVersionDataFromTable();
        remove(dataset);
        assertEquals(expected.toUpperCase(), query.toUpperCase());
    }

    public void testShoudThrowAnException_WhichIsMissingAnAlias() throws Exception {
        String tableName = "table1";
        EmfDataset dataset = dataset(0, tableName);
        add(dataset);
        dataset = (EmfDataset)load(EmfDataset.class, dataset.getName());
        Version version = version(dataset.getId());
        Version version1 = version1(dataset.getId());
        addVersionEntryToVersionsTable(version);
        addVersionEntryToVersionsTable(version1);
        QAStep qaStep = new QAStep();
        qaStep.setName("Step1");
        qaStep.setDatasetId(dataset.getId());
                
        //String userQuery = "SELECT * FROM $TABLE[1] group by";
        
        //Alternate queries -- with two tags.
        
        //String userQuery = "SELECT * FROM $TABLE[1], $DATASET_TABLE[TEST, 1] f WHERE POLL=p.cas group by POLL, P.descrptn ORDER BY POLL";

        // Variant of query with space between ']' and ,
        String userQuery = "SELECT * FROM $TABLE[1] , $DATASET_TABLE[TEST, 1] f WHERE POLL=p.cas group by POLL, P.descrptn ORDER BY POLL";

        // Variant of query with no alias in second tag 
        //String userQuery = "SELECT * FROM $TABLE[1], $DATASET_TABLE[TEST, 1] WHERE POLL=p.cas group by POLL, P.descrptn ORDER BY POLL";

        
        //String userQuery = "SELECT POLL, P.descrptn, SUM(ANN_EMIS) AS ANN_EMIS FROM $TABLE[1] e $DATASET_TABLE[TEST, 1] P WHERE POLL=p.cas group by POLL, P.descrptn ORDER BY POLL";

        
        qaStep.setProgramArguments(userQuery);
        String qaStepOutputTable = "qa_table1";
        SQLQueryParser parser = new SQLQueryParser(qaStep, qaStepOutputTable, emissioDatasourceName, dataset, version, sessionFactory);
        try {
            parser.parse();
            assertTrue("Should have thrown an exception", false);
        } catch (EmfException e) {
            assertEquals("A one-character alias value is expected after the ']'", e.getMessage());
        } finally {
            dropVersionDataFromTable();
            remove(dataset);
        }
    }

    public void testShoudThrowAnException_WhichIsMissingAnEndTag() throws Exception {
        String tableName = "table1";
        EmfDataset dataset = dataset(0, tableName);
        add(dataset);
        dataset = (EmfDataset)load(EmfDataset.class, dataset.getName());
        Version version = version(dataset.getId());
        Version version1 = version1(dataset.getId());
        addVersionEntryToVersionsTable(version);
        addVersionEntryToVersionsTable(version1);
        QAStep qaStep = new QAStep();
        qaStep.setName("Step1");
        qaStep.setDatasetId(dataset.getId());
                
        //String userQuery = "SELECT * FROM $TABLE[1 e group by";
        
        //Alternative query -- with multiple tags
        String userQuery = "SELECT POLL, P.descrptn, SUM(ANN_EMIS) AS ANN_EMIS  FROM $TABLE[1 e $DATASET_TABLE[INVTABLE_CAP_HG, 1] P WHERE POLL=p.cas group by POLL, P.descrptn ORDER BY POLL";

        qaStep.setProgramArguments(userQuery);
        String qaStepOutputTable = "qa_table1";
        
        SQLQueryParser parser = new SQLQueryParser(qaStep, qaStepOutputTable, emissioDatasourceName, dataset, version, sessionFactory);
        
        try {
            parser.parse();
            assertTrue("Should have thrown an exception", false);
        } catch (EmfException e) {
            assertEquals("The ']' is expected in the program arguments", e.getMessage());
        } finally {
            dropVersionDataFromTable();
            remove(dataset);
        }
    }

    public void testShoudThrowAnException_WhichHasAnInvalidDataset() throws Exception {
        String tableName = "table1";
        EmfDataset dataset = dataset(0, tableName);
        add(dataset);
        dataset = (EmfDataset)load(EmfDataset.class, dataset.getName());
        Version version = version(dataset.getId());
        Version version1 = version1(dataset.getId());
        addVersionEntryToVersionsTable(version);
        addVersionEntryToVersionsTable(version1);
        QAStep qaStep = new QAStep();
        qaStep.setName("Step1");
        qaStep.setDatasetId(dataset.getId());
        
        String userQuery = "SELECT POLL, P.descrptn, SUM(ANN_EMIS) AS ANN_EMIS  FROM $TABLE[1] e $DATASET_TABLE[abcde, 1] P WHERE POLL=p.cas group by POLL, P.descrptn ORDER BY POLL";

        // Alternative query -- for three tags
        
        //String userQuery = "SELECT * FROM $TABLE[1] e left outer join $DATASET_TABLE[TEST, 1] f inner join $DATASET_TABLE_VERSION[dsfdf, 1, 0] k group by";

        qaStep.setProgramArguments(userQuery);
        String qaStepOutputTable = "qa_table1";
        
        SQLQueryParser parser = new SQLQueryParser(qaStep, qaStepOutputTable, emissioDatasourceName, dataset, version, sessionFactory);
        
        try {
            parser.parse();
            assertTrue("Should have thrown an exception", false);
        } catch (EmfException e) {
            assertEquals("The dataset name must be valid", e.getMessage());
        } finally {
            dropVersionDataFromTable();
            remove(dataset);
        }
    }

    public void testShoudThrowAnException_WhichHasAnInvalidVersion() throws Exception {
        String tableName = "table1";
        EmfDataset dataset = dataset(0, tableName);
        add(dataset);
        dataset = (EmfDataset)load(EmfDataset.class, dataset.getName());
        Version version = version(dataset.getId());
        Version version1 = version1(dataset.getId());
        addVersionEntryToVersionsTable(version);
        addVersionEntryToVersionsTable(version1);
        QAStep qaStep = new QAStep();
        qaStep.setName("Step1");
        qaStep.setDatasetId(dataset.getId());
        
        //String userQuery = "SELECT POLL, P.descrptn, SUM(ANN_EMIS) AS ANN_EMIS  FROM $TABLE[1] e $DATASET_TABLE[abcde, 1] P WHERE POLL=p.cas group by POLL, P.descrptn ORDER BY POLL";

        // Alternative query -- for three tags
        
        String userQuery = "SELECT * FROM $TABLE[1] e left outer join $DATASET_TABLE[TEST, 1] f inner join $DATASET_TABLE_VERSION[TEST, 1, 10] k group by";

        qaStep.setProgramArguments(userQuery);
        String qaStepOutputTable = "qa_table1";
        
        SQLQueryParser parser = new SQLQueryParser(qaStep, qaStepOutputTable, emissioDatasourceName, dataset, version, sessionFactory);
        
        try {
            parser.parse();
            assertTrue("Should have thrown an exception", false);
        } catch (EmfException e) {
            assertEquals("The version name must be valid", e.getMessage());
        } finally {
            dropVersionDataFromTable();
            remove(dataset);
        }
    }

    
    public void testShoudThrowAnException_TableNoIsLessThanOne() throws Exception {
        String tableName = "table1";
        int datasetId = 0;
        EmfDataset dataset = dataset(datasetId, tableName);
        Version version = version(datasetId);

        QAStep qaStep = new QAStep();
        qaStep.setName("Step1");
        qaStep.setDatasetId(dataset.getId());
        String userQuery = "SELECT * FROM $TABLE[0] j group by";
        qaStep.setProgramArguments(userQuery);
        String qaStepOutputTable = "qa_table1";
        SQLQueryParser parser = new SQLQueryParser(qaStep, qaStepOutputTable, emissioDatasourceName, dataset, version, sessionFactory);
        try {
            parser.parse();
            assertTrue("Should have thrown an exception", false);
        } catch (EmfException e) {
            assertEquals("The table number should be greater or equal to one", e.getMessage());
        }
    }
    
    public void testShoudThrowAnException_TableNoIsMoreThanExistingNumberOfTables() throws Exception {
        String tableName = "table1";
        int datasetId = 0;
        EmfDataset dataset = dataset(datasetId, tableName);
        Version version = version(datasetId);

        QAStep qaStep = new QAStep();
        qaStep.setName("Step1");
        qaStep.setDatasetId(dataset.getId());
        String userQuery = "SELECT * FROM $TABLE[2] d group by";
        qaStep.setProgramArguments(userQuery);
        String qaStepOutputTable = "qa_table1";
        SQLQueryParser parser = new SQLQueryParser(qaStep, qaStepOutputTable, emissioDatasourceName, dataset, version, sessionFactory);
        try {
            parser.parse();
            assertTrue("Should have thrown an exception", false);
        } catch (EmfException e) {
            assertEquals("The table number is larger than the number of tables in the dataset", e.getMessage());
        }
    }
  
    public void testShoudThrowAnException_CannotConvertTableNoToInteger() throws Exception {
        String tableName = "table1";
        int datasetId = 0;
        EmfDataset dataset = dataset(datasetId, tableName);
        Version version = version(datasetId);

        QAStep qaStep = new QAStep();
        qaStep.setName("Step1");
        qaStep.setDatasetId(dataset.getId());
        String userQuery = "SELECT * FROM $TABLE[r] d group by";
        qaStep.setProgramArguments(userQuery);
        String qaStepOutputTable = "qa_table1";
        SQLQueryParser parser = new SQLQueryParser(qaStep, qaStepOutputTable, emissioDatasourceName, dataset, version, sessionFactory);
        try {
            parser.parse();
            assertTrue("Should have thrown an exception", false);
        } catch (EmfException e) {
            assertEquals("Could not convert the table number to an integer -" + qaStep.getProgramArguments(), e.getMessage());
        }
    }
    
    public void testShoudThrowAnException_WhichHasARemainingDollarSign() throws Exception {
        String tableName = "table1";
        EmfDataset dataset = dataset(0, tableName);
        add(dataset);
        dataset = (EmfDataset)load(EmfDataset.class, dataset.getName());
        Version version = version(dataset.getId());
        Version version1 = version1(dataset.getId());
        addVersionEntryToVersionsTable(version);
        addVersionEntryToVersionsTable(version1);
        QAStep qaStep = new QAStep();
        qaStep.setName("Step1");
        qaStep.setDatasetId(dataset.getId());
    
        String userQuery = "SELECT * FROM $TABLE[1] a, $DATSET_TABLE[3] g, $DATASET_TABLE[TEST, 1] f WHERE POLL=p.cas group by POLL";
        
        qaStep.setProgramArguments(userQuery);
        String qaStepOutputTable = "qa_table1";
        
        SQLQueryParser parser = new SQLQueryParser(qaStep, qaStepOutputTable, emissioDatasourceName, dataset, version, sessionFactory);
       
        try {
            parser.parse();
            assertTrue("Should have thrown an exception", false);
        } catch (EmfException e) {
            assertEquals("There is still a  '$' in the program arguments", e.getMessage());
        } finally {
            dropVersionDataFromTable();
            remove(dataset);
        }
    }

    
    //Commented out for now until I see if this is realy needed.
    
   /* public void testShoudThrowAnException_VersionNoIsLessThanZero() throws Exception {
        String tableName = "table1";
        int datasetId = 0;
        EmfDataset dataset = dataset(datasetId, tableName);
        Version version = version(datasetId);

        QAStep qaStep = new QAStep();
        qaStep.setName("Step1");
        qaStep.setDatasetId(dataset.getId());
        String userQuery = "SELECT * FROM $TABLE[1] e left outer join $DATASET_TABLE[TEST, 1] f inner join $DATASET_TABLE_VERSION[TEST, 1, 2] k group by ";

        qaStep.setProgramArguments(userQuery);
        String qaStepOutputTable = "qa_table1";
        SQLQueryParser parser = new SQLQueryParser(qaStep, qaStepOutputTable, emissioDatasourceName, dataset, version, sessionFactory);
        try {
            parser.parse();
            assertTrue("Should have thrown an exception", false);
        } catch (EmfException e) {
            assertEquals("The version number should be greater or equal to zero", e.getMessage());
        }
    }*/

    
    
//
//    public void testShouldExpandTwoTagsOnTheQuery() throws Exception {
//        String tableName = "table1";
//        int datasetId = 0;
//        EmfDataset dataset = dataset(datasetId, tableName);
//        Version version = version(datasetId);
//
//        QAStep qaStep = new QAStep();
//        qaStep.setName("Step1");
//        qaStep.setDatasetId(dataset.getId());
//        String userQuery = "SELECT * FROM $TABLE[1] WHERE $TABLE[1].scc=reference.scc.scc";
//        qaStep.setProgramArguments(userQuery);
//        String qaStepOutputTable = "qa_table1";
//        SQLQueryParser parser = new SQLQueryParser(qaStep, qaStepOutputTable, emissioDatasourceName, dataset, version, sessionFactory);
//        String query = parser.parse();
//        String expected = "CREATE TABLE "
//                + qualfiedName(qaStepOutputTable)
//                + " AS SELECT * FROM "
//                + qualfiedName(tableName)
//                + " WHERE emissions.table1.scc=reference.scc.scc AND version IN (0) AND  delete_versions NOT SIMILAR TO '(0|0,%|%,0,%|%,0)' AND dataset_id="
//                + dataset.getId() + " ";
//
//        assertEquals(expected.toUpperCase(), query.toUpperCase());
//
//    }
//
//public void testShouldExpandWithWHEREClause() throws Exception {
//        String tableName = "table1";
//        int datasetId = 0;
//        EmfDataset dataset = dataset(datasetId, tableName);
//        Version version = version(datasetId);
//        
//        QAStep qaStep = new QAStep();
//        qaStep.setName("Step1");
//        qaStep.setDatasetId(dataset.getId());
//        String userQuery = "SELECT * FROM $TABLE[1] WHERE poll='NOX'";
//        qaStep.setProgramArguments(userQuery);
//        String qaStepOutputTable = "qa_table1";
//        SQLQueryParser parser = new SQLQueryParser(qaStep, qaStepOutputTable, emissioDatasourceName, dataset, version, sessionFactory);
//        String query = parser.parse();
//        String expected = "CREATE TABLE "
//                        + qualfiedName(qaStepOutputTable)
//                        + " AS SELECT * FROM "
//                        + qualfiedName(tableName)
//                        + " WHERE poll='NOX' AND version IN (0) AND  delete_versions NOT SIMILAR TO '(0|0,%|%,0,%|%,0)' AND dataset_id="
//                        + dataset.getId()+" ";
//        assertEquals(expected.toUpperCase(), query.toUpperCase());
//
//    }    public void testShouldExpandWithGROUPBYClause() throws Exception {
//        String tableName = "table1";
//        int datasetId = 0;
//        EmfDataset dataset = dataset(datasetId, tableName);
//        Version version = version(datasetId);
//
//        QAStep qaStep = new QAStep();
//        qaStep.setName("Step1");
//        qaStep.setDatasetId(dataset.getId());
//        String userQuery = "SELECT scc,sum(ANN_EMIS) FROM $TABLE[1] GROUP BY scc";
//        qaStep.setProgramArguments(userQuery);
//        String qaStepOutputTable = "qa_table1";
//        SQLQueryParser parser = new SQLQueryParser(qaStep, qaStepOutputTable, emissioDatasourceName, dataset, version, sessionFactory);
//        String query = parser.parse();
//
//        String expected = "CREATE TABLE " + qualfiedName(qaStepOutputTable) + " AS SELECT scc,sum(ANN_EMIS) FROM "
//                + qualfiedName(tableName)
//                + "  WHERE version IN (0) AND  delete_versions NOT SIMILAR TO '(0|0,%|%,0,%|%,0)' AND dataset_id="
//                + dataset.getId() + " GROUP BY scc";
//        assertEquals(expected.toUpperCase(), query.toUpperCase());
//
//    }
//
//    public void testShouldExpandWithGROUPBY_AND_WHEREClause() throws Exception {
//        String tableName = "table1";
//        int datasetId = 0;
//        EmfDataset dataset = dataset(datasetId, tableName);
//        Version version = version(datasetId);
//
//        QAStep qaStep = new QAStep();
//        qaStep.setName("Step1");
//        qaStep.setDatasetId(dataset.getId());
//        String userQuery = "SELECT scc,sum(ANN_EMIS) FROM $TABLE[1] WHERE Poll='NOx' GROUP BY scc";
//        qaStep.setProgramArguments(userQuery);
//        String qaStepOutputTable = "qa_table1";
//        SQLQueryParser parser = new SQLQueryParser(qaStep, qaStepOutputTable, emissioDatasourceName, dataset, version, sessionFactory);
//        String query = parser.parse();
//
//        String expected = "CREATE TABLE "
//                + qualfiedName(qaStepOutputTable)
//                + " AS SELECT scc,sum(ANN_EMIS) FROM "
//                + qualfiedName(tableName)
//                + " WHERE Poll='NOx'  AND version IN (0) AND  delete_versions NOT SIMILAR TO '(0|0,%|%,0,%|%,0)' AND dataset_id="
//                + dataset.getId() + " GROUP BY scc";
//        assertEquals(expected.toUpperCase(), query.toUpperCase());
//
//    }
//
//    public void testShouldExpandWithORDERBY_AND_WHEREClause() throws Exception {
//        String tableName = "table1";
//        int datasetId = 0;
//        EmfDataset dataset = dataset(datasetId, tableName);
//        Version version = version(datasetId);
//
//        QAStep qaStep = new QAStep();
//        qaStep.setName("Step1");
//        qaStep.setDatasetId(dataset.getId());
//        String userQuery = "SELECT * FROM $TABLE[1] WHERE Poll='NOx' ORDER BY ANN_EMIS";
//        qaStep.setProgramArguments(userQuery);
//        String qaStepOutputTable = "qa_table1";
//        SQLQueryParser parser = new SQLQueryParser(qaStep, qaStepOutputTable, emissioDatasourceName, dataset, version, sessionFactory);
//        String query = parser.parse();
//
//        String expected = "CREATE TABLE "
//                + qualfiedName(qaStepOutputTable)
//                + " AS SELECT * FROM "
//                + qualfiedName(tableName)
//                + " WHERE Poll='NOx'  AND version IN (0) AND  delete_versions NOT SIMILAR TO '(0|0,%|%,0,%|%,0)' AND dataset_id="
//                + dataset.getId() + " ORDER BY ANN_EMIS";
//        assertEquals(expected.toUpperCase(), query.toUpperCase());
//
//    }
//
//    public void testShouldExpandWithGROUPBY_ORDERBY_LIMIT_AND_WHEREClause() throws Exception {
//        String tableName = "table1";
//        int datasetId = 0;
//        EmfDataset dataset = dataset(datasetId, tableName);
//        Version version = version(datasetId);
//
//        QAStep qaStep = new QAStep();
//        qaStep.setName("Step1");
//        qaStep.setDatasetId(dataset.getId());
//        String userQuery = "SELECT SCC,SUM(ANN_EMIS) AS SUM_EMIS FROM $TABLE[1] WHERE Poll='NOx' GROUP BY SCC ORDER BY SUM_EMIS LIMIT 5";
//        qaStep.setProgramArguments(userQuery);
//        String qaStepOutputTable = "qa_table1";
//        SQLQueryParser parser = new SQLQueryParser(qaStep, qaStepOutputTable, emissioDatasourceName, dataset, version, sessionFactory);
//        String query = parser.parse();
//
//        String expected = "CREATE TABLE "
//                + qualfiedName(qaStepOutputTable)
//                + " AS SELECT SCC,SUM(ANN_EMIS) AS SUM_EMIS FROM "
//                + qualfiedName(tableName)
//                + " WHERE Poll='NOx'  AND version IN (0) AND  delete_versions NOT SIMILAR TO '(0|0,%|%,0,%|%,0)' AND dataset_id="
//                + dataset.getId() + " GROUP BY SCC ORDER BY SUM_EMIS LIMIT 5";
//        assertEquals(expected.toUpperCase(), query.toUpperCase());
//
//    }

    private String qualfiedName(String tableName) {
        return emissioDatasourceName + "." + tableName;
    }

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
    
    private Version version1(int datasetId) {
        Version version = new Version();
        version.setId(0);
        version.setDatasetId(datasetId);
        version.setVersion(1);
        version.setName("Changed Version");
        version.setPath("0");
        return version;
    }
    
    private String[] createVersionData(Version version) {
        return new String[] {
            "", "" + version.getDatasetId(), "" + version.getVersion(), version.getName(), version.getPath()   
        };
    }

    private void addVersionEntryToVersionsTable(Version version) throws Exception {
        TableModifier modifier = new TableModifier(dbSetup.getDbServer().getEmissionsDatasource(), "versions");
        modifier.insertOneRow(createVersionData(version));
    }

    private void dropVersionDataFromTable() throws Exception {
        TableModifier modifier = new TableModifier(dbSetup.getDbServer().getEmissionsDatasource(), "versions");
        modifier.dropAllData();
    }
    
    protected void doSetUp() throws Exception {
        emissioDatasourceName = "emissions";
    }

    protected void doTearDown() throws Exception {
 //
    }

}
