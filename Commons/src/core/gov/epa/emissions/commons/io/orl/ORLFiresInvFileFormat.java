package gov.epa.emissions.commons.io.orl;

import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.db.postgres.PostgresSqlDataTypes;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.DelimitedFileFormat;
import gov.epa.emissions.commons.io.FileFormatWithOptionalCols;
import gov.epa.emissions.commons.io.IntegerFormatter;
import gov.epa.emissions.commons.io.RealFormatter;
import gov.epa.emissions.commons.io.StringFormatter;
import gov.epa.emissions.commons.io.importer.FillDefaultValues;
import gov.epa.emissions.commons.io.importer.FillRecordWithBlankValues;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ORLFiresInvFileFormat implements FileFormatWithOptionalCols, DelimitedFileFormat {

    private SqlDataTypes types;

    private FillDefaultValues filler;

    public ORLFiresInvFileFormat(SqlDataTypes types) {
        this(types, new FillRecordWithBlankValues());
    }

    public ORLFiresInvFileFormat(SqlDataTypes types, FillDefaultValues filler) {
        this.types = types;
        this.filler = filler;
    }

    public String identify() {
        return "ORL Fires Inventory";
    }

    public Column[] cols() {
        return asArray(minCols(), optionalCols());
    }

    private Column[] asArray(Column[] minCols, Column[] optionalCols) {
        List list = new ArrayList();
        list.addAll(Arrays.asList(minCols));
        list.addAll(Arrays.asList(optionalCols));

        return (Column[]) list.toArray(new Column[0]);
    }

    public Column[] minCols() {
        List cols = new ArrayList();

        cols.add(new Column("FIPS", types.stringType(6), 6, new StringFormatter(6)));
        cols.add(new Column("FIREID", types.stringType(15), 15, new StringFormatter(15)));
        cols.add(new Column("LOCID", types.stringType(15), 15, new StringFormatter(15)));
        cols.add(new Column("SCC", types.stringType(10), 10, new StringFormatter(10)));

        return (Column[]) cols.toArray(new Column[0]);
    }

    public Column[] optionalCols() {
        List cols = new ArrayList();
        cols.add(new Column("FIRENAME", types.stringType(40), 40, new StringFormatter(40)));
        cols.add(new Column("LAT", types.realType(), new RealFormatter()));
        cols.add(new Column("LON", types.realType(), new RealFormatter()));
        cols.add(new Column("NFDRSCODE", types.stringType(3), 3, new StringFormatter(3)));
        cols.add(new Column("MATBURNED", types.intType(), new IntegerFormatter()));
        cols.add(new Column("DATAVALUE", types.realType(), new RealFormatter()));
        
        return (Column[]) cols.toArray(new Column[0]);
    }

    public void fillDefaults(List data, long datasetId) {
        filler.fill(this, data, datasetId);
    }
    
    public static void main(String[] args) {
        ORLFiresInvFileFormat format = new ORLFiresInvFileFormat(new PostgresSqlDataTypes());
        PrintWriter writer = null;
        Column[] mincols = format.minCols();
        Column[] optcols = format.optionalCols();
        
        try {
            writer = new PrintWriter(new File(System.getProperty("user.home"), "orl_firesinv_format.csv"));
            writer.println("name,type,default_value,mandatory,description,formatter,constraints,width,spaces,fix_format_start,fix_format_end");
            
            for (Column col : mincols)
                writer.println(col.getName()+","+col.getSqlType()+",,true,,,,,,,");
            
            for (Column col : optcols)
                writer.println(col.getName()+","+col.getSqlType()+",,false,,,,,,,");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (writer != null)
                writer.close();
        }
    }

}
