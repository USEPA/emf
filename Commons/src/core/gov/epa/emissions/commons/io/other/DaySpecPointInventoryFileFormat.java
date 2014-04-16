package gov.epa.emissions.commons.io.other;

import java.util.ArrayList;
import java.util.List;

import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.FileFormat;
import gov.epa.emissions.commons.io.FixedWidthFileFormat;
import gov.epa.emissions.commons.io.IntegerFormatter;
import gov.epa.emissions.commons.io.RealFormatter;
import gov.epa.emissions.commons.io.StringFormatter;

public class DaySpecPointInventoryFileFormat implements FileFormat, FixedWidthFileFormat {
    private Column[] columns;
    
    public DaySpecPointInventoryFileFormat(SqlDataTypes type){
        columns = createCols(type);
    }
    
    public String identify() {
        return "Day-Specific Point Inventory";
    }

    public Column[] cols() {
        return columns;
    }
    
    private Column[] createCols(SqlDataTypes types) {
        List<Column> columns = new ArrayList<Column>();
        //FIXME: String type uses 32 as the number of characters assuming it is big enough to 
        //hold the relevant fields
        columns.add(new Column("STATE", types.intType(), 2, new IntegerFormatter()));
        columns.add(new Column("COUNTY", types.intType(), 3, new IntegerFormatter()));
        columns.add(new Column("PLANTID", types.stringType(15), 15, new StringFormatter(15)));
        columns.add(new Column("POINTID", types.stringType(12), 12, new StringFormatter(12)));
        columns.add(new Column("STACKID", types.stringType(12), 12, new StringFormatter(12)));
        columns.add(new Column("SEGMENT", types.stringType(12), 12, new StringFormatter(12)));
        columns.add(new Column("POLLUTANT", types.stringType(5), 5, new StringFormatter(5)));
        //FIXME: NOXRATE column should be a date type, but the SqlDataTypes doesn't have
        //a DateFormater
        columns.add(new Column("DATE", types.stringType(8), 8, new StringFormatter(8)));
        columns.add(new Column("TIMEZONE", types.stringType(3), 3, new StringFormatter(3)));
        columns.add(new Column("DAILYTOTAL", types.realType(), 18, new RealFormatter(18,0)));
        //FIXME: SCC code is a 10 digit code
        columns.add(new Column("SCC", types.stringType(9), 9, new StringFormatter(9)));
        
        return columns.toArray(new Column[0]);
    }
}
