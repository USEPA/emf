package gov.epa.emissions.commons.io.speciation;

import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.DelimitedFileFormat;
import gov.epa.emissions.commons.io.FileFormatWithOptionalCols;
import gov.epa.emissions.commons.io.IntegerFormatter;
import gov.epa.emissions.commons.io.RealFormatter;
import gov.epa.emissions.commons.io.StringFormatter;
import gov.epa.emissions.commons.io.importer.FillDefaultValues;
import gov.epa.emissions.commons.io.importer.FillRecordWithBlankValues;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SpeciationComboProfileFileFormat implements FileFormatWithOptionalCols, DelimitedFileFormat {
  
    private SqlDataTypes types;

    private FillDefaultValues filler;
    
    public SpeciationComboProfileFileFormat(SqlDataTypes types) {
        this(types, new FillRecordWithBlankValues());
    }

    public SpeciationComboProfileFileFormat(SqlDataTypes types, FillDefaultValues filler) {
        this.types = types;
        this.filler = filler;
    }
    
    public String identify() {
        return "Speciation Combo-Profile";
    }

    public Column[] cols() {
        return asArray(minCols(), optionalCols());
    }

    private Column[] asArray(Column[] minCols, Column[] optionalCols) {
        List<Column> list = new ArrayList<Column>();
        list.addAll(Arrays.asList(minCols));
        list.addAll(Arrays.asList(optionalCols));

        return list.toArray(new Column[0]);
    }
    
    public Column[] optionalCols() {
        List<Column> columns = new ArrayList<Column>();
        columns.add(new Column("PROF2", types.stringType(10), 10, new StringFormatter(10)));
        columns.add(new Column("FRAC2", types.realType(), new RealFormatter()));
        columns.add(new Column("PROF3", types.stringType(10), 10, new StringFormatter(10)));
        columns.add(new Column("FRAC3", types.realType(), new RealFormatter()));
        columns.add(new Column("PROF4", types.stringType(10), 10, new StringFormatter(10)));
        columns.add(new Column("FRAC4", types.realType(), new RealFormatter()));
        columns.add(new Column("PROF5", types.stringType(10), 10, new StringFormatter(10)));
        columns.add(new Column("FRAC5", types.realType(), new RealFormatter()));
        columns.add(new Column("PROF6", types.stringType(10), 10, new StringFormatter(10)));
        columns.add(new Column("FRAC6", types.realType(), new RealFormatter()));
        columns.add(new Column("PROF7", types.stringType(10), 10, new StringFormatter(10)));
        columns.add(new Column("FRAC7", types.realType(), new RealFormatter()));
        columns.add(new Column("PROF8", types.stringType(10), 10, new StringFormatter(10)));
        columns.add(new Column("FRAC8", types.realType(), new RealFormatter()));
        columns.add(new Column("PROF9", types.stringType(10), 10, new StringFormatter(10)));
        columns.add(new Column("FRAC9", types.realType(), new RealFormatter()));
        columns.add(new Column("PROF10", types.stringType(10), 10, new StringFormatter(10)));
        columns.add(new Column("FRAC10", types.realType(), new RealFormatter()));
        
        return columns.toArray(new Column[0]);
    }
    
    public Column[] minCols() {
        List<Column> columns = new ArrayList<Column>();
        columns.add(new Column("POLL", types.stringType(16), 16, new StringFormatter(16)));
        columns.add(new Column("FIPS", types.stringType(6), 6, new StringFormatter(6)));
        columns.add(new Column("PERIOD", types.intType(), new IntegerFormatter()));
        columns.add(new Column("NPROF", types.intType(), new IntegerFormatter()));
        columns.add(new Column("PROF1", types.stringType(10), 10, new StringFormatter(10)));
        columns.add(new Column("FRAC1", types.realType(), new RealFormatter()));
        
        return columns.toArray(new Column[0]);
    }

    public void fillDefaults(List data, long datasetId) {
        filler.fill(this, data, datasetId);
    }
}
