package gov.epa.emissions.framework.client.module;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.*;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.gui.buttons.CloseButton;
import gov.epa.emissions.commons.gui.buttons.NewButton;
import gov.epa.emissions.commons.gui.buttons.RemoveButton;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.ReusableInteralFrame;
import gov.epa.emissions.framework.client.ViewMode;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.login.LoginWindow;
import gov.epa.emissions.framework.client.moduletype.ModuleTypeVersionPropertiesWindow;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.BasicSearchFilter;
import gov.epa.emissions.framework.services.basic.FilterField;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.module.*;
import gov.epa.emissions.framework.ui.RefreshButton;
import gov.epa.emissions.framework.ui.RefreshObserver;
import gov.epa.emissions.framework.ui.SelectableSortFilterWrapper;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

import javax.swing.*;

public class ModulesManagerWindow extends ReusableInteralFrame implements ModulesManagerView, RefreshObserver {

    private ModulesManagerPresenter presenter;

    private SelectableSortFilterWrapper table;

    SelectAwareButton viewButton;
    SelectAwareButton editButton;
    Button lockButton;
    Button unlockButton;
    NewButton newButton;
    SelectAwareButton copyButton;
    RemoveButton removeButton;
    Button runButton;
    Button compareButton;
    private TextField simpleTextFilter;

    private JPanel layout;

    private JPanel tablePanel;

    private SingleLineMessagePanel messagePanel;

    private EmfConsole parentConsole;

    private EmfSession session;
    private ComboBox filterFieldsComboBox;

    public ModulesManagerWindow(EmfSession session, EmfConsole parentConsole, DesktopManager desktopManager) {
        super("Module Manager", new Dimension(1200, 800), desktopManager);
        super.setName("moduleManager");

        this.session = session;
        this.parentConsole = parentConsole;

        layout = new JPanel();
        this.getContentPane().add(layout);
    }

    public void observe(ModulesManagerPresenter presenter) {
        this.presenter = presenter;
    }

    private void panelRefresh() {
        tablePanel.removeAll();
        tablePanel.add(table);
        super.refreshLayout();
    }

    public void display() {
        createLayout();
        super.display();
        populate();
    }

    private void createLayout() {
        layout.removeAll();
        layout.setLayout(new BorderLayout());

        layout.add(createTopPanel(), BorderLayout.NORTH);
        layout.add(createTablePanel(), BorderLayout.CENTER);
        layout.add(createControlPanel(), BorderLayout.SOUTH);
    }

    private JPanel createTopPanel() {
        JPanel msgRefreshPanel = new JPanel(new BorderLayout());
        messagePanel = new SingleLineMessagePanel();
        msgRefreshPanel.add(messagePanel, BorderLayout.CENTER);
        Button button = new RefreshButton(this, "Refresh Module Types", messagePanel);
        msgRefreshPanel.add(button, BorderLayout.EAST);

        JPanel topPanel = new JPanel(new BorderLayout());
        simpleTextFilter = new TextField("textfilter", 10);
        simpleTextFilter.setPreferredSize(new Dimension(360, 25));
        simpleTextFilter.setEditable(true);
        simpleTextFilter.addActionListener(simpleFilterTypeAction());


        JPanel advPanel = new JPanel(new BorderLayout(5, 2));

        //get table column names
//        String[] columns = new String[] {"Module Name", "Composite?", "Final?", "Tags", "Project", "Module Type", "Version", "Creator", "Date", "Lock Owner", "Lock Date", "Description" };//(new ModulesTableData(new ConcurrentSkipListMap<Integer, LiteModule>())).columns();

        filterFieldsComboBox = new ComboBox("Select one", (new ModuleFilter()).getFilterFieldNames());
        filterFieldsComboBox.setPreferredSize(new Dimension(180, 25));
        filterFieldsComboBox.addActionListener(simpleFilterTypeAction());

        advPanel.add(getDSTypePanel("Filter Fields:", filterFieldsComboBox), BorderLayout.LINE_START);
        advPanel.add(simpleTextFilter, BorderLayout.EAST);
//        advPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 5));

        topPanel.add(advPanel, BorderLayout.EAST);

        JPanel panel = new JPanel(new GridLayout(2, 1));
        panel.add(msgRefreshPanel);
        panel.add(topPanel);

        return panel;
    }

