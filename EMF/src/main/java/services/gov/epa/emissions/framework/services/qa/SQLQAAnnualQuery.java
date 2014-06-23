package gov.epa.emissions.framework.services.qa;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.util.ArrayList;
import java.util.StringTokenizer;

public class SQLQAAnnualQuery extends SQLQAProgramQuery{
    
    // Input is currently a set of 12 or 24 or 12*n monthly files
    // The lists are filled using command-line and/or GUI input.
    

    public SQLQAAnnualQuery(HibernateSessionFactory sessionFactory, String emissioDatasourceName, String tableName, QAStep qaStep) {       
        super(sessionFactory,emissioDatasourceName,tableName,qaStep);
        
    }
    
    ArrayList<String> janDatasetNames = new ArrayList<String>();
    
    ArrayList<String> febDatasetNames = new ArrayList<String>();
    ArrayList<String> marDatasetNames = new ArrayList<String>();
    ArrayList<String> aprDatasetNames = new ArrayList<String>();
    ArrayList<String> mayDatasetNames = new ArrayList<String>();
    ArrayList<String> junDatasetNames = new ArrayList<String>();
    ArrayList<String> julDatasetNames = new ArrayList<String>();
    ArrayList<String> augDatasetNames = new ArrayList<String>();
    ArrayList<String> sepDatasetNames = new ArrayList<String>();
    ArrayList<String> octDatasetNames = new ArrayList<String>();
    ArrayList<String> novDatasetNames = new ArrayList<String>();
    ArrayList<String> decDatasetNames = new ArrayList<String>();
    
    String janQuery = "";
    String febQuery = "";
    String marQuery = "";
    String aprQuery = "";
    String mayQuery = "";
    String junQuery = "";
    String julQuery = "";
    String augQuery = "";
    String sepQuery = "";
    String octQuery = "";
    String novQuery = "";
    String decQuery = "";
    
    ArrayList<String> allDatasetNames = new ArrayList<String>();
    
