package gov.epa.emissions.commons.io.other;

import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.DelimitedFileFormat;
import gov.epa.emissions.commons.io.FileFormatWithOptionalCols;
import gov.epa.emissions.commons.io.RealFormatter;
import gov.epa.emissions.commons.io.StringFormatter;
import gov.epa.emissions.commons.io.importer.FillDefaultValues;
import gov.epa.emissions.commons.io.importer.FillRecordWithBlankValues;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AllowablePacketFileFormat implements FileFormatWithOptionalCols, DelimitedFileFormat {
    
    private FillDefaultValues filler;
    
    private SqlDataTypes types;
    
    public AllowablePacketFileFormat(SqlDataTypes types) {
        this(types, new FillRecordWithBlankValues());
    }
    
    public AllowablePacketFileFormat(SqlDataTypes types, FillDefaultValues filler){
        this.types = types;
        this.filler = filler;
    }
    
    public String identify() {
        return "Allowable Packet File";
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

    public Column[] minCols() {
        List<Column> columns = new ArrayList<Column>();

        columns.add(new Column("FIPS", types.stringType(6), 6, new StringFormatter(6)));
        columns.add(new Column("SCC", types.stringType(10), 10, new StringFormatter(10)));
        columns.add(new Column("POLL", types.stringType(16), 16, new StringFormatter(16)));
        columns.add(new Column("CONTROL_FACTOR", types.realType(), new RealFormatter()));
        columns.add(new Column("CAP", types.realType(), new RealFormatter()));
        columns.add(new Column("REPLACEMENT", types.realType(), new RealFormatter()));
        
        return columns.toArray(new Column[0]);
    }

    public Column[] optionalCols() {
        List<Column> cols = new ArrayList<Column>();

        cols.add(new Column("SIC", types.stringType(4), 4, new StringFormatter(4)));
        cols.add(new Column("PLANTID", types.stringType(15), 15, new StringFormatter(15)));
        cols.add(new Column("POINTID", types.stringType(15), 15, new StringFormatter(15)));
        cols.add(new Column("STACKID", types.stringType(15), 15, new StringFormatter(15)));
        cols.add(new Column("SEGMENT", types.stringType(15), 15, new StringFormatter(15)));
        cols.add(new Column("COMPLIANCE_DATE", types.timestamp()));
        cols.add(new Column("NAICS", types.stringType(6), 6, new StringFormatter(6)));
        
        return cols.toArray(new Column[0]);
    }
    
    public void fillDefaults(List<Column> data, long datasetId) {
        filler.fill(this, data, datasetId);
    }
    
}
