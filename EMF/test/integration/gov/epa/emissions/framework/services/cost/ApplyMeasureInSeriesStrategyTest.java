package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.commons.db.postgres.PostgresDbUpdate;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.services.EmfDbServer;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.data.QAStepResult;
import gov.epa.emissions.framework.services.persistence.EmfDatabaseSetup;
import gov.epa.emissions.framework.services.qa.QADAO;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

//import org.hibernate.Session;

public class ApplyMeasureInSeriesStrategyTest extends ApplyMeasureInSeriesStrategyTestBase {

    private double percentDiff = 1e-2;
    
    public void testShouldRunMaxEmsRedStrategyWithNonpointDataAndFilterOnAllMeasureClasses() throws Exception {
        ControlStrategy strategy = null;
        ControlStrategyInputDataset inputDataset = setInputDataset("ORL nonpoint");
        
        ResultSet rs = null;
        Connection cn = null;
        try {
            ControlMeasureClass[] cmcs = {};
//            ControlMeasureClass[] cmcs = {(ControlMeasureClass)load(ControlMeasureClass.class, "Known"),
//                    (ControlMeasureClass)load(ControlMeasureClass.class, "Emerging")};
            strategy = controlStrategy(inputDataset, "CS_test_case__" + Math.round(Math.random() * 1000), pm10Pollutant(), cmcs);

            strategy = (ControlStrategy) load(ControlStrategy.class, strategy.getName());

            runStrategy(strategy);

            //get detailed result dataset
            ControlStrategyResult result = getControlStrategyResult(strategy);
            Dataset detailedResultDataset = result.getDetailedResultDataset();
            String tableName = detailedResultDataset.getInternalSources()[0].getTable();

            cn = dbServer().getEmissionsDatasource().getConnection();
            Statement stmt = cn.createStatement(
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);

            //make sure 15 records come back...
            rs = stmt.executeQuery("SELECT count(*) FROM "+ EmfDbServer.EMF_EMISSIONS_SCHEMA + "." + tableName);
            rs.next();
            assertTrue("make sure there are 21 records in the summary results." + rs.getInt(1), rs.getInt(1) == 21);

            //make sure this shows up, this would be applied for this scc/fips...
            rs = stmt.executeQuery("SELECT * FROM "+ EmfDbServer.EMF_EMISSIONS_SCHEMA + "." + tableName 
                    + " where scc = '2302002100' and cm_abbrev='PCHRBESP'");
            assertTrue("SCC = 2302002100 and CM = PCHRBESP, use this cm...", rs.first());

            //make sure nothing shows up, assigned different pollutant (PM2.5) for same SCC...
            rs = stmt.executeQuery("SELECT * FROM "+ EmfDbServer.EMF_EMISSIONS_SCHEMA + "." + tableName 
                    + " where scc = '2104008000' and fips = '37019' and poll ='PM2.5'");
            assertTrue("assigned different pollutant for same SCC", !rs.first());

            //make sure inv entry has the right numbers, there are NO locale specific measures for this entry...
            //check SCC = 2104008000 FIPS = 37029 inv entry
            rs = stmt.executeQuery("SELECT * FROM "+ EmfDbServer.EMF_EMISSIONS_SCHEMA + "." + tableName 
                    + " where scc = '2104008000' and fips = '37029' and cm_abbrev = 'PRESWDCSTV'");
            rs.next();
            assertTrue("SCC = 2104008000 FIPS = 37029 reduction = 88.2", Math.abs(rs.getDouble("percent_reduction") - 88.2)/88.2 < percentDiff);
            assertTrue("SCC = 2104008000 FIPS = 37029 annual cost = 35017100", Math.abs(rs.getDouble("annual_cost") - 35017100)/35017100 < percentDiff);
            assertTrue("SCC = 2104008000 FIPS = 37029 emis reduction = 17640", Math.abs(rs.getDouble("emis_reduction") - 17640)/17640 < percentDiff);

            //make sure inv entry has the right numbers, this a locale (37015) specific measure for this entry...
            //check SCC = 2104008000 FIPS = 37029 inv entry
            rs = stmt.executeQuery("SELECT * FROM "+ EmfDbServer.EMF_EMISSIONS_SCHEMA + "." + tableName 
                    + " where scc = '2801500000' and fips = '37015'");
            rs.next();
            assertTrue("SCC = 2801500000 FIPS = 37015 reduction = 63", Math.abs(rs.getDouble("percent_reduction") - 63)/63 < percentDiff);
            assertTrue("SCC = 2801500000 FIPS = 37015 annual cost = 37553698.05", Math.abs(rs.getDouble("annual_cost") - 37553698.05)/37553698.05 < percentDiff);
            assertTrue("SCC = 2801500000 FIPS = 37015 emis reduction = 8820", Math.abs(rs.getDouble("emis_reduction") - 8820)/8820 < percentDiff);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (rs != null) rs.close();
            if (cn != null) cn.close();
            dropTables(strategy);
            removeData();
        }
    }

