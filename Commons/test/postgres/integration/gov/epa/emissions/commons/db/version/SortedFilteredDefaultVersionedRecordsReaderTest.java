package gov.epa.emissions.commons.db.version;

import gov.epa.emissions.commons.db.DatabaseRecord;
import gov.epa.emissions.commons.db.Datasource;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

public class SortedFilteredDefaultVersionedRecordsReaderTest extends SortedFilteredVersionedRecordsTestCase {
    private DefaultVersionedRecordsFactory reader;

    int datasetId;

    protected void setUp() throws Exception {
        super.setUp();
        datasetId = (int) Math.random();

        setupVersionZero(datasource, versionsTable);
        setupVersionZeroData(datasource, dataTable);
        reader = new DefaultVersionedRecordsFactory(datasource);
    }

    private void setupVersionZero(Datasource datasource, String table) throws SQLException {
        addRecord(datasource, table, new String[] { null, "" + datasetId, "0", "Initial Version", "", "true" });
    }

    private void setupVersionZeroData(Datasource datasource, String table) throws SQLException {
        addRecord(datasource, table, new String[] { "1", "" + datasetId, "0", null, "A91", "W27", "D52", "X74", "L14" });
        addRecord(datasource, table, new String[] { "2", "" + datasetId, "0", null, "E11", "F99", "K23", "P87", "Y78" });
        addRecord(datasource, table, new String[] { "3", "" + datasetId, "0", null, "C17", "U45", "H19", "Z23", "B34" });
        addRecord(datasource, table, new String[] { "4", "" + datasetId, "0", null, "G19", "N97", "R87", "J22", "Q26" });
        addRecord(datasource, table, new String[] { "5", "" + datasetId, "0", null, "I61", "T18", "O16", "S15", "M19" });
        addRecord(datasource, table, new String[] { "6", "" + datasetId, "0", null, "T16", "X14", "V18", "Z17", "B12" });
        addRecord(datasource, table, new String[] { "7", "" + datasetId, "0", null, "A22", "W11", "D62", "X19", "L99" });
        addRecord(datasource, table, new String[] { "8", "" + datasetId, "0", null, "A11", "W55", "D11", "X23", "L47" });
        addRecord(datasource, table, new String[] { "9", "" + datasetId, "0", null, "G98", "N23", "R34", "J16", "Q19" });
        addRecord(datasource, table,
                new String[] { "10", "" + datasetId, "0", null, "G09", "N27", "R98", "J89", "Q01" });
    }

    public void testFetchVersionZeroWithNoFilters() throws Exception {
        Version versionZero = new Version();
        versionZero.setDatasetId(datasetId);
        versionZero.setVersion(0);

        VersionedRecord[] records = reader.fetchAll(versionZero, dataTable, null, null, null, session);

        assertEquals(10, records.length);

        for (int i = 0; i < records.length; i++) {
            DatabaseRecord rec = records[i];
            List all = rec.tokens();

            assertEquals(6, all.size());
            Iterator iter = all.listIterator();
            int k = 0;
            while (iter.hasNext()) {
                String tokin = (String) iter.next();
                if (i == 0) {
                    if (k == 0)
                        assertEquals("A91", tokin);
                    if (k == 1)
                        assertEquals("W27", tokin);
                    if (k == 2)
                        assertEquals("D52", tokin);
                    if (k == 3)
                        assertEquals("X74", tokin);
                    if (k == 4)
                        assertEquals("L14", tokin);
                }

                if (i == 1) {
                    if (k == 0)
                        assertEquals("E11", tokin);
                    if (k == 1)
                        assertEquals("F99", tokin);
                    if (k == 2)
                        assertEquals("K23", tokin);
                    if (k == 3)
                        assertEquals("P87", tokin);
                    if (k == 4)
                        assertEquals("Y78", tokin);
                }

                if (i == 2) {
                    if (k == 0)
                        assertEquals("C17", tokin);
                    if (k == 1)
                        assertEquals("U45", tokin);
                    if (k == 2)
                        assertEquals("H19", tokin);
                    if (k == 3)
                        assertEquals("Z23", tokin);
                    if (k == 4)
                        assertEquals("B34", tokin);
                }

                if (i == 3) {
                    if (k == 0)
                        assertEquals("G19", tokin);
                    if (k == 1)
                        assertEquals("N97", tokin);
                    if (k == 2)
                        assertEquals("R87", tokin);
                    if (k == 3)
                        assertEquals("J22", tokin);
                    if (k == 4)
                        assertEquals("Q26", tokin);
                }

                if (i == 4) {
                    if (k == 0)
                        assertEquals("I61", tokin);
                    if (k == 1)
                        assertEquals("T18", tokin);
                    if (k == 2)
                        assertEquals("O16", tokin);
                    if (k == 3)
                        assertEquals("S15", tokin);
                    if (k == 4)
                        assertEquals("M19", tokin);
                }

                if (i == 5) {
                    if (k == 0)
                        assertEquals("T16", tokin);
                    if (k == 1)
                        assertEquals("X14", tokin);
                    if (k == 2)
                        assertEquals("V18", tokin);
                    if (k == 3)
                        assertEquals("Z17", tokin);
                    if (k == 4)
                        assertEquals("B12", tokin);
                }

                k++;
            }
        }

    }

