package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.buttons.AddButton;
import gov.epa.emissions.commons.gui.buttons.RemoveButton;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.casemanagement.editor.EditableCaseSummaryTabPresenterImpl;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.data.region.RegionChooser;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.GeoRegion;
import gov.epa.emissions.framework.ui.ListWidget;

import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.Arrays;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class AddRemoveRegionsWidget extends JPanel {
    private ListWidget regionsList;
    private GeoRegion[] allRegions;
    private EmfSession session;
    private EmfConsole parentConsole;
    private DesktopManager desktopManager;
    private Button addButton;
    private Button removeButton;
    private Object parentPresenter;

    public AddRemoveRegionsWidget(GeoRegion[] allGrids, ManageChangeables changeables, EmfConsole parentConsole) {
        this.allRegions = allGrids;
        this.parentConsole = parentConsole;
        setupLayout(changeables);

    }

    public AddRemoveRegionsWidget(GeoRegion[] allRegions) {
        setupLayout();

    }
    public void setRegions(GeoRegion[] grids) {
        regionsList.removeAllElements();   
        Arrays.sort(grids);
        for (int i = 0; i < grids.length; i++) {
            regionsList.addElement(grids[i]);
        }
    }
    
    public GeoRegion[] getRegions() {
        return Arrays.asList(regionsList.getAllElements()).toArray(new GeoRegion[0]);
    }

    private void setupLayout(ManageChangeables changeables) {
        this.regionsList = new ListWidget(new GeoRegion[0]);
        changeables.addChangeable(regionsList);
        
        JScrollPane pane = new JScrollPane(regionsList);
        JPanel buttonPanel = addRemoveButtonPanel();

        this.setLayout(new BorderLayout(1, 1));
        this.add(pane);
        this.add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void setupLayout() {
        this.regionsList = new ListWidget(new GeoRegion[0]);
        
        JScrollPane pane = new JScrollPane(regionsList);

        this.setLayout(new BorderLayout(1, 1));
        this.add(pane);
    }


    private JPanel addRemoveButtonPanel() {
        JPanel panel = new JPanel();
        addButton = new AddButton("Add", addAction());
        removeButton = new RemoveButton("Remove", removeAction());
        addButton.setMargin(new Insets(1, 2, 1, 2));      
        removeButton.setMargin(new Insets(1, 2, 1, 2));
        panel.add(addButton);
        panel.add(removeButton);

        return panel;
    }

    private Action removeAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                removeGrids();
            }
        };
    }

    private Action addAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    addRegions();
                } catch (EmfException e1) {
                    JOptionPane.showMessageDialog(parentConsole, e1.getMessage());
                }
            }
        };
    }

    private void addRegions() throws EmfException {
        allRegions = ((EditableCaseSummaryTabPresenterImpl)parentPresenter).getAllGeoRegions();
        RegionChooser regionSelector = new RegionChooser(allRegions, regionsList, parentConsole);
        regionSelector.setDesktopManager(desktopManager);
        regionSelector.setEmfSession(session);
        regionSelector.observeParentPresenter(parentPresenter);
        regionSelector.display();
    }

    private void removeGrids() {
        Object[] removeValues = regionsList.getSelectedValues();
        if (removeValues.length>0){
            try{
                String warnMessage = ""; 
                String[] message = ((EditableCaseSummaryTabPresenterImpl) parentPresenter).isGeoRegionUsed(Arrays.asList(removeValues).toArray(new GeoRegion[0]));
                if(message[0]!=null && message[0].trim().length() > 0)
                    warnMessage = " Cannot remove region " + message[0] + ", used by one or more jobs";
                if(message[1]!=null && message[1].trim().length() > 0)
                    warnMessage += "\n Cannot remove region " + message[1] + ", used by one or more inputs";
                if(message[2]!=null && message[2].trim().length() > 0)
                    warnMessage += "\n Cannot remove region " + message[2] + ", used by one or more params";
                if (warnMessage.trim().length()>0)
                    JOptionPane.showMessageDialog(parentConsole, warnMessage);               
                else
                    regionsList.removeElements(removeValues);
            }catch (EmfException e1) {
                JOptionPane.showMessageDialog(parentConsole, e1.getMessage());
            }
        }
    }

    public void viewOnly() {
        addButton.setVisible(false);
        removeButton.setVisible(false);  
    }
    
    public void addRegion(GeoRegion region){
        if (!regionsList.contains(region))
            regionsList.addElement(region);
        sort(); 
    }
    
    private void sort() {
        GeoRegion[] grids = Arrays.asList(regionsList.getAllElements()).toArray(new GeoRegion[0]);
        
        if (grids == null || grids.length == 0)
            return;
        
        Arrays.sort(grids);
        regionsList.removeAllElements();
        
        for (int i = 0; i < grids.length; i++) {
            regionsList.addElement(grids[i]);
        }
    }
    
    public void setEmfSession(EmfSession session) {
        this.session = session;
    }
    
    public void setDesktopManager(DesktopManager dm) {
        this.desktopManager = dm;
    }
    
    public void observeParentPresenter(Object presenter) {
        this.parentPresenter = presenter;
    }

}
