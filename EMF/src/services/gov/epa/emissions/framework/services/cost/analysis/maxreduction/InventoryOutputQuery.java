package gov.epa.emissions.framework.services.cost.analysis.maxreduction;


public interface InventoryOutputQuery {
    
    String selectClause(String inputDatasetTableAlia, String detailResultTableAlias);
    
    String conditionalClause(String inputDatasetTableAlias, String detailResultTableAlias);

}
