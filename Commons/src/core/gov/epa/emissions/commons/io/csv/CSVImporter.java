package gov.epa.emissions.commons.io.csv;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.DatasetTypeUnit;
import gov.epa.emissions.commons.data.KeyVal;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.DataFormatFactory;
import gov.epa.emissions.commons.io.FileFormat;
import gov.epa.emissions.commons.io.FormatUnit;
import gov.epa.emissions.commons.io.TableFormat;
import gov.epa.emissions.commons.io.importer.Comments;
import gov.epa.emissions.commons.io.importer.DataTable;
import gov.epa.emissions.commons.io.importer.DatasetLoader;
import gov.epa.emissions.commons.io.importer.FileVerifier;
import gov.epa.emissions.commons.io.importer.FixedColumnsDataLoader;
import gov.epa.emissions.commons.io.importer.Importer;
import gov.epa.emissions.commons.io.importer.ImporterException;
import gov.epa.emissions.commons.io.importer.NonVersionedDataFormatFactory;
import gov.epa.emissions.commons.io.importer.Reader;
import gov.epa.emissions.commons.io.reference.CSVFileFormat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class CSVImporter implements Importer {
    
    private Dataset dataset;

    private Datasource datasource;

    private File file;

    private FormatUnit formatUnit;

    private CSVFileReader reader;

    private SqlDataTypes sqlDataTypes;

    public CSVImporter(File folder, String[] filenames, Dataset dataset, DbServer dbServer, SqlDataTypes sqlDataTypes)
            throws ImporterException {
        this(folder, filenames, dataset, dbServer, sqlDataTypes, new NonVersionedDataFormatFactory());
    }

    public CSVImporter(File folder, String[] filenames, Dataset dataset, DbServer dbServer, SqlDataTypes sqlDataTypes,
            DataFormatFactory dataFormatFactory) throws ImporterException {
        new FileVerifier().shouldHaveOneFile(filenames);
        this.file = new File(folder, filenames[0]);
        this.dataset = dataset;
        this.datasource = dbServer.getEmissionsDatasource();
        this.sqlDataTypes = sqlDataTypes;

        reader = new CSVFileReader(file, findInlineCommentDelimiter(), findSystemDefinedColNamesDelimiter());
        // TODO: get header from the reader
        // pass header to fileFormat
        FileFormat fileFormat = fileFormat(reader);
        TableFormat tableFormat = dataFormatFactory.tableFormat(fileFormat, sqlDataTypes);
        formatUnit = new DatasetTypeUnit(tableFormat, fileFormat);
    }

    private String[] findSystemDefinedColNamesDelimiter() {
        DatasetType datasetType = dataset.getDatasetType();
        KeyVal[] keyvalues = datasetType.getKeyVals();
        String[] names = null;
        KeyVal keyVal = findColNames(keyvalues);
        // if the column types keyword is set for the dataset type, then override file format 
        if (keyVal != null){
            //System.out.println("Find KeyVal:  " + keyVal.getName() + "  "+ keyVal.getValue());   
            // here types specified from the dataset itself will be overridden by the types in
            // the dataset type
            names = getColNames(keyVal.getValue());
        }
        return names;
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
                throw new ImporterException("could not create csv type data table. " + (e.getMessage() != null ? e.getMessage() : "Please" +
                		"check column names from the data file."));
                
            
            throw new ImporterException(e.getMessage());
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

    private void doImport(File file, Dataset dataset, String table, TableFormat tableFormat) throws Exception {
        FixedColumnsDataLoader loader = new FixedColumnsDataLoader(datasource, tableFormat);
        List comments = getComments(reader);
        loader.load(reader, dataset, table);
        loadDataset(file, table, formatUnit.tableFormat(), dataset, comments);
    }

    private void loadDataset(File file, String table, TableFormat tableFormat, Dataset dataset, List comments) {
        DatasetLoader loader = new DatasetLoader(dataset);
        loader.internalSource(file, table, tableFormat);
        if (comments != null)
            dataset.setDescription(new Comments(comments).all());
    }

    private List getComments(CSVFileReader reader) {
        return reader.getHeader();
    }

    private CSVFileFormat fileFormat(CSVFileReader reader) throws ImporterException {
        String[] types = reader.getColTypes();
        String[] names = null;
        
        DatasetType datasetType = dataset.getDatasetType();
        KeyVal[] keyvalues = datasetType.getKeyVals();
        KeyVal keyVal = findColTypes(keyvalues);
        // if the column types keyword is set for the dataset type, then override file format 
        if (keyVal != null){
            //System.out.println("Find KeyVal:  " + keyVal.getName() + "  "+ keyVal.getValue());   
            // here types specified from the dataset itself will be overridden by the types in
            // the dataset type
            types = getColTypes(keyVal.getValue());
        }
        keyVal = findColNames(keyvalues);
        // if the column types keyword is set for the dataset type, then override file format 
        if (keyVal != null){
            //System.out.println("Find KeyVal:  " + keyVal.getName() + "  "+ keyVal.getValue());   
            // here types specified from the dataset itself will be overridden by the types in
            // the dataset type
            names = getColNames(keyVal.getValue());
        }
        if (types!=null && types.length>0){
            //System.out.println("There are " + reader.getCols().length + " the third column is "+types[2].toString() );
            if(reader.getCols().length != types.length)
                throw new ImporterException("There are " + reader.getCols().length + " column names, but "+types.length + " column types. (Use | between the types, or export file using EXPORT_INLINE_COMMENT as false)");
           
            return new CSVFileFormat(sqlDataTypes, (names != null ? names : reader.getCols()), types);
        }
        return new CSVFileFormat(sqlDataTypes, (names != null ? names : reader.getCols()));
    }
    
    

    private KeyVal findColTypes(KeyVal[] keyValues){
        for (KeyVal keyVal : keyValues){
            if (keyVal.getName().equals("COLUMN_TYPES"))
                return keyVal; 
        }
        return null; 
    }

    private KeyVal findColNames(KeyVal[] keyValues){
        for (KeyVal keyVal : keyValues){
            if (keyVal.getName().equals("COLUMN_LABELS"))
                return keyVal; 
        }
        return null; 
    }

    private String findInlineCommentDelimiter(){
        if (dataset.getDatasetType() != null && dataset.getDatasetType().getKeyVals() != null) {
            for (KeyVal keyVal : dataset.getDatasetType().getKeyVals()){
                if (keyVal.getName().equals(Dataset.inline_comment_char))
                    return (keyVal.getValue() == null || keyVal.getValue().trim().isEmpty() ? null : keyVal.getValue()); 
            }
        }
        return null; 
    }

    private String[] getColTypes(String lineRead){
        List<String> columnTypes = new ArrayList<String>();

        StringTokenizer st = new StringTokenizer(lineRead, "|");

        while (st.hasMoreTokens())
            columnTypes.add(st.nextToken());

        return columnTypes.toArray(new String[0]);
    }

    private String[] getColNames(String lineRead){
        List<String> columnNames = new ArrayList<String>();

        StringTokenizer st = new StringTokenizer(lineRead, "|");

        while (st.hasMoreTokens())
            columnNames.add(st.nextToken());

        return columnNames.toArray(new String[0]);
    }

}
