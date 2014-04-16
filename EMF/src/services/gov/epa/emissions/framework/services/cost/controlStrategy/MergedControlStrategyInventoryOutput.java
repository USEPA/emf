package gov.epa.emissions.framework.services.cost.controlStrategy;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.TableFormat;
import gov.epa.emissions.commons.io.VersionedQuery;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.ControlStrategyDAO;
import gov.epa.emissions.framework.services.cost.ControlStrategyInputDataset;
import gov.epa.emissions.framework.services.cost.StrategyType;
import gov.epa.emissions.framework.services.cost.controlStrategy.AbstractControlStrategyInventoryOutput;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.services.cost.controlStrategy.FileFormatFactory;
import gov.epa.emissions.framework.services.cost.controlStrategy.StrategyResultType;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;
import gov.epa.emissions.framework.tasks.DebugLevels;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Date;

import org.hibernate.Session;

public class MergedControlStrategyInventoryOutput extends AbstractControlStrategyInventoryOutput {
   
    public MergedControlStrategyInventoryOutput(User user, ControlStrategy controlStrategy,
            ControlStrategyResult controlStrategyResult, String namePrefix, 
            HibernateSessionFactory sessionFactory, DbServerFactory dbServerFactory) throws Exception {
        super(user, controlStrategy,
                controlStrategyResult, namePrefix,
                sessionFactory, dbServerFactory);
    }

    public void create() throws Exception {
        createInventories();
    }

    protected void createInventories() throws EmfException, Exception, SQLException {
        startStatus(statusServices);
        try {

            ControlStrategyInputDataset[] inventories = controlStrategy.getControlStrategyInputDatasets();
            //we need to create a controlled inventory for each invnentory, except the merged inventory
            for (int i = 0; i < inventories.length; i++) {
//                EmfDataset inventory = inventories[i].getInputDataset();
                this.inputDataset = inventories[i].getInputDataset();
                if (!inputDataset.getDatasetType().getName().equals(DatasetType.orlMergedInventory)) {
                    tableFormat = new FileFormatFactory(dbServer).tableFormat(inputDataset.getDatasetType());
                    //create controlled inventory dataset
                    EmfDataset dataset = creator.addControlledInventoryDataset(creator.createControlledInventoryDatasetName(namePrefix, inputDataset), 
                            inputDataset, inputDataset.getDatasetType(), 
                            tableFormat, description(inputDataset));
                    //get table name
                    String outputInventoryTableName = getDatasetTableName(dataset);

                    //create strategy result for each controlled inventory
                    ControlStrategyResult result = createControlStrategyResult(inventories[i], dataset, 
                            getControlledInventoryStrategyResultType());

                    createControlledInventory(dataset.getId(), getDatasetTableName(inputDataset), 
                            detailDatasetTable(controlStrategyResult), outputInventoryTableName, 
                            version(inputDataset, inventories[i].getVersion()), datasource, 
                            tableFormat);

                    //set the cont inv record count
                    setResultRecordCount(result);
                    
                    result.setCompletionTime(new Date());
                    result.setRunStatus("Completed.");
                    saveControlStrategyResult(result);
                    updateVersion(dataset, dbServer, sessionFactory.getSession(), user);
                }
            }
        } catch (Exception e) {
            failStatus(statusServices, e.getMessage());
            e.printStackTrace();
            throw e;
        } finally {
//            setandRunQASteps();
        }
        dbServer.disconnect();
    }

    private void createControlledInventory(int datasetId, String inputTable, 
            String detailResultTable, String outputTable, 
            Version version, Datasource datasource,
            TableFormat tableFormat) throws EmfException {
        String query = populateInventory(datasetId, inputTable, 
                detailResultTable, outputTable,
                version(inputDataset, controlStrategyResult.getInputDatasetVersion()), inputDataset, 
                datasource, tableFormat);
        try {
            datasource.query().execute(query);
        } catch (SQLException e) {
            throw new EmfException("Error occured when copying data from " + inputTable + " to "
                    + outputTable + "\n" + e.getMessage());
        }
    }

