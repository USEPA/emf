package gov.epa.emissions.framework.client.casemanagement;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.StringTokenizer;
import java.util.UUID;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.casemanagement.editor.CaseEditorPresenter;
import gov.epa.emissions.framework.client.casemanagement.editor.CaseEditorPresenterImpl;
import gov.epa.emissions.framework.client.casemanagement.editor.CaseEditorView;
import gov.epa.emissions.framework.client.casemanagement.editor.CaseViewerPresenter;
import gov.epa.emissions.framework.client.casemanagement.editor.CaseViewerPresenterImpl;
import gov.epa.emissions.framework.client.casemanagement.editor.CaseViewerView;
import gov.epa.emissions.framework.client.casemanagement.sensitivity.SensitivityPresenter;
import gov.epa.emissions.framework.client.casemanagement.sensitivity.SensitivityView;
import gov.epa.emissions.framework.client.preference.DefaultUserPreferences;
import gov.epa.emissions.framework.client.preference.UserPreference;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseCategory;
import gov.epa.emissions.framework.services.casemanagement.CaseService;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.data.QAStepResult;

public class CaseManagerPresenterImpl implements CaseManagerPresenter {

    private CaseManagerView view;

    private EmfSession session;
    
    private CaseObjectManager caseObjectManager = null;

    public CaseManagerPresenterImpl(EmfSession session, CaseManagerView view) {
        this.session = session;
        this.view = view;
        this.caseObjectManager = CaseObjectManager.getCaseObjectManager(session);
    }

    public void display() {
        view.observe(this);
        view.display();
    }

    private CaseService service() {
        return session.caseService();
    }

    public void doRemove(Case caseObj) throws EmfException {
        service().removeCase(caseObj);
    }

    public void doRefresh() throws EmfException{
        //view.refresh(service().getCases());
        view.refreshWithLastCategory();
    }

    public void doClose() {
        view.disposeView();
    }

    public void doNew(NewCaseView view) {
        NewCasePresenter presenter = new NewCasePresenter(session, view, this);
        presenter.doDisplay();
    }
    
    public void doSensitivity(SensitivityView view, Case case1) {
        SensitivityPresenter presenter = new SensitivityPresenter(session, view, this);
        presenter.doDisplay(case1, this);
    }
    
    public void addNewCaseToTableData(Case newCase) {
        view.addNewCaseToTableData(newCase);
    }

//    public Case[] getCases(int[] caseIds) throws EmfException {
//        return service().getCases();
//    }
////    
////    private boolean isDuplicate(Case newCase) throws EmfException {
//        Case[] cases = service().getCases();
//        for (int i = 0; i < cases.length; i++) {
//            if (cases[i].getName().equals(newCase.getName()))
//                return true;
//        }
//
//        return false;
//    }

    public void doEdit(CaseEditorView caseView, Case caseObj) throws EmfException {
        CaseEditorPresenter presenter = new CaseEditorPresenterImpl(caseObj, session, caseView, this);
        displayEditor(presenter);
    }
    
    public void doView(CaseViewerView caseView, Case caseObj) throws EmfException {
        CaseViewerPresenter presenter = new CaseViewerPresenterImpl(caseObj, session, caseView, this);
        presenter.doDisplay();
    }

    void displayEditor(CaseEditorPresenter presenter) throws EmfException {
        presenter.doDisplay();
    }

    public void doCopyCases(int[] caseIds) throws EmfException {
        startCopyMessage(caseIds.length > 1 ? "s" : "");
        service().copyCaseObject(caseIds, session.user());
    }
    
    private void startCopyMessage(String pad) {
        String message = "Started copying case" + pad + ".  See Status window below.";
        view.setMessage(message);
    }

    public CaseCategory[] getCategories() throws EmfException {
        return service().getCaseCategories();
    }

    public Case[] getCases(CaseCategory category ) throws EmfException {
        this.caseObjectManager.refresh();
        this.caseObjectManager.refreshJobList();
        
        if (category == null)
            return new Case[0];
        
        if (category.getName().equals("All"))
            return service().getCases();
        
        return service().getCases(category);
    }
    
    public Case[] getCases(CaseCategory category, String nameContains) throws EmfException {
        this.caseObjectManager.refresh();
        this.caseObjectManager.refreshJobList();
        
        if (category == null){
            view.setSelectedCategory();
            return service().getCases(nameContains);
        }
        if (category.getName().equals("All"))
            return service().getCases(nameContains);
        
        return service().getCases(category, nameContains);
        //eturn cases==null?  new Case[0] :cases;
    }

