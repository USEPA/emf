
package gov.epa.mims.analysisengine.gui;

import gov.epa.mims.analysisengine.tree.TextBoxArrow;

import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import javax.swing.*;



/*
 * TextBoxArrowEditor.java
 * An editor for the TextBoxArrow
 * @see TextBoxArrow.java
 * @version $Id: TextBoxArrowEditor.java,v 1.2 2005/09/19 14:50:03 rhavaldar Exp $
 */
public class TextBoxArrowEditor extends OptionDialog
{
   /** a text box arrow */
   private TextBoxArrow textBoxArrow;

   /** a check box to enable/disable a textbox arrow */
   private JCheckBox enableCheckBox;

 /** a panel to set the style of the line stype **/
   private ImageChooserPanel lineStyleIPanel = null;

   /** a pretty image converter for the line styles
    */
   private PrettyOptionImageIconConverter lineConverter;

   /** a panel to set the color of the arrow **/
   private ColorValuePanel lineColorCPanel = null;

   /** a panel to set the width of the arrow**/
   private DoubleValuePanel lineWidthDPanel = null;

   /** a panel to set the length of the arrow**/
   private DoubleValuePanel lineLengthDPanel = null;

   /** a double value panel to set the x position */
   private DoubleValuePanel xPosDPanel;

   /** a double value panel to set the y position */
   private DoubleValuePanel yPosDPanel;

   /** a string chooser panel to set the units */
   private StringChooserPanel unitPosSPanel;

   /** a string chooser panel to set the type of contact */
   private StringChooserPanel typeContactSPanel;

   /** to specify the pretty strings for the type of contact */
   private PrettyOptionStringConverter positionConverter;

   /** a string chooser panel for arrow type(code) */
   private StringChooserPanel typeSPanel;

   /** an double value panel to specify the angle of the arrow line */
   private DoubleValuePanel angleDPanel;

   /** a double value panel to specify the distance from the targetted position */
   private DoubleValuePanel distanceDPanel;

   /** a string corresponds to the double headed arrow */
   public static final String ARROW_IS_DOUBLE_HEADED = "Double Headed";

   /** a string corresponds to the point away the box arrow */
   public static final String ARROW_POINTS_FROM_BOX = "Point Away From Box";

   /** a string corresponds to the point away the box arrow */
   public static final String ARROW_POINTS_TO_BOX = "Point Towards Box";

   /** array consist of all the pretty code options for the arrow */
   public static final String []ALL_ARROW_CODE_OPTIONS= {ARROW_IS_DOUBLE_HEADED,
      ARROW_POINTS_FROM_BOX, ARROW_POINTS_TO_BOX};

   /* hash map to store different arrow pretty codes key: system options */
   public static HashMap codePrettyHashMap;

   /* hash map to store different arrow system codes key: pretty options */
   public static HashMap codeSystemHashMap;

   static
   {
      codePrettyHashMap = new HashMap();
      codePrettyHashMap.put(Integer.valueOf(TextBoxArrow.ARROW_IS_DOUBLE_HEADED), ARROW_IS_DOUBLE_HEADED);
      codePrettyHashMap.put(Integer.valueOf(TextBoxArrow.ARROW_POINTS_FROM_BOX), ARROW_POINTS_FROM_BOX);
      codePrettyHashMap.put(Integer.valueOf(TextBoxArrow.ARROW_POINTS_TO_BOX), ARROW_POINTS_TO_BOX);
      codeSystemHashMap = new HashMap();
      codeSystemHashMap.put(ARROW_IS_DOUBLE_HEADED,Integer.valueOf(TextBoxArrow.ARROW_IS_DOUBLE_HEADED));
      codeSystemHashMap.put(ARROW_POINTS_FROM_BOX,Integer.valueOf(TextBoxArrow.ARROW_POINTS_FROM_BOX));
      codeSystemHashMap.put(ARROW_POINTS_TO_BOX,Integer.valueOf(TextBoxArrow.ARROW_POINTS_TO_BOX));

   }

   /** Creates a new instance of TextBoxArrowEditor */
   public TextBoxArrowEditor(TextBoxArrow aTextBoxArrow)
   {
      super();
      initialize();
      setDataSource(aTextBoxArrow, "TextBoxArrow Editor");
   }



