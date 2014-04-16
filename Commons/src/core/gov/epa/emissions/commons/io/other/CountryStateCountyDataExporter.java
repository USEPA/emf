package gov.epa.emissions.commons.io.other;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.db.DataQuery;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.OptimizedQuery;
import gov.epa.emissions.commons.db.SqlDataTypes;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

public class CountryStateCountyDataExporter implements Exporter {
    private Dataset dataset;

    private Datasource datasource;

    private DataFormatFactory dataFormatFactory;

    private String delimiter;

    protected FileFormat fileFormat;

    protected SqlDataTypes types;

    private int batchSize;

    private long exportedLinesCount;

    private String inlineCommentChar;

    protected int startColNumber;

    private Datasource emfDatasource;
    
    protected String rowFilters;

    protected Dataset filterDataset;

    protected Version filterDatasetVersion;

    protected String filterDatasetJoinCondition;
    
    public CountryStateCountyDataExporter(Dataset dataset, String rowFilters, DbServer dbServer,
            Integer optimizedBatchSize) {
        setup(dataset, rowFilters, dbServer, new NonVersionedDataFormatFactory(), optimizedBatchSize, null, null, null);
    }

    public CountryStateCountyDataExporter(Dataset dataset, String rowFilters, DbServer dbServer,
            DataFormatFactory factory, Integer optimizedBatchSize, Dataset filterDataset, Version filterDatasetVersion, String filterDatasetJoinCondition) {
        setup(dataset, rowFilters, dbServer, factory, optimizedBatchSize, filterDataset, filterDatasetVersion, filterDatasetJoinCondition);
    }

    private void setup(Dataset dataset, String rowFilters, DbServer dbServer, DataFormatFactory dataFormatFactory,
            Integer optimizedBatchSize, Dataset filterDataset, Version filterDatasetVersion, String filterDatasetJoinCondition) {
        this.dataset = dataset;
        this.datasource = dbServer.getEmissionsDatasource();
        this.emfDatasource = dbServer.getEmfDatasource();
        this.dataFormatFactory = dataFormatFactory;
        this.types = dbServer.getSqlDataTypes();
        this.batchSize = optimizedBatchSize.intValue();
        this.inlineCommentChar = dataset.getInlineCommentChar();
        this.rowFilters = rowFilters; 
        this.filterDataset = filterDataset;
        this.filterDatasetVersion = filterDatasetVersion;
        this.filterDatasetJoinCondition = filterDatasetJoinCondition;
        setDelimiter("");
    }

