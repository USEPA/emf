package gov.epa.mims.analysisengine.rcommunicator;

import java.util.HashMap;


/**
 * DOCUMENT ME!
 *
 * @version $Revision: 1.2 $
 * @author Tommy E. Cathey
 */
public class OptMap implements java.io.Serializable
{
   /** DOCUMENT ME! */
   static final long serialVersionUID = 1;

   /** DOCUMENT_ME */
   private static HashMap lineTypeMap = new HashMap();

   /** DOCUMENT_ME */
   private static HashMap typeOfPlotMap = new HashMap();

   /** DOCUMENT_ME */
   private static HashMap symbolMap = new HashMap();

   static
   {
      lineTypeMap.put("BLANK", "0");
      lineTypeMap.put("SOLID", "1");
      lineTypeMap.put("DASHED", "2");
      lineTypeMap.put("DOTTED", "3");
      lineTypeMap.put("DOTDASH", "4");
      lineTypeMap.put("LONGDASH", "5");
      lineTypeMap.put("TWODASH", "6");

      typeOfPlotMap.put("POINTS", "p");
      typeOfPlotMap.put("LINES", "l");
      typeOfPlotMap.put("POINTS_n_LINES", "b");
      typeOfPlotMap.put("HISTOGRAM", "h");
      typeOfPlotMap.put("STAIR_STEPS", "s");
      typeOfPlotMap.put("NO_PLOTTING", "n");

      symbolMap.put("CIRCLE", "1");
      symbolMap.put("TRIANGLE_UP", "2");
      symbolMap.put("PLUS", "3");
      symbolMap.put("CROSS", "4");
      symbolMap.put("SQUARE_ROTATED", "5");
      symbolMap.put("TRIANGLE_DOWN", "6");
      symbolMap.put("CROSS_IN_SQUARE", "7");
      symbolMap.put("STARBURST", "8");
      symbolMap.put("PLUS_IN_SQUARE_ROTATED", "9");
      symbolMap.put("PLUS_IN_CIRCLE", "10");
      symbolMap.put("TRIANGLE_UP_AND_DOWN", "11");
      symbolMap.put("PLUS_IN_SQUARE", "12");
      symbolMap.put("CIRCLE_AND_CROSS", "13");
      symbolMap.put("UP_TRIANGLE_IN_SQUARE", "14");
      symbolMap.put("SQUARE_SOLID", "15");
      symbolMap.put("CIRCLE_SOLID", "16");
      symbolMap.put("UP_TRIANGLE_SOLID", "17");
      symbolMap.put("SQUARE_ROTATED_SOLID", "18");
      symbolMap.put("CIRCLE_FILLED", "19");
      symbolMap.put("BULLET", "20");
      symbolMap.put("CIRCLE2", "21");
      symbolMap.put("SQUARE", "22");
      symbolMap.put("DIAMOND", "23");
      symbolMap.put("*", "\"*\"");
      symbolMap.put("#", "\"#\"");
      symbolMap.put("!", "\"!\"");
      symbolMap.put("@", "\"@\"");
      symbolMap.put("$", "\"$\"");
      symbolMap.put("%", "\"%\"");
      symbolMap.put("^", "\"^\"");
      symbolMap.put("&", "\"&\"");
      symbolMap.put(")", "\")\"");
      symbolMap.put("(", "\"(\"");
      symbolMap.put("_", "\"_\"");
      symbolMap.put("-", "\"-\"");
      symbolMap.put("+", "\"+\"");
      symbolMap.put("=", "\"=\"");
      symbolMap.put("|", "\"|\"");
      symbolMap.put("\\", "\"\\\"");
      symbolMap.put(":", "\":\"");
      symbolMap.put(";", "\";\"");
      symbolMap.put("?", "\"?\"");
      symbolMap.put("/", "\"/\"");
      symbolMap.put("a", "\"a\"");
      symbolMap.put("b", "\"b\"");
      symbolMap.put("c", "\"c\"");
      symbolMap.put("d", "\"d\"");
      symbolMap.put("e", "\"e\"");
      symbolMap.put("f", "\"f\"");
      symbolMap.put("g", "\"g\"");
      symbolMap.put("h", "\"h\"");
      symbolMap.put("i", "\"i\"");
      symbolMap.put("j", "\"j\"");
      symbolMap.put("k", "\"k\"");
      symbolMap.put("l", "\"l\"");
      symbolMap.put("m", "\"m\"");
      symbolMap.put("n", "\"n\"");
      symbolMap.put("o", "\"o\"");
      symbolMap.put("p", "\"p\"");
      symbolMap.put("q", "\"q\"");
      symbolMap.put("r", "\"r\"");
      symbolMap.put("s", "\"s\"");
      symbolMap.put("t", "\"t\"");
      symbolMap.put("u", "\"u\"");
      symbolMap.put("v", "\"v\"");
      symbolMap.put("w", "\"w\"");
      symbolMap.put("x", "\"x\"");
      symbolMap.put("y", "\"y\"");
      symbolMap.put("z", "\"z\"");
      symbolMap.put("A", "\"A\"");
      symbolMap.put("B", "\"B\"");
      symbolMap.put("C", "\"C\"");
      symbolMap.put("D", "\"D\"");
      symbolMap.put("E", "\"E\"");
      symbolMap.put("F", "\"F\"");
      symbolMap.put("G", "\"G\"");
      symbolMap.put("H", "\"H\"");
      symbolMap.put("I", "\"I\"");
      symbolMap.put("J", "\"J\"");
      symbolMap.put("K", "\"K\"");
      symbolMap.put("L", "\"L\"");
      symbolMap.put("M", "\"M\"");
      symbolMap.put("N", "\"N\"");
      symbolMap.put("O", "\"O\"");
      symbolMap.put("P", "\"P\"");
      symbolMap.put("Q", "\"Q\"");
      symbolMap.put("R", "\"R\"");
      symbolMap.put("S", "\"S\"");
      symbolMap.put("T", "\"T\"");
      symbolMap.put("U", "\"U\"");
      symbolMap.put("V", "\"V\"");
      symbolMap.put("W", "\"W\"");
      symbolMap.put("X", "\"X\"");
      symbolMap.put("Y", "\"Y\"");
      symbolMap.put("Y", "\"Y\"");
   }

   /**
    * DOCUMENT_ME
    *
    * @param key DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   public static String getLineType(Object key)
   {
      return (String) lineTypeMap.get(key);
   }

   /**
    * DOCUMENT_ME
    *
    * @param key DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   public static String getTypeOfPlot(Object key)
   {
      return (String) typeOfPlotMap.get(key);
   }

   /**
    * DOCUMENT_ME
    *
    * @param key DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   public static String getSymbol(Object key)
   {
      return (String) symbolMap.get(key);
   }

   /**
    * describe object in a String
    *
    * @return String describing object
    ******************************************************/
   public String toString()
   {
      return Util.toString(this);
   }
}
