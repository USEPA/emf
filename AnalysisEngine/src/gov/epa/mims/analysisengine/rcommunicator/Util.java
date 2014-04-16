package gov.epa.mims.analysisengine.rcommunicator;

import gov.epa.mims.analysisengine.tree.DateDataSetIfc;
import gov.epa.mims.analysisengine.tree.Text;

import java.awt.Color;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.StringTokenizer;


/**
 * DOCUMENT ME!
 *
 * @version $Revision: 1.2 $
 * @author $author$
 */
public class Util implements java.io.Serializable
{
   /** DOCUMENT ME! */
   static final long serialVersionUID = 1;

   /**
    * DOCUMENT_ME
    *
    * @param colors DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   public static String[] parseColors(Color[] colors)
   {
      String[] results = new String[] 
      {
         "NULL"
      };

      if (colors != null)
      {
         results = new String[colors.length];

         for (int i = 0; i < colors.length; i++)
         {
            results[i] = parseColor(colors[i]);
         }
      }

      return results;
   }

   /**
    * DOCUMENT_ME
    *
    * @param color DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   public static String parseColor(Color color)
   {
      if (color == null)
      {
         return "NULL";
      }

      int radix = 16;
      String result = "#";
      result += intToRadix(color.getRed(), radix);
      result += intToRadix(color.getGreen(), radix);
      result += intToRadix(color.getBlue(), radix);

      return escapeQuote(result);
   }

   /**
    * DOCUMENT_ME
    *
    * @param b DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   public static String parseBoolean(boolean b)
   {
      String result = "FALSE";
      if(b)
         result = "TRUE";

      //return escapeQuote(result);
      return result;
   }

   /**
    * DOCUMENT_ME
    *
    * @param b DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   public static String[] parseBoolean(boolean[] b)
   {
      String[] results = new String[b.length];

      for (int i = 0; i < b.length; i++)
      {
         results[i] = parseBoolean(b[i]);
      }

      return results;
   }

   /**
    * DOCUMENT_ME
    *
    * @param array DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   public static String[] escapeQuote(String[] array)
   {
      String[] escaped = new String[array.length];

      for (int i = 0; i < array.length; i++)
      {
         escaped[i] = escapeQuote(array[i]);
      }

      return escaped;
   }

   /**
    * DOCUMENT_ME
    *
    * @param s DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   public static String escapeQuote(String s)
   {
      return "\"" + s + "\"";
   }

   /**
    * DOCUMENT_ME
    *
    * @param i DOCUMENT_ME
    * @param radix DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   private static String intToRadix(int i, int radix)
   {
      if (i < radix)
      {
         return "0" + Integer.toString(i, radix);
      }

      return Integer.toString(i, radix);
   }

   /**
    * DOCUMENT_ME
    *
    * @param position DOCUMENT_ME
    * @param xjust DOCUMENT_ME
    * @param yjust DOCUMENT_ME
    * @param xRvar DOCUMENT_ME
    * @param yRvar DOCUMENT_ME
    * @param rZoneFunction DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   public static String[] news2XY(String position, double xjust, double yjust, 
      String xRvar, String yRvar, String rZoneFunction)
   {
      //
      //          columns
      //         0    1    2
      //
      //    0   NW  | N  | NE
      // r      --------------
      // o  1    W  | C  | E
      // w      --------------
      //    2   SW  | S  | SE
      //
      //
      int r;
      r = (position.indexOf("N") >= 0)
          ? 0
          : 1;
      r = (position.indexOf("S") >= 0)
          ? 2
          : r;

      int c;
      c = (position.indexOf("W") >= 0)
          ? 0
          : 1;
      c = (position.indexOf("E") >= 0)
          ? 2
          : c;

      //
      // add position commands to rCommands
      //
      String xposCmd = xRvar + "<-(" + xjust + "*(" + rZoneFunction + "[2+" + c
                       + "] - ";
      xposCmd += (rZoneFunction + "[1+" + c + "] )" + "+" + rZoneFunction + "[1+" + c + "] )");

      String yposCmd = yRvar + "<-(" + yjust + "*(" + rZoneFunction + "[8-" + r
                       + "] - ";
      yposCmd += (rZoneFunction + "[7-" + r + "] )" + "+" + rZoneFunction + "[7-" + r + "] )");

      return new String[] 
      {
         xposCmd, 
         yposCmd
      };
   }

   /**
    * DOCUMENT_ME
    *
    * @param funcName DOCUMENT_ME
    * @param options DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   public static String buildArrayCommand(String funcName, String[] options)
   {
      String cmd = funcName + "(";

      for (int i = 0; i < options.length; i++)
      {
         cmd += options[i];

         if (i < (options.length - 1))
         {
            cmd += ",";
         }
      }

      cmd += ")";

      return cmd;
   }

   /**
    * DOCUMENT_ME
    *
    * @param funcName DOCUMENT_ME
    * @param dbl DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   public static String buildArrayCommand(String funcName, double[] dbl)
   {
      String cmd = funcName + "(";

      for (int i = 0; i < dbl.length; i++)
      {
         cmd += Double.toString(dbl[i]);

         if (i < (dbl.length - 1))
         {
            cmd += ",";
         }
      }

      cmd += ")";

      return cmd;
   }

   /**
    * DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   public static String zoneFunction()
   {
      //
      //                       <-8
      //        NW  | N  | NE
      //        -------------- <-7
      //         W  | C  | E
      //        -------------- <-6
      //        SW  | S  | SE
      //                       <-5
      //       ^    ^    ^    ^
      //       |    |    |    |
      //       1    2    3    4
      //
      //
      String func = "zone<-function() {";
      func += "w1 <- ((par(\"usr\")[2]-par(\"usr\")[1])/3);";
      func += "w2 <- ((par(\"usr\")[4]-par(\"usr\")[3])/3);";
      func += "c( (par(\"usr\")[1]   ),(par(\"usr\")[1]+w1),";
      func += "(par(\"usr\")[2]-w1),(par(\"usr\")[2]   ),";
      func += "(par(\"usr\")[3]   ),(par(\"usr\")[3]+w2),";
      func += "(par(\"usr\")[4]-w2),(par(\"usr\")[4]   ) )}";

      return func;
   }

   /**
    * DOCUMENT_ME
    *
    * @param strArray DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   public static String[] parseSymbols(String[] strArray)
   {
      String[] results = new String[strArray.length];

      for (int i = 0; i < strArray.length; i++)
      {
         results[i] = (String) OptMap.getSymbol(strArray[i]);

         if (results[i] == null)
         {
            results[i] = escapeQuote(strArray[i]);
         }
      }

      return results;
   }

   /**
    * DOCUMENT_ME
    *
    * @param strArray DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   public static String[] parseLineTypes(String[] strArray)
   {
      String[] results = new String[strArray.length];

      for (int i = 0; i < strArray.length; i++)
      {
         results[i] = parseLineTypes(strArray[i]);
      }

      return results;
   }

   /**
    * DOCUMENT_ME
    *
    * @param str DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   public static String parseLineTypes(String str)
   {
      String results;
      results = (String) OptMap.getLineType(str);

      if (results == null)
      {
         results = escapeQuote(str);
      }

      return results;
   }

   /**
    * DOCUMENT_ME
    *
    * @param prefix DOCUMENT_ME
    * @param suffix DOCUMENT_ME
    * @param deleteOnExit DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    *
    * @throws java.io.IOException DOCUMENT_ME
    ********************************************************/
   public static String createTmpFile(String prefix, String suffix, 
      boolean deleteOnExit) throws java.io.IOException
   {
      File tmp = File.createTempFile(prefix, "." + suffix);
      String tmpFilename = tmp.getCanonicalPath();

      if (deleteOnExit)
      {
         tmp.deleteOnExit();
      }

      //
      // TEC 10-10-2002
      // added tmpFilenameForwardSlash because R 
      // expects the backslashes in the filename
      // to be escaped with '\'. However, after 
      // reading "R for Windows FAQ #2.12" it is
      // clear the R will work with forward slashes. 
      // Replacing backslashes with forward
      // slashes is a lot easier done with a single 
      // call to replace( char , char ) than
      // escaping each backslash.
      //
      String rtrnStr = tmpFilename.replace('\\', '/');

      return rtrnStr;
   }

