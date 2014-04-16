package gov.epa.mims.analysisengine.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import gov.epa.mims.analysisengine.tree.Legend;
import gov.epa.mims.analysisengine.AnalysisEngineConstants;

/**
 * Editor for the legend
 *
 * @author Prashant Pai, UNC
 * @version $Id: LegendEditor.java,v 1.3 2005/09/21 14:16:49 parthee Exp $
 *
 **/
public class LegendEditor
extends OptionDialog
{
   /******************************************************
    *
    * fields
    *
    *****************************************************/

   /** True if the legend should be drawn. */
   private BooleanValuePanel enablePanel = null;

   /** a panel to orient the legend either horizontally or vertically **/
   private BooleanValuePanel orientPanel = null;

   /** a panel to edit the absolute x coordinates **/
   private DoubleValuePanel xCoordPanel = null;

   /** a panel to edit the absolute y coordinates **/
   private DoubleValuePanel yCoordPanel = null;

   /** a panel to select the margin to place the legend in **/
   private StringChooserPanel marginSidePanel = null;

   /** a panel to set the size of the legend **/
   //private DoubleValuePanel sizePanel = null;

   /** a panel for setting the x justification **/
   private DoubleValuePanel xJustMarginPanel = null;

   /** a panel for setting the y justification **/
   private DoubleValuePanel yJustMarginPanel = null;

   /** a panel for setting the x justification **/
   private DoubleValuePanel xJustGridPanel = null;

   /** a panel for setting the y justification **/
   private DoubleValuePanel yJustGridPanel = null;

   /** a panel for setting the x interspacing among characters **/
   private DoubleValuePanel xSpacingPanel = null;

   /** a panel for setting the y interspacing among characters **/
   private DoubleValuePanel ySpacingPanel = null;

   /** a panel for setting the expansion coefficient (size) of the characters **/
   private DoubleValuePanel expFactorPanel = null;

   /** a panel for setting the background color **/
   private ColorValuePanel colorPanel = null;

   /** a panel to set the number of columns to be used **/
   private IntegerValuePanel noColsPanel = null;

   /** the legend to be edited **/
   private Legend legend = null;

   /** A interface to convert a system options for margins into a more descriptive
    * Strings */
   private PrettyOptionStringConverter marginConverter;


   /******************************************************
    *
    * methods
    *
    *****************************************************/

   /**
    * constructor that edits a legend
    * @param aLegend the legend to be edited
    */
   public LegendEditor(Legend aLegend)
   {
      super();
      initialize();
      setDataSource(aLegend, "");
   }//LegendEditor(Legend)


   /**
    * constructor need for class.newInstance
    */
   public LegendEditor()
   {
      this(null);
   }//LegendEditor()


   public void setDataSource(Object dataSource, String optionName)
   {
      this.legend = (Legend)dataSource;
      super.setDataSource(dataSource, optionName);
      if (legend != null)
      {
         initGUIFromModel();
      }
      pack();
      this.repaint();
   }


   /**
    * Enable or disable the color/columns panel.
    */
   protected void enableCharacterPanel(boolean enable)
    {
       expFactorPanel.setEnabled(enable);
       xSpacingPanel.setEnabled(enable);
       ySpacingPanel.setEnabled(enable);
    }


   /**
    * Enable or disable the color/columns panel.
    */
   protected void enableColorColsPanel(boolean enable)
    {
      colorPanel.setEnabled(enable);
      noColsPanel.setEnabled(enable & !orientPanel.getValue());
    }


    /**
     * Enable or disable the entire GUI (except the enable/disable panel).
     * @param boolean enable that is true if the GUI should be enabled.
     */
   protected void enableEntireGUI(boolean enable)
   {
      enableCharacterPanel(enable);
      enableColorColsPanel(enable);
      enableOrientationPanel(enable);
      enablePositionPanel(enable);
   }


   /**
    * Enable or disable the orientation panel.
    */
    protected void enableOrientationPanel(boolean enable)
    {
       orientPanel.setEnabled(enable);
    }


    /**
     * Enable or disable the position panel.
     */
    protected void enablePositionPanel(boolean enable)
     {
       xJustMarginPanel.setEnabled(enable);
       yJustMarginPanel.setEnabled(enable);
       marginSidePanel.setEnabled(enable);
//       sizePanel.setEnabled(enable);
    }


   /**
    * a private method to initialize the GUI
    */
   private void initialize()
   {
      this.setModal(true);

      enablePanel = new BooleanValuePanel("Enable Legend?");
      enablePanel.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            enableEntireGUI(enablePanel.getValue());
         }
      });

      Container contentPane = getContentPane();
      //setLocationRelativeTo(JOptionPane.getRootFrame());
      setLocation(ScreenUtils.getPointToCenter(this));
      setTitle("Edit Legend Properties");

      contentPane.setLayout(new BorderLayout());
      JPanel mainPanel = new JPanel();
      mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
      contentPane.add(mainPanel);

      mainPanel.add(enablePanel);

      JPanel topPanel = new JPanel();
      topPanel.setLayout(new GridLayout(1,2,3,3));
      mainPanel.add(topPanel);

      orientPanel = new BooleanValuePanel("Vertical", "Horizontal", true);
      orientPanel.setBorder(BorderFactory.createTitledBorder(
      BorderFactory.createBevelBorder(BevelBorder.LOWERED), "Orientation",
      TitledBorder.LEFT, TitledBorder.TOP));
      topPanel.add(orientPanel);

      orientPanel.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            // Disable number of columns for horizontal (horizontal = false)
            boolean enabled = !orientPanel.getValue();
            noColsPanel.setEnabled(enabled);
         }
      }
      );


      JPanel colorColsPanel = new JPanel(new GridLayout(2, 1, 3, 3));
      topPanel.add(colorColsPanel);

      colorPanel = new ColorValuePanel("Background Color:", false);
      colorColsPanel.add(colorPanel);

      Border innerBorder = BorderFactory.createBevelBorder(BevelBorder.LOWERED);
      Border outerBorder = BorderFactory.createEmptyBorder(8,2,2,2);
      colorColsPanel.setBorder(BorderFactory.createCompoundBorder(
                 outerBorder,innerBorder));

      noColsPanel = new IntegerValuePanel("Number of Columns", false, 0, Integer.MAX_VALUE);
      noColsPanel.setToolTipText("No. of columns must be an integer > 0");
      colorColsPanel.add(noColsPanel);

      final JPanel positionPanel = new JPanel(new BorderLayout());
      mainPanel.add(positionPanel);
      positionPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createBevelBorder(BevelBorder.LOWERED), "Position",
            TitledBorder.LEFT, TitledBorder.TOP));

      JPanel justifyMarginPanel = new JPanel(new GridLayout(1, 2, 3, 3));
      justifyMarginPanel.setBorder(BorderFactory.createTitledBorder(
      BorderFactory.createLineBorder(Color.black), "Alignment",
         TitledBorder.LEFT, TitledBorder.TOP));
      xJustMarginPanel = new DoubleValuePanel("Horizontal:", false, 0, 1);
      xJustMarginPanel.setToolTipText(
         "Alignment values between 0 and 1.(0=left end, 0.5=middle and 1=right end)");
      yJustMarginPanel = new DoubleValuePanel("Vertical:", false, 0, 1);
      yJustMarginPanel.setToolTipText(
         "Alignment values between 0 and 1.(0=bottom end, 0.5=middle and 1=top end)");
      justifyMarginPanel.add(xJustMarginPanel);
      justifyMarginPanel.add(yJustMarginPanel);

      final JPanel marginPanel = new JPanel(new BorderLayout());

      //create a pretty string margin converter
      marginConverter = PrettyOptionStringConverter.getMarginConverter();
      String [] prettyMarginStrings = marginConverter.getAllPrettyOptions();

      marginSidePanel = new StringChooserPanel("Placement:", false,prettyMarginStrings);
      JPanel topMargin = new JPanel();
      marginPanel.add(topMargin, BorderLayout.NORTH);
      topMargin.add(marginSidePanel);

      //sizePanel = new DoubleValuePanel("Legend Size:", false, Double.MIN_VALUE, Double.MAX_VALUE);
      //sizePanel.setToolTipText("The relative region size(> 0)");
      //topMargin.add(sizePanel);
      marginPanel.add(justifyMarginPanel, BorderLayout.SOUTH);

      positionPanel.add(marginPanel, BorderLayout.CENTER);

      JPanel lowerPanel = new JPanel(new BorderLayout());
      mainPanel.add(lowerPanel);

      JPanel characPanel = new JPanel();
      characPanel.setLayout(new BoxLayout(characPanel,BoxLayout.X_AXIS));
      lowerPanel.add(characPanel, BorderLayout.CENTER);
      characPanel.setBorder(BorderFactory.createTitledBorder(
      BorderFactory.createBevelBorder(BevelBorder.LOWERED), "Character",
      TitledBorder.LEFT, TitledBorder.TOP));

      expFactorPanel = new DoubleValuePanel("Size:", false, Double.MIN_VALUE,
      Double.MAX_VALUE);

      expFactorPanel.setToolTipText(
      "The relative size of the font compared to the default system font size > 0");
      characPanel.add(expFactorPanel);

      xSpacingPanel = new DoubleValuePanel("Horizontal Spacing:", false, 0, Double.MAX_VALUE);
      xSpacingPanel.setToolTipText(
      "The relative spacing compare to the font size > 0");
      ySpacingPanel = new DoubleValuePanel("Vertical Spacing:", false, 0, Double.MAX_VALUE);
      ySpacingPanel.setToolTipText(
      "The relative spacing compared to the font size > 0");
      characPanel.add(xSpacingPanel);
      characPanel.add(ySpacingPanel);

      contentPane.add(getButtonPanel(), BorderLayout.SOUTH);

   }//initialize()

   /**
    * @pre the object i.e. BarType is not null
    */
   protected void initGUIFromModel()
   {
      enablePanel.setValue(legend.getEnable());

      orientPanel.setValue(legend.getHorizontal());

      marginSidePanel.setValue(marginConverter.getPrettyOption(legend.getPosition()));
//      sizePanel.setValue(legend.getLegendRegionSize());
      xJustMarginPanel.setValue(legend.getXJustification());
      yJustMarginPanel.setValue(legend.getYJustification());

      xSpacingPanel.setValue(legend.getXInterspacing());
      ySpacingPanel.setValue(legend.getYInterspacing());
      expFactorPanel.setValue(legend.getCharacterExpansion());

      colorPanel.setValue(legend.getBackgroundColor());
      noColsPanel.setValue(legend.getNumberColumns());

      // Disable number of columns for horizontal (horizontal = false)
      boolean enabled = !orientPanel.getValue();
      noColsPanel.setEnabled(enabled);

      boolean enable = true;
      enableCharacterPanel(enable);
      enableColorColsPanel(enable);
      enableOrientationPanel(enable);
      enablePositionPanel(enable);

//      disableWidgetsFromPlotType();
   } // initGUIFromModel()


   protected void saveGUIValuesToModel() throws Exception
   {
      legend.setEnable(enablePanel.getValue());
      legend.setHorizontal(orientPanel.getValue());
//      legend.setPosition(marginConverter.getSystemOption(marginSidePanel.getValue()), sizePanel.getValue(),
      String prettyMargin = marginSidePanel.getValue();
      String systemOption = marginConverter.getSystemOption(prettyMargin);
      double xJust = xJustMarginPanel.getValue();
      double yJust = yJustMarginPanel.getValue();
      legend.setPosition(systemOption, xJust, yJust);

      legend.setXInterspacing(xSpacingPanel.getValue());
      legend.setYInterspacing(ySpacingPanel.getValue());
      legend.setCharacterExpansion(expFactorPanel.getValue());

      if (!colorPanel.getBackground().equals(colorPanel.getValue()))
         legend.setBackgroundColor(colorPanel.getValue());
      legend.setNumberColumns(noColsPanel.getValue());

   }//saveGUIValuesToModel()


   /**
    * Set the type of plot that this dialog is reprenting.
    * This should be one of the plot constant from AnalysisEngineConstants.
    * @param plotTypeName String that is the name of the plot being produced.
    */
   public void setPlotTypeName(String newName)
   {
      super.setPlotTypeName(newName);
      // Don't do anything if we don't have a plot type.
      if (plotTypeName == null)
         return;

//      disableWidgetsFromPlotType();
   }

//   private void disableWidgetsFromPlotType()
//   {
//      if (plotTypeName == null) return;

      // DiscreteCategory
//      if (plotTypeName.equals(AnalysisEngineConstants.DISCRETE_CATEGORY_PLOT))
//      {
//         sizePanel.setVisible(false);
//      }
//      else if(plotTypeName.equals(AnalysisEngineConstants.RANK_ORDER_PLOT))
//      {
//         sizePanel.setVisible(false);
//      }
//      else if(plotTypeName.equals(AnalysisEngineConstants.SCATTER_PLOT))
//      {
//         sizePanel.setVisible(false);
//      }
//      else if(plotTypeName.equals(AnalysisEngineConstants.TIME_SERIES_PLOT))
//      {
//         sizePanel.setVisible(false);
//      }

//   } // setPlotTypeName()


   public static void main(String[] args)
   {
      DefaultUserInteractor.set(new GUIUserInteractor());
      LegendEditor legendEditor = new LegendEditor(new Legend());
      legendEditor.setVisible(true);
   }//main()

}
