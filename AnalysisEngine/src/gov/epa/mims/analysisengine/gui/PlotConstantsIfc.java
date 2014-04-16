package gov.epa.mims.analysisengine.gui;

/**
 * <p>Description: The constants for the name of each plot.  Add a new constant
 * each time that a new plot is added. </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: UNC-CH Carolina Environmental Program </p>
 * @author Daniel Gatti
 * @version $Id: PlotConstantsIfc.java,v 1.2 2005/09/19 14:50:03 rhavaldar Exp $
 */
public interface PlotConstantsIfc
{
   public static final String BAR_PLOT = "BarPlot";
   public static final String DISCRETE_CATEGORY_PLOT = "DiscreteCategoryPlot";
   public static final String HISTOGRAM_PLOT = "Histogram";
   public static final String RANK_ORDER_PLOT = "RankOrderPlot";
   public static final String SCATTER_PLOT = "ScatterPlot";
   public static final String CDF_PLOT = "CdfPlot";
   public static final String TIME_SERIES_PLOT = "TimeSeries";

   public static final String[] ALL_PLOTS = {BAR_PLOT, DISCRETE_CATEGORY_PLOT,
      HISTOGRAM_PLOT, RANK_ORDER_PLOT, SCATTER_PLOT, TIME_SERIES_PLOT,CDF_PLOT};
}