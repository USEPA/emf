package gov.epa.emissions.commons.io;

import java.sql.ResultSet;
import java.sql.SQLException;

import corejava.Format;

public class LongFormatter implements ColumnFormatter {

    public final Format FORMAT = new Format("%10d");// FIXME: precision for long

    public String format(String name, ResultSet data) throws SQLException {
        if (data.getString(name) == null || data.getFloat(name) == -9)
            return "";

        return FORMAT.format(data.getInt(name));
    }

}