   /**
    * DOCUMENT_ME
    *
    * @param ts DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   public static boolean[] calendarChanges(DateDataSetIfc ts)
   {
      boolean[] totalChanges = new boolean[] 
      {
         false, 
         false, 
         false, 
         false, 
         false, 
         false, 
         false
      };

      try
      {
         ts.open();

         int numElements = ts.getNumElements();

         for (int i = 1; i < numElements; i++)
         {
            //            Object[] datum = ts.getTimeStamp(i-1);
            //            Date date1 = (Date)datum[0];
            Date date1 = ts.getDate(i - 1);
            GregorianCalendar g1 = new GregorianCalendar();
            g1.setTime(date1);

            //            datum = ts.getTimeStamp(i);
            //            Date date2 = (Date)datum[0];
            Date date2 = ts.getDate(i);
            GregorianCalendar g2 = new GregorianCalendar();
            g2.setTime(date2);

            boolean[] deltas = calendarDiff(g2, g1);

            for (int j = 0; j < totalChanges.length; j++)
            {
               totalChanges[j] = (deltas[j])
                                 ? (deltas[j])
                                 : (totalChanges[j]);
            }
         }

         ts.close();
      }
      catch (java.lang.Exception e)
      {
         e.printStackTrace();
      }

      return totalChanges;
   }

   /**
    * DOCUMENT_ME
    *
    * @param cal1 DOCUMENT_ME
    * @param cal2 DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   public static boolean[] calendarDiff(Calendar cal1, Calendar cal2)
   {
      boolean[] delta = new boolean[7];
      Calendar c1 = (Calendar) cal1.clone();
      Calendar c2 = (Calendar) cal2.clone();
      delta[0] = (boolean) calendarFieldDiff(c1, c2, Calendar.YEAR);
      delta[1] = (boolean) calendarFieldDiff(c1, c2, Calendar.MONTH);
      delta[2] = (boolean) calendarFieldDiff(c1, c2, Calendar.DATE);
      delta[3] = (boolean) calendarFieldDiff(c1, c2, Calendar.HOUR);
      delta[4] = (boolean) calendarFieldDiff(c1, c2, Calendar.MINUTE);
      delta[5] = (boolean) calendarFieldDiff(c1, c2, Calendar.SECOND);
      delta[6] = (boolean) calendarFieldDiff(c1, c2, Calendar.MILLISECOND);

      return delta;
   }

   /**
    * DOCUMENT_ME
    *
    * @param cal1 DOCUMENT_ME
    * @param cal2 DOCUMENT_ME
    * @param field DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   public static boolean calendarFieldDiff(Calendar cal1, Calendar cal2, 
      int field)
   {
      return (cal1.get(field) != cal2.get(field))
             ? true
             : false;
   }

   /**
    * convert an array of double to an array of String
    *
    *
    * @param array an array of doubles to be converted
    * @return double array values as Strings
    *
    * @pre array != null
    *******************************************************************/
   public static String[] parseNumbers(double[] array)
   {
      String[] results = new String[array.length];

      for (int i = 0; i < array.length; i++)
      {
         results[i] = Double.toString(array[i]);
      }

      return results;
   }

