package gov.epa.mims.analysisengine.gui;

import java.io.Serializable;
import java.util.*;

import gov.epa.mims.analysisengine.tree.FontConstantsIfc;
import gov.epa.mims.analysisengine.tree.Legend;
import gov.epa.mims.analysisengine.tree.Text;
import gov.epa.mims.analysisengine.tree.TextBox;


/**
 * An interface to convert the program command options to user friendly
 * String and vice versa
 *
 * =============================================================================
 * Example: Using PrettyOptionStringConverter
 * =============================================================================
 *
 * //Initializing the converter and add the pretty style strings and corresponding
 * //system options to the vectors
 * PrettyOptionStringConverter prettyStyleConverter =
 *   PrettyOptionStringConverter.getPrettyStyleConverter();
 *
 * //To get a pretty style string for a corresponding system option
 * String prettyyString = prettyStyleConverter.getPrettyOption(system option);
 *
 * //To get an array of pretty style strings for an array of corresponding
 * //system options
 * String[] prettyStrings = prettyStyleConverter.getPrettyOptions(an array
 * of systemOptions );
 *
 * //To get a system option for a corresponding pretty style string
 * String systemOption = prettyStyleConverter.getSystemOption(prettyString);
 *
 * //To get an array of system options for an array of corresponding
 * pretty style string
 * String[] systemOption = prettyStyleConverter.getSystemOptions(an array of
 * prettyStrings);
 *
 * // To get a all pretty style strings in an array of string
 * String [] prettyStrings = prettyStyleConverter.getAllPrettyOptions();
 *
 * ===========================================================================
 *
 * @author Alison Eyth, Dan Gatti, Parthee Partheepan CEP UNC
 * @version $Id: PrettyOptionStringConverter.java,v 1.3 2007/05/31 14:29:32 qunhe Exp $
 * @see
 */

public class PrettyOptionStringConverter implements Serializable
{
	static final long serialVersionUID = 1;
   
   /** This is used to store more descriptional(pretty) options **/
   private Vector prettyOptions;
   
   /** This is used to store system options(program constants).**/
   private Vector systemOptions;
   
   /** This static variable contains converter interface between pretty sector
    * Strings and system sector strings */
   private static PrettyOptionStringConverter sectorConverter;
   
   /** This static variable contains converter interface between pretty axis
    * label text position strings and system axis label positon strings
    * including two text based position strings*/
   private static PrettyOptionStringConverter axisLabelSectorConverter;
   
    /** This static variable contains converter interface between pretty sector
    * Strings and system sector strings but this excludes 'CENTER' */
   private static PrettyOptionStringConverter positionConverter;
   
   /** This static variable contains converter interface between pretty margin
    * Strings and system margin strings */
   private static PrettyOptionStringConverter marginConverter;
   
   /** This static variable contains converter interface between pretty region
    * Strings and system region strings */
   private static PrettyOptionStringConverter textBoxRegionConverter;
   
   /** This static variable contains converter interface between pretty textbox unit
    * Strings and system text box unit  strings */
   private static PrettyOptionStringConverter textBoxUnitConverter;
   
   /** This static variable contains converter interface between pretty textbox type
    * Strings and system text box type  strings */
   private static PrettyOptionStringConverter textBoxTypeConverter;
   
   /** This static variable contains converter interface between pretty margin
    * Strings and system margin strings */
   private static PrettyOptionStringConverter fontStyleConverter;
   
   /** This static variable contains converter interface between pretty font typeface
    * strings and system font type face strings */
   private static PrettyOptionStringConverter fontTypefaceConverter;
   
   /** a type face constant for default */
   public static final String DEFAULT = "Default";
   
