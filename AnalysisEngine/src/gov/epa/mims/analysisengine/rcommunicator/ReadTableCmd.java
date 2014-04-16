package gov.epa.mims.analysisengine.rcommunicator;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.io.PrintWriter;
import java.io.FileOutputStream;

import gov.epa.mims.analysisengine.tree.DataSetIfc;
import gov.epa.mims.analysisengine.tree.LabeledDataSetIfc;
import gov.epa.mims.analysisengine.tree.DateDataSetIfc;
import gov.epa.mims.analysisengine.AnalysisEngineConstants;

/**
 * DOCUMENT_ME
 *
 * @version $Revision: 1.2 $
 * @author Tommy E. Cathey
 ********************************************************/
public class ReadTableCmd extends Cmd
{
   private static HashMap key2DataSet;
   private static List keys;
   private static boolean deleteOnExit;
   private static int maxElements;
   private boolean dataSetsOpen = false;
   private PrintWriter out = null;
   private TimeSeriesDefaultFormatter timeSeriesDefaultFormatter;
   private boolean initializedForTimeSeries = false;
   private Date earliestTimeStamp = null;
   private Date latestTimeStamp = null;
   private ArrayList conversionCmds = new ArrayList();

   public ReadTableCmd(HashMap key2DataSet, List keys, boolean deleteOnExit)
   {
      super();
      super.setName( "read.table" );
      super.setReturnVariable( "plotData" );
      super.variableAdd("colClasses",Util.escapeQuote("character"));
      conversionCmds.clear();

      this.key2DataSet = key2DataSet;
      this.keys = uniqueList(keys);
      this.deleteOnExit = deleteOnExit;
      maxElements = openDataSets();
      initializeTimeSeries();
   }
   private List uniqueList(List nonuniqueList)
   {
      List uniqueList = new ArrayList();
      for( int i=0; i< nonuniqueList.size(); i++)
      {
         if(! uniqueList.contains(nonuniqueList.get(i)) )
            uniqueList.add(nonuniqueList.get(i));
      }
      return uniqueList;
   }

   public Date getEarliestTimeStamp()
   {
      return earliestTimeStamp;
   }

   public Date getLatestTimeStamp()
   {
      return latestTimeStamp;
   }

   public void finalize()
   {
      if( dataSetsOpen )
      {
         out.close();
      }
   }

   public List getCommands()
   {
      try
      {
         out = createTempFile();
         printHeader(out);
         printData(out,maxElements);
         closeDataSets();
         out.close();
      }
      catch( java.lang.Exception e )
      {
         e.printStackTrace();
      }

      rCommandsPostAdd( conversionCmds );
      rCommandsPostAdd( "attach(plotData)" );
      return super.getCommands();
   }

   private void initializeTimeSeries()
   {
      timeSeriesDefaultFormatter = TimeSeriesAxisConverter.getDefaultFormatter();
      double U1 = AnalysisEngineConstants.TIME_SERIES_USER_COORD1;
      double U2 = AnalysisEngineConstants.TIME_SERIES_USER_COORD2;

      rCommandsPostAdd( "U1 <- " + U1 );
      rCommandsPostAdd( "U2 <- " + U2 );
      rCommandsPostAdd( "attach(plotData)" );
      rCommandsPostAdd( "normalizeTimeStamps(plotData,U1,U2,world1,world2)");

      initializedForTimeSeries = true;
   }

   private void closeDataSets() 
   {
      for(int i = 0;i<keys.size();i++)
      {
         Object key = keys.get(i);
         DataSetIfc ds = (DataSetIfc)key2DataSet.get(key);
         try
         {
            ds.close();
         }
         catch(java.lang.Exception e)
         {
            e.printStackTrace();
         }
      }
      dataSetsOpen = false;
   }

   private int openDataSets()
   {
      int maxElements = 0;
      for(int i = 0;i<keys.size();i++)
      {
         Object key = keys.get(i);
         DataSetIfc ds = (DataSetIfc)key2DataSet.get(key);
         try
         {
            ds.open();
            maxElements = Math.max(maxElements,ds.getNumElements());
         }
         catch(java.lang.Exception e)
         {
            e.printStackTrace();
         }

      }
      dataSetsOpen = true;
      return maxElements;
   }

   private void printData(PrintWriter out, int maxElements)
   {
      for(int i = 0;i<maxElements;i++)
      {
         int colNum = i + 1;
         out.println("" + colNum + " " + getDataLine(i));
      }
   }

   private String getDataLine(int elementIndex)
   {
      StringBuffer b = new StringBuffer();

      for(int i = 0;i<keys.size();i++)
      {
         b.append(" ");
         Object key = keys.get(i);
         Object ds = key2DataSet.get(key);
         if( ds instanceof DateDataSetIfc)
         {
            b.append(elementAsString((DateDataSetIfc)ds,elementIndex));
         }
         else if( ds instanceof LabeledDataSetIfc)
         {
            b.append(elementAsString((LabeledDataSetIfc)ds,elementIndex));
         }
         else
         {
            b.append(elementAsString((DataSetIfc)ds,elementIndex));
         }
      }
      return b.toString();
   }

