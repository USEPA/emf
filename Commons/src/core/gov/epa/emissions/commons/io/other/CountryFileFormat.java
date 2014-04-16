package gov.epa.emissions.commons.io.other;

import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.FileFormat;
import gov.epa.emissions.commons.io.FixedWidthFileFormat;
import gov.epa.emissions.commons.io.IntegerFormatter;
import gov.epa.emissions.commons.io.StringFormatter;

public class CountryFileFormat implements FileFormat, FixedWidthFileFormat {
    private SqlDataTypes types;

    public CountryFileFormat(SqlDataTypes types) {
        this.types = types;
    }

    public String identify() {
        return "Country";
    }

    public Column[] cols() {
        Column code = new Column("CODE", types.intType(), 1, new IntegerFormatter());
        Column spacer = new Column("spacer", types.stringType(1), 1, new StringFormatter(1));
        Column name = new Column("NAME", types.stringType(20), 20, new StringFormatter(20));
        
        return new Column[] { code, spacer, name };
    }
}
