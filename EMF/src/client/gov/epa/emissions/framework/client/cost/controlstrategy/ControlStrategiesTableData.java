package gov.epa.emissions.framework.client.cost.controlstrategy;

import gov.epa.emissions.commons.data.Project;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.StrategyType;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.Row;
import gov.epa.emissions.framework.ui.ViewableRow;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ControlStrategiesTableData extends AbstractTableData {

    private List rows;

    private final static Double NAN_VALUE = new Double(Double.NaN);
    
//    private EmfSession session;
    
    public ControlStrategiesTableData(ControlStrategy[] controlStrategies) {
//        this.session = session;
        this.rows = createRows(controlStrategies);
    }

    public String[] columns() {
        return new String[] { "Name", "Last Modified", "Is Final", "Run Status", "Region", 
                "Target Pollutant", "Total Cost", "Reduction (tons)", "Average Cost Per Ton", 
                "Project", "Strategy Type", "Cost Year", 
                "Inv. Year", "Creator" };
    }

    public Class getColumnClass(int col) {
        if (col == 2)
            return Boolean.class;

        if (col == 7 || col == 6 || col == 8)
            return Double.class;

        return String.class;
    }

    public List rows() {
        return rows;
    }

    public boolean isEditable(int col) {
        return false;
    }

    private List createRows(ControlStrategy[] controlStrategies) {
        List rows = new ArrayList();
        for (int i = 0; i < controlStrategies.length; i++) {
            ControlStrategy element = controlStrategies[i];
            Object[] values = { element.getName(), format(element.getLastModifiedDate()), (element.getIsFinal() == null || !element.getIsFinal() ? false : true), element.getRunStatus(), region(element),
                    element.getTargetPollutant(), element.getTotalCost() != null ? element.getTotalCost() : NAN_VALUE /*getTotalCost(element.getId())*/, element.getTotalReduction() != null ? element.getTotalReduction() : NAN_VALUE /*getReduction(element.getId())*/, element.getTotalReduction() != null && element.getTotalReduction() != 0.0D && element.getTotalCost() != null ? element.getTotalCost() / element.getTotalReduction() : NAN_VALUE, 
                    project(element), analysisType(element), costYear(element), 
                    "" + (element.getInventoryYear() != 0 ? element.getInventoryYear() : ""), 
                    element.getCreator().getName() };
            Row row = new ViewableRow(element, values);
            rows.add(row);
        }

        return rows;
    }

//    private Double getReduction(int controlStrategyId) throws EmfException {
//        ControlStrategyResultsSummary summary = getResultSummary(controlStrategyId);
//        if (summary == null)
//            return NAN_VALUE;
// 
//        return new Double(summary.getStrategyTotalReduction());
//    }
//
//    private Double getTotalCost(int controlStrategyId) throws EmfException {
//        ControlStrategyResultsSummary summary = getResultSummary(controlStrategyId);
//        if (summary == null)
//            return NAN_VALUE;
//
//        return new Double(summary.getStrategyTotalCost());
//    }
//
//    private ControlStrategyResult[] getControlStrategyResults(int controlStrategyId) throws EmfException {
//        return session.controlStrategyService().getControlStrategyResults(controlStrategyId);
//    }

//    private ControlStrategyResultsSummary getResultSummary(int controlStrategyId) throws EmfException {
//        ControlStrategyResult[] controlStrategyResults = getControlStrategyResults(controlStrategyId);
//        if (controlStrategyResults.length == 0)
//            return null;
//
//        return new ControlStrategyResultsSummary(controlStrategyResults);
//    }
//
    private String project(ControlStrategy element) {
        Project project = element.getProject();
        return project != null ? project.getName() : "";
    }

    private String region(ControlStrategy element) {
        return element.getRegion() != null ? element.getRegion().getName() : "";
    }

    private String analysisType(ControlStrategy element) {
        StrategyType type = element.getStrategyType();
        return type != null ? type.getName() : "";
    }

    private String costYear(ControlStrategy element) {
        return "" + (element.getCostYear() != 0 ? element.getCostYear() : "");
    }

    public ControlStrategy[] sources() {
        List sources = sourcesList();
        return (ControlStrategy[]) sources.toArray(new ControlStrategy[0]);
    }

    private List sourcesList() {
        List sources = new ArrayList();
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            ViewableRow row = (ViewableRow) iter.next();
            sources.add(row.source());
        }

        return sources;
    }

    private void remove(ControlStrategy record) {
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            ViewableRow row = (ViewableRow) iter.next();
            ControlStrategy source = (ControlStrategy) row.source();
            if (source == record) {
                rows.remove(row);
                return;
            }
        }
    }

    public void remove(ControlStrategy[] records) {
        for (int i = 0; i < records.length; i++)
            remove(records[i]);
    }
}
