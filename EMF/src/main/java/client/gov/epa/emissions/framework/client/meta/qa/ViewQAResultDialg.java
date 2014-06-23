package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.gui.buttons.CancelButton;
import gov.epa.emissions.commons.gui.buttons.OKButton;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.ui.Dialog;

import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class ViewQAResultDialg extends Dialog {

    private TextField linesText;

    protected boolean viewAll;
    protected boolean viewNone;
    
    private int lines;
    
    public ViewQAResultDialg(String stepName, EmfConsole parent) {
        super("View QA results choices: "+ stepName, parent);
        super.setSize(new Dimension(380, 130));

        super.getContentPane().add(createLayout());
        super.center();
    }

    private JPanel createLayout() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(inputPanel());
        panel.add(buttonsPanel());

        return panel;
    }

    private JPanel inputPanel() {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        linesText = new TextField("", 10);
        layoutGenerator.addLabelWidgetPair("Number of records to preview (i.e., 1000): ", linesText, panel);
       
        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 1, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }

    private JPanel buttonsPanel() {
        JPanel panel = new JPanel();
        Button ok = new OKButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (verifyInput()) {
                    viewAll = false;
                    viewNone = false;
                    close();
                }
            }
        });
        getRootPane().setDefaultButton(ok);
        panel.add(ok);

        Button viewAllButton = new Button( "ViewAll", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                viewAll = true;
                viewNone = false;
                close();
            }
        });
        panel.add(viewAllButton);
        
        Button cancel = new CancelButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                viewAll = false;
                viewNone = true;
                close();
            }
        });
        panel.add(cancel);

        return panel;
    }

    protected boolean verifyInput() {
       
        try{
            lines= Integer.parseInt(linesText.getText().trim());
        }
        catch (NumberFormatException nfe)
        {
            JOptionPane.showMessageDialog(super.getParent(), 
                    "Please enter a positive number", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if ( lines <= 0) {
            JOptionPane.showMessageDialog(super.getParent(), 
                    "Please enter a positive number", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        return true;
    }

    protected void close() {
        super.dispose();
    }

    public void run() {
        super.display();
    }

    public boolean shouldViewall() {
        return viewAll;
    }
    
    public boolean shouldViewNone() {
        return viewNone;
    }

    public int getLines() {
        return lines;
    }
    
}
