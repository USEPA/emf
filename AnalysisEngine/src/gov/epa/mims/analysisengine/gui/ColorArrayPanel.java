package gov.epa.mims.analysisengine.gui;

import javax.swing.*;
import java.awt.event.*;
import java.awt.Color;

/**
 * A utility panel to display and edit color arrays
 *
 * @author Prashant Pai, CEP UNC
 * @version $Id: ColorArrayPanel.java,v 1.2 2005/09/19 14:50:03 rhavaldar Exp $
 */

public class ColorArrayPanel extends JPanel {

  /** the label for this panel **/
  private String label = null;

  /** the color array to be edited **/
  private Color[] array = null;

  /** the edit button **/
  private JButton editButton = null;

  /** the array editor dialog **/
  private ArrayEditorDialog editor = null;

  public ColorArrayPanel(String label, boolean labelOnTop, String dialogLabel)
  {
    this.label = dialogLabel;
    setAlignmentX(JPanel.CENTER_ALIGNMENT);
    setAlignmentY(JPanel.CENTER_ALIGNMENT);
    JLabel jLabel = new JLabel(label);
    editButton = new JButton("Edit");
    editButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent ae)
      {
        if (editor == null || editor.getColorValues() == null)
          editor = new ArrayEditorDialog(array, ColorArrayPanel.this.label);
        else
          editor.invoke(array);
        array = editor.getColorValues();
      }//actionPerformed()
    });//addActionListener()

    add(jLabel);
    add(editButton);
  }

  public void setValue(Color[] colors)
  {
    array = colors;
  }//setValue()

  public Color[] getValue()
  {
    if (array == null) System.out.println("Color array in getvalue is null");
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