    public void testShouldRunMaxEmsRedStrategyWithNonpointDataAndFilterOnKnownMeasureClass() throws Exception {
        ControlStrategy strategy = null;
        ControlStrategyInputDataset inputDataset = setInputDataset("ORL nonpoint");
        
        ResultSet rs = null;
        Connection cn = null;
        try {
            ControlMeasureClass[] cmcs = {(ControlMeasureClass)load(ControlMeasureClass.class, "Known")};
            strategy = controlStrategy(inputDataset, "CS_test_case__" + Math.round(Math.random() * 1000), pm10Pollutant(), cmcs);

            strategy = (ControlStrategy) load(ControlStrategy.class, strategy.getName());

            runStrategy(strategy);

            //get detailed result dataset
            ControlStrategyResult result = getControlStrategyResult(strategy);
            Dataset detailedResultDataset = result.getDetailedResultDataset();
            String tableName = detailedResultDataset.getInternalSources()[0].getTable();

            cn = dbServer().getEmissionsDatasource().getConnection();
            Statement stmt = cn.createStatement(
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);

            //make sure 15 records come back...
            rs = stmt.executeQuery("SELECT count(*) FROM "+ EmfDbServer.EMF_EMISSIONS_SCHEMA + "." + tableName);
            rs.next();
            assertTrue("make sure there are 18 records in the summary results." + rs.getInt(1), rs.getInt(1) == 18);

            //make sure this shows up, this would be applied for this scc/fips...
            rs = stmt.executeQuery("SELECT * FROM "+ EmfDbServer.EMF_EMISSIONS_SCHEMA + "." + tableName 
                    + " where scc = '2302002100' and cm_abbrev='PCHRBESP'");
            assertTrue("SCC = 2302002100 and CM = PCHRBESP, use this cm...", rs.first());

            //make sure nothing shows up, assigned different pollutant (PM2.5) for same SCC...
            rs = stmt.executeQuery("SELECT * FROM "+ EmfDbServer.EMF_EMISSIONS_SCHEMA + "." + tableName 
                    + " where scc = '2104008000' and fips = '37019' and poll ='PM2.5'");
            assertTrue("assigned different pollutant for same SCC", !rs.first());

            //make sure inv entry has the right numbers, there are NO locale specific measures for this entry...
            //check SCC = 2104008000 FIPS = 37029 inv entry
            rs = stmt.executeQuery("SELECT * FROM "+ EmfDbServer.EMF_EMISSIONS_SCHEMA + "." + tableName 
                    + " where scc = '2104008000' and fips = '37029' and cm_abbrev = 'PRESWDCSTV'");
            rs.next();
            assertTrue("SCC = 2104008000 FIPS = 37029 reduction = 88.2", Math.abs(rs.getDouble("percent_reduction") - 88.2)/88.2 < percentDiff);
            assertTrue("SCC = 2104008000 FIPS = 37029 annual cost = 70034200", Math.abs(rs.getDouble("annual_cost") - 70034200)/70034200 < percentDiff);
            assertTrue("SCC = 2104008000 FIPS = 37029 emis reduction = 17640", Math.abs(rs.getDouble("emis_reduction") - 35280)/35280 < percentDiff);

            //make sure inv entry has the right numbers, this a locale (37015) specific measure for this entry...
            //check SCC = 2104008000 FIPS = 37029 inv entry
            rs = stmt.executeQuery("SELECT * FROM "+ EmfDbServer.EMF_EMISSIONS_SCHEMA + "." + tableName 
                    + " where scc = '2801500000' and fips = '37015'");
            rs.next();
            assertTrue("SCC = 2801500000 FIPS = 37015 reduction = 63", Math.abs(rs.getDouble("percent_reduction") - 63)/63 < percentDiff);
            assertTrue("SCC = 2801500000 FIPS = 37015 annual cost = 37553698.05", Math.abs(rs.getDouble("annual_cost") - 37553698.05)/37553698.05 < percentDiff);
            assertTrue("SCC = 2801500000 FIPS = 37015 emis reduction = 8820", Math.abs(rs.getDouble("emis_reduction") - 8820)/8820 < percentDiff);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (rs != null) rs.close();
            if (cn != null) cn.close();
            dropTables(strategy);
            removeData();
        }
    }

    public void testShouldRunMaxEmsRedStrategyWithNonpointDataAndFilterOnKnownAndEmergingMeasureClasses() throws Exception {
        ControlStrategy strategy = null;
        ControlStrategyInputDataset inputDataset = setInputDataset("ORL nonpoint");
        
        ResultSet rs = null;
        Connection cn = null;
        try {
            ControlMeasureClass[] cmcs = {(ControlMeasureClass)load(ControlMeasureClass.class, "Known"),
                    (ControlMeasureClass)load(ControlMeasureClass.class, "Emerging")};
            strategy = controlStrategy(inputDataset, "CS_test_case__" + Math.round(Math.random() * 1000), pm10Pollutant(), cmcs);

            strategy = (ControlStrategy) load(ControlStrategy.class, strategy.getName());

            runStrategy(strategy);

            //get detailed result dataset
            ControlStrategyResult result = getControlStrategyResult(strategy);
            Dataset detailedResultDataset = result.getDetailedResultDataset();
            String tableName = detailedResultDataset.getInternalSources()[0].getTable();

            cn = dbServer().getEmissionsDatasource().getConnection();
            Statement stmt = cn.createStatement(
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);

            //make sure 15 records come back...
            rs = stmt.executeQuery("SELECT count(*) FROM "+ EmfDbServer.EMF_EMISSIONS_SCHEMA + "." + tableName);
            rs.next();
            assertTrue("make sure there are 21 records in the summary results.", rs.getInt(1) == 21);

            //make sure this shows up, this would be applied for this scc/fips...
            rs = stmt.executeQuery("SELECT * FROM "+ EmfDbServer.EMF_EMISSIONS_SCHEMA + "." + tableName 
                    + " where scc = '2302002100' and cm_abbrev='PCHRBESP'");
            assertTrue("SCC = 2302002100 and CM = PCHRBESP, use this cm...", rs.first());

            //make sure nothing shows up, assigned different pollutant (PM2.5) for same SCC...
            rs = stmt.executeQuery("SELECT * FROM "+ EmfDbServer.EMF_EMISSIONS_SCHEMA + "." + tableName 
                    + " where scc = '2104008000' and fips = '37019' and poll ='PM2.5'");
            assertTrue("assigned different pollutant for same SCC", !rs.first());

            //make sure inv entry has the right numbers, there are NO locale specific measures for this entry...
            //check SCC = 2104008000 FIPS = 37029 inv entry
            rs = stmt.executeQuery("SELECT * FROM "+ EmfDbServer.EMF_EMISSIONS_SCHEMA + "." + tableName 
                    + " where scc = '2104008000' and fips = '37029' and cm_abbrev = 'PRESWDCSTV'");
            rs.next();
            assertTrue("SCC = 2104008000 FIPS = 37029 reduction = 88.2", Math.abs(rs.getDouble("percent_reduction") - 88.2)/88.2 < percentDiff);
            assertTrue("SCC = 2104008000 FIPS = 37029 annual cost = 35017100", Math.abs(rs.getDouble("annual_cost") - 35017100)/35017100 < percentDiff);
            assertTrue("SCC = 2104008000 FIPS = 37029 emis reduction = 17640", Math.abs(rs.getDouble("emis_reduction") - 17640)/17640 < percentDiff);

            //make sure inv entry has the right numbers, this a locale (37015) specific measure for this entry...
            //check SCC = 2104008000 FIPS = 37029 inv entry
            rs = stmt.executeQuery("SELECT * FROM "+ EmfDbServer.EMF_EMISSIONS_SCHEMA + "." + tableName 
                    + " where scc = '2801500000' and fips = '37015'");
            rs.next();
            assertTrue("SCC = 2801500000 FIPS = 37015 reduction = 63", Math.abs(rs.getDouble("percent_reduction") - 63)/63 < percentDiff);
            assertTrue("SCC = 2801500000 FIPS = 37015 annual cost = 37553698.05", Math.abs(rs.getDouble("annual_cost") - 37553698.05)/37553698.05 < percentDiff);
            assertTrue("SCC = 2801500000 FIPS = 37015 emis reduction = 8820", Math.abs(rs.getDouble("emis_reduction") - 8820)/8820 < percentDiff);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (rs != null) rs.close();
            if (cn != null) cn.close();
            dropTables(strategy);
            removeData();
        }
    }

