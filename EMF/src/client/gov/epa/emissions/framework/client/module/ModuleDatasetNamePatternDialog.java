package gov.epa.emissions.framework.client.module;

import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.gui.buttons.CancelButton;
import gov.epa.emissions.commons.gui.buttons.OKButton;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.meta.EmfImageTool;
import gov.epa.mims.analysisengine.gui.ScreenUtils;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class ModuleDatasetNamePatternDialog extends JDialog implements ModuleDatasetNamePatternView {

    private EmfConsole parent;

    private ModuleDatasetNamePatternPresenter presenter;

    private String initialNamePattern;
    
    private TextField datasetNamePattern;
    
    private boolean isOK;

    public ModuleDatasetNamePatternDialog(EmfConsole parent, String initialText) {
        super(parent);
        super.setIconImage(EmfImageTool.createImage("/logo.JPG"));
        this.parent = parent;
        this.initialNamePattern = initialText;
        this.isOK = false;
        setModal(true);
    }

    public void display() {
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout(5, 5));
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.add(selectionPanel(), BorderLayout.NORTH);
        panel.add(buttonPanel(), BorderLayout.SOUTH);
        contentPane.add(panel);
        
        setTitle("Dataset Name Pattern");

        this.pack();
        this.setSize(725, 110);
        this.setLocation(ScreenUtils.getPointToCenter(this));
        this.setVisible(true);
    }

    public void observe(ModuleDatasetNamePatternPresenter presenter) {
        this.presenter = presenter;
    }

    public String getDatasetNamePattern() {
        return datasetNamePattern.getText();
    }
    
    public boolean isOK() {
        return isOK;
    }
    
    private JPanel selectionPanel() {

        JPanel formPanel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        datasetNamePattern = new TextField("name", 60);
        datasetNamePattern.setText(initialNamePattern);
        datasetNamePattern.setMaximumSize(new Dimension(550, 20));
        layoutGenerator.addLabelWidgetPair("", datasetNamePattern, formPanel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(formPanel, 1, 2, // rows, cols
                10, 10, // initialX, initialY
                10, 10);// xPad, yPad

        return formPanel;
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
                datasetNamePattern.setText(initialNamePattern);
                dispose();
            }
        };
    }

    private Action okAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                isOK = true;
                setVisible(false);
                dispose();
            }
        };
    }
}
