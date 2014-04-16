package gov.epa.mims.analysisengine.table;

import gov.epa.mims.analysisengine.table.filter.FilterCriteria;
import gov.epa.mims.analysisengine.table.filter.FilteringTableModel;
import gov.epa.mims.analysisengine.table.format.NullFormatter;

import java.text.DecimalFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import junit.framework.TestCase;

public class FilterTableModelTest extends TestCase {
	
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
 * Test that the filter model is transparent when no filter is applied
 */

public void testFilterModelTransparent()
{
   SimpleTestModel dfm   = new SimpleTestModel(intData, null, columnNames);
   FilteringTableModel ftm = new FilteringTableModel(dfm);

   // Check columns
   for (int i = 0; i < columnNames.length; i++)
   {
      assertEquals("column names", columnNames[i][0], ftm.getColumnHeaders(i)[0]);
   } // for(i)

   // Check data
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertEquals("data (" + r + ", "+ c + ")", intData[r][c],
            ftm.getValueAt(r,c));
      } // for(c)
   } // for(r)
} // testFilterModelTransparent()


/**
 * Test filter with greater than.
 */
   public void FIXME_testFilterRowsGreaterThan()
{
   // Strings
   String[][] strs = {{"a"}, {"A"}, {"b"}, {"B"}, {"c"}, {"C"}, {"d"}, {"D"},
      {"e"}, {"E"}};
   String[][] cols = {{"col1"}};

   SimpleTestModel dfm   = new SimpleTestModel(strs, null, cols);
   FilteringTableModel ftm = new FilteringTableModel(dfm);

   // > "c"
   String[] names = {"col1"};
   int[] ops = {FilterCriteria.GREATER_THAN};
   Comparable[] cutoff = {"c"};
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0],true));

   // Check data
   assertEquals("number of rows", 2, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
            ((Comparable)ftm.getValueAt(r,c)).compareTo(cutoff[0]) > 0);
      } // for(c)
   } // for(r)

   // > "C"
   names[0]  = "col1";
   ops[0]    = FilterCriteria.GREATER_THAN;
   cutoff[0] = "C";
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0], true));

   // Check data
   assertEquals("number of rows", 7, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
            ((Comparable)ftm.getValueAt(r,c)).compareTo(cutoff[0]) > 0);
      } // for(c)
   } // for(r)

   // > "@" (should be all data)
   names[0] = "col1";
   ops[0] = FilterCriteria.GREATER_THAN;
   cutoff[0] = "@";
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0], true));

   // Check data
   assertEquals("number of rows", 10, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
                    ((Comparable)ftm.getValueAt(r,c)).compareTo(cutoff[0]) > 0);
      } // for(c)
   } // for(r)

   // > "e" (should be no data)
   names[0] = "col1";
   ops[0] = FilterCriteria.GREATER_THAN;
   cutoff[0] = "e";
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0], true));

   // Check data
   assertEquals("number of rows", 0, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
                    ((Comparable)ftm.getValueAt(r,c)).compareTo(cutoff[0]) > 0);
      } // for(c)
   } // for(r)

   // Integers
   int[] ints = {-5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5};
   Integer[][] iData = new Integer[11][1];
   for (int i = 0; i < 11; i++)
   {
      iData[i][0] = new Integer(ints[i]);
   }

   dfm = new SimpleTestModel(iData, null, cols);
   ftm = new FilteringTableModel(dfm);

   // > 0
   names[0] = "col1";
   ops[0] = FilterCriteria.GREATER_THAN;
   cutoff[0] = new Integer(0);
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0],true));

   // Check data
   assertEquals("number of rows", 5, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
            ((Comparable)ftm.getValueAt(r,c)).compareTo(cutoff[0]) > 0);
      } // for(c)
   } // for(r)

   // > -2
   cutoff[0] = new Integer(-2);
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0], true));

   // Check data
   assertEquals("number of rows", 7, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
            ((Comparable)ftm.getValueAt(r,c)).compareTo(cutoff[0]) > 0);
      } // for(c)
   } // for(r)

   // > -6 (should be all data)
   cutoff[0] = new Integer(-6);
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0], true));

   // Check data
   assertEquals("number of rows", 11, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
            ((Comparable)ftm.getValueAt(r,c)).compareTo(cutoff[0]) > 0);
      } // for(c)
   } // for(r)

   // > 5 (should be no data)
   cutoff[0] = new Integer(5);
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0], true));

   // Check data
   assertEquals("number of rows", 0, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
            ((Comparable)ftm.getValueAt(r,c)).compareTo(cutoff[0]) > 0);
      } // for(c)
   } // for(r)

   // Doubles
   double[] dbls = {-5.5, -4.2, -3.8, -2.6, -1.3, 0.0, 1.9, 2.1, 3.8, 4.7, 5.9};
   Double[][] dData = new Double[11][1];
   for (int i = 0; i < 11; i++)
   {
      dData[i][0] = new Double(dbls[i]);
   }

   dfm = new SimpleTestModel(dData, null, cols);
   ftm = new FilteringTableModel(dfm);

   // > 0.0
   cutoff[0] = new Double(0.0);
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0], true));

   // Check data
   assertEquals("number of rows", 5, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
            ((Comparable)ftm.getValueAt(r,c)).compareTo(cutoff[0]) > 0);
      } // for(c)
   } // for(r)

   // > 1.89999999
   cutoff[0] = new Double(1.89999999);
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0], true));

   // Check data
   assertEquals("number of rows", 5, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
            ((Comparable)ftm.getValueAt(r,c)).compareTo(cutoff[0]) > 0);
      } // for(c)
   } // for(r)

   // > -5.6 (should be all data)
   cutoff[0] = new Double(-5.6);
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0], true));

   // Check data
   assertEquals("number of rows", 11, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
            ((Comparable)ftm.getValueAt(r,c)).compareTo(cutoff[0]) > 0);
      } // for(c)
   } // for(r)

   // > 5.9 (should be no data)
   cutoff[0] = new Double(5.9);
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0], true));

   // Check data
   assertEquals("number of rows", 0, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
            ((Comparable)ftm.getValueAt(r,c)).compareTo(cutoff[0]) > 0);
      } // for(c)
   } // for(r)

   // Dates
   TimeZone tz = TimeZone.getTimeZone("EST");

   Calendar cal = Calendar.getInstance(tz);
   cal.set(2000, 0, 1, 0, 0);
   cal.set(Calendar.SECOND, 0);
   cal.set(Calendar.MILLISECOND, 0);
   // These are months in a (zero based, ie: Jan = 0)
   int[] dts = {11, 4, 7, 5, 2, 3, 0, 9, 8, 6, 10, 1};
   Date[][] dtData = new Date[12][1];
   for (int i = 0; i < 12; i++)
   {
      cal.set(Calendar.MONTH, dts[i]);
      dtData[i][0] = cal.getTime();
   }

   dfm = new SimpleTestModel(dtData, null, cols);
   ftm = new FilteringTableModel(dfm);

   // > Jun. 15, 2000
   cal.set(Calendar.MONTH, 5);
   cal.set(Calendar.DATE, 15);
   cutoff[0] = cal.getTime();
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0], true));

   // Check data
   assertEquals("number of rows", 6, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
            ((Comparable)ftm.getValueAt(r,c)).compareTo(cutoff[0]) > 0);
      } // for(c)
   } // for(r)

   // > Feb. 28, 2000
   cal.set(Calendar.MONTH, 1);
   cal.set(Calendar.DATE, 28);
   cutoff[0] = cal.getTime();
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0], true));

   // Check data
   assertEquals("number of rows", 10, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
            ((Comparable)ftm.getValueAt(r,c)).compareTo(cutoff[0]) > 0);
      } // for(c)
   } // for(r)

   // > Dec. 31, 1999 (should be all data)
   cal.set(Calendar.YEAR, 2000);
   cal.set(Calendar.MONTH, 0);
   cal.set(Calendar.DATE, 1);
   cal.add(Calendar.MILLISECOND, -1);
   cutoff[0] = cal.getTime();
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0], true));

   // Check data
   assertEquals("number of rows", 12, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
            ((Comparable)ftm.getValueAt(r,c)).compareTo(cutoff[0]) > 0);
      } // for(c)
   } // for(r)

   // > Dec. 1, 2000 (should be no data)
   cal.set(Calendar.YEAR, 2000);
   cal.set(Calendar.MONTH, 12);
   cal.set(Calendar.DATE, 1);
   cutoff[0] = cal.getTime();
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0], true));

   // Check data
   assertEquals("number of rows", 0, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
            ((Comparable)ftm.getValueAt(r,c)).compareTo(cutoff[0]) > 0);
      } // for(c)
   } // for(r)
}