    public void testShouldRunMaxEmsRedStrategyWithNonpointDataAndFilterOnEmergingMeasureClass() throws Exception {
        ControlStrategy strategy = null;
        ControlStrategyInputDataset inputDataset = setInputDataset("ORL nonpoint");
        
        ResultSet rs = null;
        Connection cn = null;
        try {
            ControlMeasureClass[] cmcs = {(ControlMeasureClass)load(ControlMeasureClass.class, "Emerging")};
            strategy = controlStrategy(inputDataset, "CS_test_case__" + Math.round(Math.random() * 1000), pm10Pollutant(), cmcs);

            strategy = (ControlStrategy) load(ControlStrategy.class, strategy.getName());

            runStrategy(strategy);

            //get detailed result dataset
            ControlStrategyResult result = getControlStrategyResult(strategy);
            Dataset detailedResultDataset = result.getDetailedResultDataset();
            String tableName = detailedResultDataset.getInternalSources()[0].getTable();

            cn = dbServer().getEmissionsDatasource().getConnection();
            Statement stmt = cn.createStatement(
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);

            //make sure 15 records come back...
            rs = stmt.executeQuery("SELECT count(*) FROM "+ EmfDbServer.EMF_EMISSIONS_SCHEMA + "." + tableName);
            rs.next();
            assertTrue("make sure there are 3 records in the summary results.", rs.getInt(1) == 3);

            //make sure inv entry has the right numbers, there are NO locale specific measures for this entry...
            //check SCC = 2104008000 FIPS = 37029 inv entry
            rs = stmt.executeQuery("SELECT * FROM "+ EmfDbServer.EMF_EMISSIONS_SCHEMA + "." + tableName 
                    + " where scc = '2104008000' and fips = '37029'");
            rs.next();
            assertTrue("SCC = 2104008000 FIPS = 37029 reduction = 50", Math.abs(rs.getDouble("percent_reduction") - 50)/50 < percentDiff);
            assertTrue("SCC = 2104008000 FIPS = 37029 annual cost = 36438495.61", Math.abs(rs.getDouble("annual_cost") - 36438495.61)/36438495.61 < percentDiff);
            assertTrue("SCC = 2104008000 FIPS = 37029 emis reduction = 20000", Math.abs(rs.getDouble("emis_reduction") - 20000)/20000 < percentDiff);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (rs != null) rs.close();
            if (cn != null) cn.close();
            dropTables(strategy);
            removeData();
        }
    }

    public void testShouldRunMaxEmsRedStrategyWithNonpointDataAndFilterOnSpecificMeasures() throws Exception {
        ControlStrategy strategy = null;
        ControlStrategyInputDataset inputDataset = setInputDataset("ORL nonpoint");
        
        ResultSet rs = null;
        Connection cn = null;
        try {
            ControlStrategyMeasure[] controlStrategyMeasures = new ControlStrategyMeasure[2];
            ControlStrategyMeasure controlStrategyMeasure = new ControlStrategyMeasure((LightControlMeasure)load(LightControlMeasure.class, "Bale Stack/Propane Burning; Agricultural Burning"));
            controlStrategyMeasure.setRulePenetration(75.0);
            controlStrategyMeasure.setRuleEffectiveness(100.0);
            controlStrategyMeasure.setApplyOrder(1.0);
            controlStrategyMeasures[0] = controlStrategyMeasure;
            controlStrategyMeasure = new ControlStrategyMeasure((LightControlMeasure)load(LightControlMeasure.class, "ESP for Commercial Cooking; Conveyorized Charbroilers"));
            controlStrategyMeasure.setRulePenetration(75.0);
            controlStrategyMeasure.setRuleEffectiveness(100.0);
            controlStrategyMeasure.setApplyOrder(1.0);
            controlStrategyMeasures[1] = controlStrategyMeasure;
            strategy = controlStrategy(inputDataset, "CS_test_case__" + Math.round(Math.random() * 1000), pm10Pollutant(), controlStrategyMeasures);

            strategy = (ControlStrategy) load(ControlStrategy.class, strategy.getName());

            runStrategy(strategy);

            //get detailed result dataset
            ControlStrategyResult result = getControlStrategyResult(strategy);
            Dataset detailedResultDataset = result.getDetailedResultDataset();
            String tableName = detailedResultDataset.getInternalSources()[0].getTable();

            cn = dbServer().getEmissionsDatasource().getConnection();
            Statement stmt = cn.createStatement(
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);

            //make sure 15 records come back...
            rs = stmt.executeQuery("SELECT count(*) FROM "+ EmfDbServer.EMF_EMISSIONS_SCHEMA + "." + tableName);
            rs.next();
            assertTrue("make sure there are 4 records in the summary results.", rs.getInt(1) == 4);

            //make sure inv entry has the right numbers...
            //check SCC = 2302002100 FIPS = 37013 inv entry
            rs = stmt.executeQuery("SELECT * FROM "+ EmfDbServer.EMF_EMISSIONS_SCHEMA + "." + tableName 
                    + " where scc = '2302002100' and fips = '37005'");
            rs.next();
            assertTrue("SCC = 2302002100 FIPS = 37005 reduction = 13.88", Math.abs(rs.getDouble("percent_reduction") - 13.88)/13.88 < percentDiff);
            assertTrue("SCC = 2302002100 FIPS = 37005 annual cost = 7712102.28", Math.abs(rs.getDouble("annual_cost") - 7712102.28)/7712102.28 < percentDiff);
            assertTrue("SCC = 2302002100 FIPS = 37005 emis reduction = 1110", Math.abs(rs.getDouble("emis_reduction") - 1110)/1110 < percentDiff);

            //make sure inv entry has the right numbers...
            //check SCC = 2801500000 FIPS = 37029 inv entry
            rs = stmt.executeQuery("SELECT * FROM "+ EmfDbServer.EMF_EMISSIONS_SCHEMA + "." + tableName 
                    + " where scc = '2801500000' and fips = '37015'");
            rs.next();
            assertTrue("SCC = 2801500000 FIPS = 37015 reduction = 47.25", Math.abs(rs.getDouble("percent_reduction") - 47.25)/47.25 < percentDiff);
            assertTrue("SCC = 2801500000 FIPS = 37015 annual cost = 28165273.58", Math.abs(rs.getDouble("annual_cost") - 28165273.5375)/28165273.5375 < percentDiff);
            assertTrue("SCC = 2801500000 FIPS = 37015 emis reduction = 6615", Math.abs(rs.getDouble("emis_reduction") - 6615)/6615 < percentDiff);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (rs != null) rs.close();
            if (cn != null) cn.close();
            dropTables(strategy);
            removeData();
        }
    }

