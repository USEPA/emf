package gov.epa.mims.analysisengine.gui;

import java.util.*;
import javax.swing.*;

import gov.epa.mims.analysisengine.tree.OutlineType;
import gov.epa.mims.analysisengine.tree.LineType;

/**
 * An interface to convert the program command options to user friendly
 * ImageIcon and vice versa. This class also includes two built in style converter
 * features for Line Styles and Symbol styles and objects for these styles can be
 * created  using getLineStyleConverter() or getSymbolStyleConverter() respectively.
 *
 * =============================================================================
 * Example: Using PrettyOptionImageConverter
 * =============================================================================
 *
 * //Initializing the converter and add the pretty style icons and corresponding
 * //system options to the vectors
 * PrettyOptionImageIconConverter prettyStyleConverter =
 *   PrettyOptionImageIconConverter.getPrettyStyleConverter();
 *
 * //To get a pretty style image for a corresponding system option
 * ImageIcon prettyIcon = prettyStyleConverter.getPrettyOption(system option);
 *
 * //To get an array of pretty style image icons for an array of corresponding
 * //system options
 * ImageIcons[] imageIcons = prettyStyleConverter.getPrettyOptions(an array
 * of systemOptions );
 *
 * //To get a system option for a corresponding pretty style image icon
 * String systemOption = prettyStyleConverter.getSystemOption(imageIcon);
 *
 ** //To get an array of system options for an array of corresponding
 * pretty style image icons
 * String[] systemOption = prettyStyleConverter.getSystemOptions(an array of
 * image icons);
 *
 * // To get a all pretty style image icons in an array of ImageIcon
 * ImageIcon [] imageIcons = prettyStyleConverter.getAllPrettyOptions();
 * =============================================================================
 *
 *
 * @author Alison Eyth, Dan Gatti, Parthee Partheepan CEP UNC
 * @version $Id: PrettyOptionImageIconConverter.java,v 1.2 2005/09/19 14:50:03 rhavaldar Exp $
 * @see
 *
 */


