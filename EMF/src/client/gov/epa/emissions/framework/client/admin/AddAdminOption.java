package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.commons.security.User;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

public class AddAdminOption implements AdminOption {

    private JCheckBox isAdmin;

    public AddAdminOption() {
        isAdmin = new JCheckBox("Is Admin?");
    }

    public AddAdminOption(boolean enable) {
        this();
        isAdmin.setEnabled(enable);
    }

    public void add(JPanel panel) {
        panel.add(isAdmin);
    }

    public void isAdmin(User user) {
        user.setAdmin(isAdmin.isSelected());
    }

    public void setAdmin(User user) {
        isAdmin.setSelected(user.isAdmin());
    }

}
