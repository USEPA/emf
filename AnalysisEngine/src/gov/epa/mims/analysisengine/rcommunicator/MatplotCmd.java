package gov.epa.mims.analysisengine.rcommunicator;

import gov.epa.mims.analysisengine.AnalysisEngineConstants;
import gov.epa.mims.analysisengine.tree.Axis;
import gov.epa.mims.analysisengine.tree.AxisNumeric;
import gov.epa.mims.analysisengine.tree.AxisCategory;
import gov.epa.mims.analysisengine.tree.AxisTime;
import gov.epa.mims.analysisengine.tree.LineType;
import gov.epa.mims.analysisengine.tree.Plot;
import gov.epa.mims.analysisengine.tree.ScatterPlot;
import gov.epa.mims.analysisengine.tree.CdfPlot;
import gov.epa.mims.analysisengine.tree.DiscreteCategoryPlot;
import gov.epa.mims.analysisengine.tree.RankOrderPlot;
import gov.epa.mims.analysisengine.tree.TimeSeries;


/**
 * generates the matplot command
 *
 * @version $Revision: 1.2 $
 * @author Tommy E. Cathey
 ********************************************************/
public class MatplotCmd extends PlotCmd
{
   /** valid R variables for the X data sets */
   private String[] x = null;

   /** valid R variables for the Y data sets */
   private String[] y = null;

   /** min and Max range values for the X axis */
   private String[] xRange = null;

   /** min and Max range values for the Y axis */
   private String[] yRange = null;

   /** array of cyclic colors for the plotted data */
   private String[] col = null;

   /** array of cyclic line types */
   private String[] lty = null;

   /** array of cyclic line widths */
   private String[] lwd = null;

   /** array of cyclic symbols */
   private String[] pch = null;

   /** array of cyclic expansion coefficients */
   private String[] cex = null;

   /** type of plot to display */
   private String[] type = null;

   /** X axis Log scale flag */
   private boolean xLog = false;

   /** Y axis Log scale flag */
   private boolean yLog = false;

   /**
    * Creates a new MatplotCmd object.
    * <p>
    * calls:
    * <ul>
    * <li>
    * calls {@link Cmd#setName(String)} with setName("matplot")
    * to set the name of the function to be created.
    * <li>
    * calls {@link Cmd#setBannerOn(boolean)} with setBannerOn(true)
    * to turn on banner in the communication between Java and R
    * in order to enhance readability.
    * </ul>
    * @param p 
    ********************************************************/
   public MatplotCmd(CdfPlot p)
   {
      super(p);
      setName("matplot");
      setBannerOn(true);
      processLineTypeInfo(p);
      //processAxisInfo(p);
      processXaxis( Option.getXAxis(p) );
      processYaxis( Option.getYAxis(p) );
   }
   public MatplotCmd(ScatterPlot p)
   {
      super(p);
      setName("matplot");
      setBannerOn(true);
      processLineTypeInfo(p);
      //processAxisInfo(p);
      processXaxis( Option.getXAxis(p) );
      processYaxis( Option.getYAxis(p) );
   }
   public MatplotCmd(DiscreteCategoryPlot p)
   {
      super(p);
      setName("matplot");
      setBannerOn(true);
      processLineTypeInfo(p);
      //processAxisInfo(p);
      processXaxis( Option.getXAxis(p) );
      processYaxis( Option.getYAxis(p) );
   }
   public MatplotCmd(RankOrderPlot p)
   {
      super(p);
      setName("matplot");
      setBannerOn(true);
      processLineTypeInfo(p);
      //processAxisInfo(p);
      processXaxis( Option.getXAxis(p) );
      processYaxis( Option.getYAxis(p) );
   }
   public MatplotCmd(TimeSeries p)
   {
      super(p);
      setName("matplot");
      setBannerOn(true);
      processLineTypeInfo(p);
      //processAxisInfo(p);
      processXaxis( Option.getXAxis(p) );
      processYaxis( Option.getYAxis(p) );
   }

   /**
    * return the list of R commands we have generated. This
    * method overrides {@link Cmd#getCommands()} in order to
    * make sure the matplot sizing command preceeds the matplot
    * commands for displaying the data.
    * <p>
    * calls:
    * <ul>
    * <li>
    * {@link #sizingCommand()} to generate a matplot command for
    * sizing the user space of the plot
    * <li>
    * {@link #drawingCommands()} to generate the matplot commands
    * for plotting the data sets
    * <li>
    * {@link Cmd#clearAllVariables()} to clear the super class's
    * {@link Cmd#keys} data structure. We do this because all commands
    * are generated as "pre-commands" and we don't want the 
    * {@link Cmd#getCommands()} call generating unwanted commands
    * from elements left in {@link Cmd#keys}.
    * </ul>
    * @return list of R matplot commands 
    ********************************************************/
   public java.util.List getCommands()
   {
      sizingCommand();
      drawingCommands();
      super.clearAllVariables();

      return super.getCommands();
   }

