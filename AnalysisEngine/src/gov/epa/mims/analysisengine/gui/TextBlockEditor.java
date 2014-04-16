
package gov.epa.mims.analysisengine.gui;

import gov.epa.mims.analysisengine.tree.TextBlock;
import gov.epa.mims.analysisengine.tree.TextAttribute;
import gov.epa.mims.analysisengine.tree.BackgroundBox;
import gov.epa.mims.analysisengine.tree.Border;

import java.awt.Color;
import java.awt.event.*;
import java.awt.BorderLayout;
import java.util.HashMap;
import javax.swing.*;


/*
 * TextBlockEditor.java
 * An editor for the TextBlock
 * @author Parthee Partheepan UNC
 * @version $Id: TextBlockEditor.java,v 1.2 2005/09/19 14:50:03 rhavaldar Exp $
 */
public class TextBlockEditor extends JPanel
{
   private TextBlock txtBlock;

   private DoubleValuePanel txtSizePanel;

   private ColorValuePanel txtColorPanel;

   private StringChooserPanel txtFontPanel;

   private PrettyOptionStringConverter fontConverter;

   private JRadioButton absPosRB;

   private JRadioButton gridPosRB;

   private PrettyOptionStringConverter regionConverter;

   private StringChooserPanel regionSPanel;

   private PrettyOptionStringConverter sectorConverter;

   private StringChooserPanel sectorSPanel;

   private StringChooserPanel unitPosSPanel;

   private DoubleValuePanel xPosDPanel;

   private DoubleValuePanel yPosDPanel;

   private DoubleValuePanel horAlignmentDPanel;

   private DoubleValuePanel verAlignmentDPanel;

   private BackgroundBoxEditor bgBoxEditor;

   /** indicate User Units in the TextBlock */
   public static final String USER_UNITS = "Data Coordinates";

   /** indicate FIGURE_UNITS in the TextB */
   public static final String FIGURE_UNITS = "% of Figure";

    /** indicate DEVICE_UNITS in the TextBlock */
   public static final String DEVICE_UNITS = "% of Page";

     /**  a hash map for geting pretty unit from using system unit as a key */
   public static HashMap unitPrettyHashMap;

   /**  a hash map for geting system unit from using pretty unit as a key */
   public static HashMap unitSystemHashMap;

   static
   {
      unitPrettyHashMap = new HashMap();
      unitPrettyHashMap.put(new Integer(TextBlock.DEVICE_UNITS),DEVICE_UNITS);
      unitPrettyHashMap.put(new Integer(TextBlock.FIGURE_UNITS),FIGURE_UNITS);
      unitPrettyHashMap.put(new Integer(TextBlock.USER_UNITS),USER_UNITS);
      unitSystemHashMap = new HashMap();
      unitSystemHashMap.put(DEVICE_UNITS, new Integer(TextBlock.DEVICE_UNITS));
      unitSystemHashMap.put(FIGURE_UNITS, new Integer(TextBlock.FIGURE_UNITS));
      unitSystemHashMap.put(USER_UNITS, new Integer(TextBlock.USER_UNITS));
   }
   /** Creates a new instance of TextBlockEditor */
   public TextBlockEditor()
   {
      initialize();
   }

