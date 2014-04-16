package gov.epa.mims.analysisengine.rcommunicator;

import gov.epa.mims.analysisengine.AnalysisException;
import gov.epa.mims.analysisengine.tree.BarPlot;
import gov.epa.mims.analysisengine.tree.BarType;
import gov.epa.mims.analysisengine.tree.DataSetIfc;
import gov.epa.mims.analysisengine.tree.AxisNumeric;
import gov.epa.mims.analysisengine.tree.LabeledDataSetIfc;


import java.util.List;
import java.util.Iterator;


/**
 * generate a bar plot R command
 *
 * @version $Revision: 1.2 $
 * @author Tommy E. Cathey
 ********************************************************/
public class BarPlotCmd extends PlotCmd
{

   /**
    * Creates a new BarPlotCmd object.
    * @param p a BarPlot 
    ********************************************************/
   public BarPlotCmd(BarPlot p)
   {
      super(p);
/*
*tmp <- BarHeightX()
*lx <- tmp[[2]]  #label matrix
*
*xl <- NULL
*yl <- NULL
*cv <- c("red","blue","green","yellow")
*cp <- t( tmp[[3]] )
*hx <- t( tmp[[1]] )
*wv <- c(0.1)
*sv <- c(1,3)
*hz <- FALSE
*bs <- TRUE
*dv <- NULL
*av <- c(45.0)
*tv <- c(1,2)
*lv <- c(1,2)
*pt <- FALSE
*lg <- TRUE
*sd <- FALSE
*
*myBarPlot(xl,yl,cv,cp,hx,wv,sv,hz,dv,av,tv,lv,pt,lg,bs,sd)


bc 

*/

if(true)
{

      //
      // get the BarType options for this plot
      //
      BarType barType = Option.getBarType(p);

      //
      //BarHeightX() returns a "list" containing:
      //  hx - height matrix
      //  lx - label matrix
      //  cp - color permutation matrix
      //if the data is a LabeledDataSetIfc then the column
      //values are sorted alphanumerically
      //
      if(barType.getAlphabetize())
      {
         rCommandsPreAdd("tmp <- BarHeightX(T)");
      }
      else
      {
         rCommandsPreAdd("tmp <- BarHeightX(F)");
      }
      rCommandsPreAdd("lx <- tmp[[2]]  #label matrix");
      rCommandsPreAdd("cp <- tmp[[3]] ");
      rCommandsPreAdd("hx <- tmp[[1]] ");

      //
      // color the bars
      //
      String[] colArray = Util.parseColors(barType.getColor());
      if (colArray.length == 0)
      {
         throw new AnalysisException("colArray.length = 0");
      }
      rCommandsPreAdd("cv <- " + Util.buildArrayCommand("c", colArray));

      //
      //lty for border
      //
      String[] borderLtyArray = barType.getBorderLty();
      String borderLty= (borderLtyArray == null)
                     ? Util.escapeQuote("FALSE")
                     : Util.buildArrayCommand("c", 
                                             Util.parseLineTypes(borderLtyArray));
      rCommandsPreAdd("b1 <- " + borderLty + " #lty vector for border");

      //
      //lwd for border
      //
      double[] borderLwdArray = barType.getBorderLwd();
      String borderLwd = (borderLwdArray == null)
                     ? "c(1)"
                     : Util.buildArrayCommand("c", 
                                             Util.convertToStringArray(borderLwdArray));
      rCommandsPreAdd("b2 <- " + borderLwd + " #lwd vector for border");

      //
      // color the bar borders
      //
      String[] colBorderArray = Util.parseColors(barType.getBorderColor());
      if (colBorderArray.length == 0)
      {
         throw new AnalysisException("colBorderArray.length = 0");
      }
      rCommandsPreAdd("cb <- " + Util.buildArrayCommand("c", colBorderArray));


      //
      // user selected widths of the bars
      //
      double[] widthArray = barType.getWidth();
      String width = (widthArray == null)
                     ? "c(1)"
                     : Util.buildArrayCommand("c", 
                                             Util.convertToStringArray(widthArray));
      rCommandsPreAdd("wv <- " + width);

      //
      //density of angled lines 
      //
      double[] densityArray = barType.getDensity();
      String density= (densityArray == null)
                     ? "NULL"
                     : Util.buildArrayCommand("c", 
                                             Util.convertToStringArray(densityArray));
      rCommandsPreAdd("dv <- " + density + " #angle line density vector");

      //
      //angle of fill lines 
      //
      double[] angleArray = barType.getAngle();
      String angle= (angleArray == null)
                     ? "NULL"
                     : Util.buildArrayCommand("c", 
                                             Util.convertToStringArray(angleArray));
      rCommandsPreAdd("av <- " + angle + " #angle line angle vector");

      //
      //lwd 
      //
      double[] lwdArray = barType.getLwd();
      String lwd= (lwdArray == null)
                     ? "c(1)"
                     : Util.buildArrayCommand("c", 
                                             Util.convertToStringArray(lwdArray));
      rCommandsPreAdd("lv <- " + lwd + " #lwd vector");

      //
      //lty 
      //
      String[] ltyArray = barType.getLty();
      String lty= (ltyArray == null)
                     ? "c(1)"
                     : Util.buildArrayCommand("c", 
                                             Util.parseLineTypes(ltyArray));
      rCommandsPreAdd("tv <- " + lty + " #lty vector");

      //
      //xlim 
      //
      double[] xlimArray = barType.getXlim();
      String xlim= (xlimArray == null)
                     ? "c(0,100)"
                     : Util.buildArrayCommand("c", 
                                             Util.convertToStringArray(xlimArray));
      rCommandsPreAdd("xl <- " + xlim + " #x limits % of plot x axis");

      //
      //Spacing
      //
      double sp1 = barType.getSpaceBetweenBars();
      double sp2 = barType.getSpaceBetweenCategories();
      rCommandsPreAdd("sv <- " + "c(" + sp1 + "," + sp2 + ")");


      //
      // orientation of the bars
      //
      String horiz = barType.getHorizontal()
                     ? "TRUE"
                     : "FALSE";
      rCommandsPreAdd("hz <- " + horiz);

      //
      // stacked bars or groups of bars?
      //
      String beside = barType.getStacked()
                      ? "FALSE"
                      : "TRUE";
      rCommandsPreAdd("bs <- " + beside + "#beside flag T=nonstacked F=stacked");

      //
      // allow (min,max) settings of y-axis
      //
      AxisNumeric yaxis = Option.getYAxis(p);
      Double[] yaxisRange = (Double[])yaxis.getAxisRange();
      if(yaxisRange != null)
      {
         double[] yRange = new double[]{yaxisRange[0].doubleValue(),yaxisRange[1].doubleValue()};
         rCommandsPreAdd("yl <- c(" + yRange[0] + "," + yRange[1] + ")");
      }
      else
      {
         rCommandsPreAdd("yl <- NULL");
      }

      //
      //do Categories Span the data sets?
      //
      if (barType.getCategoriesSpanDataSets())
      {
         rCommandsPreAdd("sd <- TRUE #Categories Span Data Sets flag");
         rCommandsPostAdd("d1Label <- rev(d1Label)");
      }
      else
      {
         rCommandsPreAdd("sd <- FALSE#Categories Span Data Sets flag");
      }

      //
      //do log scale?
      //
//091504      if (barType.getLog())
      AxisNumeric axisNumeric = Option.getYAxis(p);
      if (axisNumeric.getLogScale())
      {
         rCommandsPreAdd("lg <- TRUE #log scale flag");
      }
      else
      {
         rCommandsPreAdd("lg <- FALSE #log scale flag");
      }

      rCommandsPreAdd("pt <- FALSE #plot flag NOT YET IMPLEMENTED");
      rCommandsPreAdd("mx <- myBarPlot(xl,yl,cv,cb,cp,hx,wv,sv,hz,dv,av,tv,lv,pt,lg,bs,sd,b1,b2)");

}
else
{



      setName("barplot");
      setReturnVariable("mx");

      //
      // get the BarType options for this plot
      //
      BarType barType = Option.getBarType(p);

      //
      // allow (min,max) settings of y-axis
      //
      AxisNumeric yaxis = Option.getYAxis(p);
      Double[] yaxisRange = (Double[])yaxis.getAxisRange();
      String lim = "ylim";
      if(barType.getHorizontal())
      {
         lim = "xlim";
      }
      if(yaxisRange != null)
      {
         double[] yRange = new double[]{yaxisRange[0].doubleValue(),yaxisRange[1].doubleValue()};
         variableAdd(lim,Util.buildArrayCommand("c",yRange));
      }

      //
      // build a matrix of values describing the bars
      //
      String[] y = Rvariable.getName(p.getKeys(0));
      int numDataKeys = p.getKeys(0).length; // num of column vectors of data
      String[] dimString = new String[2]; // the dimension of data matrix
      dimString[0] = "NROW(" + y[0] + ")";
      dimString[1] = Integer.toString(numDataKeys);

      String height = "NULL";
      String[] cyclicColors = null;

      if (barType != null)
      {
         if (!barType.getCategoriesSpanDataSets())
         {

            Object[] ds = new Object[(p.getKeys(0)).length];

            for (int i = 0; i < ds.length; ++i)
            {
               ds[i] = p.getDataSet((p.getKeys(0))[i]);
            }

            if (ds[0] instanceof LabeledDataSetIfc)
            {
               rCommandsPreAdd("HeightMatrix <- BarHeight()");
            }
            else
            {
               height = "array(" + Util.buildArrayCommand("c", y) + ",dim="
                        + Util.buildArrayCommand("c", dimString) + ")";
               rCommandsPreAdd("HeightMatrix <- " + height);
            }

         }
         else
         {
            Object[] ds = new Object[(p.getKeys(0)).length];

            for (int i = 0; i < ds.length; ++i)
            {
               ds[i] = p.getDataSet((p.getKeys(0))[i]);
            }

            if (ds[0] instanceof LabeledDataSetIfc)
            {
               rCommandsPreAdd("HeightMatrix <- t(BarHeight())");
            }
            else
            {
               height = "t(array(" + Util.buildArrayCommand("c", y) + ",dim="
                     + Util.buildArrayCommand("c", dimString) + "))";
               rCommandsPreAdd("HeightMatrix <- " + height);
            }

         }
      }

      //
      // turn off annotation and axis generation
      //
      String ann = "FALSE";
      String axes = "FALSE";

      //
      // color the bars
      //
      String[] colArray = Util.parseColors(barType.getColor());
      if (colArray.length == 0)
      {
         throw new AnalysisException("colArray.length = 0");
      }
      rCommandsPreAdd("ColorArray <- " + Util.buildArrayCommand("c", colArray));
      String col = "ColorArray[1:min(length(ColorArray),dim(HeightMatrix)[1])]";

      //
      // color the bar borders
      //
//      String border = Util.parseColor(barType.getBorderColor());
//      rCommandsPreAdd("originalFG <- par(\"fg\")");
//      rCommandsPreAdd("par(\"fg\" = " + border + ")");

      //
      // user selected widths of the bars
      //
      double[] widthArray = barType.getWidth();
      String width = (widthArray == null)
                     ? "1"
                     : Util.buildArrayCommand("c", 
                                             Util.convertToStringArray(widthArray));

      //
      // user selected space
      //
      List keyList = p.getDataKeyList();
      Iterator iter = keyList.iterator();
      Object key = iter.next();
      DataSetIfc dataSeries = p.getDataSet(key);
      int numElements = 0;

      try
      {
         dataSeries.open();
         numElements = dataSeries.getNumElements();
         dataSeries.close();
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }

      double sp1 = barType.getSpaceBetweenBars();
      double sp2 = barType.getSpaceBetweenCategories();
      String space;

      if (numElements > 1)
      {
         space = "c(" + sp1 + "," + sp2 + ")";
      }
      else
      {
         space = "" + sp1;
      }

      //
      // orientation of the bars
      //
      String horiz = barType.getHorizontal()
                     ? "TRUE"
                     : "FALSE";

      //
      // stacked bars or groups of bars?
      //
      String beside = barType.getStacked()
                      ? "FALSE"
                      : "TRUE";


      variableAdd("height","HeightMatrix");
      variableAdd("ann",ann);
      variableAdd("axes",axes);
      variableAdd("col",col);
      variableAdd("horiz",horiz);
      variableAdd("width",width);
      variableAdd("space",space);
      variableAdd("beside",beside);

      generateAsPreCommand();

      variableAdd("plot","FALSE");

      //
      // restore the original foreground color
      //
      rCommandsPreAdd("par(\"fg\" = originalFG )");
}

   }
}
