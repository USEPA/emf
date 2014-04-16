package gov.epa.emissions.commons.io.temporal;

import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.FileFormat;
import gov.epa.emissions.commons.io.FixedWidthFileFormat;
import gov.epa.emissions.commons.io.IntegerFormatter;

public class WeeklyFileFormat implements FileFormat, FixedWidthFileFormat {

    private SqlDataTypes types;

    public WeeklyFileFormat(SqlDataTypes types) {
        this.types = types;
    }

    public String identify() {
        return "Temporal Profile";
    }

    public Column[] cols() {
        Column code = new Column("Code", types.intType(), 5, new IntegerFormatter());
        Column mon = new Column("Mon", types.intType(), 4, new IntegerFormatter());
        Column tue = new Column("Tue", types.intType(), 4, new IntegerFormatter());
        Column wed = new Column("Wed", types.intType(), 4, new IntegerFormatter());
        Column thu = new Column("Thu", types.intType(), 4, new IntegerFormatter());
        Column fri = new Column("Fri", types.intType(), 4, new IntegerFormatter());
        Column sat = new Column("Sat", types.intType(), 4, new IntegerFormatter());
        Column sun = new Column("Sun", types.intType(), 4, new IntegerFormatter());
        Column totalWeights = new Column("Total_Weights", types.intType(), 6, new IntegerFormatter());

        return new Column[] { code, mon, tue, wed, thu, fri, sat, sun, totalWeights };
    }

}
