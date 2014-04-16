package gov.epa.emissions.commons.data;

import java.io.Serializable;

public class QAProgram implements Serializable, Comparable {

    private int id;

    private String name;
    
    private String runClassName;

    /*
     * Default constructor needed for hibernate and axis serialization
     */
    public QAProgram() {
        super();
    }

    public QAProgram(String name) {
        this.name = name;
    }
    
    public QAProgram(QAProgram program) {
        this.id = program.id;
        this.name = program.name;
        this.runClassName = program.runClassName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean equals(Object other) {
        if (other == null || !(other instanceof QAProgram))
            return false;

        return ((QAProgram) other).name.equals(this.name) || ((QAProgram)other).id == id;
    }

    public int hashCode() {
        return name.hashCode();
    }

    public String toString() {
        return getName();
    }

    public int compareTo(Object other) {
        return name.compareToIgnoreCase(((QAProgram) other).getName());
    }

    public String getRunClassName() {
        return runClassName;
    }

    public void setRunClassName(String runClassName) {
        this.runClassName = runClassName;
    }
}
