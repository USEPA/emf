package gov.epa.mims.analysisengine.rcommunicator;

import gov.epa.mims.analysisengine.tree.Axis;
import gov.epa.mims.analysisengine.tree.AxisNumeric;

import java.awt.Color;

import java.util.ArrayList;
import java.util.List;


/**
 * DOCUMENT_ME
 *
 * @version $Revision: 1.2 $
 * @author $author$
 ********************************************************/
public class AxisCmdLog extends AxisCmdAnnotated
{
   /** DOCUMENT_ME */
   private Cmd localCmd = new Cmd();

   /** DOCUMENT_ME */
   private Cmd opposingTicksCmd = new Cmd();

   /** DOCUMENT_ME */
//   private String rFuncName = "logTicks";

   /**
    * Creates a new AxisCmdLog object.
    *
    * @param side DOCUMENT_ME
    * @param options DOCUMENT_ME
    ********************************************************/
   public AxisCmdLog(int side, AxisNumeric options)
   {
      super(side, options);

      if(options.getLogScale() && options.getGridEnable())
      {
         rCommandsPreAdd(doGrid(side, options));
         rCommandsPreAdd(doTicks(side, options));
      }
   }

   /**
    * DOCUMENT_ME
    *
    * @param side DOCUMENT_ME
    * @param o DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   private List doGrid(int side, AxisNumeric o)
   {
      ArrayList rtrn = new ArrayList();

      if (o.getGridEnable())
      {
         String lty = o.getGridlineStyle();
         lty = Util.parseLineTypes(lty);

         double lwd = o.getGridlineWidth();
         Color c = o.getGridColor();

         double min = o.getInitialPoint();
         double max = o.getFinalPoint();
         double incr = o.getIncrement();

         Cmd logGridCmd = new LogGridCmd(side, c, lty, lwd,min,max,incr);
         rtrn.addAll(logGridCmd.getCommands());
      }

      return rtrn;
   }

   /**
    * DOCUMENT_ME
    *
    * @param side DOCUMENT_ME
    * @param o DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   private List doTicks(int side, AxisNumeric o)
   {
      ArrayList rtrn = new ArrayList();

//      if (o.getTickmarkEnable())
      if (o.getGridTickmarkEnable())
      {
//         double tcl = o.getTickmarkLength();
         double tcl = o.getGridTickmarkLength();

         double lwd = o.getGridlineWidth();
//         Color c = o.getTickmarkColor();
         Color c = o.getGridColor();

         double min = o.getInitialPoint();
         double max = o.getFinalPoint();
         double incr = o.getIncrement();

         Cmd logTickCmd = new LogTickCmd(side, c, tcl, lwd,min,max,incr);
         rtrn.addAll(logTickCmd.getCommands());

         int opposite = (side > 2)
                        ? (side - 2)
                        : (side + 2);


         Cmd logTickCmdOp = new LogTickCmd(opposite, c, tcl, lwd,min,max,incr);
         rtrn.addAll(logTickCmdOp.getCommands());
      }

      return rtrn;
   }
}
