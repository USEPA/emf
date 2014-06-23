package gov.epa.emissions.framework.services.qa;

import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.Versions;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DatasetDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.data.QAStepResult;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.util.Hashtable;

import org.hibernate.Session;

public class SQLQueryParser {
    // Made extensive changes to this class to handle multiple versioned datasets.

    private QAStep qaStep;

    private String tableName;

    private String emissionDatasourceName;

    private EmfDataset dataset;

    private Version version;

    private String aliasValue;

    private HibernateSessionFactory sessionFactory;

    private Hashtable<String, String[]> tableValuesAliasesVersions;

    private static final String startQueryTag = "$TABLE[";

    // Created two new tags to search:
    // 1. $DATASET_TABLE[ datasetname, tablenum ], uses default version for dataset.
    // 2. $DATASET_TABLE_VERSION[ datasetname, tablenum, versionnum ].

    private static final String startSecondQueryTag = "$DATASET_TABLE[";

    private static final String startSecondVersQueryTag = "$DATASET_TABLE_VERSION[";
    
    private static final String startQAstepQueryTag = "$DATASET_QASTEP[";
    
    private static final String startQAstepVersQueryTag = "$DATASET_QASTEP_VERSION[";

    private static final String endQueryTag = "]";

    private static final String isErrorQueryTag = "$";
    
    private QAServiceImpl qaServiceImpl;

    public SQLQueryParser(QAStep qaStep, String tableName, String emissionDatasourceName, EmfDataset dataset,
            Version version, HibernateSessionFactory sessionFactory) {
        this.qaStep = qaStep;
        this.tableName = tableName;
        this.emissionDatasourceName = emissionDatasourceName;
        this.dataset = dataset;
        this.version = version;
        this.sessionFactory = sessionFactory;
        tableValuesAliasesVersions = new Hashtable<String, String[]>();
        qaServiceImpl = new QAServiceImpl(sessionFactory);
    }

    // New constructor for far fewer arguments.
    public SQLQueryParser(HibernateSessionFactory sessionFactory, String emissionDatasourceName, String tableName) {
        this.sessionFactory = sessionFactory; 
        this.emissionDatasourceName = emissionDatasourceName;
        this.tableName = tableName;
        tableValuesAliasesVersions = new Hashtable<String, String[]>();
        qaServiceImpl = new QAServiceImpl(sessionFactory);
    }

    public String parse() throws EmfException { // BUG3621
        return createTableQuery() + userQuery(qaStep.getProgramArguments());
    }

    public String parse(String inputQuery, boolean createClause) throws EmfException {
        if (createClause == true) {
            return createTableQuery() + userQuery(inputQuery);
        }

        return userQuery(inputQuery);
    }

    private String userQuery(String query) throws EmfException {
        // This is an indication that the startQueryString tag was not found in the query.

        if (query.indexOf(startQueryTag) == -1 && query.indexOf(startSecondQueryTag) == -1
                && query.indexOf(startSecondVersQueryTag) == -1 && query.indexOf(startQAstepQueryTag) == -1
                && query.indexOf(startQAstepVersQueryTag) == -1)
            return query;

        return expandTag(query); // BUG3621 //BUG3618
    }

    // SELECT - REQUIRED to STARTS WITH
    // FROM - REQUIRED
    // WHERE - OPTIONAL
    // ASSUME table is emissions datasource
    // ASSUME table is versioned

    private String expandTag(String query) throws EmfException {

        //System.out.println("Query: " + query); 
        while ((query.indexOf(startQueryTag)) != -1) {
            query = expandOneTag(query);
        }
        // Added two new while clauses to handle the parsing of the two new tags
        // Added code to add a new tag
        while ((query.indexOf(startSecondQueryTag)) != -1) {
            query = expandTwoTag(query);
        }
        // Added code to add a new tag
        while ((query.indexOf(startSecondVersQueryTag)) != -1) {
            query = expandThreeTag(query);
        }
        
        while ((query.indexOf(startQAstepQueryTag)) != -1) {
            query = expandFourTag(query);
        }
        
        while ((query.indexOf(startQAstepVersQueryTag)) != -1) {
            query = expandFiveTag(query);
        }

        // Check to see if there are any "$'s" left
        if (query.indexOf(isErrorQueryTag) != -1) {
            throw new EmfException("There is still a  '" + isErrorQueryTag + "' in the program arguments");
        }
        
        //return versioned(query);
        return query;
    }

