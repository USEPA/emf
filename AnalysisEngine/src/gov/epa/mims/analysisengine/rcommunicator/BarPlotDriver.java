package gov.epa.mims.analysisengine.rcommunicator;

import gov.epa.mims.analysisengine.AnalysisEngineConstants;
import gov.epa.mims.analysisengine.tree.Axis;
import gov.epa.mims.analysisengine.tree.AxisNumeric;
import gov.epa.mims.analysisengine.tree.AxisCategory;
import gov.epa.mims.analysisengine.tree.BarPlot;
import gov.epa.mims.analysisengine.tree.BarType;
import gov.epa.mims.analysisengine.tree.Text;


/**
 * insures that Objects are called to generate all plot commands
 *
 * @version $Revision: 1.2 $
 * @author Tommy E. Cathey
 ********************************************************/
public class BarPlotDriver extends AnnotationDriver
{
   /**
    * Creates a new Driver object.
    * @param p BarPlotDriver
    ********************************************************/
   public BarPlotDriver(BarPlot p)
   {
      super(p);
      rCommandsPreAdd(ReadTableFacade.getCommands(p));
      
      BarPlotAxisHelper.init(p);

      Cmd cmd = new BarPlotCmd(p);
      rCommandsPreAdd(cmd.getCommands());

      BarType barType = Option.getBarType(p);
      boolean horiz = barType.getHorizontal();

      AxisCategory axisCategory = (AxisCategory)Option.getXAxis(p);
      AxisNumeric axisNumeric = (AxisNumeric)Option.getYAxis(p);
      if(horiz)
      {
         boolean juxtaposed = true;
         cmd = new AxisCmdBarPlot(2, axisCategory,juxtaposed);
         rCommandsPostAdd(cmd.getCommands());
         cmd = new AxisCmdNumeric(1, axisNumeric,juxtaposed);
         rCommandsPostAdd(cmd.getCommands());
      }
      else
      {
         boolean juxtaposed = false;
         cmd = new AxisCmdBarPlot(1, axisCategory,juxtaposed);
         rCommandsPostAdd(cmd.getCommands());
         cmd = new AxisCmdNumeric(2, axisNumeric,juxtaposed);
         rCommandsPostAdd(cmd.getCommands());
      }

   }
}
