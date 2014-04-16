package gov.epa.emissions.framework.client.meta.keywords;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.KeyVal;
import gov.epa.emissions.commons.data.Keyword;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.Keywords;
import gov.epa.emissions.framework.ui.AbstractEditableTableData;
import gov.epa.emissions.framework.ui.EditableRow;
import gov.epa.emissions.framework.ui.RowSource;
import gov.epa.emissions.framework.ui.InlineEditableTableData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class EditableKeyValueTableData extends AbstractEditableTableData implements InlineEditableTableData {
    private List rows;

    private Keywords masterKeywords;

    private DatasetType datasetType;

    public EditableKeyValueTableData(KeyVal[] keyVals, Keywords masterKeywords) {
        this.masterKeywords = masterKeywords;
        this.rows = createRows(keyVals, masterKeywords);
    }

    public EditableKeyValueTableData(KeyVal[] datasetKeyVals, KeyVal[] datasetTypeKeyVals, Keywords masterKeywords) {
        this(mergeKeyVals(datasetKeyVals, datasetTypeKeyVals), masterKeywords);
    }

    public String[] columns() {
        return new String[] { "Select", "Keyword", "Value" };
    }

    public List rows() {
        return rows;
    }

    public boolean isEditable(int row, int col) {
        EditableRow editableRow = (EditableRow) rows.get(row);
        KeyVal keyVal = (KeyVal) editableRow.source();
        if (contains(keyVal.getKeyword()) && (col == 0 || col == 1)) {
            return false;
        }
        return true;
    }

    private boolean contains(Keyword keyword) {
        KeyVal[] keyvals = datasetType.getKeyVals();
        for (int i = 0; i < keyvals.length; i++) {
            if (keyword.equals(keyvals[i].getKeyword())) {
                return true;
            }
        }
        return false;
    }

    public boolean isEditable(int col) {
        return true;
    }

    private List createRows(KeyVal[] keyVals, Keywords masterKeywords) {
        List rows = new ArrayList();
        for (int i = 0; i < keyVals.length; i++)
            rows.add(row(keyVals[i], masterKeywords));

        return rows;
    }

    private static KeyVal[] mergeKeyVals(KeyVal[] datasetKeyVals, KeyVal[] datasetTypeKeyVals) {
        List result = new ArrayList();
        result.addAll(Arrays.asList(datasetKeyVals));

        for (int i = 0; i < datasetTypeKeyVals.length; i++) {
            if (!contains(result, datasetTypeKeyVals[i])) {
                result.add(datasetTypeKeyVals[i]);
            }
        }
        return (KeyVal[]) result.toArray(new KeyVal[0]);
    }

    private static boolean contains(List keyVals, KeyVal newKeyVal) {
        for (Iterator iter = keyVals.iterator(); iter.hasNext();) {
            KeyVal element = (KeyVal) iter.next();
            if (element.getKeyword().equals(newKeyVal.getKeyword()))
                return true;
        }

        return false;
    }

    void remove(KeyVal keyValue) {
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            EditableRow row = (EditableRow) iter.next();
            KeyVal source = (KeyVal) row.source();
            if (source == keyValue) {
                rows.remove(row);
                return;
            }
        }
    }

    private EditableRow row(KeyVal keyValue, Keywords keywords) {
        RowSource source = new EditableKeyValueRowSource(keyValue, keywords);
        return new EditableRow(source);
    }

    private KeyVal[] getSelected() {
        List selected = new ArrayList();

        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            EditableRow row = (EditableRow) iter.next();
            EditableKeyValueRowSource rowSource = (EditableKeyValueRowSource) row.rowSource();
            if (rowSource.isSelected())
                selected.add(rowSource.source());
        }

        return (KeyVal[]) selected.toArray(new KeyVal[0]);
    }

    public void remove(KeyVal[] values) {
        for (int i = 0; i < values.length; i++)
            remove(values[i]);
    }

    public KeyVal[] sources() throws EmfException {
        List sources = sourcesList();
        return (KeyVal[]) sources.toArray(new KeyVal[0]);
    }

    private List sourcesList() throws EmfException {
        List sources = new ArrayList();
        int rowNumber = 0;
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            rowNumber++;
            EditableRow row = (EditableRow) iter.next();
            EditableKeyValueRowSource rowSource = (EditableKeyValueRowSource) row.rowSource();
            rowSource.validate(rowNumber);
            sources.add(rowSource.source());
        }
        return sources;
    }

    public void addBlankRow() {
        KeyVal keyVal = new KeyVal();
        keyVal.setKeyword(new Keyword(""));
        keyVal.setValue("");

        rows.add(row(keyVal, masterKeywords));
    }

    public void removeSelected() {
        remove(getSelected());
    }

    public Class getColumnClass(int col) {
        if (col == 0)
            return Boolean.class;

        return String.class;
    }

}