    private String expandOneTag(String query) throws EmfException {

        // Point to the start of the first tag, the first substring is from the beginning of the
        // query to that point. The suffix is from that point to the end of the tag.
        int index = query.indexOf(startQueryTag);
        //System.out.println("orig: " + query + " uper: " + query + " startQT:" + startQueryTag);
        String prefix = query.substring(0, index);
        String suffix = query.substring(index + startQueryTag.length());

        // Split the suffix into three tokens
        String[] suffixTokens = suffixSplit(suffix);

        // The dataset name is retreived from the attribute, as is the version for it.
        // The version has to be further subdivided to just get the number.

        String dataSetName = dataset.toString();
        String ds1version = version.toString();
        ds1version = ds1version.substring(0, 1);

        // The dataset id is retrieved from the dataset through its getId method.
        // It is then converted to a string to put in the hashtable.

        int dataSet_id = dataset.getId();
        String dsId = Integer.toString(dataSet_id);

        // The complete path of the version is retrieved from the version attribute.
        String versionCompletePath = version.createCompletePath();

        // The first string array value is calculated for the hashtable.
        // The appropriate key plus the string array (as a "value") is put into the hashtable.

        String[] ds1 = { dataSetName, suffixTokens[0], ds1version, dsId, versionCompletePath };
        tableValuesAliasesVersions.clear();
        tableValuesAliasesVersions.put("ds1", ds1);
        
        String versionClause = versionClause();
        String keywordReplaced = " (SELECT * from ";
        keywordReplaced += tableNameFromDataset(suffixTokens[1], dataset);
        keywordReplaced += " " + suffixTokens[0]; 
        keywordReplaced += " where " + versionClause;
        keywordReplaced += ") ";
        
        String rtnStr = prefix + keywordReplaced + suffixTokens[2];

        return rtnStr;
        // The table name from the dataset is derived from the method below off of the second token.
        // return prefix + versioned(tableNameFromDataset(suffixTokens[1], dataset) + suffixTokens[2]);
    }

    // Added this method to handle new tag $DATASET_TABLE[ datasetname, tablenum ]

    private String expandTwoTag(String query) throws EmfException {
        //System.out.println(query + "[]" + query);
        String prefix;
        // Split the suffix into three tokens
        String[] suffixTokens;
        //int index2 = suffixTokens[1].indexOf(",");
                //String dataSetName = suffixTokens[1].substring(0, index2) + " ";
        EmfDataset dataSet2;
        String tableNum;
        try {
            // Point to the start of the second tag, the first substring is from the beginning of the
            // query to that point. The suffix is from that point to the end of the tag.

            int index = query.indexOf(startSecondQueryTag);
            prefix = query.substring(0, index);
            String suffix = query.substring(index + startSecondQueryTag.length());

            suffixTokens = suffixSplit(suffix);

            // The dataset name is retreived as the first argument in the second token.
            // It is then trimmed and then converted into an EmfDataset type throught
            // the getDataset method created below

            // Look for first double quote (fdq).  Go from there to second double quote (sdq).
            // The new first token is from fdq to sdq.
            // The new second token is from sdq to end of string.
            // From this point parse on the next comma because the other value is a number
            
            int quoteIndex = suffixTokens[1].indexOf("\"");
            String firstQuoteRemoved = suffixTokens[1].substring(quoteIndex + 1);
//            System.out.println("firstQuoteRemoved: " + firstQuoteRemoved);
            int quoteIndex2 = firstQuoteRemoved.indexOf("\"");
            String dataSetName = firstQuoteRemoved.substring(0, quoteIndex2) + " ";
            //System.out.println("dataSetName: " + dataSetName);
            
            dataSet2 = getDataset(dataSetName.trim());
            if ( dataSet2 == null )
                throw new EmfException("The dataset name \"" + dataSetName + "\" is not valid");

            // The integer value of the default version is retrieved from the getDefaultVersion
            // method of the EmfDataset just created. It is converted to a String for the hashtable.

            int ds2IntVersion = dataSet2.getDefaultVersion();
            String ds2version = Integer.toString(ds2IntVersion);

//            System.out.println("Default Version as integer " + ds2IntVersion);

            // The dataset id is retrieved from the dataset through its getId method.
            // It is then converted to a string to put in the hashtable.
            // Next a version object is created from the dataset id and the version.

            int dataSet2_id = dataSet2.getId();
            String ds2Id = Integer.toString(dataSet2_id);
            Version version1 = version(dataSet2_id, ds2IntVersion);
//            System.out.println("version object" + version1);

            // The table number is retrieved from the second argument in the second token.
            String secondQuoteRemoved = firstQuoteRemoved.substring(quoteIndex2 + 1);
            /*int quoteIndex3 = secondQuoteRemoved.indexOf("\"");
            String thirdQuoteRemoved = secondQuoteRemoved.substring(quoteIndex3 + 1);
            int quoteIndex4 = thirdQuoteRemoved.indexOf("\"");
            String tableNum = thirdQuoteRemoved.substring(0, quoteIndex4);*/
            
            int commaIndex = secondQuoteRemoved.indexOf(",");
            tableNum = secondQuoteRemoved.substring(commaIndex + 1).trim();
            
//            System.out.println("tableNum: " + tableNum);
            //String tableNum = suffixTokens[1].substring(index2 + 1).trim();

            /*
             * System.out.println("Dataset information: " + dataSet2); System.out.println("Dataset id: " + dataSet2_id);
             * System.out.println("Dataset name: " + dataSetName); System.out.println("Table number: " + tableNum);
             */

            // The complete path of the version is retrieved from the version object just created.
            String versionCompletePath = version1.createCompletePath();

            // The second string array value is calculated for the hashtable.
            // The appropriate key plus the string array (as a "value") is put into the hashtable.
            String[] ds2 = { dataSetName, suffixTokens[0], ds2version, ds2Id, versionCompletePath };
            tableValuesAliasesVersions.clear();
            tableValuesAliasesVersions.put("ds2", ds2);

            String versionClause = versionClause();
            String keywordReplaced = " (SELECT * from ";
            keywordReplaced += tableNameFromDataset(tableNum, dataSet2);
            keywordReplaced += " " + suffixTokens[0]; 
            keywordReplaced += " where " + versionClause;
            keywordReplaced += ") ";
            
            String rtnStr = prefix + keywordReplaced + suffixTokens[2];

            return rtnStr;
            
            /*
             * System.out.println("prefix: " + prefix); System.out.println("Table number: " + tableNum);
             * System.out.println("dataset of 2: " + dataSetName); System.out.println("version of 2: " + ds2version);
             * System.out.println("alias of 2: " + suffixTokens[0]); System.out.println("Rest of query: " +
             * suffixTokens[2]);
             */

            // The table name from the dataset is derived from the method below off of the second token.
            // return prefix + versioned(tableNameFromDataset(tableNum, dataSet2) + suffixTokens[2]);
        } catch (RuntimeException e) {
            // NOTE Auto-generated catch block
            System.out.println("Could not parse query: "+query);
            e.printStackTrace();
            throw new EmfException(e.getMessage());
        }
    }

