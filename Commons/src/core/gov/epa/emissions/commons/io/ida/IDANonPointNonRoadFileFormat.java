package gov.epa.emissions.commons.io.ida;

import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.FixedWidthFileFormat;
import gov.epa.emissions.commons.io.IntegerFormatter;
import gov.epa.emissions.commons.io.RealFormatter;
import gov.epa.emissions.commons.io.StringFormatter;

import java.util.ArrayList;
import java.util.List;

public class IDANonPointNonRoadFileFormat implements IDAFileFormat, FixedWidthFileFormat {

    private List<Column> cols;

    private SqlDataTypes sqlDataTypes;

    public IDANonPointNonRoadFileFormat( SqlDataTypes types) {
        cols = createCols(types);
        sqlDataTypes = types;
    }

    public void addPollutantCols(String[] pollutants) {
        cols.addAll(pollutantCols(pollutants, sqlDataTypes));
    }

    public String identify() {
        return "IDA Area";
    }

    public Column[] cols() {
        return cols.toArray(new Column[0]);
    }

    private List<Column> createCols(SqlDataTypes types) {
        List<Column> cols = new ArrayList<Column>();

        cols.add(new Column("STID", types.intType(), 2, new IntegerFormatter(2,0)));
        cols.add(new Column("CYID", types.intType(), 3, new IntegerFormatter(3,0)));
        cols.add(new Column("SCC", types.stringType(10), 10, new StringFormatter(10)));
        return cols;
    }

    private List<Column> pollutantCols(String[] pollutants, SqlDataTypes types) {
        List<Column> cols = new ArrayList<Column>();
        for (int i = 0; i < pollutants.length; i++) {
            cols.add(new Column(replaceSpecialChars(pollutants[i]), types.realType(), 10, new RealFormatter(10,0)));
            cols.add(new Column("AVD_" + pollutants[i], types.realType(), 10, new RealFormatter(10,0)));
            cols.add(new Column("EMF_" + pollutants[i], types.realType(), 11, new RealFormatter(11,0)));
            cols.add(new Column("CE_" + pollutants[i], types.realType(), 7, new RealFormatter(7,0)));
            cols.add(new Column("RE_" + pollutants[i], types.realType(), 3, new RealFormatter(3,0)));
            cols.add(new Column("RP_" + pollutants[i], types.realType(), 6, new RealFormatter(6,0)));
        }
        return cols;
    }
    
    private String replaceSpecialChars(String colName) {
        if (Character.isDigit(colName.charAt(0)))
            colName = "_" + colName; 
        
        return colName.replace(' ', '_');
    }

}
