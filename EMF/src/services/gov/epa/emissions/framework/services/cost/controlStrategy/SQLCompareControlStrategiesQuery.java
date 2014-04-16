package gov.epa.emissions.framework.services.cost.controlStrategy;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

public class SQLCompareControlStrategiesQuery {

    public SQLCompareControlStrategiesQuery() {
        //
    }

    public String createCompareQuery(int[] controlStrategyIds) throws EmfException {
        StringBuilder sql = new StringBuilder();
        String tabSelectList = "cs0.tab";
        String nameSelectList = "cs0.\"name\"";
        String matchSelectList = "coalesce(cs0.\"value\", '') = coalesce(cs0.\"value\", '')";
        String valueSelectList = "regexp_replace(regexp_replace(regexp_replace(cs0.\"value\", '\n', ' ', 'gi'), ',', '', 'gi'), '\"', '', 'gi') as \"strategy_1\"";
        sql.append("select ");
        for (int i = 1; i < controlStrategyIds.length; ++i) {
            tabSelectList += ",cs" + i + ".tab";
            nameSelectList += ",cs" + i + ".\"name\"";
            matchSelectList += "and coalesce(cs0.\"value\", '') = coalesce(cs" + i + ".\"value\", '')";
            valueSelectList += ", regexp_replace(regexp_replace(cs" + i + ".\"value\", '\n', ' ', 'gi'), '\"', '', 'gi') as \"strategy_" + (i + 1) + "\"";
        }
        sql.append(" coalesce(" + tabSelectList + ") as tab, ");
        sql.append(" regexp_replace(regexp_replace(coalesce(" + nameSelectList + "), '\n', ' ', 'gi'), '\"', '', 'gi') as \"name\", ");
        sql.append(" case when " + matchSelectList + " then 'true'::character varying(5) else 'false'::character varying(5) end as match,");
        sql.append(" " + valueSelectList);
        sql.append(" from ");
        String tabRunningSelectList = "cs0.tab";
        String nameRunningSelectList = "cs0.\"name\"";
        for (int i = 0; i < controlStrategyIds.length; ++i) {
            //add join condition
            if (i > 0)
                sql.append(" full join ");
            sql.append(" (");
            sql.append(buildIndividualControlStrategyQuery(controlStrategyIds[i]));
            sql.append(" ) as cs" + i);
            //add join condition
            if (i > 0) {
                sql.append(" on coalesce(cs" + i + ".tab, '') = coalesce(" + tabRunningSelectList + ", '')");
                sql.append(" and coalesce(cs" + i + ".name, '') = coalesce(" + nameRunningSelectList + ", '')");
            }
            tabRunningSelectList += ",cs" + i + ".tab";
            nameRunningSelectList += ",cs" + i + ".\"name\"";
        }

        sql.append(" order by case when coalesce(" + tabSelectList + ") = 'Summary' then 0 when coalesce(" + tabSelectList + ") = 'Inventories' then 1 when coalesce(" + tabSelectList + ") = 'Measures' then 2 when coalesce(" + tabSelectList + ") = 'Programs' then 3 else 4 end,"); 
        sql.append(" coalesce(" + nameSelectList + ")");

        return sql.toString();
    }