   private void printHeader(PrintWriter out)
   {
      StringBuffer b = new StringBuffer();

      for(int i = 0;i<keys.size();i++)
      {
         b.append(" ");
         Object key = keys.get(i);
         Object ds = key2DataSet.get(key);
         if( ds instanceof DateDataSetIfc)
         {
            b.append(headerAsString((DateDataSetIfc)ds,(String)key));
         }
         else if( ds instanceof LabeledDataSetIfc)
         {
            b.append(headerAsString((LabeledDataSetIfc)ds,(String)key));
         }
         else
         {
            b.append(headerAsString((DataSetIfc)ds,(String)key));
         }
      }
      out.println(b.toString());
   }

   private PrintWriter createTempFile() throws java.lang.Exception
   {
      PrintWriter out = null;
      String filename = null;
      try
      {
         filename = Util.createTmpFile("Rdata","txt",deleteOnExit);
         out = new PrintWriter(new FileOutputStream(filename));
         super.variableAdd("file",Util.escapeQuote(filename));
      }
      catch( java.lang.Exception e )
      {
         e.printStackTrace();
         throw new java.lang.Exception("Unable to open: " + filename );
      }
      return out;
   }

   private String headerAsString(DataSetIfc ds, Object key)
   {
      String rVar = Rvariable.getName(key);
      conversionCmds.add( "plotData$"+rVar + " <- type.convert("+rVar+")" );
      return rVar;
   }

   private String headerAsString(DateDataSetIfc ds, Object key)
   {
      String rVar = Rvariable.getName(key);
      String ts = rVar + "TimeStamp";
      String c = ts + " <- date2user(" + ts + ")";
      conversionCmds.add( "plotData$"+rVar + " <- type.convert(plotData$"+rVar+")" );
      conversionCmds.add( "plotData$"+ts + " <- type.convert(plotData$"+ts+")" );
      conversionCmds.add( "plotData$"+ts + " <- date2user(plotData$" + ts + ")" );
      return ts + " " + rVar;
   }

   private String headerAsString(LabeledDataSetIfc ds, Object key)
   {
      String rVar = Rvariable.getName(key);
      conversionCmds.add( "plotData$"+rVar + " <- type.convert(plotData$"+rVar+")" );
      return rVar + " " + rVar + "Label";
   }

   private String elementAsString(DataSetIfc ds , int elementIndex)
   {
      String elementStr = null;

      try
      {
         double val = ds.getElement(elementIndex);

         if (Double.isNaN(val))
         {
            elementStr = " NA ";
         }
         else
         {
            elementStr = " " + val;
         }
      }
      catch (NoSuchElementException e)
      {
         elementStr = " NA ";
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
      return elementStr;
   }

   private String elementAsString(DateDataSetIfc ds , int elementIndex)
   {
      String elementStr = null;
      String xStr = null;

      try
      {
         Date date = ds.getDate(elementIndex);

         //pass the date to timeSeriesDefaultFormatter; this is
         //necessary for generating the time format;
         //timeSeriesDefaultFormatter needs to see each date data
         //pt in order to properly format the timestamps
         timeSeriesDefaultFormatter.addDate(date);

         xStr = " " + date.getTime();

         if( earliestTimeStamp == null )
         {
            earliestTimeStamp = date;
         }
         else
         {
            earliestTimeStamp = (date.before(earliestTimeStamp))
                     ? date
                     : earliestTimeStamp;
         }
         if( latestTimeStamp == null )
         {
            latestTimeStamp = date;
         }
         else
         {
            latestTimeStamp = (date.before(latestTimeStamp))
                     ? latestTimeStamp
                     : date;
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
         double val = ds.getElement(elementIndex);

         if (Double.isNaN(val))
         {
            elementStr = xStr + " NA ";
         }
         else
         {
            elementStr = xStr + " " + val;
         }
      }
      catch (NoSuchElementException e)
      {
         elementStr = xStr + " NA ";
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }

      return elementStr;
   }

   private String elementAsString(LabeledDataSetIfc lds , int elementIndex)
   {
      String elementStr = null;

      String label = null;

      try
      {
         label = lds.getLabel(elementIndex);
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
         double val = lds.getElement(elementIndex);

         if (Double.isNaN(val))
         {
            elementStr = " NA " + label;
         }
         else
         {
            elementStr = " " + val + " " + label;
         }
      }
      catch (NoSuchElementException e)
      {
         elementStr = " NA " + label;
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }

      return elementStr;
   }

   public static void main(String[] args)
   {
      HashMap key2DataSet = new HashMap();
      java.util.ArrayList keys = new java.util.ArrayList();

      LabeledDoubleSeries lds1 = new LabeledDoubleSeries();
      lds1.setName("series 1");
      lds1.addData(-1.6400241,"alvdkdjds");
      lds1.addData(-0.5901705,"yevxds");
      lds1.addData(0.9075,"xds");

      key2DataSet.put("key1",lds1);
      keys.add("key1");

      ReadTableCmd c = null;
      c = new ReadTableCmd(key2DataSet, keys, false);

      List cmds = c.getCommands();
      for(int i=0;i<cmds.size();i++)
      {
         System.out.println(cmds.get(i));
      }
   }
}

