package gov.epa.emissions.framework.client.sms.sectorscenario.base;

import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.gui.buttons.CancelButton;
import gov.epa.emissions.commons.gui.buttons.OKButton;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.meta.EmfImageTool;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.sms.SectorScenario;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;
import gov.epa.mims.analysisengine.gui.ScreenUtils;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class NewSectorScenarioDialog extends JDialog implements NewSectorScenarioView {

    private NewSectorScenarioPresenter presenter;
    
    //private SectorScenarioManagerPresenter managerPresenter;

    private TextField name; 
    
    private TextArea description; 
    
    private TextField abbreviation; 

    private JList datasetList;
    
    private EmfSession session;
    
    private SingleLineMessagePanel messagePanel;
    
    public NewSectorScenarioDialog(EmfConsole parent, EmfSession session,
            DesktopManager desktopManager) {
        super(parent);
        super.setIconImage(EmfImageTool.createImage("/logo.JPG"));
        this.session = session;
        setModal(true);
    }

    public void display(){
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout(5, 5));
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        messagePanel = new SingleLineMessagePanel();
        
        panel.add(buildTopPanel(), BorderLayout.NORTH);
        panel.add(buttonPanel(), BorderLayout.SOUTH);
        contentPane.add(panel);
        setTitle("Create New Sector Scenario");
        this.pack();
        this.setSize(500, 300);
        this.setLocation(ScreenUtils.getPointToCenter(this));
        this.setVisible(true);
    }

    public void refreshDatasets(EmfDataset[] datasets) {
        datasetList.setListData(datasets);
    }

    private JPanel buildTopPanel() {
        JPanel panel = new JPanel ();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(messagePanel);
        panel.add(buildNameContains());
        return panel; 
    }

    private JPanel buildNameContains() {
        JPanel panel = new JPanel(new SpringLayout()); 
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        name = new TextField("Name:", 25);
        layoutGenerator.addLabelWidgetPair("Name: ", name, panel);
        
        abbreviation = new TextField("Abbreviation:", 25);
        layoutGenerator.addLabelWidgetPair("Abbreviation: ", abbreviation, panel);
        
        description = new TextArea("Description:", "", 25, 4);
        layoutGenerator.addLabelWidgetPair("Description: ", description, panel);

        layoutGenerator.makeCompactGrid(panel, 3, 2, // rows, cols
                25, 10, // initialX, initialY
                5, 5);// xPad, yPad
        return panel; 
    }
 
    private JPanel buttonPanel() {
        JPanel panel = new JPanel();
        panel.add(new OKButton(okAction()));
        panel.add(new CancelButton(cancelAction()));
        return panel;
    }

    private Action cancelAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                dispose();
            }
        };
    }

    private Action okAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                
                SectorScenario sectorScenario = new SectorScenario();
                sectorScenario.setName(name.getText());
                sectorScenario.setDescription(description.getText());
                sectorScenario.setAbbreviation(abbreviation.getText());
                sectorScenario.setCreator(session.user());
                sectorScenario.setRunStatus("Not started");

                try {
                    presenter.addSectorScenario(sectorScenario);              
                    setVisible(false);
                    dispose();

                } catch (EmfException e1) {
                    messagePanel.setError(e1.getMessage());
                    //e1.printStackTrace();
                }
            }
        };
    }

    public void observe(NewSectorScenarioPresenter presenter) {
        this.presenter = presenter;
    }
    
    public void clearMessage() {
        messagePanel.clear();
        
    }

}