package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.gui.buttons.BrowseButton;
import gov.epa.emissions.commons.gui.buttons.ExportButton;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.data.dataset.AddRemoveDatasetVersionWidget;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.EmfFileInfo;
import gov.epa.emissions.framework.services.basic.EmfFileSystemView;
import gov.epa.emissions.framework.services.data.DataCommonsService;
import gov.epa.emissions.framework.services.data.DatasetVersion;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.ui.EmfFileChooser;
import gov.epa.emissions.framework.ui.ImageResources;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpringLayout;
import javax.swing.ToolTipManager;

public class ExportWindow extends DisposableInteralFrame implements ExportView {

    private EmfDataset[] datasets;

    private SingleLineMessagePanel messagePanel;

    private JTextField folder;
    
    private JTextField prefix;

    private ExportPresenter presenter;

    private JCheckBox overwrite;

    private TextArea purpose;
    private TextArea colOrder;
    private TextArea rowFilter;   
    private String colOrders;
    private String rowFilters;
    private ComboBox version;

    private AddRemoveDatasetVersionWidget filterDatasetVersionWidget;

    private TextArea filterDatasetJoinCondition;

    private JButton exportButton;

    private EmfConsole parentConsole;

    private DataCommonsService service;
    
    private EmfSession session;

    private JCheckBox download;

    private JButton browseFileButton;

    public ExportWindow(EmfDataset[] datasets, DesktopManager desktopManager, EmfConsole parentConsole,
            EmfSession session, String colOrders, String rowFilters) {
        super(title(datasets), desktopManager);
        super.setName("exportWindow:" + hashCode());

        this.datasets = datasets;
        this.parentConsole = parentConsole;
        this.service = session.dataCommonsService();
        this.session = session;
        this.colOrders = colOrders;
        this.rowFilters = rowFilters;

        this.getContentPane().add(createLayout());
        this.pack();
    }

    private static String title(EmfDataset[] datasets) {
        StringBuffer buf = new StringBuffer("Export: ");
        for (int i = 0; i < datasets.length; i++) {
            buf.append(datasets[i].getName());
            if (i + 1 < datasets.length)
                buf.append(", ");
        }

        return buf.toString();
    }

    public void observe(ExportPresenter presenter) {
        this.presenter = presenter;
    }

    private JPanel createLayout() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        //this will force tooltips to stay open for 5000 milliseconds
        ToolTipManager.sharedInstance().setDismissDelay(5000);
        
