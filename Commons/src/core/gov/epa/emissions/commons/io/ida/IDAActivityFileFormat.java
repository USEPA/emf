package gov.epa.emissions.commons.io.ida;

import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.DelimitedFileFormat;
import gov.epa.emissions.commons.io.FileFormatWithOptionalCols;
import gov.epa.emissions.commons.io.IntegerFormatter;
import gov.epa.emissions.commons.io.RealFormatter;
import gov.epa.emissions.commons.io.StringFormatter;
import gov.epa.emissions.commons.io.importer.FillDefaultValues;

import java.util.ArrayList;
import java.util.List;

public class IDAActivityFileFormat implements IDAFileFormat, FileFormatWithOptionalCols, DelimitedFileFormat {

    private List<Column> requiredCols;

    private SqlDataTypes sqlDataTypes;

    private List<Column> optionalCols;

    private FillDefaultValues filler;

    public IDAActivityFileFormat(SqlDataTypes types, FillDefaultValues filler) {
        requiredCols = createRequiredCols(types);
        sqlDataTypes = types;
        this.filler= filler;
    }

    public void addPollutantCols(String[] pollutants) {
        optionalCols = pollutantCols(pollutants, sqlDataTypes);
    }

    public String identify() {
        return "IDA Activity";
    }

    public Column[] cols() {
        List<Column> allCols = new ArrayList<Column>();
        allCols.addAll(requiredCols);
        allCols.addAll(optionalCols);
        return allCols.toArray(new Column[0]);
    }

    private List<Column> createRequiredCols(SqlDataTypes types) {
        List<Column> cols = new ArrayList<Column>();

        cols.add(new Column("STID", types.intType(), new IntegerFormatter()));
        cols.add(new Column("CYID", types.intType(), new IntegerFormatter()));
        cols.add(new Column("LINK_ID", types.stringType(10), 10, new StringFormatter(10)));
        cols.add(new Column("SCC", types.stringType(10), 10, new StringFormatter(10)));
        return cols;
    }

    private List<Column> pollutantCols(String[] pollutants, SqlDataTypes types) {
        List<Column> cols = new ArrayList<Column>();
        for (int i = 0; i < pollutants.length; i++) {
            Column col = new Column(replaceSpecialChars(pollutants[i]), types.realType(), new RealFormatter());
            cols.add(col);
        }
        return cols;
    }
    
    private String replaceSpecialChars(String colName) {
        if (Character.isDigit(colName.charAt(0)))
            colName = "_" + colName; 
        
        return colName.replace(' ', '_');
    }

    public Column[] optionalCols() {
        return optionalCols.toArray(new Column[0]);
    }

    public Column[] minCols() {
        return requiredCols.toArray(new Column[0]);
    }

    public void fillDefaults(List data, long datasetId) {
        filler.fill(this, data, datasetId);        
    }
}
