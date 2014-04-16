package gov.epa.emissions.commons.data;

import java.io.Serializable;

public class QAStepTemplate implements Serializable{

    private long listIndex;

    private String name;

    private QAProgram program;

    private String programArguments;

    private boolean required;

    private float order;

    private String description;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public QAProgram getProgram() {
        return program;
    }

    public void setProgram(QAProgram program) {
        this.program = program;
    }

    public void setProgramArguments(String args) {
        this.programArguments = args;
    }

    public String getProgramArguments() {
        return programArguments;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean optional) {
        this.required = optional;
    }

    public float getOrder() {
        return order;
    }

    public void setOrder(float order) {
        this.order = order;
    }

    public long getListIndex() {
        return listIndex;
    }

    public void setListIndex(long listIndex) {
        this.listIndex = listIndex;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String toString() {
        return name;
    }

}
