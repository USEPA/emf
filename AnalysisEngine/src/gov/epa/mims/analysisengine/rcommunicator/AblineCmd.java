package gov.epa.mims.analysisengine.rcommunicator;

import java.awt.Color;


/**
 * DOCUMENT_ME
 *
 * @version $Revision: 1.2 $
 * @author $author$
 */
public class AblineCmd extends Cmd implements java.io.Serializable
{
   /** DOCUMENT_ME */
   private String n = getClass().getName();

   /**
    * Creates a new AblineCmd object.
    *
    * @param side DOCUMENT_ME
    * @param c DOCUMENT_ME
    * @param t DOCUMENT_ME
    * @param w DOCUMENT_ME
    ********************************************************/
   public AblineCmd(int side, Color c, String t, double w)
   {
      if ((side < 1) || (side > 4))
      {
         throw new IllegalArgumentException(n + " side=" + side);
      }

      setUp(c, t, w);
      abline(side);
   }

   /**
    * Creates a new AblineCmd object.
    *
    * @param side DOCUMENT_ME
    * @param at DOCUMENT_ME
    * @param c DOCUMENT_ME
    * @param t DOCUMENT_ME
    * @param w DOCUMENT_ME
    ********************************************************/
   public AblineCmd(int side, String at, Color c, String t, double w)
   {
      if ((side < 1) || (side > 4))
      {
         throw new IllegalArgumentException(n + " side=" + side);
      }

      if (at == null)
      {
         throw new IllegalArgumentException(n + " at=null");
      }

      setUp(c, t, w);
      abline(side, at);
   }

   /**
    * DOCUMENT_ME
    *
    * @param c DOCUMENT_ME
    * @param t DOCUMENT_ME
    * @param w DOCUMENT_ME
    ********************************************************/
   private void setUp(Color c, String t, double w)
   {
      //super methods to setup for this command
      setName("abline");
      setBannerOn(true);

      xpd();
      color(c);
      linetype(t);
      linewidth(w);
   }

   private void xpd()
   {
      rCommandsPreAdd("ablineSavedXPD<-par(xpd=FALSE)");
      rCommandsPostAdd("par(xpd=ablineSavedXPD)");
   }

   /**
    * DOCUMENT_ME
    *
    * @param c DOCUMENT_ME
    ********************************************************/
   private void color(Color c)
   {
      if (c != null)
      {
         rCommandsPreAdd("ablineSavedColor<-par(\"fg\")");
         rCommandsPreAdd("par(fg=" + Util.parseColor(c) + ")");
         rCommandsPostAdd("par(fg=ablineSavedColor)");
      }
   }

   /**
    * DOCUMENT_ME
    *
    * @param t DOCUMENT_ME
    ********************************************************/
   private void linetype(String t)
   {
      if (t != null)
      {
         String lty = Util.parseLineTypes(t);
         rCommandsPreAdd("ablineSavedlty<-par(lty=" + lty + ")");
         rCommandsPostAdd("par(lty=ablineSavedlty)");
      }
   }

   /**
    * DOCUMENT_ME
    *
    * @param w DOCUMENT_ME
    ********************************************************/
   private void linewidth(double w)
   {
      if (!Double.isNaN(w))
      {
         rCommandsPreAdd("ablineSavedlwd<-par(lwd=" + w + ")");
         rCommandsPostAdd("par(lwd=ablineSavedlwd)");
      }
   }

   /**
    * DOCUMENT_ME
    *
    * @param side DOCUMENT_ME
    * @param at DOCUMENT_ME
    ********************************************************/
   private void abline(int side, String at)
   {
      variableAdd(orientation(side), at);
   }

   /**
    * DOCUMENT_ME
    *
    * @param side DOCUMENT_ME
    * @param x DOCUMENT_ME
    ********************************************************/
   private void abline(int side, double x)
   {
   }

   /**
    * DOCUMENT_ME
    *
    * @param side DOCUMENT_ME
    ********************************************************/
   private void abline(int side)
   {
      if ((side < 1) || (side > 4))
      {
         throw new IllegalArgumentException(n + " side=" + side);
      }

      variableAdd(orientation(side), "axTicks(" + side + ")");
   }

   /**
    * DOCUMENT_ME
    *
    * @param side DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   private String orientation(int side)
   {
      if ((side < 1) || (side > 4))
      {
         throw new IllegalArgumentException(n + " side=" + side);
      }

      String orientation = "v";

      if ((side == 2) || (side == 4))
      {
         orientation = "h";
      }

      return orientation;
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
