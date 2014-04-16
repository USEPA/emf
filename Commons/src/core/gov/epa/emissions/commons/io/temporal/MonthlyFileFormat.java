package gov.epa.emissions.commons.io.temporal;

import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.FileFormat;
import gov.epa.emissions.commons.io.FixedWidthFileFormat;
import gov.epa.emissions.commons.io.IntegerFormatter;

public class MonthlyFileFormat implements FileFormat, FixedWidthFileFormat {

    private SqlDataTypes types;

    public MonthlyFileFormat(SqlDataTypes types) {
        this.types = types;
    }

    public String identify() {
        return "Temporal Profile";
    }

    public Column[] cols() {
        Column code = new Column("Code", types.intType(), 5, new IntegerFormatter());
        Column jan = new Column("Jan", types.intType(), 4, new IntegerFormatter());
        Column feb = new Column("Feb", types.intType(), 4, new IntegerFormatter());
        Column mar = new Column("Mar", types.intType(), 4, new IntegerFormatter());
        Column apr = new Column("Apr", types.intType(), 4, new IntegerFormatter());
        Column may = new Column("May", types.intType(), 4, new IntegerFormatter());
        Column jun = new Column("Jun", types.intType(), 4, new IntegerFormatter());
        Column jul = new Column("Jul", types.intType(), 4, new IntegerFormatter());
        Column aug = new Column("Aug", types.intType(), 4, new IntegerFormatter());
        Column sep = new Column("Sep", types.intType(), 4, new IntegerFormatter());
        Column oct = new Column("Oct", types.intType(), 4, new IntegerFormatter());
        Column nov = new Column("Nov", types.intType(), 4, new IntegerFormatter());
        Column dec = new Column("Dece", types.intType(), 4, new IntegerFormatter());//Dec is a keyword in mysql
        Column totalWeights = new Column("Total_Weights", types.intType(), 5, new IntegerFormatter());

        return new Column[] { code, jan, feb, mar, apr, may, jun, jul, aug, sep, oct, nov, dec, totalWeights };
    }

}
