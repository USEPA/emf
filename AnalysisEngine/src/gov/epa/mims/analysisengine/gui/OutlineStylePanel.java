package gov.epa.mims.analysisengine.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import gov.epa.mims.analysisengine.tree.OutlineType;

/**
 * A utility panel which have a Label, the ImageChooserPanel, the DoubleValuePanel
 * and a CheckBox
 * @author Alison Eyth, Daniel Gatti, Parthee Partheepan, CEP UNC
 * @version $Id: OutlineStylePanel.java,v 1.2 2005/09/19 14:50:03 rhavaldar Exp $
 */

public class OutlineStylePanel extends javax.swing.JPanel
{
   /** a panel to set the style of the outline **/
   private ImageChooserPanel stylePanel = null;

   /** a panel to set the color of the outline **/
   private ColorValuePanel colorPanel = null;

   /** a panel to set the width of the outline **/
   private DoubleValuePanel widthPanel = null;

   /** a panel to set whether the plot should be outline or not **/
   private JCheckBox drawBox = null;

   private PrettyOptionImageIconConverter lineStyleConverter;

   /** a label to describe the panel*/
   private JLabel typeLabel;

   /** Creates a new instance of OutLineStylePanel
    * @param label A String to create label name for the panel
    */

   public OutlineStylePanel(String label)
   {

      this.setLayout(new GridLayout(1,5,3,3));
      typeLabel = new JLabel(label);
      this.add(typeLabel);

      drawBox = new JCheckBox();
      drawBox.addItemListener(new ItemListener()
      {
         public void itemStateChanged(ItemEvent ie)
         {
            boolean enable = (ie.getStateChange() == ItemEvent.SELECTED);

            stylePanel.setEnabled(enable);
            colorPanel.setEnabled(enable);
            widthPanel.setEnabled(enable);
         }
      });
      drawBox.setAlignmentX(Component.CENTER_ALIGNMENT);

      //instantiate converter to load the pretty image icons into image chooser panel
      lineStyleConverter = PrettyOptionImageIconConverter.getLineStyleConverter();
      ImageIcon [] imageIcons = lineStyleConverter.getAllPrettyOptions();
      stylePanel = new ImageChooserPanel("",false,imageIcons);

      colorPanel = new ColorValuePanel("", false);
      colorPanel.setAlignmentX(Component.RIGHT_ALIGNMENT);

      widthPanel = new DoubleValuePanel("", false, 0.001, Double.POSITIVE_INFINITY);
      widthPanel.setToolTipText("The width of the border in pixels.");
   }


   /**
    * Return the GUI components used to build this panel. This is so that
    * the parent panel can take control of layout.
    */
   protected JComponent[] getGUIComponents()
   {
      JComponent[] retval = new JComponent[5];

      retval[0] = typeLabel;
      retval[1] = drawBox;
      retval[2] = stylePanel;
      retval[3] = colorPanel;
      retval[4] = widthPanel;

      return retval;
   } // getGUIComponents()

   /** Initialize contents of the panel from the value of the data object
    * @param outlineType  A data object of type OutlineType
    * @param type int type will denote one of this choices: OutlineType.PLOT,
    * OutlineType.FIGURE, OutlineType.INNER, OR OutlineType.OUTER
    */
   protected void initModel(OutlineType outlineType, int type)
   {
//System.out.println("type = "+type);
//System.out.println("outlineType.getDraw(type)="+outlineType.getDraw(type));
      drawBox.setSelected(outlineType.getDraw(type));
      stylePanel.setValue(lineStyleConverter.getPrettyOption(
      outlineType.getLineStyle(type)));
      colorPanel.setValue(outlineType.getColor(type));
      widthPanel.setValue(outlineType.getLineWidth(type));
   }

   /**
    * A method to store info from the contens of this panel in the data object
    * @param outlineType  A data object of type OutlineType
    * @param type int type will denote one of this choices: OutlineType.PLOT,
    * OutlineType.FIGURE, OutlineType.INNER, OR OutlineType.OUTER
    * @throws Exception
    */
   protected void saveModel(OutlineType outlineType, int type)throws Exception
   {
//System.out.println("type = "+type);
//System.out.println("drawBox.isSelected()="+drawBox.isSelected());
      outlineType.setDraw(type, drawBox.isSelected());

      outlineType.setLineStyle(type,
      lineStyleConverter.getSystemOption(stylePanel.getValue()));
      if (!colorPanel.getBackground().equals(colorPanel.getValue()))
      {
         outlineType.setColor(type, colorPanel.getValue());
      }
      outlineType.setLineWidth(type, widthPanel.getValue());
   }

   /**
    * This will enable or disable the components of this panel
    * @param enable
    */
   public void setEnabled(boolean enable)
   {
      stylePanel.setEnabled(enable);
      colorPanel.setEnabled(enable);
      widthPanel.setEnabled(enable);
      drawBox.setSelected(enable);
   }

   public static void main(String arg[])
   {
      JFrame frame = new JFrame();
      OutlineStylePanel outlineEditor = new OutlineStylePanel("Plot");
      frame.getContentPane().add(outlineEditor);
      frame.pack();
      frame.setVisible(true);

   }
}

