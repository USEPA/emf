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
public class AxisCmdGrids extends AxisCmdAnnotated
{
   /**
    * Creates a new AxisCmdGrids object.
    *
    * @param side DOCUMENT_ME
    * @param options DOCUMENT_ME
    ********************************************************/
   public AxisCmdGrids(int side, AxisNumeric options)
   {
      super(side, options);

      if (options.getGridEnable())
      {
         if (options.getLogScale())
         {
            rCommandsPreAdd(doGrid(side, options));
            rCommandsPreAdd(doTicks(side, options));
         }
         else
         {
            rCommandsPreAdd(doLinearGrid(side, options));
         }
      }

   }
   public AxisCmdGrids(int side, AxisNumeric options,boolean juxtaposed)
   {
      super(side, options,juxtaposed);

      if (options.getGridEnable())
      {
         if (options.getLogScale())
         {
            rCommandsPreAdd(doGrid(side, options));
            rCommandsPreAdd(doTicks(side, options));
         }
         else
         {
            rCommandsPreAdd(doLinearGrid(side, options));
         }
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

   private List doLinearGrid(int side, AxisNumeric o)
   {
         double initPt = o.getInitialPoint();
         double finalPt = o.getFinalPoint();
         double increment = o.getIncrement();
         int intervals = o.getIntervalCount();

         String at = null;

         if (!Double.isNaN(initPt))
         {
            double x1 = initPt;
            String x2;
            double by;

            if (intervals > 0)
            {
               int cnt = intervals;

               if (!Double.isNaN(finalPt))
               {
                  x2 = "" + finalPt;
               }
               else
               {
                  String cName = getClass().getName();
                  throw new IllegalArgumentException(cName
                                                     + " finalPt==NaN but intervals!=null");
               }

               by = (finalPt - x1) / cnt;
            }
            else if (!Double.isNaN(increment))
            {
               by = increment;

               if (!Double.isNaN(finalPt))
               {
                  x2 = "" + finalPt;
               }
               else
               {
                  int indx = ((side == 1) || (side == 3))
                             ? 2
                             : 4;
                  x2 = "par(\"usr\")[" + indx + "]";
               }
            }
            else
            {
               String cName = getClass().getName();
               throw new IllegalArgumentException(cName
                                                  + " initPt==NaN and increment==null");
            }

            at = "seq(" + x1 + "," + x2 + ",by=" + by + ")";
         }
         else
         {
            at = "axTicks(" + side + ")";
         }

         String lty = o.getGridlineStyle();
         double lwd = o.getGridlineWidth();
         Color color = o.getGridColor();

         Cmd cmd = new AblineCmd(side, at, color, lty, lwd);

         return cmd.getCommands();
   }

}
