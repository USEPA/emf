package gov.epa.mims.analysisengine.rcommunicator;

import gov.epa.mims.analysisengine.tree.Text;
import gov.epa.mims.analysisengine.tree.TextBorder;


/**
 * DOCUMENT_ME
 *
 * @version $Revision: 1.2 $
 * @author $author$
 ********************************************************/
public class TextBorderCmd extends Text2Cmd
{
   /** DOCUMENT_ME */
   private String srt = null;

   /**
    * Creates a new TextBorderCmd object.
    ********************************************************/
   public TextBorderCmd(TextBorder textBorder)
   {
      super(textBorder);

      if (textBorder == null)
      {
         throw new IllegalArgumentException(getClass().getName()
                                            + " textBorder==null");
      }

      //
      //process options
      //
      if (textBorder.getDrawBorder())
      {
         processOptions(textBorder);
      }
   }

   /**
    * Creates a new TextBorderCmd object.
    ********************************************************/
   public TextBorderCmd(TextBorder textBorder, String srt)
   {
      super(textBorder);

      if (textBorder == null)
      {
         throw new IllegalArgumentException(getClass().getName()
                                            + " textBorder==null");
      }

      if (srt == null)
      {
         throw new IllegalArgumentException(getClass().getName()
                                            + " srt==null");
      }

      this.srt = srt;

      //
      //process options
      //
      if (textBorder.getDrawBorder())
      {
         processOptions(textBorder);
      }
   }

   /**
    * DOCUMENT_ME
    *
    * @param textBorder DOCUMENT_ME
    ********************************************************/
   private void processOptions(TextBorder textBorder)
   {
      /**********************************
      we want to create something like the following statements
      par(srt=DeviceRot2UsrRot(200)) #in usr coordinates
      par(cex=3)
      par(lty=1)
      par(lwd=1)
      par(xpd=NA) #clipping FALSE=plot region TRUE=figure region NA=device
      par(col="black") #text color
      
      borderedText(mystring,xc,yc,"red","yellow")
      *********************************/
      String cex = Double.toString(textBorder.getTextExpansion());
      super.rCommandsPreAdd("par(cex=" + cex + ")");

      String lty = Util.parseLineTypes(textBorder.getBorderLinestyle());
      super.rCommandsPreAdd("par(lty=" + lty + ")");

      super.rCommandsPreAdd("par(lwd=" + textBorder.getBorderLinewidth() + ")");

      String col = Util.parseColor(textBorder.getBorderColor());
      super.rCommandsPreAdd("borderCol <- " + col);

      String fillCol = Util.parseColor(textBorder.getBackgroundColor());
      super.rCommandsPreAdd("fillCol <- " + fillCol);

      if (srt == null)
      {
//         String deg = Double.toString(textBorder.getTextDegreesRotation());
//         super.rCommandsPreAdd("srt <- " + deg);

         double srt = textBorder.getTextDegreesRotation();
         if(Double.isNaN(srt))
         {
            srt = 0.0;
         }
         super.rCommandsPreAdd("srt <- " + srt);
      }

      String rP = "0.25";
      String lP = "0.25";
      String tP = "0.25";
      String bP = "0.25";
      super.rCommandsPreAdd("rP <- " + textBorder.getPadRight());
      super.rCommandsPreAdd("lP <- " + textBorder.getPadLeft());
      super.rCommandsPreAdd("tP <- " + textBorder.getPadTop());
      super.rCommandsPreAdd("bP <- " + textBorder.getPadBottom());
      super.rCommandsPreAdd(
            "borderedText(txtStr,xpos,ypos,srt,borderCol,fillCol,rP,lP,bP,tP)");

      //initialize the "xloc" and "yloc" R variables
      //      super.rCommandsPreAdd("xloc <- " + textBorder.getX());
      //      super.rCommandsPreAdd("yloc <- " + textBorder.getY());
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

      text.setBackgroundColor(java.awt.Color.blue);
      text.setBorderColor(java.awt.Color.red);
      text.setBorderLinestyle(TextBorder.SOLID);
      text.setBorderLinewidth(1.0);
      text.setPadLeft(1.0);
      text.setPadRight(1.0);
      text.setPadTop(1.0);
      text.setPadBottom(1.0);

      text.setColor(java.awt.Color.green);
      text.setPosition(Text.LEFT_HAND_MARGIN, text.NORTH, 1.0, 3.0);
      text.setTextExpansion(0.1);
      text.setTextDegreesRotation(0);
      text.setTypeface("bold");
      text.setStyle("times");
      text.setTextString("my text string");

      TextBorderCmd textCmd = new TextBorderCmd(text);

      java.util.ArrayList l = (java.util.ArrayList) textCmd.getCommands();

      for (int i = 0; i < l.size(); ++i)
      {
         System.out.println(l.get(i));
      }

      System.out.println();
      System.out.println();

      text = new TextBorder();
      text.setColor(java.awt.Color.green);


      //      text.setPosition(Text.LEFT_HAND_MARGIN, text.NORTH, 1.0, 3.0);
      text.setPosition(91.4, 35.06);
      text.setTextExpansion(0.1);
      text.setTextDegreesRotation(0);
      text.setTypeface("bold");
      text.setStyle("times");
      text.setTextString("my text string");

      textCmd = new TextBorderCmd(text);

      l = (java.util.ArrayList) textCmd.getCommands();

      for (int i = 0; i < l.size(); ++i)
      {
         System.out.println(l.get(i));
      }

      System.out.println();
      System.out.println();

      System.out.println(textCmd);
   }
}
