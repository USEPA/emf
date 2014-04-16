package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.commons.security.User;

import javax.swing.JPanel;

public class NoAdminOption implements AdminOption {

    public void add(JPanel profileValuesPanel) {
        // Note: No Op
    }

    public void isAdmin(User user) {
        // Note: No Op
    }

    public void setAdmin(User user) {
        // Note: No Op
    }

}
