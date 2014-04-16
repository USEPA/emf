package gov.epa.mims.analysisengine.rcommunicator;

import gov.epa.mims.analysisengine.AnalysisEngineConstants;
import gov.epa.mims.analysisengine.AnalysisException;
import gov.epa.mims.analysisengine.tree.AnalysisOptions;
import gov.epa.mims.analysisengine.tree.DisplaySizeType;
import gov.epa.mims.analysisengine.tree.Axis;
import gov.epa.mims.analysisengine.tree.AxisNumeric;
import gov.epa.mims.analysisengine.tree.AxisCategory;
import gov.epa.mims.analysisengine.tree.BoxPlot;
import gov.epa.mims.analysisengine.tree.BoxType;
import gov.epa.mims.analysisengine.tree.TornadoPlot;
import gov.epa.mims.analysisengine.tree.BarPlot;
import gov.epa.mims.analysisengine.tree.BarType;
import gov.epa.mims.analysisengine.tree.DataSetIfc;
import gov.epa.mims.analysisengine.tree.DataSets;
import gov.epa.mims.analysisengine.tree.DiscreteCategoryPlot;
import gov.epa.mims.analysisengine.tree.GridType;
import gov.epa.mims.analysisengine.tree.HistogramPlot;
import gov.epa.mims.analysisengine.tree.LabeledDataSetIfc;
import gov.epa.mims.analysisengine.tree.Layout;
import gov.epa.mims.analysisengine.tree.Legend;
import gov.epa.mims.analysisengine.tree.LineType;
import gov.epa.mims.analysisengine.tree.VisitorIfc;
import gov.epa.mims.analysisengine.tree.OutlineType;
import gov.epa.mims.analysisengine.tree.Page;
import gov.epa.mims.analysisengine.tree.PageType;
import gov.epa.mims.analysisengine.tree.Plot;
import gov.epa.mims.analysisengine.tree.RankOrderPlot;
import gov.epa.mims.analysisengine.tree.ScatterPlot;
import gov.epa.mims.analysisengine.tree.CdfPlot;
import gov.epa.mims.analysisengine.tree.Text;
import gov.epa.mims.analysisengine.tree.TimeSeries;

import java.awt.Color;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.lang.reflect.Method;


/**
 * class_description
 *
 * @author Tommy E. Cathey
 * @version $Id: RCommandGenerator.java,v 1.2 2005/09/19 14:50:10 rhavaldar Exp $
 *
 **/
