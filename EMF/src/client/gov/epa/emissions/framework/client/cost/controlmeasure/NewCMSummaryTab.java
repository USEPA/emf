package gov.epa.emissions.framework.client.cost.controlmeasure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.DefaultComboBoxModel;

import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.ControlMeasureClass;
import gov.epa.emissions.framework.ui.MessagePanel;

public class NewCMSummaryTab extends ControlMeasureSummaryTab {

    public NewCMSummaryTab(ControlMeasure measure, EmfSession session, MessagePanel messagePanel,
            ManageChangeables changeablesList) {
        super(measure, session, messagePanel, changeablesList, null);
        super.setName("summary");
    }

    public void populateValues() {
        super.populateFields();
        deviceCode.setText("");
        equipmentLife.setText("");
        lastModifiedBy.setText("");
        
        try {
            ControlMeasureClass[] allClasses = session.controlMeasureService().getMeasureClasses();
            if ( this.checkIfSuperUser()) {
                //cmClass.setModel(new DefaultComboBoxModel(allClasses));  //= new ComboBox("Choose a class", allClasses);
                List list = new ArrayList(Arrays.asList(allClasses));
                if (!list.contains("Choose a class"))
                    list.add(0, "Choose a class");

                cmClass.setModel(new DefaultComboBoxModel(list.toArray()));
                cmClass.setEnabled(true);
            } else {
                ControlMeasureClass[] cmClasses = new ControlMeasureClass[1];
                for ( int i=0; i<allClasses.length; i++) {
                    if ( allClasses[i].getName().equals( "Temporary")){
                        cmClasses[0] = allClasses[i];
                        break;
                    }
                }
                List list = new ArrayList(Arrays.asList(cmClasses));
                if (!list.contains("Choose a class"))
                    list.add(0, "Choose a class");

                cmClass.setModel(new DefaultComboBoxModel(list.toArray()));
                //cmClass.setModel(new DefaultComboBoxModel(cmClasses));
                cmClass.setSelectedIndex(1);
                cmClass.setEnabled(false);
            }
        } catch (EmfException e1) {
            messagePanel.setError("Could not retrieve control measure classes");
        }
    }

    public void save(ControlMeasure measure) throws EmfException {
        super.save(measure);
    }

}
