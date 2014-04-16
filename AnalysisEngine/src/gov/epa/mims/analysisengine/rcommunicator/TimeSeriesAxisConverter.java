package gov.epa.mims.analysisengine.rcommunicator;

import gov.epa.mims.analysisengine.AnalysisEngineConstants;
import gov.epa.mims.analysisengine.tree.Axis;
import gov.epa.mims.analysisengine.tree.AxisTime;
import gov.epa.mims.analysisengine.tree.Text;
import gov.epa.mims.analysisengine.tree.TimeSeries;

import java.text.SimpleDateFormat;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;


/**
 * DOCUMENT ME!
 *
 * @version $Revision: 1.2 $
 * @author $author$
 */
public class TimeSeriesAxisConverter implements Serializable, Cloneable,
gov.epa.mims.analysisengine.tree.AnalysisOptionConstantsIfc
{
   /** DOCUMENT ME! */
   static final long serialVersionUID = 1;

   /** DOCUMENT_ME */
   private static final double U1 = AnalysisEngineConstants.TIME_SERIES_USER_COORD1;

   /** DOCUMENT_ME */
   private static final double U2 = AnalysisEngineConstants.TIME_SERIES_USER_COORD2;

   /** DOCUMENT_ME */
   private static final int NUM_TICKS = AnalysisEngineConstants.TIME_SERIES_DEF_MAX_TICKS;

   /** DOCUMENT ME! */
   private static long t1;

   /** DOCUMENT ME! */
   private static long t2;

   /** DOCUMENT ME! */
   private static double m;

   /** DOCUMENT ME! */
   private static double[] tickmarks = null;

   /** DOCUMENT_ME */
   private static boolean initilized = false;

   /** DOCUMENT_ME */
   //private static TimeSeriesDefaultFormatter timeSeriesDefaultFormatter = new TimeSeriesDefaultFormatter();
   private static TimeSeriesDefaultFormatter timeSeriesDefaultFormatter;

   /**
    * DOCUMENT_ME
    *
    * @param t1Date DOCUMENT_ME
    * @param t2Date DOCUMENT_ME
    ********************************************************/
   public static void init(Date t1Date, Date t2Date)
   {
      t1 = t1Date.getTime();
      t2 = t2Date.getTime();
      m = (U2 - U1) / (t2 - t1);
      initilized = true;
      //timeSeriesDefaultFormatter = new TimeSeriesDefaultFormatter();
   }

   /**
    * DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   public static TimeSeriesDefaultFormatter getDefaultFormatter()
   {
      timeSeriesDefaultFormatter = new TimeSeriesDefaultFormatter();
      return timeSeriesDefaultFormatter;
   }

   /**
    * DOCUMENT_ME
    *
    * @param date DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   public static double date2user(Date date)
   {
      long t = date.getTime();

      return U1 + (m * (t - t1));
   }

   /**
    * DOCUMENT_ME
    *
    * @param at DOCUMENT_ME
    * @param tickLabelFormat DOCUMENT_ME
    * @param timeZone DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   public static String[] getLabels(double[] at, String tickLabelFormat, 
      TimeZone timeZone)
   {
      String[] rtrn = new String[at.length];
      String fmt = tickLabelFormat;

      if (tickLabelFormat == null)
      {
         fmt = timeSeriesDefaultFormatter.getTickLabelFormat();
      }

      SimpleDateFormat formatter = new SimpleDateFormat(fmt);
      formatter.setTimeZone(timeZone);

      for (int i = 0; i < at.length; ++i)
      {
         try
         {
            rtrn[i] = formatter.format(user2date(at[i]));
         }
         catch (java.lang.Exception e)
         {
            e.printStackTrace();
            rtrn[i] = "NULL";
         }
      }

      return rtrn;
   }

   /**
    * DOCUMENT_ME
    *
    * @param u DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    *
    * @throws java.lang.Exception DOCUMENT_ME
    ********************************************************/
   public static Date user2date(double u) throws java.lang.Exception
   {
      if (!initilized)
      {
         String msg = "TimeSeriesAxisConverter not initilized";
         throw new java.lang.Exception(msg);
      }

      double val = t1 + ((u - U1) / m);
      Double dbl = new Double(val);
      long ms = dbl.longValue();

      return new Date(ms);
   }

   /**
    * DOCUMENT_ME
    *
    * @param minTicks DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   public static double[] getRoundedTicks(int minTicks)
   {
      Date d1 = new Date(t1);
      Date d2 = new Date(t2);
      int minorField = timeSeriesDefaultFormatter.getLeastSignificantChangeField();
      int[] stepInfo = getStepInfo(d1, d2, minorField, minTicks);
      double[] at = buildRoundedTickArray(d1, d2, minorField, stepInfo);

      tickmarks = (double[]) at.clone();

      return at;
   }

   /**
    * DOCUMENT_ME
    *
    * @param d1 DOCUMENT_ME
    * @param minTicks DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   public static double[] getRoundedTicks(Date d1, int minTicks)
   {
      Date d2 = new Date(t2);
      int minorField = timeSeriesDefaultFormatter.getLeastSignificantChangeField();
      int[] stepInfo = getStepInfo(d1, d2, minorField, minTicks);
      double[] at = buildRoundedTickArray(d1, d2, minorField, stepInfo);

      tickmarks = (double[]) at.clone();

      return at;
   }

   /**
    * DOCUMENT_ME
    *
    * @param t1 DOCUMENT_ME
    * @param t2 DOCUMENT_ME
    * @param minorField DOCUMENT_ME
    * @param minTicks DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   public static double[] getRoundedTicks(Date t1, Date t2, int minorField, 
      int minTicks)
   {
      int[] stepInfo = getStepInfo(t1, t2, minorField, minTicks);
      double[] at = buildRoundedTickArray(t1, t2, minorField, stepInfo);

      tickmarks = (double[]) at.clone();

      return at;
   }

   /**
    * DOCUMENT_ME
    *
    * @param t1 DOCUMENT_ME
    * @param field DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   private static long getMillisecondsPerFieldUnit(Date t1, int field)
   {
      long delta;

      switch (field)
      {
      case Calendar.YEAR:
      case Calendar.MONTH:

         GregorianCalendar cal1 = new GregorianCalendar();
         GregorianCalendar cal2 = new GregorianCalendar();
         cal1.setTime(t1);
         cal2 = (GregorianCalendar) cal1.clone();
         cal2.add(field, 1);
         delta = (cal2.getTime()).getTime() - (cal1.getTime()).getTime();

         break;

      case Calendar.DATE:
         delta = 24 * 60 * 60 * 1000;

         break;

      case Calendar.HOUR:
         delta = 60 * 60 * 1000;

         break;

      case Calendar.MINUTE:
         delta = 60 * 1000;

         break;

      case Calendar.SECOND:
         delta = 1000;

         break;

      case Calendar.MILLISECOND:
         delta = 1;

         break;

      default:
         System.out.println("\n*********ERROR*******\n");
         delta = -1;

         break;
      }

      return delta;
   }

   /**
    * DOCUMENT_ME
    *
    * @param field DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   private static int[][] getStepTable(int field)
   {
      int[][] stepTable = null;

      switch (field)
      {
      case Calendar.YEAR:
         stepTable = AnalysisEngineConstants.TIME_SERIES_YEAR_STEPS;

         break;

      case Calendar.MONTH:
         stepTable = AnalysisEngineConstants.TIME_SERIES_MONTH_STEPS;

         break;

      case Calendar.DATE:
         stepTable = AnalysisEngineConstants.TIME_SERIES_DATE_STEPS;

         break;

      case Calendar.HOUR:
         stepTable = AnalysisEngineConstants.TIME_SERIES_HOUR_STEPS;

         break;

      case Calendar.MINUTE:
         stepTable = AnalysisEngineConstants.TIME_SERIES_MINUTE_STEPS;

         break;

      case Calendar.SECOND:
         stepTable = AnalysisEngineConstants.TIME_SERIES_SECOND_STEPS;

         break;

      case Calendar.MILLISECOND:
         stepTable = AnalysisEngineConstants.TIME_SERIES_MILLISECOND_STEPS;

         break;

      default:
         break;
      }

      return stepTable;
   }

   /**
    * DOCUMENT_ME
    *
    * @param t1 DOCUMENT_ME
    * @param t2 DOCUMENT_ME
    * @param field DOCUMENT_ME
    * @param minTicks DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   private static int[] getStepInfo(Date t1, Date t2, int field, int minTicks)
   {
      int[] stepInfo = null;
      long wMS = (t2.getTime() - t1.getTime()) / minTicks;
      long msFU = getMillisecondsPerFieldUnit(t1, field);
      int wFU = (int) (wMS / msFU);
      int[][] stepTable = getStepTable(field);

      for (int i = 0; i < stepTable.length; ++i)
      {
         if (wFU <= stepTable[i][0])
         {
            stepInfo = stepTable[i];

            break;
         }
      }

      if (stepInfo == null)
      {
         stepInfo = stepTable[stepTable.length - 1];


         //step will multiple of largest value in stepTable
         stepInfo[0] = (wFU / stepInfo[0]) * stepInfo[0];
      }

      return stepInfo;
   }

   /**
    * DOCUMENT_ME
    *
    * @param t1 DOCUMENT_ME
    * @param t2 DOCUMENT_ME
    * @param field DOCUMENT_ME
    * @param stepInfo DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   private static double[] buildRoundedTickArray(Date t1, Date t2, int field, 
      int[] stepInfo)
   {
      Calendar cal = new GregorianCalendar();
      Calendar cal1 = new GregorianCalendar();
      Calendar cal2 = new GregorianCalendar();
      cal1.setTime(t1);
      cal2.setTime(t2);

      int currentVal = cal1.get(field);
      cal1.add(field, -currentVal % stepInfo[1]);

      cal = (GregorianCalendar) cal1.clone();

      ArrayList atList = new ArrayList();

      while (cal.before(cal2))
      {
         atList.add(cal.getTime());
         cal.add(field, stepInfo[0]);
      }

      double[] at = new double[atList.size()];

      for (int i = 0; i < atList.size(); ++i)
      {
         at[i] = date2user((Date) atList.get(i));
      }

      return at;
   }

   /**
    * DOCUMENT_ME
    *
    * @param format DOCUMENT_ME
    * @param tz DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   public static String getConstantTimeString(String format, TimeZone tz)
   {
      //if a format is not given, then use the default format
      format = (format == null)
               ? (timeSeriesDefaultFormatter.getConstantTimeFormat())
               : format;

      //return the first data point in this format
      SimpleDateFormat formatter = null;
      formatter = new SimpleDateFormat(format);
      formatter.setTimeZone(tz);

      return formatter.format(new Date(t1));
   }

   /**
    * DOCUMENT_ME
    *
    * @param p DOCUMENT_ME
    * @param side DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   public static String getFormattedStartTime(TimeSeries p, int side)
   {
      int minorField = UtilTimeAxis.getMinorTimeSeriesField(p);
      SimpleDateFormat formatter = null;

      Axis axis = (side == 1)
                  ? (Axis) p.getOption(X_TIME_AXIS)
                  : (Axis) p.getOption(Y_NUMERIC_AXIS);

      String format = null;

      if (axis instanceof AxisTime)
      {
         AxisTime timeSeriesAxis = (AxisTime) axis;
         Text text = timeSeriesAxis.getConstantTimeLabelFormat();
         format = text.getTimeFormat();
      }

      if (format == null)
      {
         switch (minorField)
         {
         case Calendar.YEAR:
            format = "yyyy zzz";

            break;

         case Calendar.MONTH:
            format = "yyyy/MMM zzz";

            break;

         case Calendar.DATE:
            format = "yyyy/MMM/dd zzz";

            break;

         case Calendar.HOUR:
            format = "yyyy/MMM/dd 'at' hh a zzz";

            break;

         case Calendar.MINUTE:
            format = "yyyy/MMM/dd 'at' hh:mm a zzz";

            break;

         case Calendar.SECOND:
            format = "yyyy/MMM/dd 'at' hh:mm:ss a zzz";

            break;

         case Calendar.MILLISECOND:
            format = "yyyy/MMM/dd 'at' hh:mm:ss:SSS a zzz";

            break;

         default:
            System.out.println("\n*********ERROR*******\n");

            break;
         }
      }

      formatter = new SimpleDateFormat(format);

      return formatter.format(new Date(t1));
   }

   /**
    * DOCUMENT_ME
    *
    * @param tickInc DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   public static double[] getTicks(long tickInc)
   {
      long t1L = t1;
      long t2L = t2;
      ArrayList atList = new ArrayList();
      long t0L = t1L;

      while (t0L <= t2L)
      {
         atList.add(new Long(t0L));
         t0L += tickInc;
      }

      double[] at = new double[atList.size()];

      for (int i = 0; i < atList.size(); ++i)
      {
         at[i] = date2user(new Date(((Long) atList.get(i)).longValue()));
      }

      tickmarks = (double[]) at.clone();

      return at;
   }

   /**
    * DOCUMENT_ME
    *
    * @param t1 DOCUMENT_ME
    * @param tickInc DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   public static double[] getTicks(Date t1, long tickInc)
   {
      long t1L = t1.getTime();
      long t2L = t2;
      ArrayList atList = new ArrayList();
      long t0L = t1L;

      while (t0L <= t2L)
      {
         atList.add(new Long(t0L));
         t0L += tickInc;
      }

      double[] at = new double[atList.size()];

      for (int i = 0; i < atList.size(); ++i)
      {
         at[i] = date2user(new Date(((Long) atList.get(i)).longValue()));
      }

      tickmarks = (double[]) at.clone();

      return at;
   }

   /**
    * DOCUMENT_ME
    *
    * @param t1 DOCUMENT_ME
    * @param t2 DOCUMENT_ME
    * @param tickInc DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    * @deprecated deprecated_text
    ********************************************************/
   public static double[] getTicks(Date t1, Date t2, long tickInc)
   {
      long t1L = t1.getTime();
      long t2L = t2.getTime();
      ArrayList atList = new ArrayList();
      long t0L = t1L;

      while (t0L <= t2L)
      {
         atList.add(new Long(t0L));
         t0L += tickInc;
      }

      double[] at = new double[atList.size()];

      for (int i = 0; i < atList.size(); ++i)
      {
         at[i] = date2user(new Date(((Long) atList.get(i)).longValue()));
      }

      tickmarks = (double[]) at.clone();

      return at;
   }

   /**
    * DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   public static double[] getGeneratedTickmarks()
   {
      return (double[]) tickmarks.clone();
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
