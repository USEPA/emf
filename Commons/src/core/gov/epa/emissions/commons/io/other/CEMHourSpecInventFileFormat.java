package gov.epa.emissions.commons.io.other;

import java.util.ArrayList;
import java.util.List;

import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.DelimitedFileFormat;
import gov.epa.emissions.commons.io.FileFormat;
import gov.epa.emissions.commons.io.IntegerFormatter;
import gov.epa.emissions.commons.io.RealFormatter;
import gov.epa.emissions.commons.io.StringFormatter;

public class CEMHourSpecInventFileFormat implements FileFormat, DelimitedFileFormat {
    private Column[] columns;
    
    public CEMHourSpecInventFileFormat(SqlDataTypes type){
        columns = createCols(type);
    }
    
    public String identify() {
        return "CEM Hour-Specific Point Inventory";
    }

    public Column[] cols() {
        return columns;
    }
    
    private Column[] createCols(SqlDataTypes types) {
        List columns = new ArrayList();
        //FIXME: String type uses 32 as the number of characters assuming it is big enough to 
        //hold the relevant fields
        columns.add(new Column("ORISID", types.stringType(10), 10, new StringFormatter(10)));
        columns.add(new Column("BLRID", types.stringType(10), 10, new StringFormatter(10)));
        columns.add(new Column("OPDATE", types.intType(), new IntegerFormatter()));
        columns.add(new Column("OPHOUR", types.intType(), new IntegerFormatter()));
        columns.add(new Column("OPTIME", types.intType(), new IntegerFormatter()));
        columns.add(new Column("GLOAD", types.realType(), new RealFormatter()));
        columns.add(new Column("SLOAD", types.realType(), new RealFormatter()));
        columns.add(new Column("NOXMASS", types.realType(), new RealFormatter()));
        //FIXME: NOXRATE column should be a real type data, but the sample input data
        //has some values like "**GT2"
        columns.add(new Column("NOXRATE", types.stringType(10), 10, new StringFormatter(10)));
        columns.add(new Column("SO2MASS", types.realType(), new RealFormatter()));
        columns.add(new Column("HTINPUT", types.realType(), new RealFormatter()));
        columns.add(new Column("FLOW", types.intType(), new IntegerFormatter()));
        
        return (Column[]) columns.toArray(new Column[0]);
    }
}
