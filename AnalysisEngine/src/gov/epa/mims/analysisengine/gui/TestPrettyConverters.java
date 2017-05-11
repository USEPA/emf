package gov.epa.mims.analysisengine.gui;

import junit.framework.*;
import java.util.Arrays;
import java.text.Collator;
import javax.swing.ImageIcon;

/**
 * A class to test the PrettyOptionStringConverter and the PrettyOptionImageIconConverter.
 *
 * @author Daniel Gatti
 * @version $Id: TestPrettyConverters.java,v 1.2 2005/09/19 14:50:03 rhavaldar Exp $
 */
public class TestPrettyConverters extends TestCase
{
   /** System strings to test String converter. */
   private String[] systemStrings = new String[]
   {
      "ONE", "TWO", "THREE", "FOUR", "FIVE", "SIX", "SEVEN", "EIGHT", "NINE", "TEN"
   };

   /** Pretty strings to test String converter. */
   private String[] prettyStrings = new String[]
   {
      "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten"
   };

   /** Ssytem strings to test Icon converter. */
   private String[] iconSystemStrings = new String[]
   {
      "ONE", "TWO", "THREE", "FOUR", "FIVE", "SIX", "SEVEN", "EIGHT"
   };

   /**The number of icons to create. Must match the nubmer of icon file names. */
   private static final int NUM_ICONS = 8;

   /** Test Icons from jlfgr-1_0.jar to test Icon converter. */
   private ImageIcon[] testIcons = new ImageIcon[NUM_ICONS];

   /** Path to jlfgr-1_0.jar icons for testing. */
   private String testIconDir = "/toolbarButtonGraphics/text/";

   /** Icon files to test Icon converter. */
   private String[] iconFileNames =
   {
      "Italic16.gif",
      "Italic24.gif",
      "Bold16.gif",
      "Bold24.gif",
      "Normal16.gif",
      "Normal24.gif",
      "Underline16.gif",
      "Underline24.gif"
   };

   /** System strings for line style icons. */
   private String[] lineStyleSystemStrings =
   {
      "BLANK",
      "DASHED",
      "DOTDASH",
      "DOTTED",
      "LONGDASH",
      "SOLID",
      "TWODASH"
   };

   /** Path to line style icons file. */
   private String lineStyleIconDir = "/gov/epa/mims/analysisengine/gui/icons/lineStyles/";

   /** Icon file names for line styles. */
   private String[] lineStyleIconFileNames =
   {
      "blank.jpg",
      "dashed.jpg",
      "dotDash.jpg",
      "dotted.jpg",
      "longDash.jpg",
      "solid.jpg",
      "twoDash.jpg"
   };

   /** Test Icons for line style. */
   private ImageIcon[] lineStyleTestIcons = null;

   /** System strings for line style icons. */
   private String[] symbolSystemStrings =
   {
           "CIRCLE_SOLID",
           "UP_TRIANGLE_SOLID",
           "SQUARE_ROTATED_SOLID",
           "CIRCLE",
           "SQUARE",
           "PLUS",
           "CROSS",
           "STARBURST",
           "SQUARE_ROTATED",
           "PLUS_IN_CIRCLE",
           "SQUARE_SOLID",
           "TRIANGLE_UP",
           "TRIANGLE_DOWN",
           "CROSS_IN_SQUARE",
           "PLUS_IN_SQUARE_ROTATED",
           "TRIANGLE_UP_AND_DOWN",
           "PLUS_IN_SQUARE",
           "CIRCLE_AND_CROSS",
           "UP_TRIANGLE_IN_SQUARE",
           "CIRCLE_FILLED",
           "BULLET",
           "CIRCLE2",
           "DIAMOND"
   };

   /** Path to line style icons file. */
   private String symbolIconDir = "/gov/epa/mims/analysisengine/gui/icons/symbols/";

