package gov.epa.mims.analysisengine.gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.*;
import gov.epa.mims.analysisengine.AnalysisEngineConstants;
import gov.epa.mims.analysisengine.tree.Axis;
import gov.epa.mims.analysisengine.tree.AxisNumeric;
import gov.epa.mims.analysisengine.tree.ReferenceLine;
import gov.epa.mims.analysisengine.tree.Text;

/**
 * An editor to display and edit the AxisNumeric object.
 *
 * @author Daniel Gatti
 * @see gov.epa.mims.analysisengine.tree.axis.java
 * @see gov.epa.mims.analysisengine.tree.Axis.java
 * @version $Id: AxisNumericEditor.java,v 1.3 2005/09/21 14:22:48 parthee Exp $
 */
public class AxisNumericEditor extends AxisContinuousEditor
{
   /** A button to bring up an editor for the referenceLines in AxisNumeric. */
   protected JButton editRefLinesBtn = null;

   /** The GUI for editing RefrenceLines. */
   protected ReferenceLineEditor refLineEditor = null;

   /** A list of ReferenceLines for the plot. */
   protected ArrayList referenceLines = null;

   /** A test field to edit the minimum axis range in AxisNumeric. */
   protected DoubleValuePanel minRangePnl = null;

   /** A test field to edit the maximum axis range in AxisNumeric. */
   protected DoubleValuePanel maxRangePnl = null;

   /** A check box for logarithmic axes  in Axis. */
   protected BooleanValuePanel logPnl = null;

   /** A double panel for the initialPoint in axis. */
   protected DoubleValuePanel initialPointPnl = null;

   /** A double panel for the finalPoint in axis. */
   protected DoubleValuePanel finalPointPnl = null;

   /** A double value for the grid increment in axis. */
   protected DoubleValuePanel incrementPnl = null;

   /** An integer for the intervalCount in axis. */
   protected IntegerValuePanel intervalPnl = null;

   /** A set of Radio buttons to choose between interval count and increment. */
   protected JRadioButton rdoIncrement = null;
   protected JRadioButton rdoInterval = null;
   protected JRadioButton rdoDefault = null;

   /** The text for the Initial Point text field. We need this to add
    * (required) or (optional) to the labels when the radio buttons are changed. */
   protected String initialPointStr = "Initial Point";

   /** The text for the Final Point text field. We need this to add
    * (required) or (optional) to the labels when the radio buttons are changed. */
   protected String finalPointStr = "Final Point";

   /** The text for the Interval text field. We need this to add
    * (required) or (optional) to the labels when the radio buttons are changed. */
   protected String intervalStr = "Divisions";

   /** The text for the Increment Point text field. We need this to add
    * (required) or (optional) to the labels when the radio buttons are changed. */
   protected String incrementStr = "Increment";

   /**
    * Constructor.
    *
    * @author Daniel Gatti
    */
   public AxisNumericEditor(AxisNumeric axis)
   {
      super(axis);
      initialize();
      setDataSource(axis, "");
      setLocation(ScreenUtils.getPointToCenter(this));
   }

   /**
    * Constructor need for class.newInstance
    */
   public AxisNumericEditor()
   {
      this(null);
   }


   /**
    * Present a GUI to the user to edit the reference lines.
    *
    * @author Daniel Gatti
    */
   protected void editReferenceLines()
   {
      refLineEditor = new ReferenceLineEditor(referenceLines);
      refLineEditor.setVisible(true);
      if (refLineEditor.getResult() == OK_RESULT)
      {
         referenceLines = refLineEditor.getReferenceLines();
         if (!(axis instanceof AxisNumeric))
            DefaultUserInteractor.get().notify(this,"Incorrect Object Type",
            "Expected an axis of type AxisNumeric but found one of type " +
            axis.getClass().toString() + ".", UserInteractor.ERROR);

         AxisNumeric axisNumeric = (AxisNumeric)axis;
         axisNumeric.setReferenceLines(referenceLines);
      }

   } //editReferenceLines()



