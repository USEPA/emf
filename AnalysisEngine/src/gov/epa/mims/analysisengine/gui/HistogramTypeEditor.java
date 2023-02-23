package gov.epa.mims.analysisengine.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import gov.epa.mims.analysisengine.tree.HistogramType;

/**
 * A GUI that allows the user to edit a HistogramType.
 *
 * @author Daniel Gatti
 * @version $Id: HistogramTypeEditor.java,v 1.3 2005/09/21 14:16:49 parthee Exp $
 * @see gov.epa.mims.analysisengine.tree.HistogramType.java
 * @see gov.epa.mims.analysisengine.tree.HistogramPlot.java
 * @see gov.epa.mims.analysisengine.tree.AnalysisOptions.java
 */
public class HistogramTypeEditor
extends OptionDialog
{
   /** The underlying HistogramType that we will be editing. */
   protected HistogramType histogramType = null;

   /** Whether the bars should be solid or shaded. */
   protected BooleanValuePanel solidShadedPanel = null;

   /** The editor for the bar shading angle. */
   protected DoubleValuePanel shadingAnglePanel = null;

   /** The editor for the shading density (number of lines/inch). */
   protected DoubleValuePanel shadingDensityPanel = null;

   /** The color editor for the shading for the bars. */
   protected  ColorValuePanel barColorPanel = null;

   /** The color editor for the border of the bars. */
   protected  ColorValuePanel borderColorPanel = null;

   /** The editor for the type of line with which to outline the bars. */
   protected ImageChooserPanel lineTypePanel = null;

   /** Editor to indicate if the values should be placed above the bars. */
   protected BooleanValuePanel showValuesPanel = null;

   /** Editor for the labels to show Frequency(true) or Probability(false)? */
   protected  BooleanValuePanel frequencyPanel = null;

   /** The editor for the lower x-range. */
   protected DoubleValuePanel lowerXPanel = null;

   /** The editor for the upper x-range. */
   protected DoubleValuePanel upperXPanel = null;

   /** The table for the breaks in the Histogram. */
   protected DoubleEditableTablePanel breaksPanel = null;

   /** The editor for the closure variable.  Left closure means that the left
    * side of a bin is greater than the values in the bin.*/
   protected BooleanValuePanel closurePanel = null;

   /** The editor for the includeLowest variable. This works only when the
    * user has set breakpoints. */
   protected BooleanValuePanel includeLowestPanel = null;

   /**an  interface to convert system strings into line style icons **/
   private PrettyOptionImageIconConverter lineStyleConverter;

   /**
    * Constructor.
    *
    * @author Daniel Gatti
    * @param histogramType HostogramType
    */
   public HistogramTypeEditor(HistogramType histogramType)
   {
      super();
      initialize();
      setDataSource(histogramType, "");
      setLocation(ScreenUtils.getPointToCenter(this));

   } // HistogramTypeEditor()


   /**
    *  Null constructor used with class.newInstance.
    *
    * @author Daniel Gatti
    */
   public HistogramTypeEditor()
   {
      this(null);
   }

   /**
    * Initialize the GUI from the value of the data object.
    *
    * @author Daniel Gatti
    */
   protected void initGUIFromModel()
   {
      if(histogramType.getShadingAngle() != null &&
         histogramType.getShadingDensity() != null)
      {
         solidShadedPanel.setValue(true); // false is solid
      }
      else
      {
         solidShadedPanel.setValue(false);
      }

      boolean enable = solidShadedPanel.getValue();
      shadingAnglePanel.setEnabled(enable);
      shadingDensityPanel.setEnabled(enable);

      Double d = histogramType.getShadingAngle();
      if (d != null)
      {
         shadingAnglePanel.setValue(d.doubleValue());
      }
      else
      {
         shadingAnglePanel.clearValue();
      }

      d = histogramType.getShadingDensity();
      if (d != null)
      {
         shadingDensityPanel.setValue(d.doubleValue());
      }
      else
      {
         shadingDensityPanel.clearValue();
      }

      Color c = histogramType.getColor();
      if (c != null)
         barColorPanel.setValue(c);

      c = histogramType.getBorderColor();
      if (c != null)
         borderColorPanel.setValue(c);

      String s = histogramType.getLinetype();
      if (s != null)
         lineTypePanel.setValue(lineStyleConverter.getPrettyOption(s));

      showValuesPanel.setValue(histogramType.getLabelsOn());
      frequencyPanel.setEnabled(histogramType.getLabelsOn());
      frequencyPanel.setValue(histogramType.getFrequency());

      double[] dblArray = (double[])histogramType.getXRange();
      if (dblArray != null)
      {
         lowerXPanel.setValue(dblArray[0]);
         upperXPanel.setValue(dblArray[1]);
      }
      else
      {
         lowerXPanel.clearValue();
        upperXPanel.clearValue();
      }

      dblArray = (double[])histogramType.getBreaks();
      if (dblArray != null)
      {
         breaksPanel.setValue(dblArray);
         try
         {
            breaksPanel.verifyListSorted(true);
         }
         catch (Exception e)
         {
            DefaultUserInteractor.get().notifyOfException(this,"Breakpoints not sorted",
                  e, UserInteractor.ERROR);
         }
      }
      else
      {
         int count = breaksPanel.getRowCount();
         for(int i = 0; i < count; i ++)
         {
            breaksPanel.removeRow(0);
         }
      }

      closurePanel.setValue(histogramType.getClosure());
      includeLowestPanel.setValue(histogramType.getIncludeLowest());

   } // initGUIFromModel()


   /**
    * Build the GUI.
    *
    * @author Daniel Gatti
    */
   private void initialize()
   {
      // Bar shading, line style and color
      solidShadedPanel       = new BooleanValuePanel("Solid", "Shaded", false);
      solidShadedPanel.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            boolean enable = solidShadedPanel.getValue();
            shadingAnglePanel.setEnabled(enable);
            shadingDensityPanel.setEnabled(enable);
         }
      }
      );
      shadingAnglePanel     = new DoubleValuePanel("Shading Angle", false, 0.0, 360.0);
      shadingAnglePanel.setToolTipText("Shading angle in degrees between 0 and 360");
      shadingDensityPanel  = new DoubleValuePanel("Shading Density", false);
      shadingDensityPanel.setToolTipText("Shading density in lines per inch ");
      barColorPanel   = new ColorValuePanel("Bar Color", false);
      borderColorPanel = new ColorValuePanel("Border Color", false);

      //load the image icons for system option for histogram line styles
      lineStyleConverter = PrettyOptionImageIconConverter.getHistogramLineStyleConverter();
      ImageIcon[] lineStyleIcons  = lineStyleConverter.getAllPrettyOptions();
      lineTypePanel = new ImageChooserPanel(
      "Border Line Style",false, lineStyleIcons);

      JPanel angleDensityPanel = new JPanel();
      angleDensityPanel.setLayout(new BoxLayout(angleDensityPanel, BoxLayout.X_AXIS));
      angleDensityPanel.add(shadingAnglePanel);
      angleDensityPanel.add(shadingDensityPanel);

      JPanel colorPanel = new JPanel();
      colorPanel.setLayout(new BoxLayout(colorPanel, BoxLayout.X_AXIS));
      colorPanel.add(barColorPanel);
      colorPanel.add(borderColorPanel);

      JPanel shadingPanel = new JPanel();
      shadingPanel.setLayout(new BoxLayout(shadingPanel, BoxLayout.Y_AXIS));
      shadingPanel.setBorder(BorderFactory.createTitledBorder(
      BorderFactory.createLoweredBevelBorder(), "Bar Appearance",
      TitledBorder.LEFT, TitledBorder.TOP));
      shadingPanel.add(solidShadedPanel);
      shadingPanel.add(angleDensityPanel);
      shadingPanel.add(colorPanel);
      shadingPanel.add(lineTypePanel);

      // Label attributes
      showValuesPanel = new BooleanValuePanel("Show values above bars?");
      showValuesPanel.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            boolean enable = showValuesPanel.getValue();
            frequencyPanel.setEnabled(enable);
         }
      }
      );
      frequencyPanel     = new BooleanValuePanel("Values indicate frequency", "Values indicate probability", true);
      JPanel labelingPanel = new JPanel();
      labelingPanel.setLayout(new BoxLayout(labelingPanel, BoxLayout.Y_AXIS));
      labelingPanel.setBorder(BorderFactory.createTitledBorder(
      BorderFactory.createLoweredBevelBorder(), "Bar Labeling",
      TitledBorder.LEFT, TitledBorder.TOP));
      labelingPanel.add(showValuesPanel);
      labelingPanel.add(frequencyPanel);

      // X-values
      lowerXPanel = new DoubleValuePanel("lower value", true);
      lowerXPanel.setToolTipText("Enter lower value in data coordinates");
      upperXPanel = new DoubleValuePanel("upper value", true);
      upperXPanel.setToolTipText("Enter upper value in data coordinates");
      JPanel xRangePanel = new JPanel();
      xRangePanel.setLayout(new BoxLayout(xRangePanel, BoxLayout.X_AXIS));
      xRangePanel.setBorder(BorderFactory.createTitledBorder(
      BorderFactory.createLoweredBevelBorder(), "X value ranges",
         TitledBorder.LEFT, TitledBorder.TOP));
      xRangePanel.add(lowerXPanel);
      xRangePanel.add(upperXPanel);

      // Histogram breakpoints
      breaksPanel = new DoubleEditableTablePanel("Break Points Between Bins");

      closurePanel = new BooleanValuePanel("Left", "Right", false);
      closurePanel.setBorder(BorderFactory.createTitledBorder(
         BorderFactory.createEtchedBorder(), "Closure",
         TitledBorder.LEFT, TitledBorder.TOP));
      closurePanel.setToolTipText("Sets whether the breakpoints between bins " +
                                  "include the value on the right or left.");
      includeLowestPanel = new BooleanValuePanel("Include Boundary Values in first/last bin");
      includeLowestPanel.setToolTipText("Handles boundary conditions at the edges ofthe histogram.");

      JPanel closureLowestPanel = new JPanel();
      closureLowestPanel.setLayout(new BoxLayout(closureLowestPanel, BoxLayout.X_AXIS));
      closureLowestPanel.add(closurePanel);
      closureLowestPanel.add(includeLowestPanel);

      Container contentPane = getContentPane();
      contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
      contentPane.add(shadingPanel);
      contentPane.add(labelingPanel);
      contentPane.add(xRangePanel);
      contentPane.add(breaksPanel);
      contentPane.add(closureLowestPanel);
      contentPane.add(getButtonPanel());

      setModal(true);
      pack();
   } // initialize()



   /**
    * Store the info from the GUI in the HistogramType.
    *
    * @author Daniel Gatti
    * @throws Exception
    */
   protected void saveGUIValuesToModel() throws Exception
   {
      double d = 0.0;
      // If the user has selected to shade tha bars, then only get values if they were typed in.
      if (solidShadedPanel.getValue())  // false = solid
      {
         d = shadingAnglePanel.getValue();
         if (Double.isNaN(d))
            histogramType.setShadingAngle(null);
         else
            histogramType.setShadingAngle(Double.valueOf(d));

         d = shadingDensityPanel.getValue();
         if (Double.isNaN(d))
            histogramType.setShadingDensity(null);
         else
            histogramType.setShadingDensity(Double.valueOf(d));
      }
      //   If the user has selected the bars to be solid, them
      else
      {
         histogramType.setShadingAngle(null);
         histogramType.setShadingDensity(null);
      }

      histogramType.setColor(barColorPanel.getValue());
      histogramType.setBorderColor(borderColorPanel.getValue());
      histogramType.setLinetype(
      lineStyleConverter.getSystemOption(lineTypePanel.getValue()));
      histogramType.setLabelsOn(showValuesPanel.getValue());
      histogramType.setFrequency(!frequencyPanel.getValue());

      d = lowerXPanel.getValue();
      double d2 = upperXPanel.getValue();
      if (Double.isNaN(d) || Double.isNaN(d2))
         histogramType.setXRange(null, null);
      else
      {
         if (d == d2)
         {
            DefaultUserInteractor.get().notify(this,"Error verifying X min and max values",
                  "The X axis minimum and maximum are set to the same number. " +
                  "Please set them to two different numbers with the minimum smaller than the maximum.",
                  UserInteractor.ERROR);
            shouldContinueClosing = false;
         }
         histogramType.setXRange(Double.valueOf(d), Double.valueOf(d2));
      }

      if (breaksPanel.isEmpty())
      {
         histogramType.setBreaks(null);
      }
      else
      {
         breaksPanel.verifyListSorted(true);
         histogramType.setBreaks(breaksPanel.getValueAsPrimitive());
      }

      histogramType.setClosure(closurePanel.getValue());
      histogramType.setIncludeLowest(includeLowestPanel.getValue());
   }


   /**
    * Set the HistogramType that this GUI will be editing and initialize the GUI to
    * reflect the current state of the HistogramType.
    *
    * @author Daniel Gatti
    * @param dataSource Object that should be a HistogramType.
    * @param optionName String
    */
   public void setDataSource(Object dataSource, String optionName)
   {
      this.histogramType = (HistogramType)dataSource;
      super.setDataSource(dataSource, optionName);
      if (histogramType != null)
         initGUIFromModel();

      pack();
      repaint();
   }

} // class HistogramTypeEditor
