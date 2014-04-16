package gov.epa.emissions.commons.io.ida;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.DataFormatFactory;
import gov.epa.emissions.commons.io.generic.GenericExporter;
import gov.epa.emissions.commons.io.importer.ImporterException;
import gov.epa.emissions.commons.io.importer.NonVersionedDataFormatFactory;

public class IDAActivityExporter extends GenericExporter {

    public IDAActivityExporter(Dataset dataset, String rowFilters, DbServer dbServer, Integer optimizedBatchSize)
            throws ImporterException {
        super(dataset, rowFilters, dbServer, fileFormat(dbServer.getSqlDataTypes(), new NonVersionedDataFormatFactory()), optimizedBatchSize);
        setup(dataset);
    }

    public IDAActivityExporter(Dataset dataset, String rowFilters, DbServer dbServer,
            DataFormatFactory dataFormatFactory, Integer optimizedBatchSize, Dataset filterDataset, Version filterDatasetVersion, String filterDatasetJoinCondition) throws ImporterException {
        super(dataset, rowFilters, dbServer, fileFormat(dbServer.getSqlDataTypes(), dataFormatFactory), dataFormatFactory, optimizedBatchSize, filterDataset, filterDatasetVersion, filterDatasetJoinCondition);
        setup(dataset);
    }

    private void setup(Dataset dataset) throws ImporterException {
        setDelimiter(" ");
        String[] comments = comments(dataset.getDescription());
        ((IDAFileFormat) fileFormat).addPollutantCols(pollutantCols(comments));
    }

    private String[] comments(String description) {
        return description.split("\n");
    }

    private String[] pollutantCols(String[] comments) throws ImporterException {
        IDAPollutantParser parser = new IDAPollutantParser();
        parser.processComments(comments);
        String[] pollutants = parser.pollutants();
        return pollutants;
    }

    private static IDAFileFormat fileFormat(SqlDataTypes sqlDatatypes, DataFormatFactory factory) {
        return new IDAActivityFileFormat(sqlDatatypes, factory.defaultValuesFiller());
    }

}
