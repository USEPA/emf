package gov.epa.mims.analysisengine.rcommunicator;

import gov.epa.mims.analysisengine.tree.Axis;


/**
 * DOCUMENT_ME
 *
 * @version $Revision: 1.2 $
 * @author $author$
 ********************************************************/
public class AxisCmdBarPlot extends AxisCmdAnnotated
{
   /**
    * Creates a new AxisCmdBarPlot object.
    *
    * @param side DOCUMENT_ME
    * @param options DOCUMENT_ME
    ********************************************************/
   public AxisCmdBarPlot(int side, Axis options)
   {
      super(side, options);

      String at = BarPlotAxisHelper.getAt();
      variableAdd("at", at);

      String labels = BarPlotAxisHelper.getLabels();
      variableAdd("labels", labels);
   }
   public AxisCmdBarPlot(int side, Axis options,boolean juxtaposed)
   {
      super(side, options,juxtaposed);

      String at = BarPlotAxisHelper.getAt();
      variableAdd("at", at);

      String labels = BarPlotAxisHelper.getLabels();
      variableAdd("labels", labels);
   }
}