    public String createAnnualQuery() throws EmfException {
        
        String programArguments = qaStep.getProgramArguments();
        
        //System.out.println("The string is: " + programArguments);
        
        // Code to filter out the datasets and put them into the main array list and String.
        
        String inventoriesToken = "";
        String invtableToken = "";
        String summaryTypeToken = "State+SCC";
        String invTableDatasetName = "";
        
        int index1 = programArguments.indexOf(QAStep.invTableTag);
        int index2 = programArguments.indexOf(QAStep.summaryTypeTag);
        inventoriesToken = programArguments.substring(0, index1);
        invtableToken = programArguments.substring(index1 + QAStep.invTableTag.length(),index2 == -1 ? programArguments.length() : index2 );

        if (index2 != -1) {
            summaryTypeToken = programArguments.substring(index2 + QAStep.summaryTypeTag.length()).trim();
        } 
        
      //default just in case...
        if (summaryTypeToken.trim().length() == 0)
            summaryTypeToken = "State+SCC";
        
         StringTokenizer tokenizer2 = new StringTokenizer(inventoriesToken, "\n");
         tokenizer2.nextToken();
         while (tokenizer2.hasMoreTokens()) {
             String datasetName = tokenizer2.nextToken().trim();
             //System.out.println("The dataset name is : \n" + datasetName);
             if (datasetName.length() > 0){
                 allDatasetNames.add(datasetName);
                 datasetNames.add(datasetName);
             }
             else {
                 //see if there are tables to build the query with, if not throw an exception
                 throw new EmfException("There are no Onroad Inventory datasets specified.");
             }
         }
         StringTokenizer tokenizer3 = new StringTokenizer(invtableToken, "\n");
         while (tokenizer3.hasMoreTokens()) {
             invTableDatasetName  = tokenizer3.nextToken().trim();
             if (invTableDatasetName.length() > 0) {
                 hasInvTableDataset = true;
                 datasetNames.add(invTableDatasetName);
             }
         }

        checkDataset();
        
        //Create the outer query        
        String outerQuery = "select @!@, coalesce(" + (hasInvTableDataset ? "i.name," : "") + "p.pollutant_code_desc, te.poll) as poll," //"select @!@, i.name, sum(coalesce(cast(i.factor as double precision)* mo_emis, 1.0*mo_emis)) as ann_emis from\n # as te left outer join\n $DATASET_TABLE[\"" + invTableDatasetName + "\", 1] i on te.poll = i.cas  group by @@@, i.name order by @@@, i.name";
            + "sum(" + (hasInvTableDataset ? "coalesce(cast(i.factor as double precision), 1.0)" : "1.0") + " * mo_emis) as ann_emis "
            + "from\n # as te "
            + (hasInvTableDataset ? "\nleft outer join\n $DATASET_TABLE[\"" + invTableDatasetName + "\", 1] i \non te.poll = i.cas " : " ")
            + " \nleft outer join reference.pollutant_codes p \non te.poll = p.pollutant_code "
            + " \ngroup by @@@, coalesce(" + (hasInvTableDataset ? "i.name," : "") + "p.pollutant_code_desc, te.poll)"
            + " \norder by @@@, coalesce(" + (hasInvTableDataset ? "i.name," : "") + "p.pollutant_code_desc, te.poll)";
        
        //System.out.println("The input for the outer query is: \n" + outerQuery);
        String almostQuery = query(outerQuery, true);
        
        //System.out.println("The outer part of the query so is: \n" + almostQuery + "\n");
        //System.out.println("The query so far is: \n" + fullQuery);
        
        //Find the pound tag to make the substitution of the monthly query part.
        int index = almostQuery.indexOf(QAStep.poundQueryTag);
        if (index == -1)
            throw new EmfException("The '" + QAStep.poundQueryTag + "' is expected in the program arguments");
        
        //Break up the token into a prefix and suffix string.
        
        String annualQueryPrefix = almostQuery.substring(0, index);
        String annualQuerySuffix = almostQuery.substring(index + QAStep.poundQueryTag.length());
        
        // Add a new section to take the different dataset names (which will be input through the GUI window).
        
        for (int j = 0; j < allDatasetNames.size(); j++) {
            //Check for month name and year name here
            
            //String year = "";
            String month = "";
            EmfDataset dataset=null;
            try {
//                System.out.println("allDatasetNames.size() = " + allDatasetNames.size() + "  "+allDatasetNames.get(j));
                dataset = getDataset(allDatasetNames.get(j).toString().trim());
            } catch(EmfException ex){
                throw new EmfException("The dataset name " + allDatasetNames.get(j) + " is not valid");
            }
                        
            // The names and/or properties of the dataset are to be checked to determine year and month that 
            // the dataset is for. If there is more than one file for a month, it must be put in its own list
            // with other such files.
            
            // New String Tokenizers for the StartDate and StopDate values.
            // They are compared to determine if they fall in the same month.
            if ( dataset.getStartDateTime()==null )
                throw new EmfException("The start date is not set for dataset: " + dataset.getName() );
            StringTokenizer tokenizer5 = new StringTokenizer(dataset.getStartDateTime().toString());
            
            String yearMonthDay = tokenizer5.nextToken();
            StringTokenizer tokenizer8 = new StringTokenizer(yearMonthDay, "-");
            
            String startYear = tokenizer8.nextToken();
            String startMonth = tokenizer8.nextToken();
            
            if ( dataset.getStopDateTime()==null )
                throw new EmfException("The stop date is not set for dataset: " + dataset.getName() );
            StringTokenizer tokenizer6 = new StringTokenizer(dataset.getStopDateTime().toString());
            
            String yearMonthDay2 = tokenizer6.nextToken();
            StringTokenizer tokenizer9 = new StringTokenizer(yearMonthDay2, "-");
            
            String stopYear = tokenizer9.nextToken();
            String stopMonth = tokenizer9.nextToken();
            
            // New String Tokenizer to parse the dataset names to find month values.
            StringTokenizer tokenizer7 = new StringTokenizer(dataset.toString(), "_");
            String month2 = "";
            while (tokenizer7.hasMoreTokens()) {
                String unsure = tokenizer7.nextToken();
                if(unsure.equalsIgnoreCase("jan")||
                   unsure.equalsIgnoreCase("feb")||
                   unsure.equalsIgnoreCase("mar")||
                   unsure.equalsIgnoreCase("apr")||
                   unsure.equalsIgnoreCase("may")||
                   unsure.equalsIgnoreCase("jun")||
                   unsure.equalsIgnoreCase("jul")||
                   unsure.equalsIgnoreCase("aug")||
                   unsure.equalsIgnoreCase("sep")||
                   unsure.equalsIgnoreCase("oct")||
                   unsure.equalsIgnoreCase("nov")||
                   unsure.equalsIgnoreCase("dec")) {
                month2 = unsure;
                }
            }
            
            if(startMonth.equals(stopMonth) && startYear.equals(stopYear)) {
                month = startMonth;
                //System.out.println("The month of the dataset from startMonth is: " + month);
            } else if (!(month2.equals(""))){
                month = month2;
                //System.out.println("The month of the dataset from month2 is: " + month);
            }else {
                throw new EmfException("The dataset covers more than one month.");
            }
            // Then the file or files must be put into the appropriate method call to create a monthly 
            // query for them.
            
            //System.out.println("The dataset is :" + allDatasetNames.get(j).toString());
            
            //Add exceptions for case where month value not found
            if (month.equalsIgnoreCase("jan") || month.equalsIgnoreCase("january") || month.equals("01"))
                janDatasetNames.add(allDatasetNames.get(j).toString());
            else if (month.equalsIgnoreCase("feb") || month.equalsIgnoreCase("february") || month.equals("02"))
                febDatasetNames.add(allDatasetNames.get(j).toString());
            else if (month.equalsIgnoreCase("mar") || month.equalsIgnoreCase("march") || month.equals("03"))
                marDatasetNames.add(allDatasetNames.get(j).toString());
            else if (month.equalsIgnoreCase("apr") || month.equalsIgnoreCase("april") || month.equals("04"))
                aprDatasetNames.add(allDatasetNames.get(j).toString());
            else if (month.equalsIgnoreCase("may") || month.equals("05"))
                mayDatasetNames.add(allDatasetNames.get(j).toString());
            else if (month.equalsIgnoreCase("jun") || month.equalsIgnoreCase("june") || month.equals("06"))
                junDatasetNames.add(allDatasetNames.get(j).toString());
            else if (month.equalsIgnoreCase("jul") || month.equalsIgnoreCase("july") || month.equals("07"))
                julDatasetNames.add(allDatasetNames.get(j).toString());
            else if (month.equalsIgnoreCase("aug") || month.equalsIgnoreCase("august") || month.equals("08"))
                augDatasetNames.add(allDatasetNames.get(j).toString());
            else if (month.equalsIgnoreCase("sep") || month.equalsIgnoreCase("september") || month.equals("09"))
                sepDatasetNames.add(allDatasetNames.get(j).toString());
            else if (month.equalsIgnoreCase("oct") || month.equalsIgnoreCase("october") || month.equals("10"))
                octDatasetNames.add(allDatasetNames.get(j).toString());
            else if (month.equalsIgnoreCase("nov") || month.equalsIgnoreCase("november") || month.equals("11"))
                novDatasetNames.add(allDatasetNames.get(j).toString());
            else if (month.equalsIgnoreCase("dec") || month.equalsIgnoreCase("december") || month.equals("12"))
                decDatasetNames.add(allDatasetNames.get(j).toString());
        }
        
        // Then the full query must be created by piecing togther all of the monthly
        // queries.  Somehow every clause needs to include a union all except the last one.
        
        // Here there needs to be a series of "if"/"else if" clauses which collectively run
        // each of the appropriate monthly queries by determining if there are any datasets 
        // for that month.  The results can be stored in a String array.  If no datasets exist 
        // for a given month, that string must be empty.
        
        
        if (janDatasetNames.size() > 0) {
            janQuery = createMonthlyQuery(31, janDatasetNames);
        }
        if (febDatasetNames.size() > 0) {
            febQuery = createMonthlyQuery(28, febDatasetNames);
        }
        if (marDatasetNames.size() > 0) {
            marQuery = createMonthlyQuery(31, marDatasetNames);
        }
        if (aprDatasetNames.size() > 0) {
            aprQuery = createMonthlyQuery(30, aprDatasetNames);
        }
        if (mayDatasetNames.size() > 0) {
            mayQuery = createMonthlyQuery(31, mayDatasetNames);
        }
        if (junDatasetNames.size() > 0) {
            junQuery = createMonthlyQuery(30, junDatasetNames);
        }
        if (julDatasetNames.size() > 0) {
            julQuery = createMonthlyQuery(31, julDatasetNames);
        }
        if (augDatasetNames.size() > 0) {
            augQuery = createMonthlyQuery(31, augDatasetNames);
        }
        if (sepDatasetNames.size() > 0) {
            sepQuery = createMonthlyQuery(30, sepDatasetNames);
        }
        if (octDatasetNames.size() > 0) {
            octQuery = createMonthlyQuery(31, octDatasetNames);
        }
        if (novDatasetNames.size() > 0) {
            novQuery = createMonthlyQuery(30, novDatasetNames);
        }
        if (decDatasetNames.size() > 0) {
            decQuery = createMonthlyQuery(31, decDatasetNames);
        }
        
        String fullQuery1 = annualQueryPrefix + "(" + janQuery + febQuery + marQuery + aprQuery + mayQuery + 
        junQuery + julQuery + augQuery + sepQuery + octQuery + novQuery + decQuery + ")" + annualQuerySuffix;
        
        //Use pattern matching to remove the last and unrequired union all from the query
        String fullQuery = fullQuery1.replaceAll("poll  union all...as te", "poll ) as te");
      //replace @!@ symbol with main columns in outer select statement
        String sql = "";
        if (summaryTypeToken.equals("State+SCC")) 
            sql = "te.fipsst, te.scc";
        else if (summaryTypeToken.equals("State")) 
            sql = "te.fipsst";
        else if (summaryTypeToken.equals("County")) 
            sql = "te.fips";
        else if (summaryTypeToken.equals("County+SCC")) 
            sql = "te.fips, te.scc";
        else if (summaryTypeToken.equals("SCC")) 
            sql = "te.scc";
        fullQuery = fullQuery.replaceAll("@!@", sql);
        
      //replace @@@ symbol with group by columns in outer select statement
        if (summaryTypeToken.equals("State+SCC")) 
            sql = "te.fipsst, te.scc";
        else if (summaryTypeToken.equals("State")) 
            sql = "te.fipsst";
        else if (summaryTypeToken.equals("County")) 
            sql = "te.fips";
        else if (summaryTypeToken.equals("County+SCC")) 
            sql = "te.fips, te.scc";
        else if (summaryTypeToken.equals("SCC")) 
            sql = "te.scc";
        fullQuery = fullQuery.replaceAll("@@@", sql);
        
      //replace !@! symbol with main columns in inner select statement
        if (summaryTypeToken.equals("State+SCC")) 
            sql = "substr(fips, 1, 2) as fipsst, scc";
        else if (summaryTypeToken.equals("State")) 
            sql = "substr(fips, 1, 2) as fipsst";
        else if (summaryTypeToken.equals("County")) 
            sql = "fips";
        else if (summaryTypeToken.equals("County+SCC")) 
            sql = "fips, scc";
        else if (summaryTypeToken.equals("SCC")) 
            sql = "scc";
        fullQuery = fullQuery.replaceAll("!@!", sql);
        
      //replace !!! symbol with group by columns in inner select statement
        if (summaryTypeToken.equals("State+SCC")) 
            sql = "substr(fips, 1, 2), scc";
        else if (summaryTypeToken.equals("State")) 
            sql = "substr(fips, 1, 2)";
        else if (summaryTypeToken.equals("County")) 
            sql = "fips";
        else if (summaryTypeToken.equals("County+SCC")) 
            sql = "fips, scc";
        else if (summaryTypeToken.equals("SCC")) 
            sql = "scc";
        fullQuery = fullQuery.replaceAll("!!!", sql);

        //System.out.println("The final query is : " + fullQuery);
        return fullQuery;
        //return query(fullQuery, true);
        
    }

