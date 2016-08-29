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
    private Map<String, ModuleType> moduleTypeMap;
    String[] moduleTypeNames;
    
    ModuleType        selectedModuleType;
    ModuleTypeVersion selectedModuleTypeVersion;

    private ComboBox moduleTypeCB;

    private ComboBox versionCB;

    public ModuleTypeVersionSelectionDialog(EmfConsole parent) {
        super(parent);
        super.setIconImage(EmfImageTool.createImage("/logo.JPG"));
        this.parent = parent;
        setModal(true);
    }

    private void loadModuleTypes() {
        moduleTypes = presenter.getModuleTypes();
        moduleTypeMap = new HashMap<String, ModuleType>();
        moduleTypeNames = new String[moduleTypes.length];
        int i = 0;
        for(ModuleType moduleType : moduleTypes) {
            moduleTypeMap.put(moduleType.getName(), moduleType);
            moduleTypeNames[i++] = moduleType.getName();
        }
        Arrays.sort(moduleTypeNames);
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
        String[] versionNames = new String[moduleTypeVersions.size()];
        SortedSet<Integer> sortedVersions = new TreeSet<Integer>(moduleTypeVersions.keySet());
        int i = 0;
        for(int version : sortedVersions) {
            ModuleTypeVersion moduleTypeVersion = moduleTypeVersions.get(version);
            versionNames[i++] = moduleTypeVersion.getVersion() + " - " + moduleTypeVersion.getName() + (moduleTypeVersion.getIsFinal() ? " - Final" : "");
        }
        versionCB.resetModel(versionNames);
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
                selectedModuleType = moduleTypeMap.get(selectedItem);
                refreshModuleTypeVersions(selectedModuleType.getModuleTypeVersions());
            }
        });
        layoutGenerator.addLabelWidgetPair("Module Type:", moduleTypeCB, selectionPanel);

        versionCB = new ComboBox("Select version", new String[]{});
        versionCB.setMaximumSize(new Dimension(575, 20));
        versionCB.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                int selectedIndex = versionCB.getSelectedIndex();
                if (selectedIndex > 0) {
                    selectedModuleTypeVersion = selectedModuleType.getModuleTypeVersions().get(selectedIndex - 1);
                }
            }
        });
        layoutGenerator.addLabelWidgetPair("Version:", versionCB, selectionPanel);

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