package gov.epa.emissions.framework.client.moduletype;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.buttons.CancelButton;
import gov.epa.emissions.commons.gui.buttons.OKButton;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.Label;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.meta.EmfImageTool;
import gov.epa.emissions.framework.services.module.ModuleTypeVersion;
import gov.epa.emissions.framework.services.module.ModuleTypeVersionRevision;
import gov.epa.mims.analysisengine.gui.ScreenUtils;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;

public class ModuleTypeVersionNewRevisionDialog extends JDialog implements ModuleTypeVersionNewRevisionView {

    private EmfConsole parent;

    private ModuleTypeVersionNewRevisionPresenter presenter;

    ModuleTypeVersion moduleTypeVersion;
    ModuleTypeRevisionsObserver moduleTypeRevisionsObserver;
    
    private Label moduleTypeName;
    private Label moduleTypeVersionName;

    private TextArea oldRevisionsReport;
    
    private TextArea newRevisionText;
    
    public ModuleTypeVersionNewRevisionDialog(EmfConsole parent, ModuleTypeVersion moduleTypeVersion, ModuleTypeRevisionsObserver moduleTypeRevisionsObserver) {
        super(parent);
        super.setIconImage(EmfImageTool.createImage("/logo.JPG"));
        this.parent = parent;
        setModal(true);

        this.moduleTypeVersion = moduleTypeVersion;
        this.moduleTypeRevisionsObserver = moduleTypeRevisionsObserver;
    }

    public void display() {
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout(5, 5));
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.add(revisionPanel(), BorderLayout.NORTH);
        panel.add(buttonPanel(), BorderLayout.SOUTH);
        contentPane.add(panel);
        
        setTitle("New Revision");           

        this.pack();
        this.setSize(820, 440);
        this.setLocation(ScreenUtils.getPointToCenter(this));
        newRevisionText.requestFocusInWindow();
        this.setVisible(true);
    }

    public void observe(ModuleTypeVersionNewRevisionPresenter presenter) {
        this.presenter = presenter;
    }

    private JPanel revisionPanel() {

        JPanel formPanel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        moduleTypeName = new Label(moduleTypeVersion.getModuleType().getName());
        layoutGenerator.addLabelWidgetPair("Module Type:", moduleTypeName, formPanel);
        
        moduleTypeVersionName = new Label(moduleTypeVersion.versionName());
        layoutGenerator.addLabelWidgetPair("Version:", moduleTypeVersionName, formPanel);
        
        oldRevisionsReport = new TextArea("Old Revisions", moduleTypeVersion.revisionsReport(), 60, 8);
        oldRevisionsReport.setEditable(false);
        ScrollableComponent oldRrevisionsReportScrollableTextArea = new ScrollableComponent(oldRevisionsReport);
        oldRrevisionsReportScrollableTextArea.setMaximumSize(new Dimension(575, 200));
        layoutGenerator.addLabelWidgetPair("Old Revisions:", oldRrevisionsReportScrollableTextArea, formPanel);

        newRevisionText = new TextArea("New Revision", "", 60, 8);
        ScrollableComponent newRevisionScrollableTextArea = new ScrollableComponent(newRevisionText);
        newRevisionScrollableTextArea.setMaximumSize(new Dimension(575, 200));
        layoutGenerator.addLabelWidgetPair("New Revision:", newRevisionScrollableTextArea, formPanel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(formPanel, 4, 2, // rows, cols
                10, 10, // initialX, initialY
                10, 10);// xPad, yPad

        oldRevisionsReport.setCaretPosition(oldRevisionsReport.getDocument().getLength());
        
        return formPanel;
    }
    
    private JPanel buttonPanel() {
        JPanel panel = new JPanel();
        
        Button addButton = new Button("Add Revision", okAction());
        addButton.setMnemonic(KeyEvent.VK_A);
        
        Button skipButton = new Button("Skip Revision", cancelAction());
        skipButton.setMnemonic(KeyEvent.VK_S);
        
        panel.add(addButton);
        panel.add(skipButton);
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
                String text = newRevisionText.getText().trim();
                if (text.isEmpty()) { 
                    JOptionPane.showMessageDialog(parent, 
                            "Please enter the new revision text", 
                            "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    ModuleTypeVersionRevision moduleTypeVersionRevision = new ModuleTypeVersionRevision();
                    moduleTypeVersionRevision.setCreationDate(new Date());
                    moduleTypeVersionRevision.setCreator(presenter.getSession().user());
                    moduleTypeVersionRevision.setDescription(text);
                    moduleTypeVersion.addModuleTypeVersionRevision(moduleTypeVersionRevision);
                    moduleTypeRevisionsObserver.refreshRevisions();
                    setVisible(false);
                    dispose();
                }
            }
        };
    }
}