/**
 * Test filter with greater than or equal for all types.
 */
   public void FIXME_testFilterRowsGreaterThanOrEqual()
{
   // Strings
   String[][] strs = {{"a"}, {"A"}, {"b"}, {"B"}, {"c"}, {"C"}, {"d"}, {"D"},
      {"e"}, {"E"}};
   String[][] cols = {{"col1"}};

   SimpleTestModel dfm   = new SimpleTestModel(strs, null, cols);
   FilteringTableModel ftm = new FilteringTableModel(dfm);

   // >= "c"
   String[] names = {"col1"};
   int[] ops = {FilterCriteria.GREATER_THAN_OR_EQUAL};
   Comparable[] cutoff = {"c"};
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0],
                                     true));

   // Check data
   assertEquals("number of rows", 3, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
            ((Comparable)ftm.getValueAt(r,c)).compareTo(cutoff[0]) >= 0);
      } // for(c)
   } // for(r)

   // >= "C"
   cutoff[0] = "C";
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0],true));

   // Check data
   assertEquals("number of rows", 8, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
            ((Comparable)ftm.getValueAt(r,c)).compareTo(cutoff[0]) >= 0);
      } // for(c)
   } // for(r)

   // >= "A" (should be all data)
   cutoff[0] = "A";
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0],true));

   // Check data
   assertEquals("number of rows", 10, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
                    ((Comparable)ftm.getValueAt(r,c)).compareTo(cutoff[0]) >= 0);
      } // for(c)
   } // for(r)

   // >= "f" (should be no data)
   cutoff[0] = "f";
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0],true));

   // Check data
   assertEquals("number of rows", 0, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
                    ((Comparable)ftm.getValueAt(r,c)).compareTo(cutoff[0]) >= 0);
      } // for(c)
   } // for(r)

   // Integers
   int[] ints = {-5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5};
   Integer[][] iData = new Integer[11][1];
   for (int i = 0; i < 11; i++)
   {
      iData[i][0] = new Integer(ints[i]);
   }

   dfm = new SimpleTestModel(iData, null, cols);
   ftm = new FilteringTableModel(dfm);

   // >= 0
   cutoff[0] = new Integer(0);
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0],true));

   // Check data
   assertEquals("number of rows", 6, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
            ((Comparable)ftm.getValueAt(r,c)).compareTo(cutoff[0]) >= 0);
      } // for(c)
   } // for(r)

   // >= -2
   cutoff[0] = new Integer(-2);
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0],true));

   // Check data
   assertEquals("number of rows", 8, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
            ((Comparable)ftm.getValueAt(r,c)).compareTo(cutoff[0]) >= 0);
      } // for(c)
   } // for(r)

   // >= -5 (should be all data)
   cutoff[0] = new Integer(-6);
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0],true));

   // Check data
   assertEquals("number of rows", 11, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
            ((Comparable)ftm.getValueAt(r,c)).compareTo(cutoff[0]) >= 0);
      } // for(c)
   } // for(r)

   // >= 6 (should be no data)
   cutoff[0] = new Integer(6);
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0],true));

   // Check data
   assertEquals("number of rows", 0, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
            ((Comparable)ftm.getValueAt(r,c)).compareTo(cutoff[0]) >= 0);
      } // for(c)
   } // for(r)

   // Doubles
   double[] dbls = {-5.5, -4.2, -3.8, -2.6, -1.3, 0.0, 1.9, 2.1, 3.8, 4.7, 5.9};
   Double[][] dData = new Double[11][1];
   for (int i = 0; i < 11; i++)
   {
      dData[i][0] = new Double(dbls[i]);
   }

   dfm = new SimpleTestModel(dData, null, cols);
   ftm = new FilteringTableModel(dfm);

   // >= 0.0
   cutoff[0] = new Double(0.0);
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0],true));

   // Check data
   assertEquals("number of rows", 6, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
            ((Comparable)ftm.getValueAt(r,c)).compareTo(cutoff[0]) >= 0);
      } // for(c)
   } // for(r)

   // >= 1.9
   cutoff[0] = new Double(1.9);
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0],true));

   // Check data
   assertEquals("number of rows", 5, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
            ((Comparable)ftm.getValueAt(r,c)).compareTo(cutoff[0]) >= 0);
      } // for(c)
   } // for(r)

   // >= -5.5 (should be all data)
   cutoff[0] = new Double(-5.6);
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0],true));

   // Check data
   assertEquals("number of rows", 11, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
            ((Comparable)ftm.getValueAt(r,c)).compareTo(cutoff[0]) >= 0);
      } // for(c)
   } // for(r)

   // >= 5.900000001 (should be no data)
   cutoff[0] = new Double(5.900000001);
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0],true));

   // Check data
   assertEquals("number of rows", 0, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
            ((Comparable)ftm.getValueAt(r,c)).compareTo(cutoff[0]) >= 0);
      } // for(c)
   } // for(r)

   // Dates
   TimeZone tz = TimeZone.getTimeZone("EST");

   Calendar cal = Calendar.getInstance(tz);
   cal.set(2000, 0, 1, 0, 0);
   cal.set(Calendar.SECOND, 0);
   cal.set(Calendar.MILLISECOND, 0);
   // These are months in a (zero based, ie: Jan = 0)
   int[] dts = {11, 4, 7, 5, 2, 3, 0, 9, 8, 6, 10, 1};
   Date[][] dtData = new Date[12][1];
   for (int i = 0; i < 12; i++)
   {
      cal.set(Calendar.MONTH, dts[i]);
      dtData[i][0] = cal.getTime();
   }

   dfm = new SimpleTestModel(dtData, null, cols);
   ftm = new FilteringTableModel(dfm);

   // >= Jun. 15, 2000
   cal.set(Calendar.MONTH, 5);
   cal.set(Calendar.DATE, 15);
   cutoff[0] = cal.getTime();
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0],true));

   // Check data
   assertEquals("number of rows", 6, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
            ((Comparable)ftm.getValueAt(r,c)).compareTo(cutoff[0]) >= 0);
      } // for(c)
   } // for(r)

   // >= Feb. 28, 2000
   cal.set(Calendar.MONTH, 1);
   cal.set(Calendar.DATE, 28);
   cutoff[0] = cal.getTime();
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0],true));

   // Check data
   assertEquals("number of rows", 10, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
            ((Comparable)ftm.getValueAt(r,c)).compareTo(cutoff[0]) >= 0);
      } // for(c)
   } // for(r)

   // >= Jan 1, 2000 (should be all data)
   cal.set(Calendar.YEAR, 2000);
   cal.set(Calendar.MONTH, 0);
   cal.set(Calendar.DATE, 1);
   cutoff[0] = cal.getTime();
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0],true));

   // Check data
   assertEquals("number of rows", 12, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
            ((Comparable)ftm.getValueAt(r,c)).compareTo(cutoff[0]) >= 0);
      } // for(c)
   } // for(r)

   // >= Dec. 2, 2000 (should be no data)
   cal.set(Calendar.YEAR, 2000);
   cal.set(Calendar.MONTH, 12);
   cal.set(Calendar.DATE, 1);
   cal.add(Calendar.MILLISECOND, 1);
   cutoff[0] = cal.getTime();
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0],true));

   // Check data
   assertEquals("number of rows", 0, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
            ((Comparable)ftm.getValueAt(r,c)).compareTo(cutoff[0]) >= 0);
      } // for(c)
   } // for(r)
}


