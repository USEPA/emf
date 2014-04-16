package gov.epa.mims.analysisengine.rcommunicator;

import java.awt.Color;
import java.util.List;

import gov.epa.mims.analysisengine.tree.LinearRegression;
import gov.epa.mims.analysisengine.tree.LinearRegressionStatistics;
import gov.epa.mims.analysisengine.tree.Text;
import gov.epa.mims.analysisengine.tree.TextBorder;


/**
 * DOCUMENT_ME
 *
 * @version $Revision: 1.2 $
 * @author Tommy E. Cathey
 ********************************************************/
public class LinearRegressionCmd extends Cmd
{
   private int signf = 4;
   private String sep = null;
   private Cmd secondaryCmds = new Cmd();
   private String xkey = null;
   private String ykey = null;
   /**
    * Creates a new ReferenceLineCmd object.
    ********************************************************/
   public LinearRegressionCmd(LinearRegression lr,String xkey, String ykey, String regressionName)
   {
      if( (lr == null) || (xkey == null) || (ykey == null) )
      {
         StringBuffer b = new StringBuffer();
         b.append(getClass().getName());
         if(lr == null) b.append("\nlr=null");
         if(xkey == null) b.append("\nxkey=null");
         if(ykey == null) b.append("\nykey=null");
         throw new IllegalArgumentException(b.toString());
      }
      this.xkey = xkey;
      this.ykey = ykey;

      //
      //process options
      //
      if (lr.getEnable())
      {
         processOptions(lr,regressionName);
      }
   }

   /**
    * DOCUMENT_ME
    *
    * @param LinearRegression DOCUMENT_ME
    ********************************************************/
   private void processOptions(LinearRegression lr, String regressionName)
   {
      Color c = lr.getLinecolor();
      String l = lr.getLinestyle();
      boolean e = lr.getEnable();
      double w = lr.getLinewidth();
      Text t = lr.getLabel();
      signf = lr.getSignificantFigures();
      int sepCount = lr.getSeparation();
      
      StringBuffer b = new StringBuffer();
      for(int i= 0;i<sepCount;i++)
      {
         b.append(" ");
      }
      sep = b.toString();

      rCommandsPostAdd("par(fg=" +Util.parseColor(c) +")");
      rCommandsPostAdd("par(lty=" +Util.parseLineTypes(l) +")");
      rCommandsPostAdd("par(lwd=" + w +")");
      String xR = Rvariable.getName(this.xkey);
      String yR = Rvariable.getName(this.ykey);
      rCommandsPostAdd(regressionName +" <- lm( " + yR + " ~ " + xR + ")");
      rCommandsPostAdd("abline(" + regressionName + ")");
//      rCommandsPostAdd("segments("+ xR + "," + "fitted(" + regressionName + ")," + xR + "," + yR +")");
      if(t != null)
      {
         linearRegressionLabel(t,regressionName);
      }

      if(lr.getDiagnosticPlots())
      {
         processDiagnosticPlots(lr.getDiagnosticPlotsSinglePage(),regressionName);
      }
      LinearRegressionStatistics stats = lr.getStats();
      Cmd cmd = new LinearRegressionStatisticsCmd(lr.getStats(),regressionName);
      rCommandsPostAdd(cmd.getCommands());
   }

   public List getCommands2()
   {
      return secondaryCmds.getCommands();
   }

   private void processDiagnosticPlots(boolean singlePage, String regressionName )
   {
      String selectedDevice = RCommandGenerator.getSelectedDevice();
      String os = System.getProperty("os.name").startsWith("Windows")
                     ? "Windows"
                     : "Unix";

      if (os.equals("Windows"))
      {
         if( singlePage )
         {
            secondaryCmds.rCommandsPreAdd("opar <- par(mfrow = c(2,2), oma = c(0, 0, 1.1, 0))");
            secondaryCmds.rCommandsPreAdd("plot(" + regressionName + ", las = 1)");
            secondaryCmds.rCommandsPreAdd("par(opar)");
         }
         else
         {
            secondaryCmds.rCommandsPreAdd("plot(" + regressionName + ")");
         }
      }
      else
      {
         if( singlePage )
         {
            secondaryCmds.rCommandsPreAdd("x11()");
            secondaryCmds.rCommandsPreAdd("opar <- par(mfrow = c(2,2), oma = c(0, 0, 1.1, 0))");
            secondaryCmds.rCommandsPreAdd("plot(" + regressionName + ", las = 1)");
            secondaryCmds.rCommandsPreAdd("par(opar)");
         }
         else
         {
            secondaryCmds.rCommandsPreAdd("x11()");
            secondaryCmds.rCommandsPreAdd("plot(" + regressionName + ")");
         } 
      }
   }

