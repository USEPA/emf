package gov.epa.emissions.framework.client.module;

import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.module.HistoryDataset;
import gov.epa.emissions.framework.services.module.HistorySubmodule;
import gov.epa.emissions.framework.services.module.ModuleDataset;
import gov.epa.emissions.framework.services.module.ModuleTypeVersionDataset;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.ViewableRow;
import gov.epa.emissions.framework.ui.Row;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HistorySubmodulesTableData extends AbstractTableData {
    private List rows;

    public HistorySubmodulesTableData(Map<String, HistorySubmodule> historySubmodules) {
        this.rows = createRows(historySubmodules);
    }

    public String[] columns() {
        return new String[] { "Submodule", "Date", "Duration", "Status", "Result", "Error"};
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

    private List createRows(Map<String, HistorySubmodule> historySubmodules) {
        List rows = new ArrayList();

        for (HistorySubmodule historySubmodule : historySubmodules.values()) {
            Object[] values = { historySubmodule.getSubmodulePathNames(),
                                CustomDateFormat.format_YYYY_MM_DD_HH_MM(historySubmodule.getCreationDate()),
                                historySubmodule.getDurationSeconds(),
                                historySubmodule.getStatus(),
                                historySubmodule.getResult(),
                                historySubmodule.getErrorMessage() };

            Row row = new ViewableRow(historySubmodule, values);
            rows.add(row);
        }
        return rows;
    }
}
