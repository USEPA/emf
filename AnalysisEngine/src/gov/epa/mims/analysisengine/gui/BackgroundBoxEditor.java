
package gov.epa.mims.analysisengine.gui;

import gov.epa.mims.analysisengine.tree.BackgroundBox;
import gov.epa.mims.analysisengine.tree.Border;

import java.awt.BorderLayout;
import javax.swing.*;

/*
 * BackgroundBoxEditor.java
 */
public class BackgroundBoxEditor extends JPanel
{
   private BackgroundBox bgBox;

   private BooleanValuePanel enableBoxPanel;

   private ColorValuePanel boxColorPanel;

   private DoubleValuePanel padLeftPanel;

   private DoubleValuePanel padRightPanel;

   private DoubleValuePanel padTopPanel;

   private DoubleValuePanel padBottomPanel;

   //border
   private BooleanValuePanel borderEnablePanel;

   private ColorValuePanel borderColorPanel;

   private DoubleValuePanel borderWidthPanel;



   /** Creates a new instance of BackgroundBoxEditor */
   public BackgroundBoxEditor()
   {
      initialize();
   }

   private void initialize()
   {
      enableBoxPanel = new BooleanValuePanel("Enable Box?",false);

      boxColorPanel = new ColorValuePanel("Box Color", false);
      borderColorPanel = new ColorValuePanel("Border Color",false);
      borderWidthPanel = new DoubleValuePanel("Border Width",false);
      borderEnablePanel = new BooleanValuePanel("Enable Border?");
      JPanel colorWidthPanel = new JPanel();
      //colorWidthPanel.setLayout(new BoxLayout(colorWidthPanel, BoxLayout.X_AXIS));
      colorWidthPanel.add(boxColorPanel);
      colorWidthPanel.add(borderColorPanel);
      colorWidthPanel.add(borderWidthPanel);
      colorWidthPanel.add(borderEnablePanel);

      padLeftPanel = new DoubleValuePanel("Left",false,0, Double.POSITIVE_INFINITY);
      padLeftPanel.setToolTipText("Size of the left padding for the box >0");
      padRightPanel = new DoubleValuePanel("Right",false,0, Double.POSITIVE_INFINITY);
      padRightPanel.setToolTipText("Size of the right padding for the box >0");
      padTopPanel = new DoubleValuePanel("Top",false,0, Double.POSITIVE_INFINITY);
      padTopPanel.setToolTipText("Size of the top padding for the box >0");
      padBottomPanel = new DoubleValuePanel("Bottom",false,0, Double.POSITIVE_INFINITY);
      padBottomPanel.setToolTipText("Size of the botttom padding for the box >0");
      JPanel padPanel = new JPanel();
      //padPanel.setLayout(new BoxLayout(padPanel, BoxLayout.X_AXIS));
      padPanel.add(padLeftPanel);
      padPanel.add(padRightPanel);
      padPanel.add(padTopPanel);
      padPanel.add(padBottomPanel);

      JPanel mainPanel = new JPanel();
      mainPanel.setLayout(new BorderLayout());
      mainPanel.add(enableBoxPanel,BorderLayout.NORTH);
      mainPanel.add(colorWidthPanel,BorderLayout.CENTER);
      mainPanel.add(padPanel,BorderLayout.SOUTH);
      this.add(mainPanel);
   }

   public void initGUIFromModel(BackgroundBox bgBox)
   {
      this.bgBox = bgBox;
      enableBoxPanel.setValue(bgBox.getEnable());
      boxColorPanel.setValue(bgBox.getColor());
      padLeftPanel.setValue(bgBox.getPadLeft());
      padRightPanel.setValue(bgBox.getPadRight());
      padBottomPanel.setValue(bgBox.getPadBottom());
      padTopPanel.setValue(bgBox.getPadTop());

      Border border = bgBox.getBorder();
      borderEnablePanel.setValue(border.getEnable());
      borderColorPanel.setValue(border.getColor());
      borderWidthPanel.setValue(border.getLinewidth());
   }//initGUIFromModel

   public BackgroundBox saveGUIValuesToModel() throws Exception
   {
       bgBox.setEnable(bgBox.getEnable());
      bgBox.setColor(bgBox.getColor());
      double left = padLeftPanel.getValue();
      double right = padRightPanel.getValue();
      double top = padTopPanel.getValue();
      double bottom = padBottomPanel.getValue();
      bgBox.setPadding(right, left, top, bottom);

      Border border = bgBox.getBorder();
      border.setEnable(borderEnablePanel.getValue());
      border.setColor(borderColorPanel.getValue());
      border.setLinewidth(borderWidthPanel.getValue());
      bgBox.setBorder(border);

      return bgBox;
   }

   /**
    * @param args the command line arguments
    */
   public static void main(String[] args)
   {
      BackgroundBoxEditor editor = new BackgroundBoxEditor();
      BackgroundBox box = new BackgroundBox();
      editor.initGUIFromModel(box);
      JFrame f = new JFrame();
      f.getContentPane().add(editor);
      f.pack();
      f.setVisible(true);
   }

}
