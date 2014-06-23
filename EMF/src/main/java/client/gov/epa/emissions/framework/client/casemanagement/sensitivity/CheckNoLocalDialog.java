package gov.epa.emissions.framework.client.casemanagement.sensitivity;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.buttons.OKButton;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.ui.Dialog;
import gov.epa.mims.analysisengine.gui.ScreenUtils;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JPanel;

public class CheckNoLocalDialog  extends Dialog{
    
    private SetCasePresenter presenter;
    
    private EmfConsole parentConsole;
    
    private Case caseObj; 
    
    private boolean hasValues = false; 
    
    public CheckNoLocalDialog(EmfConsole parentConsole, SetCasePresenter presenter, Case caseObj){
        super("Validation for Case: " + caseObj.getName(), parentConsole);
        super.setSize(new Dimension(400, 200));
        this.presenter = presenter; 
        this.parentConsole =parentConsole;
        this.caseObj = caseObj; 
    }
    
    
    public void display(String msg) {
        //String validationMsg = validateValues();
        getContentPane().add(createPanel(msg));
        setLocation(ScreenUtils.getPointToCenter(parentConsole));
        pack();
        setModal(false);
        setVisible(true);
    }
    
    private JPanel createPanel(String msg){
        JPanel panel = new JPanel(new BorderLayout());
        Button ok = new OKButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                close();
            }
        });
        
        int width = 50;
        int height = (msg.length() / 50)+3;
        //getContentPane().add(createMsgScrollPane(msg, width, 20));
        panel.add(createMsgScrollPane(msg, width, height));
        
        JPanel okPanel = new JPanel();
        okPanel.add(ok);
        panel.add(okPanel, BorderLayout.SOUTH);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));
        return panel;
    }
    
    private ScrollableComponent createMsgScrollPane(String msg, int width, int height) {
        TextArea message = new TextArea("msgArea", msg, width, height);
        message.setEditable(false);
        ScrollableComponent descScrollableTextArea = new ScrollableComponent(message);
        return descScrollableTextArea;
    }
    
    public String validateValues() throws EmfException{
        String validateValues = "Jobs in the case may not run until the following items are corrected: \n";
        String validateNLInputs = presenter.validateNLInputs(caseObj.getId());
        if (!validateNLInputs.trim().isEmpty()){
            hasValues =true; 
            validateValues += "\nThe following non-local inputs do not have datasets specified: \n";
            validateValues += validateNLInputs;
        }
        String validateNLPara = presenter.validateNLParameters(caseObj.getId());
        if (!validateNLPara.trim().isEmpty()){
            hasValues =true; 
            validateValues += "\nThe following non-local parameters do not have values:  \n";
            validateValues += validateNLPara;
        }
        return validateValues;
    }
    
    public boolean getHasValues(){
        return hasValues;
    }
}