   /** Icon file names for symbols. */
   private String[] symbolIconFileNames =
   {
           "circleSolid.jpg",
           "upTriangleSolid.jpg",
           "squareRotatedSolid.jpg",
           "circle.jpg",
           "square.jpg",
           "plus.jpg",
           "cross.jpg",
           "starBurst.jpg",
           "squareRotated.jpg",
           "plusInCircle.jpg",
           "squareSolid.jpg",
           "triangleUp.jpg",
           "triangleDown.jpg",
           "crossInSquare.jpg",
           "plusInSquareRotated.jpg",
           "triangleUpAndDown.jpg",
           "plusInSquare.jpg",
           "circleAndCross.jpg",
           "upTriangleInSquare.jpg",
           "circleFilled.jpg",
           "bullet.jpg",
           "circle2.jpg",
           "diamond.jpg"
   };

    private String[] symbolIconFileNames_ORIGINAL =
            {
                    "circle.jpg",
                    "triangleUp.jpg",
                    "plus.jpg",
                    "cross.jpg",
                    "squareRotated.jpg",
                    "triangleDown.jpg",
                    "crossInSquare.jpg",
                    "starBurst.jpg",
                    "plusInSquareRotated.jpg",
                    "plusInCircle.jpg",
                    "triangleUpAndDown.jpg",
                    "plusInSquare.jpg",
                    "circleAndCross.jpg",
                    "upTriangleInSquare.jpg",
                    "squareSolid.jpg",
                    "circleSolid.jpg",
                    "upTriangleSolid.jpg",
                    "squareRotatedSolid.jpg",
                    "circleFilled.jpg",
                    "bullet.jpg",
                    "circle2.jpg",
                    "square.jpg",
                    "diamond.jpg"
            };

   /** Test Icons for symbol. */
   private ImageIcon[] symbolTestIcons = null;


   /**
    *  Constructor
    */
   public TestPrettyConverters(String str)
   {
      super(str);
   }

   /**
    * Test loading in a set of Strings.
    */
   public void testAddingStrings()
   {
        // Make a new PrettyOptionStringConverter and add our strings.
      PrettyOptionStringConverter posc = new PrettyOptionStringConverter();
      for (int i = 0; i < prettyStrings.length; i++)
          posc.addPrettyOption(prettyStrings[i], systemStrings[i], false);

      // Test getting pretty strings one at a time.
      String s = null;
      for (int i = 0; i < prettyStrings.length; i++)
      {
         s = posc.getPrettyOption(systemStrings[i]);
         assertNotNull(s);
         assertEquals("Failed getting " + systemStrings[i], prettyStrings[i], s);
      }

      // Test getting system strings one at a time.
     for (int i = 0; i < prettyStrings.length; i++)
     {
       s = posc.getSystemOption(prettyStrings[i]);
        assertNotNull(s);
       assertEquals("Failed getting " + prettyStrings[i], systemStrings[i], s);
      }

      // Test getting system strings all at once.
      String[] systemOptions = posc.getSystemOptions(prettyStrings);
      assertEquals("System options array length", systemStrings.length, systemOptions.length);
      for (int i = 0; i < systemStrings.length; i++)
      {
         assertEquals("Failed getting " + systemStrings[i], systemStrings[i], systemOptions[i]);
      }

      //   Test getting pretty strings all at once.
      String[] prettyOptions = null;
      prettyOptions = posc.getPrettyOptions(systemOptions);
      assertEquals("Pretty options array length", prettyStrings.length, prettyOptions.length);
      for (int i = 0; i < prettyStrings.length; i++)
      {
         assertEquals("Failed getting " + prettyStrings[i], prettyStrings[i], prettyOptions[i]);
      }

      // Test getting all pretty options through the getAllPrettyOptions() method.
      prettyOptions = null;
      prettyOptions = posc.getAllPrettyOptions();
      assertEquals("Pretty options array length", prettyStrings.length, prettyOptions.length);
      for (int i = 0; i < prettyStrings.length; i++)
      {
         assertEquals("Failed getting " + prettyStrings[i], prettyStrings[i], prettyOptions[i]);
      }
   }