   /**
    * generate a matplot command to size the plot prior to plotting data
    * <p>
    * <ul>
    * <li>make the following calls to {@link Cmd#variableAdd(String,String)}
    *   <ul>
    *   <li>variableAdd("add", "FALSE") - we are not adding to an existing
    *       plot.
    *   <li>variableAdd("ann", "FALSE") - do not annotate the plot
    *   <li>variableAdd("axes", "FALSE") - do not draw axes
    *   <li>variableAdd("type", Util.escapeQuote("n")) - do not draw the plot
    *   <li>call {@link #sizingCommand2} to set the data ranges
    *   <li>call {@link #sizingCommand3} to set the Log Scale flag
    *   </ul>
    * <li>return super.generateAsPreCommand()
    * </ul>
    * 
    * 
    ********************************************************/
   private void sizingCommand()
   {
      variableAdd("add", "FALSE"); //no add 2 existing plot
      variableAdd("ann", "FALSE"); //no annotation
      variableAdd("axes", "FALSE"); //do not draw axes
      variableAdd("type", Util.escapeQuote("n")); //do not draw plot


      //call sizingCommand2() to determine data ranges
      sizingCommand2();


      //call sizingCommand3() to determine Log Scales
      sizingCommand3();


      //generate matplot sizing command as a pre-command
      super.generateAsPreCommand();
   }

   /**
    * determines the x & y data ranges for the initial sizing
    * matplot command
    * <p>
    * tell R to:
    * <ul>
    * <li>write all x data values to "x1"
    * <li>write all y data values to "y1"
    * </ul>
    * the data ranges will be
    * <ul>
    * <li>"c(min(sort(x1)),max(sort(x1)))"
    * <li>"c(min(sort(y1)),max(sort(y1)))"
    * </ul>
    * if the user has specified a data range for an axis then
    * {@link #xRange} and/or {@link #yRange} will be set and
    * their values used to override the default range values
    *
    ********************************************************/
   private void sizingCommand2()
   {
      //x & y contain the R variables from the data to be plotted
      if( x == null)
      {
         rCommandsPreAdd("x1 <- c(0,length(d1) + 1)");
      }
      else
      {
         rCommandsPreAdd(Util.buildArrayCommand("x1 <- c", x));
      }
      rCommandsPreAdd(Util.buildArrayCommand("y1 <- c", y));

      if ((xRange == null) && (yRange == null))
      {
         //no Range values are set
         variableAdd("x", "c(min(sort(x1)),max(sort(x1)))");
         variableAdd("y", "c(min(sort(y1)),max(sort(y1)))");
      }
      else if ((xRange != null) && (yRange != null))
      {
         //both Range values are set
         variableAdd("x", Util.buildArrayCommand("c", xRange));
         variableAdd("y", Util.buildArrayCommand("c", yRange));
      }
      else if ((xRange != null) && (yRange == null))
      {
         //xRange values are set but not yRange
         variableAdd("x", Util.buildArrayCommand("c", xRange));
         variableAdd("y", "c(min(sort(y1)),max(sort(y1)))");
      }
      else if ((xRange == null) && (yRange != null))
      {
         //yRange values are set but not xRange
         variableAdd("x", "c(min(sort(x1)),max(sort(x1)))");
         variableAdd("y", Util.buildArrayCommand("c", yRange));
      }
      else
      {
         //this should not happen
         String msg = getClass().getName() + " unknown Range option";
         throw new IllegalArgumentException(msg);
      }
   }

   /**
    * determines the Log scale flag for the initial sizing
    * matplot command
    * <p>
    * the "log" argument of matplot is set to:
    * <ul>
    * <li>"xy" if {@link #xLog} and {@link #yLog} are both true
    * <li>"x" if {@link #xLog} is true and {@link #yLog} is false
    * <li>"y" if {@link #xLog} is false and {@link #yLog} is true
    * </ul>
    * 
    ********************************************************/
   private void sizingCommand3()
   {
      if (xLog && yLog)
      {
         variableAdd("log", Util.escapeQuote("xy"));
      }
      else if (xLog)
      {
         variableAdd("log", Util.escapeQuote("x"));
      }
      else if (yLog)
      {
         variableAdd("log", Util.escapeQuote("y"));
      }
   }

