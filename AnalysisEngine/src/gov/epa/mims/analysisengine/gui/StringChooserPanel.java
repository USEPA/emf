package gov.epa.mims.analysisengine.gui;

import java.awt.*;
import java.awt.event.ActionListener;
import javax.swing.*;

/**
 * A utility panel for choosing among string values.. includes options to have a label,
 * place the label on top or on the side
 *
 * @author Prashant Pai, CEP UNC
 * @version $Id: StringChooserPanel.java,v 1.2 2005/09/19 14:50:03 rhavaldar Exp $
 */
public class StringChooserPanel extends JPanel {

  /** the combobox to select the value **/
  private JComboBox valueBox = null;

  /** the label **/
  private String label = null;

  /** the JLabel for the above label **/
  private JLabel jLabel = null;

  public StringChooserPanel(String label, boolean labelOnTop, String[] availOptions)
  {
    this.label = label;
    jLabel = new JLabel(label);
    valueBox = new JComboBox(availOptions);
    valueBox.setSelectedIndex(0);
    valueBox.setMaximumSize(new Dimension(200, 20));

    if (labelOnTop)
    {
       setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
       jLabel.setHorizontalAlignment(SwingConstants.CENTER);
    }
    else
    {
         setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
      jLabel.setHorizontalAlignment(SwingConstants.CENTER);
    }

    setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

    add(jLabel);
    if (!labelOnTop)
       add(Box.createHorizontalStrut(3));
    add(valueBox);
  }//StringChooserPanel()

  
   public StringChooserPanel(String label, boolean labelOnTop,
      String[] availOptions, boolean editable)
   {
      this(label,labelOnTop,availOptions);
      valueBox.setEditable(editable);
   }
  /**
   * Add an ActionListener that fires when the combo box selection changes.
   * @param litener ActionListener to add.
   */
  public void addActionListener(ActionListener listener)
  {
     valueBox.addActionListener(listener);
  }


  /**
   * return the value selected in the combobox
   * @return the selected value in the combobox
   */
  public String getValue()
  {
    return (String) valueBox.getSelectedItem();
  }//getValue()

  /**
   * set the integer value in this panel
   * @param val the value to be set
   */
  public void setValue(String value)
  {
    valueBox.setSelectedItem(value);
    if (valueBox.getModel().getSelectedItem() == null)
      valueBox.setSelectedIndex(0);
  }//setValue()

  /**
   * set the usability of the textfield within the panel
   * @param enable to enable the textfield or not
   */
  public void setEnabled(boolean enable)
  {
    jLabel.setEnabled(enable);
    valueBox.setEnabled(enable);
  }//setEnabled()

  /**
   * Set the tool tip to appear over all of the components in
   * the panel.
   */
  public void setToolTipText(String newText)
  {
     jLabel.setToolTipText(newText);
     valueBox.setToolTipText(newText);
  }

}

