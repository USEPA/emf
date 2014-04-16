
package gov.epa.mims.analysisengine.gui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.event.*;
import java.awt.*;

import gov.epa.mims.analysisengine.tree.TextBorder;

/*
 * TextBorderEditor.java
 * A editor for the Text Border
 * @author  Parthee R Partheepan
 * @version $Id: TextBorderEditor.java,v 1.2 2005/09/19 14:50:03 rhavaldar Exp $
 */


public class TextBorderEditor extends TextEditor
{

   /** a panel to set the style of the outline **/
   private ImageChooserPanel borderLineStylePanel = null;

   /** a panel to set the color of the outline **/
   private ColorValuePanel borderColorPanel = null;

   /** a panel to set the width of the outline **/
   private DoubleValuePanel borderLineWidthPanel = null;

   /** a panel to set the color of the outline **/
   private ColorValuePanel borderBGColorPanel = null;

   /** a panel to set whether the plot should be outline or not **/
   //private JCheckBox drawBorderCheckBox = null;
   private BooleanValuePanel drawBorderBooleanPanel = null;

   /** to set the left padding in percentage */
   private DoubleValuePanel padLeftDoublePanel = null;

   /** to set the right padding in percentage */
   private DoubleValuePanel padRightDoublePanel = null;

   /** to set the top padding in percentage */
   private DoubleValuePanel padTopDoublePanel = null;

   /** to set the bottom padding in percentage */
   private DoubleValuePanel padBottomDoublePanel = null;

   /** to convert the system strings to pretty image icons */
   private PrettyOptionImageIconConverter lineStyleConverter;


   /** To check whether the editor is used for Reference Line **/
   /**NOTE: This variable now set to true since this editor is used only in the
    * ReferenceLineEditor. But future this might change
    */
   protected boolean isReferenceLine = true;


   /** final variables for initializing the text variables
    * if TEXTBORDER DOES NOT HAVE A DEFAULT VALUES
    */
   private  final boolean DRAW_BORDER = false;
   private  final String  LINE_STYLE = "SOLID";
   private  final double  LINE_WIDTH = 1.0;
   private  final Color  LINE_COLOR = Color.black;
   private  final Color  BGCOLOR = Color.yellow;

   private  final double PAD_BOTTOM = 0.5;
   private  final double PAD_TOP = 0.5;
   private  final double PAD_LEFT = 0.5;
   private  final double PAD_RIGHT = 0.5;
   private  final double SIZE_EXPANSION = 1.0;

   /**preferred dimension for the main border panel*/
   private Dimension mainBorderPanelSize = new Dimension(325, 150);

   /** Creates a new instance of TextBorderEditor */
   public TextBorderEditor(TextBorder textBorder)
   {
      super(true);
      this.initialize();
      addButtonPanel();
      setDataSource(textBorder, "");

   }
   
   /** Creates a new instance of TextBorderEditor */
   public TextBorderEditor(TextBorder textBorder, String purpose)
   {
      super(true);
      this.purpose = purpose;
      this.initialize();
      addButtonPanel();
      setDataSource(textBorder, "");

   }

   public TextBorderEditor()
   {
      this(null);
   }

  /**
    * A constructor
    * NOTE: This constructor is created for the purpose if the subclass of the
    * TextBorderEditor class want to access the TextBorderEditor parent constructor.
    */
   public TextBorderEditor(boolean parent)
   {
      super(parent);
   }

   public void setDataSource(Object dataSource, String optionName)
   {
      text = (TextBorder)dataSource;

      if (text != null)
      {
         initGUIFromModel();
      }
      pack();
      setLocation(ScreenUtils.getPointToCenter(this));
      this.repaint();
   }

