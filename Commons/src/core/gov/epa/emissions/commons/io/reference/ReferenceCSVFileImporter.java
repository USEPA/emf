package gov.epa.emissions.commons.io.reference;

import gov.epa.emissions.commons.Record;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.OptimizedTableModifier;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.FileFormat;
import gov.epa.emissions.commons.io.csv.CSVFileReader;
import gov.epa.emissions.commons.io.importer.Importer;
import gov.epa.emissions.commons.io.importer.ImporterException;
import gov.epa.emissions.commons.io.importer.Reader;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReferenceCSVFileImporter implements Importer {

    private Datasource datasource;

    private File file;

    private SqlDataTypes sqlDataTypes;

    private String tableName;

    private CSVFileReader reader;

    private FileFormat fileFormat;

    public ReferenceCSVFileImporter(File file, String tableName, Datasource datasource, SqlDataTypes sqlDataTypes)
            throws ImporterException {
        this.file = file;
        this.tableName = tableName;
        this.datasource = datasource;
        this.sqlDataTypes = sqlDataTypes;
        this.file = file;

        //            reader = new DelimitedFileReader(file, new CommaDelimitedTokenizer());
        reader = new CSVFileReader(file);
        this.fileFormat = fileFormat(reader);
    }

    public void run() throws ImporterException {
        createTable(tableName, datasource, fileFormat);
        OptimizedTableModifier tableModifier = tableModifier();
        try {
            doImport(tableModifier);
        } catch (Exception e) {
            dropTable(tableName);
            e.printStackTrace();
            throw new ImporterException("Could not import file: " + file.getAbsolutePath() + "\n" + "Line No-"
                    + reader.lineNumber() + ", line-" + reader.line());
        } finally {
            close(reader, tableModifier);
        }
    }

    private OptimizedTableModifier tableModifier() throws ImporterException {
        try {
            return new OptimizedTableModifier(datasource, tableName); 
            // VERSIONS TABLE: completed - only used locally
        } catch (SQLException e) {
            throw new ImporterException(e.getMessage());
        }
    }

    private void close(Reader reader, OptimizedTableModifier tableModifier) throws ImporterException {
        try {
            if (reader != null)
                reader.close();
            if (tableModifier != null)
                tableModifier.close();
        } catch (Exception e) {
            throw new ImporterException(e.getMessage());
        }
    }

    private void dropTable(String tableName) throws ImporterException {
        try {
            datasource.tableDefinition().dropTable(tableName);
        } catch (Exception e) {
            throw new ImporterException("could not drop table '" + tableName + "'");
        }
    }

    private void doImport(OptimizedTableModifier tableModifier) throws Exception {
        tableModifier.start();
        Record record = reader.read();
        while (!record.isEnd()) {
            tableModifier.insert(data(record.getTokens(), fileFormat));
            record = reader.read();
        }
        tableModifier.finish();
    }

    private String[] data(String[] tokens, FileFormat fileFormat) throws ImporterException {
        int noEmptyTokens = fileFormat.cols().length - tokens.length;
        if (noEmptyTokens < 0) {
            throw new ImporterException("Number of columns in table '" + tableName
                    + "' is less than number of tokens\nLine number -" + reader.lineNumber() + ", Line-"
                    + reader.line());
        }
        List d = new ArrayList(Arrays.asList(tokens));
        for (int i = 0; i < noEmptyTokens; i++) {
            d.add("");
        }
        return (String[]) d.toArray(new String[0]);
    }

    // FIXME: use DataTable to create instead
    private void createTable(String tableName, Datasource datasource, FileFormat fileFormat) throws ImporterException {
        try {
            datasource.tableDefinition().createTable(tableName, fileFormat.cols());
        } catch (SQLException e) {
            throw new ImporterException("Could not create table -" + tableName + "\n" + e.getMessage());
        }
    }

//    private CSVFileFormat fileFormat(Reader reader) throws ImporterException {
//        try {
//            Record record = reader.read();
//            String[] tokens = record.getTokens();
//            return new CSVFileFormat(sqlDataTypes, tokens);
//        } catch (IOException e) {
//            throw new ImporterException(e.getMessage());
//        }
//    }

    private CSVFileFormat fileFormat(CSVFileReader reader) throws ImporterException {
        String[] types =reader.getColTypes();
        if (types!=null && types.length>0){
            if(reader.getCols().length != types.length)
                throw new ImporterException("There are " + reader.getCols().length + " column names, but "+types.length + " column types. ");
            return new CSVFileFormat(sqlDataTypes, reader.getCols(), types);
        }
        return new CSVFileFormat(sqlDataTypes, reader.getCols());
    }
}
