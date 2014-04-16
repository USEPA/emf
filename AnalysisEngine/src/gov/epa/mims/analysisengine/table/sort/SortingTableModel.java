package gov.epa.mims.analysisengine.table.sort;

import gov.epa.mims.analysisengine.table.MultiRowHeaderTableModel;
import gov.epa.mims.analysisengine.table.filter.FilteringTableModel;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

public class SortingTableModel extends MultiRowHeaderTableModel implements TableModelListener {
	private MultiRowHeaderTableModel underlyingModel = null;

	private int[] sortingMap = null;

	private SortCriteria sortCriteria = null;

	public SortingTableModel(MultiRowHeaderTableModel model) {
		super(model);
		if (model == null)
			throw new IllegalArgumentException("The underlying data model cannot be null in SortingTableModel().");

		underlyingModel = model;
		underlyingModel.addTableModelListener(this);

		int size = getRowCount();
		sortingMap = new int[size];
		for (int i = 0; i < getRowCount(); i++)
			sortingMap[i] = i;
	}
	
	public void setModel(MultiRowHeaderTableModel model) {
		if (model == null)
			throw new IllegalArgumentException("The underlying data model cannot be null in SortingTableModel().");
		if ( underlyingModel != null )
			underlyingModel.removeTableModelListener(this);
		underlyingModel = model;
		underlyingModel.addTableModelListener(this);

		sortTable(sortCriteria, getRowCount() - 1);
	}

	public Class getColumnClass(int column) {
		return underlyingModel.getColumnClass(column);
	}

	public int getColumnCount() {
		return underlyingModel.getColumnCount();
	}

	public String[] getColumnHeaders(int col) {
		return underlyingModel.getColumnHeaders(col);
	}

	public String getColumnName(int column) {
		return underlyingModel.getColumnName(column);
	}

	public int getRowCount() {
		return underlyingModel.getRowCount();
	}

	public SortCriteria getSortCriteria() {
		return sortCriteria;
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		return underlyingModel.getValueAt(sortingMap[rowIndex], columnIndex);
	}

	public int getBaseModelRowIndex(int rowIndex) {
		return underlyingModel.getBaseModelRowIndex(sortingMap[rowIndex]);
	}

	public void sortTable(SortCriteria criteria, int lastRow) {
		this.sortCriteria = criteria;
		// of rows.
		int size = underlyingModel.getRowCount();
		sortingMap = new int[size];
		for (int i = 0; i < getRowCount(); i++) {
			sortingMap[i] = i;
		}

		if (criteria != null) {
			// Now sort the columns that we have from the underlying model.
			String[] columnNames = criteria.getColumnNames();
			int[] columns = new int[columnNames.length];
			for (int i = 0; i < columns.length; i++) {
				Integer intObj = (Integer) ((FilteringTableModel) underlyingModel).nameToIndexHash.get(columnNames[i]);
				if (intObj != null)
					columns[i] = intObj.intValue();
			} // for(i)
             
			boolean[] ascending = criteria.getAscending();
			boolean[] caseSensitive = criteria.getCaseSensitive();
			// Sort the last column first to that the first column is sorted when we get done.
			for (int i = columns.length - 1; i >= 0; --i) {
				if (columns[i] >= 0){
					mergeSort(0, lastRow, columns[i], ascending[i], caseSensitive[i]);
					if (getColumnClass(columns[i]) == Double.class) {
						moveNaNsDown(0, lastRow, columns[i]);
					}
				}
			} // for(i)
		}

		fireTableDataChanged();
	} // sortTable()

	private void moveNaNsDown(int firstElement, int lastElement, int column) {
		boolean begin = true;
		int index = firstElement;
		int[] tempMap = new int[sortingMap.length];
		for (int i = 0; i < tempMap.length; i++)
			tempMap[i] = sortingMap[i];
		while (index < lastElement && ((Double) getValueAt(index, column)).equals(new Double(Double.NaN)))
			index++;
		if (index == firstElement)
			begin = false;

		if (begin) {
			int i = index;
			while (i > firstElement)
				sortingMap[lastElement - (--i)] = tempMap[i];
			for (i = 0; i <= (lastElement - index); i++)
				sortingMap[i] = tempMap[i + index];
		}
	}

	private void mergeSort(int start, int finish, int column, boolean ascending, boolean caseSensitive) {
		if (start >= finish)
			return;
		int middle = (start + finish) / 2;
		mergeSort(start, middle, column, ascending, caseSensitive);
		mergeSort(middle + 1, finish, column, ascending, caseSensitive);
		merge(start, middle, finish, column, ascending, caseSensitive);
	}

