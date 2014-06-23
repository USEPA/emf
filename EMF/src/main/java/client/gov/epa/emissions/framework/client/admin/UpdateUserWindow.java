package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.LabelWidget;
import gov.epa.emissions.commons.gui.Widget;
import gov.epa.emissions.commons.gui.buttons.CloseButton;
import gov.epa.emissions.commons.gui.buttons.SaveButton;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;
import gov.epa.emissions.framework.ui.YesNoDialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

public class UpdateUserWindow extends DisposableInteralFrame implements UpdatableUserView {

    private UpdateUserPresenter presenter;
    
    private SingleLineMessagePanel messagePanel; 

    private User user;

    private EditableUserProfilePanel panel;
    
    private SelectDTypePanel sPanel;

    private AdminOption adminOption;
    
    private EmfConsole parentConsole;
    
    private PopulateUserOnUpdateStrategy populateUserStrategy;

    public UpdateUserWindow(AdminOption adminOption, DesktopManager desktopManager, EmfConsole console) {
        super("Edit User", new Dimension(820, 560), desktopManager);
        this.adminOption = adminOption;
        this.parentConsole = console;
    }

    public void display(User user) throws EmfException {
        this.user = user;
        this.populateUserStrategy = new PopulateUserOnUpdateStrategy(user);
        setEmbellishments(user);
        JPanel mainContainer = new JPanel();
        mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.X_AXIS));
        panel = createLayout(adminOption);
        container.add(panel);
        container.add(selectDTPanel());
        
        messagePanel = new SingleLineMessagePanel();
        mainContainer.add(messagePanel);
        mainContainer.add(container);
        mainContainer.add(createButtonsPanel());
        super.getContentPane().add(mainContainer);      
        super.display();
    }

    private void setEmbellishments(User user) {
        super.setTitle("Edit User: " + user.getUsername());
        super.setName("updateUser" + user.getId());
    }
    
    private SelectDTypePanel selectDTPanel() throws EmfException {
        DatasetType[] edatasetTypes = user.getExcludedDatasetTypes();
        DatasetType[] idatasetTypes = presenter.getDatasetTypes(user.getId());
        sPanel = new SelectDTypePanel(user, this, edatasetTypes, idatasetTypes);
        return sPanel;
    }

    private EditableUserProfilePanel createLayout(AdminOption adminOption) {
        Widget username = new LabelWidget("username", user.getUsername());
        return createUserProfilePanel(username, adminOption);
    }
    
    private JPanel createButtonsPanel() {
        Action saveAction = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                updateUser();
            }
        };
        Action closeAction = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                try {
                    if ( shouldDiscardChanges())
                        presenter.doClose();
                } catch (EmfException e) {
                    messagePanel.setError(e.getMessage());
                }
            }
        };

        JPanel panel = new JPanel(new BorderLayout());

        JPanel container = new JPanel();
        FlowLayout layout = new FlowLayout();
        layout.setHgap(10);
        layout.setVgap(15);
        container.setLayout(layout);

        Button okButton = new SaveButton(saveAction);
        container.add(okButton);
        CloseButton closeButton = new CloseButton(closeAction);
        container.add(closeButton);

        panel.add(container, BorderLayout.CENTER);

        return panel;
    }

    private EditableUserProfilePanel createUserProfilePanel(Widget username,
            AdminOption adminOption) {
        EditableUserProfilePanel panel=null;
        try {
            panel = new EditableUserProfilePanel(user, username, presenter, 
                    adminOption, parentConsole, this );
        } catch (EmfException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
            messagePanel.setError(e.getMessage());
        }

        return panel;
    }

    private void updateUser() {
        try {
            populateUser();
            presenter.doSave();
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
            return;
        }

        disposeView();
    }
    
    protected void populateUser() throws EmfException {
        populateUserStrategy.populate(panel.getName(), panel.getAffi(), panel.getPhone(), panel.getEmail(), panel.getUsername(),
                panel.getPassword(), panel.getConfirmPassword(), panel.getWantEmails(), sPanel.getExcludedDTs(),
                panel.getExcludedFeatures());
        adminOption.isAdmin(user);
    }

    public void observe(UpdateUserPresenter presenter) {
        this.presenter = presenter;
    }

    public void closeOnConfirmLosingChanges() {
        String message = "Would you like to close without saving and lose your updates?";
        YesNoDialog dialog = new YesNoDialog(this, "Close", message);
        if (dialog.confirm())
            disposeView();
    }

    public void windowClosing() {
        try {
            presenter.doClose();
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
    }

}
