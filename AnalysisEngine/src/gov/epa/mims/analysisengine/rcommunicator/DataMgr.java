package gov.epa.mims.analysisengine.rcommunicator;

import gov.epa.mims.analysisengine.AnalysisException;
import gov.epa.mims.analysisengine.tree.DataSetIfc;
import gov.epa.mims.analysisengine.tree.DateDataSetIfc;
import gov.epa.mims.analysisengine.tree.LabeledDataSetIfc;

import java.io.FileOutputStream;
import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;


/**
 * controls the generation of the R data file
 *
 * @version $Revision: 1.2 $
 * @author Tommy E. Cathey
 */
public class DataMgr
{
   /** list of DataInfo object; each one wraps a single data series */
   private ArrayList dataSets = new ArrayList();

   /** earliest timestamp to plot; (tw1,tw2) defines the plot window
     * for time axes */
   private Date t1w = null;

   /** latest timestamp to plot; (tw1,tw2) defines the plot window
     * for time axes */
   private Date t2w = null;

   /** max number of elements among all the data sets */
   private int maxNumElements = 0;

   /** reference to a TimeSeriesDefaultFormatter object*/
   private TimeSeriesDefaultFormatter timeSeriesDefaultFormatter;

   /**
    * Creates a new DataMgr object.
    *
    * @param dataKeys2DataSeries HashMap which maps data keys to data series
    *
    * @throws java.lang.Exception if unable to successfully initialize the
    *  DataInfo obj
    ********************************************************/
   public DataMgr(HashMap dataKeys2DataSeries) throws java.lang.Exception
   {
      //for each key-Data series create a DataInfo obj and add it the List
      //of DataInfo objects, dataSets
      //Note: DataInfo is an inner class
      Set keys = dataKeys2DataSeries.keySet();
      Iterator iter = keys.iterator();

      while (iter.hasNext())
      {
         Object key = iter.next();
         Object ds = dataKeys2DataSeries.get(key);
         DataInfo di = new DataInfo((DataSetIfc) ds, key);
         dataSets.add(di);
      }


      //set the class variable maxNumElements
      findMaxNumElements();


      //set the class variables tw1 & tw2 if this is a DateDataSetIfc obj
      findWorldDateRange();
   }

   public DataMgr(HashMap dataKeys2DataSeries,Date t1w)
   throws java.lang.Exception
   {
      this.t1w = t1w;
      //for each key-Data series create a DataInfo obj and add it the List
      //of DataInfo objects, dataSets
      //Note: DataInfo is an inner class
      Set keys = dataKeys2DataSeries.keySet();
      Iterator iter = keys.iterator();

      while (iter.hasNext())
      {
         Object key = iter.next();
         Object ds = dataKeys2DataSeries.get(key);
         DataInfo di = new DataInfo((DataSetIfc) ds, key);
         dataSets.add(di);
      }


      //set the class variable maxNumElements
      findMaxNumElements();


      //set the class variables tw1 & tw2 if this is a DateDataSetIfc obj
      findWorldDateRange2();
   }

   public DataMgr(HashMap dataKeys2DataSeries,Date t1w, Date t2w)
   throws java.lang.Exception
   {
      this.t1w = t1w;
      this.t2w = t2w;

      //for each key-Data series create a DataInfo obj and add it the List
      //of DataInfo objects, dataSets
      //Note: DataInfo is an inner class
      Set keys = dataKeys2DataSeries.keySet();
      Iterator iter = keys.iterator();

      while (iter.hasNext())
      {
         Object key = iter.next();
         Object ds = dataKeys2DataSeries.get(key);
         DataInfo di = new DataInfo((DataSetIfc) ds, key);
         dataSets.add(di);
      }


      //set the class variable maxNumElements
      findMaxNumElements();

   }

   /**
    * allows the user to override the world view of a date axis
    *
    * @param t1w earliest timestamp to plot; (tw1,tw2) defines the plot window
    * for time axes
    * @param t2w atest timestamp to plot; (tw1,tw2) defines the plot window
    * for time axes
    ********************************************************/
   public void setWorldDateRange(Date t1w, Date t2w)
   {
      this.t1w = t1w;
      this.t2w = t2w;
   }

   /**
    * finds the max # of elements among all the data sets; sets
    * the class variable maxNumElements
    ********************************************************/
   private void findMaxNumElements()
   {
      for (int i = 0; i < dataSets.size(); ++i)
      {
         DataInfo di = (DataInfo) dataSets.get(i);
         int numElements = di.getNumElements();
         maxNumElements = Math.max(maxNumElements, numElements);
      }
   }