   /**
    * DOCUMENT_ME
    *
    * @param str DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   public static String slash2DoubleBackSlash(String str)
   {
      String rtrnStr = "";

      for (int i = 0; i < str.length(); ++i)
      {
         if (str.charAt(i) == '/')
         {
            rtrnStr += "\\";
         }
         else
         {
            rtrnStr += str.charAt(i);
         }
      }

      return rtrnStr;
   }

   /**
    * DOCUMENT_ME
    *
    * @param str DOCUMENT_ME
    * @param from DOCUMENT_ME
    * @param to DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   public static String charReplace(String str, char from, char to)
   {
      String rtrnStr = "";

      for (int i = 0; i < str.length(); ++i)
      {
         if (str.charAt(i) == from)
         {
            rtrnStr += to;
         }
         else
         {
            rtrnStr += str.charAt(i);
         }
      }

      return rtrnStr;
   }

   /**
    * DOCUMENT_ME
    *
    * @param str DOCUMENT_ME
    * @param chr DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   public static String escapeChar(String str, char chr)
   {
      String rtrnStr = "";

      for (int i = 0; i < str.length(); ++i)
      {
         if (str.charAt(i) == chr)
         {
            rtrnStr += ("\\" + chr);
         }
         else
         {
            rtrnStr += str.charAt(i);
         }
      }

      return rtrnStr;
   }

   /**
    * convert an Object to a String array
    *
    *
    * @param obj an Object to be converted into a string array
    * @return an Array String made from Object obj
    *
    * @pre obj != null
    *******************************************************************/
   public static String[] convertToStringArray(Object obj)
   {
      String[] strArray = null;

      if (obj instanceof String[])
      {
         strArray = (String[]) obj;
      }
      else if (obj instanceof String)
      {
         strArray = new String[1];
         strArray[0] = (String) obj;
      }
      else if (obj instanceof int[])
      {
         strArray = new String[((int[]) obj).length];

         for (int i = 0; i < ((int[]) obj).length; i++)
         {
            strArray[i] = "" + ((int[]) obj)[i];
         }
      }
      else if (obj instanceof double[])
      {
         strArray = new String[((double[]) obj).length];

         for (int i = 0; i < ((double[]) obj).length; i++)
         {
            strArray[i] = "" + ((double[]) obj)[i];
         }
      }
      else
      {
         System.out.println("\n\nERROR unknown option type\n");
      }

      return strArray;
   }

