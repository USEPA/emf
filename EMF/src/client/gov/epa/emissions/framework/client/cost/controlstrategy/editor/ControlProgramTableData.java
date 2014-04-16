package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.framework.services.cost.ControlProgram;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.Row;
import gov.epa.emissions.framework.ui.ViewableRow;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ControlProgramTableData extends AbstractTableData {

    private List rows;

    public ControlProgramTableData(ControlProgram[] controlTechnologies) {
        rows = createRows(controlTechnologies);
    }

    private List createRows(ControlProgram[] controlTechnologies) {
        List rows = new ArrayList();
        for (int i = 0; i < controlTechnologies.length; i++) {
            Row row = row(controlTechnologies[i]);
            rows.add(row);
        }
        return rows;
    }

    private Row row(ControlProgram controlTechnology) {
        String[] values = { controlTechnology.getName(), controlTechnology.getControlProgramType() != null ? controlTechnology.getControlProgramType().getName() : "", controlTechnology.getDescription() };
        return new ViewableRow(controlTechnology, values);
    }

    public String[] columns() {
        return new String[] { "Name", "Type", "Description" };
    }

    public Class getColumnClass(int col) {
        return String.class;
    }

    public List rows() {
        return rows;
    }

    public boolean isEditable(int col) {
        return false;
    }

    public void add(ControlProgram[] sccs) {
        for (int i = 0; i < sccs.length; i++) {
            Row row = row(sccs[i]);
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
