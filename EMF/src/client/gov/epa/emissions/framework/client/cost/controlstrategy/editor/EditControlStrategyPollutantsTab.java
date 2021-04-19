package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.buttons.AddButton;
import gov.epa.emissions.commons.gui.buttons.RemoveButton;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.StrategyType;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.ui.ListWidget;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;
import gov.epa.emissions.framework.ui.TrackableSortFilterSelectModel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class EditControlStrategyPollutantsTab extends JPanel implements ControlStrategyPollutantsTabView {

    private ListWidget pollutantsList;

    private Pollutant[] pollutants = new Pollutant[0];

    private EditControlStrategyPollutantsTabPresenter presenter;

    private ManageChangeables changeablesList;

    private JPanel mainPanel;

    private SingleLineMessagePanel messagePanel;

    private PollutantTableData tableData;

    private TrackableSortFilterSelectModel sortFilterSelectModel;

    private EmfConsole parent;

    private EmfSession session;

    private Button addButton = new AddButton(addAction());
    
//    private ControlStrategy controlStrategy;

    // private JPanel sortFilterPanelContainer = new JPanel();

    public EditControlStrategyPollutantsTab(ControlStrategy controlStrategy, ManageChangeables changeablesList,
            SingleLineMessagePanel messagePanel, EmfConsole parentConsole, EmfSession session) {
//        this.controlStrategy = controlStrategy;
        this.changeablesList = changeablesList;
        this.messagePanel = messagePanel;
        this.parent = parentConsole;
        this.session = session;
    }

    public void display(ControlStrategy strategy) {
//        this.allClasses = presenter.getAllClasses();
//        this.classes = presenter.getControlMeasureClasses();
        mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setupLayout(changeablesList);
    }

    private void setupLayout(ManageChangeables changeables) {
        try {
            Pollutant[] cmObjs = presenter.getPollutants();
            tableData = new PollutantTableData(cmObjs);
        } catch (Exception e) {
            messagePanel.setError(e.getMessage());
        }

        this.setLayout(new BorderLayout(10, 10));
        // this.setBorder(BorderFactory.createEmptyBorder(50,50,0,300));
        this.add(createClassesPanel(changeables), BorderLayout.WEST);

    }

    private JPanel buttonPanel() {
        JPanel panel = new JPanel();
        panel.add(addButton);
        Button removeButton = new RemoveButton(removeAction());
        panel.add(removeButton);

        JPanel container = new JPanel(new BorderLayout());
        container.add(panel, BorderLayout.LINE_START);

        return container;
    }

    private Action addAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                selectionView();
            }
        };
    }

    private void selectionView() {
        PollutantSelectionView view = new PollutantSelectionDialog(parent, changeablesList);
        try {
            PollutantSelectionPresenter presenter = new PollutantSelectionPresenter(this, view, session,
                    this.presenter.getAllPollutants());
            presenter.display(view);
        } catch (Exception exp) {
            messagePanel.setError(exp.getMessage());
        }
    }

    private Action removeAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                remove();
            }
        };
    }

    protected void remove() {
        messagePanel.clear();
        List selected = sortFilterSelectModel.selected();

        if (selected.size() == 0) {
            messagePanel.setError("Please select an item to remove.");
            return;
        }

        Pollutant[] records = (Pollutant[]) selected.toArray(new Pollutant[0]);

        if (records.length == 0)
            return;

        String title = "Warning";
        String message = "Are you sure you want to remove the selected row(s)?";
        int selection = JOptionPane.showConfirmDialog(parent, message, title, JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (selection == JOptionPane.YES_OPTION) {
            tableData.remove(records);
        }

    }

    private JPanel createClassesPanel(ManageChangeables changeables) {

        // build list widget
        this.pollutantsList = new ListWidget(pollutants, new Pollutant[0]);
        changeables.addChangeable(pollutantsList);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 0));
        JLabel label = new JLabel("Pollutants to Include:");
        JScrollPane scrollPane = new JScrollPane(pollutantsList);
        scrollPane.setPreferredSize(new Dimension(20, 100));
        panel.add(label, BorderLayout.NORTH);
        JPanel scrollPanel = new JPanel(new BorderLayout());
        scrollPanel.add(scrollPane, BorderLayout.NORTH);
        scrollPanel.add(buttonPanel());
        panel.add(scrollPanel);
        
        return panel;
    }

    public void save(ControlStrategy controlStrategy) {
        controlStrategy.setControlMeasureClasses(null);//getControlMeasureClasses());
        Pollutant[] pollutants = {};
        if (tableData != null) {
            pollutants = new Pollutant[tableData.rows().size()];
            for (int i = 0; i < tableData.rows().size(); i++) {
                pollutants[i] = (Pollutant)tableData.element(i);
            }
        } else {
//            pollutants = controlStrategy.getControlMeasures();
        }
//        controlStrategy.setControlMeasures(pollutants);
    }

    public void refresh(ControlStrategy controlStrategy, ControlStrategyResult[] controlStrategyResults) {
        // NOTE Auto-generated method stub

    } 

    public void observe(EditControlStrategyPollutantsTabPresenter presenter) {
        this.presenter = presenter;
    }

    public void add(Pollutant[] pollutants) {
        for (int i = 0; i < pollutants.length; i++) {
            tableData.add(pollutants);
        }
        setupLayout(changeablesList);
    }

    public void notifyStrategyTypeChange(StrategyType strategyType) {
        // NOTE Auto-generated method stub
        
    }

    public void notifyStrategyRun(ControlStrategy controlStrategy) {
        // NOTE Auto-generated method stub
        
    }

    public void run(ControlStrategy controlStrategy) {
        // NOTE Auto-generated method stub
        
    }

    public void setTargetPollutants(Pollutant[] pollutants) {
        // NOTE Auto-generated method stub
        
    }

    public void fireStrategyTypeChanges(StrategyType strategyType) {
        // NOTE Auto-generated method stub
        
    }
}