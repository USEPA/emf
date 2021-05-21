package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.commons.data.UserFeature;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.PasswordField;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.gui.Widget;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.AddRemoveWidget;
import gov.epa.emissions.framework.client.Label;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.ui.Border;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class EditableUserProfilePanel extends JPanel {

    private Label username;

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
    
    private UpdateUserPresenter parentPresenter;
    
    private EmfConsole parentConsole;

    private User user;

    // FIXME: one to many params ?
    public EditableUserProfilePanel(User user, Label username, UpdateUserPresenter presenter,
            AdminOption adminOption, EmfConsole parentConsole,
            ManageChangeables changeableList) throws EmfException {
        this.user = user;
        this.adminOption = adminOption;
        this.parentPresenter = presenter;
        this.changeablesList = changeableList;
        this.parentConsole = parentConsole;

        createLayout(username, adminOption);
        this.setSize(new Dimension(380, 500));
    }

    private void createLayout(Label username, AdminOption adminOption) throws EmfException {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.add(createProfilePanel(adminOption));
        this.add(createLoginPanel(username));
        this.add(createHideFeaturePanel());
    }

    private JPanel createLoginPanel(Label username) {
        JPanel panel = new JPanel();
        panel.setBorder(new Border("Login"));

        GridLayout labelsLayoutManager = new GridLayout(3, 1);
        labelsLayoutManager.setVgap(15);
        JPanel labelsPanel = new JPanel(labelsLayoutManager);
        final JLabel usernameLabel = new JLabel("Username");
        final JLabel passwordLabel = new JLabel("Password");
        final JLabel confirmPasswordLabel = new JLabel("Confirm Password");
        labelsPanel.add(usernameLabel);
        labelsPanel.add(passwordLabel);
        labelsPanel.add(confirmPasswordLabel);

        panel.add(labelsPanel);

        GridLayout valuesLayoutManager = new GridLayout(3, 1);
        valuesLayoutManager.setVgap(10);
        JPanel valuesPanel = new JPanel(valuesLayoutManager);

        usernameLabel.setLabelFor(username);
        this.username = username;
        valuesPanel.add(username);

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
        
        JPanel mandatoryPanel = createManadatoryProfilePanel();
        panel.add(mandatoryPanel);
 
        //JPanel subPanel = new JPanel();
        //subPanel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        //labelsLayoutManager.setVgap(5);
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
        
        panel.add(checkPanel);
        panel.add(optionsPanel,BorderLayout.SOUTH);
        panel.setMaximumSize(new Dimension(300, 280));
        return panel;
    }

    private JPanel createManadatoryProfilePanel() {
        JPanel panel = new JPanel();

        JPanel labelsPanel = new JPanel();
        labelsPanel.setLayout(new BoxLayout(labelsPanel, BoxLayout.Y_AXIS));

        final JLabel nameLabel = new JLabel("Name");
        final JLabel affiliationLabel = new JLabel("Affiliation");
        final JLabel phoneLabel = new JLabel("Phone");
        final JLabel emailLabel = new JLabel("Email");

        labelsPanel.add(nameLabel);
        labelsPanel.add(Box.createRigidArea(new Dimension(1, 15)));
        labelsPanel.add(affiliationLabel);
        labelsPanel.add(Box.createRigidArea(new Dimension(1, 15)));
        labelsPanel.add(phoneLabel);
        labelsPanel.add(Box.createRigidArea(new Dimension(1, 15)));
        labelsPanel.add(emailLabel);

        panel.add(labelsPanel);

        JPanel valuesPanel = new JPanel();
        valuesPanel.setLayout(new BoxLayout(valuesPanel, BoxLayout.Y_AXIS));

        name = new TextField("name", user.getName(), 15);
        name.setToolTipText("User name");
        nameLabel.setLabelFor(name);
        changeablesList.addChangeable(name);
        valuesPanel.add(name);
        valuesPanel.add(Box.createRigidArea(new Dimension(1, 10)));

        affiliation = new TextField("affiliation", user.getAffiliation(), 15);
        affiliation.setToolTipText("User affiliation");
        affiliationLabel.setLabelFor(affiliation);
        changeablesList.addChangeable(affiliation);
        valuesPanel.add(affiliation);
        valuesPanel.add(Box.createRigidArea(new Dimension(1, 10)));

        phone = new TextField("phone", user.getPhone(), 15);
        phone.setToolTipText("User phone number");
        phoneLabel.setLabelFor(phone);
        changeablesList.addChangeable(phone);
        valuesPanel.add(phone);
        valuesPanel.add(Box.createRigidArea(new Dimension(1, 10)));

        email = new TextField("email", user.getEmail(), 15);
        email.setToolTipText("User email");
        emailLabel.setLabelFor(email);
        changeablesList.addChangeable(email);
        valuesPanel.add(email);

        panel.add(valuesPanel);

        panel.setMaximumSize(new Dimension(300, 175));

        return panel;
    }
    
    private JPanel createHideFeaturePanel() throws EmfException {
        JPanel panel = new JPanel();
        panel.setBorder(new Border("Hide Feature"));
       
        JPanel featurePanel = new JPanel(new BorderLayout(10,0));
        
        featurePanel.add(userFeatures());
  
        panel.add(featurePanel);
        
        panel.setMaximumSize(new Dimension(300, 280));
        return panel;
    }

    private JPanel userFeatures() throws EmfException {
        UserFeature[] userFeatures = parentPresenter.getUserFeatures();
        UserFeature[] exUserFeatures = user.getExcludedUserFeatures();
        
        featureWidget = new AddRemoveWidget(userFeatures, changeablesList, parentConsole, "Features");
        featureWidget.setObjects(exUserFeatures);
        featureWidget.setPreferredSize(new Dimension(180, 90));
        featureWidget.setToolTipText("EMF features visible to this user");

        return featureWidget;
    }

    public Boolean getWantEmails() {
        return wantEmails.isSelected();
    }
    public String getUsername() {
        return username.getText();
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
    
    public UserFeature[] getExcludedFeatures() { 
        UserFeature[] objects= Arrays.asList(featureWidget.getObjects()).toArray(new UserFeature[0]);
        return objects;
    }  
}
