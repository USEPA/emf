package gov.epa.mims.analysisengine.rcommunicator;

import gov.epa.mims.analysisengine.AnalysisException;
import gov.epa.mims.analysisengine.tree.HistogramPlot;
import gov.epa.mims.analysisengine.tree.HistogramType;
import gov.epa.mims.analysisengine.tree.AxisNumeric;
import java.util.ArrayList;


/**
 * generate a histogram R command
 *
 * @version $Revision: 1.2 $
 * @author Tommy E. Cathey
 ********************************************************/
public class HistogramPlotCmd extends PlotCmd
{
   private HistogramPlot p = null;
   /**
    * Creates a new HistogramPlotCmd object.
    * @param p a HistogramPlot 
    ********************************************************/
   public HistogramPlotCmd(HistogramPlot p)
   {
      super(p);
      this.p = p;

      setName("hist");
      setReturnVariable("rtrn");

      HistogramType o = Option.getHistogramType(p);

      String[] y = Rvariable.getName(p.getKeys(0));
      variableAdd("x", y[0]); //data



      //TRUE-plot as frequencies
      //FALSE-plot as relative frequencies
      variableAdd("freq", (o.getFrequency())
                          ? "TRUE"
                          : "FALSE");


      //fill color
      variableAdd("col", Util.parseColor(o.getColor()));


      //border color
      variableAdd("border", Util.parseColor(o.getBorderColor()));

      //IncludeLowest - the default is "true"
      if (!o.getIncludeLowest())
      {
         variableAdd("include.lowest", "FALSE");
      }

      //closure - the default is "true"
      if (!o.getClosure())
      {
         variableAdd("right", "FALSE");
      }

      //break points
      Object breaksObj = o.getBreaks();

      if (breaksObj != null)
      {
         if (breaksObj instanceof double[])
         {
            variableAdd("br", 
                        Util.buildArrayCommand("c", (double[]) breaksObj));
         }
         else if (breaksObj instanceof Integer)
         {
            variableAdd("br", ((Integer) breaksObj).toString());
         }
         else if (breaksObj instanceof String)
         {
            variableAdd("br", (String) breaksObj);
         }
         else
         {
            String msg = "unknown type returned from HistogramType.getBreaks()";
            throw new AnalysisException(msg);
         }
      }

      //bar shading angle
      Double shadingAngleDbl = (Double) o.getShadingAngle();

      if (shadingAngleDbl != null)
      {
         variableAdd("angle", shadingAngleDbl.toString());
      }

      //bar shading density
      Double shadingDensityDbl = (Double) o.getShadingDensity();

      if (shadingDensityDbl != null)
      {
         variableAdd("density", shadingDensityDbl.toString());
      }

      //line type
      String lty = (String) o.getLinetype();
      variableAdd("lty", Util.parseLineTypes(lty));

      //x range
      double[] xlimDbl = (double[]) o.getXRange();

      if (xlimDbl != null)
      {
         Cmd xlimCmd = new Cmd();
         xlimCmd.setName("c");
         xlimCmd.variableAdd(Double.toString(xlimDbl[0]));
         xlimCmd.variableAdd(Double.toString(xlimDbl[1]));
         variableAdd("xlim", (String) xlimCmd.getCommands().get(0));
      }


      //TRUE-draw labels on top of bars
      //FALSE-do not draw labels
      variableAdd("labels", (o.getLabelsOn())
                            ? "TRUE"
                            : "FALSE");


      //default title
      variableAdd("main", "NULL");

      //default x axis
      if (Option.getXAxis(p) != null)
      {
         variableAdd("xaxt", Util.escapeQuote("n"));
         variableAdd("xlab", "NULL");
      }

      //default y axis
      if (Option.getXAxis(p) != null)
      {
         variableAdd("yaxt", Util.escapeQuote("n"));
         variableAdd("ylab", "NULL");
      }
   }
   public java.util.List getCommands()
   {
      //sizingCommand();
      //drawingCommands();
      //super.clearAllVariables();
/*
      AxisNumeric axisNumeric = (AxisNumeric)Option.getYAxis(p);
      variableAdd("plot", "F");
      ArrayList rtrn = new ArrayList();
      variableAdd("x", "2*d1"); //data
      rtrn.addAll(super.getCommands());
      variableAdd("plot", "T");
      variableAdd("x", "d1"); //data
      rtrn.addAll(super.getCommands());
      if((axisNumeric != null)&&(axisNumeric.getAxisRange() != null))
      {
         Double[] range = (Double[])axisNumeric.getAxisRange();
         double[] rng = new double[]{range[0].doubleValue(),range[1].doubleValue()};
         String xl = Util.buildArrayCommand("c",rng);
         rCommandsPreAdd("xl <- " + xl);
      }
      else
      {
         rCommandsPreAdd("xl <- NULL");
      }
*/

      ArrayList rtrn = new ArrayList();
      rtrn.addAll(super.getCommands());

      return rtrn;
   }

}