    private String populateInventory(int contInvDatasetId, String invTableName, 
            String detailResultTableName, String contInvTableName, 
            Version invVersion, Dataset invDataset, 
            Datasource datasource, TableFormat invTableFormat) throws EmfException {
        VersionedQuery invVersionedQuery = new VersionedQuery(invVersion);
        int month = inputDataset.applicableMonth();
        int noOfDaysInMonth = 31;
        boolean isMonthlyInventory = false;
        if (month != -1) {
            noOfDaysInMonth = getDaysInMonth(month);
            isMonthlyInventory = true;
        }
        String sql = "select ";
        String columnList = "";
        Column[] columns = invTableFormat.cols();
        ResultSetMetaData md = getResultSetMetaData(qualifiedTable(invTableName, datasource));
        boolean hasControlMeasuresColumn = false;
        boolean hasPctReductionColumn = false;
        boolean hasCumulativeCostColumn = false;
        //flag indicating if we are doing replacement vs
        //add on controls., currently we only support replacement controls.
        boolean isReplacementControl = !controlStrategy.getStrategyType().getName().equals(StrategyType.applyMeasuresInSeries);
        try {
            for (int i = 1; i < md.getColumnCount(); i++) {
                if (md.getColumnName(i).equalsIgnoreCase("CONTROL_MEASURES")) 
                    hasControlMeasuresColumn = true;
                else if (md.getColumnName(i).equalsIgnoreCase("PCT_REDUCTION")) 
                    hasPctReductionColumn = true;
                else if (md.getColumnName(i).equalsIgnoreCase("CUMULATIVE_COST")) 
                    hasCumulativeCostColumn = true;
            }
        } catch (SQLException e) {
            //
        }
        //right before abbreviation, is an empty now...
        for (int i = 0; i < columns.length; i++) {
            String columnName = columns[i].name();
            if (columnName.equalsIgnoreCase("record_id")) {
//              sql += "record_id";
//              columnList += "record_id";
          } else if (columnName.equalsIgnoreCase("dataset_id")) {
//            sql += "," + datasetId + " as dataset_id";
//            columnList += ",dataset_id";
              sql += contInvDatasetId + " as dataset_id";
              columnList += "dataset_id";
            } else if (columnName.equalsIgnoreCase("delete_versions")) {
                sql += ", '' as delete_versions";
                columnList += "," + columnName;
            } else if (columnName.equalsIgnoreCase("version")) {
                sql += ", 0 as version";
                columnList += "," + columnName;
            } else if (columnName.equalsIgnoreCase("ceff")) {
                sql += ", case when b.source_id is not null then case when coalesce(b.starting_emissions, 0.0) <> 0.0 then (1- b.final_emissions / b.starting_emissions) * 100 else null::double precision end else ceff end as ceff";
                columnList += "," + columnName;
            } else if (columnName.equalsIgnoreCase("ann_pct_red")) {//ff10 column
                sql += ", case when b.source_id is not null then case when coalesce(b.starting_emissions, 0.0) <> 0.0 then (1- b.final_emissions / b.starting_emissions) * 100 else null::double precision end else ann_pct_red end as ann_pct_red";
                columnList += "," + columnName;
            } else if (columnName.equalsIgnoreCase("avd_emis")) {
                sql += ", case when b.source_id is not null then b.final_emissions / " + (month != -1 ? noOfDaysInMonth : "365") + " else avd_emis end as avd_emis";
                columnList += "," + columnName;
            } else if (columnName.equalsIgnoreCase("ann_emis")) {
                if (isMonthlyInventory)
                    sql += ", -9.0::double precision as ann_emis";
                else
                    sql += ", case when b.source_id is not null then b.final_emissions else ann_emis end as ann_emis";
                columnList += "," + columnName;
            } else if (columnName.equalsIgnoreCase("ann_value")) {//ff10 column
                sql += ", case when b.source_id is not null then b.final_emissions else ann_value end as ann_value";
                columnList += "," + columnName;
            } else if (columnName.equalsIgnoreCase("jan_value")
                    || columnName.equalsIgnoreCase("feb_value")
                    || columnName.equalsIgnoreCase("mar_value")
                    || columnName.equalsIgnoreCase("apr_value")
                    || columnName.equalsIgnoreCase("may_value")
                    || columnName.equalsIgnoreCase("jun_value")
                    || columnName.equalsIgnoreCase("jul_value")
                    || columnName.equalsIgnoreCase("aug_value")
                    || columnName.equalsIgnoreCase("sep_value")
                    || columnName.equalsIgnoreCase("oct_value")
                    || columnName.equalsIgnoreCase("nov_value")
                    || columnName.equalsIgnoreCase("dec_value")
                    ) {//ff10 column
                sql += ", case when b.source_id is not null then case when coalesce(b.starting_emissions, 0.0) <> 0.0 then (b.final_emissions / b.starting_emissions) else null::double precision end else 1.0::double precision end * " + columnName + " as " + columnName;
                columnList += "," + columnName;
            } else if (columnName.equalsIgnoreCase("jan_pctred")
                    || columnName.equalsIgnoreCase("feb_pctred")
                    || columnName.equalsIgnoreCase("mar_pctred")
                    || columnName.equalsIgnoreCase("apr_pctred")
                    || columnName.equalsIgnoreCase("may_pctred")
                    || columnName.equalsIgnoreCase("jun_pctred")
                    || columnName.equalsIgnoreCase("jul_pctred")
                    || columnName.equalsIgnoreCase("aug_pctred")
                    || columnName.equalsIgnoreCase("sep_pctred")
                    || columnName.equalsIgnoreCase("oct_pctred")
                    || columnName.equalsIgnoreCase("nov_pctred")
                    || columnName.equalsIgnoreCase("dec_pctred")
                    ) {//ff10 column
                sql += ", case when b.source_id is not null then case when coalesce(b.starting_emissions, 0.0) <> 0.0 then (1- b.final_emissions / b.starting_emissions) * 100.0 else null::double precision end else " + columnName + " end as " + columnName;
                columnList += "," + columnName;
            } else if (columnName.equalsIgnoreCase("reff")) {
                sql += ", case when b.source_id is not null then 100 else reff end as reff";
                columnList += "," + columnName;
            } else if (columnName.equalsIgnoreCase("rpen")) {
                sql += ", case when b.source_id is not null then 100 else rpen end as rpen";
                columnList += "," + columnName;
            } else if (columnName.equalsIgnoreCase("CONTROL_MEASURES")) {
                if (isReplacementControl)
                    sql += ", cm_abbrev_list as CONTROL_MEASURES";
                else
                    sql += ", case when " + (!hasControlMeasuresColumn ? "control_measures" : "null") + " is null or length(" + (!hasControlMeasuresColumn ? "control_measures" : "null") + ") = 0 then cm_abbrev_list else " + (!hasControlMeasuresColumn ? "control_measures" : "null") + " || '&' || cm_abbrev_list end as CONTROL_MEASURES";
                columnList += "," + columnName;
            } else if (columnName.equalsIgnoreCase("PCT_REDUCTION")) {
                if (isReplacementControl)
                    sql += ", percent_reduction_list as PCT_REDUCTION";
                else
                    sql += ", case when " + (!hasPctReductionColumn ? "pct_reduction" : "null") + " is null or length(" + (!hasPctReductionColumn ? "pct_reduction" : "null") + ") = 0 then percent_reduction_list else " + (!hasPctReductionColumn ? "pct_reduction" : "null") + " || '&' || percent_reduction_list end as PCT_REDUCTION";
                columnList += "," + columnName;
            } else if (columnName.equalsIgnoreCase("CURRENT_COST")) {
                sql += ", annual_cost as CURRENT_COST";
                columnList += "," + columnName;
            } else if (columnName.equalsIgnoreCase("CUMULATIVE_COST")) {
                if (isReplacementControl)
                    sql += ", case when b.source_id is not null then " + (!hasCumulativeCostColumn ? "case when cumulative_cost is null then annual_cost when cumulative_cost is not null then coalesce(annual_cost, 0.0) end" : "annual_cost") + " else " + (!hasCumulativeCostColumn ? "cumulative_cost" : "null::double precision") + " end as CUMULATIVE_COST";
                else
                    sql += ", case when b.source_id is not null then " + (!hasCumulativeCostColumn ? "case when cumulative_cost is null then annual_cost when cumulative_cost is not null then cumulative_cost + coalesce(annual_cost, 0.0) end" : "annual_cost") + " else " + (!hasCumulativeCostColumn ? "cumulative_cost" : "null::double precision") + " end as CUMULATIVE_COST";
                columnList += "," + columnName;
            } else {
                sql += ", " + columnName;
                columnList += "," + columnName;
            }
        }
        sql += " FROM " + qualifiedTable(invTableName, datasource) + " as inv ";
        sql += " left outer join ( "
        + "SELECT source_id, "
        + "min(final_emissions) as final_emissions, "
        + "max(input_emis) as starting_emissions, "
        + "sum(annual_cost) as annual_cost, "
        + "public.concatenate_with_ampersand(cm_abbrev) as cm_abbrev_list, "
        + "public.concatenate_with_ampersand(percent_reduction::varchar) as percent_reduction_list "
        + "FROM (select source_id, input_emis, final_emissions, annual_cost, cm_abbrev, percent_reduction "
        + "        FROM " + qualifiedTable(detailResultTableName, datasource)
        + "        WHERE ORIGINAL_DATASET_ID = " + invDataset.getId()
        + "        order by source_id, apply_order "
        + "        ) tbl "
        + "    group by source_id ) as b "
        + "on inv.record_id = b.source_id"
        + " WHERE " + invVersionedQuery.query();
        sql = "INSERT INTO " + qualifiedTable(contInvTableName, datasource) + " (" + columnList + ") " + sql;
        if (DebugLevels.DEBUG_25()) 
            System.out.println(sql);
        return sql;
    }

