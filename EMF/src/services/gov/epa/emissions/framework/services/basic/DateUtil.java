package gov.epa.emissions.framework.services.basic;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class DateUtil {
    
    public static int daysInOneBasedMonth(int year, int month) {
        
        // Create a calendar object of the desired 1-based month
        Calendar cal = new GregorianCalendar(year, month - 1, 1);
        
        // Get the number of days in that month
        return cal.getActualMaximum(Calendar.DAY_OF_MONTH); // 28
      }
    
    public static int daysInZeroBasedMonth(int year, int month) {
        
        // Create a calendar object of the desired 1-based month
        Calendar cal = new GregorianCalendar(year, month, 1);
        
        // Get the number of days in that month
        return cal.getActualMaximum(Calendar.DAY_OF_MONTH); // 28
      }
    
}
