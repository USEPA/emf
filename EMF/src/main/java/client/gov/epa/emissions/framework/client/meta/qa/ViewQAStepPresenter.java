package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.data.PivotConfiguration;
import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.commons.data.ProjectionShapeFile;
import gov.epa.emissions.commons.data.QAProgram;
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
import java.util.StringTokenizer;

public class ViewQAStepPresenter {

    private QAStepView view;

    private EmfDataset dataset;

    private EmfSession session;

    private static String lastFolder = null;

    public ViewQAStepPresenter(QAStepView view, EmfDataset dataset, EmfSession session) {
        this.view = view;
        this.dataset = dataset;
        this.session = session;
    }

    public void display(QAStep step, String versionName) throws EmfException {
        QAProgram[] programs = qaService().getQAPrograms();
        QAStepResult result = qaService().getQAStepResult(step);
        Boolean sameAstemplate = getSameAsTemplate(step);
        view.display(step, result, programs, dataset, session.user(), versionName, sameAstemplate);
    }

    public boolean getSameAsTemplate(QAStep qaStep) throws EmfException{       
        return qaService().getSameAsTemplate(qaStep); 
    }
    
    private QAService qaService(){
        return session.qaService();
    }
    public void doClose() {
        view.disposeView();
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

    public void doExport(QAStep qaStep, QAStepResult stepResult, String dirName, String fileName, boolean overide, String rowFilter) throws EmfException {
        export(qaStep, stepResult, dirName, fileName, overide, rowFilter);
    }

    public void export(QAStep qaStep, QAStepResult stepResult, String dirName, String fileName, boolean overide, String rowFilter) throws EmfException {
        if (stepResult == null || stepResult.getTable() == null)
            throw new EmfException("You have to run the QA step successfully before exporting ");

        qaStep.setOutputFolder(dirName);
        session.qaService().updateWitoutCheckingConstraints(new QAStep[] { qaStep });
        session.qaService().exportQAStep(qaStep, session.user(), dirName, fileName, overide, rowFilter);

    }

    public void download(QAStep qaStep, QAStepResult stepResult, String fileName, boolean overwrite, String rowFilter) throws EmfException {
        if (stepResult == null || stepResult.getTable() == null)
            throw new EmfException("You must run the QA step successfully before exporting ");

        qaStep.setOutputFolder(session.userService().getPropertyValue(EmfProperty.DOWNLOAD_EXPORT_FOLDER) + "/" + session.user().getUsername());
        qaService().updateWitoutCheckingConstraints(new QAStep[] { qaStep });
        qaService().downloadQAStep(qaStep, session.user(), fileName, overwrite, rowFilter);
    }
    

    public String userName() {
        return session.user().getUsername();
    }

    public void viewResults(QAStep qaStep, QAStepResult qaResult, long viewCount) throws EmfException {
//        QAStepResult qaResult = getStepResult(qaStep);
        
        if (qaResult == null || qaResult.getTable() == null || qaResult.getTable().isEmpty())
            throw new EmfException("No QA Step result available to view.");
        
        File localFile = new File(tempQAStepFilePath(qaResult));
        try {
//            if (!localFile.exists() || localFile.lastModified() != qaResult.getTableCreationDate().getTime()) {
                Writer output = new BufferedWriter(new FileWriter(localFile));
                try {
                    output.write(  writerHeader(qaStep, qaResult, dataset.getName()) );
                    long recordOffset = 0;
                    long count = 0;
                    if (viewCount < 10000L)
                        output.write( getTableAsString(qaResult, viewCount, recordOffset) );
                    else {
                        while (recordOffset < viewCount) {
                            count = viewCount - recordOffset > 10000L? 10000L: (viewCount - recordOffset);
                            output.write( getTableAsString(qaResult, count, recordOffset) );
                            recordOffset += 10000;
                        }
                    }          
                }
                finally {
                    output.close();
                    localFile.setLastModified(qaResult.getTableCreationDate().getTime());
                }
//            }
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        }
        
        view.displayResultsTable(qaStep.getName(), localFile.getAbsolutePath());
    }
    
    private String tempQAStepFilePath(QAStepResult qaStepResult) throws EmfException {
        String separator = File.separator; 
        UserPreference preferences = new DefaultUserPreferences();
        String tempDir = preferences.localTempDir();
//        String separator = exportDir.length() > 0 ? (exportDir.charAt(0) == '/') ? "/" : "\\" : "\\";
//        String tempDir = System.getProperty("IMPORT_EXPORT_TEMP_DIR"); 

        if (tempDir == null || tempDir.isEmpty())
            tempDir = System.getProperty("java.io.tmpdir");

        File tempDirFile = new File(tempDir);

        if (!(tempDirFile.exists() && tempDirFile.isDirectory() && tempDirFile.canWrite() && tempDirFile.canRead()))
            throw new EmfException("Import-export temporary folder does not exist or lacks write permissions: "
                    + tempDir);


        return tempDir + separator + qaStepResult.getTable() + ".csv"; // this is how exported file name was
    }
    
//    private String exportedQAStepFilePath(String exportDir, QAStepResult qaStepResult) {
//        String separator = (exportDir.charAt(0) == '/') ? "/" : "\\";
//        //System.
//        return exportDir + separator + qaStepResult.getTable() + ".csv"; // this is how exported file name was created
//    }

    private String writerHeader(QAStep qaStep, QAStepResult stepResult, String dsName){
        String lineFeeder = System.getProperty("line.separator");
        String header="#DATASET_NAME=" + dsName + lineFeeder;
        header +="#DATASET_VERSION_NUM= " + qaStep.getVersion() + lineFeeder;
        header +="#CREATION_DATE=" + CustomDateFormat.format_YYYY_MM_DD_HH_MM(stepResult.getTableCreationDate())+ lineFeeder;
        header +="#QA_STEP_NAME=" + qaStep.getName() + lineFeeder; 
        header +="#QA_PROGRAM=" + qaStep.getProgram()+ lineFeeder;
        String arguments= qaStep.getProgramArguments();
        StringTokenizer argumentTokenizer = new StringTokenizer(arguments);
        header += "#ARGUMENTS=" +argumentTokenizer.nextToken(); // get first token

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
    public QAStepResult getStepResult(QAStep step) throws EmfException {
        return session.qaService().getQAStepResult(step);
    }

    public String getTableAsString(QAStepResult stepResult, long recordLimit, long recordOffset) throws EmfException {
        return session.dataService().getTableAsString("emissions." + stepResult.getTable(), recordLimit, recordOffset);
    }

    public long getTableRecordCount(QAStepResult stepResult) throws EmfException {
        return session.dataService().getTableRecordCount("emissions." + stepResult.getTable());
    }
    
    public boolean checkBizzareCharInColumn(QAStep step, String colName) throws EmfException {
        return session.dataService().checkBizzareCharInColumn(step.getDatasetId(), step.getVersion(), colName);
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

    public ProjectionShapeFile[] getProjectionShapeFiles() throws EmfException {
        return session.qaService().getProjectionShapeFiles();       
    }
    
    public Pollutant[] getPollutants() throws EmfException {
        return session.dataCommonsService().getPollutants();       
    }
    
}