    // Added this method to handle new tag $DATASET_TABLE_VERSION[ datasetname, tablenum, versionnum ]

    private String expandThreeTag(String query) throws EmfException {

        String versionCompletePath;

        // Point to the start of the third tag, the first substring is from the beginning of the
        // query to that point. The suffix is from that point to the end of the tag.
        int index = query.indexOf(startSecondVersQueryTag);
        String prefix = query.substring(0, index);
        String suffix = query.substring(index + startSecondVersQueryTag.length());

        // Split the suffix into three tokens
        String[] suffixTokens = suffixSplit(suffix);

        // The dataset name is retreived as the first argument in the second token.
        // Look for first double quote (fdq).  Go from there to second double quote (sdq).
        // The new first token is from fdq to sdq.
        // The new second token is from sdq to end of string.
        // From this point parse on the next commas because the other values are numbers
        
        int quoteIndex = suffixTokens[1].indexOf("\"");
        String firstQuoteRemoved = suffixTokens[1].substring(quoteIndex + 1);
        //System.out.println("firstQuoteRemoved: " + firstQuoteRemoved);
        int quoteIndex2 = firstQuoteRemoved.indexOf("\"");
        String dataSetName = firstQuoteRemoved.substring(0, quoteIndex2) + " ";
        //System.out.println("dataSetName: " + dataSetName);
        
        String secondQuoteRemoved = firstQuoteRemoved.substring(quoteIndex2 + 1);
        //int quoteIndex3 = secondQuoteRemoved.indexOf("\"");
        
        int commaIndex = secondQuoteRemoved.indexOf(",");
        //System.out.println("biggersuffix: " + secondQuoteRemoved);
        
        String firstCommaRemoved = secondQuoteRemoved.substring(commaIndex + 1);
        //System.out.println("suffix: " + firstCommaRemoved);
        
        int commaIndex2 = firstCommaRemoved.indexOf(",");
        
        String tableNum = firstCommaRemoved.substring(0, commaIndex2).trim();
        
        //String tableNum = secondQuoteRemoved.substring(commaIndex + 1, commaIndex2).trim();
        
        //String thirdQuoteRemoved = secondQuoteRemoved.substring(quoteIndex3 + 1);
        //int quoteIndex4 = thirdQuoteRemoved.indexOf("\"");
        //String tableNum = thirdQuoteRemoved.substring(0, quoteIndex4);
        //System.out.println("tableNum: " + tableNum);
        
        //String fourthQuoteRemoved = thirdQuoteRemoved.substring(quoteIndex4 + 1);
        //int quoteIndex5 = fourthQuoteRemoved.indexOf("\"");
        //String fifthQuoteRemoved = fourthQuoteRemoved.substring(quoteIndex5 + 1);
        //int quoteIndex6 = fifthQuoteRemoved.indexOf("\"");
        
        String ds3version = firstCommaRemoved.substring(commaIndex2 +1).trim();
        
        //String ds3version = fifthQuoteRemoved.substring(0, quoteIndex6);
        //System.out.println("ds3version: " + ds3version);
        
        //StringTokenizer tokenizer = new StringTokenizer(suffixTokens[1], ",");
        // int index2 = suffixTokens[1].indexOf(",");
        //String dataSetName = tokenizer.nextToken();
        //String tableNum = tokenizer.nextToken().trim();
        //String ds3version = tokenizer.nextToken().trim();

        // The (String) value of the version is retrieved as the third argument
        // of the tag. It is then converted to an integer. The value is then compared
        // to make sure it is > or = 0.

        // String ds3version = suffixTokens[1].substring(index2 + 5).trim();
        // String ds3version = suffixTokens[1].substring(index2 + 5);
        // System.out.println("This is not a number " + ds3version);
        int ds3IntVersion = Integer.parseInt(ds3version);
        // System.out.println("This is a number " + ds3IntVersion);
        if (ds3IntVersion < 0)
            throw new EmfException("The version number should be greater or equal to zero");

        // The dataset name is trimmed and then converted into an EmfDataset type through
        // the getDataset method.
        EmfDataset dataSet3 = getDataset(dataSetName.trim());
        if ( dataSet3 == null )
            throw new EmfException("The dataset name \"" + dataSetName + "\" is not valid");
        // The dataset id is retrieved from the dataset through its getId method.
        // It is then converted to a string to put in the hashtable.
        // Then the version is converted to an integer value.

        int dataSet3_id = dataSet3.getId();
        String ds3Id = Integer.toString(dataSet3_id);

        // The table number is retrieved from the second argument in the second token.
        // String tableNum = suffixTokens[1].substring(index2 + 1, index2 + 3).trim();

        // System.out.println("The integer version is: " + ds3IntVersion);
        // System.out.println("Version as integer " + ds3IntVersion);

        // A version object is created from the dataset id and the version.
        Version version1 = version(dataSet3_id, ds3IntVersion);

        // Chcek to see if the version1 value is valid, if not throw an exception.
        if (version1 == null)
            throw new EmfException("The version name must be valid: " + dataSet3.getName());
        // System.out.println("version object " + version1);

        // The complete path of the version is retrieved from the version object just created.

        versionCompletePath = version1.createCompletePath();

        /*
         * System.out.println("prefix: " + prefix); System.out.println("Table number: " + tableNum);
         * System.out.println("Dataset information: " + dataSet3); System.out.println("Dataset id: " + dataSet3_id);
         * //System.out.println("Dataset name: " + dataSetName); System.out.println("dataset of 3: " + dataSetName);
         * //System.out.println("Table number: " + tableNum); System.out.println("version of 3: " + ds3version);
         * System.out.println("alias of 3: " + suffixTokens[0]); System.out.println("The complete path for version 3 is: " +
         * versionCompletePath);
         */

        // The third string array value is calculated for the hashtable.
        // The appropriate key plus the string array (as a "value") is put into the hashtable.
        String[] ds3 = { dataSetName, suffixTokens[0], ds3version, ds3Id, versionCompletePath };
        tableValuesAliasesVersions.clear();
        tableValuesAliasesVersions.put("ds3", ds3);

        String versionClause = versionClause();
        String keywordReplaced = " (SELECT * from ";
        keywordReplaced += tableNameFromDataset(tableNum, dataSet3);
        keywordReplaced += " " + suffixTokens[0]; 
        keywordReplaced += " where " + versionClause;
        keywordReplaced += ") ";
        
        String rtnStr = prefix + keywordReplaced + suffixTokens[2];

        return rtnStr;
        
        // The table name from the dataset is derived from the method below off of the second token.
        // return prefix + versioned(tableNameFromDataset(tableNum, dataSet3) + suffixTokens[2]);
        // return "OK";
    }
    
//  Added this method to handle new tag $DATASET_QASTEP[ datasetname, QAStepName ]

