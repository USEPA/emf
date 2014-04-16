package gov.epa.mims.analysisengine.tree;

import java.awt.Color;

import java.io.Serializable;


/**
 * describes a bordered text object for a plot
 *
 * @author    Tommy E. Cathey
 * @created   September 13, 2004
 * @version   $Id: Border.java,v 1.2 2005/09/19 14:50:10 rhavaldar Exp $
 */
public class Border
    implements LineTypeConstantsIfc,
   Serializable,
   Cloneable
{
   /** serial version UID */
   final static long serialVersionUID = 1;

   /** enable drawing of the border */
   private boolean enable = true;
   /** color of the border */
   private Color color = Color.black;
   /** line style to use for the border */
   private String Linestyle = Border.SOLID;
   /** line with to use for the border */
   private double linewidth = 1.0;


   /**
    * Sets the enable attribute of the Border object
    *
    * @param enable  The new enable value
    */
   public void setEnable(boolean enable)
   {
      this.enable = enable;
   }


   /**
    * Sets the color attribute of the Border object
    *
    * @param color  The new color value
    */
   public void setColor(Color color)
   {
      this.color = color;
   }


   /**
    * Sets the linestyle attribute of the Border object
    *
    * @param Linestyle  The new linestyle value
    */
   public void setLinestyle(String Linestyle)
   {
      this.Linestyle = Linestyle;
   }


   /**
    * Sets the linewidth attribute of the Border object
    *
    * @param linewidth  The new linewidth value
    */
   public void setLinewidth(double linewidth)
   {
      this.linewidth = linewidth;
   }


   /**
    * Gets the enable attribute of the Border object
    *
    * @return   The enable value
    */
   public boolean getEnable()
   {
      return enable;
   }


   /**
    * Gets the color attribute of the Border object
    *
    * @return   The color value
    */
   public Color getColor()
   {
      return color;
   }


   /**
    * Gets the linestyle attribute of the Border object
    *
    * @return   The linestyle value
    */
   public String getLinestyle()
   {
      return Linestyle;
   }


   /**
    * Gets the linewidth attribute of the Border object
    *
    * @return   The linewidth value
    */
   public double getLinewidth()
   {
      return linewidth;
   }

}

