package gov.epa.mims.analysisengine.gui;

import javax.swing.*;
import java.awt.event.*;

/**
 * A utility panel to display and edit color arrays
 *
 * @author Prashant Pai, CEP UNC
 * @version $Id: StringArrayPanel.java,v 1.2 2005/09/19 14:50:03 rhavaldar Exp $
 */

public class StringArrayPanel extends JPanel {

  /******************************************************
 *
 * fields
 *
 *****************************************************/

  /** the label for this panel **/
  private String label = null;

  /** the string array to be edited **/
  private String[] array = null;

  /** the available set of choices **/
  private String[] availChoices = null;

  /** the edit button **   */
  private JButton editButton = null;

  /** the array editor dialog **/
  private ArrayEditorDialog editor = null;

  /******************************************************
   *
   * methods
   *
   *****************************************************/

  public StringArrayPanel(String label, boolean labelOnTop, String[] availValues,
                          String dialogLabel)
  {
    this.label = dialogLabel;
    this.availChoices = availValues;
    setAlignmentX(JPanel.CENTER_ALIGNMENT);
    setAlignmentY(JPanel.CENTER_ALIGNMENT);
    JLabel jLabel = new JLabel(label);
    editButton = new JButton("Edit");
    editButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent ae)
      {
        if (editor == null || editor.getStringValues() == null)
          editor = new ArrayEditorDialog(array, availChoices, StringArrayPanel.this.label);
        else
          editor.invoke(array);
        array = editor.getStringValues();
        //array = ArrayEditorDialog.editStrings(array, availChoices);
      }//actionPerformed()
    });//addActionListener()

    add(jLabel);
    add(editButton);
  }

  public void setValue(String[] strings)
  {
    array = strings;
  }//setValue()

  public String[] getValue()
  {
    //if (array == null) System.out.println("color array in getvalue are null");
    return array;
  }

  /**
   * enable or disable the panel including the components inside
   * @param enable the panel?
   */
  public void setEnabled(boolean enable)
  {
    super.setEnabled(enable);
    editButton.setEnabled(enable);
  }

}