    private String expandFourTag(String query) throws EmfException {
        // Point to the start of the second tag, the first substring is from the beginning of the
        // query to that point. The suffix is from that point to the end of the tag.
        String outputTable = "";
        EmfDataset dataSet4;
        
        int index = query.indexOf(startQAstepQueryTag);
        
        String prefix = query.substring(0, index);
        //System.out.println("Prefix: "+ prefix);
        String suffix = query.substring(index + startQAstepQueryTag.length());
        //System.out.println("Suffix: "+ suffix);

        // Split the suffix into two tokens
        String[] suffixTokens = noAliasSuffixSplit(suffix);
        //for (int v = 0; v < suffixTokens.length; v++) {
           // System.out.println(suffixTokens[v]+ "\n");
            //}

        // The dataset name is retreived as the first argument in the second token.
        // It is then trimmed and then converted into an EmfDataset type throught
        // the getDataset method created below.
        // Look for first double quote (fdq).  Go from there to second double quote (sdq).
        // The new first token is from fdq to sdq.
        // The new second token is from sdq to end of string.
        // Repeat once more for this tag.

        int quoteIndex = suffixTokens[0].indexOf("\"");
        String firstQuoteRemoved = suffixTokens[0].substring(quoteIndex + 1);
        //System.out.println("almostDataSetName: " + firstQuoteRemoved);
        int quoteIndex2 = firstQuoteRemoved.indexOf("\"");
        String dataSetName = firstQuoteRemoved.substring(0, quoteIndex2) + " ";
        //System.out.println("dataSetName: " + dataSetName);
        
        //int index4 = suffixTokens[0].indexOf(",");
        //String dataSetName = suffixTokens[0].substring(0, index4) + " ";
        //System.out.println("datasetName = \n" + dataSetName + "\n");
        if (dataSetName.trim().equals("CURRENT_DATASET")) {
            dataSet4 = dataset;
        } else {
            dataSet4 = getDataset(dataSetName.trim());
        }
        //EmfDataset dataSet4 = getDataset(dataSetName.trim());
        //System.out.println("dataSet4 = \n" + dataSet4 + "\n");

        // The integer value of the default version is retrieved from the getDefaultVersion
        // method of the EmfDataset just created. It is converted to a String for the hashtable.
        if ( dataSet4 == null )
            throw new EmfException("The dataset name \"" + dataSetName + "\" is not valid");
        
        int ds4IntVersion = dataSet4.getDefaultVersion();

        //System.out.println("Default Version as integer " + ds4IntVersion);

        // The dataset id is retrieved from the dataset through its getId method.
        // It is then converted to a string to put in the hashtable.
        // Next a version object is created from the dataset id and the version.

        //int dataSet4_id = dataSet4.getId();
        //String ds4Id = Integer.toString(dataSet4_id);
        //Version version1 = version(dataSet4_id, ds4IntVersion);
        //System.out.println("version object" + version1);

        String secondQuoteRemoved = firstQuoteRemoved.substring(quoteIndex2 + 1);
        int quoteIndex3 = secondQuoteRemoved.indexOf("\"");
        String thirdQuoteRemoved = secondQuoteRemoved.substring(quoteIndex3 + 1);
        int quoteIndex4 = thirdQuoteRemoved.indexOf("\"");
        String qaStepName = thirdQuoteRemoved.substring(0, quoteIndex4);
        //System.out.println("qaStepname: " + qaStepName);
        
        
        //The QA Step name is retrieved from the second argument in the second token.
        //String qaStepName = suffixTokens[0].substring(index4 + 1).trim();
        //System.out.println("QA Step Name: " + qaStepName);
        
        // Use the QAServiceImpl object which was created in the constructor.  Run the 
        // method which gets an array of the QA Steps associated with this dataset.
        
        QAStep [] qaSteps = qaServiceImpl.getQASteps(dataSet4);
        //System.out.println("The array size is: " + qaSteps.length);
        
        // Go through each of them and find the matching dataset with the correct name and
        // version number.
        for (int r = 0; r < qaSteps.length; r++) {
            //System.out.println("Next step: " + qaSteps[r]);
            //System.out.println(" Step version : " + qaSteps[r].getVersion());
            if (qaSteps[r].toString().equals(qaStepName) && qaSteps[r].getVersion()== ds4IntVersion) {
                QAStepResult qaStepResult = qaServiceImpl.getQAStepResult(qaSteps[r]);
                
                outputTable = qaStepResult.getTable();
                break;
            //System.out.println("The output table for the next step: " + outputTable);
            }
        }
        if (outputTable == "")
            throw new EmfException("The \"" +  qaStepName + "\" QA Step could not be found in the datset \"" + dataSet4 + 
                    "\", default version is "+ds4IntVersion+". ");

        // Only need to send back the schema concatenated with the output table.
        return prefix + emissionDatasourceName + "." + outputTable + suffixTokens[1];
    }

