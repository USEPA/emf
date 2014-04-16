package gov.epa.mims.analysisengine.tree;

import java.io.Serializable;


/**
 * Description of the Class
 *
 * @author    tsb
 * @created   September 7, 2004
 */
public class TextBlock
    extends TextAttribute
    implements FontConstantsIfc, UnitsConstantsIfc, CompassConstantsIfc, MarginConstantsIfc,
   Serializable
{
   /** Description of the Field */
   private double xBlockJustification = 0.5;
   /** Description of the Field */
   private double yBlockJustification = 0.5;
   /** Description of the Field */
   private double xJustification = 0.5;
   /** Description of the Field */
   private double yJustification = 0.5;
   /** Description of the Field */
   private java.lang.String position = TextBlock.SOUTHEAST;
   /** Description of the Field */
   private java.lang.String region = TextBlock.RIGHT_HAND_MARGIN;
   /** Description of the Field */
   private double x = Double.NaN;
   /** Description of the Field */
   private double y = Double.NaN;
   /** Description of the Field */
   private int unitsXY = TextBlock.USER_UNITS;
   /** Description of the Field */
   private BackgroundBox backgroundBox = new BackgroundBox();


   /**
    * Sets the backgroundBox attribute of the TextBlock object
    *
    * @param backgroundBox  The new backgroundBox value
    */
   public void setBackgroundBox(BackgroundBox backgroundBox)
   {
      this.backgroundBox = backgroundBox;
   }


   /**
    * Gets the backgroundBox attribute of the TextBlock object
    *
    * @return   The backgroundBox value
    */
   public BackgroundBox getBackgroundBox()
   {
      return backgroundBox;
   }


   /**
    * Sets the xBlockJustification attribute of the TextBlock object
    *
    * @param xBlockJustification  The new xBlockJustification value
    */
   public void setXBlockJustification(double xBlockJustification)
   {
      this.xBlockJustification = xBlockJustification;
   }


   /**
    * Sets the yBlockJustification attribute of the TextBlock object
    *
    * @param yBlockJustification  The new yBlockJustification value
    */
   public void setYBlockJustification(double yBlockJustification)
   {
      this.yBlockJustification = yBlockJustification;
   }


   /**
    * Gets the xBlockJustification attribute of the TextBlock object
    *
    * @return   The xBlockJustification value
    */
   public double getXBlockJustification()
   {
      return xBlockJustification;
   }


   /**
    * Gets the yBlockJustification attribute of the TextBlock object
    *
    * @return   The yBlockJustification value
    */
   public double getYBlockJustification()
   {
      return yBlockJustification;
   }


   /**
    * Sets the position attribute of the TextBlock object
    *
    * @param region          The new position value
    * @param position        The new position value
    * @param xJustification  The new position value
    * @param yJustification  The new position value
    */
   public void setPosition(java.lang.String region, java.lang.String position,
      double xJustification, double yJustification)
   {
      this.region = region;
      this.position = position;
      this.xJustification = xJustification;
      this.yJustification = yJustification;
      this.x = Double.NaN;
      this.y = Double.NaN;
   }


   /**
    * Sets the position attribute of the TextBlock object
    *
    * @param x               The new position value
    * @param y               The new position value
    * @param units           The new position value
    * @param xJustification  The new position value
    * @param yJustification  The new position value
    */
   public void setPosition(double x, double y, int units,
      double xJustification, double yJustification)
   {
      this.x = x;
      this.y = y;
      this.unitsXY = units;
      this.xJustification = xJustification;
      this.yJustification = yJustification;
      this.region = null;
      this.position = null;
   }


   /**
    * Gets the xJustification attribute of the TextBlock object
    *
    * @return   The xJustification value
    */
   public double getXJustification()
   {
      return xJustification;
   }


   /**
    * Gets the yJustification attribute of the TextBlock object
    *
    * @return   The yJustification value
    */
   public double getYJustification()
   {
      return yJustification;
   }


   /**
    * Gets the position attribute of the TextBlock object
    *
    * @return   The position value
    */
   public java.lang.String getPosition()
   {
      return position;
   }


   /**
    * Gets the region attribute of the TextBlock object
    *
    * @return   The region value
    */
   public java.lang.String getRegion()
   {
      return region;
   }


   /**
    * Gets the x attribute of the TextBlock object
    *
    * @return   The x value
    */
   public double getX()
   {
      return x;
   }


   /**
    * Gets the y attribute of the TextBlock object
    *
    * @return   The y value
    */
   public double getY()
   {
      return y;
   }


   /**
    * Gets the unitsXY attribute of the TextBlock object
    *
    * @return   The unitsXY value
    */
   public int getUnitsXY()
   {
      return unitsXY;
   }

}

