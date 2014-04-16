package gov.epa.emissions.commons.io.temporal;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.DatasetTypeUnit;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.CustomCharSetInputStreamReader;
import gov.epa.emissions.commons.io.DataFormatFactory;
import gov.epa.emissions.commons.io.FileFormat;
import gov.epa.emissions.commons.io.TableFormat;
import gov.epa.emissions.commons.io.importer.DataLoader;
import gov.epa.emissions.commons.io.importer.DataTable;
import gov.epa.emissions.commons.io.importer.DatasetLoader;
import gov.epa.emissions.commons.io.importer.FileVerifier;
import gov.epa.emissions.commons.io.importer.FixedColumnsDataLoader;
import gov.epa.emissions.commons.io.importer.FixedWidthPacketReader;
import gov.epa.emissions.commons.io.importer.Importer;
import gov.epa.emissions.commons.io.importer.ImporterException;
import gov.epa.emissions.commons.io.importer.NonVersionedDataFormatFactory;
import gov.epa.emissions.commons.io.importer.Reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class TemporalProfileImporter implements Importer {

    private SqlDataTypes sqlDataTypes;

    private Datasource datasource;

    private TemporalFileFormatFactory fileFormatFactory;

    private Dataset dataset;

    private File file;

    private DataFormatFactory dataFormatFactory;
    
    private int lineNumber;

    public TemporalProfileImporter(File folder, String[] filenames, Dataset dataset, DbServer dbServer,
            SqlDataTypes sqlDataTypes) throws ImporterException {
        this(folder, filenames, dataset, dbServer, sqlDataTypes, new NonVersionedDataFormatFactory());
    }

    public TemporalProfileImporter(File folder, String[] filePatterns, Dataset dataset, DbServer dbServer,
            SqlDataTypes sqlDataTypes, DataFormatFactory factory) throws ImporterException {
        new FileVerifier().shouldHaveOneFile(filePatterns);
        this.file = new File(folder, filePatterns[0]);

        this.datasource = dbServer.getEmissionsDatasource();
        this.sqlDataTypes = sqlDataTypes;
        this.dataset = dataset;

        fileFormatFactory = new TemporalFileFormatFactory(sqlDataTypes);
        dataFormatFactory = factory;
        this.lineNumber=0;
    }

    public void run() throws ImporterException {
        BufferedReader fileReader = null;
        try {
            //fileReader = new BufferedReader(new FileReader(file));
            fileReader = new BufferedReader(new CustomCharSetInputStreamReader(new FileInputStream(file)));

            while (!isEndOfFile(fileReader)) {
                String header = readHeader(fileReader);
                if (header == null)
                    return;
                FileFormat fileFormat = fileFormat(header);
                DatasetTypeUnit unit = new DatasetTypeUnit(tableFormat(fileFormat), fileFormat);
                doImport(fileReader, unit, header);
            }
        } catch (Exception e) {
            throw new ImporterException("could not import File - " + file.getAbsolutePath() + "; Details: "
                    + e.getMessage());
        } finally {
            close(fileReader);
        }
    }

    private void close(BufferedReader fileReader) throws ImporterException {
        if (fileReader != null) {
            try {
                fileReader.close();
            } catch (IOException e) {
                throw new ImporterException(e.getMessage());
            }
        }
    }

    private TableFormat tableFormat(FileFormat fileFormat) {
        return dataFormatFactory.tableFormat(fileFormat, sqlDataTypes);
    }

    private void doImport(BufferedReader fileReader, DatasetTypeUnit unit, String header) throws Exception {
        DataTable dataTable = new DataTable(dataset, datasource);
        try {
            String tableName = table(header);
            if (!dataTable.exists(tableName)) {
                dataTable.create(tableName, unit.tableFormat());
            }
            doImport(fileReader, dataset, unit, header);
        } catch (Exception e) {
            dataTable.drop(table(header));
            throw e;
        }
    }

    private void doImport(BufferedReader fileReader, Dataset dataset, DatasetTypeUnit unit, String header)
            throws Exception {
        Reader reader = new FixedWidthPacketReader(fileReader, header, unit.fileFormat(), lineNumber);
        DataLoader loader = new FixedColumnsDataLoader(datasource, unit.tableFormat());
        // Note: header is the same as table name
        loader.load(reader, dataset, table(header));
        loadDataset(file, table(header), unit.tableFormat(), dataset);
    }

    // TODO: revisit ?
    private String table(String header) {
        return header.replaceAll(" ", "_");
    }

    private boolean isEndOfFile(BufferedReader fileReader) throws IOException {
        return !fileReader.ready();
    }

    private FileFormat fileFormat(String header) throws ImporterException {
        FileFormat meta = fileFormatFactory.get(header);
        if (meta == null)
            throw new ImporterException("invalid header - " + header);

        return meta;
    }

    private String readHeader(BufferedReader fileReader) throws IOException, ImporterException {
        String line = null;
        while ((line = fileReader.readLine()) != null) {
            this.lineNumber++;
            line = line.trim();
            if (line.startsWith("/") && line.endsWith("/")) {
                return line.trim().replaceAll("/", "");
            }
        }

        if (!isEndOfFile(fileReader))
            throw new ImporterException("Expecting a temporal header tag");

        return null;
    }

    private void loadDataset(File file, String table, TableFormat tableFormat, Dataset dataset) {
        DatasetLoader loader = new DatasetLoader(dataset);
        loader.internalSource(file, table, tableFormat);
    }

}