   private void initialize()
   {
      txtColorPanel = new ColorValuePanel("Color", false);
      txtSizePanel = new DoubleValuePanel("Size",false,0.0, 3.0);
      fontConverter = PrettyOptionStringConverter.getFontStyleConverter();
      String [] fonts = fontConverter.getAllPrettyOptions();
      txtFontPanel = new StringChooserPanel("Style",false,fonts);
      JPanel txtPanel = new JPanel();
      txtPanel.setLayout(new BoxLayout(txtPanel, BoxLayout.X_AXIS));
      txtPanel.setBorder(BorderFactory.createTitledBorder(
         BorderFactory.createLineBorder(Color.black),"Text"));
      txtPanel.add(txtColorPanel);
      txtPanel.add(txtSizePanel);
      txtPanel.add(txtFontPanel);
      //Position
      absPosRB = new JRadioButton("Data ",true);
      gridPosRB = new JRadioButton("Grid ",false);
      ButtonGroup group2 = new ButtonGroup();
      group2.add(absPosRB);
      group2.add(gridPosRB);
      absPosRB.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
            boolean selected = absPosRB.isSelected();
            enablePositionPanel(selected);
        }
      });
      gridPosRB.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
            boolean selected = gridPosRB.isSelected();
            enablePositionPanel(!selected);
        }
      });

      regionConverter = PrettyOptionStringConverter.getRegionConverter();
      String [] regions = regionConverter.getAllPrettyOptions();
      regionSPanel = new StringChooserPanel("Region", false, regions);
      sectorConverter = PrettyOptionStringConverter.getSectorConverter();
      String [] sectors = sectorConverter.getAllPrettyOptions();
      sectorSPanel = new StringChooserPanel("Sector", false, sectors);
      JPanel gridPanel = new JPanel();
      gridPanel.add(gridPosRB);
      gridPanel.add(Box.createHorizontalGlue());
      gridPanel.add(Box.createHorizontalStrut(10));
      gridPanel.add(regionSPanel);
      gridPanel.add(sectorSPanel);
      String [] unitOptions = {USER_UNITS, FIGURE_UNITS, DEVICE_UNITS};
      unitPosSPanel = new StringChooserPanel("Units", false, unitOptions);
      unitPosSPanel.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            setToolTipAndBoundForPosition();
         }
      });
      xPosDPanel = new DoubleValuePanel("X: ",false);
      yPosDPanel = new DoubleValuePanel("Y: ",false);
      setToolTipAndBoundForPosition();
      JPanel absPanel = new JPanel();
      absPanel.add(absPosRB);
      absPanel.add(unitPosSPanel);
      absPanel.add(xPosDPanel);
      absPanel.add(yPosDPanel);

      JLabel alignLabel = new JLabel("Alignment: ");
      horAlignmentDPanel = new DoubleValuePanel("Horizontal",false,0.0,1.0);
      horAlignmentDPanel.setToolTipText("Horizontal aligment of the block: " +
      "values between 0 and 1 (0=left, 0.5=middle and 1=right)");
      verAlignmentDPanel = new DoubleValuePanel("Vertical",false,0.0,1.0);
      verAlignmentDPanel.setToolTipText("Vertical aligment of the block: " +
      "values between 0 and 1 (0=bottom, 0.5=middle and 1=top)");
      JPanel blockAlignmentPanel = new JPanel();
      blockAlignmentPanel.add(alignLabel);
      //blockAlignmentPanel.add(Box.createHorizontalStrut(15));
      blockAlignmentPanel.add(horAlignmentDPanel);
      blockAlignmentPanel.add(verAlignmentDPanel);


      JPanel positionPanel = new JPanel();
      positionPanel.setLayout(new BoxLayout(positionPanel, BoxLayout.Y_AXIS));
      positionPanel.setBorder(BorderFactory.createTitledBorder(
         BorderFactory.createLineBorder(Color.black),"Position"));

      positionPanel.add(gridPanel);
      positionPanel.add(absPanel);
      positionPanel.add(blockAlignmentPanel);

      //initialization
      boolean selected = absPosRB.isSelected();
      regionSPanel.setEnabled(!selected);
      sectorSPanel.setEnabled(!selected);
      unitPosSPanel.setEnabled(selected);
      xPosDPanel.setEnabled(selected);
      yPosDPanel.setEnabled(selected);

      this.setLayout(new BorderLayout());
      this.add(txtPanel,BorderLayout.NORTH);
      this.add(positionPanel, BorderLayout.CENTER);

   }
      /** a helper method to set the bound and the tool tips */
   private void setToolTipAndBoundForPosition()
   {
      String posUnit = unitPosSPanel.getValue();
      if(posUnit.equals(FIGURE_UNITS))
      {
         xPosDPanel.setBounds(0.0,1.0);
         xPosDPanel.setToolTipText("Position of the block relative to height of "+
            "the figure: values between 0 and 1. "+
            "(0=left, 0.5=middle and 1=right)");
         yPosDPanel.setBounds(0.0,1.0);
         yPosDPanel.setToolTipText("Position of the block relative to width of " +
            "the figure: values between 0 and 1." +
            "(0=bottom, 0.5=middle and 1=top)");
      }
      else if(posUnit.equals(DEVICE_UNITS))
      {
         xPosDPanel.setBounds(0.0,1.0);
         xPosDPanel.setToolTipText("Position of the block relative to height of "+
            "the page: values between 0 and 1." +
            "(0=left, 0.5=middle and 1=right)");
         yPosDPanel.setBounds(0.0,1.0);
         yPosDPanel.setToolTipText("Position of the block relative to width of " +
         "the page: values between 0 and 1. " +
         "(0=bottom, 0.5=middle and 1=top)");
      }
      else //if(sizeUnit.equals(USER_UNITS))
      {
         xPosDPanel.setBounds(-Double.MAX_VALUE, Double.MAX_VALUE);
         xPosDPanel.setToolTipText("Position of the block relative to data coordinate values.");
         yPosDPanel.setBounds(-Double.MAX_VALUE, Double.MAX_VALUE);
         yPosDPanel.setToolTipText("Position of the block relative to data coordinate values.");
      }
   }
   /** a helper method to enable or disable certain panels in the position panels
    * @param boolean enable
    */
   private void enablePositionPanel(boolean enable)
   {
      regionSPanel.setEnabled(!enable);
      sectorSPanel.setEnabled(!enable);
      unitPosSPanel.setEnabled(enable);
      xPosDPanel.setEnabled(enable);
      yPosDPanel.setEnabled(enable);
   }//enablePositionPanel

   public void initGUIFromModel(TextBlock txtBlock)
   {
      this.txtBlock = txtBlock;
      txtColorPanel.setValue(txtBlock.getColor());
      txtSizePanel.setValue(txtBlock.getCex());
      String prettyOption = fontConverter.getPrettyOption(txtBlock.getFont());
      txtFontPanel.setValue(prettyOption);
      double x = txtBlock.getX();
      double y = txtBlock.getY();
      double xJust = txtBlock.getXJustification();
      double yJust = txtBlock.getYJustification();
      String units = (String)unitPrettyHashMap.get(new Integer(txtBlock.getUnitsXY()));

      String prettyRegion = regionConverter.getPrettyOption(txtBlock.getRegion());
      String prettySector = sectorConverter.getPrettyOption(txtBlock.getPosition());
      boolean abs = false;
      if(Double.isNaN(x) && Double.isNaN(y))
      {
         abs = false;
         xPosDPanel.setValue(xJust);
         yPosDPanel.setValue(yJust);
         regionSPanel.setValue(prettyRegion);
         sectorSPanel.setValue(prettySector);
      }
      else
      {
         abs = true;
         xPosDPanel.setValue(x);
         yPosDPanel.setValue(y);
         unitPosSPanel.setValue(units);
      }
      absPosRB.setSelected(abs);
      gridPosRB.setSelected(!abs);
      enablePositionPanel(abs);
      horAlignmentDPanel.setValue(txtBlock.getXBlockJustification());
      verAlignmentDPanel.setValue(txtBlock.getYBlockJustification());
   }

   public TextBlock saveValuesGUIToModel() throws Exception
   {
      txtBlock.setColor(txtColorPanel.getValue());
      txtBlock.setCex(txtSizePanel.getValue());
      String systemOption = fontConverter.getSystemOption(txtFontPanel.getValue());
      txtBlock.setFont(systemOption);
      if(absPosRB.isSelected())
      {
         double x = xPosDPanel.getValue();
         double y = yPosDPanel.getValue();
         int unit = ((Integer)unitSystemHashMap.get(unitPosSPanel.getValue())).intValue();
         txtBlock.setPosition(x, y, unit, 0.5, 0.5); //temp
      }
      else
      {
         double xJust = xPosDPanel.getValue();
         double yJust = yPosDPanel.getValue();
         String region = regionConverter.getSystemOption(regionSPanel.getValue());
         String sector = sectorConverter.getSystemOption(sectorSPanel.getValue());
         txtBlock.setPosition(region, sector, xJust, yJust);
      }
      txtBlock.setXBlockJustification(horAlignmentDPanel.getValue());
      txtBlock.setXBlockJustification(verAlignmentDPanel.getValue());
      return txtBlock;
   }

   /**
    * @param args the command line arguments
    */
   public static void main(String[] args)
   {
      TextBlockEditor editor = new TextBlockEditor();
      TextBlock tb = new TextBlock();
      editor.initGUIFromModel(tb);
      JFrame f = new JFrame();
      f.getContentPane().add(editor);
      f.pack();
      f.setVisible(true);
   }

}
