package gov.epa.emissions.framework.services.module;

import java.io.Serializable;

public class HistoryInternalParameter implements Serializable {

    private int id;

    private History history;

    private String parameterPath; // slash delimited list of submodule ids (at least one) and the parameter name

    private String parameterPathNames; // slash delimited list of submodule names (at least one) and the parameter name

    private String value;
    
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

    public String getParameterPath() {
        return parameterPath;
    }

    public void setParameterPath(String parameterPath) {
        this.parameterPath = parameterPath;
    }

    public String getParameterPathNames() {
        return parameterPathNames;
    }

    public void setParameterPathNames(String parameterPathNames) {
        this.parameterPathNames = parameterPathNames;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