   /**
    * DOCUMENT_ME
    *
    * @param targetFile DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   public static String windowsStartBatFile(String targetFile)
   {
      String tmpFilename = null;

      try
      {
         int index = targetFile.lastIndexOf("/") + 1;
         String basename = targetFile.substring(index);
         String directory = targetFile.substring(0, index - 1);
         tmpFilename = Util.createTmpFile("Rdisplay" + basename, "bat", false);

         PrintWriter out = new PrintWriter(new FileOutputStream(tmpFilename));
         String dirBackSlashed = slash2DoubleBackSlash(directory);


         // change to dirBackSlashed directory
         out.println(dirBackSlashed.substring(0, 2));
         out.println("cd " + Util.escapeQuote(dirBackSlashed.substring(2)));


         // run start command on file
         out.println("start " + basename);
         out.close();
      }
      catch (java.io.IOException e)
      {
         e.printStackTrace();
      }

      return tmpFilename;
   }

   /**
    * DOCUMENT_ME
    *
    * @param obj DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   public static String toString(Object obj)
   {
      Class c1 = obj.getClass();
      String r = c1.getName();

      do
      {
         r += "[";

         Field[] fields = c1.getDeclaredFields();
         AccessibleObject.setAccessible(fields, true);

         for (int i = 0; i < fields.length; ++i)
         {
            Field f = fields[i];
            r += (f.getName() + "=");

            try
            {
               Object val = f.get(obj);
               r += ((val == null)
                     ? "value is null"
                     : val.toString());
            }
            catch (Exception e)
            {
               e.printStackTrace();
            }

            if (i < (fields.length - 1))
            {
               r += ", ";
            }
         }

         r += "]";
         c1 = c1.getSuperclass();
      }
      while (c1 != Object.class);

      return r;
   }

   /**
    * DOCUMENT_ME
    *
    * @param txt DOCUMENT_ME
    * @param r DOCUMENT_ME
    * @param p DOCUMENT_ME
    * @param xj DOCUMENT_ME
    * @param yj DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   public static Text textOverRide(Text txt, String r, String p, double xj, 
      double yj)
   {
      Text clone = (Text) txt.clone();
      String reg = clone.getRegion();
//System.out.println("reg= "+reg);
//      if( !txt.getPosition().equals(Text.RELATIVE2XAXIS) )
//      {
         String pos = clone.getPosition();
         xj = (clone.getXjustSet())
              ? (clone.getXJustification())
              : xj;
         yj = (clone.getYjustSet())
              ? (clone.getYJustification())
              : yj;
         pos = (pos == null)
               ? p
               : pos;
         reg = (reg == null)
               ? r
               : reg;
         clone.setPosition(reg, pos, xj, yj);
//      }

      return clone;
   }

   /**
    * DOCUMENT_ME
    *
    * @param c DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   protected static String getBasenamePath(Class c)
   {
      String classpathStr = System.getProperty("java.class.path");
      String separator = System.getProperty("file.separator");
      String pathSeparator = System.getProperty("path.separator");
      String className = c.getName();
      String partialPath = className.replace('.', separator.charAt(0));
      StringTokenizer st = new StringTokenizer(classpathStr, pathSeparator);
      String basenamePath = null;

      while (st.hasMoreElements())
      {
         String token = (String) st.nextElement();

         if (!(token.charAt(token.length() - 1) == separator.charAt(0)))
         {
            token += separator;
         }

         basenamePath = token + partialPath;

         String fullPath = basenamePath + ".class";
         File f = new File(fullPath);

         if (f.exists())
         {
            break;
         }
      }

      return basenamePath;
   }

   public static String protectNewLineChars(String str )
   {
      StringBuffer b = new StringBuffer();
      String[] tmp = str.split("\\n");
      for(int j = 0;j<tmp.length;j++)
      {
         b.append(tmp[j]);
         if( j < tmp.length -1 )
         {
            b.append("\\n");
         }
      }
      return b.toString();
   }

}
