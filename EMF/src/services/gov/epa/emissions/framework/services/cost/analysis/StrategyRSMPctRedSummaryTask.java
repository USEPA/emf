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
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.services.cost.controlStrategy.DatasetCreator;
import gov.epa.emissions.framework.services.cost.controlStrategy.StrategyResultType;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;
import gov.epa.emissions.framework.tasks.DebugLevels;

import java.sql.SQLException;
import java.util.Date;

public class StrategyRSMPctRedSummaryTask extends AbstractStrategySummaryTask {
    
    private ControlStrategy controlStrategy;
    
    private Datasource datasource;

//    private HibernateSessionFactory sessionFactory;
//
//    private DbServerFactory dbServerFactory;

    private DbServer dbServer;

    private ControlStrategyResult countySummaryResult;
    
    public StrategyRSMPctRedSummaryTask(ControlStrategy controlStrategy, User user, 
            DbServerFactory dbServerFactory, HibernateSessionFactory sessionFactory) throws EmfException {
        super(controlStrategy, user, dbServerFactory, sessionFactory);
        this.controlStrategy = controlStrategy;
        this.dbServer = dbServerFactory.getDbServer();
        this.datasource = dbServer.getEmissionsDatasource();

//        super(controlStrategy, user, 
//                dbServerFactory, sessionFactory);
    }

    public void run() throws EmfException {
//        super.run(loader);
        
        //run any pre processes
        try {
            beforeRun();
        } catch (Exception e) {
            e.printStackTrace();
            throw new EmfException(e.getMessage());
        } finally {
            //
        }

        String status = "";
        try {

            countySummaryResult = null;
            try {
                //create new result
                countySummaryResult = createStrategyCountySummaryResult();
                
                populateStrategyCountySummaryDataset(getControlStrategyResults(), countySummaryResult);
                status = "Completed.";
            } catch (Exception e) {
                e.printStackTrace();
                status = "Failed. Error processing strategy summary: . " + e.getMessage();
                setStatus(status);
            } finally {
                //update result ending info...
                if (countySummaryResult != null) {
                    setSummaryResultCount(countySummaryResult);
                    countySummaryResult.setCompletionTime(new Date());
                    countySummaryResult.setRunStatus(status);
                    saveControlStrategyResult(countySummaryResult);
//                    addStatus(countySummaryResult);
                }
            }

        } catch (Exception e) {
            status = "Failed. Error processing input dataset";
            e.printStackTrace();
            throw new EmfException(e.getMessage());
        } finally {
            //run any post processes
            try {
                afterRun();
            } catch (Exception e) {
                status = "Failed. Error processing input dataset";
                e.printStackTrace();
                throw new EmfException(e.getMessage());
            } finally {
                //
            }
        }
    }

    public void afterRun() {
        //TODO:  might need to index tables
    }

    public void beforeRun() {
        //TODO:  might need to index tables
    }

    public ControlStrategyResult getStrategyResult() {
        // NOTE Auto-generated method stub
        return countySummaryResult;
    }

    private EmfDataset createCountySummaryDataset() throws EmfException {
        return creator.addDataset("CSCS", 
                DatasetCreator.createDatasetName("RSM_Pct_Red_" + controlStrategy.getName()), 
                getDatasetType(DatasetType.rsmPercentReduction), 
                new StrategyRSMPctRedSummaryTableFormat(dbServer.getSqlDataTypes()), 
                summaryResultDatasetDescription(DatasetType.strategyCountySummary));
    }

    private ControlStrategyResult createStrategyCountySummaryResult() throws EmfException {
        ControlStrategyResult result = new ControlStrategyResult();
        result.setControlStrategyId(controlStrategy.getId());
        EmfDataset summaryResultDataset = createCountySummaryDataset();
        
        result.setDetailedResultDataset(summaryResultDataset);
        
        result.setStrategyResultType(getStrategyResultType(StrategyResultType.rsmPercentReduction));
        result.setStartTime(new Date());
        result.setRunStatus("Start processing summary result");

        //persist result
        saveControlStrategyResult(result);
        return result;
    }

