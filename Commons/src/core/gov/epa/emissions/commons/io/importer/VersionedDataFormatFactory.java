package gov.epa.emissions.commons.io.importer;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.DataFormatFactory;
import gov.epa.emissions.commons.io.ExportStatement;
import gov.epa.emissions.commons.io.ExporterException;
import gov.epa.emissions.commons.io.FileFormat;
import gov.epa.emissions.commons.io.TableFormat;
import gov.epa.emissions.commons.io.VersionedExportStatement;
import gov.epa.emissions.commons.io.temporal.VersionedTableFormat;

public class VersionedDataFormatFactory implements DataFormatFactory {

    private Version version;

    private Dataset dataset;

    public VersionedDataFormatFactory(Version version, Dataset dataset) {
        this.version = version;
        this.dataset = dataset;
    }

    public TableFormat tableFormat(FileFormat fileFormat, SqlDataTypes sqlDataTypes) {
        return new VersionedTableFormat(fileFormat, sqlDataTypes);
    }

    public TableFormat tableFormatWithLineNoCol(FileFormat fileFormat, SqlDataTypes sqlDataTypes, String lineNum) {
        return new VersionedTableFormat(fileFormat, sqlDataTypes, lineNum);
    }

    public FillDefaultValues defaultValuesFiller() {
        return new FillDefaultValuesOfVersionedRecord();
    }

    public ExportStatement exportStatement() throws ExporterException {
        if (version.getDatasetId() != dataset.getId())
            throw new ExporterException("Dataset doesn't match version (dataset id=" + dataset.getId()
                    + " but version shows dataset id=" + version.getDatasetId() + ")");
        
        return new VersionedExportStatement(version, dataset);
    }

    public Version getVersion() {
        return version;
    }

}
