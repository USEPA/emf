package gov.epa.emissions.framework.client.sms;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.gui.buttons.CancelButton;
import gov.epa.emissions.commons.gui.buttons.OKButton;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.meta.EmfImageTool;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.sms.SectorScenario;
import gov.epa.emissions.framework.services.sms.SectorScenarioInventory;
import gov.epa.emissions.framework.ui.ListWidget;
import gov.epa.mims.analysisengine.gui.ScreenUtils;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SpringLayout;

public class SectorScenarioDialog extends JDialog implements SectorScenarioView {

//    private EmfConsole parent;

    private SectorScenarioPresenter presenter;

    private TextField name; 
    
    private TextArea description; 
    
    private TextField abbreviation; 
    
    private ComboBox eecsMappingDataset;

    private ComboBox eecsMappingDatasetVersion;

    private ComboBox sectorMappingDataset;

    private ComboBox sectorMappingDatasetVersion;

    private ComboBox inventoryDataset;

    private ComboBox inventoryDatasetVersion;

    private JList datasetList;
    
    private EmfSession session;

    private ListWidget sectorsList;
    
    public SectorScenarioDialog(EmfConsole parent, EmfSession session) {
        super(parent);
        super.setIconImage(EmfImageTool.createImage("/logo.JPG"));
//        this.parent = parent;
        this.session = session;
        setModal(true);
    }

    public void display() throws EmfException {
        presenter.test();
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout(5, 5));
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.add(buildTopPanel(), BorderLayout.NORTH);
        panel.add(buttonPanel(), BorderLayout.SOUTH);
        contentPane.add(panel);
        setTitle("Sector Scenario Analysis");
        this.pack();
        this.setSize(700, 600);
        this.setLocation(ScreenUtils.getPointToCenter(this));
        this.setVisible(true);
    }

    public void refreshDatasets(EmfDataset[] datasets) {
        datasetList.setListData(datasets);
    }

    private JPanel buildTopPanel() throws EmfException{
        JPanel panel = new JPanel ();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(buildNameContains());
        panel.add(createClassesPanel());
        return panel; 
    }

    private JPanel buildNameContains() throws EmfException{
        JPanel panel = new JPanel(new SpringLayout()); 
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        name = new TextField("Name:", 25);
        layoutGenerator.addLabelWidgetPair("Name: ", name, panel);

        description = new TextArea("Description:", "", 25, 4);
        layoutGenerator.addLabelWidgetPair("Description: ", description, panel);

        abbreviation = new TextField("Abbreviation:", 25);
        layoutGenerator.addLabelWidgetPair("Abbreviation: ", abbreviation, panel);
        
        eecsMappingDataset = new ComboBox("Not selected", presenter.getDatasets(presenter.getDatasetType(DatasetType.EECS_MAPPING)));
//        if (controlStrategy.getCountyDataset() != null) dataset.setSelectedItem(controlStrategy.getCountyDataset());

        eecsMappingDataset.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    fillVersions(eecsMappingDatasetVersion, (EmfDataset)eecsMappingDataset.getSelectedItem());
                } catch (EmfException e1) {
                    // NOTE Auto-generated catch block
                    e1.printStackTrace();
                }
            }
        });

        eecsMappingDatasetVersion =new ComboBox(new Version[0]);      
//        version.setPrototypeDisplayValue(width);
        try {
            fillVersions(eecsMappingDatasetVersion, (EmfDataset)eecsMappingDataset.getSelectedItem());
        } catch (EmfException e1) {
            // NOTE Auto-generated catch block
            e1.printStackTrace();
        }
//        if (controlStrategy.getCountyDataset() != null) version.setSelectedItem(controlStrategy.getCountyDatasetVersion());
     
        layoutGenerator.addLabelWidgetPair("EECS Mapping Dataset:", eecsMappingDataset, panel);
        layoutGenerator.addLabelWidgetPair("EECS Mapping Dataset Version:", eecsMappingDatasetVersion, panel);

        sectorMappingDataset = new ComboBox("Not selected", presenter.getDatasets(presenter.getDatasetType(DatasetType.SECTOR_MAPPING)));
//      if (controlStrategy.getCountyDataset() != null) dataset.setSelectedItem(controlStrategy.getCountyDataset());

        sectorMappingDataset.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                  try {
                      fillVersions(sectorMappingDatasetVersion, (EmfDataset)sectorMappingDataset.getSelectedItem());
                      fillSectorList();
                  } catch (EmfException e1) {
                      // NOTE Auto-generated catch block
                      e1.printStackTrace();
                  }
              }
        });

        sectorMappingDatasetVersion = new ComboBox(new Version[0]);      
        sectorMappingDatasetVersion.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                  try {
                    fillSectorList();
                } catch (EmfException e1) {
                    // NOTE Auto-generated catch block
                    e1.printStackTrace();
                }
              }
        });
        try {
            fillVersions(sectorMappingDatasetVersion, (EmfDataset)sectorMappingDataset.getSelectedItem());
        } catch (EmfException e1) {
            // NOTE Auto-generated catch block
            e1.printStackTrace();
        }

   
      layoutGenerator.addLabelWidgetPair("Sector Mapping Dataset:", sectorMappingDataset, panel);
      layoutGenerator.addLabelWidgetPair("Sector Mapping Dataset Version:", sectorMappingDatasetVersion, panel);

      inventoryDataset = new ComboBox("Not selected", presenter.getDatasets(presenter.getDatasetType(DatasetType.ORL_POINT_NATA)));
