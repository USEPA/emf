package gov.epa.emissions.framework.client.data.dataset;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.gui.buttons.CancelButton;
import gov.epa.emissions.commons.gui.buttons.OKButton;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.meta.EmfImageTool;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.mims.analysisengine.gui.ScreenUtils;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;

public class InputDatasetSelectionDialog extends JDialog implements InputDatasetSelectionView {

    private EmfConsole parent;

    private InputDatasetSelectionPresenter presenter;

    private TextField name; 
    
    private ComboBox datasetTypeCombo;

    private JList datasetList;
    
    private EmfDataset[] datasets = new EmfDataset[] {};
    
    private boolean shouldCreate = false;  
    
    private static String lastNameContains = null;
    
    private static DatasetType lastDatasetType = null;
    
    public InputDatasetSelectionDialog(EmfConsole parent) {
        super(parent);
        super.setIconImage(EmfImageTool.createImage("/logo.JPG"));
        this.parent = parent;
        setModal(true);
    }

    public void display(DatasetType[] datasetTypes) {
        display(datasetTypes, null, false); //
    }
    
    public void display(DatasetType[] datasetTypes, DatasetType defaultType, boolean selectSingle) {
//        if ((defaultType != null) && (lastDatasetType != null))
//               System.out.println("defaultType.getName()="+defaultType.getName()+","+
//                       defaultType.getName().equals(lastDatasetType.getName()));
//        else
//        {
//            System.out.println("one or both is null");
//        }
        if ((defaultType != null) && (lastDatasetType != null) && 
           !defaultType.getName().equals(lastDatasetType.getName()))
        {
            lastNameContains = null;
            lastDatasetType = null;
        }
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout(5, 5));
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.add(buildTopPanel(datasetTypes, defaultType), BorderLayout.NORTH);
        panel.add(buildDatasetsPanel(selectSingle), BorderLayout.CENTER);
        panel.add(buttonPanel(), BorderLayout.SOUTH);
        contentPane.add(panel);
        if (datasetTypes.length == 1)
        {
            setTitle("Select "+datasetTypes[0].getName()+" Datasets");           
        }
        else
        {
           setTitle("Select Datasets");
        }   
        this.pack();
        this.setSize(500, 400);
        this.setLocation(ScreenUtils.getPointToCenter(this));
        this.setVisible(true);
    }

    public void refreshDatasets(EmfDataset[] datasets) {
        datasetList.setListData(datasets);
    }

    public EmfDataset[] getDatasets() {
        return datasets;
    }
    
    private JPanel buildTopPanel(DatasetType[] datasetTypes, DatasetType defaultType){
        JPanel panel = new JPanel ();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(buildNameContains());
        panel.add(buildDatasetTypeCombo(datasetTypes, defaultType));
        return panel; 
    }

    private JPanel buildDatasetTypeCombo(DatasetType[] datasetTypes, DatasetType defaultType) {
        JPanel panel = new JPanel(new BorderLayout());
        datasetTypeCombo = new ComboBox("Choose a dataset type", datasetTypes);
        if (defaultType != null )
            datasetTypeCombo.setSelectedItem(defaultType); //setSelectedIndex(getIndex(defaultType, datasetTypes));

        datasetTypeCombo.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                lastDatasetType = (DatasetType)datasetTypeCombo.getSelectedItem();
                refresh();
            }
        });

        panel.add(datasetTypeCombo, BorderLayout.CENTER);
        panel.setBorder(BorderFactory.createEmptyBorder(1, 20, 5, 20));
        return panel;
    }
    
    private JPanel buildNameContains(){
        JPanel panel = new JPanel(new SpringLayout()); 
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();
        String defaultName = ((lastNameContains == null) ? "" : lastNameContains);
        name= new TextField("Dataset name contains", defaultName, 25);
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
    
    private void refresh(){
        try {
            if (datasetTypeCombo.getSelectedItem() == null ){
                refreshDatasets(new EmfDataset[] {});
                return; 
            }
            presenter.refreshDatasets((DatasetType) datasetTypeCombo.getSelectedItem(), name.getText());
        } catch (EmfException e1) {
            e1.printStackTrace();
        }
    }
    
    private JPanel buildDatasetsPanel(boolean selectSingle) {
        datasetList = new JList();
        if (selectSingle)
            datasetList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        else
            datasetList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
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
                if (datasetList.getSelectedValues() == null || datasetList.getSelectedValues().length == 0) { 
                    datasets = new EmfDataset[]{}; 
                    JOptionPane.showMessageDialog(parent, 
                            "Please choose some datasets", 
                            "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    // get selected datasets
                    List<EmfDataset> list = new ArrayList<EmfDataset>(datasetList.getSelectedValues().length);
                    for (int i = 0; i < datasetList.getSelectedValues().length; i++)
                        list.add((EmfDataset) datasetList.getSelectedValues()[i]);
                    datasets = list.toArray(new EmfDataset[0]);
                    shouldCreate = true; 
                    setVisible(false);
                    dispose();
                    lastDatasetType = (DatasetType)datasetTypeCombo.getSelectedItem();
                }
            }
        };
    }

    public void observe(InputDatasetSelectionPresenter presenter) {
        this.presenter = presenter;
    }
    
    public boolean shouldCreate() {
        return shouldCreate;
    }

    public void clearMessage() {
        // NOTE Auto-generated method stub
        
    }


}