	private void merge(int start, int middle, int finish, int column, boolean ascending, boolean caseSensitive) {
		int lower = start;
		int upper = middle + 1;
		int[] tmp = new int[finish - start + 1];
		if (ascending) {
			sorting(middle, finish, column, caseSensitive, lower, upper, tmp);
		} else {
			decscending(middle, finish, column, caseSensitive, lower, upper, tmp);
		}

		System.arraycopy(tmp, 0, sortingMap, start, tmp.length);
	}

	private void sorting(int middle, int finish, int column, boolean caseSensitive, int lower, int upper, int[] tmp) {
		int i = 0;
		for (i = 0; i < tmp.length; i++) {
			if (lower > middle) {
				tmp[i] = sortingMap[upper++];
			} else if (upper > finish) {
				tmp[i] = sortingMap[lower++];
			} else {
				Object comp1 = getValueAt(lower, column);
				Object comp2 = getValueAt(upper, column);
				if (comp1 == null || comp2 == null) {
					if (comp1 == null && comp2 == null) {
						tmp[i] = sortingMap[lower++];
					} else if (comp1 == null && comp2 != null) {
						tmp[i] = sortingMap[lower++];
					} else {// if ( comp1!=null && comp2 ==null)
						tmp[i] = sortingMap[upper++];
					}
				} else if (!caseSensitive && getColumnClass(column).equals(String.class)) {
					String s1 = comp1.toString();
					String s2 = comp2.toString();
					if (s1.compareToIgnoreCase(s2) <= 0) {
						tmp[i] = sortingMap[lower++];
					} else {
						tmp[i] = sortingMap[upper++];
					}
				} else if (getColumnClass(column).equals(Boolean.class)) {
					boolean c1 = ((Boolean) comp1).booleanValue();
					boolean c2 = ((Boolean) comp2).booleanValue();
					if ((c2 && !c1)) {
						tmp[i] = sortingMap[lower++];
					} else {
						tmp[i] = sortingMap[upper++];
					}
				} else {
					if (((Comparable) comp1).compareTo(comp2) <= 0) {
						tmp[i] = sortingMap[lower++];
					} else {
						tmp[i] = sortingMap[upper++];
					}
				}
			}
		}
	}

	private void decscending(int middle, int finish, int column, boolean caseSensitive, int lower, int upper, int[] tmp) {
		int i = 0;
		for (i = 0; i < tmp.length; i++) {
			if (lower > middle) {
				tmp[i] = sortingMap[upper++];
			} else if (upper > finish) {
				tmp[i] = sortingMap[lower++];
			} else {
				Object comp1 = getValueAt(lower, column);
				Object comp2 = getValueAt(upper, column);
				if (comp1 == null || comp2 == null) {
					if (comp1 == null && comp2 == null) {
						tmp[i] = sortingMap[upper++];
					} else if (comp1 == null && comp2 != null) {
						tmp[i] = sortingMap[upper++];
					} else {// if ( comp1!=null && comp2 ==null)
						tmp[i] = sortingMap[lower++];
					}
				} else if (!caseSensitive && getColumnClass(column).equals(String.class)) {
					String s1 = comp1.toString();
					String s2 = comp2.toString();
					if (s1.compareToIgnoreCase(s2) >= 0) {
						tmp[i] = sortingMap[lower++];
					} else {
						tmp[i] = sortingMap[upper++];
					}
				} else if (getColumnClass(column).equals(Boolean.class)) {
					boolean c1 = ((Boolean) comp1).booleanValue();
					boolean c2 = ((Boolean) comp2).booleanValue();
					if (!(c2 && !c1)) {
						tmp[i] = sortingMap[lower++];
					} else {
						tmp[i] = sortingMap[upper++];
					}
				} else {
					if (((Comparable) comp1).compareTo(comp2) >= 0) {
						tmp[i] = sortingMap[lower++];
					} else {
						tmp[i] = sortingMap[upper++];
					}
				}
			}
		}
	}

	public void setValueAt(Object value, int row, int column) {
		underlyingModel.setValueAt(value, sortingMap[row], column);
	}

	public void tableChanged(TableModelEvent e) {
		boolean shouldSort = true;
		if (sortCriteria != null) {
			FilteringTableModel filterModel = (FilteringTableModel) underlyingModel;
			String[] columnNames = sortCriteria.getColumnNames();
			Object obj = null;

			for (int i = 0; i < columnNames.length; i++) {
				obj = filterModel.nameToIndexHash.get(columnNames[i]);
				if (obj == null) {
					shouldSort = false;
					break;
				}
			}
		}
		if (shouldSort) {
			sortTable(sortCriteria, getRowCount() - 1);
		}
		fireTableChanged(e);
	}

	public String sortInString() {
		return (sortCriteria == null) ? "" : sortCriteria.toString();
	}

}
