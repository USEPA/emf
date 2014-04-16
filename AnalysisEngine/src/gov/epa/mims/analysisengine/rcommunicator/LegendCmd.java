package gov.epa.mims.analysisengine.rcommunicator;

import gov.epa.mims.analysisengine.AnalysisEngineConstants;
import gov.epa.mims.analysisengine.tree.BarPlot;
import gov.epa.mims.analysisengine.tree.BarType;
import gov.epa.mims.analysisengine.tree.Legend;
import gov.epa.mims.analysisengine.tree.LineType;
import gov.epa.mims.analysisengine.tree.Plot;
import gov.epa.mims.analysisengine.tree.ScatterPlot;

import java.util.ArrayList;
import java.util.List;


/**
 * DOCUMENT_ME
 *
 * @version $Revision: 1.2 $
 * @author $author$
 */
public class LegendCmd extends Cmd implements java.io.Serializable
{
   /** DOCUMENT_ME */
   private boolean absoluteXY = false;

   /** DOCUMENT_ME */
   private String region = null;

   /** DOCUMENT_ME */
   private String position = null;

   /** DOCUMENT_ME */
   private double xjust;

   /** DOCUMENT_ME */
   private double yjust;

   /** DOCUMENT_ME */
   private String rReturnVariable = "LegendCmdSize";

   /**
    * DOCUMENT_ME
    *
    * @param p DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   public LegendCmd(Legend opts)
   {
      setName("legend");
      setReturnVariable(rReturnVariable);

      if (opts == null)
      {
         throw new IllegalArgumentException(getClass().getName()
                                            + " opts=null");
      }

      processOptions(opts);


      //      rCommandsPreAdd("par(\"xpd\"=NA)");
      rCommandsPreAdd("par(\"xpd\"=TRUE)");
      rCommandsPreAdd("par(font=1)");
   }

   /**
    * DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   public List getCommands()
   {
      if (!absoluteXY)
      {
         sizeLegend();
         positionLegend();
      }

      List l = super.getCommands();

      return l;
   }

   /**
    * DOCUMENT_ME
    *
    * @param opts DOCUMENT_ME
    ********************************************************/
   private void processOptions(Legend opts)
   {
      setIssueCommand(opts.getEnable());

      if (opts.getxySet())
      {
         absoluteXY = true;
         variableAdd("x", "" + opts.getX());
         variableAdd("y", "" + opts.getY());
      }
      else
      {
         region = convertRegion(opts.getPosition());
         position = "C";
         xjust = opts.getXJustification();
         yjust = opts.getYJustification();
      }

      variableAdd("bg", Util.parseColor(opts.getBackgroundColor()));
      variableAdd("cex", "" + opts.getCharacterExpansion());

      int ncol = opts.getNumberColumns();

      if (ncol < 1)
      {
         throw new IllegalArgumentException(getClass().getName() + " ncol="
                                            + ncol);
      }

      variableAdd("ncol", "" + ncol);

      if (opts.getHorizontal())
      {
         variableAdd("horiz", "TRUE");
      }

      variableAdd("x.intersp", "" + opts.getXInterspacing());
      variableAdd("y.intersp", "" + opts.getYInterspacing());
   }

   /**
    * DOCUMENT_ME
    *
    * @param region DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   private String convertRegion(String region)
   {
      String rtrn = null;

      if (region.equals(Legend.RIGHT_HAND_MARGIN))
      {
         rtrn = "maRight";
      }
      else if (region.equals(Legend.LEFT_HAND_MARGIN))
      {
         rtrn = "maLeft";
      }
      else if (region.equals(Legend.TOP_HAND_MARGIN))
      {
         rtrn = "maTop";
      }
      else if (region.equals(Legend.BOTTOM_HAND_MARGIN))
      {
         rtrn = "maBot";
      }
      else if (region.equals(Legend.PLOT_REGION))
      {
         rtrn = "maPlot";
      }
      else
      {
         throw new IllegalArgumentException(getClass().getName() + " region=="
                                            + region);
      }

      return rtrn;
   }

   /**
    * DOCUMENT_ME
    ********************************************************/
   private void sizeLegend()
   {
      String u1 = "par(\"usr\")[1]";
      String u2 = "par(\"usr\")[2]";
      String u3 = "par(\"usr\")[3]";
      String u4 = "par(\"usr\")[4]";
      String w = u2 + " - " + u1; //width
      String h = u4 + " - " + u3; //height
      String x = u2 + "+" + w; //x pt outside of plot area
      String y = u4 + "+" + h; //y pt outside of plot area


      //      variableAdd("x",x);
      //      variableAdd("y",y);
      variableAdd("x", "ifelse(par(\"xlog\"),10^(" + x + ")," + x + ")");
      variableAdd("y", "ifelse(par(\"ylog\"),10^(" + y + ")," + y + ")");


      //
      //call getCommands() to get the commands needed
      //to draw the legend out of view of the plot region
      //and add these commands as pre commands. This way
      //the size of the legend can be determined before
      //drawing it.
      //
      rCommandsPreAdd(super.getCommands());
   }

   /**
    * DOCUMENT_ME
    ********************************************************/
   private void positionLegend()
   {
      String arg = Util.escapeQuote(region);
      Cmd cmd = new Cmd();
      cmd.setName("newsPosition");
      cmd.setReturnVariable("legendPos");
      cmd.variableAdd("region", Util.escapeQuote(region));
      cmd.variableAdd("position", Util.escapeQuote(position));
      cmd.variableAdd("xjust", Double.toString(xjust));
      cmd.variableAdd("yjust", Double.toString(yjust));


      //generate the positioning commands and add
      //them to the Pre commands list
      rCommandsPreAdd(cmd.getCommands());

      //legend width returned in rReturnVariable
      String legendW = rReturnVariable + "$rect$w";

      //legend height returned in rReturnVariable
      String legendH = rReturnVariable + "$rect$h";

      //center legend about legendPos point
      //handle the linear and log cases
      String xlinear = "legendPos[1]" + "-" + legendW + "/2";
      String ylinear = "legendPos[2]" + "+" + legendH + "/2";
      String xlog = "legendPos[1]" + "*10^(-" + legendW + "/2)";
      String ylog = "legendPos[2]" + "*10^(" + legendH + "/2)";
      String x = "ifelse(par(\"xlog\")," + xlog + "," + xlinear + ")";
      String y = "ifelse(par(\"ylog\")," + ylog + "," + ylinear + ")";


      //add to R command variable list
      variableAdd("x", x);
      variableAdd("y", y);
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
