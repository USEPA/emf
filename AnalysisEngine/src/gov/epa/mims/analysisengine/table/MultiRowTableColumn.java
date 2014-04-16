package gov.epa.mims.analysisengine.table;

import javax.swing.table.*;

public class MultiRowTableColumn extends TableColumn {
	protected String[] columnHeaders = null;

	public MultiRowTableColumn(int index, String[] headers) {
		super(index);
		columnHeaders = headers;
		headerRenderer = new MultiRowHeaderRenderer(headers);
	}

	public Object getHeaderValue() {
		return columnHeaders;
	}

	public Object getIdentifier() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < columnHeaders.length - 1; i++) {
			sb.append(columnHeaders[i]);
			sb.append('|');
		}
		sb.append(columnHeaders[columnHeaders.length - 1]);

		return sb.toString();
	}

	public TableCellRenderer getTableCellRenderer() {
		return headerRenderer;
	}
}
