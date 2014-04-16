package gov.epa.mims.analysisengine.rcommunicator;
import java.awt.Color;
import java.util.List;

import gov.epa.mims.analysisengine.AnalysisEngineConstants;
import gov.epa.mims.analysisengine.tree.Axis;
import gov.epa.mims.analysisengine.tree.LineType;
import gov.epa.mims.analysisengine.tree.CdfPlot;
import gov.epa.mims.analysisengine.tree.Text;
import gov.epa.mims.analysisengine.tree.LinearRegression;


/**
 * generate a CdfPlotCmd R command
 * <br>
 * This class extends MatplotCmd and is responsible for setting
 * parameters particular to generating a cdf Plot
 *
 * @version $Revision: 1.2 $
 * @author Tommy E. Cathey
 ********************************************************/
public class CdfPlotCmd extends MatplotCmd
{
   private Cmd diagnosticPlotCmds = new Cmd();

   /**
    * Creates a new CdfPlotCmd object.
    * <p>calls:
    * <ul>
    * <li>super constructor {@link MatplotCmd}
    * <li>{@link CdfPlotCmd#processDataInfo(CdfPlot)}
    * <li>{@link CdfPlotCmd#processLineTypeInfo(CdfPlot)}
    * <li>{@link CdfPlotCmd#processAxisInfo(CdfPlot)}
    * </ul>
    * @param p a CdfPlot 
    ********************************************************/
   public CdfPlotCmd(CdfPlot p)
   {
      super(p);
      processDataInfo(p);
//      regressionLines(p);
   }

//   private void regressionLines(CdfPlot p)
//   {
//      LineType lt = Option.getLineType(p);
//      if(lt != null)
//      {
//         int count = lt.getLinearRegressionSize();
//         for(int i=0;i<count;i++)
//         {
//            LinearRegression lr = lt.getLinearRegression(i);
////            linearRegression(lr);
//            LinearRegressionCmd lrCmd = new LinearRegressionCmd(lr,"regression"+i);
//            rCommandsPostAdd(lrCmd.getCommands());
//            diagnosticPlotCmds.rCommandsPreAdd(lrCmd.getCommands2());
//            
//         }
//      }
//   }

   /**
    * process the data information
    * <p>calls {@link CdfPlot#getKeys(int)} with (p.getKeys(0))[0]
    * to get the X data key. This X data key is then passed to 
    * {@link Rvariable#getName(Object)} which returns a valid
    * R variable name for the X data key.
    *
    * <p>calls {@link CdfPlot#getKeys(int)} with p.getKeys(1)
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
    * @param p the current CdfPlot being plotted
    ********************************************************/
   private void processDataInfo(CdfPlot p)
   {
      String[] x = Rvariable.getName(p.getKeys(0));
      String[] y = new String[x.length];

      //tell R to create an Y data set for each X data set
      for (int i = 0; i < x.length; i++)
      {
         x[i] = "sort(" + x[i] + ")";
         y[i] = "1:length(" + x[i] + ")/length(" + x[i] + ")";
      }
      super.setX(x);
      super.setY(y);


//      for (int i = 0; i < x.length; i++)
//      {
//         System.out.println(x[i]);
//      }
//System.exit(1);

//      String yRvariableName = Rvariable.getName((p.getKeys(0))[0]);
//      String[] y = Rvariable.getName(p.getKeys(y));
      //String[] x = new String[y.length];

      //there is only one X data set but it must be matched up
      //with each Y Data set
      //
//      for (int i = 0; i < x.length; i++)
//      {
//         x[i] = xRvariableName;
//      }

//      super.setX(x);
//      super.setY(y);
   }
}