    public void testShouldRunMaxEmsRedStrategyWithNonRoadData() throws Exception {
        ControlStrategy strategy = null;
        ControlStrategyInputDataset inputDataset = setInputDataset("ORL nonroad");
        
        ResultSet rs = null;
        Connection cn = null;
        try {
            strategy = controlStrategy(inputDataset, "CS_test_case__" + Math.round(Math.random() * 1000), pm10Pollutant());

            strategy = (ControlStrategy) load(ControlStrategy.class, strategy.getName());

            runStrategy(strategy);

            //get detailed result dataset
            ControlStrategyResult result = getControlStrategyResult(strategy);
            Dataset detailedResultDataset = result.getDetailedResultDataset();
            String tableName = detailedResultDataset.getInternalSources()[0].getTable();

            cn = dbServer().getEmissionsDatasource().getConnection();
            Statement stmt = cn.createStatement(
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);

            //make sure 15 records come back...
            rs = stmt.executeQuery("SELECT count(*) FROM "+ EmfDbServer.EMF_EMISSIONS_SCHEMA + "." + tableName);
            rs.next();
            assertTrue("make sure there are 3 records in the summary results." + rs.getInt(1), rs.getInt(1) == 3);

            //make sure inv entry has the right numbers...
            //check SCC = 2302002100 FIPS = 37013 inv entry
            rs = stmt.executeQuery("SELECT * FROM "+ EmfDbServer.EMF_EMISSIONS_SCHEMA + "." + tableName 
                    + " where scc = '2294000270' and fips = '37001'");
            rs.next();
            assertTrue("SCC = 2294000270 FIPS = 37001 reduction = 71.1" + rs.getDouble("percent_reduction"), Math.abs(rs.getDouble("percent_reduction") - 71.1)/71.1 < percentDiff);
            assertTrue("SCC = 2294000270 FIPS = 37001 annual cost = 7.02655E-07 " + rs.getDouble("annual_cost"), Math.abs(rs.getDouble("annual_cost") - 7.02655E-07)/7.02655E-07 < percentDiff);
            assertTrue("SCC = 2294000270 FIPS = 37001 emis reduction = 2.37705E-09", Math.abs(rs.getDouble("emis_reduction") - 2.37705E-09)/2.37705E-09 < percentDiff);

            //make sure inv entry has the right numbers...
            //check SCC = 2801500000 FIPS = 37029 inv entry
            rs = stmt.executeQuery("SELECT * FROM "+ EmfDbServer.EMF_EMISSIONS_SCHEMA + "." + tableName 
                    + " where scc = '2801500130' and fips = '37001'");
            rs.next();
            assertTrue("SCC = 2801500130 FIPS = 37001 reduction = 63", Math.abs(rs.getDouble("percent_reduction") - 63)/63 < percentDiff);
            assertTrue("SCC = 2801500130 FIPS = 37001 annual cost = 4.927410E-04", Math.abs(rs.getDouble("annual_cost") - 4.927410E-04)/4.927410E-04 < percentDiff);
            assertTrue("SCC = 2801500130 FIPS = 37001 emis reduction = 1.15727E-07", Math.abs(rs.getDouble("emis_reduction") - 1.15727E-07)/1.15727E-07 < percentDiff);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (rs != null) rs.close();
            if (cn != null) cn.close();
            dropTables(strategy);
            removeData();
        }
    }

    public void testShouldRunMaxEmsRedStrategyWithOnRoadData() throws Exception {
        ControlStrategy strategy = null;
        ControlStrategyInputDataset inputDataset = setInputDataset("ORL onroad");
        
        ResultSet rs = null;
        Connection cn = null;
        try {
            strategy = controlStrategy(inputDataset, "CS_test_case__" + Math.round(Math.random() * 1000), pm10Pollutant());

            strategy = (ControlStrategy) load(ControlStrategy.class, strategy.getName());

            runStrategy(strategy);

            //get detailed result dataset
            ControlStrategyResult result = getControlStrategyResult(strategy);
            Dataset detailedResultDataset = result.getDetailedResultDataset();
            String tableName = detailedResultDataset.getInternalSources()[0].getTable();

            cn = dbServer().getEmissionsDatasource().getConnection();
            Statement stmt = cn.createStatement(
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);

            //make sure 5 records come back...
            rs = stmt.executeQuery("SELECT count(*) FROM "+ EmfDbServer.EMF_EMISSIONS_SCHEMA + "." + tableName);
            rs.next();
            assertTrue("make sure there are 5 records in the summary results." + rs.getInt(1), rs.getInt(1) == 5);

            //make sure inv entry has the right numbers...
            //check SCC = 2302002100 FIPS = 37013 POLL = PM10 inv entry
            rs = stmt.executeQuery("SELECT * FROM "+ EmfDbServer.EMF_EMISSIONS_SCHEMA + "." + tableName 
                    + " where scc = '2311010000' and fips = '37013' and poll='PM10'");
            rs.next();
            assertTrue("SCC = 2311010000 FIPS = 37013 reduction = 63 poll = PM10" + rs.getDouble("percent_reduction"), Math.abs(rs.getDouble("percent_reduction") - 63)/63 < percentDiff);
            assertTrue("SCC = 2311010000 FIPS = 37013 annual cost = 8.608595E-01 poll = PM10" + rs.getDouble("annual_cost"), Math.abs(rs.getDouble("annual_cost") - 8.608595E-01)/8.608595E-01 < percentDiff);
            assertTrue("SCC = 2311010000 FIPS = 37013 emis reduction = 1.732500E-04 poll = PM10", Math.abs(rs.getDouble("emis_reduction") - 1.732500E-04)/1.732500E-04 < percentDiff);

            //check SCC = 2302002100 FIPS = 37013 POLL = NOX inv entry
            rs = stmt.executeQuery("SELECT * FROM "+ EmfDbServer.EMF_EMISSIONS_SCHEMA + "." + tableName 
                    + " where scc = '2311010000' and fips = '37013' and poll='NOX'");
            rs.next();
            assertTrue("SCC = 2311010000 FIPS = 37013 reduction = 70 poll = NOX" + rs.getDouble("percent_reduction"), Math.abs(rs.getDouble("percent_reduction") - 70)/70 < percentDiff);
            assertTrue("SCC = 2311010000 FIPS = 37013 annual cost = 2.536202E+03 poll = NOX" + rs.getDouble("annual_cost"), Math.abs(rs.getDouble("annual_cost") - 2.536202E+03)/2.536202E+03 < percentDiff);
            assertTrue("SCC = 2311010000 FIPS = 37013 emis reduction = 1.750000E+00 poll = NOX", Math.abs(rs.getDouble("emis_reduction") - 1.750000E+00)/1.750000E+00 < percentDiff);

            //check SCC = 2801500000 FIPS = 37029 POLL = PM10 inv entry
            rs = stmt.executeQuery("SELECT * FROM "+ EmfDbServer.EMF_EMISSIONS_SCHEMA + "." + tableName 
                    + " where scc = '2296000000' and fips = '37006' and poll='PM10'");
            rs.next();
            assertTrue("SCC = 2296000000 FIPS = 37006 reduction = 68 poll = PM10", Math.abs(rs.getDouble("percent_reduction") - 68)/68 < percentDiff);
            assertTrue("SCC = 2296000000 FIPS = 37006 annual cost = 4.959112E+02 poll = PM10", Math.abs(rs.getDouble("annual_cost") - 4.959112E+02)/4.959112E+02 < percentDiff);
            assertTrue("SCC = 2296000000 FIPS = 37006 emis reduction = 6.690724E-01 poll = PM10", Math.abs(rs.getDouble("emis_reduction") - 6.690724E-01)/6.690724E-01 < percentDiff);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (rs != null) rs.close();
            if (cn != null) cn.close();
            dropTables(strategy);
            removeData();
        }
    }

