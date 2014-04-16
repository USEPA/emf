package gov.epa.mims.analysisengine.rcommunicator;

import gov.epa.mims.analysisengine.tree.TextBox;
import gov.epa.mims.analysisengine.tree.TextBoxArrow;

import java.awt.Color;

import java.util.HashMap;
import java.util.List;


/**
 * DOCUMENT_ME
 *
 * @version $Revision: 1.2 $
 * @author Tommy E. Cathey
 ********************************************************/
public class TextBoxCmd extends Cmd
implements 
gov.epa.mims.analysisengine.tree.MarginConstantsIfc,
gov.epa.mims.analysisengine.tree.CompassConstantsIfc,
gov.epa.mims.analysisengine.tree.UnitsConstantsIfc,
gov.epa.mims.analysisengine.tree.TextBoxConstantsIfc,
gov.epa.mims.analysisengine.tree.FontConstantsIfc
{
   /**
    * Creates a new TextBoxCmd object.
    * @param side DOCUMENT_ME
    ********************************************************/
   public TextBoxCmd(TextBox tb)
   {
      setName("textBox");
      setReturnVariable("compass");

      setXY(tb);
      setXPD(tb);

      rCommandsPreAdd("txtStr <- " + Util.escapeQuote(tb.getTextString()) );
      variableAdd("txtStr", "txtStr");
      if( tb.getWrap() <= 0 )
      {
         variableAdd("wrap", "NULL");
      }
      else
      {
         variableAdd("wrap", "" + tb.getWrap());
      }
      variableAdd("indent", "" + tb.getIndent());
      variableAdd("exdent", "" + tb.getExdent());
      variableAdd("vSpace", "" + tb.getVSpace());

      variableAdd("cex", "" + tb.getTextExpansion());

      if( tb.getEnable() )
      {
         variableAdd("draw", "TRUE");
      }
      else
      {
         variableAdd("draw", "FALSE");
      }
      
      variableAdd("col", Util.parseColor(tb.getColor()));

      if( (tb.getStyle() == null) &&  (tb.getTypeface()==null))
      {
         variableAdd("font", "1");
         variableAdd("vfont", "NULL");
      }
      else if( tb.getTypeface()==null )
      {
         String f = tb.getStyle();
         if (f.equals(PLAIN_TEXT))
         {
            variableAdd("font", "1");
         }
         else if (f.equals(BOLD_TEXT))
         {
            variableAdd("font", "2");
         }
         else if (f.equals(ITALIC_TEXT))
         {
            variableAdd("font", "3");
         }
         else if (f.equals(BOLD_ITALIC_TEXT))
         {
            variableAdd("font", "4");
         }
         else
         {
            throw new IllegalArgumentException(getClass().getName()
                                               + " unknown font=" + f);
         }
         variableAdd("vfont", "NULL");
      }
      else
      {
         String s = "c(\""+tb.getTypeface()+"\",\""+tb.getStyle()+"\")";
         //String s = "c(\""+tb.getStyle()+"\",\""+tb.getTypeface()+"\")";
         variableAdd("vfont", s);
         variableAdd("font", "NULL");
      }
      //TBD
      variableAdd("offset", "c(0.0,0.0)");

      variableAdd("colFill", Util.parseColor(tb.getBackgroundColor()));
      variableAdd("colBorder", Util.parseColor(tb.getBorderColor()));
      if(tb.getDrawBorder())
      {
         variableAdd("lty", Util.parseLineTypes(tb.getBorderLinestyle()));
      }
      else
      {
         variableAdd("lty", "0");
      }
      variableAdd("lwd", "" + tb.getBorderLinewidth());
      variableAdd("srt", "" + tb.getTextDegreesRotation());
      variableAdd("justifyX", "" + tb.getTextXjustification());
      variableAdd("justifyY", "" + tb.getTextYjustification());

      setBoxPad(tb);
      setBoxWH(tb);

      arrowCommands(tb);

   }

   private void arrowCommands(TextBox tb)
   {
      for(int i = 0; i<tb.getNumArrows();i++)
      {
         TextBoxArrow tbArrow = tb.getArrow(i);
         if(tbArrow.getEnable())
         {
            Cmd arrowCmd = new Cmd();
            arrowCmd.setName("textBoxArrows");
            int u = tbArrow.getUnitsXY();
            if(u == TextBoxArrow.USER_UNITS)
            {
               rCommandsPostAdd("x1arrow <- " + tbArrow.getX() );
               rCommandsPostAdd("y1arrow <- " + tbArrow.getY() );
            }
            else if(u == TextBoxArrow.FIGURE_UNITS)
            {
               StringBuffer b = new StringBuffer();
               b.append("pt <- Nf2u() %*% c(");
               b.append("" + tbArrow.getX());
               b.append(",");
               b.append("" + tbArrow.getY());
               b.append(",1)");
               rCommandsPostAdd(b.toString());
               rCommandsPostAdd("x1arrow <- pt[1]");
               rCommandsPostAdd("y1arrow <- pt[2]");
            }
            else if(u == TextBoxArrow.DEVICE_UNITS)
            {
               StringBuffer b = new StringBuffer();
               b.append("pt <- Nf2u() %*% Nd2f() %*% c(");
               b.append("" + tbArrow.getX());
               b.append(",");
               b.append("" + tbArrow.getY());
               b.append(",1)");
               rCommandsPostAdd(b.toString());
               rCommandsPostAdd("x1arrow <- pt[1]");
               rCommandsPostAdd("y1arrow <- pt[2]");
            }
            //arrowCmd.variableAdd("x1",""+tbArrow.getX());
            //arrowCmd.variableAdd("y1",""+tbArrow.getY());
            arrowCmd.variableAdd("x1","x1arrow");
            arrowCmd.variableAdd("y1","y1arrow");
            arrowCmd.variableAdd("compass","compass");
            arrowCmd.variableAdd("compassSetting",Util.escapeQuote(tbArrow.getBoxContactPt()));
            arrowCmd.variableAdd("length",""+tbArrow.getLength());
            arrowCmd.variableAdd("angle",""+tbArrow.getAngle());
            arrowCmd.variableAdd("code",""+tbArrow.getCode());
            arrowCmd.variableAdd("col",Util.parseColor(tbArrow.getColor()));
            arrowCmd.variableAdd("lty",Util.parseLineTypes(tbArrow.getLty()));
            arrowCmd.variableAdd("lwd",""+tbArrow.getWidth());
            arrowCmd.variableAdd("xpd","NA");
            arrowCmd.variableAdd("backoff",""+tbArrow.getBackoff());

            rCommandsPostAdd(arrowCmd.getCommands() );
         }
      }
      
   }

   private void setXPD(TextBox tb)
   {
      int xpd = tb.getXpd();
 
      if(xpd == FIGURE)
      {
         variableAdd("xpd", "TRUE");
      }
      else if(xpd == PLOT)
      {
         variableAdd("xpd", "FALSE");
      }
      else if(xpd == DEVICE)
      {
         variableAdd("xpd", "NA");
      }
      else
      {
         throw new IllegalArgumentException("" + tb.getXpd());
      }
   }

   private void setBoxPad(TextBox tb)
   {
      double l = tb.getPadLeft();
      double r = tb.getPadRight();
      double b = tb.getPadBottom();
      double t = tb.getPadTop();
      String c = "c(" + l + "," + b + "," + r + "," + t + ")";
      variableAdd("pad", c);
   }

   private void setBoxWH(TextBox tb)
   {

      if(tb.getBoxWHtype() == MAX_WIDTH)
      {
         variableAdd("absoluteW", "NULL");
         variableAdd("absoluteH", "NULL");
         if( Double.isNaN(tb.getBoxWidth()) )
         {
            variableAdd("MaxBoxW", "NULL");
         }
         else
         {
            rCommandsPreAdd("MaxBoxW <- " + tb.getBoxWidth() );
            if(tb.getUnitsWH() == FIGURE_UNITS)
            {
               //String v1 = "Nf2u() %*% c(MaxBoxW,0,1)";
               //String v2 = "Nf2u() %*% c(0,0,1)";
	       //rCommandsPreAdd("MaxBoxW <- ( " + v1 + "-" + v2 + ")[1]" );
               String v1 = "Nfig2usr() %*% c(MaxBoxW,0,1)";
               String v2 = "Nfig2usr() %*% c(0,0,1)";
	       rCommandsPreAdd("MaxBoxW <- ( " + v1 + "-" + v2 + ")[1]" );
            }
            else if(tb.getUnitsWH() == DEVICE_UNITS)
            {
               //String v1 = "Nf2u() %*% Nd2f() %*% c(MaxBoxW,0,1)";
               //String v2 = "Nf2u() %*% Nd2f() %*% c(0,0,1)";
	       //rCommandsPreAdd("MaxBoxW <- ( " + v1 + "-" + v2 + ")[1]" );
               String v1 = "Nndc2usr() %*% c(MaxBoxW,0,1)";
               String v2 = "Nndc2usr() %*% c(0,0,1)";
	       rCommandsPreAdd("MaxBoxW <- ( " + v1 + "-" + v2 + ")[1]" );
            }
            else if(tb.getUnitsWH() == USER_UNITS)
            {  //nothing to do
            }
            else
            {
               throw new IllegalArgumentException("" + tb.getUnitsWH());
            }
            variableAdd("MaxBoxW", "MaxBoxW");
         }

         if( Double.isNaN(tb.getBoxHeight()) )
         {
            variableAdd("MaxBoxH", "NULL");
         }
         else
         {
            rCommandsPreAdd("MaxBoxH <- " + tb.getBoxHeight() );
            if(tb.getUnitsWH() == FIGURE_UNITS)
            {
               //String v1 = "Nf2u() %*% c(0,MaxBoxH,1)";
               //String v2 = "Nf2u() %*% c(0,0,1)";
	       //rCommandsPreAdd("MaxBoxH <- ( " + v1 + "-" + v2 + ")[2]" );
               String v1 = "Nfig2usr() %*% c(0,MaxBoxH,1)";
               String v2 = "Nfig2usr() %*% c(0,0,1)";
	       rCommandsPreAdd("MaxBoxH <- ( " + v1 + "-" + v2 + ")[2]" );
            }
            else if(tb.getUnitsWH() == DEVICE_UNITS)
            {
               //String v1 = "Nf2u() %*% Nd2f() %*% c(0,MaxBoxH,1)";
               //String v2 = "Nf2u() %*% Nd2f() %*% c(0,0,1)";
	       //rCommandsPreAdd("MaxBoxH <- ( " + v1 + "-" + v2 + ")[2]" );
               String v1 = "Nndc2usr() %*% c(0,MaxBoxH,1)";
               String v2 = "Nndc2usr() %*% c(0,0,1)";
	       rCommandsPreAdd("MaxBoxH <- ( " + v1 + "-" + v2 + ")[2]" );
            }
            else if(tb.getUnitsWH() == USER_UNITS)
            {  //nothing to do
            }
            else
            {
               throw new IllegalArgumentException("" + tb.getUnitsWH());
            }
            variableAdd("MaxBoxH", "MaxBoxH");
         }
      }
      else if(tb.getBoxWHtype() == ABSOLUTE_WIDTH)
      {
         variableAdd("MaxBoxW", "NULL");
         variableAdd("MaxBoxH", "NULL");
         if( Double.isNaN(tb.getBoxWidth()) )
         {
            variableAdd("absoluteW", "NULL");
         }
         else
         {
            rCommandsPreAdd("absoluteW <- " + tb.getBoxWidth() );
            if(tb.getUnitsWH() == FIGURE_UNITS)
            {
               rCommandsPreAdd("absoluteW <- ( Nf2u() %*% c(absoluteW,0,1) - Nf2u() %*% c(0,0,1) )[1]" );
            }
            else if(tb.getUnitsWH() == DEVICE_UNITS)
            {
               String v1 = "Nf2u() %*% Nd2f() %*% c(absoluteW,0,1)";
               String v2 = "Nf2u() %*% Nd2f() %*% c(0,0,1)";
               rCommandsPreAdd("absoluteW <- ( " + v1 + "-" + v2 + ")[1]" );
            }
            else if(tb.getUnitsWH() == USER_UNITS)
            {  //nothing to do
            }
            else
            {
               throw new IllegalArgumentException("" + tb.getUnitsWH());
            }
            variableAdd("absoluteW", "absoluteW");
         }

         if( Double.isNaN(tb.getBoxHeight()) )
         {
            variableAdd("absoluteH", "NULL");
         }
         else
         {
            rCommandsPreAdd("absoluteH <- " + tb.getBoxHeight() );
            if(tb.getUnitsWH() == FIGURE_UNITS)
            {
               rCommandsPreAdd("absoluteH <- ( Nf2u() %*% c(0,absoluteH,1) - Nf2u() %*% c(0,0,1) )[2]" );
            }
            else if(tb.getUnitsWH() == DEVICE_UNITS)
            {
               String v1 = "Nf2u() %*% Nd2f() %*% c(0,absoluteH,1)";
               String v2 = "Nf2u() %*% Nd2f() %*% c(0,0,1)";
               rCommandsPreAdd("absoluteH <- ( " + v1 + "-" + v2 + ")[2]" );
            }
            else if(tb.getUnitsWH() == USER_UNITS)
            {  //nothing to do
            }
            else
            {
               throw new IllegalArgumentException("" + tb.getUnitsWH());
            }
            variableAdd("absoluteH", "absoluteH");
         }
      }
      else
      {
         throw new IllegalArgumentException("" + tb.getBoxWHtype());
      }
   }

   private void setXY(TextBox tb)
   {
      String p = getPosition(tb);
      String r = getRegion(tb);

      if( p == null || r == null )
      {
         setXY2(tb);
      }
      else
      {
         setXY3(tb,r,p);
      }
   }

   private void setXY3(TextBox tb,String region, String position)
   {
      HashMap regionMap = new HashMap();
      HashMap positionMap = new HashMap();

      regionMap.put(RIGHT_HAND_MARGIN,"maRight");
      regionMap.put(LEFT_HAND_MARGIN,"maLeft");
      regionMap.put(TOP_HAND_MARGIN,"maTop");
      regionMap.put(BOTTOM_HAND_MARGIN,"maBot");
      regionMap.put(PLOT_REGION,"plot");

      positionMap.put(NORTHWEST,"NW");
      positionMap.put(NORTH,"N");
      positionMap.put(NORTHEAST,"NE");
      positionMap.put(WEST,"W");
      positionMap.put(CENTER,"C");
      positionMap.put(EAST,"E");
      positionMap.put(SOUTHWEST,"SW");
      positionMap.put(SOUTH,"S");
      positionMap.put(SOUTHEAST,"SE");

      String r = Util.escapeQuote( (String)regionMap.get(region) );
      String p = Util.escapeQuote( (String)positionMap.get(position) );
      double xj = tb.getXJustification();
      double yj = tb.getYJustification();

      Cmd newsPositionCmd = new Cmd();

      //legendPos<-newsPosition(region="maRight",position="C",xjust=0.5,yjust=0.5)
      newsPositionCmd.setName("newsPosition");
      newsPositionCmd.setReturnVariable("xy");
      newsPositionCmd.variableAdd("region",r);
      newsPositionCmd.variableAdd("position",p);
      newsPositionCmd.variableAdd("xjust",""+xj);
      newsPositionCmd.variableAdd("yjust",""+yj);

      rCommandsPreAdd(newsPositionCmd.getCommands() );
      rCommandsPreAdd("x <- logScale2uX(xy[1])");
      rCommandsPreAdd("y <- logScale2uY(xy[2])");

      variableAdd("adj", "c(0.5,0.5)");
      variableAdd("x", "x");
      variableAdd("y", "y");

   }

   private String getRegion(TextBox tb)
   {
      String rtrn = null;
      String r = tb.getRegion();
      if(r == null)
      {
         rtrn = null;
      }
      else if( r.equals(RIGHT_HAND_MARGIN) )
      {
         rtrn = RIGHT_HAND_MARGIN;
      }
      else if( r.equals(LEFT_HAND_MARGIN) )
      {
         rtrn = LEFT_HAND_MARGIN;
      }
      else if( r.equals(TOP_HAND_MARGIN) )
      {
         rtrn = TOP_HAND_MARGIN;
      }
      else if( r.equals(BOTTOM_HAND_MARGIN) )
      {
         rtrn = BOTTOM_HAND_MARGIN;
      }
      else if( r.equals(PLOT_REGION) )
      {
         rtrn = PLOT_REGION;
      }
      else
      {
         throw new IllegalArgumentException(r);
      }
      return rtrn;
   }

   private String getPosition(TextBox tb)
   {
      String rtrn = null;
      String p = tb.getPosition();
      if(p == null)
      {
         rtrn = null;
      }
      else if( p.equals(NORTH) )
      {
         rtrn = NORTH;
      }
      else if( p.equals(NORTHWEST) )
      {
         rtrn = NORTHWEST;
      }
      else if( p.equals(NORTHEAST) )
      {
         rtrn = NORTHEAST;
      }
      else if( p.equals(WEST) )
      {
         rtrn = WEST;
      }
      else if( p.equals(CENTER) )
      {
         rtrn = CENTER;
      }
      else if( p.equals(EAST) )
      {
         rtrn = EAST;
      }
      else if( p.equals(SOUTHEAST) )
      {
         rtrn = SOUTHEAST;
      }
      else if( p.equals(SOUTHWEST) )
      {
         rtrn = SOUTHWEST;
      }
      else if( p.equals(SOUTH) )
      {
         rtrn = SOUTH;
      }
      else
      {
         throw new IllegalArgumentException(p);
      }

      return rtrn;
   }

   private void setXY2(TextBox tb)
   {
      variableAdd("adj", "c(" + tb.getXJustification()+ "," + tb.getYJustification()+")");
      if( tb.getUnitsXY() == USER_UNITS )
      {
         double x = tb.getX();
         variableAdd("x", "" + x);
         double y = tb.getY();
         variableAdd("y", "" + y);
      }
      else if( tb.getUnitsXY() == FIGURE_UNITS )
      {
         double x = tb.getX();
         rCommandsPreAdd("x <- " + x );
         rCommandsPreAdd("x <- ( Nf2u() %*% c(x,0,1) )[1]" );
         variableAdd("x", "x");

         double y = tb.getY();
         rCommandsPreAdd("y <- " + y );
         rCommandsPreAdd("y <- ( Nf2u() %*% c(0,y,1) )[2]" );
         variableAdd("y", "y");
      }
      else if( tb.getUnitsXY() == DEVICE_UNITS )
      {
         double x = tb.getX();
         rCommandsPreAdd("x <- " + x );
         rCommandsPreAdd("x <- ( Nf2u() %*% Nd2f() %*% c(x,0,1) )[1]" );
         variableAdd("x", "x");

         double y = tb.getY();
         rCommandsPreAdd("y <- " + y );
         rCommandsPreAdd("y <- ( Nf2u() %*% Nd2f() %*% c(0,y,1) )[2]" );
         variableAdd("y", "y");
      }
      else
      {
         throw new IllegalArgumentException("" + tb.getUnitsXY());
      }
      //rCommandsPreAdd("points(x=x,y=y,pch=18,col=\"red\")");
   }

}
