package gov.epa.emissions.framework.client.casemanagement;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.buttons.CancelButton;
import gov.epa.emissions.commons.gui.buttons.OKButton;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseCategory;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

public class NewCaseWindow extends DisposableInteralFrame implements NewCaseView {
    private NewCasePresenter presenter;

    private JPanel layout;

    private SingleLineMessagePanel messagePanel;

    private JTextField name;

    private ComboBox categoriesCombo;
    
    private Dimension defaultDimension = new Dimension(255, 22);

    public NewCaseWindow(DesktopManager desktopManager) {
        super("Create a Case", new Dimension(400, 200), desktopManager);
        layout = new JPanel();
        layout.setLayout(new BoxLayout(layout, BoxLayout.Y_AXIS));
        super.getContentPane().add(layout);
    }

    private void doLayout(JPanel layout) {
        messagePanel = new SingleLineMessagePanel();
        layout.add(messagePanel);
        
        try {
            layout.add(createInputPanel());
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
        
        layout.add(createButtonsPanel());
    }

    public void observe(NewCasePresenter presenter) {
        this.presenter = presenter;
    }

    public void display() {
        super.setLabel("Create a Case");
        layout.removeAll();
        doLayout(layout);

        super.display();
    }

    private JPanel createInputPanel() throws EmfException {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        name = new JTextField();
        name.setPreferredSize(defaultDimension);
        layoutGenerator.addLabelWidgetPair("Name:", name, panel);
        layoutGenerator.addLabelWidgetPair("Category:", categories(), panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 2, 2, // rows, cols
                10, 0, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }
    
    private ComboBox categories() throws EmfException {
        categoriesCombo = new ComboBox(presenter.getCaseCategories());
        categoriesCombo.setSelectedItem(presenter.getSelectedCategory());
        categoriesCombo.setPreferredSize(defaultDimension);

        return categoriesCombo;
    }

    private Action saveAction() {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                resetChanges();
                CaseCategory cat = (CaseCategory)categoriesCombo.getSelectedItem();

                if (name.getText() == null || name.getText().trim().isEmpty()) {
                    messagePanel.setError("Please give a name for the new case.");
                    return;
                }
                    
                if (cat == null) {
                    messagePanel.setError("Please select a valid category.");
                    return;
                }
                
                Case newCase = new Case(name.getText().trim());
                newCase.setCaseCategory(cat);
                
                try {
                    presenter.doSave(newCase);
                } catch (EmfException e) {
                    messagePanel.setError(e.getMessage());
                }
            }
        };

        return action;
    }

    public void windowClosing() {
        doClose();
    }

    private Action closeAction() {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                doClose();
            }
        };

        return action;
    }

    private void doClose() {
        if (shouldDiscardChanges())
            presenter.doClose();
    }

    private JPanel createButtonsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel container = new JPanel();
        FlowLayout layout = new FlowLayout();
        layout.setHgap(20);
        layout.setVgap(25);
        container.setLayout(layout);

        Button saveButton = new OKButton(saveAction());
        container.add(saveButton);
        container.add(new CancelButton(closeAction()));
        getRootPane().setDefaultButton(saveButton);

        panel.add(container, BorderLayout.CENTER);

        return panel;
    }

}
