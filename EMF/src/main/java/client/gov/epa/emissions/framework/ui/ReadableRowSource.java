package gov.epa.emissions.framework.ui;

public class ReadableRowSource implements RowSource {

    private Object[] values;

    public ReadableRowSource(Object[] values) {
        this.values = values;
    }

    public Object[] values() {
        return values;
    }

    public void setValueAt(int column, Object val) {
        // No Op - read only
    }

    public Object source() {
        return values;
    }

    public void validate(int rowNumber) {
        // FIXME: add validate code before save
    }

}
