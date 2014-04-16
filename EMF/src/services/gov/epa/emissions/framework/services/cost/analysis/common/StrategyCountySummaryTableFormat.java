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

public class StrategyCountySummaryTableFormat implements TableFormat {
    private SqlDataTypes types;
    
    private Column[] cols;

    public StrategyCountySummaryTableFormat(SqlDataTypes types) {
        this.types = types;
        this.cols = createCols();
    }

    public String identify() {
        return StrategyResultType.strategyCountySummary;
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

        //could be multiple strat summaries
        cols.add(new Column("SECTOR", types.stringType(64), 64, new StringFormatter(64)));
        cols.add(new Column("FIPS", types.stringType(6), new StringFormatter(6)));
        cols.add(new Column("Poll", types.stringType(20), new StringFormatter(20)));
        cols.add(new Column("Input_Emis", types.realType(), new RealFormatter()));
        cols.add(new Column("Emis_Reduction", types.realType(), new RealFormatter()));
        cols.add(new Column("Remaining_Emis", types.realType(), new RealFormatter()));
        cols.add(new Column("Pct_Red", types.realType(), new RealFormatter()));
        cols.add(new Column("Annual_Cost", types.realType(), new RealFormatter()));
        cols.add(new Column("Annual_Oper_Maint_Cost", types.realType(), new RealFormatter()));
        cols.add(new Column("Annualized_Capital_Cost", types.realType(), new RealFormatter()));
        cols.add(new Column("Total_Capital_Cost", types.realType(), new RealFormatter()));
        cols.add(new Column("Avg_Ann_Cost_per_Ton", types.realType(), new RealFormatter()));
        cols.add(new Column("Comment", types.stringType(128), new StringFormatter(128)));

        return cols.toArray(new Column[0]);
    }

    public int getBaseLength() {
        // NOTE Auto-generated method stub
        return 0;
    }

    public int getOffset() {
        // NOTE Auto-generated method stub
        return 0;
    }
}