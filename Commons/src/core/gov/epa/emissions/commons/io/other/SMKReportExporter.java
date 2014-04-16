package gov.epa.emissions.commons.io.other;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.db.DataQuery;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.OptimizedQuery;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.CustomCharSetOutputStreamWriter;
import gov.epa.emissions.commons.io.DataFormatFactory;
import gov.epa.emissions.commons.io.ExportStatement;
import gov.epa.emissions.commons.io.Exporter;
import gov.epa.emissions.commons.io.ExporterException;
import gov.epa.emissions.commons.io.VersionedQuery;
import gov.epa.emissions.commons.io.importer.NonVersionedDataFormatFactory;
import gov.epa.emissions.commons.util.CustomDateFormat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

public class SMKReportExporter implements Exporter {
    protected Dataset dataset;

    private Datasource datasource;

    private Datasource emfDatasource;

    private String delimiter;

    private String tableframe;
    
    private String rowFilters;

    private DataFormatFactory dataFormatFactory;

    private int batchSize;

    private long exportedLinesCount = 0;

    private String inlineCommentChar;

    protected int startColNumber;

    protected List<Integer> colTypes = new ArrayList<Integer>();

    protected List<String> colNames = new ArrayList<String>();

    protected Dataset filterDataset;

    protected Version filterDatasetVersion;

    protected String filterDatasetJoinCondition;
    
    public SMKReportExporter(Dataset dataset, String rowFilters, DbServer dbServer, Integer optimizedBatchSize) {
        setup(dataset, rowFilters, dbServer, new NonVersionedDataFormatFactory(), optimizedBatchSize, null, null, null);
    }

    public SMKReportExporter(Dataset dataset, String rowFilters, DbServer dbServer, DataFormatFactory factory,
            Integer optimizedBatchSize, Dataset filterDataset, Version filterDatasetVersion, String filterDatasetJoinCondition) {
        setup(dataset, rowFilters, dbServer, factory, optimizedBatchSize, filterDataset, filterDatasetVersion, filterDatasetJoinCondition);
    }

    private void setup(Dataset dataset, String rowFilters, DbServer dbServer, DataFormatFactory dataFormatFactory,
            Integer optimizedBatchSize, Dataset filterDataset, Version filterDatasetVersion, String filterDatasetJoinCondition) {
        this.dataset = dataset;
        this.datasource = dbServer.getEmissionsDatasource();
        this.emfDatasource = dbServer.getEmfDatasource();
        this.dataFormatFactory = dataFormatFactory;
        this.batchSize = optimizedBatchSize.intValue();
        this.inlineCommentChar = dataset.getInlineCommentChar();
        this.rowFilters = rowFilters;
        this.filterDataset = filterDataset;
        this.filterDatasetVersion = filterDatasetVersion;
        this.filterDatasetJoinCondition = filterDatasetJoinCondition;
        setDelimiter(";");
    }

    public void setInlineCommentChar(String inlineCommentChar) {
        this.inlineCommentChar = inlineCommentChar;
    }

    public void export(File file) throws ExporterException {
        PrintWriter writer = null;
        try {
            // writer = new PrintWriter(new BufferedWriter(new FileWriter(file)));
            writer = new PrintWriter(new CustomCharSetOutputStreamWriter(new FileOutputStream(file)));
            write(file, writer);
        } catch (Exception e) {
            throw new ExporterException(e.getMessage());
        }
    }