    private void populateStrategyCountySummaryDataset(ControlStrategyResult[] results, ControlStrategyResult countySummaryResult) throws EmfException {
        if (results.length > 0) {

            //SET work_mem TO '512MB';
            String sql = "INSERT INTO " + qualifiedEmissionTableName(countySummaryResult.getDetailedResultDataset()) + " (dataset_id, version, scenario, name, region, factor_type, factor) " 
            + "select " + countySummaryResult.getDetailedResultDataset().getId() + ", 0, scenario, name, region, factor_type, factor " 
            + "from (";
            
            for (int j = 0; j < results.length; j++) {
                if (results[j].getDetailedResultDataset() != null 
                    && results[j].getStrategyResultType().getName().equals(StrategyResultType.strategyCountySummary)) {
                    String detailedresultTableName = qualifiedEmissionTableName(results[j].getDetailedResultDataset());

//                    select *
//                    from (select distinct on (substring(aa_rsm_factor, 2, 4)::int) 1::character varying(64) as scenario, 
//                        'Base'::character varying(64) as name, 
//                        'all'::character varying(64) as region, 
//                        aa_rsm_factor as factor_type,
//                        case 
//                            when sum(uncontrolled_emis) <> 0 then (sum(uncontrolled_emis) - coalesce(sum(emis_reduction),0)) / sum(uncontrolled_emis) else 1.0
//                        end as factor
//                    from reference.rsm_inv_sectors s
//                        left outer join emissions.CSCS__Strat_County_Sum__20080925005704622_20080925005704625 e
//                        on e.sector=s.inv_sector 
//                        and e.poll=s.pollutant
//                    and substring(e.fips,1 , 2) not in ('72','78','99','70','69','68','66','64','60','02','15')
//                        and e.fips not in (select fips from reference.rsm_naa_fips) 
//                    group by aa_rsm_factor
//                    --order by substring(aa_rsm_factor, 2, 4)::int
//
//                    union all
//
//                    select distinct on (substring(naa_rsm_factor, 2, 4)::int) 1::character varying(64) as scenario, 
//                        'Base'::character varying(64) as name, 
//                        'all'::character varying(64) as region, 
//                        naa_rsm_factor as factor_type,
//                        case 
//                            when sum(uncontrolled_emis) <> 0 then (sum(uncontrolled_emis) - coalesce(sum(emis_reduction),0)) / sum(uncontrolled_emis) else 1.0
//                        end as factor
//                    from reference.rsm_inv_sectors s
//                        left outer join emissions.CSCS__Strat_County_Sum__20080925005704622_20080925005704625 e
//                        on e.sector=s.inv_sector 
//                        and e.poll=s.pollutant
//                    and substring(e.fips,1 , 2) not in ('72','78','99','70','69','68','66','64','60','02','15')
//                        and e.fips in (select fips from reference.rsm_naa_fips) 
//                    group by naa_rsm_factor
//                    ) tbl
//                    order by substring(factor_type, 2, 4)::int
                    
                    sql += "select distinct on (substring(aa_rsm_factor, 2, 4)::int) 1::character varying(64) as scenario, " 
                        + "'Control'::character varying(64) as name, "
                        + "'all'::character varying(64) as region, "
                        + "aa_rsm_factor as factor_type, "
                        + "case "
                        + "    when sum(uncontrolled_emis) <> 0 then (sum(uncontrolled_emis) - coalesce(sum(emis_reduction),0)) / sum(uncontrolled_emis) else 1.0 "
                        + "end as factor "
                        + "from reference.rsm_inv_sectors s "
                        + "left outer join " + detailedresultTableName + " e "
                        + "on e.sector=s.inv_sector " 
                        + "and e.poll=s.pollutant "
                        + "and substring(e.fips,1 , 2) not in ('72','78','99','70','69','68','66','64','60','02','15') "
                        + "and e.fips not in (select fips from reference.rsm_naa_fips) " 
                        + "group by aa_rsm_factor "

                        + "union all "

                        + "select distinct on (substring(naa_rsm_factor, 2, 4)::int) 1::character varying(64) as scenario, " 
                        + "'Base'::character varying(64) as name, "
                        + "'all'::character varying(64) as region, "
                        + "naa_rsm_factor as factor_type, "
                        + "case "
                        + "    when sum(uncontrolled_emis) <> 0 then (sum(uncontrolled_emis) - coalesce(sum(emis_reduction),0)) / sum(uncontrolled_emis) else 1.0 "
                        + "end as factor "
                        + "from reference.rsm_inv_sectors s "
                        + "left outer join " + detailedresultTableName + " e "
                        + "on e.sector=s.inv_sector "
                        + "and e.poll=s.pollutant "
                        + "and substring(e.fips,1 , 2) not in ('72','78','99','70','69','68','66','64','60','02','15') "
                        + "and e.fips in (select fips from reference.rsm_naa_fips) "
                        + "group by naa_rsm_factor "
                    + ") tbl "
                    + "order by substring(factor_type, 2, 4)::int ";
                    break;
                    }
                }
            
            if (DebugLevels.DEBUG_25())
                System.out.println(sql);
            try {
                datasource.query().execute(sql);
            } catch (SQLException e) {
                throw new EmfException("Error occured when inserting data to strategy summary table" + "\n" + e.getMessage());
            }
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