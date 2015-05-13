package gov.epa.emissions.framework.services.cost.analysis.leastcost;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.Versions;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.TableFormat;
import gov.epa.emissions.commons.io.VersionedQuery;
import gov.epa.emissions.commons.io.XFileFormat;
import gov.epa.emissions.commons.io.orl.ORLMergedFileFormat;
import gov.epa.emissions.commons.io.temporal.VersionedTableFormat;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.ControlStrategyInputDataset;
import gov.epa.emissions.framework.services.cost.analysis.common.AbstractCheckMessagesStrategyTask;
import gov.epa.emissions.framework.services.cost.analysis.common.StrategyLoader;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.services.cost.controlStrategy.DatasetCreator;
import gov.epa.emissions.framework.services.data.DatasetTypesDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;
import gov.epa.emissions.framework.tasks.DebugLevels;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

import org.hibernate.Session;

public abstract class LeastCostAbstractStrategyTask extends AbstractCheckMessagesStrategyTask {

    protected ControlStrategyResult leastCostCMWorksheetResult;

    protected ControlStrategyResult leastCostCurveSummaryResult;

    public LeastCostAbstractStrategyTask(ControlStrategy controlStrategy, User user, DbServerFactory dbServerFactory,
            HibernateSessionFactory sessionFactory, StrategyLoader loader) throws EmfException {
        super(controlStrategy, user, dbServerFactory, sessionFactory, loader);
    }

    protected void compareInventoriesTemporalResolution() {
        ControlStrategyInputDataset[] inputDatasets = controlStrategy.getControlStrategyInputDatasets();
        if (inputDatasets.length > 1) {
            Date startDate1 = null;
            Date stopDate1 = null;
            Date startDate2 = null;
            Date stopDate2 = null;
            int count = 0;
            for (ControlStrategyInputDataset inputDataset : controlStrategy.getControlStrategyInputDatasets())
                if (!inputDataset.getInputDataset().getDatasetType().getName().equals(DatasetType.orlMergedInventory) &&
                    !inputDataset.getInputDataset().getDatasetType().getName().equals(DatasetType.ff10MergedInventory)) {
                    startDate1 = inputDatasets[0].getInputDataset().getStartDateTime();
                    stopDate1 = inputDatasets[0].getInputDataset().getStopDateTime();
                    if (startDate1 == null || stopDate1 == null)
                        setStatus("The dataset: " + inputDatasets[0].getInputDataset()
                                + ", is missing a start or stop date time.");
                    if (count > 0) {
                        startDate2 = inputDataset.getInputDataset().getStartDateTime();
                        stopDate2 = inputDataset.getInputDataset().getStopDateTime();

                        if (startDate1 != null && stopDate1 != null && startDate2 != null && stopDate2 != null
                                && (!startDate1.equals(startDate2) || !stopDate1.equals(stopDate2))) {
                            // only show a warning at this point, don't stop the strategy run
                            setStatus("Warning: The datasets have different start or stop date times.");
                            break;
                        }
                        startDate2 = startDate1;
                        stopDate2 = stopDate1;
                    }
                    ++count;
                }
        }
    }

    protected void finalizeCMWorksheetResult() throws EmfException {
        // finalize the result, update completion time and run status...
        leastCostCMWorksheetResult.setCompletionTime(new Date());
        leastCostCMWorksheetResult.setRunStatus("Completed.");
        setSummaryResultCount(leastCostCMWorksheetResult);
        saveControlStrategySummaryResult(leastCostCMWorksheetResult);
        strategyResultList.add(leastCostCMWorksheetResult);
        // runSummaryQASteps((EmfDataset)leastCostCMWorksheetResult.getDetailedResultDataset(), 0);
    }

    protected void finalizeCostCuveSummaryResult() throws EmfException {
        // finalize the result, update completion time and run status...
        leastCostCurveSummaryResult.setCompletionTime(new Date());
        leastCostCurveSummaryResult.setRunStatus("Completed.");
        setSummaryResultCount(leastCostCurveSummaryResult);
        saveControlStrategySummaryResult(leastCostCurveSummaryResult);
        strategyResultList.add(leastCostCurveSummaryResult);
        // runSummaryQASteps((EmfDataset)leastCostCurveSummaryResult.getDetailedResultDataset(), 0);
    }

