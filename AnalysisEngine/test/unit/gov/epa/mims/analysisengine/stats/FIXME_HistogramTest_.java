package gov.epa.mims.analysisengine.stats;

import gov.epa.mims.analysisengine.gui.DoubleSeries;
import gov.epa.mims.analysisengine.gui.DoubleTimeSeries;
import gov.epa.mims.analysisengine.gui.LabeledDoubleSeries;
import gov.epa.mims.analysisengine.tree.DataSetIfc;
import gov.epa.mims.analysisengine.tree.LabeledDataSetIfc;

import java.util.Calendar;

import junit.framework.TestCase;

/**
 * <p>Title: Tests for the Histogram class.</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: UNC-CH, Carolina Environmental Program</p>
 * @author Daniel Gatti
 * @version $Id: FIXME_HistogramTest_.java,v 1.1 2006/01/10 23:29:38 parthee Exp $
 */
public class FIXME_HistogramTest_ extends TestCase
{
   static boolean setUpRunAlready = false;

   // The input data series'. See setUp() for the population of this
   // data.
   static DoubleSeries wholeNumberDS = new DoubleSeries();
   static DoubleSeries smallNumberDS = new DoubleSeries();
   static DoubleSeries bigNumberDS   = new DoubleSeries();
   static DoubleSeries negativeDS   = new DoubleSeries();

   static LabeledDoubleSeries logNumbersLDS = new LabeledDoubleSeries();
   static LabeledDoubleSeries repeatsLDS = new LabeledDoubleSeries();

   static LabeledDoubleSeries zeroLDS = new LabeledDoubleSeries();
   static LabeledDoubleSeries zeroOneLDS = new LabeledDoubleSeries();
   static LabeledDoubleSeries zeroOneTwoLDS = new LabeledDoubleSeries();
   static LabeledDoubleSeries oneTwoLDS = new LabeledDoubleSeries();
   static LabeledDoubleSeries twoLDS = new LabeledDoubleSeries();
   static LabeledDoubleSeries oneTwoThreeLDS = new LabeledDoubleSeries();
   static LabeledDoubleSeries twoThreeLDS = new LabeledDoubleSeries();
   static LabeledDoubleSeries zeroOneTwoThreeLDS = new LabeledDoubleSeries();
   static LabeledDoubleSeries multiplesLDS = new LabeledDoubleSeries();

   static DoubleTimeSeries largeDataSet = new DoubleTimeSeries();

   static double[] wholeNumbers = {1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0};
   static double[] smallNumbers = {1.2E-50, 3.4E-51, 6.2E-49, 9.6E-50, 5.5E-51,
                          9.0E-50, 7.2E-49, 6.6E-51, 2.1E-50, 7.2E-49, 4.4E-51};
   static double[] bigNumbers = {1.2E50, 3.4E51, 6.2E49, 9.6E50, 5.5E51,
                          9.0E50, 7.2E49, 6.6E51, 2.1E50, 7.2E49, 4.4E51};
   static double[] zero        = {0.0};
   static double[] zeroOne     = {0.0, 1.0};
   static double[] zeroOneTwo  = {0.0, 1.0, 2.0};
   static double[] oneTwo      = {1.0, 2.0};
   static double[] two         = {2.0};
   static double[] oneTwoThree = {1.0, 2.0, 3.0};
   static double[] twoThree    = {2.0, 3.0};
   static double[] zeroOneTwoThree = {0.0, 1.0, 2.0, 3.0};

   static double[] logNumbers = {0.01, 0.1, 1.0, 10.0, 100.0, 1000.0, 10000.0};
   static double[] repeats = {1.0,1.0,1.0,1.0,2.0,2.0,3.0,4.0,5.0,5.0,5.0,5.0,6.0,7.0,
      7.0,7.0,8.0,8.0,8.0,9.0,10.0,10.0,10.0,10.0,10.0,10.0,10.0};
   static double[] multiples = {0.0, 0.0, 1.0, 1.0, 1.0, 1.0, 2.0, 2.0, 2.0, 3.0};
   static double[] negatives= {-2.0, -1.0, 0.0, 1.0, 2.0};

