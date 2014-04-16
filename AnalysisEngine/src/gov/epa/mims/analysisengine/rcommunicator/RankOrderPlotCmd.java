package gov.epa.mims.analysisengine.rcommunicator;

import gov.epa.mims.analysisengine.AnalysisEngineConstants;
import gov.epa.mims.analysisengine.tree.DataSetIfc;
import gov.epa.mims.analysisengine.tree.LineType;
import gov.epa.mims.analysisengine.tree.RankOrderPlot;
import gov.epa.mims.analysisengine.tree.SortType;


/**
 * generate a RankOrderPlotCmd R command
 *
 * @version $Revision: 1.2 $
 * @author Tommy E. Cathey
 ********************************************************/
public class RankOrderPlotCmd extends MatplotCmd
{
   /**
    * Creates a new RankOrderPlotCmd object.
    * @param p a RankOrderPlot 
    ********************************************************/
   public RankOrderPlotCmd(RankOrderPlot p)
   {
      super(p);
      processDataInfo(p);
   }

   /**
    * DOCUMENT_ME
    *
    * @param p DOCUMENT_ME
    ********************************************************/
   private void processDataInfo(RankOrderPlot p)
   {
      String[] y = Rvariable.getName(p.getKeys(0));
      String[] x = new String[y.length];

      //tell R to create an X data set for each Y data set
      for (int i = 0; i < x.length; i++)
      {
         x[i] = "1:length(" + y[i] + ")";
      }

      //how to sort and handle missing data
      SortType sortType = Option.getSortType(p);
      boolean enableSort = sortType.getEnable();

      if (enableSort)
      {
         String handleMissingData = sortType.getMissingData();

         String decreasing = "TRUE";

         if (sortType.getAscending())
         {
            decreasing = "FALSE";
         }

         String method = sortType.getSortMethod();

         if (method.equals(SortType.SHELL))
         {
            method = "\"shell\"";
         }
         else if (method.equals(SortType.QUICK))
         {
            method = "\"quick\"";
         }
         else
         {
            throw new IllegalArgumentException("unknown sort method: "
                                               + method);
         }

         String ns_last = sortType.getMissingData();

         if (ns_last.equals(SortType.BEGINNING))
         {
            ns_last = "FALSE";
         }
         else if (ns_last.equals(SortType.ENDING))
         {
            ns_last = "TRUE";
         }
         else if (ns_last.equals(SortType.REMOVE))
         {
            ns_last = "NA";
         }
         else
         {
            throw new IllegalArgumentException("unknown data method: "
                                               + ns_last);
         }

         for (int i = 0; i < y.length; i++)
         {
            y[i] = "sort(" + y[i] + ",decreasing=" + decreasing + ",method="
                   + method + ",na.last=" + ns_last + ")";
         }
      }

      super.setX(x);
      super.setY(y);
   }
}