   private void linearRegressionLabel(Text t, String regressionName)
   {
      rCommandsPostAdd("slope <- as.vector(coefficients("+regressionName+"))[2]");
      rCommandsPostAdd("intercept <- as.vector(coefficients("+regressionName+"))[1]");
      rCommandsPostAdd("rotRadians <- atan(slope)");
      rCommandsPostAdd("xj <- " + t.getXJustification());
      rCommandsPostAdd("yj <- " + t.getYJustification());
      rCommandsPostAdd("h <- strheight(\"X\")");
      rCommandsPostAdd("endPts <- refLine(0,intercept,m=slope)");
      rCommandsPostAdd("srt <- findRotation(endPts[1],endPts[2],endPts[3],endPts[4])");
      rCommandsPostAdd("xy <- positionText(endPts[1],endPts[2],endPts[3],endPts[4],xj,yj,h)");
      rCommandsPostAdd("xpos <- xy[1]");
      rCommandsPostAdd("ypos <- xy[2]");

      if (t instanceof TextBorder)
      {
         StringBuffer b = new StringBuffer();
         b.append("equation <- paste(");
         b.append(Util.escapeQuote("y"));
         b.append(",");
         b.append(Util.escapeQuote("="));
         b.append(",");
         b.append("signif(slope," + signf + ")");
         b.append(",");
         b.append(Util.escapeQuote("*"));
         b.append(",");
         b.append(Util.escapeQuote("x"));
         b.append(",");
         b.append(Util.escapeQuote("+"));
         b.append(",");
         b.append("signif(intercept," + signf + ")");
         b.append(",");
         b.append("sep=");
         b.append(Util.escapeQuote(sep));
         b.append(")");
         rCommandsPostAdd(b.toString());
//         String eq = "paste("y","=",signif(slope,4),"*","m","+",signif(intercept,4),sep=" ")";
         TextBorderCmd textCmd = new TextBorderCmd((TextBorder) t, "srt");
         rCommandsPostAdd(textCmd.getCommands());
      }


   }





//      if (Double.isNaN(x1))
//      {
//         throw new IllegalArgumentException(getClass().getName()
//                                            + " x1 == Double.NaN");
//      }
//
//      if (Double.isNaN(y1))
//      {
//         throw new IllegalArgumentException(getClass().getName()
//                                            + " y1 == Double.NaN");
//      }
//
//      super.rCommandsPreAdd("x1 <- " + x1);
//      super.rCommandsPreAdd("y1 <- " + y1);
//
//      if ((m == Double.NEGATIVE_INFINITY) || (m == Double.POSITIVE_INFINITY))
//      {
//         if (!Double.isNaN(x2))
//         {
//            throw new IllegalArgumentException(getClass().getName()
//                                               + " x2 must == Double.NaN when m= " + m);
//         }
//
//         if (!Double.isNaN(y2))
//         {
//            throw new IllegalArgumentException(getClass().getName()
//                                               + " y2 must == Double.NaN when m= " + m);
//         }
//
//         super.rCommandsPreAdd("endPts <- refLine(x1,y1,m=\"Inf\")");
//      }
//      else if (m == 0.0)
//      {
//         if (!Double.isNaN(x2))
//         {
//            throw new IllegalArgumentException(getClass().getName()
//                                               + " x2 must == Double.NaN when m= " + m);
//         }
//
//         if (!Double.isNaN(y2))
//         {
//            throw new IllegalArgumentException(getClass().getName()
//                                               + " y2 must == Double.NaN when m= " + m);
//         }
//
//         super.rCommandsPreAdd("endPts <- refLine(x1,y1,m=0)");
//      }
//      else if (Double.isNaN(m))
//      {
//         if (Double.isNaN(x2))
//         {
//            throw new IllegalArgumentException(getClass().getName()
//                                               + " x2 and m cannot both == Double.NaN");
//         }
//
//         if (Double.isNaN(y2))
//         {
//            throw new IllegalArgumentException(getClass().getName()
//                                               + " y2 and m cannot both == Double.NaN");
//         }
//
//         super.rCommandsPreAdd("x2 <- " + x2);
//         super.rCommandsPreAdd("y2 <- " + y2);
//         super.rCommandsPreAdd("endPts <- refLine(x1,y1,x2,y2)");
//      }
//      else
//      {
//         if (!Double.isNaN(x2))
//         {
//            throw new IllegalArgumentException(getClass().getName()
//                                               + " x2 must == Double.NaN when m= " + m);
//         }
//
//         if (!Double.isNaN(y2))
//         {
//            throw new IllegalArgumentException(getClass().getName()
//                                               + " y2 must == Double.NaN when m= " + m);
//         }
//
//         super.rCommandsPreAdd("m <- " + m);
//         super.rCommandsPreAdd("endPts <- refLine(x1,y1,m=m)");
//      }
//
//      if (text != null)
//      {
//         super.rCommandsPreAdd("xj <- " + text.getXJustification());
//         super.rCommandsPreAdd("yj <- " + text.getYJustification());
//         super.rCommandsPreAdd(
//               "srt <- findRotation(endPts[1],endPts[2],endPts[3],endPts[4])");
//         super.rCommandsPreAdd("h <- strheight(\"X\")");
//         super.rCommandsPreAdd(
//               "xy <- positionText(endPts[1],endPts[2],endPts[3],endPts[4],xj,yj,h)");
//         super.rCommandsPreAdd("xpos <- xy[1]");
//         super.rCommandsPreAdd("ypos <- xy[2]");
//
//         if (text instanceof TextBorder)
//         {
//            TextBorderCmd textCmd = new TextBorderCmd((TextBorder) text, "srt");
//            super.rCommandsPostAdd(textCmd.getCommands());
//         }
//      }
//
//      super.rCommandsPostAdd("par(cex=1.0)");
//
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

