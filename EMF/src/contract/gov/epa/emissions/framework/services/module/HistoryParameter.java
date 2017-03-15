package gov.epa.emissions.framework.services.module;

import java.io.Serializable;

public class HistoryParameter implements Serializable {

    private int id;

    private History history;

    private String parameterName;

    private String value;
    
    public HistoryParameter() {
    }
    
    public HistoryParameter(History history, String parameterName, String value) {
        this.history = history;
        this.parameterName = parameterName;
        this.value = value;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public History getHistory() {
        return history;
    }

    public void setHistory(History history) {
        this.history = history;
    }

    public String getParameterName() {
        return parameterName;
    }

    public void setParameterName(String parameterName) {
        this.parameterName = parameterName;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    // could return null if the module type for this module was changed after this history record was created
    public ModuleTypeVersionParameter getModuleTypeVersionParameter() {
        return history.getModule().getModuleTypeVersion().getModuleTypeVersionParameters().get(parameterName);
    }
}
