package gov.epa.emissions.framework.client.fast;

import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.Row;
import gov.epa.emissions.framework.ui.ViewableRow;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractMPSDTTableData<T> extends AbstractTableData {

    private List<Row<T>> rows;

    public static final String DEFAULT_VALUE = "N/A";

    public AbstractMPSDTTableData(T[] sources) {

        List<T> sourceList = new ArrayList<T>(sources.length);
        for (T source : sources) {
            sourceList.add(source);
        }

        this.rows = createRows(sourceList);
    }

    abstract public String[] columns();

    public Class<?> getColumnClass(int col) {
        return String.class;
    }

    public List<Row<T>> rows() {
        return rows;
    }

    public boolean isEditable(int col) {
        return false;
    }

    // private Row<T> row(T source) {
    //
    // Object[] values = this.createRowValues(source);
    // return new ViewableRow<T>(source, values);
    // }

    private List<Row<T>> createRows(List<T> sources) {

        List<Row<T>> list = new ArrayList<Row<T>>();

        if (sources != null) {
            for (T source : sources) {

                Row<T> row = new ViewableRow<T>(source, this.createRowValues(source));
                list.add(row);
            }
        }

        return list;
    }

    abstract protected String[] createRowValues(T source);

    public List<T> sources() {

        List<T> sources = new ArrayList<T>();
        for (Row<T> row : this.rows) {
            sources.add(row.source());
        }

        return sources;
    }

    public void add(T[] sourcesToAdd) {

        for (T source : sourcesToAdd) {

            Row<T> row = new ViewableRow<T>(source, this.createRowValues(source));
            if (!rows.contains(row)) {
                rows.add(row);
            }
        }

        refresh();
    }

    public void refresh() {
        this.rows = createRows(sources());
    }

    private void remove(T sourceToRemove) {

        for (Row<T> row : this.rows) {

            T source = row.source();
            if (source.equals(sourceToRemove)) {

                rows.remove(row);
                break;
            }
        }
    }

    public void remove(T[] sourcesToRemove) {

        for (T source : sourcesToRemove) {
            remove(source);
        }
    }

    protected String getValueWithDefault(String value) {
        return value == null || value.trim().length() == 0 ? DEFAULT_VALUE : value.trim();
    }

    protected String getNameWithDefault(EmfDataset dataset) {

        String name = DEFAULT_VALUE;
        if (dataset != null) {
            name = this.getValueWithDefault(dataset.getName());
        }

        return name;
    }

    protected String getValueWithDefault(Integer value) {

        String name = DEFAULT_VALUE;
        if (value != null) {
            name = this.getValueWithDefault(value.toString());
        }

        return name;
    }
}
