package gov.epa.mims.analysisengine.gui;

import gov.epa.mims.analysisengine.AnalysisEngineConstants;
import gov.epa.mims.analysisengine.rcommunicator.RGenerator;
import gov.epa.mims.analysisengine.tree.*;

import java.awt.Color;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;


/**
 * class_description
 *
 * @author Tommy E. Cathey
 * @version $Id: GUITestInvoker.java,v 1.1 2006/07/21 17:36:37 parthee Exp $
 *
 **/
public class GUITestInvoker
implements
gov.epa.mims.analysisengine.tree.AnalysisOptionConstantsIfc
{
   /** DOCUMENT_ME */
   private ArrayList commandsToTest = new ArrayList();

   /** DOCUMENT_ME */
   private final boolean ECHO = false;

   /** DOCUMENT_ME */
   private final boolean RUN_R = false;

   /** DOCUMENT_ME */
   private ArrayList expectedResults = new ArrayList();

   /** DOCUMENT_ME */
   private AnalysisOptions optionsGlobal;

   /** DOCUMENT_ME */
   private AnalysisOptions optionsLocalPlot1;

   /** DOCUMENT_ME */
   private AnalysisOptions optionsLocalPlot2;

   /** DOCUMENT_ME */
   private AnalysisOptions optionsLocalPlot3;

   /** DOCUMENT_ME */
   private AnalysisOptions optionsLocalPage2;

   /** DOCUMENT_ME */
   private AnalysisOptions optionsLocalPage3;

   /** DOCUMENT_ME */
   private DataSets dataSets1;

   /** DOCUMENT_ME */
   private DataSets dataSets2;

   /** DOCUMENT_ME */
   private Page page1;

   /** DOCUMENT_ME */
   private Page page2;

   /** DOCUMENT_ME */
   private Page page3;

   /** DOCUMENT_ME */
   private ScatterPlot scatterPlot1;

   /** DOCUMENT_ME */
   private ScatterPlot scatterPlot2;

   /** DOCUMENT_ME */
   private BarPlot barPlot1;

   /** DOCUMENT_ME */
   private BarType barType1;

   private GridType gridType;

   private Legend legend;

   private Text titleText1;

   /** DOCUMENT_ME */
   private TimeSeries TimeSeriesPlot;

   /** DOCUMENT_ME */
   private DoubleSeries ds1;

   /** DOCUMENT_ME */
   private DoubleSeries ds2;

   /** DOCUMENT_ME */
   private DoubleSeries ds3;

   /** DOCUMENT_ME */
   private DoubleSeries ds4;

   /** DOCUMENT_ME */
   private DoubleSeries ds5;

   /** DOCUMENT_ME */
   private DoubleSeries ds6;

   /** DOCUMENT_ME */
   private DoubleSeries ds7;

   /** DOCUMENT_ME */
   private DoubleSeries ds8;

   /** DOCUMENT_ME */
   private DoubleSeries ds9;

   /**
    * Creates a new GUITest object.
    ********************************************************/
   public GUITestInvoker()
   {
     optionsGlobal = new AnalysisOptions();
     optionsLocalPlot1 = new AnalysisOptions();
     optionsLocalPlot2 = new AnalysisOptions();
     optionsLocalPlot3 = new AnalysisOptions();
     optionsLocalPage2 = new AnalysisOptions();
     optionsLocalPage3 = new AnalysisOptions();
     dataSets1 = new DataSets();
     dataSets2 = new DataSets();
     page1 = new Page();
     page2 = new Page();
     page3 = new Page();
     scatterPlot1 = new ScatterPlot();
     scatterPlot2 = new ScatterPlot();
     barPlot1 = new BarPlot();
     barType1 = new BarType();
     ds1 = new DoubleSeries();
     ds2 = new DoubleSeries();
     ds3 = new DoubleSeries();
     ds4 = new DoubleSeries();
     ds5 = new DoubleSeries();
     ds6 = new DoubleSeries();
     ds7 = new DoubleSeries();
     ds8 = new DoubleSeries();
     ds9 = new DoubleSeries();

     Color[] colors = new Color[]
     {
        Color.blue, Color.red, Color.yellow, Color.green, Color.orange,
        Color.pink
     };

     String[] lineTypes = new String[]
     {
        "SOLID", "DASHED", "DOTTED", "DOTDASH", "LONGDASH", "TWODASH"
     };

     double[] lineWidths = new double[] { 2.5, 3.3 };

     String[] symbols = new String[] { "TRIANGLE_UP", "CIRCLE", "TRIANGLE_UP" };

     LineType lineType1 = new LineType();
     lineType1.setPlotStyle("POINTS_n_LINES");
     lineType1.setColor(colors);
     lineType1.setLineStyle(lineTypes);
     lineType1.setLineWidth(lineWidths);
     lineType1.setSymbol(symbols);
     lineType1.setSymbolExpansion(new double[] { 1.5, 2.5 });
     optionsGlobal.addOption("LINE_TYPE",
                             lineType1);

     gridType = new GridType();
     gridType.setLineStyle("215131");
     gridType.setColor(Color.green);
     gridType.setLineWidth(1.0);
     gridType.setNumberXcells(3);
     gridType.setNumberYcells(-1); // -1 = default to tick marks
     gridType.setDraw(true);
     optionsGlobal.addOption("GRID_TYPE",
                             gridType);

     OutlineType outlineType = new OutlineType();
     outlineType.setLineStyle(OutlineType.PLOT, "SOLID");
     outlineType.setLineWidth(OutlineType.PLOT, 2.0);
     outlineType.setColor(OutlineType.PLOT, Color.red);
     outlineType.setDraw(OutlineType.PLOT, true);
     optionsGlobal.addOption("OUTLINE_TYPE",
                             outlineType);

     ds1.setData(new double[]
     {
        -0.07933926, 1.89976074, -1.18010911, -1.96434966, 0.42043384,
        -0.61419898, -0.59516337, 0.67621901, -0.65481466, 0.43962053,
        -1.18350314, 0.06924401, 0.81824585, 1.71812279, 0.11406066, 0.12
     });

     ds2.setData(new double[]
     {
        2.4302503, 2.7578401, 1.1578873, -2.1609027, -0.9619740, 0.7278891,
        -1.4118504, -1.8953461, 0.9269839, -1.2146265, 0.7873825, 0.2559219,
        0.8357666, -0.1338156, -0.8093797
     });

     ds3.setData(new double[]
     {
        0.41749691, -0.56145016, 1.57874680, -0.36855991, -1.35039071,
        -0.02571504, -0.32187162, 0.53296439, -0.16513854, 1.35647847,
        0.51114237, -0.31337636, -1.74215200, -1.30813949, -0.65415411
     });

     ds4.setData(new double[]
     {
        -0.60992276, 0.62793871, 1.12138778, 2.36647371, -0.07124480,
        -0.25774145, 1.58306992, -1.39942837, -0.71327156, 0.98158741,
        -0.09663926, 1.63492913, 0.24413480, 0.47653353, 0.82995247
     });

     ds5.setData(new double[] { 11.7, 2.3, 1.5 });

     ds6.setData(new double[] { 8.7, 1.8, 2.5, 5.6 });

     ds7.setData(new double[] { 6.7 });

     ds8.setData(new double[] { 1.5 });

     ds9.setData(new double[] { 7.5 });

     ds1.setName("My data set 1");
     ds2.setName("My data set 2");
     ds3.setName("My data set 3");
     ds4.setName("My data set 4");
     ds5.setName("My data set 5");
     ds6.setName("My data set 6");
     ds7.setName("My data set 7");
     ds8.setName("My data set 8");
     ds9.setName("My data set 9");

     // store data; use data sets unique ID as an unique key name

     /*
           Object key1 = new Integer(1);
           Object key2 = new Integer(2);
           Object key3 = new Integer(3);
           Object key4 = new Integer(4);
           Object key5 = new Integer(5);
           Object key6 = new Integer(6);
           Object key7 = new Integer(7);
           Object key8 = new Integer(8);
           Object key9 = new Integer(9);
     */
     String key1 = "key1";
     String key2 = "key2";
     String key3 = "key3";
     String key4 = "key4";
     String key5 = "key5";
     String key6 = "key6";
     String key7 = "key7";
     String key8 = "key8";
     String key9 = "key9";
     dataSets1.add(ds1,
                   key1);
     dataSets1.add(ds2,
                   key2);
     dataSets1.add(ds4,
                   key4);
     dataSets1.add(ds5,
                   key5);
     dataSets1.add(ds6,
                   key6);
     dataSets1.add(ds7,
                   key7);
     dataSets1.add(ds8,
                   key8);
     dataSets1.add(ds9,
                   key9);

     dataSets2.add(ds3,
                   key3);

     /*
           scatterPlot1.setxKey( key1 );
           scatterPlot1.addyKey( key2 );
           scatterPlot1.addyKey( key3 );
           scatterPlot1.addyKey( key4 );
     */
     Object[] scatterPlot1Keys = new Object[2];
     scatterPlot1Keys[0] = key1;
     scatterPlot1Keys[1] = new String[] { key2, key3, key4 };
     scatterPlot1.setDataSetKeys(scatterPlot1Keys);

     /*
           scatterPlot2.setxKey( key1 );
           scatterPlot2.addyKey( key3 );
     */
     Object[] scatterPlot2Keys = new Object[2];
     scatterPlot2Keys[0] = key1;
     scatterPlot2Keys[1] = key3;
     scatterPlot2.setDataSetKeys(scatterPlot2Keys);

     /*
           barPlot1.addyKey( key5 );
           barPlot1.addyKey( key6 );
     */
     Object[] barPlot1Keys = new Object[1];
     barPlot1Keys[0] = new String[] { key5, key6 };
     barPlot1.setDataSetKeys(barPlot1Keys);

     barType1.setColor(new Color[]
     {
        Color.blue, Color.red, Color.green, Color.yellow,
     });
     //barType1.setBorderColor(Color.green);
     barType1.setHorizontal(true);
     barType1.setStacked(false);
     barType1.setSpaceBetweenBars(1.2);
     barType1.setSpaceBetweenCategories(2.0);
     barType1.setWidth(new double[] { 0.5 });

     PageType pageType = new PageType();


     //      pageType.setForm( "PDF_READER" );
     pageType.setPDFreader("xpdf");

     //      pageType.setForm( "PDF" );
     String form = System.getProperty("setForm",
                                      "X11");
     pageType.setForm(form);
     pageType.setFilename("MyPlot1.pdf");
     pageType.setDeleteTemporaryFileOnExit(false);
     optionsGlobal.addOption(PAGE_TYPE,
                             pageType);

     legend = new Legend();
     legend.setPosition("R",
                        2.0,
                        0.75,
                        0.75);


     //      legend.setPosition( "R" , 2.0);
     //      legend.setXJustification( 0.75 );
     //      legend.setYJustification( 0.75 );
     //      legend.setX(double);
     //      legend.setY(double);
     //      legend.setBackgroundColor( Color.Green );
     legend.setCharacterExpansion(1.2);
     legend.setNumberColumns(1);
     legend.setHorizontal(false);
     legend.setXInterspacing(1.2);
     legend.setYInterspacing(1.2);

     optionsGlobal.addOption(LEGEND,
                             legend); //
     optionsGlobal.addOption(BAR_TYPE,
                             barType1);

     Text xAxisTextGlobal = new Text();
     xAxisTextGlobal.setTextString("The X Axis Label");
     xAxisTextGlobal.setColor(Color.blue);
     xAxisTextGlobal.setPosition("N",
                                 0.5,
                                 0.4);


     //      xAxisTextGlobal.setPosition( "N" );
     //      xAxisTextGlobal.setXJustification( 0.5 );
     //      xAxisTextGlobal.setYJustification( 0.4 );
     //      xAxisTextGlobal.setX( 0.5 );
     //      xAxisTextGlobal.setY( 0.5 );
     xAxisTextGlobal.setTextExpansion(1.2);
     xAxisTextGlobal.setTextDegreesRotation(0.0);
     xAxisTextGlobal.setTypeface("sans serif");
     xAxisTextGlobal.setStyle("italic");

     AxisNumeric xAxisGlobal = new AxisNumeric();
     xAxisGlobal.setAxisLabelText(xAxisTextGlobal);
//     xAxisGlobal.setDrawOpposingAxis(false);
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
     optionsGlobal.addOption("XAXIS",
                             xAxisGlobal);

     Text yAxisTextGlobal = (Text) xAxisTextGlobal.clone();
     yAxisTextGlobal.setPosition("N",
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
     optionsGlobal.addOption("YAXIS",
                             yAxisGlobal);

     titleText1 = new Text();
     titleText1.setTextString("Scatter Plot 1");
     titleText1.setColor(Color.blue);
     titleText1.setPosition("N",
                            0.5,
                            0.5);


     //      titleText1.setPosition( "N" );
     //      titleText1.setX( 0.5 );
     //      titleText1.setY( 0.5 );
     titleText1.setTextExpansion(1.2);
     titleText1.setTypeface("sans serif");
     titleText1.setStyle("italic");
     optionsLocalPlot1.addOption("PLOT_TITLE",
                                 titleText1);

     Text footerText1 = (Text) titleText1.clone();
     footerText1.setTextString("My Foot");
     optionsLocalPlot1.addOption("PLOT_FOOTER",
                                 footerText1);

/*
     PageType pageType1 = new PageType();
     pageType1.setForm("PDF");
     pageType1.setFilename("MyPlot1.pdf");
     pageType1.setDeleteTemporaryFileOnExit(false);
     optionsLocalPlot1.addOption(PAGE_TYPE,
                                 pageType1);
*/
     PageType pageType2 = new PageType();
     pageType2.setForm("PDF");
     pageType2.setFilename("MyPlot2.pdf");
     pageType2.setDeleteTemporaryFileOnExit(false);
     optionsLocalPlot2.addOption(PAGE_TYPE,
                                 pageType2);

     Text titleText2 = (Text) titleText1.clone();
     titleText2.setTextString("Scatter Plot 2");
     titleText2.setColor(Color.red);
     optionsLocalPlot2.addOption("PLOT_TITLE",
                                 titleText2);

     Text footerText2 = (Text) titleText2.clone();
     footerText2.setTextString("Your Foot");
     optionsLocalPlot2.addOption("PLOT_FOOTER",
                                 footerText2);

     LineType lineType2 = (LineType) lineType1.clone();
     lineType2.setSymbolExpansion(new double[] { 2.5 });
     optionsLocalPlot2.addOption("LINE_TYPE",
                                 lineType2);



      /*
      *                                  dataSets1
      *                                      |
      *                                optionsGlobal
      *                                      |
      *                                  dataSets2
      *                                      |
      *                        -------------------------------------------------------------
      *                        |                         |                                 |
      *                optionsLocalPlot1         optionsLocalPlot2                         |
      *                        |                         |                                 |
      *                      page1                      page2                            page3
      *                        |                         |                                 |
      *                   ScatterPlot1              ScatterPlot2                        BarPlot1
      *
      *
      *
      */

/*
      // base of tree
      dataSets1.add(optionsGlobal);
      optionsGlobal.add(dataSets2);


      // page1 branch
      dataSets2.add(optionsLocalPlot1);
      optionsLocalPlot1.add(page1);
      page1.add(scatterPlot1);

      PageType pageType1 = new PageType();
      pageType1.setForm("SCREEN");
      pageType1.setFilename("MyPlot1.pdf");
      pageType1.setDeleteTemporaryFileOnExit(false);
      optionsLocalPlot1.addOption(PAGE_TYPE,
                                  pageType1);


      // page2 branch
      dataSets2.add(optionsLocalPlot2);
      optionsLocalPlot2.add(page2);
      page2.add(scatterPlot2);
      optionsLocalPlot2.addOption("WORLD_COORDINATES",
                                  new double[] { -6.5, 5.0, -4.5, 7.0 }); //

      PageType pageType2 = new PageType();
      pageType2.setForm("SCREEN");
      pageType2.setFilename("MyPlot2.pdf");
      pageType2.setDeleteTemporaryFileOnExit(false);
      optionsLocalPlot2.addOption(PAGE_TYPE,
                                  pageType2);

      // page3 branch
      PageType pageType3 = new PageType();
      pageType3.setForm("SCREEN");
      pageType3.setFilename("MyPlot3.pdf");
      pageType3.setDeleteTemporaryFileOnExit(false);
      optionsLocalPlot3.addOption(PAGE_TYPE,
                                  pageType3);

      Text xAxisTextBarPlot = new Text();
      xAxisTextBarPlot.setTextString("The Axis Label");
      xAxisTextBarPlot.setColor(Color.blue);
      xAxisTextBarPlot.setPosition("N",
                                   0.5,
                                   0.5);


      //      xAxisTextBarPlot.setPosition( "N" );
      //      xAxisTextBarPlot.setX( 0.5 );
      //      xAxisTextBarPlot.setY( 0.5 );
      xAxisTextBarPlot.setTextExpansion(1.2);
      xAxisTextBarPlot.setTypeface("sans serif");
      xAxisTextBarPlot.setStyle("italic");

      Axis xBarPlotAxis = new Axis();
      xBarPlotAxis.setAxisLabelText(xAxisTextBarPlot);
      xBarPlotAxis.setDrawOpposingAxis(false);
      xBarPlotAxis.setDrawAxis(true);
      xBarPlotAxis.setDrawTickMarks(true);
      xBarPlotAxis.setDrawTickMarkLabels(true);
      xBarPlotAxis.setDrawTickMarkLabelsPerpendicularToAxis(
            false);


      //    xBarPlotAxis.setLinesIntoMargin(double);
      //    xBarPlotAxis.setPosition(double);
      xBarPlotAxis.setAxisColor(Color.blue);
      xBarPlotAxis.setTickMarkLabelColor(Color.red);
      xBarPlotAxis.setTickMarkFont("italic");
      xBarPlotAxis.setTickMarkLabelExpansion(1.1);


      //    xBarPlotAxis.setUserTickMarkPositions( double[] );
      //    xBarPlotAxis.setUserTickMarkLabels( new String[]{ "Test1","Test2"} );
      optionsLocalPlot3.addOption("XAXIS",
                                  xBarPlotAxis);

      Axis yAxisBarChart = (Axis) yAxisGlobal.clone();
      yAxisBarChart.setDrawTickMarkLabelsPerpendicularToAxis(
            false);
      optionsLocalPlot3.addOption("YAXIS",
                                  yAxisBarChart);

      dataSets2.add(page3);
      page3.add(optionsLocalPlot3);
      optionsLocalPlot3.add(barPlot1);

      Text titleText3 = (Text) titleText1.clone();
      titleText3.setTextString("Bar Plot Test");
      optionsLocalPlot3.addOption("PLOT_TITLE",
                                  titleText3);

      */

   }

   public void constructTree(String guiName)
   {
     if (guiName.equals("BarType"))
     {
       // base of tree
       dataSets1.add(optionsGlobal);
       optionsGlobal.add(dataSets2);

       // page3 branch
       PageType pageType3 = new PageType();
       pageType3.setForm("SCREEN");
       pageType3.setFilename("MyPlot3.pdf");
       pageType3.setDeleteTemporaryFileOnExit(false);
       optionsLocalPlot3.addOption(PAGE_TYPE,
                                   pageType3);

       Text xAxisTextBarPlot = new Text();
       xAxisTextBarPlot.setTextString("The Axis Label");
       xAxisTextBarPlot.setColor(Color.blue);
       xAxisTextBarPlot.setPosition("N",
                                    0.5,
                                    0.5);


       //      xAxisTextBarPlot.setPosition( "N" );
       //      xAxisTextBarPlot.setX( 0.5 );
       //      xAxisTextBarPlot.setY( 0.5 );
       xAxisTextBarPlot.setTextExpansion(1.2);
       xAxisTextBarPlot.setTypeface("sans serif");
       xAxisTextBarPlot.setStyle("italic");

       AxisCategory xBarPlotAxis = new AxisCategory();
       xBarPlotAxis.setAxisLabelText(xAxisTextBarPlot);
//       xBarPlotAxis.setDrawOpposingAxis(false);
       xBarPlotAxis.setEnableAxis(true);
       xBarPlotAxis.setDrawTickMarks(true);
       xBarPlotAxis.setDrawTickMarkLabels(true);
       xBarPlotAxis.setDrawTickMarkLabelsPerpendicularToAxis(
             false);


       //    xBarPlotAxis.setLinesIntoMargin(double);
       //    xBarPlotAxis.setPosition(double);
       xBarPlotAxis.setAxisColor(Color.blue);
       xBarPlotAxis.setTickMarkLabelColor(Color.red);
       xBarPlotAxis.setTickMarkFont("italic");
       xBarPlotAxis.setTickMarkLabelExpansion(1.1);


       //    xBarPlotAxis.setUserTickMarkPositions( double[] );
       //    xBarPlotAxis.setUserTickMarkLabels( new String[]{ "Test1","Test2"} );
       optionsLocalPlot3.addOption("XAXIS",
                                   xBarPlotAxis);

       BarType localBarType = (BarType) barType1.clone();
       //BarType localBarType = new BarType();
       BarTypeEditor barTypeEditor = new BarTypeEditor(localBarType);
       optionsLocalPlot3.addOption(BAR_TYPE,
                               localBarType);


/*
       Axis yAxisBarChart = (Axis) yAxisGlobal.clone();
       yAxisBarChart.setDrawTickMarkLabelsPerpendicularToAxis(
             false);
       optionsLocalPlot3.addOption("YAXIS",
                                   yAxisBarChart);
*/
       dataSets2.add(page3);
       page3.add(optionsLocalPlot3);
       optionsLocalPlot3.add(barPlot1);

/*
       Text titleText3 = (Text) titleText1.clone();
       titleText3.setTextString("Bar Plot Test");
       optionsLocalPlot3.addOption("PLOT_TITLE",
                                   titleText3);
*/

     }//if (guiName.equals("BarType"))
     else if (guiName.equals("GridType"))
     {
       // base of tree
       dataSets1.add(optionsGlobal);
       optionsGlobal.add(dataSets2);


       // page1 branch
       dataSets2.add(optionsLocalPlot1);
       optionsLocalPlot1.add(page1);
       page1.add(scatterPlot1);

       PageType pageType1 = new PageType();
       pageType1.setForm("SCREEN");
       pageType1.setFilename("MyPlot1.pdf");
       pageType1.setDeleteTemporaryFileOnExit(false);
       optionsLocalPlot1.addOption(PAGE_TYPE,
                                   pageType1);

       //GridType localGridType = new GridType();
       GridType localGridType = (GridType) gridType.clone();
       GridTypeEditor gridTypeEditor = new GridTypeEditor(localGridType);

       optionsLocalPlot1.addOption("GRID_TYPE",
                               localGridType);
     }
     else if (guiName.equals("Legend"))
     {
       // base of tree
       dataSets1.add(optionsGlobal);
       optionsGlobal.add(dataSets2);

       PageType pageType1 = new PageType();
       pageType1.setForm("SCREEN");
       pageType1.setFilename("MyPlot1.pdf");
       pageType1.setDeleteTemporaryFileOnExit(false);
       optionsLocalPlot1.addOption(PAGE_TYPE,
                                   pageType1);

       Legend localLegend = (Legend) legend.clone();
       //Legend localLegend = new Legend();
       LegendEditor legendEditor = new LegendEditor(localLegend);
       //optionsLocalPlot1.addOption(LEGEND,
       //                        legend); //
       optionsGlobal.addOption(LEGEND, localLegend);

       // page1 branch
       dataSets2.add(optionsLocalPlot1);
       optionsLocalPlot1.add(page1);
       page1.add(scatterPlot1);
     }
     else if (guiName.equals("LineType"))
     {
       // base of tree
       dataSets1.add(optionsGlobal);
       optionsGlobal.add(dataSets2);

       PageType pageType1 = new PageType();
       pageType1.setForm("SCREEN");
       pageType1.setFilename("MyPlot1.pdf");
       pageType1.setDeleteTemporaryFileOnExit(false);
       optionsLocalPlot1.addOption(PAGE_TYPE,
                                   pageType1);

       LineType localLineType = new LineType();


       Color[] colors = new Color[]
       {
          Color.blue, Color.red, Color.yellow, Color.green, Color.orange,
          Color.pink
       };

       String[] lineTypes = new String[]
       {
          "SOLID", "DASHED", "DOTTED", "DOTDASH", "LONGDASH", "TWODASH"
       };

       double[] lineWidths = new double[] { 2.5, 3.3 };

       String[] symbols = new String[] { "TRIANGLE_UP", "CIRCLE", "TRIANGLE_UP" };

       localLineType.setPlotStyle(LineType.LINES);
       localLineType.setColor(colors);
       localLineType.setLineStyle(lineTypes);
       localLineType.setLineWidth(lineWidths);
       localLineType.setSymbol(symbols);
       localLineType.setSymbolExpansion(new double[] { 1.5, 2.5 });

       LineTypeEditor lineEditor = new LineTypeEditor(localLineType);
       optionsLocalPlot1.addOption(LINE_TYPE, localLineType);

       // page1 branch
       dataSets2.add(optionsLocalPlot1);
       optionsLocalPlot1.add(page1);
       page1.add(scatterPlot1);
     }
     else if (guiName.equals("OutlineType"))
     {
       // base of tree
       dataSets1.add(optionsGlobal);
       optionsGlobal.add(dataSets2);

       PageType pageType1 = new PageType();
       pageType1.setForm("SCREEN");
       pageType1.setFilename("MyPlot1.pdf");
       pageType1.setDeleteTemporaryFileOnExit(false);
       optionsLocalPlot1.addOption(PAGE_TYPE,
                                   pageType1);

       OutlineType localOutlineType = new OutlineType();
       OutlineTypeEditor outlineEditor = new OutlineTypeEditor(localOutlineType);

       optionsLocalPlot1.addOption(OUTLINE_TYPE, localOutlineType);

       // page1 branch
       dataSets2.add(optionsLocalPlot1);
       optionsLocalPlot1.add(page1);
       page1.add(scatterPlot1);

     }
     else if (guiName.equals("Text"))
     {
       // base of tree
       dataSets1.add(optionsGlobal);
       optionsGlobal.add(dataSets2);

       // page3 branch
       PageType pageType3 = new PageType();
       pageType3.setForm("SCREEN");
       pageType3.setFilename("MyPlot3.pdf");
       pageType3.setDeleteTemporaryFileOnExit(false);
       optionsLocalPlot3.addOption(PAGE_TYPE,
                                   pageType3);

       Text titleText3 = (Text) titleText1.clone();
       titleText3.setTextString("Bar Plot Test");
       TextEditor textEditor = new TextEditor(titleText3);
       optionsLocalPlot3.addOption("PLOT_TITLE",
                                   titleText3);

       dataSets2.add(page3);
       page3.add(optionsLocalPlot3);
       optionsLocalPlot3.add(barPlot1);
     }
     else if (guiName.equals("PageType"))
     {
       // base of tree
       dataSets1.add(optionsGlobal);
       optionsGlobal.add(dataSets2);

       // page3 branch
       PageType pageType3 = new PageType();
       pageType3.setForm("PDF");
       pageType3.setFilename("MyPlot3.pdf");
       pageType3.setDeleteTemporaryFileOnExit(true);
       PageTypeEditor pageEditor = new PageTypeEditor(pageType3);
       optionsLocalPlot3.addOption(PAGE_TYPE,
                                   pageType3);

       dataSets2.add(page3);
       page3.add(optionsLocalPlot3);
       optionsLocalPlot3.add(barPlot1);
     }
   }

   public void generatePlots() throws Exception
   {
     //====================================================================
     //
     // GENERATE THE PLOTS
     //
     //====================================================================
     String Rexec = System.getProperty("os.name").startsWith(
                          "Windows") ? "RTerm.exe" : "R";
     RGenerator rGen = new RGenerator(Rexec);

     try
     {
        rGen.setLog(new File("myLogFile.txt"));
        rGen.execute(dataSets1);
     }
     catch (IOException ioe)
     {
        ioe.printStackTrace();
     }
     catch(Exception e)
     {
       e.printStackTrace();
     }


     //      RCommunicator.getInstance().closePlotWindows(2000);
     //      RCommunicator.getInstance().terminate();

   }

   /**
    * DOCUMENT_ME
    *
    * @param args DOCUMENT_ME
    ********************************************************/
   public static void main(String[] args)
   {
      DefaultUserInteractor.set(new GUIUserInteractor());
      String guiName = args[0];
      try
      {
        GUITestInvoker guiTest = new GUITestInvoker();
        guiTest.constructTree(guiName);
        guiTest.generatePlots();
        System.exit(1);
      }
      catch(Exception e)
      {
        e.printStackTrace();
        System.exit(1);
      }
   }

}

