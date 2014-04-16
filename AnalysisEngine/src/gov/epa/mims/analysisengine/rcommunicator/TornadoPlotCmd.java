package gov.epa.mims.analysisengine.rcommunicator;

import gov.epa.mims.analysisengine.AnalysisException;
import gov.epa.mims.analysisengine.tree.TornadoPlot;
import gov.epa.mims.analysisengine.tree.TornadoType;
import gov.epa.mims.analysisengine.tree.DataSetIfc;
import gov.epa.mims.analysisengine.tree.AxisNumeric;
import gov.epa.mims.analysisengine.tree.LabeledDataSetIfc;
import gov.epa.mims.analysisengine.tree.TextAttribute;


import java.util.List;
import java.util.Iterator;


/**
 * generate a bar plot R command
 *
 * @version $Revision: 1.2 $
 * @author Tommy E. Cathey
 ********************************************************/
public class TornadoPlotCmd extends PlotCmd
{

   /**
    * Creates a new TornadoPlotCmd object.
    * @param p a TornadoPlot 
    ********************************************************/
   public TornadoPlotCmd(TornadoPlot p)
   {
      super(p);
//      
//      ###########################################################
//      # TornadoPlot
//      ###########################################################
//      # cv - color
//      # zv - border color
//      # hx - height matrix
//      # wv - width
//      # sv - space
//      # xl - x limit vector
//      # yl - y limit vector
//      # pt - plot flag
//      # b1 - lty vector for border
//      # b2 - lwd vector for border
//      ###########################################################
//      Tornado <- function(cv,zv,hx,wv,sv,xl,yl,pt,b1,b2)
//      
// public   java.awt.Color getColor();
// public   [D getYlim();
// public   java.awt.Color getBorderColor();
// public   java.lang.String getBorderLty();
// public   double getBorderLwd();
// public   double getWidth();
// public   double getSpaceBetweenBars();
      TornadoType tornadoType = Option.getTornadoType(p);

      rCommandsPreAdd("hx <- d1");

      rCommandsPreAdd("cv <- " + Util.parseColor( tornadoType.getColor() ));

      double[] ylimArray = tornadoType.getYlim();
      String ylim= (ylimArray == null)
                     ? "NULL"
                     : Util.buildArrayCommand("c", 
                                             Util.convertToStringArray(ylimArray));
      rCommandsPreAdd("yl <- " + ylim + " #y limits % of plot y axis");

      AxisNumeric axisNumeric = (AxisNumeric)Option.getXAxis(p);
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

      rCommandsPreAdd("zv <- " + Util.parseColor( tornadoType.getBorderColor() ));

      rCommandsPreAdd("b1 <- " + Util.parseLineTypes(tornadoType.getBorderLty()) + " #lty for border");

      rCommandsPreAdd("b2 <- " + tornadoType.getBorderLwd() + " #lwd for border");

      rCommandsPreAdd("wv <- " + tornadoType.getWidth() + " #width for border");

      rCommandsPreAdd("sv <- " + tornadoType.getSpaceBetweenBars() + " #spacing between bars");
       
      rCommandsPreAdd("mxANDhx <- TornadoPlot(cv,zv,hx,wv,sv,xl,yl,pt,b1,b2)");

      TextAttribute textAttribute = tornadoType.getTextAttribute();

      String fontColor = Util.parseColor(textAttribute.getColor());
      rCommandsPreAdd("fgOrig <- par(fg="+fontColor + ")");
      rCommandsPostAdd("par(fg=fgOrig)");

      double fontSize = textAttribute.getCex();
      rCommandsPreAdd("cexOrig <- par(cex="+fontSize + ")");
      rCommandsPostAdd("par(cex=cexOrig)");

      String font = LookUp.get(textAttribute.getFont());
      rCommandsPreAdd("fontOrig <- par(font="+font + ")");
      rCommandsPostAdd("par(font=fontOrig)");

      rCommandsPreAdd("mx <- mxANDhx[[1]]");
      rCommandsPreAdd("hx <- mxANDhx[[2]]");
      rCommandsPreAdd("lb <- d1Label[mxANDhx[[3]]]");
      rCommandsPreAdd("TornadoLabels(mx, hx, lb)");

   }
}
