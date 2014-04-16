package gov.epa.mims.analysisengine.rcommunicator;

import java.awt.Color;
import java.util.List;

import gov.epa.mims.analysisengine.tree.LinearRegression;
import gov.epa.mims.analysisengine.tree.LinearRegressionStatistics;
import gov.epa.mims.analysisengine.tree.Text;
import gov.epa.mims.analysisengine.tree.TextAttribute;
import gov.epa.mims.analysisengine.tree.TextBorder;
import gov.epa.mims.analysisengine.tree.TextBlock;


/**
 * DOCUMENT_ME
 *
 * @version $Revision: 1.2 $
 * @author Tommy E. Cathey
 ********************************************************/
public class LinearRegressionStatisticsCmd extends Cmd
{
   private void doEnableFlags(LinearRegressionStatistics lrs)
   {
      StringBuffer b = new StringBuffer();
      String flg = null;

      b.append("enable <- list(");
      flg = (lrs.getShowEquation())?("T"):("F");
      b.append("eq=" + flg + ",");

      flg = (lrs.getShowResiduals())?("T"):("F");
      b.append("resid=" + flg + ",");

      flg = (lrs.getShowResidualStdErr())?("T"):("F");
      b.append("resid.std.err=" + flg + ",");

      flg = (lrs.getShowCoefficients())?("T"):("F");
      b.append("f.statistics=" + flg + ",");

      flg = (lrs.getShowCoefficientsStdErr())?("T"):("F");
      b.append("coef=" + flg + ",");

      flg = (lrs.getShowCoefficientsTvalue())?("T"):("F");
      b.append("coef.std.err=" + flg + ",");

      flg = (lrs.getShowCoefficientsPvalue())?("T"):("F");
      b.append("coef.t.value=" + flg + ",");

      flg = (lrs.getShowFstatistics())?("T"):("F");
      b.append("coef.p.value=" + flg + ")");

      rCommandsPreAdd(b.toString());
   }
   /**
    * Creates a new ReferenceLineCmd object.
    ********************************************************/
   public LinearRegressionStatisticsCmd(LinearRegressionStatistics lrs,String regressionName)
   {
      rCommandsPreAdd("#=====================START LinearRegressionStatisticsCmd=============================");
      if(lrs.getEnable())
      {
         double spacing = 0.5;
         //generate statistics
//rCommandsPreAdd("enable <- list(eq=T,resid=T,resid.std.err=T,f.statistics=T,coef=T,coef.std.err=T,coef.t.value=T,coef.p.value=T)");
         doEnableFlags(lrs);
         rCommandsPreAdd("txtStr <- my.print.summary.lm(summary("+regressionName+"),enable=enable)");
//         rCommandsPreAdd("labels <- split.text(txtStr)");
//         rCommandsPreAdd("tBox <- my.text.width.height(labels," + spacing + ")");
//         rCommandsPreAdd("tBox <- add.box.insets(tBox,insets)");
//         //size the text 
//         Cmd lrsCmd = new LRStextCmd(tb, lrs.getBox(), "txtStr","tBox",false);
         TextBlock tb = lrs.getTextBlock();
         Cmd lrsCmd = new LRStextCmd(tb, "txtStr");
         rCommandsPreAdd(lrsCmd.getCommands());

         //draw the box
         //draw the text
/*
         rCommandsPreAdd("par(xpd=T)");
         TextAttribute t = lrs.getTextAttribute();
         String textColor = Util.parseColor(t.getColor());
         rCommandsPreAdd("enable <- list(eq=T,resid=T,resid.std.err=T,f.statistics=T,coef=T,coef.std.err=T,coef.t.value=T,coef.p.value=T)");
         rCommandsPreAdd("txtStr <- my.print.summary.lm(summary("+regressionName+"),enable=enable)");
         rCommandsPreAdd("lbls <- strsplit(txtStr,split=\"\\n\")");
         rCommandsPreAdd("xy <- newsPosition(\"maRight\",\"C\",0.5,0.5)");

         String textFont = t.getFont();
         String textCex = "" + t.getCex();
         rCommandsPreAdd("par(cex="+textCex+")");
         Cmd cmd = new FilledRectangleCmd(lrs.getBox());
         rCommandsPreAdd(cmd.getCommands());
         rCommandsPreAdd("my.text.bordered(x=xy[1],y=xy[2],lbls[[1]],sp=0.5,adj=c(0.0,0.0),box.adj=c(0.5,0.5),insets=insets,fill.col=fill.col,border.col=border.col,lty=lty,lwd=lwd,plot=T)");
         rCommandsPreAdd("par(cex=1.0)");
         rCommandsPreAdd("par(xpd=F)");
*/
      }
/*
         lrs.getEnable();
         lrs.getShowEquation();
         lrs.getShowResiduals();
         lrs.getShowResidualStdErr();
         lrs.getShowCoefficients();
         lrs.getShowCoefficientsStdErr();
         lrs.getShowCoefficientsTvalue();
         lrs.getShowCoefficientsPvalue();
         lrs.getShowFstatistics();
*/
      rCommandsPreAdd("#=====================END LinearRegressionStatisticsCmd=============================");
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

