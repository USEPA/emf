package gov.epa.emissions.framework.services.cost.controlStrategy;

import gov.epa.emissions.framework.services.EmfException;

public class SQLSummarizeControlStrategiesQuery {

    public String createSummarizeQuery(int[] controlStrategyIds) throws EmfException {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT cs.name Strategy_Name, "
                + "proj.name Project, "
                + "cs.last_modified_date Last_Modified, "
                + "cs.is_final Is_Final, "
                + "cs.run_status Run_Status, "
                + "regions.name Region, "
                + "cs.total_cost Strategy_Total_Cost, "
                + "cs.reduction Strategy_Total_Reduction, "
                + "CASE WHEN cs.reduction <> 0 THEN cs.total_cost / cs.reduction ELSE NULL END Avg_Cost_Per_Ton, "
                + "users.name Creator, "
                + "types.name Strategy_Type, "
                + "cs.cost_year Cost_Yr, "
                + "cs.analysis_year Inv_Yr, "
                + "poll.name Target_Poll, "
                + "rtypes.name Result_Type, "
                + "sr.record_count Record_Count, "
                + "rdset.name Result_Dataset, "
                + "sr.run_status Result_Status, "
                + "sr.total_cost Total_Cost, "
                + "sr.total_reduction Total_Reduction, "
                + "sr.start_time Start_Time, "
                + "sr.completion_time Completion_Time, "
                + "idset.name Input_Inventory, "
                + "cidset.name Controlled_Inventory "
                + "FROM emf.control_strategies cs "
                + "LEFT JOIN emf.projects proj "
                + "ON proj.id = cs.project_id "
                + "LEFT JOIN emf.regions "
                + "ON regions.id = cs.region_id "
                + "JOIN emf.users "
                + "ON users.id = cs.creator_id "
                + "LEFT JOIN emf.strategy_types types "
                + "ON types.id = cs.strategy_type_id "
                + "LEFT JOIN emf.pollutants poll "
                + "ON poll.id = cs.pollutant_id "
                + "LEFT JOIN emf.strategy_results sr "
                + "ON sr.control_strategy_id = cs.id "
                + "LEFT JOIN emf.strategy_result_types rtypes "
                + "ON rtypes.id = sr.strategy_result_type_id "
                + "LEFT JOIN emf.datasets rdset "
                + "ON rdset.id = sr.detailed_result_dataset_id "
                + "LEFT JOIN emf.datasets idset "
                + "ON idset.id = sr.dataset_id "
                + "LEFT JOIN emf.datasets cidset "
                + "ON cidset.id = sr.controlled_inven_dataset_id "
                + "WHERE cs.id IN (");
        for (int i = 0; i < controlStrategyIds.length; i++) {
            if (i > 0) sql.append(", ");
            sql.append(controlStrategyIds[i]);
        }
        sql.append(") "
                + "AND rtypes.name = 'Strategy Detailed Result' "
                + "ORDER BY cs.name, sr.id");

        return sql.toString();
    }
    
    public static void main(String[] args) {
        try {
            System.out.println(new SQLSummarizeControlStrategiesQuery().createSummarizeQuery(new int[] {1, 2, 3}));
        } catch (EmfException e) {
            e.printStackTrace();
        }
    }
}
