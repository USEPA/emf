package gov.epa.emissions.framework.services.basic;

import java.io.Serializable;

public class BasicSearchFilter implements Serializable {
    private String fieldName;
    private String fieldValue;

    public BasicSearchFilter() {
        //
    }

    public BasicSearchFilter(String fieldName,
                             String fieldValue) {
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    public String getFieldValue() {
        return fieldValue;
    }

    public void setFieldValue(String fieldValue) {
        this.fieldValue = fieldValue;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }
}
