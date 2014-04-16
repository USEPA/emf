package gov.epa.mims.analysisengine.rcommunicator;

import java.awt.Color;
import java.util.List;

import gov.epa.mims.analysisengine.tree.LinearRegression;
import gov.epa.mims.analysisengine.tree.LinearRegressionStatistics;
import gov.epa.mims.analysisengine.tree.Text;
import gov.epa.mims.analysisengine.tree.TextBlock;
import gov.epa.mims.analysisengine.tree.BackgroundBox;
import gov.epa.mims.analysisengine.tree.Border;


/**
 * DOCUMENT_ME
 *
 * @version $Revision: 1.2 $
 * @author Tommy E. Cathey
 ********************************************************/
public class LRStextCmd extends Cmd
{
   /**
    * Creates a new ReferenceLineCmd object.
    ********************************************************/
   public LRStextCmd(TextBlock t, String rVarTxtStr)
   {
      rCommandsPreAdd("#==============START LRStextCmd===================");
      rCommandsPreAdd("xpdOrig <- par(xpd=T)");
      rCommandsPostAdd("par(xpd=xpdOrig)");

      double cex = t.getCex();
      rCommandsPreAdd("cexOrig <- par(cex=" + cex + ")");
      rCommandsPostAdd("par(cex=cexOrig)");

      double lineSpacing = t.getLineSpacing();
      rCommandsPreAdd("sp <- " + lineSpacing);
      rCommandsPreAdd("labels <- split.text(txtStr)");
      rCommandsPreAdd("tBoxWH <- my.text.width.height(labels,sp)");
      processPosition(t, "xy");
      processBoxBoundary("xy", "tBoxWH", t.getXBlockJustification(), t.getYBlockJustification(),"tBoxBoundary");
      BackgroundBox box = t.getBackgroundBox();
      if(box != null)
      {
         processInsets("tBoxBoundary", box, "tBoxBoundaryInset" );
         processDrawBox("tBoxBoundaryInset", box);
      }
      processFont(t.getFont());

//       rCommandsPreAdd("tBox <- add.box.insets(box,insets)");

//FIX THIS
      rCommandsPreAdd("adj <- c(0,0)");
      rCommandsPreAdd("box.adj <- c(0.5,0.5)");


      String textColor = Util.parseColor(t.getColor());
      rCommandsPreAdd("fgOrig <- par(fg=" + textColor + ")");

      //tBox <- my.text(x,y,labels,sp,adj,box.adj=box.adj,plot=FALSE)
      StringBuffer b = new StringBuffer();
      b.append("my.text2(");
      b.append("xy[1]");
      b.append(",xy[2]");
      b.append(",tBoxWH");
      b.append(",labels");
      b.append(",adj,box.adj=box.adj,plot=TRUE)");
      rCommandsPreAdd(b.toString());

      rCommandsPreAdd("#==============END LRStextCmd===================");
   }
   private void processFont(String f)
   {
      String font = LookUp.get(f);
      if( font == null )
      {
         String msg = getClass().getName() + " unknown font=" + font;
         throw new IllegalArgumentException(msg);
      }
      rCommandsPreAdd("fontOrig <- par(font="+font + ")");
   }

   private void processPosition(TextBlock t, String returnVar)
   {
      double x = t.getX();
      double y = t.getY();

      if( !Double.isNaN(x) && !Double.isNaN(y) )
      {
         int units = t.getUnitsXY();
         if(units == gov.epa.mims.analysisengine.tree.UnitsConstantsIfc.USER_UNITS)
         {
            rCommandsPreAdd("xy <- c(" + x + "," + y + ")");
         }
         else if( units == gov.epa.mims.analysisengine.tree.UnitsConstantsIfc.FIGURE_UNITS)
         {
            rCommandsPreAdd("xy <- Nfig2usr() %*% c(" + x + "," + y + ",1)");
         }
         else if( units == gov.epa.mims.analysisengine.tree.UnitsConstantsIfc.DEVICE_UNITS)
         {
            rCommandsPreAdd("xy <- Nndc2usr() %*% c(" + x + "," + y + ",1)");
         }
         else
         {
            String msg = getClass().getName() + " unknown units=" + units;
            throw new IllegalArgumentException(msg);
         }
      }
      else
      {

         String r = Util.escapeQuote( LookUp.get(t.getRegion()) );
         String p = Util.escapeQuote( LookUp.get(t.getPosition()) );
   
         double xJust = t.getXJustification();
         double yJust = t.getYJustification();
         StringBuffer b = new StringBuffer();
         b.append(returnVar + " <- newsPosition(");
         b.append(r + "," + p + ",");
         b.append("" + xJust + "," + yJust + ")");
         rCommandsPreAdd(b.toString());
      }
   }

   private void processInsets(String textBox, BackgroundBox box, String returnVar)
   {
      rCommandsPreAdd("cW <- strwidth(\"X\")");
      rCommandsPreAdd("cH <- strheight(\"X\")");
      double b = box.getPadBottom();
      double l = box.getPadLeft();
      double r = box.getPadRight();
      double t = box.getPadTop();
      StringBuffer buf = new StringBuffer();
      buf.append("insets <- list(");
      buf.append("xleft= cW * " + l);
      buf.append(", ybottom= cH * " + b);
      buf.append(", xright= cW * " + r);
      buf.append(", ytop= cH * " + t);
      buf.append(")");
      rCommandsPreAdd(buf.toString());

      rCommandsPreAdd(returnVar + " <- add.box.insets(" + textBox + ",insets)" );
   }

   private void processBoxBoundary(String xy, String box, double xJust, double yJust, String returnVal)
   {
      StringBuffer b = new StringBuffer();
      b.append(returnVal + " <- box.boundary(");
      b.append(xy);
      b.append("," + box);
      b.append("," + xJust + "," + yJust + ")" );
      rCommandsPreAdd(b.toString());
   }

   private void processDrawBox(String boxBoundary, BackgroundBox b)
   {
      if(b.getEnable())
      {
         String fillCol = Util.parseColor(b.getColor());
         rCommandsPreAdd("fill.col <- " + fillCol);

         Border border = b.getBorder();
         if(border != null)
         {
            String borderCol = Util.parseColor(border.getColor());
            rCommandsPreAdd("border.col <- " + borderCol);
            String lty = Util.parseLineTypes(border.getLinestyle());
            rCommandsPreAdd("par(lty=" + lty + ")");
            rCommandsPreAdd("lty <- " + lty );
            String lwd = "" + border.getLinewidth();
            rCommandsPreAdd("par(lwd=" + border.getLinewidth() + ")");
            rCommandsPreAdd("lwd <- " + lwd );
         }
         else
         {
            rCommandsPreAdd("border.col <- NULL # no border");
            rCommandsPreAdd("lty <- 0 # no border");
         }
         rCommandsPreAdd("my.box(" + boxBoundary + ",fill.col,border.col,lty,lwd,TRUE)");
      }
/*
      String bgColor = Util.parseColor(box.getBackgroundColor());
      String borderColor = Util.parseColor(box.getBorderColor());
      String lty = Util.parseLineTypes(box.getBorderLinestyle());
      double lwd = box.getBorderLinewidth();
      boolean drawBorder = box.getDrawBorder();
      if(!drawBorder)
      {
         lty = "0";
         lwd = 0;
      }
      StringBuffer b = new StringBuffer();
      b.append("draw.box(" + boxBoundary);
      b.append(", " + bgColor);
      b.append(", " + borderColor);
      b.append(", " + lty);
      b.append(", " + lwd);
      b.append(", TRUE)");
      rCommandsPreAdd(b.toString());
*/

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

