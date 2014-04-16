package gov.epa.emissions.framework.ui;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.services.data.EmfDataset;

import java.util.Date;
import java.util.List;

import junit.framework.TestCase;

public class EmfDatasetTableDataTest extends TestCase {

    public void testShouldAppropriateColumnClassDefinedForAllColumns() {
        EmfDatasetTableData data = new EmfDatasetTableData(new EmfDataset[0]);

        assertEquals(String.class, data.getColumnClass(0));
        assertEquals(String.class, data.getColumnClass(1));
        assertEquals(String.class, data.getColumnClass(2));
        assertEquals(String.class, data.getColumnClass(3));
        assertEquals(String.class, data.getColumnClass(4));
        assertEquals(String.class, data.getColumnClass(5));
        assertEquals(String.class, data.getColumnClass(6));
        assertEquals(String.class, data.getColumnClass(7));
        assertEquals(String.class, data.getColumnClass(8));
    }

    public void testShouldFormatDates() {
        EmfDataset dataset = new EmfDataset();
        dataset.setName("name");
        dataset.setDatasetType(new DatasetType("type"));

        Date startDate = new Date();
        dataset.setStartDateTime(startDate);
        Date modifiedDate = new Date(startDate.getTime() + 50000);
        dataset.setModifiedDateTime(modifiedDate);

        EmfDataset[] datasets = { dataset };
        EmfDatasetTableData data = new EmfDatasetTableData(datasets);

        List rows = data.rows();
        assertEquals(1, rows.size());

        Row row = (Row) rows.get(0);
        assertEquals(CustomDateFormat.format_YYYY_MM_DD_HH_MM(modifiedDate), row.getValueAt(1));
        assertEquals(CustomDateFormat.format_YYYY_MM_DD_HH_MM(startDate), row.getValueAt(8));
        
    }

    public void testShouldFormatStartDateAsNAIfUnavailable() {
        EmfDataset dataset = new EmfDataset();
        dataset.setName("name");
        dataset.setDatasetType(new DatasetType("type"));
        dataset.setModifiedDateTime(new Date());
        
        EmfDataset[] datasets = { dataset };
        EmfDatasetTableData data = new EmfDatasetTableData(datasets);

        List rows = data.rows();
        assertEquals(1, rows.size());

        Row row = (Row) rows.get(0);
        assertEquals("N/A", row.getValueAt(8));
    }
}