   /**
    * finds the earliest & latest date among all the data sets; sets
    * the class variables tw1 & tw2
    ********************************************************/
   private void findWorldDateRange()
   {

      for (int i = 0; i < dataSets.size(); ++i)
      {
         DataInfo di = (DataInfo) dataSets.get(i);
         Date t1x = di.getDateT1();
         Date t2x = di.getDateT2();
  
         if (t1x != null)
         {
            if (t1w == null)
            {
               t1w = t1x;
               t2w = t2x;
            }
            else
            {
               t1w = (t1x.before(t1w))
                     ? t1x
                     : t1w;
               t2w = (t2x.before(t2w))
                     ? t2w
                     : t2x;
            }
         }
      }
   }

   /**
    * finds the latest date among all the data sets; sets
    * the class variable tw2
    ********************************************************/
   private void findWorldDateRange2()
   {

      for (int i = 0; i < dataSets.size(); ++i)
      {
         DataInfo di = (DataInfo) dataSets.get(i);
         Date t1x = di.getDateT1();
         Date t2x = di.getDateT2();
  
         if (t2x != null)
         {
            if (t2w == null)
            {
               t2w = t2x;
            }
            else
            {
               t2w = (t2x.before(t2w))
                     ? t2w
                     : t2x;
            }
         }
      }
   }

   /**
    * writes the data file which R reads
    *
    * @param file output filename
    *
    * @throws java.lang.Exception if unable to successfully write data file
    ********************************************************/
   public void writeDataFile(String file) throws java.lang.Exception
   {
      //check to see if we are outputing DateDateSetIfc data
      if ((t1w != null) && (t2w != null))
      {
         //initialize the TimeSeriesAxisConverter static class
         TimeSeriesAxisConverter.init(t1w, t2w);


         //call TimeSeriesAxisConverter to have it generate us a
         //TimeSeriesDefaultFormatter for formatting timestamp data
         timeSeriesDefaultFormatter = TimeSeriesAxisConverter.getDefaultFormatter();
      }

      //open a PrintWriter
      PrintWriter out = new PrintWriter(new FileOutputStream(file));

      //ask each DataInfo obj to print its header
      for (int i = 0; i < dataSets.size(); ++i)
      {
         DataInfo di = (DataInfo) dataSets.get(i);
         di.printHeader(out);
      }


      //line return
      out.println();

      //write maxNumElements lines of data to the output stream
      for (int j = 0; j < maxNumElements; ++j)
      {
         //start each line with an index
         out.print((j + 1) + " ");

         //ask each DataInfo obj to print its j-th data element
         for (int i = 0; i < dataSets.size(); ++i)
         {
            DataInfo di = (DataInfo) dataSets.get(i);
            di.printElement(out, j);
         }


         //line return
         out.println();
      }


      //close the PrintWriter
      out.close();
   }

   /**
    * describe object in a String
    *
    * @return String describing object
    ******************************************************/
   public String toString()
   {
      return Util.toString(this);
   }

   /**
    * inner class which wraps a DataSetIfc obj; it contains the ugly details of
    * writing the R data file
    *
    * @version $Revision: 1.2 $
    * @author Tommy E. Cathey
    */
   private class DataInfo
   {
      /** refenence to a DataSetIfc obj */
      private DataSetIfc ds;

      /** num data elements in DataSetIfc obj "ds" */
      private int numElements;

      /** earliest timestamp if this is DateDataSetIfc obj */
      private Date t1 = null;

      /** last timestamp if this is DateDataSetIfc obj */
      private Date t2 = null;

      /** R variable for R to use when referring to this data */
      private String rVar;

      /**
       * Creates a new DataInfo object.
       *
       * @param ds DataSetIfc obj
       * @param key key associated with the Data Series obj
       *
       * @throws java.lang.Exception if unable to successfully create obj
       ********************************************************/
      public DataInfo(DataSetIfc ds, Object key) throws java.lang.Exception
      {
         this.ds = ds;
         this.numElements = ds.getNumElements();

         if (ds instanceof DateDataSetIfc)
         {
            t1 = ((DateDataSetIfc) ds).getDate(0);
            t2 = ((DateDataSetIfc) ds).getDate(numElements - 1);
         }

         rVar = Rvariable.getName(key);
      }

