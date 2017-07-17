package gov.epa.emissions.framework.client.moduletype;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ConfirmDialog;
import gov.epa.emissions.commons.gui.SelectAwareButton;
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
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.module.LiteModule;
import gov.epa.emissions.framework.services.module.LiteModuleType;
import gov.epa.emissions.framework.services.module.ModuleType;
import gov.epa.emissions.framework.services.module.ModuleTypeVersion;
import gov.epa.emissions.framework.services.module.ModuleTypeVersionDataset;
import gov.epa.emissions.framework.services.module.ModuleTypeVersionSubmodule;
import gov.epa.emissions.framework.services.module.ModuleTypesExportImport;
import gov.epa.emissions.framework.ui.RefreshButton;
import gov.epa.emissions.framework.ui.RefreshObserver;
import gov.epa.emissions.framework.ui.SelectableSortFilterWrapper;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
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

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class ModuleTypesManagerWindow extends ReusableInteralFrame implements ModuleTypesManagerView, RefreshObserver {

    private ModuleTypesManagerPresenter presenter;

    private ModuleType[] moduleTypes;
    
    private SelectableSortFilterWrapper table;

    private JPanel layout;

    private JPanel tablePanel;

    private SingleLineMessagePanel messagePanel;

    private EmfConsole parentConsole;

    private EmfSession session;

    public ModuleTypesManagerWindow(EmfSession session, EmfConsole parentConsole, DesktopManager desktopManager) {
        super("Module Type Manager", new Dimension(1200, 800), desktopManager);
        super.setName("moduleTypeManager");

        this.session = session;
        this.parentConsole = parentConsole;

        moduleTypes = new ModuleType[0];
        
        layout = new JPanel();
        this.getContentPane().add(layout);
    }

    public void observe(ModuleTypesManagerPresenter presenter) {
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
    }

    private void createLayout() {
        layout.removeAll();
        layout.setLayout(new BorderLayout());

        layout.add(createTopPanel(), BorderLayout.NORTH);
        layout.add(createTablePanel(), BorderLayout.CENTER);
        layout.add(createControlPanel(), BorderLayout.SOUTH);
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        messagePanel = new SingleLineMessagePanel();
        panel.add(messagePanel, BorderLayout.CENTER);

        Button button = new RefreshButton(this, "Refresh Module Types", messagePanel);
        panel.add(button, BorderLayout.EAST);

        return panel;
    }

    private JPanel createTablePanel() {
        tablePanel = new JPanel(new BorderLayout());
        table = new SelectableSortFilterWrapper(parentConsole, new ModuleTypesTableData(moduleTypes), null);
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
                viewModuleTypes();
            }
        };
        SelectAwareButton viewButton = new SelectAwareButton("View", viewAction, table, confirmDialog);

        Action editAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                editModuleTypes();
            }
        };
        SelectAwareButton editButton = new SelectAwareButton("Edit", editAction, table, confirmDialog);

        Action createAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                createModuleType();
            }
        };
        Button newButton = new NewButton(createAction);

        Action removeAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                removeModuleType();
            }
        };
        Button removeButton = new RemoveButton(removeAction);
        
        Action exportAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                exportModuleTypes();
            }
        };
        SelectAwareButton exportButton = new SelectAwareButton("Export", exportAction, table, confirmDialog);

        Action importAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                importModuleTypes();
            }
        };
        SelectAwareButton importButton = new SelectAwareButton("Import", importAction, table, confirmDialog);

        JPanel crudPanel = new JPanel();
        crudPanel.setLayout(new FlowLayout());
        crudPanel.add(viewButton);
        crudPanel.add(editButton);
        crudPanel.add(newButton);
        crudPanel.add(removeButton);
        crudPanel.add(Box.createRigidArea(new Dimension(5,0)));
        crudPanel.add(exportButton);
        crudPanel.add(importButton);
        return crudPanel;
    }

    private void viewModuleTypes() {
        List selected = selected();
        if (selected.isEmpty()) {
            messagePanel.setMessage("Please select one or more module types");
            return;
        }   

        for (Iterator iter = selected.iterator(); iter.hasNext();) {
            ModuleType moduleType = (ModuleType) iter.next();
            try {
                ModuleTypeVersionsManagerWindow view = new ModuleTypeVersionsManagerWindow(session, parentConsole, desktopManager, ViewMode.VIEW, moduleType);
                presenter.displayModuleTypeVersions(view);
            } catch (EmfException e) {
                messagePanel.setError("Could not diplay module type versions for: " + moduleType.getName() + "." + e.getMessage());
                break;
            }
        }
    }

    private void editModuleTypes() {
        List selected = selected();
        if (selected.isEmpty()) {
            messagePanel.setMessage("Please select one or more module types");
            return;
        }   

        for (Iterator iter = selected.iterator(); iter.hasNext();) {
            ModuleType moduleType = (ModuleType) iter.next();
            try {
                moduleType = session.moduleService().obtainLockedModuleType(session.user(), moduleType.getId());
            } catch (EmfException e) {
                messagePanel.setError("Failed to lock " + moduleType.getName() + ": " + e.getMessage());
                break;
            }
            if (!moduleType.isLocked(session.user())) {
                messagePanel.setError("Failed to lock " + moduleType.getName() + ".");
                break;
            }
            try {
                ModuleTypeVersionsManagerWindow view = new ModuleTypeVersionsManagerWindow(session, parentConsole, desktopManager, ViewMode.EDIT, moduleType);
                presenter.displayModuleTypeVersions(view);
            } catch (EmfException e) {
                messagePanel.setError("Could not diplay module type versions for: " + moduleType.getName() + "." + e.getMessage());
                break;
            }
        }
    }

    private void createModuleType() {
        int selection = JOptionPane.showConfirmDialog(parentConsole, "Create a composite module type?", "", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        ModuleTypeVersionPropertiesWindow view = new ModuleTypeVersionPropertiesWindow(parentConsole, desktopManager, session, this, selection == JOptionPane.YES_OPTION);
        presenter.displayNewModuleTypeView(view);
    }

    private void removeModuleType() {
        messagePanel.clear();
        List<?> selected = selected();
        if (selected.isEmpty()) {
            messagePanel.setMessage("Please select one or more module types");
            return;
        }   

        ModuleType[] selectedModuleTypes = selected.toArray(new ModuleType[0]);
        Map<Integer, ModuleType> selectedModuleTypesMap = new HashMap<Integer, ModuleType>();
        for (int i = 0; i < selectedModuleTypes.length; i++) {
            ModuleType selectedModuleType = selectedModuleTypes[i]; 
            selectedModuleTypesMap.put(selectedModuleType.getId(), selectedModuleType); 
            if (selectedModuleType.isLocked()) {
                String error = String.format("Can't remove the %s module type: it's locked by %s", selectedModuleType.getName(), selectedModuleType.getLockOwner());
                messagePanel.setError(error);
                return;
            }
        }
        
        List<String> usingObjects = new ArrayList<String>();
        
        int moduleTypeCount = 0;
        int moduleTypeVersionCount = 0;
        int moduleTypeVersionSubmoduleCount = 0;
        for(ModuleType moduleType : moduleTypes) {
            if (selectedModuleTypesMap.containsKey(moduleType.getId()))
                continue;
            boolean moduleTypeFound = false;
            for (ModuleTypeVersion moduleTypeVersion : moduleType.getModuleTypeVersions().values()) {
                boolean moduleTypeVersionFound = false;
                for (ModuleTypeVersionSubmodule moduleTypeVersionSubmodule : moduleTypeVersion.getModuleTypeVersionSubmodules().values()) {
                    ModuleType submoduleType = moduleTypeVersionSubmodule.getModuleTypeVersion().getModuleType();
                    if (selectedModuleTypesMap.containsKey(submoduleType.getId())) {
                        if (moduleType.isLocked()) {
                            String error = String.format("Can't delete the \"%s\" module type because the \"%s\" module type is locked by %s",
                                                         submoduleType.getName(), moduleType.getName(), moduleType.getLockOwner());
                            messagePanel.setError(error);
                            return;
                        }
                        moduleTypeVersionSubmoduleCount++;
                        moduleTypeVersionFound = true;
                        usingObjects.add(moduleTypeVersionSubmodule.fullName());
                    }
                }
                if (moduleTypeVersionFound) {
                    moduleTypeVersionCount++;
                    moduleTypeFound = true;
                }
            }
            if (moduleTypeFound) {
                moduleTypeCount++;
            }
        }

        int moduleCount = 0;
        ConcurrentSkipListMap<Integer, LiteModule> liteModules = session.getLiteModules();
        for(LiteModule liteModule : liteModules.values()) {
            LiteModuleType liteModuleType = liteModule.getLiteModuleTypeVersion().getLiteModuleType();
            if (selectedModuleTypesMap.containsKey(liteModuleType.getId())) {
                if (liteModule.isLocked()) {
                    String error = String.format("Can't remove the %s module type: the %s module is locked by %s", liteModuleType.getName(), liteModule.getName(), liteModule.getLockOwner());
                    messagePanel.setError(error);
                    return;
                }
                moduleCount++;
                usingObjects.add(String.format("module \"%s\"", liteModule.getName()));
            }
        }

        StringBuilder message = new StringBuilder();
        if (selectedModuleTypes.length == 1)
            message.append(String.format("Are you sure you want to remove the \"%s\" module type?\n", selectedModuleTypes[0].getName()));
        else
            message.append(String.format("Are you sure you want to remove the %d selected module types?\n", selectedModuleTypes.length));
                
        if (moduleTypeCount > 0 || moduleCount > 0) {
            message.append(((selectedModuleTypes.length == 1) ? "It is " : "They are ") + "used by ");
            if (moduleTypeCount > 0) {
                message.append(String.format("%d module type%s, %d module type version%s, %s%d submodule%s",
                                              moduleTypeCount, (moduleTypeCount == 1) ? "" : "s",
                                              moduleTypeVersionCount, (moduleTypeVersionCount == 1) ? "" : "s",
                                              (moduleCount > 0) ? "" : "and ", 
                                              moduleTypeVersionSubmoduleCount, (moduleTypeVersionSubmoduleCount == 1) ? "" : "s"));
            }
            if (moduleTypeCount > 0 && moduleCount > 0) {
                message.append(", and ");
            }
            if (moduleCount > 0) {
                message.append(String.format("%d module%s", moduleCount, (moduleCount == 1) ? "" : "s"));
            }
            message.append(":\n");
            for(String usingObject : usingObjects) {
                message.append(usingObject + "\n");
            }
        }
        message.append("There is no undo for this action!");
        
        int selection = JOptionPane.showConfirmDialog(parentConsole, message, "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (selection == JOptionPane.YES_OPTION) {
            try {
                presenter.doRemove(selectedModuleTypes);
                messagePanel.setMessage(selectedModuleTypes.length + " module types have been removed. Please Refresh to see the revised list of types.");
            } catch (EmfException e) {
              JOptionPane.showConfirmDialog(parentConsole, e.getMessage(), "Error", JOptionPane.CLOSED_OPTION);
            }
        }
    }

    private static void saveToXML(String filename, ModuleTypesExportImport moduleTypesExportImport) {
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(filename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }
        XMLEncoder encoder = new XMLEncoder(new BufferedOutputStream(fos));
        encoder.writeObject(moduleTypesExportImport);
        encoder.close();
    }

    private ModuleTypesExportImport readFromXML(String filename) {
        FileInputStream fis;
        try {
            fis = new FileInputStream(filename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        XMLDecoder decoder = new XMLDecoder(new BufferedInputStream(fis));
        ModuleTypesExportImport moduleTypesExportImport = (ModuleTypesExportImport)decoder.readObject();
        decoder.close();
        return moduleTypesExportImport;
    }

    private void exportModuleTypes() {
        try {
            doExportModuleTypes();
        } catch (Exception e) {
            e.printStackTrace();
            // ignore
        }

        JOptionPane.showMessageDialog(parentConsole, "Module Type Manager needs to be refreshed and all other windows must be closed.", "Warning", JOptionPane.OK_OPTION);
        
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            doRefresh();
        } catch (Exception e) {
            e.printStackTrace();
            // ignore
        }
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));        
        
        messagePanel.setMessage("Module Type Manager has been refreshed. Please close all other windows.");
    }
    
    private void doExportModuleTypes() {
        List<?> selected = selected();
        if (selected.isEmpty()) {
            messagePanel.setMessage("Please select one or more module types");
            return;
        }   

        Map<Integer, DatasetType> datasetTypesMap = new HashMap<Integer, DatasetType>();
        Map<Integer, ModuleType> moduleTypesMap = new HashMap<Integer, ModuleType>();
        List<ModuleType> moduleTypesList = new ArrayList<ModuleType>(); // in order of dependencies (most independent first, most dependent last)
        Map<Integer, ModuleType> moduleTypesInProgress = new HashMap<Integer, ModuleType>();

        for(Object object : selected) {
            ModuleType moduleType = (ModuleType)object;
            try {
                moduleType.exportTypes(datasetTypesMap, moduleTypesMap, moduleTypesList, moduleTypesInProgress);
            } catch (EmfException e) {
                e.printStackTrace();
                messagePanel.setError("Failed to export module type: " + e.getMessage());
                return;
            }
        }
        
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

        ModuleTypesExportImport moduleTypesExportImport = new ModuleTypesExportImport(datasetTypesMap, moduleTypesList);
        moduleTypesExportImport.setExportEmfServer(session.serviceLocator().getBaseUrl());
        moduleTypesExportImport.setExportEmfVersion(LoginWindow.EMF_VERSION);
        moduleTypesExportImport.setExportFileName(file.getName());
        moduleTypesExportImport.setExportUserName(session.user().getName());
        moduleTypesExportImport.setExportDate(new Date());
        
        moduleTypesExportImport.prepareForExport();
        
        saveToXML(filename, moduleTypesExportImport);
        
        String summary = String.format("Exported %d dataset type%s and %d module type%s to \"%s\".",
                                       moduleTypesExportImport.getDatasetTypes().length, (moduleTypesExportImport.getDatasetTypes().length == 1) ? "" : "s",
                                       moduleTypesExportImport.getModuleTypes().length,  (moduleTypesExportImport.getModuleTypes().length == 1) ? "" : "s",
                                       file.getName());
        messagePanel.setMessage(summary);
    }

    // TODO move this to the server
    private String findAvailableDatasetTypeName(String baseName) {
        while (true) {
            String availableName = "Imported " + baseName + " " + CustomDateFormat.format_YYYYMMDDHHMMSSSS(new Date());
            DatasetType localDatasetType = null;
            try {
                localDatasetType = presenter.getDatasetType(availableName);
            } catch (EmfException e) {
                // e.printStackTrace();
            }
            if (localDatasetType == null) {
                return availableName;
            }
            
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // e.printStackTrace();
            }
        }
    }

    // TODO move this to the server
    private String findAvailableModuleTypeName(String baseName) {
        while (true) {
            String availableName = "Imported " + baseName + " " + CustomDateFormat.format_YYYYMMDDHHMMSSSS(new Date());
            ModuleType localModuleType = null;
            try {
                localModuleType = presenter.getModuleType(availableName);
            } catch (EmfException e) {
                // e.printStackTrace();
            }
            if (localModuleType == null) {
                return availableName;
            }
            
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // e.printStackTrace();
            }
        }
    }

    private void importModuleTypes() {
        try {
            doImportModuleTypes();
        } catch (Exception e) {
            e.printStackTrace();
            // ignore
        }

        JOptionPane.showMessageDialog(parentConsole, "Module Type Manager needs to be refreshed and all other windows must be closed.", "Warning", JOptionPane.OK_OPTION);
        
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            doRefresh();
        } catch (Exception e) {
            e.printStackTrace();
            // ignore
        }
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));        

        messagePanel.setMessage("Module Type Manager has been refreshed. Please close all other windows.");
    }
    
    private void doImportModuleTypes() {
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
        
        ModuleTypesExportImport moduleTypesExportImport = readFromXML(filename);
        
        String importEmfServer = session.serviceLocator().getBaseUrl();
        String importEmfVersion = LoginWindow.EMF_VERSION;
        String importFileName = file.getName();
        User importUser = session.user();
        Date importDate = new Date();

        // TODO add export/import version compatibility table 
        if (!moduleTypesExportImport.getExportEmfVersion().equals(importEmfVersion)) {
            String message = String.format("Can't import \"%s\": file version \"%s\" incompatible with local version \"%s\".",
                                           importFileName, moduleTypesExportImport.getExportEmfVersion(), importEmfVersion);
            messagePanel.setError(message);
            return;
        }

        String exportMessage = String.format("Exported from server \"%s\" version \"%s\" to \"%s\" by %s on %s.",
                                             moduleTypesExportImport.getExportEmfServer(), moduleTypesExportImport.getExportEmfVersion(),
                                             moduleTypesExportImport.getExportFileName(), moduleTypesExportImport.getExportUserName(),
                                             CustomDateFormat.format_MM_DD_YYYY_HH_mm(moduleTypesExportImport.getExportDate()));

        String importMessage = String.format("Imported on server \"%s\" version \"%s\" from file \"%s\" by %s on %s.",
                                             importEmfServer, importEmfVersion, importFileName, importUser.getName(),
                                             CustomDateFormat.format_MM_DD_YYYY_HH_mm(importDate));
        
        String exportImportMessage = exportMessage + "\n" + importMessage;
        
        StringBuilder changeLog = new StringBuilder();
        
        int datasetTypeMatches = 0;
        int datasetTypeConflicts = 0;
        int datasetTypeAdditions = 0;
        
        for (DatasetType importedDatasetType : moduleTypesExportImport.getDatasetTypes()) {
            String oldDatasetTypeName = importedDatasetType.getName();
            DatasetType localDatasetType = null;
            try {
                localDatasetType = presenter.getDatasetType(oldDatasetTypeName);
            } catch (EmfException e) {
                // e.printStackTrace();
            }
            
            if (localDatasetType == null) { // importedDatasetType not found on the local server
                try {
                    ModuleTypesExportImport.prepareForImport(importedDatasetType, exportImportMessage, importUser, importDate);
                    localDatasetType = presenter.addDatasetType(importedDatasetType);
                } catch (EmfException e) {
                    e.printStackTrace();
                    messagePanel.setError("Failed to add imported dataset type \"" + oldDatasetTypeName + "\": " + e.getMessage());
                    return;
                }
                changeLog.append(String.format("Added imported dataset type \"%s\" to current server.\n", oldDatasetTypeName));
                datasetTypeAdditions++;
            } else { // found a dataset type with the same name on the local server
                StringBuilder differences = new StringBuilder();
                if (ModuleTypeVersionDataset.matchesImportedDatasetType("    ", differences, localDatasetType, importedDatasetType)) {
                    if (differences.length() > 0) {
                        changeLog.append(String.format("Using the local dataset type \"%s\" because it matches the imported dataset type \"%s\":\n%s",
                                                       localDatasetType.getName(), importedDatasetType.getName(), differences));
                    } else {
                        changeLog.append(String.format("Using the local dataset type \"%s\" because it matches the imported dataset type \"%s\".\n\n",
                                                       localDatasetType.getName(), importedDatasetType.getName()));
                    }
                    datasetTypeMatches++;
                } else { // name conflict with a non-matching dataset type
                    changeLog.append(String.format("Found local dataset type \"%s\" but it doesn't match the imported dataset type \"%s\":\n%s",
                                                   localDatasetType.getName(), importedDatasetType.getName(), differences));

                    String newDatasetTypeName = findAvailableDatasetTypeName(oldDatasetTypeName);
                    importedDatasetType.setName(newDatasetTypeName);
                    try {
                        ModuleTypesExportImport.prepareForImport(importedDatasetType, exportImportMessage, importUser, importDate);
                        localDatasetType = presenter.addDatasetType(importedDatasetType);
                    } catch (EmfException e) {
                        e.printStackTrace();
                        messagePanel.setError("Failed to add imported dataset type \"" + oldDatasetTypeName + "\" with modified name \"" + newDatasetTypeName + "\": " + e.getMessage());
                        return;
                    }
                    changeLog.append(String.format("Added imported dataset type \"%s\" with modified name \"%s\" to current server.\n", oldDatasetTypeName, newDatasetTypeName));
                    datasetTypeConflicts++;
                }
            }
            if (localDatasetType == null) {
                messagePanel.setError("Failed to add imported dataset type \"" + oldDatasetTypeName + "\" to current server.");
                return;
            }
            moduleTypesExportImport.replaceDatasetType(importedDatasetType, localDatasetType);
        }

        
        int moduleTypeMatches = 0;
        int moduleTypeConflicts = 0;
        int moduleTypeAdditions = 0;
        
        for(ModuleType importedModuleType : moduleTypesExportImport.getModuleTypes()) {
            String oldModuleTypeName = importedModuleType.getName();
            ModuleType localModuleType = null;
            try {
                localModuleType = presenter.getModuleType(oldModuleTypeName);
            } catch (EmfException e) {
                // e.printStackTrace();
            }

            if (localModuleType == null) { // importedModuleType not found on the local server
                try {
                    importedModuleType.prepareForImport(exportImportMessage, importUser, importDate);
                    localModuleType = presenter.addModuleType(importedModuleType);
                } catch (EmfException e) {
                    e.printStackTrace();
                    messagePanel.setError("Failed to add imported module type \"" + oldModuleTypeName + "\": " + e.getMessage());
                    return;
                }
                changeLog.append(String.format("Added imported module type \"%s\" to current server.\n", oldModuleTypeName));
                moduleTypeAdditions++;
            } else { // found a module type with the same name on the local server
                StringBuilder differences = new StringBuilder();
                if (localModuleType.matchesImportedModuleType("    ", differences, importedModuleType)) {
                    if (differences.length() > 0) {
                        changeLog.append(String.format("Using the local module type \"%s\" because it matches the imported module type \"%s\":\n%s",
                                                       localModuleType.getName(), importedModuleType.getName(), differences));
                    } else {
                        changeLog.append(String.format("Using the local module type \"%s\" because it matches the imported module type \"%s\".\n\n",
                                                       localModuleType.getName(), importedModuleType.getName()));
                    }
                    moduleTypeMatches++;
                } else { // name conflict with a non-matching module type
                    changeLog.append(String.format("Found local module type \"%s\" but it doesn't match the imported module type \"%s\":\n%s",
                                                   localModuleType.getName(), importedModuleType.getName(), differences));

                    String newModuleTypeName = findAvailableModuleTypeName(oldModuleTypeName);
                    importedModuleType.setName(newModuleTypeName);
                    try {
                        importedModuleType.prepareForImport(exportImportMessage, importUser, importDate);
                        localModuleType = presenter.addModuleType(importedModuleType);
                    } catch (EmfException e) {
                        e.printStackTrace();
                        messagePanel.setError("Failed to add imported module type \"" + oldModuleTypeName + "\" with modified name \"" + newModuleTypeName + "\": " + e.getMessage());
                        return;
                    }
                    changeLog.append(String.format("Added imported module type \"%s\" with modified name \"%s\" to current server.\n", oldModuleTypeName, newModuleTypeName));
                    moduleTypeConflicts++;
                }
            }
            if (localModuleType == null) {
                messagePanel.setError("Failed to add imported module type \"" + oldModuleTypeName + "\" to current server.");
                return;
            }
            moduleTypesExportImport.replaceModuleType(importedModuleType, localModuleType);
        }
        
        String summary = String.format("Imported %d dataset type%s (%d matche%s, %d conflict%s, %d addition%s) and %d module type%s (%d matche%s, %d conflict%s, %d addition%s) from \"%s\".",
                                       moduleTypesExportImport.getDatasetTypes().length, (moduleTypesExportImport.getDatasetTypes().length == 1) ? "" : "s",
                                       datasetTypeMatches,     (datasetTypeMatches == 1) ? "" : "s",
                                       datasetTypeConflicts, (datasetTypeConflicts == 1) ? "" : "s",
                                       datasetTypeAdditions, (datasetTypeAdditions == 1) ? "" : "s",
                                       moduleTypesExportImport.getModuleTypes().length, (moduleTypesExportImport.getModuleTypes().length == 1) ? "" : "s",
                                       moduleTypeMatches,     (moduleTypeMatches == 1) ? "" : "s",
                                       moduleTypeConflicts, (moduleTypeConflicts == 1) ? "" : "s",
                                       moduleTypeAdditions, (moduleTypeAdditions == 1) ? "" : "s", file.getName());
        changeLog.append("\n" + summary + "\n");
        showLargeMessage(summary, changeLog.toString());
    }

    private void showLargeErrorMessage(String title, String error) {
        // reusing implementation from ModuleTypeVersionPropertiesWindow
        ModuleTypeVersionPropertiesWindow.showLargeErrorMessage(messagePanel, title, error);
    }

    private void showLargeMessage(String title, String message) {
        // reusing implementation from ModuleTypeVersionPropertiesWindow
        ModuleTypeVersionPropertiesWindow.showLargeMessage(messagePanel, title, message);
    }

    private List selected() {
        return table.selected();
    }

    public EmfConsole getParentConsole() {
        return this.parentConsole;
    }

    @Override
    public void doRefresh() throws EmfException {
        populate();
    }

    @Override
    public void populate() {
        try {
            moduleTypes = presenter.getModuleTypes();
            table.refresh(new ModuleTypesTableData(moduleTypes));
            panelRefresh();
        } catch (EmfException e) {
            messagePanel.setError("Refresh failed: " + e.getMessage());
        }
    }

    @Override
    public void closedChildWindow(ModuleTypeVersion moduleTypeVersion, ViewMode viewMode) {
        // nothing to do
    }
}
