package gov.epa.mims.analysisengine.gui;

import java.awt.*;
import javax.swing.*;

/**
 * A utility panel for editing integer values.. includes options to have a label,
 * place the label on top or on the side, and having bounds checking on the value
 *
 * @author Prashant Pai, CEP UNC
 * @version $Id: IntegerValuePanel.java,v 1.2 2005/09/19 14:50:03 rhavaldar Exp $
 */

public class IntegerValuePanel extends JPanel
{
   /** the textfield to edit the value **/
   private JTextField valueField = null;

   /** the label **/
   private String label = null;

   /** the JLabel for the above label **/
   JLabel jLabel = null;

   /** the unit label */
   private String unitLabel = null;

   /** the JLabel for the aboeve label **/
   private JLabel jUnitLabel = null;

   /** the lower bound if one is required **/
   private int lowBound;

   /** the upper bound if one is required **/
   private int upBound;

   public IntegerValuePanel(String label, String unitLabel, boolean labelOnTop,
   int lowerBound, int upperBound)
   {
      this.label = label;
      this.unitLabel = unitLabel;
      this.lowBound = lowerBound;
      this.upBound = upperBound;
      initialize(labelOnTop);
      DefaultUserInteractor.set(new GUIUserInteractor());
   }//DoubleValuePanel()

   public IntegerValuePanel(String label, boolean labelOnTop, int lowerBound,
   int upperBound)
   {
      this(label, null, labelOnTop, lowerBound, upperBound);
   }


   public IntegerValuePanel(String label, boolean labelOnTop)
   {
      this(label, labelOnTop, Integer.MIN_VALUE, Integer.MAX_VALUE);
   }//IntegerValuePanel()


   public IntegerValuePanel(String label, String unitLabel, boolean labelOnTop)
   {
      this(label, unitLabel, labelOnTop, Integer.MIN_VALUE, Integer.MAX_VALUE);
   }//IntegerValuePanel()



   /**
    * Build the GUI.
    */
   protected void initialize(boolean labelOnTop)
   {
      setAlignmentX(JPanel.CENTER_ALIGNMENT);
      setAlignmentY(JPanel.CENTER_ALIGNMENT);
      jLabel = new JLabel(label);
      if (labelOnTop)
      {
         jLabel.setHorizontalAlignment(SwingConstants.CENTER);
         jLabel.setHorizontalTextPosition(SwingConstants.CENTER);
      }
      valueField = new JTextField(3);
      valueField.setMaximumSize(new Dimension(200, 20));
      if (unitLabel != null)
         jUnitLabel = new JLabel(unitLabel);

      InputVerifier intVerifier = new InputVerifier()
      {
         public boolean verify(JComponent input)
         {
            int enteredValue = 0;
            try
            {
               String str = ((JTextField) input).getText();
               // This handles the case where the user entered nothing.
               if (str == null || str.trim().length() == 0)
                  return true;

               enteredValue = Integer.parseInt(((JTextField) input).getText());

               if (enteredValue < lowBound || enteredValue > upBound)
               {
                  String message = null;
                  if (upBound == Integer.MAX_VALUE)
                     message = "Please enter an integer > " + lowBound;
                  else
                     message = "Please enter an integer between " + lowBound
                     + " and " + upBound;
                  DefaultUserInteractor.get().notify(IntegerValuePanel.this,"Error verifying input value",
                  message, UserInteractor.ERROR);
                  return false;
               }
               else
                  return true;
            }
            catch(Exception e)
            {
               DefaultUserInteractor.get().notify(IntegerValuePanel.this,"Error verifying input value",
               "Please enter an integer", UserInteractor.ERROR);
               return false;
            }
         }//verify()
      };
      valueField.setInputVerifier(intVerifier);

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
      if (unitLabel != null)
         add(jUnitLabel);

      setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
   }

   /**
    * return the value in the textfield
    * @return the verified value in the textfield. Return Integer.MIN_VALUE if
    * there is no value in the box.
    */
   public int getValue()
   {
      String s = valueField.getText().trim();
      if (s.length() == 0)
         return Integer.MIN_VALUE;
      else
         return Integer.parseInt(s);
   }//getValue()

   /**
    * set the integer value in this panel
    * @param val the value to be set
    */
   public void setValue(int val)
   {
      valueField.setText(val + "");
   }//setValue()

   /**
    * set the usability of the textfield within the panel
    * @param enable to enable the textfield or not
    */
   public void setEnabled(boolean enable)
   {
      jLabel.setEnabled(enable);
      valueField.setEnabled(enable);
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

   /** set the preferred size
    */
   public void setPreferredSize(Dimension dim)
   {
      valueField.setPreferredSize(dim);
   }
   /**
    * clear the text field
    */
   public void clearValue()
   {
      valueField.setText("");
   }
}
