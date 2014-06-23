package gov.epa.emissions.framework.ui;

public interface Row<T> {

    Object getValueAt(int column);

    T source();

    void setValueAt(Object value, int column);

}