package gov.epa.emissions.framework.client.moduletype;

import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.buttons.CancelButton;
import gov.epa.emissions.commons.gui.buttons.OKButton;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.meta.EmfImageTool;
import gov.epa.emissions.framework.services.module.ModuleType;
import gov.epa.emissions.framework.services.module.ModuleTypeVersion;
import gov.epa.mims.analysisengine.gui.ScreenUtils;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class ModuleTypeVersionSelectionDialog extends JDialog implements ModuleTypeVersionSelectionView {

    private EmfConsole parent;

    private ModuleTypeVersionSelectionPresenter presenter;

    private ModuleType[] moduleTypes;
    private TreeMap<String, ModuleType> moduleTypeMap;
    String[] moduleTypeNames;

    private TreeMap<String, ModuleTypeVersion> moduleTypeVersionMap;
    String[] moduleTypeVersionNames;
    
    ModuleTypeVersion initialModuleTypeVersion;
    
    ModuleType        selectedModuleType;
    ModuleTypeVersion selectedModuleTypeVersion;

    private ComboBox moduleTypeCB;
    private ComboBox versionCB;

    public ModuleTypeVersionSelectionDialog(EmfConsole parent, ModuleTypeVersion initialModuleTypeVersion) {
        super(parent);
        super.setIconImage(EmfImageTool.createImage("/logo.JPG"));
        this.parent = parent;
        setModal(true);
        this.initialModuleTypeVersion = initialModuleTypeVersion;
    }

    public ModuleTypeVersionSelectionDialog(EmfConsole parent) {
        this(parent, null);
    }

    private void loadModuleTypes() {
        moduleTypes = presenter.getModuleTypes();
        moduleTypeMap = new TreeMap<String, ModuleType>();
        int i = 0;
        for(ModuleType moduleType : moduleTypes) {
            moduleTypeMap.put(moduleType.getName(), moduleType);
        }
        moduleTypeNames = moduleTypeMap.keySet().toArray(new String[] {});
    }

    public void display() {
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout(5, 5));
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.add(selectionPanel(), BorderLayout.NORTH);
        panel.add(buttonPanel(), BorderLayout.SOUTH);
        contentPane.add(panel);
        
        setTitle("Select Module Type Version");           

        this.pack();
        this.setSize(500, 200);
        this.setLocation(ScreenUtils.getPointToCenter(this));
        this.setVisible(true);
    }

    public void observe(ModuleTypeVersionSelectionPresenter presenter) {
        this.presenter = presenter;
    }

    public void refreshModuleTypeVersions(Map<Integer, ModuleTypeVersion> moduleTypeVersions) {
        moduleTypeVersionMap = new TreeMap<String, ModuleTypeVersion>();
        for(ModuleTypeVersion moduleTypeVersion : moduleTypeVersions.values()) {
            moduleTypeVersionMap.put(moduleTypeVersion.versionNameFinal(), moduleTypeVersion);
        }
        moduleTypeVersionNames = moduleTypeVersionMap.keySet().toArray(new String[] {});
        versionCB.resetModel(moduleTypeVersionNames);
    }

    public ModuleTypeVersion getSelectedModuleTypeVersion() {
        return selectedModuleTypeVersion;
    }
    
    private JPanel selectionPanel() {

        JPanel selectionPanel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        loadModuleTypes();

        moduleTypeCB = new ComboBox(moduleTypeNames);
        moduleTypeCB.setMaximumSize(new Dimension(575, 20));
        moduleTypeCB.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                String selectedItem = moduleTypeCB.getSelectedItem().toString();
                if (moduleTypeMap.containsKey(selectedItem)) {
                    selectedModuleType = moduleTypeMap.get(selectedItem);
                    refreshModuleTypeVersions(selectedModuleType.getModuleTypeVersions());
                } else {
                    selectedModuleType = null;
                }
            }
        });
        layoutGenerator.addLabelWidgetPair("Module Type:", moduleTypeCB, selectionPanel);

        versionCB = new ComboBox("Select version", new String[]{});
        versionCB.setMaximumSize(new Dimension(575, 20));
        versionCB.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                String selectedItem = versionCB.getSelectedItem().toString();
                if (moduleTypeVersionMap.containsKey(selectedItem)) {
                    selectedModuleTypeVersion = moduleTypeVersionMap.get(selectedItem);
                } else {
                    selectedModuleTypeVersion = null;
                }
            }
        });
        layoutGenerator.addLabelWidgetPair("Version:", versionCB, selectionPanel);

        if (moduleTypes.length > 0) {
            if (initialModuleTypeVersion == null) {
                moduleTypeCB.setSelectedIndex(0);
                versionCB.setSelectedIndex(1);
            } else {
                moduleTypeCB.setSelectedItem(initialModuleTypeVersion.getModuleType().getName());
                versionCB.setSelectedItem(initialModuleTypeVersion.versionNameFinal());
            }
        }
        
        // Lay out the panel.
        layoutGenerator.makeCompactGrid(selectionPanel, 2, 2, // rows, cols
                10, 10, // initialX, initialY
                10, 10);// xPad, yPad

        return selectionPanel;
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
                selectedModuleType = null;
                selectedModuleTypeVersion = null;
                dispose();
            }
        };
    }

    private Action okAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if ((moduleTypes.length > 0) && (selectedModuleTypeVersion == null)) { 
                    JOptionPane.showMessageDialog(parent, 
                            "Please choose a module type version", 
                            "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    setVisible(false);
                    dispose();
                }
            }
        };
    }
}