   /**
    * loop over the data sets generating a matplot command for
    * each one. At the end of the loop each matplot command is
    * added to {@link Cmd#rCommandsPre} List by calling
    * {@link Cmd#generateAsPreCommand}.
    * <p>
    * The following arguments are set:
    * <ul>
    * <li>add = "TRUE" (add to the existing plot)
    * <li>ann = "FALSE" (do not annotate)
    * <li>axes = "FALSE" (do not draw axes)
    * <li>type = {@link #type} (set by calling {@link #setType(String)})
    * <li>x = cyclic value in {@link #x} (x data)
    * <li>y = cyclic value in {@link #y} (y data)
    * <li>col = cyclic value in {@link #col} (plot color)
    * <li>lty = cyclic value in {@link #lty} (line type)
    * <li>lwd = cyclic value in {@link #lwd} (line width)
    * <li>pch = cyclic value in {@link #pch} (symbol)
    * <li>cex = cyclic value in {@link #cex} (expansion coefficient)
    * </ul>
    *
    ********************************************************/
   private void drawingCommands()
   {
      for (int i = 0; i < y.length; ++i)
      {
         variableAdd("add", "TRUE"); //add the existing plot
         variableAdd("ann", "FALSE"); //do not annotate
         variableAdd("axes", "FALSE"); //do not draw axes
         variableAdd("type", type[i % type.length]);
         if(x == null)
            variableAdd("x", "1:length(" + y[i % y.length] + ")" );
         else
            variableAdd("x", x[i % x.length]);
         variableAdd("y", y[i % y.length]);
         variableAdd("col", col[i % col.length]);
         variableAdd("lty", lty[i % lty.length]);
         variableAdd("lwd", lwd[i % lwd.length]);
         variableAdd("pch", pch[i % pch.length]);
         variableAdd("cex", cex[i % cex.length]);

         super.generateAsPreCommand();
      }
   }

   /**
    * Set the R variable names for the x data)
    *
    * @param arg String[] of valid R variable names containg x data
    ********************************************************/
   public void setX(java.lang.String[] arg)
   {
      this.x = arg;
   }

   /**
    * Set the R variable names for the y data)
    *
    * @param arg String[] of valid R variable names containg y data
    ********************************************************/
   public void setY(java.lang.String[] arg)
   {
      this.y = arg;
   }

   /**
    * Set the cyclic plotting colors in R's format.
    *
    * @param arg String[] of cyclic plotting colors in R's format
    ********************************************************/
   public void setCol(java.lang.String[] arg)
   {
      this.col = arg;
   }

   /**
    * Set the cyclic plotting colors. The Java color formats
    * are converted into R's format.
    *
    * @param arg Color[] of cyclic plotting colors
    ********************************************************/
   public void setCol(java.awt.Color[] arg)
   {
      this.col = Util.parseColors(arg);
   }

   /**
    * Set the cyclic plotting color with a single color. 
    * The Java color format is converted into R's format.
    *
    * @param arg Color for plotting
    ********************************************************/
   public void setCol(java.awt.Color arg)
   {
      this.col = new String[1];
      this.col[0] = Util.parseColor(arg);
   }

   /**
    * set the cyclic line type for plotting the data
    *
    * @param arg String[] cyclic line types
    ********************************************************/
   public void setLty(java.lang.String[] arg)
   {
      this.lty = Util.parseLineTypes(arg);
   }

   /**
    * set the cyclic line width for plotting the data
    *
    * @param arg String[] cyclic line width
    ********************************************************/
   public void setLwd(java.lang.String[] arg)
   {
      this.lwd = arg;
   }

   /**
    * set the cyclic line width to a single value with a double.
    *
    * @param arg double to use as the cyclic line width
    ********************************************************/
   public void setLwd(double arg)
   {
      this.lwd = new String[1];
      this.lwd[0] = Double.toString(arg);
   }

   /**
    * set the cyclic line widths with an double[].
    *
    * @param arg double[] to use as the cyclic line widths
    ********************************************************/
   public void setLwd(double[] arg)
   {
      this.lwd = new String[arg.length];

      for (int i = 0; i < this.lwd.length; i++)
      {
         this.lwd[i] = Double.toString(arg[i]);
      }
   }

