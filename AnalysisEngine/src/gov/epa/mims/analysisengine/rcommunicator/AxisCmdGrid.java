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
public class AxisCmdGrid extends AxisCmdAnnotated
{
   /**
    * Creates a new AxisCmdGrid object.
    *
    * @param side DOCUMENT_ME
    * @param options DOCUMENT_ME
    ********************************************************/
   public AxisCmdGrid(int side, AxisNumeric options)
   {
      super(side, options);

      if (options.getGridEnable())
      {
         double initPt = options.getInitialPoint();
         double finalPt = options.getFinalPoint();
         double increment = options.getIncrement();
         int intervals = options.getIntervalCount();

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
                                                  + " initPt==null and increment==null");
            }

            at = "seq(" + x1 + "," + x2 + ",by=" + by + ")";
         }
         else
         {
            at = "axTicks(" + side + ")";
         }

         String lty = options.getGridlineStyle();
         double lwd = options.getGridlineWidth();
         Color color = options.getGridColor();

         Cmd cmd = new AblineCmd(side, at, color, lty, lwd);

         rCommandsPostAdd(cmd.getCommands());
      }
   }
}