   // The bin range to work with.

   static double[] pointFiveBins = {0.5, 1.5, 2.5, 3.5, 4.5, 5.5, 6.5, 7.5, 8.5, 9.5};
   static double[] oneThruTenBins = {1.0 ,2.0 ,3.0 ,4.0 ,5.0 ,6.0 ,7.0 ,8.0 ,9.0, 10.0};
   static double[] smallBins = {1.0E-51, 5.0e-51, 1.0E-50, 5.0E-50, 1.0E-49, 5.0E-49, 1.0E-48};
   static double[] largeBins = {1.0E48, 5.0e48, 1.0E49, 5.0E49, 1.0E50, 5.0E50, 1.0E51, 5.0E51, 1.0E52};
   static double[] zeroThru1000Bins =
      {0.0, 100.0, 200.0, 300.0, 400.0, 500.0, 600.0, 700.0, 800.0, 900.0, 1000.0,
      1100.0, 1200.0, 1300.0, 1400.0, 1500.0, 1600.0, 1700.0, 1800.0, 1900.0, 2000.0,
      2100.0, 2200.0, 2300.0, 2400.0, 2500.0, 2600.0, 2700.0, 2800.0, 2900.0, 3000.0,
      3100.0, 3200.0, 3300.0, 3400.0, 3500.0, 3600.0, 3700.0, 3800.0, 3900.0, 4000.0,
      4100.0, 4200.0, 4300.0, 4400.0, 4500.0, 4600.0, 4700.0, 4800.0, 4900.0, 5000.0,
      5100.0, 5200.0, 5300.0, 5400.0, 5500.0, 5600.0, 5700.0, 5800.0, 5900.0, 6000.0,
      6100.0, 6200.0, 6300.0, 6400.0, 6500.0, 6600.0, 6700.0, 6800.0, 6900.0, 7000.0,
      7100.0, 7200.0, 7300.0, 7400.0, 7500.0, 7600.0, 7700.0, 7800.0, 7900.0, 8000.0,
      8100.0, 8200.0, 8300.0, 8400.0, 8500.0, 8600.0, 8700.0, 8800.0, 8900.0, 9000.0,
      9100.0, 9200.0, 9300.0, 9400.0, 9500.0, 9600.0, 9700.0, 9800.0, 9900.0, 10000.0};
   static double[] twoBins   = {1.0, 2.0, 3.0};
   static double[] oneBin    = {1.0, 2.0};
   static double[] zeroBins  = {1.0};
   static double[] negativeBins = {-2.0, -1.0, 0.0, 1.0 ,2.0};
   static double[] largeDataBins = {-1.0, -0.8, -0.6, -0.4, -0.2, 0.0, 0.2, 0.4, 0.6, 0.8, 1.0};

   /**
    * Constructor.
    * @param name String that is the name of the test method to run.
    */
   public FIXME_HistogramTest_(String name)
   {
      super(name);
   }// HistogramTest()


