package gov.epa.mims.analysisengine.rcommunicator;

import gov.epa.mims.analysisengine.tree.Text;


/**
 * DOCUMENT_ME
 *
 * @version $Revision: 1.2 $
 * @author $author$
 ********************************************************/
public class Text2Cmd extends Cmd implements
gov.epa.mims.analysisengine.tree.CompassConstantsIfc,
gov.epa.mims.analysisengine.tree.MarginConstantsIfc
{
   /**
    * Creates a new Text2Cmd object.
    ********************************************************/
   public Text2Cmd(Text text)
   {
      if (text == null)
      {
         throw new IllegalArgumentException(getClass().getName()
                                            + " text==null");
      }


      //
      //process options
      //
      processOptions(text);
   }

   /**
    * Creates a new Text2Cmd object.
    ********************************************************/
   public Text2Cmd(Text text, String xVar, String yVar)
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


      //
      //process options
      //
      processOptions(text);

      variableAdd("x", xVar);
      variableAdd("y", xVar);
   }

   /**
    * Creates a new Text2Cmd object.
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
      //rotation & expansion
      //
      if ((text.getRegion()).equals(REFERENCE_LINE))
      {
         variableAdd("srt", "srt");

         //set the cex par value to text.getTextExpansion() and
         //the cex argument of the text command to 1; The size
         //of the text will be determined by par("cex")
         double cex = text.getTextExpansion();
         rCommandsPreAdd("par(cex= " + cex + ")");
         variableAdd("cex", "1");
      }
      else if ((text.getRegion()).equals(REGRESSION_LINE))
      {
         variableAdd("srt", "srt");
         double cex = text.getTextExpansion();
         rCommandsPreAdd("par(cex= " + cex + ")");
         variableAdd("cex", "1");
      }
      else
      {
         double srt = text.getTextDegreesRotation();
         if(Double.isNaN(srt))
         {
            srt = 0.0;
         }
         variableAdd("srt", Double.toString(srt));
         variableAdd("cex", Double.toString(text.getTextExpansion()));
      }


      //
      //color
      //
      variableAdd("col", Util.parseColor(text.getColor()));


      //
      //labels
      //
//      rCommandsPreAdd("txtStr <- " + Util.escapeQuote(text.getTextString()));
      String txtStr = text.getTextString();
      if((txtStr==null)&&(text.getRegion().equals(REGRESSION_LINE)))
      {
         rCommandsPreAdd( "txtStr <- equation" );
      }
      else
      {
         rCommandsPreAdd("txtStr <- " + Util.escapeQuote(txtStr));
      }
//System.out.println("\ntxtStr=" + Util.escapeQuote(text.getTextString()));
//IllegalArgumentException e = new IllegalArgumentException(getClass().getName());
//e.printStackTrace();

      variableAdd("labels", "txtStr");

      //      variableAdd("labels", Util.escapeQuote(text.getTextString()));
      //
      //position
      // absolute positioning overrides other positioning logic
      //
      if (text.getXYset())
      {
         String xposCmd = "xpos <- " + Double.toString(text.getX());
         String yposCmd = "ypos <- " + Double.toString(text.getY());
         rCommandsPreAdd(xposCmd);
         rCommandsPreAdd(yposCmd);

         variableAdd("x", "xpos");
         variableAdd("y", "ypos");

         //         variableAdd("x", Double.toString(text.getX()));
         //         variableAdd("y", Double.toString(text.getY()));
      }
      else
      {
         //
         //get the region of the plot to write in
         //
         String r = text.getRegion();

         if ((!r.equals(RIGHT_HAND_MARGIN)) && (!r.equals(LEFT_HAND_MARGIN))
                && (!r.equals(TOP_HAND_MARGIN)) && (!r.equals(REFERENCE_LINE))
                && (!r.equals(BOTTOM_HAND_MARGIN)) && (!r.equals(PLOT_REGION))
                && (!r.equals(Text.REGRESSION_LINE))
                && (!r.equals(Text.RELATIVE2XAXIS))
                && (!r.equals(Text.RELATIVE2YAXIS)))
         {
            throw new IllegalArgumentException(getClass().getName()
                                               + " text.getRegion()==" + r);
         }

         if(r.equals(Text.RELATIVE2XAXIS))
         {
            double xJust = text.getXJustification();
            double yJust = text.getYJustification();
            String s = null;
            //hz - horizontal flag defined for Bar Plots
            rCommandsPreAdd("axisID <- ifelse(exists(\"hz\"),ifelse(hz,2,1),1)");
            s = "xy <- Relative2Axis(axisID,"+xJust+","+yJust+")";
            rCommandsPreAdd(s);
            rCommandsPreAdd("xpos <- u2logScaleX(xy[1])");
            rCommandsPreAdd("ypos <- u2logScaleY(xy[2])");
            rCommandsPreAdd("print(xpos)");
            rCommandsPreAdd("print(ypos)");
         } 
         else if(r.equals(Text.RELATIVE2YAXIS))
         {
            double xJust = text.getXJustification();
            double yJust = text.getYJustification();
            String s = null;
            //hz - horizontal flag defined for Bar Plots
            rCommandsPreAdd("axisID <- ifelse(exists(\"hz\"),ifelse(hz,1,2),2)");
            s = "xy <- Relative2Axis(axisID,"+xJust+","+yJust+")";
            rCommandsPreAdd(s);
            rCommandsPreAdd("xpos <- u2logScaleX(xy[1])");
            rCommandsPreAdd("ypos <- u2logScaleY(xy[2])");
            rCommandsPreAdd("print(xpos)");
            rCommandsPreAdd("print(ypos)");
         } 
         else if(r.equals(REGRESSION_LINE))
         {
         }
         else if (!r.equals(REFERENCE_LINE))
         {
            r = convertRegion(r);

            //
            //get the position within the region
            //
            String p = text.getPosition();

            if ((!p.equals(NORTHWEST)) && (!p.equals(NORTH))
                   && (!p.equals(NORTHEAST)) && (!p.equals(WEST))
                   && (!p.equals(CENTER)) && (!p.equals(EAST))
                   && (!p.equals(SOUTHWEST)) && (!p.equals(SOUTH))
                   && (!p.equals(SOUTHEAST)))
            {
               throw new IllegalArgumentException(getClass().getName()
                                                  + " text.getPostion()==" + p);
            }

            p = convertPosition(p);

            double xJust = text.getXJustification();
            double yJust = text.getYJustification();

            String xposCmd = null;
            String yposCmd = null;

            xposCmd = "xpos<-newsPosition(\"" + r + "\",\"" + p + "\","
                      + xJust + "," + yJust + ")[1]";
            yposCmd = "ypos<-newsPosition(\"" + r + "\",\"" + p + "\","
                      + xJust + "," + yJust + ")[2]";

            rCommandsPreAdd(xposCmd);
            rCommandsPreAdd(yposCmd);
         }

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

      if (region.equals(RIGHT_HAND_MARGIN))
      {
         rtrn = "maRight";
      }
      else if (region.equals(LEFT_HAND_MARGIN))
      {
         rtrn = "maLeft";
      }
      else if (region.equals(TOP_HAND_MARGIN))
      {
         rtrn = "maTop";
      }
      else if (region.equals(BOTTOM_HAND_MARGIN))
      {
         rtrn = "maBot";
      }
      else if (region.equals(PLOT_REGION))
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

      if (position.equals(NORTHWEST))
      {
         rtrn = "NW";
      }
      else if (position.equals(NORTH))
      {
         rtrn = "N";
      }
      else if (position.equals(NORTHEAST))
      {
         rtrn = "NE";
      }
      else if (position.equals(WEST))
      {
         rtrn = "W";
      }
      else if (position.equals(CENTER))
      {
         rtrn = "C";
      }
      else if (position.equals(EAST))
      {
         rtrn = "E";
      }
      else if (position.equals(SOUTHWEST))
      {
         rtrn = "SW";
      }
      else if (position.equals(SOUTH))
      {
         rtrn = "S";
      }
      else if (position.equals(SOUTHEAST))
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
      Text text = new Text();
      text.setColor(java.awt.Color.green);
      text.setPosition(LEFT_HAND_MARGIN, text.NORTH, 1.0, 3.0);
      text.setTextExpansion(0.1);
      text.setTextDegreesRotation(0);
      text.setTypeface("bold");
      text.setStyle("times");
      text.setTextString("my text string");

      Text2Cmd textCmd = new Text2Cmd(text);

      java.util.ArrayList l = (java.util.ArrayList) textCmd.getCommands();

      for (int i = 0; i < l.size(); ++i)
      {
         System.out.println(l.get(i));
      }

      System.out.println();
      System.out.println();

      text = new Text();
      text.setColor(java.awt.Color.green);
      text.setPosition(LEFT_HAND_MARGIN, text.NORTH, 1.0, 3.0);
      text.setPosition(91.4, 35.06);
      text.setTextExpansion(0.1);
      text.setTextDegreesRotation(0);
      text.setTypeface("bold");
      text.setStyle("times");
      text.setTextString("my text string");

      textCmd = new Text2Cmd(text);

      l = (java.util.ArrayList) textCmd.getCommands();

      for (int i = 0; i < l.size(); ++i)
      {
         System.out.println(l.get(i));
      }

      System.out.println();
      System.out.println();

      text = new Text();
      text.setColor(java.awt.Color.green);
      text.setPosition(LEFT_HAND_MARGIN, text.NORTH, 1.0, 3.0);
      text.setPosition(91.4, 35.06);
      text.setTextExpansion(0.1);
      text.setTextDegreesRotation(0);
      text.setTypeface("bold");
      text.setStyle("times");
      text.setTextString("my text string");

      textCmd = new Text2Cmd(text, "myX", "myY");

      l = (java.util.ArrayList) textCmd.getCommands();

      for (int i = 0; i < l.size(); ++i)
      {
         System.out.println(l.get(i));
      }
   }
}
