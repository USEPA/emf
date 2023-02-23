
package gov.epa.mims.analysisengine.gui;

import gov.epa.mims.analysisengine.tree.TextBox;
import gov.epa.mims.analysisengine.tree.TextBoxArrow;

import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.ArrayList;
import javax.swing.*;

/*
 * TextBoxEditor.java
 * A editor for the Text Box. At present this box will only edit the configuration
 * that are related Box configuration and text wrapping
 * @author  Parthee R Partheepan
 * @version $Id: TextBoxEditor.java,v 1.2 2005/09/19 14:50:03 rhavaldar Exp $
 */
public class TextBoxEditor extends TextBorderEditor
{

   /** a integer value panel to specify the no of character before wrapping
    * the line */
   private IntegerValuePanel wrapIPanel;

   /** a integer value panel to specify the indent for the new lines */
   private IntegerValuePanel indentIPanel;

   /** a integer value panel to specify the exdent for the lines created by wrapping
    */
   private IntegerValuePanel exdentIPanel;

   /** a double value panel to specify the  vertical spacing between lines */
   private DoubleValuePanel lineSpacingIPanel;

   /** a double value panel to specify the text x alignment inside the box */
   private DoubleValuePanel xTxtAlignmentDPanel;

   /** a double value panel to specify the text y alignment inside the box */
   private DoubleValuePanel yTxtAlignmentDPanel;

   //box size
   /** a double value panel to specify the width of the box*/
   private DoubleValuePanel  widthDPanel;

   /** a double value panel to specify the height of the box*/
   private DoubleValuePanel  heightDPanel;

   /** A radio button for the TextBox.MAX_WIDTH = Variable */
   private JRadioButton variableWidthRB;

   /** A radio button for the TextBox.ABSOLUTE_WIDTH = Fixed */
   private JRadioButton fixedWidthRB;

   /** a String chooserPanel for to specify the unit for the box
   * TextBox.USER_UNITS = User Specified
   * TextBox.FIGURE_UNITS = Relative to Figure
   * TextBox.DEVICE_UNITS = Device
   */
   private StringChooserPanel unitSPanel;

   //position
   /** a radio button to select the absolute option for position*/
   private JRadioButton absPosRB;

   /** a radio button to select the grid option for position*/
   private JRadioButton gridPosRB;

   /** a string chooser panel for the region selection for the box position */
   private StringChooserPanel regionSPanel;

   /** a pretty string converter for converting region strings */
   private PrettyOptionStringConverter regionConverter;

   /** a pretty string converter for converting sector strings */
   private PrettyOptionStringConverter sectorConverter;

   /** A String chooser panel for the sector selection for the box position */
   private StringChooserPanel sectorSPanel;

   /** A String chooser panel for the unit selction for the box position */
   private StringChooserPanel unitPosSPanel;

   /** a double value panel for the x box position */
   private DoubleValuePanel xPosDPanel;

   /** a double value panel for the y box position */
   private DoubleValuePanel yPosDPanel;


   /** a tabbed pane to store the different editor panels */
   private JTabbedPane tabbedPane;

   /** a double value panel for the horizontal aligment */
   private DoubleValuePanel horAlignmentDPanel;

   /** a double value panel for the y box position */
   private DoubleValuePanel verAlignmentDPanel;

   /** A String Chooser panel for selecting the clippinf */
   private StringChooserPanel clipSPanel;

   /** An ArrowsEditor to edit the text box arrows */
   private ArrowsEditor arrowsEditor;


      /** indicate Max Width in the TextBox */
   public static final String MAX_WIDTH = "Variable" ;

   /** indicate Absolute Width in the TextBox */
   public static final String ABSOLUTE_WIDTH = "Fixed";

   /** indicate User Units in the TextBox */
   public static final String USER_UNITS = "Data Coordinates";

   /** indicate FIGURE_UNITS in the TextBox */
   public static final String FIGURE_UNITS = "% of Figure";

