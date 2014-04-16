package gov.epa.mims.analysisengine.rcommunicator;

import gov.epa.mims.analysisengine.AnalysisEngineConstants;
import gov.epa.mims.analysisengine.tree.Axis;
import gov.epa.mims.analysisengine.tree.AxisNumeric;
import gov.epa.mims.analysisengine.tree.HistogramPlot;
import gov.epa.mims.analysisengine.tree.Text;


/**
 * insures that Objects are called to generate all plot commands
 *
 * @version $Revision: 1.2 $
 * @author Tommy E. Cathey
 ********************************************************/
public class HistogramPlotDriver extends AnnotationDriver
{
   /**
    * Creates a new Driver object.
    * @param p HistogramPlotDriver
    ********************************************************/
   public HistogramPlotDriver(HistogramPlot p)
   {
      super(p);
      rCommandsPreAdd(ReadTableFacade.getCommands(p));

      Cmd cmd = new HistogramPlotCmd(p);
      rCommandsPreAdd(cmd.getCommands());

      //
      //x axis 
      //
      AxisNumeric axis;

      axis = (AxisNumeric)Option.getXAxis(p);

      if(axis == null)
      {
         throw new IllegalArgumentException(getClass().getName()+" axis == null");
      }

      cmd = new AxisCmdNumeric(1,axis);
      rCommandsPreAdd(cmd.getCommands());

      //
      //y axis 
      //
      axis = (AxisNumeric)Option.getYAxis(p);

      if(axis == null)
      {
         throw new IllegalArgumentException(getClass().getName()+" axis == null");
      }

      cmd = new AxisCmdNumeric(2,axis);
      rCommandsPreAdd(cmd.getCommands());

   }
}