    public void testFetchVersionZeroWithOneRowFilter() throws Exception {
        Version versionZero = new Version();
        versionZero.setDatasetId(datasetId);
        versionZero.setVersion(0);

        VersionedRecord[] records = reader.fetchAll(versionZero, dataTable, null, "col_three = 'R87'", null, session);

        assertEquals(1, records.length);

        for (int i = 0; i < records.length; i++) {
            DatabaseRecord rec = records[i];
            List all = rec.tokens();

            assertEquals(6, all.size());
            Iterator iter = all.listIterator();
            int k = 0;
            while (iter.hasNext()) {
                String tokin = (String) iter.next();

                if (i == 0) {
                    if (k == 0)
                        assertEquals("G19", tokin);
                    if (k == 1)
                        assertEquals("N97", tokin);
                    if (k == 2)
                        assertEquals("R87", tokin);
                    if (k == 3)
                        assertEquals("J22", tokin);
                    if (k == 4)
                        assertEquals("Q26", tokin);
                }

                k++;
            }
        }

    }

    public void testFetchVersionZeroWithTwoRowFiltersDefaultSortOrder() throws Exception {
        Version versionZero = new Version();
        versionZero.setDatasetId(datasetId);
        versionZero.setVersion(0);

        VersionedRecord[] records = reader.fetchAll(versionZero, dataTable, null,
                "col_one = 'A91' OR col_three = 'R87'", null, session);

        assertEquals(2, records.length);

        for (int i = 0; i < records.length; i++) {
            DatabaseRecord rec = records[i];
            List all = rec.tokens();

            assertEquals(6, all.size());
            Iterator iter = all.listIterator();
            int k = 0;
            while (iter.hasNext()) {
                String tokin = (String) iter.next();

                if (i == 0) {
                    if (k == 0)
                        assertEquals("A91", tokin);
                    if (k == 1)
                        assertEquals("W27", tokin);
                    if (k == 2)
                        assertEquals("D52", tokin);
                    if (k == 3)
                        assertEquals("X74", tokin);
                    if (k == 4)
                        assertEquals("L14", tokin);
                }

                if (i == 1) {
                    if (k == 0)
                        assertEquals("G19", tokin);
                    if (k == 1)
                        assertEquals("N97", tokin);
                    if (k == 2)
                        assertEquals("R87", tokin);
                    if (k == 3)
                        assertEquals("J22", tokin);
                    if (k == 4)
                        assertEquals("Q26", tokin);
                }

                k++;
            }
        }

    }

    public void testFetchVersionZeroWithTwoRowFiltersOneSortOrder() throws Exception {
        Version versionZero = new Version();
        versionZero.setDatasetId(datasetId);
        versionZero.setVersion(0);

        VersionedRecord[] records = reader.fetchAll(versionZero, dataTable, null,
                "col_one = 'A91' OR col_three = 'R87'", "col_four", session);

        assertEquals(2, records.length);

        for (int i = 0; i < records.length; i++) {
            DatabaseRecord rec = records[i];
            List all = rec.tokens();

            assertEquals(6, all.size());
            Iterator iter = all.listIterator();
            int k = 0;
            while (iter.hasNext()) {
                String tokin = (String) iter.next();

                if (i == 0) {
                    if (k == 0)
                        assertEquals("G19", tokin);
                    if (k == 1)
                        assertEquals("N97", tokin);
                    if (k == 2)
                        assertEquals("R87", tokin);
                    if (k == 3)
                        assertEquals("J22", tokin);
                    if (k == 4)
                        assertEquals("Q26", tokin);
                }

                if (i == 1) {
                    if (k == 0)
                        assertEquals("A91", tokin);
                    if (k == 1)
                        assertEquals("W27", tokin);
                    if (k == 2)
                        assertEquals("D52", tokin);
                    if (k == 3)
                        assertEquals("X74", tokin);
                    if (k == 4)
                        assertEquals("L14", tokin);
                }

                k++;
            }
        }

    }

