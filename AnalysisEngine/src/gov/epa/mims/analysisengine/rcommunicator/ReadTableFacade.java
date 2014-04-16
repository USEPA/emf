package gov.epa.mims.analysisengine.rcommunicator;

import java.util.HashMap;
import java.util.List;
import java.util.Date;
import java.util.ArrayList;
import gov.epa.mims.analysisengine.tree.Plot;
import gov.epa.mims.analysisengine.tree.TimeSeries;
import gov.epa.mims.analysisengine.tree.TornadoPlot;
import gov.epa.mims.analysisengine.tree.BarPlot;
import gov.epa.mims.analysisengine.tree.BoxPlot;
import gov.epa.mims.analysisengine.tree.DiscreteCategoryPlot;
import gov.epa.mims.analysisengine.tree.HistogramPlot;
import gov.epa.mims.analysisengine.tree.RankOrderPlot;
import gov.epa.mims.analysisengine.tree.ScatterPlot;
import gov.epa.mims.analysisengine.tree.CdfPlot;
import gov.epa.mims.analysisengine.tree.AxisTime;
import gov.epa.mims.analysisengine.tree.DataSetIfc;
import gov.epa.mims.analysisengine.tree.LabeledDataSetIfc;
import gov.epa.mims.analysisengine.tree.PageType;
import gov.epa.mims.analysisengine.AnalysisException;



/**
 * DOCUMENT ME!
 *
 * @version $Revision: 1.2 $
 * @author Tommy E. Cathey 
 */
public class ReadTableFacade
{
   public static List getCommands(TimeSeries p)
   {
      List keys = p.getDataKeyList();
      HashMap key2DataSet = getKey2DataSet(p,keys);
      boolean deleteOnExit = getDeleteOnExit(p);
      AxisTime axisX = Option.getXAxis(p);

      ArrayList commands = new ArrayList();
      Date w1 = axisX.getFirstTickMark();
      Date w2 = null;
      Object[] wObj = axisX.getAxisRange();
      if(wObj != null)
      {
         w1 = (Date)wObj[0];
         w2 = (Date)wObj[1];
         commands.add("world1 <- " + w1.getTime());
         commands.add("world2 <- " + w2.getTime());
         
      }  
      else
      {
         if( w1 != null)
         {
            commands.add("world1 <- " + w1.getTime());
         }
         else
         {
            commands.add("world1 <- NULL");
         }
         commands.add("world2 <- NULL");
      }
      ReadTableCmd cmd = null;
      cmd = new ReadTableCmd(key2DataSet, keys, deleteOnExit);
      commands.addAll(cmd.getCommands());

      Date d1 = cmd.getEarliestTimeStamp();
      Date d2 = cmd.getLatestTimeStamp();
      TimeSeriesAxisConverter.init(d1,d2);

      return commands;
   }
   
   private static boolean getDeleteOnExit(Plot p)
   {
      PageType pageType = Option.getPageType(p);
      boolean deleteOnExit = pageType.getDeleteTemporaryFileOnExit();
      return deleteOnExit;
   }

   private static HashMap getKey2DataSet(Plot p, List keys)
   {
      HashMap key2DataSet = new HashMap();

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
      return key2DataSet;
   }


   public static List getCommands(BarPlot p)
   {
      List keys = p.getDataKeyList();
      HashMap key2DataSet = getKey2DataSet(p,keys);
      boolean deleteOnExit = getDeleteOnExit(p);
      Cmd cmd = new ReadTableCmd(key2DataSet, keys, deleteOnExit);
      return cmd.getCommands();
   }


   public static List getCommands(TornadoPlot p)
   {
      List keys = p.getDataKeyList();
      HashMap key2DataSet = getKey2DataSet(p,keys);
      boolean deleteOnExit = getDeleteOnExit(p);
      Cmd cmd = new ReadTableCmd(key2DataSet, keys, deleteOnExit);
      return cmd.getCommands();
   }

   public static List getCommands(BoxPlot p)
   {
      List keys = p.getDataKeyList();
      HashMap key2DataSet = getKey2DataSet(p,keys);
      boolean deleteOnExit = getDeleteOnExit(p);
      Cmd cmd = new ReadTableCmd(key2DataSet, keys, deleteOnExit);
      return cmd.getCommands();
   }

   public static List getCommands(DiscreteCategoryPlot p)
   {
      List keys = p.getDataKeyList();
      HashMap key2DataSet = getKey2DataSet(p,keys);
      boolean deleteOnExit = getDeleteOnExit(p);
      Cmd cmd = new ReadTableCmd(key2DataSet, keys, deleteOnExit);
      return cmd.getCommands();
   }

   public static List getCommands(HistogramPlot p)
   {
      List keys = p.getDataKeyList();
      HashMap key2DataSet = getKey2DataSet(p,keys);
      boolean deleteOnExit = getDeleteOnExit(p);
      Cmd cmd = new ReadTableCmd(key2DataSet, keys, deleteOnExit);
      return cmd.getCommands();
   }

   public static List getCommands(RankOrderPlot p)
   {
      List keys = p.getDataKeyList();
      HashMap key2DataSet = getKey2DataSet(p,keys);
      boolean deleteOnExit = getDeleteOnExit(p);
      Cmd cmd = new ReadTableCmd(key2DataSet, keys, deleteOnExit);
      return cmd.getCommands();
   }

   public static List getCommands(ScatterPlot p)
   {
      List keys = p.getDataKeyList();
      HashMap key2DataSet = getKey2DataSet(p,keys);
      boolean deleteOnExit = getDeleteOnExit(p);
      Cmd cmd = new ReadTableCmd(key2DataSet, keys, deleteOnExit);
      return cmd.getCommands();
   }

   public static List getCommands(CdfPlot p)
   {
      List keys = p.getDataKeyList();
      HashMap key2DataSet = getKey2DataSet(p,keys);
      boolean deleteOnExit = getDeleteOnExit(p);
      Cmd cmd = new ReadTableCmd(key2DataSet, keys, deleteOnExit);
      return cmd.getCommands();
   }
}
