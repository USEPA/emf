package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.framework.services.cost.ControlProgram;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.Row;
import gov.epa.emissions.framework.ui.ViewableRow;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ControlStrategyProgramTableData extends AbstractTableData {

    private List<Row> rows;

    public ControlStrategyProgramTableData(ControlProgram[] controlPrograms) {
        rows = createRows(controlPrograms);
    }

    private List<Row> createRows(ControlProgram[] controlPrograms) {
        List<Row> rows = new ArrayList<Row>();
        for (int i = 0; i < controlPrograms.length; i++) {
            Row row = row(controlPrograms[i]);
            rows.add(row);
        }
        return rows;
    }

    private Row row(ControlProgram controlProgram) {
        Object[] values = { controlProgram.getName(), controlProgram.getDescription(), (controlProgram.getControlProgramType() != null ? controlProgram.getControlProgramType().getName() : ""), (controlProgram.getDataset() != null ? controlProgram.getDataset().getName() : ""), (controlProgram.getDatasetVersion() != null ? controlProgram.getDatasetVersion() + "" : "") };
        return new ViewableRow(controlProgram, values);
    }

   
    public String[] columns() {
        return new String[] { "Name", "Desc", "Type", "Dataset", "Dataset Version" };
    }

    public Class getColumnClass(int col) {
        return String.class;
    }

    public List<Row> rows() {
        return rows;
    }

    public boolean isEditable(int col) {
        return false;
    }

    public void add(ControlProgram[] cm) {
        for (int i = 0; i < cm.length; i++) {
            Row row = row(cm[i]);
            if (!rows.contains(row))
                rows.add(row);
        }
        refresh();
    }

    public void refresh() {
        this.rows = createRows(sources());
    }

    public ControlProgram[] sources() {
        List sources = sourcesList();
        return (ControlProgram[]) sources.toArray(new ControlProgram[0]);
    }

    private List sourcesList() {
        List sources = new ArrayList();
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            ViewableRow row = (ViewableRow) iter.next();
            sources.add(row.source());
        }

        return sources;
    }

    private void remove(ControlProgram record) {
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            ViewableRow row = (ViewableRow) iter.next();
            ControlProgram source = (ControlProgram) row.source();
            if (source == record) {
                rows.remove(row);
                return;
            }
        }
    }

    public void remove(ControlProgram[] records) {
        for (int i = 0; i < records.length; i++)
            remove(records[i]);

        refresh();
    }

}
