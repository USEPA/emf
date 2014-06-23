package gov.epa.emissions.framework.services.cost;

import java.io.Serializable;

public class ControlMeasureProperty implements Serializable {

    private int id;

    private String name;

    private ControlMeasurePropertyCategory category;

    private String units;
    
    private String dataType;
    
    private String dbFieldName;
    
    private String value;
    
    public ControlMeasureProperty() {// persistence/bean
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setCategory(ControlMeasurePropertyCategory category) {
        this.category = category;
    }

    public ControlMeasurePropertyCategory getCategory() {
        return category;
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ControlMeasureProperty)) {
            return false;
        }

        ControlMeasureProperty other = (ControlMeasureProperty) obj;

        return (id == other.getId());
    }

    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public String getUnits() {
        return units;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDbFieldName(String dbFieldName) {
        this.dbFieldName = dbFieldName;
    }

    public String getDbFieldName() {
        return dbFieldName;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
