package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

public class ViewUserWindow extends DisposableInteralFrame implements UserView {

    private ViewUserPresenter presenter;

    private JPanel layout;

    private SingleLineMessagePanel messagePanel;

    public ViewUserWindow(DesktopManager desktopManager) {
        super("User: ", new Dimension(350, 425), desktopManager);

        layout = new JPanel();
        layout.setLayout(new BoxLayout(layout, BoxLayout.Y_AXIS));
        super.getContentPane().add(layout);
    }

    public void display(User user) {
        doLayout(user);
        super.setName("userView:" + user.getId());

        super.setTitle("User: " + user.getUsername());
        super.setResizable(false);

        super.display();
    }

    private void doLayout(User user) {
        messagePanel = new SingleLineMessagePanel();
        messagePanel.setMessage(lockStatus(user));
        layout.add(messagePanel);

        layout.add(createProfilePanel(user));
    }

    private String lockStatus(User user) {
        if (!user.isLocked())
            return "";

        return "Locked by " + user.getLockOwner() + " at " + CustomDateFormat.format_MM_DD_YYYY_HH_mm(user.getLockDate());
    }

    private ViewableUserProfilePanel createProfilePanel(User user) {
        Action closeAction = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                try {
                    presenter.doClose();
                } catch (EmfException e) {
                    setError(e.getMessage());
                }
            }
        };

        return new ViewableUserProfilePanel(user, closeAction);
    }

    protected void setError(String message) {
        messagePanel.setError(message);
    }

    public void observe(ViewUserPresenter presenter) {
        this.presenter = presenter;
    }

}
