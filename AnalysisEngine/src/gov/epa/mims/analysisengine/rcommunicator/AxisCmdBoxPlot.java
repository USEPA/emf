package gov.epa.mims.analysisengine.rcommunicator;

import gov.epa.mims.analysisengine.tree.Axis;


/**
 * a class to handle the tick mark axis labeling of a Box Plot
 *
 * @version $Revision: 1.2 $
 * @author Tommy E. Cathey
 ********************************************************/
public class AxisCmdBoxPlot extends AxisCmdAnnotated
{
   public AxisCmdBoxPlot(int side, Axis options,boolean juxtaposed)
   {
      super(side, options,juxtaposed);

      //atX is the return variable from bxp() (i.e. atX <- bxp(...) )
      //and contains the locations of the boxes; the R variable "atXlabels"
      //should have already been set by BoxPlotDriver.java but the user
      //can override "atXlabels" by a call to 
      //Axis.setUserTickMarkLabels(java.lang.String[] arg);
      //he can also override "atX" by calling 
      //Axis.setUserTickMarkPositions(double[] arg)
      //the drawing of labels is toggled by calling
      //Axis.setDrawTickMarkLabels(boolean arg)

      String[] userTickMarkLabels = options.getUserTickMarkLabels();
      double[] userTickMarkPositions = options.getUserTickMarkPositions();
      boolean drawTickMarkLabels = options.getDrawTickMarkLabels();

      if(drawTickMarkLabels)
      {
         if(userTickMarkLabels != null)
         {
            String[] labels = Util.escapeQuote(userTickMarkLabels);
            rCommandsPreAdd("atXlabels <- " + Util.buildArrayCommand("c",labels));
         }
         if(userTickMarkPositions != null)
         {
            rCommandsPreAdd("atX <- " + Util.buildArrayCommand("c",userTickMarkPositions));
         }
         variableAdd("at","atX");
         variableAdd("labels","atXlabels");
      }

   }
}
