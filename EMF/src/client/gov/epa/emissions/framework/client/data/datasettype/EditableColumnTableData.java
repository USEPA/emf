package gov.epa.emissions.framework.client.data.datasettype;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.ui.AbstractEditableTableData;
import gov.epa.emissions.framework.ui.EditableRow;
import gov.epa.emissions.framework.ui.InlineEditableTableData;
import gov.epa.emissions.framework.ui.RowSource;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class EditableColumnTableData extends AbstractEditableTableData implements InlineEditableTableData {
    private List rows;

//    private Keywords masterKeywords;
//
    private DatasetType datasetType;

    public EditableColumnTableData(Column[] column) {
        this.rows = createRows(column);
    }

    public String[] columns() {
        return new String[] { "Selected", "Column", "Data Type", "Default Value", "Mandatory", "Description", "Formatter Class", "Constraints", "Width", "Spaces", "Fix Format Start", "Fix Format End" };
    }

    public List rows() {
        return rows;
    }

    public boolean isEditable(int row, int col) {
        if (col == 4
                || col == 6)
            return false;

        return true;
    }

    public boolean isEditable(int col) {

        return true;
    }

    private List createRows(Column[] columns) {
        List rows = new ArrayList();
        for (int i = 0; i < columns.length; i++)
            rows.add(row(columns[i]));

        return rows;
    }

    void remove(Column column) {
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            EditableRow row = (EditableRow) iter.next();
            Column source = (Column) row.source();
            if (source == column) {
                rows.remove(row);
                return;
            }
        }
    }

    public void remove(Column[] values) {
        for (int i = 0; i < values.length; i++)
            remove(values[i]);
    }

    private EditableRow row(Column column) {
        RowSource source = new EditableColumnRowSource(column);
        return new EditableRow(source);
    }

    public Column[] sources() throws EmfException {
        List sources = sourcesList();
        return (Column[]) sources.toArray(new Column[0]);
    }

    private List sourcesList() throws EmfException {
        List sources = new ArrayList();
        int rowNumber = 0;
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            rowNumber++;
            EditableRow row = (EditableRow) iter.next();
            EditableColumnRowSource rowSource = (EditableColumnRowSource) row.rowSource();
            rowSource.validate(rowNumber);
            sources.add(rowSource.source());
        }
        return sources;
    }

    public void addBlankRow() {
        Column column = new Column();

        rows.add(row(column));
    }

    private Column[] getSelected() {
        List selected = new ArrayList();

        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            EditableRow row = (EditableRow) iter.next();
            EditableColumnRowSource rowSource = (EditableColumnRowSource) row.rowSource();
            if (rowSource.isSelected())
                selected.add(rowSource.source());
        }

        return (Column[]) selected.toArray(new Column[0]);
    }

    public void removeSelected() {
        remove(getSelected());
    }

    @Override
    public int getSelectedCount() {
        return getSelected().length;
    }

    public Class getColumnClass(int col) {
        if (col == 0)
            return Boolean.class;

        return String.class;
    }

}
