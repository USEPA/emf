package gov.epa.emissions.commons.io.ida;

import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.CharFormatter;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.FixedWidthFileFormat;
import gov.epa.emissions.commons.io.IntegerFormatter;
import gov.epa.emissions.commons.io.RealFormatter;
import gov.epa.emissions.commons.io.StringFormatter;

import java.util.ArrayList;
import java.util.List;

public class IDAPointFileFormat implements IDAFileFormat, FixedWidthFileFormat {

    private List<Column> cols;
    
    private SqlDataTypes sqlTypes;

    public IDAPointFileFormat(SqlDataTypes types) {
        sqlTypes= types;
        cols = createCols(types);
    }
    
    public void addPollutantCols(String[] pollutants) {
      cols.addAll(pollutantsBasedCols(sqlTypes, pollutants));
    }

    public String identify() {
        return "IDA Point";
    }

    public Column[] cols() {
        return cols.toArray(new Column[0]);
    }

    private List<Column> createCols(SqlDataTypes types) {
        List<Column> cols = new ArrayList<Column>();
        cols.addAll(createMandatoryCols(types));
        return cols;
    }

    private List<Column> pollutantsBasedCols(SqlDataTypes types, String[] pollutants) {
        List<Column> pollCols = new ArrayList<Column>();
        for (int i = 0; i < pollutants.length; i++) {
            pollCols.add( new Column(replaceSpecialChars(pollutants[i]), types.realType(), 13, new RealFormatter(13,0)));
            pollCols.add( new Column("AVD_" + pollutants[i], types.realType(), 13, new RealFormatter(13,0)));
            pollCols.add( new Column("CE_" + pollutants[i], types.realType(), 7, new RealFormatter(7,0)));
            pollCols.add( new Column("RE_" + pollutants[i], types.realType(), 3, new RealFormatter(3,0)));
            pollCols.add( new Column("EMF_" + pollutants[i], types.realType(), 10, new RealFormatter(10,0)));
            pollCols.add( new Column("CPRI_" + pollutants[i], types.realType(), 3, new RealFormatter(3,0)));
            pollCols.add( new Column("CSEC_" + pollutants[i], types.realType(), 3, new RealFormatter(3,0)));
        }
        return pollCols;
    }

    private List<Column> createMandatoryCols(SqlDataTypes types) {
        List<Column> cols = new ArrayList<Column>();

        cols.add(new Column("STID", types.intType(), 2, new IntegerFormatter(2,0)));
        cols.add(new Column("CYID", types.intType(), 3, new IntegerFormatter(3,0)));
        cols.add(new Column("PLANTID", types.stringType(15), 15, new StringFormatter(15)));
        cols.add(new Column("POINTID", types.stringType(15), 15, new StringFormatter(15)));
        cols.add(new Column("STACKID", types.stringType(12), 12, new StringFormatter(12)));
        cols.add(new Column("ORISID", types.stringType(6), 6, new StringFormatter(6)));
        cols.add(new Column("BLRID", types.stringType(6), 6, new StringFormatter(6)));
        cols.add(new Column("SEGMENT", types.stringType(2), 2, new StringFormatter(2)));
        cols.add(new Column("PLANT", types.stringType(40), 40, new StringFormatter(40)));
        cols.add(new Column("SCC", types.stringType(10), 10, new StringFormatter(10)));
        cols.add(new Column("BEGYR", types.intType(), 4, new IntegerFormatter(4,0)));
        cols.add(new Column("ENDYR", types.intType(), 4, new IntegerFormatter(4,0)));
        cols.add(new Column("STKHGT", types.realType(), 4, new RealFormatter(4,0)));
        cols.add(new Column("STKDIAM", types.realType(), 6, new RealFormatter(6,0)));
        cols.add(new Column("STKTEMP", types.realType(), 4, new RealFormatter(4,0)));
        cols.add(new Column("STKFLOW", types.realType(), 10, new RealFormatter(10,0)));
        cols.add(new Column("STKVEL", types.realType(), 9, new RealFormatter(9,0)));
        cols.add(new Column("BOILCAP", types.realType(), 8, new RealFormatter(8,0)));
        cols.add(new Column("CAPUNITS", types.charType(), 1, new CharFormatter()));
        cols.add(new Column("WINTHRU", types.realType(), 2, new RealFormatter(2,0)));
        cols.add(new Column("SPRTHRU", types.realType(), 2, new RealFormatter(2,0)));
        cols.add(new Column("SUMTHRU", types.realType(), 2, new RealFormatter(2,0)));
        cols.add(new Column("FALTHRU", types.realType(), 2, new RealFormatter(2,0)));
        cols.add(new Column("HOURS", types.intType(), 2, new IntegerFormatter(2,0)));
        cols.add(new Column("START", types.intType(), 2, new IntegerFormatter(2,0)));
        cols.add(new Column("DAYS", types.intType(), 1, new IntegerFormatter(1,0)));
        cols.add(new Column("WEEKS", types.intType(), 2, new IntegerFormatter(2,0)));
        cols.add(new Column("THRUPUT", types.realType(), 11, new RealFormatter(11,0)));
        cols.add(new Column("MAXRATE", types.realType(), 12, new RealFormatter(12,0)));
        cols.add(new Column("HEATCON", types.realType(), 8, new RealFormatter(8,0)));
        cols.add(new Column("SULFCON", types.realType(), 5, new RealFormatter(5,0)));
        cols.add(new Column("ASHCON", types.realType(), 5, new RealFormatter(5,0)));
        cols.add(new Column("NETDC", types.realType(), 9, new RealFormatter(9,0)));
        cols.add(new Column("SIC", types.intType(), 4, new IntegerFormatter(4,0)));
        cols.add(new Column("LATC", types.realType(), 9, new RealFormatter(9,0)));
        cols.add(new Column("LONC", types.realType(), 9, new RealFormatter(9,0)));
        cols.add(new Column("OFFSHORE", types.stringType(1), 1, new StringFormatter(1)));

        return cols;
    }
    
    private String replaceSpecialChars(String colName) {
        if (Character.isDigit(colName.charAt(0)))
            colName = "_" + colName; 
        
        return colName.replace(' ', '_');
    }

    
}