    // Added method to handle new tag $DATASET_QASTEP_VERSION[ datasetname, QAStepName, versionnum]
    
    private String expandFiveTag(String query) throws EmfException {
        // Point to the start of the second tag, the first substring is from the beginning of the
        // query to that point. The suffix is from that point to the end of the tag.
        String outputTable = "";
        EmfDataset dataSet5;
        
        int index = query.indexOf(startQAstepVersQueryTag);
        
        String prefix = query.substring(0, index);
        //System.out.println("Prefix: "+ prefix);
        String suffix = query.substring(index + startQAstepVersQueryTag.length());
        //System.out.println("Suffix: "+ suffix);

        // Split the suffix into two tokens
        String[] suffixTokens = noAliasSuffixSplit(suffix);

        // The dataset name is retreived as the first argument in the second token.
        // It is then trimmed and then converted into an EmfDataset type throught
        // the getDataset method created below.
        
        // The dataset name is retreived as the first argument in the first token.
        // Look for first double quote (fdq).  Go from there to second double quote (sdq).
        // The new first token is from fdq to sdq.
        // The new second token is from sdq to end of string.
        // Repeat once more for this tag.
        // From that point parse on the next comma because the other value is a number
        
        int quoteIndex = suffixTokens[0].indexOf("\"");
        String firstQuoteRemoved = suffixTokens[0].substring(quoteIndex + 1);
        //System.out.println("firstQuoteRemoved: " + firstQuoteRemoved);
        int quoteIndex2 = firstQuoteRemoved.indexOf("\"");
        String dataSetName = firstQuoteRemoved.substring(0, quoteIndex2) + " ";
        //System.out.println("dataSetName: " + dataSetName);
        
        String secondQuoteRemoved = firstQuoteRemoved.substring(quoteIndex2 + 1);
        int quoteIndex3 = secondQuoteRemoved.indexOf("\"");
        String thirdQuoteRemoved = secondQuoteRemoved.substring(quoteIndex3 + 1);
        int quoteIndex4 = thirdQuoteRemoved.indexOf("\"");
        String qaStepName = thirdQuoteRemoved.substring(0, quoteIndex4);
        //System.out.println("tableNum: " + qaStepName);
        
        String fourthQuoteRemoved = thirdQuoteRemoved.substring(quoteIndex4 + 1);
        //int quoteIndex5 = fourthQuoteRemoved.indexOf("\"");
        //String fifthQuoteRemoved = fourthQuoteRemoved.substring(quoteIndex5 + 1);
        //int quoteIndex6 = fifthQuoteRemoved.indexOf("\"");
        //String ds5version = fifthQuoteRemoved.substring(0, quoteIndex6);
        
        int commaIndex = fourthQuoteRemoved.indexOf(",");
        String ds5version = fourthQuoteRemoved.substring(commaIndex + 1).trim();
        
        //System.out.println("ds5version: " + ds5version);
        
        //StringTokenizer tokenizer = new StringTokenizer(suffixTokens[0], ",");
        // int index2 = suffixTokens[1].indexOf(",");
        //String dataSetName = tokenizer.nextToken();
        //System.out.println("datasetName = \n" + dataSetName + "\n");
        //String qaStepName = tokenizer.nextToken().trim();
        //String ds5version = tokenizer.nextToken().trim();

        //int index5 = suffixTokens[0].indexOf(",");
        //String dataSetName = suffixTokens[0].substring(0, index5) + " ";
        if (dataSetName.trim().equals("CURRENT_DATASET")) {
            dataSet5 = dataset;
        } else {
            dataSet5 = getDataset(dataSetName.trim());
        }
        
        if ( dataSet5 == null )
            throw new EmfException("The dataset name \"" + dataSetName + "\" is not valid");
        //EmfDataset dataSet5 = getDataset(dataSetName.trim());
        //System.out.println("Database name = \n" + dataSet5 + "\n");

        // The integer value of the default version is retrieved from the getDefaultVersion
        // method of the EmfDataset just created. It is converted to a String for the hashtable.

        //int ds5IntVersion = dataSet5.getDefaultVersion();

        //System.out.println("Default Version as integer " + ds4IntVersion);

        // The dataset id is retrieved from the dataset through its getId method.
        // It is then converted to a string to put in the hashtable.
        // Next a version object is created from the dataset id and the version.
        
        // The (String) value of the version is retrieved as the third argument
        // of the tag. It is then converted to an integer. The value is then compared
        // to make sure it is > or = 0.

        int ds5IntVersion = Integer.parseInt(ds5version);
        //System.out.println("This is a number " + ds5IntVersion);
        if (ds5IntVersion < 0)
            throw new EmfException("The version number should be greater or equal to zero");

        //int dataSet5_id = dataSet5.getId();
        //String ds5Id = Integer.toString(dataSet5_id);
        //Version version1 = version(dataSet5_id, ds5IntVersion);
        //System.out.println("version object" + version1);
        //System.out.println("version as integer" + ds5Id);

        //System.out.println("QA Step Name: " + qaStepName);
        
        // Use the QAServiceImpl object which was created in the constructor.  Run the 
        // method which gets an array of the QA Steps associated with this dataset.
        
        QAStep [] qaSteps = qaServiceImpl.getQASteps(dataSet5);
        //System.out.println("The array size is: " + qaSteps.length);
        
        // Go through each of them and find the matching dataset with the correct name and
        // version number.
        for (int r = 0; r < qaSteps.length; r++) {
            //System.out.println("The next step: " + qaSteps[r]);
            //int qaName = qaSteps[r].getId();
            //System.out.println("The id of the next step: " + qaName);
            if (qaSteps[r].toString().equals(qaStepName) && qaSteps[r].getVersion()== ds5IntVersion) {
                QAStepResult qaStepResult = qaServiceImpl.getQAStepResult(qaSteps[r]);
                outputTable = qaStepResult.getTable();
                break;
            //System.out.println("The output table for the next step: " + outputTable);
            }
        }
        if (outputTable == "")
            throw new EmfException("The \"" +  qaStepName + "\" QA Step could not be found in the " + dataSet5 + " dataset.");
        
        // Only need to send back the schema concatenated with the output table.
        return prefix + emissionDatasourceName + "." + outputTable + suffixTokens[1];
    }


