package gov.epa.emissions.commons.db;

import gov.epa.emissions.commons.Record;

public class DbRecord extends Record {

    private int id;

    public DbRecord() {
        this(-9);// only to be used by 'tools' (e.g hibernate)
    }

    public DbRecord(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
