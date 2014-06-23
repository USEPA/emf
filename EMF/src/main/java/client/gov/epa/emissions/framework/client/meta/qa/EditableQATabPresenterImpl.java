package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.preference.DefaultUserPreferences;
import gov.epa.emissions.framework.client.preference.UserPreference;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.data.QAStepResult;
import gov.epa.emissions.framework.services.editor.DataEditorService;
import gov.epa.emissions.framework.services.qa.QAService;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.Date;
import java.util.StringTokenizer;

public class EditableQATabPresenterImpl implements EditableQATabPresenter {

    private EditableQATabView view;

    private EmfDataset dataset;

    private EmfSession session;

    public EditableQATabPresenterImpl(EmfDataset dataset, EmfSession session, EditableQATabView view) {
        this.dataset = dataset;
        this.session = session;
        this.view = view;
        view.observe(this);
    }

    public void display() throws EmfException {
        QAStep[] steps = qaService().getQASteps(dataset);
        QAStepResult[] qaStepResults =qaService().getQAStepResults(dataset);
        view.display(dataset, steps, qaStepResults,  versions());
    }

    private Version[] versions() throws EmfException {
        DataEditorService service = session.dataEditorService();
        return service.getVersions(dataset.getId());
    }

    private QAService qaService() {
        return session.qaService();
    }

    public void doSave() {
        // DO NOTHING
    }
    public void doAddUsingTemplate(NewQAStepView stepView) {
        DatasetType type = dataset.getDatasetType();
        if (type.getQaStepTemplates().length == 0) {
            view.informLackOfTemplatesForAddingNewSteps(type);
            return;
        }

        stepView.display(dataset, type);
        if (stepView.shouldCreate()) {
            view.addFromTemplate(stepView.steps());
        }
    }

    public void doAddCustomized(NewCustomQAStepView stepView) throws EmfException {
        NewCustomQAStepPresenter presenter = new NewCustomQAStepPresenter(stepView, dataset, versions(), view, session);
        doAddCustomized(stepView, presenter);
    }

    void doAddCustomized(NewCustomQAStepView stepView, NewCustomQAStepPresenter presenter) throws EmfException {
        stepView.observe(presenter);
        presenter.display();
    }

    public void doSetStatus(SetQAStatusView statusView, QAStep[] steps) {
        SetQAStatusPresenter presenter = new SetQAStatusPresenter(statusView, steps, view, session);
        presenter.display();
    }

    public synchronized void runStatus(QAStep step) throws EmfException {
        step.setStatus("In Progress");
        step.setDate(new Date());
        step.setWho(session.user().getUsername());
        session.qaService().runQAStep(step, session.user());
        //QAStepResult result = session.qaService().getQAStepResult(step);
        //view.refresh(step, result);
    }

    public void doEdit(QAStep step, EditQAStepView performView, String versionName) throws EmfException {
        EditQAStepPresenter presenter = new EditQAStepPresenter(performView, dataset, view, session);
        presenter.display(step, versionName);
    }

    public QAStep[] addFromTemplates(QAStep[] newSteps) throws EmfException {
        return session.qaService().updateWitoutCheckingConstraints(newSteps);
    }
    
    public EmfSession getSession(){
        return session; 
    }

    public void doCopyQASteps(QAStep[] steps, int[] datasetIds, boolean replace)
            throws EmfException {
        session.qaService().copyQAStepsToDatasets(session.user(), steps, datasetIds, replace);
    }

    public void checkIfLockedByCurrentUser() throws EmfException{
        EmfDataset reloaded = session.dataService().getDataset(dataset.getId());
        if (!reloaded.isLocked())
            throw new EmfException("Lock on current dataset object expired. " );  
        if (!reloaded.isLocked(session.user()))
            throw new EmfException("Lock on current dataset object expired. User " + reloaded.getLockOwner()
                    + " has it now.");    
    }

    public QAStepResult getStepResult(QAStep step) throws EmfException {
        return session.qaService().getQAStepResult(step);
    }

    public long getTableRecordCount(QAStepResult stepResult) throws EmfException {
        return session.dataService().getTableRecordCount("emissions." + stepResult.getTable());
    }

    public void viewResults( QAStep qaStep, long viewCount ) throws EmfException {
        QAStepResult qaResult = getStepResult(qaStep);
        
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
                    if (viewCount <= 10000L)
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

        if (tempDir == null || tempDir.isEmpty()) {
            tempDir = System.getProperty("java.io.tmpdir");
        }

        File tempDirFile = new File(tempDir);

        if (!tempDirFile.exists() || !tempDirFile.isDirectory() || !tempDirFile.canWrite() || !tempDirFile.canRead()) {
            throw new EmfException("Import-export temporary folder does not exist or lacks write permissions: "
                    + tempDir);
        }

        return tempDir + separator + qaStepResult.getTable() + ".csv"; // TODO: 2011-02
    }
    
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

    public String getTableAsString(QAStepResult stepResult) throws EmfException {
        return session.dataService().getTableAsString("emissions." + stepResult.getTable());
    }

    public String getTableAsString(QAStepResult stepResult, long recordLimit, long recordOffset) throws EmfException {
        return session.dataService().getTableAsString("emissions." + stepResult.getTable(), recordLimit, recordOffset);
    }

    public void doDelete(QAStep[] steps) throws EmfException { //BUG3615
        // NOTE Auto-generated method stub
        session.qaService().deleteQASteps(session.user(), steps, dataset.getId());
    }
    
    public boolean checkBizzareCharInColumn(QAStep step, String colName) throws EmfException {
        return session.dataService().checkBizzareCharInColumn(step.getDatasetId(), step.getVersion(), colName);
    }

}