    // Added method to create new version objects associated with the second and third tags.
    private Version version(int dataset_id, int versionNum) {
        // System.out.println("OK");
        Session session = sessionFactory.getSession();
        try {
            // System.out.println("OK");
            return new Versions().get(dataset_id, versionNum, session);

        } finally {
            session.close();
        }
    }

    private String versioned(String partQuery) {  //BUG 3618
        String versionClause = versionClause();
        // For debugging purposes
        // System.out.println("The version clause part of the query is " + versionClause);
        return insertVersionClause(partQuery, versionClause);

    }

    private String insertVersionClause(String partQuery, String versionClause) {
        
        String[] keywords = { "GROUP BY", "HAVING", "ORDER BY", "LIMIT" };
        //System.out.println("Version clause: " + versionClause);
        //Make new string of upper case just to handle these keywords.
        String tempPartQuery = partQuery.toUpperCase();
        //Create a "tempQuery to upper case" here
        String firstPart = partQuery;
        String secondPart = "";
        for (int i = 0; i < keywords.length; i++) {
            int index = tempPartQuery.indexOf(keywords[i]);
            if (index != -1) {
                firstPart = partQuery.substring(0, index);
                secondPart = partQuery.substring(index);
                break;
            }
        }
        if (tempPartQuery.indexOf("WHERE") == -1) {
        //if (firstPart.indexOf("WHERE") == -1)
            if (versionClause == "")
                return firstPart +  versionClause + " " + secondPart;
            
            return firstPart + " WHERE " + versionClause + " " + secondPart;
        }
        return firstPart + " AND " + versionClause + " " + secondPart;
    }

