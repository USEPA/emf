package gov.epa.emissions.framework.client.sms.sectorscenario.editor;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.sms.SectorScenario;
import gov.epa.emissions.framework.services.sms.SectorScenarioOutput;
import gov.epa.emissions.framework.ui.Border;
import gov.epa.emissions.framework.ui.MessagePanel;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SpringLayout;

public class EditSectorScenarioOptionsTab extends JPanel implements EditSectorScenarioOptionsTabView {

    private MessagePanel messagePanel;
    
    protected SectorScenario sectorScenario;
    
    protected JCheckBox annoInvEECS;
    
    private JRadioButton addEECSCol;
    
    private JRadioButton useEECSFromInv;
    
    private JRadioButton fillMissEECS;
    
    private JCheckBox exportOutput;
    
    private ButtonGroup buttonGroup;
    
    protected EmfSession session;
    
    protected EmfConsole parentConsole;

    private EditSectorScenarioOptionsTabPresenter presenter;
    
    public EditSectorScenarioOptionsTab(SectorScenario sectorScenario, 
            MessagePanel messagePanel, EmfConsole parentConsole, 
            EmfSession session, DesktopManager desktopManager){
        super.setName("sectorscenarioinputs");
        
        this.messagePanel = messagePanel;
        this.sectorScenario = sectorScenario;
        this.parentConsole = parentConsole;
        this.session = session;
    }


    public void observe(EditSectorScenarioOptionsTabPresenter presenter){
        this.presenter = presenter;
    }

    public void display() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(getBorderedPanel(createmMainSection(), ""), BorderLayout.NORTH);
        
        setLayout(new BorderLayout(5, 5));
        add(panel,BorderLayout.NORTH);
        this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        this.add(radioPanel(), BorderLayout.CENTER);
        this.add(buildListPanel(), BorderLayout.SOUTH);
        
    }
    
    private JPanel radioPanel() {
        addEECSCol = new JRadioButton("add/overwrite eecs column");
        useEECSFromInv = new JRadioButton("use eecs from inventory");
        fillMissEECS = new JRadioButton ("fill-in missing eecs within inventory");
        buttonGroup = new ButtonGroup();
        buttonGroup.add(addEECSCol);     
        buttonGroup.add(useEECSFromInv);
        buttonGroup.add(fillMissEECS);
        
        short annEccsOpt = sectorScenario.getAnnotatingEecsOption();
        
        if (annEccsOpt == 1)
            addEECSCol.setSelected(true);
        else if (annEccsOpt == 2)
            useEECSFromInv.setSelected(true);
        else
            fillMissEECS.setSelected(true);
        
        JPanel radioPanel = new JPanel();
        radioPanel.setLayout(new BoxLayout(radioPanel, BoxLayout.Y_AXIS));
        radioPanel.add(new JLabel(" "));
        radioPanel.add(new JLabel("Annotate eecs options"));
        radioPanel.add(addEECSCol);
        radioPanel.add(useEECSFromInv);
        radioPanel.add(fillMissEECS);
        
        this.exportOutput = new JCheckBox("transfer");
        this.exportOutput.setSelected(sectorScenario.getExportOutput());
        //this.exportOutput.setEnabled( false);
        
        radioPanel.add(new JLabel(" "));
        radioPanel.add(new JLabel("Transfer Sector Scenario Output"));
        radioPanel.add(this.exportOutput);
        
        return radioPanel;
    }
        
    private JPanel getBorderedPanel(JPanel component, String border) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new Border(border));
        panel.add(component);

        return panel;
    }
    
    private JPanel createmMainSection() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        
        JPanel panel = new JPanel(new SpringLayout());
        
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();
        
        layoutGenerator.addLabelWidgetPair("Annotate inventory with eecs:", annoInv(), panel);
        layoutGenerator.makeCompactGrid(panel, 1, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad
        mainPanel.add(panel);
        return mainPanel;
    }
    
    private JCheckBox annoInv() {
        annoInvEECS = new JCheckBox("", null, sectorScenario.getAnnotateInventoryWithEECS() != null ? sectorScenario.getAnnotateInventoryWithEECS() : true);
        return annoInvEECS;
    } 

    private JPanel buildListPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.add(listPanel(), BorderLayout.CENTER);
        presenter.getClass();
        
        messagePanel.clear();
        return panel; 
    }
    
    private JPanel listPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        return panel;
    }
    
    public void save(SectorScenario sectorScenario){
        sectorScenario.setAnnotateInventoryWithEECS(annoInvEECS.isSelected());
        if (buttonGroup.getSelection().equals(addEECSCol.getModel())){
            Short choice = 1; 
            sectorScenario.setAnnotatingEecsOption(choice);
        }
        if (buttonGroup.getSelection().equals(useEECSFromInv.getModel())){
            Short choice = 2; 
            sectorScenario.setAnnotatingEecsOption(choice);
        }
        if (buttonGroup.getSelection().equals(fillMissEECS.getModel())){
            Short choice = 3; 
            sectorScenario.setAnnotatingEecsOption(choice);
        }
        if (this.exportOutput.isSelected()) {
            sectorScenario.setExportOutput( true);
        } else {
            sectorScenario.setExportOutput( false);
        }
    }

    public void refresh(SectorScenario secSce, SectorScenarioOutput[] sectorScenarioOutputs) {

//DON'T refresh anything from this tab, leave as is...
//        annoInvEECS.setSelected(secSce.getAnnotateInventoryWithEECS());
//        
//        short annEccsOpt = secSce.getAnnotatingEecsOption();
//        
//        if (annEccsOpt == 1)
//            addEECSCol.setSelected(true);
//        else if (annEccsOpt == 2)
//            useEECSFromInv.setSelected(true);
//        else
//            fillMissEECS.setSelected(true);
    }


    public void viewOnly() {
        // NOTE Auto-generated method stub
        
    }


}