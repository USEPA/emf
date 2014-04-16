package gov.epa.emissions.commons.io.other;

import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.DelimitedFileFormat;
import gov.epa.emissions.commons.io.FileFormat;
import gov.epa.emissions.commons.io.RealFormatter;
import gov.epa.emissions.commons.io.StringFormatter;

import java.util.ArrayList;
import java.util.List;

public class PointStackReplacementsFileFormat implements FileFormat, DelimitedFileFormat {
    
    private Column[] columns;
    
    public PointStackReplacementsFileFormat(SqlDataTypes type){
        columns = createCols(type);
    }
    
    public String identify() {
        return "Point-Source Stack Replacements";
    }

    public Column[] cols() {
        return columns;
    }
    
    private Column[] createCols(SqlDataTypes types) {
        List columns = new ArrayList();

        columns.add(new Column("FIPS", types.stringType(6), 6, new StringFormatter(6)));
        columns.add(new Column("SCC", types.stringType(10), 10, new StringFormatter(10)));
        columns.add(new Column("STKHGT", types.realType(), new RealFormatter()));
        columns.add(new Column("STKDIAM", types.realType(), new RealFormatter()));
        columns.add(new Column("STKTEMP", types.realType(), new RealFormatter()));
        columns.add(new Column("STKVEL", types.realType(), new RealFormatter()));

        return (Column[]) columns.toArray(new Column[0]);
    }
}
