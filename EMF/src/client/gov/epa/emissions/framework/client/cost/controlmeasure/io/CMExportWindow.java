package gov.epa.emissions.framework.client.cost.controlmeasure.io;

import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.buttons.BrowseButton;
import gov.epa.emissions.commons.gui.buttons.ExportButton;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.cost.controlmeasure.ControlMeasureTableData;
import gov.epa.emissions.framework.client.cost.controlmeasure.LightControlMeasureTableData;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.EmfFileInfo;
import gov.epa.emissions.framework.services.basic.EmfFileSystemView;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.ui.EmfFileChooser;
import gov.epa.emissions.framework.ui.ImageResources;
import gov.epa.emissions.framework.ui.SelectableSortFilterWrapper;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;
import gov.epa.mims.analysisengine.table.sort.SortCriteria;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.border.EtchedBorder;

public class CMExportWindow extends DisposableInteralFrame implements CMExportView {

    private ControlMeasure[] controlMeasures;

    private SingleLineMessagePanel messagePanel;

    private JTextField folder;

    private CMExportPresenter presenter;

    private JCheckBox overwrite;

    private JTextField prefix;
    
    private JButton exportButton;
    
    private EmfSession session;
    
    private EmfConsole parentConsole;
    
    private JRadioButton controlMeasureRadioButton;
    
    private JRadioButton sectorRadioButton;
    
    private ButtonGroup radioButtonGroup;
    
    private ControlMeasureTableData tableData;
    
    private SelectableSortFilterWrapper table;
    
//    private JComboBox sectorComboBox;
    private JList sectorListBox;
    
    private Sector[] allSectors;
    
    private boolean bySector;
    
    Sector[] sectors;
    

    public CMExportWindow(ControlMeasure[] controlMeasures, DesktopManager desktopManager, int totalMeasuers, EmfSession session, EmfConsole parentConsole, boolean bySector) {
        super(title(controlMeasures, totalMeasuers), desktopManager);
        super.setName("cmExportWindow:" + hashCode());

        this.controlMeasures = controlMeasures;
        this.session = session;
        this.parentConsole = parentConsole;
        this.bySector = bySector;

//        this.getContentPane().add(createLayout());
//        this.pack();
    }
    
    @Override
    public void display(){
      this.getContentPane().add(createLayout());
      this.pack();
      desktopManager.openWindow(this);
    }

    private static String title(ControlMeasure[] controlMeasures, int total) {
        int num = controlMeasures.length;
        StringBuffer buf = new StringBuffer("Exporting " + num + " of the " + total + " Control Measures");

        return buf.toString();
    }

    public void observe(CMExportPresenter presenter) {
        this.presenter = presenter;
//        try {
//            for (Sector sector : presenter.getDistinctControlMeasureSectors()) {
//                System.out.println(sector.getId() + " " + sector.getName());
//            }
//            for (ControlMeasure sector : presenter.getControlMeasureBySector(new int[] {1,2})) {
//                System.out.println(sector.getId() + " " + sector.getName());
//            }
//            for (ControlMeasure sector : presenter.getControlMeasureBySector(new int[] {})) {
//                System.out.println(sector.getId() + " " + sector.getName());
//            }
//        } catch (EmfException e) {
//            // NOTE Auto-generated catch block
//            e.printStackTrace();
//        }


    }

    private JPanel createLayout() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        messagePanel = new SingleLineMessagePanel();
        panel.add(messagePanel);
        panel.add(createTopPanel());
        panel.add(createExportPanel());
        panel.add(createButtonsPanel());
        

