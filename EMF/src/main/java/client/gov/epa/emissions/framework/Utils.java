package gov.epa.emissions.framework;

import gov.epa.emissions.framework.services.EmfException;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    private static final String MMDDYYY_DATE_REGEX = "\\d{2}/\\d{2}/\\d{4}";

    public static void validateDate(String dateStr) throws EmfException {

        Pattern p = Pattern.compile(MMDDYYY_DATE_REGEX);
        Matcher m = p.matcher(dateStr);

        if (m.matches()) {

            if (!isValidDayMonth(dateStr)) {
                throw new EmfException("Invalid date '" + dateStr + ".");
            }
        } else {
            throw new EmfException("Date '" + dateStr + "' not of the form mm/dd/yyyy.");
        }
    }

    public static void validateDate(String dateStr, String fieldName) throws EmfException {

        Pattern p = Pattern.compile(MMDDYYY_DATE_REGEX);
        Matcher m = p.matcher(dateStr);

        if (m.matches()) {

            if (!isValidDayMonth(dateStr)) {
                throw new EmfException("Invalid date '" + dateStr + "' for '" + fieldName + "'.");
            }
        } else {
            throw new EmfException("Date '" + dateStr + "' for '" + fieldName + "' not of the form mm/dd/yyyy.");
        }
    }

    private static boolean isValidDayMonth(String strDate) {

        boolean valid = false;

        String[] dateArray = strDate.split("/");

        int month = Integer.valueOf(dateArray[0]).intValue();
        int day = Integer.valueOf(dateArray[1]).intValue();
        int year = Integer.valueOf(dateArray[2]).intValue();

        if ((day > 0 && day <= 31) && (month > 0 && month <= 12)) {

            /*
             * should be correct for most cases but still will not be correct in fringe cases like feb having 30 days or
             * april having 31 days.
             */
            valid = true;
            try {
                GregorianCalendar cal = new GregorianCalendar();

                /*
                 * setLenient to false to force calendar to throw IllegalArgumentException in case any field, day, month
                 * or year is not valid (invalid year would be '00')
                 */
                cal.setLenient(false);

                /*
                 * month - 1 is done because Calander uses 0-11 for months
                 */
                cal.set(year, (month - 1), day);
                /*
                 * add is called just to invoke the method Calendar.complete(). complete() is the method that throws the
                 * IllegalArgumentException.
                 * 
                 * Note : Calendar.set() does not compute the date fields only methods like add(), roll() or getTime()
                 * force the Calendar object to calculate field values
                 */

                /*
                 * done only to force Calendar to compute all fields
                 */
                cal.add(Calendar.SECOND, 1);
            } catch (IllegalArgumentException iae) {
                valid = false;
            }
        }

        return valid;
    }

}