/**
 * Test filter with less than for all types.
 */
   public void FIXME_testFilterRowsLessThan()
{
   // Strings
   String[][] strs = {{"a"}, {"A"}, {"b"}, {"B"}, {"c"}, {"C"}, {"d"}, {"D"},
      {"e"}, {"E"}};
   String[][] cols = {{"col1"}};

   SimpleTestModel dfm   = new SimpleTestModel(strs, null, cols);
   FilteringTableModel ftm = new FilteringTableModel(dfm);

   // < "c"
   String[] names = {"col1"};
   int[] ops = {FilterCriteria.LESS_THAN};
   Comparable[] cutoff = {"c"};
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0],true));

   // Check data
   assertEquals("number of rows", 7, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
            ((Comparable)ftm.getValueAt(r,c)).compareTo(cutoff[0]) < 0);
      } // for(c)
   } // for(r)

   // < "C"
   cutoff[0] = "C";
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0],true));

   // Check data
   assertEquals("number of rows", 2, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
            ((Comparable)ftm.getValueAt(r,c)).compareTo(cutoff[0]) < 0);
      } // for(c)
   } // for(r)

   // < "A" (should be no data)
   cutoff[0] = "A";
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0],true));

   // Check data
   assertEquals("number of rows", 0, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
                    ((Comparable)ftm.getValueAt(r,c)).compareTo(cutoff[0]) < 0);
      } // for(c)
   } // for(r)

   // < "f" (should be all data)
   cutoff[0] = "f";
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0],true));

   // Check data
   assertEquals("number of rows", 10, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
                    ((Comparable)ftm.getValueAt(r,c)).compareTo(cutoff[0]) < 0);
      } // for(c)
   } // for(r)

   // Integers
   int[] ints = {-5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5};
   Integer[][] iData = new Integer[11][1];
   for (int i = 0; i < 11; i++)
   {
      iData[i][0] = new Integer(ints[i]);
   }

   dfm = new SimpleTestModel(iData, null, cols);
   ftm = new FilteringTableModel(dfm);

   // < 0
   cutoff[0] = new Integer(0);
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0],true));

   // Check data
   assertEquals("number of rows", 5, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
            ((Comparable)ftm.getValueAt(r,c)).compareTo(cutoff[0]) < 0);
      } // for(c)
   } // for(r)

   // < -2
   cutoff[0] = new Integer(-2);
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0],true));

   // Check data
   assertEquals("number of rows", 3, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
            ((Comparable)ftm.getValueAt(r,c)).compareTo(cutoff[0]) < 0);
      } // for(c)
   } // for(r)

   // < -5 (should be no data)
   cutoff[0] = new Integer(-5);
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0],true));

   // Check data
   assertEquals("number of rows", 0, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
            ((Comparable)ftm.getValueAt(r,c)).compareTo(cutoff[0]) < 0);
      } // for(c)
   } // for(r)

   // < 6 (should be no data)
   cutoff[0] = new Integer(6);
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0],true));

   // Check data
   assertEquals("number of rows", 11, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
            ((Comparable)ftm.getValueAt(r,c)).compareTo(cutoff[0]) < 0);
      } // for(c)
   } // for(r)

   // Doubles
   double[] dbls = {-5.5, -4.2, -3.8, -2.6, -1.3, 0.0, 1.9, 2.1, 3.8, 4.7, 5.9};
   Double[][] dData = new Double[11][1];
   for (int i = 0; i < 11; i++)
   {
      dData[i][0] = new Double(dbls[i]);
   }

   dfm = new SimpleTestModel(dData, null, cols);
   ftm = new FilteringTableModel(dfm);

   // < 0.0
   cutoff[0] = new Double(0.0);
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0],true));

   // Check data
   assertEquals("number of rows", 5, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
            ((Comparable)ftm.getValueAt(r,c)).compareTo(cutoff[0]) < 0);
      } // for(c)
   } // for(r)

   // < 1.9
   cutoff[0] = new Double(1.9);
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0],true));

   // Check data
   assertEquals("number of rows", 6, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
            ((Comparable)ftm.getValueAt(r,c)).compareTo(cutoff[0]) < 0);
      } // for(c)
   } // for(r)

   // < -5.5 (should be no data)
   cutoff[0] = new Double(-5.5);
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0],true));

   // Check data
   assertEquals("number of rows", 0, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
            ((Comparable)ftm.getValueAt(r,c)).compareTo(cutoff[0]) < 0);
      } // for(c)
   } // for(r)

   // < 5.900000001 (should be all data)
   cutoff[0] = new Double(5.900000001);
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0],true));

   // Check data
   assertEquals("number of rows", 11, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
            ((Comparable)ftm.getValueAt(r,c)).compareTo(cutoff[0]) < 0);
      } // for(c)
   } // for(r)

   // Dates
   TimeZone tz = TimeZone.getTimeZone("EST");

   Calendar cal = Calendar.getInstance(tz);
   cal.set(2000, 0, 1, 0, 0);
   cal.set(Calendar.SECOND, 0);
   cal.set(Calendar.MILLISECOND, 0);
   // These are months in a (zero based, ie: Jan = 0)
   int[] dts = {11, 4, 7, 5, 2, 3, 0, 9, 8, 6, 10, 1};
   Date[][] dtData = new Date[12][1];
   for (int i = 0; i < 12; i++)
   {
      cal.set(Calendar.MONTH, dts[i]);
      dtData[i][0] = cal.getTime();
   }

   dfm = new SimpleTestModel(dtData, null, cols);
   ftm = new FilteringTableModel(dfm);

   // < Jun. 15, 2000
   cal.set(Calendar.MONTH, 5);
   cal.set(Calendar.DATE, 15);
   cutoff[0] = cal.getTime();
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0],true));

   // Check data
   assertEquals("number of rows", 6, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
            ((Comparable)ftm.getValueAt(r,c)).compareTo(cutoff[0]) < 0);
      } // for(c)
   } // for(r)

   // < Feb. 28, 2000
   cal.set(Calendar.MONTH, 1);
   cal.set(Calendar.DATE, 28);
   cutoff[0] = cal.getTime();
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0],true));

   // Check data
   assertEquals("number of rows", 2, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
            ((Comparable)ftm.getValueAt(r,c)).compareTo(cutoff[0]) < 0);
      } // for(c)
   } // for(r)

   // < Jan 1, 2000 (should be no data)
   cal.set(Calendar.YEAR, 2000);
   cal.set(Calendar.MONTH, 0);
   cal.set(Calendar.DATE, 1);
   cutoff[0] = cal.getTime();
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0],true));

   // Check data
   assertEquals("number of rows", 0, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
            ((Comparable)ftm.getValueAt(r,c)).compareTo(cutoff[0]) < 0);
      } // for(c)
   } // for(r)

   // < Dec. 2, 2000 (should be all data)
   cal.set(Calendar.YEAR, 2000);
   cal.set(Calendar.MONTH, 12);
   cal.set(Calendar.DATE, 1);
   cal.add(Calendar.MILLISECOND, 1);
   cutoff[0] = cal.getTime();
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0],true));

   // Check data
   assertEquals("number of rows", 12, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
            ((Comparable)ftm.getValueAt(r,c)).compareTo(cutoff[0]) < 0);
      } // for(c)
   } // for(r)
} // testFilterLessThan()