   //static initialization block
   static
   {
      //initializing and set up the pretty sector strings
      sectorConverter = new PrettyOptionStringConverter();
      sectorConverter.addPrettyOption("Center", Text.CENTER,false);
      sectorConverter.addPrettyOption("East", Text.EAST,false);
      sectorConverter.addPrettyOption("South", Text.SOUTH,false);
      sectorConverter.addPrettyOption("West", Text.WEST,false);
      sectorConverter.addPrettyOption("North", Text.NORTH,false);
      sectorConverter.addPrettyOption("North East", Text.NORTHEAST,false);
      sectorConverter.addPrettyOption("South East", Text.SOUTHEAST,false);
      sectorConverter.addPrettyOption("South West", Text.SOUTHWEST,false);
      sectorConverter.addPrettyOption("North West", Text.NORTHWEST,false);
      
      //initializing and set up the pretty sector strings
      axisLabelSectorConverter = new PrettyOptionStringConverter();
      axisLabelSectorConverter.addPrettyOption("Default", TextEditor.NOTRELATIVE2AXIS,false);
      axisLabelSectorConverter.addPrettyOption("Relative To X Axis", Text.RELATIVE2XAXIS,false);
      axisLabelSectorConverter.addPrettyOption("Relative To Y Axis", Text.RELATIVE2YAXIS,false);
      
      //initializing and set up the pretty sector strings
      positionConverter = new PrettyOptionStringConverter();
      positionConverter.addPrettyOption("East", Text.EAST,false);
      positionConverter.addPrettyOption("South", Text.SOUTH,false);
      positionConverter.addPrettyOption("West", Text.WEST,false);
      positionConverter.addPrettyOption("North", Text.NORTH,false);
      positionConverter.addPrettyOption("North East", Text.NORTHEAST,false);
      positionConverter.addPrettyOption("South East", Text.SOUTHEAST,false);
      positionConverter.addPrettyOption("South West", Text.SOUTHWEST,false);
      positionConverter.addPrettyOption("North West", Text.NORTHWEST,false);
      
      //initializing and set up the pretty sector strings
      marginConverter = new PrettyOptionStringConverter();
      marginConverter.addPrettyOption("Right", Legend.RIGHT_HAND_MARGIN,true);
      marginConverter.addPrettyOption("Left", Legend.LEFT_HAND_MARGIN,true);
      marginConverter.addPrettyOption("Bottom", Legend.BOTTOM_HAND_MARGIN,true);
      marginConverter.addPrettyOption("Top", Legend.TOP_HAND_MARGIN,true);
      
      //initializing and set up the pretty region strings for text box
      textBoxRegionConverter = new PrettyOptionStringConverter();
      textBoxRegionConverter.addPrettyOption("Plot", TextBox.PLOT_REGION,false);
      textBoxRegionConverter.addPrettyOption("Top Margin", TextBox.TOP_HAND_MARGIN,false);
      textBoxRegionConverter.addPrettyOption("Left Margin", TextBox.LEFT_HAND_MARGIN,false);
      textBoxRegionConverter.addPrettyOption("Bottom Margin", TextBox.BOTTOM_HAND_MARGIN,false);
      textBoxRegionConverter.addPrettyOption("Right Margin", TextBox.RIGHT_HAND_MARGIN,false);
      
      //initializing and set up the pretty font style strings
      fontStyleConverter = new PrettyOptionStringConverter();
      fontStyleConverter.addPrettyOption("plain ", FontConstantsIfc.PLAIN_TEXT,false);
      fontStyleConverter.addPrettyOption("bold", FontConstantsIfc.BOLD_TEXT,false);
      fontStyleConverter.addPrettyOption("italic", FontConstantsIfc.ITALIC_TEXT,false);
      fontStyleConverter.addPrettyOption("bold and italic", FontConstantsIfc.BOLD_ITALIC_TEXT,false);
      
      //initializing and set up the pretty font style strings
      fontTypefaceConverter = new PrettyOptionStringConverter();
      fontTypefaceConverter.addPrettyOption(PrettyOptionStringConverter.DEFAULT,
         TextEditor.DEFAULT,false);
      fontTypefaceConverter.addPrettyOption("serif ", TextEditor.SERIF,false);
      fontTypefaceConverter.addPrettyOption("sans Serif", TextEditor.SANS_SERIF,false);
      fontTypefaceConverter.addPrettyOption("script", TextEditor.SCRIPT,false);
      
   }
   
