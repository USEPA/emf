package gov.epa.emissions.framework.client.casemanagement;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.buttons.CancelButton;
import gov.epa.emissions.commons.gui.buttons.OKButton;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.meta.qa.EditQAArgumentsPresenter;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

public class CaseDescriptionWindow extends DisposableInteralFrame implements CaseDescriptionView { // BUG3621

    private JTextArea description;
    
    private JPanel layout;
    
    private String textDescription;

    private CaseDescriptionPresenter presenter;
    
    private boolean editable; // true if called by CaseEditor, false if called by CaseViewer
    
    public CaseDescriptionWindow(DesktopManager desktopManager, String textDescription, boolean editable) {
        super("Case Description", new Dimension(750, 350), desktopManager);
        this.textDescription = textDescription;
        this.editable = editable;
        this.getContentPane().add(createLayout());
    }
    
    public void display() {
        if ( editable) 
            super.setTitle("Edit Case Description"); 
        else 
            super.setTitle("View Case Description"); 
        super.display();
    }

    public void observe(CaseDescriptionPresenter presenter) {
        this.presenter = presenter;        
    }
    
    public JPanel createLayout() {
        layout = new JPanel();
        layout.setLayout(new BoxLayout(layout, BoxLayout.Y_AXIS));
        
        JPanel pnlArgument = new JPanel();
        pnlArgument.setLayout(new BoxLayout(pnlArgument, BoxLayout.X_AXIS));
        
        JLabel lblArgument = new JLabel("Description:    ");
        pnlArgument.add( lblArgument);
        
        description = new JTextArea();
        description.setWrapStyleWord(true);
        description.setLineWrap(true);
        description.setText(textDescription);
        JScrollPane scrollPane = new JScrollPane( description);
        scrollPane.setPreferredSize(new Dimension(650,250));
        pnlArgument.add( scrollPane);
        
        pnlArgument.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        //pnlArgument.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));

        layout.add( pnlArgument);
        layout.add(buttonPanel());
       
        return layout;
    }
    
    private JPanel buttonPanel() {
        JPanel panel = new JPanel();
        FlowLayout layout = new FlowLayout();
        layout.setHgap(20);
        layout.setVgap(25);
        panel.setLayout(layout);
        if ( editable) {
            panel.add(new OKButton(okAction()));
            panel.add(new CancelButton(cancelAction()));
        } else {
            panel.add(new OKButton(cancelAction()));
        }
        panel.setMaximumSize(new Dimension(1000,80));
        return panel;
    }
    
    private Action cancelAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                dispose();
                disposeView();
            }
        };
    }

    private Action okAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                String descText = description.getText();
                // Send the modified text from the arguments text area to the one in
                // the CaseSummaryTab description text area.
                presenter.refreshDescription(descText);
                dispose();
                disposeView();
            }
        };
    }
}
