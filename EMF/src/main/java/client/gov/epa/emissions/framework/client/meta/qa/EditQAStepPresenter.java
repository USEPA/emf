package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.data.PivotConfiguration;
import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.commons.data.ProjectionShapeFile;
import gov.epa.emissions.commons.data.QAProgram;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.preference.DefaultUserPreferences;
import gov.epa.emissions.framework.client.preference.UserPreference;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.EmfProperty;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.data.QAStepResult;
import gov.epa.emissions.framework.services.qa.QAService;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.Date;
import java.util.StringTokenizer;

public class EditQAStepPresenter {

    private EditQAStepView view;

    private EmfDataset dataset;

    private EditableQATabView tabView;

    private QAStep qaStep;

    private EmfSession session;

    private static String lastFolder = null;

    public EditQAStepPresenter(EditQAStepView view, EmfDataset dataset, EditableQATabView tabView, EmfSession session) {
        this.view = view;
        this.tabView = tabView;
        this.dataset = dataset;
        this.session = session;
    }

    public void display(QAStep step, String versionName) throws EmfException {
        view.observe(this);
        this.qaStep = step;
        
        QAProgram[] programs = qaService().getQAPrograms();
        QAStepResult result = qaService().getQAStepResult(step);
        Boolean sameAstemplate = getSameAsTemplate(step);
        view.display(step, result, programs, dataset, versionName, sameAstemplate, session);

        // Reversed the following line from behind the one after it to make sure that most recent folder
        // is displayed properly.
        view.setMostRecentUsedFolder(getFolder());
    }
    
    private QAService qaService(){
        return session.qaService();
    }

    public void close() {
        view.disposeView();
    }

    public void save() throws EmfException {
        QAStep step = view.save();
        session.qaService().updateWitoutCheckingConstraints(new QAStep[] { step });
        tabView.refresh();
        //close();
    }

    public void run() throws EmfException {
        QAStep step = view.save();
        step.setStatus("In Progress");
        step.setDate(new Date());
        step.setWho(session.user().getUsername());
        tabView.refresh();
        qaService().runQAStep(step, session.user());
        //view.resetChanges();
    }

    public void export(QAStep qaStep, QAStepResult stepResult, String dirName, String fileName, boolean overide, String rowFilter) throws EmfException {
        lastFolder = dirName;

        if (stepResult == null || stepResult.getTable() == null)
            throw new EmfException("You must have run the QA step successfully before exporting ");

        qaStep.setOutputFolder(dirName);
        qaService().updateWitoutCheckingConstraints(new QAStep[] { qaStep });
        qaService().exportQAStep(qaStep, session.user(), dirName, fileName, overide, rowFilter);
    }
    
    public void download(QAStep qaStep, QAStepResult stepResult, String fileName, boolean overwrite, String rowFilter) throws EmfException {
        if (stepResult == null || stepResult.getTable() == null)
            throw new EmfException("You must run the QA step successfully before exporting ");

        qaStep.setOutputFolder(session.userService().getPropertyValue(EmfProperty.DOWNLOAD_EXPORT_FOLDER) + "/" + session.user().getUsername());
        qaService().updateWitoutCheckingConstraints(new QAStep[] { qaStep });
        qaService().downloadQAStep(qaStep, session.user(), fileName, overwrite, rowFilter);
    }
    
    public boolean getSameAsTemplate(QAStep qaStep) throws EmfException{       
        return qaService().getSameAsTemplate(qaStep); 
    }

    public void exportToShapeFile(QAStep qaStep, QAStepResult stepResult, 
            String dirName, String fileName, 
            boolean overide, ProjectionShapeFile projectionShapeFile, 
            String rowFilter, PivotConfiguration pivotConfiguration) throws EmfException {
        lastFolder = dirName;

        if (stepResult == null || stepResult.getTable() == null)
            throw new EmfException("You must have run the QA step successfully before exporting ");

        qaStep.setOutputFolder(dirName);
        session.qaService().updateWitoutCheckingConstraints(new QAStep[] { qaStep });
//        ProjectionShapeFile projectionShapeFile = session.qaService().getProjectionShapeFiles()[1];
        session.qaService().exportShapeFileQAStep(qaStep, session.user(), dirName, fileName, overide, projectionShapeFile, rowFilter, pivotConfiguration);
    }