   /**
    * set the cyclic symbols for plotting. 
    *
    * @param arg String[] of cyclic symbols
    ********************************************************/
   public void setPch(java.lang.String[] arg)
   {
      this.pch = Util.parseSymbols(arg);
   }

   /**
    * set the cyclic expansion coefficients
    *
    * @param arg String[] of cyclic expansion coefficients
    ********************************************************/
   public void setCex(java.lang.String[] arg)
   {
      this.cex = arg;
   }

   /**
    * set the cyclic expansion coefficient to a single double value
    *
    * @param arg single cyclic expansion coefficient as a double
    ********************************************************/
   public void setCex(double arg)
   {
      this.cex = new String[1];
      this.cex[0] = Double.toString(arg);
   }

   /**
    * set the cyclic expansion coefficients as a double[]
    *
    * @param arg double[] of cyclic expansion coefficients
    ********************************************************/
   public void setCex(double[] arg)
   {
      this.cex = new String[arg.length];

      for (int i = 0; i < this.cex.length; i++)
      {
         this.cex[i] = Double.toString(arg[i]);
      }
   }

   /**
    * set the range of the X axis
    *
    * @param arg range of the X axis
    ********************************************************/
   public void setXRange(String[] arg)
   {
      this.xRange = arg;
   }

   /**
    * set the range of the Y axis
    *
    * @param arg range of the Y axis
    ********************************************************/
   public void setYRange(String[] arg)
   {
      this.yRange = arg;
   }

   /**
    * set the range of the X axis
    *
    * @param xMin min value for the X axis
    * @param xMax max value for the X axis
    ********************************************************/
   public void setXRange(double xMin, double xMax)
   {
      if ((Double.isNaN(xMin)) && (Double.isNaN(xMax)))
      {
         this.xRange = null;
      }
      else
      {
         this.xRange = new String[2];
         this.xRange[0] = Double.toString(xMin);
         this.xRange[1] = Double.toString(xMax);
      }
   }

   /**
    * set the range of the Y axis
    *
    * @param yMin min value for the Y axis
    * @param yMax max value for the Y axis
    ********************************************************/
   public void setYRange(double yMin, double yMax)
   {
      if ((Double.isNaN(yMin)) && (Double.isNaN(yMax)))
      {
         this.yRange = null;
      }
      else
      {
         this.yRange = new String[2];
         this.yRange[0] = Double.toString(yMin);
         this.yRange[1] = Double.toString(yMax);
      }
   }

   /**
    * set the x log scale flag
    *
    * @param arg true=Log Scale; false=Linear Scale
    ********************************************************/
   public void setXLog(boolean arg)
   {
      this.xLog = arg;
   }

   /**
    * set the y log scale flag
    *
    * @param arg true=Log Scale; false=Linear Scale
    ********************************************************/
   public void setYLog(boolean arg)
   {
      this.yLog = arg;
   }

   /**
    * set the Type of plot to generate
    *
    * @param arg type of plot to generate
    ********************************************************/
   protected void setType(java.lang.String[] arg)
   {
      this.type = new String[arg.length];
      for(int i=0;i<arg.length;i++)
      {
         this.type[i] = Util.escapeQuote(OptMap.getTypeOfPlot(arg[i]));
      }
   }

