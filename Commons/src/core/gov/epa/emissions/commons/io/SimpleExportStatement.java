package gov.epa.emissions.commons.io;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.version.Version;

public class SimpleExportStatement implements ExportStatement {

    public String generate(String table, String rowFilters) {
        if (rowFilters.trim().length()>0)
            return "SELECT * FROM " + table + " WHERE " + rowFilters;
        return "SELECT * FROM " + table;
    }

    public String generate(Datasource datasource, String table, String rowFilters, Dataset filterDataset, Version filterDatasetVersion,
            String filterDatasetJoinCondition) {
        // TODO Auto-generated method stub
        return null;
    }
}