   /**
    * Test sorting of strings.
    *
    */
   public void testSortingStrings()
   {
      //   Make a new PrettyOptionStringConverter and add our strings.
      PrettyOptionStringConverter posc = new PrettyOptionStringConverter();
      for (int i = 0; i < prettyStrings.length; i++)
          posc.addPrettyOption(prettyStrings[i], systemStrings[i], true);

      // Make a temporary array and sort it.
      String[] tmpPretty = new String[prettyStrings.length];
      System.arraycopy(prettyStrings, 0, tmpPretty, 0, prettyStrings.length);
      Arrays.sort(tmpPretty, Collator.getInstance());

        String[] prettyOptions = posc.getAllPrettyOptions();
      for (int i = 0; i < prettyOptions.length; i++)
      {
         assertEquals("Sorting", tmpPretty[i], prettyOptions[i]);
      }
   }


   /**
    * Test basic adding and retreiving of icons.
    */
   public void testAddingIcons()
   {
         PrettyOptionImageIconConverter poiic = new PrettyOptionImageIconConverter();
         for (int i = 0; i < NUM_ICONS; i++)
            poiic.addPrettyOption(testIcons[i], iconSystemStrings[i]);

         // Test that we get the correct icons back for each system string one at a time.
         ImageIcon icon = null;
      for (int i = 0; i < NUM_ICONS; i++)
      {
         icon = poiic.getPrettyOption(iconSystemStrings[i]);
         assertEquals("Icons from systems", testIcons[i], icon);
      }

      //   Test that we get the correct system strings back for each icon one at a time.
      String s = null;
      for (int i = 0; i < NUM_ICONS; i++)
      {
         s = poiic.getSystemOption(testIcons[i]);
         assertEquals("systems from icons", iconSystemStrings[i], s);
      }

      //   Test that we get the correct icons back for each system string.
      ImageIcon[] icons = new ImageIcon[0];
      icons = poiic.getPrettyOptions(iconSystemStrings);
      assertEquals("icons same length as icons", testIcons.length, icons.length);
      for (int i = 0; i < NUM_ICONS; i++)
      {
         assertEquals("Icons from systems", testIcons[i], icons[i]);
      }

      //   Test that we get the correct system strings back for each icon.
      String[] strs = new String[0];
      strs = poiic.getSystemOptions(testIcons);
      assertEquals("system strings same length as strs", iconSystemStrings.length, strs.length);
      for (int i = 0; i < NUM_ICONS; i++)
      {
         assertEquals("systems from icons", iconSystemStrings[i], strs[i]);
      }

      //Test that we get the correct icons back for each system string using getAllPrettyOptions().
      icons = new ImageIcon[0];
      icons = poiic.getAllPrettyOptions();
      for (int i = 0; i < NUM_ICONS; i++)
      {
         assertEquals("Icons from systems", testIcons[i], icons[i]);
      }
   }


