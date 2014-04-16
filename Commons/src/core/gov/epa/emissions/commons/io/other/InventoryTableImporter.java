package gov.epa.emissions.commons.io.other;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.DatasetTypeUnit;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.CustomCharSetInputStreamReader;
import gov.epa.emissions.commons.io.DataFormatFactory;
import gov.epa.emissions.commons.io.FileFormat;
import gov.epa.emissions.commons.io.FormatUnit;
import gov.epa.emissions.commons.io.TableFormat;
import gov.epa.emissions.commons.io.importer.Comments;
import gov.epa.emissions.commons.io.importer.DataLoader;
import gov.epa.emissions.commons.io.importer.DataReader;
import gov.epa.emissions.commons.io.importer.DataTable;
import gov.epa.emissions.commons.io.importer.DatasetLoader;
import gov.epa.emissions.commons.io.importer.FileVerifier;
import gov.epa.emissions.commons.io.importer.FixedColumnsWithLineNoColDataLoader;
import gov.epa.emissions.commons.io.importer.FixedWidthParser;
import gov.epa.emissions.commons.io.importer.Importer;
import gov.epa.emissions.commons.io.importer.ImporterException;
import gov.epa.emissions.commons.io.importer.NonVersionedDataFormatFactory;
import gov.epa.emissions.commons.io.importer.Reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public class InventoryTableImporter implements Importer {
    private Datasource datasource;

    private File file;

    private FormatUnit formatUnit;

    private Dataset dataset;

    public InventoryTableImporter(File folder, String[] filenames, Dataset dataset, DbServer dbServer,
            SqlDataTypes sqlDataTypes) throws ImporterException {
        this(folder, filenames, dataset, dbServer, sqlDataTypes, new NonVersionedDataFormatFactory());
    }

    public InventoryTableImporter(File folder, String[] filenames, Dataset dataset, DbServer dbServer,
            SqlDataTypes sqlDataTypes, DataFormatFactory factory) throws ImporterException {
        new FileVerifier().shouldHaveOneFile(filenames);
        this.file = new File(folder, filenames[0]);
        this.dataset = dataset;
        this.datasource = dbServer.getEmissionsDatasource();

        FileFormat fileFormat = new InventoryTableFileFormat(sqlDataTypes, 1);
        TableFormat tableFormat = factory.tableFormatWithLineNoCol(fileFormat, sqlDataTypes, "Line_Number");
        formatUnit = new DatasetTypeUnit(tableFormat, fileFormat);
    }

    public void run() throws ImporterException {
        DataTable dataTable = new DataTable(dataset, datasource);
        String table = dataTable.name();

        try {
            if (!dataTable.exists(table))
                dataTable.create(formatUnit.tableFormat());
            doImport(file, dataset, table, formatUnit.tableFormat());
        } catch (Exception e) {
            e.printStackTrace();
            dataTable.drop();
            throw new ImporterException("could not import File - " + file.getAbsolutePath() + "; Details: "
                    +e.getMessage());
        }

    }

    private void doImport(File file, Dataset dataset, String table, TableFormat tableFormat) throws Exception {

        Reader fileReader = null;
        try {
            DataLoader loader = new FixedColumnsWithLineNoColDataLoader(datasource, tableFormat);
//            BufferedReader reader = new BufferedReader(new FileReader(file.getAbsolutePath()));
            BufferedReader reader = new BufferedReader(new CustomCharSetInputStreamReader(new FileInputStream(file.getAbsolutePath())));
            // FIXME: Due to irregularity in inventory data names (1st column in
            // input data file),
            // some extra chars may be extracted into 2nd column (CAS number).
            fileReader = new DataReader(reader, 0, new FixedWidthParser(formatUnit.fileFormat()));
            loader.load(fileReader, dataset, table);
            loadDataset(file, table, formatUnit.tableFormat(), dataset, fileReader.comments());
        } finally {
            close(fileReader);
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

    private void loadDataset(File file, String table, TableFormat tableFormat, Dataset dataset, List comments) {
        DatasetLoader loader = new DatasetLoader(dataset);
        loader.internalSource(file, table, tableFormat);
        dataset.setDescription(new Comments(comments).all());
    }
}
