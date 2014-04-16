package gov.epa.emissions.framework.ui;

import gov.epa.emissions.framework.services.data.EmfDataset;

import java.util.ArrayList;
import java.util.List;

public class EmfDatasetTableData extends AbstractTableData {

    private List rows;

    public EmfDatasetTableData(EmfDataset[] datasets) {
        this.rows = createRows(datasets);
    }

    public String[] columns() {
        return new String[] { "Name", "Last Modified Date", "Type", "Status", "Creator", "Intended Use", "Project",
                "Region", "Start Date", "End Date", "Temporal Resolution" };
    }

    public List rows() {
        return this.rows;
    }

    private List createRows(EmfDataset[] datasets) {
        List rows = new ArrayList();

        for (int i = 0; i < datasets.length; i++) {
            EmfDataset dataset = datasets[i];
            Object[] values = { dataset.getName(), format(dataset.getModifiedDateTime()), dataset.getDatasetTypeName(),
                    dataset.getStatus(), getFullName(dataset), dataset.getIntendedUse(), dataset.getProject(),
                    dataset.getRegion(), format(dataset.getStartDateTime()), format(dataset.getStopDateTime()), dataset.getTemporalResolution() };

            Row row = new ViewableRow(dataset, values);
            rows.add(row);
        }

        return rows;
    }

    public boolean isEditable(int col) {
        return false;
    }

    public Class getColumnClass(int col) {
        if (col < 0 || col > 10) {
            throw new IllegalArgumentException("Allowed values are between 0 and 8, but the value is " + col);
        }
        return String.class;
    }

    private String getFullName(EmfDataset dataset){
        String fullName = dataset.getCreatorFullName();
        if ((fullName == null) || (fullName.trim().equalsIgnoreCase("")))
            fullName = dataset.getCreator();
        return fullName;
    }   
}
