package gov.epa.emissions.framework.services.data;

import gov.epa.emissions.commons.data.Country;
import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.data.KeyVal;
import gov.epa.emissions.commons.data.Lockable;
import gov.epa.emissions.commons.data.Mutex;
import gov.epa.emissions.commons.data.Project;
import gov.epa.emissions.commons.data.QAStepTemplate;
import gov.epa.emissions.commons.data.Region;
import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.db.intendeduse.IntendedUse;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

public class EmfDataset implements Dataset, Lockable, Comparable<EmfDataset> {

    private int id;// unique id needed for hibernate persistence

    private String name;

    private int year;

    private String description;

    private String status = ""; //so the selection based on status won't fail

    private Region region;

    private IntendedUse intendedUse;

    private Country country;

    private String units;

    private String creator;
    
    private String creatorFullName;

    private String temporalResolution;

    private Date startDateTime;

    private Date endDateTime;

    private Project project;

    private Date createdDateTime;

    private Date modifiedDateTime;

    private Date accessedDateTime;

    private DatasetType datasetType;

    private InternalSource[] internalSources = new InternalSource[]{};

    private KeyVal[] keyValsList = new KeyVal[]{};

    private Sector[] sectorsList = new Sector[]{};

    private int defaultVersion;

    private Mutex lock;

    public EmfDataset() {
        lock = new Mutex();
    }

    //constructor is really only useful for reporting purposes, the datasettype should be a
    //heavyweight object, not a light object like this...
    public EmfDataset(int id, String name, int defaultVersion, int datasetTypeId, String datasetTypeName) {
        this();
        this.id = id;
        this.name = name;
        this.defaultVersion = defaultVersion;
        this.datasetType = new DatasetType(datasetTypeId, datasetTypeName);
    }

    //constructor is really only useful for reporting purposes, the datasettype should be a
    //heavyweight object, not a light object like this...
    public EmfDataset(int id, String name, int defaultVersion, 
            Date modifiedDateTime, int datasetTypeId, 
            String datasetTypeName, String status,
            String creator, String creatorFullName, String intendedUse, 
            String project, String region, 
            Date startDateTime, Date stopDateTime,
            String temporalResolution) {
        this();
        this.id = id;
        this.name = name;
        this.defaultVersion = defaultVersion;
        this.modifiedDateTime = modifiedDateTime;
        this.datasetType = new DatasetType(datasetTypeId, datasetTypeName);
        this.status = status;
        this.creator = creator;
        this.creatorFullName = creatorFullName;
        if (intendedUse != null) this.intendedUse = new IntendedUse(intendedUse);
        if (project != null) this.project = new Project(project);
        if (region != null) this.region = new Region(region);
        this.startDateTime = startDateTime;
        this.endDateTime = stopDateTime;
        this.temporalResolution = temporalResolution;
    }

    public int getDefaultVersion() {
        return defaultVersion;
    }

    public void setDefaultVersion(int defaultVersion) {
        this.defaultVersion = defaultVersion;
    }

    public Date getAccessedDateTime() {
        return accessedDateTime;
    }

    public void setAccessedDateTime(Date accessedDateTime) {
        this.accessedDateTime = accessedDateTime;
    }

    public Date getCreatedDateTime() {
        return createdDateTime;
    }

    public void setCreatedDateTime(Date createdDateTime) {
        this.createdDateTime = createdDateTime;
    }

    public Date getModifiedDateTime() {
        return modifiedDateTime;
    }

