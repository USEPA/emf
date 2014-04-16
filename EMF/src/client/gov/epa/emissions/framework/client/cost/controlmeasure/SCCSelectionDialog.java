package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.SortFilterSelectionPanel;
import gov.epa.emissions.commons.gui.buttons.CancelButton;
import gov.epa.emissions.commons.gui.buttons.OKButton;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.meta.EmfImageTool;
import gov.epa.emissions.framework.services.cost.controlmeasure.Scc;
import gov.epa.emissions.framework.ui.EmfTableModel;
import gov.epa.emissions.framework.ui.TrackableSortFilterSelectModel;
import gov.epa.mims.analysisengine.gui.ScreenUtils;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class SCCSelectionDialog extends JDialog implements SCCSelectionView {

    private TrackableSortFilterSelectModel selectModel;

    private EmfConsole parent;

    private SCCSelectionPresenter presenter;
    
    private ManageChangeables changeables;

    public SCCSelectionDialog(EmfConsole parent, ManageChangeables changeables) {
        super(parent);
        super.setIconImage(EmfImageTool.createImage("/logo.JPG"));
        
        this.parent = parent;
        this.changeables = changeables;
    }

    public void display(SCCTableData tableData) {
        EmfTableModel tableModel = new EmfTableModel(tableData);
        selectModel = new TrackableSortFilterSelectModel(tableModel);
        changeables.addChangeable(selectModel);
        SortFilterSelectionPanel panel = new SortFilterSelectionPanel(parent, selectModel);

        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(panel, BorderLayout.CENTER);
        contentPane.add(buttonPanel(), BorderLayout.SOUTH);

        setTitle("Select SCCs");
        this.pack();
        this.setSize(900,600);
        this.setLocation(ScreenUtils.getPointToCenter(parent));
        this.setVisible(true);
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
                if (selectModel.selected().size() > 0) {
                    add();
                    setVisible(false);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(parent, 
                       "Please choose some SCCs", 
                       "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
    }

    private void add() {
        List selected = selectModel.selected();
        Scc[] sccs = (Scc[]) selected.toArray(new Scc[0]);
        presenter.doAdd(sccs);

    }

    public void observe(Object presenter) {
        this.presenter = (SCCSelectionPresenter)presenter;
    }

}
