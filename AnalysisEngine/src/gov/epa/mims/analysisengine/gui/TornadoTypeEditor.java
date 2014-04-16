
package gov.epa.mims.analysisengine.gui;

import gov.epa.mims.analysisengine.tree.TornadoType;
import gov.epa.mims.analysisengine.tree.TextAttribute;

import java.awt.Color;
import java.awt.Container;
import java.awt.BorderLayout;

import javax.swing.*;

/**
 * A Dialog to edit "properties" of a Tornadotype, which is one of the analysis
 * options for a TornadoPlot.
 *
 * @author Parthee R Partheepan UNC-CH, CEP
 * @version $Id:
 * @see TornadoType.java
 * @see TornadoPlot.java
 * @see AnalysisOptions.java
 *
 */
public class TornadoTypeEditor extends OptionDialog
{
   /** data source(model) for the editor */
   private TornadoType tornadoType;

   /** a color panel for setting bar colors
    */
   private ColorValuePanel barColorPanel;

   /** a double to specify the space */
   private DoubleValuePanel barSpacePanel;

   /** a color panel for the border color */
   private ColorValuePanel borderColorPanel;

   /** a double value panel line width of the border */
   private DoubleValuePanel borderWidthPanel;

   /** a panel to specify the color of the text */
   private ColorValuePanel textColorPanel;

   /** a string chooser panel to specify the font style
    */
   private StringChooserPanel textStylePanel;

   /** a converter for converting font styles
    */
   private PrettyOptionStringConverter fontStyleConverter =
      PrettyOptionStringConverter.getFontStyleConverter();

   /** a panel to specify the text size
    */
   private DoubleValuePanel textSizePanel;


   /** Creates a new instance of TornadoTypeEditor */
   public TornadoTypeEditor(TornadoType aTornadoType)
   {
      super();
      initialize();
      setDataSource(aTornadoType, "");
   }//TornadoTypeEditor()


  /**
   * constructor need for class.newInstance
   */
    public TornadoTypeEditor()
    {
       this(null);
    }

    public void setDataSource(Object dataSource, String optionName)
    {
       this.tornadoType = (TornadoType)dataSource;
       super.setDataSource(dataSource, optionName);
       if (tornadoType != null)
       {
          initGUIFromModel();
       }
       pack();
       setLocation(ScreenUtils.getPointToCenter(this));
       this.repaint();
    }

    private void initialize()
    {
       this.setModal(true);
       // set the title
       this.setTitle("Edit Tornado Type Properties");

       barColorPanel = new ColorValuePanel("Color", false);
       barColorPanel.setToolTipText("Bar Color");
       barSpacePanel = new DoubleValuePanel("Space",false,0, 10);
       barSpacePanel.setToolTipText("Space between bars: Enter value between 0 and 10");
       JPanel barPanel = new JPanel();
       barPanel.setBorder(BorderFactory.createTitledBorder(
         BorderFactory.createLineBorder(Color.black),"Bars"));
       barPanel.add(barColorPanel);
       barPanel.add(barSpacePanel);

       borderColorPanel = new ColorValuePanel("Color",false);
       borderColorPanel.setToolTipText("Border Color");
       borderWidthPanel = new DoubleValuePanel("Width",false,0,10);
       borderWidthPanel.setToolTipText("Width of the bar border: Enter value between 0 and 10");
       JPanel borderPanel = new JPanel();
       borderPanel.setBorder(BorderFactory.createTitledBorder(
         BorderFactory.createLineBorder(Color.black),"Borders"));
       borderPanel.add(borderColorPanel);
       borderPanel.add(borderWidthPanel);

       textColorPanel = new ColorValuePanel("Color",false);
       textColorPanel.setToolTipText("Label font color");
       String [] fontStyles = fontStyleConverter.getAllPrettyOptions();
       textStylePanel = new StringChooserPanel("Style",false,fontStyles);
       textStylePanel.setToolTipText("Label font style");
       textSizePanel = new DoubleValuePanel("Size",false,0, 5);
       textSizePanel.setToolTipText("Label font size between 0 and 5");

       JPanel textPanel1 = new JPanel();
       textPanel1.add(textColorPanel);
       textPanel1.add(textSizePanel);
       JPanel textPanel2 = new JPanel();
       textPanel2.add(textStylePanel);

       JPanel textPanel = new JPanel(new BorderLayout());
       textPanel.setBorder(BorderFactory.createTitledBorder(
         BorderFactory.createLineBorder(Color.black),"Labels"));
       textPanel.add(textPanel1, BorderLayout.NORTH);
       textPanel.add(textPanel2, BorderLayout.SOUTH);

       JPanel mainPanel = new JPanel();
       mainPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(4,4,4,4),
            BorderFactory.createCompoundBorder(
               BorderFactory.createLoweredBevelBorder(),
               BorderFactory.createEmptyBorder(4,4,4,4))));
       mainPanel.setLayout(new BorderLayout());
       mainPanel.add(barPanel, BorderLayout.NORTH);
       mainPanel.add(borderPanel);
       mainPanel.add(textPanel, BorderLayout.SOUTH);

       Container container = getContentPane();
       container.setLayout(new BorderLayout());
       container.add(mainPanel,BorderLayout.CENTER);
       container.add(getButtonPanel(),BorderLayout.SOUTH);
    }
    protected void initGUIFromModel()
    {
         barColorPanel.setValue(tornadoType.getColor());
         barSpacePanel.setValue(tornadoType.getSpaceBetweenBars());
         borderColorPanel.setValue(tornadoType.getBorderColor());
         borderWidthPanel.setValue(tornadoType.getBorderLwd());
         TextAttribute txtAttribute = tornadoType.getTextAttribute();
         textColorPanel.setValue(txtAttribute.getColor());
         String prettyOption = fontStyleConverter.getPrettyOption(txtAttribute.getFont());
         textStylePanel.setValue(prettyOption);
         textSizePanel.setValue(txtAttribute.getCex());
    }

    protected void saveGUIValuesToModel() throws Exception
    {
       tornadoType.setColor(barColorPanel.getValue());
       tornadoType.setSpaceBetweenBars(barSpacePanel.getValue());
       tornadoType.setBorderColor(borderColorPanel.getValue());
       tornadoType.setBorderLwd(borderWidthPanel.getValue());

       TextAttribute txtAttribute = tornadoType.getTextAttribute();
       txtAttribute.setColor(textColorPanel.getValue());
       txtAttribute.setCex(textSizePanel.getValue());
       String prettyOption = textStylePanel.getValue();
       String systemOption = fontStyleConverter.getSystemOption(prettyOption);
       txtAttribute.setFont(systemOption);
       tornadoType.setTextAttribute(txtAttribute);
    }

   /**
    * @param args the command line arguments
    */
   public static void main(String[] args)
   {
      DefaultUserInteractor.set(new GUIUserInteractor());
      TornadoTypeEditor tornadoTypeEditor = new TornadoTypeEditor(new TornadoType());
      tornadoTypeEditor.setVisible(true);
   }


}
