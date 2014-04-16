package gov.epa.emissions.commons.io;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.version.Version;

public interface ExportStatement {

    public abstract String generate(String qualifiedTableName, String rowFilters);
    public abstract String generate(Datasource datasource, String table, String rowFilters, Dataset filterDataset, Version filterDatasetVersion, String filterDatasetJoinCondition) throws Exception;

}