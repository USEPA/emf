
package gov.epa.mims.analysisengine.stats;

import cern.colt.list.DoubleArrayList;
import cern.jet.stat.Descriptive;

import java.util.HashMap;

/**
 * <p>Description: This class provides methods to calculate regular statistics
 * using colt libaray
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: UNC-CH, Carolina Environmental Program</p>
 * @author Parthee Partheepan
 * @version $Id: SummaryStats.java,v 1.2 2005/09/19 14:50:15 rhavaldar Exp $
 */
public class SummaryStats
{
   /** stats constans */
   public static final String MIN = "Minimum";
   
   /** stats constans */
   public static final String MAX = "Maximum";
   
   /** stats constans */
   public static final String MEAN = "Mean";
   
   /** stats constans */
   public static final String MEDIAN = "Median";
   
   /** stats constans */
   public static final String SUM = "Sum";
   
   /** stats constans */
   public static final String STD_DEVIATION = "Std. Deviation";
   
   /** stats constans */
   public static final String SKEW = "Skew";
   
   /** stats constans */
   public static final String KURTOSIS = "Kurtosis";
   
   /** array containing basic statistics */
   public static final String [] BASIC_STATISTICS = {MIN, MAX, MEAN,MEDIAN,
      SUM, STD_DEVIATION,SKEW,KURTOSIS};
   
   /** a hash map to store the calculated stats from the colt package */
   private HashMap summaryStats = new HashMap();
   
   /** A constructor to create an object
    * @param double array of Double 
    */
   public SummaryStats(Double [] doubles )
   {
      DoubleArrayList list = DoubleArrayListAdapter.getDoubleArrayList(doubles);
      calculateSummaryStats(list);
   }
   
   private void calculateSummaryStats(DoubleArrayList list)
   {
      double min = Descriptive.min(list);
      double max = Descriptive.max(list);
      double mean = Descriptive.mean(list);
      double median = Descriptive.median(list);
      double sum = Descriptive.sum(list);
      
      double sumOfSquares = Descriptive.sumOfSquares(list);
      double var = Descriptive.variance(list.size(),sum,sumOfSquares);
      double stdDev = Descriptive.standardDeviation(var);
      double skew = Descriptive.skew(list, mean, stdDev);
      double kurtosis = Descriptive.kurtosis(list, mean, stdDev);
      
      summaryStats.put(MIN,Double.valueOf(min));
      summaryStats.put(MAX,Double.valueOf(max));
      summaryStats.put(MEAN,Double.valueOf(mean));
      summaryStats.put(MEDIAN,Double.valueOf(median));
      summaryStats.put(SUM,Double.valueOf(sum));
      summaryStats.put(STD_DEVIATION,Double.valueOf(stdDev));
      summaryStats.put(SKEW,Double.valueOf(skew));
      summaryStats.put(KURTOSIS,Double.valueOf(kurtosis));
   }//calculateSummarStats()
   
   /** return the HashMap which contains the caculated statistical summary
    * key for the HashMap are 
    * MIN, MAX, MEAN, MEDIAN, SUM, STD_DEVIATION,SKEW, KURTOSIS 
    * @return HashMap
    */
   public HashMap getSummaryStats()
   {
      return summaryStats;
   }//getSummaryStats
   

   
   
   /** calculates the minimum value for the array
    * @param double Double array
    * @return double min value
    */
   public static double getMin(Double [] doubles)
   {
      DoubleArrayList list = DoubleArrayListAdapter.getDoubleArrayList(doubles);
      return Descriptive.min(list);
   }
   
   /** calculates the minimum value for the array
    * @param primDoubles double array
    * @return double min value
    */
   public static double getMin(double [] primDoubles)
   {
      DoubleArrayList list = DoubleArrayListAdapter.getDoubleArrayList(primDoubles);      
      return Descriptive.min(list);
   }
   
   /** calculates the maximum value for the array
    * @param doubles Double array
    * @return double max value
    */
   public static double getMax(Double [] doubles)
   {
      DoubleArrayList list = DoubleArrayListAdapter.getDoubleArrayList(doubles);
      return Descriptive.max(list);
   }
   
   /** calculates the maximum value for the array
    * @param primDoubles double array
    * @return double max value
    */
   public static double getMax(double [] primDoubles)
   {
      DoubleArrayList list = DoubleArrayListAdapter.getDoubleArrayList(primDoubles);      
      return Descriptive.max(list);
   }//getMax()
   
   /** calculates the sum for all the values in the array
    * @param double Double array
    * @return double sums value
    */
   public static double getSum(Double [] doubles)
   {
      DoubleArrayList list = DoubleArrayListAdapter.getDoubleArrayList(doubles);
      return Descriptive.sum(list);
   }//getSum()
   
   /** calculates the sum for all the values in the array
    * @param primDoubles double array
    * @return double sums value
    */
   public static double getSum(double [] primDoubles)
   {
      DoubleArrayList list = DoubleArrayListAdapter.getDoubleArrayList(primDoubles);
      return Descriptive.sum(list);
   }//getSum()
   