    /** indicate DEVICE_UNITS in the TextBox */
   public static final String DEVICE_UNITS = "% of Page";

   /** indicate DEVICE clipping in the TextBox */
   public static final String DEVICE = "None ";

   /** indicate PLOT clipping in the TextBox */
   public static final String PLOT = "Plot";

   /** indicate FIGURE clipping in the TextBox */
   public static final String FIGURE = "Figure";

   /**  a hash map for geting pretty unit from using system unit as a key */
   public static HashMap unitPrettyHashMap;

   /**  a hash map for geting system unit from using pretty unit as a key */
   public static HashMap unitSystemHashMap;

   /**  a hash map for geting pretty type from using system type as a key */
   public static HashMap typePrettyHashMap;

   /**  a hash map for geting system type from using pretty type as a key */
   public static HashMap typeSystemHashMap;

   /**  a hash map for geting pretty clipping style from using system clipping style as a key */
   public static HashMap clipStylePrettyHashMap;

   /**  a hash map for geting system clipping style from using pretty clipping as a key */
   public static HashMap clipStyleSystemHashMap;

   /** first tab no */
   public static final int TEXTBORDER_TAB = 0;

   /** second tab */
   public static final int TEXTBOX_TAB = 1;

   /** third tab */
   public static final int ARROWS_TAB = 2;

   static
   {
      unitPrettyHashMap = new HashMap();
      unitPrettyHashMap.put(Integer.valueOf(TextBox.DEVICE_UNITS),DEVICE_UNITS);
      unitPrettyHashMap.put(Integer.valueOf(TextBox.FIGURE_UNITS),FIGURE_UNITS);
      unitPrettyHashMap.put(Integer.valueOf(TextBox.USER_UNITS),USER_UNITS);
      unitSystemHashMap = new HashMap();
      unitSystemHashMap.put(DEVICE_UNITS, Integer.valueOf(TextBox.DEVICE_UNITS));
      unitSystemHashMap.put(FIGURE_UNITS, Integer.valueOf(TextBox.FIGURE_UNITS));
      unitSystemHashMap.put(USER_UNITS, Integer.valueOf(TextBox.USER_UNITS));
      typePrettyHashMap = new HashMap();
      typePrettyHashMap.put(Integer.valueOf(TextBox.MAX_WIDTH), MAX_WIDTH);
      typePrettyHashMap.put(Integer.valueOf(TextBox.ABSOLUTE_WIDTH), ABSOLUTE_WIDTH);
      typeSystemHashMap = new HashMap();
      typeSystemHashMap.put(MAX_WIDTH, Integer.valueOf(TextBox.MAX_WIDTH));
      typeSystemHashMap.put(ABSOLUTE_WIDTH, Integer.valueOf(TextBox.ABSOLUTE_WIDTH));
      clipStylePrettyHashMap = new HashMap();
      clipStylePrettyHashMap.put(Integer.valueOf(TextBox.DEVICE),DEVICE);
      clipStylePrettyHashMap.put(Integer.valueOf(TextBox.PLOT),PLOT);
      clipStylePrettyHashMap.put(Integer.valueOf(TextBox.FIGURE),FIGURE);
      clipStyleSystemHashMap = new HashMap();
      clipStyleSystemHashMap.put(DEVICE, Integer.valueOf(TextBox.DEVICE));
      clipStyleSystemHashMap.put(PLOT, Integer.valueOf(TextBox.PLOT));
      clipStyleSystemHashMap.put(FIGURE,Integer.valueOf(TextBox.FIGURE));
   }
   /** Creates a new instance of TextBoxEditor */
   public TextBoxEditor(TextBox aTextBox)
   {
      super(true);
      isReferenceLine = false;
      text = aTextBox;
      initialize();
      setDataSource(aTextBox, "");
   }//TextBoxEditor

