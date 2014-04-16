package gov.epa.emissions.commons.io.generic;

import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.FileFormat;
import gov.epa.emissions.commons.io.RealFormatter;
import gov.epa.emissions.commons.io.StringFormatter;

public class LineFileFormat implements FileFormat {

    private Column[] columns;

    public LineFileFormat(SqlDataTypes typeMapper) {
        columns = createCols(typeMapper);
    }

    public String identify() {
        return "Text File";
    }

    public Column[] cols() {
        return columns;
    }

    private Column[] createCols(SqlDataTypes types) {
        return new Column[] { new Column("Line_Number", types.realType(), new RealFormatter()),
                new Column("Lines", types.text(), 255, new StringFormatter()) }; // use 255 to get double quotes when
                                                                                    // export
    }
}
