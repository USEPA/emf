package gov.epa.mims.analysisengine.tree;

import java.awt.Color;

import java.io.Serializable;


/**
 * Description of the Class
 *
 * @author    tsb
 * @created   September 7, 2004
 */
public class TextAttribute
    implements FontConstantsIfc,
   Serializable
{
   /** Description of the Field */
   private Color color = Color.black;

   /** Description of the Field */
   private String font = PLAIN_TEXT;

   /** Description of the Field */
   private double cex = 1.0;
   /** Description of the Field */
   private double lineSpacing = 0.5;


   /**
    * Sets the lineSpacing attribute of the TextAttribute object
    *
    * @param lineSpacing  The new lineSpacing value
    */
   public void setLineSpacing(double lineSpacing)
   {
      this.lineSpacing = lineSpacing;
   }


   /**
    * Gets the lineSpacing attribute of the TextAttribute object
    *
    * @return   The lineSpacing value
    */
   public double getLineSpacing()
   {
      return lineSpacing;
   }


   /**
    * Sets the color attribute of the TextAttribute object
    *
    * @param color  The new color value
    */
   public void setColor(Color color)
   {
      this.color = color;
   }


   /**
    * Sets the font attribute of the TextAttribute object
    *
    * @param font  The new font value
    */
   public void setFont(String font)
   {
      this.font = font;
   }


   /**
    * Sets the cex attribute of the TextAttribute object
    *
    * @param cex  The new cex value
    */
   public void setCex(double cex)
   {
      this.cex = cex;
   }


   /**
    * Gets the color attribute of the TextAttribute object
    *
    * @return   The color value
    */
   public Color getColor()
   {
      return color;
   }


   /**
    * Gets the font attribute of the TextAttribute object
    *
    * @return   The font value
    */
   public String getFont()
   {
      return font;
   }


   /**
    * Gets the cex attribute of the TextAttribute object
    *
    * @return   The cex value
    */
   public double getCex()
   {
      return cex;
   }

}

