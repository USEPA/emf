package gov.epa.emissions.framework.services.qa.compareDatasetFields;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.TableMetaData;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.VersionedQuery;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DatasetVersion;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;
import gov.epa.emissions.framework.services.qa.SQLQAProgramQuery;
import gov.epa.emissions.framework.services.qa.SQLQueryParser;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SQLCompareDatasetFieldsProgramQuery extends SQLQAProgramQuery{
    
    public static final String BASE_TAG = "-base"; 
    //Sample:
/*
dataset name1|1
dataset name2|5
dataset name3|3
*/
    
    public static final String COMPARE_TAG = "-compare";
    //Sample:
/*
dataset name1|1
dataset name2|5
dataset name3|3
*/
    
    public static final String JOIN_EXPRESSIONS_TAG = "-join_expressions";
    //Sample:
/*
scc
fips
plantid
pointid
stackid
segment
poll
*/
    
    public static final String COMPARISON_EXPRESSIONS_TAG = "-comparison_expressions";
    //Sample:
/*
ann_emis
avd_emis
*/
    
    public static final String MATCHING_EXPRESSIONS_TAG = "-matching";
   
/*
scc|scc
fips|fips
plantid|plantid
pointid|pointid
stackid|stackid
segment|segment
poll|poll
*/
    public static final String JOIN_TYPE_TAG = "-join";

//    private static final String FULL_JOIN_EXPRESSIONS_TAG = "-fulljoin";

    public static final String WHERE_FILTER_TAG = "-where";
    public static final String BASE_SUFFIX_TAG = "-base_field_suffix";
    public static final String COMPARE_SUFFIX_TAG = "-compare_field_suffix";
    
    ArrayList<String> baseDatasetNames = new ArrayList<String>();
    
    ArrayList<String> compareDatasetNames = new ArrayList<String>();
        
    private Datasource datasource;
    
    public SQLCompareDatasetFieldsProgramQuery(HibernateSessionFactory sessionFactory, Datasource datasource, String emissioDatasourceName, String tableName, QAStep qaStep) {
        super(sessionFactory,emissioDatasourceName,tableName,qaStep);
        this.datasource = datasource;    
    }

    public class ColumnMatchingMap {
        private String baseExpression;
        private String compareExpression;
        
        public ColumnMatchingMap(String baseExpression, String compareExpression) {
            this.setBaseExpression(baseExpression);
            this.setCompareExpression(compareExpression);
        }

        public void setBaseExpression(String baseExpression) {
            this.baseExpression = baseExpression;
        }

        public String getBaseExpression() {
            return baseExpression;
        }

        public void setCompareExpression(String compareExpression) {
            this.compareExpression = compareExpression;
        }

        public String getCompareExpression() {
            return compareExpression;
        }
        
    }

    public class Expression {
        
        private String expression;
        private String alias;

        public Expression(String expression, String alias) {
            this.setExpression(expression);
            this.setAlias(alias);
        }

        public void setExpression(String expression) {
            this.expression = expression;
        }

        public String getExpression() {
            return expression;
        }

        public void setAlias(String alias) {
            this.alias = alias;
        }

        public String getAlias() {
            return alias;
        }

    }

    public String createProgramQuery() throws EmfException, SQLException {
        
        String programArguments = qaStep.getProgramArguments();
        
        //get applicable tables from the program arguments
        String[] baseTokens = new String[] {};
        String[] compareTokens = new String[] {};
        String[] joinExpressions = new String[] {};
        String[] comparisonExpressions = new String[] {};
        String[] matchingExpressionTokens = new String[] {};
        String whereFilter = new String();
        String baseWhereFilter = new String();
        String compareWhereFilter = new String();
        String baseSuffix = new String();
        String compareSuffix = new String();
        
//        String[] fullJoinExpressionTokens = new String[] {};
        Map<String, Column> baseColumns;
        Map<String, Column> compareColumns;
        
        List<DatasetVersion> baseDatasetList = new ArrayList<DatasetVersion>();
        List<DatasetVersion> compareDatasetList = new ArrayList<DatasetVersion>();
        Map<String, ColumnMatchingMap> matchingExpressionMap = new HashMap<String, ColumnMatchingMap>();
        Map<String, String> joinExpressionsAliasMap = new TreeMap<String, String>(); //use treemap so the map stays sorted in the order the columns names are specified
        
        String[] arguments;
        
        
        
        //Load up arguments into local variables...
        
        
        int indexBase = programArguments.indexOf(BASE_TAG +"\n");
        int indexCompare = programArguments.indexOf(COMPARE_TAG +"\n");
        int indexJoinExpressions = programArguments.indexOf(JOIN_EXPRESSIONS_TAG +"\n");
        int indexComparisonExpressions = programArguments.indexOf(COMPARISON_EXPRESSIONS_TAG +"\n");
        int indexMatching = programArguments.indexOf(MATCHING_EXPRESSIONS_TAG +"\n");
        int indexJoinType = programArguments.indexOf(JOIN_TYPE_TAG +"\n");
        int indexWhereFilter = programArguments.indexOf(WHERE_FILTER_TAG +"\n");
        int indexBSuffix = programArguments.indexOf(BASE_SUFFIX_TAG);
        int indexCSuffix = programArguments.indexOf(COMPARE_SUFFIX_TAG);
        
        //parse base and compare dataset name and version
        if (indexBase != -1) {
            arguments = parseSwitchArguments(programArguments, indexBase, programArguments.indexOf("\n-", indexBase) != -1 ? programArguments.indexOf("\n-", indexBase) : programArguments.length());
            if (arguments != null && arguments.length > 0) baseTokens = arguments;
            for (String datasetVersion : baseTokens) {
                String[] datasetVersionToken = new String[] {};
                if (!datasetVersion.equals("$DATASET")) { 
                    datasetVersionToken = datasetVersion.split("\\|");
                } else {
                    EmfDataset qaStepDataset = getDataset(qaStep.getDatasetId());
                    datasetVersionToken = new String[] { qaStepDataset.getName(), qaStepDataset.getDefaultVersion() + "" };
                }
                datasetNames.add(datasetVersionToken[0]);
                baseDatasetList.add(new DatasetVersion(datasetVersionToken[0], Integer.parseInt(datasetVersionToken[1])));
            }
        }
        if (indexCompare != -1) {
            arguments = parseSwitchArguments(programArguments, indexCompare, programArguments.indexOf("\n-", indexCompare) != -1 ? programArguments.indexOf("\n-", indexCompare) : programArguments.length());
            if (arguments != null && arguments.length > 0) compareTokens = arguments;
            for (String datasetVersion : compareTokens) {
                String[] datasetVersionToken = new String[] {};
                if (!datasetVersion.equals("$DATASET")) { 
                    datasetVersionToken = datasetVersion.split("\\|");
                } else {
                    EmfDataset qaStepDataset = getDataset(qaStep.getDatasetId());
                    datasetVersionToken = new String[] { qaStepDataset.getName(), qaStepDataset.getDefaultVersion() + "" };
                }
                datasetNames.add(datasetVersionToken[0]);
                compareDatasetList.add(new DatasetVersion(datasetVersionToken[0], Integer.parseInt(datasetVersionToken[1])));
            }
        }
        
//        checkDataset();
        
        //parse SQL join type
        String joinSQL="";       
        if (indexJoinType != -1
                && (indexJoinType + JOIN_TYPE_TAG.length() + 1) < (programArguments.indexOf("\n-", indexJoinType) != -1 ? programArguments.indexOf("\n-", indexJoinType) : programArguments.length())) 
            joinSQL = programArguments.substring(indexJoinType + JOIN_TYPE_TAG.length() + 1, programArguments.indexOf("\n-", indexJoinType) != -1 ? programArguments.indexOf("\n-", indexJoinType) : programArguments.length());          
        if (joinSQL.trim().isEmpty())
            joinSQL = " full outer join ";
        if (indexJoinExpressions != -1) {
            arguments = parseSwitchArguments(programArguments, indexJoinExpressions, programArguments.indexOf("\n-", indexJoinExpressions) != -1 ? programArguments.indexOf("\n-", indexJoinExpressions) : programArguments.length());
            if (arguments != null && arguments.length > 0) joinExpressions = arguments;
        }

        //parse Comparison Expressions
        if (indexComparisonExpressions != -1) {
            arguments = parseSwitchArguments(programArguments, indexComparisonExpressions, programArguments.indexOf("\n-", indexComparisonExpressions) != -1 ? programArguments.indexOf("\n-", indexComparisonExpressions) : programArguments.length());
            if (arguments != null && arguments.length > 0) comparisonExpressions = arguments;
        }

        //parse Matching Expressions
        if (indexMatching != -1) {
            arguments = parseSwitchArguments(programArguments, indexMatching, programArguments.indexOf("\n-", indexMatching) != -1 ? programArguments.indexOf("\n-", indexMatching) : programArguments.length());
            if (arguments != null && arguments.length > 0) matchingExpressionTokens = arguments;
            for (String matchingExpressionToken : matchingExpressionTokens) {
                String[] matchingExpression = matchingExpressionToken.split("\\=");
                String dataset1Expression = matchingExpression[0].toLowerCase();
                String dataset2Expression = matchingExpression[1].toLowerCase();
                matchingExpressionMap.put(dataset1Expression, new ColumnMatchingMap(dataset1Expression, dataset2Expression));
                matchingExpressionMap.put(dataset2Expression, new ColumnMatchingMap(dataset1Expression, dataset2Expression));
            }
        }

        //parse Where Filter Expressions
        if (indexWhereFilter != -1 
                && (indexWhereFilter + WHERE_FILTER_TAG.length() + 1) < (programArguments.indexOf("\n-", indexWhereFilter) != -1 ? programArguments.indexOf("\n-", indexWhereFilter) : programArguments.length())) 
            whereFilter = programArguments.substring(indexWhereFilter + WHERE_FILTER_TAG.length() + 1, programArguments.indexOf("\n-", indexWhereFilter) != -1 ? programArguments.indexOf("\n-", indexWhereFilter) : programArguments.length());
        //strip off any unnecessary characters
        whereFilter = whereFilter.replaceAll("\n","").trim();
        //Validate program arguments (i.e., does dataset and version exist, does mapping make sense, etc...)
       
        if (indexBSuffix != -1 
                && (indexBSuffix + BASE_SUFFIX_TAG.length() + 1) < (programArguments.indexOf("\n-", indexBSuffix) != -1 ? programArguments.indexOf("\n-", indexBSuffix) : programArguments.length())) 
            baseSuffix = programArguments.substring(indexBSuffix + BASE_SUFFIX_TAG.length() + 1, programArguments.indexOf("\n-", indexBSuffix) != -1 ? programArguments.indexOf("\n-", indexBSuffix) : programArguments.length());
        baseSuffix = baseSuffix.replaceAll("\n","").trim();
        if (baseSuffix.isEmpty())
            baseSuffix = "b";
        //strip off any unnecessary characters
        baseSuffix = "_" + baseSuffix;
       
        //Get the compare dataset suffix
        if (indexCSuffix != -1 
                && (indexCSuffix + COMPARE_SUFFIX_TAG.length() + 1) < (programArguments.indexOf("\n-", indexCSuffix) != -1 ? programArguments.indexOf("\n-", indexCSuffix) : programArguments.length())) 
            compareSuffix = programArguments.substring(indexCSuffix + COMPARE_SUFFIX_TAG.length() + 1, programArguments.indexOf("\n-", indexCSuffix) != -1 ? programArguments.indexOf("\n-", indexCSuffix) : programArguments.length());
        compareSuffix = compareSuffix.replaceAll("\n","").trim();
        if (compareSuffix.isEmpty())
            compareSuffix = "c";
        //strip off any unnecessary characters
        compareSuffix = "_" + compareSuffix;
        
        //see if there is issues with the base datasets 
        if (baseDatasetList.size() > 0 ) {
            for (DatasetVersion datasetVersion : baseDatasetList) {
                EmfDataset dataset = getDataset(datasetVersion.getDatasetName());
                //make sure dataset exists
                if (dataset == null)
                    throw new EmfException("Dataset, " + datasetVersion.getDatasetName() + ", doesn't exist.");
                datasetVersion.setDataset(dataset);
                Version version = version(dataset.getId(), datasetVersion.getDatasetVersion());
                //make sure version exists
                if (version == null)
                    throw new EmfException("Version, " + datasetVersion.getDatasetName() + " - " + datasetVersion.getDatasetVersion() + ", doesn't exists.");
                datasetVersion.setVersion(version);
            }
            //do one last pass now that the dataset and version objects have been populated and 
            //make sure all of these datasets are of the same dataset type!
            DatasetType prevDatasetType = null;
            for (DatasetVersion datasetVersion : baseDatasetList) {
                EmfDataset dataset = datasetVersion.getDataset();
                if (prevDatasetType != null && !prevDatasetType.equals(dataset.getDatasetType()))
                    throw new EmfException("The base datasets must be of the same dataset type.");
                prevDatasetType = dataset.getDatasetType();
            }
            
        } else {
            throw new EmfException("There are no base datasets specified.");
        }
        
        //see if there are issues with the compare datasets 
        if (compareDatasetList.size() > 0 ) {
            for (DatasetVersion datasetVersion : compareDatasetList) {
                EmfDataset dataset = getDataset(datasetVersion.getDatasetName());
                //make sure dataset exists
                if (dataset == null)
                    throw new EmfException("Dataset, " + datasetVersion.getDatasetName() + ", doesn't exist.");
                datasetVersion.setDataset(dataset);
                Version version = version(dataset.getId(), datasetVersion.getDatasetVersion());
                //make sure version exists
                if (version == null)
                    throw new EmfException("Version, " + datasetVersion.getDatasetName() + " - " + datasetVersion.getDatasetVersion() + ", doesn't exists.");
                datasetVersion.setVersion(version);
            }
            //do one last pass now that the dataset and version objects have been populated and 
            //make sure all of these datasets are of the same dataset type!
            DatasetType prevDatasetType = null;
            for (DatasetVersion datasetVersion : compareDatasetList) {
                EmfDataset dataset = datasetVersion.getDataset();
                if (prevDatasetType != null && !prevDatasetType.equals(dataset.getDatasetType()))
                    throw new EmfException("The compare datasets must be of the same dataset type.");
                prevDatasetType = dataset.getDatasetType();
            }
            
        } else {
            throw new EmfException("There are no compare datasets specified.");
        }

        //get columns that represent both the compare and base datasets
        baseColumns = getDatasetColumnMap((baseDatasetList.get(0)).getDataset());
        compareColumns = getDatasetColumnMap((compareDatasetList.get(0)).getDataset());

        //see if there are issues with the matching expressions
        if (matchingExpressionMap.size() > 0 ) {
            //make sure these expressions exists
            for (String matchingExpression : matchingExpressionTokens) {
                String[] matchingExpressionToken = matchingExpression.split("\\=");
                //make sure expression exists
                if (!expressionExists(matchingExpressionToken[0], baseColumns))
                    throw new EmfException("The base matching expression, " + matchingExpressionToken[0] + ", doesn't exist as a column in the dataset.");
                //make sure expression exists
                if (!expressionExists(matchingExpressionToken[1], compareColumns))
                    throw new EmfException("The compare matching expression, " + matchingExpressionToken[1] + ", doesn't exist as a column in the dataset.");

            }
        } 
//there might not be any matching criteria specified, could be comparing like datasets        
//        else {
//            throw new EmfException("There are no matching expressions specified.");
//        }

        //see if there are issues with the join expressions
        if (joinExpressions.length > 0 ) {
            //make sure these expressions exists
            for (String joinExpression : joinExpressions) {

                //parse group by token and put in a map for later use...
                String[] joinExpressionParts = joinExpression.split(" (?i)as ");
//                StringTokenizer tokenizer = new StringTokenizer(groupByExpression.toLowerCase(), "\\ as ");
                int count = joinExpressionParts.length;
                String expression = "";
                String alias = "";
                //has no alias
                if (count == 1) {
                    expression = joinExpressionParts[0];//tokenizer.nextToken();
                    alias = expression;
                //has alias
                } else if (count == 2) {
                    expression = joinExpressionParts[0];//tokenizer.nextToken();
                    alias = joinExpressionParts[1];//tokenizer.nextToken();
                //unkown number of tokens, throw an error
                } else if (count > 2) {
                    throw new EmfException("Invalid formatted Join Expression, " + joinExpression + ". Should be formatted as: expression AS alias (i.e., subtring(fips,1,2) as fipsst).");
                }
                if (joinExpressionsAliasMap.containsKey(alias))
                    throw new EmfException("Join expression, " + joinExpression + ", has already been specified.  Only specify the expression once.");
                //add to map, will be used to help build sql statement
                joinExpressionsAliasMap.put(alias, expression);
            
            
                //ignoring mappings for now, just see if expression is appropriate for dataset(s)
                boolean baseExpressionExists = expressionExists(expression, baseColumns);
                boolean compareExpressionExists = expressionExists(expression, compareColumns);

                //make sure group by expression exists
                if (!baseExpressionExists && !compareExpressionExists)
                    throw new EmfException("Join expression, " + expression + ", doesn't exist as a column in either the base or compare datasets.");

                //if either one of the dataset types doesn't contain the column, then make sure we have a mapping for it...
                baseExpressionExists = expressionExists(expression, baseColumns, matchingExpressionMap);
                compareExpressionExists = expressionExists(expression, compareColumns, matchingExpressionMap);
                if (!baseExpressionExists || !compareExpressionExists) {
                    if (matchingExpressionMap.get(expression.toLowerCase()) == null)
                        throw new EmfException("Join expression, " + expression + ", needs a mapping entry specified, the column doesn't exist in either the base or compare datasets.");
                }
            }
        } else {
            throw new EmfException("There are no Join expressions specified.");
        }

        //see if there are issues with the Comparison expressions
        if (comparisonExpressions.length > 0 ) {
            //make sure these expressions returns a number
            for (String comparisonExpression : comparisonExpressions) {
                boolean baseColumnExists = expressionExists(comparisonExpression, baseColumns, matchingExpressionMap);
//                Column baseColumn = baseColumns.get(aggregateExpression.toLowerCase());
                boolean compareColumnExists = expressionExists(comparisonExpression, compareColumns, matchingExpressionMap);
//                Column compareColumn = compareColumns.get(aggregateExpression.toLowerCase());
                //make sure aggregate expression exists
                if (!baseColumnExists && !compareColumnExists)
                    throw new EmfException("Comparison expression, " + comparisonExpression + ", doesn't exist as a column in either the base or compare datasets.");
                //if either one of the dataset types doesn't contain the column, then make sure we have a mapping for it...
                if (!baseColumnExists || !compareColumnExists) {
                    if (matchingExpressionMap.get(comparisonExpression.toLowerCase()) == null)
                        throw new EmfException("Comparison expression, " + comparisonExpression + ", needs a mapping entry specified, the column doesn't exist in either the base or compare datasets.");
                }
//                
//                Column column = (baseColumn != null ? baseColumn : (compareColumn != null ? compareColumn : null));
//                //make sure aggregate expression represents a number data type
//                if (!(column.getSqlType() == "INTEGER"
//                    || column.getSqlType() == "float(15)"
//                    || column.getSqlType() == "INT"
//                    || column.getSqlType() == "BIGINT"
//                    || column.getSqlType() == "double precision"
//                    || column.getSqlType() == "INT2"
//                ))
//                    throw new EmfException("Aggregate expression, " + aggregateExpression + ", must be a numeric data type (i.e., tinyint, smallint, int, bigint, double precision, float, numeric, decimal).");
                
            }
        } else {
            throw new EmfException("There are no Comparison expressions specified.");
        }

        //see if there are issues with the where filter expression
        if (whereFilter.length() > 0 ) {
            boolean baseColumnExists = expressionExists(whereFilter, baseColumns, matchingExpressionMap);
//          Column baseColumn = baseColumns.get(aggregateExpression.toLowerCase());
            boolean compareColumnExists = expressionExists(whereFilter, compareColumns, matchingExpressionMap);
//          Column compareColumn = compareColumns.get(aggregateExpression.toLowerCase());
            //make sure aggregate expression exists
            if (!baseColumnExists && !compareColumnExists)
                throw new EmfException("Where Filter contains expressions that don't exist as a column in either the base or compare datasets.");
            
            //evaluate each part of where clause to make sure there is mapping, if needed...
//            try {
//                getBaseExpression(whereFilter, matchingExpressionMap, baseColumns, "b");
//                getCompareExpression(whereFilter, matchingExpressionMap, compareColumns, "c");
//            } catch (EmfException ex) {
//                if (ex.getMessage().equals("Unknown compare dataset expression") || ex.getMessage().equals("Unknown base dataset expression"))
//                throw new EmfException("Where Filter contains expression that needs a mapping entry specified, the column doesn't exist in either the base or compare datasets.");
//            }
            
            //if either one of the dataset types doesn't contain the column, then make sure we have a mapping for it...
            if (!baseColumnExists || !compareColumnExists) {
                if (!expressionExists(whereFilter, baseColumns, matchingExpressionMap) || !expressionExists(whereFilter, compareColumns, matchingExpressionMap))
                    throw new EmfException("Where Filter contains expression that needs a mapping entry specified, the column doesn't exist in either the base or compare datasets.");
          }
        } 
        
        //Build SQL statement
        
        String selectSQL = "select ";
        String baseSelectSQL = "select ";
        String compareSelectSQL = "select ";
        String baseUnionSelectSQL = "select ";
        String compareUnionSelectSQL = "select ";
//        String groupBySQL = "group by ";
//        String baseGroupBySQL = "group by ";
//        String compareGroupBySQL = "group by ";
//        String baseUnionGroupBySQL = "group by ";
//        String compareUnionGroupBySQL = "group by ";
        String fullJoinClauseSQL = "on ";

        //build core group by expressions into SELECT statement 
        int counter = 0;
        
        Set<String> expressionAliasKeySet = joinExpressionsAliasMap.keySet();
        Iterator<String> expressionAliasKeySetIterator = expressionAliasKeySet.iterator();
        Iterator<String> expressionAliasValuesIterator = joinExpressionsAliasMap.values().iterator();
        while (expressionAliasKeySetIterator.hasNext()) {
            String joinExpression = expressionAliasValuesIterator.next();
            String joinExpressionAlias = expressionAliasKeySetIterator.next();
            
            
            
//        for (int counter = 0; counter < expressionAliasMap.size(); ++counter) {
        //String groupByExpression : groupByExpressions) {
            String baseExpression = getBaseExpression(joinExpression, matchingExpressionMap, baseColumns, "b");
//            Column baseColumn = getBaseColumn(groupByExpression, matchingExpressionMap, baseColumns);
            String compareExpression = getCompareExpression(joinExpression, matchingExpressionMap, compareColumns, "c");
//            Column compareColumn = getCompareColumn(groupByExpression, matchingExpressionMap, compareColumns);
            
//            selectSQL += (!selectSQL.equals("select ") ? ", " : "") + "coalesce(b." + baseColumn.getName() + ",c." + compareColumn.getName() + ") as " + groupByExpression + "";
//            baseSelectSQL += (!baseSelectSQL.equals("select ") ? ", " : "") + "b." + baseColumn.getName();
//            compareSelectSQL += (!compareSelectSQL.equals("select ") ? ", " : "") + "c." + compareColumn.getName();

            selectSQL += (!selectSQL.equals("select ") ? ", " : "") + "coalesce(b.expr" + counter + ",c.expr" + counter + ") as \"" + joinExpressionAlias + "\"";
            baseSelectSQL += (!baseSelectSQL.equals("select ") ? ", " : "") + baseExpression + " as expr" + counter;
            compareSelectSQL += (!compareSelectSQL.equals("select ") ? ", " : "") + compareExpression + " as expr" + counter;
            baseUnionSelectSQL += (!baseUnionSelectSQL.equals("select ") ? ", " : "") + " expr" + counter;
            compareUnionSelectSQL += (!compareUnionSelectSQL.equals("select ") ? ", " : "") + " expr" + counter;
            
//            groupBySQL += (!groupBySQL.equals("group by ") ? ", " : "") + "coalesce(b.expr" + counter + ",c.expr" + counter + ")";
//            baseGroupBySQL += (!baseGroupBySQL.equals("group by ") ? ", " : "") + baseExpression;
//            compareGroupBySQL += (!compareGroupBySQL.equals("group by ") ? ", " : "") + compareExpression;
//            baseUnionGroupBySQL += (!baseUnionGroupBySQL.equals("group by ") ? ", " : "") + " expr" + counter;
//            compareUnionGroupBySQL += (!compareUnionGroupBySQL.equals("group by ") ? ", " : "") + " expr" + counter;

            fullJoinClauseSQL += (!fullJoinClauseSQL.equals("on ") ? " and " : "") + "c.expr" + counter + " = b.expr" + counter + " ";
            ++counter;
        }
        
        //build comparison expressions into SELECT statement 
        for (String comparisonExpression : comparisonExpressions) {
//            Column baseColumn = getBaseColumn(aggregateExpression, matchingExpressionMap, baseColumns);
            String baseAggregateExpression = getBaseExpression(comparisonExpression, matchingExpressionMap, baseColumns, "b");
//            Column compareColumn = getCompareColumn(aggregateExpression, matchingExpressionMap, compareColumns);
            String compareAggregateExpression = getCompareExpression(comparisonExpression, matchingExpressionMap, compareColumns, "c");
            selectSQL += ",b.\"" + comparisonExpression + "\" as \"" + comparisonExpression + baseSuffix + "\", c.\"" + comparisonExpression + "\" as \"" + comparisonExpression + compareSuffix +"\"" + ",(b.\"" + comparisonExpression + "\" = " + "c.\"" + comparisonExpression + "\") as \"" + comparisonExpression + "_match\"";
            baseSelectSQL += "," + baseAggregateExpression + " as \"" + comparisonExpression + "\"";
            compareSelectSQL += "," + compareAggregateExpression + " as \"" + comparisonExpression + "\"";
            baseUnionSelectSQL += ",\"" + comparisonExpression + "\" as \"" + comparisonExpression + "\"";
            compareUnionSelectSQL += ",\"" + comparisonExpression + "\" as \"" + comparisonExpression + "\"";
        }

        //alias where filter for use in the base and compare datasets
        baseWhereFilter = aliasBaseExpression(whereFilter, matchingExpressionMap, baseColumns, "b");
//      Column compareColumn = getCompareColumn(aggregateExpression, matchingExpressionMap, compareColumns);
        compareWhereFilter = aliasCompareExpression(whereFilter, matchingExpressionMap, compareColumns, "c");

      //build inner sql statement with the datasets specified, make sure and unionize (append) the tables together
         String innerSQLBase = "";
         if (baseDatasetList.size() > 1) 
             innerSQLBase = baseUnionSelectSQL + " from (";
         for (int j = 0; j < baseDatasetList.size(); j++) {
             DatasetVersion datasetVersion = baseDatasetList.get(j);
             EmfDataset dataset = datasetVersion.getDataset();
             VersionedQuery datasetVersionedQuery = new VersionedQuery(datasetVersion.getVersion(), "b");

             innerSQLBase += (j > 0 ? " \nunion all " : "") + baseSelectSQL + " from emissions." + dataset.getInternalSources()[0].getTable() + " as b where " + datasetVersionedQuery.query() + (baseWhereFilter.length() > 0 ? " and (" + baseWhereFilter + ")" : "");
         }
         if (baseDatasetList.size() > 1) 
             innerSQLBase += ") b ";

         String innerSQLCompare = "";

         if (compareDatasetList.size() > 1) 
             innerSQLCompare = compareUnionSelectSQL + " from (";
         for (int j = 0; j < compareDatasetList.size(); j++) {
             DatasetVersion datasetVersion = compareDatasetList.get(j);
             EmfDataset dataset = datasetVersion.getDataset();
             VersionedQuery datasetVersionedQuery = new VersionedQuery(datasetVersion.getVersion(), "c");
             innerSQLCompare += (j > 0 ? " \nunion all " : "") + compareSelectSQL + " from emissions." + dataset.getInternalSources()[0].getTable() + " as c where " + datasetVersionedQuery.query() + (compareWhereFilter.length() > 0 ? " and (" + compareWhereFilter + ")": "");
         }
         if (compareDatasetList.size() > 1) 
             innerSQLCompare += ") c ";
         
         String sql = selectSQL + " from (" + innerSQLBase + ") as b " + joinSQL + " (" + innerSQLCompare + ") as c ";
         
         sql += fullJoinClauseSQL;// + " " + groupBySQL.replace("group by", "order by");

//        System.out.println(sql);

        
        SQLQueryParser parser = new SQLQueryParser(sessionFactory, emissionDatasourceName, tableName );
//return the built query
        return parser.createTableQuery() + " " + sql;
    }
    
    private boolean expressionExists(String expression, Map<String,Column> columns) {
        Set<String> columnsKeySet = columns.keySet();
        Iterator<String> iterator = columnsKeySet.iterator();
        while (iterator.hasNext()) {
            String columnName = iterator.next();
            Pattern pattern = getPattern(columnName);
            Matcher matcher = pattern.matcher(expression);

            //check to see if anything is found...
            if (matcher.find()) {
                return true;
            }
        }
        return false;
    }
    
    private boolean expressionExists(String expression, Map<String,Column> columns, Map<String,ColumnMatchingMap> columnMatchingMap) {
        if (expressionExists(expression, columns)) 
            return true;

        //didn't find the column in the known list of columns, lets see if there is a mapping for this.
        Set<String> columnMatchingMapKeySet = columnMatchingMap.keySet();
        Iterator<String> iterator = columnMatchingMapKeySet.iterator();
        while (iterator.hasNext()) {
            String mappingExpression = iterator.next();
            Pattern pattern = getPattern(mappingExpression);
            Matcher matcher = pattern.matcher(expression);

            if (matcher.find()) {
                return true;
            }
        }
        
        return false;
    }
    
    private String getBaseExpression(String expression, Map<String,ColumnMatchingMap> columnMatchingMap, Map<String,Column> baseColumns, String tableAlias) throws EmfException {
        Set<String> columnsKeySet = baseColumns.keySet();
        Iterator<String> iterator = columnsKeySet.iterator();
        String aliasedExpression = expression;
        
        //see if there is a mapping if so override the expression
        if (columnMatchingMap.get(expression.toLowerCase()) != null) {
            aliasedExpression = columnMatchingMap.get(expression.toLowerCase()).getBaseExpression();
        }
        
        //find all column names in the expression and alias each one
        
        //first check the expressions contains columns from the base dataset
        while (iterator.hasNext()) {
            String columnName = iterator.next();

            Pattern pattern = getPattern(columnName);
            Matcher matcher = pattern.matcher(aliasedExpression);

            //check to see if anything is found...
            if (matcher.find()) {
                aliasedExpression = matcher.replaceAll(tableAlias + ".\"" + baseColumns.get(columnName).getName() + "\"");
            }
        }
        //something has changed, go ahead and return now
        if (!aliasedExpression.equals(expression)) 
            return aliasedExpression;
        
        throw new EmfException("Unknown base dataset expression.");
    }
    
    private String aliasBaseExpression(String expression, Map<String,ColumnMatchingMap> columnMatchingMap, Map<String,Column> baseColumns, String tableAlias) {
        Set<String> columnsKeySet = baseColumns.keySet();
        Iterator<String> iterator = columnsKeySet.iterator();
        Set<String> columnMatchingMapKeySet = columnMatchingMap.keySet();
        Iterator<String> iterator2 = columnMatchingMapKeySet.iterator();
        String aliasedExpression = expression;
        
        //see if there is a mapping if so override the expression
//        if (columnMatchingMap.get(expression.toLowerCase()) != null) {
//            aliasedExpression = columnMatchingMap.get(expression.toLowerCase()).getBaseExpression();
//        }
        while (iterator2.hasNext()) {
            String mappedExpression = iterator2.next();
            Pattern pattern = getPattern(mappedExpression);
            Matcher matcher = pattern.matcher(aliasedExpression);

            //check to see if anything is found...
            if (matcher.find()) {
                aliasedExpression = matcher.replaceAll(columnMatchingMap.get(mappedExpression).getBaseExpression());
            }
        }
        
        //find all column names in the expression and alias each one
        
        //first check the expressions contains columns from the base dataset
        while (iterator.hasNext()) {
            String columnName = iterator.next();

            Pattern pattern = getPattern(columnName);
            Matcher matcher = pattern.matcher(aliasedExpression);

            //check to see if anything is found...
            if (matcher.find()) {
                aliasedExpression = matcher.replaceAll(tableAlias + ".\"" + baseColumns.get(columnName).getName() + "\"");
            }
        }
        return aliasedExpression;
    }
    
    
    //Patterns are expensive, so lets use a map to cache them...
    private Map<String,Pattern> patternMap = new HashMap<String,Pattern> ();
    
    private Pattern getPattern(String columnName) {
        if (!patternMap.containsValue(columnName)) {
            patternMap.put(columnName, Pattern.compile("\\b(?i)" + columnName.replaceAll("\\(", "\\\\(").replaceAll("\\)", "\\\\)") + "\\b"));
//            patternMap.put(columnName, Pattern.compile("\\s\\b(?i)" + columnName + "\\b\\s"));
        }
        return patternMap.get(columnName);
    }
    
    
//    Pattern pattern = Pattern.compile("\\b(?i)" + columnName + "\\b");
//    Matcher matcher = pattern.matcher(aliasedExpression);
//
//    //find all column names in the expression and alias each one
//    //check to see if anything is found...
//    if (matcher.find()) {
//        
//        aliasedExpression = matcher.replaceAll(tableAlias + "." + columnName);
//        ++matchedColumnsCount;
    private String getCompareExpression(String expression, Map<String,ColumnMatchingMap> columnMatchingMap, Map<String,Column> compareColumns, String tableAlias) throws EmfException {
        Set<String> columnsKeySet = compareColumns.keySet();
        Iterator<String> iterator = columnsKeySet.iterator();
        String aliasedExpression = expression;

        //see if there is a mapping if so override the expression
        if (columnMatchingMap.get(expression.toLowerCase()) != null) {
            aliasedExpression = columnMatchingMap.get(expression.toLowerCase()).getCompareExpression();
        }
        
        while (iterator.hasNext()) {
            String columnName = iterator.next();
            Pattern pattern = getPattern(columnName);
            Matcher matcher = pattern.matcher(aliasedExpression);

            //check to see if anything is found...
            if (matcher.find()) {
                aliasedExpression = matcher.replaceAll(tableAlias + ".\"" + compareColumns.get(columnName).getName() + "\"");
            }
        }
        //something has changed, go ahead and return now
        if (!aliasedExpression.equals(expression)) 
            return aliasedExpression;

        throw new EmfException("Unknown compare dataset expression.");
    }
    
    private String aliasCompareExpression(String expression, Map<String,ColumnMatchingMap> columnMatchingMap, Map<String,Column> compareColumns, String tableAlias) {
        Set<String> columnsKeySet = compareColumns.keySet();
        Iterator<String> iterator = columnsKeySet.iterator();
        Set<String> columnMatchingMapKeySet = columnMatchingMap.keySet();
        Iterator<String> iterator2 = columnMatchingMapKeySet.iterator();
        String aliasedExpression = expression;

        //see if there is a mapping if so override the expression
//        if (columnMatchingMap.get(expression.toLowerCase()) != null) {
//            aliasedExpression = columnMatchingMap.get(expression.toLowerCase()).getCompareExpression();
//        }
        while (iterator2.hasNext()) {
            String mappedExpression = iterator2.next();
            Pattern pattern = getPattern(mappedExpression);
            Matcher matcher = pattern.matcher(aliasedExpression);

            //check to see if anything is found...
            if (matcher.find()) {
                aliasedExpression = matcher.replaceAll(columnMatchingMap.get(mappedExpression).getCompareExpression());
            }
        }
        
        while (iterator.hasNext()) {
            String columnName = iterator.next();
            Pattern pattern = getPattern(columnName);
            Matcher matcher = pattern.matcher(aliasedExpression);

            //check to see if anything is found...
            if (matcher.find()) {
                aliasedExpression = matcher.replaceAll(tableAlias + ".\"" + compareColumns.get(columnName).getName() + "\"");
            }
        }
        return aliasedExpression;
    }
    
    private Map<String,Column> getDatasetColumnMap(EmfDataset dataset) throws SQLException {
        if (dataset.getInternalSources() == null )
            throw new SQLException("Dataset, " + dataset.getName() + ", does't have internalsources. " );
        Map<String,Column> datasetColumnMap = new TableMetaData(datasource).getColumnMap(dataset.getInternalSources()[0].getTable());
        Map<String,Column> lowerCaseDatasetColumnMap = new HashMap<String,Column>();
        
        //let's make sure and add ONLY user lower case versions so there is a consistent map regardless of case...
        Set<String> columnsKeySet = datasetColumnMap.keySet();
        Iterator<String> iterator = columnsKeySet.iterator();
        while (iterator.hasNext()) {
            String columnName = iterator.next();
            lowerCaseDatasetColumnMap.put(columnName.toLowerCase(), datasetColumnMap.get(columnName));
        }
        
        return lowerCaseDatasetColumnMap;
    }

    private String[] parseSwitchArguments(String programSwitches, int beginIndex, int endIndex) {
        List<String> inventoryList = new ArrayList<String>();
        String value = "";
        String valuesString = "";
        
        valuesString = programSwitches.substring(beginIndex, endIndex);
        StringTokenizer tokenizer2 = new StringTokenizer(valuesString, "\n");
        tokenizer2.nextToken(); // skip the flag

        while (tokenizer2.hasMoreTokens()) {
            value = tokenizer2.nextToken().trim();
            if (!value.isEmpty())
                inventoryList.add(value);
        }
        return inventoryList.toArray(new String[0]);
    }
}
