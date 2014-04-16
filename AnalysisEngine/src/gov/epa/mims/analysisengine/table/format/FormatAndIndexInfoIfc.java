package gov.epa.mims.analysisengine.table.format;

import java.text.Format;

public interface FormatAndIndexInfoIfc {

	/**
	 * Return the column index for the corresponding column name, not case sensitive
	 * 
	 * @param columnName
	 *            String
	 * @return int return the column index corresponds to the column name, if corresponding column name does not exist
	 *         then return -1
	 */
	public int getColumnNameIndex(String colName);

	/**
	 * get format of the column given the column name.
	 * 
	 * @param columnName
	 *            String
	 * @return Format
	 */
	public Format getFormat(String name);

	public Class getColumnClass(int index);

}