    public void testShouldRunMaxEmsRedStrategyWithNonpointDataAndFilterOnAllMeasureClassesAndCreateControlledInv() throws Exception {
        ControlStrategy strategy = null;
        ControlStrategyInputDataset inputDataset = setInputDataset("ORL nonpoint");
        
        ResultSet rs = null;
        Connection cn = null;
        Connection cn2 = null;
        String tableName2 = "";
        ControlStrategyResult result = null;
        try {
            ControlMeasureClass[] cmcs = {};
//            ControlMeasureClass[] cmcs = {(ControlMeasureClass)load(ControlMeasureClass.class, "Known"),
//                    (ControlMeasureClass)load(ControlMeasureClass.class, "Emerging")};
            String strategyName = "CS_test_case__" + Math.round(Math.random() * 10000);
            strategy = controlStrategy(inputDataset, strategyName, pm10Pollutant(), cmcs);

            strategy = (ControlStrategy) load(ControlStrategy.class, strategy.getName());

            runStrategy(strategy);

//            session.flush();
//            session.clear();

            //get detailed result dataset
            result = getControlStrategyResult(strategy);
            Dataset detailedResultDataset = result.getDetailedResultDataset();
            String tableName = detailedResultDataset.getInternalSources()[0].getTable();

//            session.flush();
//            session.clear();

            cn = dbServer.getEmissionsDatasource().getConnection();
            Statement stmt = cn.createStatement(
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);

            //make sure 15 records come back...
            rs = stmt.executeQuery("SELECT count(*) FROM "+ EmfDbServer.EMF_EMISSIONS_SCHEMA + "." + tableName);
            rs.next();

            assertTrue("make sure there are 21 records in the summary results.", rs.getInt(1) == 21);

            //make sure this shows up, this would be applied for this scc/fips...
            rs = stmt.executeQuery("SELECT * FROM "+ EmfDbServer.EMF_EMISSIONS_SCHEMA + "." + tableName 
                    + " where scc = '2302002100' and cm_abbrev='PCHRBESP'");
            assertTrue("SCC = 2302002100 and CM = PCHRBESP, use this cm...", rs.first());

            //make sure nothing shows up, assigned different pollutant (PM2.5) for same SCC...
            rs = stmt.executeQuery("SELECT * FROM "+ EmfDbServer.EMF_EMISSIONS_SCHEMA + "." + tableName 
                    + " where scc = '2104008000' and fips = '37019' and poll ='PM2.5'");
            assertTrue("assigned different pollutant for same SCC", !rs.first());

            //make sure inv entry has the right numbers, there are NO locale specific measures for this entry...
            //check SCC = 2104008000 FIPS = 37029 inv entry
            rs = stmt.executeQuery("SELECT * FROM "+ EmfDbServer.EMF_EMISSIONS_SCHEMA + "." + tableName 
                    + " where scc = '2104008000' and fips = '37029' and cm_abbrev = 'PRESWDCSTV'");
            rs.next();
            assertTrue("SCC = 2104008000 FIPS = 37029 reduction = 88.2", Math.abs(rs.getDouble("percent_reduction") - 88.2)/88.2 < percentDiff);
            assertTrue("SCC = 2104008000 FIPS = 37029 annual cost = 35017100", Math.abs(rs.getDouble("annual_cost") - 35017100)/35017100 < percentDiff);
            assertTrue("SCC = 2104008000 FIPS = 37029 emis reduction = 17640", Math.abs(rs.getDouble("emis_reduction") - 17640)/17640 < percentDiff);

            //make sure inv entry has the right numbers, this a locale (37015) specific measure for this entry...
            //check SCC = 2104008000 FIPS = 37029 inv entry
            rs = stmt.executeQuery("SELECT * FROM "+ EmfDbServer.EMF_EMISSIONS_SCHEMA + "." + tableName 
                    + " where scc = '2801500000' and fips = '37015'");
            rs.next();
            assertTrue("SCC = 2801500000 FIPS = 37015 reduction = 63", Math.abs(rs.getDouble("percent_reduction") - 63)/63 < percentDiff);
            assertTrue("SCC = 2801500000 FIPS = 37015 annual cost = 37553698.05", Math.abs(rs.getDouble("annual_cost") - 37553698.05)/37553698.05 < percentDiff);
            assertTrue("SCC = 2801500000 FIPS = 37015 emis reduction = 8820", Math.abs(rs.getDouble("emis_reduction") - 8820)/8820 < percentDiff);

            //create the controlled inventory for this strategy run....
            createControlledInventory(strategy, result);

            //reload
            result = getControlStrategyResult(strategy);

            tableName2 = 
                result.getControlledInventoryDataset().getInternalSources()[0].getTable();
//            result.getControlledInventoryDataset().getName().replaceAll("ControlledInventory", "CSINVEN");

            cn2 = dbServer.getEmissionsDatasource().getConnection();
            stmt = cn2.createStatement(
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);

            rs = stmt.executeQuery("SELECT count(*) FROM "+ EmfDbServer.EMF_EMISSIONS_SCHEMA + "." + tableName2);
            rs.next();
            assertTrue("make sure there are 31 records in the controlled inventory results. ", rs.getInt(1) == 31);

/*
 * FIXME
 
                //make sure nothing shows up, this would not be the best control measure for this scc/fips...
//                rs = stmt.executeQuery("SELECT * FROM "+ EmfDbServer.EMF_EMISSIONS_SCHEMA + "." + tableName2 
//                        + " where scc = '2302002100' and cm_abbrev='PCHRBESP'");
//                assertTrue("SCC = 2302002100 and CM = PCHRBESP, don't use this cm, not the max reduction cm...", !rs.first());
            
                //make sure nothing shows up, assigned different pollutant (PM2.5) for same SCC...
                rs = stmt.executeQuery("SELECT * FROM "+ EmfDbServer.EMF_EMISSIONS_SCHEMA + "." + tableName2 
                        + " where scc = '2104008000' and fips = '37019' and poll ='PM2.5'");
                assertTrue("assigned different pollutant for same SCC", !rs.first());
            
                //make sure inv entry has the right numbers, there are NO locale specific measures for this entry...
                //check SCC = 2104008000 FIPS = 37029 inv entry
                rs = stmt.executeQuery("SELECT * FROM "+ EmfDbServer.EMF_EMISSIONS_SCHEMA + "." + tableName2 
                        + " where scc = '2104008000' and fips = '37029'");
                rs.next();
                assertTrue("SCC = 2104008000 FIPS = 37029 reduction = 88.2", Math.abs(rs.getDouble("percent_reduction") - 88.2)/88.2 < tolerance);
                assertTrue("SCC = 2104008000 FIPS = 37029 annual cost = 70034226.09", Math.abs(rs.getDouble("annual_cost") - 70034226.09)/70034226.09 < tolerance);
                assertTrue("SCC = 2104008000 FIPS = 37029 emis reduction = 35280", Math.abs(rs.getDouble("emis_reduction") - 35280)/35280 < tolerance);
            
                //make sure inv entry has the right numbers, this a locale (37015) specific measure for this entry...
                //check SCC = 2104008000 FIPS = 37029 inv entry
                rs = stmt.executeQuery("SELECT * FROM "+ EmfDbServer.EMF_EMISSIONS_SCHEMA + "." + tableName2 
                        + " where scc = '2801500000' and fips = '37015'");
                rs.next();
                assertTrue("SCC = 2801500000 FIPS = 37015 reduction = 63", Math.abs(rs.getDouble("percent_reduction") - 63)/63 < tolerance);
                assertTrue("SCC = 2801500000 FIPS = 37015 annual cost = 37553698.05", Math.abs(rs.getDouble("annual_cost") - 37553698.05)/37553698.05 < tolerance);
                assertTrue("SCC = 2801500000 FIPS = 37015 emis reduction = 8820", Math.abs(rs.getDouble("emis_reduction") - 8820)/8820 < tolerance);
*/            
            
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (rs != null) rs.close();
            if (cn != null) cn.close();
            if (cn2 != null) cn2.close();
            dropTables(strategy);
            removeData();
        }
    }

