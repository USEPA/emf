package gov.epa.mims.analysisengine.gui;

/**
 * 
 * A JTextField that only accepts decimal nubmers.
 * Copied from the Sun Website.
 * Used in the DoubleEditableTablePanel
 * 
 * @author Daniel Gatti
 * @version $Id: DecimalField.java,v 1.2 2005/09/19 14:50:03 rhavaldar Exp $
 */
import javax.swing.*; 
import javax.swing.text.*; 

import java.awt.Toolkit;
import java.text.*;

public class DecimalField extends JTextField 
{
   private NumberFormat format;

   public DecimalField(double value, int columns, NumberFormat f) 
   {
      super(columns);
      setDocument(new FormattedDocument(f));
      format = f;
      setValue(value);
   }

   public double getValue() 
   {
      double retVal = 0.0;

      try 
      {
         retVal = format.parse(getText()).doubleValue();
      } 
      catch (ParseException e) 
      {
         // This should never happen because insertString allows
         // only properly formatted data to get in the field.
         Toolkit.getDefaultToolkit().beep();
         System.err.println("getValue: could not parse: " + getText());
      }
      return retVal;
   }

   public void setValue(double value) {
      setText(format.format(value));
   }
   
   class FormattedDocument extends PlainDocument 
   {
      private Format format;

      public FormattedDocument(Format f)
       {
         format = f;
      }

      public Format getFormat() {
         return format;
      }

      public void insertString(int offs, String str, AttributeSet a) 
         throws BadLocationException 
      {

         String currentText = getText(0, getLength());
         String beforeOffset = currentText.substring(0, offs);
         String afterOffset = currentText.substring(offs, currentText.length());
         String proposedResult = beforeOffset + str + afterOffset;

         try 
         {
            format.parseObject(proposedResult);
            super.insertString(offs, str, a);
         } 
         catch (ParseException e) 
         {
            Toolkit.getDefaultToolkit().beep();
            System.err.println("insertString: could not parse: "
                           + proposedResult);
         }
      }

      public void remove(int offs, int len) throws BadLocationException 
      {
         String currentText = getText(0, getLength());
         String beforeOffset = currentText.substring(0, offs);
         String afterOffset = currentText.substring(len + offs,
                                          currentText.length());
         String proposedResult = beforeOffset + afterOffset;

         try 
         {
            if (proposedResult.length() != 0)
               format.parseObject(proposedResult);
            super.remove(offs, len);
         } 
         catch (ParseException e) 
         {
            Toolkit.getDefaultToolkit().beep();
            System.err.println("remove: could not parse: " + proposedResult);
         }
      }
   }
}

