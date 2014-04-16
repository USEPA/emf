package gov.epa.mims.analysisengine.table;

import gov.epa.mims.analysisengine.table.format.FormatChooserPanel;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;

/**
 * <p>Description: A panel that accepts a SimpleDateFormat and displays a set
 * of possible date formats for the user to select from. </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: UNC - CEP</p>
 * @author Daniel Gatti
 * @version $Id: DateFormatPanel.java,v 1.4 2006/11/01 15:33:37 parthee Exp $
 */
public class DateFormatPanel extends FormatChooserPanel
{
   /** The date formatter that will be edited in this panel. */
   protected SimpleDateFormat format = null;

   /** The default time/date format. */
   public static final String DEFAULT_DATE_FORMAT = "MM/dd/yyyy";

   public static final String [] EXAMPLE_DATETIME_FORMATS =
     new String[] {DEFAULT_DATE_FORMAT, "yyyy/MM/dd", "yyyyMMdd",
    "MMddyyyy", "yyyyDDD", "MMM yyyy", "yyyy/MM/dd HH:mm:ss",
    "yyyyMMdd HH:mm:ss", "MM/dd/yyyy HH:mm:ss", "MMddyyyy HH:mm:ss",
    "yyyyDDD HH:mm:ss", "MM/dd/yyyy HH:mm:ss zzz", "HH:mm:ss",
    "HH:mm", "HH", "hh a"};

     /** empty date format */
     public static final String EMPTY_DATE_FORMAT = "";

     /** empty time format */
     public static final String EMPTY_TIME_FORMAT = "";

     /** the example date formats PLEASE REMEMBER TO NOT ANY SPACES IN BETWEEN **/
     public static final String [] EXAMPLE_DATE_FORMATS = new String[]
        {DEFAULT_DATE_FORMAT,
        "yyyy/MM/dd",
        "yyyyMMdd",
        "MMddyyyy",
        "yyyyDDD",
        "MMMyyyy",
        EMPTY_DATE_FORMAT};

      public static final String [] EXAMPLE_TIME_FORMATS = new String[]
        {EMPTY_TIME_FORMAT,
        "HH:mm:ss",
        "HH:mm:ss zzz",
        "HH:mm",
        "HH",
        "hh a"};

   /**
    * Constructor.
    * @param format SimpleDateFormat that will be displayed and edited in this
    * GUI.
    * @param availableFormats String[] that is a list of date formats that the
    *    user should choose from.
    * @param allowUserValue boolean that is true if we want the user to be able
    *    to type in a custom format that is not in the list.
    */
   public DateFormatPanel(SimpleDateFormat format, String[] availableFormats,
      boolean allowUserValue)
   {
      super(format, "Date/Time", availableFormats, allowUserValue);
      init();
   } // DateFormatPanel()

   /**
    * Constructor.
    * @param format SimpleDateFormat that will be displayed and edited in this
    * GUI.
    * @param availableFormats String[] that is a list of date formats that the
    *    user should choose from.
    * @param availableFormats2 String[] that is a list of time formats that the
    *    user should choose from.
    * @param allowUserValue boolean that is true if we want the user to be able
    *    to type in a custom format that is not in the list.
    */
   public DateFormatPanel(SimpleDateFormat format, String[] availableFormats,
      String[] availableFormats2, boolean allowUserValue)
   {
      super(format, "Date:", availableFormats, "   Time:",
            availableFormats2, allowUserValue);
      init();
   } // DateFormatPanel()

   /**
    * have functions common in the constructors here
    */
   protected void init()
   {
      sampleObject = new Date();
      this.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
            "Date Format"));
      setSampleLabel();
   }//init()

   /**
    * Create a new formatter with the given format String and add it to the
    * hashtable of existing formatters. Then return it.
    * @return Format that has just been created.
    */
   protected Format createNewFormat(String formatString)
   {
      SimpleDateFormat newFormat = new SimpleDateFormat(formatString);
      formatters.put(formatString, newFormat);
      return newFormat;
   } // createNewFormat()

   /**
    * This method splits a format string into the date format string and the
    * time format string..
    * Currently, use the logic that there is a space between the date and time
    * formats.. THIS MAY NOT BE ALWAYS TRUE
    *
    * @param formatString a StringBuffer that initially contains the entire format
    * and later is changed to just the date format
    * @return String the string containing the time format
    */
   public String splitToGetSecondString(StringBuffer formatString)
   {
      int spaceIndex = formatString.indexOf(" ");
      String returnString = formatString.substring(spaceIndex+1);
      formatString.delete(spaceIndex, formatString.length());
      return returnString;
   }//splitToGetSecondString(StringBuffer)


} // class DateFormatPanel

