package gov.epa.emissions.framework.client.sms.sectorscenario.editor;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.meta.versions.VersionsSet;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class VersionPanel extends JPanel {

    protected ComboBox comboBox;

    protected String selected;

    private EmfSession session;

    public VersionPanel(EmfSession session, ManageChangeables changeables)
            throws EmfException {
        this.session = session;
        createLayout(changeables);
    }

    public void update(Version[] versions) {
        updateComboModel(versions);
    }

    private void updateComboModel(Version[] versions) {
        VersionsSet versionSet = new VersionsSet(versions);
        String[] labels = labels(versionSet);
        ComboBoxModel model = new DefaultComboBoxModel(labels);
        comboBox.setEnabled(true);
        comboBox.setModel(model);
        comboBox.repaint();
        selected = labels[0];
        comboBox.setSelectedItem(selected); // assumed there is atleast one verion exists(initial version)
    }

    public int datasetVersion() {
        // FIXME: refactor the version set!!!
        int forPerenth = selected.indexOf('(');
        int backPerenth = selected.indexOf(')');
        return Integer.parseInt(selected.substring(forPerenth + 1, backPerenth));
    }

    protected void createLayout(ManageChangeables changeables) throws EmfException {
        super.setLayout(new BorderLayout(5, 5));

        super.add(new JLabel("Version:"), BorderLayout.WEST);

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

    protected ComboBox comboBox() {
        ComboBox comboBox = new ComboBox();
        //comboBox.setPreferredSize(new Dimension(150, 20));
        comboBox.setName("versions");
        return comboBox;
    }

    protected void initialize() throws EmfException {
        EmfDataset[] datasets = {};//controlStrategy.getControlStrategyInputDatasets();
        if (datasets.length == 0) {// new control strategy
            comboBox.setEnabled(false);
        } else {
            Version[] versions = session.dataEditorService().getVersions(datasets[0].getId());
            update(versions);
            selected = getVersion(new VersionsSet(versions));
            comboBox.setSelectedItem(selected);
        }
    }

    protected String getVersion(VersionsSet versionsSet) {
        int ver = 0;//controlStrategy.getDatasetVersion();
        return versionsSet.getVersionName(ver) + " (" + ver + ")";
    }

    protected String[] labels(VersionsSet versionsSet) {
        return versionsSet.nameAndNumbers();
    }

}
