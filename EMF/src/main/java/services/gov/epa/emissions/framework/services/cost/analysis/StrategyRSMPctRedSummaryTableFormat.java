package gov.epa.emissions.framework.services.cost.analysis;

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

public class StrategyRSMPctRedSummaryTableFormat implements TableFormat {
    private SqlDataTypes types;
    
    private Column[] cols;

    public StrategyRSMPctRedSummaryTableFormat(SqlDataTypes types) {
        this.types = types;
        this.cols = createCols();
    }

    public String identify() {
        return StrategyResultType.rsmPercentReduction;
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
        cols.add(new Column("scenario", types.stringType(64), new StringFormatter(64)));
        cols.add(new Column("name", types.stringType(64), new StringFormatter(64)));
        cols.add(new Column("region", types.stringType(64), new StringFormatter(64)));
        cols.add(new Column("factor_type", types.stringType(4), new StringFormatter(4)));
        cols.add(new Column("factor", types.realType(), new RealFormatter()));
        cols.add(new Column("comments", types.stringType(256), new StringFormatter(256)));

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