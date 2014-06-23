package gov.epa.emissions.framework.client.meta.logs;

import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.services.basic.AccessLog;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.Row;
import gov.epa.emissions.framework.ui.ViewableRow;

import java.util.ArrayList;
import java.util.List;

public class LogsTableData extends AbstractTableData {

    private List rows;

    public LogsTableData(AccessLog[] logs) {
        this.rows = createRows(logs);
    }

    public String[] columns() {
        return new String[] { "User", "Version", 
                "Start Date", "End Date", "Lines Exported", 
                "Time Reqd. (secs)", "Purpose", "Export Location" };
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

    private List createRows(AccessLog[] logs) {
        List rows = new ArrayList();

        for (int i = 0; i < logs.length; i++) {
            AccessLog log = logs[i];
            Object[] values = { log.getUsername(), log.getVersion(), 
                    CustomDateFormat.format_YYYY_MM_DD_HH_MM(log.getTimestamp()),
                    CustomDateFormat.format_YYYY_MM_DD_HH_MM(log.getEnddate()), 
                    log.getLinesExported() + "", log.getTimereqrd() + "", 
                    log.getDescription(), log.getFolderPath() };

            Row row = new ViewableRow(log, values);
            rows.add(row);
        }

        return rows;
    }

}
