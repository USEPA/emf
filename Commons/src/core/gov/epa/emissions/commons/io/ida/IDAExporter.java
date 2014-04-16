package gov.epa.emissions.commons.io.ida;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.DataFormatFactory;
import gov.epa.emissions.commons.io.generic.GenericExporter;
import gov.epa.emissions.commons.io.importer.ImporterException;

import java.sql.ResultSet;
import java.sql.SQLException;

public class IDAExporter extends GenericExporter {

    private IDAFileFormat fileFormat;

    public IDAExporter(Dataset dataset, String rowFilters, DbServer dbServer, IDAFileFormat fileFormat, Integer optimizedBatchSize) throws ImporterException {
        super(dataset, rowFilters, dbServer, fileFormat, optimizedBatchSize);
        setup(fileFormat,dataset, "");
    }

    public IDAExporter(Dataset dataset, String rowFilters, DbServer dbServer, IDAFileFormat fileFormat, DataFormatFactory dataFormatFactory,
            Integer optimizedBatchSize, Dataset filterDataset, Version filterDatasetVersion, String filterDatasetJoinCondition) throws ImporterException {
        super(dataset, rowFilters, dbServer, fileFormat, dataFormatFactory, optimizedBatchSize, filterDataset, filterDatasetVersion, filterDatasetJoinCondition);
        setup(fileFormat,dataset, "");
    }

    private void setup(IDAFileFormat fileFormat, Dataset dataset, String delimiter) throws ImporterException {
        this.fileFormat = fileFormat;
        String[] comments = comments(dataset.getDescription());
        fileFormat.addPollutantCols(pollutantCols(comments));
        setDelimiter(delimiter);
    }
    
    public void setDelimiter(String delimiter){
        this.delimiter = delimiter;
    }

    private String[] pollutantCols(String[] comments) throws ImporterException {
        IDAPollutantParser parser = new IDAPollutantParser();
        parser.processComments(comments);
        String[] pollutants = parser.pollutants();
        return pollutants;
    }

    private String[] comments(String description) {
        return description.split("\n");
    }

    protected String formatValue(int index, ResultSet data) throws SQLException {
        Column column = fileFormat.cols()[index - startColNumber];
        return getFixedPositionValue(column, data);
    }

    // Due to two more columns added to dataset during import
    protected int startCol(String[] cols) {
        if (isTableVersioned(cols))
            return 7;

        return 4;
    }

}
