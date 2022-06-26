package gov.epa.emissions.framework.client.admin;

import java.text.ParseException;
import java.util.Date;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.ui.RowSource;

public class UserRowSource implements RowSource {

    private User source;

    public UserRowSource(User source) {
        this.source = source;
    }

    public Object[] values() {
        return new Object[] { source.getUsername(), source.getName(), source.isLoggedIn(), source.getPhone(), source.getEmail(), 
                new Boolean(source.isAdmin()), source.getWantEmails(), format(source.getLastLoginDate()), format(source.getLastResetDate()),
                new Boolean(source.isAccountDisabled()), source.getFailedLoginAttempts() };
        }

    public Object source() {
        return source;
    }

    public void validate(int rowNumber) {// No Op
    }

    public void setValueAt(int column, Object val) {// No Op
    }
    
    private Object format(Date date) {
        return date == null ? "N/A" : CustomDateFormat.format_yyyyMMdd(date);
    }
}