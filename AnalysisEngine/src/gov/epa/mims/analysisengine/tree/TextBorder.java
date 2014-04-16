package gov.epa.mims.analysisengine.tree;

import java.awt.Color;

import java.io.Serializable;


/**
 *
 * describes a bordered text object for a plot
 *
 * @author Tommy E. Cathey
 * @version $Id: TextBorder.java,v 1.2 2005/09/19 14:50:10 rhavaldar Exp $
 *
 **/
public class TextBorder
   extends Text
   implements Serializable,
              Cloneable
{
   /** serial version UID */
   static final long serialVersionUID = 1;

   /** back ground color */
   private Color backgroundColor = Color.yellow;

   /** color of the border around the text */
   private Color borderColor = Color.black;

   /** line style of the border around the text */
   private String borderLinestyle = SOLID;

   /** enable the drawing of the border */
   private boolean drawBorder = true;

   /** line width of the border around the text */
   private double borderLinewidth = 0.1;

   /** percentage of padding between the text and the border */
   private double padBottom = 0.5;

   /** percentage of padding between the text and the border */
   private double padLeft = 0.5;

   /** percentage of padding between the text and the border */
   private double padRight = 0.5;

   /** percentage of padding between the text and the border */
   private double padTop = 0.5;

   /**
    * set the background color
    *
    * @param arg background color
    ********************************************************/
   public void setBackgroundColor(java.awt.Color arg)
   {
      this.backgroundColor = arg;
   }

   /**
    * retrieve the background color
    *
    * @return background color
    ********************************************************/
   public java.awt.Color getBackgroundColor()
   {
      return backgroundColor;
   }

   /**
    * set the border color
    *
    * @param arg border color
    ********************************************************/
   public void setBorderColor(java.awt.Color arg)
   {
      this.borderColor = arg;
   }

   /**
    * retrieve the border color
    *
    * @return border color
    ********************************************************/
   public java.awt.Color getBorderColor()
   {
      return borderColor;
   }

   /**
    * set border line stlye
    *
    * @param arg border line stlye
    ********************************************************/
   public void setBorderLinestyle(java.lang.String arg)
   {
      this.borderLinestyle = arg;
   }

   /**
    * retrieve the border line style
    *
    * @return border line style
    ********************************************************/
   public java.lang.String getBorderLinestyle()
   {
      return borderLinestyle;
   }

   /**
    * set border line width
    *
    * @param arg border line width
    ********************************************************/
   public void setBorderLinewidth(double arg)
   {
      this.borderLinewidth = arg;
   }

   /**
    * retrieve border line width
    *
    * @return border line width
    ********************************************************/
   public double getBorderLinewidth()
   {
      return borderLinewidth;
   }

   /**
    * enable drawing of border
    *
    * @param arg draw border flag
    ********************************************************/
   public void setDrawBorder(boolean arg)
   {
      this.drawBorder = arg;
   }

   /**
    * retrieve draw border flag
    *
    * @return draw border flag
    ********************************************************/
   public boolean getDrawBorder()
   {
      return drawBorder;
   }

   /**
    * set percentage of padding between text and border
    *
    * @param arg percentage of padding between text and border
    ********************************************************/
   public void setPadBottom(double arg)
   {
      this.padBottom = arg;
   }

   /**
    * retrieve percentage of padding between text and border
    *
    * @return percentage of padding between text and border
    ********************************************************/
   public double getPadBottom()
   {
      return padBottom;
   }

   /**
    * set percentage of padding between text and border
    *
    * @param arg percentage of padding between text and border
    ********************************************************/
   public void setPadLeft(double arg)
   {
      this.padLeft = arg;
   }

   /**
    * retrieve percentage of padding between text and border
    *
    * @return percentage of padding between text and border
    ********************************************************/
   public double getPadLeft()
   {
      return padLeft;
   }

   /**
    * set percentage of padding between text and border
    *
    * @param arg percentage of padding between text and border
    ********************************************************/
   public void setPadRight(double arg)
   {
      this.padRight = arg;
   }

   /**
    * retrieve percentage of padding between text and border
    *
    * @return percentage of padding between text and border
    ********************************************************/
   public double getPadRight()
   {
      return padRight;
   }

   /**
    * set percentage of padding between text and border
    *
    * @param arg percentage of padding between text and border
    ********************************************************/
   public void setPadTop(double arg)
   {
      this.padTop = arg;
   }

   /**
    * retrieve percentage of padding between text and border
    *
    * @return percentage of padding between text and border
    ********************************************************/
   public double getPadTop()
   {
      return padTop;
   }

   /**
    * DOCUMENT_ME
    *
    * @param separator DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   public String toStringPretty(String separator)
   {
      StringBuffer b = new StringBuffer();
      b.append("setBackgroundColor(" + backgroundColor + ")");
      b.append(separator);
      b.append("setBorderColor(" + borderColor + ")");
      b.append(separator);
      b.append("setBorderLinestyle(" + borderLinestyle + ")");
      b.append(separator);
      b.append("setBorderLinewidth(" + borderLinewidth + ")");
      b.append(separator);
      b.append("setDrawBorder(" + drawBorder + ")");
      b.append(separator);
      b.append("setPadBottom(" + padBottom + ")");
      b.append(separator);
      b.append("setPadLeft(" + padLeft + ")");
      b.append(separator);
      b.append("setPadRight(" + padRight + ")");
      b.append(separator);
      b.append("setPadTop(" + padTop + ")");
      b.append(separator);

      b.append(super.toStringPretty(separator));

      return b.toString();
   }
}