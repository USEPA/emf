package gov.epa.mims.analysisengine.table;

import gov.epa.mims.analysisengine.table.format.FormattedCellRenderer;

import java.awt.Component;
import java.awt.FontMetrics;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public class RowHeaderTable extends JTable {

	public final static int DEFAULT_WIDTH = 50;

	/** The data model for this table. */
	protected MultiRowHeaderTableModel underlyingModel = null;

	public final static int DEFAULT_PADDING = 15;

	public final static int MAX_ROW_CHECK_FOR_WIDTH = 15;

	public RowHeaderTable(MultiRowHeaderTableModel model) {
		super(model);
		underlyingModel = model;
		setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		setUpColumnHeaders();
		setUpDataRenderers();
		setColumnWidths();

		setCellSelectionEnabled(true);
		new ClipBoardCopy(this).registerCopyKeyStroke();
	} // RowHeaderTable()
	
	public void setFormat(MultiRowHeaderTableModel model){
		underlyingModel = model;
		
		setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		setUpDataRenderers();
		setColumnWidths();

		setCellSelectionEnabled(true);
		//new ClipBoardCopy(this).registerCopyKeyStroke();	
	}

	/**
	 * We needed to override this for the case when we change the number of columns in the table. It vaporizes the
	 * columns, so we were losing our custom renderers. Overiding this method restores them.
	 */
	public void createDefaultColumnsFromModel() {
		// We require that the model be a MultiRowHeaderTableModel.
		MultiRowHeaderTableModel model = (MultiRowHeaderTableModel) getModel();
		if (model != null) {
			// Remove any current columns
			TableColumnModel cm = getColumnModel();
			while (cm.getColumnCount() > 0) {
				cm.removeColumn(cm.getColumn(0));
			}
			TableColumn newColumn = new MultiRowTableColumn(0, new String[] { "Row" });
			addColumn(newColumn);
			// Create new columns from the data model info.
			// The model contains one extra column for the row labels, so only
			// loop up to columnCount - 1.
			for (int i = 0; i < model.getColumnCount() - 1; i++) {
				// Set the index to be i+1 because we need to skip the row number
				// column.
				newColumn = new MultiRowTableColumn(i + 1, model.getColumnHeaders(i));
				addColumn(newColumn);
			}
			setUpColumnHeaders();
			setUpDataRenderers();
			setColumnWidths();
		}
	}

	/**
	 * Based on the width of the column name and the value in the first row, set the column width. This also fills the
	 * Format[] array.
	 */
	private void setColumnWidths() {
		TableColumnModel columnModel = getColumnModel();
		TableColumn column = columnModel.getColumn(0);
		// We need to handle the row header column specially because we know
		// that it's widest value will be at the last row. Also, the renderer does
		// not use a formatter.
		// Get the column renderer.
		TableCellRenderer rend = column.getCellRenderer();
		if (rend != null) {
			Component rendComp = rend.getTableCellRendererComponent(this, getValueAt(1, 0), false, false, 1, 0);
			FontMetrics fm = rendComp.getFontMetrics(rendComp.getFont());
			// Get the width of the header and add a 2 spaces on each side.
			int valueWidth = fm.stringWidth(getValueAt(getRowCount() - 1, 0).toString()) + 4 * fm.charWidth('O');
			// System.out.println("valueWidth="+ valueWidth);
			// Find the larger of the two widths (header or column value)
			int newWidth = valueWidth + DEFAULT_PADDING;
			// System.out.println("column.getWidth()="+ column.getWidth());
			// If the current column width is smaller than the new width, set
			// the column width to the new width.
			if (newWidth > column.getWidth()) {
				column.setPreferredWidth(newWidth);
			}
		} // if (rend != null)

		// formats = new Format[columnModel.getColumnCount()];
		// formats[0] = FormattedCellRenderer.nullFormatter;
		// Now loop through the rest of the columns and set the column width
		// to be the wider of either the column name or the value in the first row.
		for (int c = 1; c < columnModel.getColumnCount(); c++) {
			// Get the column header renderer.
			column = columnModel.getColumn(c);
			rend = column.getHeaderRenderer();

			int valueWidth = 0;
			int headerWidth = 0;
			if (rend != null) {
				Component rendComp = rend.getTableCellRendererComponent(this, "", false, false, 0, c);
				FontMetrics fm = rendComp.getFontMetrics(rendComp.getFont());
				// Get the width of the header and add a space on each side.
				valueWidth = 2 * fm.charWidth(' ');
				headerWidth = 2 * fm.charWidth(' ');
				// Get the width of the header.
				headerWidth += fm.stringWidth(getColumnName(c));
				// System.out.println("headerWidth="+ headerWidth);
				// Get the column renderer.
				rend = column.getCellRenderer();
				if (rend != null) {
					// Only check the cell contents if we have at least one row.
					int rowCount = getRowCount();
					rowCount = (rowCount > MAX_ROW_CHECK_FOR_WIDTH) ? MAX_ROW_CHECK_FOR_WIDTH : rowCount;
					if (rowCount > 0) {
						int maxWidth = Integer.MIN_VALUE;
						int width = 0;
						for (int i = 0; i < rowCount; i++) {
							rendComp = rend.getTableCellRendererComponent(this, getValueAt(i, c), false, false, i, c);
							fm = rendComp.getFontMetrics(rendComp.getFont());

							Object obj = getValueAt(i, c);

							if (obj == null) {
								width = DEFAULT_WIDTH;
							} else {
								width = fm.stringWidth(obj.toString());
							}
							if (maxWidth < width) {
								maxWidth = width;
							}
						}// for(rowCount_)
						valueWidth = maxWidth;
					}// if (getRowCount() > 0)
				} // if (rend2 != null)
				// System.out.println("valueWidth="+ valueWidth);
				// Find the larger of the two widths (header or column value)
				int newWidth = headerWidth;
				if (valueWidth > headerWidth) {
					newWidth = valueWidth;
				} else {
					newWidth = headerWidth;
				}
				newWidth += DEFAULT_PADDING;
				// If the current column width is smaller than the new width, set
				// the column width to the new width.
				if (newWidth > column.getWidth()) {
					column.setPreferredWidth(newWidth);
				}
			}
		}
	}

	private void setUpFormatRenderers() {
		TableColumnModel columnModel = getColumnModel();
		TableColumn column = columnModel.getColumn(0);
		for (int c = 1; c < columnModel.getColumnCount(); c++) {
			column = columnModel.getColumn(c);
			Class columnClass = getColumnClass(c);
			if (columnClass.equals(Boolean.class)) {
				// TODO: 2011-02-14
				//column.
			} else {
				column.setCellRenderer(FormattedCellRenderer.getDefaultFormattedCellRenderer(columnClass));
			}
		}
	}

	private void setUpDataRenderers() {
		// Column 0 is the row headers with the row numbers.
		// It gets a special renderer.
		TableColumnModel columnModel = getColumnModel();
		TableColumn column = columnModel.getColumn(0);
       
		MultiRowHeaderTableModel model = (MultiRowHeaderTableModel) getModel();
		MultiRowHeaderRenderer headerRenderer = new MultiRowHeaderRenderer(model.getColumnRowHeaders());
		column.setHeaderRenderer(headerRenderer);
		RowHeaderRenderer cellRenderer = new RowHeaderRenderer();
		column.setCellRenderer(cellRenderer);
		setUpFormatRenderers();
	}

	protected void setUpColumnHeaders() {
		MultiRowHeaderTableModel model = (MultiRowHeaderTableModel) getModel();
		TableColumnModel columnModel = getColumnModel();
		for (int i = 1; i < columnModel.getColumnCount(); i++) {
			TableColumn col = columnModel.getColumn(i);
			col.setHeaderRenderer(new MultiRowHeaderRenderer(model.getColumnHeaders(i - 1)));
		}
	}
	
}