    public void testFetchVersionZeroWithThreeRowFiltersOneSortOrder() throws Exception {
        Version versionZero = new Version();
        versionZero.setDatasetId(datasetId);
        versionZero.setVersion(0);

        VersionedRecord[] records = reader.fetchAll(versionZero, dataTable, null,
                "col_one = 'A91' OR col_three = 'R87' OR col_five='B12'", "col_five", session);

        assertEquals(3, records.length);

        for (int i = 0; i < records.length; i++) {
            DatabaseRecord rec = records[i];
            List all = rec.tokens();

            assertEquals(6, all.size());
            Iterator iter = all.listIterator();
            int k = 0;
            while (iter.hasNext()) {
                String tokin = (String) iter.next();

                if (i == 0) {
                    if (k == 0)
                        assertEquals("T16", tokin);
                    if (k == 1)
                        assertEquals("X14", tokin);
                    if (k == 2)
                        assertEquals("V18", tokin);
                    if (k == 3)
                        assertEquals("Z17", tokin);
                    if (k == 4)
                        assertEquals("B12", tokin);
                }

                if (i == 1) {
                    if (k == 0)
                        assertEquals("A91", tokin);
                    if (k == 1)
                        assertEquals("W27", tokin);
                    if (k == 2)
                        assertEquals("D52", tokin);
                    if (k == 3)
                        assertEquals("X74", tokin);
                    if (k == 4)
                        assertEquals("L14", tokin);
                }

                if (i == 2) {
                    if (k == 0)
                        assertEquals("G19", tokin);
                    if (k == 1)
                        assertEquals("N97", tokin);
                    if (k == 2)
                        assertEquals("R87", tokin);
                    if (k == 3)
                        assertEquals("J22", tokin);
                    if (k == 4)
                        assertEquals("Q26", tokin);
                }
                k++;
            }
        }

    }

    public void testFetchVersionZeroWithFiveRowFiltersTwoSortOrders() throws Exception {
        Version versionZero = new Version();
        versionZero.setDatasetId(datasetId);
        versionZero.setVersion(0);

        VersionedRecord[] records = reader.fetchAll(versionZero, dataTable, null,
                "(col_one = 'A91' OR col_three = 'R87' OR col_five='B12' OR col_two='T18' OR col_two='F99')",
                "col_five,col_one", session);

        assertEquals(5, records.length);

        for (int i = 0; i < records.length; i++) {
            DatabaseRecord rec = records[i];
            List all = rec.tokens();

            assertEquals(6, all.size());
            Iterator iter = all.listIterator();
            int k = 0;
            while (iter.hasNext()) {
                String tokin = (String) iter.next();
                if (i == 0) {
                    if (k == 0)
                        assertEquals("T16", tokin);
                    if (k == 1)
                        assertEquals("X14", tokin);
                    if (k == 2)
                        assertEquals("V18", tokin);
                    if (k == 3)
                        assertEquals("Z17", tokin);
                    if (k == 4)
                        assertEquals("B12", tokin);
                }

                if (i == 1) {
                    if (k == 0)
                        assertEquals("A91", tokin);
                    if (k == 1)
                        assertEquals("W27", tokin);
                    if (k == 2)
                        assertEquals("D52", tokin);
                    if (k == 3)
                        assertEquals("X74", tokin);
                    if (k == 4)
                        assertEquals("L14", tokin);
                }

                if (i == 2) {
                    if (k == 0)
                        assertEquals("I61", tokin);
                    if (k == 1)
                        assertEquals("T18", tokin);
                    if (k == 2)
                        assertEquals("O16", tokin);
                    if (k == 3)
                        assertEquals("S15", tokin);
                    if (k == 4)
                        assertEquals("M19", tokin);
                }

                if (i == 3) {
                    if (k == 0)
                        assertEquals("G19", tokin);
                    if (k == 1)
                        assertEquals("N97", tokin);
                    if (k == 2)
                        assertEquals("R87", tokin);
                    if (k == 3)
                        assertEquals("J22", tokin);
                    if (k == 4)
                        assertEquals("Q26", tokin);
                }

                if (i == 4) {
                    if (k == 0)
                        assertEquals("E11", tokin);
                    if (k == 1)
                        assertEquals("F99", tokin);
                    if (k == 2)
                        assertEquals("K23", tokin);
                    if (k == 3)
                        assertEquals("P87", tokin);
                    if (k == 4)
                        assertEquals("Y78", tokin);
                }

                k++;
            }
        }

    }

}
