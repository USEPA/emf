package gov.epa.emissions.framework.client.cost.controlstrategy.groups;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.util.List;

import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.SortFilterSelectionPanel;
import gov.epa.emissions.commons.gui.buttons.AddButton;
import gov.epa.emissions.commons.gui.buttons.CancelButton;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.cost.controlstrategy.ControlStrategiesTableData;
import gov.epa.emissions.framework.client.meta.EmfImageTool;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.ui.EmfTableModel;
import gov.epa.emissions.framework.ui.TrackableSortFilterSelectModel;
import gov.epa.mims.analysisengine.gui.ScreenUtils;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JPanel;

public class ControlStrategySelectionDialog extends JDialog implements ControlStrategySelectionView {

    private TrackableSortFilterSelectModel selectModel;

    private EmfConsole parent;
    
    private ControlStrategySelectionPresenter presenter;
    
    private ManageChangeables changeables;

    public ControlStrategySelectionDialog(EmfConsole parent, ManageChangeables changeables) {
        super(parent);
        super.setIconImage(EmfImageTool.createImage("/logo.JPG"));
        setModal(true);
        
        this.parent = parent;
        this.changeables = changeables;
    }
    
    public void display(ControlStrategiesTableData tableData) {
        EmfTableModel tableModel = new EmfTableModel(tableData);
        selectModel = new TrackableSortFilterSelectModel(tableModel);
        changeables.addChangeable(selectModel);
        SortFilterSelectionPanel panel = new SortFilterSelectionPanel(parent, selectModel);

        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(panel, BorderLayout.CENTER);
        contentPane.add(buttonPanel(), BorderLayout.SOUTH);

        setTitle("Select Control Strategies");
        this.pack();
        this.setSize(600,400);
        this.setLocation(ScreenUtils.getPointToCenter(parent));
        this.setVisible(true);
    }

    private JPanel buttonPanel() {
        JPanel panel = new JPanel();
        panel.add(new AddButton("Add Selected", addAction()));
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

    private Action addAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                add();
                setVisible(false);
                dispose();
            }
        };
    }

    private void add() {
        List selected = selectModel.selected();
        ControlStrategy[] strategies = (ControlStrategy[]) selected.toArray(new ControlStrategy[0]);
        presenter.doAdd(strategies);

    }

    public void observe(ControlStrategySelectionPresenter presenter) {
        this.presenter = presenter;
    }

}
