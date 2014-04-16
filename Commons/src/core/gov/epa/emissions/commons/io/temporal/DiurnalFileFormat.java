package gov.epa.emissions.commons.io.temporal;

import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.FileFormat;
import gov.epa.emissions.commons.io.FixedWidthFileFormat;
import gov.epa.emissions.commons.io.IntegerFormatter;

public class DiurnalFileFormat implements FileFormat, FixedWidthFileFormat {

    private SqlDataTypes types;

    private Column[] cols;
    
    public DiurnalFileFormat(SqlDataTypes types) {
        this.types = types;
        this.cols = createCols();
    }

    public String identify() {
        return "Temporal Profile";
    }

    public Column[] cols() {
        return cols;
    }

    private Column[] createCols() {
        Column code = new Column("Code", types.intType(), 5, new IntegerFormatter());
        Column hr0 = new Column("hr0", types.intType(), 4, new IntegerFormatter());
        Column hr1 = new Column("hr1", types.intType(), 4, new IntegerFormatter());
        Column hr2 = new Column("hr2", types.intType(), 4, new IntegerFormatter());
        Column hr3 = new Column("hr3", types.intType(), 4, new IntegerFormatter());
        Column hr4 = new Column("hr4", types.intType(), 4, new IntegerFormatter());
        Column hr5 = new Column("hr5", types.intType(), 4, new IntegerFormatter());
        Column hr6 = new Column("hr6", types.intType(), 4, new IntegerFormatter());
        Column hr7 = new Column("hr7", types.intType(), 4, new IntegerFormatter());
        Column hr8 = new Column("hr8", types.intType(), 4, new IntegerFormatter());
        Column hr9 = new Column("hr9", types.intType(), 4, new IntegerFormatter());
        Column hr10 = new Column("hr10", types.intType(), 4, new IntegerFormatter());
        Column hr11 = new Column("hr11", types.intType(), 4, new IntegerFormatter());
        Column hr12 = new Column("hr12", types.intType(), 4, new IntegerFormatter());
        Column hr13 = new Column("hr13", types.intType(), 4, new IntegerFormatter());
        Column hr14 = new Column("hr14", types.intType(), 4, new IntegerFormatter());
        Column hr15 = new Column("hr15", types.intType(), 4, new IntegerFormatter());
        Column hr16 = new Column("hr16", types.intType(), 4, new IntegerFormatter());
        Column hr17 = new Column("hr17", types.intType(), 4, new IntegerFormatter());
        Column hr18 = new Column("hr18", types.intType(), 4, new IntegerFormatter());
        Column hr19 = new Column("hr19", types.intType(), 4, new IntegerFormatter());
        Column hr20 = new Column("hr20", types.intType(), 4, new IntegerFormatter());
        Column hr21 = new Column("hr21", types.intType(), 4, new IntegerFormatter());
        Column hr22 = new Column("hr22", types.intType(), 4, new IntegerFormatter());
        Column hr23 = new Column("hr23", types.intType(), 4, new IntegerFormatter());
        Column totalWeights = new Column("Total_Weights", types.intType(), 5, new IntegerFormatter());

        return new Column[] { code, hr0, hr1, hr2, hr3, hr4, hr5, hr6, hr7, hr8, hr9, hr10, hr11, hr12, hr13, hr14,
                hr15, hr16, hr17, hr18, hr19, hr20, hr21, hr22, hr23, totalWeights };
    }

}
