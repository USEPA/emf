package gov.epa.emissions.commons.data;

import java.io.Serializable;

public class ProjectionShapeFile implements Serializable {

    private int id;

    private String name;

    private String description;

    private String tableSchema;

    private String tableName;

    private String prjText;

    private int srid;

    private String type;

    public ProjectionShapeFile() {// needed as it's a Java bean
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int hashCode() {
        return name.hashCode();
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setTableSchema(String tableSchema) {
        this.tableSchema = tableSchema;
    }

    public String getTableSchema() {
        return tableSchema;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setPrjText(String prjText) {
        this.prjText = prjText;
    }

    public String getPrjText() {
        return prjText;
    }

    public int getSrid() {
        return srid;
    }

    public void setSrid(int srid) {
        this.srid = srid;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public String toString() {
        return name + (description != null && !description.isEmpty() ? " [" + description + "]" : "");
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ProjectionShapeFile))
            return false;

        ProjectionShapeFile step = (ProjectionShapeFile) obj;
        if (id == step.id
                || (name.equals(step.getName())))
            return true;

        return false;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}