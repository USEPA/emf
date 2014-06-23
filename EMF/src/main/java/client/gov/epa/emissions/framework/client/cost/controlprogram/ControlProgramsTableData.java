package gov.epa.emissions.framework.client.cost.controlprogram;

import gov.epa.emissions.framework.services.cost.ControlProgram;
import gov.epa.emissions.framework.services.cost.ControlProgramType;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.Row;
import gov.epa.emissions.framework.ui.ViewableRow;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ControlProgramsTableData extends AbstractTableData {

    private List rows;

    public ControlProgramsTableData(ControlProgram[] controlPrograms) {
        this.rows = createRows(controlPrograms);
    }

    public String[] columns() {
        return new String[] { "Name", "Type", 
                "Start", "Last Modified","End", 
                "Dataset", "Version" };
    }

    public Class getColumnClass(int col) {
        if (col == 6)
            return Integer.class;

        return String.class;
    }

    public List rows() {
        return rows;
    }

    public boolean isEditable(int col) {
        return false;
    }

    private List createRows(ControlProgram[] controlPrograms) {
        List<Row> rows = new ArrayList<Row>();
        for (int i = 0; i < controlPrograms.length; i++) {
            ControlProgram element = controlPrograms[i];
            Object[] values = { element.getName(), controlProgramType(element), 
                    format(element.getStartDate()), this.format(element.getLastModifiedDate()), format(element.getEndDate()), 
                    dataset(element), datasetVersion(element) };
            Row row = new ViewableRow(element, values);
            rows.add(row);
        }

        return rows;
    }
    
    private String controlProgramType(ControlProgram element) {
        ControlProgramType controlProgramType = element.getControlProgramType();
        return controlProgramType != null ? controlProgramType.getName() : "";
    }

    private String dataset(ControlProgram element) {
        EmfDataset dataset = element.getDataset();
        return dataset != null ? dataset.getName() : "";
    }

    private Integer datasetVersion(ControlProgram element) {
        Integer version = element.getDatasetVersion();
        return version != null ? version : 0;
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
    }
}
