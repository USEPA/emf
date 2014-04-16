package gov.epa.emissions.commons.io.generic;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.DatasetTypeUnit;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.DataFormatFactory;
import gov.epa.emissions.commons.io.FileFormat;
import gov.epa.emissions.commons.io.FormatUnit;
import gov.epa.emissions.commons.io.TableFormat;
import gov.epa.emissions.commons.io.importer.DataLoader;
import gov.epa.emissions.commons.io.importer.DataTable;
import gov.epa.emissions.commons.io.importer.DatasetLoader;
import gov.epa.emissions.commons.io.importer.FileVerifier;
import gov.epa.emissions.commons.io.importer.FixedColumnsDataLoader;
import gov.epa.emissions.commons.io.importer.Importer;
import gov.epa.emissions.commons.io.importer.ImporterException;
import gov.epa.emissions.commons.io.importer.NonVersionedDataFormatFactory;
import gov.epa.emissions.commons.io.importer.Reader;

import java.io.File;
import java.io.IOException;

public class LineImporter implements Importer {

    private Dataset dataset;

    private Datasource datasource;

    private File file;

    private FormatUnit formatUnit;

    public LineImporter(File folder, String[] filenames, Dataset dataset, DbServer dbServer, SqlDataTypes sqlDataTypes)
            throws ImporterException {
        create(folder, filenames, dataset, dbServer, sqlDataTypes, new NonVersionedDataFormatFactory());
    }

    public LineImporter(File folder, String[] filenames, Dataset dataset, DbServer dbServer, SqlDataTypes sqlDataTypes,
            DataFormatFactory factory) throws ImporterException {
        create(folder, filenames, dataset, dbServer, sqlDataTypes, factory);
    }

    private void create(File folder, String[] filenames, Dataset dataset, DbServer dbServer, SqlDataTypes types,
            DataFormatFactory factory) throws ImporterException {
        new FileVerifier().shouldHaveOneFile(filenames);
        this.file = new File(folder, filenames[0]);
        this.dataset = dataset;
        this.datasource = dbServer.getEmissionsDatasource();
        FileFormat fileFormat = new LineFileFormat(types);
        TableFormat tableFormat = factory.tableFormat(fileFormat, types);
        this.formatUnit = new DatasetTypeUnit(tableFormat, fileFormat);
    }

    public void run() throws ImporterException {
        DataTable dataTable = new DataTable(dataset, datasource);
        String table = null;
        
        try {
            table = dataTable.createConsolidatedTable(formatUnit.tableFormat());
            doImport(file, dataset, table, formatUnit.tableFormat());
            dataTable.updateConsolidatedTable(dataset.getDatasetType().getId(), table);
        } catch (Exception e) {
            e.printStackTrace();
            
            if (table == null || table.isEmpty())
                throw new ImporterException("could not create line-based type data table. " 
                        + (e.getMessage() != null ? e.getMessage() : ""));
            
            throw new ImporterException(e.getMessage());
        }
    }

    private void doImport(File file, Dataset dataset, String table, TableFormat tableFormat) throws Exception {
        Reader reader = null;
        try {
            DataLoader loader = new FixedColumnsDataLoader(datasource, tableFormat);
            reader = new LineReader(file);
            loader.load(reader, dataset, table);
            loadDataset(file, table, formatUnit.tableFormat(), dataset);
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

    private void loadDataset(File file, String table, TableFormat tableFormat, Dataset dataset) {
        DatasetLoader loader = new DatasetLoader(dataset);
        loader.internalSource(file, table, tableFormat);
    }
}
