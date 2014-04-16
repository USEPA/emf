package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.PasswordField;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.gui.Widget;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.AddRemoveWidget;
import gov.epa.emissions.framework.ui.Border;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class RegisterUserProfilePanel extends JPanel {

    private Widget username;

    private PasswordField password;

    private PasswordField confirmPassword;

    private TextField name;

    private TextField affiliation;

    private TextField phone;

    private TextField email;

    private AdminOption adminOption;
    
    private JCheckBox wantEmails;

    private ManageChangeables changeablesList;
    
    private AddRemoveWidget featureWidget;
    
    private User user;

    // FIXME: one to many params ?
    public RegisterUserProfilePanel(User user, Widget usernameWidget,
            AdminOption adminOption,
            ManageChangeables changeableList) {  
        this.user = user;
        this.adminOption = adminOption;        
        this.changeablesList = changeableList;

        createLayout(usernameWidget, adminOption);
        this.setSize(new Dimension(380, 540));
    }

    private void createLayout(Widget usernameWidget, AdminOption adminOption) {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.add(createProfilePanel(adminOption));
        this.add(createLoginPanel(usernameWidget));
    }

    private JPanel createLoginPanel(Widget usernameWidget) {
        JPanel panel = new JPanel();
        panel.setBorder(new Border("Login"));

        GridLayout labelsLayoutManager = new GridLayout(3, 1);
        labelsLayoutManager.setVgap(15);
        JPanel labelsPanel = new JPanel(labelsLayoutManager);
        labelsPanel.add(new JLabel("Username"));
        labelsPanel.add(new JLabel("Password"));
        labelsPanel.add(new JLabel("Confirm Password"));

        panel.add(labelsPanel);

        GridLayout valuesLayoutManager = new GridLayout(3, 1);
        valuesLayoutManager.setVgap(10);
        JPanel valuesPanel = new JPanel(valuesLayoutManager);

        username = usernameWidget;
        valuesPanel.add(usernameWidget.element());

        password = new PasswordField("password", 10);
        changeablesList.addChangeable(password);
        valuesPanel.add(password);

        confirmPassword = new PasswordField("confirmPassword", 10);
        changeablesList.addChangeable(confirmPassword);
        valuesPanel.add(confirmPassword);

        panel.add(valuesPanel);

        panel.setMaximumSize(new Dimension(300, 125));

        return panel;
    }

    private JPanel createProfilePanel(AdminOption adminOption) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new Border("Profile"));
        
        JPanel mandatoryPanel = createManadatoryProfilePanel();
        panel.add(mandatoryPanel);
 
        //JPanel subPanel = new JPanel();
        //subPanel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        //labelsLayoutManager.setVgap(5);
        JPanel checkPanel = new JPanel(new BorderLayout(10,0));
        wantEmails = new JCheckBox("Receives EMF update emails? ");
        wantEmails.setSelected(user.getWantEmails());    
        checkPanel.add(wantEmails, BorderLayout.NORTH);
        checkPanel.setBorder(BorderFactory.createEmptyBorder(2,30,2,20));
        
        JPanel optionsPanel = new JPanel(new BorderLayout(10,0));
        adminOption.add(checkPanel);
        adminOption.setAdmin(user);
        optionsPanel.setBorder(BorderFactory.createEmptyBorder(2,30,2,20));
        
        panel.add(checkPanel);
        panel.add(optionsPanel,BorderLayout.SOUTH);
        panel.setMaximumSize(new Dimension(300, 280));
        return panel;
    }

    private JPanel createManadatoryProfilePanel() {
        JPanel panel = new JPanel();

        JPanel labelsPanel = new JPanel();
        labelsPanel.setLayout(new BoxLayout(labelsPanel, BoxLayout.Y_AXIS));

        labelsPanel.add(new JLabel("Name"));
        labelsPanel.add(Box.createRigidArea(new Dimension(1, 15)));
        labelsPanel.add(new JLabel("Affiliation"));
        labelsPanel.add(Box.createRigidArea(new Dimension(1, 15)));
        labelsPanel.add(new JLabel("Phone"));
        labelsPanel.add(Box.createRigidArea(new Dimension(1, 15)));
        labelsPanel.add(new JLabel("Email"));

        panel.add(labelsPanel);

        JPanel valuesPanel = new JPanel();
        valuesPanel.setLayout(new BoxLayout(valuesPanel, BoxLayout.Y_AXIS));

        name = new TextField("name", user.getName(), 15);
        changeablesList.addChangeable(name);
        valuesPanel.add(name);
        valuesPanel.add(Box.createRigidArea(new Dimension(1, 10)));

        affiliation = new TextField("affiliation", user.getAffiliation(), 15);
        changeablesList.addChangeable(affiliation);
        valuesPanel.add(affiliation);
        valuesPanel.add(Box.createRigidArea(new Dimension(1, 10)));

        phone = new TextField("phone", user.getPhone(), 15);
        changeablesList.addChangeable(phone);
        valuesPanel.add(phone);
        valuesPanel.add(Box.createRigidArea(new Dimension(1, 10)));

        email = new TextField("email", user.getEmail(), 15);
        changeablesList.addChangeable(email);
        valuesPanel.add(email);

        panel.add(valuesPanel);

        panel.setMaximumSize(new Dimension(300, 175));

        return panel;
    }
    
    private JPanel createHideFeaturePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new Border("Hide Feature"));
       
        JPanel checkPanel = new JPanel(new BorderLayout(10,0));
        wantEmails = new JCheckBox("Receives EMF update emails? ");
        wantEmails.setSelected(user.getWantEmails());    
        checkPanel.add(wantEmails, BorderLayout.NORTH);
        checkPanel.setBorder(BorderFactory.createEmptyBorder(2,30,2,20));
        
        JPanel optionsPanel = new JPanel(new BorderLayout(10,0));
        adminOption.add(checkPanel);
        adminOption.setAdmin(user);
        optionsPanel.setBorder(BorderFactory.createEmptyBorder(2,30,2,20));
        
        panel.add(checkPanel);
        panel.add(optionsPanel,BorderLayout.SOUTH);
        panel.setMaximumSize(new Dimension(300, 280));
        return panel;
    }

    public Boolean getWantEmails() {
        return wantEmails.isSelected();
    }
    public String getUsername() {
        return username.value();
    }
    
    public char[] getPassword() {
        return password.getPassword();
    }
    
    public char[] getConfirmPassword() {
        return confirmPassword.getPassword();
    }
    
    public String getEmail() {
        return email.getText();
    }
    public String getName(){
        return name.getText();
    }
   
    public String getAffi() {
        return affiliation.getText();
    }
       
    public String getPhone(){
        return phone.getText();
    }
}
