package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.buttons.OKButton;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.meta.EmfImageTool;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.mims.analysisengine.gui.ScreenUtils;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

public class DatasetChooserDialog extends JDialog {

    private JList list;

    private EmfDataset selectedDataset;

    DatasetChooserDialog(DatasetType datasetType, EmfSession session, EmfConsole console, JComponent parent)
            throws EmfException {
        super(console);
        super.setIconImage(EmfImageTool.createImage("/logo.JPG"));
        
        JPanel panel = datasetsPanel(datasetType, session);
        JPanel buttonPanel = buttonPanel();

        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout(5, 5));
        contentPane.add(panel);
        contentPane.add(buttonPanel, BorderLayout.SOUTH);
        setTitle("Choose a dataset ("+datasetType.getName()+")");
        pack();
        setLocation(ScreenUtils.getPointToCenter(parent));
        setModal(true);
    }

    public EmfDataset dataset() {
        return selectedDataset;
    }

    private JPanel buttonPanel() {
        Button ok = new OKButton(selectDatasetset());
        JPanel panel = new JPanel();
        panel.add(ok);
        return panel;
    }

    private Action selectDatasetset() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                selectedDataset = (EmfDataset) list.getSelectedValue();
                DatasetChooserDialog.this.setVisible(false);
                DatasetChooserDialog.this.dispose();
            }
        };
    }

    private JPanel datasetsPanel(DatasetType datasetType, EmfSession session) throws EmfException {
        EmfDataset[] datasets = session.dataService().getDatasets(datasetType);
        list = new JList(datasets);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(list, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setPreferredSize(new Dimension(500, 300));
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.add(scrollPane);
        return panel;
    }

}
