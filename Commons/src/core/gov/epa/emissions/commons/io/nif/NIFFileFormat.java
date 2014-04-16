package gov.epa.emissions.commons.io.nif;

import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.ColumnFormatter;
import gov.epa.emissions.commons.io.RealFormatter;
import gov.epa.emissions.commons.io.StringFormatter;

import java.util.ArrayList;
import java.util.List;

public class NIFFileFormat {
    
    private SqlDataTypes sqlDataTypes;

    public NIFFileFormat(SqlDataTypes type){
        this.sqlDataTypes = type;
    }
    
    public Column[] createCols(String[] names, String[] colTypes, int[] widths) {
        
        List<Column> columns = new ArrayList<Column>();
        for (int i = 0; i < names.length; i++) {
            String type = sqlType(colTypes[i], widths[i], sqlDataTypes);
            ColumnFormatter formatter = colFormatter(colTypes[i], widths[i]);
            Column col = new Column(names[i], type, widths[i], formatter);
            columns.add(col);
        }
        return columns.toArray(new Column[0]);
    }

    private String sqlType(String type, int width, SqlDataTypes sqlTypes) {
        if (type.equals("C")) {
            return sqlTypes.stringType(width);
        }
        if (type.equals("N")) {
            return sqlTypes.realType();
        }
        throw new IllegalArgumentException("The type '" + type + "' is not handled");
    }

    private ColumnFormatter colFormatter(String type, int width) {
        if (type.equals("C")) {
            return new StringFormatter(width);
        }
        if (type.equals("N")) {
            return new RealFormatter();
        }
        throw new IllegalArgumentException("The type '" + type + "' is not handled");
    }
}