    // Modified method below to use an object of class QAVersioned Query instead of an object of class
    // VersionedQuery.

    private String versionClause() {
        QAVersionedQuery query = new QAVersionedQuery();

        // Modified to pass the hashtable instead of a scalar.
        return query.query(tableValuesAliasesVersions);
    }

    // Modified method below to add the alias value for multiple tables.

    private String[] suffixSplit(String token) throws EmfException {

        String nextDollarSign;

        // Look for a missing end tag and either the end of the query or the next dollar sign
        // If it is missing, throw an exception.

        if (token.indexOf(isErrorQueryTag) != -1)
            nextDollarSign = token.substring(0, token.indexOf(isErrorQueryTag));
        else
            nextDollarSign = token;

        int index = nextDollarSign.indexOf(endQueryTag);
        if (index == -1)
            throw new EmfException("The '" + endQueryTag + "' is expected in the program arguments:" + nextDollarSign);

        // Break up the token into a prefix and suffix string.

        String prefix = token.substring(0, index);
        String suffix = token.substring(index + endQueryTag.length());

//        System.out.println("suffixSplit prefix: " + prefix); 
//        System.out.println("suffixSplit suffix: " + suffix);
//        System.out.println("Index: " + index);
        
        if (prefix.trim().equals("")) {
            throw new EmfException("Appropriate comma-separated arguments must be specified within the brackets");
        }

        // Determine whether the alias value exists or not. If it does, isolate it.
        // If it does not, throw an exception.
        
        if (suffix.length() >= 1 && suffix.charAt(0) == ')') {
            aliasValue = null;
        } else if (suffix.length() >= 2 && suffix.charAt(1) == ')') {
            aliasValue = null;
        } else if 
        (
            suffix.length() == 2 && 
            (
                (
                    Character.isSpaceChar(suffix.charAt(0)) && Character.isLetter(suffix.charAt(1))
                    && suffix.charAt(1) != ')' 
                )
            )
        )
        {
            aliasValue = suffix.charAt(1) + "";
        } else if 
        (
                suffix.length() >= 3 && 
                (
                        (
                                Character.isSpaceChar(suffix.charAt(0)) && Character.isLetter(suffix.charAt(1))
                                && suffix.charAt(1) != ')' 
                                    && (!Character.isLetterOrDigit(suffix.charAt(2)))
                        )
                )
        )
        {
            aliasValue = suffix.charAt(1) + "";
        } else {
            throw new EmfException("A one-character alias value is expected after the '" + endQueryTag + "'" + suffix);
        }
        //System.out.println("alias: " + aliasValue);
        return new String[] { aliasValue, prefix, suffix };
    }
    
    
    private String[] noAliasSuffixSplit(String token) throws EmfException {

        String nextDollarSign;

        // Look for a missing end tag and either the end of the query or the next dollar sign
        // If it is missing, throw an exception.

        if (token.indexOf(isErrorQueryTag) != -1)
            nextDollarSign = token.substring(0, token.indexOf(isErrorQueryTag));
        else
            nextDollarSign = token;

        int index = nextDollarSign.indexOf(endQueryTag);
        if (index == -1)
            throw new EmfException("The '" + endQueryTag + "' is expected in the program arguments");

        // Break up the token into a prefix and suffix string.

        String prefix = token.substring(0, index);
        String suffix = token.substring(index + endQueryTag.length());
        
        return new String[] {prefix, suffix };
    }


