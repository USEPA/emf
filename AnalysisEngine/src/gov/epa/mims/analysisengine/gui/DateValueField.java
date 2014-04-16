package gov.epa.mims.analysisengine.gui;



import javax.swing.JTextField;import javax.swing.*;
import javax.swing.text.*;
import java.awt.Toolkit;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

/**
 * A JTextField that will only allow the entry of dates.
 * @author Daniel Gatti
 * @version $Id: DateValueField.java,v 1.2 2005/09/19 14:50:03 rhavaldar Exp $
 */

public class DateValueField extends JTextField
{
   private Toolkit toolkit;
   private SimpleDateFormat dateFormatter;
   public DateValueField(String format)
   {
      super(format.length());
      toolkit = Toolkit.getDefaultToolkit();
   }
   
   public Date getValue()
   {
      Date retVal = null;
      try
      {
         String str = getText();
         if (str != null || str.trim().length() > 0)
         {
            retVal = dateFormatter.parse(str);
         }
      }
      catch (ParseException e)
      {
         // This should never happen because insertString allows
         // only properly formatted data to get in the field.
         toolkit.beep();
      }
      return retVal;
      
   }
   
   
   
   public void setValue(int value)
   
   {
      
      //      setText(dateFormatter.format(value));
      
   }
   
   
   
   
   
   protected Document createDefaultModel()
   
   {
      
      return new DateDocument();
      
   }
   
   
   
   /**
    *
    * The class that validates the Date.
    *
    * @author Daniel Gatti
    *
    */
   
   protected class DateDocument extends PlainDocument
   
   {
      
      public void insertString(int offs, String str, AttributeSet a)
      
      throws BadLocationException
      
      {
         
         char[] source = str.toCharArray();
         
         char[] result = new char[source.length];
         
         int j = 0;
         
         
         
         for (int i = 0; i < result.length; i++)
            
         {
            
            if (Character.isDigit(source[i]))
               
               result[j++] = source[i];
            
            else
            {
               
               toolkit.beep();
               
               System.err.println("insertString: " + source[i]);
               
            }
            
         }
         
         super.insertString(offs, new String(result, 0, j), a);
         
      }
      
   } // class DateDocument
   
   
   
} // class DateValueField

