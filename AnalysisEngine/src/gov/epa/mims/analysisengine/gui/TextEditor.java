package gov.epa.mims.analysisengine.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.*;

import gov.epa.mims.analysisengine.tree.Text;

/**
 * Editor for text
 *
 * @author Prashant Pai
 * @version $Id: TextEditor.java,v 1.2 2005/09/19 14:50:03 rhavaldar Exp $
 *
 **/
public class TextEditor
extends OptionDialog
{
   /******************************************************
    *
    * fields
    *
    *****************************************************/

   /** a text area to edit the actual string text **/
   protected JTextArea stringField = null;

   /** a panel to edit the font style BOLD, ITALIC etc. **/
   private StringChooserPanel stylePanel = null;

   /** a converter for the font style */

   /** a panel to edit the typeface ARIAL, COURIER etc **/
   private StringChooserPanel typefacePanel = null;

   /** a panel to edit the text color **/
   private ColorValuePanel colorPanel = null;

   /** a panel to edit the size of the text **/
   private DoubleValuePanel sizePanel = null;

   /** a panel to set the angle at which the text is drawn **/
   private DoubleValuePanel rotationPanel = null;

   /** a panel to set the position of the text in user coordinates in X direction **/
   private DoubleValuePanel xCoordPanel = null;

   /** a panel to set the position of the text in user coordinates in Y direction **/
   private DoubleValuePanel yCoordPanel = null;

   /** a panel to select the plot sector in which the text needs to be placed **/
   protected StringChooserPanel sectorPanel = null;

   /** a panel to select the plot sector in which the text needs to be placed **/
   protected StringChooserPanel axisLabelPosPanel = null;

   /** a panel to set the X justification **/
   protected DoubleValuePanel xJustPanel = null;

   /** a panel to set the Y justification **/
   protected DoubleValuePanel yJustPanel = null;


   /** the text to be edited **/
   protected Text text = null;

   /** a string indicate that relative postitioning is not selected */
   public static final String NOTRELATIVE2AXIS = "Not Relative";

   /** radiobuttons to set the position **/
   protected JRadioButton absolute = new JRadioButton("Data");
   private JRadioButton grid = new JRadioButton("Grid");

   private Dimension mainPanelSize = new Dimension(325, 250);

   /** A interface to convert a system options for sector into a more descriptive
    * Strings */
   private PrettyOptionStringConverter sectorConverter;

   /** A interface to convert a system options for axis text position  into a more descriptive
    * Strings */
   private PrettyOptionStringConverter axisTextPosConverter;

   /** A interface to convert a system options for font style into a more descriptive
    * Strings */
   private PrettyOptionStringConverter fontStyleConverter;

   /** A interface to convert a system options for font type face into a more descriptive
    * Strings */
   private PrettyOptionStringConverter fontTypefaceConverter;

   //   /**A boolean to indicate whether this gui is used for setting labels in the Axis 
//    * Editors
//    */
//   private boolean axisLabelEditor = false;
//   
//   /**A boolean to indicate whether this gui is used for setting text for the reference lines 
//    */
//   private boolean referenceLineText = false; 
   
   /** defining type face constants for the text*/
   public static final String DEFAULT = "default";
   public static final String SERIF = "serif";
   public static final String SANS_SERIF = "sans serif";
   public static final String SCRIPT = "script";
   
   /** defining the constants for this editor to be used in contex */
   public static final String EDITOR_DEFAULT = "editorDefault";
   public static final String EDITOR_AXIS = "editorAxis";
   public static final String EDITOR_REFLINE = "editorRefLine";
   
   protected String purpose = EDITOR_DEFAULT;
   
   /******************************************************
    *
    * methods
    *
    *****************************************************/

   /**
    * constructor that edits text
    * @param someText the text to be edited
    */
   public TextEditor(Text aText)
   {
      super();
      initialize();
      addButtonPanel();
      setDataSource(aText, "");
   }//TextEditor(Text)

   /**
    * constructor that edits text
    * @param someText the text to be edited
    * @param isAxis boolean whether this gui is user for setting labels in the Axis
    * @param refLine boolean whehter this gui is used for setting labels for 
    */
   public TextEditor(Text aText, String purpose)
   {
      super();
      if(purpose.equals(EDITOR_DEFAULT)|| purpose.equals(EDITOR_AXIS) 
         ||purpose.equals(EDITOR_REFLINE)){
      }
      else{
         throw new IllegalArgumentException("Please use constants one of the following" + 
         " constants: EDITOR_DEFAULT, EDITOR_AXIS, EDITOR_REFLINE");
      }
      this.purpose = purpose;
      initialize();
      addButtonPanel();
      setDataSource(aText, "");
   }//TextEditor(Text)


   /**
    * a constructor need for class.newInstance
    */
   public TextEditor()
   {
      this(null);

   }//TextEditor()



   /**
    * A constructor
    * NOTE: This constructor is created for the purpose if the subclass of the
    * TextEditor class want to access the TextEditor parent constructor.
    */

   public TextEditor(boolean parent)
   {
      super();
   }


   public void setDataSource(Object dataSource, String optionName)
   {
      this.text = (Text)dataSource;
      super.setDataSource(dataSource, optionName);
      if (text != null)
      {
         initGUIFromModel();
      }
      pack();
      setLocation(ScreenUtils.getPointToCenter(this));
   }

   public Text getCurrentText()
   {
      return text;
   }

   /**
    * Override this to specify the info shown in the table
    * @return String info to show in the options table
    */
   public String getInfoString()
   {
      if(text != null)
         return text.getTextString();
      else
         return null;
   }

   /**
    * a private method to initialize the GUI
    */
   protected void initialize()
   {
      this.setModal(true);
      Container contentPane = getContentPane();
      contentPane.setLayout(new BoxLayout(contentPane,BoxLayout.Y_AXIS));
      setTitle("Edit Text Properties");

      JPanel mainPanel = createTextPanel();
      contentPane.add(mainPanel);

   }//initialize()

   /** a helper method to create a text panel */
   protected JPanel createTextPanel()
   {
      JPanel mainPanel = new JPanel();
      //contentPane.setLayout(new BorderLayout());
      mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
//      mainPanel.setPreferredSize(mainPanelSize);
      Border outerBorder = BorderFactory.createEmptyBorder(2,2,2,2);
      Border innerBorder = BorderFactory.createBevelBorder(BevelBorder.LOWERED);
      mainPanel.setBorder(BorderFactory.createCompoundBorder(
                                                    outerBorder, innerBorder));

      JPanel stringPanel = new JPanel();
      stringPanel.setLayout(new BoxLayout(stringPanel,BoxLayout.X_AXIS));
      stringPanel.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
      stringPanel.setAlignmentX(JPanel.CENTER_ALIGNMENT);
      stringPanel.add(new JLabel("Text:"),BorderLayout.WEST);
      stringPanel.add(Box.createHorizontalStrut(10));
      stringField = new JTextArea(3, 20);
      stringField.setWrapStyleWord(true);
      JScrollPane scrollPane = new JScrollPane(stringField,
         JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
         JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      scrollPane.setPreferredSize(new Dimension(250,70));
      stringPanel.add(scrollPane);
      //      stringPanel.setBorder(AxisEditor.getCustomBorder(null));
      //mainPanel.add(stringPanel, BorderLayout.NORTH);
      mainPanel.add(stringPanel);


      JPanel centerPanel = new JPanel(new BorderLayout());
      //mainPanel.add(centerPanel, BorderLayout.CENTER);

      centerPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
      mainPanel.add(centerPanel);

      JPanel fontPanel = new JPanel(new BorderLayout());
      centerPanel.add(fontPanel, BorderLayout.NORTH);
      //      fontPanel.setBorder(AxisEditor.getCustomBorder("Font"));

      JPanel topPanel = new JPanel();
      topPanel.setLayout(new BoxLayout(topPanel,BoxLayout.X_AXIS));
      JPanel bottomPanel = new JPanel();
      bottomPanel.setLayout(new BoxLayout(bottomPanel,BoxLayout.X_AXIS));
      fontPanel.add(topPanel, BorderLayout.NORTH);
      fontPanel.add(bottomPanel, BorderLayout.SOUTH);

      topPanel.add(Box.createHorizontalGlue());
      fontStyleConverter = PrettyOptionStringConverter.getFontStyleConverter();
      String [] styleOptions = fontStyleConverter.getAllPrettyOptions();
      stylePanel = new StringChooserPanel("Style:", false, styleOptions);
      topPanel.add(stylePanel);

      fontTypefaceConverter = PrettyOptionStringConverter.getFontTypefaceConverter();
      String [] typefaceOptions = fontTypefaceConverter.getAllPrettyOptions();
      typefacePanel = new StringChooserPanel("Typeface:", false, typefaceOptions);
      topPanel.add(typefacePanel);
      topPanel.add(Box.createHorizontalGlue());

      colorPanel = new ColorValuePanel("Color:", false);
      colorPanel.setToolTipText("Color of text");
      bottomPanel.add(Box.createHorizontalGlue());
      bottomPanel.add(colorPanel);
      sizePanel = new DoubleValuePanel("Size:", false, 0, Double.POSITIVE_INFINITY);
      sizePanel.setToolTipText("The relative size of the font to the default system font size > 0");
      bottomPanel.add(sizePanel);
      bottomPanel.add(Box.createHorizontalStrut(10));
      rotationPanel = new DoubleValuePanel("Rotation:",false, -180, 180);
      rotationPanel.setToolTipText("The text can be rotated from -180 to +180 degrees");
      bottomPanel.add(rotationPanel);
      if(purpose.equals(EDITOR_REFLINE)){
         rotationPanel.setEnabled(false);
      }
      bottomPanel.add(Box.createHorizontalGlue());

      final JPanel positionPanel = new JPanel(new BorderLayout());
//      positionPanel.setMinimumSize(new Dimension(200, 50));
      positionPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(),"Position",
            TitledBorder.LEFT,TitledBorder.TOP));
      centerPanel.add(positionPanel, BorderLayout.CENTER);

      JPanel choicePanel = new JPanel(new GridLayout(2, 1, 3, 3));
      choicePanel.setAlignmentX(JPanel.CENTER_ALIGNMENT);
      positionPanel.add(choicePanel, BorderLayout.WEST);

      ButtonGroup choiceGroup = new ButtonGroup();
      choiceGroup.add(absolute);
      choiceGroup.add(grid);

      choicePanel.add(absolute);
      choicePanel.add(grid);

      final JPanel absolutePanel = new JPanel();
      absolutePanel.setToolTipText("The text position set by data coordinates");
      absolutePanel.setBorder(BorderFactory.createEtchedBorder());
      xCoordPanel = new DoubleValuePanel("X data coordinate: ", false);
      yCoordPanel = new DoubleValuePanel("Y data coordinate: ", false);
      absolutePanel.setLayout(new BoxLayout(absolutePanel, BoxLayout.Y_AXIS));
      absolutePanel.add(Box.createVerticalStrut(10));
      absolutePanel.add(xCoordPanel);
      absolutePanel.add(Box.createVerticalStrut(10));
      absolutePanel.add(yCoordPanel);
      absolutePanel.add(Box.createVerticalStrut(10));


      final JPanel gridPanel = new JPanel();
      gridPanel.setLayout(new BoxLayout(gridPanel, BoxLayout.Y_AXIS));
      gridPanel.setToolTipText("The text position set by grid format");
      gridPanel.setBorder(BorderFactory.createEtchedBorder());

      //creating pretty sector String converter
      sectorConverter = PrettyOptionStringConverter.getSectorConverter();
      String [] prettyStrings = sectorConverter.getAllPrettyOptions();
      sectorPanel = new StringChooserPanel("Sector:", false, prettyStrings);
      JPanel sectorAxisLabelPanel = new JPanel();
      sectorAxisLabelPanel.add(sectorPanel);

      //justifyPanel.setBorder(BorderFactory.createTitledBorder(
      //BorderFactory.createLineBorder(Color.black), "Alignment"));
      if(purpose.equals(EDITOR_AXIS))
      {
         axisTextPosConverter =
            PrettyOptionStringConverter.getAxisLabelSectorConverter();
         String[] prettyPosStrings = axisTextPosConverter.getAllPrettyOptions();
         axisLabelPosPanel = new StringChooserPanel("Position:",false,prettyPosStrings);
         sectorAxisLabelPanel.add(axisLabelPosPanel);
      }


      JPanel justifyPanel = new JPanel();
      justifyPanel.setLayout(new BoxLayout(justifyPanel, BoxLayout.X_AXIS));
      JLabel alignLabel = new JLabel(" Alignment: ");
      xJustPanel = new DoubleValuePanel("Horizontal ", false, 0, 1);
      xJustPanel.setToolTipText("Alignment values between 0 and 1 (0=left, 0.5=middle and 1=right)");
      yJustPanel = new DoubleValuePanel("Vertical ", false, 0, 1);
      yJustPanel.setToolTipText("Alignment values between 0 and 1 (0=bottom, 0.5=middle and 1=top)");
      justifyPanel.add(alignLabel);
      justifyPanel.add(xJustPanel);
      justifyPanel.add(yJustPanel);
      gridPanel.add(sectorAxisLabelPanel);
      gridPanel.add(justifyPanel);
      gridPanel.add(Box.createVerticalStrut(10));
//      JPanel contnXJustPanel = new JPanel();
//      contnXJustPanel.setLayout(new BoxLayout(contnXJustPanel,BoxLayout.X_AXIS));
//      contnXJustPanel.add(Box.createHorizontalGlue());
//      contnXJustPanel.add(xJustPanel);
//      contnXJustPanel.add(Box.createHorizontalGlue());
//
//      JPanel contnYJustPanel = new JPanel();
//      contnYJustPanel.setLayout(new BoxLayout(contnYJustPanel,BoxLayout.X_AXIS));
//      contnYJustPanel.add(Box.createHorizontalGlue());
//      contnYJustPanel.add(yJustPanel);
//      contnYJustPanel.add(Box.createHorizontalGlue());
//      justifyPanel.add(contnXJustPanel);
//      justifyPanel.add(contnYJustPanel);


      absolute.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent ae)
         {
            positionPanel.remove(gridPanel);
            positionPanel.add(absolutePanel, BorderLayout.CENTER);
            positionPanel.revalidate();
            positionPanel.repaint();
         }
      });

      grid.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent ae)
         {
            positionPanel.remove(absolutePanel);
            positionPanel.add(gridPanel, BorderLayout.CENTER);
            positionPanel.revalidate();
            positionPanel.repaint();
         }
      });

      positionPanel.add(gridPanel, BorderLayout.CENTER);
      grid.setSelected(true);

      return mainPanel;
   }
   /**
    * @pre the object i.e. BarType is not null
    */
   protected void initGUIFromModel()
   {
      stringField.setText(text.getTextString());

      stylePanel.setValue(fontStyleConverter.getPrettyOption(text.getStyle()));
      String typeface = text.getTypeface();
      if(typeface == null)
      {
         typefacePanel.setValue(PrettyOptionStringConverter.DEFAULT);
      }
      else
      {
         typefacePanel.setValue(fontTypefaceConverter.getPrettyOption(typeface));
      }

      colorPanel.setValue(text.getColor());

      sizePanel.setValue(text.getTextExpansion());
      rotationPanel.setValue(text.getTextDegreesRotation());

      xCoordPanel.setValue(text.getX());
      yCoordPanel.setValue(text.getY());

      sectorPanel.setValue(sectorConverter.getPrettyOption(text.getPosition()));
      if(purpose.equals(EDITOR_AXIS))
      {
         String systemRegion = text.getRegion();
//System.out.println("InitGUI: systemRegion=" + systemsRegion);
         if(systemRegion == null)
         {
            systemRegion = TextEditor.NOTRELATIVE2AXIS;
         }
         String region = axisTextPosConverter.getPrettyOption(systemRegion);
         axisLabelPosPanel.setValue(region);
      }
      xJustPanel.setValue(text.getXJustification());
      yJustPanel.setValue(text.getYJustification());
   }//initGUIFromModel()

   protected void updateModelFromGUI() throws Exception
   {
      saveGUIValuesToModel();
   }

   protected void saveGUIValuesToModel() throws Exception
   {
      text.setTextString(stringField.getText());
//System.out.println("font style = "+fontStyleConverter.getSystemOption(stylePanel.getValue()));
      text.setStyle(fontStyleConverter.getSystemOption(stylePanel.getValue()));
      //text.setStyle("bold italic");
      String typeface = fontTypefaceConverter.getSystemOption(typefacePanel.getValue());
      //text.setTypeface("serif");
//System.out.println("type face ="+typeface);
      if(typeface.equals(DEFAULT))
      {
         text.setTypeface(null);
      }
      else
      {
         text.setTypeface(typeface);
      }
      if (!colorPanel.getBackground().equals(colorPanel.getValue()))
         text.setColor(colorPanel.getValue());

      text.setTextExpansion(sizePanel.getValue());
      text.setTextDegreesRotation(rotationPanel.getValue());

      if (absolute.isSelected())
      {
//System.out.println("absolute: x="+xCoordPanel.getValue()+ " y="+ yCoordPanel.getValue());
         text.setPosition(xCoordPanel.getValue(), yCoordPanel.getValue());
      }
      else if (grid.isSelected())
      {
         String sector = sectorConverter.getSystemOption(sectorPanel.getValue());
         if(!purpose.equals(EDITOR_AXIS))
         {
            text.setPosition(sector, xJustPanel.getValue(),
               yJustPanel.getValue());
//System.out.println("grid: xJust="+xJustPanel.getValue()+ " y="+yJustPanel.getValue());
         }
         else
         {
            String region = axisTextPosConverter.getSystemOption(
               axisLabelPosPanel.getValue());
            if(region.equals(TextEditor.NOTRELATIVE2AXIS))
            {
               text.setPosition(null,sector, xJustPanel.getValue(),
                  yJustPanel.getValue());
            }
            else
            {
               text.setPosition(region, sector, xJustPanel.getValue(),
                  yJustPanel.getValue());
            }
         }
      }
   }//saveGUIValuesToModel()

   /**
    * Adds the Ok/Cancel button panel to the main panel
    */
   protected void addButtonPanel()
   {
      Container contentPane = getContentPane();
      //contentPane.add(getButtonPanel(), BorderLayout.SOUTH);
      contentPane.add(getButtonPanel());

   }


   public static void main(String[] args)
   {
      Text titleText1 = new Text();
      titleText1.setTextString("Scatter Plot 1");
      titleText1.setColor(Color.blue);
      titleText1.setPosition("N",
      0.5,
      0.5);

      titleText1.setTextExpansion(1.2);
      titleText1.setTypeface("sans serif");
      titleText1.setStyle("italic");

      try
      {
         TextEditor textEditor = new TextEditor(titleText1,EDITOR_DEFAULT);
         textEditor.setVisible(true);
      }
      catch(Exception e)
      {
         e.printStackTrace();
      }

      Text text2 = titleText1;
   }//main()
}
