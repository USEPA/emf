package gov.epa.emissions.framework.client.moduletype;

import gov.epa.emissions.framework.services.module.Tag;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.ViewableRow;
import gov.epa.emissions.framework.ui.Row;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TagsTableData extends AbstractTableData {
    private List rows;

    public TagsTableData(Set<Tag> tags) {
        this.rows = createRows(tags);
    }

    public String[] columns() {
        return new String[] { "Name", "Description"};
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

    private List createRows(Set<Tag> tags) {
        List rows = new ArrayList();

        for (Tag tag : tags) {
            Object[] values = { tag.getName(),
                                tag.getDescription() };

            Row row = new ViewableRow(tag, values);
            rows.add(row);
        }

        return rows;
    }
}
