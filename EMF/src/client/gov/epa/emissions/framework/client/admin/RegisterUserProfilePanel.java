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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

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
        panel.setToolTipText("User Profile Login; username and password");


        GridLayout labelsLayoutManager = new GridLayout(3, 1);
        labelsLayoutManager.setVgap(15);
        JPanel labelsPanel = new JPanel(labelsLayoutManager);
        final JLabel usernameLabel = new JLabel("Username");
        usernameLabel.setToolTipText("User username");
        final JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setToolTipText("User password");
        final JLabel confirmPasswordLabel = new JLabel("Confirm Password");
        labelsPanel.add(usernameLabel);
        labelsPanel.add(passwordLabel);
        labelsPanel.add(confirmPasswordLabel);

        panel.add(labelsPanel);

        GridLayout valuesLayoutManager = new GridLayout(3, 1);
        valuesLayoutManager.setVgap(10);
        JPanel valuesPanel = new JPanel(valuesLayoutManager);

        username = usernameWidget;
        usernameLabel.setLabelFor(username.element());
        valuesPanel.add(usernameWidget.element());

        password = new PasswordField("password", 10);
        password.setToolTipText("Type in password");
        passwordLabel.setLabelFor(password);
        changeablesList.addChangeable(password);
        valuesPanel.add(password);

        confirmPassword = new PasswordField("confirmPassword", 10);
        confirmPassword.setToolTipText("Confirm password");
        confirmPasswordLabel.setLabelFor(confirmPassword);
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
        panel.setToolTipText("User Profile Information; name, phone, email");

        JPanel topPanel = new JPanel(new GridBagLayout());

        createManadatoryProfilePanel(topPanel);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.weightx = 1;
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.insets = new Insets(5, 5, 5, 5);


        JPanel checkPanel = new JPanel(new BorderLayout(10,0));
        wantEmails = new JCheckBox("Receives EMF update emails? ");
        wantEmails.setSelected(user.getWantEmails());
        wantEmails.setToolTipText("Does the user want to receive EMF update emails?");
        checkPanel.add(wantEmails, BorderLayout.NORTH);
        checkPanel.setBorder(BorderFactory.createEmptyBorder(2,30,2,20));

        JPanel optionsPanel = new JPanel(new BorderLayout(10,0));
        adminOption.add(checkPanel);
        adminOption.setAdmin(user);
        optionsPanel.setBorder(BorderFactory.createEmptyBorder(2,30,2,20));

        constraints.gridx = 0;
        constraints.gridy = 4;
        constraints.gridwidth = 2;

        topPanel.add(checkPanel, constraints);

        constraints.gridx = 0;
        constraints.gridy = 5;
        topPanel.add(optionsPanel, constraints);


        panel.setMaximumSize(new Dimension(300, 280));
        panel.add(topPanel);
        return panel;
    }

    private JPanel createManadatoryProfilePanel(JPanel mainPanel) {

        final JLabel nameLabel = new JLabel("Name");
        final JLabel affiliationLabel = new JLabel("Affiliation");
        final JLabel phoneLabel = new JLabel("Phone");
        final JLabel emailLabel = new JLabel("Email");

        name = new TextField("name", user.getName(), 15);
        name.setToolTipText("Full name");
        nameLabel.setLabelFor(name);
        changeablesList.addChangeable(name);

        affiliation = new TextField("affiliation", user.getAffiliation(), 15);
        affiliation.setToolTipText("User affiliation");
        affiliationLabel.setLabelFor(affiliation);
        changeablesList.addChangeable(affiliation);

        phone = new TextField("phone", user.getPhone(), 15);
        phone.setToolTipText("User phone number, format (000-000-0000)");
        phoneLabel.setLabelFor(phone);
        changeablesList.addChangeable(phone);

        email = new TextField("email", user.getEmail(), 15);
        email.setToolTipText("User email");
        emailLabel.setLabelFor(email);
        changeablesList.addChangeable(email);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.weightx = 1;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(5, 5, 5, 5);

        mainPanel.add(nameLabel, constraints);
        constraints.gridx = 1;
        constraints.gridy = 0;
        mainPanel.add(name, constraints);
        constraints.gridx = 0;
        constraints.gridy = 1;
        mainPanel.add(affiliationLabel, constraints);
        constraints.gridx = 1;
        constraints.gridy = 1;
        mainPanel.add(affiliation, constraints);
        constraints.gridx = 0;
        constraints.gridy = 2;
        mainPanel.add(phoneLabel, constraints);
        constraints.gridx = 1;
        constraints.gridy = 2;
        mainPanel.add(phone, constraints);
        constraints.gridx = 0;
        constraints.gridy = 3;
        mainPanel.add(emailLabel, constraints);
        constraints.gridx = 1;
        constraints.gridy = 3;
        mainPanel.add(email, constraints);

        return mainPanel;
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
