package gov.epa.mims.analysisengine.tree;

/**
 * a Constants interface
 *
 * @author Tommy E. Cathey
 * @version $Id: BoxPlotConstantsIfc.java,v 1.2 2005/09/19 14:50:10 rhavaldar Exp $
 *
 ***************************************************************/
public interface BoxPlotConstantsIfc
{
   /** constant */
   public static final String LOWER_WHISKER = "Lower Whisker";

   /** constant */
   public static final String LOWER_HINGE = "Lower Hinge";

   /** constant */
   public static final String MEDIAN = "Median";

   /** constant */
   public static final String UPPER_HINGE = "Upper Hinge";

   /** constant */
   public static final String UPPER_WHISKER = "Upper Whisker";

   /** constant */
   public static final String NUM_OBSERVATION = "Num Observations";

   /** constant */
   public static final String LOWER_NOTCH_EXTREME = "Lower Notch Extreme";

   /** constant */
   public static final String UPPER_NOTCH_EXTREME = "Upper Notch Extreme";

   /** constant */
   public static final String OUTLIER = "Outlier";

   /** DOCUMENT_ME */
   public static final int USE_R = 0;

   /** DOCUMENT_ME */
   public static final int PRECOMPUTED = 1;

   /** DOCUMENT_ME */
   public static final int CUSTOM = 2;
}
