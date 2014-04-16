package gov.epa.mims.analysisengine.table;

import gov.epa.mims.analysisengine.table.format.FormatAndIndexInfoIfc;
import gov.epa.mims.analysisengine.table.format.NullFormatter;

import java.text.Format;
import javax.swing.table.DefaultTableModel;

public class ColumnSelectionTableModel extends DefaultTableModel implements FormatAndIndexInfoIfc {

	ColumnSelectionTableModel(Object[][] data, String[] names) {
		super(data, names);
	}

	/**
	 * Return the column index for the corresponding column name, not case sensitive
	 * 
	 * @param columnName
	 *            String
	 * @return int return the column index corresponds to the column name, if corresponding column name does not exist
	 *         then return -1
	 */
	public int getColumnNameIndex(String colName) {
		if (columnIdentifiers.contains(colName))
			return columnIdentifiers.indexOf(colName);
		return -1;
	}

	public Format getFormat(String name) {
		return new NullFormatter();
	}

}
