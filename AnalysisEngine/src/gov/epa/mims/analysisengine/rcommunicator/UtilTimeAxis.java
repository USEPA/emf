package gov.epa.mims.analysisengine.rcommunicator;

import gov.epa.mims.analysisengine.AnalysisEngineConstants;
import gov.epa.mims.analysisengine.tree.Axis;
import gov.epa.mims.analysisengine.tree.DateDataSetIfc;
import gov.epa.mims.analysisengine.tree.Text;
import gov.epa.mims.analysisengine.tree.TimeSeries;

import java.text.SimpleDateFormat;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;


/**
 * DOCUMENT ME!
 *
 * @version $Revision: 1.2 $
 * @author $author$
 */
public class UtilTimeAxis implements Serializable, Cloneable,
gov.epa.mims.analysisengine.tree.AnalysisOptionConstantsIfc
{
   /** DOCUMENT ME! */
   static final long serialVersionUID = 1;

   /**
    * DOCUMENT_ME
    *
    * @param p DOCUMENT_ME
    * @param side DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   public static String getTickLabelFormat(TimeSeries p, int side)
   {
      Axis axis;

      if (side == 1)
      {
         axis = (Axis) p.getOption(X_TIME_AXIS);
      }
      else
      {
         axis = (Axis) p.getOption(Y_NUMERIC_AXIS);
      }

      //      String formatStr =axis.getTickLabelDateFormat();
      String formatStr = null;

      if (formatStr == null)
      {
         formatStr = getDefaultTickLabelDateFormat(p, side);
      }

      return formatStr;
   }

   /**
    * DOCUMENT_ME
    *
    * @param p DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   public static int getMinorTimeSeriesField(TimeSeries p)
   {
      boolean[] calChanges = totalCalendarChanges(p);

      if (calChanges[calChanges.length - 1])
      {
         return Calendar.MILLISECOND;
      }

      if (calChanges[calChanges.length - 2])
      {
         return Calendar.SECOND;
      }

      if (calChanges[calChanges.length - 3])
      {
         return Calendar.MINUTE;
      }

      if (calChanges[calChanges.length - 4])
      {
         return Calendar.HOUR;
      }

      if (calChanges[calChanges.length - 5])
      {
         return Calendar.DATE;
      }

      if (calChanges[calChanges.length - 6])
      {
         return Calendar.MONTH;
      }

      if (calChanges[calChanges.length - 7])
      {
         return Calendar.YEAR;
      }

      System.out.println("Error");

      return Calendar.YEAR;
   }

   /**
    * DOCUMENT_ME
    *
    * @param p DOCUMENT_ME
    * @param side DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   public static String getDefaultTickLabelDateFormat(TimeSeries p, int side)
   {
      boolean[] calChanges = totalCalendarChanges(p);
      ArrayList strList = new ArrayList();
      String formatStr = "";

      if (calChanges[0])
      {
         strList.add("yyyy");
      }

      if (calChanges[1])
      {
         strList.add("MMM");
      }

      if (calChanges[2])
      {
         strList.add("dd");
      }

      if (calChanges[3])
      {
         strList.add("HH");
      }

      if (calChanges[4])
      {
         strList.add("mm");
      }

      if (calChanges[5])
      {
         strList.add("ss");
      }

      if (calChanges[6])
      {
         strList.add("SSS");
      }

      for (int i = 0; i < strList.size(); ++i)
      {
         formatStr += strList.get(i);

         if (i < (strList.size() - 1))
         {
            if (((String) strList.get(i)).equals("dd"))
            {
               formatStr += " ";
            }
            else
            {
               formatStr += ":";
            }
         }
      }

      return formatStr;
   }

   /**
    * DOCUMENT_ME
    *
    * @param p DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   public static boolean[] totalCalendarChanges(TimeSeries p)
   {
      boolean[] totalChanges = new boolean[] 
      {
         false, 
         false, 
         false, 
         false, 
         false, 
         false, 
         false
      };
      List keyList = p.getDataKeyList();

      for (int i = 0; i < keyList.size(); ++i)
      {
         DateDataSetIfc doubleTimeSeries = (DateDataSetIfc) p.getDataSet(
                                                 keyList.get(i));
         boolean[] changes = calendarChanges(doubleTimeSeries);

         for (int j = 0; j < totalChanges.length; ++j)
         {
            totalChanges[j] = (changes[j])
                              ? true
                              : totalChanges[j];
         }
      }

      return totalChanges;
   }

   /**
    * DOCUMENT_ME
    *
    * @param at DOCUMENT_ME
    * @param formatStr DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    *
    * @throws java.lang.Exception DOCUMENT_ME
    ********************************************************/
   public static String[] createLabels(double[] at, String formatStr)
                                throws java.lang.Exception
   {
      String[] labels = new String[at.length];

      for (int i = 0; i < at.length; ++i)
      {
         Date date = TimeSeriesAxisConverter.user2date(at[i]);
         SimpleDateFormat formatter = new SimpleDateFormat(formatStr);
         labels[i] = formatter.format(date);
      }

      return labels;
   }

   /**
    * DOCUMENT_ME
    *
    * @param ts DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   public static boolean[] calendarChanges(DateDataSetIfc ts)
   {
      boolean[] totalChanges = new boolean[] 
      {
         false, 
         false, 
         false, 
         false, 
         false, 
         false, 
         false
      };

      try
      {
         ts.open();

         int numElements = ts.getNumElements();

         for (int i = 1; i < numElements; i++)
         {
            //            Object[] datum = ts.getTimeStamp(i-1);
            //            Date date1 = (Date)datum[0];
            Date date1 = ts.getDate(i - 1);
            GregorianCalendar g1 = new GregorianCalendar();
            g1.setTime(date1);

            //            datum = ts.getTimeStamp(i);
            //            Date date2 = (Date)datum[0];
            Date date2 = ts.getDate(i);
            GregorianCalendar g2 = new GregorianCalendar();
            g2.setTime(date2);

            boolean[] deltas = calendarDiff(g2, g1);

            for (int j = 0; j < totalChanges.length; j++)
            {
               totalChanges[j] = (deltas[j])
                                 ? (deltas[j])
                                 : (totalChanges[j]);
            }
         }

         ts.close();
      }
      catch (java.lang.Exception e)
      {
         e.printStackTrace();
      }

      return totalChanges;
   }

   /**
    * DOCUMENT_ME
    *
    * @param cal1 DOCUMENT_ME
    * @param cal2 DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   public static boolean[] calendarDiff(Calendar cal1, Calendar cal2)
   {
      boolean[] delta = new boolean[7];
      Calendar c1 = (Calendar) cal1.clone();
      Calendar c2 = (Calendar) cal2.clone();
      delta[0] = (boolean) calendarFieldDiff(c1, c2, Calendar.YEAR);
      delta[1] = (boolean) calendarFieldDiff(c1, c2, Calendar.MONTH);
      delta[2] = (boolean) calendarFieldDiff(c1, c2, Calendar.DATE);
      delta[3] = (boolean) calendarFieldDiff(c1, c2, Calendar.HOUR);
      delta[4] = (boolean) calendarFieldDiff(c1, c2, Calendar.MINUTE);
      delta[5] = (boolean) calendarFieldDiff(c1, c2, Calendar.SECOND);
      delta[6] = (boolean) calendarFieldDiff(c1, c2, Calendar.MILLISECOND);

      return delta;
   }

   /**
    * DOCUMENT_ME
    *
    * @param cal1 DOCUMENT_ME
    * @param cal2 DOCUMENT_ME
    * @param field DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   public static boolean calendarFieldDiff(Calendar cal1, Calendar cal2, 
      int field)
   {
      return (cal1.get(field) != cal2.get(field))
             ? true
             : false;
   }

   /**
    * DOCUMENT_ME
    *
    * @param d1 DOCUMENT_ME
    * @param field DOCUMENT_ME
    * @param step DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   private static Calendar getFirstTick(Date d1, int field, int step)
   {
      Calendar cal1 = new GregorianCalendar();
      cal1.setTime(d1);

      int year1 = cal1.get(Calendar.YEAR);
      int month1 = cal1.get(Calendar.MONTH);
      int day1 = cal1.get(Calendar.DATE);
      int hour1 = cal1.get(Calendar.HOUR);
      int min1 = cal1.get(Calendar.MINUTE);
      int sec1 = cal1.get(Calendar.SECOND);
      int ms1 = cal1.get(Calendar.MILLISECOND);
      int year = 0;
      int month = 0;
      int day = 0;
      int hour = 0;
      int min = 0;
      int sec = 0;
      int ms = 0;

      switch (field)
      {
      case Calendar.YEAR:
         year = year1 - (year1 % step);

         break;

      case Calendar.MONTH:
         year = year1;
         month = month1 - (month1 % step);

         break;

      case Calendar.DATE:
         year = year1;
         month = month1;
         day = day1 - (day1 % step);

         break;

      case Calendar.HOUR:
         year = year1;
         month = month1;
         day = day1;
         hour = hour1 - (hour1 % step);

         break;

      case Calendar.MINUTE:
         year = year1;
         month = month1;
         day = day1;
         hour = hour1;
         min = min1 - (min1 % step);

         break;

      case Calendar.SECOND:
         year = year1;
         month = month1;
         day = day1;
         hour = hour1;
         min = min1;
         sec = sec1 - (sec1 % step);

         break;

      case Calendar.MILLISECOND:

         //millseconds need to be handled differently
         year = year1;
         month = month1;
         day = day1;
         hour = hour1;
         min = min1;
         sec = sec1;
         ms = ms1 - (ms1 % step);

         break;

      default:
         System.out.println("\n*********ERROR*******\n");

         break;
      }

      return new GregorianCalendar(year, month, day, hour, min, sec);
   }

   /**
    * DOCUMENT_ME
    *
    * @param d1 DOCUMENT_ME
    * @param d2 DOCUMENT_ME
    * @param minTicks DOCUMENT_ME
    * @param field DOCUMENT_ME
    * @param step DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   private static Date[] getRoundedTickLocations(Date d1, Date d2, 
      int minTicks, int field, int step)
   {
      System.out.println("getRoundedTickLocations(" + d1 + "," + d2 + ","
                         + minTicks + "," + field + "," + step + ")");

      Calendar cal1 = new GregorianCalendar();
      cal1.setTime(d1);

      Calendar cal2 = new GregorianCalendar();
      cal2.setTime(d2);

      Calendar cal = getFirstTick(d1, field, step);
      ArrayList ticks = new ArrayList();
      ticks.add(cal.clone());

      while ((cal.getTime()).before(cal2.getTime()))
      {
         cal.add(field, step);
         ticks.add(cal.clone());
      }

      Date[] at = null;

      if (ticks.size() >= minTicks)
      {
         at = new Date[ticks.size()];

         for (int i = 0; i < ticks.size(); ++i)
         {
            at[i] = ((GregorianCalendar) ticks.get(i)).getTime();
            System.out.println("at[" + i + "]=" + at[i]);
         }
      }

      return at;
   }

   /**
    * DOCUMENT_ME
    *
    * @param d1 DOCUMENT_ME
    * @param d2 DOCUMENT_ME
    * @param minTicks DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   public static Date[] getSmartTicks(Date d1, Date d2, int minTicks)
   {
      int[] yearSteps = new int[] 
      {
         100, 
         50, 
         25, 
         10, 
         5, 
         1
      };
      int[] monthSteps = new int[] 
      {
         6, 
         3, 
         1
      };
      int[] daySteps = new int[] 
      {
         15, 
         5, 
         1
      };
      int[] hourSteps = new int[] 
      {
         20, 
         10, 
         5, 
         1
      };
      int[] minSteps = new int[] 
      {
         30, 
         15, 
         10, 
         5, 
         1
      };
      int[] secSteps = new int[] 
      {
         30, 
         15, 
         10, 
         5, 
         1
      };
      int[] msSteps = new int[] 
      {
         500, 
         250, 
         100, 
         50, 
         25, 
         10, 
         5, 
         1
      };
      int[] fields = new int[] 
      {
         Calendar.YEAR, 
         Calendar.MONTH, 
         Calendar.DATE, 
         Calendar.HOUR, 
         Calendar.MINUTE, 
         Calendar.SECOND, 
         Calendar.MILLISECOND
      };
      HashMap hashMap = new HashMap();
      hashMap.put(new Integer(fields[0]), yearSteps);
      hashMap.put(new Integer(fields[1]), monthSteps);
      hashMap.put(new Integer(fields[2]), daySteps);
      hashMap.put(new Integer(fields[3]), hourSteps);
      hashMap.put(new Integer(fields[4]), minSteps);
      hashMap.put(new Integer(fields[5]), secSteps);
      hashMap.put(new Integer(fields[6]), msSteps);

      Date[] at = null;
      boolean done = false;
      int i = -1;

      while (!done)
      {
         int field = fields[++i];
         int[] steps = (int[]) hashMap.get(new Integer(field));

         for (int j = 0; j < steps.length; ++j)
         {
            int step = steps[j];
            at = getRoundedTickLocations(d1, d2, minTicks, field, step);

            if (at != null)
            {
               done = true;

               break;
            }
         }
      }

      return at;
   }

   /**
    * describe object in a String
    *
    * @return String describing object
    ******************************************************/
   public String toString()
   {
      return Util.toString(this);
   }
}
