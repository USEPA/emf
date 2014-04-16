package gov.epa.mims.analysisengine.rcommunicator;

import gov.epa.mims.analysisengine.AnalysisEngineConstants;
import gov.epa.mims.analysisengine.tree.Axis;
import gov.epa.mims.analysisengine.tree.AxisTime;
import gov.epa.mims.analysisengine.tree.AxisNumeric;
import gov.epa.mims.analysisengine.tree.AxisCategory;
import gov.epa.mims.analysisengine.tree.BoxPlot;
import gov.epa.mims.analysisengine.tree.BoxType;
import gov.epa.mims.analysisengine.tree.TornadoPlot;
import gov.epa.mims.analysisengine.tree.TornadoType;
import gov.epa.mims.analysisengine.tree.BarPlot;
import gov.epa.mims.analysisengine.tree.BarType;
import gov.epa.mims.analysisengine.tree.DiscreteCategoryPlot;
import gov.epa.mims.analysisengine.tree.HistogramPlot;
import gov.epa.mims.analysisengine.tree.HistogramType;
import gov.epa.mims.analysisengine.tree.LineType;
import gov.epa.mims.analysisengine.tree.Node;
import gov.epa.mims.analysisengine.tree.Page;
import gov.epa.mims.analysisengine.tree.PageType;
import gov.epa.mims.analysisengine.tree.Plot;
import gov.epa.mims.analysisengine.tree.RankOrderPlot;
import gov.epa.mims.analysisengine.tree.ScatterPlot;
import gov.epa.mims.analysisengine.tree.CdfPlot;
import gov.epa.mims.analysisengine.tree.SortType;
import gov.epa.mims.analysisengine.tree.Text;
import gov.epa.mims.analysisengine.tree.TextBoxesType;
import gov.epa.mims.analysisengine.tree.TimeSeries;
import gov.epa.mims.analysisengine.tree.LinearRegressionType;

import java.awt.Color;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;


/**
 * DOCUMENT ME!
 *
 * @version $Revision: 1.2 $
 * @author $author$
 */
