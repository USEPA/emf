package gov.epa.emissions.framework.services.cost.analysis.common;

import gov.epa.emissions.commons.CoSTConstants;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.IntegerFormatter;
import gov.epa.emissions.commons.io.LongFormatter;
import gov.epa.emissions.commons.io.NullFormatter;
import gov.epa.emissions.commons.io.RealFormatter;
import gov.epa.emissions.commons.io.StringFormatter;
import gov.epa.emissions.commons.io.TableFormat;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.controlStrategy.StrategyResultType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StrategyLeastCostCMWorksheetTableFormat implements TableFormat {
    private SqlDataTypes types;
    
    private Column[] cols;

    public StrategyLeastCostCMWorksheetTableFormat(SqlDataTypes types) {
        this.types = types;
        this.cols = createCols();
    }

    public String identify() {
        return "Strategy " + StrategyResultType.leastCostControlMeasureWorksheet;
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

        cols.add(new Column("Disable", types.booleanType(), new StringFormatter(5)));
        cols.add(new Column("CM_Abbrev", types.stringType(CoSTConstants.CM_ABBREV_LEN), new StringFormatter(CoSTConstants.CM_ABBREV_LEN), "DEFAULT ''"));
        cols.add(new Column("Poll", types.stringType(20), new StringFormatter(20)));
        cols.add(new Column("SCC", types.stringType(10), new StringFormatter(10)));
        cols.add(new Column("FIPS", types.stringType(6), new StringFormatter(6))); //after fips will add 4 more cols plantid, etc.
 
        //new columns for point sources...
        cols.add(new Column("PLANTID", types.stringType(15), 15, new StringFormatter(15)));
        cols.add(new Column("POINTID", types.stringType(15), 15, new StringFormatter(15)));
        cols.add(new Column("STACKID", types.stringType(15), 15, new StringFormatter(15)));
        cols.add(new Column("SEGMENT", types.stringType(15), 15, new StringFormatter(15)));

        cols.add(new Column("Annual_Cost", types.realType(), new RealFormatter()));
        cols.add(new Column("Ann_Cost_per_Ton", types.realType(), new RealFormatter()));
        cols.add(new Column("Annual_Oper_Maint_Cost", types.realType(), new RealFormatter()));
        cols.add(new Column("Annual_Variable_Oper_Maint_Cost", types.realType(), new RealFormatter()));
        cols.add(new Column("Annual_Fixed_Oper_Maint_Cost", types.realType(), new RealFormatter()));
        cols.add(new Column("Annualized_Capital_Cost", types.realType(), new RealFormatter()));
        cols.add(new Column("Total_Capital_Cost", types.realType(), new RealFormatter()));

        cols.add(new Column("Annual_Cost_3Pct", types.realType(), new RealFormatter()));
        cols.add(new Column("Ann_Cost_per_Ton_3Pct", types.realType(), new RealFormatter()));
        cols.add(new Column("Annualized_Capital_Cost_3Pct", types.realType(), new RealFormatter()));

        cols.add(new Column("Control_Eff", types.realType(), new RealFormatter()));
        cols.add(new Column("Rule_Pen", types.realType(), new RealFormatter()));
        cols.add(new Column("Rule_Eff", types.realType(), new RealFormatter()));
        cols.add(new Column("Percent_Reduction", types.realType(), new RealFormatter()));

        

        cols.add(new Column("CONTROL_IDS", types.stringType(256), 256, new StringFormatter(256)));
        
        cols.add(new Column("Inv_Ctrl_Eff", types.realType(), new RealFormatter()));
        cols.add(new Column("Inv_Rule_Pen", types.realType(), new RealFormatter()));
        cols.add(new Column("Inv_Rule_Eff", types.realType(), new RealFormatter()));
        cols.add(new Column("Final_emissions", types.realType(), new RealFormatter()));
        cols.add(new Column("Emis_Reduction", types.realType(), new RealFormatter()));
        cols.add(new Column("Inv_emissions", types.realType(), new RealFormatter()));
        cols.add(new Column("Apply_Order", types.intType(), new IntegerFormatter(), "DEFAULT 1"));
        cols.add(new Column("input_emis", types.realType(), new RealFormatter()));
        cols.add(new Column("output_emis", types.realType(), new RealFormatter()));

        cols.add(new Column("FIPSST", types.stringType(2), 2, new StringFormatter(2)));
        cols.add(new Column("FIPSCTY", types.stringType(3), 3, new StringFormatter(3)));
        cols.add(new Column("SIC", types.stringType(4), 4, new StringFormatter(4)));
        cols.add(new Column("NAICS", types.stringType(6), 6, new StringFormatter(6)));

        
        cols.add(new Column("Source_Id", types.intType(), new IntegerFormatter(), "NOT NULL"));
        cols.add(new Column("Input_DS_Id", types.intType(), new IntegerFormatter(), "NOT NULL"));
        cols.add(new Column("CS_Id", types.intType(), new IntegerFormatter()));
        cols.add(new Column("CM_Id", types.intType(), new IntegerFormatter()));
        cols.add(new Column("equation_type", types.stringType(255), new StringFormatter(255)));

        cols.add(new Column("marginal", types.realType(), new RealFormatter()));
        cols.add(new Column("cum_annual_cost", types.realType(), new RealFormatter()));
        cols.add(new Column("cum_emis_reduction", types.realType(), new RealFormatter()));
        cols.add(new Column("source_annual_cost", types.realType(), new RealFormatter()));
        cols.add(new Column("remove", types.booleanType()));
        cols.add(new Column("status", types.smallInt()));
        cols.add(new Column("ORIGINAL_DATASET_ID", types.intType(), new IntegerFormatter()));
        cols.add(new Column("SECTOR", types.stringType(255), 255, new StringFormatter(255)));
        cols.add(new Column("SOURCE", types.intType(), new IntegerFormatter()));

        cols.add(new Column("XLOC", types.realType(), new RealFormatter()));
        cols.add(new Column("YLOC", types.realType(), new RealFormatter()));
        cols.add(new Column("PLANT", types.stringType(255), 255, new StringFormatter(255)));
        cols.add(new Column("REPLACEMENT_ADDON", types.stringType(1), 1, new StringFormatter(1)));
        //cols.add(new Column("EXISTING_MEASURE_ABBREVIATION", types.stringType(10), 10, new StringFormatter(10))); // JIZHEN20110727
        cols.add(new Column("EXISTING_MEASURE_ABBREVIATION", types.stringType(CoSTConstants.CM_ABBREV_LEN), CoSTConstants.CM_ABBREV_LEN, new StringFormatter(CoSTConstants.CM_ABBREV_LEN)));
        cols.add(new Column("EXISTING_PRIMARY_DEVICE_TYPE_CODE", types.stringType(4), 4, new StringFormatter(4)));
        cols.add(new Column("source_poll_cnt", types.intType(), new IntegerFormatter()));
        cols.add(new Column("Control_Technology", types.stringType(128), 128, new StringFormatter(128)));        
        cols.add(new Column("Source_Group", types.stringType(255), 255, new StringFormatter(255)));
        cols.add(new Column("County_Name", types.stringType(255), 255, new StringFormatter(255)));
        cols.add(new Column("State_Name", types.stringType(100), 100, new StringFormatter(100)));
        cols.add(new Column("SCC_L1", types.stringType(54), 54, new StringFormatter(54)));
        cols.add(new Column("SCC_L2", types.stringType(54), 54, new StringFormatter(54)));
        cols.add(new Column("SCC_L3", types.stringType(70), 70, new StringFormatter(70)));
        cols.add(new Column("SCC_L4", types.stringType(70), 70, new StringFormatter(70)));
        
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
