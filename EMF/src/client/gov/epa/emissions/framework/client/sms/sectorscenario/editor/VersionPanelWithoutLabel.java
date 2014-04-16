package gov.epa.emissions.framework.client.sms.sectorscenario.editor;

import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class VersionPanelWithoutLabel extends VersionPanel {
    
    public VersionPanelWithoutLabel(EmfSession session, ManageChangeables changeables)
            throws EmfException {
        super(session, changeables);
    }

    protected void createLayout(ManageChangeables changeables) throws EmfException {
        super.setLayout(new BorderLayout(5, 5));
        comboBox = comboBox();
        initialize();
        comboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selected = (String) comboBox.getSelectedItem();
            }
        });

        super.add(comboBox);
        changeables.addChangeable(comboBox);
    }
    
}
