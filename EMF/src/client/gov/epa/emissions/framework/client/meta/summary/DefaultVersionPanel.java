package gov.epa.emissions.framework.client.meta.summary;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.framework.client.meta.versions.VersionsSet;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.ui.MessagePanel;

import java.awt.Dimension;

import javax.swing.JPanel;

public class DefaultVersionPanel extends JPanel {

    private EmfDataset dataset;

    private Version[] versions;
    
    private ComboBox versionCombo; 
    
    private MessagePanel messagePanel;

    private String toolTipText;

    public DefaultVersionPanel(EmfDataset dataset, Version[] versions, ManageChangeables changeables,
       MessagePanel messagePanel) {
        this.dataset = dataset;
        this.versions = versions;
        this.messagePanel = messagePanel;
        createLayout(changeables);
    }

    public DefaultVersionPanel(EmfDataset dataset, Version[] versions, ManageChangeables changeables,
                               MessagePanel messagePanel, String toolTipText) {
        this.dataset = dataset;
        this.versions = versions;
        this.messagePanel = messagePanel;
        this.toolTipText = toolTipText;
        createLayout(changeables);
    }
    private void createLayout(ManageChangeables changeables) {
        //super.add(new JLabel("Default Version:"));

        int defaultVersionNum = dataset.getDefaultVersion();
        VersionsSet versionsSet = new VersionsSet(versions);
        Version [] finalVersions = versionsSet.finalVersionObjects();
        if (finalVersions.length < 1)
        {   
            messagePanel.setError(
                    "There is a problem with your dataset because there are no final versions available");
            return;
        }    
        
        versionCombo = new ComboBox(finalVersions, toolTipText);
        versionCombo.setPreferredSize(new Dimension(175,20));
        versionCombo.setSelectedIndex(getIndexOfDefaultVersion(defaultVersionNum, finalVersions));
        
        versionCombo.setName("defaultVersions");

        changeables.addChangeable(versionCombo);

        super.add(versionCombo);
    }

    private int getIndexOfDefaultVersion(int defaultVersionNum, Version[] finalVersions) {
        int i = 0;
        while ((i < finalVersions.length) && (finalVersions[i].getVersion() != defaultVersionNum)) 
        {
            i++;
        }    
        // if in a previous version, the user had set the default version to a nonfinal version,
        // set the default version back to 0
        if (i == finalVersions.length) 
        {
          i = 0;
          messagePanel.setMessage("Resetting default version back to zero from non-final version");
        }
        return i;
    }
    

    public int getSelectedDefaultVersionNum()
    {
        if (versionCombo == null || (Version)versionCombo.getSelectedItem() == null)
            return 0;
        
        return ((Version)versionCombo.getSelectedItem()).getVersion();
    }

}