   /**
    * @pre the object i.e. BarType is not null
    */
   protected void initGUIFromModel()
   {
      TextBorder textBorder = (TextBorder)text;
      setInitialDefaults();
      super.initGUIFromModel();

      drawBorderBooleanPanel.setValue(textBorder.getDrawBorder());
      borderLineStylePanel.setValue(lineStyleConverter.getPrettyOption(
         textBorder.getBorderLinestyle()));
      borderLineWidthPanel.setValue(textBorder.getBorderLinewidth());
      borderColorPanel.setValue(textBorder.getBorderColor());
      borderBGColorPanel.setValue(textBorder.getBackgroundColor());
      padBottomDoublePanel.setValue(textBorder.getPadBottom());
      padTopDoublePanel.setValue(textBorder.getPadTop());
      padLeftDoublePanel.setValue(textBorder.getPadLeft());
      padRightDoublePanel.setValue(textBorder.getPadRight());

      if(isReferenceLine)
      {
         xJustPanel.setValue(textBorder.getXJustification());
         yJustPanel.setValue(textBorder.getYJustification());

         //Talk to Dan
         textBorder.setPosition(TextBorder.REFERENCE_LINE,
         sectorPanel.getValue(), xJustPanel.getValue()
         , yJustPanel.getValue());
      }

   }

   protected void saveGUIValuesToModel() throws Exception
   {
      super.saveGUIValuesToModel();
      TextBorder textBorder = (TextBorder)text;
      textBorder.setDrawBorder(drawBorderBooleanPanel.getValue());
      textBorder.setBorderLinestyle(
      lineStyleConverter.getSystemOption(borderLineStylePanel.getValue()));
      textBorder.setBorderLinewidth(borderLineWidthPanel.getValue());
      textBorder.setBorderColor(borderColorPanel.getValue());
      textBorder.setBackgroundColor(borderBGColorPanel.getValue());
      textBorder.setPadBottom(padBottomDoublePanel.getValue());
      textBorder.setPadTop(padTopDoublePanel.getValue());
      textBorder.setPadLeft(padLeftDoublePanel.getValue());
      textBorder.setPadRight(padRightDoublePanel.getValue());

      if(isReferenceLine)
      {
         textBorder.setPosition(TextBorder.REFERENCE_LINE,
         sectorPanel.getValue(), xJustPanel.getValue()
         , yJustPanel.getValue());
      }

   }

   /**
    * set the inital text border properties
    */
   private void setInitialDefaults()
   {
      TextBorder textBorder = (TextBorder)text;
      if(textBorder.getBorderLinestyle()== null)
         textBorder.setBorderLinestyle(LINE_STYLE);

      if(textBorder.getBorderLinewidth() == 0 )
         textBorder.setBorderLinewidth(LINE_WIDTH);

      if(textBorder.getBorderColor() == null )
         textBorder.setBorderColor(LINE_COLOR);

      if(textBorder.getBackgroundColor() == null )
         textBorder.setBackgroundColor(BGCOLOR);

      if( new Double(textBorder.getPadBottom()).isNaN() )
         textBorder.setPadBottom(PAD_BOTTOM);

      if( new Double(textBorder.getPadTop()).isNaN() )
         textBorder.setPadTop(PAD_TOP);

      if(new Double(textBorder.getPadLeft()).isNaN())
         textBorder.setPadLeft(PAD_LEFT);

      if(new Double(textBorder.getPadRight()).isNaN())
         textBorder.setPadRight(PAD_RIGHT);

      if(textBorder.getTextExpansion() == 0)
         textBorder.setTextExpansion(SIZE_EXPANSION);
   }


   protected void initialize()
   {
      super.initialize();

      if(isReferenceLine)
      {
         this.absolute.setEnabled(false);
      }
      JPanel mainBorderPanel = createBorderEditorPanel();
      //this.getContentPane().add(borderPanel);
      //this.getContentPane().add(paddingPanel);
      this.getContentPane().add(mainBorderPanel);
   }

