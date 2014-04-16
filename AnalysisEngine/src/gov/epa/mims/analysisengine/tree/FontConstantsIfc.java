package gov.epa.mims.analysisengine.tree;

/**
 * a Constants interface
 *
 * @author Tommy E. Cathey
 * @version $Id: FontConstantsIfc.java,v 1.2 2005/09/19 14:50:10 rhavaldar Exp $
 *
 ***************************************************************/
public interface FontConstantsIfc
{
   /** Predefined font */
   public static final String PLAIN_TEXT = "plain";

   /** Predefined font */
   public static final String BOLD_TEXT = "bold";

   /** Predefined font */
   public static final String ITALIC_TEXT = "italic";

   /** Predefined font */
   public static final String BOLD_ITALIC_TEXT = "bold italic";

   /** Array of Predefined font */
   public static final String[] AVAILABLE_FONTS = 
   {
      PLAIN_TEXT, 
      BOLD_TEXT, 
      ITALIC_TEXT, 
      BOLD_ITALIC_TEXT
   };
}