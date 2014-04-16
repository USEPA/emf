package gov.epa.emissions.commons.io;

import java.sql.ResultSet;

public class NullFormatter implements ColumnFormatter {

    public String format(String name, ResultSet data) {
        return name;
    }

}
