package gov.epa.mims.analysisengine.rcommunicator;

import gov.epa.mims.analysisengine.AnalysisEngineConstants;
import gov.epa.mims.analysisengine.tree.Axis;
import gov.epa.mims.analysisengine.tree.AxisCategory;
import gov.epa.mims.analysisengine.tree.AxisContinuous;
import gov.epa.mims.analysisengine.tree.AxisNumeric;
import gov.epa.mims.analysisengine.tree.DataSetIfc;
import gov.epa.mims.analysisengine.tree.DiscreteCategoryPlot;
import gov.epa.mims.analysisengine.tree.Legend;
import gov.epa.mims.analysisengine.tree.LineType;
import gov.epa.mims.analysisengine.tree.Text;


/**
 * insures that Objects are called to generate all plot commands
 *
 * @version $Revision: 1.2 $
 * @author Tommy E. Cathey
 ********************************************************/
public class DiscreteCategoryPlotDriver extends AnnotationDriver
implements
gov.epa.mims.analysisengine.tree.AnalysisOptionConstantsIfc
{
   /**
    * Creates a new Driver object.
    * @param p DiscreteCategoryPlotDriver
    ********************************************************/
   public DiscreteCategoryPlotDriver(DiscreteCategoryPlot p)
   {
      super(p);
      rCommandsPreAdd(ReadTableFacade.getCommands(p));

      Cmd cmd = new DiscreteCategoryPlotCmd(p);
      rCommandsPreAdd(cmd.getCommands());

      Object axisObj;


      //
      //x axis 
      //
      axisObj = Option.getXAxis(p);

      if (axisObj != null)
      {
         if (!(axisObj instanceof AxisCategory))
         {
            String msg1 = "Only AxisCategory object allowed ";
            String msg2 = "for X axis of DiscreteCategoryPlot";
            throw new IllegalArgumentException(msg1 + msg2);
         }
         else
         {
//            if (p.getKeys(0) == null)
//            {
               cmd = new AxisCmdAnnotated(1, (Axis) axisObj, "tmpfX", "tmpfL");
//            }
//            else
//            {
//               cmd = new AxisCmdAnnotated(1, (Axis) axisObj, "d1", 
//                                          "as.character(d1Label)");
//            }
         }

         rCommandsPreAdd(cmd.getCommands());
      }


      //
      //y axis 
      //
      axisObj = Option.getYAxis(p);

//      if (axisObj != null)
//      {
//         if (axisObj instanceof AxisLog)
//         {
//            cmd = new AxisCmdLog(2, (AxisLog) axisObj);
//         }
//         else if (axisObj instanceof AxisGrid)
//         {
//            cmd = new AxisCmdGrid(2, (AxisGrid) axisObj);
//         }
//         else
//         {
//            cmd = new AxisCmdAnnotated(2, (Axis) axisObj);
//         }
//
//         rCommandsPreAdd(cmd.getCommands());
//      }

      if (axisObj != null)
      {
         if(axisObj instanceof AxisNumeric)
         {
            AxisNumeric a = (AxisNumeric)axisObj;
            if(a.getLogScale())
            {
               cmd = new AxisCmdNumeric(2, a);
            }
            else 
            {
               cmd = new AxisCmdGrid(2, a);
            }

         }
         else
         {
            throw new IllegalArgumentException(getClass().getName()
            + " the Y axis of the Category Plot must be AxisNumeric");
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

         LegendCmdDiscreteCategoryPlot legCmd = new LegendCmdDiscreteCategoryPlot(
                                                      (Legend) p.getOption(
                                                            LEGEND), 
                                                      (LineType) p.getOption(
                                                            LINE_TYPE), 
                                                      p.getKeys(0), //1 corresponds to the y value keys
                                                      ds);
         rCommandsPreAdd(legCmd.getCommands());
      }
   }
}
