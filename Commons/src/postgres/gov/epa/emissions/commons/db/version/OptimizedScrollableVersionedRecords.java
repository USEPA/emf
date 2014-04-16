package gov.epa.emissions.commons.db.version;

import gov.epa.emissions.commons.CommonDebugLevel;
import gov.epa.emissions.commons.db.Datasource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class OptimizedScrollableVersionedRecords implements ScrollableVersionedRecords {

    private Datasource datasource;

    private String query;

    private ResultSet resultSet;

    private ResultSetMetaData metadata;

    private int totalCount;

    private Statement statement;

    private ScrollableResultSetIndex resultSetIndex;
    
    private int batchSize;

    public OptimizedScrollableVersionedRecords(Datasource datasource, int batchSize, int pageSize, String query, String table, String whereClause)
            throws SQLException {
        this.datasource = datasource;
        this.query = query;
        resultSetIndex = new ScrollableResultSetIndex(batchSize, pageSize);
        this.batchSize=batchSize+pageSize;

        obtainTotalCount(table, whereClause);
        executeQuery(resultSetIndex.start());
    }

    private void obtainTotalCount(String table, String whereClause) throws SQLException {
        Connection connection = datasource.getConnection();
        Statement statement = connection.createStatement();

        String countQuery = "SELECT count(*) FROM " + table + " " + whereClause;
        ResultSet resultSet = statement.executeQuery(countQuery);
        resultSet.next();
        totalCount = resultSet.getInt(1);

        resultSet.close();
    }

    public int total() {
        return totalCount;
    }

    /**
     * @return returns a range of records inclusive of start and end
     */
    public VersionedRecord[] range(int start, int end) throws SQLException {
        moveTo(start);// one position prior to start

        List range = new ArrayList();
        for (int i = start; (i <= end) && (i < totalCount); i++) {
            VersionedRecord record = next();
            if (record != null)
                range.add(record);
        }
        return (VersionedRecord[]) range.toArray(new VersionedRecord[0]);
    }

    private int columnCount() throws SQLException {
        return metadata.getColumnCount();
    }

    public void close() throws SQLException {
        resultSet.close();
        statement.close();
    }

    private void createStatement() throws SQLException {
        Connection connection = datasource.getConnection();
        statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        statement.setFetchSize(batchSize);
        statement.setMaxRows(batchSize);
    }

    private void executeQuery(int offset) throws SQLException {
        createStatement();

        String currentQuery = query + " LIMIT " + batchSize + " OFFSET " + offset;
        resultSet = statement.executeQuery(currentQuery);
        metadata = resultSet.getMetaData();
    }

    private void moveTo(int index) throws SQLException {
        if (!resultSetIndex.inRange(index)) {
            newResultSet(index);
        }

        int relativeIndex = resultSetIndex.relative(index);// relative index in new range

        if (relativeIndex == 0)
            resultSet.beforeFirst();
        else
            resultSet.absolute(relativeIndex);
    }

    private void newResultSet(int index) throws SQLException {
        close();
        executeQuery(resultSetIndex.newStart(index));
    }

    private VersionedRecord next() throws SQLException {
        if (!resultSet.next())
            return null;

        VersionedRecord record = new VersionedRecord();
        record.setRecordId(resultSet.getInt("record_id"));
        record.setDatasetId(resultSet.getInt("dataset_id"));
        record.setVersion(resultSet.getInt("version"));
        record.setDeleteVersions(resultSet.getString("delete_versions"));

        if ( CommonDebugLevel.DEBUG_PAGE) {
            System.out.println("---------\nOptimizedScrollableVersionedRecords.next()\n-------");
        }

        for (int i = 5; i <= columnCount(); i++) {
            Object obj = resultSet.getObject(i);

            if ( CommonDebugLevel.DEBUG_PAGE) {
                if (obj != null)
                    System.out.println(i + "> class: " + obj.getClass() + ", value: " + obj);
                else
                    System.out.println(i + "> null");
            }
            
            record.add(obj);
        }
        return record;
    }

}
