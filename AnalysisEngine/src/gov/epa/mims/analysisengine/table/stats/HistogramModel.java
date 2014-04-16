
package gov.epa.mims.analysisengine.table.stats;

import gov.epa.mims.analysisengine.stats.Histogram;
import gov.epa.mims.analysisengine.table.format.SignificantDigitsFormat;
import gov.epa.mims.analysisengine.tree.DataSetIfc;
import gov.epa.mims.analysisengine.tree.DataSetsAdapter;

import java.util.Vector;


/** HistogramModel.java
 * <p> A data model for the histogram gui </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: CEP, UNC-Chapel Hill </p>
 * @author Parthee Partheepan
 * @version $Id: HistogramModel.java,v 1.1 2006/11/01 15:33:39 parthee Exp $
 */

public class HistogramModel
{
   
   /** bin break points  */
   private double[] breakPoints;

   /** default e format pattern */
   public final static String DEFAULT_EFORMAT= "0.00E00";
   
   /** format for the labels */
   private SignificantDigitsFormat format =  new SignificantDigitsFormat(DEFAULT_EFORMAT);
   
   /** type of the histogram */
   private byte histogramType = Histogram.FREQUENCY;

   /** minimum value in the DataSetsAdapter */
   private double minValue ;
   
   /** maximum value in the DataSetsAdapter */
   private double maxValue;

   /** a counter to track the no of histogram created during a model run and used
   * in naming the tab name 
   */
   public static int histogram_counter =1;
   
   /** a tab name for the histogram */
   private String tabName = "Histogram "+ histogram_counter;
 
   /** denote the default bin size */
   private final int DEFAULT_BIN_SIZE = 10;

   /** no of bins, initially equal to the default size */
   private int noOfBins = DEFAULT_BIN_SIZE;
   
   /** indicates equal bins */
   public static final int EQUAL_BIN_SIZE = 10001; 
   /** indicates custom bins */
   public static final int CUSTOM_BIN_SIZE = 10002;
   /** indicates factor of 10 bins */
   public static final int FACTOR_OF10 = 10003;
   /** a variable to indicate the bin type */
   private int binType = EQUAL_BIN_SIZE;
   
   /** Creates a new instance of HistogramModel */
   public HistogramModel(DataSetsAdapter dataSets) throws Exception
   {
      calculateMinMaxValue(dataSets);
   }
   
   
   /** a helper method to find the max and min value *
    */
   private void calculateMinMaxValue(DataSetsAdapter dataSets) throws Exception
   {
      
      minValue = Double.MAX_VALUE;
      maxValue = Integer.MIN_VALUE;
      DataSetIfc dataSeries;
      Vector data = dataSets.getDataSets(null,null);
      for (int i= 0; i < data.size(); i++)
      {
         dataSeries = (DataSetIfc) data.get(i);
         
         int numOfElements = 0;
         try
         {
            dataSeries.open();
            numOfElements = dataSeries.getNumElements();
         }
         catch (Exception e)
         {
            throw e;
         }
         
         for (int j=0; j < numOfElements; j++)
         {
            double value = 0;
            try
            {
               value = dataSeries.getElement(j);
               //initialize
               if(i==0 && j==0)
               {
                  maxValue = value;
                  minValue = value;
               }
            }
            catch(Exception e)
            {
               throw e;
            }
            //min maxing
            if(value > maxValue)
            {
               maxValue = value;
            }
            else if( value < minValue)
            {
               minValue = value;
            }
         }//for(j)
      }//for(i)
   }//calculateMinMaxValue()
   
   /** a helper method to create bins *
    */
   public double[] createEqualSizeBins(int noOfBins, double minValue, double maxValue)
   {
      double [] bins = new double[noOfBins+1];
      double gapLength = (maxValue - minValue)/noOfBins;
      double VERY_SMALL_VALUE= gapLength/10000.0; //this one thins need to be teseted
      for (int i=0; i< noOfBins+1; i++)
      {
         bins[i] = minValue + i*gapLength;
      }
      bins[noOfBins] = bins[noOfBins] + VERY_SMALL_VALUE;
      return bins;
   }//createEqualSizeBins()
   
   /** a helper method to create bins *
    */
   public double[] createFactorTenBins(int noOfSteps, double minValue)
   {
      double [] bins = new double[noOfSteps+1];
      bins[0] = minValue;
      for (int i=1; i< noOfSteps+1; i++)
      {
         bins[i] = bins[i-1] * 10.0;
      }//for(i)
      return bins;
   }//createFactorTenBins
   
   /** getter bin break points
    * @return double[]
    */
   public double [] getBinBreakPoints()
   {  
      return breakPoints;
   }//getBins()
   
   /** Getter for property binType.
    * @return Value of property binType.
    *
    */
   public int getBinType()
   {
      return binType;
   }
   
   /** Getter for property format.
    * @return Value of property format.
    *
    */
   public SignificantDigitsFormat getFormat()
   {
      return format;
   }   
   
   /** to get the histogram type
    */
   public byte getHistogramType()
   {
      return histogramType;
   }
   
   /** to get the min value
    * @return double 
    */
   public double getMinValue()
   {
      return minValue;
   }//getMinValue()
   
   /** to get the max value
    * @return double 
    */
   public double getMaxValue()
   {
      return maxValue;
   }// getMaxValue()
   
   /** get the no of bins */
   public int getNoOfBins()
   {
      return noOfBins;
   }

      /** Getter for property tabName.
    * @return Value of property tabName.
    *
    */
   public String getTabName()
   {
      return tabName;
   }   

   
   /** Setter for property binType.
    * @param binType New value of property binType.
    *
    */
   public void setBinType(int binType)
   {
      this.binType = binType;
   }
   
   /** Setter for property bin break points
    * @param bin breakPoints double [] 
    */
   public void setBinBreakPoints(double[] binBreakPoints)
   {
      this.breakPoints = binBreakPoints;
   }

    /** Setter for property format.
    * @param format New value of property format.
    *
    */
   public void setFormat(SignificantDigitsFormat format)
   {
      this.format = format;
   }   

    /** set the histogram type */
   public void setHistogramType(byte histogramType)
   {
      this.histogramType = histogramType;
   }
   
    /** set the no of bins */
   public void setNoOfBins(int noOfBins)
   {
      this.noOfBins = noOfBins;
   }
   
   /*setter for property maxValue
    *@param maxValue 
    */
   public void setMaxValue(double maxValue)
   {
      this.maxValue = maxValue;
   }

   /*setter for property minValue
    *@param minValue 
    */
   public void setMinValue(double minValue)
   {
      this.minValue = minValue;
   }
   
   /** Setter for property tabName.
    * @param tabName New value of property tabName.
    *
    */
   public void setTabName(String tabName)
   {
      this.tabName = tabName;
   }  
}