   /**
   * constructor need for class.newInstance
   */
   public TextBoxEditor()
   {
      this(null);
   }//TextBoxEditor

    /**
    * Set the data source for the editor: tree.TextBox
    * @param dataSource source of the data of type tree.TextBox
    * @param optionName String title for the dialog
    */
   public void setDataSource(Object dataSource, String optionName)
   {
      super.setDataSource(dataSource, optionName);
      if (text != null)
      {
         initGUIFromModel();
      }
      pack();
      setLocation(ScreenUtils.getPointToCenter(this));
      this.repaint();
   }//setDataSource()

   /** initilize the gui components */
   protected void initialize()
   {
      this.setModal(true);

      JPanel textPanel = createTextPanel();


      JPanel borderPanel = createBorderEditorPanel();
      JPanel textBorderPanel = new JPanel();
      textBorderPanel.setLayout(new BoxLayout(textBorderPanel, BoxLayout.Y_AXIS));
      textBorderPanel.add(textPanel);
      textBorderPanel.add(borderPanel);

      JPanel boxEditorPanel = createBoxEditorPanel();

      arrowsEditor = new ArrowsEditor(null);

      tabbedPane = new JTabbedPane();
      tabbedPane.insertTab("Text & Border", null, textBorderPanel,
         "An Editor for the Text and border properties ", TEXTBORDER_TAB);
      tabbedPane.insertTab("Box", null, boxEditorPanel,
         "An Editor for the box properties ", TEXTBOX_TAB);
      tabbedPane.insertTab("Arrows", null, arrowsEditor,
         "An Editor for the arrow properties ", ARROWS_TAB);

      Container container = getContentPane();
      container.setLayout(new BoxLayout(container,BoxLayout.Y_AXIS));
      container.add(tabbedPane);
      container.add(getButtonPanel());

   }//initialize()