    protected ControlStrategyResult createControlStrategyResult(ControlStrategyInputDataset inventory, EmfDataset controlledInventory,
            StrategyResultType strategyResultType) throws EmfException {
        ControlStrategyResult result = new ControlStrategyResult();
        result.setControlStrategyId(controlStrategy.getId());
        
        result.setInputDataset(inventory.getInputDataset());
        result.setInputDatasetVersion(inventory.getVersion());
        result.setDetailedResultDataset(controlledInventory);
        result.setStrategyResultType(strategyResultType);
        result.setStartTime(new Date());
        result.setRunStatus("Start processing controlled inventory");

        //persist result
        addControlStrategyResult(result);
        return result;
    }

    protected StrategyResultType getControlledInventoryStrategyResultType() throws EmfException {
        StrategyResultType strategyResultType = null;
        Session session = sessionFactory.getSession();
        try {
            strategyResultType = new ControlStrategyDAO().getStrategyResultType(StrategyResultType.controlledInventory, session);
        } catch (RuntimeException e) {
            throw new EmfException("Could not get strategy result type");
        } finally {
            session.close();
        }
        return strategyResultType;
    }
    
    private ResultSetMetaData getResultSetMetaData(String qualifiedTableName) throws EmfException {
        ResultSet rs;
        ResultSetMetaData md;
        try {
            rs = datasource.query().executeQuery("select * from " + qualifiedTableName);
            md = rs.getMetaData();
        } catch (SQLException e) {
            throw new EmfException(e.getMessage());
        }
        return md;
    }