    public void setModifiedDateTime(Date modifiedDateTime) {
        this.modifiedDateTime = modifiedDateTime;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public String getDatasetTypeName() {
        return datasetType != null ? datasetType.getName() : null;
    }

    public DatasetType getDatasetType() {
        return datasetType;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public void setTemporalResolution(String temporalResolution) {
        this.temporalResolution = temporalResolution;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public void setStartDateTime(Date time) {
        this.startDateTime = time;
    }

    public void setStopDateTime(Date time) {
        this.endDateTime = time;
    }

    public Region getRegion() {
        return region;
    }

    public int getYear() {
        return year;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDatasetType(DatasetType datasetType) {
        this.datasetType = datasetType;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public String getUnits() {
        return units;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getCreator() {
        return creator;
    }
    
    public void setCreatorFullName(String creatorFullName) {
        this.creatorFullName = creatorFullName;
    }

    public String getCreatorFullName() {
        return creatorFullName;
    }

    public String getTemporalResolution() {
        return temporalResolution;
    }

    public Date getStartDateTime() {
        return startDateTime;
    }

    public Date getStopDateTime() {
        return endDateTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean equals(Object other) {
        if (other == null || !(other instanceof Dataset)) {
            return false;
        }

        Dataset otherDataset = (Dataset) other;

        return (id == otherDataset.getId());
    }

    public int hashCode() {
        return name.hashCode();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public InternalSource[] getInternalSources() {
        return this.internalSources;
    }

    public void setInternalSources(InternalSource[] internalSources) {
        this.internalSources = internalSources;
    }

    public void addInternalSource(InternalSource source) {
        List<InternalSource> internalSourceList = new ArrayList<InternalSource>();
        internalSourceList.addAll(Arrays.asList(internalSources));
        internalSourceList.add(source);
        this.internalSources = internalSourceList.toArray(new InternalSource[0]);
    }

    public void addSector(Sector sector) {
        List<Sector> sectors = new ArrayList<Sector>();
        sectors.addAll(Arrays.asList(this.sectorsList));
        sectors.add(sector);
        
        this.sectorsList = sectors.toArray(new Sector[0]);
    }

    public Sector[] getSectors() {
        return this.sectorsList;
    }

    public void setSectors(Sector[] sectors) {
        this.sectorsList = sectors;
    }

    public void addKeyVal(KeyVal keyval) {
        List<KeyVal> keyVals = new ArrayList<KeyVal>();
        keyVals.addAll(Arrays.asList(this.keyValsList));
        keyVals.add(keyval);
        
        this.keyValsList = keyVals.toArray(new KeyVal[0]);
    }
    
    public void addKeyVal(KeyVal[] newKeyvals) {
        List<KeyVal> keyVals = new ArrayList<KeyVal>();
        keyVals.addAll(Arrays.asList(this.keyValsList));
        keyVals.addAll(Arrays.asList(newKeyvals));
        
        this.keyValsList = keyVals.toArray(new KeyVal[0]);
    }

    public KeyVal[] getKeyVals() {
        return this.keyValsList;
    }

    public void setKeyVals(KeyVal[] keyvals) {
        this.keyValsList = keyvals;
    }

    public void setSummarySource(InternalSource summary) {
        // TODO: implement Summary
    }

    public InternalSource getSummarySource() {
        return null;
    }

    public Date getLockDate() {
        return lock.getLockDate();
    }

    public void setLockDate(Date lockDate) {
        lock.setLockDate(lockDate);
    }

    public String getLockOwner() {
        return lock.getLockOwner();
    }

    public void setLockOwner(String owner) {
        lock.setLockOwner(owner);
    }

    public boolean isLocked(String owner) {
        return lock.isLocked(owner);
    }

    public boolean isLocked(User owner) {
        return lock.isLocked(owner);
    }

    public boolean isLocked() {
        return lock.isLocked();
    }

    public IntendedUse getIntendedUse() {
        return intendedUse;
    }

    public void setIntendedUse(IntendedUse intendedUse) {
        this.intendedUse = intendedUse;
    }

    public boolean getInlineCommentSetting() {
        KeyVal[] keyvals = mergeKeyVals();
        for (int i = 0; i < keyvals.length; i++) {
            String keyword = keyvals[i].getKeyword().getName();
            String value = keyvals[i].getValue();
            if (keyword.equalsIgnoreCase(inline_comment_key))
                return (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("yes")) ? true : false;
        }

        return true;
    }

    public boolean getHeaderCommentsSetting() {
        KeyVal[] keyvals = mergeKeyVals();
        for (int i = 0; i < keyvals.length; i++) {
            String keyword = keyvals[i].getKeyword().getName();
            String value = keyvals[i].getValue();
            if (keyword.equalsIgnoreCase(header_comment_key))
                return (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("yes")) ? true : false;
        }

        return true;
    }

    public String getInlineCommentChar() {
        KeyVal[] keyvals = mergeKeyVals();
        for (int i = 0; i < keyvals.length; i++) {
            String keyword = keyvals[i].getKeyword().getName();
            String value = keyvals[i].getValue();
            if (keyword.equalsIgnoreCase(inline_comment_char))
                return (value.length() == 1) ? value : "!";
        }

        return "!";
    }

    public String getHeaderCommentChar() {
        KeyVal[] keyvals = mergeKeyVals();
        for (int i = 0; i < keyvals.length; i++) {
            String keyword = keyvals[i].getKeyword().getName();
            String value = keyvals[i].getValue();
            if (keyword.equalsIgnoreCase(header_comment_char))
                return (value.length() == 1) ? value : "#";
        }

        return "#";
    }
    
    public int getCSVHeaderLineSetting() {
        KeyVal[] keyvals = mergeKeyVals();
        for (int i = 0; i < keyvals.length; i++) {
            String keyword = keyvals[i].getKeyword().getName();
            String value = keyvals[i].getValue();
            if (keyword.equalsIgnoreCase(csv_header_line)) {
                if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("yes"))
                    return lower_case;
                
                if (value.toUpperCase().equals(csv_header_line_value))
                    return upper_case;
                
                return no_head_line;
            }
        }

        return lower_case;
    }

    public boolean isExternal() {
        return datasetType == null ? false : datasetType.isExternal();
    }

    public KeyVal[] mergeKeyVals() {
        if (datasetType == null)
            return keyValsList;
        
        List<KeyVal> result = new ArrayList<KeyVal>();
        result.addAll(Arrays.asList(keyValsList));

        KeyVal[] datasetTypeKeyVals = datasetType.getKeyVals();

        for (int i = 0; i < datasetTypeKeyVals.length; i++) {
            if (!contains(result, datasetTypeKeyVals[i])) {
                result.add(datasetTypeKeyVals[i]);
            }
        }
        return result.toArray(new KeyVal[0]);
    }

    public boolean contains(List keyVals, KeyVal newKeyVal) {
        for (Iterator iter = keyVals.iterator(); iter.hasNext();) {
            KeyVal element = (KeyVal) iter.next();
            if (element.getKeyword().equals(newKeyVal.getKeyword()))
                return true;
        }

        return false;
    }

    public String toString() {
        return name;
    }
    
    public void checkAndRunSummaryQASteps(QAStep[] summarySteps, int version) throws EmfException {
        if (!templateExists(summarySteps))
            throw new EmfException("Summary QAStepTemplate doesn't exist in dataset type: " + datasetType.getName());
        
        try {
            //
        } catch (Exception e) {
            throw new EmfException("Cann't run summary QASteps: " + e.getMessage());
        }
    }

    private boolean templateExists(QAStep[] summarySteps) {
        QAStepTemplate[] templates = datasetType.getQaStepTemplates();
        String[] names = new String[templates.length];
        
        for (int i = 0; i < templates.length; i++)
            names[i] = templates[i].getName();
        
        List templateNames = Arrays.asList(names);
        
        for (int i = 0; i < summarySteps.length; i++)
            if (!templateNames.contains(summarySteps[i].getName()))
                return false;
        
        return true;
    }

    //for testing...
    public static void main(String[] args) {
        //
        EmfDataset dataset = new EmfDataset();
        dataset.setName("test");
        Calendar cal = Calendar.getInstance();
        dataset.setStartDateTime(cal.getTime());
        dataset.setStopDateTime(cal.getTime());
        System.out.println(dataset.applicableMonth());
        
    }
    
    //returns:  -1 indicates no month, most likely a annual dataset not monthly
    //          if not -1 will return Calendar.month values
    //          values are based on 0 based (i.e., Jan = 0 ... Dec = 11)
    public int applicableMonth() {

        //code was copied from SQLQAAnnualQuery.java class
        
        //Check for month name and year name here
        
        //String year = "";
        int month = -1;
        
        
        // The names and/or properties of the dataset are to be checked to determine year and month that 
        // the dataset is for. If there is more than one file for a month, it must be put in its own list
        // with other such files.
        
        // New String Tokenizers for the StartDate and StopDate values.
        // They are compared to determine if they fall in the same month.
        int startYear = -1;
        int startMonth = -1;
        int stopYear = -1;
        int stopMonth = -1;
        Calendar cal = Calendar.getInstance();
        if (startDateTime != null) {
            cal.setTime(startDateTime);
            startYear = cal.get(cal.YEAR);
            startMonth = cal.get(cal.MONTH);
        }

        if (endDateTime != null) {
            cal.setTime(endDateTime);
            stopYear = cal.get(cal.YEAR);
            stopMonth = cal.get(cal.MONTH);
        }
        
        // New String Tokenizer to parse the dataset names to find month values.
        StringTokenizer tokenizer7 = new StringTokenizer(name, "_");
        String month2 = "";
        while (tokenizer7.hasMoreTokens()) {
            String unsure = tokenizer7.nextToken();
            if(unsure.equalsIgnoreCase("jan")||unsure.toLowerCase().startsWith("jan")) {
                month2 = "jan";
                break;
            }
            if(unsure.equalsIgnoreCase("feb")||unsure.toLowerCase().startsWith("feb"))
            {
                month2 = "feb";
            break;
            }
            if(unsure.equalsIgnoreCase("mar")||unsure.toLowerCase().startsWith("mar"))
            {
                month2 = "mar";
            break;
            }
            if(unsure.equalsIgnoreCase("apr")||unsure.toLowerCase().startsWith("apr"))
            {
                month2 = "apr";
            break;
            }
            if(unsure.equalsIgnoreCase("may")||unsure.toLowerCase().startsWith("may"))
            {
                month2 = "may";
            break;
            }
            if(unsure.equalsIgnoreCase("jun")||unsure.toLowerCase().startsWith("jun"))
            {
                month2 = "jun";
            break;
            }
            if(unsure.equalsIgnoreCase("jul")||unsure.toLowerCase().startsWith("jul"))
            {
                month2 = "jul";
            break;
            }
            if(unsure.equalsIgnoreCase("aug")||unsure.toLowerCase().startsWith("aug"))
            {
                month2 = "aug";
            break;
            }
            if(unsure.equalsIgnoreCase("sep")||unsure.toLowerCase().startsWith("sep"))
            {
                month2 = "sep";
            break;
            }
            if(unsure.equalsIgnoreCase("oct")||unsure.toLowerCase().startsWith("oct"))
            {
                month2 = "oct";
            break;
            }
            if(unsure.equalsIgnoreCase("nov")||unsure.toLowerCase().startsWith("nov"))
            {
                month2 = "nov";
            break;
            }
            if(unsure.equalsIgnoreCase("dec")||unsure.toLowerCase().startsWith("dec")) {
                {
                    month2 = "dec";
                break;
            }
            }
        }        
    
        if(startMonth == stopMonth && startYear == stopYear) {
            month = startMonth;
            //System.out.println("The month of the dataset from startMonth is: " + month);
        } else if (!(month2.equals(""))){
            if (month2.equalsIgnoreCase("jan") || month2.equalsIgnoreCase("january") || month2.equals("01"))
                month = cal.JANUARY;
            else if (month2.equalsIgnoreCase("feb") || month2.equalsIgnoreCase("february") || month2.equals("02"))
                month = cal.FEBRUARY;
            else if (month2.equalsIgnoreCase("mar") || month2.equalsIgnoreCase("march") || month2.equals("03"))
                month = cal.MARCH;
            else if (month2.equalsIgnoreCase("apr") || month2.equalsIgnoreCase("april") || month2.equals("04"))
                month = cal.APRIL;
            else if (month2.equalsIgnoreCase("may") || month2.equals("05"))
                month = cal.MAY;
            else if (month2.equalsIgnoreCase("jun") || month2.equalsIgnoreCase("june") || month2.equals("06"))
                month = cal.JUNE;
            else if (month2.equalsIgnoreCase("jul") || month2.equalsIgnoreCase("july") || month2.equals("07"))
                month = cal.JULY;
            else if (month2.equalsIgnoreCase("aug") || month2.equalsIgnoreCase("august") || month2.equals("08"))
                month = cal.AUGUST;
            else if (month2.equalsIgnoreCase("sep") || month2.equalsIgnoreCase("september") || month2.equals("09"))
                month = cal.SEPTEMBER;
            else if (month2.equalsIgnoreCase("oct") || month2.equalsIgnoreCase("october") || month2.equals("10"))
                month = cal.OCTOBER;
            else if (month2.equalsIgnoreCase("nov") || month2.equalsIgnoreCase("november") || month2.equals("11"))
                month = cal.NOVEMBER;
            else if (month2.equalsIgnoreCase("dec") || month2.equalsIgnoreCase("december") || month2.equals("12"))
                month = cal.DECEMBER;
        }
        // Then the file or files must be put into the appropriate method call to create a monthly 
        // query for them.
        
        //System.out.println("The dataset is :" + allDatasetNames.get(j).toString());
        
        //Add exceptions for case where month value not found
        
        return month;
    }
    
    public int compareTo(EmfDataset other) {
        return getName().compareToIgnoreCase(other.getName());
    }

//    public Integer getApplicableYear() throws EmfException {
//
//        //code was copied from SQLQAAnnualQuery.java class
//        
//        //Check for month name and year name here
//        
//        Integer year = null;
//        
//        
//        // The names and/or properties of the dataset are to be checked to determine year and month that 
//        // the dataset is for. If there is more than one file for a month, it must be put in its own list
//        // with other such files.
//        
//        // New String Tokenizers for the StartDate and StopDate values.
//        // They are compared to determine if they fall in the same month.
//        
//        StringTokenizer tokenizer5 = new StringTokenizer(startDateTime.toString());
//        
//        String yearMonthDay = tokenizer5.nextToken();
//        StringTokenizer tokenizer8 = new StringTokenizer(yearMonthDay, "-");
//        
//        String startYear = tokenizer8.nextToken();
//        String startMonth = tokenizer8.nextToken();
//        
//        StringTokenizer tokenizer6 = new StringTokenizer(endDateTime.toString());
//        
//        String yearMonthDay2 = tokenizer6.nextToken();
//        StringTokenizer tokenizer9 = new StringTokenizer(yearMonthDay2, "-");
//        
//        String stopYear = tokenizer9.nextToken();
//        String stopMonth = tokenizer9.nextToken();
//        
//        if(startMonth.equals(stopMonth) && startYear.equals(stopYear)) {
//            year = Integer.parseInt(startYear);
//            //System.out.println("The month of the dataset from startMonth is: " + month);
//            //System.out.println("The month of the dataset from month2 is: " + month);
//        }else {
//            throw new EmfException("The dataset covers more than one month.");
//        }
//        return year;
//    }
}
