package gov.epa.mims.analysisengine.rcommunicator;

import gov.epa.mims.analysisengine.tree.Text;

import java.util.List;


/**
 * DOCUMENT_ME
 *
 * @version $Revision: 1.2 $
 * @author Tommy E. Cathey
 ********************************************************/
public class TextCmd extends Cmd
{
   /** DOCUMENT_ME */
   Text text = null;

   /** DOCUMENT_ME */
   boolean constructor2 = false;

   /** DOCUMENT_ME */
   String xVar = null;

   /** DOCUMENT_ME */
   String yVar = null;

   /**
    * Creates a new TextCmd object.
    ********************************************************/
   public TextCmd(Text text)
   {
      this.text = text;
      constructor2 = false;
   }

   /**
    * Creates a new TextCmd object.
    ********************************************************/
   public TextCmd(Text text, String xVar, String yVar)
   {
      if (text == null)
      {
         throw new IllegalArgumentException(getClass().getName()
                                            + " text==null");
      }

      if (xVar == null)
      {
         throw new IllegalArgumentException(getClass().getName()
                                            + " xVar==null");
      }

      if (yVar == null)
      {
         throw new IllegalArgumentException(getClass().getName()
                                            + " yVar==null");
      }

      this.text = text;
      this.xVar = xVar;
      this.yVar = yVar;
      constructor2 = true;
   }

   /**
    * return list of commands
    ********************************************************/
   public List getCommands()
   {
      processOptions(text);

      if (constructor2)
      {
         variableAdd("x", xVar);
         variableAdd("y", xVar);
      }

      List l = super.getCommands();

      return l;
   }

   /**
    * DOCUMENT_ME
    *
    * @param text DOCUMENT_ME
    ********************************************************/
   public void setText(Text text)
   {
      this.text = text;
   }

   /**
    * DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   public Text getText()
   {
      return (Text) text.clone();
   }

   /**
    * Creates a new TextCmd object.
    ********************************************************/
   public void processOptions(Text text)
   {
      //
      //set R name for text command
      //
      setName("text");


      //
      //allow printing in margins
      //
      variableAdd("xpd", "TRUE");


      //
      //expansion
      //
      variableAdd("cex", Double.toString(text.getTextExpansion()));


      //
      //rotation
      //
//      variableAdd("srt", Double.toString(text.getTextDegreesRotation()));
      double srt = text.getTextDegreesRotation();
      if(Double.isNaN(srt))
      {
         srt = 0.0;
      }
      variableAdd("srt", Double.toString(srt));


      //
      //color
      //
      variableAdd("col", Util.parseColor(text.getColor()));


      //
      //labels
      //
      variableAdd("labels", Util.escapeQuote(text.getTextString()));

      //
      //position
      // absolute positioning overrides other positioning logic
      //
      if (text.getXYset() || (text.getRegion() == null))
      {
         variableAdd("x", Double.toString(text.getX()));
         variableAdd("y", Double.toString(text.getY()));
      }
      else
      {
         //
         //get the region of the plot to write in
         //
         String r = text.getRegion();

         if ((!r.equals(Text.RIGHT_HAND_MARGIN)) && (!r.equals(Text.LEFT_HAND_MARGIN))
                && (!r.equals(Text.TOP_HAND_MARGIN))
                && (!r.equals(Text.BOTTOM_HAND_MARGIN)) && (!r.equals(Text.PLOT_REGION)))
         {
            throw new IllegalArgumentException(getClass().getName()
                                               + " text.getRegion()==" + r);
         }

         r = convertRegion(r);

         //
         //get the position within the region
         //
         String p = text.getPosition();

         if ((!p.equals(Text.NORTHWEST)) && (!p.equals(Text.NORTH))
                && (!p.equals(Text.NORTHEAST)) && (!p.equals(Text.WEST))
                && (!p.equals(Text.CENTER)) && (!p.equals(Text.EAST))
                && (!p.equals(Text.SOUTHWEST)) && (!p.equals(Text.SOUTH))
                && (!p.equals(Text.SOUTHEAST)))
         {
            throw new IllegalArgumentException(getClass().getName()
                                               + " text.getPostion()==" + p);
         }

         p = convertPosition(p);

         double xJust = text.getXJustification();
         double yJust = text.getYJustification();

         String xposCmd = null;
         String yposCmd = null;

         xposCmd = "xpos<-newsPosition(\"" + r + "\",\"" + p + "\"," + xJust
                   + "," + yJust + ")[1]";
         yposCmd = "ypos<-newsPosition(\"" + r + "\",\"" + p + "\"," + xJust
                   + "," + yJust + ")[2]";

         rCommandsPreAdd(xposCmd);
         rCommandsPreAdd(yposCmd);

         variableAdd("x", "xpos");
         variableAdd("y", "ypos");
      }

      //
      //fonts
      //
      String vfont = "NULL";
      String typeface = text.getTypeface();
      String style = text.getStyle();

      if ((typeface != null) && (style != null))
      {
         vfont = "c(" + Util.escapeQuote(typeface) + ","
                 + Util.escapeQuote(style) + ")";
      }

      variableAdd("vfont", vfont);
   }