   /** calculates the arithmetic mean for all the values in the array
    * @param double Double array
    * @return double sums value
    */
   public static double getMean(Double [] doubles)
   {
      DoubleArrayList list = DoubleArrayListAdapter.getDoubleArrayList(doubles);
      return Descriptive.mean(list);
   }//getMean()
   
   /** calculates the arithmetic mean for all the values in the array
    * @param primDouble double array
    * @return double mean value
    */
   public static double getMean(double [] primDoubles)
   {
      DoubleArrayList list = DoubleArrayListAdapter.getDoubleArrayList(primDoubles);
      return Descriptive.mean(list);
   }//getMean()
   
   /** calculates the median for all the values in the array
   * @param double Double array
   * @return double meadian value
   */
   public static double getMedian(Double [] doubles)
   {
      DoubleArrayList list = DoubleArrayListAdapter.getDoubleArrayList(doubles);
      return Descriptive.median(list);
   }//getMedian()
   
   /** calculates the median for all the values in the array
    * @param primDouble double array
    * @return double median value
    */
   public static double getMedian(double [] primDoubles)
   {
      DoubleArrayList list = DoubleArrayListAdapter.getDoubleArrayList(primDoubles);
      return Descriptive.median(list);
   }//getMedian()
   
   /** calculates the Std Deviation for all the values in the array
   * @param double Double array
   * @return double std dev value
   */
   public static double getStdDeviation(Double [] doubles)
   {
      DoubleArrayList list = DoubleArrayListAdapter.getDoubleArrayList(doubles);
      return getStdDeviation(list);
   }//getStdDeviation()
   
   /** calculates the median for all the values in the array
    * @param primDouble double array
    * @return double std. dev. value
    */
   public static double getStdDeviation(double [] primDoubles)
   {
      DoubleArrayList list = DoubleArrayListAdapter.getDoubleArrayList(primDoubles);
      return getStdDeviation(list);
   }//getStdDeviation()
   
   /** a helper method to calculate the standard deviation */
   private static double getStdDeviation(DoubleArrayList list)
   {
      double sumOfSquares = Descriptive.sumOfSquares(list);
      double sum = Descriptive.sum(list);
      double var = Descriptive.variance(list.size(),sum, sumOfSquares);
     
      return Descriptive.standardDeviation(var);
   }//getStdDeviation
   
   /** calculates the skewness for all the values in the array
   * @param double Double array
   * @return double skew value
   */
   public static double getSkew(Double [] doubles)
   {
      DoubleArrayList list = DoubleArrayListAdapter.getDoubleArrayList(doubles);
      return getSkew(list);
   }//getSkew()
   
   /** calculates the skewness for all the values in the array
   * @param double double array
   * @return double skew value
   */
   public static double getSkew(double [] doubles)
   {
      DoubleArrayList list = DoubleArrayListAdapter.getDoubleArrayList(doubles);
      return getSkew(list);
   }//getSkew()
   
   /** calculates the skewness for all the values in the array
   * @param list DoubleArrayList
   * @return double skew value
   */
   private static double getSkew(DoubleArrayList list)
   {
      double mean = Descriptive.mean(list);
      double stdDev = getStdDeviation(list);
      
      return Descriptive.skew(list,mean, stdDev);
   }//getSkew()
   
   /** calculates the kurtosis for all the values in the array
   * @param double Double array
   * @return double kurtosis value
   */
   public static double getKurtosis(Double [] doubles)
   {
      DoubleArrayList list = DoubleArrayListAdapter.getDoubleArrayList(doubles);
      return getKurtosis(list);
   }//getKurtosis()

   /** calculates the kurtosis for all the values in the array
   * @param double double array
   * @return double kurtosis value
   */
   public static double getKurtosis(double [] doubles)
   {
      DoubleArrayList list = DoubleArrayListAdapter.getDoubleArrayList(doubles);
      return getKurtosis(list);
   }//getKurtosis()   
   
   /** calculates the kurtosis for all the values in the array
   * @param double double array
   * @return double kurtosis value
   */
   public static double getKurtosis(DoubleArrayList list)
   {
      double mean = Descriptive.mean(list);
      double stdDev = getStdDeviation(list);
      
      return Descriptive.kurtosis(list, mean, stdDev);
   }//getKurtosis()   
   
   public static HashMap getAllNaNSummary()
   {
      Double nan = Double.valueOf(Double.NaN);
      HashMap sumStats = new HashMap();
      sumStats.put(MIN,nan);
      sumStats.put(MAX,nan);
      sumStats.put(MEAN,nan);
      sumStats.put(MEDIAN,nan);
      sumStats.put(SUM,nan);
      sumStats.put(STD_DEVIATION,nan);
      sumStats.put(SKEW,nan);
      sumStats.put(KURTOSIS,nan);
      return sumStats;
   }
}
