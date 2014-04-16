package gov.epa.emissions.commons.io.generic;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.db.DataQuery;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.OptimizedQuery;
import gov.epa.emissions.commons.db.TableMetaData;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.CustomCharSetOutputStreamWriter;
import gov.epa.emissions.commons.io.DataFormatFactory;
import gov.epa.emissions.commons.io.ExportStatement;
import gov.epa.emissions.commons.io.Exporter;
import gov.epa.emissions.commons.io.ExporterException;
import gov.epa.emissions.commons.io.FileFormat;
import gov.epa.emissions.commons.io.VersionedQuery;
import gov.epa.emissions.commons.io.importer.NonVersionedDataFormatFactory;
import gov.epa.emissions.commons.util.CustomDateFormat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

public class GenericExporter implements Exporter {

    private Dataset dataset;

    private Datasource datasource;

    private Datasource emfDatasource;

    protected String delimiter;

    private DataFormatFactory dataFormatFactory;

    protected FileFormat fileFormat;

    protected String inlineCommentChar;

    private int batchSize;

    protected int startColNumber = 2; // shifted by "obj_id","record_id" when write data

    protected long exportedLinesCount = 0;
    
    protected String rowFilters; 

    protected Dataset filterDataset;

    protected Version filterDatasetVersion;

    protected String filterDatasetJoinCondition;
    
    public GenericExporter(Dataset dataset, String rowFilters, DbServer dbServer, FileFormat fileFormat, Integer optimizedBatchSize) {
        this(dataset, rowFilters, dbServer, fileFormat, new NonVersionedDataFormatFactory(), optimizedBatchSize, null, null, null);
    }

    public GenericExporter(Dataset dataset, String rowFilters, DbServer dbServer, FileFormat fileFormat,
            DataFormatFactory dataFormatFactory, Integer optimizedBatchSize, Dataset filterDataset, Version filterDatasetVersion, String filterDatasetJoinCondition) {
        this.dataset = dataset;
        this.datasource = dbServer.getEmissionsDatasource();
        this.emfDatasource = dbServer.getEmfDatasource();
        this.dataFormatFactory = dataFormatFactory;
        this.fileFormat = fileFormat;
        this.batchSize = optimizedBatchSize.intValue();
        this.inlineCommentChar = dataset.getInlineCommentChar();
        this.rowFilters = rowFilters;
        this.filterDataset = filterDataset;
        this.filterDatasetVersion = filterDatasetVersion;
        this.filterDatasetJoinCondition = filterDatasetJoinCondition;
        setDelimiter(";");
    }

    public void export(File file) throws ExporterException {
        try {
            PrintWriter writer = new PrintWriter(new CustomCharSetOutputStreamWriter(new FileOutputStream(file)));
            write(file, writer);
        } catch (IOException e) {
            throw new ExporterException("could not open file - " + file + " for writing");
        } catch (Exception e2) {
            e2.printStackTrace();
            throw new ExporterException(e2.getMessage());
        }
    }

    final protected void write(File file, PrintWriter writer) throws Exception {
        try {
            boolean headercomments = dataset.getHeaderCommentsSetting();
            boolean inlinecomments = dataset.getInlineCommentSetting();

            if (headercomments && inlinecomments) {
                writeHeaders(writer, dataset);
                writeDataWithComments(writer, dataset, datasource);
            }

            if (headercomments && !inlinecomments) {
                writeHeaders(writer, dataset);
                writeDataWithoutComments(writer, dataset, datasource);
            }

            if (!headercomments && inlinecomments) {
                writeDataWithComments(writer, dataset, datasource);
            }

            if (!headercomments && !inlinecomments) {
                writeDataWithoutComments(writer, dataset, datasource);
            }
        } catch (SQLException e) {
            throw new ExporterException("could not export file - " + file, e);
        } finally {
            writer.close();
        }
    }

    protected void writeHeaders(PrintWriter writer, Dataset dataset) throws SQLException {
        String header = dataset.getDescription();
        String cr = System.getProperty("line.separator");

        if (header != null && !header.trim().isEmpty()) {
            StringTokenizer st = new StringTokenizer(header, "#");
            String lasttoken = "";
            while (st.hasMoreTokens()) {
                lasttoken = st.nextToken();
                writer.print("#" + lasttoken);
            }

            if (lasttoken.indexOf(cr) < 0)
                writer.print(cr);
        }

        printExportInfo(writer);
    }

