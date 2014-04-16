package gov.epa.mims.analysisengine.rcommunicator;

import gov.epa.mims.analysisengine.AnalysisException;
import gov.epa.mims.analysisengine.tree.BoxPlot;
import gov.epa.mims.analysisengine.tree.BoxType;
import gov.epa.mims.analysisengine.tree.DataSetIfc;
import gov.epa.mims.analysisengine.tree.AxisNumeric;
import gov.epa.mims.analysisengine.tree.LabeledDataSetIfc;
import gov.epa.mims.analysisengine.stats.Percentile;


import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.Set;


/**
 * generate a bar plot R command
 *
 * @version $Revision: 1.2 $
 * @author Tommy E. Cathey
 ********************************************************/
public class BoxPlotCmd extends PlotCmd
{

   /**
    * Creates a new BoxPlotCmd object.
    * @param p a BoxPlot 
    ********************************************************/
   public BoxPlotCmd(BoxPlot p)
   {
      super(p);

      setName("bxp");
      setReturnVariable("atX");

      //
      // get the BoxType options for this plot
      //
      BoxType boxType = Option.getBoxType(p);
//System.out.println("BoxType= " + boxType);

      Object[] ds = new Object[(p.getKeys(0)).length];

      for (int i = 0; i < ds.length; ++i)
      {
         ds[i] = p.getDataSet((p.getKeys(0))[i]);
      }

      rCommandsPreAdd("range <- " + boxType.getRange()  );
      variableAdd("range","range");

      //
      //BoxType.USE_R
      //BoxType.PRECOMPUTED
      //BoxType.CUSTOM
      //
      int processing = boxType.getProcessing();

      if (processing == BoxType.PRECOMPUTED)
      {
         if(!(ds[0] instanceof LabeledDataSetIfc))
         {
            StringBuffer b = new StringBuffer();
            b.append("BoxType.setProcessing(BoxType.PRECOMPUTED) ");
            b.append("is only allowed when using a LabeledDataSetIfc");
            throw new IllegalArgumentException(b.toString());
         }
         doPrecomputedProcessing(ds);
      }
      else if (processing == BoxType.USE_R)
      {
         doDefaultProcessing(ds.length);
      }
      else if (processing == BoxType.CUSTOM)
      {
         doCustomProcessing(ds,boxType.getCustomPercentiles());
      }
      else
      {
         String s = "Unknown processing type returned from ";
         s += "BoxType.getProcessing()="+processing;
         throw new IllegalArgumentException(s);
      }

      rCommandsPreAdd("at <- 1:dim(bx$stats)[2]");
      if( boxType.getAt() != null )
      {
         rCommandsPreAdd("at <- " + Util.buildArrayCommand("c",boxType.getAt()));
      }
      if(boxType.getReversePlotOrder())
      {
         rCommandsPreAdd("at <- rev(at)");
      }
      variableAdd("at","at");
/*
public   java.awt.Color getBorderColor();
public   [Ljava.awt.Color; getColor();
public   boolean getHorizontal();
public   [D getWidth();
public   [Z getNotch();
public   [Z getVarwidths();
public   boolean getOutliers();
public   [D getBoxwex();
public   double getNotchFrac();
public   double getLwd();
public   [D getAt();
notch if notch is TRUE, a notch is drawn in each side of the boxes. If the notches of two plots do not overlap then the medians are significantly different at the 5 percent level.

width a vector giving the relative widths of the boxes making up the plot.

varwidth if varwidth is TRUE, the boxes are drawn with widths proportional to the square-roots of the number of observations in the groups. 

outline if outline is not true, the boxplot lines are not drawn.

boxwex a scale factor to be applied to all boxes. When there are only a few groups, the appearance of the plot can be improved by making the boxes narrower. 

notch.frac numeric in (0,1). When notch=TRUE, the fraction of the box width that the notches should use. 

border character, the color of the box borders. Is recycled for multiple boxes. 

col character; the color within the box. Is recycled for multiple boxes 

log character, indicating if any axis should be drawn in logarithmic scale, as in plot.default. 

frame.plot logical, indicating if a ``frame'' (box) should be drawn; defaults to TRUE, unless axes = FALSE is specified. 

horizontal logical indicating if the boxplots should be horizontal; default FALSE means vertical boxes. 

add logical, if true add boxplot to current plot. 

at numeric vector giving the locations where the boxplots should be drawn, particularly when add = TRUE; defaults to 1:n where n is the number of boxes. 

show.names Set to TRUE or FALSE to override the defaults on whether an x-axis label is printed for each group. 
*/


      rCommandsPreAdd("par(lwd= " + boxType.getLwd() + ")" );
      rCommandsPreAdd("par(xaxt= \"n\")");
      rCommandsPreAdd("par(yaxt= \"n\")");

//      if(check the axis for log)
//      {
//         variableAdd("log","y");
//      }

      AxisNumeric yaxis = Option.getYAxis(p);
      if(yaxis.getLogScale())
      {
         variableAdd("log","y");
      }
      variableAdd("frame.plot","FALSE");
      variableAdd("show.names","FALSE");
      variableAdd("outline","FALSE");
      if(boxType.getOutliers())
      {
         variableAdd("outline","TRUE");
      }
      variableAdd("horizontal","FALSE");
      if(boxType.getHorizontal())
      {
         variableAdd("horizontal","TRUE");
      }
      if( boxType.getColor() != null )
      {
         String col = Util.buildArrayCommand("c",Util.parseColors(boxType.getColor()));
         rCommandsPreAdd("bxpCol <- " + col);
         variableAdd("col","bxpCol");
      }
      variableAdd("border",Util.parseColor(boxType.getBorderColor()));
      variableAdd("notch.frac","" + boxType.getNotchFrac());
      if( boxType.getBoxwex() != null )
      {
         variableAdd("boxwex",Util.buildArrayCommand("c",boxType.getBoxwex()));
      }
      if( boxType.getWidth() != null )
      {
         variableAdd("width",Util.buildArrayCommand("c",boxType.getWidth()));
      }
      variableAdd("varwidth",Util.parseBoolean(boxType.getVarwidths()));
      variableAdd("notch",Util.parseBoolean(boxType.getNotch()));
      variableAdd("z","bx");
   }

