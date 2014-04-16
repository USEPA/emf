package gov.epa.mims.analysisengine.tree;

import java.util.HashMap;


/**
 * a Constants interface
 *
 * @author    Tommy E. Cathey
 * @created   July 30, 2004
 * @version   $Id: AnalysisOptionConstantsIfc.java,v 1.6 2004/06/02 00:00:51 tsb
 *      Exp $
 */
public interface AnalysisOptionConstantsIfc
{
   /** AnalysisOption */
   public final static String LINEAR_REGRESSIONS = "LINEAR_REGRESSIONS";

   /** AnalysisOption */
   public final static String TEXT_BOXES = "TEXT_BOXES";

   /** AnalysisOption */
   public final static String PLOT_TITLE = "PLOT_TITLE";

   /** AnalysisOption */
   public final static String PLOT_SUBTITLE = "PLOT_SUBTITLE";

   /** AnalysisOption */
   public final static String PLOT_FOOTER = "PLOT_FOOTER";

   /** AnalysisOption */
   public final static String WORLD_COORDINATES = "WORLD_COORDINATES";

   /** AnalysisOption */
   public final static String LEGEND = "LEGEND";

   /** AnalysisOption */
   public final static String TORNADO_TYPE = "TORNADO_TYPE";

   /** AnalysisOption */
   public final static String BAR_TYPE = "BAR_TYPE";

   /** AnalysisOption */
   public final static String BOX_TYPE = "BOX_TYPE";

   /** AnalysisOption */
   public final static String HISTOGRAM_TYPE = "HISTOGRAM_TYPE";

   /** AnalysisOption */
   public final static String X_NUMERIC_AXIS = "X_NUMERIC_AXIS";

   /** AnalysisOption */
   public final static String Y_NUMERIC_AXIS = "Y_NUMERIC_AXIS";

   /** AnalysisOption */
   public final static String NUMERIC_AXIS = "NUMERIC_AXIS";

   /** AnalysisOption */
   public final static String CATEGORY_AXIS = "CATEGORY_AXIS";

   /** AnalysisOption */
   public final static String X_TIME_AXIS = "X_TIME_AXIS";

   /** AnalysisOption */
   public final static String LINE_TYPE = "LINE_TYPE";

   /** AnalysisOption */
   public final static String GRID_TYPE = "GRID_TYPE";

   /** AnalysisOption */
   public final static String OUTLINE_TYPE = "OUTLINE_TYPE";

   /** AnalysisOption */
   public final static String PAGE_TYPE = "PAGE_TYPE";

   /** AnalysisOption */
   public final static String SORT_TYPE = "SORT_TYPE";

   /** AnalysisOption */
   public final static String DISPLAY_SIZE_TYPE = "DISPLAY_SIZE_TYPE";

   /** DOCUMENT_ME */
   public static HashMap mapOption2Class = new HashMap();
}
