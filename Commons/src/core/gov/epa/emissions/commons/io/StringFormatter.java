package gov.epa.emissions.commons.io;


import java.sql.ResultSet;
import java.sql.SQLException;

public class StringFormatter implements ColumnFormatter {

    private StringFormat format;

    public StringFormatter() {
        format = new StringFormat(0);
    }
    public StringFormatter(int size) {
        format = new StringFormat(size);
    }

    public StringFormatter(int size, int trailingSpaces) {
        format = new StringFormat(size, trailingSpaces);
    }

    public String format(String name, ResultSet data) throws SQLException {
        String value = data.getString(name);
        String evalValue = (value == null) || (value.equals(""))? "" : value;
        
        return format.format(evalValue);
    }

}
