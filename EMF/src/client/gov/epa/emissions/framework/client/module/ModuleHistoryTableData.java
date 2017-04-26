package gov.epa.emissions.framework.client.module;

import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.services.module.History;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.ViewableRow;
import gov.epa.emissions.framework.ui.Row;

import java.util.ArrayList;
import java.util.List;

public class ModuleHistoryTableData extends AbstractTableData {
    private List rows;

    public ModuleHistoryTableData(List<History> moduleHistory) {
        this.rows = createRows(moduleHistory);
    }

    public String[] columns() {
        return new String[] { "Run ID", "Date", "User", "Duration", "Status", "Result", "Error"};
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

    private void add(History history) {
        Object[] values = { history.getRunId(),
                            CustomDateFormat.format_YYYY_MM_DD_HH_MM(history.getCreationDate()),
                            history.getCreator().getName(),
                            history.getDurationSeconds(),
                            history.getStatus(),
                            history.getResult(),
                            history.getErrorMessage() };

        Row row = new ViewableRow(history, values);
        this.rows.add(row);
    }

    private List createRows(List<History> moduleHistory) {
        List rows = new ArrayList();

        for (History history : moduleHistory) {
            Object[] values = { history.getRunId(),
                                CustomDateFormat.format_YYYY_MM_DD_HH_MM(history.getCreationDate()),
                                history.getCreator().getName(),
                                history.getDurationSeconds(),
                                history.getStatus(),
                                history.getResult(),
                                history.getErrorMessage() };

            Row row = new ViewableRow(history, values);
            rows.add(row);
        }

        return rows;
    }
}