   /** a helper method to create the box editor panel
    */
   private JPanel createBoxEditorPanel()
   {
      xTxtAlignmentDPanel = new DoubleValuePanel("Horizontal ",false,0.0, 1.0);
      xTxtAlignmentDPanel.setToolTipText(
         "Text alignment inside the box: values between 0 and 1 (0=left, 0.5=middle and 1=right)");

      yTxtAlignmentDPanel = new DoubleValuePanel("Vertical ",false,0.0, 1.0);
      yTxtAlignmentDPanel.setToolTipText(
         "Text alignment inside the box: values between 0 and 1 (0=bottom, 0.5=middle and 1=top)");
      JPanel alignmentPanel = new JPanel();
      //alignmentPanel.setLayout(new BoxLayout(alignmentPanel, BoxLayout.X_AXIS));
      alignmentPanel.setBorder(BorderFactory.createTitledBorder(
         BorderFactory.createLineBorder(Color.black),"Text Alignment "));
      alignmentPanel.add(xTxtAlignmentDPanel);
      alignmentPanel.add(yTxtAlignmentDPanel);

      wrapIPanel = new IntegerValuePanel("Characters per Line ",false,
         -1, Integer.MAX_VALUE);
      wrapIPanel.setToolTipText("Number of characters that will be " +
         "allowed per line: values > 0");
      lineSpacingIPanel = new DoubleValuePanel("Line Spacing",false,
         0.0, 1.0);
      lineSpacingIPanel.setToolTipText("Vertical spacing between two lines: values between 0.0 and 1.0");
      indentIPanel = new IntegerValuePanel("First Line Indent ",false,
         0, Integer.MAX_VALUE);
      indentIPanel.setToolTipText("Number of characters that will be " +
         "indented for the new lines: values > 0");
      exdentIPanel = new IntegerValuePanel("Indent for Wrapped Lines",false,
         0, Integer.MAX_VALUE);
      exdentIPanel.setToolTipText("Number of characters that will be " +
         "indented for the lines create by wrapping: values > 0");

      JPanel wrapPanel = new JPanel();
      wrapPanel.setLayout(new BoxLayout(wrapPanel, BoxLayout.X_AXIS));
      wrapPanel.add(wrapIPanel);
      wrapPanel.add(Box.createHorizontalStrut(10));
      wrapPanel.add(lineSpacingIPanel);
      wrapPanel.add(Box.createHorizontalGlue());

      JPanel indentPanel = new JPanel();
      indentPanel.add(indentIPanel);
      indentPanel.add(exdentIPanel);

      JPanel lineConfigPanel = new JPanel();
      lineConfigPanel.setLayout(new BoxLayout(lineConfigPanel, BoxLayout.Y_AXIS));
      lineConfigPanel.setBorder(BorderFactory.createTitledBorder(
         BorderFactory.createLineBorder(Color.black),"Line Configuration"));
      lineConfigPanel.add(wrapPanel);
      lineConfigPanel.add(indentPanel);

      //box configuration
      //size
      String [] unitOptions = {USER_UNITS, FIGURE_UNITS, DEVICE_UNITS};
      unitSPanel = new StringChooserPanel("Units", false, unitOptions);
      unitSPanel.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            setToolTipAndBoundForSize();
         }
      });

      unitSPanel.setToolTipText("The unit for box size to be specified in the "
         + "height and width text boxes");

      String [] clipOptions = {DEVICE,PLOT,FIGURE};
      clipSPanel = new StringChooserPanel("Clipping Style",false, clipOptions);
      clipSPanel.setToolTipText("Clip the portion of the text box on the selected area.");
      JPanel typeUnitPanel = new JPanel();
      typeUnitPanel.add(unitSPanel);
      typeUnitPanel.add(clipSPanel);

      widthDPanel = new DoubleValuePanel("Width",false);
      heightDPanel = new DoubleValuePanel("Height",false);
      setToolTipAndBoundForSize();
      variableWidthRB = new JRadioButton("Variable Width",true);
      variableWidthRB.setToolTipText("Size of the text box will vary upto the "+
         "maximum size specified by the width text box ");
      fixedWidthRB = new JRadioButton("Fixed Width",false);
      fixedWidthRB.setToolTipText("Size of the text box is fixed and "+
         "specified by the width text box ");
      ButtonGroup group = new ButtonGroup();
      group.add(variableWidthRB);
      group.add(fixedWidthRB);
      JPanel sizePanel = new JPanel();
      //sizePanel.setLayout(new BoxLayout(sizePanel,BoxLayout.X_AXIS));
      sizePanel.add(heightDPanel);
      sizePanel.add(widthDPanel);
      sizePanel.add(variableWidthRB);
      sizePanel.add(fixedWidthRB);

      //typeUnitPanel.setLayout(new BoxLayout(typeUnitPanel,BoxLayout.X_AXIS));
      //typeUnitPanel.add(typeSPanel);


      JPanel sizeConfigPanel = new JPanel();
      sizeConfigPanel.setLayout(new BoxLayout(sizeConfigPanel, BoxLayout.Y_AXIS));
      sizeConfigPanel.setBorder(BorderFactory.createTitledBorder(
         BorderFactory.createLineBorder(Color.black),"Box Size"));
      sizeConfigPanel.add(typeUnitPanel);
      sizeConfigPanel.add(sizePanel);

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

