package gov.epa.mims.analysisengine.rcommunicator;

import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

import gov.epa.mims.analysisengine.AnalysisEngineConstants;
import gov.epa.mims.analysisengine.tree.Axis;
import gov.epa.mims.analysisengine.tree.AxisTime;
import gov.epa.mims.analysisengine.tree.AxisNumeric;
import gov.epa.mims.analysisengine.tree.Text;
import gov.epa.mims.analysisengine.tree.TimeSeries;
import gov.epa.mims.analysisengine.tree.PageType;
import gov.epa.mims.analysisengine.tree.DataSetIfc;
import gov.epa.mims.analysisengine.AnalysisException;


/**
 * insures that Objects are called to generate all plot commands
 *
 * @version $Revision: 1.2 $
 * @author Tommy E. Cathey
 ********************************************************/
public class TimeSeriesDriver extends AnnotationDriver
implements
gov.epa.mims.analysisengine.tree.AnalysisOptionConstantsIfc
{
   /**
    * Creates a new Driver object.
    * @param p TimeSeriesDriver
    ********************************************************/
   public TimeSeriesDriver(TimeSeries p)
   {
      super(p);
/*
      HashMap key2DataSet = new HashMap();
      List keys = p.getDataKeyList();
      for(int i=0;i<keys.size();i++)
      {
         Object key = keys.get(i);
         DataSetIfc ds = p.getDataSet(key);
         if(ds ==null)
         {
            String msg = "Unable to find DataSetIfc with key=" + key;
            throw new AnalysisException(msg);
         }
         key2DataSet.put(key,ds);
      }
      PageType pageType = Option.getPageType(p);
      boolean deleteOnExit = pageType.getDeleteTemporaryFileOnExit();
      AxisTime axisX = Option.getXAxis(p);
      Date firstTickMark = axisX.getFirstTickMark();
      Object[] wObj = axisX.getAxisRange();
      Date[] world = null;
      if(wObj != null)
      {
         world = new Date[2];
         world[0] = (Date)wObj[0];
         world[1] = (Date)wObj[1];
      }
      Cmd cmd = null;
      cmd = new ReadTableDateDataSetIfcCmd(key2DataSet, keys,
         deleteOnExit,world,firstTickMark);
      rCommandsPreAdd(cmd.getCommands());
*/
      Cmd cmd = null;
      rCommandsPreAdd(ReadTableFacade.getCommands(p));

      //      rCommandsPreAdd(MatplotCmd.getCommands(p));
      cmd = new TimeSeriesPlotCmd(p);
      rCommandsPreAdd(cmd.getCommands());

      cmd = null;

      //
      //x axis 
      //
      Axis axis;

      axis = (Axis)Option.getXAxis(p);

      if (axis != null)
      {
         if(axis instanceof AxisTime)
         {
            cmd = new AxisCmdTime(1, (AxisTime)axis);
         }
         else
         {
            throw new IllegalArgumentException(getClass().getName()
            + " Only AxisTime can be used as X axis in TimeSeriesPlot");
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
            + " Only AxisNumeric can be used as Y axis in TimeSeriesPlot");
         }

         rCommandsPreAdd(cmd.getCommands());
      }

      rCommandsPreAdd(cmd.getCommands());

      //
      //      axisObj = Option.getXAxis(p);
      //
      //      if (axisObj != null)
      //      {
      //         if (axisObj instanceof AxisLog)
      //         {
      //            cmd = new AxisCmdLog(1, (AxisLog) axisObj);
      //         }
      //         else if (axisObj instanceof AxisGrid)
      //         {
      //            cmd = new AxisCmdGrid(1, (AxisGrid) axisObj);
      //         }
      //         else
      //         {
      //            cmd = new AxisCmdAnnotated(1, (Axis) axisObj);
      //         }
      //
      //         rCommandsPreAdd(cmd.getCommands());
      //      }
      //
      //
      //      //
      //      //y axis 
      //      //
      //      axisObj = Option.getYAxis(p);
      //
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
   }
}