   private void doDefaultProcessing(int dataSetArrayLength)
   {
         StringBuffer b = new StringBuffer();
         b.append("bxList <- list(");
         for(int i = 1; i <= dataSetArrayLength; ++i)
         {
            b.append("d"+i+"=d"+i);
            if(i < dataSetArrayLength)
               b.append(",");
         }
         b.append(");");
         rCommandsPreAdd(b.toString());
         rCommandsPreAdd("bx <- boxplot(bxList,plot=FALSE)");
   }
   private void doCustomProcessing(Object[] dsIN,HashMap h)
   {
      String[] labels = new String[]{BoxType.LOWER_WHISKER, BoxType.LOWER_HINGE,
         BoxType.LOWER_NOTCH_EXTREME, BoxType.MEDIAN, 
         BoxType.UPPER_NOTCH_EXTREME, BoxType.UPPER_HINGE,
         BoxType.UPPER_WHISKER};

/*
*Set keys = h.keySet();
*Iterator keyIter = keys.iterator();
*while (keyIter.hasNext())
*{
*   String key = (String) keyIter.next();
*   System.out.println("key= "+key);
*}
*/

      //
      //determine the keys from labels[] which are actually used
      //
      ArrayList keysPresent = new ArrayList();
      for(int i=0;i<labels.length;i++)
      {
         if(h.containsKey(labels[i]))
         {
            keysPresent.add(labels[i]);
         }
      }

      //
      //create double[] to pass to Percentile.generate(DataSetIfc,double[])
      //
      double[] percentiles = new double[keysPresent.size()];
      for(int i=0;i<keysPresent.size();i++)
      {
         percentiles[i] = ((Double)h.get(keysPresent.get(i))).doubleValue();
//         percentiles[i] = ((Double)keysPresent.get(i)).doubleValue();
      }

      //
      //create a LabeledDataSetIfc[] the same length as dsIN
      //
      LabeledDataSetIfc[] lds = new LabeledDataSetIfc[dsIN.length];

      //
      //for each dataSet in dsIN[]
      //
      for(int j=0;j<dsIN.length;j++)
      {
         DataSetIfc ds = (DataSetIfc)dsIN[j];
         try
         {
            LabeledDoubleSeries ldsNEW = new LabeledDoubleSeries();
            LabeledDataSetIfc lds2;
            lds2 = (LabeledDataSetIfc)Percentile.generate(ds, percentiles);
            lds2.open();
            ldsNEW.open();
            for(int i =0;i<lds2.getNumElements();i++)
            {
               //System.out.println(lds2.getLabel(i) + " " + lds2.getElement(i) );
               String key = (String)keysPresent.get(i);
               double val = lds2.getElement(i);
               ldsNEW.addData(val,key);
            }
            lds2.close();
            ds.open();
            ldsNEW.addData(ds.getNumElements(),BoxType.NUM_OBSERVATION);
            ds.close();
            ldsNEW.close();
            lds[j] = ldsNEW;
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
      }

      //
      //pass the newly created array of LabeledDataSetIfc's to 
      //doPrecomputedProcessing(Object[])
      //
      doPrecomputedProcessing(lds);

   }
   private void doPrecomputedProcessing(Object[] ldsArray)
   {
         int nBoxes = ldsArray.length;
         rCommandsPreAdd("bx <- NULL");
         rCommandsPreAdd("bx$stats <- array(dim=c(5," + nBoxes + "))");
         rCommandsPreAdd("bx$conf <- array(dim=c(2," + nBoxes + "))");
         rCommandsPreAdd("bx$names <- array(dim=c(" + nBoxes + "))");
         rCommandsPreAdd("bx$n <- array(dim=c(" + nBoxes + "))");
         rCommandsPreAdd("bx$out <- array(dim=c(" + nBoxes + "))");
         rCommandsPreAdd("bx$group <- array(dim=c(" + nBoxes + "))");
         int outlierIndx = 1;
         for(int i = 0; i < nBoxes; ++i)
         {
            try
            {
               LabeledDataSetIfc lds = (LabeledDataSetIfc)ldsArray[i];
               rCommandsPreAdd( "bx$names["+(i+1)+"] <- " + Util.escapeQuote(lds.getName()) );
               lds.open();
               int numElements = lds.getNumElements();
               for(int j = 0; j < numElements; ++j)
               {
//                  System.out.println(lds.getLabel(j) + " = " + lds.getElement(j));
                  if(lds.getLabel(j).equals(BoxType.LOWER_WHISKER))
                  {
                     String cmd = "bx$stats[1,"+(i+1)+"] <- " + lds.getElement(j);
                     rCommandsPreAdd( cmd );
                  }
                  else if(lds.getLabel(j).equals(BoxType.LOWER_HINGE))
                  {
                     String cmd = "bx$stats[2,"+(i+1)+"] <- " + lds.getElement(j);
                     rCommandsPreAdd( cmd );
                  }
                  else if(lds.getLabel(j).equals(BoxType.MEDIAN))
                  {
                     String cmd = "bx$stats[3,"+(i+1)+"] <- " + lds.getElement(j);
                     rCommandsPreAdd( cmd );
                  }
                  else if(lds.getLabel(j).equals(BoxType.UPPER_HINGE))
                  {
                     String cmd = "bx$stats[4,"+(i+1)+"] <- " + lds.getElement(j);
                     rCommandsPreAdd( cmd );
                  }
                  else if(lds.getLabel(j).equals(BoxType.UPPER_WHISKER))
                  {
                     String cmd = "bx$stats[5,"+(i+1)+"] <- " + lds.getElement(j);
                     rCommandsPreAdd( cmd );
                  }
                  else if(lds.getLabel(j).equals(BoxType.NUM_OBSERVATION))
                  {
                     String cmd = "bx$n["+(i+1)+"] <- " + lds.getElement(j);
                     rCommandsPreAdd( cmd );
                  }
                  else if(lds.getLabel(j).equals(BoxType.LOWER_NOTCH_EXTREME))
                  {
                     String cmd = "bx$conf[1,"+(i+1)+"] <- " + lds.getElement(j);
                     rCommandsPreAdd( cmd );
                  }
                  else if(lds.getLabel(j).equals(BoxType.UPPER_NOTCH_EXTREME))
                  {
                     String cmd = "bx$conf[2,"+(i+1)+"] <- " + lds.getElement(j);
                     rCommandsPreAdd( cmd );
                  }
                  else if(lds.getLabel(j).equals(BoxType.OUTLIER))
                  {
                     String cmd = "bx$out["+ outlierIndx +"] <- " + lds.getElement(j);
                     rCommandsPreAdd( cmd );
                     rCommandsPreAdd( "bx$group["+ outlierIndx +"] <- " + (i+1) );
                     outlierIndx++;
                  }
                  else
                  {
                     System.err.println("Unknown label: " + lds.getLabel(j) + " = " + lds.getElement(j));
                  }
               }
               lds.close();
            }
            catch( java.lang.Exception e )
            {
               e.printStackTrace();
            }
         }
         //bxPrep() is predefined R function which insures that the upper and
         //lower whiskers of "bx" are not "NA"; R's bxp() command will not
         //plot the box unless the upper and lower whiskers have values. If
         //the upper or lower Whisker is "NA", then "bxPrep" will set them to
         //the value of the hinge. This way the box is drawn without the whisker
         //because the whisker is on top of the hinge
         //
         rCommandsPreAdd("bx <- bxPrep(bx)");
   }
}
