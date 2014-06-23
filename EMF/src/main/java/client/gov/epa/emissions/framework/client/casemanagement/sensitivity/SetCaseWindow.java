package gov.epa.emissions.framework.client.casemanagement.sensitivity;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.buttons.OKButton;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.casemanagement.CaseManagerPresenter;
import gov.epa.emissions.framework.client.casemanagement.editor.CaseEditor;
import gov.epa.emissions.framework.client.casemanagement.inputs.SetInputFieldsPanel;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseInput;
import gov.epa.emissions.framework.services.casemanagement.parameters.CaseParameter;
import gov.epa.emissions.framework.ui.InfoDialog;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;
import gov.epa.emissions.framework.ui.YesNoDialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

public class SetCaseWindow extends DisposableInteralFrame implements SetCaseView {

    private JPanel layout;
    
    private JPanel mainPanel;
    
    private EmfConsole parentConsole;

    private SetCasePresenter presenter;
    
    private CaseManagerPresenter managerPresenter;

    private MessagePanel messagePanel;
    
    private List<SetCaseObject> setCaseObjects; 
   
    private Case caseObj;

    private Button prevButton, nexButton, editButton;

    private SetCaseObject  currentObject; 
    
    private int currentIndex=0; 
    
    private SetInputFieldsPanel setInputFieldsPanel;
    
    private SetCaseFoldersPanel setCaseFoldersPanel; 
    
    private SetCaseParameterPanel setCaseParameterPanel;
    
    private List<CaseInput> existingInputs; 
    
    private List<CaseParameter> existingParas; 
    
    public SetCaseWindow(String title, EmfConsole parentConsole, 
            DesktopManager desktopManager, List<CaseInput> existingInputs, List<CaseParameter> existingParas) {
        super(title, new Dimension(520, 450), desktopManager);
        this.parentConsole = parentConsole;
        this.existingInputs = existingInputs;
        this.existingParas = existingParas;
    }

    public void display(Case caseObj) throws EmfException {
        this.caseObj = caseObj;
        layout = createLayout();
        super.getContentPane().add(layout);
        super.display();
        super.resetChanges();
    }
    
    public void observe(SetCasePresenter presenter, CaseManagerPresenter managerPresenter) {
        this.presenter =presenter; 
        this.managerPresenter = managerPresenter;

    }

    private JPanel createLayout() throws EmfException {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        messagePanel = new SingleLineMessagePanel();
        panel.add(messagePanel);
        
        setupObjects(); 
        panel.add(mainPanel());
        panel.add(buttonsPanel());

        return panel;
    }
    
    private void setupObjects() throws EmfException {
        // add parameters to setCaseObjects
        setCaseObjects = new ArrayList<SetCaseObject>();
        SetCaseObject folderObj = new SetCaseObject(caseObj.getName(), SetCaseObject.WIZARD_PATH);
        setCaseObjects.add(folderObj);
//        System.out.println(existingInputs==null? "inputs is null, ": "existing input " + existingInputs.size()); 
//        System.out.println(existingParas==null? "parameterss is null," : "existing para " + existingParas.size());
       
        
        CaseInput[] inputList = presenter.getCaseInput(caseObj.getId(), new Sector("All", "All"), false);
        if (existingInputs == null || existingInputs.size() == 0){
            for (CaseInput input :inputList){
                SetCaseObject obj = new SetCaseObject(input, SetCaseObject.WIZARD_INPUT);
                setCaseObjects.add(obj);
            }
        }else { // only edit new inputs
            for (CaseInput input :inputList){
                if (!existingInputs.contains(input)){
                    SetCaseObject obj = new SetCaseObject(input, SetCaseObject.WIZARD_INPUT);
                    setCaseObjects.add(obj);
                }
            }
        }
        
        CaseParameter[] paraList = presenter.getCaseParameters(caseObj.getId(), new Sector("All", "All"), false);
        if (existingParas == null || existingParas.size() == 0 ){
            for (CaseParameter par :paraList){
                SetCaseObject obj = new SetCaseObject(par, SetCaseObject.WIZARD_PARA);
                setCaseObjects.add(obj);
            }
        }else{  // only edit new parameters
            for (CaseParameter par :paraList){
                if (!existingParas.contains(par)){
                    SetCaseObject obj = new SetCaseObject(par, SetCaseObject.WIZARD_PARA);
                    setCaseObjects.add(obj);
                }
            }
        }
        
        if (setCaseObjects.size()==0)
            throw new EmfException("No input or parameter to edit");  
    }

    
    private JPanel mainPanel() throws EmfException{
        mainPanel = new JPanel(new BorderLayout());
        //get first setCaseObjects
        currentObject = setCaseObjects.get(currentIndex);
        if (currentObject.getWizardType().equalsIgnoreCase(SetCaseObject.WIZARD_PATH))
            mainPanel.add(displayFolders());
        if (currentObject.getWizardType().equalsIgnoreCase(SetCaseObject.WIZARD_INPUT))
            mainPanel.add(displayInput((CaseInput)currentObject.getObject()));
        else if (currentObject.getWizardType().equalsIgnoreCase(SetCaseObject.WIZARD_PARA))
            mainPanel.add(displayParam((CaseParameter)currentObject.getObject()));
        return mainPanel;
    }
    
