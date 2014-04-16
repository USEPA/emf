package gov.epa.emissions.commons.db.version;

import gov.epa.emissions.commons.db.Datasource;

import java.sql.SQLException;
import java.util.StringTokenizer;

import org.hibernate.Session;

public class DefaultVersionedRecordsFactory implements VersionedRecordsFactory {
    private Datasource datasource;

    private Versions versions;

    public DefaultVersionedRecordsFactory(Datasource datasource) {
        this.datasource = datasource;
        versions = new Versions();
    }

    public ScrollableVersionedRecords fetch(Version version, String table, Session session) throws SQLException {
        return fetch(version, table, null, null, null, session);
    }

    public ScrollableVersionedRecords optimizedFetch(Version version, String table, int batchSize, 
            int pageSize, Session session) throws SQLException {
        return optimizedFetch(version, table, batchSize, pageSize, null, null, null, session);
    }

    public ScrollableVersionedRecords optimizedFetch(Version version, String table, int batchSize, 
            int pageSize, String columnFilter,
           String rowFilter, String sortOrder, Session session) throws SQLException {
        String query = createQuery(version, table, columnFilter, rowFilter, sortOrder, session);
        String versions = versionsList(version, session);
        String fullyQualifiedTable = fullyQualifiedTable(table);
        String whereClause = whereClause(version, rowFilter, versions);

        return new OptimizedScrollableVersionedRecords(datasource, batchSize, pageSize, query,  fullyQualifiedTable, whereClause);
    }

    public ScrollableVersionedRecords fetch(Version version, String table, String columnFilter, String rowFilter,
            String sortOrder, Session session) throws SQLException {
        String query = createQuery(version, table, columnFilter, rowFilter, sortOrder, session);
        return new SimpleScrollableVersionedRecords(datasource, query);
    }

    VersionedRecord[] fetchAll(Version version, String table, Session session) throws SQLException {
        return fetchAll(version, table, null, null, null, session);
    }

    VersionedRecord[] fetchAll(Version version, String table, String columnFilter, String rowFilter, String sortOrder,
            Session session) throws SQLException {
        ScrollableVersionedRecords records = fetch(version, table, columnFilter, rowFilter, sortOrder, session);
        return records.range(0, records.total());
    }

    private String createQuery(Version version, String table, String columnFilter, String rowFilter, String sortOrder,
            Session session) {
        String versions = versionsList(version, session);

        String columnFilterClause = columnFilterClause(columnFilter);
        String rowFilterClause = rowFilterClause(version, rowFilter, versions);
        String sortOrderClause = sortOrderClause(sortOrder);

        String extraWhereClause = "";
        extraWhereClause = extraWhereClause + "";

        String query = "SELECT " + columnFilterClause + " FROM " + fullyQualifiedTable(table) + rowFilterClause
                + " ORDER BY " + sortOrderClause;
        return query;
    }

    private String fullyQualifiedTable(String table) {
        if ("versions".equalsIgnoreCase(table.toLowerCase()) && "emissions".equalsIgnoreCase(datasource.getName().toLowerCase())) {
            System.err.println("Versions table moved to EMF. Error in " + this.getClass().getName());
        }
        return datasource.getName() + "." + table;
    }

    private String sortOrderClause(String sortOrder) {
        final String defaultSortOrderClause = "record_id";
        String sortOrderClause = defaultSortOrderClause;
        if ((sortOrder != null) && (sortOrder.length() > 0)) {
            sortOrderClause = sortOrder + "," + defaultSortOrderClause;
        }

        return sortOrderClause;
    }

    private String columnFilterClause(String columnFilter) {
        final String defaultColumnFilterClause = "*";
        String columnFilterClause = defaultColumnFilterClause;
        if ((columnFilter != null) && (columnFilter.length() > 0)) {
            columnFilterClause = columnFilter + ", " + "record_id, dataset_id, version, delete_versions";
        }

        return columnFilterClause;
    }

    private String whereClause(Version version, String rowFilter, String versions) {
        return rowFilterClause(version, rowFilter, versions);
    }

    private String rowFilterClause(Version version, String rowFilter, String versions) {
        String deleteClause = createDeleteClause(versions);

        String defaultRowFilterClause = " WHERE dataset_id = " + version.getDatasetId() + " AND version IN ("
                + versions + ")" + deleteClause;
        String rowFilterClause = defaultRowFilterClause;
        if ((rowFilter != null) && (rowFilter.length() > 0)) {
            rowFilterClause = defaultRowFilterClause + " AND (" + rowFilter + ")";
        }
        return rowFilterClause;
    }

    private String createDeleteClause(String versions) {
        StringBuffer buffer = new StringBuffer();

        StringTokenizer tokenizer = new StringTokenizer(versions, ",");
        // e.g.: delete_version NOT SIMILAR TO '(6|6,%|%,6,%|%,6)'
        while (tokenizer.hasMoreTokens()) {
            String version = tokenizer.nextToken().trim();
            if (!version.equals("0")) {
                String regex = "(" + version + "|" + version + ",%|%," + version + ",%|%," + version + ")";
                if (buffer.length()==0)
                {
                    buffer.append(" AND ");
                }
                buffer.append(" delete_versions NOT SIMILAR TO '" + regex + "'");

                if (tokenizer.hasMoreTokens())
                    buffer.append(" AND ");
            }
        }
        return buffer.toString();
    }

    private String versionsList(Version finalVersion, Session session) {
        Version[] path = versions.getPath(finalVersion.getDatasetId(), finalVersion.getVersion(), session);

        StringBuffer result = new StringBuffer();
        for (int i = 0; i < path.length; i++) {
            result.append(path[i].getVersion());
            if ((i + 1) < path.length)
                result.append(",");
        }
        return result.toString();
    }

}
