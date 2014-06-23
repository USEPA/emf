package gov.epa.emissions.framework.services.cost.analysis.common;

import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.LongFormatter;
import gov.epa.emissions.commons.io.NullFormatter;
import gov.epa.emissions.commons.io.RealFormatter;
import gov.epa.emissions.commons.io.StringFormatter;
import gov.epa.emissions.commons.io.TableFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LeastCostCurveSummaryTableFormat implements TableFormat {
    private SqlDataTypes types;
    
    private Column[] cols;

    public LeastCostCurveSummaryTableFormat(SqlDataTypes types) {
        this.types = types;
        this.cols = createCols();
    }

    public String identify() {
        return "Strategy Least Cost Curve Summary";
    }
    
    public String key() {
        return "Record_Id";
    }
    
    public Column[] cols() {
        return cols;
    }
    
    private Column[] createCols() {
        List cols = new ArrayList();

        cols.addAll(Arrays.asList(versionCols()));
        cols.addAll(Arrays.asList(baseCols()));

        return (Column[]) cols.toArray(new Column[0]);
    }
    
    private Column[] versionCols() {
        Column recordId = recordID(types);
        Column datasetId = new Column("Dataset_Id", types.longType(), new LongFormatter(), "NOT NULL");
        Column version = new Column("Version", types.intType(), new NullFormatter(), "NULL DEFAULT 0");
        Column deleteVersions = new Column("Delete_Versions", types.text(), new NullFormatter(), "DEFAULT ''::text");

        return new Column[] { recordId, datasetId,  version, deleteVersions};
    }

    private Column recordID(SqlDataTypes types) {
        String key = key();
        String keyType = types.autoIncrement() + ",Primary Key(" + key + ")";
        Column recordId = new Column(key, keyType, new NullFormatter());
        return recordId;
    }

    private Column[] baseCols() {
        List<Column> cols = new ArrayList<Column>();

        cols.add(new Column("Poll", types.stringType(20), new StringFormatter(20)));

        cols.add(new Column("Uncontrolled_Emis", types.realType(), new RealFormatter()));
        cols.add(new Column("Total_Emis_Reduction", types.realType(), new RealFormatter()));
        cols.add(new Column("Target_Percent_Reduction", types.realType(), new RealFormatter()));
        cols.add(new Column("Actual_Percent_Reduction", types.realType(), new RealFormatter()));
        cols.add(new Column("Total_Annual_Cost", types.realType(), new RealFormatter()));
        cols.add(new Column("Average_Ann_Cost_per_Ton", types.realType(), new RealFormatter()));
        cols.add(new Column("Total_Annual_Oper_Maint_Cost", types.realType(), new RealFormatter()));
        cols.add(new Column("Total_Annualized_Capital_Cost", types.realType(), new RealFormatter()));
        cols.add(new Column("Total_Capital_Cost", types.realType(), new RealFormatter()));

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
