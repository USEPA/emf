package gov.epa.mims.analysisengine.rcommunicator;

import gov.epa.mims.analysisengine.tree.Axis;


/**
 * DOCUMENT_ME
 *
 * @version $Revision: 1.2 $
 * @author $author$
 ********************************************************/
public class AxisCmdHistogramPlot extends AxisCmdAnnotated
{
   /**
    * Creates a new AxisCmdHistogramPlot object.
    *
    * @param side DOCUMENT_ME
    * @param options DOCUMENT_ME
    ********************************************************/
   public AxisCmdHistogramPlot(int side, Axis options)
   {
      super(side, options);

      variableAdd("at", "rtrn$mids");

      variableAdd("labels", "format(rtrn$mids,digits=1,trim=TRUE)");
   }
}
