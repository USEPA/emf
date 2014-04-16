package gov.epa.mims.analysisengine.table;

import gov.epa.mims.analysisengine.table.filter.FilteringTableModel;
import gov.epa.mims.analysisengine.table.sort.SortCriteria;
import gov.epa.mims.analysisengine.table.sort.SortingTableModel;

import java.util.Calendar;
import java.util.Date;

import junit.framework.TestCase;

/**
 * <p>
 * Description: Test to check the table model
 * </p>
 * <p>
 * Company: UNC - CEP
 * </p>
 * 
 * @author Daniel Gatti
 * @version $Id: SortTableModelTest.java,v 1.3 2006/11/01 15:33:40 parthee Exp $
 */
public class SortTableModelTest extends TestCase {
	static Integer[][] intData = new Integer[5][5];

	static Double[][] dblData = new Double[5][5];

	static String[][] strData = new String[5][5];

	static Date[][] dateData = new Date[5][5];

	static String[][] columnNames = { { "col1", "col2", "col3", "col4", "col5" } };

	static {
		int[][] ints = { { 5, 4, 3, 4, 1 }, { 4, 5, 5, 5, 2 }, { 3, 2, 2, 3, 3 }, { 2, 1, 4, 1, 4 }, { 1, 3, 1, 2, 5 } };

		for (int i = 0; i < 5; i++) {
			for (int j = 0; j < 5; j++) {
				intData[i][j] = new Integer(ints[i][j]);
			}
		}

		double[][] dbls = { { 1.0, 2.0, 3.0, 4.0, 5.0 }, { 2.0, 5.0, 2.0, 5.0, 1.0 }, { 3.0, 4.0, 5.0, 1.0, 2.0 },
				{ 4.0, 3.0, 4.0, 2.0, 4.0 }, { 5.0, 1.0, 1.0, 3.0, 3.0 } };

		for (int i = 0; i < 5; i++) {
			for (int j = 0; j < 5; j++) {
				dblData[i][j] = new Double(dbls[i][j]);
			}
		}

		strData[0] = new String[] { "a", "b", "c", "d", "e" };
		strData[1] = new String[] { "b", "c", "a", "c", "b" };
		strData[2] = new String[] { "e", "d", "e", "e", "d" };
		strData[3] = new String[] { "d", "a", "d", "b", "a" };
		strData[4] = new String[] { "c", "e", "b", "a", "c" };


		Calendar cal = Calendar.getInstance();
		cal.set(2000, 0, 1, 0, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		int[][] dates = { { 1, 2, 3, 4, 5 }, { 4, 3, 1, 5, 2 }, { 3, 5, 4, 2, 1 }, { 5, 4, 5, 1, 3 }, { 2, 1, 2, 3, 4 } };

		for (int i = 0; i < 5; i++) {
			for (int j = 0; j < 5; j++) {
				cal.set(Calendar.MONTH, dates[j][i]);
				dateData[i][j] = cal.getTime();
			}
		}
	} // static

	/**
	 * Constructor.
	 */
	public SortTableModelTest(String name) {
		super(name);
	} // TableTest()

	public void testModelTransparent() {
		SimpleTestModel dfm = new SimpleTestModel(intData, null, columnNames);
		SortingTableModel stm = new SortingTableModel(dfm);

		// Check columns
		for (int i = 0; i < columnNames.length; i++) {
			assertEquals("column names", columnNames[i][0], stm.getColumnHeaders(i)[0]);
		} // for(i)

		// Check data
		for (int r = 0; r < stm.getRowCount(); r++) {
			for (int c = 0; c < stm.getColumnCount(); c++) {
				assertEquals("data (" + r + ", " + c + ")", intData[r][c], stm.getValueAt(r, c));
			} // for(c)
		} // for(r)
	} // testModelTransparent()

	public void testSortOneColumnWithNullsAscending() {
		String[][] strDataWithNulls = new String[5][1];
		strDataWithNulls[0][0] = null;
		strDataWithNulls[1][0] = "a";
		strDataWithNulls[2][0] = null;
		strDataWithNulls[3][0] = "c";
		strDataWithNulls[4][0] = "b";
		SimpleTestModel dfm = new SimpleTestModel(strDataWithNulls, null, new String[][] { { "col1" } });
		FilteringTableModel ftm = new FilteringTableModel(dfm);
		SortingTableModel stm = new SortingTableModel(ftm);
		String[] columnsToSort = new String[1];

		// Sort column 0 ascending.
		columnsToSort[0] = columnNames[0][0];
		SortCriteria sc = new SortCriteria(columnsToSort, new boolean[] { true }, new boolean[] { false });
		stm.sortTable(sc, stm.getRowCount() - 1);

		String[] correctOrder = new String[] { null, null, "a", "b", "c" };
		for (int r = 0; r < stm.getRowCount(); r++) {
			System.out.println(stm.getValueAt(r, 0));
			assertEquals(correctOrder[r], stm.getValueAt(r, 0));
		}
	}
	
	public void testSortOneColumnWithNullsDescending() {
		String[][] strDataWithNulls = new String[5][1];
		strDataWithNulls[0][0] = null;
		strDataWithNulls[1][0] = "a";
		strDataWithNulls[2][0] = null;
		strDataWithNulls[3][0] = "c";
		strDataWithNulls[4][0] = "b";
		SimpleTestModel dfm = new SimpleTestModel(strDataWithNulls, null, new String[][] { { "col1" } });
		FilteringTableModel ftm = new FilteringTableModel(dfm);
		SortingTableModel stm = new SortingTableModel(ftm);
		String[] columnsToSort = new String[1];

		// Sort column 0 ascending.
		columnsToSort[0] = columnNames[0][0];
		SortCriteria sc = new SortCriteria(columnsToSort, new boolean[] { false }, new boolean[] { false });
		stm.sortTable(sc, stm.getRowCount() - 1);

		String[] correctOrder = new String[] { "c","b", "a", null, null  };
		for (int r = 0; r < stm.getRowCount(); r++) {
			assertEquals(correctOrder[r], stm.getValueAt(r, 0));
		}
	}


	/**
	 * Sort each column and see if it is sorted. Sort one column ascending, the next descending, the next ascending...
	 * Integers and Doubles are tested here.
	 */
	public void testSortOneColumn() {
		SimpleTestModel dfm = new SimpleTestModel(dblData, null, columnNames);
		FilteringTableModel ftm = new FilteringTableModel(dfm);
		SortingTableModel stm = new SortingTableModel(ftm);
		String[] columnsToSort = new String[1];

		// Sort column 0 ascending.
		columnsToSort[0] = columnNames[0][0];
		SortCriteria sc = new SortCriteria(columnsToSort, new boolean[] { true }, new boolean[] { false });
		stm.sortTable(sc, stm.getRowCount() - 1);

		for (int r = 0; r < dblData.length; r++) {
			for (int c = 0; c < dblData[0].length; c++) {
				assertEquals("data (" + r + ", " + c + ")", dblData[r][c], stm.getValueAt(r, c));
			}
		}

		// Sort column 1 descending.
		columnsToSort[0] = columnNames[0][1];
		sc = new SortCriteria(columnsToSort, new boolean[] { false }, new boolean[] { false });
		stm.sortTable(sc, stm.getRowCount() - 1);

		// The correctly sorted order of the data.
		int[] correct = { 1, 2, 3, 0, 4 };

		// Sort column 2 ascending.
		columnsToSort[0] = columnNames[0][2];
		sc = new SortCriteria(columnsToSort, new boolean[] { true }, new boolean[] { false });
		stm.sortTable(sc, stm.getRowCount() - 1);

		correct = new int[] { 4, 1, 0, 3, 2 };

		for (int r = 0; r < stm.getRowCount(); r++) {
			for (int c = 0; c < stm.getColumnCount(); c++) {
				assertEquals("(" + r + ", " + c + ")", dblData[correct[r]][c], stm.getValueAt(r, c));
			} // for(c)
		}
		// Sort column 3 descending.
		columnsToSort[0] = columnNames[0][3];
		sc = new SortCriteria(columnsToSort, new boolean[] { false }, new boolean[] { false });
		stm.sortTable(sc, stm.getRowCount() - 1);
		correct = new int[] { 1, 0, 4, 3, 2 };
		for (int r = 0; r < stm.getRowCount(); r++) {
			for (int c = 0; c < stm.getColumnCount(); c++) {
				assertEquals("(" + r + ", " + c + ")", dblData[correct[r]][c], stm.getValueAt(r, c));
			}
		}

		// Sort column 4 ascending.
		columnsToSort[0] = columnNames[0][4];
		sc = new SortCriteria(columnsToSort, new boolean[] { true }, new boolean[] { false });
		stm.sortTable(sc, stm.getRowCount() - 1);

		correct = new int[] { 1, 2, 4, 3, 0 };

		for (int r = 0; r < stm.getRowCount(); r++) {
			for (int c = 0; c < stm.getColumnCount(); c++) {
				assertEquals("(" + r + ", " + c + ")", dblData[correct[r]][c], stm.getValueAt(r, c));
			} // for(c)
		}
	} // testSortOneColumn()

	/**
	 * Test sorting Strings.
	 */
	public void testSortingStrings() {
		SimpleTestModel dfm = new SimpleTestModel(strData, null, columnNames);
		FilteringTableModel ftm = new FilteringTableModel(dfm);
		SortingTableModel stm = new SortingTableModel(ftm);
		String[] columnsToSort = new String[1];

		// Sort column 0 ascending.
		columnsToSort[0] = columnNames[0][0];
		SortCriteria sc = new SortCriteria(columnsToSort, new boolean[] { true }, new boolean[] { false });
		stm.sortTable(sc, stm.getRowCount() - 1);

		int[] correct = { 0, 1, 4, 3, 2 };

		for (int r = 0; r < stm.getRowCount(); r++) {
			for (int c = 0; c < stm.getColumnCount(); c++) {
				assertEquals("(" + r + ", " + c + ")", strData[correct[r]][c], stm.getValueAt(r, c));
			} // for(c)
		}

		// Sort column 1 descending.
		columnsToSort[0] = columnNames[0][1];
		sc = new SortCriteria(columnsToSort, new boolean[] { false }, new boolean[] { false });
		stm.sortTable(sc, stm.getRowCount() - 1);

		correct = new int[] { 4, 2, 1, 0, 3 };

		for (int r = 0; r < stm.getRowCount(); r++) {
			for (int c = 0; c < stm.getColumnCount(); c++) {
				assertEquals("(" + r + ", " + c + ")", strData[correct[r]][c], stm.getValueAt(r, c));
			} // for(c)
		}

		// Sort column 2 ascending.
		columnsToSort[0] = columnNames[0][2];
		sc = new SortCriteria(columnsToSort, new boolean[] { true }, new boolean[] { false });
		stm.sortTable(sc, stm.getRowCount() - 1);

		correct = new int[] { 1, 4, 0, 3, 2 };

		for (int r = 0; r < stm.getRowCount(); r++) {
			for (int c = 0; c < stm.getColumnCount(); c++) {
				assertEquals("(" + r + ", " + c + ")", strData[correct[r]][c], stm.getValueAt(r, c));
			} // for(c)
		}

		// Sort column 3 descending.
		columnsToSort[0] = columnNames[0][3];
		sc = new SortCriteria(columnsToSort, new boolean[] { false }, new boolean[] { false });
		stm.sortTable(sc, stm.getRowCount() - 1);

		correct = new int[] { 2, 0, 1, 3, 4 };

		for (int r = 0; r < stm.getRowCount(); r++) {
			for (int c = 0; c < stm.getColumnCount(); c++) {
				assertEquals("(" + r + ", " + c + ")", strData[correct[r]][c], stm.getValueAt(r, c));
			} // for(c)
		}

		// Sort column 4 ascending.
		columnsToSort[0] = columnNames[0][4];
		sc = new SortCriteria(columnsToSort, new boolean[] { true }, new boolean[] { false });
		stm.sortTable(sc, stm.getRowCount() - 1);

		correct = new int[] { 3, 1, 4, 2, 0 };

		for (int r = 0; r < stm.getRowCount(); r++) {
			for (int c = 0; c < stm.getColumnCount(); c++) {
				assertEquals("(" + r + ", " + c + ")", strData[correct[r]][c], stm.getValueAt(r, c));
			} // for(c)
		}

	} // testSortingStrings()

	/**
	 * Test sorting dates.
	 */
	public void testSortingDates() {
		SimpleTestModel dfm = new SimpleTestModel(dateData, null, columnNames);
		FilteringTableModel ftm = new FilteringTableModel(dfm);
		SortingTableModel stm = new SortingTableModel(ftm);
		String[] columnsToSort = new String[1];

		// Sort column 0 ascending.
		columnsToSort[0] = columnNames[0][0];
		SortCriteria sc = new SortCriteria(columnsToSort, new boolean[] { true }, new boolean[] { true });
		stm.sortTable(sc, stm.getRowCount() - 1);

		int[] correct = { 0, 1, 2, 3, 4 };

		for (int r = 0; r < stm.getRowCount(); r++) {
			for (int c = 0; c < stm.getColumnCount(); c++) {
				assertEquals("(" + r + ", " + c + ")", dateData[correct[r]][c], stm.getValueAt(r, c));
			} // for(c)
		} // for(r)

		// Sort column 1 descending.
		columnsToSort[0] = columnNames[0][1];
		sc = new SortCriteria(columnsToSort, new boolean[] { false }, new boolean[] { true });
		stm.sortTable(sc, stm.getRowCount() - 1);

		correct = new int[] { 3, 0, 1, 4, 2 };

		for (int r = 0; r < stm.getRowCount(); r++) {
			for (int c = 0; c < stm.getColumnCount(); c++) {
				assertEquals("(" + r + ", " + c + ")", dateData[correct[r]][c], stm.getValueAt(r, c));
			} // for(c)
		} // for(r)

		// Sort column 2 ascending.
		columnsToSort[0] = columnNames[0][2];
		sc = new SortCriteria(columnsToSort, new boolean[] { true }, new boolean[] { true });
		stm.sortTable(sc, stm.getRowCount() - 1);

		correct = new int[] { 4, 3, 0, 2, 1 };

		for (int r = 0; r < stm.getRowCount(); r++) {
			for (int c = 0; c < stm.getColumnCount(); c++) {
				assertEquals("(" + r + ", " + c + ")", dateData[correct[r]][c], stm.getValueAt(r, c));
			} // for(c)
		}

		// Sort column 3 descending.
		columnsToSort[0] = columnNames[0][3];
		sc = new SortCriteria(columnsToSort, new boolean[] { false }, new boolean[] { true });
		stm.sortTable(sc, stm.getRowCount() - 1);

		correct = new int[] { 0, 2, 1, 4, 3 };

		for (int r = 0; r < stm.getRowCount(); r++) {
			for (int c = 0; c < stm.getColumnCount(); c++) {
				assertEquals("(" + r + ", " + c + ")", dateData[correct[r]][c], stm.getValueAt(r, c));
			} // for(c)
		}

		// Sort column 4 ascending.
		columnsToSort[0] = columnNames[0][4];
		sc = new SortCriteria(columnsToSort, new boolean[] { true }, new boolean[] { true });
		stm.sortTable(sc, stm.getRowCount() - 1);

		correct = new int[] { 1, 0, 2, 3, 4 };

		for (int r = 0; r < stm.getRowCount(); r++) {
			for (int c = 0; c < stm.getColumnCount(); c++) {
				assertEquals("(" + r + ", " + c + ")", dateData[correct[r]][c], stm.getValueAt(r, c));
			} // for(c)
		}

	} // testSortingDates()

	/**
	 * Test sorting multiple columns.
	 */
	public void testMultipleColumnSort() {
		String[] column1 = { "b", "c", "c", "b", "d", "a", "b", "a", "c", "a" };
		int[] column2 = { 2, 2, 3, 1, 1, 3, 3, 2, 1, 1 };
		double[] column3 = { 1.0, 3.0, 3.0, 2.0, 3.0, 2.0, 1.0, 2.0, 2.0, 1.0 };

		Object[][] data = new Object[10][3];
		for (int r = 0; r < 10; r++) {
			data[r] = new Object[3];
			data[r][0] = column1[r];
			data[r][1] = new Integer(column2[r]);
			data[r][2] = new Double(column3[r]);
		} // for(r)

		String[][] colNames = { { "col1", "col2", "col3" } };

		SimpleTestModel dfm = new SimpleTestModel(data, null, colNames);
		FilteringTableModel ftm = new FilteringTableModel(dfm);
		SortingTableModel stm = new SortingTableModel(ftm);
		String[] columnsToSort = new String[2];

		// Sort by columns 0 (ascending), 1 (descending)
		columnsToSort[0] = colNames[0][0];
		columnsToSort[1] = colNames[0][1];
		boolean[] asc = { true, false };
		boolean[] cas = { false, false };
		SortCriteria sc = new SortCriteria(columnsToSort, asc, cas);
		stm.sortTable(sc, stm.getRowCount() - 1);

		// The indexes in the correct, sorted order.
		int[] correct = { 5, 7, 9, 6, 0, 3, 2, 1, 8, 4 };

		for (int r = 0; r < stm.getRowCount(); r++) {
			for (int c = 0; c < stm.getColumnCount(); c++) {
				assertEquals("(" + r + ", " + c + ")", data[correct[r]][c], stm.getValueAt(r, c));
			}
		}

		// Sort by columns 0 (descending), 1 (ascending)
		columnsToSort[0] = colNames[0][0];
		columnsToSort[1] = colNames[0][1];
		asc = new boolean[] { false, true };
		cas = new boolean[] { false, false };
		sc = new SortCriteria(columnsToSort, asc, cas);
		stm.sortTable(sc, stm.getRowCount() - 1);

		// The indexes in the csorrect, sorted order.
		correct = new int[] { 4, 8, 1, 2, 3, 0, 6, 9, 7, 5 };

		for (int r = 0; r < stm.getRowCount(); r++) {
			for (int c = 0; c < stm.getColumnCount(); c++) {
				assertEquals("(" + r + ", " + c + ")", data[correct[r]][c], stm.getValueAt(r, c));
			}
		}

		// Sort by columns 2 (descending), 1 (ascending)
		columnsToSort[0] = colNames[0][2];
		columnsToSort[1] = colNames[0][1];
		asc = new boolean[] { false, true };
		cas = new boolean[] { false, false };
		sc = new SortCriteria(columnsToSort, asc, cas);
		stm.sortTable(sc, stm.getRowCount() - 1);

		// The indexes in the correct, sorted order.
		correct = new int[] { 4, 1, 2, 3, 8, 7, 5, 9, 0, 6 };

		for (int r = 0; r < stm.getRowCount(); r++) {
			for (int c = 0; c < stm.getColumnCount(); c++) {
				assertEquals("(" + r + ", " + c + ")", data[correct[r]][c], stm.getValueAt(r, c));
			}
		}

		// Sort by columns 0 (ascending), 2 (deascending)
		columnsToSort[0] = colNames[0][0];
		columnsToSort[1] = colNames[0][2];
		asc = new boolean[] { true, false };
		cas = new boolean[] { false, false };
		sc = new SortCriteria(columnsToSort, asc, cas);
		stm.sortTable(sc, stm.getRowCount() - 1);

		// The indexes in the correct, sorted order.
		correct = new int[] { 5, 7, 9, 3, 0, 6, 1, 2, 8, 4 };

		for (int r = 0; r < stm.getRowCount(); r++) {
			for (int c = 0; c < stm.getColumnCount(); c++) {
				assertEquals("(" + r + ", " + c + ")", data[correct[r]][c], stm.getValueAt(r, c));
			}
		}

	} // testMultipleColumnSort()

	/**
	 * Test sorting three columns when the first column has all the same numbers, the second column contains two numbers
	 * and the third has unique numbers.
	 */
	public void testCascadingSort() {
		int[][] ints = { { 1, 2, 3 }, { 1, 3, 4 }, { 1, 2, 7 }, { 1, 2, 9 }, { 1, 3, 5 }, { 1, 3, 6 } };

		Integer[][] data = new Integer[6][3];
		for (int i = 0; i < 6; i++) {
			for (int j = 0; j < 3; j++) {
				data[i][j] = new Integer(ints[i][j]);
			}
		}

		String[][] colNames = { { "col1", "col2", "col3" } };

		SimpleTestModel dfm = new SimpleTestModel(data, null, colNames);
		FilteringTableModel ftm = new FilteringTableModel(dfm);
		SortingTableModel stm = new SortingTableModel(ftm);
		String[] columnsToSort = new String[3];

		// Sort by columns 0 (ascending), 1 (descending), 2 (ascending)
		columnsToSort[0] = colNames[0][0];
		columnsToSort[1] = colNames[0][1];
		columnsToSort[2] = colNames[0][2];
		boolean[] asc = { true, false, true };
		boolean[] cas = { false, false, false };
		SortCriteria sc = new SortCriteria(columnsToSort, asc, cas);
		stm.sortTable(sc, stm.getRowCount() - 1);

		// The indexes in the correct, sorted order.
		int[] correct = { 1, 4, 5, 0, 2, 3 };

		for (int r = 0; r < stm.getRowCount(); r++) {
			for (int c = 0; c < stm.getColumnCount(); c++) {
				assertEquals("(" + r + ", " + c + ")", data[correct[r]][c], stm.getValueAt(r, c));
			}
		}

		// Sort by columns 0 (descending), 1 (ascending), 2 (descending)
		columnsToSort[0] = colNames[0][0];
		columnsToSort[1] = colNames[0][1];
		columnsToSort[2] = colNames[0][2];
		asc = new boolean[] { false, true, false };
		cas = new boolean[] { false, false, false };
		sc = new SortCriteria(columnsToSort, asc, cas);
		stm.sortTable(sc, stm.getRowCount() - 1);

		// The indexes in the correct, sorted order.
		correct = new int[] { 3, 2, 0, 5, 4, 1 };

		for (int r = 0; r < stm.getRowCount(); r++) {
			for (int c = 0; c < stm.getColumnCount(); c++) {
				assertEquals("(" + r + ", " + c + ")", data[correct[r]][c], stm.getValueAt(r, c));
			}
		}
	}
}
