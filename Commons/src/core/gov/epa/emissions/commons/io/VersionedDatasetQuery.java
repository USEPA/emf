package gov.epa.emissions.commons.io;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.TableMetaData;
import gov.epa.emissions.commons.db.version.Version;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VersionedDatasetQuery implements ExportStatement {

    private DatasetType datasetType;

    private VersionedQuery versionedQuery;

    private Version version;

    private Dataset dataset;

    public VersionedDatasetQuery(Version version, Dataset dataset) {
        this.datasetType = dataset.getDatasetType();
        versionedQuery = new VersionedQuery(version);
        this.version = version;
        this.dataset = dataset;
    }
    
    public String generate(String table, String rowFilters) {
        if (rowFilters.trim().length()>0)
            return "SELECT * FROM " + table + " WHERE " + versionedQuery.query() + " AND " + rowFilters+ orderByClause();

        return "SELECT * FROM " + table + " WHERE " + versionedQuery.query() + orderByClause();
    }
    
    public String versionWhereClause() {
        return " WHERE " + versionedQuery.query() + orderByClause();
    }
    
    public String generateFilteringQuery(String colString, String table, String filter) {
        if (filter == null || filter.trim().isEmpty())
            filter = "";
        else
            filter = " AND " + filter;
        
        return "SELECT " + colString + " FROM " + table + " WHERE " + versionedQuery.query() + filter + orderByClause();
    }

    public String generateFilteringQueryWithoutOrderBy(String colString, String table, String filter) {
        if (filter == null || filter.trim().isEmpty())
            filter = "";
        else
            filter = " AND " + filter;
        
        return "SELECT " + colString + " FROM " + table + " WHERE " + versionedQuery.query() + filter;
    }
    
    public String getVersionQuery() {
        return versionedQuery.query();
    }
    
    public String getDefaultOrderByClasue() {
        return orderByClause();
    }

    private String orderByClause() {
        String defaultSortOrder = datasetType.getDefaultSortOrder();
        if (defaultSortOrder != null) {
            defaultSortOrder = defaultSortOrder.trim();
            if (defaultSortOrder.length() > 0)
                return " ORDER BY " + defaultSortOrder + ", record_id";
        }

        return " ORDER BY record_id"; // TODO: check if the record_id exists in every dataset
    }



    public String generate(Datasource datasource, String table, String rowFilters, Dataset filterDataset, Version filterDatasetVersion, String filterDatasetJoinCondition) throws Exception {
        String sqlFilterDatasetJoinCondition = "";
        String filterTable = "";
        if ( datasource != null && 
             filterDataset != null && 
             filterDatasetVersion != null && 
             filterDatasetJoinCondition != null && 
             filterDataset.getInternalSources() != null && 
             filterDataset.getInternalSources()[0] != null) {
            if (filterDataset != null && filterDatasetVersion != null && filterDatasetJoinCondition != null) {
                StringTokenizer tokenizer = new StringTokenizer(filterDatasetJoinCondition, "\n");
                filterTable = datasource.getName() + "." + filterDataset.getInternalSources()[0].getTable();

                Map<String, Column> datasetColumns;
                Map<String, Column> filterDatasetColumns = null;
        
                //get columns that represent both the dataset to be exported and the filter dataset
                datasetColumns = getDatasetColumnMap(datasource, table.split("\\.")[1]);
                if ( filterTable.length() > 0){
                    filterDatasetColumns = getDatasetColumnMap(datasource, filterTable.split("\\.")[1]);
                    while (tokenizer.hasMoreTokens()) {
                        String[] filterDatasetJoinConditionToken = tokenizer.nextToken().trim().split("\\=");
                        sqlFilterDatasetJoinCondition += " AND " + aliasExpression(dataset, filterDatasetJoinConditionToken[0], datasetColumns, "t") + "= " + aliasExpression(filterDataset, filterDatasetJoinConditionToken[1], filterDatasetColumns, "f");
                    }
                }
                    

            }
        }
        VersionedQuery filterDatasetVersionedQuery = (filterDataset != null && filterDatasetVersion != null ? new VersionedQuery(filterDatasetVersion, "f") : null);
        VersionedQuery datasetVersionedQuery = new VersionedQuery(this.version, "t");
        if (rowFilters.trim().length()>0)
            return "SELECT t.* FROM " + table + " t WHERE " + datasetVersionedQuery.query() + " AND " + rowFilters + (filterTable.length() > 0 ? " AND EXISTS (SELECT 1 FROM " + filterTable + " f WHERE " + filterDatasetVersionedQuery.query() + sqlFilterDatasetJoinCondition + ")" : "") + " " + orderByClause();
        return "SELECT * FROM " + table + " t WHERE " + datasetVersionedQuery.query() + (filterTable.length() > 0 ? " AND EXISTS (SELECT 1 FROM " + filterTable + " f WHERE " + filterDatasetVersionedQuery.query() + sqlFilterDatasetJoinCondition + ")" : "") + " " + orderByClause();
    }

    private String aliasExpression(Dataset dataset, String expression, Map<String,Column> baseColumns, String tableAlias) throws Exception {
        int matchedColumnsCount = 0;
        String aliasedExpression = expression;
        Set<String> columnsKeySet = baseColumns.keySet();
        Iterator<String> iterator = columnsKeySet.iterator();
        while (iterator.hasNext()) {
            String columnName = iterator.next();
            Pattern pattern = Pattern.compile("\\b(?i)" + columnName + "\\b");
            Matcher matcher = pattern.matcher(aliasedExpression);

            //find all column names in the expression and alias each one
            //check to see if anything is found...
            if (matcher.find()) {
                
                aliasedExpression = matcher.replaceAll(tableAlias + "." + columnName);
                ++matchedColumnsCount;
                
//                boolean go = true;
//                int startIndexOfColumn = aliasedExpression.toLowerCase().indexOf(columnName.toLowerCase());
//                int expressionLength = aliasedExpression.length();
//                int columnNameLength = columnName.length();
//                int endIndexOfColumn = startIndexOfColumn + columnNameLength;
//                aliasedExpression = expression;
//                while (go) {
//                    if ((startIndexOfColumn == 0 || !Character.isLetter(aliasedExpression.charAt(startIndexOfColumn - 1)))
//                            && (endIndexOfColumn + 1 == expressionLength || !Character.isLetter(aliasedExpression.charAt(endIndexOfColumn + 1)))
//                            ) {
//                        ++matchedColumnsCount;
////                        aliasedExpression = aliasedExpression.replaceAll("(?i)" + columnName, tableAlias + "." + columnName);
//                        aliasedExpression = (startIndexOfColumn == 0 ? "" : aliasedExpression.substring(0, startIndexOfColumn - 1))
//                            + tableAlias + "." + columnName 
//                            + (endIndexOfColumn + 1 == expressionLength ? "" : aliasedExpression.substring(startIndexOfColumn + columnNameLength));
//                        expressionLength = aliasedExpression.length();
//                        endIndexOfColumn += 1 + tableAlias.length();
//                    }
//                    if (endIndexOfColumn + 1 == expressionLength) {
//                        go = false;    
//                    } else if (aliasedExpression.toLowerCase().substring(endIndexOfColumn + 1).contains(columnName.toLowerCase())) {
//                        go = true;
//                        startIndexOfColumn = endIndexOfColumn + 1;
//                        endIndexOfColumn = startIndexOfColumn + columnNameLength;
//                    } else {
//                        go = false;    
//                    }
//                }
            }
            }
        if (matchedColumnsCount == 0)
            throw new Exception("Invalid join expression, " + expression + ", for dataset, " + dataset.getName() + ".");
        return aliasedExpression;
    }
    
    private Map<String,Column> getDatasetColumnMap(Datasource datasource, String table) throws SQLException {
        return new TableMetaData(datasource).getColumnMap(table);
    }

//    public class ColumnMatchingMap {
//        private String dataset1Expression;
//        private String dataset2Expression;
//        
//        public ColumnMatchingMap(String dataset1Expression, String dataset2Expression) {
//            this.setDataset1Expression(dataset1Expression);
//            this.setDataset2Expression(dataset2Expression);
//        }
//
//        public void setDataset1Expression(String dataset1Expression) {
//            this.dataset1Expression = dataset1Expression;
//        }
//
//        public String getDataset1Expression() {
//            return dataset1Expression;
//        }
//
//        public void setDataset2Expression(String dataset2Expression) {
//            this.dataset2Expression = dataset2Expression;
//        }
//
//        public String getDataset2Expression() {
//            return dataset2Expression;
//        }
//        
//    }

    public static void main(String args[]) {
//        Pattern pattern = Pattern.compile("\\b(?i)plant\\b");
//        String aliasedExpression = "PLanT||plantid||plant||scc";
//        Matcher matcher = pattern.matcher(aliasedExpression);
////        aliasedExpression.replaceAll("[^A-Za-z0-9]plant[^A-Za-z0-9]", "a" + "." + "plant");
//        System.out.println(matcher.find());
//        System.out.println(matcher.replaceAll("a" + "." + "plant"));
//        aliasedExpression.replaceAll("\\b(?i)plant\\b", "a" + "." + "plant");
        
    }
    
}
