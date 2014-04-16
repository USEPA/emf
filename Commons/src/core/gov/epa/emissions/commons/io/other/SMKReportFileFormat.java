package gov.epa.emissions.commons.io.other;

import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.DelimitedFileFormat;
import gov.epa.emissions.commons.io.FileFormat;
import gov.epa.emissions.commons.io.IntegerFormatter;
import gov.epa.emissions.commons.io.RealFormatter;
import gov.epa.emissions.commons.io.StringFormatter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SMKReportFileFormat implements FileFormat, DelimitedFileFormat {

    private Column[] columns;
    
    private Map map;

    public SMKReportFileFormat(String[]cols, SqlDataTypes types) throws Exception {
        this.map = createMap(types);
        this.columns = createCols(cols, types);
    }
    
    public String identify() {
        return "SMOKE Report";
    }

    public Column[] cols() {
        return columns;
    }
    
    private Map createMap(SqlDataTypes types) {
        HashMap map = new HashMap();
        map.put("DATE", new Column("DATE", types.stringType(10), 10, new StringFormatter(10)));
        map.put("STATE", new Column("STATE", types.stringType(50), 50, new StringFormatter(50)));
        map.put("# STATE", new Column("STATE", types.stringType(50), 50, new StringFormatter(50)));
        map.put("REGION", new Column("REGION", types.stringType(10), 10, new StringFormatter(10)));
        map.put("HOUR", new Column("HOUR", types.intType(), new IntegerFormatter()));
        map.put("SCC", new Column("SCC", types.stringType(10), 10, new StringFormatter(10)));
        map.put("SCC DESCRIPTION", new Column("SCC_DESCRIPTION", types.stringType(256), 256, new StringFormatter(256)));
        map.put("COUNTY", new Column("COUNTY", types.stringType(50), 50, new StringFormatter(50)));
        map.put("LABEL", new Column("LABEL", types.stringType(128), 128, new StringFormatter(128)));
        map.put("LAYER", new Column("LAYER", types.stringType(128), 128, new StringFormatter(128)));
        map.put("X CELL", new Column("X_CELL", types.stringType(128), 128, new StringFormatter(128)));
        map.put("Y CELL", new Column("Y_CELL", types.stringType(128), 128, new StringFormatter(128)));
        map.put("SOURCE ID", new Column("SOURCE_ID", types.stringType(32), 32, new StringFormatter(32)));
        map.put("PLANT ID", new Column("PLANT_ID", types.stringType(32), 32, new StringFormatter(32)));
        map.put("ORIS DESCRIPTION", new Column("ORIS_DESCRIPTION", types.stringType(256), 256, new StringFormatter(256)));
        map.put("COUNTRY", new Column("COUNTRY", types.stringType(128), 128, new StringFormatter(128)));
        map.put("SCC TIER 1", new Column("SCC_TIER_1", types.stringType(32), 32, new StringFormatter(32)));
        map.put("SCC TIER 2", new Column("SCC_TIER_2", types.stringType(32), 32, new StringFormatter(32)));
        map.put("SCC TIER 3", new Column("SCC_TIER_3", types.stringType(32), 32, new StringFormatter(32)));
        map.put("SIC", new Column("SIC", types.stringType(32), 32, new StringFormatter(32)));
        map.put("MACT", new Column("MACT", types.stringType(32), 32, new StringFormatter(32)));
        map.put("NAICS", new Column("NAICS", types.stringType(32), 32, new StringFormatter(32)));
        map.put("SOURCE TYPE", new Column("SOURCE_TYPE", types.stringType(128), 128, new StringFormatter(128)));
        map.put("PRIMARY SRG", new Column("PRIMARY_SRG", types.stringType(16), 16, new StringFormatter(16)));
        map.put("FALLBK SRG", new Column("FALLBK_SRG", types.stringType(16), 16, new StringFormatter(16)));
        map.put("MONTHLY PRF", new Column("MONTHLY_PRF", types.stringType(16), 16, new StringFormatter(16)));
        map.put("MONTHLY PRF", new Column("MONTHLY_PRF", types.stringType(16), 16, new StringFormatter(16)));
        map.put("WEEKLY PRF", new Column("WEEKLY_PRF", types.stringType(16), 16, new StringFormatter(16)));
        map.put("DIURNAL PRF", new Column("DIURNAL_PRF", types.stringType(16), 16, new StringFormatter(16)));
        map.put("SPEC PRF", new Column("SPEC_PRF", types.stringType(16), 16, new StringFormatter(16)));
        map.put("ELEVSTAT", new Column("ELEVSTAT", types.stringType(1), 1, new StringFormatter(1)));
        map.put("PLT NAME", new Column("PLT_NAME", types.stringType(128), 128, new StringFormatter(128)));
        map.put("SIC DESCRIPTION", new Column("SIC_DESCRIPTION", types.stringType(256), 256, new StringFormatter(256)));
        map.put("MACT DESCRIPTION", new Column("MACT_DESCRIPTION", types.stringType(256), 256, new StringFormatter(256)));
        map.put("NAICS DESCRIPTION", new Column("NAICS_DESCRIPTION", types.stringType(256), 256, new StringFormatter(256)));
        map.put("VARIABLE", new Column("VARIABLE", types.stringType(32), 32, new StringFormatter(32)));
        map.put("POLLUTANT", new Column("POLLUTANT", types.stringType(32), 32, new StringFormatter(32)));
        map.put("UNITS", new Column("UNITS", types.stringType(32), 32, new StringFormatter(32)));
        map.put("NFDRS", new Column("NFDRS", types.stringType(16), 16, new StringFormatter(16)));
        map.put("MATBURNED", new Column("MATBURNED", types.stringType(16), 16, new StringFormatter(16)));
        map.put("Char 1", new Column("Char_1", types.stringType(15), 15, new StringFormatter(15)));
        map.put("Char 2", new Column("Char_2", types.stringType(15), 15, new StringFormatter(15)));
        map.put("Char 3", new Column("Char_3", types.stringType(15), 15, new StringFormatter(15)));
        map.put("SCC2", new Column("SCC2", types.stringType(10), 10, new StringFormatter(10)));
        map.put("Road", new Column("ROAD", types.intType(), new IntegerFormatter()));
        map.put("Link", new Column("LINK", types.stringType(15), 15, new StringFormatter(15)));
        map.put("Veh Type", new Column("VEH_TYPE", types.stringType(10), 10, new StringFormatter(10)));

        return map;
    }
    
    private Column[] createCols(String[] cols, SqlDataTypes types) throws Exception { 
        List<Column> base = new ArrayList<Column>();
        
        for(int i = 0; i < cols.length; i++) {
            Column col = (Column)map.get(cols[i].trim().toUpperCase());           
            if(col != null)
                base.add(i, col);
            else
                base.add(new Column(fillSpacesInColName(cols[i].trim()), types.realType(), new RealFormatter()));    
        }
        
        return base.toArray(new Column[0]);
    }
    
    private String fillSpacesInColName(String col) {
        if (Character.isDigit(col.charAt(0)))
            col = "_" + col;
        
        return  col.replace(' ', '_');
    }

}