public class PrettyOptionImageIconConverter
   implements gov.epa.mims.analysisengine.tree.LineTypeConstantsIfc
{
   
   /** This is used to store more descriptional(pretty) options **/
   private Vector prettyOptions;
   
   /** This is used to store system options(program constants).**/
   private Vector systemOptions;
   
   /** this static variable represents interface for conversion between line styles
    * and system options **/
   private static PrettyOptionImageIconConverter lineStyleConverter;
   
   /** this static variable represents interface for conversion between symbols
    * and system options **/
   private static PrettyOptionImageIconConverter symbolsConverter;
   
   /** this static variable represents interface for conversion between hisogram 
    * line styles and system options **/
   private static PrettyOptionImageIconConverter histogramLineStyleConverter;
   
   //static initialization block
   static
   {
      //initialize and set up lineStyleConverter
      lineStyleConverter = new PrettyOptionImageIconConverter();
      lineStyleConverter.addLineStyles();
      
      //initialize and set up symbolStylesConverter
      symbolsConverter = new PrettyOptionImageIconConverter();
      symbolsConverter.addSymbolStyles();
      
      //initialize and set up histogramLineStyleConverter
      histogramLineStyleConverter = new PrettyOptionImageIconConverter();
      histogramLineStyleConverter.addHistogramLineStyles();
   }
   
   /**To get a line style converter which is created in the static initialization
    * block
    * @return PrettyOptionImageIconConverter
    * CAUTION: There is only a single copy of this, so when new option is added
    * make sure it's available sytem wide.
    */
   
   public static PrettyOptionImageIconConverter getLineStyleConverter()
   {
      return lineStyleConverter;
   }
   
   /**To get a symbols converter which is created in the static initialization
    * block
    * @return PrettyOptionImageIconConverter
    * CAUTION: There is only a single copy of this, so when new option is added
    * make sure it's available sytem wide.
    */
   
   public static PrettyOptionImageIconConverter getSymbolsConverter()
   {
      return symbolsConverter;
   }
   
   
   /**To get a histogram line style converter which is created in the static 
    * initialization block
    * @return PrettyOptionImageIconConverter
    * CAUTION: There is only a single copy of this, so when new option is added
    * make sure it's available sytem wide.
    */
   
   public static PrettyOptionImageIconConverter getHistogramLineStyleConverter()
   {
      return histogramLineStyleConverter;
   }
   
   /** Creates a new instance of PrettyOptionImageIconConverter */
   public PrettyOptionImageIconConverter()
   {
      prettyOptions = new Vector();
      systemOptions = new Vector();
   }
   
   
   /** Add a more descriptive(pretty) ImageIcon and a program constant
    * (system option) to corresponding Vectors
    * @param prettyOption An descriptive ImageIcon
    * @param systemOption A String program constant
    */
   public void addPrettyOption(ImageIcon prettyOption, String systemOption)
   {
      this.prettyOptions.add(prettyOption);
      this.systemOptions.add(systemOption);
   }
   
   
   /** Returns a pretty icon(or system options) for a
    *  corresponding a program constant
    *  @param A program constant(or system options) of String type
    *  @return ImageIcon A pretty icon of ImageIcon type
    */
   public ImageIcon getPrettyOption(String systemOption)
   {
      for(int i=0; i<systemOptions.size(); i++)
      {
         String tmpObject = (String)systemOptions.get(i);
         if(tmpObject.equals(systemOption))
         {
            return (ImageIcon)prettyOptions.get(i);
         }
      }
      return null;
   }
   
   
   /** Returns An array of pretty icons for the corresponding
    * an array of program constants(or system options)
    * @param An array of program constant(or system options) of String type
    * @return ImageIcon [] A pretty icon of ImageIcon type
    */
   public ImageIcon[] getPrettyOptions(String [] options)
   {
      Vector icons = new Vector();
      
      for(int i=0; i<options.length; i++)
      {
         for(int j=0; j<systemOptions.size(); j++)
         {
            String tmpObject = (String)systemOptions.get(j);
            if(tmpObject.equals(options[i]))
            {
               icons.add(prettyOptions.get(j));
            }
         }
      }
      ImageIcon [] a =
      {};
      return (ImageIcon [])icons.toArray(a);
   }
   
   /** Returns a program constant(or system options) for a
    *  corresponding a pretty icon
    *  @param A pretty icon of ImageIcon type
    *  @return String A program constant(or system options) of String type
    */
   public String getSystemOption(ImageIcon prettyOption)
   {
      for(int i=0; i<prettyOptions.size(); i++)
      {
         ImageIcon tmpObject = (ImageIcon)prettyOptions.get(i);
         String string = tmpObject.toString();
         String iconString = prettyOption.toString();
         if(string.compareTo(iconString) == 0 )
         {
            return (String)systemOptions.get(i);
         }
      }
      return null;
   }
   
   /** Returns an array of program constants(or system options) for the
    *  corresponding an array of pretty icons
    *  @param An array of pretty icon of ImageIcon
    *  @return String[] An array of program constants(or system options) of String type
    */
   public String[] getSystemOptions(ImageIcon [] icons)
   {
      Vector systemStrings = new Vector();
      
      for(int i=0; i<icons.length; i++)
      {
         for(int j=0; j<prettyOptions.size(); j++)
         {
            ImageIcon tmpObject = (ImageIcon)prettyOptions.get(j);
            String string = tmpObject.toString();
            String iconString = icons[i].toString();
            if(string.equals(iconString))
            {
               systemStrings.add(systemOptions.get(j));
            }
         }
      }
      String [] a =  {};
      return (String [])systemStrings.toArray(a);
   }
   
   /** Return the vector containing all the pretty options
    *  @return ImageIcon[] An array of  pretty ImageIcons
    */
   public ImageIcon [] getAllPrettyOptions()
   {
      ImageIcon [] a =
      {};
      return (ImageIcon [])this.prettyOptions.toArray(a);
   }
   
   /**
    * A helper method to load the icons
    * @param path location of the image icon.
    * @return ImageIcon
    */
   private ImageIcon createImageIcon(String path)
   {
      java.net.URL imgURL = getClass().getResource(path);
      if (imgURL != null)
      {
         return new ImageIcon(imgURL);
      }
      else
      {
         System.err.println("Could not find file: " + path + " in classpath.");
         return null;
      }
   }
   
   /** A helper method to load the image icons and then add with corresponding
    * line style system options to the respective vectors using addPrettyOption()
    */
   private void addLineStyles()
   {
      addPrettyOption(createImageIcon(
      "/gov/epa/mims/analysisengine/gui/icons/lineStyles/blank.jpg")
      , BLANK);
      
      addPrettyOption(createImageIcon(
      "/gov/epa/mims/analysisengine/gui/icons/lineStyles/dashed.jpg")
      ,DASHED);
      
      addPrettyOption(createImageIcon(
      "/gov/epa/mims/analysisengine/gui/icons/lineStyles/dotDash.jpg")
      ,DOTDASH);
      
      addPrettyOption(createImageIcon(
      "/gov/epa/mims/analysisengine/gui/icons/lineStyles/dotted.jpg")
      ,DOTTED);
      
      addPrettyOption(createImageIcon(
      "/gov/epa/mims/analysisengine/gui/icons/lineStyles/longDash.jpg")
      , LONGDASH);
      
      addPrettyOption(createImageIcon(
      "/gov/epa/mims/analysisengine/gui/icons/lineStyles/solid.jpg")
      , SOLID);
      
      addPrettyOption(createImageIcon(
      "/gov/epa/mims/analysisengine/gui/icons/lineStyles/twoDash.jpg")
      , TWODASH);
   }

      /** A helper method to load the image icons and then add with corresponding
    * Histogram line style system options to the respective vectors using addPrettyOption()
    */
   private void addHistogramLineStyles()
   {
      addPrettyOption(createImageIcon(
      "/gov/epa/mims/analysisengine/gui/icons/lineStyles/solid.jpg")
      , SOLID);
      
      addPrettyOption(createImageIcon(
      "/gov/epa/mims/analysisengine/gui/icons/lineStyles/dashed.jpg")
      ,DASHED);
      
      addPrettyOption(createImageIcon(
      "/gov/epa/mims/analysisengine/gui/icons/lineStyles/dotDash.jpg")
      ,DOTDASH);
      
      addPrettyOption(createImageIcon(
      "/gov/epa/mims/analysisengine/gui/icons/lineStyles/dotted.jpg")
      ,DOTTED);
      
      addPrettyOption(createImageIcon(
      "/gov/epa/mims/analysisengine/gui/icons/lineStyles/longDash.jpg")
      , LONGDASH);
      
      addPrettyOption(createImageIcon(
      "/gov/epa/mims/analysisengine/gui/icons/lineStyles/twoDash.jpg")
      , TWODASH);
   }
   
   
   /** A helper method to load the symbols image icons and then add with corresponding
    * symbols system options to the respective vectors using addPrettyOption()
    */
   private void addSymbolStyles()
   {
      
      addPrettyOption(createImageIcon(
      "/gov/epa/mims/analysisengine/gui/icons/symbols/circleSolid.jpg")
      , LineType.CIRCLE_SOLID);
      
      addPrettyOption(createImageIcon(
      "/gov/epa/mims/analysisengine/gui/icons/symbols/upTriangleSolid.jpg")
      , LineType.UP_TRIANGLE_SOLID);
      
      addPrettyOption(createImageIcon(
      "/gov/epa/mims/analysisengine/gui/icons/symbols/squareRotatedSolid.jpg")
      , LineType.SQUARE_ROTATED_SOLID);
      
      
      addPrettyOption(createImageIcon(
      "/gov/epa/mims/analysisengine/gui/icons/symbols/circle.jpg")
      , LineType.CIRCLE);
      
      addPrettyOption(createImageIcon(
      "/gov/epa/mims/analysisengine/gui/icons/symbols/square.jpg")
      , LineType.SQUARE);
      
      addPrettyOption(createImageIcon(
      "/gov/epa/mims/analysisengine/gui/icons/symbols/plus.jpg")
      , LineType.PLUS);
      
      addPrettyOption(createImageIcon(
      "/gov/epa/mims/analysisengine/gui/icons/symbols/cross.jpg")
      , LineType.CROSS);
      
      addPrettyOption(createImageIcon(
      "/gov/epa/mims/analysisengine/gui/icons/symbols/starBurst.jpg")
      , LineType.STARBURST);
      
      addPrettyOption(createImageIcon(
      "/gov/epa/mims/analysisengine/gui/icons/symbols/squareRotated.jpg")
      , LineType.SQUARE_ROTATED);
      
      addPrettyOption(createImageIcon(
      "/gov/epa/mims/analysisengine/gui/icons/symbols/plusInCircle.jpg")
      , LineType.PLUS_IN_CIRCLE);
      
      addPrettyOption(createImageIcon(
      "/gov/epa/mims/analysisengine/gui/icons/symbols/squareSolid.jpg")
      , LineType.SQUARE_SOLID);
      
      addPrettyOption(createImageIcon(
      "/gov/epa/mims/analysisengine/gui/icons/symbols/triangleUp.jpg")
      , LineType.TRIANGLE_UP);
      
      addPrettyOption(createImageIcon(
      "/gov/epa/mims/analysisengine/gui/icons/symbols/triangleDown.jpg")
      , LineType.TRIANGLE_DOWN);
      
      addPrettyOption(createImageIcon(
      "/gov/epa/mims/analysisengine/gui/icons/symbols/crossInSquare.jpg")
      , LineType.CROSS_IN_SQUARE);
      
      addPrettyOption(createImageIcon(
      "/gov/epa/mims/analysisengine/gui/icons/symbols/plusInSquareRotated.jpg")
      , LineType.PLUS_IN_SQUARE_ROTATED);
      
      
      addPrettyOption(createImageIcon(
      "/gov/epa/mims/analysisengine/gui/icons/symbols/triangleUpAndDown.jpg")
      , LineType.TRIANGLE_UP_AND_DOWN) ;
      
      addPrettyOption(createImageIcon(
      "/gov/epa/mims/analysisengine/gui/icons/symbols/plusInSquare.jpg")
      , LineType.PLUS_IN_SQUARE);
      
      addPrettyOption(createImageIcon(
      "/gov/epa/mims/analysisengine/gui/icons/symbols/circleAndCross.jpg")
      , LineType.CIRCLE_AND_CROSS);
      
      addPrettyOption(createImageIcon(
      "/gov/epa/mims/analysisengine/gui/icons/symbols/upTriangleInSquare.jpg")
      , LineType.UP_TRIANGLE_IN_SQUARE);
      
      
      
      
      addPrettyOption(createImageIcon(
      "/gov/epa/mims/analysisengine/gui/icons/symbols/circleFilled.jpg")
      , LineType.CIRCLE_FILLED);
      
      addPrettyOption(createImageIcon(
      "/gov/epa/mims/analysisengine/gui/icons/symbols/bullet.jpg")
      , LineType.BULLET);
      
      addPrettyOption(createImageIcon(
      "/gov/epa/mims/analysisengine/gui/icons/symbols/circle2.jpg")
      , LineType.CIRCLE2);
      
      
      
      addPrettyOption(createImageIcon(
      "/gov/epa/mims/analysisengine/gui/icons/symbols/diamond.jpg")
      , LineType.DIAMOND);
      
   }
   
}
