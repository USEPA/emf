package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.buttons.AddButton;
import gov.epa.emissions.commons.gui.buttons.RemoveButton;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.data.dataset.InputDatasetSelectionDialog;
import gov.epa.emissions.framework.client.data.dataset.InputDatasetSelectionPresenter;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.ui.ListWidget;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class AddRemoveDatasetWidget extends JPanel {

    private EmfConsole parentConsole;
    
    private EmfSession session;
    
//    private SingleLineMessagePanel messagePanel;
    
    private ListWidget datasetsList;
    
    private Button addButton;
    
    private Button removeButton;
    
    public AddRemoveDatasetWidget(ManageChangeables changeables, String program, EmfConsole parentConsole, EmfSession session) {
        this.parentConsole = parentConsole;
        this.session = session;
        setupLayout(changeables, program);

    }
    
    // called when adding datasets
    private void setDatasets(EmfDataset [] datasets) {
        for (int i = 0; i < datasets.length; i++) {
            if (!isDuplicateDataset(datasetsList.getModel(),datasets[i]))
               datasetsList.addElement(datasets[i]); 
        }
    }

    private boolean isDuplicateDataset(javax.swing.ListModel model, EmfDataset dataset) {
        for (int i = 0; i < model.getSize(); i++)
           if (model.getElementAt(i).equals(dataset))
              return true;
        return false;
    }
    
    public void setDatasetsFromStepWindow(EmfDataset [] datasets) {
        datasetsList.removeElements(datasetsList.getAllElements());
        for (int i = 0; i < datasets.length; i++) {
            datasetsList.addElement(datasets[i]);
        }
    }
    
   public Object[] getDatasets() {
        return datasetsList.getAllElements();
        
    }
   
   private DatasetType getDatasetType(int index) {
       Object[] types = getDatasets();
       if (types == null )
           return null; 
       return (types.length == 0) ? null : ((EmfDataset)types[index]).getDatasetType();
   }
   
    private void setupLayout(ManageChangeables changeables, String program) {
        
        this.datasetsList = new ListWidget(new EmfDataset[0]);
        changeables.addChangeable(datasetsList);
        
        JScrollPane pane = new JScrollPane(datasetsList);
        pane.setPreferredSize(new Dimension(500, 300));
        JPanel buttonPanel = addRemoveButtonPanel(program);
        //JLabel emisLabel = new JLabel("Emission Inventories");
        this.setLayout(new BorderLayout(1, 1));
        //this.add(emisLabel, BorderLayout.WEST);
        this.add(pane);
        this.add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel addRemoveButtonPanel(String program) {
        JPanel panel = new JPanel();
        addButton = new AddButton("Add", addAction(program));
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
            
                datasetsList.removeSelectedElements();
                //System.out.println(parentConsole);
            }
        };
    }

    private Action addAction(final String program) {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doAddWindow(program);
            }
        };
    }

    private void doAddWindow(String program) {
        List<DatasetType> datasetTypeList = new ArrayList<DatasetType>();
        boolean selectSingle = false; 
        try {
            DatasetType[] allDatasetTypes = session.getLightDatasetTypes();
            
            if (program.toLowerCase().startsWith("fire data summary")){
                for (int i = 0; i < allDatasetTypes.length; i++) {
                    //only get dataset "Fire Data Summary (Day-specific)"
                    if (allDatasetTypes[i].getName().startsWith("ORL Day-Specific Fire"))
                        datasetTypeList.add(allDatasetTypes[i]);
                }
            }else if (program.toLowerCase().startsWith("compare control strategies")){
                selectSingle =true; 
                for (int i = 0; i < allDatasetTypes.length; i++) {
                    //get all dataset types that start with ORL
                    if (allDatasetTypes[i].getName().equals(DatasetType.strategyDetailedResult) )
                        datasetTypeList.add(allDatasetTypes[i]);
                }
            }else{   
                for (int i = 0; i < allDatasetTypes.length; i++) {
                    //get all dataset types that start with ORL
                    if (allDatasetTypes[i].getName().startsWith("ORL") 
                            && !allDatasetTypes[i].getName().startsWith("ORL Day-Specific Fire")
                            && !allDatasetTypes[i].getName().startsWith("ORL Fire"))
                        datasetTypeList.add(allDatasetTypes[i]);
                }
                
            }
            // Make an object of the view and presenter of the dialog, and run the presenter's display ().
            // Set the list of datasets in the JList of this widget (which is part of the EditQAEmissionsWindow
            // to that of the datasets retrieved from the presenter.
            InputDatasetSelectionDialog view = new InputDatasetSelectionDialog (parentConsole);
            InputDatasetSelectionPresenter presenter = new InputDatasetSelectionPresenter(view, session, datasetTypeList.toArray(new DatasetType[0]));
            presenter.display(getDatasetType(0), selectSingle);
            setDatasets(presenter.getDatasets());

        } catch (Exception e) {
            e.printStackTrace();
 //            messagePanel.setError(e.getMessage());
        }
    }
    
    public void viewOnly() {
        addButton.setVisible(false);
        removeButton.setVisible(false);  
    }
}
