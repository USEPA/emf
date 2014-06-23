package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.ui.AbstractEditableTableData;
import gov.epa.emissions.framework.ui.EditableRow;
import gov.epa.emissions.framework.ui.InlineEditableTableData;
import gov.epa.emissions.framework.ui.RowSource;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class EditableInputDatasetTableData extends AbstractEditableTableData implements InlineEditableTableData {
    private List<EditableRow> rows;

    public EditableInputDatasetTableData(EmfDataset[] inputDatasets) {
        this.rows = createRows(inputDatasets);
    }

    public String[] columns() {
        return new String[] { "Select", "Type", "Dataset", "Version" };
    }

    public List<EditableRow> rows() {
        return rows;
    }

    public boolean isEditable(int row, int col) {
//        EditableRow editableRow = rows.get(row);
//        EmfDataset inputDataset = (EmfDataset) editableRow.source();
//        if (contains(EmfDataset.getName()) && (col == 0 || col == 1)) {
//            return false;
//        }
        return false;
    }

//    private boolean contains(EmfDataset inputDataset) {
//        KeyVal[] keyvals = datasetType.getKeyVals();
//        for (int i = 0; i < keyvals.length; i++) {
//            if (keyword.equals(keyvals[i].getKeyword())) {
//                return true;
//            }
//        }
//        return false;
//    }

    public boolean isEditable(int col) {
        return false;
    }

    private List createRows(EmfDataset[] inputDatasets) {
        List rows = new ArrayList();
        for (int i = 0; i < inputDatasets.length; i++)
            rows.add(row(inputDatasets[i]));

        return rows;
    }

//    private static boolean contains(List keyVals, KeyVal newKeyVal) {
//        for (Iterator iter = keyVals.iterator(); iter.hasNext();) {
//            KeyVal element = (KeyVal) iter.next();
//            if (element.getKeyword().equals(newKeyVal.getKeyword()))
//                return true;
//        }
//
//        return false;
//    }

    void remove(EmfDataset inputDataset) {
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            EditableRow row = (EditableRow) iter.next();
            EmfDataset source = (EmfDataset) row.source();
            if (source == inputDataset) {
                rows.remove(row);
                return;
            }
        }
    }

    private EditableRow row(EmfDataset inputDataset) {
        RowSource source = new EditableInputDatasetRowSource(inputDataset);
        return new EditableRow(source);
    }

    private EmfDataset[] getSelected() {
        List<EmfDataset> selected = new ArrayList<EmfDataset>();

        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            EditableRow row = (EditableRow) iter.next();
            EditableInputDatasetRowSource rowSource = (EditableInputDatasetRowSource) row.rowSource();
            if (rowSource.isSelected())
                selected.add((EmfDataset)rowSource.source());
        }

        return selected.toArray(new EmfDataset[0]);
    }

    public void remove(EmfDataset[] values) {
        for (int i = 0; i < values.length; i++)
            remove(values[i]);
    }

    public EmfDataset[] sources() {
        List<EmfDataset> sources = sourcesList();
        return sources.toArray(new EmfDataset[0]);
    }

    private List<EmfDataset> sourcesList() {
        List<EmfDataset> sources = new ArrayList<EmfDataset>();
        int rowNumber = 0;
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            rowNumber++;
            EditableRow row = (EditableRow) iter.next();
            EditableInputDatasetRowSource rowSource = (EditableInputDatasetRowSource) row.rowSource();
            rowSource.validate(rowNumber);
            sources.add((EmfDataset)rowSource.source());
        }
        return sources;
    }

    public void addBlankRow() {
//        KeyVal keyVal = new KeyVal();
//        keyVal.setKeyword(new Keyword(""));
//        keyVal.setValue("");

//        rows.add(row(keyVal, masterKeywords));
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