public class RCommandGenerator 
extends
gov.epa.mims.analysisengine.tree.Visitor
implements Serializable, Cloneable,
gov.epa.mims.analysisengine.tree.AnalysisOptionConstantsIfc
{
   /******************************************************
    *
    * fields
    *
    *****************************************************/
   static final long serialVersionUID = 1;

   private static String selectedDevice = null;

   /** DOCUMENT ME! */
   private ArrayList rCommands;

   /** DOCUMENT ME! */
   private ArrayList rCommands2;

   /** DOCUMENT ME! */
   private String dataFrame = "plotData";

   /******************************************************
    *
    * methods
    *
    *****************************************************/
   public RCommandGenerator()
   {
      rCommands = new ArrayList();
      rCommands2 = new ArrayList();
   }

   public static String getSelectedDevice()
   {
      return selectedDevice;
   }

   /**
    * return the list of R commands generated
    *
    *
    * @return list of R commands
    *
    *******************************************************************/
   public ArrayList getCommands()
   {
      return rCommands;
   }

   /**
    * method_description
    *
    *
    * @param p Page being visited
    *
    * @preXXX p != null
    *******************************************************************/
   public Object visit(Page p)
   {
      String cmd;

      //
      // get listing of all Plot children
      //
      ArrayList plotList = new ArrayList();
      p.getLeaves(plotList);


      //
      // initialize R
      //
      rCommands.addAll(InitializeCmd.getCommands());

      //
      // set the device type
      //
      PageType pageType = Option.getPageType(p);

      selectedDevice =null;

      if (pageType == null)
      {
         selectedDevice = AnalysisEngineConstants.DEFAULT_DEVICE; // the default device
      }
      else
      {
         selectedDevice = pageType.getForm();
      }

      if (selectedDevice.equals("X11"))
      {
         x11Cmd(p);
      }
      else if (selectedDevice.equals("SCREEN"))
      {
         x11Cmd(p);
      }
      else if (selectedDevice.equals("JPEG"))
      {
         jpgCmd(p);
      }
      else if (selectedDevice.equals("PS"))
      {
         psCmd(p);
      }
      else if (selectedDevice.equals("PDF_READER"))
      {
         pdfReaderCmd(p);
      }
      else if (selectedDevice.equals("PDF"))
      {
         pdfCmd(p);
      }
      else if (selectedDevice.equals("PNG"))
      {
         pngCmd(p);
      }
      else if (selectedDevice.equals("PICTEC"))
      {
         pictecCmd(p);
      }

      //
      // set layout if requested
      //
      if (pageType != null)
      {
         Layout layout = pageType.getLayout();

         if (layout != null)
         {
            layoutCmd(p);
         }
      }

      //
      // visit each plot to generate plot commands
      //
      for (int i = 0; i < plotList.size(); i++)
      {
         ((gov.epa.mims.analysisengine.tree.VisitableIfc)plotList.get(i)).accept(this);
      }

      rCommands.addAll(rCommands2);

      //
      // generate detach command
      //
      rCommands.add("par()");
      cmd = "detach(" + dataFrame + ")";
      rCommands.add(cmd);


      //
      // generate rm data frame command
      //
      cmd = "rm(" + dataFrame + ")";
      rCommands.add(cmd);
      return null;
   }

   /**
    * visit AnalysisOptions Node
    *
    *
    * @param p AnalysisOptions being visited
    *
    * @preXXX p != null
    *******************************************************************/
   public Object visit(AnalysisOptions p)
   {
      return null;
   }

   /**
    * visit DataSets
    *
    *
    * @param p DataSets to visit
    *
    * @preXXX p != null
    *******************************************************************/
   public Object visit(DataSets p)
   {
      return null;
   }

   /**
    * add x11() command to rCommands list
    *
    *
    *******************************************************************/
   private void layoutCmd(Page p)
   {
      PageType pageType = Option.getPageType(p);
      Layout layout = pageType.getLayout();
      int[][] matrix = layout.getMatrix();

      //
      // the layout matrix
      //
      String[] matrixStringValues = new String[(matrix.length) * (matrix[0].length)];
      int i = 0;

      for (int r = 0; r < matrix.length; r++)
      {
         for (int c = 0; c < matrix[0].length; c++)
         {
            matrixStringValues[i++] = Integer.toString(matrix[r][c]);
         }
      }

      //
      // set widths and heights of layout
      //
      // absolute widths and heights will override relative widths and height
      //
      String widths = null;
      String heights = null;

      double[] absWidths = layout.getAbsoluteWidths();
      double[] absHeights = layout.getAbsoluteHeights();

      if ((absWidths != null) || (absHeights != null))
      {
         if (absWidths != null)
         {
            String[] temp = new String[absWidths.length];

            for (int j = 0; j < absWidths.length; j++)
            {
               temp[j] = "lcm(" + absWidths[j] + ")";
            }

            widths = Util.buildArrayCommand("c", temp);
         }

         if (absHeights != null)
         {
            String[] temp = new String[absHeights.length];

            for (int j = 0; j < absHeights.length; j++)
            {
               temp[j] = "lcm(" + absHeights[j] + ")";
            }

            heights = Util.buildArrayCommand("c", temp);
         }
      }
      else
      {
         //
         // relative widths
         //
         int[] relWidths = layout.getRelativeWidths();
         widths = (relWidths == null)
                  ? "1"
                  : Util.buildArrayCommand("c", 
                                           convertToStringArray(relWidths));

         //
         // relative heights
         //
         int[] relHeights = layout.getRelativeHeights();
         heights = (relHeights == null)
                   ? "1"
                   : Util.buildArrayCommand("c", 
                                            convertToStringArray(relHeights));
      }

      //
      // respect width units and height units
      //
      String respect = (layout.getWidthUnitEqualsHeightUnit() == false)
                       ? "FALSE"
                       : "TRUE";

      //
      // generate layout command
      //
      String cmd = "layout(matrix("
                   + Util.buildArrayCommand("c", matrixStringValues);
      cmd += ("," + matrix.length + "," + matrix[0].length + ",byrow=TRUE)");
      cmd += (",widths=" + widths);
      cmd += (",heights=" + heights);
      cmd += (",respect=" + respect + ")");

      rCommands.add(cmd);

      //
      // layout title and subtitle
      //
      Text title = layout.getLayoutTitle();
      Text subtitle = layout.getLayoutSubTitle();

      if ((title != null) || (subtitle != null))
      {
         rCommands.add("o.par <- par(mar = rep(0, 4))");
         rCommands.add("plot.new()");

         if (title != null)
         {
            double xJustification = title.getXJustification();
            double yJustification = title.getYJustification();
            String yposCmd = "ypos<-(" + yJustification
                             + "*(par(\"usr\")[4]-par(\"usr\")[3]) + par(\"usr\")[3])";
            String xposCmd = "xpos<-(" + xJustification
                             + "*(par(\"usr\")[2]-par(\"usr\")[1]) + par(\"usr\")[1])";
            rCommands.add(yposCmd);
            rCommands.add(xposCmd);
            textCmd(title, "xpos", "ypos");
         }

         if (subtitle != null)
         {
            double xJustification = subtitle.getXJustification();
            double yJustification = subtitle.getYJustification();
            String yposCmd = "ypos<-(" + yJustification
                             + "*(par(\"usr\")[4]-par(\"usr\")[3]) + par(\"usr\")[3])";
            String xposCmd = "xpos<-(" + xJustification
                             + "*(par(\"usr\")[2]-par(\"usr\")[1]) + par(\"usr\")[1])";
            rCommands.add(yposCmd);
            rCommands.add(xposCmd);
            textCmd(subtitle, "xpos", "ypos");
         }

         rCommands.add("par(o.par)");
      }
   }

   /**
    * add x11() command to rCommands list
    *
    *
    *******************************************************************/
   private void x11Cmd(Page p)
   {
      boolean usingDOSstartCommand = true;

      double[] wh = new double[]{10,8};
      DisplaySizeType o = null;
      o = (DisplaySizeType)p.getOption(DISPLAY_SIZE_TYPE);
      wh = (o != null)?(o.getDisplay()):(wh);
      
      if (usingDOSstartCommand)
      {
         String os = System.getProperty("os.name").startsWith("Windows")
                     ? "Windows"
                     : "Unix";

         if (os.equals("Windows"))
         {
            try
            {
               //
               // create a temporary file to be deleted on exit
               //
               PageType pageType = Option.getPageType(p);
               boolean deleteOnExitFlag = pageType.getDeleteTemporaryFileOnExit();
               String tmpFilename = Util.createTmpFile("Rdata", "pdf", 
                                                       deleteOnExitFlag);
               rCommands.add("pdf(file=\"" + tmpFilename + "\",width="+wh[0]+",height="+wh[1]+")");

               String batchFile = Util.windowsStartBatFile(tmpFilename);
               rCommands2.add("dev.off()");
               rCommands2.add("system('\"" + batchFile + "\"')");
            }
            catch (IOException e)
            {
               System.out.println("Error creating temporary file ");
               e.printStackTrace();
            }
         }
         else
         {
            rCommands.add("x11(width="+wh[0]+",height="+wh[1]+")");
         }
      }
      else
      {
         rCommands.add("x11(width="+wh[0]+",height="+wh[1]+")");
      }
   }

   /**
    * add postscript command to rCommands list
    *
    *
    * @param p Page for which postscript is requested
    *
    * @preXXX p != null
    *******************************************************************/
   private void psCmd(Page p)
   {
      double[] wh = new double[]{10,8};
      DisplaySizeType o = null;
      o = (DisplaySizeType)p.getOption(DISPLAY_SIZE_TYPE);
      //wh = (o != null)?(o.getDisplay()):(wh);
      wh = (o != null)?(convertInchesToPixels(o.getDisplay())):(wh);
      rCommands.add("ps(width="+wh[0]+",height="+wh[1]+")");
   }

   /**
    * add png command to rCommands list
    *
    *
    * @param p Page for which png is requested
    *
    * @preXXX p != null
    *******************************************************************/
   private void pngCmd(Page p)
   {
      double[] wh = new double[]{10,8};
      DisplaySizeType o = null;
      o = (DisplaySizeType)p.getOption(DISPLAY_SIZE_TYPE);
      wh = (o != null)?(convertInchesToPixels(o.getDisplay())):(wh);

      //      rCommands.add("png()");
      //
      // get the pageType analysisOptions for this page
      //
      PageType pageType = Option.getPageType(p);

      //
      // if filename is not set then use the default
      //
      String file;
      file = Util.escapeQuote(pageType.getFilename());

      if (file.equals("NULL"))
      {
         rCommands.add("png(width="+wh[0]+",height="+wh[1]+")");
      }
      else
      {
         rCommands.add("png(filename=" + file + ",width="+wh[0]+",height="+wh[1]+")");
      }
   }

   /**
    * add pictec command to rCommands list
    *
    *
    * @param p Page for which pictec is requested
    *
    * @preXXX p != null
    *******************************************************************/
   private void pictecCmd(Page p)
   {
      double[] wh = new double[]{10,8};
      DisplaySizeType o = null;
      o = (DisplaySizeType)p.getOption(DISPLAY_SIZE_TYPE);
      wh = (o != null)?(convertInchesToPixels(o.getDisplay())):(wh);

      rCommands.add("pictec(width="+wh[0]+",height="+wh[1]+")");
   }

   /**
    * add jpg command to rCommands list
    *
    *
    * @param p Page for which jpg is requested
    *
    * @preXXX p != null
    *******************************************************************/
   private void jpgCmd(Page p)
   {
      double[] wh = new double[]{10,8};
      DisplaySizeType o = null;
      o = (DisplaySizeType)p.getOption(DISPLAY_SIZE_TYPE);
      double [] userWH = convertInchesToPixels(o.getDisplay());
      wh = (o != null)?(userWH):(wh);

      //
      // get the pageType analysisOptions for this page
      //
      PageType pageType = Option.getPageType(p);

      //
      // if filename is not set then use the default
      //
      String file;
      file = Util.escapeQuote(pageType.getFilename());

      if (file.equals("NULL"))
      {
         rCommands.add("jpeg(width="+wh[0]+",height="+wh[1]+")");
      }
      else
      {
         rCommands.add("jpeg(filename=" + file + ",width="+wh[0]+",height="+wh[1]+")");
      }
   }

   /**
    * DOCUMENT_ME
    *
    * @param p DOCUMENT_ME
    ********************************************************/
   private void pdfReaderCmd(Page p)
   {
      double[] wh = new double[]{10,8};
      DisplaySizeType o = null;
      o = (DisplaySizeType)p.getOption(DISPLAY_SIZE_TYPE);
      wh = (o != null)?(o.getDisplay()):(wh);
      try
      {
         //
         // create a temporary file to be deleted on exit
         //
         PageType pageType = Option.getPageType(p);
         boolean deleteOnExitFlag = pageType.getDeleteTemporaryFileOnExit();
         String tmpFilename = Util.createTmpFile("Rdata", "pdf", 
                                                 deleteOnExitFlag);
         rCommands.add("pdf(file=\"" + tmpFilename + "\",width="+wh[0]+",height="+wh[1]+")");

         String pdfReader = pageType.getPDFreader();
         pdfReader = (pdfReader != null)
                     ? pdfReader
                     : AnalysisEngineConstants.DEFAULT_PDF_READER;
         rCommands2.add("dev.off()");

         String cmd;
         String os = System.getProperty("os.name").startsWith("Windows")
                     ? "Windows"
                     : "Unix";

         if (os.equals("Windows"))
         {
            tmpFilename = Util.slash2DoubleBackSlash(tmpFilename);
            cmd = "system('" + pdfReader + " " + tmpFilename + " ')";
         }
         else
         {
            cmd = "system(\"" + pdfReader + " " + tmpFilename + " &\")";
         }

         rCommands2.add(cmd);
      }
      catch (IOException e)
      {
         System.out.println("Error creating temporary file ");
         e.printStackTrace();
      }
   }
   
   /** a helper method to covert the inches to pixes
    */
   private double [] convertInchesToPixels(double [] display)
   {
         //creating a  new array, since we don't won't to modify the orignal
         //display size in Inches
         double [] newDisplay = new double[2];
         newDisplay[0] = display[0] * DisplaySizeType.PIXELS_PER_INCH;
         newDisplay[1] = display[1] * DisplaySizeType.PIXELS_PER_INCH;
         return newDisplay;
   }

   /**
    * add pdf command to rCommands list
    *
    *
    * @param p Page for which pdf is requested
    *
    * @preXXX p != null
    *******************************************************************/
   private void pdfCmd(Page p)
   {
      double[] wh = new double[]{10,8};
      DisplaySizeType o = null;
      o = (DisplaySizeType)p.getOption(DISPLAY_SIZE_TYPE);
      wh = (o != null)?(o.getDisplay()):(wh);

      //
      // get the pageType analysisOptions for this page
      //
      PageType pageType = Option.getPageType(p);

      //
      // if filename is not set then use the default
      //
      String file = "\"default.pdf\"";

      if (pageType != null)
      {
         file = Util.escapeQuote(pageType.getFilename());
      }

      rCommands.add("pdf(file=" + file + ",width="+wh[0]+",height="+wh[1]+")");
   }

   /**
    * R function which returns the figure boundaries in user coordinates
    *
    *
    *******************************************************************/
   private void figInUsrCoordFunction()
   {
      String func = "FigInUsrCoord<-function() {";
      func += "m1 <- (par(\"usr\")[2]-par(\"usr\")[1])/(par(\"plt\")[2]-par(\"plt\")[1]);";
      func += "m2 <- (par(\"usr\")[4]-par(\"usr\")[3])/(par(\"plt\")[4]-par(\"plt\")[3]);";
      func += "b1 <- par(\"usr\")[1] - m1 * par(\"plt\")[1];";
      func += "b2 <- par(\"usr\")[3] - m2 * par(\"plt\")[3];";
      func += "c( (b1), (m1 + b1),(b2), (m2 + b2) )}";
      rCommands.add(func);
   }

   /**
    * R title command
    *
    * @param p current Plot object
    *
    * @preXXX p != null
    *******************************************************************/
   private void titleCmd(Plot p)
   {
      Text text = (Text) p.getOption(PLOT_TITLE);

      if (text != null)
      {
         Text t = Util.textOverRide(text, Text.TOP_HAND_MARGIN, Text.NORTH, 
                                    0.5, 0.5);
         Text2Cmd textCmd = new Text2Cmd(t);
         rCommands.addAll(textCmd.getCommands());
      }
   }

   /**
    * R subtitle
    *
    *
    * @param p current Plot object
    *
    * @preXXX p != null
    *******************************************************************/
   private void subtitleCmd(Plot p)
   {
      Text text = (Text) p.getOption(PLOT_SUBTITLE);

      if (text != null)
      {
         Text t = Util.textOverRide(text, Text.TOP_HAND_MARGIN, Text.SOUTH, 
                                    0.5, 0.5);
         Text2Cmd textCmd = new Text2Cmd(t);
         rCommands.addAll(textCmd.getCommands());
      }
   }

   /**
    * R footer command
    *
    *
    * @param p current Plot object
    *
    * @preXXX p != null
    *******************************************************************/
   private void footerCmd(Plot p)
   {
      Text footer = (Text) p.getOption(PLOT_FOOTER);

      if (footer != null)
      {
         Text t = Util.textOverRide(footer, Text.BOTTOM_HAND_MARGIN, 
                                    Text.SOUTH, 0.5, 0.15);
         Text2Cmd textCmd = new Text2Cmd(t);
         rCommands.addAll(textCmd.getCommands());
      }
   }

   /**
    * generic routine for text commands
    *
    *
    * @param text current text object
    * @param xPos x position to draw text
    * @param yPos y position to draw text
    *
    * @preXXX text != null
    * @preXXX xPos != null
    * @preXXX yPos != null
    *******************************************************************/
   private void textCmd(Text text, String xPos, String yPos)
   {
      if (text != null)
      {
         String textString = text.getTextString();
         String typeface = text.getTypeface();
         String style = text.getStyle();
         double textExpansion = text.getTextExpansion();
         double srt = text.getTextDegreesRotation();
         String col = Util.parseColor(text.getColor());

         String vfont = "NULL";

         if ((typeface != null) && (style != null))
         {
            vfont = "c(" + Util.escapeQuote(typeface) + ","
                    + Util.escapeQuote(style) + ")";
         }

         String labelCmd = "text(x=" + xPos;
         labelCmd += (",y=" + yPos);
         labelCmd += (",labels=" + Util.escapeQuote(textString));
         labelCmd += (",xpd=" + "TRUE");
         labelCmd += (",cex=" + textExpansion);
         labelCmd += (",vfont=" + vfont);
         labelCmd += (",srt=" + srt);
         labelCmd += (",col=" + col);
         labelCmd += ")";
         rCommands.add(labelCmd);
         rCommands.add("par(\"xpd\"=FALSE)");
      }
   }

   /**
    * convert an Object to a String array
    *
    *
    * @param obj an Object to be converted into a string array
    * @return an Array String made from Object obj
    *
    * @preXXX obj != null
    * @deprecated deprecated_text
    *******************************************************************/
   private String[] convertToStringArray(Object obj)
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
    * convert an array of double to an array of String
    *
    *
    * @param array an array of doubles to be converted
    * @return double array values as Strings
    *
    * @preXXX array != null
    *******************************************************************/
   private String[] parseNumbers(double[] array)
   {
      String[] results = new String[array.length];

      for (int i = 0; i < array.length; i++)
      {
         results[i] = Double.toString(array[i]);
      }

      return results;
   }

   /**
    * convert an int to a given radix
    *
    *
    * @param i int to convert
    * @param radix the radix to convert to
    * @return converted int as a String
    *
    * @preXXX ((radix > 0) && (radix <= 16))
    *******************************************************************/
   private String intToRadix(int i, int radix)
   {
      if (i < radix)
      {
         return "0" + Integer.toString(i, radix);
      }

      return Integer.toString(i, radix);
   }

   /**
    * visit a histogram plot node
    *
    * @param p a HistogramPlot object
    *
    * @preXXX p != null
    *******************************************************************/
   public Object visit(HistogramPlot p)
   {
      //      Rvariable.reset();
      //      rCommands.addAll(ReadTableCmd.getCommands(p));
      //      Cmd cmd = new HistogramPlotCmd(p);
      //      rCommands.addAll(cmd.getCommands());
      Cmd cmd = new HistogramPlotDriver(p);
      rCommands.addAll(cmd.getCommands());
      return null;
   }

   /**
    * visit a box plot node
    *
    * @param p a BoxPlot object
    *
    * @pre p != null
    *******************************************************************/
   public Object visit(BoxPlot p)
   {
      Cmd cmd = new BoxPlotDriver(p);
      rCommands.addAll(cmd.getCommands());
      return null;
   }

   /**
    * visit a bar plot node
    *
    * @param p a BarPlot object
    *
    * @pre p != null
    *******************************************************************/
   public Object visit(BarPlot p)
   {
      Cmd cmd = new BarPlotDriver(p);
      rCommands.addAll(cmd.getCommands());

      if (p.getOption(LEGEND) != null)
      {
         LegendCmdBarPlot legCmd = null;
         Object[] ds = new Object[(p.getKeys(0)).length];

         for (int i = 0; i < ds.length; ++i)
         {
            ds[i] = p.getDataSet((p.getKeys(0))[i]);
         }

         if (ds[0] instanceof LabeledDataSetIfc)
         {
            LabeledDataSetIfc[] lds = new LabeledDataSetIfc[ds.length];
            System.arraycopy(ds, 0, lds, 0, lds.length);
            legCmd = new LegendCmdBarPlot(
                           (Legend) p.getOption(LEGEND), 
                           Option.getBarType(p), p.getKeys(0), lds);
         }
         else if (ds[0] instanceof DataSetIfc)
         {
            DataSetIfc[] ds2 = new DataSetIfc[ds.length];
            System.arraycopy(ds, 0, ds2, 0, ds2.length);
            legCmd = new LegendCmdBarPlot(
                           (Legend) p.getOption(LEGEND), 
                           Option.getBarType(p), p.getKeys(0), ds2);
         }
         else
         {
            throw new IllegalArgumentException();
         }

         rCommands.addAll(legCmd.getCommands());
      }
      return null;
   }

   /**
    * visit a tornado plot node
    *
    * @param p a TornadoPlot object
    *
    * @pre p != null
    *******************************************************************/
   public Object visit(TornadoPlot p)
   {
      Cmd cmd = new TornadoPlotDriver(p);
      rCommands.addAll(cmd.getCommands());

/*
      if (p.getOption(LEGEND) != null)
      {
         LegendCmdBarPlot legCmd = null;
         Object[] ds = new Object[(p.getKeys(0)).length];

         for (int i = 0; i < ds.length; ++i)
         {
            ds[i] = p.getDataSet((p.getKeys(0))[i]);
         }
         if (ds[0] instanceof LabeledDataSetIfc)
         {
            LabeledDataSetIfc[] lds = new LabeledDataSetIfc[ds.length];
            System.arraycopy(ds, 0, lds, 0, lds.length);
            legCmd = new LegendCmdBarPlot(
                           (Legend) p.getOption(LEGEND), 
                           Option.getBarType(p), p.getKeys(0), lds);
         }
         else if (ds[0] instanceof DataSetIfc)
         {
            DataSetIfc[] ds2 = new DataSetIfc[ds.length];
            System.arraycopy(ds, 0, ds2, 0, ds2.length);
            legCmd = new LegendCmdBarPlot(
                           (Legend) p.getOption(LEGEND), 
                           Option.getBarType(p), p.getKeys(0), ds2);
         }
         else
         {
            throw new IllegalArgumentException();
         }
         rCommands.addAll(legCmd.getCommands());
      }
*/
      return null;
   }

   /**
    * visit a CdfPlot
    *
    *
    * @param p a CdfPlot object
    *
    * @preXXX p != null
    *******************************************************************/
   public Object visit(CdfPlot p)
   {
      Cmd cmd = new CdfPlotDriver(p);
      rCommands.addAll(cmd.getCommands());

      return null;
   }

   /**
    * visit a ScatterPlot
    *
    *
    * @param p a ScatterPlot object
    *
    * @preXXX p != null
    *******************************************************************/
   public Object visit(ScatterPlot p)
   {
      Cmd cmd = new ScatterPlotDriver(p);
      rCommands.addAll(cmd.getCommands());

      return null;
   }

   /**    
    * visit a RankOrderPlot
    *     
    *     
    * @param p a RankOrderPlot object
    *     
    * @preXXX p != null
    *******************************************************************/
   public Object visit(RankOrderPlot p)
   {
      Cmd cmd = new RankOrderPlotDriver(p);
      rCommands.addAll(cmd.getCommands());
      return null;
   }

   /**    
    * visit a DiscreteCategoryPlot
    *     
    *     
    * @param p a DiscreteCategoryPlot object
    *     
    * @preXXX p != null
    *******************************************************************/
   public Object visit(DiscreteCategoryPlot p)
   {
      Cmd cmd = new DiscreteCategoryPlotDriver(p);
      rCommands.addAll(cmd.getCommands());
      return null;
   }

   /**
    * visit a TimeSeries Object
    *
    *
    * @param p TimeSeries Plot object
    *
    * @preXXX p != null
    *******************************************************************/
   public Object visit(TimeSeries p)
   {
      Cmd cmd = new TimeSeriesDriver(p);
      rCommands.addAll(cmd.getCommands());

      if (p.getOption(LEGEND) != null)
      {
         DataSetIfc[] ds = new DataSetIfc[(p.getKeys(0)).length];

         for (int i = 0; i < ds.length; ++i)
         {
            ds[i] = p.getDataSet((p.getKeys(0))[i]);
         }

         LegendCmdScatterPlot legCmd = new LegendCmdScatterPlot(
                                             (Legend) p.getOption(
                                                   LEGEND), 
                                             (LineType) p.getOption(
                                                   LINE_TYPE), 
                                             p.getKeys(0), ds);
         rCommands.addAll(legCmd.getCommands());
      }
      return null;
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

