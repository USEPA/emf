package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.data.SourceGroup;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.ControlMeasureClass;
import gov.epa.emissions.framework.services.cost.controlStrategy.CostYearTable;
import gov.epa.emissions.framework.services.cost.controlmeasure.YearValidation;
import gov.epa.emissions.framework.services.cost.data.SumEffRec;
import gov.epa.emissions.framework.services.cost.data.ControlTechnology;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.Row;
import gov.epa.emissions.framework.ui.ViewableRow;

import java.util.ArrayList;
import java.util.List;

public class ControlMeasureTableData extends AbstractTableData {

    protected List rows;

    protected ControlMeasure[] allMeasures;

    protected CostYearTable costYearTable;

    protected int targetYear;

    protected final static Double NAN_VALUE = new Double(Double.NaN);
    
    protected Pollutant pollutant; 

    public ControlMeasureTableData(ControlMeasure[] measures, CostYearTable costYearTable, Pollutant pollutant, String year)
            throws EmfException {
        this.allMeasures = measures;
        this.costYearTable = costYearTable;
        this.pollutant = pollutant;
//        filter(pollutant, year);
        this.targetYear = (year != null) ? new Integer(year) : 2000;
        this.rows = createRows(measures);
    }

    public ControlMeasureTableData(ControlMeasure[] measures, Pollutant pollutant) {
        this.allMeasures = measures;
        this.pollutant = pollutant;
//        this.rows = createRows(measures);
    }

    public String[] columns() {
        return new String[] { "Name", "Abbreviation", "Pollutant", 
                "Avg CPT", "Avg CE", "Min CE", 
                "Max CE", "Min CPT", "Max CPT", 
                "Avg Rule Eff.", "Avg Rule Pen.", "Control Technology", 
                "Source Group", "Equipment Life", 
                "Sectors", "Class", "Last Modified Time", 
                "Last Modified By", "Date Reviewed", "Creator", 
                "Data Source", "Description" };
    }

    public Class getColumnClass(int col) {
        if ((col >= 3 && col <= 10) || col == 13)
            return Double.class;
        return String.class;
    }

    public List rows() {
        return this.rows;
    }

    public void refresh(Pollutant pollutant, String year) throws EmfException {
        filter(pollutant, year);
        this.rows = createRows(allMeasures);
    }

    private List createRows(ControlMeasure[] measures) throws EmfException {
        List rows = new ArrayList();
        int year = targetYear;
        boolean found = false;
        targetYear = year;
        boolean majorPollutant = pollutant.getName().equalsIgnoreCase("major");
        for (int i = 0; i < measures.length; i++) {
            ControlMeasure measure = measures[i];
            SumEffRec[] apers = measure.getSumEffRecs();
            found = false;
            for (int j = 0; j < apers.length; j++) {
                SumEffRec aper = apers[j];
                
                if (majorPollutant && measure.getMajorPollutant().equals(aper.getPollutant())) {
                    Object[] values = { measure.getName(), measure.getAbbreviation(), getPollutantName(measure),
                            getCostPerTon(aper.getAvgCPT()), new Double(aper.getAvgCE()), new Double(aper.getMinCE()), 
                            new Double(aper.getMaxCE()), getCostPerTon(aper.getMinCPT()), getCostPerTon(aper.getMaxCPT()), 
                            new Double(aper.getAvgRE()), new Double(aper.getAvgRP()), getControlTechnology(measure), 
                            getSourceGroup(measure), new Double(measure.getEquipmentLife()),  
                            getSectors(measure), measureClass(measure.getCmClass()), getLastModifiedTime(measure), 
                            measure.getLastModifiedBy(), getDateReviewed(measure), measure.getCreator().getName(), 
                            measure.getDataSouce(), measure.getDescription() };
                    Row row = new ViewableRow(measure, values);
                    rows.add(row);
                    found = true;
                    break;
                } else if (pollutant.equals(aper.getPollutant())) {
                    Object[] values = { measure.getName(), measure.getAbbreviation(), getPollutantName(aper.getPollutant()),
                            getCostPerTon(aper.getAvgCPT()), new Double(aper.getAvgCE()), new Double(aper.getMinCE()), 
                            new Double(aper.getMaxCE()), getCostPerTon(aper.getMinCPT()), getCostPerTon(aper.getMaxCPT()), 
                            new Double(aper.getAvgRE()), new Double(aper.getAvgRP()), getControlTechnology(measure), 
                            getSourceGroup(measure), new Double(measure.getEquipmentLife()),  
                            getSectors(measure), measureClass(measure.getCmClass()), getLastModifiedTime(measure), 
                            measure.getLastModifiedBy(), getDateReviewed(measure), measure.getCreator().getName(), 
                            measure.getDataSouce(), measure.getDescription() };
                    Row row = new ViewableRow(measure, values);
                    rows.add(row);
                    found = true;
                    break;
                }
            }
            if (!found) {
                Object[] values = { measure.getName(), measure.getAbbreviation(), (majorPollutant ? getPollutantName(measure) : getPollutantName(pollutant)),
                        NAN_VALUE, NAN_VALUE, NAN_VALUE, 
                        NAN_VALUE, NAN_VALUE, NAN_VALUE, 
                        NAN_VALUE, NAN_VALUE, getControlTechnology(measure), 
                        getSourceGroup(measure), new Double(measure.getEquipmentLife()),  
                        getSectors(measure), measureClass(measure.getCmClass()), getLastModifiedTime(measure), 
                        measure.getLastModifiedBy(), getDateReviewed(measure), measure.getCreator().getName(), 
                        measure.getDataSouce(), measure.getDescription() };
                Row row = new ViewableRow(measure, values);
                rows.add(row);
            }
        }
        return rows;
    }

