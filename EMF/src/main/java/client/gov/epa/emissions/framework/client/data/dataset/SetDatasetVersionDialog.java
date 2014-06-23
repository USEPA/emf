package gov.epa.emissions.framework.client.data.dataset;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.buttons.CancelButton;
import gov.epa.emissions.commons.gui.buttons.OKButton;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.meta.EmfImageTool;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DatasetVersion;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.mims.analysisengine.gui.ScreenUtils;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class SetDatasetVersionDialog extends JDialog implements SetDatasetVersionView {

    private EmfDataset dataset;
    
    private ComboBox versionCombo;
    
    private Version version;

    private SetDatasetVersionPresenter presenter;

    public SetDatasetVersionDialog(EmfConsole parent, EmfDataset dataset) {
        super(parent);
        super.setIconImage(EmfImageTool.createImage("/logo.JPG"));
        this.dataset = dataset;
    }

    public void display() {
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout(5, 10));
  
        try {
            contentPane.add(createSection(), BorderLayout.CENTER);
        } catch (EmfException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }

        setTitle("Set Version: " + dataset.getName());
        this.pack();
        this.setSize(450,130);
        this.setLocation(ScreenUtils.getPointToCenter(this));
        this.setVisible(true);
    }
   
    private JPanel createSection() throws EmfException {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(createPropertySection(), BorderLayout.CENTER);
        panel.add(buttonPanel(), BorderLayout.SOUTH);
        return panel;  
    }
    
    private JPanel createPropertySection() throws EmfException{
        JPanel panel = new JPanel(new SpringLayout());

        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();
        Version[] versions = presenter.getVersions(dataset);
        versionCombo =new ComboBox(versions);           
 //       versionCombo.setSize(new Dimension(200,10));
        versionCombo.setPreferredSize(new Dimension(300,15));
        versionCombo.setSelectedIndex(getDefaultVersionIndex(versions, dataset));

        layoutGenerator.addLabelWidgetPair("Version:", versionCombo, panel);
        layoutGenerator.makeCompactGrid(panel, 1, 2, // rows, cols
                55, 15, // initialX, initialY
                5, 15);// xPad, yPad
        return panel;
    }

    private int getDefaultVersionIndex(Version[] versions, EmfDataset dataset) {
        int defaultversion = dataset.getDefaultVersion();

        for (int i = 0; i < versions.length; i++)
            if (defaultversion == versions[i].getVersion())
                return i;

        return 0;
    }
 
    private JPanel buttonPanel() {
        JPanel panel = new JPanel();
        panel.add(new OKButton(okAction()));
        panel.add(new CancelButton(cancelAction()));
        return panel;
    }

    private Action cancelAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                dispose();
            }

        };
    }

    private Action okAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e){            
                version = (dataset !=null ? (Version) versionCombo.getSelectedItem(): null);
                presenter.doSetVersion(new DatasetVersion(dataset, version));
                dispose();
            }
        };
    }

    public void observe(SetDatasetVersionPresenter presenter) {
        this.presenter = presenter;
    }
}
