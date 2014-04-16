package gov.epa.emissions.commons.io;

import java.util.StringTokenizer;

import gov.epa.emissions.commons.db.version.Version;

public class VersionedQuery {

    private Version version;

    private String alias = "";

    public VersionedQuery(Version version) {
        this.version = version;
    }

    public VersionedQuery(Version version, String alias) {
        this(version);
        this.alias = (alias != null && alias.trim().length() > 0 ? alias + "." : "");
    }

    public String query() {
        String versionsPath = version.createCompletePath();
        String deleteClause = createDeleteClause(versionsPath);

        // TBD: If dataset type does not have multiple datasets in a table, don't need datasetIdClause
        return alias + "version IN (" + versionsPath + ")" + deleteClause + " AND " + datasetIdClause();
    }

    public void query(String alias) {
    }

    private String datasetIdClause() {
        return alias + "dataset_id=" + version.getDatasetId();
    }

    private String createDeleteClause(String versions) {
        StringBuffer buffer = new StringBuffer();

        StringTokenizer tokenizer = new StringTokenizer(versions, ",");
        // e.g.: delete_version NOT SIMILAR TO '(6|6,%|%,6,%|%,6)'
        while (tokenizer.hasMoreTokens()) {
            String version = tokenizer.nextToken();
            if (!version.equals("0")) // don't need to check to see if items are deleted from version 0
            {
                String regex = "(" + version + "|" + version + ",%|%," + version + ",%|%," + version + ")";
                if (buffer.length() == 0) {
                    buffer.append(" AND ");
                }
                buffer.append(" " + alias + "delete_versions NOT SIMILAR TO '" + regex + "'");

                if (tokenizer.hasMoreTokens())
                    buffer.append(" AND ");
            }
        }

        return buffer.toString();
    }

    public String revisionHistoryQuery(String[] revisionsTableCols, String revisionsTable, String[] userCols,
            String usersTable) {
        
        if (version == null)
            return "";
        
        String versionsPath = version.createCompletePath();

        return "SELECT " + getSelectColsString(revisionsTableCols, "r", userCols, "u") + " FROM " + revisionsTable
                + " AS r, " + usersTable + " AS u WHERE u.id = r.creator AND dataset_id = " + version.getDatasetId()
                + " AND version in (" + versionsPath + ")";
    }

    private String getSelectColsString(String[] colsOne, String tableOneLabel, String[] colsTwo, String tableTwoLabel) {
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < colsOne.length; i++)
            sb.append(tableOneLabel + "." + colsOne[i] + ", ");

        for (int j = 0; j < colsTwo.length; j++)
            sb.append(tableTwoLabel + "." + colsTwo[j] + ", ");

        String clause = sb.toString();

        if (clause == null || clause.isEmpty())
            return "";

        return clause.substring(0, clause.lastIndexOf(','));
    }

}
