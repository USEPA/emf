package gov.epa.mims.analysisengine.gui;

import javax.swing.*;
import java.awt.event.*;


/**
 * A utility panel to display and edit color arrays
 *
 * @author Prashant Pai, CEP UNC
 * @version $Id: DoubleArrayPanel.java,v 1.2 2005/09/19 14:50:03 rhavaldar Exp $
 */

public class DoubleArrayPanel extends JPanel {

  ////////////////////////////FIELDS//////////////////////////////////////////

  /** the label for this panel **/
  private String label = null;

  /** the double array to be edited **/
  private double[] array = null;

  /** the edit button **/
  private JButton editButton = null;

  /** the array editor dialog **/
  private ArrayEditorDialog editor = null;
  
  /** the label for the panel **/
  private JLabel jLabel = null;

  /** the lower bound to be placed on the values in the array **/
  //private double

  //////////////////////////////METHODS///////////////////////////////////////
  /**
   * Constructor taking in a label and an option to place the label
   * @param label the label
   * @param labelOnTop whether to place the label on top of the edit button if
   * false the label is placed to the left hand side
   */
  public DoubleArrayPanel(String label, boolean labelOnTop, String dialogLabel)
  {
    this.label = dialogLabel;
    setAlignmentX(JPanel.CENTER_ALIGNMENT);
    setAlignmentY(JPanel.CENTER_ALIGNMENT);
    jLabel = new JLabel(label);
    editButton = new JButton("Edit");
    editButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent ae)
      {
        if (editor == null || editor.getDoubleValues() == null)
          editor = new ArrayEditorDialog(array, DoubleArrayPanel.this.label,
             Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        else
          editor.invoke(array);
        array = editor.getDoubleValues();
      }//actionPerformed()
    });//addActionListener()

    add(jLabel);
    add(editButton);
  }//DoubleArrayPanel()

  /**
   * set the array values to be displayed in this panel
   * @param doubles an array of doubles
   */
  public void setValue(double[] doubles)
  {
    array = doubles;
  }//setValue()

  /**
   * return the double array edited by the user
   * @return double[] edited by the user
   */
  public double[] getValue()
  {
    return array;
  }//getValue()

  /**
   * set the bounds to be placed on the values in the array
   * @param lowerBound the lower bound
   * @param upperBound the upper bound
   */
  public void setBounds(double lowerBound, double upperBound)
  {

  }//setBounds()

  /**
   * enable or disable the panel including the components inside
   * @param enable the panel?
   */
  public void setEnabled(boolean enable)
  {
    super.setEnabled(enable);
    jLabel.setEnabled(enable);
    editButton.setEnabled(enable);
  }

}

