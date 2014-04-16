package gov.epa.emissions.framework.client.fast.run.tabs;

import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.gui.buttons.CancelButton;
import gov.epa.emissions.commons.gui.buttons.OKButton;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.meta.EmfImageTool;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.fast.FastDataset;
import gov.epa.mims.analysisengine.gui.ScreenUtils;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;

@SuppressWarnings("serial")
public class FastDatasetSelectionDialog extends JDialog implements FastDatasetSelectionView {

    private EmfConsole parent;

    private FastDatasetSelectionPresenter presenter;

    private TextField name;

    private JList datasetList;

    private List<FastDataset> selectedDatasets = new ArrayList<FastDataset>();

    private boolean shouldCreate = false;

    private static String lastNameContains = null;

    public FastDatasetSelectionDialog(EmfConsole parent) {

        super(parent);

        this.parent = parent;

        this.setIconImage(EmfImageTool.createImage("/logo.JPG"));
        this.setModal(true);
        
        this.setTitle("Select FAST Dataset");
    }

    public void display() {
        display(false);
    }

    public void display(boolean selectSingle) {

        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout(5, 5));
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.add(buildTopPanel(), BorderLayout.NORTH);
        panel.add(buildDatasetsPanel(selectSingle), BorderLayout.CENTER);
        panel.add(buttonPanel(), BorderLayout.SOUTH);
        contentPane.add(panel);

        this.pack();
        this.setSize(500, 400);
        this.setLocation(ScreenUtils.getPointToCenter(this));
        this.setVisible(true);
    }

    public void refreshDatasets(List<FastDataset> datasets) {
        datasetList.setListData(datasets.toArray(new FastDataset[0]));
    }

    public List<FastDataset> getDatasets() {
        return selectedDatasets;
    }

    private JPanel buildTopPanel() {

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(buildNameContains());

        return panel;
    }

    private JPanel buildNameContains() {

        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();
        String defaultName = ((lastNameContains == null) ? "" : lastNameContains);
        name = new TextField("Dataset name contains", defaultName, 25);
        name.setEditable(true);
        name.addActionListener(new AbstractAction() {

            public void actionPerformed(ActionEvent e) {
                lastNameContains = name.getText();
                refresh();
            }
        });

        layoutGenerator.addLabelWidgetPair("Dataset name contains:  ", name, panel);
        layoutGenerator.makeCompactGrid(panel, 1, 2, // rows, cols
                25, 10, // initialX, initialY
                5, 5);// xPad, yPad
        return panel;
    }

    private void refresh() {

        try {
            presenter.refreshDatasets(name.getText());
        } catch (EmfException e1) {
            e1.printStackTrace();
        }
    }

    private JPanel buildDatasetsPanel(boolean selectSingle) {

        this.datasetList = new JList();
        if (selectSingle) {
            this.datasetList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        } else {
            this.datasetList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        }

        JScrollPane scrollPane = new JScrollPane(datasetList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setPreferredSize(new Dimension(500, 300));
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.add(scrollPane);
        refresh();

        return panel;
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

                Object[] selectedObjects = datasetList.getSelectedValues();
                List<FastDataset> selectedValues = new ArrayList<FastDataset>();
                for (Object object : selectedObjects) {
                    selectedValues.add((FastDataset) object);
                }

                selectedDatasets.clear();
                if (selectedValues == null || selectedValues.isEmpty()) {
                    JOptionPane
                            .showMessageDialog(parent, "Please choose a dataset", "Error", JOptionPane.ERROR_MESSAGE);
                } else {

                    // get selected datasets
                    for (FastDataset fastDataset : selectedValues) {
                        selectedDatasets.add(fastDataset);
                    }

                    shouldCreate = true;
                    setVisible(false);
                    dispose();
                }
            }
        };
    }

    public void observe(FastDatasetSelectionPresenter presenter) {
        this.presenter = presenter;
    }

    public boolean shouldCreate() {
        return shouldCreate;
    }

    public void clearMessage() {
        // NOTE Auto-generated method stub

    }

}