   /**
    * Test a Histogram with all of the numbers in the middle of the
    * bin and no boundary or overflow values.
    */
   public void FIXME_testBasicOperation()
   {
      try
      {
         // Frequency
         DataSetIfc returnDS = Histogram.generate(wholeNumberDS, pointFiveBins,null, Histogram.FREQUENCY);
         double[] freqAnswers = {0.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.0};
         double[] percAnswers = {0.0, 10.0, 10.0, 10.0, 10.0, 10.0, 10.0, 10.0,
                                 10.0, 10.0, 10.0, 0.0};
         double[] probAnswers = {0.0, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1,
                                 0.1, 0.1, 0.0};

         returnDS.open();
         int size = returnDS.getNumElements();
         // We expect one value in each bin, so all bins should read "1.0".
         for (int i = 0; i < size; i++)
         {
            assertEquals("frequency" + i, freqAnswers[i], returnDS.getElement(i), 1.0E-14);
         }
         returnDS.close();

         // Percentage
         returnDS = Histogram.generate(wholeNumberDS, pointFiveBins, null,Histogram.PERCENTAGE);
         returnDS.open();
         size = returnDS.getNumElements();
         // We expect one value in each bin, so all bins should read "10.0" (for percentage).
         for (int i = 0; i < size; i++)
         {
            assertEquals("percentage" + i, percAnswers[i], returnDS.getElement(i),1.0E-14);
         }
         returnDS.close();

         // Probability
         returnDS = Histogram.generate(wholeNumberDS, pointFiveBins, null,Histogram.PROBABILITY);
         returnDS.open();
         size = returnDS.getNumElements();
         // We expect one value in each bin, so all bins should read "0.1" (for probability).
         for (int i = 0; i < size; i++)
         {
            assertEquals("probability" + i, probAnswers[i], returnDS.getElement(i), 1.0E-14);
         }
         returnDS.close();
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   } // testBasicOperation()



   /**
    * Test what happens when the values are at the breakpoints between bins.
    */
   public void FIXME_testValuesAtBreakpoints()
   {
      try
      {
         // Frequency
         DataSetIfc returnDS = Histogram.generate(wholeNumberDS, oneThruTenBins, null,Histogram.FREQUENCY);
         double[] freqAnswers = {0.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, };
         double[] percAnswers = {0.0, 10.0, 10.0, 10.0, 10.0, 10.0, 10.0, 10.0,
                                10.0, 10.0, 10.0, 0.0};
          double[] probAnswers = {0.0, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1,
                                  0.1, 0.1, 0.0};
         returnDS.open();
         int size = returnDS.getNumElements();
         // We expect one value in each bin, so all bins should read "1.0".
         for (int i = 0; i < size; i++)
         {
            assertEquals("frequency " + i, freqAnswers[i], returnDS.getElement(i), 1.0E-14);
         }
         returnDS.close();

         // Percentage
         returnDS = Histogram.generate(wholeNumberDS, oneThruTenBins, null,Histogram.PERCENTAGE);
         returnDS.open();
         size = returnDS.getNumElements();
         // We expect one value in each bin, so all bins should read "0.1" (for percentage).
         for (int i = 0; i < size; i++)
         {
            assertEquals("percentage " + i, percAnswers[i], returnDS.getElement(i),1.0E-14);
         }
         returnDS.close();

         // Probability
         returnDS = Histogram.generate(wholeNumberDS, oneThruTenBins,null, Histogram.PROBABILITY);
         returnDS.open();
         size = returnDS.getNumElements();
         for (int i = 0; i < size; i++)
         {
            assertEquals("probability "+ i, probAnswers[i], returnDS.getElement(i), 1.0E-14);
         }
         returnDS.close();
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   } // testValuesAtBreakpoints()


   /**
    * Test small values.
    */
   public void testSmallValues()
   {
      try
      {
         // Frequency
         DataSetIfc returnDS = Histogram.generate(smallNumberDS, smallBins, null,Histogram.FREQUENCY);
         double[] freqAnswers = {0.0, 2.0, 2.0, 2.0, 2.0, 0.0, 3.0, 0.0, 0.0};
         double[] percAnswers = {0.0, 18.1818181818181818, 18.1818181818181818,
            18.1818181818181818, 18.1818181818181818, 0.0, 27.27272727272727, 0.0, 0.0};
         double[] probAnswers = {0.0, 0.1818181818181818, 0.1818181818181818,
            0.1818181818181818, 0.1818181818181818, 0.0, 0.27272727272727, 0.0, 0.0};
         returnDS.open();
         int size = returnDS.getNumElements();
         // We expect one value in each bin, so all bins should read "1.0".
         for (int i = 0; i < size; i++)
         {
            assertEquals("frequency", freqAnswers[i], returnDS.getElement(i), 1.0E-14);
         }
         returnDS.close();

         // Percentage
         returnDS = Histogram.generate(smallNumberDS, smallBins, null,Histogram.PERCENTAGE);
         returnDS.open();
         size = returnDS.getNumElements();
         // We expect one value in each bin, so all bins should read "0.1" (for percentage).
         for (int i = 0; i < size; i++)
         {
            assertEquals("percentage", percAnswers[i], returnDS.getElement(i),1.0E-14);
         }
         returnDS.close();

         // Probability
         returnDS = Histogram.generate(smallNumberDS, smallBins, null,Histogram.PROBABILITY);
         returnDS.open();
         size = returnDS.getNumElements();
         for (int i = 0; i < size; i++)
         {
            assertEquals("probability", probAnswers[i], returnDS.getElement(i), 1.0E-14);
         }
         returnDS.close();
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   } // testSmallValues()


   /**
    * Test large values.
    */
   public void testLargeValues()
   {
      try
      {
         // Frequency
         DataSetIfc returnDS = Histogram.generate(bigNumberDS, largeBins,null, Histogram.FREQUENCY);
         double[] freqAnswers = {0.0, 0.0, 0.0, 0.0, 3.0, 2.0, 2.0, 2.0, 2.0, 0.0, 0.0};
         double[] percAnswers = {0.0, 0.0, 0.0, 0.0, 27.27272727272727,
            18.1818181818181818, 18.1818181818181818,
            18.1818181818181818, 18.1818181818181818, 0.0, 0.0};
         double[] probAnswers = {0.0, 0.0, 0.0, 0.0, 0.27272727272727,
            0.1818181818181818, 0.1818181818181818,
            0.1818181818181818, 0.1818181818181818, 0.0, 0.0};
         returnDS.open();
         int size = returnDS.getNumElements();
         for (int i = 0; i < size; i++)
         {
            assertEquals("frequency", freqAnswers[i], returnDS.getElement(i), 1.0E-14);
         }
         returnDS.close();

         // Percentage
         returnDS = Histogram.generate(bigNumberDS, largeBins,null, Histogram.PERCENTAGE);
         returnDS.open();
         size = returnDS.getNumElements();
         for (int i = 0; i < size; i++)
         {
            assertEquals("percentage", percAnswers[i], returnDS.getElement(i),1.0E-14);
         }
         returnDS.close();

         // Probability
         returnDS = Histogram.generate(bigNumberDS, largeBins,null, Histogram.PROBABILITY);
         returnDS.open();
         size = returnDS.getNumElements();
         for (int i = 0; i < size; i++)
         {
            assertEquals("probability", probAnswers[i], returnDS.getElement(i), 1.0E-14);
         }
         returnDS.close();
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   } // testLargeValues()



   public void testOneBinBreakpoint()
   {
      try
      {
         // Frequency
         DataSetIfc returnDS = Histogram.generate(zeroOneLDS, zeroBins,null, Histogram.FREQUENCY);
         double[] freqAnswers = {1.0, 1.0};
         double[] percAnswers = {50.0, 50.0};
         double[] probAnswers = {0.5, 0.5};
         returnDS.open();
         int size = returnDS.getNumElements();
         for (int i = 0; i < size; i++)
         {
            assertEquals("frequency "+ i, freqAnswers[i], returnDS.getElement(i), 1.0E-14);
         }
         returnDS.close();

         // Percentage
         returnDS = Histogram.generate(zeroOneLDS, zeroBins, null,Histogram.PERCENTAGE);
         returnDS.open();
         size = returnDS.getNumElements();
         for (int i = 0; i < size; i++)
         {
            assertEquals("percentage "+ i, percAnswers[i], returnDS.getElement(i),1.0E-14);
         }
         returnDS.close();

         // Probability
         returnDS = Histogram.generate(zeroOneLDS, zeroBins, null,Histogram.PROBABILITY);
         returnDS.open();
         size = returnDS.getNumElements();
         for (int i = 0; i < size; i++)
         {
            assertEquals("probability " + i, probAnswers[i], returnDS.getElement(i), 1.0E-14);
         }
         returnDS.close();
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   } // testOneBinBreakpoint()


   /**
    * Test placing a value in a bin and in the underflow.
    */
   public void testValuesInUnderflow()
   {
      try
      {
         // Frequency
         DataSetIfc returnDS = Histogram.generate(zeroOneLDS, twoBins, null,Histogram.FREQUENCY);
         double[] freqAnswers = {1.0, 1.0, 0.0, 0.0};
         double[] percAnswers = {50.0, 50.0, 0.0, 0.0};
         double[] probAnswers = {0.5, 0.5, 0.0, 0.0};

         returnDS.open();
         int size = returnDS.getNumElements();
         for (int i = 0; i < size; i++)
         {
            assertEquals("frequency", freqAnswers[i], returnDS.getElement(i), 1.0E-14);
         }
         returnDS.close();

         // Percentage
         returnDS = Histogram.generate(zeroOneLDS, twoBins,null, Histogram.PERCENTAGE);
         returnDS.open();
         size = returnDS.getNumElements();
         for (int i = 0; i < size; i++)
         {
            assertEquals("percentage", percAnswers[i], returnDS.getElement(i),1.0E-14);
         }
         returnDS.close();

         // Probability
         returnDS = Histogram.generate(zeroOneLDS, twoBins, null,Histogram.PROBABILITY);
         returnDS.open();
         size = returnDS.getNumElements();
         for (int i = 0; i < size; i++)
         {
            assertEquals("probability", probAnswers[i], returnDS.getElement(i), 1.0E-14);
         }
         returnDS.close();
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   } // testValuesInUnderflow()


   /**
    * Test placing a value in a bin and in the overflow.
    */
   public void testValuesInOverflow()
   {
      try
      {
         // Frequency
         DataSetIfc returnDS = Histogram.generate(twoThreeLDS, twoBins,null, Histogram.FREQUENCY);
         double[] freqAnswers = {0.0, 0.0, 1.0, 1.0};
         double[] percAnswers = {0.0, 0.0, 50.0, 50.0};
         double[] probAnswers = {0.0, 0.0, 0.5, 0.5};

         returnDS.open();
         int size = returnDS.getNumElements();
         for (int i = 0; i < size; i++)
         {
            assertEquals("frequency", freqAnswers[i], returnDS.getElement(i), 1.0E-14);
         }
         returnDS.close();

         // Percentage
         returnDS = Histogram.generate(twoThreeLDS, twoBins,null, Histogram.PERCENTAGE);
         returnDS.open();
         size = returnDS.getNumElements();
         for (int i = 0; i < size; i++)
         {
            assertEquals("percentage", percAnswers[i], returnDS.getElement(i),1.0E-14);
         }
         returnDS.close();

         // Probability
         returnDS = Histogram.generate(twoThreeLDS, twoBins, null,Histogram.PROBABILITY);
         returnDS.open();
         size = returnDS.getNumElements();
         for (int i = 0; i < size; i++)
         {
            assertEquals("probability", probAnswers[i], returnDS.getElement(i), 1.0E-14);
         }
         returnDS.close();
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   } // testValuesInOverflow()


   /**
    * Test placing a value in bins and in the underflow and overflow.
    */
   public void testValuesInUnderflowAndOverflow()
   {
      try
      {
         // Frequency
         DataSetIfc returnDS = Histogram.generate(zeroOneTwoThreeLDS, twoBins,null, Histogram.FREQUENCY);
         double[] freqAnswers = {1.0, 1.0, 1.0, 1.0};
         double[] percAnswers = {25.0, 25.0, 25.0, 25.0};
         double[] probAnswers = {0.25, 0.25, 0.25, 0.25};

         returnDS.open();
         int size = returnDS.getNumElements();
         for (int i = 0; i < size; i++)
         {
            assertEquals("frequency", freqAnswers[i], returnDS.getElement(i), 1.0E-14);
         }
         returnDS.close();

         // Percentage
         returnDS = Histogram.generate(zeroOneTwoThreeLDS, twoBins,null, Histogram.PERCENTAGE);
         returnDS.open();
         size = returnDS.getNumElements();
         for (int i = 0; i < size; i++)
         {
            assertEquals("percentage", percAnswers[i], returnDS.getElement(i),1.0E-14);
         }
         returnDS.close();

         // Probability
         returnDS = Histogram.generate(zeroOneTwoThreeLDS, twoBins,null, Histogram.PROBABILITY);
         returnDS.open();
         size = returnDS.getNumElements();
         for (int i = 0; i < size; i++)
         {
            assertEquals("probability", probAnswers[i], returnDS.getElement(i), 1.0E-14);
         }
         returnDS.close();
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   } // testValuesInUnderflowAndOverflow()


   public void testNegativeNumbers()
   {
      try
      {
         // Frequency
         DataSetIfc returnDS = Histogram.generate(negativeDS, negativeBins,null, Histogram.FREQUENCY);
         double[] freqAnswers = {0.0, 1.0, 1.0, 1.0, 1.0, 1.0};
         double[] percAnswers = {0.0, 20.0, 20.0, 20.0, 20.0, 20.0};
         double[] probAnswers = {0.0, 0.2, 0.2, 0.2, 0.2, 0.2};

         returnDS.open();
         int size = returnDS.getNumElements();
         for (int i = 0; i < size; i++)
         {
            assertEquals("frequency "+ i, freqAnswers[i], returnDS.getElement(i), 1.0E-14);
         }
         returnDS.close();

         // Percentage
         returnDS = Histogram.generate(negativeDS, negativeBins, null,Histogram.PERCENTAGE);
         returnDS.open();
         size = returnDS.getNumElements();
         for (int i = 0; i < size; i++)
         {
            assertEquals("percentage "+ i, percAnswers[i], returnDS.getElement(i),1.0E-14);
         }
         returnDS.close();

         // Probability
         returnDS = Histogram.generate(negativeDS, negativeBins, null,Histogram.PROBABILITY);
         returnDS.open();
         size = returnDS.getNumElements();
         for (int i = 0; i < size; i++)
         {
            assertEquals("probability " + i, probAnswers[i], returnDS.getElement(i), 1.0E-14);
         }
         returnDS.close();
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   } // testNegativeNumbers()


   /**
    * Test Multiple Values.
    */
   public void testMultipleValues()
   {
      try
      {
         // Frequency
         DataSetIfc returnDS = Histogram.generate(multiplesLDS, twoBins,null, Histogram.FREQUENCY);
         double[] freqAnswers = {2.0, 4.0, 3.0, 1.0};
         double[] percAnswers = {20.0, 40.0, 30.0, 10.0};
         double[] probAnswers = {0.2, 0.4, 0.3, 0.1};

         returnDS.open();
         int size = returnDS.getNumElements();
         for (int i = 0; i < size; i++)
         {
            assertEquals("frequency "+ i, freqAnswers[i], returnDS.getElement(i), 1.0E-14);
         }
         returnDS.close();

         // Percentage
         returnDS = Histogram.generate(multiplesLDS, twoBins,null, Histogram.PERCENTAGE);
         returnDS.open();
         size = returnDS.getNumElements();
         for (int i = 0; i < size; i++)
         {
            assertEquals("percentage "+ i, percAnswers[i], returnDS.getElement(i),1.0E-14);
         }
         returnDS.close();

         // Probability
         returnDS = Histogram.generate(multiplesLDS, twoBins, null,Histogram.PROBABILITY);
         returnDS.open();
         size = returnDS.getNumElements();
         for (int i = 0; i < size; i++)
         {
            assertEquals("probability " + i, probAnswers[i], returnDS.getElement(i), 1.0E-14);
         }
         returnDS.close();
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   } // testMultipleValues()


   public void testBinLabels()
   {
      try
      {
         // Frequency
         LabeledDataSetIfc returnDS = Histogram.generate(zeroOneTwoThreeLDS, twoBins, null,Histogram.FREQUENCY);
         String[] answers = {"-Inf to 1.0", "1.0 to 2.0", "2.0 to 3.0", "3.0 to Inf"};

         returnDS.open();
         int size = returnDS.getNumElements();
         for (int i = 0; i < size; i++)
         {
            assertEquals("frequency " + i, answers[i], returnDS.getLabel(i));
         }
         returnDS.close();

         // Percentage
         returnDS = Histogram.generate(zeroOneTwoThreeLDS, twoBins, null,Histogram.PERCENTAGE);
         returnDS.open();
         size = returnDS.getNumElements();
         for (int i = 0; i < size; i++)
         {
            assertEquals("percentage", answers[i], returnDS.getLabel(i));
         }
         returnDS.close();

         // Probability
         returnDS = Histogram.generate(zeroOneTwoThreeLDS, twoBins, null,Histogram.PROBABILITY);
         returnDS.open();
         size = returnDS.getNumElements();
         for (int i = 0; i < size; i++)
         {
            assertEquals("probability", answers[i], returnDS.getLabel(i));
         }
         returnDS.close();
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   } // testBinLabels()


   /**
    * Test large data set with 300000 numbers.
    */
   public void testLargeDataset()
   {
      try
      {
         // Frequency
         long start = System.currentTimeMillis();
         DataSetIfc returnDS = Histogram.generate(largeDataSet, largeDataBins,null, Histogram.FREQUENCY);
         System.out.println("Large dataset processing time : " +
                          (System.currentTimeMillis() - start) + " ms.");
         double[] freqAnswers = {0.0, 100000.0, 200000.0, 300000.0, 400000.0, 500000.0,
            500000.0, 400000.0, 300000.0, 200000.0, 100000.0, 0.0};
         double[] percAnswers = {0.0, 3.33333333, 6.666666667, 10.0,  13.333333333,
            16.666666667, 16.666666667, 13.333333333, 10.0, 6.666666667, 3.33333333, 0.0};
         double[] probAnswers = {0.0, 0.033333333, 0.06666666667, 0.1,  0.13333333333,
            0.16666666667, 0.16666666667, 0.13333333333, 0.1, 0.06666666667, 0.03333333333, 0.0};

         returnDS.open();
         int size = returnDS.getNumElements();
         for (int i = 0; i < size; i++)
         {
            assertEquals("frequency "+ i, freqAnswers[i], returnDS.getElement(i), 1.0E-7);
         }
         returnDS.close();

         // Percentage
         returnDS = Histogram.generate(largeDataSet, largeDataBins,null, Histogram.PERCENTAGE);
         returnDS.open();
         size = returnDS.getNumElements();
         for (int i = 0; i < size; i++)
         {
            assertEquals("percentage "+ i, percAnswers[i], returnDS.getElement(i),1.0E-7);
         }
         returnDS.close();

         // Probability
         returnDS = Histogram.generate(largeDataSet, largeDataBins, null,Histogram.PROBABILITY);
         returnDS.open();
         size = returnDS.getNumElements();
         for (int i = 0; i < size; i++)
         {
            assertEquals("probability " + i, probAnswers[i], returnDS.getElement(i), 1.0E-7);
         }
         returnDS.close();
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   } // testLargeDataset()


   /**
    * Produce some data set to work with.
    */
   public void setUp()
   {
      if (setUpRunAlready)
         return;

      wholeNumberDS.setData(wholeNumbers);

      smallNumberDS.setData(smallNumbers);

      bigNumberDS.setData(bigNumbers);

      negativeDS.setData(negatives);

      for (int i = 0; i < logNumbers.length; i++)
      {
         logNumbersLDS.addData(logNumbers[i], "value" + i);
      }

      for (int i = 0; i < repeats.length; i++)
      {
         repeatsLDS.addData(repeats[i], "value" + i);
      }

      for (int i = 0; i < zero.length; i++)
      {
         zeroLDS.addData(zero[i], "value" + i);
      }

      for (int i = 0; i < zeroOne.length; i++)
      {
         zeroOneLDS.addData(zeroOne[i], "value" + i);
      }

      for (int i = 0; i < zeroOneTwo.length; i++)
      {
         zeroOneTwoLDS.addData(zeroOneTwo[i], "value" + i);
      }

      for (int i = 0; i < oneTwo.length; i++)
      {
         oneTwoLDS.addData(oneTwo[i], "value" + i);
      }

      for (int i = 0; i < two.length; i++)
      {
         twoLDS.addData(two[i], "value" + i);
      }

      for (int i = 0; i < oneTwoThree.length; i++)
      {
         oneTwoThreeLDS.addData(oneTwoThree[i], "value" + i);
      }

      for (int i = 0; i < twoThree.length; i++)
      {
         twoThreeLDS.addData(twoThree[i], "value" + i);
      }

      for (int i = 0; i < zeroOneTwoThree.length; i++)
      {
         zeroOneTwoThreeLDS.addData(zeroOneTwoThree[i], "value" + i);
      }

      for (int i = 0; i < multiples.length; i++)
      {
         multiplesLDS.addData(multiples[i], "value" + i);
      }

      Calendar cal = Calendar.getInstance();
      cal.set(Calendar.YEAR, 1990);
      cal.set(Calendar.MONTH, 0);
      cal.set(Calendar.DATE, 1);
      cal.set(Calendar.HOUR, 0);
      cal.set(Calendar.MINUTE, 0);
      cal.set(Calendar.SECOND, 0);
      cal.set(Calendar.MILLISECOND, 0);

      // 10000, 20000, 30000, 40000, 50000, 50000, 40000, 30000, 20000, 10000
      // -0.9 , -0.7 , -0.5 , -0.3 , -0.1 ,  0.1 ,  0.3 ,  0.5 ,  0.7 ,  0.9
      for (int i = 0; i < 100000; i++)
      {
         largeDataSet.addTimeStamp(cal.getTime(), -0.9);
         cal.add(Calendar.DATE, 1);
      }

      for (int i = 0; i < 200000; i++)
      {
         largeDataSet.addTimeStamp(cal.getTime(), -0.7);
         cal.add(Calendar.DATE, 1);
      }

      for (int i = 0; i < 300000; i++)
      {
         largeDataSet.addTimeStamp(cal.getTime(), -0.5);
         cal.add(Calendar.DATE, 1);
      }

      for (int i = 0; i < 400000; i++)
      {
         largeDataSet.addTimeStamp(cal.getTime(), -0.3);
         cal.add(Calendar.DATE, 1);
      }

      for (int i = 0; i < 500000; i++)
      {
         largeDataSet.addTimeStamp(cal.getTime(), 0.1);
         cal.add(Calendar.DATE, 1);
      }

      for (int i = 0; i < 500000; i++)
      {
         largeDataSet.addTimeStamp(cal.getTime(), -0.1);
         cal.add(Calendar.DATE, 1);
      }

      for (int i = 0; i < 400000; i++)
      {
         largeDataSet.addTimeStamp(cal.getTime(), 0.3);
         cal.add(Calendar.DATE, 1);
      }

      for (int i = 0; i < 300000; i++)
      {
         largeDataSet.addTimeStamp(cal.getTime(), 0.5);
         cal.add(Calendar.DATE, 1);
      }

      for (int i = 0; i < 200000; i++)
      {
         largeDataSet.addTimeStamp(cal.getTime(), 0.7);
         cal.add(Calendar.DATE, 1);
      }

      for (int i = 0; i < 100000; i++)
      {
         largeDataSet.addTimeStamp(cal.getTime(), 0.9);
         cal.add(Calendar.DATE, 1);
      }

      setUpRunAlready = true;
   } // setUp()


} // class HistogramTest
