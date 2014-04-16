package gov.epa.emissions.commons.db;

import org.dbunit.dataset.ITable;

public interface TableReader {

    int count(String table);

    boolean exists(String schema, String table);

    int count(String schema, String table);

    ITable table(String schema, String table);

}