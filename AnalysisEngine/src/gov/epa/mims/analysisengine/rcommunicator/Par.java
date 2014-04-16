package gov.epa.mims.analysisengine.rcommunicator;

import gov.epa.mims.analysisengine.AnalysisEngineConstants;
import gov.epa.mims.analysisengine.tree.Legend;
import gov.epa.mims.analysisengine.tree.Plot;

import java.util.ArrayList;

import java.io.Serializable;

/**
 * initialize R with some par commands
 *
 * @version $Revision: 1.2 $
 * @author $author$
 */
public class Par implements Serializable, Cloneable,
gov.epa.mims.analysisengine.tree.AnalysisOptionConstantsIfc
{
   /** list of R commands to return */
   private static ArrayList rCommands = new ArrayList();

   /**
    * generate R par commands to handle legends in the margin of a plot
    *
    * @param p the current Plot
    *
    * @return R par commands
    */
   public static String[] getPltCmd(Plot p)
   {
      //
      // clear out any old commands
      //
      rCommands.clear();


      //
      // save the original par(plt) into parORIGplt, parLEGENDplt, & parPLOTplt
      //
      rCommands.add("parORIGplt<-par(\"plt\")");

      //      rCommands.add("parLEGENDplt<-par(\"plt\")");
      //      rCommands.add("parPLOTplt<-par(\"plt\")");
      Legend legend = (Legend) p.getOption(LEGEND);

      if (legend != null)
      {
         double legendRegionSize = legend.getLegendRegionSize();
         String position = legend.getPosition();

         if (position.equals("R"))
         {
            //
            // legend on the right
            //
            rCommands.add(
                  "par(mai=c(par(\"mai\")[1],par(\"mai\")[1],par(\"mai\")[1],"
                  + legendRegionSize + "))");
         }
         else if (position.equals("L"))
         {
            //
            // legend on the left
            //
            rCommands.add("tmpLegendSize<-((parORIGplt[2]-parORIGplt[1])*"
                          + legendRegionSize + ")");
            rCommands.add("tmpBoundary<-(parORIGplt[1]+tmpLegendSize)");

            String t1 = "parORIGplt[3],parORIGplt[4])";
            rCommands.add("parPLOTplt  <-c(tmpBoundary  ,parORIGplt[2]," + t1);
            rCommands.add("parLEGENDplt<-c(parORIGplt[1],tmpBoundary  ," + t1);
         }
         else if (position.equals("T"))
         {
            //
            // legend on the top
            //
            rCommands.add("tmpLegendSize<-((parORIGplt[4]-parORIGplt[3])*"
                          + legendRegionSize + ")");
            rCommands.add("tmpBoundary<-(parORIGplt[4]-tmpLegendSize)");

            String t1 = "<-c(parORIGplt[1],parORIGplt[2],";
            rCommands.add("parPLOTplt  " + t1 + "parORIGplt[3],tmpBoundary  )");
            rCommands.add("parLEGENDplt" + t1 + "tmpBoundary  ,parORIGplt[4])");
         }
         else if (position.equals("B"))
         {
            //
            // legend on the bottom
            //
            rCommands.add("tmpLegendSize<-((parORIGplt[4]-parORIGplt[3])*"
                          + legendRegionSize + ")");
            rCommands.add("tmpBoundary<-(parORIGplt[3]+tmpLegendSize)");

            String t1 = "<-c(parORIGplt[1],parORIGplt[2],";
            rCommands.add("parPLOTplt  " + t1 + "tmpBoundary  ,parORIGplt[4])");
            rCommands.add("parLEGENDplt" + t1 + "parORIGplt[3],tmpBoundary  )");
         }
      }

      //
      // convert the rCommands List to an array
      //
      String[] returnStrings = new String[rCommands.size()];
      rCommands.toArray(returnStrings);

      return returnStrings;
   }

   /**
    * describe object in a String
    *
    * @return String describing object
    ******************************************************/
   public String toString()
   {
      return Util.toString(this);
   }
}
