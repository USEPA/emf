package gov.epa.emissions.framework.client.cost.controlstrategy.groups;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.List;

import gov.epa.emissions.commons.gui.BorderlessButton;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.cost.controlstrategy.ControlStrategiesTableData;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.controlStrategy.StrategyGroup;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.SelectableSortFilterWrapper;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class StrategyGroupStrategiesTab extends JPanel implements StrategyGroupTabView {

    private StrategyGroup strategyGroup;

    private ManageChangeables changeablesList;

    private EmfSession session;

    private MessagePanel messagePanel;
    
    private EmfConsole parentConsole;
    
    private ControlStrategiesTableData tableData;

    private SelectableSortFilterWrapper table;

    private JPanel tablePanel;
    
    public StrategyGroupStrategiesTab(StrategyGroup strategyGroup, EmfSession session, 
            ManageChangeables changeablesList, SingleLineMessagePanel messagePanel, 
            EmfConsole parentConsole) {
        super.setName("strategies");
        this.strategyGroup = strategyGroup;
        tableData = new ControlStrategiesTableData(strategyGroup.getControlStrategies());
        this.session = session;
        this.changeablesList = changeablesList;
        this.messagePanel = messagePanel;
        this.parentConsole = parentConsole;
    }
    
    public void setStrategyGroup(StrategyGroup strategyGroup) {
        this.strategyGroup = strategyGroup;
    }
    
    public void display() {
        super.setLayout(new BorderLayout());

        JPanel panel = new JPanel(new BorderLayout(10, 10));

        tablePanel = new JPanel(new BorderLayout());
        table = new SelectableSortFilterWrapper(parentConsole, tableData, null);
        tablePanel.add(table);

        panel.add(tablePanel, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel();

        Button addButton = new BorderlessButton("Add to Group", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                addAction();
            }
        });
        addButton.setMnemonic('A');
        buttonPanel.add(addButton);

        Button removeButton = new BorderlessButton("Remove from Group", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {           
                removeAction();
            }
        });
        removeButton.setMnemonic('R');
        buttonPanel.add(removeButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        super.add(panel, BorderLayout.CENTER);
    }
    
    private void addAction() {
        ControlStrategySelectionView view = new ControlStrategySelectionDialog(parentConsole, changeablesList);
        ControlStrategySelectionPresenter presenter = new ControlStrategySelectionPresenter(this, session);
        try {
            presenter.display(view);
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
    }
    
    public void add(ControlStrategy[] strategies) {
        messagePanel.clear();
        if (strategies.length > 0) {
            tableData.add(strategies);
            
            refresh();
        }
    }
    
    private void removeAction() {
        messagePanel.clear();
        List selected = table.selected();

        if (selected.size() == 0) {
            messagePanel.setError("Please select a control strategy to remove from the group.");
            return;
        }

        ControlStrategy[] strategies = (ControlStrategy[])selected.toArray(new ControlStrategy[0]);

        if (strategies.length == 0)
            return;

        String title = "Warning";
        String message = "Are you sure you want to remove the selected control strategies from the group?";
        int selection = JOptionPane.showConfirmDialog(parentConsole, message, title, JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (selection == JOptionPane.YES_OPTION) {
            tableData.remove(strategies);
            refresh();
        }
    }
    
    public void save() {
        ControlStrategy[] strategies = {};
        if (tableData != null) {
            strategies = new ControlStrategy[tableData.rows().size()];
            for (int i = 0; i < tableData.rows().size(); i++) {
                strategies[i] = (ControlStrategy)tableData.element(i);
            }
            strategyGroup.setControlStrategies(strategies);
        }
    }
    
    private void refresh(){
        table.refresh(tableData);
        panelRefresh();
    }
    
    private void panelRefresh() {
        tablePanel.removeAll();
        tablePanel.add(table);
        super.validate();
    }
}
