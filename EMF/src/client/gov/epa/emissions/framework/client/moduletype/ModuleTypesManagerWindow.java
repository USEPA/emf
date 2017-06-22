package gov.epa.emissions.framework.client.moduletype;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ConfirmDialog;
import gov.epa.emissions.commons.gui.SelectAwareButton;
import gov.epa.emissions.commons.gui.buttons.CloseButton;
import gov.epa.emissions.commons.gui.buttons.NewButton;
import gov.epa.emissions.commons.gui.buttons.RemoveButton;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.ReusableInteralFrame;
import gov.epa.emissions.framework.client.ViewMode;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.module.LiteModule;
import gov.epa.emissions.framework.services.module.LiteModuleType;
import gov.epa.emissions.framework.services.module.ModuleType;
import gov.epa.emissions.framework.services.module.ModuleTypeVersion;
import gov.epa.emissions.framework.services.module.ModuleTypeVersionDataset;
import gov.epa.emissions.framework.services.module.ModuleTypesExportImport;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.RefreshButton;
import gov.epa.emissions.framework.ui.RefreshObserver;
import gov.epa.emissions.framework.ui.SelectableSortFilterWrapper;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
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
        table = new SelectableSortFilterWrapper(parentConsole, new ModuleTypesTableData(new ModuleType[] {}), null);
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
        int[] selectedModuleTypeIds = new int[selectedModuleTypes.length];
        for (int i = 0; i < selectedModuleTypes.length; i++) {
            ModuleType selectedModuleType = selectedModuleTypes[i]; 
            selectedModuleTypeIds[i] = selectedModuleType.getId(); 
            if (selectedModuleType.isLocked()) {
                String error = String.format("Can't remove the %s module type: it's locked by %s", selectedModuleType.getName(), selectedModuleType.getLockOwner());
                messagePanel.setError(error);
                return;
            }
        }
        
        int count = 0;
        ConcurrentSkipListMap<Integer, LiteModule> liteModules = session.getLiteModules();
        for(LiteModule liteModule : liteModules.values()) {
            LiteModuleType liteModuleType = liteModule.getLiteModuleTypeVersion().getLiteModuleType(); 
            for(ModuleType selectedModuleType : selectedModuleTypes) {
                if (liteModuleType.getId() == selectedModuleType.getId()) {
                    count++;
                    if (liteModule.isLocked()) {
                        String error = String.format("Can't remove the %s module type: the %s module is locked by %s", selectedModuleType.getName(), liteModule.getName(), liteModule.getLockOwner());
                        messagePanel.setError(error);
                        return;
                    }
                }
            }
        }

        String message = "";
        if (count > 0) {
            message = String.format("Are you sure you want to remove the selected %d module type(s)? The %d module(s) that use this module type(s) will also be removed. There is no undo for this action.", selectedModuleTypes.length, count);
        } else {
            message = String.format("Are you sure you want to remove the selected %d module type(s)? There is no undo for this action.", selectedModuleTypes.length);
        }
        
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
        
        ModuleTypesExportImport moduleTypesExportImport = new ModuleTypesExportImport(datasetTypesMap, moduleTypesList);
        moduleTypesExportImport.prepareForExport(file.getName(), session.user());
        
        saveToXML(filename, moduleTypesExportImport);
        messagePanel.setMessage(moduleTypesExportImport.getModuleTypes().length + " module type(s) exported successfully to: " + file.getName());
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
        
        ModuleTypesExportImport moduleTypesExportImport = readFromXML(filename);
        moduleTypesExportImport.prepareForImport(file.getName(), session.user());

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
                        changeLog.append(String.format("Using the local dataset type \"%s\" because it matches the imported dataset type \"%s\".\n",
                                                       localDatasetType.getName(), importedDatasetType.getName()));
                    }
                    datasetTypeMatches++;
                } else { // name conflict with a non-matching dataset type
                    changeLog.append(String.format("Found local dataset type \"%s\" but it doesn't match the imported dataset type \"%s\":\n%s",
                                                   localDatasetType.getName(), importedDatasetType.getName(), differences));

                    String newDatasetTypeName = findAvailableDatasetTypeName(oldDatasetTypeName);
                    importedDatasetType.setName(newDatasetTypeName);
                    try {
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
                        changeLog.append(String.format("Using the local module type \"%s\" because it matches the imported module type \"%s\".\n",
                                                       localModuleType.getName(), importedModuleType.getName()));
                    }
                    moduleTypeMatches++;
                } else { // name conflict with a non-matching module type
                    changeLog.append(String.format("Found local module type \"%s\" but it doesn't match the imported module type \"%s\":\n%s",
                                                   localModuleType.getName(), importedModuleType.getName(), differences));

                    String newModuleTypeName = findAvailableModuleTypeName(oldModuleTypeName);
                    importedModuleType.setName(newModuleTypeName);
                    try {
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
        
        String summary = String.format("Imported %d dataset type%s (%d matche%s, %d conflict%s, %d addition%s) and %d module type%s (%d matche%s, %d conflict%s, %d addition%s) from \"%s\"",
                                       moduleTypesExportImport.getDatasetTypes().length, (moduleTypesExportImport.getDatasetTypes().length == 1) ? "" : "s",
                                       datasetTypeMatches,     (datasetTypeMatches == 1) ? "" : "s",
                                       datasetTypeConflicts, (datasetTypeConflicts == 1) ? "" : "s",
                                       datasetTypeAdditions, (datasetTypeAdditions == 1) ? "" : "s",
                                       moduleTypesExportImport.getModuleTypes().length, (moduleTypesExportImport.getModuleTypes().length == 1) ? "" : "s",
                                       moduleTypeMatches,     (moduleTypeMatches == 1) ? "" : "s",
                                       moduleTypeConflicts, (moduleTypeConflicts == 1) ? "" : "s",
                                       moduleTypeAdditions, (moduleTypeAdditions == 1) ? "" : "s", file.getName());
        changeLog.append("\n\n" + summary + "\n\n");
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
            ModuleType[] types = presenter.getModuleTypes();
            table.refresh(new ModuleTypesTableData(types));
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
