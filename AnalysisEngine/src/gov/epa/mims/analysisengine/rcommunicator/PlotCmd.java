package gov.epa.mims.analysisengine.rcommunicator;

import gov.epa.mims.analysisengine.tree.Plot;
import gov.epa.mims.analysisengine.tree.DisplaySizeType;
import gov.epa.mims.analysisengine.AnalysisEngineConstants;

/**
 * command options common to all Plots
 *
 * @version $Revision: 1.2 $
 * @author Tommy E. Cathey
 ********************************************************/
public class PlotCmd extends Cmd
implements
gov.epa.mims.analysisengine.tree.AnalysisOptionConstantsIfc
{
   /**
    * Creates a new PlotCmd object.
    * @param side DOCUMENT_ME
    ********************************************************/
   public PlotCmd(Plot p)
   {
      DisplaySizeType o = null;
      o = (DisplaySizeType)p.getOption(
            DISPLAY_SIZE_TYPE);
      Cmd cmd = new DisplaySizeCmd( o );
      super.rCommandsPreAdd( cmd.getCommands() );

      super.rCommandsPreAdd("par(xpd=FALSE)");

//      super.rCommandsPostAdd("box(which=\"plot\",col=\"blue\")");
//      super.rCommandsPostAdd("box(which=\"figure\",col=\"red\")");
//      super.rCommandsPostAdd("box(which=\"inner\",col=\"yellow\")");
//      super.rCommandsPostAdd("box(which=\"outer\",col=\"green\")");
   }
}
