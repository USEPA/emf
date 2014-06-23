package gov.epa.emissions.framework.client.data.editor;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.framework.client.ReusableInteralFrame;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

public class DataFindReplaceWindow extends ReusableInteralFrame implements FindReplaceWindowView {

    private String[] cols = new String[0];

    private SingleLineMessagePanel messagePanel;

    private FindReplaceViewPresenter presenter;

    private JPanel layout;

    private JLabel filterLabel;

    private Button okButton;

    private Version version;

    private String table;

    private JTextArea sortOrder;
    
    private ManageChangeables listOfChangeables;
    
    private JTabbedPane tabbedPane;
    
    private DataFindReplaceBasicTab noFilterTab;
    
    private DataFindReplaceAdvancedTab withFilterTab;
    
    private JTextArea filter;

    public DataFindReplaceWindow(String dsName, String table, Version version, JTextArea filter, JTextArea sortOrder,
            DesktopManager desktopManager, String[] cols, ManageChangeables listOfChangeables) {
        super("Find and Replace Column Values", new Dimension(490, 270), desktopManager);
        super.setLabel("Find and Replace Column Values: " + dsName + " (version: " + version.getVersion() + ")");
        
        this.cols = cols;
        this.table = table;
        this.version = version;
        this.listOfChangeables = listOfChangeables;
        this.sortOrder = sortOrder;
        this.filter = filter;
        this.filterLabel = new JLabel(filter.getText() == null || filter.getText().trim().isEmpty() ? "NO FILTER"
                : filter.getText().trim());
        layout = new JPanel();
        this.getContentPane().add(layout);
    }

    public void display() {
        
        this.filterLabel.setText(filter.getText() == null || filter.getText().trim().isEmpty() ? "NO FILTER"
                : filter.getText().trim());

        layout.setLayout(new BoxLayout(layout, BoxLayout.Y_AXIS));

        messagePanel = new SingleLineMessagePanel();

        layout.add( messagePanel, BorderLayout.PAGE_START );
        layout.add(createTabbedPane());
        layout.add(createButtonPanel(), BorderLayout.PAGE_END);

//        layout.add(selectionPanel());
//        layout.add(createButtonPanel());

        setResizable(true);
        super.display();
    }
    
    private JTabbedPane createTabbedPane() {
        tabbedPane = new JTabbedPane();

        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.addTab("Basic", createNoFilterTab());
        tabbedPane.addTab("Advanced", createWithFilterTab());
        return tabbedPane;
    }

    private JPanel createNoFilterTab() {
        noFilterTab = new DataFindReplaceBasicTab( table, version, filterLabel, 
                sortOrder, cols, listOfChangeables, messagePanel, this);
        noFilterTab.observe(presenter);
        noFilterTab.display();
        return noFilterTab;
    }
    
    private JPanel createWithFilterTab() {
        withFilterTab = new DataFindReplaceAdvancedTab( table, version, filterLabel, 
                sortOrder, listOfChangeables, messagePanel, this);
        withFilterTab.observe(presenter);
        withFilterTab.display();
        return withFilterTab;
    }

    private Component createButtonPanel() {
        JPanel panel = new JPanel();

        okButton = new Button("Apply", applyAction());
        panel.add(okButton);

        Button closeButton = new Button("Close", closeWindowAction());
        panel.add(closeButton);

        return panel;
    }

    public void observe(FindReplaceViewPresenter presenter) {
        this.presenter = presenter;
    }

    public void setOkButtonEnableState(boolean state) {
        okButton.setEnabled(state);
    }
    
    private Action applyAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                clearMsgPanel();
                try {
                    Integer tabIdx = tabbedPane.getSelectedIndex();
                    if ( tabIdx == 0 )
                        noFilterTab.apply();
                    if ( tabIdx == 1 )
                        withFilterTab.apply();
                     
                } catch (EmfException e) {
//                    e.printStackTrace();
                    if (!e.getMessage().trim().isEmpty())
                        setErrorMsg(e.getMessage());
                }
            }
        };
    }


    private Action closeWindowAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                disposeView();
            }
        };
    }
    
    private void clearMsgPanel() {
        messagePanel.clear();
    }

    public void setErrorMsg(String errorMsg) {
        messagePanel.setError(errorMsg);
    }

    private void setMsg(String msg) {
        messagePanel.setMessage(msg);
    }

}
