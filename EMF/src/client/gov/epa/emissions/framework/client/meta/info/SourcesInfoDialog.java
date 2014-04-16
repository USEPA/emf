package gov.epa.emissions.framework.client.meta.info;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.buttons.CancelButton;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;
import gov.epa.mims.analysisengine.gui.ScreenUtils;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class SourcesInfoDialog extends JDialog {

    private SingleLineMessagePanel messagePanel;

    private JTextField numOfSrcsField;
    
    private static final int OK = -1;
    
    private static final int CANCEL = 0;
    
    private int numOfSources = CANCEL;
    
    private JButton okButton;

    public SourcesInfoDialog(String title, int totalNumber, Component container, EmfConsole parentConsole) {
        super(parentConsole);
        super.setTitle(title);
        super.setLocation(ScreenUtils.getCascadedLocation(container, container.getLocation(), 300, 300));
        super.setModal(true);

        this.getContentPane().add(createLayout(totalNumber));
    }
    
    public int showDialog() {
        this.pack();
        this.setVisible(true);
        
        return numOfSources;
    }

    private JPanel createLayout(int total) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        messagePanel = new SingleLineMessagePanel();
        panel.add(messagePanel);
        panel.add(createInputFieldPanel(total));
        panel.add(createButtonsPanel());

        return panel;
    }

    private JPanel createInputFieldPanel(int total) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        
        String msg = "<html>There are " + total + " external file sources.<br>" +
        		"How many would you like to view?";
        JLabel msgLabel = new JLabel();
        msgLabel.setText(msg);
        panel.add(msgLabel, BorderLayout.CENTER);

        numOfSrcsField = new JTextField(10);
        numOfSrcsField.setName("numofsrcsfolder");
        
        panel.add(numOfSrcsField, BorderLayout.SOUTH);
        panel.setBorder(BorderFactory.createEmptyBorder(2, 40, 10, 60));

        return panel;
    }
    
    private JPanel createButtonsPanel() {
        JPanel panel = new JPanel();

        JPanel container = new JPanel();
        FlowLayout layout = new FlowLayout();
        layout.setHgap(10);
        layout.setVgap(5);
        container.setLayout(layout);

        okButton = new Button("OK", okAction());
        container.add(okButton);
        getRootPane().setDefaultButton(okButton);

        JButton cancelButton = new CancelButton(cancelAction());
        container.add(cancelButton);

        panel.add(container);

        return panel;
    }

    private Action okAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e){
                messagePanel.clear();
                
                try {
                    checkFolderField();
                    dispose();
                } catch (Exception e1) {
                    messagePanel.clear();
                    messagePanel.setError(e1.getMessage());
                }
            }
        };
    }

    private Action cancelAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e){ 
                numOfSources = CANCEL;
                dispose();
            }
        };
    }
    
    private void checkFolderField() throws EmfException {
        String num = numOfSrcsField.getText();
        
        try {
            if (num == null || num.trim().isEmpty()) {
                numOfSources = OK;
                return;
            }
            
            numOfSources = Integer.parseInt(num);
        } catch (NumberFormatException e) {
            throw new EmfException("Please specify a valid integer number.");
        }
    }
}