//    if (controlStrategy.getCountyDataset() != null) dataset.setSelectedItem(controlStrategy.getCountyDataset());

      inventoryDataset.addActionListener(new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
            try {
                fillVersions(inventoryDatasetVersion, (EmfDataset)inventoryDataset.getSelectedItem());
            } catch (EmfException e1) {
                // NOTE Auto-generated catch block
                e1.printStackTrace();
            }
        }
    });

      inventoryDatasetVersion = new ComboBox(new Version[0]);      
//    version.setPrototypeDisplayValue(width);
    try {
        fillVersions(inventoryDatasetVersion, (EmfDataset)inventoryDataset.getSelectedItem());
    } catch (EmfException e1) {
        // NOTE Auto-generated catch block
        e1.printStackTrace();
    }
//    if (controlStrategy.getCountyDataset() != null) version.setSelectedItem(controlStrategy.getCountyDatasetVersion());
    layoutGenerator.addLabelWidgetPair("Inventory:", inventoryDataset, panel);
    layoutGenerator.addLabelWidgetPair("Inventory Version:", inventoryDatasetVersion, panel);

      
      layoutGenerator.makeCompactGrid(panel, 9, 2, // rows, cols
                25, 10, // initialX, initialY
                5, 5);// xPad, yPad
        return panel; 
    }
    
    private JPanel createClassesPanel() throws EmfException {
        // build list widget
        
        String[] sectors = new String[] {};
        if (sectorMappingDataset.getSelectedItem() != null) {
            sectors = presenter.getDistinctSectorListFromDataset(((EmfDataset)sectorMappingDataset.getSelectedItem()).getId(), ((Version)sectorMappingDatasetVersion.getSelectedItem()).getVersion());
        }
        this.sectorsList = new ListWidget(sectors, new Object[] {});
        this.sectorsList.setToolTipText("Use Ctrl or Shift to select multiple classes");

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 100, 0, 300));
        JLabel label = new JLabel("Sectors to Include:");
        JScrollPane scrollPane = new JScrollPane(sectorsList);
        scrollPane.setPreferredSize(new Dimension(20, 100));
        panel.add(label, BorderLayout.NORTH);
        JPanel scrollPanel = new JPanel(new BorderLayout());
        scrollPanel.add(scrollPane, BorderLayout.NORTH);
        panel.add(scrollPanel);
        return panel;
    }

    private void fillVersions(ComboBox version, EmfDataset dataset) throws EmfException{
        version.setEnabled(true);

        if (dataset != null && dataset.getName().equals("Not selected")) dataset = null;
        Version[] versions = presenter.getVersions(dataset);
        version.removeAllItems();
        version.setModel(new DefaultComboBoxModel(versions));
        version.revalidate();
        if (versions.length > 0)
            version.setSelectedIndex(getDefaultVersionIndex(versions, dataset));

    }
    
    private void fillSectorList() throws EmfException {
        sectorsList.removeAllElements();
        if (sectorMappingDataset.getSelectedItem() == null ) 
            return;
        for (String sector : presenter.getDistinctSectorListFromDataset(((EmfDataset)sectorMappingDataset.getSelectedItem()).getId(), ((Version)sectorMappingDatasetVersion.getSelectedItem()).getVersion())) {
            sectorsList.addElement(sector);
        }
    }
    
    private int getDefaultVersionIndex(Version[] versions, EmfDataset dataset) {
        int defaultversion = dataset.getDefaultVersion();

        for (int i = 0; i < versions.length; i++)
            if (defaultversion == versions[i].getVersion())
                return i;

        return 0;
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
                
                SectorScenario sectorScenario = new SectorScenario();
                sectorScenario.setName(name.getText());
                sectorScenario.setDescription(description.getText());
                sectorScenario.setAbbreviation(abbreviation.getText());
                sectorScenario.setCreator(session.user());
                sectorScenario.setRunStatus("Not started");
                sectorScenario.setLastModifiedDate(new Date());
                sectorScenario.setEecsMapppingDataset((EmfDataset)eecsMappingDataset.getSelectedItem());
                sectorScenario.setEecsMapppingDatasetVersion(((Version)eecsMappingDatasetVersion.getSelectedItem()).getVersion());
                sectorScenario.setSectorMapppingDataset((EmfDataset)sectorMappingDataset.getSelectedItem());
                sectorScenario.setSectorMapppingDatasetVersion(((Version)sectorMappingDatasetVersion.getSelectedItem()).getVersion());

//                SectorScenarioInventory sectorScenarioInventory = new SectorScenarioInventory((EmfDataset)inventoryDataset.getSelectedItem(), (Integer)inventoryDatasetVersion.getSelectedItem());
                sectorScenario.setInventories(new SectorScenarioInventory[] { new SectorScenarioInventory((EmfDataset)inventoryDataset.getSelectedItem(), ((Version)inventoryDatasetVersion.getSelectedItem()).getVersion()) });
                sectorScenario.setSectorMapppingDatasetVersion(0);
                sectorScenario.setSectors(Arrays.asList(sectorsList.getSelectedValues()).toArray(new String[0]));

                try {
                    int id = presenter.addSectorScenario(sectorScenario);
                    sectorScenario = presenter.getSectorScenario(session.user(), id);
                    presenter.runSectorScenario(id);
                } catch (EmfException e1) {
                    // NOTE Auto-generated catch block
                    e1.printStackTrace();
                }
                
                setVisible(false);
                dispose();
            }
        };
    }

    public void observe(SectorScenarioPresenter presenter) {
        this.presenter = presenter;
    }
    
    public void clearMessage() {
        // NOTE Auto-generated method stub
        
    }


}