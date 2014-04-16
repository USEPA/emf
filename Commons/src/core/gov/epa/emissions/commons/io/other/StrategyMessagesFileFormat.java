package gov.epa.emissions.commons.io.other;

import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.DelimitedFileFormat;
import gov.epa.emissions.commons.io.FileFormat;
import gov.epa.emissions.commons.io.StringFormatter;
import gov.epa.emissions.commons.io.TableFormat;

import java.util.ArrayList;
import java.util.List;

public class StrategyMessagesFileFormat implements FileFormat, DelimitedFileFormat, TableFormat {
    private Column[] columns;
    
    public StrategyMessagesFileFormat(SqlDataTypes type){
        columns = createCols(type);
    }
    
    public String identify() {
        return "Strategy Messages";
    }

    public Column[] cols() {
        return columns;
    }
    
    private Column[] createCols(SqlDataTypes types) {
        List<Column> cols = new ArrayList<Column>();
        cols.add(new Column("FIPS", types.stringType(6), new StringFormatter(6)));
        cols.add(new Column("SCC", types.stringType(10), new StringFormatter(10)));
        cols.add(new Column("PLANTID", types.stringType(15), 15, new StringFormatter(15)));
        cols.add(new Column("POINTID", types.stringType(15), 15, new StringFormatter(15)));
        cols.add(new Column("STACKID", types.stringType(15), 15, new StringFormatter(15)));
        cols.add(new Column("SEGMENT", types.stringType(15), 15, new StringFormatter(15)));
        cols.add(new Column("POLL", types.stringType(16), 16, new StringFormatter(16)));
        cols.add(new Column("Status", types.stringType(11), 11, new StringFormatter(11)));
        cols.add(new Column("Control_Program", types.stringType(255), 255, new StringFormatter(255)));
        cols.add(new Column("Message_Type", types.stringType(255), 255, new StringFormatter(255)));
        cols.add(new Column("Message", types.text()));
        cols.add(new Column("Inventory", types.stringType(255), 255, new StringFormatter(255)));
        cols.add(new Column("Packet_FIPS", types.stringType(6), new StringFormatter(6)));
        cols.add(new Column("Packet_SCC", types.stringType(10), new StringFormatter(10)));
        cols.add(new Column("Packet_PLANTID", types.stringType(15), 15, new StringFormatter(15)));
        cols.add(new Column("Packet_POINTID", types.stringType(15), 15, new StringFormatter(15)));
        cols.add(new Column("Packet_STACKID", types.stringType(15), 15, new StringFormatter(15)));
        cols.add(new Column("Packet_SEGMENT", types.stringType(15), 15, new StringFormatter(15)));
        cols.add(new Column("Packet_POLL", types.stringType(16), 16, new StringFormatter(16)));
        cols.add(new Column("Packet_SIC", types.stringType(4), 4, new StringFormatter(4)));
        cols.add(new Column("Packet_MACT", types.stringType(6), 6, new StringFormatter(6)));
        cols.add(new Column("Packet_NAICS", types.stringType(6), 6, new StringFormatter(6)));
        cols.add(new Column("Packet_COMPLIANCE_EFFECTIVE_DATE", types.timestamp()));
        cols.add(new Column("Packet_REPLACEMENT", types.stringType(1), 1, new StringFormatter(1)));
        cols.add(new Column("Packet_ANNUAL_MONTHLY", types.stringType(1), 1, new StringFormatter(1)));
        return cols.toArray(new Column[0]);
    }

    public String key() {
        return "Record_Id";
    }

    public int getBaseLength() {
        // TODO Auto-generated method stub
        return 0;
    }

    public int getOffset() {
        // TODO Auto-generated method stub
        return 0;
    }
}
