package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.framework.services.cost.ControlStrategyMeasure;
import gov.epa.emissions.framework.services.cost.LightControlMeasure;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.Row;
import gov.epa.emissions.framework.ui.ViewableRow;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ControlStrategyMeasureTableData extends AbstractTableData {

    private List rows;

    public ControlStrategyMeasureTableData(ControlStrategyMeasure[] cms) {
        rows = createRows(cms);
    }

    private List createRows(ControlStrategyMeasure[] cms) {
        List rows = new ArrayList();
        for (int i = 0; i < cms.length; i++) {
            Row row = row(cms[i]);
            if ( row != null) {
                rows.add(row);
            }
        }
        return rows;
    }

    private Row row(ControlStrategyMeasure csm) {
        if ( csm == null) {
            return null;
        }
        
        LightControlMeasure cm = csm.getControlMeasure();
        Object[] values = { cm.getAbbreviation(), 
        csm.getApplyOrder() != null ? csm.getApplyOrder() : Double.NaN,
        csm.getRuleEffectiveness() != null ? csm.getRuleEffectiveness() : Double.NaN,
        csm.getRulePenetration() != null ? csm.getRulePenetration() : Double.NaN,  
        cm.getName(), csm.getRegionDataset()!=null? csm.getRegionDataset().getName(): null, 
        csm.getRegionDatasetVersion()!= null ? csm.getRegionDatasetVersion(): null};
        return new ViewableRow(csm, values);
    }

   
    public String[] columns() {
        return new String[] { "Abbrev", "Order", 
                "RE", "RP", "Name", "Region", "Version"};
    }

    public Class getColumnClass(int col) {
        
        if (col == 1 || col == 2 || col == 3)
            return Double.class;

        return String.class;
    }

    public List rows() {
        return rows;
    }

    public boolean isEditable(int col) {
        return false;
    }

    public void add(ControlStrategyMeasure[] cms) {
        for (int i = 0; i < cms.length; i++) {
            Row row = row(cms[i]);
            if (!rows.contains(row))
                rows.add(row);
        }
        refresh();
    }

    public void refresh() {
        this.rows = createRows(sources());
    }

    public ControlStrategyMeasure[] sources() {
        List sources = sourcesList();
        return (ControlStrategyMeasure[]) sources.toArray(new ControlStrategyMeasure[0]);
    }

    private List sourcesList() {
        List sources = new ArrayList();
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            ViewableRow row = (ViewableRow) iter.next();
            sources.add(row.source());
        }

        return sources;
    }

    private void remove(ControlStrategyMeasure record) {
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            ViewableRow row = (ViewableRow) iter.next();
            ControlStrategyMeasure source = (ControlStrategyMeasure) row.source();
            if (source == record) {
                rows.remove(row);
                return;
            }
        }
    }

    public void remove(ControlStrategyMeasure[] records) {
        for (int i = 0; i < records.length; i++)
            remove(records[i]);

        refresh();
    }

}