    public void export(File file) throws ExporterException {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new CustomCharSetOutputStreamWriter(new FileOutputStream(file)));
            write(file, writer);
        } catch (IOException e) {
            throw new ExporterException("could not open file - " + file + " for writing");
        } catch (Exception e2) {
            e2.printStackTrace();
            throw new ExporterException(e2.getMessage() != null ? e2.getMessage()
                    : "Problem exporting country state county file");
        }
    }

    private void write(File file, PrintWriter writer) throws Exception {
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

    private void writeHeaders(PrintWriter writer, Dataset dataset) throws SQLException {
        String header = dataset.getDescription();

        if (header != null && !header.trim().isEmpty()) {
            StringTokenizer st = new StringTokenizer(header, "#");
            while (st.hasMoreTokens()) {
                writer.println("#" + st.nextToken());
            }
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

    protected void writeDataWithComments(PrintWriter writer, Dataset dataset, Datasource datasource) throws Exception {
        writeData(writer, dataset, datasource, true);
    }

    protected void writeDataWithoutComments(PrintWriter writer, Dataset dataset, Datasource datasource)
            throws Exception {
        writeData(writer, dataset, datasource, false);
    }

    protected void writeData(PrintWriter writer, Dataset dataset, Datasource datasource, boolean comments)
            throws Exception {
        InternalSource[] sources = dataset.getInternalSources();

        for (int i = 0; i < sources.length; i++) {
            String section = sources[i].getTable();
            writer.println("/" + section + "/");
            this.fileFormat = getFileFormat(sources[i].getTable());

            writeResultSet(writer, sources[i], datasource, comments, section);
        }
    }

    protected void writeResultSet(PrintWriter writer, InternalSource source, Datasource datasource, boolean comments,
            String section) throws Exception {
        String query = getQueryString(source, datasource);
        String orderby = "";

        if (section.toUpperCase().equals("COUNTRY"))
            orderby = " ORDER BY code, record_id";
        else if (section.toUpperCase().equals("STATE"))
            orderby = " ORDER BY countrycode, statecode, record_id";
        else if (section.toUpperCase().equals("COUNTY"))
            orderby = " ORDER BY countrycode, statecode, countycode, record_id";
        else
            orderby = " ORDER BY record_id";

        OptimizedQuery runner = datasource.optimizedQuery(query.substring(0, query.indexOf(" ORDER BY ")) + orderby, batchSize);
        boolean firstbatch = true;
        String[] cols = null;

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

        runner.close();
    }

    private void writeBatchOfData(PrintWriter writer, ResultSet data, String[] cols, boolean comments)
            throws SQLException {
        if (comments)
            writeWithComments(writer, data, cols);
        else
            writeWithoutComments(writer, data, cols);
    }

    private void writeWithComments(PrintWriter writer, ResultSet data, String[] cols) throws SQLException {
        while (data.next())
            writeRecordWithComment(cols, data, writer);
    }

    private void writeWithoutComments(PrintWriter writer, ResultSet data, String[] cols) throws SQLException {
        while (data.next())
            writeRecordWithoutComment(cols, data, writer);
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
            writer.write(formatValue(i, data));

            if (i + 1 < cols.length)
                writer.print(delimiter);// delimiter
        }
    }

    protected String formatValue(int index, ResultSet data) throws SQLException {
        Column column = fileFormat.cols()[index - startColNumber];
        return getFixedPositionValue(column, data);
    }

    protected String getDelimitedValue(Column column, ResultSet data) throws SQLException {
        String value = column.format(data).trim();

        if (column.sqlType().startsWith("FLOAT") || column.sqlType().startsWith("DOUBLE")) {
            value = "" + Float.parseFloat(value);
            if (value.endsWith(".0"))
                value = value.substring(0, value.lastIndexOf(".0"));
        }

        return value;
    }

    protected String getFixedPositionValue(Column column, ResultSet data) throws SQLException {
        String value = getDelimitedValue(column, data);
        String leadingSpace = "";
        int spaceCount = column.width() - value.length();

        for (int i = 0; i < spaceCount; i++)
            leadingSpace += " ";

        return leadingSpace + value;
    }

    protected String getComment(String value) {
        value = value.trim();
        if (value.equals(""))
            return value;

        if (!value.startsWith(inlineCommentChar))
            value = inlineCommentChar + value;

        return value;
    }

    protected FileFormat getFileFormat(String fileFormatName) {
        CountryStateCountyFileFormatFactory factory = new CountryStateCountyFileFormatFactory(types);

        return factory.get(fileFormatName);
    }

    private String getQueryString(InternalSource source, Datasource datasource) throws ExporterException {
        String table = source.getTable();
        if ("versions".equalsIgnoreCase(source.getTable().toLowerCase()) && "emissions".equalsIgnoreCase(datasource.getName().toLowerCase())) {
            System.err.println("Versions table moved to EMF. Error in " + this.getClass().getName());
        }
        String qualifiedTable = datasource.getName() + "." + table;
        ExportStatement export = dataFormatFactory.exportStatement();

        try {
            return export.generate(datasource, qualifiedTable, rowFilters, filterDataset, filterDatasetVersion, filterDatasetJoinCondition);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ExporterException(e.getMessage(), e);
        }
    }

    protected String[] getCols(ResultSet data) throws SQLException {
        List cols = new ArrayList();
        ResultSetMetaData md = data.getMetaData();
        for (int i = 1; i <= md.getColumnCount(); i++)
            cols.add(md.getColumnName(i));

        return (String[]) cols.toArray(new String[0]);
    }

    protected int startCol(String[] cols) {
        if (isTableVersioned(cols))
            return 5;

        return 2;
    }

    protected boolean isTableVersioned(String[] cols) {
        return cols[2].equalsIgnoreCase("version") && cols[3].equalsIgnoreCase("delete_versions");
    }

    public void setDelimiter(String del) {
        this.delimiter = del;
    }

    public long getExportedLinesCount() {
        return this.exportedLinesCount;
    }

}
