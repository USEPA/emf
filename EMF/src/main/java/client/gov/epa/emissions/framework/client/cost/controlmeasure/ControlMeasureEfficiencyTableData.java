package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.Row;
import gov.epa.emissions.framework.ui.ViewableRow;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class ControlMeasureEfficiencyTableData extends AbstractTableData {

    private List rows;

    private final static Double NAN_VALUE = new Double(Double.NaN);
    
    public ControlMeasureEfficiencyTableData(EfficiencyRecord[] records) {
        this.rows = createRows(records);
    }

    public void add(EfficiencyRecord record) {
        rows.add(row(record));
    }

    public String[] columns() {
        return new String[] { "Pollutant", "Locale", 
                "Effective Date", "Cost Year",
                "CPT", "Ref Yr CPT", 
                "Control Efficiency", "Min Emis", 
                "Max Emis", "Rule Effectiveness", 
                "Rule Penetration", "Equation Type", 
                "Capital Rec Fac", "Discount Rate", "Cap Ann Ratio", "Incremental CPT", 
                "Last Modifed By", "Last Modifed Date", 
                "Details", "Existing Measure", 
                "Existing NEI Dev"};
    }

    public List rows() {
        return this.rows;
    }

    public boolean isEditable(int col) {
        return false;
    }

    private Row row(EfficiencyRecord record) {
        Object[] values = {
                record.getPollutant().getName(), 
                record.getLocale(),
                effectiveDate(record.getEffectiveDate()), 
                record.getCostYear() != null ? record.getCostYear() : "",
                record.getCostPerTon() != null ? record.getCostPerTon() : NAN_VALUE, 
                record.getRefYrCostPerTon() != null ? record.getRefYrCostPerTon() : NAN_VALUE, 
                record.getEfficiency() != null ? record.getEfficiency() : NAN_VALUE,
                record.getMinEmis() != null ? record.getMinEmis() : NAN_VALUE, 
                record.getMaxEmis() != null ? record.getMaxEmis() : NAN_VALUE, 
                new Double(record.getRuleEffectiveness()), 
                new Double(record.getRulePenetration()), 
                record.getEquationType(), 
                new Double(record.getCapRecFactor()!= null ? record.getCapRecFactor() : NAN_VALUE), 
                new Double(record.getDiscountRate()!= null ? record.getDiscountRate() : NAN_VALUE), 
                new Double(record.getCapitalAnnualizedRatio()!= null ? record.getCapitalAnnualizedRatio() : NAN_VALUE), 
                new Double(record.getIncrementalCostPerTon()!= null ? record.getIncrementalCostPerTon() : NAN_VALUE),
                record.getLastModifiedBy(), 
                CustomDateFormat.format_MM_DD_YYYY_HH_mm(record.getLastModifiedTime()), 
                record.getDetail(), 
                record.getExistingMeasureAbbr(),
                new Integer(record.getExistingDevCode())
        };

        return new ViewableRow(record, values);
    }

    private String effectiveDate(Date effectiveDate) {
        return effectiveDate == null ? "" : CustomDateFormat.format_MM_DD_YYYY(effectiveDate);
    }

    private List createRows(EfficiencyRecord[] records) {
        List rows = new ArrayList();

        for (int i = 0; i < records.length; i++) {
            EfficiencyRecord record = records[i];
            rows.add(row(record));
        }

        return rows;
    }

    public Class getColumnClass(int col) {
        if (col == 20)
            return Integer.class;

        if (col == 4 || col == 5 || col == 6 || col == 7 || col == 8 || 
                col == 9 || col == 10 || col == 12 || col == 13 || col==14 || col==15 )
            return Double.class;

        return String.class;
    }

    public EfficiencyRecord[] sources() {
        List sources = sourcesList();
        return (EfficiencyRecord[]) sources.toArray(new EfficiencyRecord[0]);
    }

    private List sourcesList() {
        List sources = new ArrayList();
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            ViewableRow row = (ViewableRow) iter.next();
            sources.add(row.source());
        }

        return sources;
    }

    private void remove(EfficiencyRecord record) {
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            ViewableRow row = (ViewableRow) iter.next();
            EfficiencyRecord source = (EfficiencyRecord) row.source();
            if (source == record) {
                rows.remove(row);
                return;
            }
        }
    }

    public void remove(EfficiencyRecord[] records) {
        for (int i = 0; i < records.length; i++)
            remove(records[i]);
    }

    public void refresh() {
        this.rows = createRows(sources());
    }

}