//      JPanel positionRBPanel = new JPanel();
//      positionRBPanel.setLayout(new BoxLayout(positionRBPanel, BoxLayout.Y_AXIS));
//      positionRBPanel.add(Box.createVerticalStrut(10));
//      positionRBPanel.add(gridPosRB);
//      positionRBPanel.add(Box.createVerticalStrut(20));
//      positionRBPanel.add(absPosRB);


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
      horAlignmentDPanel.setToolTipText("Horizontal aligment of the box: " +
      "values between 0 and 1 (0=left, 0.5=middle and 1=right)");
      verAlignmentDPanel = new DoubleValuePanel("Vertical",false,0.0,1.0);
      verAlignmentDPanel.setToolTipText("Vertical aligment of the box: " +
      "values between 0 and 1 (0=bottom, 0.5=middle and 1=top)");
      JPanel boxAlignmentPanel = new JPanel();
      boxAlignmentPanel.add(alignLabel);
      //boxAlignmentPanel.add(Box.createHorizontalStrut(15));
      boxAlignmentPanel.add(horAlignmentDPanel);
      boxAlignmentPanel.add(verAlignmentDPanel);

//      JPanel gridAbsPanel = new JPanel();
//      gridAbsPanel.setLayout(new BoxLayout(gridAbsPanel, BoxLayout.Y_AXIS));
//      gridAbsPanel.add(gridPanel);
//      gridAbsPanel.add(absPanel);

//      JPanel gridAbsWithButPanel = new JPanel(new BorderLayout());
//      gridAbsWithButPanel.add(positionRBPanel,BorderLayout.WEST);
//      gridAbsWithButPanel.add(gridAbsPanel, BorderLayout.CENTER);

      JPanel positionPanel = new JPanel();
      positionPanel.setLayout(new BoxLayout(positionPanel, BoxLayout.Y_AXIS));
      positionPanel.setBorder(BorderFactory.createTitledBorder(
         BorderFactory.createLineBorder(Color.black),"Box Position"));

      positionPanel.add(gridPanel);
      positionPanel.add(absPanel);
      positionPanel.add(boxAlignmentPanel);

      //arrows editor
//      arrowsEditor = new ArrowsEditor(null);
//      JPanel arrowsPanel = new JPanel(new BorderLayout());
//      arrowsPanel.setBorder(BorderFactory.createCompoundBorder(
//         BorderFactory.createTitledBorder(
//            BorderFactory.createLineBorder(Color.black)," Arrows "),
//         BorderFactory.createRaisedBevelBorder()));
//      arrowsPanel.add(arrowsEditor);

      //initialization
      boolean selected = absPosRB.isSelected();
      regionSPanel.setEnabled(!selected);
      sectorSPanel.setEnabled(!selected);
      unitPosSPanel.setEnabled(selected);
      xPosDPanel.setEnabled(selected);
      yPosDPanel.setEnabled(selected);

      JPanel mainPanel = new JPanel();
      mainPanel.setBorder(BorderFactory.createCompoundBorder(
         BorderFactory.createEmptyBorder(4,4,4,4),
         BorderFactory.createCompoundBorder(
            BorderFactory.createLoweredBevelBorder(),
            BorderFactory.createEmptyBorder(4,4,4,4))));

      mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
      mainPanel.add(alignmentPanel);
      mainPanel.add(lineConfigPanel);
      mainPanel.add(sizeConfigPanel);
      mainPanel.add(positionPanel);
