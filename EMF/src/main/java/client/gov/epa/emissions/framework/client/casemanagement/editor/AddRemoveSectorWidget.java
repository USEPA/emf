package gov.epa.emissions.framework.client.casemanagement.editor;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.buttons.AddButton;
import gov.epa.emissions.commons.gui.buttons.RemoveButton;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.ui.ListWidget;

import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.Arrays;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class AddRemoveSectorWidget extends JPanel {

    private ListWidget sectorsList;

    private Sector[] allSectors;

    private EmfConsole parentConsole;
    
    private Button addButton;
    private Button removeButton;

    public AddRemoveSectorWidget(Sector[] allSectors, ManageChangeables changeables, EmfConsole parentConsole) {
        this.allSectors = allSectors;
        this.parentConsole = parentConsole;
        setupLayout(changeables);

    }

    public AddRemoveSectorWidget(Sector[] allSectors) {
        setupLayout();

    }
    public void setSectors(Sector[] sectors) {
        sectorsList.removeAllElements();   
        Arrays.sort(sectors);
        for (int i = 0; i < sectors.length; i++) {
            sectorsList.addElement(sectors[i]);
        }
    }
    
    public Sector[] getSectors() {
        return Arrays.asList(sectorsList.getAllElements()).toArray(new Sector[0]);
    }

    private void setupLayout(ManageChangeables changeables) {
        this.sectorsList = new ListWidget(new Sector[0]);
        changeables.addChangeable(sectorsList);
        
        JScrollPane pane = new JScrollPane(sectorsList);
        JPanel buttonPanel = addRemoveButtonPanel();

        this.setLayout(new BorderLayout(1, 1));
        this.add(pane);
        this.add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void setupLayout() {
        this.sectorsList = new ListWidget(new Sector[0]);
        
        JScrollPane pane = new JScrollPane(sectorsList);

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
                removeSectors();
            }
        };
    }

    private Action addAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                addSectors();
            }
        };
    }

    private void addSectors() {
        SectorChooser sectorSelector = new SectorChooser(allSectors, sectorsList, parentConsole);
        sectorSelector.display();
    }

    private void removeSectors() {
        Object[] removeValues = sectorsList.getSelectedValues();
        sectorsList.removeElements(removeValues);

    }

    public void viewOnly() {
        addButton.setVisible(false);
        removeButton.setVisible(false);  
    }
    
    public void addSector(Sector sector){
        if (!sectorsList.contains(sector))
            sectorsList.addElement(sector);
        sort(); 
    }
    
    private void sort() {
        Sector[] sectors = Arrays.asList(sectorsList.getAllElements()).toArray(new Sector[0]);
        
        if (sectors == null || sectors.length == 0)
            return;
        
        Arrays.sort(sectors);
        sectorsList.removeAllElements();
        
        for (int i = 0; i < sectors.length; i++) {
            sectorsList.addElement(sectors[i]);
        }
    }
   

}
