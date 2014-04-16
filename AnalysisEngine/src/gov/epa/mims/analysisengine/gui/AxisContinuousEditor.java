package gov.epa.mims.analysisengine.gui;

import java.awt.Color;
import javax.swing.ImageIcon;
import gov.epa.mims.analysisengine.tree.*;

/**
 * The editor for continuous axes. This contains the GUI elements for the
 * variables in AxisContinuous.
 *
 * @author Daniel Gatti
 * @version $Id: AxisContinuousEditor.java,v 1.2 2005/09/19 14:50:03 rhavaldar Exp $
 */
public class AxisContinuousEditor extends AxisEditor
{
   /** The GUI editor for gridColor in AxisContinuous. */
   protected ColorValuePanel gridColorPnl = null;

   /** The GUI editor for the gridlineStyle in AxisContinuous. */
   protected ImageChooserPanel gridLineStylePnl = null;

   /** The converter for the line style images. */
   protected PrettyOptionImageIconConverter lineStyleConv = null;

   /** The GUI editor for gridEnable in AxisContinuous. */
   protected BooleanValuePanel gridEnablePnl = null;

   /** The GUI editor for gridTickmarkEnable in AxisContinuous. */
   protected BooleanValuePanel gridTickmarkEnablePnl = null;

   /** The GUI editor for gridTickmarkLength in AxisContinuous. */
   protected DoubleValuePanel gridTickmarkLengthPnl = null;

   /** The GUI editor for gridlineWidth in AxisContinuous. */
   protected DoubleValuePanel gridLineWidthPnl = null;

   public AxisContinuousEditor(AxisContinuous axis)
   {
      super();
   }

  /**
   * Contruct the GUI elements.
   *
   * @author Daniel Gatti
   */
   protected void initialize()
   {
      super.initialize();
      gridColorPnl = new ColorValuePanel("Color", false);
      gridColorPnl.setToolTipText("The color of the grid lines.");
      lineStyleConv = PrettyOptionImageIconConverter.getLineStyleConverter();
      gridLineStylePnl = new ImageChooserPanel("Line Style", false,
         lineStyleConv.getAllPrettyOptions());
      gridLineStylePnl.setToolTipText("The style of line to use for the grid.");
      gridEnablePnl = new BooleanValuePanel("Draw Grid?");
      gridEnablePnl.setToolTipText("Should the grid be drawn?");
      gridTickmarkEnablePnl = new BooleanValuePanel("Enable Grid Tick Marks?");
      gridTickmarkEnablePnl.setToolTipText("Should the tick marks be drawn on the axis?");
      gridTickmarkLengthPnl = new DoubleValuePanel("Grid Tick Mark Length", false);
      gridTickmarkLengthPnl.setToolTipText("The relative length of the grid tick marks.");
      gridLineWidthPnl = new DoubleValuePanel("Line Width", false);
      gridLineWidthPnl.setToolTipText("The width of the grid lines in pixels.");
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
      if (!(axis instanceof AxisContinuous))
      {
         DefaultUserInteractor.get().notify(this,"Incorrect Object Type",
               "Expected an axis of type AxisContinuous but found one of type " +
               axis.getClass().toString() + ".", UserInteractor.ERROR);
         return;
      }

      AxisContinuous axisContinuous = (AxisContinuous)axis;
      // DMG - This is a temporary work-around to a bug.
      // If you don't specify a color for Log and Grid, the plot will not draw.
      Color c = axisContinuous.getGridColor();
      if (c == null)
         c = Color.black;
      gridColorPnl.setValue(c);
      String sysStr = axisContinuous.getGridlineStyle();
      ImageIcon img = lineStyleConv.getPrettyOption(sysStr);
      if (img != null)
         gridLineStylePnl.setValue(img);
      gridEnablePnl.setValue(axisContinuous.getGridEnable());
      gridTickmarkEnablePnl.setValue(axisContinuous.getGridTickmarkEnable());
      gridTickmarkLengthPnl.setValue(axisContinuous.getGridTickmarkLength());
      // DMG - This is a temporary work-around to a bug.
      // If you don't specify a width for Log and Grid, the plot will not draw.
      double d = axisContinuous.getGridlineWidth();
      if (Double.isNaN(d))
         d = 1.0;
      gridLineWidthPnl.setValue(d);
   }


   /**
    * Populate the model with the values stored in the GUI.
    *
    * @author Daniel Gatti
    * @see gov.epa.mims.analysisengine.gui.OptionDialog#saveGUIValuesToModel()
    */
   protected void saveGUIValuesToModel() throws Exception
   {
      super.saveGUIValuesToModel();
      if (!(axis instanceof AxisContinuous))
          DefaultUserInteractor.get().notify(this,"Incorrect Object Type",
                "Expected an axis of type AxisContinuous but found one of type " +
                axis.getClass().toString() + ".", UserInteractor.ERROR);

      AxisContinuous axisContinuous = (AxisContinuous)axis;

      axisContinuous.setGridColor(gridColorPnl.getValue());
      ImageIcon img = gridLineStylePnl.getValue();
      String sysStr = lineStyleConv.getSystemOption(img);
      if (sysStr != null)
         axisContinuous.setGridlineStyle(sysStr);
      axisContinuous.setGridEnable(gridEnablePnl.getValue());
      axisContinuous.setGridTickmarkEnable(gridTickmarkEnablePnl.getValue());
      axisContinuous.setGridTickmarkLength(gridTickmarkLengthPnl.getValue());
      axisContinuous.setGridlineWidth(gridLineWidthPnl.getValue());
   }
} // class AxisContinuousEditor

