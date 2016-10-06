package gov.epa.emissions.framework.client.moduletype;

import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.services.module.ModuleTypeVersionRevision;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.ViewableRow;
import gov.epa.emissions.framework.ui.Row;

import java.util.ArrayList;
import java.util.List;

public class ModuleTypeVersionRevisionsTableData extends AbstractTableData {
    private List rows;

    public ModuleTypeVersionRevisionsTableData(List<ModuleTypeVersionRevision> moduleTypeVersionRevisions) {
        this.rows = createRows(moduleTypeVersionRevisions);
    }

    public String[] columns() {
        return new String[] { "Revision", "Date", "Creator", "Description"};
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

    private void add(ModuleTypeVersionRevision element) {
        String safeCreationDate = (element.getCreationDate() == null) ? "" : CustomDateFormat.format_YYYY_MM_DD_HH_MM(element.getCreationDate());
        String safeCreator = (element.getCreator() == null) ? "" : element.getCreator().getName();
        Object[] values = { element.getRevision(),
                            safeCreationDate,
                            safeCreator,
                            getShortDescription(element.getDescription()) };

        Row row = new ViewableRow(element, values);
        this.rows.add(row);
    }

    private List createRows(List<ModuleTypeVersionRevision> moduleTypeVersionRevisions) {
        List rows = new ArrayList();

        for (ModuleTypeVersionRevision element : moduleTypeVersionRevisions) {
            String safeCreationDate = (element.getCreationDate() == null) ? "" : CustomDateFormat.format_YYYY_MM_DD_HH_MM(element.getCreationDate());
            String safeCreator = (element.getCreator() == null) ? "" : element.getCreator().getName();
            Object[] values = { element.getRevision(),
                                safeCreationDate,
                                safeCreator,
                                getShortDescription(element.getDescription()) };

            Row row = new ViewableRow(element, values);
            rows.add(row);
        }

        return rows;
    }

    private String getShortDescription(String description) {
        if (description == null)
            return "";
        
        if (description.length() > 100)
            return description.substring(0, 96) + " ...";

        return description;
    }
}