    protected void write(File file, PrintWriter writer) throws Exception {
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
            throw new ExporterException(e.getMessage());
        } finally {
            writer.close();
        }
    }

    protected void writeWithInlineComments(File file, PrintWriter writer) throws ExporterException {
        try {
            writeDataWithComments(writer, dataset, datasource);
        } catch (Exception e) {
            throw new ExporterException("could not export file - " + file, e);
        } finally {
            writer.close();
        }
    }

    protected void writeHeaders(PrintWriter writer, Dataset dataset) throws SQLException {
        String desc = dataset.getDescription();

        if (desc != null && !desc.trim().isEmpty()) {
            if (desc.lastIndexOf('#') + 2 == desc.length()) {
                StringTokenizer st = new StringTokenizer(desc, System.getProperty("line.separator"));
                while (st.hasMoreTokens()) {
                    tableframe = st.nextToken();
                }

                writer.print(desc.substring(0, desc.indexOf(tableframe)));
            } else
                writer.print(desc);
        }

        printExportInfo(writer);
    }

    private void printExportInfo(PrintWriter writer) throws SQLException {
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

    protected void writeDataWithComments(PrintWriter writer, Dataset dataset, Datasource datasource) throws Exception {
        writeData(writer, dataset, datasource, true);
    }

    protected void writeDataWithoutComments(PrintWriter writer, Dataset dataset, Datasource datasource)
            throws Exception {
        writeData(writer, dataset, datasource, false);
    }

    private void writeData(PrintWriter writer, Dataset dataset, Datasource datasource, boolean comments)
            throws Exception {
        int csvHeaderLine = dataset.getCSVHeaderLineSetting();
        String query = getQueryString(dataset, datasource);
        OptimizedQuery runner = datasource.optimizedQuery(query, batchSize);
        boolean firstbatch = true;
        String[] cols = null;

        int pad = 1;
//        if (!comments) {
//            pad = 1; // Add a comment column to header line
//        }

        while (runner.execute()) {
            ResultSet rs = runner.getResultSet();

            if (firstbatch) {
                getCols(rs);
                cols = this.colNames.toArray(new String[0]);
                this.startColNumber = startCol(cols);
                writeCols(writer, cols, pad, csvHeaderLine);
                firstbatch = false;
            }

            //writeCols(writer, cols, pad, csvHeaderLine);

            writeBatchOfData(writer, rs, cols, comments);
            //pad = 2;
            rs.close();
        }
        runner.close();
        // if (tableframe != null) //NOTE: don't need them on the exported data
        // writer.println(System.getProperty("line.separator") + tableframe);
    }

    private void writeBatchOfData(PrintWriter writer, ResultSet data, String[] cols, boolean comments)
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
        for (int i = startColNumber; i < cols.length; i++) {
            String value = data.getString(i);

            if (value != null)
                writer.write(formatValue(cols, colTypes.get(i - 1).intValue(), i, value)); // NOTE: SQL count column
                                                                                            // from 1, colTypes from 0

            if (i + 1 < cols.length)
                writer.print(delimiter);// delimiter
        }
    }

    private String getQueryString(Dataset dataset, Datasource datasource) throws Exception {
        InternalSource source = dataset.getInternalSources()[0];
        if ("versions".equalsIgnoreCase(source.getTable().toLowerCase()) && "emissions".equalsIgnoreCase(datasource.getName().toLowerCase())) {
            System.err.println("Versions table moved to EMF. Error in " + this.getClass().getName());
        }
        String qualifiedTable = datasource.getName() + "." + source.getTable();
        ExportStatement export = dataFormatFactory.exportStatement();

        return export.generate(datasource, qualifiedTable, rowFilters, filterDataset, filterDatasetVersion, filterDatasetJoinCondition);
//        return export.generate(qualifiedTable, rowFilters);
    }

    protected String formatValue(String[] cols, int colType, int index, String value) {
        if (cols[index - 1].toUpperCase().contains("DESCRIPTION"))
            return "\"" + value + "\"";

        if (cols[index - 1].equalsIgnoreCase("STATE"))
            return "\"" + value + "\"";

        if (cols[index - 1].equalsIgnoreCase("COUNTY"))
            return "\"" + value + "\"";

        if (containsDelimiter(value))
            return "\"" + value + "\"";

        if (colType == Types.DOUBLE || colType == Types.FLOAT)
            return new Double(Double.valueOf(value)).toString();

        return value;
    }

    protected String getComment(String value) {
        value = value.trim();
        if (value.equals(""))
            return value;

        if (inlineCommentChar != null && !value.startsWith(inlineCommentChar))
            value = inlineCommentChar + value;

        return " " + value;
    }

    private void getCols(ResultSet data) throws SQLException {
        ResultSetMetaData md = data.getMetaData();
        colNames.clear();
        colTypes.clear();

        for (int i = 1; i <= md.getColumnCount(); i++) {
            colNames.add(md.getColumnName(i));
            colTypes.add(md.getColumnType(i));
        }
    }

    public void setDelimiter(String del) {
        this.delimiter = del;
    }

    private boolean containsDelimiter(String s) {
        return s.indexOf(delimiter) >= 0;
    }

    private void writeCols(PrintWriter writer, String[] cols, int pad, int csvHeaderLine) {
        if (csvHeaderLine == Dataset.no_head_line)
            return; // Turn off head line

        int i = startCol(cols) - 1;
        
        for (; i < cols.length - pad; i++) {
            String temp = (csvHeaderLine == Dataset.upper_case) ? cols[i].toUpperCase().replaceAll("_", " ") : cols[i];
            writer.print(temp);
            
            if (i + 1 + pad < cols.length)
                writer.print(delimiter);// delimiter
        }

        writer.println();
    }

    protected int startCol(String[] cols) {
        if (isTableVersioned(cols))
            return 5;

        return 2;
    }

    protected boolean isTableVersioned(String[] cols) {
        return cols[2].equalsIgnoreCase("version") && cols[3].equalsIgnoreCase("delete_versions");
    }

    public long getExportedLinesCount() {
        return this.exportedLinesCount;
    }

}