    protected void printExportInfo(PrintWriter writer) throws SQLException {
        Version version = dataFormatFactory.getVersion();

        writer.println("#EXPORT_DATE=" + new Date().toString());
        writer.println("#EXPORT_VERSION_NAME=" + (version == null ? "None" : version.getName()));
        writer.println("#EXPORT_VERSION_NUMBER=" + (version == null ? "None" : version.getVersion()));

        writeRevisionHistories(writer, version);
    }

    private void writeRevisionHistories(PrintWriter writer, Version version) throws SQLException {
        VersionedQuery versionQuery = new VersionedQuery(version);
        DataQuery query = datasource.query();
        String revisionsTable = emfDatasource.getName() + ".revisions";
        String[] revisionsTableCols = { "version", "date_time", "what", "why" };
        String usersTable = emfDatasource.getName() + ".users";
        String[] userCols = { "name" };
        String revisionsHistoryQuery = versionQuery.revisionHistoryQuery(revisionsTableCols, revisionsTable, userCols,
                usersTable);

        if (revisionsHistoryQuery == null || revisionsHistoryQuery.isEmpty())
            return;

        ResultSet data = null;

        try {
            data = query.executeQuery(revisionsHistoryQuery);

            while (data.next())
                writer.println("#REV_HISTORY v" + data.getInt(1) + "("
                      + CustomDateFormat.format_MM_DD_YYYY(data.getDate(2)) + ") "
                      + replaceLineSeparator(data.getString(5)) + ".  "
                      + replaceLineSeparator(data.getString(3)) + " "
                      + replaceLineSeparator(data.getString(4)));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (data != null)
                data.close();
        }
    }

    private String replaceLineSeparator(String content) {
        if (content == null)
            return "";

        String ls = System.getProperty("line.separator");
        StringTokenizer st = new StringTokenizer(content, ls);
        StringBuffer sb = new StringBuffer();

        while (st.hasMoreTokens())
            sb.append(" " + st.nextToken());

        return sb.toString();
    }

    final protected void writeDataWithComments(PrintWriter writer, Dataset dataset, Datasource datasource)
            throws Exception {
        writeData(writer, dataset, datasource, true);
    }

    final protected void writeDataWithoutComments(PrintWriter writer, Dataset dataset, Datasource datasource)
            throws Exception {
        writeData(writer, dataset, datasource, false);
    }

