package gov.epa.emissions.framework.services.cost.analysis.common;

import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.LongFormatter;
import gov.epa.emissions.commons.io.NullFormatter;
import gov.epa.emissions.commons.io.RealFormatter;
import gov.epa.emissions.commons.io.StringFormatter;
import gov.epa.emissions.commons.io.TableFormat;
import gov.epa.emissions.framework.services.cost.controlStrategy.StrategyResultType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StrategyImpactSummaryTableFormat implements TableFormat {
    private SqlDataTypes types;
    
    private Column[] cols;

    public StrategyImpactSummaryTableFormat(SqlDataTypes types) {
        this.types = types;
        this.cols = createCols();
    }

    public String identify() {
        return StrategyResultType.strategyImpactSummary;
    }
    
    public String key() {
        return "Record_Id";
    }
    
    public Column[] cols() {
        return cols;
    }
    
    private Column[] createCols() {
        List<Column> cols = new ArrayList<Column>();

        cols.addAll(Arrays.asList(versionCols()));
        cols.addAll(Arrays.asList(baseCols()));

        return cols.toArray(new Column[0]);
    }
    
    private Column[] versionCols() {
        List<Column> cols = new ArrayList<Column>();

        cols.add(recordID(types));
        cols.add(new Column("Dataset_Id", types.longType(), new LongFormatter(), "NOT NULL"));
        cols.add(new Column("Version", types.intType(), new NullFormatter(), "NULL DEFAULT 0"));
        cols.add(new Column("Delete_Versions", types.text(), new NullFormatter(), "DEFAULT ''::text"));

        return cols.toArray(new Column[0]);
    }

    private Column recordID(SqlDataTypes types) {
        String key = key();
        String keyType = types.autoIncrement() + ",Primary Key(" + key + ")";
        Column recordId = new Column(key, keyType, new NullFormatter());
        return recordId;
    }

    private Column[] baseCols() {
        List<Column> cols = new ArrayList<Column>();

        cols.add(new Column("FIPS", types.stringType(6), new StringFormatter(6)));
        cols.add(new Column("RSM_SECTOR", types.stringType(128), 128, new StringFormatter(128)));
        cols.add(new Column("INV_SECTOR", types.stringType(128), 128, new StringFormatter(128)));
        cols.add(new Column("Poll", types.stringType(128), new StringFormatter(128)));
        cols.add(new Column("Impact_pollutant", types.stringType(128), new StringFormatter(128)));
        cols.add(new Column("Ton_Reduced", types.realType(), new RealFormatter()));
        cols.add(new Column("Annual_cost", types.realType(), new RealFormatter()));
        cols.add(new Column("Impact_per_ton", types.realType(), new RealFormatter()));
        cols.add(new Column("Impact", types.realType(), new RealFormatter()));
        cols.add(new Column("Cost_per_change_conc", types.realType(), new RealFormatter()));
        cols.add(new Column("Total_cost", types.realType(), new RealFormatter()));
        cols.add(new Column("Total_impact", types.realType(), new RealFormatter()));
        cols.add(new Column("Total_cost_per_change_conc", types.realType(), new RealFormatter()));
        cols.add(new Column("Comment", types.stringType(256), new StringFormatter(256)));

        return cols.toArray(new Column[0]);
    }

    public int getBaseLength() {
        // NOTE Auto-generated method stub
        return cols.length - 4;
    }

    public int getOffset() {
        // NOTE Auto-generated method stub
        return 4;
    }
}