    protected ControlStrategyInputDataset getInventory() {
        ControlStrategyInputDataset[] inputDatasets = controlStrategy.getControlStrategyInputDatasets();
        ControlStrategyInputDataset dataset = null;
        if (inputDatasets.length == 1) {
            dataset = inputDatasets[0];
        } else {
            for (ControlStrategyInputDataset inputDataset : controlStrategy.getControlStrategyInputDatasets())
                if (inputDataset.getInputDataset().getDatasetType().getName().equals(DatasetType.orlMergedInventory) ||
                    inputDataset.getInputDataset().getDatasetType().getName().equals(DatasetType.ff10MergedInventory)) {
                    dataset = inputDataset;
                }
        }
        return dataset;
    }
    
    protected DatasetType getMergedInventoryDatasetType(String type) {
        DatasetType datasetType = null;
        Session session = sessionFactory.getSession();
        try {
            datasetType = new DatasetTypesDAO().get(type, session);
        } finally {
            session.close();
        }
        return datasetType;
    }

    protected EmfDataset createMergedORLInventoryDataset(String country) throws EmfException {
        // TableFormat tableFormat = new ORLMergedFileFormat(dbServer.getSqlDataTypes());
        TableFormat tableFormat = new VersionedTableFormat(new ORLMergedFileFormat(dbServer.getSqlDataTypes()),
                dbServer.getSqlDataTypes());
        // "MergedORL_",
        EmfDataset mergedDataset = creator.addDataset("DS", DatasetCreator.createDatasetName(controlStrategy.getName() + "_MergedORL"),
                getMergedInventoryDatasetType(DatasetType.orlMergedInventory), tableFormat, getORLMergedInventoryDatasetDescription(tableFormat,
                        country));

        //auto - update sector with the all sector
        mergedDataset.addSector(getSector("All Sectors"));
        creator.update(mergedDataset);
        
        return mergedDataset;
    }
    
    protected EmfDataset createMergedFF10InventoryDataset(String country) throws EmfException {
        DatasetType type = getMergedInventoryDatasetType(DatasetType.ff10MergedInventory);
        TableFormat tableFormat = new VersionedTableFormat(type.getFileFormat(), dbServer.getSqlDataTypes());
        EmfDataset mergedDataset = creator.addDataset("DS", DatasetCreator.createDatasetName(controlStrategy.getName() + "_MergedFF10"),
                type, tableFormat, getFF10MergedInventoryDatasetDescription(tableFormat,
                        country));

        mergedDataset.addSector(getSector("All Sectors"));
        creator.update(mergedDataset);
        
        return mergedDataset;
    }

    protected String getORLMergedInventoryDatasetDescription(TableFormat tableFormat, String country) {
        return "#" + tableFormat.identify() + "\n#COUNTRY " + country + "\n#YEAR " + controlStrategy.getInventoryYear();
    }

    protected String getFF10MergedInventoryDatasetDescription(TableFormat tableFormat, String country) {
        return "#FORMAT=FF10\n#COUNTRY " + country + "\n#YEAR " + controlStrategy.getInventoryYear();
    }

