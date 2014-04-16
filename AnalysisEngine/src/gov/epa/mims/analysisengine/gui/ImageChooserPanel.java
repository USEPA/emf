package gov.epa.mims.analysisengine.gui;

import java.awt.Dimension;
import javax.swing.*;

 /**
 * A utility panel for choosing among icon values.. includes options to have a label,
 * place the label on top or on the side
 * @author Prashant Pai, Parthee Partheepan CEP UNC
 * @version $Id: ImageChooserPanel.java,v 1.2 2005/09/19 14:50:03 rhavaldar Exp $
 * @see
 */

public class ImageChooserPanel extends JPanel {

   /** the combobox to select the value **/
  private JComboBox valueBox = null;

  /** The JLabel that appears on the GUI.*/
  JLabel jLabel = null;

  /** the string for the label **/
  private String label = null;

  public ImageChooserPanel(String label, boolean labelOnTop, ImageIcon [] availOptions)
  {
    setAlignmentX(JPanel.CENTER_ALIGNMENT);
    setAlignmentY(JPanel.CENTER_ALIGNMENT);

    this.label = label;
    jLabel = new JLabel(label);
    jLabel.setHorizontalAlignment(SwingConstants.CENTER);
    jLabel.setAlignmentX(this.CENTER_ALIGNMENT);

    valueBox = new JComboBox(availOptions);
    valueBox.setAlignmentX(this.CENTER_ALIGNMENT);

    //valueBox.setPreferredSize(new Dimension(100, 25));

    valueBox.setSelectedIndex(0);

    if (labelOnTop)
    {
       setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }
    else
    {
         setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    }

    add(jLabel);

    if (!labelOnTop)
       add(Box.createHorizontalStrut(5));
//  else
//     add(Box.createVerticalStrut(2));

    add(valueBox);

    setBorder(BorderFactory.createEmptyBorder(2 ,2 ,2 ,2));
  }//StringChooserPanel()


  /**
   * return the value selected in the combobox
   * @return The ImageIcon selected in the combobox
   */
  public ImageIcon getValue()
  {
    return (ImageIcon) valueBox.getSelectedItem();
  }//getValue()

  /**
   * set the integer value in this panel
   * @param value The Image Icon value to be set
   */
  public void setValue(ImageIcon value)
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

