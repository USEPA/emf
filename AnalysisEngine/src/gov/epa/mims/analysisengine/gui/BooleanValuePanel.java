package gov.epa.mims.analysisengine.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * A utility panel for editing boolean values.. boolean values can be either
 * represented as checkboxes or radio buttons.. if one label is provided then
 * they are checkboxes, else if two are provided then radio buttons..
 *
 * @author Prashant Pai, CEP UNC
 * @version $Id: BooleanValuePanel.java,v 1.2 2005/09/19 14:50:03 rhavaldar Exp $
 */

public class BooleanValuePanel extends JPanel
{

   /** the checkbox to edit the value **/
   private JCheckBox valueCheckBox = null;

   /** the first radion button **/
   private JRadioButton valueButton1 = null;

   /** the second radion button **/
   private JRadioButton valueButton2 = null;

   public BooleanValuePanel(String label1, String label2, boolean labelsOnTop)
   {
      valueButton1 = new JRadioButton(label1);
      valueButton2 = new JRadioButton(label2);
      ButtonGroup group = new ButtonGroup();
      group.add(valueButton1);
      group.add(valueButton2);
      if (labelsOnTop)
         setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
      else
         setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

      add(valueButton1);
      add(valueButton2);

   }//BooleanValuePanel()


   public BooleanValuePanel(String label)
   {
      valueCheckBox = new JCheckBox(label);
      add(valueCheckBox);
   }//BooleanValuePanel()

   public BooleanValuePanel(String label, boolean labelsOnTop)
   {
      setAlignmentX(JPanel.CENTER_ALIGNMENT);
      setAlignmentY(JPanel.CENTER_ALIGNMENT);

      JLabel nameLabel = new JLabel(label);
      nameLabel.setAlignmentX(this.CENTER_ALIGNMENT);
      if (labelsOnTop)
         setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
      else
         setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

      valueCheckBox = new JCheckBox();
      valueCheckBox.setAlignmentX(this.CENTER_ALIGNMENT);
      add(nameLabel);
      if(!labelsOnTop)
         add(Box.createHorizontalStrut(5));
      else
         add(Box.createVerticalStrut(5));

      add(valueCheckBox);
      //this.setBorder(BorderFactory.createLineBorder(java.awt.Color.black));
      setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
   }//BooleanValuePanel()


   /**
    * Add an ActionListener to the checkbox or the radio buttons.
    *
    * @author Daniel Gatti
    * @param listener ActionListener that will recieve events from this GUI.
    */
   public void addActionListener(ActionListener listener)
   {
      if (valueCheckBox != null)
         valueCheckBox.addActionListener(listener);
      else
      {
         valueButton1.addActionListener(listener);
         valueButton2.addActionListener(listener);
      }
   }



   /**
    * return the value set by the radiobuttons or the checkbox
    * @return the boole
    */
   public boolean getValue()
   {
      if (valueCheckBox != null)
         return valueCheckBox.isSelected();
      else
         return valueButton2.isSelected();
   }//getValue()


   /**
    * Remove an ActionListener from the checkbox or the radio buttons.
    *
    * @author Daniel Gatti
    * @param listener ActionListener to be removed.
    */
   public void removeActionListener(ActionListener listener)
   {
      if (valueCheckBox != null)
         valueCheckBox.removeActionListener(listener);
      else
      {
         valueButton1.removeActionListener(listener);
         valueButton2.removeActionListener(listener);
      }
   }


   /**
    * Enable or disable the entire panel.
    *
    * @author Daniel Gatti
    * @param enable boolean that is true to enable the panel and false to disable.
    */
   public void setEnabled(boolean enable)
   {
      if (valueCheckBox != null)
         valueCheckBox.setEnabled(enable);
      else
      {
         valueButton1.setEnabled(enable);
         valueButton2.setEnabled(enable);
      }
   }

   public void setValue(boolean val)
   {
      if (valueCheckBox != null)
         valueCheckBox.setSelected(val);
      else
      {
         valueButton1.setSelected(!val);
         valueButton2.setSelected(val);
      }
   }//setValue()

   /**
    * Set the tool tip to appear over all of the components in
    * the panel.
    */
   public void setToolTipText(String newText)
   {
     if (valueCheckBox != null)
        valueCheckBox.setToolTipText(newText);
     else
     {
        valueButton1.setToolTipText(newText);
        valueButton2.setToolTipText(newText);
     }
   }
}

