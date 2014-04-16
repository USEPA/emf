package gov.epa.mims.analysisengine.gui;

import javax.swing.*;
import java.awt.*;
import javax.swing.text.*;
import java.awt.event.FocusListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
/**
 * A utility panel for editing String values. Includes options to have a label,
 * place the label on top or on the side, and having bounds checking on the value
 *
 * @author Daniel Gatti, CEP UNC
 * @version $Id: StringValuePanel.java,v 1.2 2005/09/19 14:50:03 rhavaldar Exp $
 */
public class StringValuePanel extends JPanel
{
   /** the textfield to edit the value **/
   private JTextField valueField = null;

   /** the label **/
   private String label = null;

   /** the JLabel for the above label **/
   private JLabel jLabel = null;

   /** the unit label */
   private String unitLabel = null;

   /** the JLabel for the aboeve label **/
   private JLabel jUnitLabel = null;

   /**
    * Constructor.
    * @param label
    * @param labelOnTop
    */
   public StringValuePanel(String label, boolean labelOnTop, boolean singleChar)
   {
      initialize(label, labelOnTop, singleChar);
   } // StringValuePanel()

   /**
    * Constructor.
    * @param label
    * @param labelOnTop
    */
   public StringValuePanel(String label, boolean labelOnTop)
   {
      initialize(label, labelOnTop, false);
   } // StringValuePanel()


   /**
    * @param label
    * @param unitLabel A String type that specifies the unit of the parameter to be displayed
    * @param labelOnTop
    */
   public StringValuePanel(String label, String unitLabel, boolean labelOnTop)
   {
      initialize(label, labelOnTop, false);
      this.unitLabel = unitLabel;
      jUnitLabel = new JLabel(unitLabel);
      add(jUnitLabel);
   } // StringValuePanel()


   /**
    * A private method which contains the common piece of code for the above two
    * constructors
    */
   private void initialize(String label, boolean labelOnTop, boolean singleChar)
   {

      this.label = label;
      setAlignmentX(JPanel.CENTER_ALIGNMENT);
      setAlignmentY(JPanel.CENTER_ALIGNMENT);
      jLabel = new JLabel(label);
      valueField = new JTextField(5);
      if (singleChar)
      {
        valueField.setDocument(new SingleCharDocument());
        valueField.setHorizontalAlignment(JTextField.CENTER);
      }//if (singleChar)
      //valueField.setMaximumSize(new Dimension(200, 20));

      // Set up the X or Y axis as the layout type.
      int layoutType = BoxLayout.X_AXIS;
      if (labelOnTop)
         layoutType = BoxLayout.Y_AXIS;

      setLayout(new BoxLayout(this, layoutType));
      add(jLabel);
      if (labelOnTop){}
         //add(Box.createVerticalStrut(1));
      else
         add(Box.createHorizontalStrut(5));
      add(valueField);

      setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
   }


   /**
    * return the value in the textfield
    * @return the verified value in the textfield
    */
   public String getValue()
   {
      if (valueField.getText().trim().length() == 0)
      {
         return null;
      }

      return valueField.getText();
   } // getValue()

   /**
    * set the String value in this panel
    * @param val the value to be set
    */
   public void setValue(String val)
   {
      if (val == null)
      {
         valueField.setText("");
      }
      else
      {
         valueField.setText(val);
      }
   }//setValue()

   /**
    * set the usability of the textfield within the panel
    * @param enable to enable the textfield or not
    */
   public void setEnabled(boolean enable)
   {
      jLabel.setEnabled(enable);
      valueField.setEnabled(enable);
      if(jUnitLabel != null)
      {
         jUnitLabel.setEnabled(enable);
      }
   }//setEnabled()


  /**
    * Set the text field editable or non editable
    * @param edit boolean
    */
   public void setEditable(boolean edit)
   {
      valueField.setEditable(edit) ;
   }
   /**
    * Set a new label to display next to the text field.
    * @param str String that is the new text to display.
    */
   public void setLabel(String str)
   {
      jLabel.setText(str);
   }

   /**
    * Set a preferred size
    * @param dim Dimension
    */
   public void setFieldPreferredSize(Dimension dim)
   {
      valueField.setPreferredSize(dim);
   }

   /**
    * Set a no of columns
    * @param columns int
    */
   public void setFieldColumnSize(int columns)
   {
      valueField.setColumns(columns);
   }

   public void addSelectAllFocusListener()
   {
      valueField.addFocusListener(new FocusAdapter()
      {
         public void focusGained(FocusEvent e)
         {
            valueField.selectAll();
         }
      });
   }

   static class SingleCharDocument extends PlainDocument {

     public void insertString(int offs, String str, AttributeSet a)
       throws BadLocationException
     {
       if (offs != 0) throw new BadLocationException("Can only enter one character",
         offs);
       else if (str.length() > 1) throw new BadLocationException("Can only enter one character",
         str.length());
       else super.insertString(offs, str, a);
     }
   }


   public static void main(String[] args)
   {
      JFrame testFrame = new JFrame();
      //testFrame.getContentPane().add(new DoubleValuePanel("test label", true));
      testFrame.getContentPane().setLayout(new GridLayout(2,1));
      testFrame.getContentPane().add(new JLabel("1"));
      testFrame.getContentPane().add(new JLabel("2"));
      testFrame.pack();
      testFrame.getContentPane().setLayout(new GridLayout(3,1));
      testFrame.getContentPane().add(new JLabel("3"));
      testFrame.pack();
      testFrame.setVisible(true);
   }
} // class StringValuePanel

