package gov.epa.emissions.commons.io;

import java.sql.ResultSet;
import java.sql.SQLException;

import corejava.Format;

public class SmallIntegerFormatter implements ColumnFormatter {

    private final Format FORMAT = new Format("%2d");

    public String format(String name, ResultSet data) throws SQLException {
        return FORMAT.format(data.getInt(name));
    }

}
