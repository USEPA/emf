package gov.epa.emissions.framework.ui;

import gov.epa.emissions.framework.services.EmfException;

public interface RowSource<T> {

    Object[] values();

    void setValueAt(int column, Object val);

    T source();

    void validate(int rowNumber) throws EmfException;

}
