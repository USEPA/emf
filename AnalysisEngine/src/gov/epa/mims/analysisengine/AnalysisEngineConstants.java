package gov.epa.mims.analysisengine;


/**
 * global constants used in the analysisengine code
 * @version $Revision: 1.3 $
 * @author Tommy E. Cathey
 */
public class AnalysisEngineConstants
{
   
   /** plot name */
   public static final String BOX_PLOT = "Box Plot";

   /** plot name */
   public static final String BAR_PLOT = "Bar Plot";

  /** plot name */
   public static final String TORNADO_PLOT = "Tornado Plot";

   /** plot name */
   public static final String DISCRETE_CATEGORY_PLOT = "Discrete Category Plot";
 
   /** plot name */
   public static final String HISTOGRAM_PLOT = "Histogram Plot";

   /** plot name */
   public static final String RANK_ORDER_PLOT = "Rank Order Plot";

   /** plot name */
   public static final String CDF_PLOT = "CDF Plot";

   /** plot name */
   public static final String SCATTER_PLOT = "Scatter Plot";

   /** plot name */
   public static final String TIME_SERIES_PLOT = "Time Series Plot";
   
   public static String[] getNonLabeledPlotTypes()
   {
      String [] plotNames = {SCATTER_PLOT,RANK_ORDER_PLOT,TORNADO_PLOT,BOX_PLOT,
         CDF_PLOT,HISTOGRAM_PLOT};
      return plotNames;
   }
   
   public static String[] getLabeledPlotTypes()
   {
      String [] plotNames = {BAR_PLOT,DISCRETE_CATEGORY_PLOT};
      return plotNames;
   }

   /** array of plots as classes */
   public static final Class[] PLOT_CLASSES = 
   {
/*
     ScatterPlot.class, 
      BarPlot.class, 
      BoxPlot.class, 
      TimeSeries.class, 
      DiscreteCategoryPlot.class, 
      HistogramPlot.class, 
      RankOrderPlot.class
*/
   };

   /** array of plot names */
   public static final String[] PLOT_NAMES = 
   {
      SCATTER_PLOT, 
      TORNADO_PLOT, 
      BAR_PLOT, 
      BOX_PLOT, 
      TIME_SERIES_PLOT, 
      DISCRETE_CATEGORY_PLOT, 
      HISTOGRAM_PLOT, 
      RANK_ORDER_PLOT,
      CDF_PLOT
   };

   /** max time allowed to start the child process */
   public static final int MAX_TIME_TO_START_CHILD = 10000;

   /** sleep slice */
   public static final int SLEEP_SLICE = 3;

   /** low x value for mapping time data onto the X axis */
   public static final int TIME_SERIES_USER_COORD1 = 0;

   /** max x value for mapping time data onto the X axis */
   public static final int TIME_SERIES_USER_COORD2 = 10000;

   /** parameter used to manipulate and format time series labels */
   public static final int TIME_SERIES_DEF_MAX_TICKS = 5;

   /** parameter used to manipulate and format time series labels */
   public static final int TIME_SERIES_DEF_MIN_TICKS = 5;

   /** parameter used to manipulate and format time series labels */
   public static final int TIME_SERIES_ROUND_YEARS = 5;

   /** parameter used to manipulate and format time series labels */
   public static final int TIME_SERIES_ROUND_MONTHS = 3;

   /** parameter used to manipulate and format time series labels */
   public static final int TIME_SERIES_ROUND_DAYS = 5;

   /** parameter used to manipulate and format time series labels */
   public static final int TIME_SERIES_ROUND_HOURS = 5;

   /** parameter used to manipulate and format time series labels */
   public static final int TIME_SERIES_ROUND_MINUTES = 6;

   /** parameter used to manipulate and format time series labels */
   public static final int TIME_SERIES_ROUND_SECONDS = 6;

   /** parameter used to manipulate and format time series labels */
   public static final int TIME_SERIES_ROUND_MILLISECONDS = 5;

   /** parameter used to manipulate and format time series labels */
   public static final int[][] TIME_SERIES_YEAR_STEPS = 
         
   {
      {
         1, 
         5
      }, 

     {
         5, 
         5
      }, 
      {
         10, 
         5
      }, 
   };

   /** parameter used to manipulate and format time series labels */
   public static final int[][] TIME_SERIES_MONTH_STEPS = 
         
   {
      {
         1, 
         1
      }, 
      {
         6, 
         7
      }, 
      {
         12, 
         1
      }, 
   };

   /** parameter used to manipulate and format time series labels */
   public static final int[][] TIME_SERIES_DATE_STEPS =         
   {
      {
         1, 
         1
      }, 
      {
         5, 
         5
      }, 
      {
         10, 
         5
      }, 

  };

   /** parameter used to manipulate and format time series labels */
   public static final int[][] TIME_SERIES_HOUR_STEPS =         
   {
      {
         1, 
         5
      }, 
      {
         6, 
         6
      }, 
      {
         12, 
         12
      }, 
      {
         24, 
         6
      }, 
   };

   /** parameter used to manipulate and format time series labels */
   public static final int[][] TIME_SERIES_MINUTE_STEPS =       
   {
     {
         1, 
         5
      }, 
      {
         5, 
         5
      }, 
      {
         15, 
         15
      }, 
      {
         30, 
         30
      }, 
      {
         60, 
         5
      }
   };

   /** parameter used to manipulate and format time series labels */
   public static final int[][] TIME_SERIES_SECOND_STEPS =          
   {
      {
         1, 
         5
      }, 
      {
         5, 
        5
      }, 
      {
         15, 
         15
      }, 
      {
         30, 
         30
      }, 
      {
         60, 
         5
      }
   };

   /** parameter used to manipulate and format time series labels */
   public static final int[][] TIME_SERIES_MILLISECOND_STEPS =         
   {
      {
         1, 
         5
      }, 
      {
         5, 
         5
      }, 
      {
         10, 
         5
      }, 
      {
         25, 
         25
      }, 
      {
         50, 

        50
      }, 
      {
        100, 
         10
      }, 
     {
         250, 
         250
      }, 
      {
         1000, 
         5
      }
   };

   /** the default device type; probably is not used; probably need to remove */
  public static final String DEFAULT_DEVICE = "PDF";
 
  /** default PDF reader; may not be used */
  public static final String DEFAULT_PDF_READER = "acroread";

}