    public void downloadToShapeFile(QAStep qaStep, QAStepResult stepResult, 
            String fileName, ProjectionShapeFile projectionShapeFile, 
            String rowFilter, PivotConfiguration pivotConfiguration, boolean overwrite) throws EmfException {

        if (stepResult == null || stepResult.getTable() == null)
            throw new EmfException("You must have run the QA step successfully before exporting ");

        qaStep.setOutputFolder(session.userService().getPropertyValue(EmfProperty.DOWNLOAD_EXPORT_FOLDER) + "/" + session.user().getUsername());
        session.qaService().updateWitoutCheckingConstraints(new QAStep[] { qaStep });
//        ProjectionShapeFile projectionShapeFile = session.qaService().getProjectionShapeFiles()[1];
        session.qaService().downloadShapeFileQAStep(qaStep, session.user(), fileName, projectionShapeFile, rowFilter, pivotConfiguration, overwrite);
    }

    private String getFolder() {
        return (lastFolder != null) ? lastFolder : getDefaultFolder();
    }

    private String getDefaultFolder() {
        // Added code here to fix the Null Pointer exception that occurs at first launch of Edit Window
        // because qastep is null.
        String folder;
        if (qaStep != null) {
            folder = qaStep.getOutputFolder();
        } else {
            folder = session.preferences().outputFolder();
        }

        return folder;
    }

    public String userName() {
        return session.user().getUsername();
    }

    public void viewResults(QAStepResult stepResult, long viewCount, String exportDir) throws EmfException {

        File localFile = new File(tempQAStepFilePath(exportDir, stepResult));
        try {
//            if (!localFile.exists() || localFile.lastModified() != qaResult.getTableCreationDate().getTime()) {
                Writer output = new BufferedWriter(new FileWriter(localFile));
                try {
                    output.write( writeHeader(qaStep, stepResult, dataset.getName()));
                    
                    //long tableRecordCount = getTableRecordCount(qaResult);
                    long recordOffset = 0;
                    long count = 0;
                    if (viewCount <= 10000L)
                        output.write( getTableAsString(stepResult, viewCount, recordOffset) );
                    else {
                        while (recordOffset < viewCount) {
                            count = viewCount - recordOffset > 10000L? 10000L: (viewCount - recordOffset);
                            output.write( getTableAsString(stepResult, count, recordOffset) );
                            recordOffset += 10000;
                        }
                    }            
//                    output.write( getTableAsString(qaResult) );
//                } catch (Exception e)  {
//                    e.printStackTrace();
//                    throw e;
                } finally {
                    output.close();
                    localFile.setLastModified(stepResult.getTableCreationDate().getTime());
                }
//            }
        } catch (Exception e) {
            e.printStackTrace(); // BUG3588
            throw new EmfException(e.getMessage());
        }
        
        view.displayResultsTable(qaStep.getName(), localFile.getAbsolutePath());
    }

    private String tempQAStepFilePath(String exportDir, QAStepResult qaStepResult) throws EmfException {
        String separator = File.separator; 
        UserPreference preferences = new DefaultUserPreferences();
        String tempDir = preferences.localTempDir();
        
        if (tempDir == null || tempDir.isEmpty())
            tempDir = System.getProperty("java.io.tmpdir");

        File tempDirFile = new File(tempDir);

        if (!(tempDirFile.exists() && tempDirFile.isDirectory() && tempDirFile.canWrite() && tempDirFile.canRead()))
            throw new EmfException("Import-export temporary folder does not exist or lacks write permissions: "
                    + tempDir);


        return tempDir + separator + qaStepResult.getTable() + ".csv"; // this is how exported file name was
    }
    
