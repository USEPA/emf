package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.LabelWidget;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.TextFieldWidget;
import gov.epa.emissions.commons.gui.Widget;
import gov.epa.emissions.commons.gui.buttons.CloseButton;
import gov.epa.emissions.commons.gui.buttons.SaveButton;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.EmfInternalFrame;
import gov.epa.emissions.framework.client.EmfView;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

public class RegisterUserPanel extends JPanel {

    private RegisterUserPresenter presenter;

    private PostRegisterStrategy postRegisterStrategy;

    private EmfView container;

    protected JPanel profileValuesPanel;

    private RegisterCancelStrategy cancelStrategy;

    private RegisterUserProfilePanel panel;

    private User user;
    
    private Boolean isNewUser; 

    private ManageChangeables changeablesList;
    
    private SingleLineMessagePanel messagePanel;
    
    private PopulateUserOnRegisterStrategy populateUserStrategy;
    
    public RegisterUserPanel(PostRegisterStrategy postRegisterStrategy, RegisterCancelStrategy cancelStrategy,
            EmfView parent, ManageChangeables changeablesList, User user) {
        //this.user = user; 
        this(postRegisterStrategy, cancelStrategy, parent, new NoAdminOption(), 
                changeablesList, user);
    }

    public RegisterUserPanel(PostRegisterStrategy postRegisterStrategy, RegisterCancelStrategy cancelStrategy,
            EmfView parent, AdminOption adminOption, ManageChangeables changeablesList, User user) {
        this.postRegisterStrategy = postRegisterStrategy;
        this.cancelStrategy = cancelStrategy;
        this.container = parent;
        this.changeablesList = changeablesList;
        this.user = user; 
        this.messagePanel = new SingleLineMessagePanel();
         
        createLayout(adminOption);

        this.setSize(new Dimension(375, 425));
    }

    private void createLayout(AdminOption adminOption) {
        Widget username;
        if ( user == null ) {
            user = new User();
            isNewUser = true; 
            username = new TextFieldWidget("username", user.getUsername(), 10);
        }
        else {
            isNewUser = false; 
            username = new LabelWidget("username", user.getUsername());
        }
        
        this.populateUserStrategy = new PopulateUserOnRegisterStrategy(user);
        panel = new RegisterUserProfilePanel(user, username, adminOption, changeablesList);
        
        JPanel mainContainer = new JPanel();
        mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.X_AXIS));
         
        container.add(panel);
//        container.add(selectDTPanel());        
        messagePanel = new SingleLineMessagePanel();
        
        mainContainer.add(messagePanel);
        mainContainer.add(container);
        mainContainer.add(createButtonsPanel());
        this.add(mainContainer);
    }
    
//    private SelectDTypePanel selectDTPanel() throws EmfException {
//        DatasetType[] idatasetTypes = presenter.getDatasetTypes();
//        return new SelectDTypePanel(user, this, null, idatasetTypes);
//    }
    
    private JPanel createButtonsPanel() {
        Action okAction = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                registerUser();
            }
        };
        Action cancelAction = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                closeWindow();
            }
        };

        JPanel panel = new JPanel(new BorderLayout());

        JPanel container = new JPanel();
        FlowLayout layout = new FlowLayout();
        layout.setHgap(10);
        layout.setVgap(15);
        container.setLayout(layout);

        Button okButton = new SaveButton(okAction);
        container.add(okButton);
        CloseButton closeButton = new CloseButton(cancelAction);
        container.add(closeButton);

        panel.add(container, BorderLayout.CENTER);

        return panel;
    }

    private void registerUser() {
        try {
            if (!isNewUser)
                populateUserStrategy.checkNewPwd(panel.getPassword());
            populateUser();

            // FIXME: monitor.resetChanges();
            //user.setLoggedIn(true);
            user = presenter.doRegister(user, isNewUser);
            postRegisterStrategy.execute(user);
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
            refresh();
            return;
        }

        container.disposeView();
    }

    public void refresh() {
        this.validate();
    }

    public void observe(RegisterUserPresenter presenter) {
        this.presenter = presenter;
    }

    public RegisterUserPresenter getPresenter() {
        return presenter;
    }

    public boolean confirmDiscardChanges() {
        if (changeablesList instanceof EmfInternalFrame)
            return ((EmfInternalFrame) changeablesList).shouldDiscardChanges();

        return true;
    }

    public void closeWindow() {
        if (confirmDiscardChanges())
            cancelStrategy.execute(presenter);
    }
    
    protected void populateUser() throws EmfException {
        populateUserStrategy.populate(panel.getName(), panel.getAffi(), panel.getPhone(), panel.getEmail(), panel.getUsername(),
                panel.getPassword(), panel.getConfirmPassword(), panel.getWantEmails());
    }
}
