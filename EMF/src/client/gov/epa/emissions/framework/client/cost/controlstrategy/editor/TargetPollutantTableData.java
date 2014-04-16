package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyTargetPollutant;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.ui.AbstractEditableTableData;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.EditableRow;
import gov.epa.emissions.framework.ui.InlineEditableTableData;
import gov.epa.emissions.framework.ui.RowSource;
import gov.epa.emissions.framework.ui.ViewableRow;

import java.util.ArrayList;
import java.util.List;

public class TargetPollutantTableData extends AbstractTableData {

    private List<ViewableRow> rows;
    
    private Pollutant[] pollutants;

    public TargetPollutantTableData(ControlStrategyTargetPollutant[] targets, Pollutant[] allPollutants) {
        pollutants = allPollutants;
        rows = createRows(targets);
    }

    private List<ViewableRow> createRows(ControlStrategyTargetPollutant[] targets) {
        List<ViewableRow> rows = new ArrayList<ViewableRow>();
       
        for (int i = 0; i < targets.length; i++)
            rows.add(row(i + 1, targets[i], pollutants));

        return rows;
    }
    
    private ViewableRow row(int order, ControlStrategyTargetPollutant target, Pollutant[] polls) {
        RowSource<ControlStrategyTargetPollutant> source = new TargetPollutantRowSource(order, target, polls);
        return new ViewableRow(source);
    }
    
    public String[] columns() {
        return new String[] { "Order"
                ,"Pollutant"
                , "Min Emis Red"
                , "Min CEFF"
                , "Max CPT"
                , "Max Ann Cost"
                , "Inv Filter" 
                , "County Dataset" 
                , "County Dataset Version" 
                , "Replacement Control Min Efficiency Diff" 
                };
    }

    public Class<?> getColumnClass(int col) {
        if (col == 1
                || col == 6
                )
            return String.class;
                
        if (col == 7)
            return EmfDataset.class;

        if (col == 0 
                || col == 8)
            return Integer.class;

        return Double.class;
    }

    public List<ViewableRow> rows() {
        return this.rows;
    }

    public boolean isEditable(int col) {
        return false;
    }
    
    public void refresh() {
        this.rows = createRows(sources());
    }

    public ControlStrategyTargetPollutant[] sources() {
        return sourcesList().toArray(new ControlStrategyTargetPollutant[0]);
    }

    private List<ControlStrategyTargetPollutant> sourcesList() {
        List<ControlStrategyTargetPollutant> sources = new ArrayList<ControlStrategyTargetPollutant>();        
        for (ViewableRow row : this.rows) {
            sources.add((ControlStrategyTargetPollutant)row.source());
        }

        return sources;
    }

//    private List<ControlStrategyTargetPollutant> selected() {
//        List<ControlStrategyTargetPollutant> sources = new ArrayList<ControlStrategyTargetPollutant>();        
//        for (ViewableRow row : this.rows) {
//            if (row.)
//            ControlStrategyTargetPollutant controlStrategyTargetPollutant = (ControlStrategyTargetPollutant)row.source();
//            if (controlStrategyTargetPollutant)
//            sources.add(controlStrategyTargetPollutant);
//        }
//
//        return sources;
//    }

    public boolean contains(ControlStrategyTargetPollutant reference) {
        return this.sourcesList().contains(reference);
    }
    
    public void add(ControlStrategyTargetPollutant target) {
        rows.add(row(rows.size() + 1, target, pollutants));
    }

    private void remove(ControlStrategyTargetPollutant target) {
        for (ViewableRow row : this.rows) {
            ControlStrategyTargetPollutant source = (ControlStrategyTargetPollutant)row.source();
            
            if (source.equals(target)) {
                rows.remove(row);
                return;
            }
        }
    }

    public void remove(List<ControlStrategyTargetPollutant> references) {
        for (ControlStrategyTargetPollutant reference : references) {
            this.remove(reference);
        }
    }
}