/**
 * Test filter with less or equal than for all types.
 */
   public void FIXME_testFilterRowsLessThanOrEqual()
{
   // Strings
   String[][] strs = {{"a"}, {"A"}, {"b"}, {"B"}, {"c"}, {"C"}, {"d"}, {"D"},
      {"e"}, {"E"}};
   String[][] cols = {{"col1"}};

   SimpleTestModel dfm   = new SimpleTestModel(strs, null, cols);
   FilteringTableModel ftm = new FilteringTableModel(dfm);

   // <= "c"
   String[] names = {"col1"};
   int[] ops = {FilterCriteria.LESS_THAN_OR_EQUAL};
   Comparable[] cutoff = {"c"};
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0],true));

   // Check data
   assertEquals("number of rows", 8, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
            ((Comparable)ftm.getValueAt(r,c)).compareTo(cutoff[0]) <= 0);
      } // for(c)
   } // for(r)

   // <= "C"
   cutoff[0] = "C";
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0],true));

   // Check data
   assertEquals("number of rows", 3, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
            ((Comparable)ftm.getValueAt(r,c)).compareTo(cutoff[0]) <= 0);
      } // for(c)
   } // for(r)

   // <= "@" (should be no data)
   cutoff[0] = "@";
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0],true));

   // Check data
   assertEquals("number of rows", 0, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
                    ((Comparable)ftm.getValueAt(r,c)).compareTo(cutoff[0]) <= 0);
      } // for(c)
   } // for(r)

   // <= "e" (should be all data)
   cutoff[0] = "e";
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0],true));

   // Check data
   assertEquals("number of rows", 10, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
                    ((Comparable)ftm.getValueAt(r,c)).compareTo(cutoff[0]) <= 0);
      } // for(c)
   } // for(r)

   // Integers
   int[] ints = {-5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5};
   Integer[][] iData = new Integer[11][1];
   for (int i = 0; i < 11; i++)
   {
      iData[i][0] = new Integer(ints[i]);
   }

   dfm = new SimpleTestModel(iData, null, cols);
   ftm = new FilteringTableModel(dfm);

   // <= 0
   cutoff[0] = new Integer(0);
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0],true));

   // Check data
   assertEquals("number of rows", 6, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
            ((Comparable)ftm.getValueAt(r,c)).compareTo(cutoff[0]) <= 0);
      } // for(c)
   } // for(r)

   // <= -2
   cutoff[0] = new Integer(-2);
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0],true));

   // Check data
   assertEquals("number of rows", 4, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
            ((Comparable)ftm.getValueAt(r,c)).compareTo(cutoff[0]) <= 0);
      } // for(c)
   } // for(r)


   // <= -6 (should be no data)
   cutoff[0] = new Integer(-6);
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0],true));

   // Check data
   assertEquals("number of rows", 0, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
            ((Comparable)ftm.getValueAt(r,c)).compareTo(cutoff[0]) <= 0);
      } // for(c)
   } // for(r)

   // <= 5 (should be all data)
   cutoff[0] = new Integer(5);
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0],true));

   // Check data
   assertEquals("number of rows", 11, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
            ((Comparable)ftm.getValueAt(r,c)).compareTo(cutoff[0]) <= 0);
      } // for(c)
   } // for(r)

   // Doubles
   double[] dbls = {-5.5, -4.2, -3.8, -2.6, -1.3, 0.0, 1.9, 2.1, 3.8, 4.7, 5.9};
   Double[][] dData = new Double[11][1];
   for (int i = 0; i < 11; i++)
   {
      dData[i][0] = new Double(dbls[i]);
   }

   dfm = new SimpleTestModel(dData, null, cols);
   ftm = new FilteringTableModel(dfm);

   // <= 0.0
   cutoff[0] = new Double(0.0);
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0],true));

   // Check data
   assertEquals("number of rows", 6, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
            ((Comparable)ftm.getValueAt(r,c)).compareTo(cutoff[0]) <= 0);
      } // for(c)
   } // for(r)

   // <= 1.9
   cutoff[0] = new Double(1.9);
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0],true));

   // Check data
   assertEquals("number of rows", 7, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
            ((Comparable)ftm.getValueAt(r,c)).compareTo(cutoff[0]) <= 0);
      } // for(c)
   } // for(r)

   // <= -5.5000001 (should be no data)
   cutoff[0] = new Double(-5.5000001);
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0],true));

   // Check data
   assertEquals("number of rows", 0, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
            ((Comparable)ftm.getValueAt(r,c)).compareTo(cutoff[0]) <= 0);
      } // for(c)
   } // for(r)

   // <= 5.9 (should be all data)
   cutoff[0] = new Double(5.9);
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0],true));

   // Check data
   assertEquals("number of rows", 11, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
            ((Comparable)ftm.getValueAt(r,c)).compareTo(cutoff[0]) <= 0);
      } // for(c)
   } // for(r)

   // Dates
   TimeZone tz = TimeZone.getTimeZone("EST");

   Calendar cal = Calendar.getInstance(tz);
   cal.set(2000, 0, 1, 0, 0);
   cal.set(Calendar.SECOND, 0);
   cal.set(Calendar.MILLISECOND, 0);
   // These are months in a (zero based, ie: Jan = 0)
   int[] dts = {11, 4, 7, 5, 2, 3, 0, 9, 8, 6, 10, 1};
   Date[][] dtData = new Date[12][1];
   for (int i = 0; i < 12; i++)
   {
      cal.set(Calendar.MONTH, dts[i]);
      dtData[i][0] = cal.getTime();
   }

   dfm = new SimpleTestModel(dtData, null, cols);
   ftm = new FilteringTableModel(dfm);

   // <= Jun. 15, 2000
   cal.set(Calendar.MONTH, 5);
   cal.set(Calendar.DATE, 15);
   cutoff[0] = cal.getTime();
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0],true));

   // Check data
   assertEquals("number of rows", 6, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
            ((Comparable)ftm.getValueAt(r,c)).compareTo(cutoff[0]) <= 0);
      } // for(c)
   } // for(r)

   // <= Feb. 28, 2000
   cal.set(Calendar.MONTH, 1);
   cal.set(Calendar.DATE, 28);
   cutoff[0] = cal.getTime();
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0],true));

   // Check data
   assertEquals("number of rows", 2, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
            ((Comparable)ftm.getValueAt(r,c)).compareTo(cutoff[0]) <= 0);
      } // for(c)
   } // for(r)

   // <= Dec 31, 1999 (should be no data)
   cal.set(Calendar.YEAR, 2000);
   cal.set(Calendar.MONTH, 0);
   cal.set(Calendar.DATE, 1);
   cal.add(Calendar.MILLISECOND, -1);
   cutoff[0] = cal.getTime();
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0],true));

   // Check data
   assertEquals("number of rows", 0, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
            ((Comparable)ftm.getValueAt(r,c)).compareTo(cutoff[0]) <= 0);
      } // for(c)
   } // for(r)

   // <= Dec. 1, 2000 (should be all data)
   cal.set(Calendar.YEAR, 2000);
   cal.set(Calendar.MONTH, 12);
   cal.set(Calendar.DATE, 1);
   cutoff[0] = cal.getTime();
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0],true));

   // Check data
   assertEquals("number of rows", 12, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
            ((Comparable)ftm.getValueAt(r,c)).compareTo(cutoff[0]) <= 0);
      } // for(c)
   } // for(r)
} // testFilterLessThanOrEqual()