        return panel;
    }
    
    private JPanel createTopPanel()
    {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        controlMeasureRadioButton = new JRadioButton("Control Measures");
        sectorRadioButton = new JRadioButton("Sectors");        
        radioButtonGroup = new ButtonGroup();
        radioButtonGroup.add( this.controlMeasureRadioButton);
        radioButtonGroup.add( this.sectorRadioButton);
        if ( this.bySector){
            sectorRadioButton.setSelected(true);
            controlMeasureRadioButton.setSelected( false);
        } else {
            sectorRadioButton.setSelected(false);
            controlMeasureRadioButton.setSelected( true);
        }
        controlMeasureRadioButton.setAction(
          new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                checkBySector();
            }});
        sectorRadioButton.setAction(
          new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                checkBySector();
            }});
        
        panel.add( createControlMeasurePanel());
        panel.add( createSectorPanel());
        panel.setBorder( BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        
        return panel;
    }
    private void checkBySector(){
        if ( controlMeasureRadioButton.isSelected()){
            this.sectorListBox.setEnabled(false);
            this.bySector = false;
        } else {
            this.sectorListBox.setEnabled(true);
            this.bySector = true;
        }
    }
    
    private JPanel createControlMeasurePanel(){
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Export By Measure:"));
        
        panel.add( this.controlMeasureRadioButton);
        
        tableData = new LightControlMeasureTableData(controlMeasures, null, null, null);        
        JPanel tablePanel = new JPanel();
        tablePanel.setLayout(new BorderLayout());
        table = new SelectableSortFilterWrapper(parentConsole, tableData, sortCriteria());
        tablePanel.add(table); 
        
        panel.add( tablePanel);
        
//        panel.setBorder( BorderFactory.createEmptyBorder(5,5,5,10));
        
        return panel;        
    }
    
    private SortCriteria sortCriteria() {
        String[] columnNames = { "Name" };
        return new SortCriteria(columnNames, new boolean[] { true }, new boolean[] { true });
    }
    
    private JPanel createSectorPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Export By Sector:"));
        
        panel.add(this.sectorRadioButton);
        
        try {
            this.sectors = presenter.getDistinctControlMeasureSectors();
            List<Sector> sectorList = new ArrayList<Sector>();
            sectorList.add(new Sector("All", "All"));
            sectorList.addAll( Arrays.asList( sectors));
            allSectors = (Sector[]) sectorList.toArray(new Sector[0]);
            
//            List<String> testStringList = new ArrayList<String>();
//            testStringList.add( "Item1");
//            testStringList.add( "Item2");

//            sectorComboBox = new ComboBox("Select Sector", allSectors);
////            sectorComboBox = new ComboBox(testStringList.toArray());
//            sectorComboBox.setEnabled( true);
//            sectorComboBox.setSelectedIndex(0);
//            sectorComboBox.addActionListener(new AbstractAction() {
//                public void actionPerformed(ActionEvent e) {
//                    // TODO
//                }
//            });
            
            List<String> sectorStrList = new ArrayList<String>();
            
            sectorListBox = new JList( allSectors);
            sectorListBox.setVisibleRowCount(5);
            JScrollPane scrollPane = new JScrollPane( sectorListBox);
//            double oldH = scrollPane.getViewportBorderBounds().getHeight();
//            double oldW = scrollPane.getViewportBorderBounds().getWidth();
//            scrollPane.getViewportBorderBounds().resize((int)oldW, 30);

            panel.add(scrollPane); 
            //panel.add(sectorComboBox);
//            panel.setBorder( BorderFactory.createEmptyBorder(5,5,5,10));
            
        } 
        catch (EmfException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
            messagePanel.setMessage(e.getMessage());
            
        }
        
        return panel;
    }    

    private JPanel createExportPanel() {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        // folder
        folder = new JTextField(30);
        folder.setName("folder");
        Button button = new BrowseButton(new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                selectFolder();
            }
        });
        Icon icon = new ImageResources().open("Export a Control Measure");
        button.setIcon(icon);
        
        JPanel folderPanel = new JPanel(new BorderLayout(2,0));
        folderPanel.add(folder, BorderLayout.LINE_START);
        folderPanel.add(button, BorderLayout.LINE_END);
        layoutGenerator.addLabelWidgetPair("Folder", folderPanel, panel);

        // purpose
        prefix = new JTextField(30);
        prefix.setName("prefix");
        layoutGenerator.addLabelWidgetPair("Prefix", prefix, panel);

        // overwrite
        JPanel overwritePanel = new JPanel(new BorderLayout());
        overwrite = new JCheckBox("Overwrite files if they exist?", false);
        overwrite.setEnabled(true);
        overwrite.setName("overwrite");
        overwritePanel.add(overwrite, BorderLayout.LINE_START);

        panel.add(new JPanel());// filler
        panel.add(overwritePanel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 3, 2, // rows, cols
                5, 5, // initialX, initialY
                5, 5);// xPad, yPad
        
        panel.setBorder( BorderFactory.createEtchedBorder(EtchedBorder.RAISED));

        return panel;
    }

    private JPanel createButtonsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel container = new JPanel();
        FlowLayout layout = new FlowLayout();
        layout.setHgap(20);
        layout.setVgap(25);
        container.setLayout(layout);

        exportButton = new ExportButton(new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                clearMessagePanel();
                doExport();
            }
        });
        container.add(exportButton);
        getRootPane().setDefaultButton(exportButton);

        JButton done = new Button("Close", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                presenter.notifyDone();
            }
        });
        container.add(done);

        panel.add(container, BorderLayout.EAST);
        
        panel.setBorder( BorderFactory.createEtchedBorder(EtchedBorder.RAISED));

        return panel;
    }

    private void refresh() {
        super.validate();
    }
    
    private int[] getSectorIDs(){
        int [] IDs = null;
        if ( this.bySector) {
//            if ( sectorComboBox.getSelectedIndex() == 0){
//                messagePanel.setError("Please select Sector(s).");
//            } else if (sectorComboBox.getSelectedIndex() == 1) {
//                IDs = new int[ this.sectors.length];
//                for ( int i = 0; i<this.sectors.length; i++) {
//                    IDs[i] = this.sectors[i].getId();
//                }
//            } else {
//                IDs = new int[ 1];
//                IDs[0] = this.sectors[ sectorComboBox.getSelectedIndex()-2].getId();
//                return IDs;
//            }
            int [] inx = sectorListBox.getSelectedIndices();
            if ( inx.length == 0){
                messagePanel.setError("Please select Sector(s).");
            } else {
                IDs = new int[ inx.length];
                for ( int i = 0; i < inx.length; i++) {
                    //this means the all item was selected...
                    if (inx[i] == 0)
                        return new int[] {};
                    IDs[i] = this.sectors[inx[i] - 1].getId();
                }
            }            
        } else {
            messagePanel.setError("Export by Control Measures, not by Sector.");
        }
        return IDs;
    }

    private void doExport() {
        try {
            validateFolder(folder.getText());
            
            if (!overwrite.isSelected()) {
                if ( !this.bySector) {
                    presenter.doExportWithoutOverwrite(getControlMeasureIds(controlMeasures), folder.getText(), prefix.getText());
                }
                else {
                    int [] sectorIDs = this.getSectorIDs();
                    if ( sectorIDs != null){
                        ControlMeasure[] controlMeasures = presenter.getControlMeasureBySector(sectorIDs);
                        presenter.doExportWithoutOverwrite(getControlMeasureIds(controlMeasures), folder.getText(), prefix.getText());
                    } else {
                        return;
                    }
                }
            } else {
                if ( !this.bySector) {
                    presenter.doExportWithOverwrite(getControlMeasureIds(controlMeasures), folder.getText(), prefix.getText());
                } else {
                    int [] sectorIDs = this.getSectorIDs();
                    if ( sectorIDs != null){
                        ControlMeasure[] controlMeasures = presenter.getControlMeasureBySector(sectorIDs);
                        presenter.doExportWithOverwrite(getControlMeasureIds(controlMeasures), folder.getText(), prefix.getText());
                    } else {
                        return;
                    }
                }
            }
            messagePanel.setMessage("Started export. Please monitor the Status window "
                    + "to track your Export request.");
            exportButton.setEnabled(false);
        } catch (EmfException e) {
            exportButton.setEnabled(true);
            messagePanel.setError(e.getMessage());
        }
    }

    private int[] getControlMeasureIds(ControlMeasure[] cms) {
        int[] ids = new int[cms.length];
        
        for (int i = 0; i < cms.length; i++)
            ids[i] = cms[i].getId();
        
        return ids;
    }

    private void clearMessagePanel() {
        messagePanel.clear();
        refresh();
    }

    public void setMostRecentUsedFolder(String mostRecentUsedFolder) {
        if (mostRecentUsedFolder != null)
            folder.setText(mostRecentUsedFolder);
    }

    private void selectFolder() {
        EmfFileInfo initDir = new EmfFileInfo(folder.getText(), true, true);
        
        EmfFileChooser chooser = new EmfFileChooser(initDir, new EmfFileSystemView(session.dataCommonsService()));
        chooser.setTitle("Select a folder to hold the exported control measure files");
        int option = chooser.showDialog(parentConsole, "Select a folder");

        EmfFileInfo file = (option == EmfFileChooser.APPROVE_OPTION) ? chooser.getSelectedDir() : null;
        if (file == null)
            return;

        if (file.isDirectory()) {
            folder.setText(file.getAbsolutePath());
        }
    }
    
    private void validateFolder(String folder) throws EmfException {
        if (folder == null || folder.trim().isEmpty())
            throw new EmfException("Please select a valid folder to export.");
        
        if (folder.contains("/home/") || folder.endsWith("/home"))
            throw new EmfException("Export data into user's home directory is not allowed.");
    }
    
    class RadioListener implements ActionListener { 
        public void actionPerformed(ActionEvent e) {
            //jlbPicture.setIcon(new ImageIcon(""+e.getActionCommand() 
            //                              + ".jpg"));
        }
    }

}