   /**To get a pretty string sector converter which is created in the static initialization
    * block
    * @return PrettyOptionStringConverter
    * CAUTION: There is only a single copy of this, so when new option is added
    * make sure it's available sytem wide.
    */
   
   public static PrettyOptionStringConverter getSectorConverter()
   {
      return sectorConverter;
   }
   
    /**To get a pretty string sector converter which is created in the static initialization
    * block
    * @return PrettyOptionStringConverter
    * CAUTION: There is only a single copy of this, so when new option is added
    * make sure it's available sytem wide.
    */
   
   public static PrettyOptionStringConverter getAxisLabelSectorConverter()
   {
      return axisLabelSectorConverter;
   }
   
   /**To get a pretty string positon converter which is created in the static initialization
    * block. This converter is same as sectorConverter except this does not have 'CENTER'
    * @return PrettyOptionStringConverter
    * CAUTION: There is only a single copy of this, so when new option is added
    * make sure it's available sytem wide.
    */
   
   public static PrettyOptionStringConverter getPositionConverter()
   {
      return positionConverter;
   }
   
   /**To get a pretty string margin converter which is created in the static initialization
    * block
    * @return PrettyOptionStringConverter
    * CAUTION: There is only a single copy of this, so when new option is added
    * make sure it's available sytem wide.
    */
   
   public static PrettyOptionStringConverter getMarginConverter()
   {
      return marginConverter;
   }
   
   /**To get a pretty string region converter which is created in the static initialization
    * block
    * @return PrettyOptionStringConverter regionConverter
    * CAUTION: There is only a single copy of this, so when new option is added
    * make sure it's available sytem wide.
    */
   
   public static PrettyOptionStringConverter getRegionConverter()
   {
      return textBoxRegionConverter;
   }
   
    /**To get a pretty string fontStyle converter which is created in the static initialization
    * block
    * @return PrettyOptionStringConverter fontStyleConverter
    * CAUTION: There is only a single copy of this, so when new option is added
    * make sure it's available sytem wide.
    */
   
   public static PrettyOptionStringConverter getFontStyleConverter()
   {
      return fontStyleConverter;
   }
   
    /**To get a pretty string fontTypeface converter which is created in the static initialization
    * block
    * @return PrettyOptionStringConverter fontTypeFaceConverter
    * CAUTION: There is only a single copy of this, so when new option is added
    * make sure it's available sytem wide.
    */
   
   public static PrettyOptionStringConverter getFontTypefaceConverter()
   {
      return fontTypefaceConverter;
   }
   
   
   /** Creates a new instance of PrettyOptionStringConverter */
   public PrettyOptionStringConverter()
   {
      prettyOptions = new Vector();
      systemOptions = new Vector();
   }
   
   /** Add a more descriptive(pretty) String and a program constant
    * (system option) to corresponding Vectors
    * @param prettyOption String descriptive String
    * @param systemOption String program constant
    * @alphabeticalSort boolean If true, requires Strings to be sorted
    * in alphabetical order
    */
   
   public void addPrettyOption(String prettyOption, String systemOption,
   boolean alphabeticalSort)
   {
      if(alphabeticalSort && (prettyOptions.size()> 0))
      {
         insertInOrder(prettyOption, systemOption);
      }
      else
      {
         this.prettyOptions.add(prettyOption);
         this.systemOptions.add(systemOption);
      }
   }
   
   
   
   /** This method goes through the prettyOptions vector from the 0th index and
    * compares whether new prettyOption is alphabetically greater than each
    * element. If the new option is greater then it's inserted into the vector
    * above the compared element in the vector.
    * @param prettyOption A descriptive String
    * @param systemOption A program constant
    */
   private void insertInOrder(String prettyOption, String systemOption)
   {
      for(int i=1; i<=prettyOptions.size(); i++)
      {
         String tmpObject = (String)prettyOptions.get(i-1);
         if(prettyOption instanceof String)
         {
            if((tmpObject).compareTo(prettyOption)>0)
            {
               prettyOptions.insertElementAt(prettyOption, i-1);
               systemOptions.insertElementAt(systemOption, i-1);
               return;
            }
         }
      }
      //inserting at the end
      this.prettyOptions.add(prettyOption);
      this.systemOptions.add(systemOption);
   }
   
   
   /** Given a program constant(system option) returns the corresponding pretty
    *  option
    *  @param systemOption a prgram constant
    *  @return String a pretty option String
    */
   public String getPrettyOption(String systemOption)
   {
      for(int i=0; i<systemOptions.size(); i++)
      {
         String tmpObject = (String)systemOptions.get(i);
         if(tmpObject.equals(systemOption))
         {
            return (String)prettyOptions.get(i);
         }
      }
      return null;
   }
   
