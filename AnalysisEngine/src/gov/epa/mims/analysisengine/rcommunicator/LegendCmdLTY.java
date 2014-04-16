package gov.epa.mims.analysisengine.rcommunicator;

import gov.epa.mims.analysisengine.AnalysisEngineConstants;
import gov.epa.mims.analysisengine.tree.DataSetIfc;
import gov.epa.mims.analysisengine.tree.Legend;
import gov.epa.mims.analysisengine.tree.LineType;
import gov.epa.mims.analysisengine.tree.Plot;
import gov.epa.mims.analysisengine.tree.ScatterPlot;

import java.util.ArrayList;
import java.util.List;


/**
 * DOCUMENT_ME
 *
 * @version $Revision: 1.2 $
 * @author $author$
 */
public class LegendCmdLTY extends LegendCmd 
implements 
gov.epa.mims.analysisengine.tree.SymbolsConstantsIfc,
java.io.Serializable
{
   /**
    * DOCUMENT_ME
    *
    * @param p DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   public LegendCmdLTY(Legend l, LineType lt, int count)
   {
      super(l);

      if (lt == null)
      {
         //default case when user has not defined a LINE_TYPE
         variableAdd("lty", defaultLTY(count));

         variableAdd("col", defaultCOL(count));
      }
      else
      {
         if(lt.getPlotStyle() != null)
         {
            handleSinglePlotSyle(lt,count);
         }
         else if(lt.getPlotStyles() != null)
         {
            handleMultiPlotSyles(lt,count);
         }
         else
         {
            throw new IllegalArgumentException(getClass().getName() + 
            " plot style is null ");
         }

         variableAdd("col", processColor(lt.getColor()));
      }
   }
   private void handleSinglePlotSyle(LineType lt, int count)
   {
      //process according to plot type
      // l = line; p = points ; b = points and lines
      String typeOfPlot;
      typeOfPlot = Util.escapeQuote(OptMap.getTypeOfPlot(lt.getPlotStyle()));

      if (typeOfPlot.equals("\"l\"") || typeOfPlot.equals("\"b\"") || typeOfPlot.equals("\"s\""))
      {
         //the plot is a type that has lines
         String tmp = processLineStyle(lt.getLineStyle(), count);
         if( tmp != null )
         {
            variableAdd("lty", tmp );

            if (lt.getLineWidth() != null)
            {
               variableAdd("lwd", processLineWidth(lt.getLineWidth(), count));
            }
         }
      }
      if (typeOfPlot.equals("\"p\"") || typeOfPlot.equals("\"b\""))
      {
         //the plot has symbols
         variableAdd("pch", processSymbols(lt.getSymbol()));
      }
   }

   private void handleMultiPlotSyles(LineType lt, int count)
   {
      String[] plotStyles = lt.getPlotStyles();
      processLineStyleAndPch(lt.getLineStyle(), plotStyles, lt.getSymbol(),count);
      variableAdd("lwd", processLineWidth(lt.getLineWidth(), count));

   }

   /**
    * DOCUMENT_ME
    *
    * @param count DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   private String defaultCOL(int count)
   {
      //build col String making sure that it doesn't have
      //more than 'count' elements. R generates an error
      //if col has more elements than actual data sets
      int num = Math.min(5, count);
      String[] col = new String[num];

      for (int i = 0; i < num; ++i)
      {
         col[i] = "" + i + 1;
      }

      //build R concatenate function
      return Util.buildArrayCommand("c", col);
   }

   /**
    * DOCUMENT_ME
    *
    * @param count DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   private String defaultLTY(int count)
   {
      //build lty String making sure that it doesn't have
      //more than 'count' elements. R generates an error
      //if lty has more elements than actual data sets
      int num = Math.min(5, count);
      String[] ltype = new String[num];

      for (int i = 0; i < num; ++i)
      {
         ltype[i] = "" + i + 1;
      }

      //build R concatenate function
      return Util.buildArrayCommand("c", ltype);
   }

   /**
    * DOCUMENT_ME
    *
    * @param c DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   private String processColor(java.awt.Color[] c)
   {
      if (c == null)
      {
         throw new IllegalArgumentException(getClass().getName() + "c=null");
      }

      return Util.buildArrayCommand("c", Util.parseColors(c));
   }

   /**
    * DOCUMENT_ME
    *
    * @param styles DOCUMENT_ME
    * @param count DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   private String processLineStyle(String[] styles, int count)
   {
      //validate parameters
      String cn = getClass().getName();

      if (styles == null)
      {
         throw new IllegalArgumentException(cn + "styles=null");
      }

      if (count <= 0)
      {
         throw new IllegalArgumentException(cn + "count=" + count);
      }

      //convert styles to a String array
      String[] s = Util.parseLineTypes(styles);

      //build lty String making sure that it doesn't have
      //more than 'count' elements. R generates an error
      //if lty has more elements than actual data sets
      int num = Math.min(s.length, count);
      String[] ltype = new String[num];


      boolean allZero = true;

      for (int i = 0; i < num; ++i)
      {
         ltype[i] = s[i];
         if( ! s[i].equals("0") )
         {
            allZero = false;
         }
      }

      //build R concatenate function
      String rtrn = null;
      if(! allZero )
      {
         rtrn = Util.buildArrayCommand("c", ltype);
      }

      return rtrn;
   }

   /**
    * DOCUMENT_ME
    *
    * @param styles DOCUMENT_ME
    * @param count DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   private void processLineStyleAndPch(String[] styles, String[] plotStyles,
    String[] syms, int count)
   {
      //validate parameters
      String cn = getClass().getName();

      if (styles == null)
      {
         throw new IllegalArgumentException(cn + "styles=null");
      }

      if (plotStyles == null)
      {
         throw new IllegalArgumentException(cn + "plotStyles=null");
      }

      if (count <= 0)
      {
         throw new IllegalArgumentException(cn + "count=" + count);
      }

      //convert styles to a String array
      String[] s = Util.parseLineTypes(styles);

      //convert symbols to a String array
      String[] sym = Util.parseSymbols(syms);

      String[] ltype = new String[count];
      String[] symbols = new String[count];
      for(int i=0;i<count;i++)
      {
         String pStyle = plotStyles[i % plotStyles.length];
         if(pStyle.equals(POINTS))
         {
            //ltype[i] = "0";
            symbols[i] = sym[i % sym.length];
            //variableAdd("lty", Util.buildArrayCommand("c", ltype) );
            variableAdd("pch", Util.buildArrayCommand("c", symbols));
         }
         else if(pStyle.equals(LINES) || pStyle.equals(STAIR_STEPS))
         {
            ltype[i] = s[i % s.length];
            symbols[i] = "-1";
            variableAdd("lty", Util.buildArrayCommand("c", ltype) );
            //variableAdd("pch", Util.buildArrayCommand("c", symbols));
         }
         else if(pStyle.equals(POINTS_N_LINES))
         {
            ltype[i] = s[i % s.length];
            symbols[i] = sym[i % sym.length];
            variableAdd("lty", Util.buildArrayCommand("c", ltype) );
            variableAdd("pch", Util.buildArrayCommand("c", symbols));
         }
         else if(pStyle.equals(NO_PLOTTING))
         {
            ltype[i] = "0";
            symbols[i] = "-1";
            //variableAdd("lty", Util.buildArrayCommand("c", ltype) );
            //variableAdd("pch", Util.buildArrayCommand("c", symbols));
         }
         else
         {
            throw new IllegalArgumentException(getClass().getName() + 
            " pStyle= "+pStyle);
         }
      }

//      variableAdd("lty", Util.buildArrayCommand("c", ltype) );
//      variableAdd("pch", Util.buildArrayCommand("c", symbols));

   }

   /**
    * DOCUMENT_ME
    *
    * @param widths DOCUMENT_ME
    * @param keyCount DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   private String processLineWidth(double[] widths, int keyCount)
   {
      //validate parameters
      String cn = getClass().getName();

      if (widths == null)
      {
         throw new IllegalArgumentException(cn + "widths=null");
      }

      if (keyCount <= 0)
      {
         throw new IllegalArgumentException(cn + "keyCount=" + keyCount);
      }

      //convert widths to String array
      String[] s = Util.convertToStringArray(widths);

      //make sure the number of widths passed to R is
      //less than or equal to the num of Data Sets, keyCount
      int count = Math.min(s.length, keyCount);
      String[] lwidth = new String[count];

      for (int i = 0; i < count; ++i)
      {
         lwidth[i] = s[i];
      }

      return Util.buildArrayCommand("c", lwidth);
   }

   /**
    * DOCUMENT_ME
    *
    * @param symbols DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   private String processSymbols(String[] symbols)
   {
      //validate parameters
      String cn = getClass().getName();

      if (symbols == null)
      {
         throw new IllegalArgumentException(cn + "symbols=null");
      }

      return Util.buildArrayCommand("c", Util.parseSymbols(symbols));
   }
}
