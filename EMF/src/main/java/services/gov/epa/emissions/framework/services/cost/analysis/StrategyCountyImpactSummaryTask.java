package gov.epa.emissions.framework.services.cost.analysis;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.analysis.common.StrategyImpactSummaryTableFormat;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.services.cost.controlStrategy.DatasetCreator;
import gov.epa.emissions.framework.services.cost.controlStrategy.StrategyResultType;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;
import gov.epa.emissions.framework.tasks.DebugLevels;

import java.sql.SQLException;
import java.util.Date;

public class StrategyCountyImpactSummaryTask extends AbstractStrategySummaryTask {

    private ControlStrategy controlStrategy;

    private Datasource datasource;

    private ControlStrategyResult countyImpactSummaryResult;

    public StrategyCountyImpactSummaryTask(ControlStrategy controlStrategy, User user, DbServerFactory dbServerFactory,
            HibernateSessionFactory sessionFactory) throws EmfException {
        super(controlStrategy, user, dbServerFactory, sessionFactory);
        this.controlStrategy = controlStrategy;
        DbServer dbServer = dbServerFactory.getDbServer();
        this.datasource = dbServer.getEmissionsDatasource();
    }

    public void run() throws EmfException {
        String status = "";
        countyImpactSummaryResult = null;

        try {
            // create new result
            countyImpactSummaryResult = createCountyImpactSummaryResult();

            populateCountyImpactSummaryDataset(getCountySummaryResult(), countyImpactSummaryResult);
            status = "Completed";
        } catch (Exception e) {
            e.printStackTrace();
            status = "Failed. Error processing strategy impact summary: " + e.getMessage();
            setStatus(status);
        } finally {
            // update result ending info...
            if (countyImpactSummaryResult != null) {
                setSummaryResultCount(countyImpactSummaryResult);
                countyImpactSummaryResult.setCompletionTime(new Date());
                countyImpactSummaryResult.setRunStatus(status);
                saveControlStrategyResult(countyImpactSummaryResult);
            }
        }
    }

    private ControlStrategyResult getCountySummaryResult() {
        ControlStrategyResult[] results = getControlStrategyResults();
        Date latest = null;
        ControlStrategyResult toReturn = null;
        
        for (ControlStrategyResult result : results) {
            String type = result.getStrategyResultType().getName();
            
            if (type.equals(StrategyResultType.strategyCountySummary)) {
                if (latest == null) {
                    latest = result.getStartTime();
                    toReturn = result;
                    continue;
                }
                
                if (latest.before(result.getStartTime())) {
                    latest = result.getStartTime();
                    toReturn = result;
                }
            }
        }

        return toReturn;
    }

    public void afterRun() {
        // TODO: might need to index tables
    }

    public void beforeRun() {
        // TODO: might need to index tables
    }

    public ControlStrategyResult getStrategyResult() {
        // NOTE Auto-generated method stub
        return countyImpactSummaryResult;
    }

    private EmfDataset createCountyImpactSummaryDataset() throws EmfException {
        return creator.addDataset("CSIS", DatasetCreator.createDatasetName("Impact_Sum_" + controlStrategy.getName()),
                getDatasetType(DatasetType.strategyImpactSummary), new StrategyImpactSummaryTableFormat(dbServer
                        .getSqlDataTypes()), summaryResultDatasetDescription(DatasetType.strategyImpactSummary));
    }

    private ControlStrategyResult createCountyImpactSummaryResult() throws EmfException {
        ControlStrategyResult result = new ControlStrategyResult();
        result.setControlStrategyId(controlStrategy.getId());
        EmfDataset impactSummaryDataset = createCountyImpactSummaryDataset();

        result.setDetailedResultDataset(impactSummaryDataset);

        result.setStrategyResultType(getStrategyResultType(StrategyResultType.strategyImpactSummary));
        result.setStartTime(new Date());
        result.setRunStatus("Start processing impact summary result");

        // persist result
        saveControlStrategyResult(result);
        return result;
    }

