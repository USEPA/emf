package gov.epa.emissions.framework.client.cost.controlmeasure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTextField;

import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.ControlMeasureClass;
import gov.epa.emissions.framework.ui.MessagePanel;

public class EditableCMSummaryTab extends ControlMeasureSummaryTab{

    public EditableCMSummaryTab(ControlMeasure measure, EmfSession session, MessagePanel messagePanel,
            ManageChangeables changeablesList, EmfConsole parentConsole) {
        super(measure, session, messagePanel, changeablesList, parentConsole);
        super.setName("summary");

    }

    public void populateValues() {
        super.populateFields();

        if ( this.checkIfSuperUser()) {
            cmClass.setEnabled(true);
        } else {
            cmClass.setEnabled(false);
        }
        
    }

    public void save(ControlMeasure measure) throws EmfException {
        super.save(measure);
    }

}