//      mainPanel.add(arrowsPanel);

      return mainPanel;
   }//createBoxEditorPanel()


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

   /** a helper method to set the bound and the tool tips */
   private void setToolTipAndBoundForSize()
   {
      String sizeUnit = unitSPanel.getValue();
      if(sizeUnit.equals(FIGURE_UNITS))
      {
         heightDPanel.setBounds(0.0,1.0);
         heightDPanel.setToolTipText("Height of the box relative to height of the figure: values between 0 and 1.");
         widthDPanel.setBounds(0.0, 1.0);
         widthDPanel.setToolTipText("Width of the box relative to width of the figure: values between 0 and 1.");
      }
      else if(sizeUnit.equals(DEVICE_UNITS))
      {
         heightDPanel.setBounds(0.0,1.0);
         heightDPanel.setToolTipText("Height of the box relative to height of the page: values between 0 and 1.");
         widthDPanel.setBounds(0.0, 1.0);
         widthDPanel.setToolTipText("Width of the box relative to width of the page: values between 0 and 1.");
      }
      else //if(sizeUnit.equals(USER_UNITS))
      {
         heightDPanel.setBounds(-Double.MAX_VALUE,Double.MAX_VALUE);
         heightDPanel.setToolTipText("Height of the box relative to data coordinate values");
         widthDPanel.setBounds(-Double.MAX_VALUE,Double.MAX_VALUE);
         widthDPanel.setToolTipText("Width of the box relative to data coordinate values");
      }
   }//setToolTipAndBoundForSize()

    /** a helper method to set the bound and the tool tips */
   private void setToolTipAndBoundForPosition()
   {
      String posUnit = unitPosSPanel.getValue();
      if(posUnit.equals(FIGURE_UNITS))
      {
         xPosDPanel.setBounds(0.0,1.0);
         xPosDPanel.setToolTipText("Position of the box relative to height of "+
            "the figure: values between 0 and 1. "+
            "(0=left, 0.5=middle and 1=right)");
         yPosDPanel.setBounds(0.0,1.0);
         yPosDPanel.setToolTipText("Position of the box relative to width of " +
            "the figure: values between 0 and 1." +
            "(0=bottom, 0.5=middle and 1=top)");
      }
      else if(posUnit.equals(DEVICE_UNITS))
      {
         xPosDPanel.setBounds(0.0,1.0);
         xPosDPanel.setToolTipText("Position of the box relative to height of "+
            "the page: values between 0 and 1." +
            "(0=left, 0.5=middle and 1=right)");
         yPosDPanel.setBounds(0.0,1.0);
         yPosDPanel.setToolTipText("Position of the box relative to width of " +
         "the page: values between 0 and 1. " +
         "(0=bottom, 0.5=middle and 1=top)");
      }
      else //if(sizeUnit.equals(USER_UNITS))
      {
         xPosDPanel.setBounds(-Double.MAX_VALUE, Double.MAX_VALUE);
         xPosDPanel.setToolTipText("Position of the box relative to data coordinate values.");
         yPosDPanel.setBounds(-Double.MAX_VALUE, Double.MAX_VALUE);
         yPosDPanel.setToolTipText("Position of the box relative to data coordinate values.");
      }
   }

   protected void initGUIFromModel()
   {
      tabbedPane.setSelectedIndex(TextBoxesTypeEditor.rememberTabNo);
      super.initGUIFromModel();

      TextBox aTextBox = (TextBox)text;
      xTxtAlignmentDPanel.setValue(aTextBox.getTextXjustification());
      yTxtAlignmentDPanel.setValue(aTextBox.getTextYjustification());
      wrapIPanel.setValue(aTextBox.getWrap());
      indentIPanel.setValue(aTextBox.getIndent());
      exdentIPanel.setValue(aTextBox.getExdent());
      lineSpacingIPanel.setValue(aTextBox.getVSpace());
      widthDPanel.setValue(aTextBox.getBoxWidth());
      heightDPanel.setValue(aTextBox.getBoxHeight());
      int type = aTextBox.getBoxWHtype();
      String stringType = getPrettyType(this,type);
      if(stringType.equals(MAX_WIDTH))
      {
         variableWidthRB.setSelected(true);
      }
      else if(stringType.equals(ABSOLUTE_WIDTH))
      {
         fixedWidthRB.setSelected(true);
      }
      else
      {
         DefaultUserInteractor.get().notify(this, "Error", "The " + stringType + " is " +
         " not a recognized type.",UserInteractor.ERROR);
      }

      String stringSizeUnit = getPrettyUnit(this,aTextBox.getUnitsWH());
      unitSPanel.setValue(stringSizeUnit);
      String clipStyle = getPrettyClipStyle(aTextBox.getXpd());
      clipSPanel.setValue(clipStyle);

      String region = aTextBox.getRegion();
      if(region != null)
      {
         regionSPanel.setValue(regionConverter.getPrettyOption(region));
      }
      String sector = aTextBox.getPosition();

      if(sector !=null)
      {
         sectorSPanel.setValue(sectorConverter.getPrettyOption(sector));
      }
      if(region == null && sector == null)
      {
         absPosRB.setSelected(true);
         enablePositionPanel(true);
      }
      else
      {
         gridPosRB.setSelected(true);
         enablePositionPanel(false);
      }
      String  posUnit = getPrettyUnit(this,aTextBox.getUnitsXY());
      unitPosSPanel.setValue(posUnit);
      xPosDPanel.setValue(aTextBox.getX());
      yPosDPanel.setValue(aTextBox.getY());
      horAlignmentDPanel.setValue(aTextBox.getXJustification());
      verAlignmentDPanel.setValue(aTextBox.getYJustification());

      int noOfArrows = aTextBox.getNumArrows();
      ArrayList arrows = new ArrayList();
      for(int i=0; i< noOfArrows; i++)
      {
         arrows.add(i, aTextBox.getArrow(i));
      }
      arrowsEditor.setTextBoxArrows(arrows);

   }//initGUIFromModel()

   protected void saveGUIValuesToModel() throws Exception
   {
      TextBoxesTypeEditor.rememberTabNo = tabbedPane.getSelectedIndex();
      super.saveGUIValuesToModel();
      TextBox aTextBox = (TextBox)text;
      aTextBox.setTextJustification(xTxtAlignmentDPanel.getValue(),
         yTxtAlignmentDPanel.getValue());
      aTextBox.setWrap(wrapIPanel.getValue(), indentIPanel.getValue(),
         exdentIPanel.getValue());
      aTextBox.setVSpace(lineSpacingIPanel.getValue());
      int sizeUnit = getSystemUnit(this,unitSPanel.getValue());
      int sizeType = -1;
      if(variableWidthRB.isSelected())
      {
         sizeType = getSystemType(this,MAX_WIDTH);
      }
      else if(fixedWidthRB.isSelected())
      {
         sizeType = getSystemType(this,ABSOLUTE_WIDTH);
      }
      aTextBox.setBoxWidthAndHeight(widthDPanel.getValue(), heightDPanel.getValue(),
         sizeUnit, sizeType);
      int clipStyle = getSystemClipStyle(clipSPanel.getValue());
      aTextBox.setXpd(clipStyle);

      if(absPosRB.isSelected())
      {
         int posUnit = getSystemUnit(this,unitPosSPanel.getValue());
         aTextBox.setPosition(xPosDPanel.getValue(),yPosDPanel.getValue(),
            posUnit,horAlignmentDPanel.getValue(),
            verAlignmentDPanel.getValue());
      }
      else //gridPosRB is selected
      {
         String region = regionConverter.getSystemOption(regionSPanel.getValue());
         String sector = sectorConverter.getSystemOption(sectorSPanel.getValue());
         aTextBox.setPosition(region,sector,horAlignmentDPanel.getValue(),
            verAlignmentDPanel.getValue());
      }//else
      aTextBox.clearArrows();
      ArrayList arrows = arrowsEditor.getTextBoxArrows();
      if(arrows != null && arrows.size() != 0)
      {
         for(int i=0; i< arrows.size(); i++)
         {
            TextBoxArrow arrow = (TextBoxArrow)arrows.get(i);
            aTextBox.addArrow(arrow);
         }//for(i)
      }//if

   }//saveGUIValuesToModel()

   /** a info string about the box to be displayed on the table
    *
    * @return String
    */
   public String getBoxInfoString()
   {
      TextBox aTextBox = (TextBox)text;
      if(absPosRB.isSelected())
      {
         return "Pos("+ aTextBox.getX()+ ", "+ aTextBox.getY()+ ")";
      }
      else //gridPosRB.isSelected() == true
      {
         return "Pos("+ aTextBox.getRegion()+ ", "+ aTextBox.getPosition()+ ")";
      }
   }

     /**
    * getter for the date source
    * @return Object actual type type TextBox
    */
   public Object getDataSource()
   {
      return (TextBox)text;
   }

   /** a helper methood to get the pretty units
    * @param sytemUnit int value
    * @return String pretty String
    */
   public static String getPrettyUnit(Component parent, int systemUnit)
   {
      Object key = Integer.valueOf(systemUnit);
      String stringUnit = (String)TextBoxEditor.unitPrettyHashMap.get(key);
      if(stringUnit == null)
      {
         DefaultUserInteractor.get().notify(parent, "Error", "The " + systemUnit
            + " is an unexpected unit", UserInteractor.ERROR);
      }//if
      return stringUnit;
   }//getPrettyUnit()

   /** a helper method to get the system units
    * @param pretty String
    * @return int system value
    */
   public static int getSystemUnit(Component parent,String prettyUnit)
   {
      Integer value = (Integer)TextBoxEditor.unitSystemHashMap.get(prettyUnit);
      if(value == null)
      {
         DefaultUserInteractor.get().notify(parent, "Error", "The " + prettyUnit
            + " is an unexpected unit", UserInteractor.ERROR);
      }//if

      return value.intValue();
   }//getSystemUnit()

   /** a helper methood to get the pretty types
    * @param systemType int value
    * @return String pretty String
    */
   public String getPrettyType(Component parent, int systemType)
   {
      Object key = Integer.valueOf(systemType);
      String stringType = (String)TextBoxEditor.typePrettyHashMap.get(key);
      if(stringType == null)
      {
         DefaultUserInteractor.get().notify(parent,"Error", "The " + systemType
            + " is an unexpected type", UserInteractor.ERROR);
      }//if
      return stringType;
   }//getPrettyType()

   /** a helper method to get the system Types
    * @param pretty String
    * @return int system value
    */
   private int getSystemType(Component parent, String prettyType)
   {
      Integer value = (Integer)TextBoxEditor.typeSystemHashMap.get(prettyType);
      if(value == null)
      {
         DefaultUserInteractor.get().notify(parent, "Error", "The " + prettyType
            + " is an unexpected type", UserInteractor.ERROR);
      }//if

      return value.intValue();
   }//getSystemType()

   /** a helper methood to get the pretty clipping style
    * @param sytemUnit int value
    * @return String pretty String
    */
   private String getPrettyClipStyle(int systemClipStyle)
   {
      Object key = Integer.valueOf(systemClipStyle);
      String stringStyle = (String)TextBoxEditor.clipStylePrettyHashMap.get(key);
      if(stringStyle == null)
      {
         DefaultUserInteractor.get().notify(this, "Error", "The " + systemClipStyle
            + " is an unexpected unit", UserInteractor.ERROR);
      }//if
      return stringStyle;
   }//getPrettyClipStyle()

   /** a helper method to get the system clip style
    * @param pretty String
    * @return int system value
    */
   private int getSystemClipStyle(String prettyStyle)
   {
      Integer value = (Integer)TextBoxEditor.clipStyleSystemHashMap.get(prettyStyle);
      if(value == null)
      {
         DefaultUserInteractor.get().notify(this,"Error", "The " + prettyStyle
            + " is an unexpected unit", UserInteractor.ERROR);
      }//if

      return value.intValue();
   }//getSystemClipStyle()


   /**
    * @param args the command line arguments
    */
   public static void main(String[] args)
   {
      DefaultUserInteractor.set(new GUIUserInteractor());
      try
      {
         TextBox aTextBox = new TextBox();
         TextBoxEditor editor = new TextBoxEditor(aTextBox);
         editor.setVisible(true);
      }
      catch(Exception e)
      {
         e.printStackTrace();
      }
   }//main()


}//End
