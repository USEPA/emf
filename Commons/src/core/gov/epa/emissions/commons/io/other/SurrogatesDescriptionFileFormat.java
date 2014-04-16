package gov.epa.emissions.commons.io.other;

import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.DelimitedFileFormat;
import gov.epa.emissions.commons.io.FileFormat;
import gov.epa.emissions.commons.io.IntegerFormatter;
import gov.epa.emissions.commons.io.StringFormatter;

import java.util.ArrayList;
import java.util.List;

public class SurrogatesDescriptionFileFormat implements FileFormat, DelimitedFileFormat {
    private Column[] columns;
    
    public SurrogatesDescriptionFileFormat(SqlDataTypes type){
        columns = createCols(type);
    }
    
    public String identify() {
        return "Surrogates Description";
    }

    public Column[] cols() {
        return columns;
    }
    
    private Column[] createCols(SqlDataTypes types) {
        List<Column> columns = new ArrayList<Column>();

        columns.add(new Column("REGION", types.stringType(10), 10, new StringFormatter(10)));
        columns.add(new Column("CODE", types.intType(), new IntegerFormatter()));
        columns.add(new Column("NAME", types.stringType(128), 128, new StringFormatter(128)));
        columns.add(new Column("FILENAME", types.stringType(128), 128, new StringFormatter(128)));
        
        return columns.toArray(new Column[0]);
    }
}