    private String writeHeader(QAStep qaStep, QAStepResult stepResult, String dsName){
        String lineFeeder = System.getProperty("line.separator");
        String header="#DATASET_NAME=" + dsName + lineFeeder;
        header +="#DATASET_VERSION_NUM= " + qaStep.getVersion() + lineFeeder;
        header +="#CREATION_DATE=" + CustomDateFormat.format_YYYY_MM_DD_HH_MM(stepResult.getTableCreationDate())+ lineFeeder;
        header +="#QA_STEP_NAME=" + qaStep.getName() + lineFeeder; 
        header +="#QA_PROGRAM=" + qaStep.getProgram()+ lineFeeder;
        String arguments= qaStep.getProgramArguments();
        StringTokenizer argumentTokenizer = new StringTokenizer(arguments);
        header += "#ARGUMENTS=" + lineFeeder ;
        header += "#" + argumentTokenizer.nextToken(); // get first token

        while (argumentTokenizer.hasMoreTokens()){
            String next = argumentTokenizer.nextToken().trim(); 
            if (next.contains("-"))
                header += lineFeeder+ "#" +next;
            else 
                header += "  " +next;
        }
        header +=lineFeeder;
        //arguments.replaceAll(lineFeeder, "#");
        //System.out.println("after replace  \n" + header);
        return header;
    }
    
//    private String exportedQAStepFilePath(String exportDir, QAStepResult qaStepResult) {
//        String separator = (exportDir.charAt(0) == '/') ? "/" : "\\";
//        return exportDir + separator + qaStepResult.getTable() + ".csv"; // this is how exported file name was
//    }

    public QAStepResult getStepResult(QAStep step) throws EmfException {
        return session.qaService().getQAStepResult(step);
    }
    
    public boolean checkBizzareCharInColumn(QAStep step, String colName) throws EmfException {
        return session.dataService().checkBizzareCharInColumn(step.getDatasetId(), step.getVersion(), colName);
    }

    public EmfDataset getDataset(String datasetName ) throws EmfException {
        return session.dataService().getDataset(datasetName);
    }

    public EmfDataset getDataset(int id) throws EmfException {
        return session.dataService().getDataset(id);
    }

    public String getTableAsString(QAStepResult stepResult) throws EmfException {
        return session.dataService().getTableAsString("emissions." + stepResult.getTable());
    }

    public String getTableAsString(QAStepResult stepResult, long recordLimit, long recordOffset) throws EmfException {
        return session.dataService().getTableAsString("emissions." + stepResult.getTable(), recordLimit, recordOffset);
    }

    public long getTableRecordCount(QAStepResult stepResult) throws EmfException {
        return session.dataService().getTableRecordCount("emissions." + stepResult.getTable());
    }
    
    public ProjectionShapeFile[] getProjectionShapeFiles() throws EmfException {
        return session.qaService().getProjectionShapeFiles();       
    }
    
    public Pollutant[] getPollutants() throws EmfException {
        return session.dataCommonsService().getPollutants();       
    }
    
    public Version version(int datasetId, int version) throws EmfException {
        try {
            return session.dataViewService().getVersion(datasetId, version);
        } finally {
            //
        }
    }
    
    public boolean isShapeFileCapable(QAStepResult stepResult) throws EmfException {
        return session.qaService().isShapefileCapable(stepResult);
    }
    

    public boolean ignoreShapeFileFunctionality() throws EmfException {
        try {
            String value = session.userService().getPropertyValue("IGNORE_SHAPEFILE_FUNCTIONALITY");
//            return (value != null || value.equalsIgnoreCase("true") ? true : false);
            return (value != null && value.equalsIgnoreCase("true") ? true : false);
        } finally {
            //
        }
    }
}
