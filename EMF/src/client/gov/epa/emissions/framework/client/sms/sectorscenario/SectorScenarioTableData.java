package gov.epa.emissions.framework.client.sms.sectorscenario;

import gov.epa.emissions.framework.services.sms.SectorScenario;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.Row;
import gov.epa.emissions.framework.ui.ViewableRow;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SectorScenarioTableData extends AbstractTableData {

    private List rows;

//    private final static Double NAN_VALUE = Double.valueOf(Double.NaN);
    
//    private EmfSession session;
    
    public SectorScenarioTableData(SectorScenario[] sectorScerarios) {
//        this.session = session;
        this.rows = createRows(sectorScerarios);
    }

    public String[] columns() {
        return new String[] { "Name", "Abbreviation", "Project", "Last Modified", "Run Status", 
                 "Creator", "Start Date", "End Date" };
    }

    public Class getColumnClass(int col) {
//        if (col == 1 )
//            return Integer.class;

        return String.class;
    }

    public List rows() {
        return rows;
    }

    public boolean isEditable(int col) {
        return false;
    }

    private List createRows(SectorScenario[] sectorScerarios) {
        List rows = new ArrayList();
        for (int i = 0; i < sectorScerarios.length; i++) {
            SectorScenario element = sectorScerarios[i];
            Object[] values = { element.getName(), element.getAbbreviation(), project(element), 
                    format(element.getLastModifiedDate()), element.getRunStatus(), 
                    element.getCreator().getName(), format(element.getStartDate()),
                    format(element.getCompletionDate())};
            Row row = new ViewableRow(element, values);
            rows.add(row);
        }

        return rows;
    }

    public SectorScenario[] sources() {
        List sources = sourcesList();
        return (SectorScenario[]) sources.toArray(new SectorScenario[0]);
    }

    private List sourcesList() {
        List sources = new ArrayList();
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            ViewableRow row = (ViewableRow) iter.next();
            sources.add(row.source());
        }

        return sources;
    }

    private void remove(SectorScenario record) {
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            ViewableRow row = (ViewableRow) iter.next();
            SectorScenario source = (SectorScenario) row.source();
            if (source == record) {
                rows.remove(row);
                return;
            }
        }
    }

    public void remove(SectorScenario[] records) {
        for (int i = 0; i < records.length; i++)
            remove(records[i]);
    }
    
    private String project(SectorScenario element) {
        //return element.getProject() != null ? element.getProject().getName() : "";
        return "";
    }
}