   /**
    * process Line Type Infomation
    * <p>retrieves the {@link LineType} Analysis Option and
    * then passes:
    * <ul>
    * <li>{@link LineType#getColor()} to
    *     method {@link MatplotCmd#setCol(java.awt.Color[])}
    * <li>{@link LineType#getSymbolExpansion()} to
    *     method {@link MatplotCmd#setCex(double[])}
    * <li>{@link LineType#getSymbol()} to
    *     method {@link MatplotCmd#setPch(String[])}
    * <li>{@link LineType#getLineWidth()} to
    *     method {@link MatplotCmd#setLwd(String[])}
    * <li>{@link LineType#getLineStyle()} to
    *     method {@link MatplotCmd#setLty(String[])}
    * <li>{@link LineType#getPlotStyle()} to
    *     method {@link MatplotCmd#setType(String)}
    * </ul>
    * @param p the current Plot being plotted
    ********************************************************/
   private void processLineTypeInfo(Plot p)
   {
      LineType lt = Option.getLineType(p);

      this.setCol(lt.getColor());
      this.setCex(lt.getSymbolExpansion());
      this.setPch(lt.getSymbol());
      this.setLwd(lt.getLineWidth());
      this.setLty(lt.getLineStyle());

      if(lt.getPlotStyles() != null)
      {
         this.setType(lt.getPlotStyles());
      }
      else if(lt.getPlotStyle() != null)
      {
         this.setType(new String[]{lt.getPlotStyle()});
      }
      else
      {
         throw new IllegalArgumentException("LineType.getPlotStyle()=" +
          lt.getPlotStyle());
      }
   }

//   /**
//    * process Axis Infomation
//    * <p>for the X & Y axis retrieve its AnalysisOption and
//    * then call processXaxis( Axis axis ) and  
//    * processYaxis( Axis axis ) respectively
//    * @param p the current Plot being plotted
//    ********************************************************/
//   private void processAxisInfo(Plot p)
//   {
//      Object obj;
//      obj = p.getOption(XAXIS);
//
//      if(obj == null)
//      {
//         throw new IllegalArgumentException(getClass().getName() 
//          + " obj == null");
//      }
//      if(obj instanceof AxisNumeric)
//      {
//         processXaxis((AxisNumeric)obj);
//      }
//      else if(obj instanceof AxisCategory)
//      {
//         processXaxis((AxisCategory)obj);
//      }
//      else if(obj instanceof AxisTime)
//      {
//         processXaxis((AxisTime)obj);
//      }
//      else
//      {
//         throw new IllegalArgumentException(getClass().getName() 
//          + " unknown Axis" + obj.toString());
//      }
//
//      obj = p.getOption(YAXIS);
//
//      if(obj instanceof AxisNumeric)
//      {
//         processYaxis((AxisNumeric)obj);
//      }
//      else if(obj instanceof AxisCategory)
//      {
//         processYaxis((AxisCategory)obj);
//      }
//      else
//      {
//         throw new IllegalArgumentException(getClass().getName() + " unknown Axis");
//      }
//   }

   /**
    * process X Axis Infomation
    *
    * @param axis X Axis
    ********************************************************/
   private void processXaxis(AxisNumeric axis)
   {
      //check for Log axis
      if(axis.getLogScale())
      {
         this.setXLog(true);
      }


      //set the window range values
      Double[] range = (Double[])axis.getAxisRange();
      if(range == null)
      {
         this.setXRange(Double.NaN,Double.NaN);
      }
      else
      {
         this.setXRange(range[0].doubleValue(), range[1].doubleValue());
      }
   }

   /**
    * process Y Axis Infomation
    * <p>This method:
    * <ul>
    * <li>calls method {@link MatplotCmd#setYLog(boolean)}
    *  with "true"
    * if axis is instance of {@link AxisLog}
    * <li>passes window range values {@link Axis#getWinMin()}
    * and {@link Axis#getWinMax()} to 
    * {@link MatplotCmd#setYRange(double,double)}
    * </ul>
    *
    * @param axis Y Axis
    ********************************************************/
   private void processYaxis(AxisNumeric axis)
   {
      //check for Log axis
      if(axis.getLogScale())
      {
         this.setYLog(true);
      }

      //set the window range values
      Double[] range = (Double[])axis.getAxisRange();
      if(range == null)
      {
         this.setYRange(Double.NaN,Double.NaN);
      }
      else
      {
         this.setYRange(range[0].doubleValue(), range[1].doubleValue());
      }
   }

   /**
    * process X Axis Infomation
    *
    * @param axis X Axis
    ********************************************************/
   private void processXaxis(AxisCategory axis)
   {
      //set the window range values
      Double[] range = (Double[])axis.getAxisRange();
      if(range == null)
      {
         this.setXRange(Double.NaN,Double.NaN);
      }
      else
      {
         this.setXRange(range[0].doubleValue(), range[1].doubleValue());
      }
   }

   /**
    * process X Axis Infomation
    *
    * @param axis X Axis
    ********************************************************/
   private void processXaxis(AxisTime axis)
   {
      this.setXRange(Double.NaN,Double.NaN);
   }

   /**
    * process Y Axis Infomation
    *
    * @param axis Y Axis
    ********************************************************/
   private void processYaxis(AxisTime axis)
   {
      this.setYRange(Double.NaN,Double.NaN);
   }

   /**
    * process Y Axis Infomation
    *
    * @param axis Y Axis
    ********************************************************/
   private void processYaxis(AxisCategory axis)
   {

      //set the window range values
      Double[] range = (Double[])axis.getAxisRange();
      if(range == null)
      {
         this.setYRange(Double.NaN,Double.NaN);
      }
      else
      {
         this.setYRange(range[0].doubleValue(), range[1].doubleValue());
      }
   }
}