    private JPanel getDSTypePanel(String label, JComboBox box) {
        JPanel panel = new JPanel(new BorderLayout(5, 2));
        JLabel jlabel = new JLabel(label);
        jlabel.setHorizontalAlignment(JLabel.RIGHT);
        panel.add(jlabel, BorderLayout.WEST);
        panel.add(box, BorderLayout.CENTER);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 10));

        return panel;
    }

    private Action simpleFilterTypeAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
//                DatasetType type = getSelectedDSType();
//                try {
//                    // count the number of datasets and do refresh
//                    if (dsTypesBox.getSelectedIndex() > 0)
                        doRefresh();
//                } catch (EmfException e1) {
////                    messagePanel.setError("Could not retrieve all modules " /*+ type.getName()*/);
//                }
            }
        };
    }


    private JPanel createTablePanel() {
        tablePanel = new JPanel(new BorderLayout());
        table = new SelectableSortFilterWrapper(parentConsole, new ModulesTableData(new ConcurrentSkipListMap<Integer, LiteModule>()), null);
        tablePanel.add(table);
        return tablePanel;
    }

    private JPanel createControlPanel() {
        JPanel crudPanel = createCrudPanel();

        JPanel closePanel = new JPanel();
        Button closeButton = new CloseButton(new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                presenter.doClose();
            }
        });
        closePanel.add(closeButton);
        getRootPane().setDefaultButton(closeButton);

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BorderLayout());

        controlPanel.add(crudPanel, BorderLayout.WEST);
        controlPanel.add(closePanel, BorderLayout.EAST);

        return controlPanel;
    }

    private JPanel createCrudPanel() {
        String message = "You have asked to open a lot of windows. Do you wish to proceed?";
        ConfirmDialog confirmDialog = new ConfirmDialog(message, "Warning", this);

        Action viewAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                viewModules();
            }
        };
        viewButton = new SelectAwareButton("View", viewAction, table, confirmDialog);
        viewButton.setMnemonic(KeyEvent.VK_V);

        Action editAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                editModules();
            }
        };
        editButton = new SelectAwareButton("Edit", editAction, table, confirmDialog);
        editButton.setMnemonic(KeyEvent.VK_E);

        Action lockAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                lockModules();
            }
        };
        lockButton = new Button("Lock", lockAction);
        lockButton.setMnemonic(KeyEvent.VK_L);

        Action unlockAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                unlockModules();
            }
        };
        unlockButton = new Button("Unlock", unlockAction);
        unlockButton.setMnemonic(KeyEvent.VK_O);

        Action createAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                createModule();
            }
        };
        newButton = new NewButton(createAction);
        newButton.setMnemonic(KeyEvent.VK_N);

        Action copyAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                copyModules();
            }
        };
        copyButton = new SelectAwareButton("Copy", copyAction, table, confirmDialog);
        copyButton.setMnemonic(KeyEvent.VK_Y);

        Action removeAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                removeModules();
            }
        };
        removeButton = new RemoveButton(removeAction);
        removeButton.setMnemonic(KeyEvent.VK_M);

        Action runAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                runModules();
            }
        };
        runButton = new Button("Run", runAction);
        runButton.setMnemonic(KeyEvent.VK_U);

        Action compareAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                compareModules();
            }
        };
        compareButton = new Button("Compare", compareAction);
        compareButton.setMnemonic(KeyEvent.VK_C);
        
        Action exportAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                exportModules();
            }
        };
        Button exportButton = new Button("Export", exportAction);

        Action importAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                importModules();
            }
        };
        Button importButton = new Button("Import", importAction);

        JPanel crudPanel = new JPanel();
        crudPanel.setLayout(new FlowLayout());
        crudPanel.add(viewButton);
        crudPanel.add(editButton);
        crudPanel.add(newButton);
        crudPanel.add(copyButton);
        crudPanel.add(removeButton);
        crudPanel.add(Box.createRigidArea(new Dimension(5,0)));
        crudPanel.add(compareButton);
        crudPanel.add(Box.createRigidArea(new Dimension(5,0)));
        crudPanel.add(lockButton);
        crudPanel.add(unlockButton);
        crudPanel.add(Box.createRigidArea(new Dimension(5,0)));
        crudPanel.add(runButton);
        crudPanel.add(Box.createRigidArea(new Dimension(5,0)));
        crudPanel.add(exportButton);
        crudPanel.add(importButton);

        return crudPanel;
    }

    private void viewModules() {
        int[] selectedModuleIds = selectedModuleIds();
        if (selectedModuleIds.length == 0) {
            if (table.getTable().getRowCount() > 0) {
                messagePanel.setMessage("Please select one or more modules");
            }
            return;
        }   

        for (int moduleId : selectedModuleIds) {
            Module module = null;
            try {
                module = presenter.getModule(moduleId);
            } catch (EmfException e) {
                messagePanel.setError("Failed to get module (ID = " + moduleId + "): " + e.getMessage());
                continue;
            }
            if (module == null)
                continue;
            ModulePropertiesWindow view = new ModulePropertiesWindow(parentConsole, desktopManager, session, ViewMode.VIEW, module, null);
            presenter.displayNewModuleView(view);
        }
    }

    private void editModules() {
        int[] selectedModuleIds = selectedModuleIds();
        if (selectedModuleIds.length == 0) {
            if (table.getTable().getRowCount() > 0) {
                messagePanel.setMessage("Please select one or more modules");
            }
            return;
        }   

        for (int moduleId : selectedModuleIds) {
            Module module = null;
            try {
                module = presenter.getModule(moduleId);
            } catch (EmfException e) {
                messagePanel.setError("Failed to get module (ID = " + moduleId + "): " + e.getMessage());
                continue;
            }
            if (module == null)
                continue;
            ViewMode viewMode = module.getIsFinal() ? ViewMode.VIEW : ViewMode.EDIT;
            if (viewMode == ViewMode.EDIT) {
                try {
                    History last = module.lastHistory();
                    module = session.moduleService().obtainLockedModule(session.user(), module.getId());
                    module.setLastHistory(last);
                } catch (EmfException e) {
                    messagePanel.setError("Failed to lock " + module.getName() + ": " + e.getMessage());
                    continue;
                }
                if (!module.isLocked(session.user())) {
                    messagePanel.setError("Failed to lock " + module.getName() + ".");
                    continue;
                }
            } else {
                try {
                    module = presenter.getModule(module.getId());
                } catch (EmfException e) {
                    messagePanel.setError("Failed to get module: " + e.getMessage());
                    continue;
                }
            }
            ModulePropertiesWindow view = new ModulePropertiesWindow(parentConsole, desktopManager, session, viewMode, module, null);
            presenter.displayNewModuleView(view);
        }
    }

    private void lockModules() {
        int[] selectedModuleIds = selectedModuleIds();
        if (selectedModuleIds.length == 0) {
            if (table.getTable().getRowCount() > 0) {
                messagePanel.setMessage("Please select one or more modules");
            }
            return;
        }   

        boolean mustRefresh = false;
        
        try {
            session.moduleService().lockModules(session.user(), selectedModuleIds);
            mustRefresh = true;
        } catch (EmfException e) {
            messagePanel.setError("Failed to lock module(s): " + e.getMessage());
        }

        if (mustRefresh)
            doRefresh();
    }

    private void unlockModules() {
        int[] selectedModuleIds = selectedModuleIds();
        if (selectedModuleIds.length == 0) {
            if (table.getTable().getRowCount() > 0) {
                messagePanel.setMessage("Please select one or more modules");
            }
            return;
        }
        
        boolean mustRefresh = false;
        
        try {
            session.moduleService().unlockModules(session.user(), selectedModuleIds);
            mustRefresh = true;
        } catch (EmfException e) {
            messagePanel.setError("Failed to lock module(s): " + e.getMessage());
        }

        if (mustRefresh)
            doRefresh();
    }

    private void createModule() {
        ModuleTypeVersion moduleTypeVersion = ModulePropertiesWindow.selectModuleTypeVersion(parentConsole, session);
        if (moduleTypeVersion != null) {
            ModulePropertiesWindow view = new ModulePropertiesWindow(parentConsole, desktopManager, session, ViewMode.NEW, null, moduleTypeVersion);
            presenter.displayNewModuleView(view);
        }
    }

    private void copyModules() {
        int[] selectedModuleIds = selectedModuleIds();
        if (selectedModuleIds.length == 0) {
            if (table.getTable().getRowCount() > 0) {
                messagePanel.setMessage("Please select one or more modules");
            }
            return;
        }
        
        for (int moduleId : selectedModuleIds) {
            Module module = null;
            try {
                module = presenter.getModule(moduleId);
            } catch (EmfException e) {
                messagePanel.setError("Failed to get module (ID = " + moduleId + "): " + e.getMessage());
                continue;
            }
            if (module == null)
                continue;
            Module copy = module.deepCopy(session.user());
            ModulePropertiesWindow view = new ModulePropertiesWindow(parentConsole, desktopManager, session, ViewMode.NEW, copy, null);
            presenter.displayNewModuleView(view);
        }
    }

    private void removeModules() {
        messagePanel.clear();
        
        int[] selectedModuleIds = selectedModuleIds();
        if (selectedModuleIds.length == 0) {
            if (table.getTable().getRowCount() > 0) {
                messagePanel.setMessage("Please select one or more modules");
            }
            return;
        }
        
        // for non-admin users, check ownership and finalization of selected modules
        if (!session.user().isAdmin()) {
            for (int moduleId : selectedModuleIds) {
                Module module = null;
                try {
                    // fetch full module instead of using lite version in case changes haven't
                    // been refreshed
                    module = presenter.getModule(moduleId);
                } catch (EmfException e) {
                    messagePanel.setError("Failed to get module (ID = " + moduleId + "): " + e.getMessage());
                    return;
                }
                if (module == null)
                    continue;
                if (!module.getCreator().equals(session.user())) {
                    messagePanel.setError("Cannot delete module \"" + module.getName() + "\" because you did not create it.");
                    return;
                }
                if (module.getIsFinal()) {
                    messagePanel.setError("Cannot delete final module \"" + module.getName() + "\".");
                    return;
                }
            }
        }
        
        String message = "Are you sure you want to remove the selected " + selectedModuleIds.length + " module(s)?";
        JCheckBox deleteOutputs = new JCheckBox("Delete any output datasets?");
        Object[] contents = {message, deleteOutputs};
        int selection = JOptionPane.showConfirmDialog(parentConsole, contents, "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (selection != JOptionPane.YES_OPTION)
            return;
        
        try {
            int[] removedModuleIds = presenter.doRemove(selectedModuleIds, deleteOutputs.isSelected());
            if (removedModuleIds.length == 0)
                messagePanel.setError("No module has been removed.");
            else if (removedModuleIds.length == 1)
                messagePanel.setMessage("One module has been removed. Please Refresh to see the revised list of modules.");
            else
                messagePanel.setMessage(removedModuleIds.length + " modules have been removed. Please Refresh to see the revised list of modules.");
        } catch (EmfException e) {
          JOptionPane.showConfirmDialog(parentConsole, e.getMessage(), "Error", JOptionPane.CLOSED_OPTION);
        }
    }

    private void runModules() {
        messagePanel.clear();
        
        int[] selectedModuleIds = selectedModuleIds();
        if (selectedModuleIds.length == 0) {
            if (table.getTable().getRowCount() > 0) {
                messagePanel.setMessage("Please select one or more modules");
            }
            return;
        }

        String message;
        if (selectedModuleIds.length == 1) {
            message = "Are you sure you want to run this module?";
        } else {
            message = "Are you sure you want to run the selected " + selectedModuleIds.length + " modules?";
        }
        int selection = JOptionPane.showConfirmDialog(parentConsole, message, "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (selection != JOptionPane.YES_OPTION)
            return;
            
        try {
            presenter.runModules(selectedModuleIds);
            if (selectedModuleIds.length == 1) {
                messagePanel.setMessage("The module has been executed.");
            } else {
                messagePanel.setMessage("The " + selectedModuleIds.length + " modules have been executed.");
            }
        } catch (EmfException e) {
          JOptionPane.showConfirmDialog(parentConsole, e.getMessage(), "Error", JOptionPane.CLOSED_OPTION);
        }
    }

    private void compareModules() {
        messagePanel.clear();
        
        int[] selectedModuleIds = selectedModuleIds();
        if (selectedModuleIds.length < 2) {
            messagePanel.setMessage("Please select two modules");
            return;
        }
        if (selectedModuleIds.length > 2) {
            messagePanel.setMessage("Please select only two modules");
            return;
        }

        Module[] modules = new Module[selectedModuleIds.length];
        for (int i = 0; i < selectedModuleIds.length; i++) {
            int moduleId = selectedModuleIds[i];
            modules[i] = null;
            try {
                modules[i] = presenter.getModule(moduleId);
            } catch (EmfException e) {
                messagePanel.setError("Failed to get module (ID = " + moduleId + "): " + e.getMessage());
                return;
            }
            if (modules[i] == null) {
                messagePanel.setError("Failed to get module (ID = " + moduleId + ")");
                return;
            }
        }

        ModuleComparisonWindow view = new ModuleComparisonWindow(session, parentConsole, desktopManager, modules[0], modules[1]);
        presenter.displayModuleComparisonView(view);
    }

    private static void saveToXML(String filename, ModulesExportImport modulesExportImport) {
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(filename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }
        XMLEncoder encoder = new XMLEncoder(new BufferedOutputStream(fos));
        encoder.writeObject(modulesExportImport);
        encoder.close();
    }

    private ModulesExportImport readFromXML(String filename) {
        FileInputStream fis;
        try {
            fis = new FileInputStream(filename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        XMLDecoder decoder = new XMLDecoder(new BufferedInputStream(fis));
        ModulesExportImport modulesExportImport = (ModulesExportImport)decoder.readObject();
        decoder.close();
        return modulesExportImport;
    }
    
    private void exportModules() {
        int[] selectedModuleIds = selectedModuleIds();
        if (selectedModuleIds.length == 0) {
            messagePanel.setMessage("Please select one or more modules to export");
            return;
        }
        
        String message = "Would you like to export the input datasets for the selected modules?";
        JCheckBox download = new JCheckBox("Download files to local machine?", true);
        String exportFolder = session.preferences().outputFolder();
        String location = "If not downloaded, files will be exported to \"" + exportFolder + "\" on the server.";
        Object[] contents = {message, download, location};
        boolean exportDatasets = (JOptionPane.showConfirmDialog(parentConsole, contents, "Export Module Datasets",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION);

        List<Module> modulesList = new ArrayList<Module>();
        Map<Integer, Map<String, String>> moduleDatasetInfo = new HashMap<Integer, Map<String, String>>();
        List<Integer> datasetIdsList = new ArrayList<Integer>();
        List<Version> versionsList = new ArrayList<Version>();
        
        for (int moduleId : selectedModuleIds) {
            Module module = null;
            try {
                module = presenter.getModule(moduleId);
            } catch (EmfException e) {
                messagePanel.setError("Failed to get module (ID = " + moduleId + "): " + e.getMessage());
                continue;
            }
            if (module == null)
                continue;
            
            modulesList.add(module);
            
            // build list of dataset names used by modules
            for (ModuleDataset moduleDataset : module.getModuleDatasets().values()) {
                if (moduleDataset.getDatasetId() == null ||
                    moduleDataset.getModuleTypeVersionDataset().isModeOUT()) continue;
                
                // check if dataset has already been processed
                if (moduleDatasetInfo.containsKey(moduleDataset.getDatasetId())) continue;
                
                EmfDataset dataset = null;
                Version version = null;
                String exportName = "";
                try {
                    dataset = session.dataService().getDataset(moduleDataset.getDatasetId());
                    version = session.eximService().getVersion(dataset, moduleDataset.getVersion());
                    exportName = session.eximService().getDatasetExportName(dataset, version, "");
                } catch (EmfException ex) {
                    continue;
                }
                
                Map<String, String> datasetInfo = new HashMap<String, String>();
                datasetInfo.put("datasetName", dataset.getName());
                datasetInfo.put("datasetTypeName", dataset.getDatasetTypeName());
                datasetInfo.put("version", Integer.toString(version.getVersion()));
                datasetInfo.put("exportName", exportName);
                moduleDatasetInfo.put(moduleDataset.getDatasetId(), datasetInfo);
                
                if (exportDatasets) {
                    datasetIdsList.add(moduleDataset.getDatasetId());
                    versionsList.add(version);
                }
            }
        }
        
        if (modulesList.isEmpty())
            return;
        
        JFileChooser fc = new JFileChooser();
        int returnVal = fc.showSaveDialog(this);
        if (returnVal != JFileChooser.APPROVE_OPTION)
            return;
        
        File file = fc.getSelectedFile();
        String filename;
        try {
            filename = file.getCanonicalPath();
        } catch (IOException e) {
            e.printStackTrace();
            messagePanel.setError("Failed to get export file full path: " + e.getMessage());
            return;
        }
        
        if (file.exists()) {
            int selection = JOptionPane.showConfirmDialog(parentConsole, "File \"" + filename + "\" already exists. Are you sure you want to replace it?", "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (selection == JOptionPane.NO_OPTION) {
                messagePanel.setMessage("Export cancelled.");
                return;
            }
        }
        
        if (exportDatasets && !datasetIdsList.isEmpty()) {
            Integer[] datasetIds = new Integer[datasetIdsList.size()];
            datasetIds = datasetIdsList.toArray(datasetIds);
            Version[] versions = new Version[versionsList.size()];
            versions = versionsList.toArray(versions);
            try {
                if (download.isSelected()) {
                    session.eximService().downloadDatasets(session.user(), datasetIds, versions, "", "",
                            null, null, "", "", "Exported with module");
                } else {
                    session.eximService().exportDatasetids(session.user(), datasetIds, versions, exportFolder, "", true,
                            "", null, null, "", "", "Exported with module");
                }
            } catch (EmfException ex) {
                // TODO
            }
        }

        ModulesExportImport modulesExportImport = new ModulesExportImport(modulesList, moduleDatasetInfo);
        modulesExportImport.setExportEmfServer(session.serviceLocator().getBaseUrl());
        modulesExportImport.setExportEmfVersion(LoginWindow.EMF_VERSION);
        modulesExportImport.setExportFileName(file.getName());
        modulesExportImport.setExportUserName(session.user().getName());
        modulesExportImport.setExportDate(new Date());
        
        modulesExportImport.prepareForExport();
        
        saveToXML(filename, modulesExportImport);
        
        String summary = String.format("Exported %d module%s to \"%s\". %s",
                                       selectedModuleIds.length, (selectedModuleIds.length == 1) ? "" : "s",
                                       file.getName(),
                                       (exportDatasets ? "Check the Status window for dataset export information." : ""));
        messagePanel.setMessage(summary);
    }
    
    // TODO move this to the server
    private String findAvailableModuleName(String baseName) {
        while (true) {
            String availableName = "Imported " + baseName + " " + CustomDateFormat.format_YYYYMMDDHHMMSSSS(new Date());
            Module localModule = null;
            try {
                localModule = presenter.getModule(availableName);
            } catch (EmfException e) {
                // e.printStackTrace();
            }
            if (localModule == null) {
                return availableName;
            }
            
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // e.printStackTrace();
            }
        }
    }
    
    private void importModules() {
        JFileChooser fc = new JFileChooser();
        int returnVal = fc.showOpenDialog(this);
        if (returnVal != JFileChooser.APPROVE_OPTION)
            return;

        File file = fc.getSelectedFile();
        String filename;
        try {
            filename = file.getCanonicalPath();
        } catch (IOException e) {
            e.printStackTrace();
            messagePanel.setError("Failed to get import file full path: " + e.getMessage());
            return;
        }
        
        if (!file.exists()) {
            messagePanel.setError("File \"" + filename + "\" doesn't exists.");
            return;
        }
        
        ModulesExportImport modulesExportImport = readFromXML(filename);
        
        String importEmfServer = session.serviceLocator().getBaseUrl();
        String importEmfVersion = LoginWindow.EMF_VERSION;
        String importFileName = file.getName();
        User importUser = session.user();
        Date importDate = new Date();

        // TODO add export/import version compatibility table 
        if (!modulesExportImport.getExportEmfVersion().equals(importEmfVersion)) {
            String message = String.format("Can't import \"%s\": file version \"%s\" incompatible with local version \"%s\".",
                                           importFileName, modulesExportImport.getExportEmfVersion(), importEmfVersion);
            messagePanel.setError(message);
            return;
        }

        String exportMessage = String.format("Exported from server \"%s\" version \"%s\" to \"%s\" by %s on %s.",
                                             modulesExportImport.getExportEmfServer(), modulesExportImport.getExportEmfVersion(),
                                             modulesExportImport.getExportFileName(), modulesExportImport.getExportUserName(),
                                             CustomDateFormat.format_MM_DD_YYYY_HH_mm(modulesExportImport.getExportDate()));

        String importMessage = String.format("Imported on server \"%s\" version \"%s\" from file \"%s\" by %s on %s.",
                                             importEmfServer, importEmfVersion, importFileName, importUser.getName(),
                                             CustomDateFormat.format_MM_DD_YYYY_HH_mm(importDate));
        
        String exportImportMessage = exportMessage + "\n" + importMessage;
        
        StringBuilder changeLog = new StringBuilder();
        
        int moduleAdditions = 0;
        int moduleSkips = 0;
        
        for (Module importedModule : modulesExportImport.getModules()) {
            String importedModuleName = importedModule.getName();
            
            // match module type by name
            ModuleType localModuleType = null;
            try {
                localModuleType = presenter.getModuleType(importedModule.getModuleTypeVersion().getModuleType().getName());
                
                // check that module type matches simple vs. composite
                if (localModuleType.isComposite() != importedModule.isComposite()) {
                    localModuleType = null;
                }
            } catch (EmfException e) {
                // e.printStackTrace();
            }
            if (localModuleType == null) {
                changeLog.append(String.format("Skipped module \"%s\" due to no matching module type.\n", importedModuleName));
                moduleSkips++;
                continue;
            }
            
            // match module type version by version number and algorithm (non-composite only)
            ModuleTypeVersion localModuleTypeVersion = null;
            for (ModuleTypeVersion moduleTypeVersion : localModuleType.getModuleTypeVersions().values()) {
                if (importedModule.getModuleTypeVersion().getVersion() == moduleTypeVersion.getVersion() &&
                    (importedModule.isComposite() ||
                     importedModule.getModuleTypeVersion().getAlgorithm().equals(moduleTypeVersion.getAlgorithm()))) {
                    localModuleTypeVersion = moduleTypeVersion;
                    break;
                }
            }
            if (localModuleTypeVersion == null) {
                changeLog.append(String.format("Skipped module \"%s\" due to no matching module type version.\n", importedModuleName));
                moduleSkips++;
                continue;
            }
            
            importedModule.setModuleTypeVersion(localModuleTypeVersion);
            
            // build unique module name if needed
            Module localModule = null;
            try {
                localModule = presenter.getModule(importedModuleName);
            } catch (EmfException e) {
                // e.printStackTrace();
            }
            
            if (localModule != null) {
                String newModuleName = findAvailableModuleName(importedModuleName);
                importedModule.setName(newModuleName);
                localModule = null;
            }
            
            // match module datasets by name, dataset type, and version number
            int datasetMatches = 0;
            int datasetsNotMatched = 0;
            for (ModuleDataset moduleDataset : importedModule.getModuleDatasets().values()) {
                if (moduleDataset.getDatasetId() == null) {
                    // dataset wasn't set at export
                    continue;
                }
                
                String datasetName = modulesExportImport.getModuleDatasetInfo().get(moduleDataset.getDatasetId()).get("datasetName");
                EmfDataset localDataset = null;
                try {
                    localDataset = presenter.getDataset(datasetName);
                } catch (EmfException e) {
                    // e.printStackTrace();
                }
                if (localDataset == null) {
                    moduleDataset.setDatasetId(null);
                    datasetsNotMatched++;
                    continue;
                }
                
                DatasetType moduleDatasetType = moduleDataset.getModuleTypeVersionDataset().getDatasetType();
                if (moduleDatasetType.getId() != localDataset.getDatasetType().getId()) {
                    moduleDataset.setDatasetId(null);
                    datasetsNotMatched++;
                    continue;
                }
                
                Version localVersion = null;
                try {
                    localVersion = presenter.getVersion(localDataset, moduleDataset.getVersion());
                } catch (EmfException e) {
                    // e.printStackTrack();
                }
                if (localVersion == null) {
                    moduleDataset.setDatasetId(null);
                    datasetsNotMatched++;
                    continue;
                }
                
                moduleDataset.setDatasetId(localDataset.getId());
                datasetMatches++;
            }
            
            try {
                importedModule.prepareForImport(exportImportMessage, importUser, importDate);
                localModule = presenter.addModule(importedModule);
            } catch (EmfException e) {
                e.printStackTrace();
                messagePanel.setError("Failed to add imported module \"" + importedModuleName + "\": " + e.getMessage());
                return;
            }
            
            if (localModule == null) {
                messagePanel.setError("Failed to add imported module \"" + importedModuleName + "\" to current server.");
                return;
            }
            
            if (localModule.getName().equals(importedModuleName)) {
                changeLog.append(String.format("Imported module \"%s\" to current server.\n", importedModuleName));
            } else {
                changeLog.append(String.format("Imported module \"%s\" with modified name \"%s\" to current server.\n", 
                                               importedModuleName, localModule.getName()));
            }
            moduleAdditions++;
            
            if (datasetMatches > 0 || datasetsNotMatched > 0) {
                changeLog.append(String.format("  %d dataset%s matched, %d dataset%s not matched\n",
                                               datasetMatches, (datasetMatches == 1) ? "" : "s",
                                               datasetsNotMatched, (datasetsNotMatched == 1) ? "" : "s"));
            }
        }
        
        String summary = String.format("Imported %d module%s (%d skipped) from \"%s\".", 
                                       moduleAdditions, (moduleAdditions == 1) ? "" : "s", moduleSkips, importFileName);
        changeLog.append("\n" + summary + "\n");
        showLargeMessage(summary, changeLog.toString());
        
        doRefresh();
    }

    private void showLargeMessage(String title, String message) {
        // reusing implementation from ModuleTypeVersionPropertiesWindow
        ModuleTypeVersionPropertiesWindow.showLargeMessage(messagePanel, title, message);
    }

    private int[] selectedModuleIds() {
        List selected = table.selected();
        int[] moduleIds = new int[selected.size()];
        int i = 0;
        for (Iterator iter = selected.iterator(); iter.hasNext();) {
            LiteModule liteModule = (LiteModule) iter.next();
            moduleIds[i++] = liteModule.getId();
        }
        return moduleIds;
    }

    public EmfConsole getParentConsole() {
        return this.parentConsole;
    }

    @Override
    public void doRefresh() {
        LiteModule[] liteModules = new LiteModule[0];
        try {
            BasicSearchFilter searchFilter = getBasicSearchFilter();
            liteModules = presenter.getLiteModules(searchFilter);
        } catch (EmfException e) {
            e.printStackTrace();
        }
        boolean hasData = (liteModules != null) && !(liteModules.length == 0);

        // FIXME these settings are reverted somewhere else
        viewButton.setEnabled(hasData);
        editButton.setEnabled(hasData);
        lockButton.setEnabled(hasData);
        unlockButton.setEnabled(hasData);
        newButton.setEnabled(true);
        removeButton.setEnabled(hasData);
        runButton.setEnabled(hasData);

        table.refresh(new ModulesTableData(liteModules));
        panelRefresh();
    }

    private BasicSearchFilter getBasicSearchFilter() {

        BasicSearchFilter searchFilter = new BasicSearchFilter();
        String fieldName = (String)filterFieldsComboBox.getSelectedItem();
        if (fieldName != null) {
            searchFilter.setFieldName(fieldName);
            searchFilter.setFieldValue(simpleTextFilter.getText());
        }
        return searchFilter;
    }

    @Override
    public void populate() {
        doRefresh();
    }
}