    protected void writeData(PrintWriter writer, Dataset dataset, Datasource datasource, boolean comments)
            throws Exception {
        String query = getQueryString(dataset, rowFilters, datasource, this.filterDataset, this.filterDatasetVersion, this.filterDatasetJoinCondition);
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

                writeBatchOfData(writer, resultSet, cols, comments);
                resultSet.close();
            }
        } catch (SQLException e) {
            throw new SQLException("Error in executing export query. Check the sort order in the dataset type.\n"
                    + e.getMessage());
        }

        runner.close();
    }

    protected String getQueryString(Dataset dataset, String rowFilters, Datasource datasource) throws ExporterException {
        InternalSource source = dataset.getInternalSources()[0];
        if ("versions".equalsIgnoreCase(source.getTable().toLowerCase()) && "emissions".equalsIgnoreCase(datasource.getName().toLowerCase())) {
            System.err.println("Versions table moved to EMF. Error in " + this.getClass().getName());
        }
        String qualifiedTable = datasource.getName() + "." + source.getTable();
        ExportStatement export = dataFormatFactory.exportStatement();

        return export.generate(qualifiedTable, rowFilters);
    }

    protected String getQueryString(Dataset dataset, String rowFilters, Datasource datasource, Dataset filterDataset, Version filterDatasetVersion, String filterDatasetJoinCondition) throws ExporterException {
        InternalSource source = dataset.getInternalSources()[0];
        
        // VERSIONS TABLE - Completed - throws exception if the following case is true
        if ("emissions".equalsIgnoreCase(datasource.getName()) && "versions".equalsIgnoreCase(source.getTable().toLowerCase())) {
            throw new ExporterException("Table versions moved to schema emf."); // VERSIONS TABLE
        }
        
        if ("versions".equalsIgnoreCase(source.getTable().toLowerCase()) && "emissions".equalsIgnoreCase(datasource.getName().toLowerCase())) {
            System.err.println("Versions table moved to EMF. Error in " + this.getClass().getName());
        }
        
        String qualifiedTable = datasource.getName() + "." + source.getTable();
        ExportStatement export = dataFormatFactory.exportStatement();

        try {
            return export.generate(datasource, qualifiedTable, rowFilters, filterDataset, filterDatasetVersion, filterDatasetJoinCondition);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ExporterException(e.getMessage(), e);
        }
    }

    protected void writeBatchOfData(PrintWriter writer, ResultSet data, String[] cols, boolean comments)
            throws SQLException {
        if (comments)
            writeWithComments(writer, data, cols);
        else
            writeWithoutComments(writer, data, cols);
    }

    private void writeWithoutComments(PrintWriter writer, ResultSet data, String[] cols) throws SQLException {
        while (data.next())
            writeRecordWithoutComment(cols, data, writer);
    }

    private void writeWithComments(PrintWriter writer, ResultSet data, String[] cols) throws SQLException {
        while (data.next())
            writeRecordWithComment(cols, data, writer);
    }

    protected void writeRecordWithComment(String[] cols, ResultSet data, PrintWriter writer) throws SQLException {
        writeDataCols(cols, data, writer);
        String value = data.getString(cols.length);
        writer.write(value == null ? "" : getComment(value));

        writer.println();
        ++exportedLinesCount;
    }

    protected void writeRecordWithoutComment(String[] cols, ResultSet data, PrintWriter writer) throws SQLException {
        writeDataCols(cols, data, writer);
        writer.println();
        ++exportedLinesCount;
    }

    protected void writeDataCols(String[] cols, ResultSet data, PrintWriter writer) throws SQLException {
        int endCol = cols.length - 1;
        String toWrite = "";

        for (int i = startColNumber; i < endCol; i++)
            toWrite += formatValue(i, data) + delimiter;

        toWrite += formatValue(endCol, data); // the last column

        writer.write(toWrite);
    }

    protected String formatValue(int index, ResultSet data) throws SQLException {
        Column column = fileFormat.cols()[index - startColNumber];
        return (delimiter.equals("")) ? getFixedPositionValue(column, data) : getDelimitedValue(column, data);
    }

    final protected String getDelimitedValue(Column column, ResultSet data) throws SQLException {
        String colType = column.sqlType().toUpperCase();
        String val = data.getString(column.name());

        if (val == null || val.equals(""))
            return "";

        if ((colType.startsWith("VARCHAR") || colType.startsWith("TEXT")) && column.width() > 10)
            return "\"" + val + "\"";
        
        if (colType.startsWith("DOUBLE")) {
            String temp = new Double(data.getDouble(column.name())).toString();
            
            return temp.equals("0.0") ? "0" : temp;
        }

        return val;
    }

    final protected String getFixedPositionValue(Column column, ResultSet data) throws SQLException {
        return column.format(data);
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
        for (int i = 1; i <= md.getColumnCount(); i++)
            cols.add(md.getColumnName(i));

        return cols.toArray(new String[0]);
    }

    protected int startCol(String[] cols) {
        if (isTableVersioned(cols))
            return 5; // shifted by "Obj_Id", "Record_Id",
        // "Dataset_Id", "Version", "Delete_Versions"

        return 2; // shifted by "Obj_Id", "Record_Id"
    }

    protected void setExportedLines(String originalQuery, Statement statement) throws SQLException {
        //Date start = new Date();

        // Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
        // ResultSet.CONCUR_READ_ONLY);
        String fromClause = getSubString(originalQuery, "FROM", false);
        String queryCount = "SELECT COUNT(\"dataset_id\") " + getSubString(fromClause, "ORDER BY", true);
        ResultSet rs = statement.executeQuery(queryCount);
        rs.next();
        this.exportedLinesCount = rs.getLong(1);
        if ( exportedLinesCount==0 ){
            //log.error("Export failure: "+exportedLinesCount+ " lines are filtered" );
            throw new SQLException("ERROR: " + dataset.getName()+
                    " will not be exported because no records satisfied the filter " );
            
        }         
        //Date ended = new Date();
        //double lapsed = (ended.getTime() - start.getTime()) / 1000.00;

        //if (lapsed > 5.0)
            //log.warn("Time used to count exported data lines(second): " + lapsed);
    }
    
    protected String getSubString(String origionalString, String mark, boolean beforeMark) {
        int markIndex = origionalString.indexOf(mark);

        if (markIndex < 0)
            return origionalString;

        if (beforeMark)
            return origionalString.substring(0, markIndex);

        return origionalString.substring(markIndex);
    }
    final protected boolean isTableVersioned(String[] cols) {
        return cols[2].equalsIgnoreCase("version") && cols[3].equalsIgnoreCase("delete_versions");
    }

    public void setDelimiter(String del) {
        this.delimiter = del;
    }

    final protected String getDelimiter() {
        return delimiter;
    }

    public long getExportedLinesCount() {
        return this.exportedLinesCount;
    }

    protected Map<String,Column> getColumnMap(Dataset dataset) throws SQLException {
        return new TableMetaData(datasource).getColumnMap(dataset.getInternalSources()[0].getTable());
    }
    
}