    protected void populateORLMergedDataset(EmfDataset mergedDataset) throws EmfException {
        try {
            
            StrategyLoader loader = this.getLoader();
            if (loader instanceof LeastCostAbstractStrategyLoader) {
                
                LeastCostAbstractStrategyLoader leastCostAbstractStrategyLoader = (LeastCostAbstractStrategyLoader)loader;
                ORLMergedFileFormat fileFormat = new ORLMergedFileFormat(dbServer.getSqlDataTypes());

                String columnDelimitedList = fileFormat.columnList();

                ControlStrategyInputDataset[] controlStrategyInputDatasets = controlStrategy
                        .getControlStrategyInputDatasets();
                //insert one table at a time...
                for (int i = 0; i < controlStrategyInputDatasets.length; i++) {
//                    if (!controlStrategyInputDatasets[i].getInputDataset().getDatasetType().getName().contains("ORL")) {
//                        setStatus("The inventory, " + controlStrategyInputDatasets[i].getInputDataset().getName() + ", won't be processed only ORL Inventores are currently supported.");
//                        break;
//                    }
                    String sql = "INSERT INTO " + qualifiedEmissionTableName(mergedDataset) + " (dataset_id, "
                    + columnDelimitedList + ") ";
                    EmfDataset inputDataset = controlStrategyInputDatasets[i].getInputDataset();
                    if (!inputDataset.getDatasetType().getName().equals(DatasetType.orlMergedInventory)) {
                        String tableName = qualifiedEmissionTableName(controlStrategyInputDatasets[i].getInputDataset());
                        boolean isPointDataset = inputDataset.getDatasetType().getName().equals(DatasetType.orlPointInventory);
                        boolean isFlatFilePointDataset = inputDataset.getDatasetType().getName().equals(DatasetType.FLAT_FILE_2010_POINT);
                        boolean isFlatFileNonpointDataset = inputDataset.getDatasetType().getName().equals(DatasetType.FLAT_FILE_2010_NONPOINT);
                        String inventoryColumnDelimitedList = columnDelimitedList;
                        ResultSetMetaData md = getDatasetResultSetMetaData(tableName);
                        // we need to figure out what dataset columns we have to work with.
                        // for example, nonpoint inv won't have plantid, and point inv has no rpen
                        // alias these columns and use null as the value
                        byte designCapacityColumnCount = 0;
                        boolean hasDesignCapacityColumns = false;
                        boolean hasSICColumn = false;
                        boolean hasNAICSColumn = false;
                        boolean hasCpriColumn = false;
                        boolean hasPrimaryDeviceTypeCodeColumn = false;
                        for (int j = 1; j <= md.getColumnCount(); j++) {
                            if (md.getColumnName(j).equalsIgnoreCase("naics")) {
                                hasNAICSColumn = true;
                            } else if (md.getColumnName(j).equalsIgnoreCase("sic")) {
                                hasSICColumn = true;
                            } else if (md.getColumnName(j).equalsIgnoreCase("cpri")) {
                                hasCpriColumn = true;
                            } else if (md.getColumnName(j).equalsIgnoreCase("PRIMARY_DEVICE_TYPE_CODE")) {
                                hasPrimaryDeviceTypeCodeColumn = true;
                            } else if (md.getColumnName(j).equalsIgnoreCase("design_capacity")) {
                                ++designCapacityColumnCount;
                            } else if (md.getColumnName(j).equalsIgnoreCase("design_capacity_unit_numerator")) {
                                ++designCapacityColumnCount;
                            } else if (md.getColumnName(j).equalsIgnoreCase("design_capacity_unit_denominator")) {
                                ++designCapacityColumnCount;
                            } else if (md.getColumnName(j).equalsIgnoreCase("design_capacity_units")) {
                                ++designCapacityColumnCount;
                            }
                        }
                        if (isPointDataset) {
                            if (designCapacityColumnCount == 3) 
                                hasDesignCapacityColumns = true;
                            inventoryColumnDelimitedList = inventoryColumnDelimitedList.replaceAll("RPEN", "100::double precision as RPEN");
                            if (!hasDesignCapacityColumns) 
                                inventoryColumnDelimitedList = inventoryColumnDelimitedList.replaceAll("DESIGN_CAPACITY, DESIGN_CAPACITY_UNIT_NUMERATOR, DESIGN_CAPACITY_UNIT_DENOMINATOR", "null::double precision as DESIGN_CAPACITY, null::text as DESIGN_CAPACITY_UNIT_NUMERATOR, null::text as DESIGN_CAPACITY_UNIT_DENOMINATOR");
                        } else if (isFlatFileNonpointDataset) {
                            inventoryColumnDelimitedList = inventoryColumnDelimitedList.replaceAll("ANN_EMIS", "ANN_VALUE as ANN_EMIS");
                            inventoryColumnDelimitedList = inventoryColumnDelimitedList.replaceAll("CEFF", "ANN_PCT_RED as CEFF");
                            inventoryColumnDelimitedList = inventoryColumnDelimitedList.replaceAll("AVD_EMIS", "null::double precision as AVD_EMIS");
                            inventoryColumnDelimitedList = inventoryColumnDelimitedList.replaceAll("FIPS, PLANTID, POINTID, STACKID, SEGMENT, PLANT", "region_cd as fips, null::text as PLANTID, null::text as POINTID, null::text as STACKID, null::text as SEGMENT, null::text as PLANT");
                            inventoryColumnDelimitedList = inventoryColumnDelimitedList.replaceAll("DESIGN_CAPACITY, DESIGN_CAPACITY_UNIT_NUMERATOR, DESIGN_CAPACITY_UNIT_DENOMINATOR", "null::double precision as DESIGN_CAPACITY, null::text as DESIGN_CAPACITY_UNIT_NUMERATOR, null::text as DESIGN_CAPACITY_UNIT_DENOMINATOR");
                            inventoryColumnDelimitedList = inventoryColumnDelimitedList.replaceAll("STKFLOW", "null::double precision as STKFLOW");
                            inventoryColumnDelimitedList = inventoryColumnDelimitedList.replaceAll("STKTEMP", "null::double precision as STKTEMP");
                            inventoryColumnDelimitedList = inventoryColumnDelimitedList.replaceAll("STKVEL", "null::double precision as STKVEL");
                            inventoryColumnDelimitedList = inventoryColumnDelimitedList.replaceAll("STKDIAM", "null::double precision as STKDIAM");
                            inventoryColumnDelimitedList = inventoryColumnDelimitedList.replaceAll("ANNUAL_AVG_HOURS_PER_YEAR", "null::double precision as ANNUAL_AVG_HOURS_PER_YEAR");
                            inventoryColumnDelimitedList = inventoryColumnDelimitedList.replaceAll("REFF", "100::double precision as REFF");
                            inventoryColumnDelimitedList = inventoryColumnDelimitedList.replaceAll("RPEN", "100::double precision as RPEN");
                            inventoryColumnDelimitedList = inventoryColumnDelimitedList.replaceAll("PCT_REDUCTION", "null::text as PCT_REDUCTION");
                        } else if (isFlatFilePointDataset) {
                            inventoryColumnDelimitedList = inventoryColumnDelimitedList.replaceAll("ANN_EMIS", "ANN_VALUE as ANN_EMIS");
                            inventoryColumnDelimitedList = inventoryColumnDelimitedList.replaceAll("CEFF", "ANN_PCT_RED as CEFF");
                            inventoryColumnDelimitedList = inventoryColumnDelimitedList.replaceAll("AVD_EMIS", "null::double precision as AVD_EMIS");
                            inventoryColumnDelimitedList = inventoryColumnDelimitedList.replaceAll("FIPS, PLANTID, POINTID, STACKID, SEGMENT, PLANT", "region_cd as fips, facility_id as PLANTID, unit_id as POINTID, rel_point_id as STACKID, process_id as SEGMENT, facility_name as PLANT");
                            inventoryColumnDelimitedList = inventoryColumnDelimitedList.replaceAll("DESIGN_CAPACITY, DESIGN_CAPACITY_UNIT_NUMERATOR, DESIGN_CAPACITY_UNIT_DENOMINATOR", "null::double precision as DESIGN_CAPACITY, case when array_length(string_to_array(DESIGN_CAPACITY_UNITS,'/'), 1) >= 1 then (string_to_array(DESIGN_CAPACITY_UNITS,'/'))[1] else null end as DESIGN_CAPACITY_UNIT_NUMERATOR, case when array_length(string_to_array(DESIGN_CAPACITY_UNITS,'/'), 1) >= 2 then (string_to_array(DESIGN_CAPACITY_UNITS,'/'))[2] else null end as DESIGN_CAPACITY_UNIT_DENOMINATOR");
                            inventoryColumnDelimitedList = inventoryColumnDelimitedList.replaceAll("REFF", "100::double precision as REFF");
                            inventoryColumnDelimitedList = inventoryColumnDelimitedList.replaceAll("RPEN", "100::double precision as RPEN");
                            inventoryColumnDelimitedList = inventoryColumnDelimitedList.replaceAll("PCT_REDUCTION", "null::text as PCT_REDUCTION");
                        } else {
                            inventoryColumnDelimitedList = inventoryColumnDelimitedList.replaceAll("PLANTID, POINTID, STACKID, SEGMENT, PLANT", "null::text as PLANTID, null::text as POINTID, null::text as STACKID, null::text as SEGMENT, null::text as PLANT");
                            inventoryColumnDelimitedList = inventoryColumnDelimitedList.replaceAll("DESIGN_CAPACITY, DESIGN_CAPACITY_UNIT_NUMERATOR, DESIGN_CAPACITY_UNIT_DENOMINATOR", "null::double precision as DESIGN_CAPACITY, null::text as DESIGN_CAPACITY_UNIT_NUMERATOR, null::text as DESIGN_CAPACITY_UNIT_DENOMINATOR");
                            inventoryColumnDelimitedList = inventoryColumnDelimitedList.replaceAll("STKFLOW", "null::double precision as STKFLOW");
                            inventoryColumnDelimitedList = inventoryColumnDelimitedList.replaceAll("STKTEMP", "null::double precision as STKTEMP");
                            inventoryColumnDelimitedList = inventoryColumnDelimitedList.replaceAll("STKVEL", "null::double precision as STKVEL");
                            inventoryColumnDelimitedList = inventoryColumnDelimitedList.replaceAll("STKDIAM", "null::double precision as STKDIAM");
                            inventoryColumnDelimitedList = inventoryColumnDelimitedList.replaceAll("ANNUAL_AVG_HOURS_PER_YEAR", "null::double precision as ANNUAL_AVG_HOURS_PER_YEAR");
                        }
                        if (!hasNAICSColumn) inventoryColumnDelimitedList = inventoryColumnDelimitedList.replaceAll("NAICS", "null::text as NAICS");
                        if (!hasSICColumn) inventoryColumnDelimitedList = inventoryColumnDelimitedList.replaceAll("SIC", "null::text as SIC");
                        if (!hasCpriColumn) inventoryColumnDelimitedList = inventoryColumnDelimitedList.replaceAll("CPRI", "null::integer as CPRI");
                        if (!hasPrimaryDeviceTypeCodeColumn) inventoryColumnDelimitedList = inventoryColumnDelimitedList.replaceAll("PRIMARY_DEVICE_TYPE_CODE", "null::varchar(4) as PRIMARY_DEVICE_TYPE_CODE");
                        inventoryColumnDelimitedList = inventoryColumnDelimitedList.replaceAll("SECTOR", "'" + getDatasetSector(inputDataset) +  "' as SECTOR");
                        inventoryColumnDelimitedList = inventoryColumnDelimitedList.replaceAll("ORIGINAL_DATASET_ID", inputDataset.getId() +  "::integer as ORIGINAL_DATASET_ID");
                        inventoryColumnDelimitedList = inventoryColumnDelimitedList.replaceAll("ORIGINAL_RECORD_ID", "RECORD_ID as ORIGINAL_RECORD_ID");
                        inventoryColumnDelimitedList = inventoryColumnDelimitedList.replaceAll("MONTH", (inputDataset.applicableMonth() + 1) + "::smallint as \"month\"");
                        VersionedQuery versionedQuery = new VersionedQuery(version(inputDataset, controlStrategyInputDatasets[i].getVersion()));
                        sql += //(i > 0 ? " union all " : "") + 
                            "select " + mergedDataset.getId() + " as dataset_id, " + inventoryColumnDelimitedList + " "
                            + "from " + tableName + " e "
                            + "where " + versionedQuery.query()
                            + leastCostAbstractStrategyLoader.getFilterForSourceQuery();
                        if (DebugLevels.DEBUG_25())
                            System.out.println(System.currentTimeMillis() + " " + sql);
                        setStatus("Started populating ORL Merged inventory, " 
                                + mergedDataset.getName() 
                                + ", with the inventory, " 
                                + inputDataset.getName() 
                                + ".");
                        datasource.query().execute(sql);
                        setStatus("Completed populating ORL Merged inventory, " 
                                + mergedDataset.getName() 
                                + ", with the inventory, " 
                                + inputDataset.getName() 
                                + ".");
                    }
                }

            }
        } catch (SQLException e) {
            throw new EmfException("Error occured when inserting data into the merged inventory dataset table" + "\n" + e.getMessage());
        }
    }
    
