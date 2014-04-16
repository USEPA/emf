package gov.epa.mims.analysisengine.rcommunicator;

import gov.epa.mims.analysisengine.tree.Axis;
import gov.epa.mims.analysisengine.tree.AxisTime;

import java.awt.Color;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * DOCUMENT_ME
 *
 * @version $Revision: 1.2 $
 * @author $author$
 ********************************************************/
public class AxisCmdTimeSeriesGrid extends AxisCmdTimeSeries
{
   /**
    * Creates a new AxisCmdTimeSeriesGrid object.
    *
    * @param side DOCUMENT_ME
    * @param options DOCUMENT_ME
    ********************************************************/
   public AxisCmdTimeSeriesGrid(int side, AxisTime options)
   {
      super(side, options);

      if (options.getGridEnable())
      {
         Date initPt = options.getInitialPoint();
         Date finalPt = options.getFinalPoint();
         long increment = options.getTickIncrement();
         Integer intervals = options.getIntervalCount();
         String at = "axTicks(" + side + ")";
         String lty = options.getGridlineStyle();
         double lwd = options.getGridlineWidth();
         Color color = options.getGridColor();

         if (initPt != null)
         {
            double x1 = TimeSeriesAxisConverter.date2user(initPt);
            String x2;
            double by;

            if (intervals != null)
            {
               int cnt = intervals.intValue();

               if (finalPt != null)
               {
                  x2 = "" + TimeSeriesAxisConverter.date2user(finalPt);
               }
               else
               {
                  String cName = getClass().getName();
                  throw new IllegalArgumentException(cName
                                                     + " finalPt==null but intervals!=null");
               }

               by = (TimeSeriesAxisConverter.date2user(finalPt) - x1) / cnt;
            }
            else if (increment > 0)
            {
               //increment is the separation between ticks on the
               //time axis in time units; we need this separation
               //in user coordinates
               long t1 = initPt.getTime();
               Date tic2Date = new Date(t1 + increment);
               double tic2InUserSpace;
               tic2InUserSpace = TimeSeriesAxisConverter.date2user(tic2Date);

               by = tic2InUserSpace - x1;

               if (finalPt != null)
               {
                  x2 = "" + TimeSeriesAxisConverter.date2user(finalPt);
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
                                                  + " initPt==null and increment<=0");
            }

            at = "seq(" + x1 + "," + x2 + ",by=" + by + ")";
         }
         else
         {
            double[] ticks = TimeSeriesAxisConverter.getGeneratedTickmarks();
            at = Util.buildArrayCommand("c", ticks);
         }

         Cmd cmd = new AblineCmd(side, at, color, lty, lwd);

         rCommandsPostAdd(cmd.getCommands());
      }
   }
}
