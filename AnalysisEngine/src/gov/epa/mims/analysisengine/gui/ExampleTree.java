package gov.epa.mims.analysisengine.gui;

import gov.epa.mims.analysisengine.AnalysisEngineConstants;
import gov.epa.mims.analysisengine.rcommunicator.*;
import gov.epa.mims.analysisengine.tree.AnalysisOptions;
import gov.epa.mims.analysisengine.tree.Axis;
import gov.epa.mims.analysisengine.tree.AxisNumeric;
import gov.epa.mims.analysisengine.tree.BarPlot;
import gov.epa.mims.analysisengine.tree.BarType;
import gov.epa.mims.analysisengine.tree.DataSets;
import gov.epa.mims.analysisengine.tree.OutlineType;
import gov.epa.mims.analysisengine.tree.Page;
import gov.epa.mims.analysisengine.tree.PageType;
import gov.epa.mims.analysisengine.tree.Text;

import java.awt.Color;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * class_description
 *
 * @author Tommy E. Cathey
 * @version $Id: ExampleTree.java,v 1.2 2005/09/19 14:50:03 rhavaldar Exp $
 *
 **/
public class ExampleTree
implements
gov.epa.mims.analysisengine.tree.AnalysisOptionConstantsIfc
{
   /** DOCUMENT_ME */
   private AnalysisOptions optionsGlobal;

   /** DOCUMENT_ME */
   private AnalysisOptions optionsLocalPlot1;

   /** DOCUMENT_ME */
   private AnalysisOptions optionsLocalPlot2;

   /** DOCUMENT_ME */
   private AnalysisOptions optionsLocalPlot3;

   /** DOCUMENT_ME */
   private DataSets dataSetsGlobal;

   /** DOCUMENT_ME */
   private Page page1;

   /** DOCUMENT_ME */
   //private Page page2;

   /** DOCUMENT_ME */
   //private Page page3;

   /** DOCUMENT_ME */
   private BarPlot plot1;

   /** DOCUMENT_ME */
   //private BarPlot plot2;

   /** DOCUMENT_ME */
   //private BarPlot plot3;

   /** DOCUMENT_ME */
   private BarType barTypeGlobal;

   /** DOCUMENT_ME */
   private BarType barType1;

   /** DOCUMENT_ME */
   private BarType barType2;

   /** DOCUMENT_ME */
   private BarType barType3;

   /** DOCUMENT_ME */
   private DoubleSeries years;

   /** DOCUMENT_ME */
   private LabeledDoubleSeries ds1;

   /** DOCUMENT_ME */
   private LabeledDoubleSeries ds2;

   /** DOCUMENT_ME */
   private LabeledDoubleSeries ds3;

   /** DOCUMENT_ME */
   private LabeledDoubleSeries ds4;

   /** DOCUMENT_ME */
   private DoubleSeries ds5;

   /** DOCUMENT_ME */
   private DoubleSeries ds6;

   /** DOCUMENT_ME */
   private DoubleSeries ds7;

   /** DOCUMENT_ME */
   private LabeledDoubleSeries ds8;

   /** DOCUMENT_ME */
   private LabeledDoubleSeries ds9;

   /** DOCUMENT_ME */
   private LabeledDoubleSeries ds10;

   /** DOCUMENT_ME */
   private DoubleTimeSeries ds11;

   /** DOCUMENT_ME */
   private DoubleTimeSeries ds12;

   /** A label series to test the bug reported by Prashant Pai(Categorized values for plotting) */
   private LabeledDoubleSeries dsTest1;
   private LabeledDoubleSeries dsTest2;
   private LabeledDoubleSeries dsTest3;
   private LabeledDoubleSeries dsTest4;

   private LabeledDoubleSeries ds13;
   private LabeledDoubleSeries ds14;
   private LabeledDoubleSeries ds15;
   private LabeledDoubleSeries ds16;

   public DataSets getTree()
   {
       return dataSetsGlobal;
   }

   /**
    * Creates a new ExampleTree object.
    ********************************************************/
   public ExampleTree(boolean entireTree)
   {
      if (!entireTree)
      {
        setDataSets();
      }
      else
      {
        setDataSets();
        setupTree();
      }
   }

   /**
    * Creates a new ExamplePlots01 object.
    ********************************************************/
   public ExampleTree()
   {
      setDataSets();
      setupTree();
   }

   private void setupTree()
   {
      // create a composite structure like so:
      //
      //
      //                   dataSetsGlobal
      //                         |
      //                   optionsGlobal
      //                         |
      //           -------------------------------------------------------->
      //           |                   |                     |
      //   optionsLocalPlot1   optionsLocalPlot2    optionsLocalPlot3
      //           |                   |                     |
      //         page1               page2                 page3
      //           |                   |                     |
      //         plot1               plot2                 plot3
      //
      //
      //
      optionsGlobal = new AnalysisOptions();
      optionsLocalPlot1 = new AnalysisOptions();
      //optionsLocalPlot2 = new AnalysisOptions();
      //optionsLocalPlot3 = new AnalysisOptions();
      page1 = new Page();
      //page2 = new Page();
      //page3 = new Page();
      plot1 = new BarPlot();
      //plot2 = new BarPlot();
      //plot3 = new BarPlot();


      //
      // trunk of tree
      //
      //
      // 1-st branch of tree
      //
      //optionsGlobal.add(optionsLocalPlot1);
      //optionsLocalPlot1.add(page1);
      //page1.add(plot1);
      dataSetsGlobal.add(optionsGlobal);
      optionsGlobal.add(page1);
      page1.add(optionsLocalPlot1);
      optionsLocalPlot1.add(plot1);
      // data sets <- options global<-optionsLocal<-page1<-plot1
      // data sets <- page options <- page <- plot options <- plot


      //
      // 2-nd branch of tree
      //
 /*     optionsGlobal.add(optionsLocalPlot2);
      optionsLocalPlot2.add(page2);
      page2.add(plot2);
*/

      //
      // 3-nd branch of tree
      //
 /*     optionsGlobal.add(optionsLocalPlot3);
      optionsLocalPlot3.add(page3);
      page3.add(plot3);
*/
      // associate the data with the plots
      //plot1.setDataSetKeys(new Object[] { new String[] { key5, key6, key7 } });
      //plot2.setDataSetKeys(new Object[] { new String[] { key4, key5, key6 } });
      //plot3.setDataSetKeys(new Object[] { new String[] { key7, key8, key9 } });


      /*
            plot1.addyKey( key1 );
            plot1.addyKey( key2 );
            plot1.addyKey( key3 );
            plot1.addyKey( key4 );

            plot2.addyKey( key5 );
            plot2.addyKey( key6 );

            plot3.addyKey( key7 );
            plot3.addyKey( key8 );
            plot3.addyKey( key9 );
      */

      //====================================================================
      //
      // GLOBAL OPTIONS SECTION
      //
      //====================================================================
      PageType pageTypeGlobal = new PageType();
      pageTypeGlobal.setForm("X11");
      pageTypeGlobal.setFilename("MyPlotGlobal.pdf");
      pageTypeGlobal.setDeleteTemporaryFileOnExit(false);
      optionsGlobal.addOption(PAGE_TYPE,
                              pageTypeGlobal);


      barTypeGlobal = new BarType();
      barTypeGlobal.setColor(new Color[]
      {
         Color.blue, Color.red, Color.green, Color.yellow,
      });
      //barTypeGlobal.setBorderColor(Color.green);
      barTypeGlobal.setHorizontal(false);
      barTypeGlobal.setStacked(false);
      barTypeGlobal.setSpaceBetweenBars(1.2);
      barTypeGlobal.setSpaceBetweenCategories(2.0);
      barTypeGlobal.setWidth(new double[] { 0.5 });
      optionsLocalPlot1.addOption(BAR_TYPE,barTypeGlobal);

      OutlineType outlineTypeGlobal = new OutlineType();
      outlineTypeGlobal.setLineStyle(OutlineType.PLOT, "SOLID");
      outlineTypeGlobal.setLineWidth(OutlineType.PLOT, 2.0);
      outlineTypeGlobal.setColor(OutlineType.PLOT, Color.red);
      outlineTypeGlobal.setDraw(OutlineType.PLOT, true);
      optionsLocalPlot1.addOption(OUTLINE_TYPE,outlineTypeGlobal);

      Text xAxisTextGlobal = new Text();
      xAxisTextGlobal.setTextString("The X Axis Label");
      xAxisTextGlobal.setColor(Color.blue);
      xAxisTextGlobal.setPosition("N",
                                  0.5,
                                  0.4);
      xAxisTextGlobal.setTextExpansion(1.2);
      xAxisTextGlobal.setTextDegreesRotation(0.0);
      xAxisTextGlobal.setTypeface("sans serif");
      xAxisTextGlobal.setStyle("italic");

      AxisNumeric xAxisGlobal = new AxisNumeric();
      xAxisGlobal.setAxisLabelText(xAxisTextGlobal);
//      xAxisGlobal.setDrawOpposingAxis(false);
      xAxisGlobal.setEnableAxis(true);
      xAxisGlobal.setDrawTickMarks(true);
      xAxisGlobal.setDrawTickMarkLabels(true);
      xAxisGlobal.setDrawTickMarkLabelsPerpendicularToAxis(
            false);


      //    xAxisGlobal.setLinesIntoMargin(double);
      //    xAxisGlobal.setPosition(double);
      xAxisGlobal.setAxisColor(Color.blue);
      xAxisGlobal.setTickMarkLabelColor(Color.green);
      xAxisGlobal.setTickMarkFont("italic");
      xAxisGlobal.setTickMarkLabelExpansion(1.1);


      //    xAxisGlobal.setUserTickMarkPositions( double[] );
      //    xAxisGlobal.setUserTickMarkLabels( String[] );
      optionsLocalPlot1.addOption(X_NUMERIC_AXIS, xAxisGlobal);

      Text yAxisTextGlobal = (Text) xAxisTextGlobal.clone();
      yAxisTextGlobal.setPosition("C",
                                  0.3,
                                  0.5);


      //      yAxisTextGlobal.setXJustification( 0.3 );
      //      yAxisTextGlobal.setYJustification( 0.5 );
      yAxisTextGlobal.setTextDegreesRotation(90.0);
      yAxisTextGlobal.setTextString("The Y Axis Label");

      Axis yAxisGlobal = (Axis) xAxisGlobal.clone();
      yAxisGlobal.setAxisLabelText(yAxisTextGlobal);
      yAxisGlobal.setDrawTickMarkLabelsPerpendicularToAxis(
            true);
      optionsLocalPlot1.addOption(Y_NUMERIC_AXIS, yAxisGlobal);

      Text titleTextGlobal = new Text();
      titleTextGlobal.setTextString("Default Title Text");
      titleTextGlobal.setColor(Color.red);
      titleTextGlobal.setPosition("N",
                                  0.5,
                                  0.5);


      //      titleTextGlobal.setPosition( "N" );
      //      titleTextGlobal.setXJustification( 0.5 );
      //      titleTextGlobal.setYJustification( 0.5 );
      //      titleTextGlobal.setX( 0.5 );
      //      titleTextGlobal.setY( 0.5 );
      titleTextGlobal.setTextExpansion(1.5);
      titleTextGlobal.setTextDegreesRotation(0.0);
      titleTextGlobal.setTypeface("sans serif");
      titleTextGlobal.setStyle("italic");
      titleTextGlobal.setColor(Color.black);
      optionsLocalPlot1.addOption(PLOT_TITLE, titleTextGlobal);

      Text subtitleTextGlobal = (Text) titleTextGlobal.clone();
      subtitleTextGlobal.setTextExpansion(1.0);
      subtitleTextGlobal.setPosition(Text.CENTER,0.5, 0.5);
      subtitleTextGlobal.setColor(Color.blue);
      subtitleTextGlobal.setTextString("My Subtitle");
      optionsLocalPlot1.addOption(PLOT_SUBTITLE, subtitleTextGlobal);

      Text footerTextGlobal = (Text) titleTextGlobal.clone();
      footerTextGlobal.setTextString("A Footer");
      footerTextGlobal.setColor(Color.red);
      footerTextGlobal.setPosition(Text.SOUTH, 0.5, 0.5);
      footerTextGlobal.setTextExpansion(1.0);
      //optionsLocalPlot1.addOption(PLOT_FOOTER,
      //                        footerTextGlobal);

      //====================================================================
      //
      // LOCAL OPTIONS  PLOT 1 SECTION
      //
      //====================================================================
/*      BarType barTypeLocal1 = (BarType) barTypeGlobal.clone();
      barTypeLocal1.setHorizontal(true);
      barTypeLocal1.setStacked(true);
      optionsLocalPlot1.addOption("BAR_TYPE",
                                  barTypeLocal1);

      Axis xAxisLocal1 = (Axis) xAxisGlobal.clone();
      xAxisLocal1.setDrawAxis(true);
      optionsLocalPlot1.addOption("XAXIS",
                                  xAxisLocal1);

      Axis yAxisLocal1 = (Axis) yAxisGlobal.clone();
      yAxisLocal1.setDrawTickMarkLabelsPerpendicularToAxis(
            false);
      yAxisLocal1.setDrawAxis(false);
      optionsLocalPlot1.addOption("YAXIS",
                                  yAxisLocal1);

      PageType pageTypeLocal1 = (PageType) pageTypeGlobal.clone();
      pageTypeLocal1.setForm("X11");
      pageTypeLocal1.setFilename("MyPlot1.pdf");
      optionsLocalPlot1.addOption(PAGE_TYPE,
                                  pageTypeLocal1);
*/
      //====================================================================
      //
      // LOCAL OPTIONS  PLOT 2 SECTION
      //
      //====================================================================
  /*    PageType pageTypeLocal2 = (PageType) pageTypeGlobal.clone();
      pageTypeLocal2.setForm("X11");
      pageTypeLocal2.setFilename("MyPlot2.pdf");
      optionsLocalPlot2.addOption(PAGE_TYPE,
                                  pageTypeLocal2);

      Axis xAxisLocal2 = (Axis) xAxisGlobal.clone();
      xAxisLocal2.setDrawAxis(true);
      optionsLocalPlot2.addOption("XAXIS",
                                  xAxisLocal2);

      Axis yAxisLocal2 = (Axis) yAxisGlobal.clone();
      yAxisLocal2.setDrawTickMarkLabelsPerpendicularToAxis(
            false);
      yAxisLocal2.setDrawAxis(false);
      optionsLocalPlot2.addOption("YAXIS",
                                  yAxisLocal2);
*/
      //====================================================================
      //
      // LOCAL OPTIONS  PLOT 3 SECTION
      //
      //====================================================================
  /*    PageType pageTypeLocal3 = (PageType) pageTypeGlobal.clone();
      pageTypeLocal3.setForm("X11");
      pageTypeLocal3.setFilename("MyPlot3.pdf");
      optionsLocalPlot3.addOption(PAGE_TYPE,
                                  pageTypeLocal3);

      Axis xAxisLocal3 = (Axis) xAxisGlobal.clone();
      xAxisLocal3.setDrawAxis(true);
      optionsLocalPlot3.addOption("XAXIS",
                                  xAxisLocal3);

      Axis yAxisLocal3 = (Axis) yAxisGlobal.clone();
      yAxisLocal3.setDrawTickMarkLabelsPerpendicularToAxis(
            false);
      yAxisLocal3.setDrawAxis(true);
      optionsLocalPlot3.addOption("YAXIS",
                                  yAxisLocal3);
*/
      //====================================================================
      //
      // GENERATE THE PLOTS
      //
      //====================================================================
/*      String Rexec = System.getProperty("os.name").startsWith(
                           "Windows") ? "RTerm.exe" : "R";
      RGenerator rGen = new RGenerator(Rexec);

      try
      {
         rGen.setLog(new File("myLogFile.txt"));
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }

      rGen.setEcho(false);
      rGen.execute(dataSetsGlobal);
      RCommunicator.getInstance().closePlotWindows(2000);
      RCommunicator.getInstance().terminate();
*/   }

   /**
    * describe object in a String
    *
    * @return String describing object
    ******************************************************/
   public String toString()
   {
      return Util.toString(this);
   }

   private void setDataSets()
   {
     dataSetsGlobal = new DataSets();
      //====================================================================
      //
      // DATA SECTION
      //
      //====================================================================
      // create data objects and initialize them
      years = new DoubleSeries();
      ds1 = new LabeledDoubleSeries();
      ds2 = new LabeledDoubleSeries();
      ds3 = new LabeledDoubleSeries();
      ds4 = new LabeledDoubleSeries();
      ds5 = new DoubleSeries();
      ds6 = new DoubleSeries();
      ds7 = new DoubleSeries();
      ds8 = new LabeledDoubleSeries();
      ds9 = new LabeledDoubleSeries();
      ds10 = new LabeledDoubleSeries();
      ds11 = new DoubleTimeSeries();
      ds12 = new DoubleTimeSeries();
      ds13 = new LabeledDoubleSeries();
      ds14 = new LabeledDoubleSeries();
      ds15 = new LabeledDoubleSeries();
      ds16 = new LabeledDoubleSeries();

      // store data; use data sets unique ID as an unique key name
      String key1 = "Elemental Mercury";
      String key2 = "Divalent Mercury";
      String key3 = "Methyl Mercury";
      String key4 = "Hexafluorobenzene";
      String key5 =  "Arkansas";
      String key6 = "North Carolina";
      String key7 = "South Dakota";
      String key8 = "1990";
      String key9 = "1995";
      String key10 = "2000";
      String key11 = "1992 dates";
      String key12 = "1994 dates";
      String key13 = "SO2";
      String key14 = "CO2";
      String key15 = "O2";
      String key16 = "one";



      dataSetsGlobal.setDataSetsDescription("Chemicals");

      String [] yearNames = { "1990", "1991", "1992", "1993", "1994", "1995",
         "1996", "1997", "1998", "1999", "2000", "2001", "2002", "2003", "2004" };
      double [] data1 =
      {
         0.07933926, 1.89976074, 1.18010911, 1.96434966, 0.42043384, 0.61419898,
         0.59516337, 0.67621901, 0.65481466, 0.43962053, 1.18350314, 0.06924401,
         0.81824585, 1.71812279, 0.11406066
      };
      //ds1.setData(data1);


      double [] data2 =
      {
         2.4302503, 2.7578401, 1.1578873, 2.1609027, 0.9619740, 0.7278891,
         1.4118504, 1.8953461, 0.9269839, 1.2146265, 0.7873825, 0.2559219,
         0.8357666, 0.1338156, 0.8093797
      };
      //ds2.setData(data2);

      double [] data3 =
      {
         0.41749691, 0.56145016, 1.57874680, 0.36855991, 1.35039071, 0.02571504,
         0.32187162, 0.53296439, 0.16513854, 1.35647847, 0.51114237, 0.31337636,
         1.74215200, 1.30813949, 0.65415411
      };
      //ds3.setData(data3);

      double [] data4 =
      {
         0.60992276, 0.62793871, 1.12138778, 2.36647371, 0.07124480, 0.25774145,
         1.58306992, 1.39942837, 0.71327156, 0.98158741, 0.09663926, 1.63492913,
         0.24413480, 0.47653353, 0.82995247
      };
      //ds4.setData(data4);

      for (int i = 0; i < data1.length; i++)
      {
        ds1.addData(data1[i],yearNames[i]);
        ds2.addData(data2[i],yearNames[i]);
        ds3.addData(data3[i],yearNames[i]);
        ds4.addData(data4[i],yearNames[i]);
      }


      ds1.setContentDescription("Annual average dose");
      ds1.setUnits("mg-kg/day");
      ds2.setContentDescription("Annual average dose");
      ds2.setUnits("mg-kg/day");
      ds3.setContentDescription("Annual average dose");
      ds3.setUnits("mg-kg/day");
      ds4.setContentDescription("Annual average dose");
      ds4.setUnits("mg-kg/day");
      ds1.setName(key1);
      ds2.setName(key2);
      ds3.setName(key3);
      ds4.setName(key4);
      
      double [] data5 = { 117000, 23000, -15000, 34000 };
      ds5.setData(data5 );
      double [] data6 = { 8.7E4, 1.8E4, 2.5E4, 4.5E4 };
      ds6.setData(data6);
      double [] data7 = { 6.7E4, 5.6E4, 2.8E4, 2.2E4 };
      ds7.setData(data7);
      ds5.setName(key5);
      ds6.setName(key6);
      ds7.setName(key7);
      
      
      

      String [] chemNames = { key1, key2, key3, key4};
      /** see how it handles small values */
      for (int i = 0; i < chemNames.length; i++)
      {
         ds8.addData((Math.random())*1E-6, chemNames[i]);
         ds9.addData((Math.random())*1E-6, chemNames[i]);
         ds10.addData((Math.random())*1E-6, chemNames[i]);
      }
      ds8.setName(key8);
      ds9.setName(key9);
      ds10.setName(key10);
      
      

      SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss zzz");
      //SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy");
      //SimpleDateFormat dateFormatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z");
      // 15 values to match chemicals.
      String[] dates_1992 = {
         "01/01/1992 00:00:00 GMT", "02/01/1992 00:00:00 GMT",
         "03/01/1992 00:00:00 GMT", "04/01/1992 00:00:00 GMT",
         "05/01/1992 00:00:00 GMT", "06/01/1992 00:00:00 GMT",
         "07/01/1992 00:00:00 GMT", "08/01/1992 00:00:00 GMT",
         "09/01/1992 00:00:00 GMT", "10/01/1992 00:00:00 GMT",
         "11/01/1992 00:00:00 GMT", "12/01/1992 00:00:00 GMT",
         "12/10/1992 00:00:00 GMT", "12/15/1992 00:00:00 GMT",
         "12/20/1992 00:00:00 GMT"
      };
      // 15 values to match chemicals.
      try
      {
         for (int i = 0; i< dates_1992.length; i++)
         {
            ds11.addTimeStamp(dateFormatter.parse(dates_1992[i]), (Math.random())*1E-6);
         }
         ds11.setName(key11);

         // 15 values to match chemicals.
         String[] dates_1994 = {
            "01/01/1994 00:00:00 GMT", "02/01/1994 01:00:00 GMT",
            "03/01/1994 02:00:00 GMT", "04/01/1994 03:00:00 GMT",
            "05/01/1994 04:00:00 GMT", "06/01/1994 05:00:00 GMT",
            "07/01/1994 06:00:00 GMT", "08/01/1994 07:00:00 GMT",
            "09/01/1994 08:00:00 GMT", "10/01/1994 09:00:00 GMT",
            "11/01/1994 10:00:00 GMT", "12/01/1994 11:00:00 GMT",
            "01/01/1994 12:00:00 GMT", "12/15/1994 13:00:00 GMT",
            "12/20/1994 14:00:00 GMT"
         };

         for (int i = 0; i< dates_1994.length; i++)
            ds12.addTimeStamp(dateFormatter.parse(dates_1994[i]), (Math.random())*1E-6);
         ds12.setName(key12);

      }
      catch (ParseException pe)
      {
         pe.printStackTrace();
      }
      
      // Data Set 13
      ds13.setName(key13);
      String[] ds13Labels = {"VE2", "VE2", "VE2", "VE3", "VE3", "VE3"};
      double[] ds13Values = {4.0, 5.0, 6.0, 7.0, 8.0, 9.0};
      for (int i = 0; i < ds13Labels.length; i++)
         ds13.addData(ds13Values[i], ds13Labels[i]);

      // Data Set 14
      ds14.setName(key14);
      String[] ds14Labels = {"VE1", "VE1", "VE2", "VE2", "VE3", "VE3"};
      double[] ds14Values = {1.5, 2.5, 3.5, 4.5, 5.5, 6.5};
      for (int i = 0; i < ds14Labels.length; i++)
         ds14.addData(ds14Values[i], ds14Labels[i]);

      // Data Set 15
      ds15.setName(key15);
      String[] ds15Labels = {"VE1", "VE1", "VE1", "VE2", "VE2", "VE2", "VE3", "VE3", "VE3"};
      double[] ds15Values = {10.0, 11.0, 12.0, 13.0, 14.0, 15.0, 16.0, 17.0, 18.0};
      for (int i = 0; i < ds15Labels.length; i++)
         ds15.addData(ds15Values[i], ds15Labels[i]);

      // Data Set 16
      ds16.setName(key16);
      String[] ds16Labels = {"one point"};
      double[] ds16Values = {1.0};
      ds16.addData(ds16Values[0], ds16Labels[0]);

      
      dataSetsGlobal.add(ds1, ds1.getName());
      dataSetsGlobal.add(ds2, ds2.getName());
      dataSetsGlobal.add(ds3, ds3.getName());
      dataSetsGlobal.add(ds4, ds4.getName());
      dataSetsGlobal.add(ds5, ds5.getName());
      dataSetsGlobal.add(ds6, ds6.getName());
      dataSetsGlobal.add(ds7, ds7.getName());
      dataSetsGlobal.add(ds8, ds8.getName());
      dataSetsGlobal.add(ds9, ds9.getName());
      dataSetsGlobal.add(ds11,ds10.getName());
      dataSetsGlobal.add(ds12,ds11.getName());
      dataSetsGlobal.add(ds13,ds12.getName());
      dataSetsGlobal.add(ds14,ds13.getName());
      dataSetsGlobal.add(ds15,ds14.getName());
      dataSetsGlobal.add(ds16,ds15.getName());

      
      String key17 = "1990 to 2005"; 
      for (int i = 1990; i < 2005; i++)
      {
        years.addData((double)i);
      }
      years.setName(key17);
      dataSetsGlobal.add(years,years.getName());


      //-----------Testing --------------// will be removed RP
      dsTest1 = new LabeledDoubleSeries();
      dsTest2 = new LabeledDoubleSeries();
      dsTest3 = new LabeledDoubleSeries();
      dsTest4 = new LabeledDoubleSeries();

      dsTest1.setName("Black");
      dsTest2.setName("NatAm");
      dsTest3.setName("Other");
      dsTest4.setName("White");

      dsTest1.addData(1.0,"F");
      dsTest1.addData(2.0,"M");
      dsTest2.addData(Double.NaN,"F");
      dsTest2.addData(4.0,"M"); //NatAm does not have a data for "F"
      dsTest3.addData(5.0,"F");
      dsTest3.addData(6.0,"M");
      dsTest4.addData(7.0,"F");
      dsTest4.addData(8.0,"M");


      dataSetsGlobal.add(dsTest1,dsTest1.getName());
      dataSetsGlobal.add(dsTest2,dsTest2.getName());
      dataSetsGlobal.add(dsTest3,dsTest3.getName());
      dataSetsGlobal.add(dsTest4,dsTest4.getName());

      //-----------Testing --------------// will be removed RP

   }

   /**
    * DOCUMENT_ME
    *
    * @param args DOCUMENT_ME
    ********************************************************/
   public static void main(String[] args)
   {
      ExampleTree examplePlots01 = new ExampleTree();
   }
}

