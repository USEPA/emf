package gov.epa.emissions.commons.io.other;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.DatasetTypeUnit;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.DataFormatFactory;
import gov.epa.emissions.commons.io.FileFormatWithOptionalCols;
import gov.epa.emissions.commons.io.FormatUnit;
import gov.epa.emissions.commons.io.NonVersionedTableFormat;
import gov.epa.emissions.commons.io.TableFormat;
import gov.epa.emissions.commons.io.importer.Comments;
import gov.epa.emissions.commons.io.importer.DataTable;
import gov.epa.emissions.commons.io.importer.DatasetLoader;
import gov.epa.emissions.commons.io.importer.DelimiterIdentifyingFileReader;
import gov.epa.emissions.commons.io.importer.FileVerifier;
import gov.epa.emissions.commons.io.importer.Importer;
import gov.epa.emissions.commons.io.importer.ImporterException;
import gov.epa.emissions.commons.io.importer.OptionalColumnsDataLoader;
import gov.epa.emissions.commons.io.importer.Reader;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ProjectionPacketImporter implements Importer {
    private Datasource datasource;

    private File file;

    private FormatUnit formatUnit;

    private Dataset dataset;

    public ProjectionPacketImporter(File folder, String[] filenames, Dataset dataset, DbServer dbServer,
            SqlDataTypes sqlDataTypes) throws ImporterException {
        FileFormatWithOptionalCols fileFormat = new ProjectionPacketFileFormat(sqlDataTypes);
        TableFormat tableFormat = new NonVersionedTableFormat(fileFormat, sqlDataTypes);
        create(folder, filenames, dataset, dbServer, fileFormat, tableFormat);
    }

    public ProjectionPacketImporter(File folder, String[] filenames, Dataset dataset, DbServer dbServer,
            SqlDataTypes sqlDataTypes, DataFormatFactory factory) throws ImporterException {
        FileFormatWithOptionalCols fileFormat = new ProjectionPacketFileFormat(sqlDataTypes, factory
                .defaultValuesFiller());
        TableFormat tableFormat = factory.tableFormat(fileFormat, sqlDataTypes);
        create(folder, filenames, dataset, dbServer, fileFormat, tableFormat);
    }

    private void create(File folder, String[] filenames, Dataset dataset, DbServer dbServer,
            FileFormatWithOptionalCols fileFormat, TableFormat tableFormat) throws ImporterException {
        new FileVerifier().shouldHaveOneFile(filenames);
        this.file = new File(folder, filenames[0]);
        this.dataset = dataset;
        this.datasource = dbServer.getEmissionsDatasource();
        formatUnit = new DatasetTypeUnit(tableFormat, fileFormat);
    }

    public void run() throws ImporterException {
        DataTable dataTable = new DataTable(dataset, datasource);
        String table = dataTable.name();

        try {
            if (!dataTable.exists(table))
                dataTable.create(formatUnit.tableFormat());
            doImport(file, dataset, table, (FileFormatWithOptionalCols) formatUnit.fileFormat(), formatUnit
                    .tableFormat());
        } catch (Exception e) {
            throw new ImporterException("could not import File - " + file.getAbsolutePath() + "; Details: "
                    + e.getMessage());
        }
    }

    private void doImport(File file, Dataset dataset, String table, FileFormatWithOptionalCols fileFormat,
            TableFormat tableFormat) throws Exception {
        Reader reader = null;
        try {
            OptionalColumnsDataLoader loader = new OptionalColumnsDataLoader(datasource, fileFormat, tableFormat.key(), false);
            reader = new DelimiterIdentifyingFileReader(file, new String[] { "#", "/PROJECTION", "/END/" },
                    ((FileFormatWithOptionalCols) formatUnit.fileFormat()).minCols().length);

            loader.load(reader, dataset, table);
            loadDataset(file, table, formatUnit.tableFormat(), dataset, reader.comments());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close(reader);
        }
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

    private void loadDataset(File file, String table, TableFormat tableFormat, Dataset dataset, List comments) throws Exception {
        DatasetLoader loader = new DatasetLoader(dataset);
        loader.internalSource(file, table, tableFormat);
        String desc = new Comments(comments).all();
        
        if (desc != null && !desc.isEmpty())
            desc = desc.replace("/END/", "");
        
        dataset.setDescription(desc);
    }
}