    private JPanel displayFolders(){
        JPanel panel = new JPanel(); 
        this.setCaseFoldersPanel = new SetCaseFoldersPanel(caseObj, messagePanel, this, parentConsole);
        setCaseFoldersPanel.display(panel, presenter.getSession());
        return panel; 
    }
    
    private JPanel displayParam(CaseParameter param) throws EmfException{
        JPanel panel = new JPanel(); 
        this.setCaseParameterPanel = new SetCaseParameterPanel(messagePanel, this, parentConsole);
        setCaseParameterPanel.display(param, panel, presenter.getSession(), presenter.getJobName(param.getJobId()));
        return panel; 
    }
    
    private JPanel displayInput(CaseInput input) throws EmfException {
        JPanel panel = new JPanel();
        this.setInputFieldsPanel = new SetInputFieldsPanel(messagePanel, this, parentConsole, desktopManager);
        presenter.doAddInputFields(input, panel, setInputFieldsPanel);
        return panel;
    }

    
    private void panelRefresh() throws EmfException {
        mainPanel.removeAll();
        if (currentObject.getWizardType().equalsIgnoreCase(SetCaseObject.WIZARD_PATH))
            mainPanel.add(displayFolders());
        if (currentObject.getWizardType().equalsIgnoreCase(SetCaseObject.WIZARD_INPUT))
            mainPanel.add(displayInput((CaseInput)currentObject.getObject()));
        else if (currentObject.getWizardType().equalsIgnoreCase(SetCaseObject.WIZARD_PARA))
            mainPanel.add(displayParam((CaseParameter)currentObject.getObject()));
        super.validate();
    }
    
    private JPanel buttonsPanel() {
        JPanel panel = new JPanel();

        JPanel container = new JPanel();
        FlowLayout layout = new FlowLayout();
        layout.setHgap(100);
        layout.setVgap(10);
        container.setLayout(layout);
        
        prevButton = new Button("Prev", prevsAction());
        container.add(prevButton);
        editButton = new OKButton("Edit Case", editAction());
        //getRootPane().setDefaultButton(editButton);
        container.add(editButton);
        nexButton = new Button("Next", nextAction());
        container.add(nexButton);
        getRootPane().setDefaultButton(nexButton);
        
        resetButtons();
        panel.add(container, BorderLayout.CENTER);
        return panel;
    }
    
