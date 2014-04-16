package gov.epa.emissions.commons.io;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface ColumnFormatter {

    String format(String name, ResultSet data) throws SQLException;

}