    private StringBuilder buildIndividualControlStrategyQuery(int controlStrategyId) {
        StringBuilder sql = new StringBuilder();
        
        
//Summary Tab Info being flattened out...
        sql.append("select ");
        sql.append("'Summary' as tab,  ");
        sql.append("summary_tab.\"key\" as \"name\",  ");
        sql.append("summary_tab.\"value\" ");
        sql.append("from ( ");
        sql.append(buildGetControlStrategyColumnValueSQL(controlStrategyId, "Name", "name"));
        sql.append(" union all " + buildGetControlStrategyColumnValueSQL(controlStrategyId, "Run Status", "run_status"));
        sql.append(" union all " + buildGetControlStrategyColumnValueSQL(controlStrategyId, "Discount Rate", "discount_rate"));
        sql.append(" union all " + buildGetControlStrategyColumnValueSQL(controlStrategyId, "Cost Year", "cost_year"));
        sql.append(" union all " + buildGetControlStrategyColumnValueSQL(controlStrategyId, "Analysis Year", "analysis_year"));
        sql.append(" union all " + buildGetControlStrategyColumnValueSQL(controlStrategyId, "Last Modified Date", "last_modified_date"));
        sql.append(" union all " + buildGetControlStrategyColumnValueSQL(controlStrategyId, "Start Date", "start_date"));
        sql.append(" union all " + buildGetControlStrategyColumnValueSQL(controlStrategyId, "Completion Date", "start_date"));
        sql.append(" union all " + buildGetControlStrategyColumnValueSQL(controlStrategyId, "Use Cost Equations", "use_cost_equations"));
        sql.append(" union all " + buildGetControlStrategyColumnValueSQL(controlStrategyId, "Copied From", "copied_from"));
        sql.append(" union all " + buildGetControlStrategyColumnValueSQL(controlStrategyId, "Include Unspecified Costs", "include_unspecified_costs"));
        sql.append(" union all " + buildGetControlStrategyColumnValueSQL(controlStrategyId, "Is Final", "is_final"));
        sql.append(" union all " + buildGetControlStrategyColumnValueSQL(controlStrategyId, "Apply CAP Measures on HAP Pollutants", "apply_cap_measures_on_hap_pollutants"));

        sql.append(" union all SELECT 'Project' as \"key\", projects.name as \"value\" FROM emf.control_strategies left outer join emf.projects on projects.id = control_strategies.project_id where control_strategies.id = " + controlStrategyId);
        sql.append(" union all SELECT 'Creator' as \"key\", users.\"name\" as \"value\" FROM emf.control_strategies inner join emf.users on users.id = control_strategies.creator_id where control_strategies.id = " + controlStrategyId);
        sql.append(" union all SELECT 'Region' as \"key\", regions.\"name\" as \"value\" FROM emf.control_strategies left outer join emf.regions on regions.id = control_strategies.creator_id where control_strategies.id = " + controlStrategyId);
        sql.append(" union all SELECT 'Strategy Type' as \"key\", strategy_types.\"name\" as \"value\" FROM emf.control_strategies inner join emf.strategy_types on strategy_types.id = control_strategies.strategy_type_id where control_strategies.id = " + controlStrategyId);
        sql.append(" union all SELECT 'Target Pollutant(s)' as \"key\", coalesce((SELECT string_agg(pollutants.name, ', ' ORDER BY pollutants.name) as \"value\" FROM emf.control_strategy_target_pollutants inner join emf.pollutants on pollutants.id = control_strategy_target_pollutants.pollutant_id where control_strategy_target_pollutants.control_strategy_id = " + controlStrategyId + "), (select pollutants.\"name\" FROM emf.control_strategies inner join emf.pollutants on pollutants.id = control_strategies.pollutant_id where control_strategies.id = " + controlStrategyId + ")) as \"value\"");

        sql.append(" ) summary_tab");

        
//Inventories Tab Data
        sql.append(" union all SELECT 'Inventories' as tab, * from (" + buildGetControlStrategyColumnValueSQL(controlStrategyId, "Inventory Filter", "filter") + ") tbl");
        sql.append(" union all SELECT 'Inventories' as tab, 'County Dataset' as \"name\", datasets.name as \"value\" FROM emf.control_strategies left outer join emf.datasets on datasets.id = control_strategies.county_dataset_id where control_strategies.id = " + controlStrategyId);
        sql.append(" union all SELECT 'Inventories' as tab, * from (" + buildGetControlStrategyColumnValueSQL(controlStrategyId, "County Dataset Version", "county_dataset_version") + ") tbl");

        sql.append(" union all select ");
        sql.append(" 'Inventories' as tab, ");
        sql.append("'Inventory, ' || dataset_types.name || ', ' || datasets.name as \"name\", ");
        sql.append(" datasets.name || ' [v' || coalesce(input_datasets_control_strategies.dataset_version || '', '') || ']' as \"value\"");

        sql.append(" from emf.input_datasets_control_strategies");

        sql.append(" left outer join emf.datasets");
        sql.append(" on datasets.id = input_datasets_control_strategies.dataset_id");

        sql.append(" left outer join emf.dataset_types");
        sql.append(" on dataset_types.id = datasets.dataset_type");

        sql.append(" where input_datasets_control_strategies.control_strategy_id = " + controlStrategyId);

//Control Programs Tab Data
        sql.append(" union all select");
        sql.append(" 'Programs' as tab, ");
        sql.append(" 'Control Program, ' || control_program_types.name || ', ' || control_programs.name as \"name\", ");
        sql.append(" datasets.name || ' [v' || coalesce(control_programs.dataset_version || '', '') || ']' as \"value\"");

        sql.append(" from emf.control_strategy_programs");

        sql.append(" left outer join emf.control_programs ");
        sql.append(" on control_programs.id = control_strategy_programs.control_program_id");

        sql.append(" left outer join emf.control_program_types ");
        sql.append(" on control_program_types.id = control_programs.control_program_type_id");

        sql.append(" left outer join emf.datasets");
        sql.append(" on datasets.id = control_programs.dataset_id");

        sql.append(" where control_strategy_programs.control_strategy_id = " + controlStrategyId);
        

//--Measures Tab Data
        sql.append(" union all SELECT 'Measures' as tab, 'Classes' as \"name\", string_agg(control_measure_classes.name, ',' ORDER BY control_measure_classes.name) as \"value\" FROM emf.control_strategy_classes inner join emf.control_measure_classes on control_measure_classes.id = control_strategy_classes.control_measure_class_id where control_strategy_classes.control_strategy_id = " + controlStrategyId);

        sql.append(" union all select");
        sql.append(" 'Measures' as tab, ");
        sql.append(" control_measures.abbreviation as \"name\",");
        sql.append(" control_measures.\"name\" as \"value\"");

        sql.append(" from emf.control_strategy_measures");

        sql.append(" left outer join emf.control_measures");
        sql.append(" on control_measures.id = control_strategy_measures.control_measure_id");

        sql.append(" where control_strategy_measures.control_strategy_id = " + controlStrategyId);

//Constraints Tab Info being flattened out...
        sql.append(" union all select ");
        sql.append("'Constraints' as tab,  ");
        sql.append("constraints_tab.\"key\" as \"name\",  ");
        sql.append("constraints_tab.\"value\" ");
        sql.append("from ( ");
        sql.append(buildGetControlStrategyConstraintColumnValueSQL(controlStrategyId, "Minimum Emissions Reduction (tons)", "max_emis_reduction"));
        sql.append(" union all " + buildGetControlStrategyConstraintColumnValueSQL(controlStrategyId, "Minimum Control Efficiency (%)", "max_control_efficiency"));
        sql.append(" union all " + buildGetControlStrategyConstraintColumnValueSQL(controlStrategyId, "Maximum 2006 Cost per Ton ($/ton)", "min_cost_per_ton"));
        sql.append(" union all " + buildGetControlStrategyConstraintColumnValueSQL(controlStrategyId, "Maximum 2006 Annualized Cost ($/yr)", "min_ann_cost"));
        sql.append(" union all " + buildGetControlStrategyConstraintColumnValueSQL(controlStrategyId, "Minimum Percent Reduction Difference for Replacement Control (%)", "replacement_control_min_eff_diff"));
        sql.append(" union all " + buildGetControlStrategyConstraintColumnValueSQL(controlStrategyId, "Minimum Percent Reduction Difference for Predicting Controls (%) - Project Future Year Specific", "control_program_measure_min_pct_red_diff"));
        sql.append(" union all " + buildGetControlStrategyConstraintColumnValueSQL(controlStrategyId, "Domain Wide Emission Reduction (tons) - Least Cost Specific", "domain_wide_emis_reduction"));
        sql.append(" union all " + buildGetControlStrategyConstraintColumnValueSQL(controlStrategyId, "Domain Wide Percent Reduction (%) - Least Cost Specific", "domain_wide_pct_reduction"));
        sql.append(" union all " + buildGetControlStrategyConstraintColumnValueSQL(controlStrategyId, "Domain Wide Percent Reduction Increment (%) - Least Cost Curve Specific", "domain_wide_pct_reduction_increment"));
        sql.append(" union all " + buildGetControlStrategyConstraintColumnValueSQL(controlStrategyId, "Domain Wide Percent Reduction Start (%) - Least Cost Curve Specific", "domain_wide_pct_reduction_start"));
        sql.append(" union all " + buildGetControlStrategyConstraintColumnValueSQL(controlStrategyId, "Domain Wide Percent Reduction End (%) - Least Cost Curve Specific", "domain_wide_pct_reduction_end"));

        sql.append(" ) constraints_tab");

        return sql;
    }
    
    private String buildGetControlStrategyColumnValueSQL(int controlStrategyId, String columnLabel, String columnName) {
        return "SELECT '" + columnLabel + "' as \"key\", \"" + columnName + "\"::text || '' as \"value\" FROM emf.control_strategies where id = " + controlStrategyId;
    }
    
    private String buildGetControlStrategyConstraintColumnValueSQL(int controlStrategyId, String columnLabel, String columnName) {
        return "SELECT '" + columnLabel + "' as \"key\", \"" + columnName + "\"::text || '' as \"value\" FROM emf.control_strategy_constraints where control_strategy_id = " + controlStrategyId;
    }
    
    public static void main(String[] args) {
        
        try {
            System.out.println(new SQLCompareControlStrategiesQuery().createCompareQuery(new int[] {7, 8}));
        } catch (EmfException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }
    }
}
