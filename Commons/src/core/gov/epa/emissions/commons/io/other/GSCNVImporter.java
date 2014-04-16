package gov.epa.emissions.commons.io.other;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.DatasetTypeUnit;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.DataFormatFactory;
import gov.epa.emissions.commons.io.FileFormatWithOptionalCols;
import gov.epa.emissions.commons.io.TableFormat;
import gov.epa.emissions.commons.io.importer.Comments;
import gov.epa.emissions.commons.io.importer.DataTable;
import gov.epa.emissions.commons.io.importer.DatasetLoader;
import gov.epa.emissions.commons.io.importer.DelimiterIdentifyingFileReader;
import gov.epa.emissions.commons.io.importer.FileVerifier;
import gov.epa.emissions.commons.io.importer.Importer;
import gov.epa.emissions.commons.io.importer.ImporterException;
import gov.epa.emissions.commons.io.importer.NonVersionedDataFormatFactory;
import gov.epa.emissions.commons.io.importer.OptionalColumnsDataLoader;
import gov.epa.emissions.commons.io.importer.Reader;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class GSCNVImporter implements Importer {

    private File file;

    private Dataset dataset;

    private Datasource datasource;

    private DatasetTypeUnit formatUnit;

    public GSCNVImporter(File folder, String[] fileNames, Dataset dataset, DbServer dbServer, SqlDataTypes sqlDataTypes)
            throws ImporterException {
        this(folder, fileNames, dataset, dbServer, sqlDataTypes, new NonVersionedDataFormatFactory());
    }

    public GSCNVImporter(File folder, String[] filenames, Dataset dataset, DbServer dbServer,
            SqlDataTypes sqlDataTypes, DataFormatFactory factory) throws ImporterException {
        new FileVerifier().shouldHaveOneFile(filenames);
        this.file = new File(folder, filenames[0]);
        this.dataset = dataset;
        this.datasource = dbServer.getEmissionsDatasource();

        FileFormatWithOptionalCols fileFormat = new GSCNVFileFormat(sqlDataTypes,factory.defaultValuesFiller());
        TableFormat tableFormat = factory.tableFormat(fileFormat, sqlDataTypes);
        formatUnit = new DatasetTypeUnit(tableFormat, fileFormat);
    }

    public void run() throws ImporterException {
        DataTable dataTable = new DataTable(dataset, datasource);
        //String table = "GSCNV";
        String table = dataTable.name();

        try {
            if (!dataTable.exists(table))
                dataTable.create(table, formatUnit.tableFormat());
            doImport(file, dataset, table, (FileFormatWithOptionalCols) formatUnit.fileFormat(), formatUnit
                    .tableFormat());
        } catch (Exception e) {
            throw new ImporterException("Filename: " + file.getAbsolutePath() + ", " + e.getMessage());
        }

    }

    private void doImport(File file, Dataset dataset, String table, FileFormatWithOptionalCols fileFormat,
            TableFormat tableFormat) throws Exception {
        Reader reader = null;
        try {
            OptionalColumnsDataLoader loader = new OptionalColumnsDataLoader(datasource, fileFormat, tableFormat.key());
            reader = new DelimiterIdentifyingFileReader(file, fileFormat.minCols().length);
            loader.load(reader, dataset, table);

            loadDataset(reader.comments(), table, file, tableFormat, dataset);
        } finally {
            close(reader);
        }
    }

    private void loadDataset(List comments, String table, File file, TableFormat tableFormat, Dataset dataset) {
        dataset.setDescription(new Comments(comments).all());
        DatasetLoader loader = new DatasetLoader(dataset);
        loader.internalSource(file, table, tableFormat);
    }

    private void close(Reader reader) throws ImporterException {
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException e) {
                throw new ImporterException(e.getMessage());
            }
        }
    }

}
