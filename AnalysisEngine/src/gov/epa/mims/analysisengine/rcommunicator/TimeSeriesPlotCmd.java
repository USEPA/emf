package gov.epa.mims.analysisengine.rcommunicator;

import gov.epa.mims.analysisengine.AnalysisEngineConstants;
import gov.epa.mims.analysisengine.tree.LineType;
import gov.epa.mims.analysisengine.tree.TimeSeries;

import java.util.List;


/**
 * generate a ScatterPlotCmd R command
 *
 * @version $Revision: 1.2 $
 * @author Tommy E. Cathey
 ********************************************************/
public class TimeSeriesPlotCmd extends MatplotCmd
{
   /**
    * Creates a new TimeSeriesPlotCmd object.
    * @param p a TimeSeries 
    ********************************************************/
   public TimeSeriesPlotCmd(TimeSeries p)
   {
      super(p);
      processDataInfo(p);
      axisRange(p);
   }

   private void axisRange(TimeSeries p)
   {
      double min;
      double max;

      min = gov.epa.mims.analysisengine.AnalysisEngineConstants.TIME_SERIES_USER_COORD1;
      max = gov.epa.mims.analysisengine.AnalysisEngineConstants.TIME_SERIES_USER_COORD2;

      super.setXRange(min, max);
   }
   /**
    * DOCUMENT_ME
    *
    * @param p DOCUMENT_ME
    ********************************************************/
   private void processDataInfo(TimeSeries p)
   {
      Object[] keyArray;
      List keyList = ((TimeSeries) p).getDataKeyList();
      keyArray = keyList.toArray();

      String[] x = new String[keyArray.length];
      String[] y = new String[keyArray.length];

      for (int i = 0; i < keyArray.length; i++)
      {
         Object key = keyArray[i];
         String rVar = Rvariable.getName(key);
         x[i] = rVar + "TimeStamp";
         y[i] = rVar;
      }

      super.setX(x);
      super.setY(y);
   }
}
