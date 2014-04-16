package gov.epa.mims.analysisengine.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.text.*;

/**
 * A utility panel for editing double values.. includes options to have a label,
 * place the label on top or on the side, and having bounds checking on the value
 *
 * @author Prashant Pai, CEP UNC
 * @version $Id: DoubleValuePanel.java,v 1.2 2005/09/19 14:50:03 rhavaldar Exp $
 */

public class DoubleValuePanel extends JPanel
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

   /** the lower bound if one is required **/
   private double lowBound;

   /** the upper bound if one is required **/
   private double upBound;

   /**
    *
    * @param label
    * @param labelOnTop
    * @param lowerBound
    * @param upperBound
    */
   public DoubleValuePanel(String label, boolean labelOnTop, double lowerBound,
                           double upperBound)
   {
      initialize(label, labelOnTop, lowerBound, upperBound);
   }//DoubleValuePanel()


   /**
    * @param label
    * @param unitLabel A String type that specifies the unit of the parameter to be displayed
    * @param labelOnTop
    * @param lowerBound
    * @param upperBound
    */
   public DoubleValuePanel(String label, String unitLabel, boolean labelOnTop, double lowerBound,
   double upperBound)
   {
      initialize(label,labelOnTop,lowerBound,upperBound);
      this.unitLabel = unitLabel;
      jUnitLabel = new JLabel(unitLabel);
      add(jUnitLabel);
   } //DoubleValuePanel()


   /**
    * A private method which contains the common piece of code for the above two
    * constructors
    */
   private void initialize(String label, boolean labelOnTop, double lowerBound,
                           double upperBound)
   {

      this.label = label;
      this.lowBound = lowerBound;
      this.upBound = upperBound;
      setAlignmentX(JPanel.CENTER_ALIGNMENT);
      setAlignmentY(JPanel.CENTER_ALIGNMENT);
      jLabel = new JLabel(label);
      if (labelOnTop)
      {
         jLabel.setHorizontalAlignment(SwingConstants.CENTER);
         jLabel.setHorizontalTextPosition(SwingConstants.CENTER);
      }
      valueField = new JTextField(5);
      valueField.setMaximumSize(new Dimension(200, 20));
      InputVerifier doubleVerifier = new InputVerifier()
      {
         public boolean verify(JComponent input)
         {
            double enteredValue;
            try
            {
               String text = ((JTextField) input).getText().trim();
               if (text.length() == 0)
               {
                  return true;
               }
               enteredValue = Double.parseDouble(text);
//System.out.println("enteredValue=" +enteredValue);
            }
            catch(Exception e)
            {
               DefaultUserInteractor.get().notify(DoubleValuePanel.this,"Error verifying input value",
               "Please enter a floating point number", UserInteractor.ERROR);
               return false;
            }
//System.out.println("enteredValue=" +enteredValue);
//System.out.println("lowBound=" +lowBound);
//System.out.println("upBound=" +upBound);
            if (enteredValue < lowBound || enteredValue > upBound)
            {
               String message = null;
               if (upBound == Double.MAX_VALUE)
                  message = "Please enter a floating point number > " + lowBound;
               else
                  message = "Please enter a floating point number between " + lowBound
                  + " and " + upBound;
//System.out.println("message=" +message);
               DefaultUserInteractor.get().notify(DoubleValuePanel.this,"Error verifying input value",
               message, UserInteractor.ERROR);
               return false;
            }
            else
               return true;
         }//verify()
      };
      valueField.setInputVerifier(doubleVerifier);

      // Set up the X or Y axis as the layout type.
      int layoutType = BoxLayout.X_AXIS;
      if (labelOnTop)
         layoutType = BoxLayout.Y_AXIS;

      setLayout(new BoxLayout(this, layoutType));
      add(jLabel);
      if (labelOnTop)
         add(Box.createVerticalStrut(5));
      else
         add(Box.createHorizontalStrut(5));
      add(valueField);

      setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
   }


   public DoubleValuePanel(String label, boolean labelOnTop)
   {
      this(label, labelOnTop, -Double.MAX_VALUE, Double.MAX_VALUE);
   }//DoubleValuePanel()


   /**
     * set the bounds
     * @param lb lower bound
     * @param ub upper bound
     */
     public void setBounds(double lb, double ub)
     {
        lowBound = lb;
        upBound = ub;
     }

     /**
      * return the text in the label
      * @return String text in the label
      */
     public String getLabelText()
     {
        return jLabel.getText();
   }


   /**
    * return the value in the textfield - NaN if empty
    * @return the verified value in the textfield
    */
   public double getValue()
   {
      if (valueField.getText().trim().length() == 0)
      {
         return Double.NaN;
      }
      return Double.parseDouble(valueField.getText());
   }//getValue()

   /**
    * set the double value in this panel
    * @param val the value to be set
    */
   public void setValue(double val)
   {
      if (Double.isNaN(val))
      {
         valueField.setText("");
      }
      else
      {
         valueField.setText(val + "");
      }
   }//setValue()


   /**
    * clear the text field
    */
   public void clearValue()
   {
      valueField.setText("");
   }

   /**
    * set the usability of the textfield within the panel
    * @param enable to enable the textfield or not
    */
   public void setEnabled(boolean enable)
   {
      jLabel.setEnabled(enable);
      valueField.setEnabled(enable);
      if (jUnitLabel != null)
         jUnitLabel.setEnabled(enable);
   }//setEnabled()


   /**
    * Set a new label to display next to the text field.
    * @param str String that is the new text to display.
    */
   public void setLabel(String str)
   {
      jLabel.setText(str);
   }


   /**
    * Set the tool tip to appear over all of the components in
    * the panel.
    */
   public void setToolTipText(String newText)
   {
      jLabel.setToolTipText(newText);
      valueField.setToolTipText(newText);
      if (jUnitLabel != null)
         jUnitLabel.setToolTipText(newText);
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
}
