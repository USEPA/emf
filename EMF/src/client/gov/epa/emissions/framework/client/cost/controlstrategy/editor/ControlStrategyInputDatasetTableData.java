package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.framework.services.cost.ControlStrategyInputDataset;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.Row;
import gov.epa.emissions.framework.ui.ViewableRow;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ControlStrategyInputDatasetTableData extends AbstractTableData {

    private List rows;

    public ControlStrategyInputDatasetTableData(ControlStrategyInputDataset[] controlStrategyInputDatasets) {
        rows = createRows(controlStrategyInputDatasets);
    }

    private List createRows(ControlStrategyInputDataset[] controlStrategyInputDatasets) {
        List rows = new ArrayList();
        for (int i = 0; i < controlStrategyInputDatasets.length; i++) {
            Row row = row(controlStrategyInputDatasets[i]);
            rows.add(row);
        }
        return rows;
    }

    private Row row(ControlStrategyInputDataset controlStrategyInputDataset) {
        Object[] values = { controlStrategyInputDataset.getInputDataset().getDatasetType().getName(), controlStrategyInputDataset.getInputDataset().getName(), controlStrategyInputDataset.getVersion()};
        return new ViewableRow(controlStrategyInputDataset, values);
    }

    public String[] columns() {
        return new String[] { "Type", "Dataset", "Version" };
    }

    public Class getColumnClass(int col) {
        if (col == 2)
            return Integer.class;

        return String.class;
    }

    public List rows() {
        return rows;
    }

    public boolean isEditable(int col) {
        return false;
    }

    public void add(ControlStrategyInputDataset[] controlStrategyInputDatasets) {
        for (int i = 0; i < controlStrategyInputDatasets.length; i++) {
            Row row = row(controlStrategyInputDatasets[i]);
            if (!rows.contains(row))
                rows.add(row);
        }
        refresh();
    }

    public void refresh() {
        this.rows = createRows(sources());
    }

    public ControlStrategyInputDataset[] sources() {
        List sources = sourcesList();
        return (ControlStrategyInputDataset[]) sources.toArray(new ControlStrategyInputDataset[0]);
    }

    private List sourcesList() {
        List sources = new ArrayList();
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            ViewableRow row = (ViewableRow) iter.next();
            sources.add(row.source());
        }

        return sources;
    }

    private void remove(ControlStrategyInputDataset controlStrategyInputDataset) {
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            ViewableRow row = (ViewableRow) iter.next();
            ControlStrategyInputDataset source = (ControlStrategyInputDataset) row.source();
            if (source == controlStrategyInputDataset) {
                rows.remove(row);
                return;
            }
        }
    }

    public void remove(ControlStrategyInputDataset[] controlStrategyInputDatasets) {
        for (int i = 0; i < controlStrategyInputDatasets.length; i++)
            remove(controlStrategyInputDatasets[i]);

        refresh();
    }
}