    public void testShouldRunMaxEmsRedStrategyWithPointDataAndFilterOnAllMeasureClasses() throws Exception {
        ControlStrategy strategy = null;
        ControlStrategyInputDataset inputDataset = setInputDataset("ORL point");
        
        ResultSet rs = null;
        Connection cn = null;
        Connection cn2 = null;
        ControlStrategyResult result = null;
        String tableName2 = "";
        try {
            strategy = controlStrategy(inputDataset, "CS_test_case__" + Math.round(Math.random() * 1000), pm10Pollutant());

            strategy = (ControlStrategy) load(ControlStrategy.class, strategy.getName());

            runStrategy(strategy);

            //get detailed result dataset
            result = getControlStrategyResult(strategy);
            Dataset detailedResultDataset = result.getDetailedResultDataset();
            String tableName = detailedResultDataset.getInternalSources()[0].getTable();

            cn = dbServer().getEmissionsDatasource().getConnection();
            Statement stmt = cn.createStatement(
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);

            //make sure 10 records come back...
            rs = stmt.executeQuery("SELECT count(*) FROM "+ EmfDbServer.EMF_EMISSIONS_SCHEMA + "." + tableName);
            rs.next();
            assertTrue("make sure there are 10 records in the summary results. " + rs.getInt(1), rs.getInt(1) == 10);

            /*
            //make sure inv entry has the right numbers, there are NO locale specific measures for this entry...
            //check SCC = 2801500000 FIPS = 37119 inv entry
            rs = stmt.executeQuery("SELECT * FROM "+ EmfDbServer.EMF_EMISSIONS_SCHEMA + "." + tableName 
                    + " where scc = '2801500000' and fips = '37119' and plantid = '0001' and pointid='0001' and stackid='1' and segment='1'");
            rs.next();
            assertTrue("SCC = 2801500000 FIPS = 37119 reduction = 63", Math.abs(rs.getDouble("percent_reduction") - 63)/63 < tolerance);
            assertTrue("SCC = 2801500000 FIPS = 37119 annual cost = 2.603046E+04", Math.abs(rs.getDouble("annual_cost") - 2.603046E+04)/2.603046E+04 < tolerance);
            assertTrue("SCC = 2801500000 FIPS = 37119 emis reduction = 6.113609E+00", Math.abs(rs.getDouble("emis_reduction") - 6.113609E+00)/6.113609E+00 < tolerance);

            //make sure inv entry has the right numbers, this a locale (37015) specific measure for this entry...
            //check SCC = 2104008010 FIPS = 37067 inv entry
            //also make sure the right control eff was used, this point already had a cm eff...
            rs = stmt.executeQuery("SELECT * FROM "+ EmfDbServer.EMF_EMISSIONS_SCHEMA + "." + tableName 
                    + " where scc = '2104008010' and fips = '37067' and plantid = '00466' and pointid='001' and stackid='1' and segment='1'");
            rs.next();
            assertTrue("SCC = 2104008010 FIPS = 37067 reduction = 88.2", Math.abs(rs.getDouble("percent_reduction") - 88.2)/88.2 < tolerance);
            assertTrue("SCC = 2104008010 FIPS = 37067 annual cost = 3.699558E+04 ", Math.abs(rs.getDouble("annual_cost") - 3.699558E+04)/3.699558E+04 < tolerance);
            assertTrue("SCC = 2104008010 FIPS = 37067 emis reduction = 1.863666E+01", Math.abs(rs.getDouble("emis_reduction") - 1.863666E+01)/1.863666E+01 < tolerance);

            //make sure inv entry has the right numbers, this a locale (37015) specific measure for this entry...
            //check SCC = 2104008010 FIPS = 37111 inv entry
            //also make sure the right control eff was used, this point already had a more eff cm ...
            rs = stmt.executeQuery("SELECT * FROM "+ EmfDbServer.EMF_EMISSIONS_SCHEMA + "." + tableName 
                    + " where scc = '2104008010' and fips = '37111' and plantid = '00778' and pointid='001' and stackid='2' and segment='1'");//00778 001 1 1 
            rs.next();
            assertTrue("SCC = 2104008010 FIPS = 37111 reduction = 99 " + rs.getDouble("percent_reduction"), Math.abs(rs.getDouble("percent_reduction") - 99)/99 < tolerance);
            assertTrue("SCC = 2104008010 FIPS = 37111 annual cost = 8.093505E+01", Math.abs(rs.getDouble("annual_cost") - 8.093505E+01)/8.093505E+01 < tolerance);
            assertTrue("SCC = 2104008010 FIPS = 37111 emis reduction = 4.576374E-02", Math.abs(rs.getDouble("emis_reduction") - 4.576374E-02)/4.576374E-02 < tolerance);
*/
            rs.close();

            //create the controlled inventory for this strategy run....
            createControlledInventory(strategy, result);

            //reload
            result = getControlStrategyResult(strategy);

            tableName2 = result.getControlledInventoryDataset().getInternalSources()[0].getTable();
            
            cn2 = new EmfDatabaseSetup(config()).getDbServer().getEmissionsDatasource().getConnection();
            stmt = cn2.createStatement(
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);

            //make sure there are the corect amount of controlled inv records...
            rs = stmt.executeQuery("SELECT count(*) FROM "+ EmfDbServer.EMF_EMISSIONS_SCHEMA + "." + tableName2);
            rs.next();
            assertTrue("make sure there are 206 records in the summary results. " + rs.getInt(1), rs.getInt(1) == 206);

            //make sure no inv info has been updated...
            rs = stmt.executeQuery("SELECT * FROM "+ EmfDbServer.EMF_EMISSIONS_SCHEMA + "." + tableName2 
                    + " where scc = '2294000270' and fips = '37067' and poll ='PM2.5'");
//            assertTrue("assigned different pollutant for same SCC", rs.getDouble("reff") == 0 && rs.getDouble("reff") == 0);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (rs != null) rs.close();
            if (cn != null) cn.close();
            if (cn2 != null) cn2.close();
            dropTables(strategy);
            removeData();
        }
    }