        private String createMonthlyQuery(int daysInMonth, ArrayList datasetNames) throws EmfException
{
           String monthlyQueryPrefix = "select !@!, poll, sum( avd_emis * ";

           String monthlyQueryMiddle = ") as mo_emis from\n $DATASET_TABLE[\"";
           //System.out.println("middle query: " + monthlyQueryMiddle);
           
           String monthlyQuerySuffix = "\", 1] m group by !!!, poll ";
           //System.out.println("end query: " + monthlyQuerySuffix);

           String fullMonthlyQuery = "";
           
           String currentMonthlyQuery = "";
           
           //String [] singleMonthlyQuery = new String [5];

           for (int i = 0; i < datasetNames.size(); i++)
           {
               //System.out.println("Dataset names: " + datasetNames );
               currentMonthlyQuery = monthlyQueryPrefix + daysInMonth + monthlyQueryMiddle + 
                  datasetNames.get(i).toString().trim() + monthlyQuerySuffix;

               fullMonthlyQuery += query(currentMonthlyQuery, false);  // expand the intermediate query to 
                 // use the version info for the monthly inventory datasets 
                 // note that the big query below does not have version info for the 
                 // monthly datasets, but the real version will need to
               
               if (i != datasetNames.size() -1 && i >=0)
                   fullMonthlyQuery+= " union all ";
                   
           }
           
           if (datasetNames.size() > 0)
           {
               fullMonthlyQuery += " union all ";
               
           }
           
           //System.out.println("Fullmonth: " + fullMonthlyQuery + "\n");
           return fullMonthlyQuery;
        }
        // This method uses a newly created constructor of SQLQueryParser.
        
        //private String query(DbServer dbServer, QAStep qaStep, String tableName, String partialQuery, EmfDataset dataset, Version version) throws EmfException {
        private String query(String partialQuery, boolean createClause) throws EmfException {
            
            SQLQueryParser parser = new SQLQueryParser(sessionFactory, emissionDatasourceName, tableName );
            return parser.parse(partialQuery, createClause);
        }
        
}