   /**
    * Build the GUI.
    *
    * @author Daniel Gatti
    */
   protected void initialize()
   {
      super.initialize();

      // Axis Panel
      minRangePnl = new DoubleValuePanel("Minimum", false);
      minRangePnl.setToolTipText("The lowest value (in data coordinates) to plot on the axis.");
      maxRangePnl = new DoubleValuePanel("Maximum", false);
      maxRangePnl.setToolTipText("The highest value (in data coordinates) to plot on the axis.");
      logPnl = new BooleanValuePanel("Log Scale?");
      logPnl.setToolTipText("Should this axis use a log scale?");
      editRefLinesBtn = new JButton("Reference Lines");
      editRefLinesBtn.setToolTipText("Use this to place additional lines on the plot.");

      JPanel axisMinMaxPanel = new JPanel();
      axisMinMaxPanel.setLayout(new BoxLayout(axisMinMaxPanel, BoxLayout.X_AXIS));
      axisMinMaxPanel.add(minRangePnl);
      axisMinMaxPanel.add(maxRangePnl);

      JPanel axisLogColorRefPanel = new JPanel();
      axisLogColorRefPanel.setLayout(new BoxLayout(axisLogColorRefPanel, BoxLayout.X_AXIS));
      axisLogColorRefPanel.add(logPnl);
      axisLogColorRefPanel.add(axisColorPnl);
      // Strut needed to make the layout look good.
      axisLogColorRefPanel.add(Box.createHorizontalStrut(50));
      axisLogColorRefPanel.add(editRefLinesBtn);

      JPanel axisTextPanel = new JPanel();
      axisTextPanel.setLayout(new BoxLayout(axisTextPanel, BoxLayout.X_AXIS));
      axisTextPanel.add(axisTextPnl);

      JPanel axisPosPnl = new JPanel();
      axisPosPnl.setLayout(new BoxLayout(axisPosPnl, BoxLayout.X_AXIS));
      axisPosPnl.add(algorithmPnl);
      axisPosPnl.add(positionPnl);

      JPanel axisSubPanel = new JPanel();
      axisSubPanel.setLayout(new BoxLayout(axisSubPanel, BoxLayout.Y_AXIS));
      axisSubPanel.setBorder(BorderFactory.createEtchedBorder());

      axisSubPanel.add(axisLogColorRefPanel);
      axisSubPanel.add(axisMinMaxPanel);
      axisSubPanel.add(axisTextPanel);
      axisSubPanel.add(axisPosPnl);

      JPanel axisPanel = new JPanel();
      axisPanel.setLayout(new BoxLayout(axisPanel, BoxLayout.Y_AXIS));
      axisPanel.setBorder(BorderFactory.createTitledBorder(
      BorderFactory.createLoweredBevelBorder(), "Axis",
      TitledBorder.LEFT, TitledBorder.TOP));
      axisPanel.add(enableAxisPnl);
      axisPanel.add(axisSubPanel);

      algorithmPnl.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            String s = posAlignConv.getSystemOption(algorithmPnl.getValue());
            int i = Integer.parseInt(s);
            positionPnl.setEnabled(i != Axis.DEFAULT_POSITIONING);
         }
      }
      );

      enableAxisPnl.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent ae)
         {
            boolean enable = enableAxisPnl.getValue();
            axisColorPnl.setEnabled(enable);
            logPnl.setEnabled(enable);
            axisTextPnl.setEnabled(enable);
            minRangePnl.setEnabled(enable);
            maxRangePnl.setEnabled(enable);
            drawTickMarksPnl.setEnabled(enable);
            drawTickLabelsPnl.setEnabled(enable);
            tickMarkLabelColorPnl.setEnabled(enable);
            tickMarkFontStylePnl.setEnabled(enable);
            tickMarkPerpPnl.setEnabled(enable);
            labelExpPnl.setEnabled(enable);
            positionPnl.setEnabled(enable);
            algorithmPnl.setEnabled(enable);

            gridLineStylePnl.setEnabled(enable);
            gridLineWidthPnl.setEnabled(enable);
            gridColorPnl.setEnabled(enable);
            rdoDefault.setEnabled(enable);
            rdoIncrement.setEnabled(enable);
            rdoInterval.setEnabled(enable);
            if (enable)
               setGridBoundsEnabling();
            else
            {
               initialPointPnl.setEnabled(enable);
               finalPointPnl.setEnabled(enable);
               incrementPnl.setEnabled(enable);
               intervalPnl.setEnabled(enable);
            }
            gridTickmarkEnablePnl.setEnabled(enable);
            gridTickmarkLengthPnl.setEnabled(enable);
            gridEnablePnl.setEnabled(enable);
            customTickMarkBtn.setEnabled(enable);
            editRefLinesBtn.setEnabled(enable);
         }
      }
      );

      editRefLinesBtn.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent ae)
         {
            editReferenceLines();
         }
      }
      );


      // Tick Marks
      JPanel tickPanel = getTickMarkPanel();

      // Grid panel
      initialPointPnl = new DoubleValuePanel(initialPointStr, true);
      initialPointPnl.setToolTipText("The starting point at which to draw the grid in data coordinates.");
      finalPointPnl   = new DoubleValuePanel(finalPointStr, true);
      finalPointPnl.setToolTipText("The final point at which to draw the grid in data coordinates.");
      incrementPnl    = new DoubleValuePanel(incrementStr, true);
      incrementPnl.setToolTipText("The increment between each grid line in data coordinates.");
      intervalPnl      = new IntegerValuePanel(intervalStr, true);
      intervalPnl.setToolTipText("The number of divisions between the initial and final grid points.");
      rdoIncrement    = new JRadioButton("Increment");
      String radioButtonToopTipText = "Set these to determine how the grid lines are drawn.";
      rdoIncrement.setToolTipText(radioButtonToopTipText);
      rdoInterval     = new JRadioButton("Divisions");
      rdoInterval.setToolTipText(radioButtonToopTipText);
      rdoDefault      = new JRadioButton("Default");
      rdoDefault.setToolTipText(radioButtonToopTipText);
      ButtonGroup buttonGroup = new ButtonGroup();
      buttonGroup.add(rdoIncrement);
      buttonGroup.add(rdoInterval);
      buttonGroup.add(rdoDefault);

      JPanel gridColorStyleWidthPnl = new JPanel();
      gridColorStyleWidthPnl.setLayout(new BoxLayout(gridColorStyleWidthPnl, BoxLayout.X_AXIS));
      gridColorStyleWidthPnl.add(gridColorPnl);
      gridColorStyleWidthPnl.add(gridLineStylePnl);
      gridColorStyleWidthPnl.add(gridLineWidthPnl);

      JPanel gridTickMarksPnl = new JPanel();
      gridTickMarksPnl.setLayout(new GridLayout(1,2));
      //gridTickMarksPnl.setLayout(new BoxLayout(gridTickMarksPnl, BoxLayout.X_AXIS));

      gridTickMarksPnl.add(gridTickmarkEnablePnl);
      gridTickMarksPnl.add(gridTickmarkLengthPnl);

      JPanel gridSpacingChoicePnl = new JPanel();
      gridSpacingChoicePnl.setLayout(new BoxLayout(gridSpacingChoicePnl, BoxLayout.Y_AXIS));
      gridSpacingChoicePnl.setBorder(BorderFactory.createEtchedBorder());
      gridSpacingChoicePnl.add(rdoDefault);
      gridSpacingChoicePnl.add(rdoIncrement);
      gridSpacingChoicePnl.add(rdoInterval);

      JPanel gridSpacingNumbersPnl = new JPanel(new GridLayout(2, 2));
      gridSpacingNumbersPnl.add(initialPointPnl);
      gridSpacingNumbersPnl.add(finalPointPnl);
      gridSpacingNumbersPnl.add(incrementPnl);
      gridSpacingNumbersPnl.add(intervalPnl);

      JPanel gridSpacingPanel = new JPanel();
      gridSpacingPanel.setLayout(new BoxLayout(gridSpacingPanel, BoxLayout.X_AXIS));
      gridSpacingPanel.setBorder(BorderFactory.createTitledBorder(
      BorderFactory.createEtchedBorder(), "Grid Bounds",
      TitledBorder.LEFT, TitledBorder.TOP));
      gridSpacingPanel.add(gridSpacingChoicePnl);
      gridSpacingPanel.add(gridSpacingNumbersPnl);

      JPanel gridSubPanel = new JPanel();
      gridSubPanel.setLayout(new BoxLayout(gridSubPanel, BoxLayout.Y_AXIS));
      gridSubPanel.setBorder(BorderFactory.createEtchedBorder());
      gridSubPanel.add(Box.createVerticalStrut(30));
      gridSubPanel.add(gridColorStyleWidthPnl);
      gridSubPanel.add(Box.createVerticalStrut(30));
      gridSubPanel.add(gridTickMarksPnl);
      gridSubPanel.add(Box.createVerticalStrut(30));
      gridSubPanel.add(gridSpacingPanel);
      gridSubPanel.add(Box.createVerticalStrut(40));
      
      JPanel gridPanel = new JPanel();
      gridPanel.setLayout(new BoxLayout(gridPanel, BoxLayout.Y_AXIS));
      gridPanel.setBorder(BorderFactory.createTitledBorder(
      BorderFactory.createLoweredBevelBorder(), "Grid",
      TitledBorder.LEFT, TitledBorder.TOP));

      gridPanel.add(gridEnablePnl);
      gridPanel.add(gridSubPanel);

      gridEnablePnl.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            boolean enabled = gridEnablePnl.getValue();
            gridLineStylePnl.setEnabled(enabled);
            gridLineWidthPnl.setEnabled(enabled);
            gridColorPnl.setEnabled(enabled);
            rdoDefault.setEnabled(enabled);
            rdoIncrement.setEnabled(enabled);
            rdoInterval.setEnabled(enabled);
            if (enabled)
               setGridBoundsEnabling();
            else
            {
               initialPointPnl.setEnabled(enabled);
               finalPointPnl.setEnabled(enabled);
               incrementPnl.setEnabled(enabled);
               intervalPnl.setEnabled(enabled);
            }
            gridTickmarkEnablePnl.setEnabled(enabled);
            gridTickmarkLengthPnl.setEnabled(gridTickmarkEnablePnl.getValue() && enabled);
         }
      }
      );

      gridTickmarkEnablePnl.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            boolean enabled = gridTickmarkEnablePnl.getValue();
            gridTickmarkLengthPnl.setEnabled(enabled);
         }
      }
      );
      // When the radio buttons for increment, interval or default grid
      // spacing change, enable and disable the appropriate text fields and
      // change the labels to show which fields are required.
      rdoIncrement.addActionListener(
      new ActionListener()
      {
         public void actionPerformed(ActionEvent ae)
         {
            setGridBoundsEnabling();
         }
      }
      );

      rdoInterval.addActionListener(
      new ActionListener()
      {
         public void actionPerformed(ActionEvent ae)
         {
            setGridBoundsEnabling();
         }
      }
      );

      rdoDefault.addActionListener(
         new ActionListener()
         {
            public void actionPerformed(ActionEvent ae)
            {
               setGridBoundsEnabling();
            }
         }
      );

      
