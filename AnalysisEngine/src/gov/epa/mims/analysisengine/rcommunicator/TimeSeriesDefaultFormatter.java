package gov.epa.mims.analysisengine.rcommunicator;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


/**
 * DOCUMENT_ME
 *
 * @version $Revision: 1.2 $
 * @author $author$
 ********************************************************/
public class TimeSeriesDefaultFormatter implements java.io.Serializable
{
   /** DOCUMENT_ME */
   static final long serialVersionUID = 1;

   /** DOCUMENT_ME */
   public static final int YEAR = 0;

   /** DOCUMENT_ME */
   public static final int MONTH = 1;

   /** DOCUMENT_ME */
   public static final int DAY = 2;

   /** DOCUMENT_ME */
   public static final int HOUR = 3;

   /** DOCUMENT_ME */
   public static final int MINUTE = 4;

   /** DOCUMENT_ME */
   public static final int SECOND = 5;

   /** DOCUMENT_ME */
   public static final int MILLISECOND = 6;

   /** DOCUMENT_ME */
   public static final int TIME_ZONE = 7;

   /** DOCUMENT_ME */
   private static final int NUM_FIELDS = 8;

   /** DOCUMENT_ME */
   public static final int[] FIELDS = new int[NUM_FIELDS];

   /** DOCUMENT_ME */
   private static String[] formats = new String[NUM_FIELDS];

   /** DOCUMENT_ME */
   private static String[] separators = new String[NUM_FIELDS];

   /** DOCUMENT_ME */
   private boolean[] changes = new boolean[NUM_FIELDS - 1];

   /** DOCUMENT_ME */
   private Date d1 = null;

   // static initialization block
   {
      FIELDS[YEAR] = Calendar.YEAR;
      FIELDS[MONTH] = Calendar.MONTH;
      FIELDS[DAY] = Calendar.DATE;
      FIELDS[HOUR] = Calendar.HOUR;
      FIELDS[MINUTE] = Calendar.MINUTE;
      FIELDS[SECOND] = Calendar.SECOND;
      FIELDS[MILLISECOND] = Calendar.MILLISECOND;
      FIELDS[TIME_ZONE] = 0;

      formats[YEAR] = "yyyy";
      formats[MONTH] = "MMM";
      formats[DAY] = "dd";
      formats[HOUR] = "HH";
      formats[MINUTE] = "mm";
      formats[SECOND] = "ss";
      formats[MILLISECOND] = "SSS";
      formats[TIME_ZONE] = "zzz";

      separators[YEAR] = ":";
      separators[MONTH] = ":";
      separators[DAY] = " ";
      separators[HOUR] = ":";
      separators[MINUTE] = ":";
      separators[SECOND] = ":";
      separators[MILLISECOND] = " ";
      separators[TIME_ZONE] = null;
   }

   /**
    * Creates a new TimeSeriesDefaultFormatter object.
    *
    ********************************************************/
   public TimeSeriesDefaultFormatter()
   {
      changes[YEAR] = false;
      changes[MONTH] = false;
      changes[DAY] = false;
      changes[HOUR] = false;
      changes[MINUTE] = false;
      changes[SECOND] = false;
      changes[MILLISECOND] = false;
   }

   /**
    * DOCUMENT_ME
    *
    * @param date DOCUMENT_ME
    ********************************************************/
   public void addDate(Date date)
   {
      if (d1 == null)
      {
         d1 = date;
      }
      else
      {
         Date d2 = date;
         boolean[] deltas = calendarChanges(d1, d2);

         for (int i = 0; i < changes.length; ++i)
         {
            changes[i] = (deltas[i])
                         ? true
                         : (changes[i]);
         }

         d1 = d2;
      }
   }

   /**
    * DOCUMENT_ME
    *
    * @param d1 DOCUMENT_ME
    * @param d2 DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   public static boolean[] calendarChanges(Date d1, Date d2)
   {
      GregorianCalendar c1 = new GregorianCalendar();
      GregorianCalendar c2 = new GregorianCalendar();
      c1.setTime(d1);
      c2.setTime(d2);

      return calendarChanges(c1, c2);
   }

   /**
    * DOCUMENT_ME
    *
    * @param c1 DOCUMENT_ME
    * @param c2 DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   public static boolean[] calendarChanges(Calendar c1, Calendar c2)
   {
      boolean[] rtrn = new boolean[NUM_FIELDS];

      for (int i = 0; i < NUM_FIELDS; ++i)
      {
         int field = FIELDS[i];
         rtrn[i] = calendarFieldDiff(c1, c2, field);
      }

      return rtrn;
   }

   /**
    * DOCUMENT_ME
    *
    * @param c1 DOCUMENT_ME
    * @param c2 DOCUMENT_ME
    * @param field DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   public static boolean calendarFieldDiff(Calendar c1, Calendar c2, int field)
   {
      return (c1.get(field) != c2.get(field))
             ? true
             : false;
   }

   /**
    * DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   public String getTickLabelFormat()
   {
      StringBuffer buf = new StringBuffer(30);
      ArrayList formatList = new ArrayList();
      ArrayList separatorList = new ArrayList();

      for (int i = 0; i < changes.length; ++i)
      {
         if (changes[i])
         {
            formatList.add(formats[i]);
            separatorList.add(separators[i]);
         }
      }

      for (int i = 0; i < formatList.size(); ++i)
      {
         buf.append(formatList.get(i));

         if (i < (formatList.size() - 1))
         {
            buf.append(separatorList.get(i));
         }
      }

      return buf.toString();
   }

   /**
    * DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   public String getConstantTimeFormat()
   {
      StringBuffer buf = new StringBuffer(30);
      ArrayList formatList = new ArrayList();
      ArrayList separatorList = new ArrayList();

      for (int i = 0; i < changes.length; ++i)
      {
         if (!changes[i])
         {
            formatList.add(formats[i]);
            separatorList.add(separators[i]);
         }
         else
         {
            break;
         }
      }

      for (int i = 0; i < formatList.size(); ++i)
      {
         buf.append(formatList.get(i));

         if (i < (formatList.size() - 1))
         {
            buf.append(separatorList.get(i));
         }
         else
         {
            buf.append(separators[MILLISECOND]);
         }
      }

      buf.append(formats[TIME_ZONE]);

      return buf.toString();
   }

   /**
    * DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   public int getLeastSignificantChangeField()
   {
      for (int i = changes.length - 1; i >= 0; --i)
      {
         if (changes[i])
         {
            return FIELDS[i];
         }
      }

      throw new IllegalArgumentException(getClass().getName()
                                         + " LeastSignificantChangeField not found");
   }
}
