package gov.epa.emissions.commons.io;

import gov.epa.emissions.commons.security.User;

import java.io.Serializable;
import java.util.Date;

public class XFileFormat implements FileFormat, Serializable, Comparable<XFileFormat> {
    private int id; // unique id needed for hibernate persistence
    
    private String name;

    private String description;
    
    private String delimiter;
    
    private boolean fixedFormat;
    
    private Column[] columns = new Column[]{};
    
    private Date lastModifiedDate;
    
    private Date dateAdded;
    
    private User creator;
    
    
    public Column[] cols() {
        return columns;
    }

    public String identify() {
        return name;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    public boolean isFixedFormat() {
        return fixedFormat;
    }

    public void setFixedFormat(boolean fixedFormat) {
        this.fixedFormat = fixedFormat;
    }

    public Column[] getColumns() {
        return columns;
    }

    public void setColumns(Column[] columns) {
        this.columns = columns;
    }

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public Date getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(Date dateAdded) {
        this.dateAdded = dateAdded;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }
    
    public String toString() {
        return name;
    }

    public int compareTo(XFileFormat other) {
        return name.compareToIgnoreCase(other.name);
    }

}
