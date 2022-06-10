package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.Row;
import gov.epa.emissions.framework.ui.ViewableRow;

import java.util.ArrayList;
import java.util.List;

public class UsersTableData extends AbstractTableData {

    private List rows;

    private User[] values;

    public UsersTableData(User[] values) {
        this.values = values;
        this.rows = createRows(values);
    }

    public String[] columns() {
        return new String[] { "Username", "Name", "Logged in", "Phone", "Email", "Is Admin ?", "Wants Emails?", "Last Logged in", "Last Password Reset", "Enabled/Disabled", "Failed Login Attempts" };
    }

    public Class getColumnClass(int col) {
        if (col == 2 || col == 5 || col == 6 || col == 9)
            return Boolean.class;
        return String.class;
    }

    public List rows() {
        return rows;
    }

    private List createRows(User[] values) {
        List rows = new ArrayList();
        for (int i = 0; i < values.length; i++)
            rows.add(row(values[i]));

        return rows;
    }

    private Row row(User user) {
        return new ViewableRow(new UserRowSource(user));
    }

    public boolean isEditable(int col) {
        return (col == 0) ? true : false;
    }

    public User[] getValues() {
        return values;
    }

}
