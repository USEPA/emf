package gov.epa.emissions.framework.client.data.region;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.buttons.CancelButton;
import gov.epa.emissions.commons.gui.buttons.OKButton;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.meta.EmfImageTool;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.GeoRegion;
import gov.epa.emissions.framework.ui.ListWidget;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;
import gov.epa.mims.analysisengine.gui.ScreenUtils;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Arrays;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class RegionChooser extends JDialog {

    private GeoRegion[] allRegions;

    private ListWidget allGridsListwidget;

    private ListWidget gridsListWidget;
    
    private EmfSession session;

    private EmfConsole parentConsole;
    
    private DesktopManager desktopManager;
    
    private final MessagePanel messagePanel;

    private Object parentPresenter;
    
    public RegionChooser(GeoRegion[] allGrids, ListWidget gridsListWidget, EmfConsole parentConsole) {
        super(parentConsole);
        super.setIconImage(EmfImageTool.createImage("/logo.JPG"));
        
        setTitle("Select Regions");
        this.allRegions = allGrids;
        this.gridsListWidget = gridsListWidget;
        this.parentConsole = parentConsole;
        messagePanel = new SingleLineMessagePanel();
    }

    public void display() {
        JScrollPane pane = listWidget();
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(messagePanel, BorderLayout.NORTH);
        panel.add(pane, BorderLayout.CENTER);
        panel.add(buttonPanel(), BorderLayout.SOUTH);
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(panel);

        pack();
        setSize(300, 300);
        setLocation(ScreenUtils.getCascadedLocation(this, parentConsole.getLocation(), 10, 100));
        //setModal(true);
        setVisible(true);
    }

    private JScrollPane listWidget() {
        allGridsListwidget = new ListWidget(allRegions);
        JScrollPane pane = new JScrollPane(allGridsListwidget);
        return pane;
    }

    private JPanel buttonPanel() {
        Button newRegionButton = new Button("New", addNewRegionAction());
        newRegionButton.setMnemonic(KeyEvent.VK_N);
        Button editButton = new Button("Edit", editRegionAction());
        editButton.setMnemonic(KeyEvent.VK_E);
        OKButton okButton = new OKButton(okAction());
        CancelButton cancelButton = new CancelButton(cancelAction());
        
        JPanel buttonPanel = new JPanel();
        
        buttonPanel.add(newRegionButton);
        buttonPanel.add(editButton);
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        return buttonPanel;
    }

    private Action cancelAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                disposeView();
            }
        };
    }

    private Action okAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                setSelectedValues();
                disposeView();
            }
        };
    }
    
    private Action addNewRegionAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                messagePanel.clear();
                RegionEditorWindow view = new RegionEditorWindow(gridsListWidget, allGridsListwidget, desktopManager, parentConsole);
                RegionEditorPresenter regionPresenter = new RegionEditorPresenter(parentPresenter, session);
                
                try {
                    view.observe(regionPresenter);
                    view.setRegion(new GeoRegion(), true);
                    regionPresenter.display(view);
                } catch (EmfException exc) {
                    messagePanel.setError(exc.getMessage());
                    return;
                }
                
                disposeView();
            }
        };
    }
    
    private Action editRegionAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                messagePanel.clear();
                Object[] selection = allGridsListwidget.getSelectedValues();
                
                if (selection == null || selection.length == 0) {
                    messagePanel.setError("Please select a region to edit.");
                    return;
                }
                
                if (selection.length > 1) {
                    messagePanel.setError("Please select only one region to edit.");
                    return;
                }
                
                RegionEditorWindow view = new RegionEditorWindow(gridsListWidget, allGridsListwidget, desktopManager, parentConsole);
                RegionEditorPresenter regionPresenter = new RegionEditorPresenter(parentPresenter, session);
                
                try {
                    view.observe(regionPresenter);
                    view.setRegion((GeoRegion)selection[0], false);
                    regionPresenter.display(view);
                } catch (EmfException exc) {
                    messagePanel.setError(exc.getMessage());
                    return;
                }
            }
        };
    }

    private void disposeView() {
        dispose();
        setVisible(false);
    }

    private void setSelectedValues() {
        Object[] values = allGridsListwidget.getSelectedValues();
        GeoRegion[] selectedValues = Arrays.asList(values).toArray(new GeoRegion[0]);
        addNewGrids(selectedValues);
    }

    private void addNewGrids(GeoRegion[] selected) {
        for (int i = 0; i < selected.length; i++) {
            if (!gridsListWidget.contains(selected[i]))
                gridsListWidget.addElement(selected[i]);
        }
        sort(); 
    }
    
    private void sort() {
        GeoRegion[] grids = Arrays.asList(gridsListWidget.getAllElements()).toArray(new GeoRegion[0]);
        Arrays.sort(grids);
        gridsListWidget.removeAllElements();
        for (int i = 0; i < grids.length; i++) {
            gridsListWidget.addElement(grids[i]);
        }
    }
    
    public void setEmfSession (EmfSession session) {
        this.session = session;
    }
    
    public void setParentConsole (EmfConsole console) {
        this.parentConsole = console;
    }
    
    public void setDesktopManager(DesktopManager dm) {
        this.desktopManager = dm;
    }
    
    public void observeParentPresenter(Object parentPresenter) {
        this.parentPresenter = parentPresenter;
    }
   
}
