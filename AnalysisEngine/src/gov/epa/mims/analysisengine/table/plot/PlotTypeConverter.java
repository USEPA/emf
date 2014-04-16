package gov.epa.mims.analysisengine.table.plot;

import gov.epa.mims.analysisengine.AnalysisEngineConstants;

import java.util.HashMap;

/**
 * <p>
 * Title:TableApp.java
 * </p>
 * <p>
 * Description: A class to map between the nice name for plots and the constants available in the
 * AnalysisEngineConstants
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * <p>
 * Company: CEP, UNC-Chapel Hill
 * </p>
 * 
 * @author Parthee Partheepan
 * @version $Id:
 */

public class PlotTypeConverter {

	public static final String BAR_PLOT = "Bar Plot";

	public static final String XY_PLOT = "XY Plot";

	public static final String XY_LINES_PLOT = "Line Plot";

	public static final String BOX_PLOT = "Box Plot";

	public static final String DISCRETE_CATEGORY_PLOT = "Discrete Category Plot";

	public static final String RANK_ORDER_PLOT = "Rank Order Plot";

	public static final String TIMESERIES_PLOT = "Time Series Plot";

	public static final String HISTOGRAM_PLOT = "Histogram Plot";

	public static final String CDF_PLOT = "CDF Plot";

	public static final String TORNADO_PLOT = "Tornado Plot";

	public static final String[] AVAILABLE_PLOT_TYPES = { BAR_PLOT, BOX_PLOT, CDF_PLOT, DISCRETE_CATEGORY_PLOT,
			HISTOGRAM_PLOT, RANK_ORDER_PLOT, XY_PLOT, XY_LINES_PLOT, TIMESERIES_PLOT, TORNADO_PLOT };

	/**
	 * mapping from constants in the table package to AnalysisEngineConstants in the tree package key= table plot type
	 * constants
	 */
	private static HashMap tableToTreeMap;

	static {
		tableToTreeMap = new HashMap();
		tableToTreeMap.put(BAR_PLOT, AnalysisEngineConstants.BAR_PLOT);
		tableToTreeMap.put(TORNADO_PLOT, AnalysisEngineConstants.TORNADO_PLOT);
		tableToTreeMap.put(CDF_PLOT, AnalysisEngineConstants.CDF_PLOT);
		tableToTreeMap.put(XY_PLOT, AnalysisEngineConstants.SCATTER_PLOT);
		tableToTreeMap.put(XY_LINES_PLOT, AnalysisEngineConstants.SCATTER_PLOT);
		tableToTreeMap.put(BOX_PLOT, AnalysisEngineConstants.BOX_PLOT);
		tableToTreeMap.put(DISCRETE_CATEGORY_PLOT, AnalysisEngineConstants.DISCRETE_CATEGORY_PLOT);
		tableToTreeMap.put(RANK_ORDER_PLOT, AnalysisEngineConstants.RANK_ORDER_PLOT);
		tableToTreeMap.put(TIMESERIES_PLOT, AnalysisEngineConstants.TIME_SERIES_PLOT);
		tableToTreeMap.put(HISTOGRAM_PLOT, AnalysisEngineConstants.HISTOGRAM_PLOT);

	}

	public static String getAEPlotType(String plotType) {
		return (String) tableToTreeMap.get(plotType);
	}
}