    private String tableNameFromDataset(String tableNo, EmfDataset dataset) throws EmfException {
        int tableID = tableID(tableNo);
        InternalSource[] internalSources = dataset.getInternalSources();
        if (internalSources.length < tableID)
            throw new EmfException("The table number is larger than the number of tables in the dataset");
        return qualifiedName(emissionDatasourceName, internalSources[tableID - 1].getTable());
    }

    private int tableID(String tableNo) throws EmfException {
        try {
            int value = Integer.parseInt(tableNo);
            if (value <= 0)
                throw new EmfException("The table number should be greater or equal to one: "+tableNo );
            return value;
        } catch (NumberFormatException e) {
            throw new EmfException("Could not convert the table number to an integer -" + qaStep.getProgramArguments());
        }
    }

    public String createTableQuery() {
        // System.out.println("The table name in createTableQuery method is: " + tableName);
        return "CREATE TABLE " + qualifiedName(emissionDatasourceName, tableName) + " AS ";
    }

    private String qualifiedName(String datasourceName, String tableName) {
        if ("versions".equalsIgnoreCase(tableName.toLowerCase()) && "emissions".equalsIgnoreCase(datasourceName.toLowerCase())) {
            System.err.println("Versions table moved to EMF. Error in " + this.getClass().getName());
        }
        return datasourceName + "." + tableName;
    }

    // Added method to get the dataset given the dataset name.

    private EmfDataset getDataset(String dsName) throws EmfException {
        //System.out.println("Dataset name = \n" + dsName + "\n");
        DatasetDAO dao = new DatasetDAO();
        try {
            return dao.getDataset(sessionFactory.getSession(), dsName);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new EmfException("The dataset name " + dsName + " is not valid");
        }
    }

}