    private String getPollutantName(ControlMeasure measure) {
        if (measure == null)
            return "";
        if (measure.getMajorPollutant() == null)
            return "";
        return measure.getMajorPollutant().getName();
    }

    private String getPollutantName(Pollutant pollutant) {
        if (pollutant == null)
            return "";
        return pollutant.getName();
    }

    private String measureClass(ControlMeasureClass cmClass) {
        return (cmClass == null) ? "" : cmClass.getName();
    }

    private String getSectors(ControlMeasure measure) {
        Sector[] sectors = measure.getSectors();
        String sectorsString = "";
        if (sectors.length == 0)
            return null;

        for (int i = 0; i < sectors.length; i++) {
            if (i == sectors.length - 1) {
                sectorsString += sectors[i].getName();
                break;
            }

            sectorsString += sectors[i].getName() + "|";

        }

        return sectorsString;
    }

    private Object getDateReviewed(ControlMeasure measure) {
        return CustomDateFormat.format_MM_DD_YYYY(measure.getDateReviewed());
    }

    private Object getSourceGroup(ControlMeasure measure) {
        SourceGroup sourcegroup = measure.getSourceGroup();
        if (sourcegroup == null)
            return null;

        return sourcegroup.getName();
    }

    private String getControlTechnology(ControlMeasure measure) {
        ControlTechnology technology = measure.getControlTechnology();
        if (technology == null)
            return null;

        return technology.getName();
    }

    private Object getLastModifiedTime(ControlMeasure measure) {
        return CustomDateFormat.format_YYYY_MM_DD_HH_MM(measure.getLastModifiedTime());
    }

    public boolean isEditable(int col) {
        return false;
    }

    private void filter(Pollutant pollutant, String year) throws EmfException {
        this.targetYear = new YearValidation("Cost Year").value(year, costYearTable.getStartYear(), costYearTable.getEndYear());
        this.pollutant = pollutant;
//        if (pollutant.equalsIgnoreCase("major"))
//            measures(allMeasures);
//        else
//            measures(allMeasures, pollutant);

    }

//    private void measures(ControlMeasure[] measures) {
//        for (int i = 0; i < measures.length; i++) {
//            EfficiencyRecord record = getMaxEffRecord(measures[i], measures[i].getMajorPollutant().getName());
//            maxEffMap.put(measures[i].getAbbreviation(), record);
//        }
//    }
//
//    private void measures(ControlMeasure[] measures, String pollutant) {
//        for (int i = 0; i < measures.length; i++) {
//            EfficiencyRecord record = getMaxEffRecord(measures[i], pollutant);
//            maxEffMap.put(measures[i].getAbbreviation(), record);
//        }
//    }

//    private EfficiencyRecord getMaxEffRecord(ControlMeasure measure, String pollutant) {
//        EfficiencyRecord[] efficiencyRecords = filterPollutant(measure, pollutant);
//        if (efficiencyRecords.length == 0)
//            return null;
//
//        EfficiencyRecord maxRecord = efficiencyRecords[0];
//        for (int i = 1; i < efficiencyRecords.length; i++) {
//            if (efficiencyRecords[i].getEfficiency() > maxRecord.getEfficiency())
//                maxRecord = efficiencyRecords[i];
//        }
//
//        return maxRecord;
//    }

//    private EfficiencyRecord[] filterPollutant(ControlMeasure measure, String pollutant) {
//        List list = new ArrayList();
//        EfficiencyRecord[] efficiencyRecords = measure.getEfficiencyRecords();
//        for (int i = 0; i < efficiencyRecords.length; i++) {
//            if (efficiencyRecords[i].getPollutant().getName().equals(pollutant))
//                list.add(efficiencyRecords[i]);
//        }
//        return (EfficiencyRecord[]) list.toArray(new EfficiencyRecord[0]);
//    }

    private Double getCostPerTon(float costPerTon) throws EmfException {
        if (costPerTon == 0)
            return NAN_VALUE;
        costYearTable.setTargetYear(targetYear);
        
        double newCost = costPerTon * costYearTable.factor(CostYearTable.REFERENCE_COST_YEAR);
        return new Double(newCost);
    }

//    private Double getCostPerTon(EfficiencyRecord record) throws EmfException {
//        if (record == null)
//            return NAN_VALUE;
//        int costYear = record.getCostYear();
//        float costPerTon = record.getCostPerTon();
//        costYearTable.setTargetYear(targetYear);
//
//        double newCost = costPerTon * costYearTable.factor(costYear);
//        return new Double(newCost);
//    }
//
//    private Double getControlEfficiency(EfficiencyRecord record) {
//        if (record == null)
//            return NAN_VALUE;
//
//        return new Double(record.getEfficiency());
//    }
//
//    private Double ruleEffectiveness(EfficiencyRecord record) {
//        if (record == null)
//            return NAN_VALUE;
//        return new Double(record.getRuleEffectiveness());
//    }
//
//    private Double rulePenetration(EfficiencyRecord record) {
//        if (record == null)
//            return NAN_VALUE;
//        return new Double(record.getRulePenetration());
//    }
//
}
