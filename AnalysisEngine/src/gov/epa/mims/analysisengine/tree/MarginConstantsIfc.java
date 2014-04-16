package gov.epa.mims.analysisengine.tree;

/**
 * a Constants interface
 *
 * @author Tommy E. Cathey
 * @version $Id: MarginConstantsIfc.java,v 1.2 2005/09/19 14:50:10 rhavaldar Exp $
 *
 ***************************************************************/
public interface MarginConstantsIfc
{
   /** Plot region constant */
   public static final String RIGHT_HAND_MARGIN = "R";

   /** Plot region constant */
   public static final String LEFT_HAND_MARGIN = "L";

   /** Plot region constant */
   public static final String TOP_HAND_MARGIN = "T";

   /** Plot region constant */
   public static final String BOTTOM_HAND_MARGIN = "B";

   /** Plot region constant */
   public static final String PLOT_REGION = "P";

   /** Plot region constant */
   public static final String REFERENCE_LINE = "REFLINE";

   /** Plot region constant */
   public static final String REGRESSION_LINE = "REGRESSION_LINE";

   /** Region constants */
   public static final int FIGURE = 0;

   /** Region constants */
   public static final int PLOT = 1;

   /** Region constants */
   public static final int INNER = 2;

   /** Region constants */
   public static final int OUTER = 3;

   /** Region constants */
   public static final int DEVICE = 4;
}