   /**
   * constructor need for class.newInstance
   */
   public TextBoxArrowEditor()
   {
      this(null);
   }//TextBoxArrowEditor

    /**
    * Set the data source for the editor: tree.TextBox
    * @param dataSource source of the data of type tree.TextBox
    * @param optionName String title for the dialog
    */
   public void setDataSource(Object dataSource, String optionName)
   {
      this.textBoxArrow = (TextBoxArrow)dataSource;
      super.setDataSource(dataSource, optionName);
      if (textBoxArrow != null)
      {
         initGUIFromModel();
      }
      pack();
      setLocation(ScreenUtils.getPointToCenter(this));
      this.repaint();
   }//setDataSource()


   private void initialize()
   {
      setModal(true);
//      enableCheckBox = new JCheckBox("Enable ",true);
//      enableCheckBox.addActionListener(new ActionListener()
//      {
//         public void actionPerformed(ActionEvent e)
//         {
//            boolean enable = enableCheckBox.isSelected();
//            enableAll(enable);
//         }
//      });
//      JPanel enablePanel = new JPanel();
//      enablePanel.add(enableCheckBox);

      lineConverter = PrettyOptionImageIconConverter.getHistogramLineStyleConverter();
      ImageIcon [] lineStyles = lineConverter.getAllPrettyOptions();
      lineStyleIPanel = new ImageChooserPanel("Style ", false, lineStyles);
      lineColorCPanel = new ColorValuePanel("Color ", false);
      lineWidthDPanel = new DoubleValuePanel("Width" ,false,0.0, Double.MAX_VALUE);
      lineWidthDPanel.setToolTipText("The width of the arrow: values > 0");
      JPanel linePanel = new JPanel();
      linePanel.setLayout(new BoxLayout(linePanel,BoxLayout.X_AXIS));
      linePanel.setBorder(BorderFactory.createTitledBorder(
         BorderFactory.createLineBorder(Color.black),"Line "));
      linePanel.add(lineStyleIPanel);
      linePanel.add(Box.createHorizontalGlue());
      linePanel.add(lineColorCPanel);
      linePanel.add(Box.createHorizontalStrut(40));
      linePanel.add(lineWidthDPanel);

      xPosDPanel = new DoubleValuePanel("X : ",false);
      yPosDPanel = new DoubleValuePanel("Y : ",false);
      String [] units = {TextBoxEditor.USER_UNITS,TextBoxEditor.FIGURE_UNITS,
         TextBoxEditor.DEVICE_UNITS};
      unitPosSPanel = new StringChooserPanel("Units", false,units);
      unitPosSPanel.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            setToolTipAndBoundForPosition();
         }
      });
      setToolTipAndBoundForPosition();
      distanceDPanel = new DoubleValuePanel("Backoff " ,false,0.0, Double.MAX_VALUE);
      distanceDPanel.setToolTipText("Distance between starting position specified and "
         + "tip of the starting end of the arrow head ");

      JPanel posPanel1 = new JPanel();
      //posPanel1.setLayout(new BoxLayout(posPanel1, BoxLayout.X_AXIS));
      posPanel1.add(unitPosSPanel);
      posPanel1.add(distanceDPanel);
      //posPanel1.add(Box.createHorizontalGlue());
      JPanel posPanel2 = new JPanel();
      posPanel2.add(xPosDPanel);
      posPanel2.add(yPosDPanel);


      JPanel posPanel = new JPanel();
      posPanel.setLayout(new BoxLayout(posPanel, BoxLayout.Y_AXIS));
      posPanel.add(posPanel1);
      posPanel.add(posPanel2);
      posPanel.setBorder(BorderFactory.createTitledBorder(
         BorderFactory.createLineBorder(Color.black),"Arrow Starting Position "));


      typeSPanel = new StringChooserPanel("Head Type", false, ALL_ARROW_CODE_OPTIONS);
      positionConverter = PrettyOptionStringConverter.getPositionConverter();
      String [] options = positionConverter.getAllPrettyOptions();
      typeContactSPanel = new StringChooserPanel("Attached to Box", false, options);
      lineLengthDPanel = new DoubleValuePanel("Length ",false,0.0, Double.MAX_VALUE);
      lineLengthDPanel.setToolTipText("Length of the arrow head: values > 0");
      angleDPanel = new DoubleValuePanel("Angle",false,0,360);
      angleDPanel.setToolTipText("Angle of the arrow head: values between 0 and 360");

      JPanel arrowPanel1 = new JPanel();
      arrowPanel1.add(typeSPanel);
      arrowPanel1.add(typeContactSPanel);
      JPanel arrowPanel2 = new JPanel();
      arrowPanel2.add(lineLengthDPanel);
      arrowPanel2.add(angleDPanel);

      JPanel arrowPanel = new JPanel();
      arrowPanel.setLayout(new BoxLayout(arrowPanel, BoxLayout.Y_AXIS));
      arrowPanel.setBorder(BorderFactory.createTitledBorder(
         BorderFactory.createLineBorder(Color.black),"Arrow "));
      arrowPanel.add(arrowPanel1);
      arrowPanel.add(arrowPanel2);

      JPanel mainPanel = new JPanel();
      mainPanel.setBorder(BorderFactory.createCompoundBorder(
         BorderFactory.createEmptyBorder(4,4,4,4),
         BorderFactory.createCompoundBorder(
            BorderFactory.createLoweredBevelBorder(),
            BorderFactory.createEmptyBorder(4,4,4,4))));
      mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
