package gov.epa.emissions.framework.client.status;
/**
 * MultiLineTable.java
 *
 * Created: Tue May 18 13:15:59 1999
 *
 * @author Thomas Wernitz, Da Vinci Communications Ltd <thomas_wernitz@clear.net.nz>
 *
 * credit to Zafir Anjum for JTableEx and thanks to SUN for their source code ;)
 * 
 * Modified by IE, UNC on 2/21/2011
 */

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.text.Segment;
import javax.swing.text.TabExpander;


public class MultiLineTable extends JTable {

	public MultiLineTable() {
		this(null, null, null);
	}

	public MultiLineTable(TableModel dm) {
		this(dm, null, null);
	}

	public MultiLineTable(TableModel dm, TableColumnModel cm) {
		this(dm, cm, null);
	}

	public MultiLineTable(TableModel dm, TableColumnModel cm, ListSelectionModel sm) {
		super(dm,cm,sm);
		setUI( new MultiLineBasicTableUI() );
		// I know this sucks tremendously, but I was too lazy to find a proper solution. :(
		// The problem is, that without this hack, a resize that changes the number of lines in
		// the TextArea does not result in the proper resizing of the table, because the
		// new width of the column is not available through getWidth until resize is complete.
		// Does this make sense? 8-/  Have a look into getRowHeight(int)! 
		addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {    
				revalidate();
			}
		});
	}

	public MultiLineTable(int numRows, int numColumns) {
		this(new DefaultTableModel(numRows, numColumns));
	}

	public MultiLineTable(final Vector rowData, final Vector columnNames) {
		super( rowData, columnNames );
		setUI( new MultiLineBasicTableUI() );
		addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {    
				revalidate();
			}
		});
	}

	public MultiLineTable(final Object[][] rowData, final Object[] columnNames) {
		super( rowData, columnNames );
		setUI( new MultiLineBasicTableUI() );
		addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {    
				revalidate();
			}
		});
	}

	public int rowAtPoint(Point point) {
		int y = point.y;
		int rowSpacing = getIntercellSpacing().height;
		int rowCount = getRowCount();
		int rowHeight = 0;
		for (int row=0; row<rowCount; row++) {
			rowHeight += getRowHeight(row) + rowSpacing;
			if (y < rowHeight) {
				return row;
			}
		}
		return -1;
	}

	public int getHeight(String text, int width) {
		FontMetrics fm = getFontMetrics(getFont());
		int numLines = 1;
		
		if (text == null || text.trim().isEmpty())
		    return numLines * fm.getHeight();
		
		Segment s = new Segment(text.toCharArray(), 0, 0);
		s.count = s.array.length;
		TabExpander te = new MyTabExpander(fm);
		int breaks = getBreakLocation(s, fm, 0, width, te, 0);
		while((breaks+s.offset) < s.array.length) {
			s.offset += breaks;
			s.count = s.array.length - s.offset;
			numLines++;
			breaks = getBreakLocation(s, fm, 0, width, te, 0);
		}
		return numLines * fm.getHeight();
	}

	public int getTabbedTextOffset(Segment s, 
			FontMetrics metrics,
			int x0, int x, TabExpander e,
			int startOffset, 
			boolean round) {
		int currX = x0;
		int nextX = currX;
		char[] txt = s.array;
		int n = s.offset + s.count;
		for (int i = s.offset; i < n; i++) {
			if (txt[i] == '\t') {
				if (e != null) {
					nextX = (int) e.nextTabStop((float) nextX,
							startOffset + i - s.offset);
				} else {
					nextX += metrics.charWidth(' ');
				}
			} else if (txt[i] == '\n') {
				return i - s.offset;
			} else if (txt[i] == '\r') {
				return i + 1 - s.offset; // kill the newline as well
			} else {
				nextX += metrics.charWidth(txt[i]);
			}
			if ((x >= currX) && (x < nextX)) {
				// found the hit position... return the appropriate side
				if ((round == false) || ((x - currX) < (nextX - x))) {
					return i - s.offset;
				}
                return i + 1 - s.offset;
			}
			currX = nextX;
		}

		return s.count;
	}

	public int getBreakLocation(Segment s, FontMetrics metrics,
			int x0, int x, TabExpander e,
			int startOffset) {

		int index = getTabbedTextOffset(s, metrics, x0, x, 
				e, startOffset, false);

		if ((s.offset+index) < s.array.length) {
			for (int i = s.offset + Math.min(index, s.count - 1); 
			i >= s.offset; i--) {

				char ch = s.array[i];
				if (Character.isWhitespace(ch)) {
					// found whitespace, break here
					index = i - s.offset + 1;
					break;
				}
			}
		}
		return index;
	}

	class MyTabExpander implements TabExpander {
		int tabSize;
		public MyTabExpander(FontMetrics metrics) {
			tabSize = 5 * metrics.charWidth('m');
		}
		public float nextTabStop(float x, int offset) {
			int ntabs = (int) x / tabSize;
			return (ntabs + 1) * tabSize;
		}
	}


	public int getRowHeight() {
//		System.err.println("getRowHeight() not valid in MultiLineTable");
//		Thread.dumpStack();
		return -1;
	}

	public int getRowHeight(int row) {
		int numCols = getColumnCount();
		TableModel tm = getModel();
		int fontHeight = getFontMetrics(getFont()).getHeight();
		int height = fontHeight;
		Enumeration cols = getColumnModel().getColumns();
		int i = 0;
		while(cols.hasMoreElements()) {
			TableColumn col = (TableColumn) cols.nextElement();
			TableCellRenderer tcr = col.getCellRenderer();
			// without the revalidate hack above, the call th getWidth does not give the
			// right value at the right time. Take out the revalidate and uncomment the
			// next line to see for your self. If you find a way to do it right, drop me
			// a mail please! :)
			// System.out.println(col.getWidth());
			int colWidth = col.getWidth();
			if (tcr instanceof MultiLineCellRenderer) {
				height = Math.max(height, getHeight((String)tm.getValueAt(row,i), colWidth));
			}
			i++;
		}
		return height;
	}

	public Rectangle getCellRect(int row, int column, boolean includeSpacing) {
		Rectangle cellFrame;
		TableColumn aColumn;

		cellFrame = new Rectangle();
		//        cellFrame.height = getRowHeight() + rowMargin;
		//        cellFrame.y = row * cellFrame.height;
		cellFrame.height = getRowHeight(row) + rowMargin;
		cellFrame.y = 0;
		for (int i=0; i<row; i++) {
			cellFrame.y += getRowHeight(i) + rowMargin;
		}

		int index = 0;
		int columnMargin = getColumnModel().getColumnMargin();
		Enumeration enumeration = getColumnModel().getColumns();
		while (enumeration.hasMoreElements()) {
			aColumn = (TableColumn)enumeration.nextElement();
			cellFrame.width = aColumn.getWidth() + columnMargin;

			if (index == column)
				break;

			cellFrame.x += cellFrame.width;
			index++;
		}

		if (!includeSpacing) {
			Dimension spacing = getIntercellSpacing();
			// This is not the same as grow(), it rounds differently.
			cellFrame.setBounds(cellFrame.x +      spacing.width/2,
					cellFrame.y +      spacing.height/2,
					cellFrame.width -  spacing.width,
					cellFrame.height - spacing.height);
		}
		return cellFrame;
	}


	public void columnSelectionChanged(ListSelectionEvent e) {
		repaint();
	}

	public void valueChanged(ListSelectionEvent e) {
		int firstIndex = e.getFirstIndex();
		int  lastIndex = e.getLastIndex();
		if (firstIndex == -1 && lastIndex == -1) { // Selection cleared.
			repaint();
		}
		Rectangle dirtyRegion = getCellRect(firstIndex, 0, false);
		int numColumns = getColumnCount();
		int index = firstIndex;
		for (int i=0;i<numColumns;i++) {
			dirtyRegion.add(getCellRect(index, i, false));
		}
		index = lastIndex;
		for (int i=0;i<numColumns;i++) {
			dirtyRegion.add(getCellRect(index, i, false));
		}
		repaint(dirtyRegion.x, dirtyRegion.y, dirtyRegion.width, dirtyRegion.height);
	}

} // MultiLineTable
