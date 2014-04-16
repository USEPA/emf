package gov.epa.emissions.commons.io.generic;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.OptimizedQuery;
import gov.epa.emissions.commons.io.DataFormatFactory;
import gov.epa.emissions.commons.io.ExportStatement;
import gov.epa.emissions.commons.io.Exporter;
import gov.epa.emissions.commons.io.ExporterException;
import gov.epa.emissions.commons.io.FileFormat;
import gov.epa.emissions.commons.io.importer.NonVersionedDataFormatFactory;

import java.io.File;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GenericExporterToString implements Exporter {

    private Dataset dataset;

    private Datasource datasource;

    protected String delimiter;

    private DataFormatFactory dataFormatFactory;

    protected FileFormat fileFormat;

    protected String inlineCommentChar;

    private int batchSize;

    protected int startColNumber = 2; // shifted by "obj_id","record_id" when write data

    protected long exportedLinesCount = 0;

    protected StringBuffer output;

    protected StringBuffer header;

    protected String lineFeeder = System.getProperty("line.separator");
    
    protected String rowFilters; 

    public GenericExporterToString(Dataset dataset, String rowFilters, DbServer dbServer, 
            Integer optimizedBatchSize) {
        this(dataset, rowFilters, dbServer, new NonVersionedDataFormatFactory(), optimizedBatchSize);
    }

    public GenericExporterToString(Dataset dataset, String rowFilters, DbServer dbServer,
            DataFormatFactory formatFactory, Integer optimizedBatchSize) {
        this.dataset = dataset;
        this.datasource = dbServer.getEmissionsDatasource();
        this.dataFormatFactory = formatFactory;
        this.fileFormat = new LineFileFormat(dbServer.getSqlDataTypes());
        this.batchSize = (optimizedBatchSize == null) ? 100000 : optimizedBatchSize.intValue();
        this.inlineCommentChar = dataset.getInlineCommentChar();
        this.output = new StringBuffer();
        this.rowFilters = rowFilters;
    }

    public void export(File file) throws ExporterException {
        try {
            write();
        } catch (Exception e) {
            e.printStackTrace();
            throw new ExporterException(e.getMessage());
        }
    }

    final protected void write() throws Exception {
        try {
            boolean inlinecomments = dataset.getInlineCommentSetting();

            if (inlinecomments) {
                writeDataWithComments(dataset, datasource);
            } else {
                writeDataWithoutComments(dataset, datasource);
            }
        } catch (SQLException e) {
            throw new ExporterException("could not export lines ", e);
        }
    }

    final protected void writeDataWithComments(Dataset dataset, Datasource datasource) throws Exception {
        writeData(dataset, datasource, true);
    }

    final protected void writeDataWithoutComments(Dataset dataset, Datasource datasource) throws Exception {
        writeData(dataset, datasource, false);
    }

    protected void writeData(Dataset dataset, Datasource datasource, boolean comments) throws Exception {
        String query = getQueryString(dataset, datasource);
        OptimizedQuery runner = datasource.optimizedQuery(query, batchSize);
        boolean firstbatch = true;
        String[] cols = null;

        try {
            while (runner.execute()) {
                ResultSet resultSet = runner.getResultSet();

                if (firstbatch) {
                    cols = getCols(resultSet);
                    this.startColNumber = startCol(cols);
                    firstbatch = false;
                }

                writeBatchOfData(resultSet, cols, comments);
                resultSet.close();
            }
        } catch (SQLException e) {
            throw new SQLException("Error in executing export query. Check the sort order in the dataset type.\n"
                    + e.getMessage());
        }

        runner.close();
    }

    protected String getQueryString(Dataset dataset, Datasource datasource) throws ExporterException {
        InternalSource source = dataset.getInternalSources()[0];
        if ("versions".equalsIgnoreCase(source.getTable().toLowerCase()) && "emissions".equalsIgnoreCase(datasource.getName().toLowerCase())) {
            System.err.println("Versions table moved to EMF. Error in " + this.getClass().getName());
        }
        String qualifiedTable = datasource.getName() + "." + source.getTable();
        ExportStatement export = dataFormatFactory.exportStatement();

        return export.generate(qualifiedTable, rowFilters);
    }

    protected void writeBatchOfData(ResultSet data, String[] cols, boolean comments) throws SQLException {
        if (comments) {
            while (data.next())
                writeRecordWithComment(cols, data);
        } else {
            while (data.next())
                writeRecordWithoutComment(data);
        }
    }

    protected void writeRecordWithComment(String[] cols, ResultSet data) throws SQLException {
        writeDataCols(data);
        String value = data.getString(cols.length);
        output.append((value == null ? "" : getComment(value)) + lineFeeder);
        ++exportedLinesCount;
    }

    protected void writeRecordWithoutComment(ResultSet data) throws SQLException {
        writeDataCols(data);
        output.append(lineFeeder);
        ++exportedLinesCount;
    }

    protected void writeDataCols(ResultSet data) throws SQLException {
        output.append(data.getString(startColNumber));
    }

    final protected String getComment(String value) {
        value = value.trim();
        if (value.equals(""))
            return value;

        if (!value.startsWith(inlineCommentChar))
            value = inlineCommentChar + value;

        return value;
    }

    private String[] getCols(ResultSet data) throws SQLException {
        List<String> cols = new ArrayList<String>();
        ResultSetMetaData md = data.getMetaData();
        int numCols = md.getColumnCount();
        for (int i = 1; i <= numCols; i++)
            cols.add(md.getColumnName(i));

        return cols.toArray(new String[0]);
    }

    protected int startCol(String[] cols) {
        if (isTableVersioned(cols))
            return 6; // shifted by "Obj_Id", "Record_Id",
        // "Dataset_Id", "Version", "Delete_Versions", "Line_Number"

        return 3; // shifted by "Obj_Id", "Record_Id", "Line_Number"
    }

    final protected boolean isTableVersioned(String[] cols) {
        return cols[2].equalsIgnoreCase("version") && cols[3].equalsIgnoreCase("delete_versions");
    }

    public long getExportedLinesCount() {
        return this.exportedLinesCount;
    }

    public String getOutputString() {
        return this.output.toString();
    }

}