   protected JPanel createBorderEditorPanel()
   {
    JPanel mainBorderPanel = new JPanel();
      Border outerBorder = BorderFactory.createEmptyBorder(2,2,2,2);
      Border innerBorder = BorderFactory.createBevelBorder(BevelBorder.LOWERED);
      mainBorderPanel.setBorder(BorderFactory.createCompoundBorder(
                                                    outerBorder, innerBorder));
      mainBorderPanel.setLayout(new BoxLayout(mainBorderPanel,BoxLayout.Y_AXIS));
      //mainBorderPanel.setPreferredSize(mainBorderPanelSize);

      JPanel borderPanel  = new JPanel();
      mainBorderPanel.add(borderPanel);
      borderPanel.setLayout(new BoxLayout(borderPanel,BoxLayout.X_AXIS));

      borderPanel.setBorder(BorderFactory.createTitledBorder("Border Style"));
      drawBorderBooleanPanel = new BooleanValuePanel("Draw?",true);
      drawBorderBooleanPanel.setToolTipText("Enable the border for the text box");
      borderPanel.add(drawBorderBooleanPanel);


      //instantiate converter to load the pretty image icons into image chooser panel
      lineStyleConverter = PrettyOptionImageIconConverter.getLineStyleConverter();
      ImageIcon [] imageIcons = lineStyleConverter.getAllPrettyOptions();
      borderLineStylePanel = new ImageChooserPanel("Line Style", true, imageIcons);
      borderLineStylePanel.setToolTipText("Text box borderline style");
      borderPanel.add(borderLineStylePanel);

      borderLineWidthPanel = new DoubleValuePanel("Line Width", true, 0, Double.POSITIVE_INFINITY);
      borderLineWidthPanel.setToolTipText("Text box borderline width >0");
      borderPanel.add(borderLineWidthPanel);

      borderColorPanel = new ColorValuePanel("Line Color", true);
      borderColorPanel.setToolTipText("Text box borderline color");
      borderPanel.add(borderColorPanel);

      borderBGColorPanel = new ColorValuePanel("Background", true);
      borderBGColorPanel.setToolTipText("The background color for the text box");
      borderPanel.add(borderBGColorPanel);

         padLeftDoublePanel = new DoubleValuePanel("Left:",false,0, 10);
      padLeftDoublePanel.setToolTipText("Left padding: values between 0 and 10 in number of characters.");

      padRightDoublePanel = new DoubleValuePanel("Right:",false,0,10);
      padRightDoublePanel.setToolTipText("Right padding: values between 0 and 10 in number of characters.");

      padTopDoublePanel = new DoubleValuePanel("Top:",false,0,10);
      padTopDoublePanel.setToolTipText("Top padding: values between 0 and 10 in number of characters.");

      padBottomDoublePanel = new DoubleValuePanel("Bottom:", false,0,10);
      padBottomDoublePanel.setToolTipText("Bottom padding: values between 0 and 10 in number of characters.");

      JPanel paddingPanel = new JPanel();
      paddingPanel.setLayout(new BoxLayout(paddingPanel,  BoxLayout.X_AXIS));
      mainBorderPanel.add(paddingPanel);
      Border border = BorderFactory.createTitledBorder("Border Padding");
      paddingPanel.setBorder(border);

      paddingPanel.add(padLeftDoublePanel);
      paddingPanel.add(padRightDoublePanel);
      paddingPanel.add(padTopDoublePanel);
      paddingPanel.add(padBottomDoublePanel);

      return mainBorderPanel;
   }//createBorderEditorPanel()

   /**
    * getter for the date source
    * @return Object actual type type TextBorder
    */
   public Object getDataSource()
   {
      return (TextBorder)text;
   }

   public static void main(String arg[])
   {
      TextBorder titleText1 = new TextBorder();
      //      titleText1.setTextString("Scatter Plot 1");
      //      titleText1.setBorderColor(java.awt.Color.blue);
      //      titleText1.setPosition("N",
      //      0.5,
      //      0.5);
      //
      //      titleText1.setTextExpansion(1.2);
      //      titleText1.setTypeface("sans serif");
      //      titleText1.setStyle("italic");
      //      titleText1.setBorderLinestyle(TextBorder.SOLID);
      //
      //      titleText1.setBorderLinewidth(1.0);
      //      titleText1.setDrawBorder(true);

      try
      {
         TextBorderEditor textEditor = new TextBorderEditor(titleText1);
         textEditor.setVisible(true);
      }
      catch(Exception e)
      {
         e.printStackTrace();
      }


   }//main()
}

