package gov.epa.emissions.commons.io;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CharFormatter implements ColumnFormatter {

    private StringFormat format;

    public CharFormatter() {
        format = new StringFormat(1);
    }

    public String format(String name, ResultSet data) throws SQLException {
        String value = data.getString(name);
        return (value == null) ? "" : format.format(value);
    }

}
