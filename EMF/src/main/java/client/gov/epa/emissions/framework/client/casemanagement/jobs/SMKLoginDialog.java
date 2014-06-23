package gov.epa.emissions.framework.client.casemanagement.jobs;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.Changeable;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.PasswordField;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.gui.buttons.CancelButton;
import gov.epa.emissions.commons.gui.buttons.SaveButton;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.ui.Dialog;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class SMKLoginDialog extends Dialog implements ManageChangeables {

    protected EditJobsTabPresenterImpl presenter;

    private MessagePanel messagePanel;
    
    private TextField host;

    private TextField user;

    private PasswordField password;
    
    public SMKLoginDialog(EmfConsole parent) {
        super("Set SMOKE Login Info", parent);
        super.setSize(new Dimension(320, 200));
        super.setResizable(false);
        super.center();
    }

    public void display() {
        JPanel panel = new JPanel(new BorderLayout());
        messagePanel = new SingleLineMessagePanel();
        panel.add(messagePanel, BorderLayout.NORTH);
        
        panel.add(inputPanel(), BorderLayout.CENTER);
        panel.add(buttonsPanel(), BorderLayout.SOUTH);

        super.getContentPane().add(panel);
        super.display();
    }

    private JPanel inputPanel() {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGen = new SpringLayoutGenerator();
        
        host = new TextField("host", 20);
        layoutGen.addLabelWidgetPair("Host name:", host, panel);
        
        user = new TextField("user", 20);
        layoutGen.addLabelWidgetPair("Login name:", user, panel);
        
        password =  new PasswordField("password", 20);
        layoutGen.addLabelWidgetPair("Password:", password, panel);

        layoutGen.makeCompactGrid(panel, 3, 2, // rows, cols
                5, 5, // initialX, initialY
                5, 10);// xPad, yPad
        
        return panel;
    }

    private JPanel buttonsPanel() {
        JPanel panel = new JPanel();
        Button ok = new SaveButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    cleareMsg();
                    String loginName = user.getText() == null ? "" : user.getText().trim();
                    String hostname = host.getText() == null ? "" : user.getText().trim();
                    char[] pwd = password.getPassword();
                    
                    if (loginName.isEmpty() || hostname.isEmpty() || pwd == null || pwd.length == 0)
                        throw new EmfException("Fields cannot be empty.");
                    
                    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    presenter.setSmokeLogin(loginName, hostname, pwd);
                    setCursor(Cursor.getDefaultCursor());
                    close();
                } catch (EmfException exc) {
                    setCursor(Cursor.getDefaultCursor());
                    messagePanel.setError(exc.getMessage());
                }
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

    public void register(Object presenter) {
        this.presenter = (EditJobsTabPresenterImpl) presenter;
    }
    
    public void addChangeable(Changeable changeable) {
        // NOTE Auto-generated method stub
    }

    private void cleareMsg() {
        this.messagePanel.clear();
    }

    public void resetChanges() {
        // NOTE Auto-generated method stub
        
    }

}
