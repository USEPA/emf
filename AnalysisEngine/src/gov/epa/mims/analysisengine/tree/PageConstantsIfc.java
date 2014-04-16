package gov.epa.mims.analysisengine.tree;

/**
 * a Constants interface
 *
 * @author Tommy E. Cathey
 * @version $Id: PageConstantsIfc.java,v 1.2 2005/09/19 14:50:10 rhavaldar Exp $
 *
 ***************************************************************/
public interface PageConstantsIfc
{
   /**
    * valid argument for setForm(String) of PageType object
    * causes output to be displayed on the screen
    * If using the Rcommunicator package on Windows and
    * Acrobat reader is in your path, then the Rcommunicator package
    * start Acrobat reader and plots will be displayed in
    * Acrobat reader. Screen and X11 are equilvalent.
    */
   public static final String SCREEN = "SCREEN";

   /**
    * valid argument for setForm(String) of PageType object
    * causes output to be displayed on the screen
    * If using the Rcommunicator package on Windows and
    * Acrobat reader is in your path, then the Rcommunicator package
    * start Acrobat reader and plots will be displayed in
    * Acrobat reader.
    * Note: Screen and X11 are equilvalent.
    */
   public static final String X11 = "X11";

   /**
    * valid argument for setForm(String) of PageType object
    * causes png output to be written to the file
    * defined in {@link PageType#setFilename(String)}
    */
   public static final String PNG_BITMAP = "PNG";

   /**
    * valid argument for setForm(String) of PageType object
    * causes jpg output to be written to the file
    * defined in {@link PageType#setFilename(String)}
    */
   public static final String JPEG = "JPEG";

   /**
    * valid argument for setForm(String) of PageType object
    * causes pdf output to be written to the file
    * defined in {@link PageType#setFilename(String)}
    */
   public static final String PDF = "PDF";

   /**
    * valid argument for setForm(String) of PageType object
    * causes PS output to be written to the file
    * defined in {@link PageType#setFilename(String)}
    */
   public static final String POSTSCRIPT = "PS";

   /**
    * HAS NOT BEEN TESTED
    * valid argument for setForm(String) of PageType object
    * causes PICTEX output to be written to the file
    * defined in {@link PageType#setFilename(String)}
    */
   public static final String LATEX_PICTEX_GRAPHICS = "PICTEX";
}