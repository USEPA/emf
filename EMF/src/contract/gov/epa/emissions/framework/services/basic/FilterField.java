package gov.epa.emissions.framework.services.basic;

public class FilterField {
    private String associationPath;
    private String fieldValue;
    private Class fieldDataType;
    private boolean aggregrateField = false;

    public FilterField(String associationPath,
        Class fieldDataType) {
        this.associationPath = associationPath;
        this.fieldDataType = fieldDataType;
    }

    public FilterField(String associationPath,
                       Class fieldDataType,
                       boolean aggregrateField) {
        this(associationPath, fieldDataType);
        this.aggregrateField = aggregrateField;
    }

    public Class getFieldDataType() {
        return fieldDataType;
    }

    public void setFieldDataType(Class fieldDataType) {
        this.fieldDataType = fieldDataType;
    }

    public String getFieldValue() {
        return fieldValue;
    }

    public void setFieldValue(String fieldValue) {
        this.fieldValue = fieldValue;
    }

    public String getAssociationPath() {
        return associationPath;
    }

    public void setAssociationPath(String associationPath) {
        this.associationPath = associationPath;
    }

    public boolean isAggregrateField() {
        return aggregrateField;
    }

    public void setAggregrateField(boolean aggregrateField) {
        this.aggregrateField = aggregrateField;
    }
}
