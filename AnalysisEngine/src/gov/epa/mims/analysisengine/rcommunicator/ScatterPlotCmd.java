package gov.epa.mims.analysisengine.rcommunicator;
import java.awt.Color;
import java.util.List;

import gov.epa.mims.analysisengine.AnalysisEngineConstants;
import gov.epa.mims.analysisengine.tree.Axis;
import gov.epa.mims.analysisengine.tree.LineType;
import gov.epa.mims.analysisengine.tree.ScatterPlot;
import gov.epa.mims.analysisengine.tree.Text;
import gov.epa.mims.analysisengine.tree.LinearRegressionType;
import gov.epa.mims.analysisengine.tree.LinearRegression;


/**
 * generate a ScatterPlotCmd R command
 * <br>
 * This class extends MatplotCmd and is responsible for setting
 * parameters particular to generating a Scatter Plot
 *
 * @version $Revision: 1.2 $
 * @author Tommy E. Cathey
 ********************************************************/
public class ScatterPlotCmd extends MatplotCmd
{
   private Cmd diagnosticPlotCmds = new Cmd();

   /**
    * Creates a new ScatterPlotCmd object.
    * <p>calls:
    * <ul>
    * <li>super constructor {@link MatplotCmd}
    * <li>{@link ScatterPlotCmd#processDataInfo(ScatterPlot)}
    * <li>{@link ScatterPlotCmd#processLineTypeInfo(ScatterPlot)}
    * <li>{@link ScatterPlotCmd#processAxisInfo(ScatterPlot)}
    * </ul>
    * @param p a ScatterPlot 
    ********************************************************/
   public ScatterPlotCmd(ScatterPlot p)
   {
      super(p);
      processDataInfo(p);
      regressionLines(p);
   }

   private void regressionLines(ScatterPlot p)
   {
      LinearRegressionType lrt = Option.getLinearRegressionType(p);
      if(lrt != null)
      {
         if( lrt.getEnable() )
         {
            if( p.getKeys(0).length == 1 )
            {
               String xkey = p.getKeys(0)[0];
               int count = lrt.getLinearRegressionSize();
               for(int i=0;i<count;i++)
               {
                  if( i < p.getKeys(1).length )
                  {
                     String ykey = p.getKeys(1)[i];
   
                     LinearRegression lr = lrt.getLinearRegression(i);
                     LinearRegressionCmd lrCmd = new LinearRegressionCmd(lr,xkey,ykey,"regression"+i);
                     rCommandsPostAdd(lrCmd.getCommands());
                     diagnosticPlotCmds.rCommandsPreAdd(lrCmd.getCommands2());
                  }
                  else
                  {
                     StringBuffer b = new StringBuffer();
                     b.append("\nore LinearRegressions than Y Data sets.");
                     b.append("\np.getKeys(1).length=" + p.getKeys(1).length);
                     b.append("\ngetLinearRegressionSize()=" + count);
                     throw new IllegalArgumentException( b.toString() );
                  }
               }
            }
         }
      }
   }


   public List getCommands2()
   {
      return diagnosticPlotCmds.getCommands();
   }

//
//   private void linearRegression(LinearRegression lr)
//   {
//      if(lr == null)
//      {
//         throw new IllegalArgumentException(getClass().getName()+" lr=" +lr);
//      }
//      Color c = lr.getLinecolor();
//      String l = lr.getLinestyle();
//      boolean e = lr.getEnable();
//      String x = lr.getXDataSetKey();
//      String y = lr.getYDataSetKey();
//      double w = lr.getLinewidth();
//      Text t = lr.getLabel();
//
//      rCommandsPostAdd("par(fg=" +Util.parseColor(c) +")");
//      rCommandsPostAdd("par(lty=" +Util.parseLineTypes(l) +")");
//      rCommandsPostAdd("par(lwd=" + w +")");
//      String xR = Rvariable.getName(x);
//      String yR = Rvariable.getName(y);
//      rCommandsPostAdd("regression <- lm( " + yR + " ~ " + xR + ")");
//      rCommandsPostAdd("abline(regression)");
//      if(t != null)
//      {
//         linearRegressionLabel(t);
//      }
//   }
//
//   private void linearRegressionLabel(Text t)
//   {
//   }
//
   /**
    * process the data information
    * <p>calls {@link ScatterPlot#getKeys(int)} with (p.getKeys(0))[0]
    * to get the X data key. This X data key is then passed to 
    * {@link Rvariable#getName(Object)} which returns a valid
    * R variable name for the X data key.
    *
    * <p>calls {@link ScatterPlot#getKeys(int)} with p.getKeys(1)
    * to get the Y data keys. The Y data keys are passed to
    * {@link Rvariable#getName(Object[])} which returns a String[]
    * of valid R variable names for the Y data sets.
    *
    * <p>matplot expects the same number of X variables as Y variables
    * but we only have one X data variable name so a String[]
    * the same length as the String array holding the valid R variable names
    * for the Y data sets is created and each element of this array
    * is set to the valid R variable name for the X data.
    *
    * <p>the two String[] arrays containing the valid R variable names for
    * the X & Y data sets are passed to the super methods
    * {@link MatplotCmd#setX(String[])} and {@link MatplotCmd#setY(String[])}
    * respectively.
    * @param p the current ScatterPlot being plotted
    ********************************************************/
   private void processDataInfo(ScatterPlot p)
   {
      String[] y = null;
      String[] x = null;

      if( p.getKeys(0).length == 1 )
      {
         String xRvariableName = Rvariable.getName((p.getKeys(0))[0]);
         y = Rvariable.getName(p.getKeys(1));
         x = new String[y.length];
   
         //there is only one X data set but it must be matched up
         //with each Y Data set
         //
         for (int i = 0; i < x.length; i++)
         {
            x[i] = xRvariableName;
         }
      }
      else if( p.getKeys(0).length == 0 )
      {
         //there is no X data set; so we plot against the index value
         y = Rvariable.getName(p.getKeys(1));
      }
      else
      {
         throw new IllegalArgumentException(getClass().getName()+" p.getKeys(0).length=" + p.getKeys(0).length);
      }

      super.setX(x);
      super.setY(y);
   }
}