    public void testShouldRunMaxEmsRedStrategyWithTwoPointDatasetsAndFilterOnAllMeasureClasses() throws Exception {
        ControlStrategy strategy = null;
        ControlStrategyInputDataset inputDataset = setInputDataset("ORL point");
        ControlStrategyInputDataset inputDataset2 = setInputDataset("ORL point");
        
        ResultSet rs = null;
        Connection cn = null;
        Connection cn2 = null;
        ControlStrategyResult result = null;
        String tableName2 = "";
        try {
            strategy = controlStrategy(new ControlStrategyInputDataset[] {inputDataset, inputDataset2}, "CS_test_case__" + Math.round(Math.random() * 1000), pm10Pollutant());

            strategy = (ControlStrategy) load(ControlStrategy.class, strategy.getName());

            runStrategy(strategy);

            //get detailed result dataset
            result = getControlStrategyResult(strategy);
            Dataset detailedResultDataset = result.getDetailedResultDataset();
            String tableName = detailedResultDataset.getInternalSources()[0].getTable();

            cn = dbServer().getEmissionsDatasource().getConnection();
            Statement stmt = cn.createStatement(
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);

            //make sure 10 records come back...
            rs = stmt.executeQuery("SELECT count(*) FROM "+ EmfDbServer.EMF_EMISSIONS_SCHEMA + "." + tableName);
            rs.next();
            assertTrue("make sure there are 10 records in the summary results. " + rs.getInt(1), rs.getInt(1) == 10);

            /*
            //make sure inv entry has the right numbers, there are NO locale specific measures for this entry...
            //check SCC = 2801500000 FIPS = 37119 inv entry
            rs = stmt.executeQuery("SELECT * FROM "+ EmfDbServer.EMF_EMISSIONS_SCHEMA + "." + tableName 
                    + " where scc = '2801500000' and fips = '37119' and plantid = '0001' and pointid='0001' and stackid='1' and segment='1'");
            rs.next();
            assertTrue("SCC = 2801500000 FIPS = 37119 reduction = 63", Math.abs(rs.getDouble("percent_reduction") - 63)/63 < tolerance);
            assertTrue("SCC = 2801500000 FIPS = 37119 annual cost = 2.603046E+04", Math.abs(rs.getDouble("annual_cost") - 2.603046E+04)/2.603046E+04 < tolerance);
            assertTrue("SCC = 2801500000 FIPS = 37119 emis reduction = 6.113609E+00", Math.abs(rs.getDouble("emis_reduction") - 6.113609E+00)/6.113609E+00 < tolerance);

            //make sure inv entry has the right numbers, this a locale (37015) specific measure for this entry...
            //check SCC = 2104008010 FIPS = 37067 inv entry
            //also make sure the right control eff was used, this point already had a cm eff...
            rs = stmt.executeQuery("SELECT * FROM "+ EmfDbServer.EMF_EMISSIONS_SCHEMA + "." + tableName 
                    + " where scc = '2104008010' and fips = '37067' and plantid = '00466' and pointid='001' and stackid='1' and segment='1'");
            rs.next();
            assertTrue("SCC = 2104008010 FIPS = 37067 reduction = 88.2", Math.abs(rs.getDouble("percent_reduction") - 88.2)/88.2 < tolerance);
            assertTrue("SCC = 2104008010 FIPS = 37067 annual cost = 3.699558E+04 ", Math.abs(rs.getDouble("annual_cost") - 3.699558E+04)/3.699558E+04 < tolerance);
            assertTrue("SCC = 2104008010 FIPS = 37067 emis reduction = 1.863666E+01", Math.abs(rs.getDouble("emis_reduction") - 1.863666E+01)/1.863666E+01 < tolerance);

            //make sure inv entry has the right numbers, this a locale (37015) specific measure for this entry...
            //check SCC = 2104008010 FIPS = 37111 inv entry
            //also make sure the right control eff was used, this point already had a more eff cm ...
            rs = stmt.executeQuery("SELECT * FROM "+ EmfDbServer.EMF_EMISSIONS_SCHEMA + "." + tableName 
                    + " where scc = '2104008010' and fips = '37111' and plantid = '00778' and pointid='001' and stackid='2' and segment='1'");//00778 001 1 1 
            rs.next();
            assertTrue("SCC = 2104008010 FIPS = 37111 reduction = 99 " + rs.getDouble("percent_reduction"), Math.abs(rs.getDouble("percent_reduction") - 99)/99 < tolerance);
            assertTrue("SCC = 2104008010 FIPS = 37111 annual cost = 8.093505E+01", Math.abs(rs.getDouble("annual_cost") - 8.093505E+01)/8.093505E+01 < tolerance);
            assertTrue("SCC = 2104008010 FIPS = 37111 emis reduction = 4.576374E-02", Math.abs(rs.getDouble("emis_reduction") - 4.576374E-02)/4.576374E-02 < tolerance);
*/
            rs.close();

            //create the controlled inventory for this strategy run....
            createControlledInventory(strategy, result);

            //reload
            result = getControlStrategyResult(strategy);

            tableName2 = result.getControlledInventoryDataset().getInternalSources()[0].getTable();
            
            cn2 = new EmfDatabaseSetup(config()).getDbServer().getEmissionsDatasource().getConnection();
            stmt = cn2.createStatement(
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);

            //make sure there are the corect amount of controlled inv records...
            rs = stmt.executeQuery("SELECT count(*) FROM "+ EmfDbServer.EMF_EMISSIONS_SCHEMA + "." + tableName2);
            rs.next();
            assertTrue("make sure there are 206 records in the summary results. " + rs.getInt(1), rs.getInt(1) == 206);

            //make sure no inv info has been updated...
            rs = stmt.executeQuery("SELECT * FROM "+ EmfDbServer.EMF_EMISSIONS_SCHEMA + "." + tableName2 
                    + " where scc = '2294000270' and fips = '37067' and poll ='PM2.5'");
//            assertTrue("assigned different pollutant for same SCC", rs.getDouble("reff") == 0 && rs.getDouble("reff") == 0);

            

            
            
            
            //create the controlled inventory for this strategy run....
            createControlledInventory(strategy, result);

            //reload
            result = getControlStrategyResult(strategy, 1);

            tableName2 = result.getControlledInventoryDataset().getInternalSources()[0].getTable();
            
            cn2 = new EmfDatabaseSetup(config()).getDbServer().getEmissionsDatasource().getConnection();
            stmt = cn2.createStatement(
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);

            //make sure there are the corect amount of controlled inv records...
            rs = stmt.executeQuery("SELECT count(*) FROM "+ EmfDbServer.EMF_EMISSIONS_SCHEMA + "." + tableName2);
            rs.next();
            assertTrue("make sure there are 206 records in the summary results. " + rs.getInt(1), rs.getInt(1) == 206);

            //make sure no inv info has been updated...
            rs = stmt.executeQuery("SELECT * FROM "+ EmfDbServer.EMF_EMISSIONS_SCHEMA + "." + tableName2 
                    + " where scc = '2294000270' and fips = '37067' and poll ='PM2.5'");
//            assertTrue("assigned different pollutant for same SCC", rs.getDouble("reff") == 0 && rs.getDouble("reff") == 0);

            

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (rs != null) rs.close();
            if (cn != null) cn.close();
            if (cn2 != null) cn2.close();
            dropTables(strategy);
            removeData();
        }
    }

