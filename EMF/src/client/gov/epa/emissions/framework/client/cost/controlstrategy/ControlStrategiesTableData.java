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

    private final static Double NAN_VALUE = Double.valueOf(Double.NaN);
    
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
            Row row = row(controlStrategies[i]);
            if ( row != null) {
                rows.add(row);
            }
        }

        return rows;
    }
    
    private Row row(ControlStrategy strategy) {
        if (strategy == null) return null;
        
        Object[] values = { strategy.getName(), format(strategy.getLastModifiedDate()), (strategy.getIsFinal() == null || !strategy.getIsFinal() ? false : true), strategy.getRunStatus(), region(strategy),
                strategy.getTargetPollutant(), strategy.getTotalCost() != null ? strategy.getTotalCost() : NAN_VALUE /*getTotalCost(element.getId())*/, strategy.getTotalReduction() != null ? strategy.getTotalReduction() : NAN_VALUE /*getReduction(element.getId())*/, strategy.getTotalReduction() != null && strategy.getTotalReduction() != 0.0D && strategy.getTotalCost() != null ? strategy.getTotalCost() / strategy.getTotalReduction() : NAN_VALUE, 
                project(strategy), analysisType(strategy), costYear(strategy), 
                "" + (strategy.getInventoryYear() != 0 ? strategy.getInventoryYear() : ""), 
                strategy.getCreator().getName() };
        Row row = new ViewableRow(strategy, values);
        
        return row;
    }

//    private Double getReduction(int controlStrategyId) throws EmfException {
//        ControlStrategyResultsSummary summary = getResultSummary(controlStrategyId);
//        if (summary == null)
//            return NAN_VALUE;
// 
//        return Double.valueOf(summary.getStrategyTotalReduction());
//    }
//
//    private Double getTotalCost(int controlStrategyId) throws EmfException {
//        ControlStrategyResultsSummary summary = getResultSummary(controlStrategyId);
//        if (summary == null)
//            return NAN_VALUE;
//
//        return Double.valueOf(summary.getStrategyTotalCost());
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

    public void add(ControlStrategy[] records) {
        for (int i = 0; i < records.length; i++) {
            Row row = row(records[i]);
            if (!rows.contains(row))
                rows.add(row);
        }
    }
}
