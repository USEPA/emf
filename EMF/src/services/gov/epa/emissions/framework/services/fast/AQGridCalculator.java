//package gov.epa.emissions.framework.services.fast;
//
//import gov.epa.emissions.commons.data.DatasetType;
//import gov.epa.emissions.commons.data.Keyword;
//import gov.epa.emissions.commons.data.Sector;
//import gov.epa.emissions.commons.db.Datasource;
//import gov.epa.emissions.commons.db.DbServer;
//import gov.epa.emissions.commons.db.postgres.PostgresSQLToShapeFile;
//import gov.epa.emissions.commons.db.version.Version;
//import gov.epa.emissions.commons.db.version.Versions;
//import gov.epa.emissions.commons.io.Column;
//import gov.epa.emissions.commons.io.ExporterException;
//import gov.epa.emissions.commons.io.TableFormat;
//import gov.epa.emissions.commons.io.VersionedQuery;
//import gov.epa.emissions.commons.io.orl.ORLPointFileFormat;
//import gov.epa.emissions.commons.io.temporal.VersionedTableFormat;
//import gov.epa.emissions.commons.security.User;
//import gov.epa.emissions.framework.client.meta.keywords.Keywords;
//import gov.epa.emissions.framework.services.DbServerFactory;
//import gov.epa.emissions.framework.services.EmfException;
//import gov.epa.emissions.framework.services.EmfProperty;
//import gov.epa.emissions.framework.services.ServiceTestCase;
//import gov.epa.emissions.framework.services.basic.UserDAO;
//import gov.epa.emissions.framework.services.cost.ControlStrategy;
//import gov.epa.emissions.framework.services.cost.controlStrategy.DatasetCreator;
//import gov.epa.emissions.framework.services.cost.controlStrategy.FileFormatFactory;
//import gov.epa.emissions.framework.services.data.DataCommonsDAO;
//import gov.epa.emissions.framework.services.data.DatasetDAO;
//import gov.epa.emissions.framework.services.data.EmfDataset;
//import gov.epa.emissions.framework.services.persistence.EmfDatabaseSetup;
//import gov.epa.emissions.framework.services.persistence.EmfPropertiesDAO;
//import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;
//import gov.epa.emissions.framework.services.persistence.LocalHibernateConfiguration;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.sql.Connection;
//import java.sql.ResultSet;
//import java.sql.ResultSetMetaData;
//import java.sql.SQLException;
//import java.sql.Statement;
//import java.sql.Types;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Properties;
//
//import javax.persistence.EntityManager;
//
//
//public class AQGridCalculator {
//
//    // private DbServerFactory dbServerFactory;
//    //
//    // private EntityManagerFactory entityManagerFactory;
//    //
//    // private User user;
//    //
//    // private static Log LOG = LogFactory.getLog(AQGridCalculator.class);
//
//    private EmfDataset invTable;
//
//    private Version invTableVersion;
//
////    private Map<String, FastCMAQResult> fastCMAQResultMap = new HashMap<String, FastCMAQResult>();
//
////    private TestServiceTestCase serviceTestCase;
//    
//    private EntityManagerFactory entityManagerFactory;
//
//    private DbServerFactory dbServerFactory;
//    
//    private DatasetDAO datasetDAO;
//    
//    private DatasetCreator creator;
//    
//    public AQGridCalculator() {
//        //
////        this.serviceTestCase = new TestServiceTestCase();
//        try {
////            this.serviceTestCase.setUp();
//            this.entityManagerFactory = entityManagerFactory();
//            this.dbServerFactory = dbServerFactory();
//            this.creator = new DatasetCreator(new ControlStrategy(), getUser("delvecch"), 
//                    entityManagerFactory, dbServerFactory,
//                    dbServerFactory.getDbServer().getEmissionsDatasource(), new Keywords(new Keyword[] { } ));
//        } catch (Exception e) {
//            // NOTE Auto-generated catch block
//            e.printStackTrace();
//        }
//        this.datasetDAO = new DatasetDAO(dbServerFactory);
//    }
//
//    private EntityManagerFactory entityManagerFactory() throws Exception {
//        String folder = "test";
//        File configFile = new File(folder, "postgres.conf");
//        LocalHibernateConfiguration config = new LocalHibernateConfiguration(configFile);
//        return new HibernateSessionFactory(config.factory());
//    }
//    
//    private DbServerFactory dbServerFactory() throws Exception {
//        String folder = "test";
//        File configFile = new File(folder, "postgres.conf");
//
//        if (!configFile.exists() || !configFile.isFile()) {
//            String error = "File: " + configFile + " does not exist. Please copy either of the two TEMPLATE files "
//                    + "(from " + configFile.getParent() + "), name it " + configFile.getName() + ", configure "
//                    + "it as needed, and rerun.";
//            throw new RuntimeException(error);
//        }
//
//        Properties properties = new Properties();
//        properties.load(new FileInputStream(configFile));
//        return new DbServerFactory(new EmfDatabaseSetup(properties));
//    }
//    
//
//
//    public AQGridCalculator(DbServerFactory dbServerFactory, EntityManagerFactory entityManagerFactory, User user) {
//        // this.dbServerFactory = dbServerFactory;
//        // this.entityManagerFactory = entityManagerFactory;
//    }
//
////    private Connection getConnection() {
////        Connection con = null;
////        try {
////            Class.forName("org.postgresql.Driver");
////            con = DriverManager.getConnection("jdbc:postgresql://localhost:5432/EMF?autoReconnect=true", "emf", "emf");
////        } catch (SQLException e) {
////            // NOTE Auto-generated catch block
////            e.printStackTrace();
////        } catch (ClassNotFoundException e) {
////            // NOTE Auto-generated catch block
////            e.printStackTrace();
////        }
////        return con;
////    }
//
//    public void go() throws Exception {
//        // DataSource ds = null;
//        DbServer dbServer = dbServerFactory.getDbServer();
//        Connection con = dbServer.getConnection();
//        Map<String, AQTransferCoefficient> transferCoefficientMap = new HashMap<String, AQTransferCoefficient>();// map
//                                                                                                                 // key
//                                                                                                                 // is
//                                                                                                                 // sector
//                                                                                                                 // + _
//                                                                                                                 // +
//                                                                                                                 // pollutant
//        // Map<String, FastCMAQResult> fastCMAQResultMap
//        // = new HashMap<String, FastCMAQResult>();//map key is sector + _ + cmaq pollutant
//
//        // load invtable and version
//        this.invTable = loadDataset("invtable_cap_hg");
//        this.invTableVersion = version(invTable, 6);
//
//        //
//        // double[][] emiss = new double[37][46];
//        // double[][] emiss2 = new double[37][46];
//        double[][] emiss = new double[36][45]; //zero based index
//        double[][] emiss2 = new double[36][45]; //zero based index
//
//        System.out.println("default conc values for grid " + System.currentTimeMillis());
//        for (int x = 1; x <= 36/* 45 */; x++) {
//            for (int y = 1; y <= 45/* 36 */; y++) {
//                emiss[x - 1][y - 1] = 0.0;
//                emiss2[x - 1][y - 1] = 0.0;
//            }
//        }
//
//        ResultSet rs = null;
//        Statement statement = null;
//
////        createShapeFile();
////        EmfDataset newPoint = addDataset("nonpt_2005ao_tox_detroit_CAP_nopfc" + "_x_point", getDatasetType(DatasetType.orlPointInventory), new VersionedTableFormat(new ORLPointFileFormat(dbServer.getSqlDataTypes()), dbServer.getSqlDataTypes()), "");
////        populatePointInvFromSMOKEGriddedSCCRpt(loadDataset("nonpt_2005ao_tox_detroit_CAP_nopfc_15may2009_v0"), 0, loadDataset("2020ac_det_link3_nonpt_det_scc_cell_4DET1"), 0, this.invTable, this.invTableVersion.getVersion(), newPoint);
//
////        EmfDataset newPoint = addDataset("nonpt_2005ao_tox_detroit_CAP_pfc" + "_x_point", getDatasetType(DatasetType.orlPointInventory), new VersionedTableFormat(new ORLPointFileFormat(dbServer.getSqlDataTypes()), dbServer.getSqlDataTypes()), "");
////        populatePointInvFromSMOKEGriddedSCCRpt(loadDataset("nonpt_2005ao_tox_detroit_CAP_pfc_15may2009_v0"), 0, loadDataset("2020ac_det_link3_nonpt_det_scc_cell_4DET1"), 0, this.invTable, this.invTableVersion.getVersion(), newPoint);
//
////        EmfDataset newPoint = addDataset("nonpt_2005ao_tox_detroit_HAP" + "_x_point", getDatasetType(DatasetType.orlPointInventory), new VersionedTableFormat(new ORLPointFileFormat(dbServer.getSqlDataTypes()), dbServer.getSqlDataTypes()), "");
////        populatePointInvFromSMOKEGriddedSCCRpt(loadDataset("nonpt_2005ao_tox_detroit_HAP_15may2009_v0"), 0, loadDataset("2020ac_det_link3_nonpt_det_scc_cell_4DET1"), 0, this.invTable, this.invTableVersion.getVersion(), newPoint);
//
////        EmfDataset newPoint = addDataset("alm_no_c3_cap2002v3" + "_x_point", getDatasetType(DatasetType.orlPointInventory), new VersionedTableFormat(new ORLPointFileFormat(dbServer.getSqlDataTypes()), dbServer.getSqlDataTypes()), "");
////        populatePointInvFromSMOKEGriddedSCCRpt(loadDataset("lm_no_c3_cap2002v3"), 0, loadDataset("2020ac_det_link3_lmb_alm_det_scc_cell_4DET1"), 0, this.invTable, this.invTableVersion.getVersion(), newPoint);
//
////        EmfDataset newPoint = addDataset("alm_no_c3_hap2002v4" + "_x_point", getDatasetType(DatasetType.orlPointInventory), new VersionedTableFormat(new ORLPointFileFormat(dbServer.getSqlDataTypes()), dbServer.getSqlDataTypes()), "");
////        populatePointInvFromSMOKEGriddedSCCRpt(loadDataset("lm_no_c3_hap2002v4_20feb2009_v0"), 0, loadDataset("2020ac_det_link3_lmb_alm_det_scc_cell_4DET1"), 0, this.invTable, this.invTableVersion.getVersion(), newPoint);
//
////        EmfDataset newPoint = addDataset("afdust_2002ad_xportfrac" + "_x_point", getDatasetType(DatasetType.orlPointInventory), new VersionedTableFormat(new ORLPointFileFormat(dbServer.getSqlDataTypes()), dbServer.getSqlDataTypes()), "");
////        populatePointInvFromSMOKEGriddedSCCRpt(loadDataset("afdust_2002ad_xportfrac_26sep2007_v0"), 0, loadDataset("2020ac_det_link3_nonpt_det_scc_cell_4DET1"), 0, this.invTable, this.invTableVersion.getVersion(), newPoint);
////
////        if (1 == 1) throw new Exception("Kill Program");
//        
//        long timing = System.currentTimeMillis();
//        EmfDataset griddedSectorSCCPollDataset = null;
//        //create new or get old Gridded Sector CMAQ-Inventory Pollutant Emission Ouput
//        if (1 == 1) 
//            griddedSectorSCCPollDataset = createGriddedSectorCMAQInventoryPollutantEmissionResult(new String[] {});
//        else
//            griddedSectorSCCPollDataset = loadDataset("base_nonpt_sectors_20100412");
//
//        //get transfer coefficients and put into a HashMap for later use.
//        transferCoefficientMap = getTransferCoefficients();
//
//        //Create list of FastCMAQResult objects for calculating air quality
//        List<FastCMAQPollutantAirQualityEmissionResult> results = new ArrayList<FastCMAQPollutantAirQualityEmissionResult>();
//
//        System.out.println("load up conc values for grid " + timing);
//        String griddedSectorCMAQInventoryPollTableName = griddedSectorSCCPollDataset.getInternalSources()[0].getTable();
//        VersionedQuery griddedSectorCMAQInventoryPollVersionedQuery = new VersionedQuery(version(griddedSectorSCCPollDataset, 0), "fo");
//        String query = "select fo.sector, fo.cmaq_pollutant, fo.inventory_pollutant, fo.x, fo.y, fo.factor, fo.emission, fo.transfer_coefficient from " + qualifiedTable(griddedSectorCMAQInventoryPollTableName, dbServer.getEmissionsDatasource()) + " as fo where " + griddedSectorCMAQInventoryPollVersionedQuery.query() + " /*and fo.sector in ('ptipm','ptnonipm','onroad','nonroad')*/ order by fo.sector, fo.cmaq_pollutant, fo.inventory_pollutant;";
//        try {
//            statement = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
//            rs = statement.executeQuery(query);
//            int y, x;
//            double emissionValue;
//            float factor;
//            String sector = "";
//            String cmaqPollutant = "";
//            String inventoryPollutant = "";
//            String prevSector = "";
//            String prevCmaqPollutant = "";
//            String prevInventoryPollutant = "";
//            String transferCoeff = "";
//            FastCMAQPollutantAirQualityEmissionResult fastCMAQResult = null;
//            // FastCMAQInventoryPollutantResult fastCMAQInventoryPollutantResult;
//
//            double[][] emission = new double[36][45];
//            FastInventoryPollutantAirQualityEmissionResult result = null;
//            while (rs.next()) {
//                sector = rs.getString(1);
//                cmaqPollutant = rs.getString(2);
//                inventoryPollutant = rs.getString(3);
//                x = rs.getInt(4);
//                y = rs.getInt(5);
//                factor = rs.getFloat(6);
//                emissionValue = rs.getDouble(7);
//                transferCoeff = rs.getString(8);
//                // if (fastCMAQResultMap.containsKey(sector + "_" + cmaqPollutant)) {
//                // fastCMAQResult = fastCMAQResultMap.get(sector + "_" + cmaqPollutant);
//                // } else {
//                // fastCMAQResult = new FastCMAQResult(sector, cmaqPollutant);
//                // // fastCMAQInventoryPollutantResult = new FastCMAQInventoryPollutantResult();
//                // }
//
//                if (!sector.equals(prevSector) || !cmaqPollutant.equals(prevCmaqPollutant)) {
//                    if (result != null) {
//                        result.setEmission(emission);
//                        fastCMAQResult.addCmaqInventoryPollutantResults(result);
//                        // fastCMAQResultMap.put(sector + "_" + cmaqPollutant, fastCMAQResult);
//                        results.add(fastCMAQResult);
//                    }
//                    emission = new double[36][45];
//                    fastCMAQResult = new FastCMAQPollutantAirQualityEmissionResult(sector, cmaqPollutant);
//                    result = new FastInventoryPollutantAirQualityEmissionResult(inventoryPollutant, factor, transferCoeff, 36, 45);
//                } else if (!inventoryPollutant.equals(prevInventoryPollutant)) {
//                    if (result != null) {
//                        result.setEmission(emission);
//                        fastCMAQResult.addCmaqInventoryPollutantResults(result);
//                    }
//                    emission = new double[36][45];
//                    result = new FastInventoryPollutantAirQualityEmissionResult(inventoryPollutant, factor, transferCoeff, 36, 45);
//                }
//
//                prevSector = sector;
//                prevCmaqPollutant = cmaqPollutant;
//                prevInventoryPollutant = inventoryPollutant;
//                emission[x - 1][y - 1] = emissionValue;
//
//                // fastCMAQResultMap.put(sector + "_" + cmaqPollutant, fastCMAQResult);
//            }
//            // get last item in there too.
//            result.setEmission(emission);
//            fastCMAQResult.addCmaqInventoryPollutantResults(result);
//            // fastCMAQResultMap.put(sector + "_" + cmaqPollutant, fastCMAQResult);
//            results.add(fastCMAQResult);
//            rs.close();
//            rs = null;
//            statement.close();
//            statement = null;
//            con.close();
//            con = null;
//        } catch (SQLException e) {
//            throw new EmfException("Could not execute query -" + query + "\n" + e.getMessage());
//        } finally {
//            if (rs != null) {
//                try {
//                    rs.close();
//                } catch (SQLException e) { //
//                }
//                rs = null;
//            }
//            if (statement != null) {
//                try {
//                    statement.close();
//                } catch (SQLException e) { //
//                }
//                statement = null;
//            }
//            if (con != null) {
//                try {
//                    con.close();
//                } catch (SQLException e) { //
//                }
//                con = null;
//            }
//        }
//        System.out.println("finished loading up conc values for grid = " + (System.currentTimeMillis() - timing));
//        timing = System.currentTimeMillis();
//        for (FastCMAQPollutantAirQualityEmissionResult cMAQResult : results) {
//            String sector = cMAQResult.getSector();
//            System.out.println("result sector = " + sector + ", pollutant = " + cMAQResult.getCmaqPollutant());
//            for (FastInventoryPollutantAirQualityEmissionResult result : cMAQResult.getCmaqInventoryPollutantResults()) {
//                double[][] emission = result.getEmission();
//                double[][] airQuality = new double[36][45];
//
//                if (sector.equals("ptnonipm") || sector.equals("ptipm") || sector.equals("point") || sector.equals("othpt"))
//                    sector = "point";
//                else
//                    sector = "all nonpoint";
//
//                System.out.println("start to calc affect for each cell on every other cell "
//                        + System.currentTimeMillis());
//
//                AQTransferCoefficient transferCoefficient = transferCoefficientMap.get(sector.toLowerCase() + "_"
//                        + result.getTranferCoefficient().toLowerCase());
//                double beta1 = transferCoefficient.getBeta1();
//                double beta2 = transferCoefficient.getBeta2();
//
//                for (int x = 1; x <= 36; x++) {
//                    for (int y = 1; y <= 45; y++) {
//                        for (int xx = 1; xx <= 36; xx++) {
//                            for (int yy = 1; yy <= 45; yy++) {
//                                airQuality[x - 1][y - 1] = airQuality[x - 1][y - 1]
//                                        + beta1 
//                                        * emission[xx - 1][yy - 1]
//                                        / (1 + Math.exp(Math.pow(Math.pow(Math.pow(Math
//                                                .abs((yy * 4000.0 + 1044.0 + 0.5 * 4000.0) / 1000
//                                                        - (y * 4000.0 + 1044.0 + 0.5 * 4000.0) / 1000), 2.0)
//                                                + Math.pow(Math.abs((xx * 4000.0 + 252.0 + 0.5 * 4000.0) / 1000
//                                                        - (x * 4000.0 + 252.0 + 0.5 * 4000.0) / 1000), 2.0), 0.5),
//                                                beta2 )));
//                            }
//                        }
//                    }
//                }
//                result.setAirQuality(airQuality);
//            }
//            System.out.println("finished calc affect for each cell on every other cell " + System.currentTimeMillis());
//        }
//        System.out.println("time to calculate aq concentrations = " + (System.currentTimeMillis() - timing));
//        
//        if (1 == 2) {
//        File outputFile = new File("C:\\temp\\My Documents\\karen\\detailed_aq.csv");
//        FileWriter writer = null;
//        outputFile.createNewFile();
//        writer = new FileWriter(outputFile);
//
//        writer.write("sector,cmaq_pollutant,inventory_pollutant,factor,x,y,emission,airquality");
//
//        for (FastCMAQPollutantAirQualityEmissionResult cmaqResult : results) {
//            String sector = cmaqResult.getSector();
//            String pollutant = cmaqResult.getCmaqPollutant();
//            System.out.println("result sector = " + cmaqResult.getSector() + ", pollutant = " + pollutant);
//            for (FastInventoryPollutantAirQualityEmissionResult result : cmaqResult.getCmaqInventoryPollutantResults()) {
//                double[][] emission = result.getEmission();
//                double[][] airQuality = result.getAirQuality();
//                String inventoryPollutant = result.getPollutant();
//                for (int x = 1; x <= 36; x++) {
//                    for (int y = 1; y <= 45; y++) {
//                        writer.write("\n" + sector + "," + pollutant + "," + inventoryPollutant + "," + result.getAdjustmentFactor() + "," + x + "," + y + "," + emission[x - 1][y - 1] + ","
//                                + airQuality[x - 1][y - 1]);
//                    }
//                    writer.flush();
//                }
//            }
//        }
//        writer.close();
//
//        File outputFile21 = new File("C:\\temp\\My Documents\\karen\\aq.csv");
//        FileWriter writer21 = null;
//        outputFile21.createNewFile();
//        writer21 = new FileWriter(outputFile21);
//
//        writer21.write("sector,pollutant,x,y,emission,airquality");
//
//        for (FastCMAQPollutantAirQualityEmissionResult cmaqResult : results) {
//            String sector = cmaqResult.getSector();
//            String pollutant = cmaqResult.getCmaqPollutant();
//            System.out.println("result sector = " + cmaqResult.getSector() + ", pollutant = " + pollutant);
//            double[][] emission = cmaqResult.getEmission();
//            double[][] airQuality = cmaqResult.getAirQuality();
//            for (int x = 1; x <= 36; x++) {
//                for (int y = 1; y <= 45; y++) {
//                    writer21.write("\n" + sector + "," + pollutant + "," + x + "," + y + "," + emission[x - 1][y - 1] + ","
//                            + airQuality[x - 1][y - 1]);
//                }
//                writer21.flush();
//            }
//        }
//        writer21.close();
//
//        }
//        
////        importDataset(getUser("delvecch"), "C:\\temp\\My Documents\\karen", "detailed_aq.csv", getDatasetType(DatasetType.fastGriddedDetailedEmissionAirQuality), new VersionedTableFormat(new GriddedDetailedEmissionAirQualityResultFileFormat(dbServer.getSqlDataTypes()), dbServer.getSqlDataTypes()), "fastGriddedDetailedEmissionAirQuality");
//        
//        timing = System.currentTimeMillis();
//        createGriddedSummaryEmissionAirQualityOutput("fastGriddedSummaryEmissionAirQuality", results.toArray(new FastCMAQPollutantAirQualityEmissionResult[0]));
//        System.out.println("time to createGriddedSummaryEmissionAirQualityOutput = " + (System.currentTimeMillis() - timing));
//        
//        timing = System.currentTimeMillis();
//        createGriddedDetailedEmissionAirQualityOutput("fastGriddedDetailedEmissionAirQuality", results.toArray(new FastCMAQPollutantAirQualityEmissionResult[0]));
//        System.out.println("time to createGriddedDetailedEmissionAirQualityOutput = " + (System.currentTimeMillis() - timing));
//        
//        // System.out.println("start to calc affect for each cell on every other cell " + System.currentTimeMillis());
//        //        
//        // AQTransferCoefficient transferCoefficient = transferCoefficientMap.get("point_nox");
//        // double beta1 = transferCoefficient.getBeta1();
//        // double beta2 = transferCoefficient.getBeta2();
//        //        
//        // for (int x = 1; x <= 36/*45*/; x++) {
//        // for (int y = 1; y <= 45/*36*/; y++) {
//        // // FOR i IN 1..5 LOOP
//        // // FOR j IN 1..5 LOOP
//        // // raise notice '%', 'now calculating cell i = ' || i || ' j = ' || j;
//        // for (int xx = 1; xx <= 36/*45*/; xx++) {
//        // for (int yy = 1; yy <= 45/*36*/; yy++) {
//        // // FOR ii IN 1..36 LOOP
//        // // FOR jj IN 1..45 LOOP
//        // emiss[x][y] = emiss[x][y]
//        // + beta1 /*transferCoefficient.getBeta1() 0.000108 0.000008470*/
//        // * 365
//        // * emiss2[xx][yy]
//        // / (1 + Math.exp(Math.pow(Math.pow(Math.pow(Math
//        // .abs((yy * 4000.0 + 1044.0 + 0.5 * 4000.0) / 1000
//        // - (y * 4000.0 + 1044.0 + 0.5 * 4000.0) / 1000), 2.0)
//        // + Math.pow(Math.abs((xx * 4000.0 + 252.0 + 0.5 * 4000.0) / 1000
//        // - (x * 4000.0 + 252.0 + 0.5 * 4000.0) / 1000), 2.0), 0.5), beta2 /*transferCoefficient.getBeta2() (0.3084*2)
//        // 0.2170*/)));
//        // // emiss[i][j] := emiss[i][j]
//        // // + 0.000008470 * 365 * emiss2[ii][jj]
//        // // -- emiss[ii][jj] := emiss[ii][jj]
//        // // -- + 0.000008470 * 365 * emiss2[i][j]
//        // // / (1
//        // // +
//        // // case when sqrt((abs((ii*4000.0 + 1044.0 + 0.5*4000.0)/1000 - (i*4000.0 + 1044.0 +
//        // // 0.5*4000.0)/1000))^2 + (abs((jj*4000.0 + 252.0 + 0.5*4000.0)/1000 - (j*4000.0 + 252.0 +
//        // // 0.5*4000.0)/1000))^2) = 0.0 then 1
//        // // else exp(sqrt((abs((ii*4000.0 + 1044.0 + 0.5*4000.0)/1000 - (i*4000.0 + 1044.0 +
//        // // 0.5*4000.0)/1000))^2 + (abs((jj*4000.0 + 252.0 + 0.5*4000.0)/1000 - (j*4000.0 + 252.0 +
//        // // 0.5*4000.0)/1000))^2)^0.2170)
//        // // end
//        // // );
//        // }
//        // // END LOOP;
//        // }
//        // // END LOOP;
//        // //
//        // }
//        // // END LOOP;
//        // }
//        // System.out.println("finished calc affect for each cell on every other cell " + System.currentTimeMillis());
//        // // END LOOP;
//        // //
//        // // -- stuff valus into table
//        // // FOR i IN 1..36 LOOP
//        // for (int x = 1; x <= 36/*45*/; x++) {
//        // for (int y = 1; y <= 45/*36*/; y++) {
//        // // FOR j IN 1..45 LOOP
//        // System.out.println("cell emis[" + x + "," + y + "] = " + emiss2[x][y] + ", aq[" + x + "," + y + "] = " +
//        // emiss[x][y]);
//        // // insert into public.test
//        // // select i, j, emiss[i][j];
//        // // -- raise notice '%', 'calculated cell i = ' || i || ' j = ' || j || ' ' || emiss[i][j] || ' ' ||
//        // // clock_timestamp();
//        // // END LOOP;
//        // }
//        // // END LOOP;
//        // }
//        if (dbServer != null) {
//            try {
//                dbServer.disconnect();
//            } catch (Exception e) {
//                // NOTE Auto-generated catch block
//                e.printStackTrace();
//            }
//            dbServer = null;
//        }
//    }
//
//    public void createShapeFile() throws EmfException {
//        DbServer dbServer = dbServerFactory.getDbServer();
//        try {
//            PostgresSQLToShapeFile shapeFileGenerator = new PostgresSQLToShapeFile(dbServer);
//            //shapeFileGenerator.create(postgresBinDir, postgresDB, postgresUser, postgresPassword, filePath, selectQuery, projectionShapeFile)
//            shapeFileGenerator.create(getProperty("postgres-bin-dir"), getProperty("postgres-db"), 
//                    getProperty("postgres-user"), getProperty("pgsql2shp-info"), 
//                    "C:\\temp\\My Documents\\karen\\shape_files\\chaka", 
//                    "select public.ST_Y(public.ST_Transform(public.GeomFromEWKT('SRID=104307;POINT(' || (1044000.0 + 4000.0 * (aq.x - 0.5)) || ' ' || (252000.0 + 4000.0 * (aq.y - 0.5)) || ')'),104308)) as lat, public.ST_X(public.ST_Transform(public.GeomFromEWKT('SRID=104307;POINT(' || (1044000.0 + 4000.0 * (aq.x - 0.5)) || ' ' || (252000.0 + 4000.0 * (aq.y - 0.5)) || ')'),104308)) as lon, aq.x, aq.y, aq.sector, aq.pollutant, aq.emission as emis, aq.airquality as aq, airquality * totalpop / 6349855.90000001 as pop_wgh_aq, airquality * cancer_risk_ure as cancer_risk, airquality * cancer_risk_ure * totalpop as total_cancer_risk, airquality * totalpop / 6349855.90000001 * cancer_risk_ure as pop_wgh_cancer_risk, totalpop as grid_cell_pop, totalpop / 6349855.90000001 * 100.0 as pct_pop_in_grid_cell, cancer_risk_ure as ure, detroit_4km_grid.the_geom from emissions.DS_airquality_analysis_1493594302 aq left outer join emissions.DS_4km_Detroit_Pop_776499559 grid on grid.row = aq.y and grid.col = aq.x left outer join emissions.DS_fast_cancer_risk_ure_1493480478 ure on ure.cmaq_pollutant = aq.pollutant full join public.detroit_4km_grid on detroit_4km_grid.gridid = (aq.x + (aq.y - 1)* 36) where aq.dataset_id = 5816 and aq.sector ='nonroad' and aq.pollutant = 'NO3' order by aq.sector, aq.pollutant, aq.x, aq.y", 
//                    null);
//        } catch (ExporterException e) {
//            throw new EmfException(e.getMessage());
//        } finally {
//            if (dbServer != null) {
//                try {
//                    dbServer.disconnect();
//                } catch (Exception e) {
//                    // NOTE Auto-generated catch block
//                    e.printStackTrace();
//                }
//                dbServer = null;
//            }
//       }
//    }
//    
//    private String getProperty(String propertyName) {
//        EntityManager entityManager = entityManagerFactory.createEntityManager();
//        try {
//            EmfProperty property = new EmfPropertiesDAO().getProperty(propertyName, entityManager);
//            return property.getValue();
//        } finally {
//            entityManager.close();
//        }
//    }
//    public Map<String, AQTransferCoefficient> getTransferCoefficients() throws EmfException {
//        Map<String, AQTransferCoefficient> transferCoefficientMap = new HashMap<String, AQTransferCoefficient>();// map
//        ResultSet rs = null;
//        Statement statement = null;
//        DbServer dbServer = dbServerFactory.getDbServer();
//        Connection con = dbServer.getConnection();
//
//        EmfDataset transferCoefficients = loadDataset("transfer_coefficients");
//        
//        VersionedQuery transferCoefficientsVersionedQuery = new VersionedQuery(version(transferCoefficients, 0));
//        String transferCoefficientsTableName = transferCoefficients.getInternalSources()[0].getTable();
//
//        System.out.println("load up transfer coefficients into a HashMap " + System.currentTimeMillis());
//        String query = "select sector, variable, b1, b2 from " + qualifiedTable(transferCoefficientsTableName, dbServer.getEmissionsDatasource()) + " where " + transferCoefficientsVersionedQuery.query() + ";";
//        try {
//            statement = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
//            rs = statement
//                    .executeQuery(query);
//            while (rs.next()) {
//                String sector = rs.getString(1).toLowerCase();
//                String pollutant = rs.getString(2).toLowerCase();
//                transferCoefficientMap.put(sector + "_" + pollutant, new AQTransferCoefficient(sector, pollutant, rs
//                        .getDouble(3), rs.getDouble(4)));
//            }
//        } catch (SQLException e) {
//            throw new EmfException("Could not execute query -" + query + "\n" + e.getMessage());
//        } finally {
//            if (rs != null) {
//                try {
//                    rs.close();
//                } catch (SQLException e) { /**/
//                }
//                rs = null;
//            }
//            if (statement != null) {
//                try {
//                    statement.close();
//                } catch (SQLException e) { /**/
//                }
//                statement = null;
//            }
//            if (dbServer != null) {
//                try {
//                    dbServer.disconnect();
//                } catch (Exception e) {
//                    // NOTE Auto-generated catch block
//                    e.printStackTrace();
//                }
//                dbServer = null;
//            }
//       }
//        System.out.println("finished loading transfer coefficients into a HashMap " + System.currentTimeMillis());
//        return transferCoefficientMap;
//    }
//    
//    public EmfDataset createGriddedSectorCMAQInventoryPollutantEmissionResult(String[] sectors) throws EmfException {
//        EmfDataset griddedSectorSCCPollDataset = null;
//        String sqlTemp = "";
//        String sql2 = "";
//
//        for (EmfDataset dataset : getInputInventoryDatasets()) {
//            if (dataset.getDatasetTypeName().equals(DatasetType.orlPointInventory)) {
//                sqlTemp = buildSQLSelectForORLPointDataset(dataset, 0);
//                if (sqlTemp.length() > 0)
//                    sql2 += (sql2.length() > 0 ? " union all " : "") + sqlTemp;
//            } else {
//                sqlTemp = buildSQLSelectForSMOKEGriddedSCCRpt(dataset, 0);
//                if (sqlTemp.length() > 0)
//                    sql2 += (sql2.length() > 0 ? " union all " : "") + sqlTemp;
//            }
//        }
//
//        DbServer dbServer = dbServerFactory.getDbServer();
//        Connection con = dbServer.getConnection();
//        Statement statement = null;
//        try {
//            griddedSectorSCCPollDataset = addDataset("sens_nonpt_sectors_20100413", getDatasetType(DatasetType.fastGriddedSectorCMAQInventoryPollutantEmission), new VersionedTableFormat(new GriddedSectorCMAQInventoryPollutantEmissionFileFormat(dbServer.getSqlDataTypes()), dbServer.getSqlDataTypes()), "");
//            EmfDataset speciesMapping = loadDataset("fast_species_mapping");
//            
//            VersionedQuery speciesMappingVersionedQuery = new VersionedQuery(version(speciesMapping, 0), "fsm");
//            String speciesMappingTableName = speciesMapping.getInternalSources()[0].getTable();
//            String griddedSectorSCCPollTableName = griddedSectorSCCPollDataset.getInternalSources()[0].getTable();
//    
//            sql2 = "INSERT INTO " + qualifiedTable(griddedSectorSCCPollTableName, dbServer.getEmissionsDatasource()) + " (dataset_id, delete_versions, version, sector, cmaq_pollutant, inventory_pollutant, x, y, emission, factor, transfer_coefficient) \nselect " + griddedSectorSCCPollDataset.getId() + "::integer as dataset_id, '' as delete_versions, 0 as version, fsm.sector, fsm.cmaq_pollutant, fsm.inventory_pollutant, x, y, sum(emis), coalesce(fsm.factor, 1.0) as factor, fsm.transfer_coeff as emis \n" + "from ( \n" + sql2;
//            sql2 += ") summary \n";
//            sql2 += " inner join " + qualifiedTable(speciesMappingTableName, dbServer.getEmissionsDatasource()) + " fsm \n on fsm.sector = summary.sector \n and fsm.inventory_pollutant = summary.poll \n and summary.scc like coalesce(case when coalesce(fsm.scc, '') = '' then null else fsm.scc end, summary.scc) \n";
//            sql2 += " where " + speciesMappingVersionedQuery.query();
//            sql2 += " group by fsm.sector, fsm.cmaq_pollutant, fsm.inventory_pollutant, x, y, fsm.factor, fsm.transfer_coeff order by fsm.sector, fsm.cmaq_pollutant, fsm.inventory_pollutant, x, y; ";
//                    
//    /*        sql2 = "create table test.fast_emis_by_cmaq_inventory_poll as \nselect fsm.sector, fsm.cmaq_pollutant, fsm.inventory_pollutant, x, y, fsm.factor, fsm.transfer_coeff, sum(emis) as emis \n" + "from ( \n" + sql2;
//            sql2 += ") summary \n";
//            sql2 += " inner join emissions.DS_fast_species_mapping_1604915993 fsm \n on fsm.sector = summary.sector \n and fsm.inventory_pollutant = summary.poll \n and summary.scc like coalesce(fsm.scc, summary.scc) \n";
//            sql2 += " group by fsm.sector, fsm.cmaq_pollutant, fsm.inventory_pollutant, x, y, fsm.factor, fsm.transfer_coeff order by fsm.sector, fsm.cmaq_pollutant, fsm.inventory_pollutant, x, y; ";
//    */
//    
//            
//            long timing = System.currentTimeMillis();
//            
//            statement = con.createStatement();
//            timing = System.currentTimeMillis();
//            statement.execute(sql2);
//            System.out.println("time to populate fast_emis_by_cmaq_inventory_poll dataset = " + (System.currentTimeMillis() - timing));
//            creator.updateVersionZeroRecordCount(griddedSectorSCCPollDataset);
//        } catch (SQLException e) {
//            throw new EmfException("Could not execute query \n" + e.getMessage());
//        } finally {
//            if (statement != null) {
//                try {
//                    statement.close();
//                } catch (SQLException e) { /**/
//                }
//                statement = null;
//            }
//            if (con != null) {
//                try {
//                    con.close();
//                } catch (SQLException e) { /**/
//                }
//                con = null;
//            }
//            if (dbServer != null) {
//                try {
//                    dbServer.disconnect();
//                } catch (Exception e) {
//                    // NOTE Auto-generated catch block
//                    e.printStackTrace();
//                }
//                dbServer = null;
//            }
//        }
//        return griddedSectorSCCPollDataset;
//    }
//    
//    private EmfDataset[] getInputInventoryDatasets() throws EmfException {
//        List<EmfDataset> datasetList = new ArrayList<EmfDataset>();
//        
//        // get SMOKE gridded SCC report
////        datasetList.add(loadDataset("2020ac_det_link3_nonpt_det_scc_cell_4DET1"));
////        datasetList.add(loadDataset("2020ac_det_link_onroad_jan_scc_cell_4DET1"));
////        datasetList.add(loadDataset("2020ac_det_link3_lmb_nonroad_jan_scc_cell_4DET1"));
////        datasetList.add(loadDataset("2020ac_det_link3_lmb_alm_det_scc_cell_4DET1"));
//
//        // add ORL Point inventory emissions...
////        datasetList.add(loadDataset("ptnonipm_hap2005v2_revised_24feb2009_v0"));
////        datasetList.add(loadDataset("ptnonipm_xportfrac_cap2005v2_20nov2008_revised_20jan2009_v0"));
////        datasetList.add(loadDataset("ptnonipm_offshore_oil_cap2005v2_20nov2008_20nov2008_v0"));
////        datasetList.add(loadDataset("canada_point_uog_2006_orl_02mar2009_v0"));
////        datasetList.add(loadDataset("canada_point_cb5_2006_orl_10mar2009_v0"));
////        datasetList.add(loadDataset("canada_point_2006_orl_09mar2009_v2"));
////        datasetList.add(loadDataset("ptipm_cap2005v2_revised12mar2009_14may2009_v3"));
////        datasetList.add(loadDataset("ptipm_hap2005v2_allHAPs_revised12mar2009_12mar2009_v0"));
//
////        datasetList.add(loadDataset("ptnonipm_hap2005v2_fast"));
////        datasetList.add(loadDataset("ptnonipm_xportfrac_cap2005v2_fast"));
////        datasetList.add(loadDataset("ptipm_cap2005v2_fast"));
////        datasetList.add(loadDataset("ptipm_hap2005v2_allHAPs_fast"));
//
//        //Nonpoint sector analysis, actually created ORL point from SMOKE Gridded rpts and base inventories
//        //this is the base scenario
////        datasetList.add(loadDataset("afdust_2002ad_xportfrac_x_point418948134667185"));
////        datasetList.add(loadDataset("alm_no_c3_cap2002v3_x_point418029641416646"));
////        datasetList.add(loadDataset("alm_no_c3_hap2002v4_x_point418618115875900"));
////        datasetList.add(loadDataset("nonpt_2005ao_tox_detroit_CAP_nopfc_x_point415281287401890"));
////        datasetList.add(loadDataset("nonpt_2005ao_tox_detroit_CAP_pfc_x_point416889326857603"));
////        datasetList.add(loadDataset("nonpt_2005ao_tox_detroit_HAP_x_point417031827196054"));
//
//        //this is the control scenario for the above nonpoint sector inventories
//        datasetList.add(loadDataset("afdust_2002ad_xportfrac_x_point418948134667185"));//not controlled
//        datasetList.add(loadDataset("alm_no_c3_cap2002v3_x_point_cntl"));
//        datasetList.add(loadDataset("alm_no_c3_hap2002v4_x_point418618115875900"));//not controlled
//        datasetList.add(loadDataset("nonpt_2005ao_tox_detroit_CAP_nopfc_x_point_cntl"));
//        datasetList.add(loadDataset("nonpt_2005ao_tox_detroit_CAP_pfc_x_point416889326857603"));//not controlled
//        datasetList.add(loadDataset("nonpt_2005ao_tox_detroit_HAP_x_point_cntl"));
//        
//        
//        
//        
//        return datasetList.toArray(new EmfDataset[0]);
//
//    }
//    
//    private String buildSQLSelectForSMOKEGriddedSCCRpt(EmfDataset griddedSCCDataset, int griddedSCCDatasetVersionNumber) throws EmfException {
//        return buildSQLSelectForSMOKEGriddedSCCRpt(griddedSCCDataset, griddedSCCDatasetVersionNumber,
//                false);
//    }
//
//    private String buildSQLSelectForSMOKEGriddedSCCRpt(EmfDataset griddedSCCDataset, int griddedSCCDatasetVersionNumber,
//            boolean includeFIPS) throws EmfException {
//        String sql = "";
//        Sector sector = griddedSCCDataset.getSectors()[0];
//        String tableName = griddedSCCDataset.getInternalSources()[0].getTable();
//        if (sector == null)
//            throw new EmfException("Dataset " + griddedSCCDataset.getName() + " is missing the sector.");
//        VersionedQuery griddedSCCDatasetVersionedQuery = new VersionedQuery(version(griddedSCCDataset, griddedSCCDatasetVersionNumber));
//        boolean isMonthly = sector.getName().equals("nonroad") || sector.getName().equals("onroad");
//        List<String> pollutantColumns = getDatasetPollutantColumns(griddedSCCDataset);
//        for (int i = 0; i < pollutantColumns.size(); i++) {
//            String pollutant = pollutantColumns.get(i);
//            sql += (i > 0 ? " union all " : "") + "select '" + sector.getName().replace("'", "''")
//                    + "'::varchar(64) as sector" + (includeFIPS ? ", substring(region,2) as fips" : "") + ", scc, '" + pollutant.toUpperCase() + "' as poll, sum("
//                    + (!isMonthly ? "" : "365 * ") + pollutant + ") as emis, x_cell as x, y_cell as y from emissions."
//                    + tableName + " where  " + griddedSCCDatasetVersionedQuery.query()
//                    + " and x_cell between 1 and 36 and y_cell between 1 and 45 and coalesce(" + pollutant
//                    + ",0.0) <> 0.0 group by x_cell, y_cell" + (includeFIPS ? ", substring(region,2)" : "") + ", scc \n";
//        }
//        return sql;
//    }
//
//    private String buildSQLSelectForORLPointDataset(EmfDataset orlPointDataset, int versionNumber)
//            throws EmfException {
//        String sql = "";
//        Sector sector = orlPointDataset.getSectors()[0];
//        String tableName = orlPointDataset.getInternalSources()[0].getTable();
//        String invTableTableName = invTable.getInternalSources()[0].getTable();
//        if (sector == null)
//            throw new EmfException("Dataset " + orlPointDataset.getName() + " is missing the sector.");
//        VersionedQuery versionedQuery = new VersionedQuery(version(orlPointDataset, versionNumber), "inv");
//        VersionedQuery invTableVersionedQuery = new VersionedQuery(invTableVersion, "invtable");
//        sql = "select '"
//                + sector.getName().replace("'", "''")
//                + "'::varchar(64) as sector, inv.scc, invtable.name as poll, sum(invtable.factor * inv.ann_emis) as emis, ceiling((public.ST_X(public.ST_Transform(public.GeomFromEWKT('SRID=104308;POINT(' || inv.xloc || ' ' || inv.yloc || ')'),104307)) - 1044000.0) / 4000.0) as x, ceiling((public.ST_Y(public.ST_Transform(public.GeomFromEWKT('SRID=104308;POINT(' || inv.xloc || ' ' || inv.yloc || ')'),104307)) - 252000.0) / 4000.0) as y from emissions."
//                + tableName + " inv inner join emissions." + invTableTableName
//                + " invtable on invtable.cas = inv.poll where coalesce(inv.ann_emis,0.0) <> 0.0 and "
//                + versionedQuery.query() + " and " + invTableVersionedQuery.query();
//        sql += " and ceiling((public.ST_X(public.ST_Transform(public.GeomFromEWKT('SRID=104308;POINT(' || inv.xloc || ' ' || inv.yloc || ')'),104307)) - 1044000.0) / 4000.0) between 1 and 36 ";
//        sql += " and ceiling((public.ST_Y(public.ST_Transform(public.GeomFromEWKT('SRID=104308;POINT(' || inv.xloc || ' ' || inv.yloc || ')'),104307)) - 252000.0) / 4000.0) between 1 and 45 ";
//        sql += " group by ceiling((public.ST_X(public.ST_Transform(public.GeomFromEWKT('SRID=104308;POINT(' || inv.xloc || ' ' || inv.yloc || ')'),104307)) - 1044000.0) / 4000.0), ceiling((public.ST_Y(public.ST_Transform(public.GeomFromEWKT('SRID=104308;POINT(' || inv.xloc || ' ' || inv.yloc || ')'),104307)) - 252000.0) / 4000.0), inv.scc, invtable.name \n";
//
//        return sql;
//    }
//
//    public void makeSureInventoryDatasetHasIndexes(String datasetTableName, Datasource datasource) {
//        String query = "SELECT public.create_orl_table_indexes('" + datasetTableName.toLowerCase() + "');analyze " + qualifiedTable(datasetTableName, datasource).toLowerCase() + ";";
//        try {
//            datasource.query().execute(query);
//        } catch (SQLException e) {
//            //e.printStackTrace();
//            //supress all errors, the indexes might already be on the table...
//        } finally {
//            //
//        }
//    }
//
//    protected void populatePointInvFromSMOKEGriddedSCCRpt(EmfDataset baseInv, int baseInvVersion, 
//            EmfDataset smokeRpt, int smokeRptVersion, 
//            EmfDataset invTable, int invTableVersion, 
//            EmfDataset newInv) throws EmfException, Exception {
//        String baseInvTableName = baseInv.getInternalSources()[0].getTable();
////        String smokeRptTableName = smokeRpt.getInternalSources()[0].getTable();
//        String newInvTableName = newInv.getInternalSources()[0].getTable();
//        String invTableTableName = invTable.getInternalSources()[0].getTable();
//        int newInvDatasetId = newInv.getId();
//        VersionedQuery baseVersionedQuery = new VersionedQuery(version(baseInv, baseInvVersion), "inv");
////      VersionedQuery smokeRptVersionedQuery = new VersionedQuery(version(smokeRpt, smokeRptVersion), "smk");
//        VersionedQuery invTableVersionedQuery = new VersionedQuery(version(invTable, invTableVersion), "invtable");
//
//        String sql = "select ";
//        String columnList = "";
//
//        DbServer dbServer = dbServerFactory.getDbServer();
//        TableFormat newTableFormat = new FileFormatFactory(dbServer).tableFormat(getDatasetType(DatasetType.orlPointInventory));
//        TableFormat baseInvTableFormat = new FileFormatFactory(dbServer).tableFormat(baseInv.getDatasetType());
//        Column[] columns = newTableFormat.cols();
//        Datasource datasource = dbServer.getEmissionsDatasource();
//        Connection con = dbServer.getConnection();
//        Statement statement = null;
//        makeSureInventoryDatasetHasIndexes(baseInvTableName, datasource);
//        try {
//            for (int i = 0; i < columns.length; i++) {
//                String columnName = columns[i].name();
//                if (columnName.equalsIgnoreCase("record_id")) {
//    //                sql += "record_id";
//    //                columnList += "record_id";
//                } else if (columnName.equalsIgnoreCase("dataset_id")) {
//    //              sql += "," + datasetId + " as dataset_id";
//    //              columnList += ",dataset_id";
//                    sql += newInvDatasetId + "::integer as dataset_id";
//                    columnList += "dataset_id";
//                } else if (columnName.equalsIgnoreCase("delete_versions")) {
//                    sql += ", '' as delete_versions";
//                    columnList += "," + columnName;
//                } else if (columnName.equalsIgnoreCase("version")) {
//                    sql += ", 0 as version";
//                    columnList += "," + columnName;
//                } else if (columnName.equalsIgnoreCase("plantid")) {
//                    sql += ", smk.x as plantid";
//                    columnList += "," + columnName;
//                } else if (columnName.equalsIgnoreCase("pointid")) {
//                    sql += ", smk.y as pointid";
//                    columnList += "," + columnName;
//                } else if (columnName.equalsIgnoreCase("stackid")) {
//                    sql += ", smk.x as stackid";
//                    columnList += "," + columnName;
//                } else if (columnName.equalsIgnoreCase("segment")) {
//                    sql += ", smk.y as segment";
//                    columnList += "," + columnName;
//                } else if (columnName.equalsIgnoreCase("poll")) {
//                    sql += ", coalesce(invtable.cas, smk.poll) as poll";
//                    columnList += "," + columnName;
//    //                invtable.cas
//                } else if (columnName.equalsIgnoreCase("avd_emis")) {
//                    sql += ", smk.emis "
////                        + " / (inv.ann_emis * coalesce(invtable.factor, 1.0)) * (select sum(inv2.ann_emis * invtable2.factor) as total_emis" 
////                        + " from " + qualifiedTable(baseInvTableName, datasource) + " as inv2"
////                        + " inner join " + qualifiedTable(invTableTableName, datasource) + " as invtable2 "
////                        + " on invtable2.cas = inv2.poll "
////                        + " and " + invTableVersionedQuery.query().replace("invtable", "invtable2")
////                        + " and invtable2.factor = 1.0"
////                        + " where " + baseVersionedQuery.query().replace("inv", "inv2") + ")"
//                        + " / 365 as avd_emis";
//                    columnList += "," + columnName;
//                } else if (columnName.equalsIgnoreCase("ann_emis")) {
//                    sql += ", smk.emis "
////                        + " / (inv.ann_emis * coalesce(invtable.factor, 1.0)) * (select sum(inv2.ann_emis * invtable2.factor) as total_emis" 
////                        + " from " + qualifiedTable(baseInvTableName, datasource) + " as inv2"
////                        + " inner join " + qualifiedTable(invTableTableName, datasource) + " as invtable2 "
////                        + " on invtable2.cas = inv2.poll "
////                        + " and " + invTableVersionedQuery.query().replace("invtable", "invtable2")
////                        + " and invtable2.factor = 1.0"
////                        + " where " + baseVersionedQuery.query().replace("inv", "inv2") + ") "
//                        + " as ann_emis";
//                    columnList += "," + columnName;
//                } else if (columnName.equalsIgnoreCase("xloc")) {
//                    sql += ", public.ST_X(public.ST_Transform(public.GeomFromEWKT('SRID=104307;POINT(' || (1044000.0 + 4000.0 * (smk.x - 0.5)) || ' ' || (252000.0 + 4000.0 * (smk.y - 0.5)) || ')'),104308)) as x_loc";
//                    columnList += "," + columnName;
//                } else if (columnName.equalsIgnoreCase("yloc")) {
//                    sql += ", public.ST_Y(public.ST_Transform(public.GeomFromEWKT('SRID=104307;POINT(' || (1044000.0 + 4000.0 * (smk.x - 0.5)) || ' ' || (252000.0 + 4000.0 * (smk.y - 0.5)) || ')'),104308)) as y_loc";
//                    columnList += "," + columnName;
//                } else if (columnName.equalsIgnoreCase("cpri") && hasColName("PRIMARY_DEVICE_TYPE_CODE", baseInvTableFormat)) {
//                    sql += ", PRIMARY_DEVICE_TYPE_CODE::integer as cpri";
//                    columnList += "," + columnName;
//                } else if (columnName.equalsIgnoreCase("CSEC") && hasColName("SECONDARY_DEVICE_TYPE_CODE", baseInvTableFormat)) {
//                    sql += ", SECONDARY_DEVICE_TYPE_CODE::integer as CSEC";
//                    columnList += "," + columnName;
//                } else {
//                    sql += "," + (hasColName(columnName, baseInvTableFormat) ? "inv." : "newinv.") + columnName;
//                    columnList += "," + columnName;
//                }
//    //            public.ST_Y(public.ST_Transform(public.GeomFromEWKT('SRID=104307;POINT(' || (1044000.0 + 4000.0 * (aq.x - 0.5)) || ' ' || (252000.0 + 4000.0 * (aq.y - 0.5)) || ')'),104308)) as lat, public.ST_X(public.ST_Transform(public.GeomFromEWKT('SRID=104307;POINT(' || (1044000.0 + 4000.0 * (aq.x - 0.5)) || ' ' || (252000.0 + 4000.0 * (aq.y - 0.5)) || ')'),104308)) as lon            
//            }
//            sql += " FROM " + qualifiedTable(baseInvTableName, datasource) + " as inv"
//                + " inner join " + qualifiedTable(invTableTableName, datasource)
//                + " as invtable"
//                + " on invtable.cas = inv.poll"
//                + " and " + invTableVersionedQuery.query()
//                + " and invtable.factor = 1.0"
//    //            + " inner join ("
//    //            + " select inv2.fips, inv2.scc, invtable2.name as smoke_name, sum(inv2.ann_emis * invtable2.factor) as total_emis" 
//    //            + " from " + qualifiedTable(baseInvTableName, datasource) + " as inv2"
//    //            + " inner join " + qualifiedTable(invTableTableName, datasource) + " as invtable2 "
//    //            + " on invtable2.cas = inv2.poll "
//    //            + " and " + invTableVersionedQuery.query().replace("invtable", "invtable2")
//    //            + " and invtable2.factor = 1.0"
//    //            + " where " + baseVersionedQuery.query().replace("inv", "inv2")
//    //            + " and inv2.fips in ('26049','26065','26075','26087','26091','26093','26099','26115','26125','26147','26155','26161','26163','39043','39051','39095','39123','39173')"
//    //            + " group by inv2.fips, inv2.scc, invtable2.name"
//    //            + " order by inv2.fips, inv2.scc, invtable2.name"
//    //            + ") as total" 
//    //            + " on total.fips = inv.fips and total.scc = inv.scc and total.smoke_name = invtable.name"
//                + " inner join ("
//                + buildSQLSelectForSMOKEGriddedSCCRpt(smokeRpt, smokeRptVersion, true)
//                + ") as smk" 
//                + " on inv.fips = smk.fips and inv.scc = smk.scc and invtable.name = smk.poll" 
//                + " left outer join " + qualifiedTable(newInvTableName, datasource) + " as newinv "
//                + " on 1 = 0"
//                + " WHERE " + baseVersionedQuery.query();
//            sql = "INSERT INTO " + qualifiedTable(newInvTableName, datasource) + " (" + columnList + ") \n" + sql;
//            System.out.println(sql);
//            statement = con.createStatement();
//            statement.execute(sql);
//        } catch (SQLException e) {
//            throw new EmfException("Could not execute query \n" + e.getMessage());
//        } finally {
//            if (statement != null) {
//                try {
//                    statement.close();
//                } catch (SQLException e) { /**/
//                }
//                statement = null;
//            }
//            if (con != null) {
//                try {
//                    con.close();
//                } catch (SQLException e) { /**/
//                }
//                con = null;
//            }
//            if (dbServer != null) {
//                try {
//                    dbServer.disconnect();
//                } catch (Exception e) {
//                    // NOTE Auto-generated catch block
//                    e.printStackTrace();
//                }
//                dbServer = null;
//            }
//        }
//    }
//
//    private boolean hasColName(String colName, TableFormat fileFormat) {
//        Column[] cols = fileFormat.cols();
//        boolean hasIt = false;
//        for (int i = 0; i < cols.length; i++)
//            if (colName.equalsIgnoreCase(cols[i].name())) hasIt = true;
//
//        return hasIt;
//    }
//
//    private String qualifiedTable(String table, Datasource datasource) {
//        return datasource.getName() + "." + table;
//    }
//    
//    private EmfDataset loadDataset(String datasetName) throws EmfException {
//        EntityManager entityManager = entityManagerFactory.createEntityManager();
//        try {
//            EmfDataset dataset = datasetDAO.getDataset(entityManager, datasetName);
//            return dataset;
//        } catch (RuntimeException e) {
//            throw new EmfException("Could not get dataset " + datasetName);
//        } finally {
//            entityManager.close();
//        }
//    }
//
//    private User getUser(String username) throws EmfException {
//        EntityManager entityManager = entityManagerFactory.createEntityManager();
//        try {
//            UserDAO dao = new UserDAO();
//            return dao.get(username, entityManager);
//        } catch (RuntimeException e) {
//            throw new EmfException("Could not get user " + username);
//        } finally {
//            entityManager.close();
//        }
//    }
//
//    private void createGriddedSummaryEmissionAirQualityOutput(String datasetName, FastCMAQPollutantAirQualityEmissionResult[] results) throws Exception {
//        DbServer dbServer = dbServerFactory.getDbServer();
//        EmfDataset dataset = addDataset(datasetName, getDatasetType(DatasetType.fastGriddedSummaryEmissionAirQuality),
//                new VersionedTableFormat(new GriddedSummaryEmissionAirQualityResultFileFormat(dbServer.getSqlDataTypes()), dbServer.getSqlDataTypes()), "");
//        int datasetId = dataset.getId();
//        String tableName = qualifiedTable(dataset.getInternalSources()[0].getTable(), dbServer.getEmissionsDatasource());
//        Connection con = dbServer.getConnection();
//        con.setAutoCommit(false);
//        Statement statement = null;
//        try {
//            statement = con.createStatement();
////            long timing = System.currentTimeMillis();
//        
//            int counter = 0;
//            for (FastCMAQPollutantAirQualityEmissionResult cmaqResult : results) {
//                String sector = cmaqResult.getSector();
//                String pollutant = cmaqResult.getCmaqPollutant();
//    //            System.out.println("result sector = " + cmaqResult.getSector() + ", pollutant = " + pollutant);
//                double[][] emission = cmaqResult.getEmission();
//                double[][] airQuality = cmaqResult.getAirQuality();
//                for (int x = 1; x <= 36; x++) {
//                    for (int y = 1; y <= 45; y++) {
//                        ++counter;
//                        statement.addBatch("INSERT INTO " + tableName + " (dataset_id, delete_versions, version, sector,CMAQ_POLLUTANT,x,y,emission,AIR_QUALITY, POPULATION_WEIGHTED_AIR_QUALITY, CANCER_RISK_PER_PERSON, TOTAL_CANCER_RISK, POPULATION_WEIGHTED_CANCER_RISK, GRID_CELL_POPULATION, PCT_POPULATION_IN_GRID_CELL_TO_MODEL_DOMAIN, URE) \nselect " + datasetId + "::integer as dataset_id, '' as delete_versions, 0 as version,'" + sector + "','" + pollutant + "'," + x + "," + y + "," + emission[x - 1][y - 1] + "," + airQuality[x - 1][y - 1] + ", " + airQuality[x - 1][y - 1] + " * totalpop / 6349855.90000001 as \"Population weighted AQ\", " + airQuality[x - 1][y - 1] + " * cancer_risk_ure as \"cancer risk/person\", " + airQuality[x - 1][y - 1] + " * cancer_risk_ure * totalpop as \"total cancer risk\", " + airQuality[x - 1][y - 1] + " * totalpop / 6349855.90000001 * cancer_risk_ure as \"pop. weighted cancer risk\", totalpop as \"grid cell population\", totalpop / 6349855.90000001 * 100.0 as \"% population in grid cell relative to modeling domain\", cancer_risk_ure as \"URE\" from (select 1) as foo left outer join emissions.DS_4km_Detroit_Pop_776499559 grid on grid.row = " + y + " and grid.col = " + x + " left outer join emissions.DS_fast_cancer_risk_ure_1493480478 ure on ure.cmaq_pollutant = '" + pollutant + "';");
//                    }
//                }
//                if (counter > 20000) {
//                    counter = 0;
//                    statement.executeBatch();
//                }
//            }
//            statement.executeBatch();
//            con.commit();
//            
//            
//            //now lets update the other fields...
////            POPULATION_WEIGHTED_AIR_QUALITY, CANCER_RISK_PER_PERSON, 
////            TOTAL_CANCER_RISK, POPULATION_WEIGHTED_CANCER_RISK, 
////            GRID_CELL_POPULATION, PCT_POPULATION_IN_GRID_CELL_TO_MODEL_DOMAIN, 
////            URE
//
////            VersionedQuery versionedQuery = new VersionedQuery(version(dataset, 0), "aq");
////            statement.execute("update " + tableName + " " 
////                    + "set POPULATION_WEIGHTED_AIR_QUALITY = air_quality * totalpop / 6349855.90000001, "
////                    + "CANCER_RISK_PER_PERSON = air_quality * cancer_risk_ure, "
////                    + "TOTAL_CANCER_RISK = air_quality * cancer_risk_ure * totalpop, "
////                    + "POPULATION_WEIGHTED_CANCER_RISK = air_quality * totalpop / 6349855.90000001 * cancer_risk_ure, "
////                    + "GRID_CELL_POPULATION = totalpop, "
////                    + "PCT_POPULATION_IN_GRID_CELL_TO_MODEL_DOMAIN = totalpop / 6349855.90000001 * 100.0, "
////                    + "URE = cancer_risk_ure "
////                    + "from " + tableName + " aq "
////                    + "left outer join emissions.DS_4km_Detroit_Pop_776499559 grid "
////                    + "on grid.row = aq.y "
////                    + "and grid.col = aq.x "
////                    + "left outer join emissions.DS_fast_cancer_risk_ure_1493480478 ure "
////                    + "on ure.cmaq_pollutant = aq.cmaq_pollutant "
////                    + "where " + versionedQuery.query());
//
////            TOTAL_CANCER_RISK, POPULATION_WEIGHTED_CANCER_RISK, 
////            GRID_CELL_POPULATION, PCT_POPULATION_IN_GRID_CELL_TO_MODEL_DOMAIN, 
////            URE);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        } finally {
//            if (statement != null) {
//                try {
//                    statement.close();
//                } catch (SQLException e) { /**/
//                }
//                statement = null;
//            }
//            if (con != null) {
//                try {
//                    con.close();
//                } catch (SQLException e) { /**/
//                }
//                con = null;
//            }
//            try {
//                dbServer.disconnect();
//            } catch (Exception e) {
//                // NOTE Auto-generated catch block
//                e.printStackTrace();
//            }
//        }
//
//
//    }
//
//    private void createGriddedDetailedEmissionAirQualityOutput(String datasetName, FastCMAQPollutantAirQualityEmissionResult[] results) throws Exception {
//        DbServer dbServer = dbServerFactory.getDbServer();
//        EmfDataset dataset = addDataset(datasetName, getDatasetType(DatasetType.fastGriddedDetailedEmissionAirQuality),
//                new VersionedTableFormat(new GriddedDetailedEmissionAirQualityResultFileFormat(dbServer.getSqlDataTypes()), dbServer.getSqlDataTypes()), "");
//        int datasetId = dataset.getId();
//        String tableName = qualifiedTable(dataset.getInternalSources()[0].getTable(), dbServer.getEmissionsDatasource());
//        Connection con = dbServer.getConnection();
//        con.setAutoCommit(false);
//        Statement statement = null;
//        try {
//            statement = con.createStatement();
////            long timing = System.currentTimeMillis();
//        
//            int counter = 0;
//
//            for (FastCMAQPollutantAirQualityEmissionResult cmaqResult : results) {
//                String sector = cmaqResult.getSector();
//                String pollutant = cmaqResult.getCmaqPollutant();
////                System.out.println("result sector = " + cmaqResult.getSector() + ", pollutant = " + pollutant);
//                for (FastInventoryPollutantAirQualityEmissionResult result : cmaqResult.getCmaqInventoryPollutantResults()) {
//                    double[][] emission = result.getEmission();
//                    double[][] airQuality = result.getAirQuality();
//                    String inventoryPollutant = result.getPollutant();
//                    for (int x = 1; x <= 36; x++) {
//                        for (int y = 1; y <= 45; y++) {
//                            ++counter;
//                            statement.addBatch("INSERT INTO " + tableName + " (dataset_id, delete_versions, version, sector,CMAQ_POLLUTANT,INVENTORY_POLLUTANT,x,y,factor,TRANSFER_COEFF,emission,AIR_QUALITY) \nselect " + datasetId + "::integer as dataset_id, '' as delete_versions, 0 as version,'" + sector + "','" + pollutant + "','" + inventoryPollutant + "'," + x + "," + y + "," + result.getAdjustmentFactor() + ",'" + result.getTranferCoefficient() + "'," + emission[x - 1][y - 1] + "," + airQuality[x - 1][y - 1] + ";");
//                        }
//                    }
//                    if (counter > 20000) {
//                        counter = 0;
//                        statement.executeBatch();
//                    }
//                }
//            }
//            statement.executeBatch();
//            con.commit();
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        } finally {
//            if (statement != null) {
//                try {
//                    statement.close();
//                } catch (SQLException e) { /**/
//                }
//                statement = null;
//            }
//            if (con != null) {
//                try {
//                    con.close();
//                } catch (SQLException e) { /**/
//                }
//                con = null;
//            }
//            try {
//                dbServer.disconnect();
//            } catch (Exception e) {
//                // NOTE Auto-generated catch block
//                e.printStackTrace();
//            }
//        }
//
//
//    }
//
//    protected EmfDataset addDataset(String datasetName) throws EmfException {
//        DbServer dbServer = dbServerFactory.getDbServer();
//        EntityManager entityManager = entityManagerFactory.createEntityManager();
//        User user = new UserDAO().get("delvecch", entityManager);
//        
//        DatasetCreator creator = new DatasetCreator(new ControlStrategy(), user, 
//                entityManagerFactory, dbServerFactory,
//                dbServerFactory.getDbServer().getEmissionsDatasource(), new Keywords(new Keyword[] { } ));
//        try {
//            EmfDataset dataset = creator.addDataset("fast", "fast_" + datasetName, getDatasetType(DatasetType.orlPointInventory), new VersionedTableFormat(new ORLPointFileFormat(dbServer.getSqlDataTypes()), dbServer.getSqlDataTypes()), "");
//            return dataset;
//        } catch (RuntimeException e) {
//            throw new EmfException("Could not get dataset " + datasetName);
//        } catch (Exception e) {
//            throw new EmfException("Could not get dataset " + datasetName);
//        } finally {
//            entityManager.close();
//            try {
//                dbServer.disconnect();
//            } catch (Exception e) {
//                // NOTE Auto-generated catch block
//                e.printStackTrace();
//            }
//        }
//    }
//
//    protected EmfDataset addDataset(String datasetName, DatasetType datasetType,
//            TableFormat tableFormat, String headerDescription) throws EmfException {
//        DbServer dbServer = dbServerFactory.getDbServer();
//        EntityManager entityManager = entityManagerFactory.createEntityManager();
//        
//        try {
//            EmfDataset dataset = creator.addDataset("fast", datasetName + System.nanoTime(), datasetType, tableFormat, headerDescription);
//            return dataset;
//        } catch (RuntimeException e) {
//            e.printStackTrace();
//            throw new EmfException("Could not get dataset " + datasetName);
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw new EmfException("Could not add dataset " + datasetName);
//        } finally {
//            entityManager.close();
//            try {
//                dbServer.disconnect();
//            } catch (Exception e) {
//                // NOTE Auto-generated catch block
//                e.printStackTrace();
//            }
//        }
//    }
//
//    private DatasetType getDatasetType(String datasetTypeName) throws EmfException {
//        EntityManager entityManager = entityManagerFactory.createEntityManager();
//        try {
//            return new DataCommonsDAO().getDatasetType(datasetTypeName, entityManager);
//        } catch (RuntimeException e) {
//            throw new EmfException("Could not get dataset type " + datasetTypeName);
//        } finally {
//            entityManager.close();
//        }
//    }
//
//    private List<String> getDatasetPollutantColumns(EmfDataset dataset) throws EmfException {
//        List<String> pollutantColumnList = new ArrayList<String>();
//        ResultSet rs;
//        ResultSetMetaData md;
//        Statement statement = null;
//        DbServer dbServer = dbServerFactory.getDbServer();
//        Connection con = dbServer.getConnection();
//        try {
//            statement = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
//            rs = statement.executeQuery("select * from emissions." + dataset.getInternalSources()[0].getTable()
//                    + " where 1 = 0;");
//            md = rs.getMetaData();
//        } catch (SQLException e) {
//            throw new EmfException(e.getMessage());
//        }
//
//        try {
//            for (int i = 1; i < md.getColumnCount(); i++) {
//                String columnName = md.getColumnName(i);
//                int columnType = md.getColumnType(i);
//                // ignore these columns, we really just want the pollutant/specie columns
//                if (!columnName.equalsIgnoreCase("x_cell") && !columnName.equalsIgnoreCase("y_cell")
//                        && !columnName.equalsIgnoreCase("source_id") && !columnName.equalsIgnoreCase("region")
//                        && !columnName.equalsIgnoreCase("scc") && !columnName.equalsIgnoreCase("scc2")
//                        && !columnName.equalsIgnoreCase("record_id") && !columnName.equalsIgnoreCase("dataset_id")
//                        && !columnName.equalsIgnoreCase("version") && !columnName.equalsIgnoreCase("delete_versions")
//                        && !columnName.equalsIgnoreCase("road") && !columnName.equalsIgnoreCase("link")
//                        && !columnName.equalsIgnoreCase("veh_type") && columnType == Types.DOUBLE)
//                    pollutantColumnList.add(columnName);
//            }
//        } catch (SQLException e) {
//            //
//        } finally {
//            if (rs != null) {
//                try {
//                    rs.close();
//                } catch (SQLException e) { /**/
//                }
//                rs = null;
//            }
//            if (statement != null) {
//                try {
//                    statement.close();
//                } catch (SQLException e) { /**/
//                }
//                statement = null;
//            }
//            if (con != null) {
//                try {
//                    con.close();
//                } catch (SQLException e) { /**/
//                }
//                con = null;
//            }
//            if (dbServer != null) {
//                try {
//                    dbServer.disconnect();
//                } catch (Exception e) {
//                    // NOTE Auto-generated catch block
//                    e.printStackTrace();
//                }
//                dbServer = null;
//            }
//            
//        }
//
//        return pollutantColumnList;
//
//    }
//
//    protected Version version(EmfDataset inputDataset, int datasetVersion) {
//        EntityManager entityManager = entityManagerFactory.createEntityManager();
//        try {
//            Versions versions = new Versions();
//            return versions.get(inputDataset.getId(), datasetVersion, entityManager);
//        } finally {
//            entityManager.close();
//        }
//    }
//
//    public static void main(String args[]) {
//        AQGridCalculator aq = new AQGridCalculator();
//        try {
//            aq.go();
//        } catch (EmfException e) {
//            // NOTE Auto-generated catch block
//            e.printStackTrace();
//        } catch (IOException e) {
//            // NOTE Auto-generated catch block
//            e.printStackTrace();
//        } catch (Exception e) {
//            // NOTE Auto-generated catch block
//            e.printStackTrace();
//        }
//    }
//    
//    public class TestServiceTestCase extends ServiceTestCase {
//
////        @Override
//        protected void doSetUp() throws Exception {
//            // NOTE Auto-generated method stub
//            
//        }
//
////        @Override
//        protected void doTearDown() throws Exception {
//            // NOTE Auto-generated method stub
//            
//        }
//        
//    }
//    
//    public class FastCMAQPollutantAirQualityEmissionResult {
//
//        private String sector;
//
//        private String cmaqPollutant;
//
//        private FastInventoryPollutantAirQualityEmissionResult[] inventoryPollutantResults = new FastInventoryPollutantAirQualityEmissionResult[] {};
//
//        public FastCMAQPollutantAirQualityEmissionResult(String sector, String cmaqPollutant) {
//            super();
//            this.sector = sector;
//            this.cmaqPollutant = cmaqPollutant;
//        }
//
//        public FastCMAQPollutantAirQualityEmissionResult() {
//            // NOTE Auto-generated constructor stub
//        }
//
//        // private Map<String, FastCMAQInventoryPollutantResult> cmaqInventoryPollutantResults;
//        public void setSector(String sector) {
//            this.sector = sector;
//        }
//
//        public String getSector() {
//            return sector;
//        }
//
//        public void setCmaqPollutant(String cmaqPollutant) {
//            this.cmaqPollutant = cmaqPollutant;
//        }
//
//        public String getCmaqPollutant() {
//            return cmaqPollutant;
//        }
//
//        public void addCmaqInventoryPollutantResults(FastInventoryPollutantAirQualityEmissionResult inventoryPollutantResult) {
//            List<FastInventoryPollutantAirQualityEmissionResult> inventoryPollutantResultList = new ArrayList<FastInventoryPollutantAirQualityEmissionResult>();
//            inventoryPollutantResultList.addAll(Arrays.asList(inventoryPollutantResults));
//            inventoryPollutantResultList.add(inventoryPollutantResult);
//            this.inventoryPollutantResults = inventoryPollutantResultList.toArray(new FastInventoryPollutantAirQualityEmissionResult[0]);
//        }
//
//        public void setCmaqInventoryPollutantResults(FastInventoryPollutantAirQualityEmissionResult[] cmaqInventoryPollutantResults) {
//            this.inventoryPollutantResults = cmaqInventoryPollutantResults;
//        }
//
//        public FastInventoryPollutantAirQualityEmissionResult[] getCmaqInventoryPollutantResults() {
//            return inventoryPollutantResults;
//        }
//
//        public double[][] getEmission() {
//            double[][] emission = new double[36][45];
//            for (FastInventoryPollutantAirQualityEmissionResult result : inventoryPollutantResults) {
//                double[][] resultEmission = result.getEmission();
//                for (int x = 1; x <= 36; x++) {
//                    for (int y = 1; y <= 45; y++) {
//                        emission[x - 1][y - 1] += resultEmission[x - 1][y - 1];
//                    }
//                }
//              
//            }
//            return emission;
//        }
//
//        public double[][] getAirQuality() {
//            double[][] airQuality = new double[36][45];
//            for (FastInventoryPollutantAirQualityEmissionResult result : inventoryPollutantResults) {
//                double[][] resultAirQuality = result.getAirQuality();
//                double adjustmentFactor = result.getAdjustmentFactor();
//                for (int x = 1; x <= 36; x++) {
//                    for (int y = 1; y <= 45; y++) {
//                        airQuality[x - 1][y - 1] += adjustmentFactor * resultAirQuality[x - 1][y - 1];
//                    }
//                }
//              
//            }
//            return airQuality;
//        }
//
//    }
//
//    public class FastCMAQInventoryPollutantResult {
//
//        private FastInventoryPollutantAirQualityEmissionResult inventoryPollutantResult;
//
//        private float adjustmentFactor;
//
//        private String tranferCoefficient;
//
//        public void setAdjustmentFactor(float adjustmentFactor) {
//            this.adjustmentFactor = adjustmentFactor;
//        }
//
//        public float getAdjustmentFactor() {
//            return adjustmentFactor;
//        }
//
//        public void setInventoryPollutantResult(FastInventoryPollutantAirQualityEmissionResult inventoryPollutantResult) {
//            this.inventoryPollutantResult = inventoryPollutantResult;
//        }
//
//        public FastInventoryPollutantAirQualityEmissionResult getInventoryPollutantResult() {
//            return inventoryPollutantResult;
//        }
//
//        public void setTranferCoefficient(String tranferCoefficient) {
//            this.tranferCoefficient = tranferCoefficient;
//        }
//
//        public String getTranferCoefficient() {
//            return tranferCoefficient;
//        }
//
//    }
//
//    public class FastInventoryPollutantAirQualityEmissionResult {
//
//        private String inventoryPollutant;
//
//        private double[][] emission;
//
//        private double[][] airQuality;
//
//        private float adjustmentFactor;
//
//        private String tranferCoefficient;
//
//        public FastInventoryPollutantAirQualityEmissionResult(String inventoryPollutant, float adjustmentFactor,
//                String tranferCoefficient, int numerOfXGridCells, int numerOfYGridCells) {
//            this.inventoryPollutant = inventoryPollutant;
//            this.adjustmentFactor = adjustmentFactor;
//            this.tranferCoefficient = tranferCoefficient;
//            this.emission = new double[numerOfXGridCells][numerOfYGridCells];
//            this.airQuality = new double[numerOfXGridCells][numerOfYGridCells];
//        }
//
//        public void setPollutant(String inventoryPollutant) {
//            this.inventoryPollutant = inventoryPollutant;
//        }
//
//        public String getPollutant() {
//            return inventoryPollutant;
//        }
//
//        public void setEmission(double[][] emission) {
//            this.emission = emission;
//        }
//
//        public double[][] getEmission() {
//            return emission;
//        }
//
//        public void setAirQuality(double[][] airQuality) {
//            this.airQuality = airQuality;
//        }
//
//        public double[][] getAirQuality() {
//            return airQuality;
//        }
//
//        public void setAdjustmentFactor(float adjustmentFactor) {
//            this.adjustmentFactor = adjustmentFactor;
//        }
//
//        public float getAdjustmentFactor() {
//            return adjustmentFactor;
//        }
//
//        public void setTranferCoefficient(String tranferCoefficient) {
//            this.tranferCoefficient = tranferCoefficient;
//        }
//
//        public String getTranferCoefficient() {
//            return tranferCoefficient;
//        }
//    }
//
//    public class AQTransferCoefficient {
//
//        private String sector;
//
//        private String pollutant;
//
//        private double beta1;
//
//        private double beta2;
//
//        public AQTransferCoefficient() {
//            //
//        }
//
//        public AQTransferCoefficient(String sector, String pollutant, double beta1, double beta2) {
//            this();
//            this.sector = sector;
//            this.pollutant = pollutant;
//            this.beta1 = beta1;
//            this.beta2 = beta2;
//        }
//
//        public void setPollutant(String pollutant) {
//            this.pollutant = pollutant;
//        }
//
//        public String getPollutant() {
//            return pollutant;
//        }
//
//        public void setSector(String sector) {
//            this.sector = sector;
//        }
//
//        public String getSector() {
//            return sector;
//        }
//
//        public void setBeta1(double beta1) {
//            this.beta1 = beta1;
//        }
//
//        public double getBeta1() {
//            return beta1;
//        }
//
//        public void setBeta2(double beta2) {
//            this.beta2 = beta2;
//        }
//
//        public double getBeta2() {
//            return beta2;
//        }
//    }
//
// }
