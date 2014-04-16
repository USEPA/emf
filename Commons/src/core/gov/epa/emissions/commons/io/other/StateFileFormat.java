package gov.epa.emissions.commons.io.other;

import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.FileFormat;
import gov.epa.emissions.commons.io.FixedWidthFileFormat;
import gov.epa.emissions.commons.io.IntegerFormatter;
import gov.epa.emissions.commons.io.StringFormatter;

public class StateFileFormat implements FileFormat, FixedWidthFileFormat {
    private SqlDataTypes types;

    public StateFileFormat(SqlDataTypes types) {
        this.types = types;
    }

    public String identify() {
        return "State";
    }

    //FIXME: The sample input data has shifted from the SMOKE description. Columns
    //created here need to be updated once the input file format fixed.
    public Column[] cols() {
        Column countrycode = new Column("COUNTRYCODE", types.intType(), 1, new IntegerFormatter());
        Column statecode = new Column("STATECODE", types.intType(), 2, new IntegerFormatter());
        Column statebrev = new Column("STATEBREV", types.stringType(2), 2, new StringFormatter(2));
        Column spacer1 = new Column("spacer1", types.stringType(1), 1, new StringFormatter(1));
        Column statename = new Column("STATENAME", types.stringType(20), 20, new StringFormatter(20));
        Column eparegion = new Column("EPAREGION", types.intType(), 2, new IntegerFormatter());
        //FIXME: descrepancy between SMOKE doc and sample data file begins here
        Column spacer2 = new Column("spacer2", types.stringType(3), 3, new StringFormatter(3));
        Column timezone = new Column("TIMEZONE", types.stringType(3), 3, new StringFormatter(3));

        return new Column[] { countrycode, statecode, statebrev, spacer1, statename, eparegion, spacer2,timezone };
    }
}
