package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.commons.data.ProjectionShapeFile;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.buttons.CancelButton;
import gov.epa.emissions.commons.gui.buttons.ExportButton;
import gov.epa.emissions.framework.client.Label;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.ui.Dialog;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class ExportSelectionDialog extends Dialog {

    private boolean shouldCreateCSV = false;
    
    private boolean shouldCreateShapeFile = false;

    private MessagePanel messagePanel;
    
    private JCheckBox csvFormat;

    private JCheckBox shapeFileFormat;
    
    private ComboBox pollutant;
    
    private ComboBox projectionShapeFile;
    
//    private EmfConsole parent;
//    
//    private EmfSession session;
    
    private ProjectionShapeFile[] projectionShapeFiles;
    
    private Pollutant[] pollutants;
    
    public ExportSelectionDialog(EmfConsole parent, ProjectionShapeFile[] projectionShapeFiles, Pollutant[] pollutants) {
        super("Export QA Step Results " , parent);
        super.setSize(new Dimension(450, 260));
        super.center();
        setModal(true);
        this.projectionShapeFiles = projectionShapeFiles;
        this.pollutants = pollutants;
//        this.parent = parent;
//        this.session = session;
    }

    public void display() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(mainPanel());
        panel.add(buttonsPanel());

        super.getContentPane().add(panel);
        super.display();
    }

    private JPanel mainPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(1, 1));
        messagePanel = new SingleLineMessagePanel();
        panel.add(messagePanel, BorderLayout.NORTH);
        panel.add(new Label("empty", "  "), BorderLayout.LINE_START);
        panel.add(formatBox(),BorderLayout.CENTER);
        
        panel.add(pollAndShape(), BorderLayout.SOUTH);
        return panel;
    }
    
    private JPanel formatBox(){
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(new Label("Export Format: "), BorderLayout.NORTH);
        csvFormat = new JCheckBox("CSV");
        csvFormat.setSelected(false);
        panel.add(csvFormat,BorderLayout.CENTER);

        shapeFileFormat = new JCheckBox("ShapeFile");
        shapeFileFormat.setSelected(false);
        shapeFileFormat.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (shapeFileFormat.isSelected()){
                    pollutant.setEnabled(true);
                    projectionShapeFile.setEnabled(true);
                }
                if (!shapeFileFormat.isSelected()){
                    pollutant.setEnabled(false);
                    projectionShapeFile.setEnabled(false);
                }
            }
        });
        panel.add(shapeFileFormat, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel pollAndShape() {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        pollutant = new ComboBox(pollutants);
        pollutant.setEnabled(false);
        if (pollutant.getItemCount() > 0)
            pollutant.setSelectedIndex(0);
        layoutGenerator.addLabelWidgetPair("Pollutant to Include: ", pollutant, panel);
        projectionShapeFile = new ComboBox(projectionShapeFiles);
        projectionShapeFile.setEnabled(false);
        if (projectionShapeFile.getItemCount() > 0)
            projectionShapeFile.setSelectedIndex(0);
        layoutGenerator.addLabelWidgetPair("Output Shapefile Template:", projectionShapeFile, panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 2, 2, // rows, cols
                25, 5, // initialX, initialY
                5, 5);// xPad, yPad

        return panel;
    }

    private JPanel buttonsPanel() {
        JPanel panel = new JPanel();
        Button ok = new ExportButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                cleareMsg();
                exportFile();
            }
        });
        getRootPane().setDefaultButton(ok);
        panel.add(ok);

        Button cancel = new CancelButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                close();
            }
        });
        panel.add(cancel);
        
        panel.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));

        return panel;
    }

    private void exportFile() {
        try {
            if (shapeFileFormat.isSelected()) {
                if (projectionShapeFile.getSelectedItem() == null)
                    throw new EmfException("Shape file must be specified");
                if (pollutant.getSelectedItem() == null)
                    throw new EmfException("Pollutant must be specified");
            }
            if (csvFormat.isSelected())
                shouldCreateCSV = true; 
            if (shapeFileFormat.isSelected())
                shouldCreateShapeFile = true; 
            setVisible(false);
            dispose();
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
    }

//    private void checkShapeFileName() throws EmfException{
//        if (projectionShapeFile.getSelectedItem() == null)
//            throw new EmfException("Shape file must be specified");
//    }

    private void cleareMsg() {
        this.messagePanel.clear();
    }

   public boolean shouldCreateCSV(){
       return shouldCreateCSV;
   }
   
   public boolean shouldCreateShapeFile(){
       return shouldCreateShapeFile;
   }

   public Pollutant getPollutant() {
       return (Pollutant)pollutant.getSelectedItem();
   }

   public ProjectionShapeFile getProjectionShapeFile() {
       return (ProjectionShapeFile)projectionShapeFile.getSelectedItem();
   }
}