    private void populateCountyImpactSummaryDataset(ControlStrategyResult countySummary,
            ControlStrategyResult countyImpactSummary) throws EmfException {
        if (countySummary == null)
            throw new EmfException("No County Summary Result to summarize.");

        EmfDataset dataset = (EmfDataset) countySummary.getDetailedResultDataset();

        if (dataset == null || dataset.getInternalSources() == null)
            throw new EmfException("County Summary Result is empty.");

        String countySummaryTable = "emissions." + dataset.getInternalSources()[0].getTable();
        String countyImpactTable = "reference.impact_estimates";
        String rsmInvSectorsTable = "reference.rsm_inv_sectors";

        String impactTableCols = "dataset_id, version, FIPS, RSM_SECTOR, INV_SECTOR, Poll, Impact_pollutant, Ton_Reduced, Annual_cost, Impact_per_ton, Impact, Cost_per_change_conc, Total_cost, Total_impact, Total_cost_per_change_conc";

        String selectSubString = "e.fips, coalesce(p.rsm_sector,'AN UNSPECIFIED RSM SECTOR') as rsm_sector, "
                + "coalesce(e.sector, 'AN UNSPECIFIED INV SECTOR') as inv_sector, "
                + "coalesce(e.poll,'AN UNSPECIFIED POLLUTANT') as poll, p.impact_pollutant, sum(emis_reduction) as ton_reduced, "
                + "sum(annual_cost) as annual_cost, p.impact_per_ton, "
                + "sum(emis_reduction) * p.impact_per_ton as impact, "
                + "sum(annual_cost) / (sum(emis_reduction) * p.impact_per_ton) as cost_per_change_conc, " +

                "(select sum(e1.annual_cost) from "
                + countySummaryTable
                + " e1 "
                + "where e1.fips=e.fips) as total_cost, "
                +

                "(select sum(e1.emis_reduction * p1.impact_per_ton) "
                + "from "
                + countySummaryTable
                + " e1 "
                + "inner join "
                + rsmInvSectorsTable
                + " s1 "
                + "on e1.sector=s1.inv_sector and e1.poll=s1.pollutant "
                + "inner join "
                + countyImpactTable
                + " p1 "
                + "on e1.fips=p1.fips and e1.poll=p1.pollutant and p1.rsm_sector=s1.rsm_sector "
                + "where e1.fips=e.fips ) as total_impact, "
                +

                "(select sum(e1.annual_cost) "
                + "from "
                + countySummaryTable
                + " e1 "
                + "where e1.fips=e.fips) / (sum(emis_reduction) * p.impact_per_ton) as total_cost_per_change_conc "
                +

                "from "
                + countySummaryTable
                + " e "
                + "inner join "
                + rsmInvSectorsTable
                + " s "
                + "on e.sector=s.inv_sector and e.poll=s.pollutant "
                + "inner join "
                + countyImpactTable
                + " p "
                + "on e.fips=p.fips and e.poll=p.pollutant "
                + "where p.rsm_sector=s.rsm_sector "
                + "group by e.fips, e.sector, e.poll, p.impact_pollutant, p.impact_per_ton, p.rsm_sector "
                + "order by e.fips";

        String sql = "INSERT INTO " + qualifiedEmissionTableName(countyImpactSummary.getDetailedResultDataset()) + " ("
                + impactTableCols + ") select " + countyImpactSummary.getDetailedResultDataset().getId() + ", 0, "
                + selectSubString;
        if (DebugLevels.DEBUG_25())
            System.out.println(sql);
        try {
            datasource.query().execute(sql);
        } catch (SQLException e) {
            throw new EmfException("Error occured when inserting data to strategy impact summary table.\n"
                    + e.getMessage());
        }
    }

    protected String qualifiedEmissionTableName(Dataset dataset) throws EmfException {
        return qualifiedName(emissionTableName(dataset));
    }

    protected String emissionTableName(Dataset dataset) {
        InternalSource[] internalSources = dataset.getInternalSources();
        return internalSources[0].getTable().toLowerCase();
    }

    private String qualifiedName(String table) throws EmfException {
        // VERSIONS TABLE - Completed - throws exception if the following case is true
        if ("emissions".equalsIgnoreCase(datasource.getName()) && "versions".equalsIgnoreCase(table.toLowerCase())) {
            throw new EmfException("Table versions moved to schema emf."); // VERSIONS TABLE
        }
        return datasource.getName() + "." + table;
    }
}