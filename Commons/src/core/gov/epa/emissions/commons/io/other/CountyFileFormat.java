package gov.epa.emissions.commons.io.other;

import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.FileFormat;
import gov.epa.emissions.commons.io.FixedWidthFileFormat;
import gov.epa.emissions.commons.io.IntegerFormatter;
import gov.epa.emissions.commons.io.RealFormatter;
import gov.epa.emissions.commons.io.StringFormatter;

public class CountyFileFormat implements FileFormat, FixedWidthFileFormat {
    private SqlDataTypes types;

    public CountyFileFormat(SqlDataTypes types) {
        this.types = types;
    }

    public String identify() {
        return "County";
    }

    //FIXME: The sample input data has shifted from the SMOKE description. Columns
    //created here need to be updated once the input file format fixed.    
    public Column[] cols() {
        Column spacer1 = new Column("spacer1", types.stringType(1), 1, new StringFormatter(1));
        Column statebrev = new Column("STATEBREV", types.stringType(2), 2, new StringFormatter(2));
        Column spacer2 = new Column("spacer2", types.stringType(1), 1, new StringFormatter(1));
        Column name = new Column("COUNTYNAME", types.stringType(20), 20, new StringFormatter(20));
        Column spacer3 = new Column("spacer3", types.stringType(1), 1, new StringFormatter(2));
        Column countrycode = new Column("COUNTRYCODE", types.intType(), 1, new IntegerFormatter());
        Column statecode = new Column("STATECODE", types.intType(), 2, new IntegerFormatter());
        Column countycode = new Column("COUNTYCODE", types.intType(), 3, new IntegerFormatter());
        Column aerosstatecode = new Column("AEROSSTATECODE", types.intType(), 3, new IntegerFormatter());
        Column aeroscountycode = new Column("AEROSCOUNTYCODE", types.intType(), 4, new IntegerFormatter());
        Column spacer4 = new Column("spacer4", types.stringType(1), 1, new StringFormatter(1));
        Column timezone = new Column("TIMEZONE", types.stringType(3), 3, new StringFormatter(3));
        Column daylightsaving = new Column("DAYLIGHTSAVING", types.stringType(1), 1, new StringFormatter(1));
        Column centerlongitude = new Column("CENTERLONGITUDE", types.realType(), 9, new RealFormatter(9,0));
        Column spacer5 = new Column("spacer5", types.stringType(1), 1, new StringFormatter(1));
        Column centerlatitude = new Column("CENTERLATITUDE", types.realType(), 8, new RealFormatter(8,0));
        Column spacer6 = new Column("spacer6", types.stringType(1), 1, new StringFormatter(1));
        Column countyarea = new Column("COUNTYAREA", types.realType(), 12, new RealFormatter(12,0));
        Column spacer7 = new Column("spacer7", types.stringType(1), 1, new StringFormatter(1));
        Column westlongitude = new Column("WESTLONGITUDE", types.realType(), 9, new RealFormatter(9,0));
        Column spacer8 = new Column("spacer8", types.stringType(1), 1, new StringFormatter(1));
        Column eastlongitude = new Column("EASTLONGITUDE", types.realType(), 9, new RealFormatter(9,0));
        Column spacer9 = new Column("spacer9", types.stringType(1), 1, new StringFormatter(1));
        Column southlatitude = new Column("SOUTHLATITUDE", types.realType(), 8, new RealFormatter(8,0));
        Column spacer10 = new Column("spacer10", types.stringType(1), 1, new StringFormatter(1));
        Column northlatitude = new Column("NORTHLATITUDE", types.realType(), 8, new RealFormatter(8,0));
        Column spacer11 = new Column("spacer11", types.stringType(1), 1, new StringFormatter(1));
        Column population = new Column("POPULATION", types.realType(), 8, new RealFormatter(8,0));

        return new Column[] { spacer1, statebrev, spacer2, name, spacer3, countrycode,
                statecode, countycode, aerosstatecode, aeroscountycode, spacer4, timezone,
                daylightsaving, centerlongitude, spacer5, centerlatitude, spacer6,
                countyarea, spacer7, westlongitude, spacer8, eastlongitude, spacer9,
                southlatitude, spacer10, northlatitude, spacer11, population };
    }
}
