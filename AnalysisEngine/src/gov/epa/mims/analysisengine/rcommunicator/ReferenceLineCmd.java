package gov.epa.mims.analysisengine.rcommunicator;

import gov.epa.mims.analysisengine.tree.ReferenceLine;
import gov.epa.mims.analysisengine.tree.Text;
import gov.epa.mims.analysisengine.tree.TextBorder;


/**
 * DOCUMENT_ME
 *
 * @version $Revision: 1.2 $
 * @author $author$
 ********************************************************/
public class ReferenceLineCmd extends Cmd
{
   /**
    * Creates a new ReferenceLineCmd object.
    ********************************************************/
   public ReferenceLineCmd(ReferenceLine referenceLine)
   {
      if (referenceLine == null)
      {
         throw new IllegalArgumentException(getClass().getName()
                                            + " referenceLine==null");
      }

      //
      //process options
      //
      if (referenceLine.getEnable())
      {
         processOptions(referenceLine);
      }
   }

   /**
    * DOCUMENT_ME
    *
    * @param referenceLine DOCUMENT_ME
    ********************************************************/
   private void processOptions(ReferenceLine referenceLine)
   {
      String lty = Util.parseLineTypes(referenceLine.getLinestyle());
      super.rCommandsPreAdd("par(lty=" + lty + ")");
      super.rCommandsPostAdd("par(lty=1.0)");

      super.rCommandsPreAdd("par(lwd=" + referenceLine.getLinewidth() + ")");
      super.rCommandsPostAdd("par(lwd=1.0)");

      String col = Util.parseColor(referenceLine.getLinecolor());
      super.rCommandsPreAdd("par(col=" + col + ")");

      double x1 = referenceLine.getX1();
      double y1 = referenceLine.getY1();
      double x2 = referenceLine.getX2();
      double y2 = referenceLine.getY2();
      double m = referenceLine.getM();
      Text text = referenceLine.getLabel();

      if (Double.isNaN(x1))
      {
         throw new IllegalArgumentException(getClass().getName()
                                            + " x1 == Double.NaN");
      }

      if (Double.isNaN(y1))
      {
         throw new IllegalArgumentException(getClass().getName()
                                            + " y1 == Double.NaN");
      }

      super.rCommandsPreAdd("x1 <- " + x1);
      super.rCommandsPreAdd("y1 <- " + y1);

      if ((m == Double.NEGATIVE_INFINITY) || (m == Double.POSITIVE_INFINITY))
      {
         if (!Double.isNaN(x2))
         {
            throw new IllegalArgumentException(getClass().getName()
                                               + " x2 must == Double.NaN when m= " + m);
         }

         if (!Double.isNaN(y2))
         {
            throw new IllegalArgumentException(getClass().getName()
                                               + " y2 must == Double.NaN when m= " + m);
         }

         super.rCommandsPreAdd("endPts <- refLine(x1,y1,m=\"Inf\")");
      }
      else if (m == 0.0)
      {
         if (!Double.isNaN(x2))
         {
            throw new IllegalArgumentException(getClass().getName()
                                               + " x2 must == Double.NaN when m= " + m);
         }

         if (!Double.isNaN(y2))
         {
            throw new IllegalArgumentException(getClass().getName()
                                               + " y2 must == Double.NaN when m= " + m);
         }

         super.rCommandsPreAdd("endPts <- refLine(x1,y1,m=0)");
      }
      else if (Double.isNaN(m))
      {
         if (Double.isNaN(x2))
         {
            throw new IllegalArgumentException(getClass().getName()
                                               + " x2 and m cannot both == Double.NaN");
         }

         if (Double.isNaN(y2))
         {
            throw new IllegalArgumentException(getClass().getName()
                                               + " y2 and m cannot both == Double.NaN");
         }

         super.rCommandsPreAdd("x2 <- " + x2);
         super.rCommandsPreAdd("y2 <- " + y2);
         super.rCommandsPreAdd("endPts <- refLine(x1,y1,x2,y2)");
      }
      else
      {
         if (!Double.isNaN(x2))
         {
            throw new IllegalArgumentException(getClass().getName()
                                               + " x2 must == Double.NaN when m= " + m);
         }

         if (!Double.isNaN(y2))
         {
            throw new IllegalArgumentException(getClass().getName()
                                               + " y2 must == Double.NaN when m= " + m);
         }

         super.rCommandsPreAdd("m <- " + m);
         super.rCommandsPreAdd("endPts <- refLine(x1,y1,m=m)");
      }

      if (text != null)
      {
         super.rCommandsPreAdd("xj <- " + text.getXJustification());
         super.rCommandsPreAdd("yj <- " + text.getYJustification());
         super.rCommandsPreAdd(
               "srt <- findRotation(endPts[1],endPts[2],endPts[3],endPts[4])");
         super.rCommandsPreAdd("h <- strheight(\"X\")");
         super.rCommandsPreAdd(
               "xy <- positionText(endPts[1],endPts[2],endPts[3],endPts[4],xj,yj,h)");
         super.rCommandsPreAdd("xpos <- xy[1]");
         super.rCommandsPreAdd("ypos <- xy[2]");

         if (text instanceof TextBorder)
         {
            TextBorderCmd textCmd = new TextBorderCmd((TextBorder) text, "srt");
            super.rCommandsPostAdd(textCmd.getCommands());
         }
      }

      super.rCommandsPostAdd("par(cex=1.0)");
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

   /**
    * DOCUMENT_ME
    *
    * @param args DOCUMENT_ME
    ********************************************************/
   public static void main(String[] args)
   {
      TextBorder text = new TextBorder();

      text.setDrawBorder(true);
      text.setBackgroundColor(java.awt.Color.yellow);
      text.setBorderColor(java.awt.Color.red);
      text.setBorderLinestyle(TextBorder.SOLID);
      text.setBorderLinewidth(1.0);
      text.setPadLeft(0.25);
      text.setPadRight(0.25);
      text.setPadTop(0.25);
      text.setPadBottom(0.25);

      text.setColor(java.awt.Color.black);
      text.setPosition(Text.REFERENCE_LINE, text.CENTER, 0.75, 0.0);
      text.setTextExpansion(2.0);
      text.setTextDegreesRotation(0);


      //text.setTypeface("bold");
      //text.setStyle("times");
      text.setTextString("my text string");

      //      ReferenceLine referenceLine = new ReferenceLine(-50,-50,Double.POSITIVE_INFINITY );
      ReferenceLine referenceLine = new ReferenceLine(-50, -50, 2.0);
      referenceLine.setEnable(true);
      referenceLine.setLinestyle(ReferenceLine.SOLID);
      referenceLine.setLinewidth(1.0);
      referenceLine.setLinecolor(java.awt.Color.red);
      referenceLine.setLabel(text);

      ReferenceLineCmd referenceLineCmd = new ReferenceLineCmd(referenceLine);
      java.util.ArrayList l = (java.util.ArrayList) referenceLineCmd.getCommands();

      for (int i = 0; i < l.size(); ++i)
      {
         System.out.println(l.get(i));
      }

      System.out.println();
      System.out.println();
   }
}
