package gov.epa.emissions.commons.io.spatial;

import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.DelimitedFileFormat;
import gov.epa.emissions.commons.io.FileFormat;
import gov.epa.emissions.commons.io.IntegerFormatter;
import gov.epa.emissions.commons.io.RealFormatter;
import gov.epa.emissions.commons.io.StringFormatter;

public class SpatialSurrogatesFileFormat implements FileFormat, DelimitedFileFormat {
    private SqlDataTypes types;

    public SpatialSurrogatesFileFormat(SqlDataTypes types) {
        this.types = types;
    }

    public String identify() {
        return "Spatial Surrogates";
    }

    public Column[] cols() {
        Column code = new Column("CODE", types.intType(), new IntegerFormatter());
        Column fips = new Column("FIPS", types.stringType(6), 6, new StringFormatter(6));
        Column col = new Column("COLNUM", types.intType(), new IntegerFormatter());
        Column row = new Column("ROWNUM", types.intType(), new IntegerFormatter());
        Column ratio = new Column("RATIO", types.realType(), new RealFormatter());
        
        return new Column[] { code, fips, col, row, ratio };
    }
}