//      mainPanel.add(enablePanel);
      mainPanel.add(linePanel);
      mainPanel.add(posPanel);
      mainPanel.add(arrowPanel);

      Container container = getContentPane();
      container.setLayout(new BorderLayout());
      container.add(mainPanel,BorderLayout.CENTER);
      container.add(getButtonPanel(),BorderLayout.SOUTH);
   }//initialize()

       /** a helper method to set the bound and the tool tips */
   private void setToolTipAndBoundForPosition()
   {
      String posUnit = unitPosSPanel.getValue();
      if(posUnit.equals(TextBoxEditor.FIGURE_UNITS))
      {
         xPosDPanel.setBounds(0.0,1.0);
         xPosDPanel.setToolTipText("Starting position of the arrow relative to height of "+
            "the figure: values between 0 and 1. "+
            "(0=left, 0.5=middle and 1=right)");
         yPosDPanel.setBounds(0.0,1.0);
         yPosDPanel.setToolTipText("Starting position of the arrow relative to width of " +
            "the figure: values between 0 and 1." +
            "(0=bottom, 0.5=middle and 1=top)");
      }
      else if(posUnit.equals(TextBoxEditor.DEVICE_UNITS))
      {
         xPosDPanel.setBounds(0.0,1.0);
         xPosDPanel.setToolTipText("Starting position of the arrow relative to height of "+
            "the page: values between 0 and 1." +
            "(0=left, 0.5=middle and 1=right)");
         yPosDPanel.setBounds(0.0,1.0);
         yPosDPanel.setToolTipText("Starting position of the arrow relative to width of " +
         "the page: values between 0 and 1. " +
         "(0=bottom, 0.5=middle and 1=top)");
      }
      else //if(sizeUnit.equals(TextBoxEditor.USER_UNITS))
      {
         xPosDPanel.setBounds(-Double.MAX_VALUE, Double.MAX_VALUE);
         xPosDPanel.setToolTipText("Starting position of the arrow relative to data coordinate values.");
         yPosDPanel.setBounds(-Double.MAX_VALUE, Double.MAX_VALUE);
         yPosDPanel.setToolTipText("Starting position of the arrow relative to data coordinate values.");
      }
   }

   protected void initGUIFromModel()
   {
//      boolean enable = textBoxArrow.getEnable();
//      enableCheckBox.setSelected(enable);
//      enableAll(enable);
      ImageIcon lineStyle = lineConverter.getPrettyOption(textBoxArrow.getLty());
      lineStyleIPanel.setValue(lineStyle);
      lineColorCPanel.setValue(textBoxArrow.getColor());
      lineLengthDPanel.setValue(textBoxArrow.getLength());
      lineWidthDPanel.setValue(textBoxArrow.getWidth());

      xPosDPanel.setValue(textBoxArrow.getX());
      yPosDPanel.setValue(textBoxArrow.getY());
      unitPosSPanel.setValue(TextBoxEditor.getPrettyUnit(this,textBoxArrow.getUnitsXY()));

      typeSPanel.setValue(getPrettyArrowCode(this,textBoxArrow.getCode()));
      typeContactSPanel.setValue(positionConverter.getPrettyOption(
         textBoxArrow.getBoxContactPt()));
      angleDPanel.setValue(textBoxArrow.getAngle());
      distanceDPanel.setValue(textBoxArrow.getBackoff());
   }//initGUIFromModel

   protected void saveGUIValuesToModel() throws Exception
   {
//      textBoxArrow.setEnable(enableCheckBox.isSelected());
      textBoxArrow.setLty(lineConverter.getSystemOption(lineStyleIPanel.getValue()));
      textBoxArrow.setColor(lineColorCPanel.getValue());
      textBoxArrow.setLength(lineLengthDPanel.getValue());
      textBoxArrow.setWidth(lineWidthDPanel.getValue());

      int systemUnit = TextBoxEditor.getSystemUnit(this,unitPosSPanel.getValue());
      textBoxArrow.setPosition(xPosDPanel.getValue(),yPosDPanel.getValue(),
         systemUnit);

      textBoxArrow.setCode(getSystemArrowCode(this,typeSPanel.getValue()));
      textBoxArrow.setBoxContactPt(positionConverter.getSystemOption(
         typeContactSPanel.getValue()));
      textBoxArrow.setAngle(angleDPanel.getValue());
      textBoxArrow.setBackoff(distanceDPanel.getValue());

   }//saveGUIValuesToModel

   /* @param sytemUnit int value
   * @return String pretty String
   */
   public static String getPrettyArrowCode(Component parent,int systemCode)
   {
      Object key = Integer.valueOf(systemCode);
      String stringCode = (String)TextBoxArrowEditor.codePrettyHashMap.get(key);
      if(stringCode == null)
      {
         DefaultUserInteractor.get().notify(parent,"Error", "The " + systemCode
            + " is an unexpected code", UserInteractor.ERROR);
      }//if
      return stringCode;
   }//getPrettyArrowCode()

   /** a helper method to get the system code
    * @param pretty String
    * @return int system value
    */
   public static int getSystemArrowCode(Component parent,String prettyArrowCode)
   {
      Integer value = (Integer)TextBoxArrowEditor.codeSystemHashMap.get(prettyArrowCode);
      if(value == null)
      {
         DefaultUserInteractor.get().notify(parent,"Error", "The " + prettyArrowCode
            + " is an unexpected code", UserInteractor.ERROR);
      }//if

      return value.intValue();
   }//getSystemArrowCode

   /** a helper method to enable all or disable all components in the editor */
   private void enableAll(boolean enable)
   {
      lineStyleIPanel.setEnabled(enable);
      lineColorCPanel.setEnabled(enable);
      lineLengthDPanel.setEnabled(enable);
      lineWidthDPanel.setEnabled(enable);

      xPosDPanel.setEnabled(enable);
      yPosDPanel.setEnabled(enable);
      unitPosSPanel.setEnabled(enable);

      typeSPanel.setEnabled(enable);
      typeContactSPanel.setEnabled(enable);
      angleDPanel.setEnabled(enable);
      distanceDPanel.setEnabled(enable);
   }//

   /** a info string about the arrow to be displayed on the table
    *
    * @return String
    */
   public String getArrowInfoString()
   {
     return "Pos("+ textBoxArrow.getX()+ ", "+ textBoxArrow.getY()+ ")";
   }

   /**
    * getter for the date source
    * @return Object actual type type TextBoxArrow
    */
   public Object getDataSource()
   {
      return textBoxArrow;
   }
   /**
    * @param args the command line arguments
    */
   public static void main(String[] args)
   {
      DefaultUserInteractor.set(new GUIUserInteractor());
      try
      {
         TextBoxArrow aTextBoxArrow = new TextBoxArrow();
         TextBoxArrowEditor editor = new TextBoxArrowEditor(aTextBoxArrow);
         editor.setVisible(true);
      }
      catch(Exception e)
      {
         e.printStackTrace();
      }
   }//main()

}