   /**
    * DOCUMENT_ME
    *
    * @param region DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   private String convertRegion(String region)
   {
      String rtrn = null;

      if (region.equals(Text.RIGHT_HAND_MARGIN))
      {
         rtrn = "maRight";
      }
      else if (region.equals(Text.LEFT_HAND_MARGIN))
      {
         rtrn = "maLeft";
      }
      else if (region.equals(Text.TOP_HAND_MARGIN))
      {
         rtrn = "maTop";
      }
      else if (region.equals(Text.BOTTOM_HAND_MARGIN))
      {
         rtrn = "maBot";
      }
      else if (region.equals(Text.PLOT_REGION))
      {
         rtrn = "maPlot";
      }
      else
      {
         throw new IllegalArgumentException(getClass().getName() + " region=="
                                            + region);
      }

      return rtrn;
   }

   /**
    * DOCUMENT_ME
    *
    * @param position DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   private String convertPosition(String position)
   {
      String rtrn = null;

      if (position.equals(Text.NORTHWEST))
      {
         rtrn = "NW";
      }
      else if (position.equals(Text.NORTH))
      {
         rtrn = "N";
      }
      else if (position.equals(Text.NORTHEAST))
      {
         rtrn = "NE";
      }
      else if (position.equals(Text.WEST))
      {
         rtrn = "W";
      }
      else if (position.equals(Text.CENTER))
      {
         rtrn = "C";
      }
      else if (position.equals(Text.EAST))
      {
         rtrn = "E";
      }
      else if (position.equals(Text.SOUTHWEST))
      {
         rtrn = "SW";
      }
      else if (position.equals(Text.SOUTH))
      {
         rtrn = "S";
      }
      else if (position.equals(Text.SOUTHEAST))
      {
         rtrn = "SE";
      }
      else
      {
         throw new IllegalArgumentException(getClass().getName()
                                            + " position==" + position);
      }

      return rtrn;
   }

   /**
    * DOCUMENT_ME
    *
    * @param args DOCUMENT_ME
    ********************************************************/
   public static void main(String[] args)
   {
      Text text = new Text();
      text.setColor(java.awt.Color.green);
      text.setPosition(Text.LEFT_HAND_MARGIN, text.NORTH, 1.0, 3.0);
      text.setTextExpansion(0.1);
      text.setTextDegreesRotation(0);
      text.setTypeface("bold");
      text.setStyle("times");
      text.setTextString("my text string");

      TextCmd textCmd = new TextCmd(text);

      java.util.ArrayList l = (java.util.ArrayList) textCmd.getCommands();

      for (int i = 0; i < l.size(); ++i)
      {
         System.out.println(l.get(i));
      }

      System.out.println();
      System.out.println();

      text = new Text();
      text.setColor(java.awt.Color.green);
      text.setPosition(Text.LEFT_HAND_MARGIN, text.NORTH, 1.0, 3.0);
      text.setPosition(91.4, 35.06);
      text.setTextExpansion(0.1);
      text.setTextDegreesRotation(0);
      text.setTypeface("bold");
      text.setStyle("times");
      text.setTextString("my text string");

      textCmd = new TextCmd(text);

      l = (java.util.ArrayList) textCmd.getCommands();

      for (int i = 0; i < l.size(); ++i)
      {
         System.out.println(l.get(i));
      }

      System.out.println();
      System.out.println();

      text = new Text();
      text.setColor(java.awt.Color.green);
      text.setPosition(Text.LEFT_HAND_MARGIN, text.NORTH, 1.0, 3.0);
      text.setPosition(91.4, 35.06);
      text.setTextExpansion(0.1);
      text.setTextDegreesRotation(0);
      text.setTypeface("bold");
      text.setStyle("times");
      text.setTextString("my text string");

      textCmd = new TextCmd(text, "myX", "myY");

      l = (java.util.ArrayList) textCmd.getCommands();

      for (int i = 0; i < l.size(); ++i)
      {
         System.out.println(l.get(i));
      }
   }
}