   /** Returns An array of pretty Strings for the corresponding
    * an array of program constants(or system options)
    * @param An array of program constant(or system options) of String type
    * @return String [] A pretty strings of String type
    */
   public String[] getPrettyOptions(String [] options)
   {
      Vector prettyStrings = new Vector();
      
      for(int i=0; i<options.length; i++)
      {
         for(int j=0; j<systemOptions.size(); j++)
         {
            String tmpObject = (String)systemOptions.get(j);
            if(tmpObject.equals(options[i]))
            {
               prettyStrings.add(prettyOptions.get(j));
            }
         }
      }
      String [] a =
      {};
      return (String [])prettyStrings.toArray(a);
   }
   
   
   
   /** Given a pretty option returns the corresponding program constant or
    *  system options
    *  @param prettyOption which is a pretty String
    *  @return String a program constant
    */
   public String getSystemOption(String prettyOption)
   {
      for(int i=0; i<prettyOptions.size(); i++)
      {
         String tmpObject = (String)prettyOptions.get(i);
         if(tmpObject.equals(prettyOption))
         {
            return (String)systemOptions.get(i);
         }
      }
      return null;
   }
   
   
   /** Returns an array of program constants(or system options) for the
    *  corresponding an array of pretty strings
    *  @param An array of pretty string of String type
    *  @return String [] An array of program constants(or system options) of String type
    */
   public String[] getSystemOptions(String [] prettyStrings)
   {
      Vector systemStrings = new Vector();
      
      for(int i=0; i<prettyStrings.length; i++)
      {
         for(int j=0; j<prettyOptions.size(); j++)
         {
            String tmpObject = (String)prettyOptions.get(j);
            if(tmpObject.equals(prettyStrings[i]))
            {
               systemStrings.add(systemOptions.get(j));
            }
         }
      }
      String [] a =
      {};
      return (String [])systemStrings.toArray(a);
   }
   
   /** Return the vector containing all the pretty options
    *  @param a  an array of String
    *  @return String []an array of pretty options
    */
   public String [] getAllPrettyOptions()
   {
      String [] a =
      {};
      return (String [])this.prettyOptions.toArray(a);
   }
   
   public static void main(String arg[])
   {
     PrettyOptionStringConverter prettyOptionStringConverter = new PrettyOptionStringConverter();
     prettyOptionStringConverter.addPrettyOption("one", "ONE",true);
     prettyOptionStringConverter.addPrettyOption("two", "TWO",true);
     prettyOptionStringConverter.addPrettyOption("three", "THREE",true);
     prettyOptionStringConverter.addPrettyOption("four", "FOUR",true);
     prettyOptionStringConverter.addPrettyOption("five", "FIVE",true);
     prettyOptionStringConverter.addPrettyOption("six","SIX",true);
     prettyOptionStringConverter.addPrettyOption("seven", "SEVEN",true);
     prettyOptionStringConverter.addPrettyOption("eight", "EIGHT",true);
     prettyOptionStringConverter.addPrettyOption("nine", "NINE",true);
     
     //String [] prettyStrings = prettyOptionStringConverter.getAllPrettyOptions();
     String [] systemStrings = prettyOptionStringConverter.getSystemOptions(new String[]{"seven","three","four"});
     String [] prettyStrings = prettyOptionStringConverter.getPrettyOptions(new String[]{"SEVEN","THREE","FOUR"});
     for(int i=0;i< prettyStrings.length;i++)
     {
         System.out.println(prettyStrings[i]);
     }//for(i)
   }
   
}