/**
 * Test filter with equal for all types.
 */
  public void FIXME_testFilterRowsEqual()
{
   // Strings
   String[][] strs = {{"a"}, {"A"}, {"b"}, {"B"}, {"c"}, {"C"}, {"d"}, {"D"},
      {"e"}, {"E"}};
   String[][] cols = {{"col1"}};

   SimpleTestModel dfm   = new SimpleTestModel(strs, null, cols);
   FilteringTableModel ftm = new FilteringTableModel(dfm);

   // = "c"
   String[] names = {"col1"};
   int[] ops = {FilterCriteria.EQUAL};
   Comparable[] cutoff = {"c"};
   ftm.filterRows(new FilterCriteria(names, ops ,cutoff,cols[0],true));

   // Check data
   assertEquals("number of rows", 1, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
            ((Comparable)ftm.getValueAt(r,c)).equals(cutoff[0]));
      } // for(c)
   } // for(r)


   // = "C"
   cutoff[0] = "C";
   ftm.filterRows(new FilterCriteria(names, ops ,cutoff, cols[0],true));

   // Check data
   assertEquals("number of rows", 1, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
            ((Comparable)ftm.getValueAt(r,c)).equals(cutoff[0]));
      } // for(c)
   } // for(r)

   // = "@" (should be no data)
   cutoff[0] = "@";
   ftm.filterRows(new FilterCriteria(names, ops ,cutoff, cols[0],true));

   // Check data
   assertEquals("number of rows", 0, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
                    ((Comparable)ftm.getValueAt(r,c)).equals(cutoff[0]));
      } // for(c)
   } // for(r)

   // = "f" (should be no data)
   cutoff[0] = "f";
   ftm.filterRows(new FilterCriteria(names, ops ,cutoff, cols[0],true));

   // Check data
   assertEquals("number of rows", 0, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
                    ((Comparable)ftm.getValueAt(r,c)).equals(cutoff[0]));
      } // for(c)
   } // for(r)

   // Integers
   int[] ints = {-5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5};
   Integer[][] iData = new Integer[11][1];
   for (int i = 0; i < 11; i++)
   {
      iData[i][0] = new Integer(ints[i]);
   }

   dfm = new SimpleTestModel(iData, null, cols);
   ftm = new FilteringTableModel(dfm);

   // = 0
   cutoff[0] = new Integer(0);
   ftm.filterRows(new FilterCriteria(names, ops ,cutoff, cols[0],true));

   // Check data
   assertEquals("number of rows", 1, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
            ((Comparable)ftm.getValueAt(r,c)).equals(cutoff[0]));
      } // for(c)
   } // for(r)

   // = -2
   cutoff[0] = new Integer(-2);
   ftm.filterRows(new FilterCriteria(names, ops ,cutoff, cols[0],true));

   // Check data
   assertEquals("number of rows", 1, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
            ((Comparable)ftm.getValueAt(r,c)).equals(cutoff[0]));
      } // for(c)
   } // for(r)

   // = -6 (should be no data)
   cutoff[0] = new Integer(-6);
   ftm.filterRows(new FilterCriteria(names, ops ,cutoff, cols[0],true));

   // Check data
   assertEquals("number of rows", 0, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
            ((Comparable)ftm.getValueAt(r,c)).equals(cutoff[0]));
      } // for(c)
   } // for(r)

   // = 6 (should be no data)
   cutoff[0] = new Integer(6);
   ftm.filterRows(new FilterCriteria(names, ops ,cutoff, cols[0],true));

   // Check data
   assertEquals("number of rows", 0, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
            ((Comparable)ftm.getValueAt(r,c)).equals(cutoff[0]));
      } // for(c)
   } // for(r)

   // Doubles
   double[] dbls = {-5.5, -4.2, -3.8, -2.6, -1.3, 0.0, 1.9, 2.1, 3.8, 4.7, 5.9};
   Double[][] dData = new Double[11][1];
   for (int i = 0; i < 11; i++)
   {
      dData[i][0] = new Double(dbls[i]);
   }

   dfm = new SimpleTestModel(dData, null, cols);
   ftm = new FilteringTableModel(dfm);

   // = 0.0
   cutoff[0] = new Double(0.0);
   ftm.filterRows(new FilterCriteria(names, ops ,cutoff, cols[0],true));

   // Check data
   assertEquals("number of rows", 1, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
            ((Comparable)ftm.getValueAt(r,c)).equals(cutoff[0]));
      } // for(c)
   } // for(r)

   // = 1.9
   cutoff[0] = new Double(1.9);
   ftm.filterRows(new FilterCriteria(names, ops ,cutoff, cols[0],true));

   // Check data
   assertEquals("number of rows", 1, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
            ((Comparable)ftm.getValueAt(r,c)).equals(cutoff[0]));
      } // for(c)
   } // for(r)

   // = -5.5000001 (should be no data)
   cutoff[0] = new Double(-5.5000001);
   ftm.filterRows(new FilterCriteria(names, ops ,cutoff, cols[0],true));

   // Check data
   assertEquals("number of rows", 0, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
            ((Comparable)ftm.getValueAt(r,c)).equals(cutoff[0]));
      } // for(c)
   } // for(r)

   // = 5.9000001 (should be no data)
   cutoff[0] = new Double(5.9000001);
   ftm.filterRows(new FilterCriteria(names, ops ,cutoff, cols[0],true));

   // Check data
   assertEquals("number of rows", 0, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
            ((Comparable)ftm.getValueAt(r,c)).equals(cutoff[0]));
      } // for(c)
   } // for(r)

   // Dates
   TimeZone tz = TimeZone.getTimeZone("EST");

   Calendar cal = Calendar.getInstance(tz);
   cal.set(2000, 0, 1, 0, 0);
   cal.set(Calendar.SECOND, 0);
   cal.set(Calendar.MILLISECOND, 0);
   // These are months in a (zero based, ie: Jan = 0)
   int[] dts = {11, 4, 7, 5, 2, 3, 0, 9, 8, 6, 10, 1};
   Date[][] dtData = new Date[12][1];
   for (int i = 0; i < 12; i++)
   {
      cal.set(Calendar.MONTH, dts[i]);
      dtData[i][0] = cal.getTime();
   }

   dfm = new SimpleTestModel(dtData, null, cols);
   ftm = new FilteringTableModel(dfm);

   // = Jun. 1, 2000
   cal.set(Calendar.MONTH, 5);
   cal.set(Calendar.DATE, 1);
   cutoff[0] = cal.getTime();
   ftm.filterRows(new FilterCriteria(names, ops ,cutoff, cols[0],true));

   // Check data
   assertEquals("number of rows", 1, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
            ((Comparable)ftm.getValueAt(r,c)).equals(cutoff[0]));
      } // for(c)
   } // for(r)

   // = Feb. 1, 2000
   cal.set(Calendar.MONTH, 1);
   cal.set(Calendar.DATE, 1);
   cutoff[0] = cal.getTime();
   ftm.filterRows(new FilterCriteria(names, ops ,cutoff, cols[0],true));

   // Check data
   assertEquals("number of rows", 1, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
            ((Comparable)ftm.getValueAt(r,c)).equals(cutoff[0]));
      } // for(c)
   } // for(r)

   // = Dec 31, 1999 (should be no data)
   cal.set(Calendar.YEAR, 2000);
   cal.set(Calendar.MONTH, 0);
   cal.set(Calendar.DATE, 1);
   cal.add(Calendar.MILLISECOND, -1);
   cutoff[0] = cal.getTime();
   ftm.filterRows(new FilterCriteria(names, ops ,cutoff, cols[0],true));

   // Check data
   assertEquals("number of rows", 0, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
            ((Comparable)ftm.getValueAt(r,c)).equals(cutoff[0]));
      } // for(c)
   } // for(r)

   // = Jan. 1, 2001 (should be no data)
   cal.set(Calendar.YEAR, 2000);
   cal.set(Calendar.MONTH, 12);
   cal.set(Calendar.DATE, 1);
   cal.set(Calendar.MILLISECOND, 1);
   cutoff[0] = cal.getTime();
   ftm.filterRows(new FilterCriteria(names, ops ,cutoff, cols[0],true));

   // Check data
   assertEquals("number of rows", 0, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
            ((Comparable)ftm.getValueAt(r,c)).equals(cutoff[0]));
      } // for(c)
   } // for(r)
} // tesrFilterRowsEqual()