    private void setResultRecordCount(ControlStrategyResult controlStrategyResult) throws EmfException {
        String query = "SELECT count(1) as record_count "
            + " FROM " + qualifiedEmissionTableName(controlStrategyResult.getDetailedResultDataset());
        ResultSet rs = null;
        if (DebugLevels.DEBUG_25()) 
            System.out.println(System.currentTimeMillis() + " " + query);
        try {
            rs = datasource.query().executeQuery(query);
            while (rs.next()) {
                controlStrategyResult.setRecordCount(rs.getInt(1));
            }
        } catch (SQLException e) {
            throw new EmfException("Could not execute query -" + query + "\n" + e.getMessage());
        } finally {
            if (rs != null)
                try {
                    rs.close();
                } catch (SQLException e) {
                    //
                }
        }
    }
    
    private String qualifiedEmissionTableName(Dataset dataset) {
        return qualifiedName(emissionTableName(dataset));
    }

    private String emissionTableName(Dataset dataset) {
        InternalSource[] internalSources = dataset.getInternalSources();
        return internalSources[0].getTable();
    }

    private String qualifiedName(String table) {
        if ("versions".equalsIgnoreCase(table.toLowerCase()) && "emissions".equalsIgnoreCase(datasource.getName().toLowerCase())) {
            System.err.println("Versions table moved to EMF. Error in " + this.getClass().getName());
        }
        return datasource.getName() + "." + table;
    }
}