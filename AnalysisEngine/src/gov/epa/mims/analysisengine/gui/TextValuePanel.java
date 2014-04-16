package gov.epa.mims.analysisengine.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import gov.epa.mims.analysisengine.tree.Text;

/**
 * A GUI to display the text for a Text object in a panel with an edit
 * button to bring up the font details.
 *
 * @author Daniel Gatti
 * @version $Id: TextValuePanel.java,v 1.2 2005/09/19 14:50:03 rhavaldar Exp $
 */

public class TextValuePanel extends JPanel
{
   /** The Text object that we are editing. */
   protected Text text = null;
   
   /** True if we should lay out the label on top of the text field.*/
   protected boolean layoutVertical = true;
   
   /** The caption to place near the text field. */
   protected String caption = null;
   
   /** The text field for editing the text of thte Text. */
   protected JTextField textField = null;
   
   /** The label for the caption. */
   protected JLabel label = null;
   
   /** The button for editing the Text font. */
   protected JButton editBtn = null;
   
   /**A boolean to indicate whether this gui is used for setting labels in the Axis 
    * Editors
    */
   private boolean axisLabelEditor = false;
   
   /**
    * Constructor.
    * @param text Text that is the text that this GUI will edit.
    */
   public TextValuePanel(String caption, boolean labelOnTop, Text text)
   {
      this.text = text;
      layoutVertical = labelOnTop;
      this.caption = caption;
      initialize();
   }
   
   /**
    * Constructor.
    * @param text Text that is the text that this GUI will edit.
    */
   public TextValuePanel(String caption, boolean labelOnTop, Text text, boolean isAxisLabel)
   {
      this.text = text;
      layoutVertical = labelOnTop;
      this.caption = caption;
      axisLabelEditor = isAxisLabel;
      initialize();
   }
   
   
   /**
    * Present the TextEditor to the user and allow them to edit
    * the attributes of the Text.
    */
   protected void editText()
   {
      if (text == null)
      {
         text = new Text();
      }
      if(textField.getText() != null)
      {
         text.setTextString(textField.getText());
      }
      
      TextEditor ed = null;
      if(!axisLabelEditor)
      {
         ed = new TextEditor(text);
      }
      else
      {
         ed = new TextEditor(text,TextEditor.EDITOR_AXIS);
      }
      ed.setModal(true);
      ed.setVisible(true);
      textField.setText(text.getTextString());
   }
   
   
   /**
    * Return the Text that this GUI has been editing.
    *
    * @returns Text that its the Text that the user has been editing.
    */
   public Text getValue()
   {
      if (text == null)
         text = new Text();
      
      text.setTextString(textField.getText());
      return text;
   }
   
   
   
   /**
    * Construct the GUI.
    */
   protected void initialize()
   {
      textField = new JTextField("");
      textField.setMaximumSize(new Dimension(200, 20));
      textField.setPreferredSize(new Dimension(100, 20));
      if (text != null)
         textField.setText(text.getTextString());
      label = new JLabel(caption);
      
      editBtn = new JButton("Edit");
      editBtn.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            editText();
         }
      }
      );
      
      if (layoutVertical)
      {
         setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
         label.setAlignmentX(Component.CENTER_ALIGNMENT);
         textField.setAlignmentX(Component.CENTER_ALIGNMENT);
         editBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
      }
      else
      {
         setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
         label.setAlignmentY(Component.CENTER_ALIGNMENT);
         textField.setAlignmentY(Component.CENTER_ALIGNMENT);
         editBtn.setAlignmentY(Component.CENTER_ALIGNMENT);
      }
      add(label);
      if (layoutVertical)
         add(Box.createVerticalStrut(5));
      else
         add(Box.createHorizontalStrut(5));
      add(textField);
      if (layoutVertical)
         add(Box.createVerticalStrut(5));
      else
         add(Box.createHorizontalStrut(5));
      add(editBtn);
      setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
   } // initialize()
   
   
   /**
    * Enable or disable the components on this GUI.
    * @param enable boolean that is true if the components should be eanbled.
    */
   public void setEnabled(boolean enable)
   {
      label.setEnabled(enable);
      textField.setEnabled(enable);
      editBtn.setEnabled(enable);
   }
   
   
   /**
    * Set the tool tip to appear over all of the components in
    * the panel.
    */
   public void setToolTipText(String newText)
   {
      label.setToolTipText(newText);
      textField.setToolTipText(newText);
      editBtn.setToolTipText(newText);
   }
   
   
   /**
    * Set the Text that this GUI should edit.
    * @param text Text to be edited in this GUI.
    */
   public void setValue(Text text)
   {
      this.text = text;
      if (text != null)
         textField.setText(text.getTextString());
   }
   
   
   /**
    * main() for bringing up a test GUI.
    * @param args ue "true" for vertical layout and "false" for horizontal.
    */
   public static void main(String[] args)
   {
      boolean vert = true;
      if (args.length > 0)
      {
         if (args[0].equalsIgnoreCase("false"))
            vert = false;
      }
      
      Text t = new Text();
      t.setTextString("Salmon, Catfish and Shrimp");
      TextValuePanel tvp = new TextValuePanel("Fishbait!!", vert, t);
      JFrame f = new JFrame("TextValuePanel text window");
      f.getContentPane().add(tvp);
      f.pack();
      f.setVisible(true);
   }
} // class TextValuePanel
