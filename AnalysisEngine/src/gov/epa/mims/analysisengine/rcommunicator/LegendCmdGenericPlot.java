package gov.epa.mims.analysisengine.rcommunicator;

import gov.epa.mims.analysisengine.tree.Legend;

import java.util.ArrayList;
import java.util.List;


/**
 * create generic legends
 *
 * @version $Revision: 1.2 $
 * @author Tommy E. Cathey
 */
public class LegendCmdGenericPlot extends LegendCmd implements java.io.Serializable
{
   private boolean NEW_CODE = true;
   /**
    * create a legend with color fill and labels
    *
    * @param fill the fill argument in R's legend command
    * @param labels the labels argument in R's legend command
    *
    ********************************************************/
   public LegendCmdGenericPlot(Legend l, String fill, String labels) 
   {
      super(l);

      if(labels != null)
         variableAdd("legend", labels);
      if(fill != null)
         variableAdd("fill", fill);
   }

}
