package gov.epa.emissions.commons.io.nif;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.CustomCharSetInputStreamReader;
import gov.epa.emissions.commons.io.FileFormat;
import gov.epa.emissions.commons.io.FormatUnit;
import gov.epa.emissions.commons.io.TableFormat;
import gov.epa.emissions.commons.io.importer.Comments;
import gov.epa.emissions.commons.io.importer.DataReader;
import gov.epa.emissions.commons.io.importer.DataTable;
import gov.epa.emissions.commons.io.importer.FileVerifier;
import gov.epa.emissions.commons.io.importer.FixedColumnsDataLoader;
import gov.epa.emissions.commons.io.importer.FixedWidthParser;
import gov.epa.emissions.commons.io.importer.ImporterException;
import gov.epa.emissions.commons.io.importer.Reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NIFImporter {

    private Dataset dataset;

    private NIFDatasetTypeUnits datasetTypeUnits;

    private Datasource datasource;

    private List tableNames;

    private FileVerifier fileVerifier;

    public NIFImporter(File[] files, Dataset dataset, NIFDatasetTypeUnits datasetTypeUnits, DbServer dbServer)
            throws ImporterException {
        this.dataset = dataset;
        this.datasetTypeUnits = datasetTypeUnits;
        this.datasource = dbServer.getEmissionsDatasource();
        this.tableNames = new ArrayList();
        this.fileVerifier = new FileVerifier();
        setup(files);
    }

    private void setup(File[] files) throws ImporterException {
        for (int i = 0; i < files.length; i++) {
            fileVerifier.shouldExist(files[i]);
        }
        datasetTypeUnits.process();
        updateInternalSources(datasetTypeUnits.formatUnits(), dataset);
    }

    public void run() throws ImporterException {
        validateTableNames(dataset.getInternalSources());
        FormatUnit[] units = datasetTypeUnits.formatUnits();
        for (int i = 0; i < units.length; i++) {
            doImport(dataset, units[i]);
        }
    }

    private void validateTableNames(InternalSource[] internalSources) throws ImporterException {
        List tables = new ArrayList();
        for (int i = 0; i < internalSources.length; i++) {
            String table = internalSources[i].getTable();
            try {
                if (datasource.tableDefinition().tableExists(table)) {
                    tables.add(table);
                }
            } catch (Exception e) {
                throw new ImporterException("Error in connecting to the database", e);
            }
        }
        if (!tables.isEmpty()) {
            String s = tables.size() > 1 ? "s " : "";
            String isOrAre = tables.size() > 1 ? " are" : " is";
            throw new ImporterException("The table name" + s + tables.toString() + isOrAre
                    + " already exist in the database");

        }
    }

    private void doImport(Dataset dataset, FormatUnit unit) throws ImporterException {
        InternalSource internalSource = unit.getInternalSource();
        if (internalSource == null) {
            return;
        }
        doImport(internalSource, unit, dataset);
    }

    private void doImport(InternalSource internalSource, FormatUnit unit, Dataset dataset) throws ImporterException {
        String tableName = internalSource.getTable();
        
        // VERSIONS TABLE - Completed - throws exception if the following case is true
        if ("emissions".equalsIgnoreCase(this.datasource.getName()) && "versions".equalsIgnoreCase(tableName.toLowerCase())) {
            throw new ImporterException("Table versions moved to schema emf."); // VERSIONS TABLE
        }
        
        String source = internalSource.getSource();
        DataTable dataTable = new DataTable(dataset, datasource);
        dataTable.create(tableName, unit.tableFormat());
        tableNames.add(tableName);
        try {
            doImport(source, dataset, tableName, unit.fileFormat(), unit.tableFormat());
        } catch (Exception e) {
            dropTables(tableNames);
            throw new ImporterException("Filename: " + source + ", " + e.getMessage());
        }
    }

    private void doImport(String fileName, Dataset dataset, String tableName, FileFormat fileFormat,
            TableFormat tableFormat) throws ImporterException, IOException {
        Reader fileReader = null;
        try {
            FixedColumnsDataLoader loader = new FixedColumnsDataLoader(datasource, tableFormat);
//            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            BufferedReader reader = new BufferedReader(new CustomCharSetInputStreamReader(new FileInputStream(fileName)));
            fileReader = new DataReader(reader, 0, new FixedWidthParser(fileFormat));
            loader.load(fileReader, dataset, tableName);
            loadDataset(fileReader.comments(), dataset);
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

    // TODO: load starttime, endtime
    private void loadDataset(List comments, Dataset dataset) {
        dataset.setDescription(new Comments(comments).all());
    }

    private void updateInternalSources(FormatUnit[] formatUnits, Dataset dataset) {
        List sources = new ArrayList();
        for (int i = 0; i < formatUnits.length; i++) {
            InternalSource source = formatUnits[i].getInternalSource();
            if (source != null) {
                sources.add(source);
                source.setCols(colNames(formatUnits[i].fileFormat().cols()));
            }
        }

        dataset.setInternalSources((InternalSource[]) sources.toArray(new InternalSource[0]));
    }

    // TODO: use HelpImporter
    private String[] colNames(Column[] cols) {
        List names = new ArrayList();
        for (int i = 0; i < cols.length; i++)
            names.add(cols[i].name());

        return (String[]) names.toArray(new String[0]);
    }

    private void dropTables(List tableNames) throws ImporterException {
        DataTable dataTable = new DataTable(dataset, datasource);
        for (int i = 0; i < tableNames.size(); i++) {
            dataTable.drop((String) tableNames.get(i));
        }
    }

    public InternalSource[] internalSources() {
        return dataset.getInternalSources();
    }

}
