package gov.epa.mims.analysisengine.rcommunicator;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


/**
 * DOCUMENT_ME
 *
 * @version $Revision: 1.2 $
 * @author $author$
 ********************************************************/
public class DateDiff
{
   /** DOCUMENT_ME */
   private Calendar c1;

   /** DOCUMENT_ME */
   private Calendar c2;

   /** DOCUMENT_ME */
   private Date d1;

   /**
    * Creates a new DateDiff object.
    ********************************************************/
   DateDiff()
   {
      c2 = new GregorianCalendar(2000, Calendar.FEBRUARY, 26);
      c1 = new GregorianCalendar(2000, Calendar.APRIL, 2);
      d1 = c1.getTime();
      System.out.println("Difference in Years : "
                         + fieldDifference(c1, c2, Calendar.YEAR));
      System.out.println("Difference in Months: "
                         + fieldDifference(c1, c2, Calendar.MONTH));
      System.out.println("Difference in Days  : "
                         + fieldDifference(c1, c2, Calendar.DATE));
   }

   /**
    * DOCUMENT_ME
    *
    * @param ca1 DOCUMENT_ME
    * @param ca2 DOCUMENT_ME
    * @param field DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   public int fieldDifference(Calendar ca1, Calendar ca2, int field)
   {
      int diff = 0;
      long targetInMilliseconds = ca1.getTime().getTime();
      long millis = ca2.getTime().getTime();

      if (millis < targetInMilliseconds)
      {
         while (true)
         {
            ca2.add(field, 1);

            long newMs = ca2.getTime().getTime();

            if (newMs > targetInMilliseconds)
            {
               ca2.setTime(new Date(millis));

               break;
            }

            millis = newMs;
            ++diff;
         }
      }
      else if (millis > targetInMilliseconds)
      {
         while (true)
         {
            ca2.add(field, -1);

            long newMs = ca2.getTime().getTime();

            if (newMs < targetInMilliseconds)
            {
               ca2.setTime(new Date(millis));

               break;
            }

            millis = newMs;
            --diff;
         }
      }

      return diff;
   }

   /**
    * DOCUMENT_ME
    *
    * @param args DOCUMENT_ME
    ********************************************************/
   public static void main(String[] args)
   {
      try
      {
         new DateDiff();
         System.exit(0);
      }
      catch (Exception e)
      {
         System.out.println("Can not start program");
      }
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