//      contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
//
//      contentPane.add(axisPanel);
//      contentPane.add(tickPanel);
//      contentPane.add(gridPanel);
//      contentPane.add(getButtonPanel());
      JPanel basicsPanel = new JPanel();
      basicsPanel.setLayout(new BoxLayout(basicsPanel,BoxLayout.Y_AXIS));
      basicsPanel.add(axisPanel);
      basicsPanel.add(tickPanel);
      JTabbedPane tabbedPane = new JTabbedPane();
      tabbedPane.add("Basic Settings", basicsPanel);
      tabbedPane.add("Grid Setttings", gridPanel);
      
      Container contentPane = getContentPane();
      contentPane.setLayout(new BorderLayout());
      contentPane.add(tabbedPane);
      contentPane.add(getButtonPanel(),BorderLayout.SOUTH);
      setModal(true);
   } // initialize()


   /**
    * Populate the GUI to reflect the data that is in the underlying axis that this
    *  class represents.
    *
    * @author Daniel Gatti
    * @see gov.epa.mims.analysisengine.gui.OptionDialog#initGUIFromModel()
    */
   protected void initGUIFromModel()
   {
      super.initGUIFromModel();
      // Convert the Axis to an AxisNumeric
      if (!(axis instanceof AxisNumeric))
         DefaultUserInteractor.get().notify(this,"Incorrect Object Type",
         "Expected an axis of type AxisNumeric but found one of type " +
         axis.getClass().toString() + ".", UserInteractor.ERROR);

      AxisNumeric axisNumeric = (AxisNumeric)axis;
      if(axisNumeric.getAxisLabelText() != null)
      {
         axisTextPnl.setValue(axisNumeric.getAxisLabelText());
      }
      else
      {
         axisTextPnl.setValue(new Text());
      }
      Object[] obj = axisNumeric.getAxisRange();
      if (obj != null && obj instanceof Double[])
      {
         Double[] minMax = (Double[])axisNumeric.getAxisRange();
         if (minMax.length == 2)
         {
            // If the log scale is being used, then these values are logs
            // and must be converted.
            if (axisNumeric.getLogScale())
            {
               minRangePnl.setValue(Math.pow(10, minMax[0].doubleValue()));
               maxRangePnl.setValue(Math.pow(10, minMax[1].doubleValue()));
            }
            else
            {
               minRangePnl.setValue(minMax[0].doubleValue());
               maxRangePnl.setValue(minMax[1].doubleValue());
            }
         }
         else
            DefaultUserInteractor.get().notify(this, "Unexpected array length",
            "Expected an Double[] of length 2 from AxisNumeric.getAxisRange() " +
            "but found one of length " + minMax.length + " instead.",
            UserInteractor.ERROR);
      }
      else
      {
         minRangePnl.clearValue();
         maxRangePnl.clearValue();
      }

      logPnl.setValue(axisNumeric.getLogScale());

      // Initialize the Reference Lines.
      int numRefs = axisNumeric.getNumReferenceLines();
      if (numRefs == 0)
         referenceLines = null;
      else
      {
         referenceLines = new ArrayList();
         for (int i = 0; i < numRefs; i++)
            referenceLines.add(axisNumeric.getReferenceLine(i));
      }

      // Enable the axis position box only if the default algorithm is NOT selected.
      String s = posAlignConv.getSystemOption(algorithmPnl.getValue());
      int i = Integer.parseInt(s);
      positionPnl.setEnabled(i != Axis.DEFAULT_POSITIONING);

      // Grid
      boolean enabled = gridEnablePnl.getValue();
      gridLineStylePnl.setEnabled(enabled);
      gridLineWidthPnl.setEnabled(enabled);
      gridColorPnl.setEnabled(enabled);
      initialPointPnl.setEnabled(enabled);
      finalPointPnl.setEnabled(enabled);
      incrementPnl.setEnabled(enabled);
      intervalPnl.setEnabled(enabled);
      rdoDefault.setEnabled(enabled);
      rdoIncrement.setEnabled(enabled);
      rdoInterval.setEnabled(enabled);
      gridTickmarkEnablePnl.setEnabled(enabled);

      gridTickmarkEnablePnl.setValue(axisNumeric.getGridTickmarkEnable());
      if (gridTickmarkEnablePnl.getValue())
         gridTickmarkLengthPnl.setValue(axisNumeric.getGridTickmarkLength());
      gridTickmarkLengthPnl.setEnabled(gridTickmarkEnablePnl.getValue() && enabled);

      // Grid
      double initialPoint = axisNumeric.getInitialPoint();
      double finalPoint = axisNumeric.getFinalPoint();
      double increment = axisNumeric.getIncrement();
      int interval = axisNumeric.getIntervalCount();

      // If the axis uses a numeric scale, then the values are logs
      if (axisNumeric.getLogScale())
      {
         initialPoint = Math.pow(10, initialPoint);
         finalPoint   = Math.pow(10, finalPoint);
      }

      initialPointPnl.setValue(initialPoint);
      finalPointPnl.setValue(finalPoint);
      if(!Double.isNaN(increment))
      {
      incrementPnl.setValue(increment);
      }
      else
      {
         incrementPnl.clearValue();
      }

      if(interval !=0 )
      {
        intervalPnl.setValue(interval);
      }
      else
      {
         intervalPnl.clearValue();
      }

      // If the initial point is NaN then the grid spacing is default.
      if (Double.isNaN(initialPoint))
      {
         rdoDefault.setSelected(true);
      }
      // Otherwise, if the increment field is NaN then the grid spacing
      // must be interval.
      else
      {
         if (Double.isNaN(increment))
            rdoInterval.setSelected(true);
         else
            rdoIncrement.setSelected(true);
      }

      setGridBoundsEnabling();

      // Axis
      logPnl.setValue(axisNumeric.getLogScale());
      disableWidgetsFromPlotType();
   } // initGUIFromModel()


   /**
    * Populate the model with the values stored in the GUI.
    *
    * @author Daniel Gatti
    * @see gov.epa.mims.analysisengine.gui.OptionDialog#saveGUIValuesToModel()
    */
   protected void saveGUIValuesToModel() throws Exception
   {
      super.saveGUIValuesToModel();

      if (!(axis instanceof AxisNumeric))
      {
         DefaultUserInteractor.get().notify(this, "Incorrect Object Type",
         "Expected an axis of type AxisNumeric but found one of type " +
         axis.getClass().toString() + ".", UserInteractor.ERROR);
         return;
      }

      AxisNumeric axisNumeric = (AxisNumeric)axis;

      // The double fields return NaN if they are empty and the tree code
      // treats NaN as "no value" for the axis range.
      Double minVal = null;
      Double maxVal = null;
      minVal = Double.valueOf(minRangePnl.getValue());
      maxVal = Double.valueOf(maxRangePnl.getValue());
      if(minVal.isNaN() && !maxVal.isNaN())
      {
         DefaultUserInteractor.get().notify(this, "Error","Please enter a minimum " + 
            "value for the axis", UserInteractor.ERROR);
         shouldContinueClosing = false;
         return;
      }
      else if(!minVal.isNaN() && maxVal.isNaN())
      {
         DefaultUserInteractor.get().notify(this, "Error","Please enter a maximum " + 
            "value for the axis", UserInteractor.ERROR);
         shouldContinueClosing = false;
         return;
      }
      axisNumeric.setAxisRange(minVal, maxVal);
      axisNumeric.setAxisLabelText(axisTextPnl.getValue());
      axisNumeric.setLogScale(logPnl.getValue());
      if (referenceLines != null)
      {
         int numRefs = referenceLines.size();
         for (int i = 0; i < numRefs; i++)
            axisNumeric.setReferenceLineAdd((ReferenceLine)referenceLines.get(i));
      }

      double initialPoint = initialPointPnl.getValue();
      double finalPoint   = finalPointPnl.getValue();

      // If the axis uses a log scale, then the grid min and max must be logs.
      if (logPnl.getValue())
      {
         initialPoint = Math.log(initialPoint) / 2.302581;
         finalPoint   = Math.log(finalPoint) / 2.302581;
      }

      // If the increment radio button is selected, then we expect a
      // required initial point and an optional final point.
      if (rdoIncrement.isSelected())
      {
         double increment = incrementPnl.getValue();
         // The initial point is required.
         if (Double.isNaN(initialPoint))
         {
            DefaultUserInteractor.get().notify(this, "Initial Point Required",
            "When using the increment grid option, an initial point is required.",
            UserInteractor.ERROR);
            shouldContinueClosing = false;
            return;
         }
         // The increment is required.
         else if (Double.isNaN(increment))
         {
            DefaultUserInteractor.get().notify(this, "Increment Required",
            "When using the increment grid option, an increment value is required.",
            UserInteractor.ERROR);
            shouldContinueClosing = false;
            return;
         }
         // The final point is optional.
         else if (Double.isNaN(finalPoint))
         {
            axisNumeric.setGrid(initialPoint, increment);
         }
         else
         {
            axisNumeric.setGrid(initialPoint, increment, finalPoint);
         }
      } // if (rdoIncrement.isSelected())
      // Interval count.
      else if (rdoInterval.isSelected())
      {
         int intervalCount = intervalPnl.getValue();
         // The initial point is required.
         if (Double.isNaN(initialPoint))
         {
            DefaultUserInteractor.get().notify(this, "Initial Point Required",
            "When using the interval grid option, an initial point is required.",
            UserInteractor.ERROR);
            shouldContinueClosing = false;
            return;
         }
         // The final point is required.
         else if (Double.isNaN(finalPoint))
         {
            DefaultUserInteractor.get().notify(this, "Final Point Required",
            "When using the interval grid option, a final point is required.",
            UserInteractor.ERROR);
            shouldContinueClosing = false;
            return;
         }
         // The interval count is required.
         else if (intervalCount == 0)
         {
            DefaultUserInteractor.get().notify(this, "Final Point Required",
            "When using the interval grid option, a final point is required.",
            UserInteractor.ERROR);
            shouldContinueClosing = false;
            return;
         }
         else
         {
            axisNumeric.setGrid(initialPoint, intervalCount, finalPoint);
         }
      }// else if (rdoInterval.isSelected())
      // Default - reset everything in the AxisNumeric.
      else
      {
         axisNumeric.setGrid();
      }

      axisNumeric.setGridTickmarkEnable(gridTickmarkEnablePnl.getValue());
      if (gridTickmarkEnablePnl.getValue())
         axisNumeric.setGridTickmarkLength(gridTickmarkLengthPnl.getValue());

   } // saveGUIValuesToModel()


   /**
    * Enable or disable the fields that the user needs for the
    * grid bounds. This affects initial point, final point,
    * increment and interval.
    */
   protected void setGridBoundsEnabling()
   {
      if (rdoDefault.isSelected())
      {
         initialPointPnl.setEnabled(false);
         initialPointPnl.setLabel(initialPointStr);
         finalPointPnl.setEnabled(false);
         finalPointPnl.setLabel(finalPointStr);
         incrementPnl.setEnabled(false);
         incrementPnl.setLabel(incrementStr);
         intervalPnl.setEnabled(false);
         intervalPnl.setLabel(intervalStr);
      }
      else if (rdoIncrement.isSelected())
      {
         initialPointPnl.setEnabled(true);
         initialPointPnl.setLabel(initialPointStr + " (required)");
         finalPointPnl.setEnabled(true);
         finalPointPnl.setLabel(finalPointStr + " (optional)");
         incrementPnl.setEnabled(true);
         incrementPnl.setLabel(incrementStr + " (required)");
         intervalPnl.setEnabled(false);
         intervalPnl.setLabel(intervalStr);
      }
      else if (rdoInterval.isSelected())
      {
         initialPointPnl.setEnabled(true);
         initialPointPnl.setLabel(initialPointStr + " (required)");
         finalPointPnl.setEnabled(true);
         finalPointPnl.setLabel(finalPointStr + " (required)");
         incrementPnl.setEnabled(false);
         incrementPnl.setLabel(incrementStr);
         intervalPnl.setEnabled(true);
         intervalPnl.setLabel(intervalStr + " (required)");
      }
   } // setGridBoundsEnabling()


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

      disableWidgetsFromPlotType();
   }

   private void disableWidgetsFromPlotType()
   {
      if (plotTypeName == null) return;

      // Bar Plot
      if (plotTypeName.equals(AnalysisEngineConstants.BAR_PLOT))
      {
         gridTickmarkEnablePnl.setVisible(false);
         gridTickmarkLengthPnl.setVisible(false);
      }
      else if( plotTypeName.equals(AnalysisEngineConstants.BOX_PLOT))
      {
//         minRangePnl.setVisible(false);
//         maxRangePnl.setVisible(false);
         logPnl.setVisible(false);
         gridTickmarkEnablePnl.setVisible(false);
         gridTickmarkLengthPnl.setVisible(false);
      }
      // Histogram
      else if (plotTypeName.equals(AnalysisEngineConstants.HISTOGRAM_PLOT))
      {
//         minRangePnl.setVisible(false);
//         maxRangePnl.setVisible(false);
         logPnl.setVisible(false);
         gridTickmarkEnablePnl.setVisible(false);
         gridTickmarkLengthPnl.setVisible(false);
      }
   } // setPlotTypeName()


   /**
    * create dainty panel
    */
   public static Border getCustomBorder(String title)
   {
      if (title != null)
         return BorderFactory.createTitledBorder(
         BorderFactory.createCompoundBorder(
         BorderFactory.createBevelBorder(BevelBorder.LOWERED),
         BorderFactory.createEmptyBorder(1, 1, 1, 1)), title,
         TitledBorder.LEFT, TitledBorder.TOP);
      else
         return BorderFactory.createBevelBorder(BevelBorder.LOWERED);
   } // getCustomBorder()


   /**
    * main() for testing the GUI.
    */
   public static void main(String[] args)
   {
      AxisNumeric axis = new AxisNumeric();
      AxisNumericEditor ane = new AxisNumericEditor(axis);
      ane.setVisible(true);
   }
} // class AxisNumericEditor