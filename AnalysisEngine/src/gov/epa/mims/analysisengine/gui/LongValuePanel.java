package gov.epa.mims.analysisengine.gui;

import java.awt.*;
import javax.swing.*;

/**
 * A utility panel for editing Long values.. includes options to have a label,
 * place the label on top or on the side, and having bounds checking on the value
 *
 * @author Prashant Pai, CEP UNC
 * @version $Id: LongValuePanel.java,v 1.2 2005/09/19 14:50:03 rhavaldar Exp $
 */

public class LongValuePanel extends JPanel
{
  /** the textfield to edit the value **/
  private JTextField valueField = null;

  /** the label **/
  private String label = null;

  /** the JLabel for the above label **/
  JLabel jLabel = null;

  /** the lower bound if one is required **/
  private long lowBound;

  /** the upper bound if one is required **/
  private long upBound;

  public LongValuePanel(String label, boolean labelOnTop, long lowerBound,
                           long upperBound)
  {
    this.label = label;
    this.lowBound = lowerBound;
    this.upBound = upperBound;
    setAlignmentX(JPanel.CENTER_ALIGNMENT);
    setAlignmentY(JPanel.CENTER_ALIGNMENT);
    jLabel = new JLabel(label);
    valueField = new JTextField(5);
    valueField.setMaximumSize(new Dimension(200, 20));

    InputVerifier longVerifier = new InputVerifier()
    {
      public boolean verify(JComponent input)
      {
        long enteredValue;
        try
        {
          enteredValue = Long.parseLong(((JTextField) input).getText());
        }
        catch(Exception e)
        {
          DefaultUserInteractor.get().notify(LongValuePanel.this,"Error verifying input value",
              "Please enter a long integer.", UserInteractor.ERROR);
          return false;
        }
        if (enteredValue < lowBound || enteredValue > upBound)
        {
          String message = null;
          if (upBound == Long.MAX_VALUE)
            message = "Please enter a long integer> " + lowBound;
          else
            message = "Please enter a long integer between " + lowBound
              + " and " + upBound;
          DefaultUserInteractor.get().notify(LongValuePanel.this,"Error verifying input value",
              message, UserInteractor.ERROR);
          return false;
        }
        else
          return true;
      }//verify()
    };
    valueField.setInputVerifier(longVerifier);

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
  } // LongValuePanel()


  public LongValuePanel(String label, boolean labelOnTop)
  {
    this(label, labelOnTop, Long.MIN_VALUE, Long.MAX_VALUE);
  } // LongValuePanel()

  /**
   * return the value in the textfield
   * @return the verified value in the textfield. Return Long.MIN_VALUE if
   * no value is in the box.
   */
  public long getValue()
  {
     String s = valueField.getText().trim();
     if (s.length() == 0)
        return Long.MIN_VALUE;
     else
        return Long.parseLong(s);
  }//getValue()

  /**
   * set the Long value in this panel
   * @param val the value to be set
   */
  public void setValue(long val)
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
} // class LongValuePanel

