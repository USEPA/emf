package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.CommonDebugLevel;
import gov.epa.emissions.commons.util.CustomDateFormat;

import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.swing.table.DefaultTableCellRenderer;

public class DateRenderer extends DefaultTableCellRenderer {

    public DateRenderer() { super(); }

    public void setValue(Object value) {
        if ( value == null){
            setText("");
            return;
        }
//        if ( value instanceof Timestamp) {
//            Date date = new Date( ((Timestamp) value).getTime());
//            setText( formatter.format(date));
//        } else if ( value instanceof Time) {
//            Date date = new Date( ((Time) value).getTime());
//            setText( formatter.format(date));
//        } else 
        if ( CommonDebugLevel.DEBUG_PAGE){
            System.out.println( "Renderer: " + value.getClass());
        }
        
        //System.out.println( "Renderer: " + value.getClass());
        
        if ( value instanceof Calendar) {
            String text = CustomDateFormat.format_yyyy_MM_dd_HHmmssSSS(((Calendar) value).getTime());
            //System.out.println( "Renderer: " + text);
            setText( text);
        } else if ( value instanceof Date) { // sql.Date, sql.Time, sql.Timestamp are subclasses of Util.Date
            String text = CustomDateFormat.format_yyyy_MM_dd_HHmmssSSS((Date)value);
            //System.out.println( "Renderer: " + text);
            setText( text);
        } else if ( value instanceof String) {
            setText( (String) value);
        } else {
            //System.out.println( "Renderer: Cannot format given Object of " + value.getClass() + ".");
            throw new IllegalArgumentException("Redenrer - Cannot format given Object of " + value.getClass() + ".");
        }
    }

}