   /**
    * Test the line style icons.
    */
   public void testLineStyleIcons()
   {
      PrettyOptionImageIconConverter poiic = PrettyOptionImageIconConverter.getLineStyleConverter();

         // Test getting each line style icon one at a time.
         ImageIcon icon = null;
         for (int i = 0; i < lineStyleSystemStrings.length; i++)
         {
            icon =poiic.getPrettyOption(lineStyleSystemStrings[i]);
            assertEquals("line style icons from system", lineStyleTestIcons[i].getImage(), icon.getImage());
         }

         //   Test getting each line style system string one at a time.
      String s = null;
      for (int i = 0; i < lineStyleTestIcons.length; i++)
      {
         s = poiic.getSystemOption(lineStyleTestIcons[i]);
         assertEquals("line style system from icons", lineStyleSystemStrings[i], s);
      }

      //   Test getting each line style system strings all at once.
        ImageIcon[] icons = poiic.getPrettyOptions(lineStyleSystemStrings);
      assertEquals("test icons and icons same length", lineStyleTestIcons.length, icons.length);
        for (int i = 0; i < icons.length; i++)
        {
           assertEquals("line style icons from system", lineStyleTestIcons[i].getImage(), icons[i].getImage());
        }

         //   Test getting each line style system strings all at once.
      String[] strs = poiic.getSystemOptions(lineStyleTestIcons);
      assertEquals("test system strings and string same length", lineStyleSystemStrings.length, strs.length);
      for (int i = 0; i < strs.length; i++)
      {
         assertEquals("line style system from icons", lineStyleSystemStrings[i], strs[i]);
      }

         // Test icons using getAllPrettyOptions().
         icons = poiic.getAllPrettyOptions();
         assertEquals("test icons and icons same length", lineStyleTestIcons.length, icons.length);
         for (int i = 0; i < lineStyleTestIcons.length; i++)
         {
            assertEquals("line style icons from system", lineStyleTestIcons[i].getImage(), icons[i].getImage());
         }
   }


/**
 * Test the symbol style icons.
 */
public void testSymbolStyleIcons()
{
   PrettyOptionImageIconConverter poiic = PrettyOptionImageIconConverter.getSymbolsConverter();

   // Test getting each symbol icon one at a time.
   ImageIcon icon = null;
   for (int i = 0; i < symbolSystemStrings.length; i++)
   {
      icon =poiic.getPrettyOption(symbolSystemStrings[i]);
     assertEquals("symbol icons from system", symbolTestIcons[i].getImage(), icon.getImage());
   }

    //Test getting each symbol system string one at a time.
    String s = null;
   for (int i = 0; i < symbolTestIcons.length; i++)
   {
      s =poiic.getSystemOption(symbolTestIcons[i]);
       assertEquals("symbol system from icons", symbolSystemStrings[i], s);
   }

     // Test getting each symbol system strings all at once.
     ImageIcon[] icons = poiic.getPrettyOptions(symbolSystemStrings);
      assertEquals("test icons and icons same length", symbolTestIcons.length, icons.length);
     for (int i = 0; i < icons.length; i++)
     {
        assertEquals("symbol icons from system", symbolTestIcons[i].getImage(), icons[i].getImage());
     }

   //   Test getting each symbol system strings all at once.
   String[] strs = poiic.getSystemOptions(symbolTestIcons);
   assertEquals("test system strings and string same length", symbolSystemStrings.length, strs.length);
   for (int i = 0; i < strs.length; i++)
   {
      assertEquals("symbol system from icons", symbolSystemStrings[i], strs[i]);
   }

   // Test icons using getAllPrettyOptions().
   icons = poiic.getAllPrettyOptions();
   assertEquals("test icons and icons same length", symbolTestIcons.length, icons.length);
   for (int i = 0; i < symbolTestIcons.length; i++)
   {
      assertEquals("symbol icons from system", symbolTestIcons[i].getImage(), icons[i].getImage());
   }
}



   /**
    * Create icons for testing.
    */
   public void setUp()
   {
      for(int i = 0; i < NUM_ICONS; i++)
      {
         java.net.URL url = getClass().getResource(testIconDir + iconFileNames[i]);
         if (url != null)
            testIcons[i] = new ImageIcon(url);
         else
            System.err.println("Can't find file " + testIconDir + iconFileNames[i] + " in the classpath.");
      }

      lineStyleTestIcons = new ImageIcon[lineStyleIconFileNames.length];
      for (int i = 0; i < lineStyleIconFileNames.length; i++)
      {
         java.net.URL url = getClass().getResource(lineStyleIconDir + lineStyleIconFileNames[i]);
         if (url != null)
            lineStyleTestIcons[i] = new ImageIcon(url);
         else
            System.err.println("Can't find file " + lineStyleIconDir + lineStyleIconFileNames[i] + " in the classpath.");
      }

      symbolTestIcons = new ImageIcon[symbolIconFileNames.length];
      for (int i = 0; i < symbolIconFileNames.length; i++)
      {
         java.net.URL url = getClass().getResource(symbolIconDir + symbolIconFileNames[i]);
         if (url != null)
         symbolTestIcons[i] = new ImageIcon(url);
         else
            System.err.println("Can't find file " + symbolIconDir + symbolIconFileNames[i] + " in the classpath.");
      }
   } // setUp()



   /**
    * Test Suite for running all tests.
    *
    * @author Daniel Gatti
    */
   public static Test suite()
   {
      TestSuite suite= new TestSuite();
      suite.addTest(new TestPrettyConverters("testAddingStrings"));
      suite.addTest(new TestPrettyConverters("testSortingStrings"));
      suite.addTest(new TestPrettyConverters("testAddingIcons"));
      suite.addTest(new TestPrettyConverters("testLineStyleIcons"));
      suite.addTest(new TestPrettyConverters("testSymbolStyleIcons"));
      return suite;
   }
} // class TestPrettyConverters