/**
 * Test filter with not equal for all types.
 */
   public void FIXME_testFilterRowsNotEqual()
{
   // Strings
   String[][] strs = {{"a"}, {"A"}, {"b"}, {"B"}, {"c"}, {"C"}, {"d"}, {"D"},
      {"e"}, {"E"}};
   String[][] cols = {{"col1"}};

   SimpleTestModel dfm   = new SimpleTestModel(strs, null, cols);
   FilteringTableModel ftm = new FilteringTableModel(dfm);

   // != "c"
   String[] names = {"col1"};
   int[] ops = {FilterCriteria.NOT_EQUAL};
   Comparable[] cutoff = {"c"};
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0],true));

   // Check data
   assertEquals("number of rows", 9, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
            !((Comparable)ftm.getValueAt(r,c)).equals(cutoff[0]));
      } // for(c)
   } // for(r)

   // != "C"
   cutoff[0] = "C";
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0],true));

   // Check data
   assertEquals("number of rows", 9, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
            !((Comparable)ftm.getValueAt(r,c)).equals(cutoff[0]));
      } // for(c)
   } // for(r)

   // != "@" (should be all data)
   cutoff[0] = "@";
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0],true));

   // Check data
   assertEquals("number of rows", 10, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
                    !((Comparable)ftm.getValueAt(r,c)).equals(cutoff[0]));
      } // for(c)
   } // for(r)

   // != "f" (should be all data)
   cutoff[0] = "f";
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0],true));

   // Check data
   assertEquals("number of rows", 10, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
                    !((Comparable)ftm.getValueAt(r,c)).equals(cutoff[0]));
      } // for(c)
   } // for(r)

   // Integers
   int[] ints = {-5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5};
   Integer[][] iData = new Integer[11][1];
   for (int i = 0; i < 11; i++)
   {
      iData[i][0] = new Integer(ints[i]);
   }

   dfm = new SimpleTestModel(iData, null, cols);
   ftm = new FilteringTableModel(dfm);

   // != 0
   cutoff[0] = new Integer(0);
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0],true));

   // Check data
   assertEquals("number of rows", 10, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
            !((Comparable)ftm.getValueAt(r,c)).equals(cutoff[0]));
      } // for(c)
   } // for(r)

   // != -2
   cutoff[0] = new Integer(-2);
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0],true));

   // Check data
   assertEquals("number of rows", 10, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
            !((Comparable)ftm.getValueAt(r,c)).equals(cutoff[0]));
      } // for(c)
   } // for(r)

   // != -6 (should be all data)
   cutoff[0] = new Integer(-6);
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0],true));

   // Check data
   assertEquals("number of rows", 11, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
            !((Comparable)ftm.getValueAt(r,c)).equals(cutoff[0]));
      } // for(c)
   } // for(r)

   // != 6 (should be all data)
   cutoff[0] = new Integer(6);
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0],true));

   // Check data
   assertEquals("number of rows", 11, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
            !((Comparable)ftm.getValueAt(r,c)).equals(cutoff[0]));
      } // for(c)
   } // for(r)

   // Doubles
   double[] dbls = {-5.5, -4.2, -3.8, -2.6, -1.3, 0.0, 1.9, 2.1, 3.8, 4.7, 5.9};
   Double[][] dData = new Double[11][1];
   for (int i = 0; i < 11; i++)
   {
      dData[i][0] = new Double(dbls[i]);
   }

   dfm = new SimpleTestModel(dData, null, cols);
   ftm = new FilteringTableModel(dfm);

   // != 0.0
   cutoff[0] = new Double(0.0);
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0],true));

   // Check data
   assertEquals("number of rows", 10, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
            !((Comparable)ftm.getValueAt(r,c)).equals(cutoff[0]));
      } // for(c)
   } // for(r)

   // != 1.9
   cutoff[0] = new Double(1.9);
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0],true));

   // Check data
   assertEquals("number of rows", 10, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
            !((Comparable)ftm.getValueAt(r,c)).equals(cutoff[0]));
      } // for(c)
   } // for(r)

   // != -5.5000001 (should be all data)
   cutoff[0] = new Double(-5.5000001);
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0],true));

   // Check data
   assertEquals("number of rows", 11, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
            !((Comparable)ftm.getValueAt(r,c)).equals(cutoff[0]));
      } // for(c)
   } // for(r)

   // != 5.9000001 (should be all data)
   cutoff[0] = new Double(5.9000001);
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0],true));

   // Check data
   assertEquals("number of rows", 11, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
            !((Comparable)ftm.getValueAt(r,c)).equals(cutoff[0]));
      } // for(c)
   } // for(r)

   // Dates
   TimeZone tz = TimeZone.getTimeZone("EST");

   Calendar cal = Calendar.getInstance(tz);
   cal.set(2000, 0, 1, 0, 0);
   cal.set(Calendar.SECOND, 0);
   cal.set(Calendar.MILLISECOND, 0);
   // These are months in a (zero based, ie: Jan = 0)
   int[] dts = {11, 4, 7, 5, 2, 3, 0, 9, 8, 6, 10, 1};
   Date[][] dtData = new Date[12][1];
   for (int i = 0; i < 12; i++)
   {
      cal.set(Calendar.MONTH, dts[i]);
      dtData[i][0] = cal.getTime();
   }

   dfm = new SimpleTestModel(dtData, null, cols);
   ftm = new FilteringTableModel(dfm);

   // != Jun. 1, 2000
   cal.set(Calendar.MONTH, 5);
   cal.set(Calendar.DATE, 1);
   cutoff[0] = cal.getTime();
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0],true));

   // Check data
   assertEquals("number of rows", 11, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
            !((Comparable)ftm.getValueAt(r,c)).equals(cutoff[0]));
      } // for(c)
   } // for(r)

   // != Feb. 1, 2000
   cal.set(Calendar.MONTH, 1);
   cal.set(Calendar.DATE, 1);
   cutoff[0] = cal.getTime();
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0],true));

   // Check data
   assertEquals("number of rows", 11, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
            !((Comparable)ftm.getValueAt(r,c)).equals(cutoff[0]));
      } // for(c)
   } // for(r)

   // != Dec 31, 1999 (should be all data)
   cal.set(Calendar.YEAR, 2000);
   cal.set(Calendar.MONTH, 0);
   cal.set(Calendar.DATE, 1);
   cal.add(Calendar.MILLISECOND, -1);
   cutoff[0] = cal.getTime();
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0],true));

   // Check data
   assertEquals("number of rows", 12, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
            !((Comparable)ftm.getValueAt(r,c)).equals(cutoff[0]));
      } // for(c)
   } // for(r)

   // != Jan. 1, 2001 (should be all data)
   cal.set(Calendar.YEAR, 2000);
   cal.set(Calendar.MONTH, 12);
   cal.set(Calendar.DATE, 1);
   cal.set(Calendar.MILLISECOND, 1);
   cutoff[0] = cal.getTime();
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0],true));

   // Check data
   assertEquals("number of rows", 12, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertTrue(ftm.getValueAt(r,c) + " > " + cutoff,
            !((Comparable)ftm.getValueAt(r,c)).equals(cutoff[0]));
      } // for(c)
   } // for(r)
} // testFilterRowsNotEqual()