      /**
       * retrieve the # elements in data set
       *
       * @return the # elements in data set
       ********************************************************/
      public int getNumElements()
      {
         return numElements;
      }

      /**
       * retrieve the first timestamp
       *
       * @return first timestamp
       ********************************************************/
      public Date getDateT1()
      {
         return t1;
      }

      /**
       * retrieve the last timestamp
       *
       * @return last timestamp
       ********************************************************/
      public Date getDateT2()
      {
         return t2;
      }

      /**
       * print header for data output data file
       *
       * @param out PrintWriter output stream
       ********************************************************/
      public void printHeader(PrintWriter out)
      {
         if (ds instanceof LabeledDataSetIfc)
         {
            out.print(" " + rVar + " " + rVar + "Label");
         }
         else if (ds instanceof DateDataSetIfc)
         {
            out.print(" " + rVar + "TimeStamp " + rVar);
         }
         else if (ds instanceof DataSetIfc)
         {
            out.print(" " + rVar);
         }
         else
         {
            throw new AnalysisException("Unknown data series");
         }
      }

      /**
       * print i-th element of data to output stream
       *
       * @param out output PrintWriter stream
       * @param i index into data elements
       ********************************************************/
      public void printElement(PrintWriter out, int i)
      {
         try
         {
            if (ds instanceof LabeledDataSetIfc)
            {
               printElement(out, (LabeledDataSetIfc) ds, i);
            }
            else if (ds instanceof DateDataSetIfc)
            {
               printElement(out, (DateDataSetIfc) ds, i);
            }
            else if (ds instanceof DataSetIfc)
            {
               printElement(out, (DataSetIfc) ds, i);
            }
            else
            {
               throw new AnalysisException("Unknown data series");
            }
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
      }

      /**
       * output the i-th data element on the output stream
       *
       * @param out PrintWriter output stream
       * @param ds DataSetIfc data series obj
       * @param i data element index
       ********************************************************/
      private void printElement(PrintWriter out, DataSetIfc ds, int i)
      {
         try
         {
            double val = ds.getElement(i);

            if (Double.isNaN(val))
            {
               out.print(" NA ");
            }
            else
            {
               out.print(" " + val);
            }
         }
         catch (NoSuchElementException e)
         {
            out.print(" NA ");
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
      }

      /**
       * output the i-th data element on the output stream
       *
       * @param out PrintWriter output stream
       * @param ds DateDataSetIfc data series obj
       * @param i data element index
       ********************************************************/
      private void printElement(PrintWriter out, DateDataSetIfc ds, int i)
      {
         String xStr = null;

         try
         {
            Date date = ds.getDate(i);


            //pass the date to timeSeriesDefaultFormatter; this is
            //necessary for generating the time format;
            //timeSeriesDefaultFormatter needs to see each date data
            //pt in order to properly format the timestamps
            timeSeriesDefaultFormatter.addDate(date);

            //convert the date into user coordinates
            double x = TimeSeriesAxisConverter.date2user(date);

            if (Double.isNaN(x))
            {
               xStr = " NA ";
            }
            else
            {
               xStr = " " + x;
            }
         }
         catch (NoSuchElementException e)
         {
            xStr = " NA ";
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }

         try
         {
            double val = ds.getElement(i);

            if (Double.isNaN(val))
            {
               out.print(xStr + " NA ");
            }
            else
            {
               out.print(xStr + " " + val);
            }
         }
         catch (NoSuchElementException e)
         {
            out.print(xStr + " NA ");
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
      }

      /**
       * output the i-th data element on the output stream
       *
       * @param out PrintWriter output stream
       * @param ds LabeledDataSetIfc data series obj
       * @param i data element index
       ********************************************************/
      private void printElement(PrintWriter out, LabeledDataSetIfc ds, int i)
      {
         String label = null;

         try
         {
            label = ds.getLabel(i);
            //label = label.trim();
            if (label.length() == 0)
            {
               label = " NA ";
            }

            label = Util.escapeQuote(label);
         }
         catch (NoSuchElementException e)
         {
            label = " NA ";
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }

         try
         {
            double val = ds.getElement(i);

            if (Double.isNaN(val))
            {
               out.print(" NA " + label);
            }
            else
            {
               out.print(" " + val + " " + label);
            }
         }
         catch (NoSuchElementException e)
         {
            out.print(" NA " + label);
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
      }
   }
}