    protected void populateFF10MergedDataset(EmfDataset mergedDataset) throws EmfException {
        try {
            StrategyLoader loader = this.getLoader();
            if (loader instanceof LeastCostAbstractStrategyLoader) {
                LeastCostAbstractStrategyLoader leastCostAbstractStrategyLoader = (LeastCostAbstractStrategyLoader)loader;
                XFileFormat fileFormat = getMergedInventoryDatasetType(DatasetType.ff10MergedInventory).getFileFormat();
                
                String columnDelimitedList = "";
                Column[] columns = fileFormat.cols();
                for (int i = 0; i < columns.length; i++) {
                    columnDelimitedList += (i > 0 ? ", " + columns[i].name() : columns[i].name());
                }
                
                ControlStrategyInputDataset[] controlStrategyInputDatasets = controlStrategy.getControlStrategyInputDatasets();
                for (int i = 0; i < controlStrategyInputDatasets.length; i++) {
                    EmfDataset inputDataset = controlStrategyInputDatasets[i].getInputDataset();
                    if (inputDataset.getId() == mergedDataset.getId()) continue;
                    
                    String sql = "INSERT INTO " + qualifiedEmissionTableName(mergedDataset) + " (dataset_id, "+ columnDelimitedList + ") ";
                    
                    boolean isPointDataset = inputDataset.getDatasetType().getName().equals(DatasetType.FLAT_FILE_2010_POINT);
                    String inventoryColumnDelimitedList = columnDelimitedList;

                    if (!isPointDataset) {
                        ArrayList<String> colsToReplace = new ArrayList<String>();
                        colsToReplace.add("FACILITY_ID");
                        colsToReplace.add("UNIT_ID");
                        colsToReplace.add("REL_POINT_ID");
                        colsToReplace.add("PROCESS_ID");
                        colsToReplace.add("FACILITY_NAME");
                        colsToReplace.add("STKHGT");
                        colsToReplace.add("STKDIAM");
                        colsToReplace.add("STKTEMP");
                        colsToReplace.add("STKFLOW");
                        colsToReplace.add("STKVEL");
                        colsToReplace.add("NAICS");
                        colsToReplace.add("DESIGN_CAPACITY,");
                        colsToReplace.add("DESIGN_CAPACITY_UNITS");
                        colsToReplace.add("ANNUAL_AVG_HOURS_PER_YEAR");
                        
                        for (String column : colsToReplace) {
                            inventoryColumnDelimitedList = inventoryColumnDelimitedList.replaceAll(column, "NULL AS " + column);
                        }
                    }
                    inventoryColumnDelimitedList = inventoryColumnDelimitedList.replaceAll("ORIGINAL_DATASET_ID", inputDataset.getId() +  "::integer as ORIGINAL_DATASET_ID");
                    inventoryColumnDelimitedList = inventoryColumnDelimitedList.replaceAll("ORIGINAL_RECORD_ID", "RECORD_ID as ORIGINAL_RECORD_ID");
                    inventoryColumnDelimitedList = inventoryColumnDelimitedList.replaceAll("SECTOR", "'" + getDatasetSector(inputDataset) +  "' as SECTOR");
                    
                    VersionedQuery versionedQuery = new VersionedQuery(version(inputDataset, controlStrategyInputDatasets[i].getVersion()));
                    sql += 
                        "SELECT " + mergedDataset.getId() + " AS dataset_id, " + inventoryColumnDelimitedList + " " +
                        "FROM " + qualifiedEmissionTableName(inputDataset) + " e " +
                        "WHERE " + versionedQuery.query() +
                        leastCostAbstractStrategyLoader.getFilterForSourceQuery();
                    if (DebugLevels.DEBUG_25())
                        System.out.println(System.currentTimeMillis() + " " + sql);
                    setStatus("Started populating FF10 Merged inventory, " + mergedDataset.getName() + ", with the inventory, " + inputDataset.getName() + ".");
                    datasource.query().execute(sql);
                    setStatus("Completed populating FF10 Merged inventory, " + mergedDataset.getName() + ", with the inventory, " + inputDataset.getName() + ".");
                }
            }
        } catch (SQLException e) {
            throw new EmfException("Error occured when inserting data into the merged inventory dataset table" + "\n" + e.getMessage());
        }
    }

