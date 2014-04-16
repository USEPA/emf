package gov.epa.emissions.commons.io.speciation;

import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.DelimitedFileFormat;
import gov.epa.emissions.commons.io.FileFormatWithOptionalCols;
import gov.epa.emissions.commons.io.IntegerFormatter;
import gov.epa.emissions.commons.io.StringFormatter;
import gov.epa.emissions.commons.io.importer.FillDefaultValues;
import gov.epa.emissions.commons.io.importer.FillRecordWithBlankValues;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SpeciationCrossRefFileFormat implements FileFormatWithOptionalCols, DelimitedFileFormat {
  
    private SqlDataTypes types;

    private FillDefaultValues filler;
    
    public SpeciationCrossRefFileFormat(SqlDataTypes types) {
        this(types, new FillRecordWithBlankValues());
    }

    public SpeciationCrossRefFileFormat(SqlDataTypes types, FillDefaultValues filler) {
        this.types = types;
        this.filler = filler;
    }
    
    public String identify() {
        return "Speciation Cross-Reference";
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
        columns.add(new Column("FIPS", types.stringType(6), 6, new StringFormatter(6)));
        columns.add(new Column("MACT", types.stringType(6), 6, new StringFormatter(6)));
        columns.add(new Column("SIC", types.intType(), new IntegerFormatter()));
        columns.add(new Column("PLANTID", types.stringType(32), 32, new StringFormatter(32)));
        columns.add(new Column("POINTID", types.stringType(32), 32, new StringFormatter(32)));
        columns.add(new Column("STACKID", types.stringType(32), 32, new StringFormatter(32)));
        columns.add(new Column("SEGMENTID", types.stringType(32), 32, new StringFormatter(32)));

        return columns.toArray(new Column[0]);
    }
    
    public Column[] minCols() {
        List<Column> columns = new ArrayList<Column>();
        columns.add(new Column("SCC", types.stringType(10), 10, new StringFormatter(10)));
        columns.add(new Column("CODE", types.stringType(32), 32, new StringFormatter(32)));
        columns.add(new Column("POLLUTANT", types.stringType(32), 32, new StringFormatter(32)));
        
        return columns.toArray(new Column[0]);
    }

    public void fillDefaults(List data, long datasetId) {
        filler.fill(this, data, datasetId);
    }
}
