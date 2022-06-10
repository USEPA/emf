package gov.epa.emissions.framework.client.casemanagement.editor;

import gov.epa.emissions.commons.gui.buttons.CancelButton;
import gov.epa.emissions.commons.gui.buttons.ViewButton;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.ui.ListWidget;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class FindCaseWindow extends DisposableInteralFrame implements RelatedCaseView {

    private EmfConsole parentConsole;
    
    private Case[] caseProduceThisDataset;

    private Case[] casesUseThisDataset;
    
    private ListWidget produceListWidget;

    private ListWidget useListWidget;
    
    private RelatedCasePresenter presenter; 
    
    private MessagePanel messagePanel;
    
    private EmfSession session;

    public FindCaseWindow(String title, EmfSession session, EmfConsole parentConsole, DesktopManager desktopManager) {
        super(title, new Dimension(550, 400), desktopManager);
        this.parentConsole = parentConsole;
        this.desktopManager = desktopManager;
        this.session = session; 
    }

    public void display(Case[] inputCases, Case[] outputCases) {
        this.caseProduceThisDataset = inputCases;
        this.casesUseThisDataset = outputCases;
        
        JPanel layout = createLayout();
        
        super.getContentPane().add(layout);
        super.display();
        
    }
    
    private JPanel createLayout() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        messagePanel = new SingleLineMessagePanel();
        panel.add(messagePanel);
        
        JPanel panel1 = inputListWidget();
        panel.add(panel1);
        
        JPanel panel2 = outputListWidget();
        panel.add(panel2);
        
        panel.add(buttonPanel());
        return panel; 
    }

    private JPanel inputListWidget() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        final JLabel caseProducerLabel = new JLabel("Case that Produced Dataset: ");
        panel.add(caseProducerLabel, BorderLayout.NORTH);
        produceListWidget = new ListWidget(caseProduceThisDataset);
        produceListWidget.setToolTipText("Choose case that produced dataset");
        caseProducerLabel.setLabelFor(produceListWidget);
        JScrollPane pane = new JScrollPane(produceListWidget);
        panel.add(pane);
        return panel;
    }
    
    private JPanel outputListWidget() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        final JLabel caseUsesDatasetLabel = new JLabel("Cases that Use Dataset: ");
        panel.add(caseUsesDatasetLabel, BorderLayout.NORTH);
        useListWidget = new ListWidget(casesUseThisDataset);
        useListWidget.setToolTipText("Choose case that uses dataset");
        caseUsesDatasetLabel.setLabelFor(useListWidget);
        JScrollPane pane = new JScrollPane(useListWidget);
        panel.add(pane);
        return panel;
    }

    private JPanel buttonPanel() {
        ViewButton viewButton = new ViewButton(viewAction());
        CancelButton cancelButton = new CancelButton(cancelAction());
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(viewButton);
        buttonPanel.add(cancelButton);
        return buttonPanel;
    }

    private Action cancelAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                disposeView();
            }
        };
    }

    private Action viewAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    viewCase(getSelectedValues());
                } catch (EmfException e1) {
                    messagePanel.setError(e1.getMessage());
                }
            }
        };
    }

    private Case[] getSelectedValues() {
        Object[] valuesIn = produceListWidget.getSelectedValues();
        Object[] valuesout = useListWidget.getSelectedValues();
        List<Object> values = new ArrayList<Object>();
        if (valuesIn !=null && valuesIn.length > 0){
            for (int i = 0; i < valuesIn.length; i++) {
                values.add(valuesIn[i]);
            }
        }
           
        if (valuesout !=null && valuesout.length > 0){
            for (int i = 0; i < valuesout.length; i++) {
                values.add(valuesout[i]);
            }
        }
        
        return values.toArray(new Case[0]);
    }

    private void viewCase(Case[] cases) throws EmfException{
        messagePanel.clear();

        if (cases == null || cases.length ==0 ) {
            messagePanel.setMessage("No case is selected.");
            return;
        }
        
        for (Case caseObj : cases){
            Case inputCase = presenter.getCaseFromName(caseObj.getName());
            CaseViewer view = new CaseViewer(parentConsole, session, desktopManager);
            presenter.doView(view, inputCase);
        }
    }

    public void observe(RelatedCasePresenter presenter) {
       this.presenter = presenter;
    }
   
}
