package gov.epa.mims.analysisengine.rcommunicator;

import gov.epa.mims.analysisengine.AnalysisEngineConstants;
import gov.epa.mims.analysisengine.tree.Axis;
import gov.epa.mims.analysisengine.tree.AxisNumeric;
import gov.epa.mims.analysisengine.tree.DataSetIfc;
import gov.epa.mims.analysisengine.tree.Legend;
import gov.epa.mims.analysisengine.tree.LineType;
import gov.epa.mims.analysisengine.tree.RankOrderPlot;
import gov.epa.mims.analysisengine.tree.Text;


/**
 * insures that Objects are called to generate all plot commands
 *
 * @version $Revision: 1.2 $
 * @author Tommy E. Cathey
 ********************************************************/
public class RankOrderPlotDriver extends AnnotationDriver
implements
gov.epa.mims.analysisengine.tree.AnalysisOptionConstantsIfc
{
   /**
    * Creates a new Driver object.
    * @param p RankOrderPlotDriver
    ********************************************************/
   public RankOrderPlotDriver(RankOrderPlot p)
   {
      super(p);
      rCommandsPreAdd(ReadTableFacade.getCommands(p));

      Cmd cmd = new RankOrderPlotCmd(p);
      rCommandsPreAdd(cmd.getCommands());

      AxisNumeric axis;


      //
      //x axis 
      //
      axis = (AxisNumeric)Option.getXAxis(p);

      if(axis == null)
      {
         throw new IllegalArgumentException(getClass().getName() + " axis==null");
      }

      cmd = new AxisCmdNumeric(1, axis);
      rCommandsPreAdd(cmd.getCommands());


      //
      //y axis 
      //
      axis = (AxisNumeric)Option.getYAxis(p);

      if(axis == null)
      {
         throw new IllegalArgumentException(getClass().getName() + " axis==null");
      }
      cmd = new AxisCmdNumeric(2, axis);
      rCommandsPreAdd(cmd.getCommands());


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
                                                      p.getKeys(0), //0 corresponds to the y value keys
                                                      ds);
         rCommandsPreAdd(legCmd.getCommands());
      }
   }
}