/**
 * Test filtering with "Starts with".
 */
   public void FIXME_testStartsWith()
{
   // Strings
   String[][] strs = {{"aardvark"}, {"animal"}, {"Brother"}, {"Bovine"},
      {"cat"}, {"Calico"}, {"dog"}, {"Doberman"},
      {"edge"}, {"elephant"}};
   String[][] cols = {{"col1"}};

   SimpleTestModel dfm   = new SimpleTestModel(strs, null, cols);
   FilteringTableModel ftm = new FilteringTableModel(dfm);

   // "a"
   String[] names = {"col1"};
   int[] ops = {FilterCriteria.STARTS_WITH};
   Comparable[] cutoff = {"c"};
   Format[] formats = {new NullFormatter()};
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0], true));

   // Check data
   int[] correct = {4};
   assertEquals("number of rows", 1, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertEquals(ftm.getValueAt(r,c) + " starts with " + ftm.getValueAt(r,c),
            strs[correct[r]][c], ftm.getValueAt(r,c));
      } // for(c)
   } // for(r)

   // "B"
   cutoff[0] = "B";
   correct = new int[]{2, 3};
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0],true));

   // Check data
   assertEquals("number of rows", 2, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertEquals(ftm.getValueAt(r,c) + " starts with " + ftm.getValueAt(r,c),
            strs[correct[r]][c], ftm.getValueAt(r,c));
      } // for(c)
   } // for(r)

   // "d"
   cutoff[0] = "d";
   correct = new int[] {6};
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0],true));

   // Check data
   assertEquals("number of rows", 1, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertEquals(ftm.getValueAt(r,c) + " starts with " + ftm.getValueAt(r,c),
            strs[correct[r]][c], ftm.getValueAt(r,c));
      } // for(c)
   } // for(r)

   // "z" (should be no data)
   cutoff[0] = "z";
   correct = new int[0];
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0],true));

   // Check data
   assertEquals("number of rows", correct.length, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertEquals(ftm.getValueAt(r,c) + " starts with " + ftm.getValueAt(r,c),
            strs[correct[r]][c], ftm.getValueAt(r,c));
      } // for(c)
   } // for(r)

   // Integers
   int[] ints = {-5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 20, 200, 2000};
   formats[0] = new DecimalFormat("0");
   Integer[][] iData = new Integer[ints.length][1];
   for (int i = 0; i < ints.length; i++)
   {
      iData[i][0] = new Integer(ints[i]);
   }

   dfm = new SimpleTestModel(iData, null, cols);
   ftm = new FilteringTableModel(dfm);

   // '-'
   cutoff[0] = "-";
   correct = new int[] {0, 1, 2, 3, 4};
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0],true));

   // Check data
   assertEquals("number of rows", correct.length, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertEquals(ftm.getValueAt(r,c) + " starts with " + ftm.getValueAt(r,c),
            new Integer(ints[correct[r]]), ftm.getValueAt(r,c));
      } // for(c)
   } // for(r)

   // '2'
   cutoff[0] = "2";
   correct = new int[] {7, 11, 12, 13};
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0],true));

   // Check data
   assertEquals("number of rows", correct.length, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertEquals(ftm.getValueAt(r,c) + " starts with " + ftm.getValueAt(r,c),
            new Integer(ints[correct[r]]), ftm.getValueAt(r,c));
      } // for(c)
   } // for(r)

   // '3'
   cutoff[0] = "3";
   correct = new int[] {8};
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0],true));

   // Check data
   assertEquals("number of rows", correct.length, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertEquals(ftm.getValueAt(r,c) + " starts with " + ftm.getValueAt(r,c),
            new Integer(ints[correct[r]]), ftm.getValueAt(r,c));
      } // for(c)
   } // for(r)

   // '9' (should be no data)
   cutoff[0] = "9";
   correct = new int[0];
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0],true));

   // Check data
   assertEquals("number of rows", correct.length, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertEquals(ftm.getValueAt(r,c) + " starts with " + ftm.getValueAt(r,c),
            new Integer(ints[correct[r]]), ftm.getValueAt(r,c));
      } // for(c)
   } // for(r)


   // Doubles
   double[] dbls = {-5.5, -4.2, -3.8, -2.6, -1.3, 0.0, 1.9, 2.1, 3.8, 4.7, 5.9, 500.05, 5000.2};
   formats[0] = new DecimalFormat("0.00");
   Double[][] dData = new Double[dbls.length][1];
   for (int i = 0; i < dbls.length; i++)
   {
      dData[i][0] = new Double(dbls[i]);
   }

   dfm = new SimpleTestModel(dData, null, cols);
   ftm = new FilteringTableModel(dfm);

   // '0'
   cutoff[0] = "0";
   correct = new int[] {5};
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0],true));

   // Check data
   assertEquals("number of rows", correct.length, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertEquals(ftm.getValueAt(r,c) + " starts with " + ftm.getValueAt(r,c),
            new Double(dbls[correct[r]]), ftm.getValueAt(r,c));
      } // for(c)
   } // for(r)

   // '-'
   cutoff[0] = "-";
   correct = new int[] {0, 1, 2, 3, 4};
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0],true));

   // Check data
   assertEquals("number of rows", correct.length, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertEquals(ftm.getValueAt(r,c) + " starts with " + ftm.getValueAt(r,c),
            new Double(dbls[correct[r]]), ftm.getValueAt(r,c));
      } // for(c)
   } // for(r)

   // '5'
   cutoff[0] = "5";
   correct = new int[] {10, 11, 12};
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0], true));

   // Check data
   assertEquals("number of rows", correct.length, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertEquals(ftm.getValueAt(r,c) + " starts with " + ftm.getValueAt(r,c),
            new Double(dbls[correct[r]]), ftm.getValueAt(r,c));
      } // for(c)
   } // for(r)

   // '9' (should be no data)
   cutoff[0] = "9";
   correct = new int[0];
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0], true));

   // Check data
   assertEquals("number of rows", correct.length, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertEquals(ftm.getValueAt(r,c) + " starts with " + ftm.getValueAt(r,c),
            new Double(dbls[correct[r]]), ftm.getValueAt(r,c));
      } // for(c)
   } // for(r)

   // Dates
   TimeZone tz = TimeZone.getTimeZone("EST");

   Calendar cal = Calendar.getInstance(tz);
   formats[0] = new SimpleDateFormat("MM/dd/yyyy");
   cal.set(2000, 0, 1, 0, 0);
   cal.set(Calendar.SECOND, 0);
   cal.set(Calendar.MILLISECOND, 0);
   // These are months in a (zero based, ie: Jan = 0)
   int[] dts = {11, 4, 7, 5, 2, 3, 0, 9, 8, 6, 10, 1};
   Date[][] dtData = new Date[12][1];
   for (int i = 0; i < 12; i++)
   {
      cal.set(Calendar.MONTH, dts[i]);
      dtData[i][0] = cal.getTime();
   }

   dfm = new SimpleTestModel(dtData, null, cols);
   ftm = new FilteringTableModel(dfm);

   // '1'
   cutoff[0] = "1";
   correct = new int[] {0, 7, 10};
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0], true));

   // Check data
   assertEquals("number of rows", correct.length, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertEquals(formats[0].format(ftm.getValueAt(r,c)) +
            " starts with " + cutoff[0], dtData[correct[r]][c],
            ftm.getValueAt(r,c));
      } // for(c)
   } // for(r)

   // '0'
   cutoff[0] = "0";
   correct = new int[] {1, 2, 3, 4, 5, 6, 8, 9, 11};
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0],true));

   // Check data
   assertEquals("number of rows", correct.length, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertEquals(formats[0].format(ftm.getValueAt(r,c)) +
            " starts with " + cutoff[0], dtData[correct[r]][c],
            ftm.getValueAt(r,c));
      } // for(c)
   } // for(r)

   // 'a' (should be no data)
   cutoff[0] = "a";
   correct = new int[0];
   ftm.filterRows(new FilterCriteria(names, ops, cutoff ,cols[0] ,true));

   // Check data
   assertEquals("number of rows", correct.length, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertEquals(formats[0].format(ftm.getValueAt(r,c)) +
            " starts with " + cutoff[0], dtData[correct[r]][c],
            ftm.getValueAt(r,c));
      } // for(c)
   } // for(r)

   // "Mon"
   formats[0] = new SimpleDateFormat("EEE MM/dd/yyyy");
   cutoff[0] = "Mon";
   correct = new int[] {1};
   ftm.filterRows(new FilterCriteria(names, ops, cutoff, cols[0],true));

   // Check data
   assertEquals("number of rows", correct.length, ftm.getRowCount());
   for (int r = 0; r < ftm.getRowCount(); r++)
   {
      for(int c = 0; c < ftm.getColumnCount(); c++)
      {
         assertEquals(formats[0].format(ftm.getValueAt(r,c)) +
            " starts with " + cutoff[0], dtData[correct[r]][c],
            ftm.getValueAt(r,c));
      } // for(c)
   } // for(r)
}


}