        messagePanel = new SingleLineMessagePanel();
        panel.add(messagePanel);
        panel.add(createExportPanel());
        panel.add(createButtonsPanel());
        return panel;
    }

    private JPanel createExportPanel() {
        JPanel panel = new JPanel(new SpringLayout());
        int width = 40;
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        // datasets
        TextArea datasetNames = new TextArea("datasets", getDatasetsLabel(datasets), width+10, 6);
        datasetNames.setEditable(false);
        datasetNames.setToolTipText("Dataset(s) that will be exported.");
        JScrollPane dsArea = new JScrollPane(datasetNames, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        layoutGenerator.addLabelWidgetPair("Datasets  ", dsArea, panel);

        // overwrite
        JPanel downloadPanel = new JPanel(new BorderLayout());
        download = new JCheckBox("Download files to local machine?", false);
        download.setEnabled(true);
        download.setName("download");
        download.setToolTipText("<html>Download the existing dataset file to your local machine.</html>");
        download.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {

                Object source = e.getItemSelectable();

//                if (source == chinButton) {
//                    //...make a note of it...
//                } else if (source == glassesButton) {
//                    //...make a note of it...
//                } else if (source == hairButton) {
//                    //...make a note of it...
//                } else if (source == teethButton) {
//                    //...make a note of it...
//                }

                if (e.getStateChange() == ItemEvent.DESELECTED) {
                    folder.setEnabled(true);
                    browseFileButton.setEnabled(true);
                } else {
                    folder.setEnabled(false);
                    browseFileButton.setEnabled(false);
                }
            }            
        });
        //overwrite.setVisible(false);
        downloadPanel.add(download, BorderLayout.LINE_START);
        //overwritePanel.setVisible(false);
        layoutGenerator.addLabelWidgetPair(" ", downloadPanel, panel);

        // folder
        JPanel chooser = new JPanel(new BorderLayout(10, 10));
        folder = new TextField("folder", width);
        folder.setToolTipText("Folder to store exported dataset(s).");
        chooser.add(folder);
        browseFileButton = browseFileButton();
        chooser.add(browseFileButton, BorderLayout.EAST);
        layoutGenerator.addLabelWidgetPair("Folder  ",chooser, panel);
        
        // overwrite
        JPanel overwritePanel = new JPanel(new BorderLayout());
        overwrite = new JCheckBox("Overwrite files if they exist?", false);
        overwrite.setEnabled(true);
        overwrite.setName("overwrite");
        overwrite.setToolTipText("<html>Overwrite the existing export dataset file.</html>");
        //overwrite.setVisible(false);
        overwritePanel.add(overwrite, BorderLayout.LINE_START);
        //overwritePanel.setVisible(false);
        layoutGenerator.addLabelWidgetPair(" ",overwritePanel, panel);

        JPanel prefixPanel = new JPanel(new BorderLayout(10, 10));
        prefix = new TextField("prefix", width);
        prefix.setToolTipText("Prefix to be added to the names of the exported files");
        prefixPanel.add(prefix);
        layoutGenerator.addLabelWidgetPair("File Name Prefix",prefixPanel, panel);
        
        //Sort Order
        colOrder = new TextArea("colOrder", colOrders, width+10, 2);
        colOrder.setToolTipText(colOrder.getText());
        JScrollPane colArea = new JScrollPane(colOrder, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        //layoutGenerator.addLabelWidgetPair("Col filter", colArea, panel);
        
        //version
        if (datasets.length == 1) {
            version = new ComboBox();
            version.setPreferredSize(new Dimension(445, 20));
            version.setToolTipText("Version of dataset.");
            
            layoutGenerator.addLabelWidgetPair("Version  ", version, panel);
        }
        // Row Filter
        rowFilter = new TextArea("rowFilter", rowFilters, width+10, 2);
        rowFilter.setToolTipText("<html>SQL WHERE clause used to filter dataset.<br/>For example to filter on a certain state,<br/>substring(fips,1,2) = '37'<br/>or<br/>fips like '37%'<html>");
        JScrollPane rowArea = new JScrollPane(rowFilter, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        layoutGenerator.addLabelWidgetPair("Row Filter ", rowArea, panel);
        
        //filter dataset version widget
        try {
            layoutGenerator.addLabelWidgetPair("Filter Dataset ", createFilterDatasetVersion(), panel);
        } catch (EmfException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }

        // Filter Dataset Join Condition
        //NOTE:  join conidition items must be delimited by a new line character
        //where left hand expression is for the dataset being exported and the right hand expressions
        //is for the filter dataset
        //i.e.,
        //fips=fips
        //scc=scc
        //plantid=plantid
        
        filterDatasetJoinCondition = new TextArea("rowFilter", rowFilters, width+10, 2);
        filterDatasetJoinCondition.setToolTipText("<html>Filter Dataset Join Condition <br/>left hand side is for dataset being exported<br/>right hand side is for filter dataset<br/>i.e.,<br/>fips=fips<br/>scc=scc<br/>plantid=plantid<br/>or<br/>scc=scc_code<br/>substring(fips,1,2)=state_cd<br/>poll=poll_code</html>");
        JScrollPane filterDatasetJoinConditionArea = new JScrollPane(filterDatasetJoinCondition, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        layoutGenerator.addLabelWidgetPair("Filter Dataset Join Condition ", filterDatasetJoinConditionArea, panel);
        
        // purpose
        //JPanel purposePanel = new JPanel(new BorderLayout(4,10));
        purpose = new TextArea("purpose", "", width+10, 4);
        purpose.setToolTipText("<html>Records the purpose of the dataset export.</html>");
        JScrollPane purposeArea = new JScrollPane(purpose, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        layoutGenerator.addLabelWidgetPair("Purpose  ", purposeArea, panel);
        //purposePanel.add(purposeArea);
 
        if (datasets.length == 1)    
            layoutGenerator.makeCompactGrid(panel, 10, 2, // rows, cols
                    10, 10, // initialX, initialY
                    5, 10);// xPad, yPad
        else
            layoutGenerator.makeCompactGrid(panel, 9, 2, // rows, cols
                    10, 10, // initialX, initialY
                    5, 10);// xPad, yPad

        return panel;
    }
    
    private JPanel createFilterDatasetVersion() throws EmfException {
        
        filterDatasetVersionWidget = new AddRemoveDatasetVersionWidget(true, 1, this, parentConsole, session);
        filterDatasetVersionWidget.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//        filterDatasetVersionWidget.setModelSize(1);
        filterDatasetVersionWidget.setPreferredSize(new Dimension(550, 80));
        filterDatasetVersionWidget.setToolTipText("<html>Dataset and version to use as filter.  The \"Filter Dataset Join Condition\" needs to be specified when using a dataset filter.<html>");

//        List<DatasetVersion> datasetVersions = new ArrayList<DatasetVersion>();
//        if(baseDatasetVersions != null && baseDatasetVersions.length > 0) {
//            
//            for (DatasetVersion datasetVersion : baseDatasetVersions) {
//                datasetVersions.add(datasetVersion);
//            }
//        }
//        filterDatasetVersionWidget.setDatasetVersions(datasetVersions.toArray(new DatasetVersion[0]));
        return filterDatasetVersionWidget;
    }
    
     private JButton browseFileButton() {
        Button button = new BrowseButton(new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                selectFolder();
            }
        });   
        Icon icon = new ImageResources().open("Export a Dataset");
        button.setIcon(icon);
        return button;
    }
    

    private String getDatasetsLabel(EmfDataset[] datasets) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < datasets.length; i++) {
            buf.append(datasets[i].getName());
            if (i + 1 < datasets.length)
                buf.append(", ");
        }
        return buf.toString();
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

        JButton done = new Button("Done", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                presenter.notifyDone();
            }
        });
        container.add(done);

        panel.add(container, BorderLayout.EAST);

        return panel;
    }

    private void refresh() {
        super.validate();
    }

    private void doExport() {
        try {
            clearMessagePanel();          
            validateFolder(folder.getText());
            
            String rowFiltersvalues= rowFilter.getText().trim();
            String colOrdersvalues = colOrder.getText().trim();
            validateRowFilterFormat(rowFiltersvalues);
            setRowAndColFilter(rowFiltersvalues, colOrdersvalues);
            
            //validate filter dataset and join condition have both been specified
            validateFilterDatasetAndJoinCondition();
            
            
            Version[] versions=null;
            if (datasets.length ==1)
                versions=new Version[]{(Version) version.getSelectedItem()};
            
            if (!overwrite.isSelected())
                presenter.doExport(datasets, versions, folder.getText(), prefix.getText(), rowFilters, (filterDatasetVersionWidget.getDatasetVersions().length > 0 ? (DatasetVersion)filterDatasetVersionWidget.getDatasetVersions()[0] : null), filterDatasetJoinCondition.getText(), colOrders, purpose.getText(), false, download.isSelected());
            else
                presenter.doExport(datasets, versions, folder.getText(), prefix.getText(), rowFilters, (filterDatasetVersionWidget.getDatasetVersions().length > 0 ? (DatasetVersion)filterDatasetVersionWidget.getDatasetVersions()[0] : null), filterDatasetJoinCondition.getText(), colOrders, purpose.getText(), true, download.isSelected());

            messagePanel.setMessage("Started export. Please monitor the Status window "
                    + "to track your Export request.");

            exportButton.setEnabled(false);
        } catch (EmfException e) {
            //e.printStackTrace();
            messagePanel.setError(e.getMessage());
        }
    }

    private void clearMessagePanel() {
        messagePanel.clear();
        refresh();
    }
    
    private void validateRowFilterFormat(String rowFilter) throws EmfException {
        if (rowFilter != null && rowFilter.contains("\""))
            throw new EmfException("Invalid Row Filter: Please use single quotes instead of double quotes.");
    }

    private void validateFilterDatasetAndJoinCondition() throws EmfException {
        if (filterDatasetVersionWidget.getDatasetVersions().length > 0 && filterDatasetJoinCondition.getText().length() == 0)
            throw new EmfException("Missing Filter Dataset Join Condition: Please specify a join condition (i.e., fips=fips).");
        if (filterDatasetVersionWidget.getDatasetVersions().length == 0 && filterDatasetJoinCondition.getText().length() > 0)
            throw new EmfException("Missing Filter Dataset: Please choose a filter dataset.");
    }

    public void setRowAndColFilter(String rowFilters, String colOrders) {
        this.rowFilters = rowFilters;
        this.colOrders = colOrders;
    }


    public void setMostRecentUsedFolder(String mostRecentUsedFolder) {
        if (datasets.length== 1) 
            fillVersions(datasets[0]);
        if (mostRecentUsedFolder != null)
            folder.setText(mostRecentUsedFolder);
    }

    private void selectFolder() {
        EmfFileInfo initDir = new EmfFileInfo(folder.getText(), true, true);
        
        EmfFileChooser chooser = new EmfFileChooser(initDir, new EmfFileSystemView(service));
        chooser.setTitle("Select a folder to contain the exported Datasets");
        int option = chooser.showDialog(parentConsole, "Select a folder");

        EmfFileInfo file = (option == EmfFileChooser.APPROVE_OPTION) ? chooser.getSelectedDir() : null;
        if (file == null)
            return;

        if (file.isDirectory()) {
            folder.setText(file.getAbsolutePath());
            presenter.setLastFolder(file.getAbsolutePath());
        }
    }
    
    private void validateFolder(String folder) throws EmfException {
        //if performing download, then don't validate directory
        if (download.isSelected())
            return;
        
        if (folder == null || folder.trim().isEmpty())
            throw new EmfException("Please specify a valid export folder.");
        
        if (folder.contains("/home/") || folder.endsWith("/home")) {
            throw new EmfException("Export data into user's home directory is not allowed.");
        }
    }
    
    private void fillVersions(EmfDataset dataset) {
        version.setEnabled(true);
        
        try {
            Version[] versions = presenter.getVersions(dataset);
            version.removeAllItems();
            version.setModel(new DefaultComboBoxModel(versions));
            version.revalidate();
            version.setSelectedIndex(getDefaultVersionIndex(versions, dataset));           
        } catch (EmfException e) {
            e.printStackTrace();
            messagePanel.setError(e.getMessage());
        }
    }
    
    private int getDefaultVersionIndex(Version[] versions, EmfDataset dataset) {
        int defaultversion = dataset.getDefaultVersion();

        for (int i = 0; i < versions.length; i++){
            if (defaultversion == versions[i].getVersion())
                return i;
        }
        return 0;
    } 

}
