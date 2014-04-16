package gov.epa.mims.analysisengine.rcommunicator;

import gov.epa.mims.analysisengine.AnalysisEngineConstants;
import gov.epa.mims.analysisengine.tree.Axis;
import gov.epa.mims.analysisengine.tree.AxisNumeric;
import gov.epa.mims.analysisengine.tree.CdfPlot;
import gov.epa.mims.analysisengine.tree.Text;
import gov.epa.mims.analysisengine.tree.DataSetIfc;
import gov.epa.mims.analysisengine.tree.Legend;
import gov.epa.mims.analysisengine.tree.LineType;


/**
 * insures that Objects are called to generate all plot commands
 *
 * @version $Revision: 1.2 $
 * @author Tommy E. Cathey
 ********************************************************/
public class CdfPlotDriver extends AnnotationDriver
{
   /**
    * Creates a new Driver object.
    * @param p CdfPlotDriver
    ********************************************************/
   public CdfPlotDriver(CdfPlot p)
   {
      super(p);
      rCommandsPreAdd(ReadTableFacade.getCommands(p));
      
      CdfPlotCmd cdfPlotCmd = new CdfPlotCmd(p);
      rCommandsPreAdd(cdfPlotCmd.getCommands());

      //
      //x axis 
      //
      Axis axis;
      Cmd cmd = null;

      axis = (Axis)Option.getXAxis(p);

      if (axis != null)
      {
         if(axis instanceof AxisNumeric)
         {
            AxisNumeric a = (AxisNumeric)axis;
            if(a.getLogScale())
            {
               cmd = new AxisCmdLog(1, (AxisNumeric) a);
            }
            else
            {
               //cmd = new AxisCmdGrid(1, (AxisNumeric) a);
               cmd = new AxisCmdNumeric(1, (AxisNumeric) a,false);

            }
         }
         else
         {
            throw new IllegalArgumentException(getClass().getName()
            + " Only AxisNumeric can be used as X axis in CdfPlot");
         }

         rCommandsPreAdd(cmd.getCommands());
      }


      //
      //y axis 
      //
      axis = Option.getYAxis(p);

      if (axis != null)
      {
         if(axis instanceof AxisNumeric)
         {
            AxisNumeric a = (AxisNumeric)axis;
            if(a.getLogScale())
            {
               cmd = new AxisCmdLog(2, a);
            }
            else
            {
               cmd = new AxisCmdGrid(2, a);
            }
         }
         else
         {
            throw new IllegalArgumentException(getClass().getName()
            + " Only AxisNumeric can be used as Y axis in CdfPlot");
         }

         rCommandsPreAdd(cmd.getCommands());
      }

      if (p.getOption(LEGEND) != null)
      {
         DataSetIfc[] ds = new DataSetIfc[(p.getKeys(0)).length];

         for (int i = 0; i < ds.length; ++i)
         {
            ds[i] = p.getDataSet((p.getKeys(0))[i]);
         }

         LegendCmdCdfPlot legCmd = new LegendCmdCdfPlot(
                                             (Legend) p.getOption(
                                                   LEGEND),
                                             (LineType) p.getOption(
                                                   LINE_TYPE),
                                             p.getKeys(0), //0 corresponds to the x value keys
                                             ds);
         rCommandsPreAdd(legCmd.getCommands());
      }

   }
}
