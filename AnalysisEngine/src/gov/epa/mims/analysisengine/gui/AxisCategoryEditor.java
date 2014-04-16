package gov.epa.mims.analysisengine.gui;

import gov.epa.mims.analysisengine.AnalysisEngineConstants;
import gov.epa.mims.analysisengine.tree.*;

import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;


/**
 * A GUI to edit an AxisCategory.
 *
 * @author Daniel Gatti
 * @version $Id: AxisCategoryEditor.java,v 1.3 2005/09/21 14:22:48 parthee Exp $
 */
public class AxisCategoryEditor
extends AxisEditor
{
   /** The minimum range for the axis. */
   protected DoubleValuePanel minRangePnl = null;

   /** The maximum range for the axis. */
   protected DoubleValuePanel maxRangePnl = null;

   /**
    * Constructor.
    */
   public AxisCategoryEditor(AxisCategory axis)
   {
      super(axis);
      initialize();
      setDataSource(axis, "");
      setLocation(ScreenUtils.getPointToCenter(this));
   }

   /**
    * Constructor need for class.newInstance
    */
   public AxisCategoryEditor()
   {
      this(null);
   }


   /**
    * Create the GUI elements.
    */
   protected void initialize()
   {
      super.initialize();
      minRangePnl = new DoubleValuePanel("Minimum", false);
      minRangePnl.setToolTipText("The lowest value to plot on the axis.");
      maxRangePnl = new DoubleValuePanel("Maximum", false);
      maxRangePnl.setToolTipText("The highest value to plot on the axis.");

      // Axis
      JPanel axisColorTextPanel = new JPanel();
      axisColorTextPanel.setLayout(new BoxLayout(axisColorTextPanel, BoxLayout.X_AXIS));
      axisColorTextPanel.add(axisColorPnl);
      axisColorTextPanel.add(axisTextPnl);

      JPanel axisMinMaxPanel = new JPanel();
      axisMinMaxPanel.setLayout(new BoxLayout(axisMinMaxPanel, BoxLayout.X_AXIS));
      axisMinMaxPanel.add(minRangePnl);
      axisMinMaxPanel.add(maxRangePnl);

      JPanel axisPositionPnl = new JPanel();
      axisPositionPnl.setLayout(new BoxLayout(axisPositionPnl, BoxLayout.X_AXIS));
      axisPositionPnl.add(algorithmPnl);
      axisPositionPnl.add(positionPnl);

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

      JPanel axisSubPanel = new JPanel();
      axisSubPanel.setLayout(new BoxLayout(axisSubPanel, BoxLayout.Y_AXIS));
      axisSubPanel.setBorder(BorderFactory.createEtchedBorder());
      axisSubPanel.add(axisColorTextPanel);
      axisSubPanel.add(axisMinMaxPanel);
      axisSubPanel.add(axisPositionPnl);

      JPanel axisPanel = new JPanel();
      axisPanel.setLayout(new BoxLayout(axisPanel, BoxLayout.Y_AXIS));
      axisPanel.setBorder(BorderFactory.createTitledBorder(
      BorderFactory.createLoweredBevelBorder(), "Axis",
      TitledBorder.LEFT, TitledBorder.TOP));
      axisPanel.add(enableAxisPnl);
      axisPanel.add(axisSubPanel);

      enableAxisPnl.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent ae)
         {
            boolean enable = enableAxisPnl.getValue();
            axisColorPnl.setEnabled(enable);
            axisTextPnl.setEnabled(enable);
            minRangePnl.setEnabled(enable);
            maxRangePnl.setEnabled(enable);
            drawTickMarksPnl.setEnabled(enable);
            drawTickLabelsPnl.setEnabled(enable);
            tickMarkLabelColorPnl.setEnabled(enable);
            tickMarkFontStylePnl.setEnabled(enable);
            tickMarkPerpPnl.setEnabled(enable);
            customTickMarkBtn.setEnabled(enable);
            labelExpPnl.setEnabled(enable);
            positionPnl.setEnabled(enable);
            algorithmPnl.setEnabled(enable);
         }
      }
      );

      // Tick Marks
      JPanel tickPanel = getTickMarkPanel();

      Container contentPane = getContentPane();
      contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
      contentPane.add(axisPanel);
      contentPane.add(tickPanel);
      contentPane.add(getButtonPanel());

      setModal(true);
   } // initialize()


   /**
    * Set the values in the GUI to match the data model.
    */
   protected void initGUIFromModel()
   {
      super.initGUIFromModel();
      if (!(axis instanceof AxisCategory))
      {
         DefaultUserInteractor.get().notify(this,"Incorrect Object Type",
         "Expected an axis of type AxisCategory but found one of type " +
         axis.getClass().toString() + ".", UserInteractor.ERROR);
         return;
      }

      AxisCategory axisCategory = (AxisCategory)axis;

      Object[] obj = axisCategory.getAxisRange();
      if (obj != null && obj instanceof Double[])
      {
         Double[] minMax = (Double[])axisCategory.getAxisRange();
         if (minMax.length == 2)
         {
            minRangePnl.setValue(minMax[0].doubleValue());
            maxRangePnl.setValue(minMax[1].doubleValue());
         }
         else
            DefaultUserInteractor.get().notify(this,"Unexpected array length",
            "Expected an Double[] of length 2 from AxisNumeric.getAxisRange() " +
            "but found one of length " + minMax.length + " instead.",
            UserInteractor.ERROR);
      } // if (obj != null && obj instanceof Double[])
      else
      {
         minRangePnl.clearValue();
         maxRangePnl.clearValue();
      }

      // Enable the axis position box only if the default algorithm is NOT selected.
      String s = posAlignConv.getSystemOption(algorithmPnl.getValue());
      int i = Integer.parseInt(s);
      positionPnl.setEnabled(i != Axis.DEFAULT_POSITIONING);
      disableWidgetsFromPlotType();
   } // initGUIFromModel()


   /**
    * Get the values from the GUI and save them to the model.
    * @throws java.lang.Exception
    */
   protected void saveGUIValuesToModel() throws java.lang.Exception
   {
      super.saveGUIValuesToModel();

      if (!(axis instanceof AxisCategory))
         DefaultUserInteractor.get().notify(this,"Incorrect Object Type",
         "Expected an axis of type AxisCategory but found one of type " +
         axis.getClass().toString() + ".", UserInteractor.ERROR);

      AxisCategory axisCategory = (AxisCategory)axis;
      // The DouvleValuePanels return NaN for empty values and the tree
      // code ignores these if they are set to NaN.
      Double minVal = new Double(minRangePnl.getValue());
      Double maxVal = new Double(maxRangePnl.getValue());
      axisCategory.setAxisRange(minVal, maxVal);
   }


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
      if (plotTypeName.equals(AnalysisEngineConstants.BAR_PLOT)
         || plotTypeName.equals(AnalysisEngineConstants.BOX_PLOT))
      {
         minRangePnl.setVisible(false);
         maxRangePnl.setVisible(false);
         customTickMarkBtn.setVisible(false);
      }
      // Discrete Category Plot
      else if (plotTypeName.equals(AnalysisEngineConstants.DISCRETE_CATEGORY_PLOT))
      {
         customTickMarkBtn.setVisible(false);
      }
      // Histogram
      else if (plotTypeName.equals(AnalysisEngineConstants.HISTOGRAM_PLOT))
      {
         minRangePnl.setVisible(false);
         maxRangePnl.setVisible(false);
      }

   } // setPlotTypeName()


   /**
    * main() for testing the GUI.
    */
   public static void main(String[] args)
   {
      AxisCategory axis = new AxisCategory();
      AxisCategoryEditor ace = new AxisCategoryEditor(axis);
      ace.setVisible(true);
   }
} // class AxisCategoryEditor