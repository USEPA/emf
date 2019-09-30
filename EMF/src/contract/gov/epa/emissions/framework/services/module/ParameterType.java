package gov.epa.emissions.framework.services.module;

import java.io.Serializable;

public class ParameterType implements Serializable {
    int id;
    String sqlType;
    boolean isTextType;

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getSqlType() {
        return sqlType;
    }
    public void setSqlType(String sqlType) {
        this.sqlType = sqlType;
    }
    public boolean getIsTextType() {
        return isTextType;
    }
    public void setIsTextType(boolean isTextType) {
        this.isTextType = isTextType;
    }
}