    protected String getDatasetSector(EmfDataset dataset) {
        String sector = "";
        Sector[] sectors = dataset.getSectors();
        if (sectors != null) {
            if (sectors.length > 0) {
                sector = sectors[0].getName();
            }
        }
        return sector;
    }

    protected void truncateMergedDataset(EmfDataset mergedDataset) throws EmfException {
        try {
            datasource.query().execute("TRUNCATE " + qualifiedEmissionTableName(mergedDataset));
        } catch (SQLException e) {
            throw new EmfException("Error occured when truncating the merged inventory dataset table" + "\n"
                    + e.getMessage());
        }
    }

    protected ResultSetMetaData getDatasetResultSetMetaData(String qualifiedTableName) throws EmfException {
        ResultSet rs = null;
        ResultSetMetaData md = null;
        if (DebugLevels.DEBUG_25())
            System.out.println(System.currentTimeMillis() + " get ResultSetMetaData");
        try {
            rs = datasource.query().executeQuery("select * from " + qualifiedTableName + " where 1 = 0");
            md = rs.getMetaData();
        } catch (SQLException e) {
            throw new EmfException("Error occured when getting metadata for the inventory dataset table" + "\n"
                    + e.getMessage());
        }
        return md;
    }

    protected Version version(EmfDataset inputDataset, int datasetVersion) {
        Session session = sessionFactory.getSession();
        try {
            Versions versions = new Versions();
            return versions.get(inputDataset.getId(), datasetVersion, session);
        } finally {
            session.close();
        }
    }

