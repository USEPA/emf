package gov.epa.mims.analysisengine.tree;

import java.awt.Color;

import java.io.Serializable;


/**
 * describes a bordered text object for a plot
 *
 * @author    Tommy E. Cathey
 * @created   September 13, 2004
 * @version   $Id: BackgroundBox.java,v 1.2 2005/09/19 14:50:10 rhavaldar Exp $
 */
public class BackgroundBox
    implements LineTypeConstantsIfc,
   Serializable,
   Cloneable
{
   /** serial version UID */
   final static long serialVersionUID = 1;

   /** back ground color */
   private Color color = Color.white;

   /** enable the drawing of the rectangle */
   private boolean enable = true;

   /** percentage of padding between the text and the border */
   private double padBottom = 0.5;

   /** percentage of padding between the text and the border */
   private double padLeft = 0.5;

   /** percentage of padding between the text and the border */
   private double padRight = 0.5;

   /** percentage of padding between the text and the border */
   private double padTop = 0.5;
   
   /** Description of the Field */
   private Border border = new Border();



   /**
    * Sets the color attribute of the BackgroundBox object
    *
    * @param color  The new color value
    */
   public void setColor(Color color)
   {
      this.color = color;
   }


   /**
    * Sets the enable attribute of the BackgroundBox object
    *
    * @param enable  The new enable value
    */
   public void setEnable(boolean enable)
   {
      this.enable = enable;
   }


   /**
    * Sets the border attribute of the BackgroundBox object
    *
    * @param border  The new border value
    */
   public void setBorder(Border border)
   {
      this.border = border;
   }


   /**
    * Gets the border attribute of the BackgroundBox object
    *
    * @return   The border value
    */
   public Border getBorder()
   {
      return border;
   }


   /**
    * Sets the padding attribute of the BackgroundBox object
    *
    * @param padRight   The new padding value
    * @param padLeft    The new padding value
    * @param padTop     The new padding value
    * @param padBottom  The new padding value
    */
   public void setPadding(double padRight, double padLeft, double padTop, double padBottom)
   {
      this.padRight = padRight;
      this.padLeft = padLeft;
      this.padTop = padTop;
      this.padBottom = padBottom;
   }


   /**
    * Gets the color attribute of the BackgroundBox object
    *
    * @return   The color value
    */
   public Color getColor()
   {
      return color;
   }


   /**
    * Gets the enable attribute of the BackgroundBox object
    *
    * @return   The enable value
    */
   public boolean getEnable()
   {
      return enable;
   }


   /**
    * Gets the padBottom attribute of the BackgroundBox object
    *
    * @return   The padBottom value
    */
   public double getPadBottom()
   {
      return padBottom;
   }


   /**
    * Gets the padLeft attribute of the BackgroundBox object
    *
    * @return   The padLeft value
    */
   public double getPadLeft()
   {
      return padLeft;
   }


   /**
    * Gets the padRight attribute of the BackgroundBox object
    *
    * @return   The padRight value
    */
   public double getPadRight()
   {
      return padRight;
   }


   /**
    * Gets the padTop attribute of the BackgroundBox object
    *
    * @return   The padTop value
    */
   public double getPadTop()
   {
      return padTop;
   }

}