    private void dropTables(ControlStrategy strategy) throws Exception {
        if (strategy != null) {
            for (int i = 0; i < strategy.getControlStrategyInputDatasets().length; i++) {
                //drop input dataset table
                dropTable(strategy.getControlStrategyInputDatasets()[i].getInputDataset().getInternalSources()[0].getTable(), dbServer.getEmissionsDatasource());
                //drop input inventory qa step tables
                dropQASummaryTables(strategy.getControlStrategyInputDatasets()[i].getInputDataset());
            }
            List results = new ControlStrategyDAO().getControlStrategyResults(strategy.getId(), session);
            for (int i = 0; i < results.size(); i++) {
                ControlStrategyResult result = (ControlStrategyResult)results.get(i);
                if (result != null) {
                    //drop detailed result table
                    dropTable(result.getDetailedResultDataset().getInternalSources()[0].getTable(), dbServer.getEmissionsDatasource());
                    //drop detailed result qa step tables
                    dropQASummaryTables((EmfDataset) result.getDetailedResultDataset());
                    //see if controlled inv was created, if so cleanup...
                    EmfDataset contInv = (EmfDataset) result.getControlledInventoryDataset();
                    if (contInv != null) {
                        //drop controlled inv table
                        dropTable(contInv.getInternalSources()[0].getTable(), dbServer.getEmissionsDatasource());
                        //drop controlled inv qa step tables
                        dropQASummaryTables(contInv);
                    }
                }
            }
        }
    }

    private void dropQASummaryTables(EmfDataset dataset) throws Exception {
        if (dataset == null) return;
        QADAO dao = new QADAO();
        QAStep[] steps =  dao.steps(dataset, session);
        for (int i = 0; i < steps.length; i++) {
            QAStepResult qaStepResult = dao.qaStepResult(steps[i], session);
            dropTable(qaStepResult.getTable(), dbServer().getEmissionsDatasource());
        }
    }

    private void removeData() throws Exception {
        dropAll("Scc");
        dropAll("QAStepResult");
        dropAll("QAStep");
        new PostgresDbUpdate().deleteAll("emf.input_datasets_control_strategies");
        dropAll("ControlStrategyResult");
        dropAll(EmfDataset.class);
        dropAll(Version.class);
        dropAll(Dataset.class);
        new PostgresDbUpdate().deleteAll("emf.control_strategy_measures");
        new PostgresDbUpdate().deleteAll("emf.aggregrated_efficiencyrecords");
        dropAll("EfficiencyRecord");
        dropAll(ControlMeasure.class);
        dropAll(ControlStrategy.class);
    }

    private Pollutant pm10Pollutant() {
        return (Pollutant) load(Pollutant.class, "PM10");
    }

//    public void testImportControlMeasures() throws EmfException, Exception {
//        importControlMeasures();
//    }
//    
}
