package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.buttons.AddButton;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.ui.ListWidget;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.Arrays;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

public class SelectDTypePanel extends JPanel {

    private ListWidget includeDSTypeList;
    
    private ListWidget excludeDSTypeList;

    private ManageChangeables changeablesList;
    
    private JScrollPane inDTscrollPane;
    
    private JScrollPane exDTscrollPane;

    private User user;

    public SelectDTypePanel(User user, ManageChangeables changeableList, DatasetType[] eDatasetTypes, DatasetType[] iDatasetTypes) {
        this.user = user;
        this.changeablesList = changeableList;
        createLayout(eDatasetTypes, iDatasetTypes);
        this.setSize(new Dimension(500, 540));
    }

    private void createLayout(DatasetType[] eDatasetTypes, DatasetType[] iDatasetTypes) {
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        
        this.add(Box.createRigidArea(new Dimension(10,0)));
        this.add(createIncludePanel(iDatasetTypes));
        this.add(Box.createRigidArea(new Dimension(10,0)));
        this.add(createButtonsPanel());
        this.add(Box.createRigidArea(new Dimension(10,0)));
        this.add(createExcludePanel(eDatasetTypes));  
        this.add(Box.createRigidArea(new Dimension(10,0)));
    }
    
    private JPanel createExcludePanel(DatasetType[] eDatasetTypes) {  
        excludeDSTypeList = new ListWidget(new DatasetType[0]);
        excludeDSTypeList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        sort(eDatasetTypes, excludeDSTypeList);
 
        changeablesList.addChangeable(excludeDSTypeList);
        exDTscrollPane = new JScrollPane(excludeDSTypeList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        exDTscrollPane.setPreferredSize(new Dimension(200, 300));
        JPanel panel = new JPanel(new BorderLayout(20, 15));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(new JLabel("Hidden Dataset types: "));
        panel.add(exDTscrollPane);
        return panel;
    }
    
    private JPanel createIncludePanel(DatasetType[] iDatasetTypes) {
        includeDSTypeList = new ListWidget(new DatasetType[0]);
        includeDSTypeList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        sort(iDatasetTypes, includeDSTypeList);
 
        changeablesList.addChangeable(includeDSTypeList);
        inDTscrollPane = new JScrollPane(includeDSTypeList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        inDTscrollPane.setPreferredSize(new Dimension(200, 300));
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(new JLabel("Visible Dataset types: "));
        panel.add(inDTscrollPane);
        return panel;
    }
    
    private JPanel createButtonsPanel() {
        JPanel buttonPanel =  new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        JPanel includeButtonPanel =  new JPanel();
        includeButtonPanel.setLayout(new BorderLayout(0, 0));
        includeButtonPanel.setPreferredSize(new Dimension(80, 45));
        includeButtonPanel.setMinimumSize(new Dimension(80, 45));
        JPanel excludeButtonPanel =  new JPanel();
        excludeButtonPanel.setLayout(new BorderLayout(0, 0));
        excludeButtonPanel.setPreferredSize(new Dimension(80, 45));
        excludeButtonPanel.setMinimumSize(new Dimension(80, 45));
        Button includeButton = new AddButton("<<Show", includeAction());
        includeButtonPanel.add(includeButton, BorderLayout.SOUTH);
//        JPanel excludeButtonPanel =  new JPanel();
//        excludeButtonPanel.setLayout(new BorderLayout(0, 0));
//        excludeButtonPanel.setPreferredSize(new Dimension(80, 45));
        Button excludeButton = new AddButton("Hide>>", excludeAction());
        excludeButtonPanel.add(excludeButton, BorderLayout.NORTH);
        buttonPanel.add(includeButtonPanel);
        buttonPanel.add(Box.createRigidArea(new Dimension(0,10)));
        buttonPanel.add(excludeButtonPanel);
        return buttonPanel;
    }
    
    private Action includeAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                DatasetType[] objects= Arrays.asList(excludeDSTypeList.getSelectedValues()).toArray(new DatasetType[0]);
                if ( objects != null && objects.length> 0 )
                    addToIncludeListWidget(objects);
            }
        };
    }
    
    private Action excludeAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                DatasetType[] selected= Arrays.asList(includeDSTypeList.getSelectedValues()).toArray(new DatasetType[0]);
                if ( selected != null && selected.length> 0 )
                    addToExcludeListWidget(selected);
            }
        };
    }
    
    private void addToExcludeListWidget(DatasetType[] dtypes) {
//        System.out.print( "Add to exclude types: " + dtypes.length );
//        System.out.print( "\nTotal to exclude types: " + excludeDSTypeList.getAllElements().length);
        for (int i=0; i< dtypes.length; i++ ) {
            if (!excludeDSTypeList.contains(dtypes[i])) {
                excludeDSTypeList.addElement(dtypes[i]);
            }   
        }
        
        includeDSTypeList.removeElements(dtypes);
        sort(Arrays.asList(excludeDSTypeList.getAllElements()).toArray(new DatasetType[0]), excludeDSTypeList);
        sort(Arrays.asList(includeDSTypeList.getAllElements()).toArray(new DatasetType[0]), includeDSTypeList);
    }

    private void addToIncludeListWidget(DatasetType[] dtypes) {      
        for (Object ds : dtypes) {
            if (!includeDSTypeList.contains(ds)) {
                includeDSTypeList.addElement(ds);
            }
        }
        excludeDSTypeList.removeElements(dtypes);
        sort(Arrays.asList(excludeDSTypeList.getAllElements()).toArray(new DatasetType[0]), excludeDSTypeList);
        sort(Arrays.asList(includeDSTypeList.getAllElements()).toArray(new DatasetType[0]), includeDSTypeList);
    }
    
    public DatasetType[] getExcludedDTs() { 
        DatasetType[] objects= Arrays.asList(excludeDSTypeList.getAllElements()).toArray(new DatasetType[0]);
        return objects;
    }  
    
    private void sort(DatasetType[] grids, ListWidget list) {
        Arrays.sort(grids);
        list.removeAllElements();

        for (int i = 0; i < grids.length; i++) {
            list.addElement(grids[i]);
        }
    }

}
