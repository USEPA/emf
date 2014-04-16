package gov.epa.emissions.commons.io.orl;

import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.db.postgres.PostgresSqlDataTypes;
import gov.epa.emissions.commons.io.CharFormatter;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.DelimitedFileFormat;
import gov.epa.emissions.commons.io.FileFormatWithOptionalCols;
import gov.epa.emissions.commons.io.IntegerFormatter;
import gov.epa.emissions.commons.io.NullFormatter;
import gov.epa.emissions.commons.io.RealFormatter;
import gov.epa.emissions.commons.io.SmallIntegerFormatter;
import gov.epa.emissions.commons.io.StringFormatter;
import gov.epa.emissions.commons.io.importer.FillDefaultValues;
import gov.epa.emissions.commons.io.importer.FillRecordWithBlankValues;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ORLPointFileFormat implements FileFormatWithOptionalCols, DelimitedFileFormat {

    protected SqlDataTypes types;

    private FillDefaultValues filler;

    public ORLPointFileFormat(SqlDataTypes types) {
        this(types, new FillRecordWithBlankValues());
    }

    public ORLPointFileFormat(SqlDataTypes types, FillDefaultValues filler) {
        this.types = types;
        this.filler = filler;
    }

    public String identify() {
        return "ORL Point";
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
        List<Column> cols = new ArrayList<Column>();

        cols.add(new Column("FIPS", types.stringType(6), 6, new StringFormatter(6)));
        cols.add(new Column("PLANTID", types.stringType(15), 15, new StringFormatter(15)));
        cols.add(new Column("POINTID", types.stringType(15), 15, new StringFormatter(15)));
        cols.add(new Column("STACKID", types.stringType(15), 15, new StringFormatter(15)));
        cols.add(new Column("SEGMENT", types.stringType(15), 15, new StringFormatter(15)));
        cols.add(new Column("PLANT", types.stringType(40), 40, new StringFormatter(40)));
        cols.add(new Column("SCC", types.stringType(10), 10, new StringFormatter(10)));
        cols.add(new Column("ERPTYPE", types.stringType(2), 2, new StringFormatter(2)));
        cols.add(new Column("SRCTYPE", types.stringType(2), 2, new StringFormatter(2)));
        cols.add(new Column("STKHGT", types.realType(), new RealFormatter()));
        cols.add(new Column("STKDIAM", types.realType(), new RealFormatter()));
        cols.add(new Column("STKTEMP", types.realType(), new RealFormatter()));
        cols.add(new Column("STKFLOW", types.realType(), new RealFormatter()));
        cols.add(new Column("STKVEL", types.realType(), new RealFormatter()));
        cols.add(new Column("SIC", types.stringType(4), 4, new StringFormatter(4)));
        cols.add(new Column("MACT", types.stringType(6), 6, new StringFormatter(6)));
        cols.add(new Column("NAICS", types.stringType(6), 6, new StringFormatter(6)));
        cols.add(new Column("CTYPE", types.stringType(1), 1, new StringFormatter(1)));
        cols.add(new Column("XLOC", types.realType(), new RealFormatter()));
        cols.add(new Column("YLOC", types.realType(), new RealFormatter()));
        cols.add(new Column("UTMZ", types.smallInt(), new SmallIntegerFormatter()));
        cols.add(new Column("POLL", types.stringType(16), 16, new StringFormatter(16)));
        cols.add(new Column("ANN_EMIS", types.realType(), new RealFormatter()));
        
        return cols.toArray(new Column[0]);
    }

    public Column[] optionalCols() {
        List<Column> cols = new ArrayList<Column>();

        cols.add(new Column("AVD_EMIS", types.realType(), new RealFormatter()));
        cols.add(new Column("CEFF", types.realType(), new RealFormatter()));
        cols.add(new Column("REFF", types.realType(), new RealFormatter()));
        cols.add(new Column("CPRI", types.intType(), new IntegerFormatter()));
        cols.add(new Column("CSEC", types.intType(), new IntegerFormatter()));
        // extended orl columns
        cols.add(new Column("NEI_UNIQUE_ID", types.stringType(20), 20, new StringFormatter(20)));
        cols.add(new Column("ORIS_FACILITY_CODE", types.stringType(6), 6, new StringFormatter(6)));
        cols.add(new Column("ORIS_BOILER_ID", types.stringType(6), 6, new StringFormatter(6)));
        cols.add(new Column("IPM_YN", types.charType(), 1, new CharFormatter()));
        cols.add(new Column("DATA_SOURCE", types.stringType(10), 10, new StringFormatter(10)));
        cols.add(new Column("STACK_DEFAULT_FLAG", types.stringType(10), 10, new StringFormatter(10)));
        cols.add(new Column("LOCATION_DEFAULT_FLAG", types.stringType(10), 10, new StringFormatter(10)));
        cols.add(new Column("YEAR", types.stringType(4), 4, new StringFormatter(4)));
        cols.add(new Column("TRIBAL_CODE", types.stringType(3), 3, new StringFormatter(3)));
        cols.add(new Column("HORIZONTAL_AREA_FUGITIVE", types.realType(), new RealFormatter()));
        cols.add(new Column("RELEASE_HEIGHT_FUGITIVE", types.realType(), new RealFormatter()));
        //columns in extended orl but not for SMOKE use
        cols.add(new Column("ZIPCODE", types.stringType(14), 14, new StringFormatter(14)));
        cols.add(new Column("NAICS_FLAG", types.stringType(3), 3, new StringFormatter(3)));
        cols.add(new Column("SIC_FLAG", types.stringType(3), 3, new StringFormatter(3)));
        cols.add(new Column("MACT_FLAG", types.stringType(15), 15, new StringFormatter(15)));
        cols.add(new Column("PROCESS_MACT_COMPLIANCE_STATUS", types.stringType(6), 6, new StringFormatter(6)));
        cols.add(new Column("IPM_FACILITY", types.stringType(3), 3, new StringFormatter(3)));
        cols.add(new Column("IPM_UNIT", types.stringType(3), 3, new StringFormatter(3)));
        cols.add(new Column("BART_SOURCE", types.stringType(10), 10, new StringFormatter(10)));
        cols.add(new Column("BART_UNIT", types.stringType(10), 10, new StringFormatter(10)));
        cols.add(new Column("CONTROL_STATUS", types.stringType(12), 12, new StringFormatter(12)));
        cols.add(new Column("START_DATE", types.stringType(10), 10, new StringFormatter(10)));
        cols.add(new Column("END_DATE", types.stringType(10), 10, new StringFormatter(10)));
        cols.add(new Column("WINTER_THROUGHPUT_PCT", types.realType(), new RealFormatter()));
        cols.add(new Column("SPRING_THROUGHPUT_PCT", types.realType(), new RealFormatter()));
        cols.add(new Column("SUMMER_THROUGHPUT_PCT", types.realType(), new RealFormatter()));
        cols.add(new Column("FALL_THROUGHPUT_PCT", types.realType(), new RealFormatter()));
        cols.add(new Column("ANNUAL_AVG_DAYS_PER_WEEK", types.realType(), new RealFormatter()));
        cols.add(new Column("ANNUAL_AVG_WEEKS_PER_YEAR", types.realType(), new RealFormatter()));
        cols.add(new Column("ANNUAL_AVG_HOURS_PER_DAY", types.realType(), new RealFormatter()));
        cols.add(new Column("ANNUAL_AVG_HOURS_PER_YEAR", types.realType(), new RealFormatter()));
        cols.add(new Column("PERIOD_DAYS_PER_WEEK", types.realType(), new RealFormatter()));
        cols.add(new Column("PERIOD_WEEKS_PER_PERIOD", types.realType(), new RealFormatter()));
        cols.add(new Column("PERIOD_HOURS_PER_DAY", types.realType(), new RealFormatter()));
        cols.add(new Column("PERIOD_HOURS_PER_PERIOD", types.realType(), new RealFormatter()));

        //new columns used for control strategy runs...
        cols.add(new Column("DESIGN_CAPACITY", types.realType(), new RealFormatter()));
        cols.add(new Column("DESIGN_CAPACITY_UNIT_NUMERATOR", types.stringType(10), 10, new StringFormatter(10)));
        cols.add(new Column("DESIGN_CAPACITY_UNIT_DENOMINATOR", types.stringType(10), 10, new StringFormatter(10)));
        cols.add(new Column("CONTROL_MEASURES", types.text(), new NullFormatter()));
        cols.add(new Column("PCT_REDUCTION", types.text(), new NullFormatter()));
        cols.add(new Column("CURRENT_COST", types.realType(), new RealFormatter()));
        cols.add(new Column("CUMULATIVE_COST", types.realType(), new RealFormatter()));

        return cols.toArray(new Column[0]);
    }

    public void fillDefaults(List<Column> data, long datasetId) {
        filler.fill(this, data, datasetId);
    }
    
    public static void main(String[] args) {
        ORLPointFileFormat format = new ORLPointFileFormat(new PostgresSqlDataTypes());
        PrintWriter writer = null;
        Column[] mincols = format.minCols();
        Column[] optcols = format.optionalCols();
        
        try {
            writer = new PrintWriter(new File(System.getProperty("user.home"), "orl_point_format.csv"));
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