    protected void mergeInventoryDatasets() throws EmfException {

        StrategyLoader loader = this.getLoader();
        if (loader instanceof LeastCostAbstractStrategyLoader) {

            LeastCostAbstractStrategyLoader leastCostAbstractStrategyLoader = (LeastCostAbstractStrategyLoader) loader;

            // if there is more than one input inventory, then merge these into one dataset,
            // then we use that as the input to the strategy run
            if (controlStrategy.getControlStrategyInputDatasets().length > 1) {
                ControlStrategyResult[] results = leastCostAbstractStrategyLoader.getControlStrategyResults();
                // if (controlStrategyInputDatasetCount >= 1) {
                ControlStrategyInputDataset[] inputDatasets = controlStrategy.getControlStrategyInputDatasets();

                // TODO: look for any errors or warnings in the input inventories
                // i.e., missing sector or differing temporal period
                compareInventoriesTemporalResolution();
                // look for missing sector
                for (ControlStrategyInputDataset inputDataset : inputDatasets)
                    if (!inputDataset.getInputDataset().getDatasetType().getName().equals(DatasetType.orlMergedInventory) &&
                        !inputDataset.getInputDataset().getDatasetType().getName().equals(DatasetType.ff10MergedInventory)) {
                        if (getDatasetSector(inputDataset.getInputDataset()).length() == 0)
                            setStatus("The dataset: " + inputDataset.getInputDataset().getName()
                                    + ", does not have a sector specified.");
                    }

                // check to see if exists already, if so, then truncate its data and start over...
                boolean hasMergedDataset = false;
                boolean useFF10Merged = true;
                EmfDataset mergedDataset = null;
                // see if it already has a merged dataset
                for (ControlStrategyInputDataset inputDataset : inputDatasets) {
                    String typeName = inputDataset.getInputDataset().getDatasetType().getName();
                    if (typeName.equals(DatasetType.orlMergedInventory) ||
                        typeName.equals(DatasetType.ff10MergedInventory)) {
                        hasMergedDataset = true;
                        mergedDataset = inputDataset.getInputDataset();
                    } else if (!typeName.equals(DatasetType.FLAT_FILE_2010_NONPOINT) &&
                        !typeName.equals(DatasetType.FLAT_FILE_2010_POINT)) {
                        useFF10Merged = false;
                    }
                }
                EmfDataset inputDataset = inputDatasets[0].getInputDataset();
                String country = inputDataset.getCountry() != null ? inputDataset.getCountry().getName() : "US";
                if (!hasMergedDataset) {
                    if (useFF10Merged) {
                        mergedDataset = createMergedFF10InventoryDataset(country);
                    } else {
                        mergedDataset = createMergedORLInventoryDataset(country);
                    }
                    // add to strategy...
                    ControlStrategyInputDataset controlStrategyInputDataset = new ControlStrategyInputDataset(
                            mergedDataset);
                    controlStrategy.addControlStrategyInputDatasets(controlStrategyInputDataset);
                    updateControlStrategyWithLock(controlStrategy);
                } 
//                else {
//                    if (controlStrategy.getDeleteResults() || results.length == 0)
//                }
//                if (controlStrategy.getDeleteResults() || results.length == 0) {
                //always truncate and repopulate this merged dataset, something could have changed
                //from prior run...
                truncateMergedDataset(mergedDataset);
                if (useFF10Merged) {
                    populateFF10MergedDataset(mergedDataset);
                } else {
                    populateORLMergedDataset(mergedDataset);
                }
                leastCostAbstractStrategyLoader.makeSureInventoryDatasetHasIndexes(mergedDataset);
                creator.updateVersionZeroRecordCount(mergedDataset);
//                }

            }
        }
    }
}