public class Option
implements Serializable, Cloneable,
gov.epa.mims.analysisengine.tree.AnalysisOptionConstantsIfc,
gov.epa.mims.analysisengine.tree.LineTypeConstantsIfc,
gov.epa.mims.analysisengine.tree.FontConstantsIfc
{
   /** DOCUMENT ME! */
   static final long serialVersionUID = 1;

   public static TextBoxesType getTextBoxes(Plot p)
   {
System.out.println("Option.TextBoxesType getTextBoxes(Plot p)");
      String optionConstant = TEXT_BOXES;
      TextBoxesType o = (TextBoxesType) p.getOption(optionConstant);

      if (o == null)
      {
         o = new TextBoxesType();
      }

      return o;
   }

   /**
    * DOCUMENT_ME
    *
    * @param p DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   public static PageType getPageType(Node p)
   {
      String optionConstant = PAGE_TYPE;
      PageType o = (PageType) p.getOption(optionConstant);

      if (o == null)
      {
         o = new PageType();
         o.setPDFreader(null);
         o.setForm(Page.SCREEN);
         o.setLayout(null);
         o.setFilename(null);


         //REMEMBER TO SET THIS BACK TO true
         o.setDeleteTemporaryFileOnExit(false);
      }

      return o;
   }

   /**
    * DOCUMENT_ME
    *
    * @param p DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   public static SortType getSortType(RankOrderPlot p)
   {
      String optionConstant = SORT_TYPE;
      SortType o = (SortType) p.getOption(optionConstant);

      if (o == null)
      {
         o = new SortType();
         o.setMissingData(SortType.ENDING);
         o.setSortMethod(SortType.SHELL);
         o.setAscending(true);
         o.setEnable(true);
      }

      return o;
   }

   /**
    * DOCUMENT_ME
    *
    * @param p DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   public static HistogramType getHistogramType(HistogramPlot p)
   {
      String optionConstant = HISTOGRAM_TYPE;
      HistogramType o = (HistogramType) p.getOption(optionConstant);

      if (o == null)
      {
         o = new HistogramType();
         o.setColor(Color.green);
         o.setBorderColor(Color.black);
         o.setFrequency(true);
         o.setShadingAngle(null);
         o.setShadingDensity(null);
         o.setLinetype(HistogramType.SOLID);
         o.setXRange(null, null);
         o.setLabelsOn(false);
      }

      return o;
   }

   /**
    * DOCUMENT_ME
    *
    * @param p DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   public static BoxType getBoxType(BoxPlot p)
   {
      String optionConstant = BOX_TYPE;
      BoxType o = (BoxType) p.getOption(optionConstant);

      if (o == null)
      {
         o = new BoxType();
         o.setProcessing(BoxType.USE_R);
      }

      return o;
   }

   public static TornadoType getTornadoType(TornadoPlot p)
   {
      String optionConstant = TORNADO_TYPE;
      TornadoType o = (TornadoType) p.getOption(optionConstant);
      return o;
   }

   /**
    * DOCUMENT_ME
    *
    * @param p DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   public static BarType getBarType(BarPlot p)
   {
      String optionConstant = BAR_TYPE;
      BarType o = (BarType) p.getOption(optionConstant);

      if (o == null)
      {
         o = new BarType();
         o.setColor(getCyclicColors());
         o.setWidth(new double[]
         {
            1.0,
            1.0
         });
         o.setSpaceBetweenBars(0.5);
         o.setSpaceBetweenCategories(2.0);
         o.setStacked(false);
         o.setHorizontal(false);
         o.setBorderColor(new Color[]{Color.red});
         o.setCategoriesSpanDataSets(false);
      }

      return o;
   }

   public static LinearRegressionType getLinearRegressionType(ScatterPlot p)
   {
      String optionConstant = LINEAR_REGRESSIONS;
      LinearRegressionType lt = (LinearRegressionType) p.getOption(optionConstant);
      if( lt == null )
      {
         lt = new LinearRegressionType();
      }
      return lt;
   }

   /**
    * DOCUMENT_ME
    *
    * @param p DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   public static LineType getLineType(Plot p)
   {
      String optionConstant = LINE_TYPE;
      LineType lt = (LineType) p.getOption(optionConstant);

      if (lt == null)
      {
         LineType o = new LineType();

         ArrayList colorList = new ArrayList();
         colorList.add(Color.red);
         colorList.add(Color.yellow);
         colorList.add(Color.blue);
         colorList.add(Color.cyan);
         colorList.add(Color.green);
         colorList.add(Color.magenta);
         colorList.add(Color.orange);
         colorList.add(Color.pink);
         colorList.add(Color.lightGray);
         colorList.add(Color.gray);
         colorList.add(Color.black);

         Color[] colors = new Color[colorList.size()];
         colorList.toArray(colors);
         o.setColor(colors);

         o.setPlotStyle(LineType.LINES);

         ArrayList l = new ArrayList();
         l.add(LineType.SOLID);
         l.add(LineType.DASHED);
         l.add(LineType.DOTTED);
         l.add(LineType.DOTDASH);
         l.add(LineType.LONGDASH);
         l.add(LineType.TWODASH);

         String[] strings = new String[l.size()];
         l.toArray(strings);
         o.setLineStyle(strings);

         o.setLineWidth(new double[]
         {
            1.0
         });

         l.clear();
         l.add(LineType.CIRCLE);
         l.add(LineType.TRIANGLE_UP);
         l.add(LineType.PLUS);
         l.add(LineType.CROSS);
         l.add(LineType.SQUARE_ROTATED);
         l.add(LineType.TRIANGLE_DOWN);
         l.add(LineType.CROSS_IN_SQUARE);
         l.add(LineType.STARBURST);
         l.add(LineType.PLUS_IN_SQUARE_ROTATED);
         l.add(LineType.PLUS_IN_CIRCLE);
         l.add(LineType.TRIANGLE_UP_AND_DOWN);
         l.add(LineType.PLUS_IN_SQUARE);
         l.add(LineType.CIRCLE_AND_CROSS);
         l.add(LineType.UP_TRIANGLE_IN_SQUARE);
         l.add(LineType.SQUARE_SOLID);
         l.add(LineType.CIRCLE_SOLID);
         l.add(LineType.UP_TRIANGLE_SOLID);
         l.add(LineType.SQUARE_ROTATED_SOLID);
         l.add(LineType.CIRCLE_FILLED);
         l.add(LineType.BULLET);
         l.add(LineType.CIRCLE2);
         l.add(LineType.SQUARE);
         l.add(LineType.DIAMOND);
         strings = new String[l.size()];
         l.toArray(strings);

         o.setSymbol(strings);

         o.setSymbolExpansion(new double[]
         {
            1.0
         });

         lt = o;
      }

      return lt;
   }


//   /**
//    * DOCUMENT_ME
//    *
//    * @param p DOCUMENT_ME
//    *
//    * @return DOCUMENT_ME
//    ********************************************************/
//   public static Axis getXAxis(TimeSeries p)
//   {
//      String optionConstant = XAXIS;
//      Axis axisObj = (Axis) p.getOption(optionConstant);
//
//      if (axisObj == null)
//      {
//         TimeSeriesAxis o = new TimeSeriesAxis();
//         o.setAxisLabelText(null);
//         o.setDrawOpposingAxis(false);
//         o.setDrawAxis(true);
//         o.setDrawTickMarks(true);
//         o.setDrawTickMarkLabels(true);
//         o.setDrawTickMarkLabelsPerpendicularToAxis(false);
//         o.setLinesIntoMargin(0.0);
//
//
//         //o.setPosition(double);
//         o.setAxisColor(java.awt.Color.black);
//         o.setTickMarkLabelColor(java.awt.Color.black);
//         o.setTickMarkFont(ITALIC_TEXT);
//         o.setTickMarkLabelExpansion(1.0);
//
//
//         //o.setUserTickMarkPositions([D);
//         //o.setUserTickMarkLabels([Ljava.lang.String;);
//         o.setTimeZone(null);
//         o.setTickIncrement(-1);
//         o.setFirstTickMark(null);
//         o.setTickLabelFormat(null);
//         o.setConstantTimeLabelFormat(null);
//         axisObj = o;
//      }
//
//      return axisObj;
//   }

   /**
    * DOCUMENT_ME
    *
    * @param p DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   public static AxisTime getXAxis(TimeSeries p)
   {
      String optionConstant = X_TIME_AXIS;
      AxisTime axis = (AxisTime) p.getOption(optionConstant);

      if (axis == null)
      {
         axis = getDefaultAxisTime();
      }
      return axis;
   }

   /**
    * DOCUMENT_ME
    *
    * @param p DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   public static AxisNumeric getXAxis(RankOrderPlot p)
   {
      String optionConstant = X_NUMERIC_AXIS;
      AxisNumeric o = (AxisNumeric) p.getOption(optionConstant);

      if (o == null)
      {
         o = getDefaultAxisNumeric();
      }

      return o;
   }

   /**
    * DOCUMENT_ME
    *
    * @param p DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   public static AxisNumeric getXAxis(ScatterPlot p)
   {
      String optionConstant = X_NUMERIC_AXIS;
      AxisNumeric o = (AxisNumeric) p.getOption(optionConstant);

      if (o == null)
      {
         o = getDefaultAxisNumeric();
      }

      return o;
   }

   /**
    * DOCUMENT_ME
    *
    * @param p DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   public static AxisNumeric getXAxis(CdfPlot p)
   {
      String optionConstant = X_NUMERIC_AXIS;
      AxisNumeric o = (AxisNumeric) p.getOption(optionConstant);

      if (o == null)
      {
         o = getDefaultAxisNumeric();
      }

      return o;
   }

   /**
    * DOCUMENT_ME
    *
    * @param p DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   public static AxisCategory getXAxis(DiscreteCategoryPlot p)
   {
      String optionConstant = CATEGORY_AXIS;
      AxisCategory o = (AxisCategory)  p.getOption(optionConstant);

      if (o == null)
      {
         o = getDefaultAxisCategory();
      }

      return o;
   }

   /**
    * DOCUMENT_ME
    *
    * @param p DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   public static AxisCategory getXAxis(BoxPlot p)
   {
      String optionConstant = CATEGORY_AXIS;
      AxisCategory o = (AxisCategory) p.getOption(optionConstant);

      if (o == null)
      {
         o = getDefaultAxisCategory();
      }

      return o;
   }

   /**
    * DOCUMENT_ME
    *
    * @param p DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   public static AxisCategory getXAxis(BarPlot p)
   {
      String optionConstant = CATEGORY_AXIS;
      AxisCategory o = (AxisCategory) p.getOption(optionConstant);

      if (o == null)
      {
         o = getDefaultAxisCategory();
      }

      return o;
   }

   /**
    * DOCUMENT_ME
    *
    * @param p DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   public static AxisNumeric getXAxis(HistogramPlot p)
   {
      String optionConstant = X_NUMERIC_AXIS;
      AxisNumeric o = (AxisNumeric) p.getOption(optionConstant);

      if (o == null)
      {
         o = getDefaultAxisNumeric();
      }

      return o;
   }

   /**
    * DOCUMENT_ME
    *
    * @param p DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   public static AxisNumeric getYAxis(TimeSeries p)
   {
      String optionConstant = Y_NUMERIC_AXIS;
      AxisNumeric o = (AxisNumeric) p.getOption(optionConstant);

      if (o == null)
      {
         o = getDefaultAxisNumeric();
      }

      return o;
   }

   /**
    * DOCUMENT_ME
    *
    * @param p DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   public static AxisNumeric getYAxis(BoxPlot p)
   {
      String optionConstant = NUMERIC_AXIS;
      AxisNumeric o = (AxisNumeric) p.getOption(optionConstant);

      if (o == null)
      {
         o = getDefaultAxisNumeric();
      }

      return o;
   }

   /**
    * DOCUMENT_ME
    *
    * @param p DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   public static AxisNumeric getYAxis(BarPlot p)
   {
      String optionConstant = NUMERIC_AXIS;
      AxisNumeric o = (AxisNumeric) p.getOption(optionConstant);

      if (o == null)
      {
         o = getDefaultAxisNumeric();
      }

      return o;
   }

   /**
    * DOCUMENT_ME
    *
    * @param p DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   public static AxisNumeric getXAxis(TornadoPlot p)
   {
      String optionConstant = NUMERIC_AXIS;
      AxisNumeric o = (AxisNumeric) p.getOption(optionConstant);

      if (o == null)
      {
         o = getDefaultAxisNumeric();
      }

      return o;
   }

   /**
    * DOCUMENT_ME
    *
    * @param p DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   public static AxisNumeric getYAxis(DiscreteCategoryPlot p)
   {
      String optionConstant = Y_NUMERIC_AXIS;
      AxisNumeric o = (AxisNumeric) p.getOption(optionConstant);

      if (o == null)
      {
         o = getDefaultAxisNumeric();
      }

      return o;
   }

   /**
    * DOCUMENT_ME
    *
    * @param p DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   public static AxisNumeric getYAxis(RankOrderPlot p)
   {
      String optionConstant = Y_NUMERIC_AXIS;
      AxisNumeric o = (AxisNumeric) p.getOption(optionConstant);

      if (o == null)
      {
         o = getDefaultAxisNumeric();
      }

      return o;
   }

   /**
    * DOCUMENT_ME
    *
    * @param p DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   public static AxisNumeric getYAxis(ScatterPlot p)
   {
      String optionConstant = Y_NUMERIC_AXIS;
      AxisNumeric o = (AxisNumeric) p.getOption(optionConstant);

      if (o == null)
      {
         o = getDefaultAxisNumeric();
      }

      return o;
   }

   /**
    * DOCUMENT_ME
    *
    * @param p DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   public static AxisNumeric getYAxis(CdfPlot p)
   {
      String optionConstant = Y_NUMERIC_AXIS;
      AxisNumeric o = (AxisNumeric) p.getOption(optionConstant);

      if (o == null)
      {
         o = getDefaultAxisNumeric();
      }

      return o;
   }

   /**
    * DOCUMENT_ME
    *
    * @param p DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   public static AxisNumeric getYAxis(HistogramPlot p)
   {
      String optionConstant = Y_NUMERIC_AXIS;
      AxisNumeric o = (AxisNumeric) p.getOption(optionConstant);

      if (o == null)
      {
         o = getDefaultAxisNumeric();
      }

      return o;
   }

   /**
    * DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   public static Color[] getCyclicColors()
   {
      ArrayList colorList = new ArrayList();
      colorList.add(Color.black);
      colorList.add(Color.blue);
      colorList.add(Color.cyan);
      colorList.add(Color.green);
      colorList.add(Color.lightGray);
      colorList.add(Color.magenta);
      colorList.add(Color.orange);
      colorList.add(Color.pink);
      colorList.add(Color.red);
      colorList.add(Color.yellow);
      colorList.add(Color.gray);

      Color[] colors = new Color[colorList.size()];
      colorList.toArray(colors);

      return colors;
   }

   public static AxisTime getDefaultAxisTime()
   {
      AxisTime o = new AxisTime();
      Text txt = new Text();
      txt.setTextString("Time");
      o.setAxisLabelText(txt);
      o.setEnableAxis(true);
      o.setDrawTickMarks(true);
      o.setDrawTickMarkLabels(true);
      o.setDrawTickMarkLabelsPerpendicularToAxis(false);
      //o.setPosition(int, double);
      o.setAxisColor(java.awt.Color.black);
      o.setTickMarkLabelColor(java.awt.Color.red);
      o.setTickMarkFont(ITALIC_TEXT);
      o.setTickMarkLabelExpansion(1.0);
      //o.setUserTickMarkPositions([D);
      //o.setUserTickMarkLabels([Ljava.lang.String;);
      //o.setGridlineStyle(java.lang.String);
      //o.setGridTickmarkLength(double);
      //o.setGridlineWidth(java.lang.Double);
      //o.setGridTickmarkEnable(boolean);
      o.setGridEnable(false);
      //o.setGridColor(java.awt.Color);
      o.setTimeZone(null);
      o.setTickIncrement(-1);
      o.setFirstTickMark(null);
      o.setTickLabelFormat(null);
      o.setConstantTimeLabelFormat(null);
      //o.setGrid();
      //o.setGrid(java.util.Date, java.lang.Long);
      //o.setGrid(java.util.Date, java.lang.Long, java.util.Date);
      //o.setGrid(java.util.Date, java.lang.Integer, java.util.Date);
      //o.setAxisRange(java.lang.Object, java.lang.Object);

      return o;
   }

   public static AxisCategory getDefaultAxisCategory()
   {
      AxisCategory o = new AxisCategory();

      Text txt = new Text();
      txt.setTextString("Categories");
      o.setAxisLabelText(txt);
      o.setEnableAxis(true);
      o.setDrawTickMarks(true);
      o.setDrawTickMarkLabels(true);
      o.setDrawTickMarkLabelsPerpendicularToAxis(false);
      //o.setPosition(int, double);
      o.setAxisColor(java.awt.Color.black);
      o.setTickMarkLabelColor(java.awt.Color.black);
      o.setTickMarkFont(ITALIC_TEXT);
      o.setTickMarkLabelExpansion(1.0);
      //o.setUserTickMarkPositions([D);
      //o.setUserTickMarkLabels([Ljava.lang.String;);
      //o.setAxisRange(java.lang.Object, java.lang.Object);

      return o;
   }

   public static AxisNumeric getDefaultAxisNumeric()
   {
      AxisNumeric o = new AxisNumeric();

      Text txt = new Text();
      txt.setTextString("");
      o.setAxisLabelText(txt);
      o.setEnableAxis(true);
      o.setDrawTickMarks(true);
      o.setDrawTickMarkLabels(true);
      o.setDrawTickMarkLabelsPerpendicularToAxis(false);
      //o.setPosition(int, double);
      o.setAxisColor(java.awt.Color.black);
      o.setTickMarkLabelColor(java.awt.Color.black);
      o.setTickMarkFont(ITALIC_TEXT);
      o.setTickMarkLabelExpansion(1.0);
      //o.setUserTickMarkPositions([D);
      //o.setUserTickMarkLabels([Ljava.lang.String;);
      o.setGridlineStyle(SOLID);
      o.setGridTickmarkLength(1.0);
      o.setGridlineWidth(1.0);
      o.setGridTickmarkEnable(false);
      o.setGridEnable(false);
      o.setGridColor(java.awt.Color.black);
      //o.setAxisRange(java.lang.Object, java.lang.Object);
      //o.setReferenceLineAdd(gov.epa.mims.analysisengine.tree.ReferenceLine);
      //o.setGrid();
      //o.setGrid(java.lang.Double, java.lang.Double);
      //o.setGrid(java.lang.Double, java.lang.Double, java.lang.Double);
      o.setLogScale(false);

      return o;
   }
}