    public void refreshWithLastCategory() throws EmfException {
        view.refreshWithLastCategory();
    }

    public String checkParentCase(Case caseObj) throws EmfException {
        return service().checkParentCase(caseObj);
    }

    public CaseCategory getSelectedCategory() {
        return view.getSelectedCategory();
    }

    public void viewCaseComparisonResult(int[] caseIds, String exportDir) throws EmfException {
        if (caseIds == null || caseIds.length == 0)
            throw new EmfException("No cases to compare.");
        
        File localFile = new File(tempQAStepFilePath());
        try {
            if (!localFile.exists()) {
                Writer output = new BufferedWriter(new FileWriter(localFile));
                try {
                    output.write(  
//                            writerHeader(qaStep, qaResult, dataset.getName())
                            ""+ getCaseComparisonResult(caseIds) 
                            );
                }
                finally {
                    output.close();
                }
            }
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        }
        
        view.displayCaseComparisonResult("Case Comparison", localFile.getAbsolutePath());
    }
    
    public String viewCaseQaReports(User user, int[] caseIds, String gridName, Sector[] sectors, 
            String[] repDims, String whereClause, String serverDir) throws EmfException {
        String message = "";
        if (caseIds == null || caseIds.length == 0)
            throw new EmfException("No cases to compare.");
          
        //File localFile = new File(tempQAStepFilePath());
        try {
            //if (!localFile.exists()) {
            //Writer output = new BufferedWriter(new FileWriter(localFile));
            try {
                message = runCaseQaReports(user, caseIds, gridName, sectors, 
                        repDims, whereClause, serverDir);   
                //output.write(  ""+ reportsInfo[0] );

//                message = reportsInfo[1];
            }catch (Exception e) {
                //e.printStackTrace();
                throw new EmfException( e.getMessage());
            }finally {
                //output.close();
            }
            //}
        } catch (Exception e) {
            e.printStackTrace();
            throw new EmfException(e.getMessage());
        }
        
        //view.displayCaseComparisonResult("Summary Case Report", localFile.getAbsolutePath());
        return message;

//        view.displayCaseComparisonResult("Summary Case Output", localFile.getAbsolutePath());
//        System.out.println(reportsInfo[1]);
//        return reportsInfo[1];
    }
    
    private String getCaseComparisonResult(int[] caseIds) throws EmfException {
        return service().getCaseComparisonResult(caseIds);
    }
    
    private String runCaseQaReports(User user, int[] caseIds, String gridName, Sector[] sectors, 
            String[] repDims, String whereClause, String serverDir) throws EmfException {
        return service().getCaseQaReports(user, caseIds, gridName, sectors, repDims, 
                whereClause, serverDir);
    }

    private String tempQAStepFilePath() throws EmfException {
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


        return tempDir + separator + (UUID.randomUUID()).toString().replaceAll("-", "") + ".csv"; // this is how exported file name was
    }

    public void doQA(int[] ids, CompareCaseView view) throws EmfException {
        CompareCasePresenter presenter = new CompareCasePresenter(session, ids, view, this);
        presenter.doDisplay();       
    }

//    private String writerHeader(QAStep qaStep, QAStepResult stepResult, String dsName){
//        String lineFeeder = System.getProperty("line.separator");
//        String header="#DATASET_NAME=" + dsName + lineFeeder;
//        header +="#DATASET_VERSION_NUM= " + qaStep.getVersion() + lineFeeder;
//        header +="#CREATION_DATE=" + CustomDateFormat.format_YYYY_MM_DD_HH_MM(stepResult.getTableCreationDate())+ lineFeeder;
//        header +="#QA_STEP_NAME=" + qaStep.getName() + lineFeeder; 
//        header +="#QA_PROGRAM=" + qaStep.getProgram()+ lineFeeder;
//        String arguments= qaStep.getProgramArguments();
//        StringTokenizer argumentTokenizer = new StringTokenizer(arguments);
//        header += "#ARGUMENTS=" +argumentTokenizer.nextToken(); // get first token
//
//        while (argumentTokenizer.hasMoreTokens()){
//            String next = argumentTokenizer.nextToken().trim(); 
//            if (next.contains("-"))
//                header += lineFeeder+ "#" +next;
//            else 
//                header += "  " +next;
//        }
//        header +=lineFeeder;
//        //arguments.replaceAll(lineFeeder, "#");
//        //System.out.println("after replace  \n" + header);
//        return header;
//    }
}
