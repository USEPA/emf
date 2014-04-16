package gov.epa.emissions.framework.ui;

import java.util.List;

public interface TableData<T> {

    String[] columns();

    Class getColumnClass(int col);

    //list of Row objects
    List<Row<T>> rows();

    boolean isEditable(int col);

    Object element(int row);

    List<T> elements(int[] selected);

    void setValueAt(Object value, int row, int col);
    
}
