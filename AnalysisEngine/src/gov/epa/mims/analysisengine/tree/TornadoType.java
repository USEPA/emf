package gov.epa.mims.analysisengine.tree;

import java.awt.Color;
import java.io.Serializable;


/**
 * Description of the Class
 *
 * @author    tsb
 * @created   September 29, 2004
 */
public class TornadoType
    extends AnalysisOption
    implements Serializable,
   Cloneable,
   LineTypeConstantsIfc,
   AnalysisOptionConstantsIfc
{
   /** fields */
   final static long serialVersionUID = 1;

   /** Description of the Field */
   private double[] ylim = null;

   /** color for the bar */
   private Color color = Color.red;

   /** color for the bar borders */
   private Color borderColor = Color.black;

   /** border line type */
   private String borderLty = LineTypeConstantsIfc.SOLID;

   /** border line width */
   private double borderLwd = 1.0;

   /** the widths of the bars */
   private double width = 1.0;

   /** space between the bars */
   private double spaceBetweenBars = 0;

   /*
    *  text
    */
   /** Description of the Field */
   private TextAttribute textAttribute = new TextAttribute();


   /**
    * Sets the textAttribute attribute of the TornadoType object
    *
    * @param textAttribute  The new textAttribute value
    */
   public void setTextAttribute(TextAttribute textAttribute)
   {
      this.textAttribute = textAttribute;
   }


   /**
    * Gets the textAttribute attribute of the TornadoType object
    *
    * @return   The textAttribute value
    */
   public TextAttribute getTextAttribute()
   {
      return textAttribute;
   }


   /**
    * Sets the color attribute of the TornadoType object
    *
    * @param color  The new color value
    */
   public void setColor(Color color)
   {
      this.color = color;
   }


   /**
    * Sets the ylim attribute of the TornadoType object
    *
    * @param ylim  The new ylim value
    */
   public void setYlim(double[] ylim)
   {
      this.ylim = ylim;
   }


   /**
    * Sets the borderColor attribute of the TornadoType object
    *
    * @param borderColor  The new borderColor value
    */
   public void setBorderColor(Color borderColor)
   {
      this.borderColor = borderColor;
   }


   /**
    * Sets the borderLty attribute of the TornadoType object
    *
    * @param borderLty  The new borderLty value
    */
   public void setBorderLty(String borderLty)
   {
      this.borderLty = borderLty;
   }


   /**
    * Sets the borderLwd attribute of the TornadoType object
    *
    * @param borderLwd  The new borderLwd value
    */
   public void setBorderLwd(double borderLwd)
   {
      this.borderLwd = borderLwd;
   }


   /**
    * Sets the width attribute of the TornadoType object
    *
    * @param width  The new width value
    */
   public void setWidth(double width)
   {
      this.width = width;
   }


   /**
    * Sets the spaceBetweenBars attribute of the TornadoType object
    *
    * @param spaceBetweenBars  The new spaceBetweenBars value
    */
   public void setSpaceBetweenBars(double spaceBetweenBars)
   {
      this.spaceBetweenBars = spaceBetweenBars;
   }


   /**
    * Gets the color attribute of the TornadoType object
    *
    * @return   The color value
    */
   public Color getColor()
   {
      return color;
   }


   /**
    * Gets the ylim attribute of the TornadoType object
    *
    * @return   The ylim value
    */
   public double[] getYlim()
   {
      return ylim;
   }


   /**
    * Gets the borderColor attribute of the TornadoType object
    *
    * @return   The borderColor value
    */
   public Color getBorderColor()
   {
      return borderColor;
   }


   /**
    * Gets the borderLty attribute of the TornadoType object
    *
    * @return   The borderLty value
    */
   public String getBorderLty()
   {
      return borderLty;
   }


   /**
    * Gets the borderLwd attribute of the TornadoType object
    *
    * @return   The borderLwd value
    */
   public double getBorderLwd()
   {
      return borderLwd;
   }


   /**
    * Gets the width attribute of the TornadoType object
    *
    * @return   The width value
    */
   public double getWidth()
   {
      return width;
   }


   /**
    * Gets the spaceBetweenBars attribute of the TornadoType object
    *
    * @return   The spaceBetweenBars value
    */
   public double getSpaceBetweenBars()
   {
      return spaceBetweenBars;
   }


   /**
    * Creates and returns a copy of this object
    *
    * @return   a copy of this object
    */
   public Object clone()
   {
      try
      {
         TornadoType clone = (TornadoType) super.clone();

         return clone;
      }
      catch (CloneNotSupportedException e)
      {
         return null;
      }
   }


   /**
    * Compares this object to the specified object.
    *
    * @param o  the object to compare this object against
    * @return   true if the objects are equal; false otherwise
    */
   public boolean equals(Object o)
   {
      boolean rtrn = true;

      if (o == null)
      {
         rtrn = false;
      }
      else if (o == this)
      {
         rtrn = true;
      }
      else if (o.getClass() != getClass())
      {
         rtrn = false;
      }
      else
      {
         TornadoType other = (TornadoType) o;

         rtrn = Util.equals(width, other.width)
             && (spaceBetweenBars == other.spaceBetweenBars)
             && Util.equals(color, other.color)
             && Util.equals(borderColor, other.borderColor)
             && Util.equals(borderLty, other.borderLty)
             && Util.equals(borderLwd, other.borderLwd);
      }

      return rtrn;
   }


   /**
    * describe object in a String
    *
    * @return   String describing object
    */
   public String toString()
   {
      return Util.toString(this);
   }

}