    private Action editAction() {
        Action action = new AbstractAction() {

            public void actionPerformed(ActionEvent event) {
                clearMessage();
                if (!shouldEditCase())
                    return; 
                try {
                    if (hasChanges()){
                        doSave();
                        resetChanges();
                    }
                    CheckNoLocalDialog dialog = new CheckNoLocalDialog(parentConsole, presenter, caseObj);
                    String validationMsg = dialog.validateValues();
                    if (dialog.getHasValues()){
                        dialog.display(validationMsg);
                    }
                    CaseEditor view = new CaseEditor(parentConsole, presenter.getSession(), desktopManager);
                    managerPresenter.doEdit(view, caseObj);
                    disposeView();
                } catch (EmfException e) {
                    e.printStackTrace();
                    messagePanel.setError(e.getMessage());
                }
            }
        };
        return action;
    }
    
    

    private Action prevsAction() {
        Action action = new AbstractAction() {

            public void actionPerformed(ActionEvent event) {
                clearMessage();
                if (hasChanges()){
                    doSave();
                    resetChanges();
                }
                if (setCaseObjects.get(currentIndex-1) != null){
                    currentObject = setCaseObjects.get(currentIndex-1);
                    currentIndex--;
                    resetButtons(); 
                    try {
                        panelRefresh();
                    } catch (EmfException e) {
                        messagePanel.setMessage(e.getMessage());
                    } 
                }
            }
        };
        return action;
    }
    
    private Action nextAction() {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                clearMessage();
                if (hasChanges()){
                    doSave();
                    resetChanges();
                }
                if (setCaseObjects.get(currentIndex+1) != null){
                    currentObject = setCaseObjects.get(currentIndex+1);
                    currentIndex++;
                    resetButtons(); 
                    try {
                        panelRefresh();
                    } catch (EmfException e) {
                        messagePanel.setMessage(e.getMessage());
                    } 
                }
            }
        };
        return action;
    }
    
    private void resetButtons(){
        prevButton.setEnabled(true);
        nexButton.setEnabled(true);
        getRootPane().setDefaultButton(nexButton);
        if (currentIndex == 0 )
            prevButton.setEnabled(false);
        if (setCaseObjects.size() == currentIndex+1){
            nexButton.setEnabled(false);
            getRootPane().setDefaultButton(editButton);
        }
    }

    private void doSave() {
        clearMessage();
        try {
            if (currentObject.getWizardType().equalsIgnoreCase(SetCaseObject.WIZARD_PATH)){
                setCaseFoldersPanel.setFields();
                presenter.doSave();
            }
            if (currentObject.getWizardType().equalsIgnoreCase(SetCaseObject.WIZARD_INPUT)){
                setInputFieldsPanel.setFields();
                presenter.doSaveInput((CaseInput)currentObject.getObject());
            }
            else if(currentObject.getWizardType().equalsIgnoreCase(SetCaseObject.WIZARD_PARA)){
                setCaseParameterPanel.setFields();
                presenter.doSaveParam((CaseParameter)currentObject.getObject());
            }
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
    }
    
    private void clearMessage() {
        messagePanel.clear();
    }

    public void windowClosing() {
        doClose();
    }

    private void doClose() {
        try {
            if (shouldDiscardChanges())
                presenter.doClose();
        } catch (EmfException e) {
            messagePanel.setMessage("Could not close: " + e.getMessage());
        }
    }
    
    public void signalChanges() {
        clearMessage();
        super.signalChanges();
    }

    public void notifyLockFailure(Case caseObj) {
        String message = "Cannot edit Case: " + caseObj + System.getProperty("line.separator")
        + " as it was locked by User: " + caseObj.getLockOwner() + "(at " + format(caseObj.getLockDate()) + ")";
        InfoDialog dialog = new InfoDialog(parentConsole, "Message", message);
        dialog.confirm();
    }
    
    private String format(Date lockDate) {
        return CustomDateFormat.format_YYYY_MM_DD_HH_MM(lockDate);
    }

    private boolean shouldEditCase() {
        String message = "Would you like to edit this case " + System.getProperty("line.separator")
                + " without finishing the wizard?";
        String title = "Finish Wizard?";
        if (nexButton.isEnabled()) {
            YesNoDialog dialog = new YesNoDialog(parentConsole, title, message);
            return dialog.confirm();
        }

        return true;
    }
}
