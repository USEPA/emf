package gov.epa.mims.analysisengine.tree;

import java.awt.Color;

import java.io.Serializable;


/**
 * class to hold parameters related to displaying text strings
 *
 * @version $Revision: 1.2 $
 * @author $author$
 ********************************************************/
public class Text
   extends AnalysisOption
   implements Serializable,
              Cloneable,
              CompassConstantsIfc,
              LineTypeConstantsIfc,
              MarginConstantsIfc,
              TextIfc
{
   public static String RELATIVE2XAXIS = "relative2Xaxis";
   
   public static String RELATIVE2YAXIS = "relative2Yaxis";

   /** serial version UID */
   static final long serialVersionUID = 1;

   /** color of the text string */
   private Color color = Color.black;

   /** string position such as "NW" "N" "NE" "W" "C" "E" "SW" "S" "SE" */
   private String position = null;

   /** plot region */
   private String region = null;

   /** font style */
   private String style = null;

   /** the text string to draw */
   private String textString = "";

   /** format to use when printing timestamp */
   private String timeFormat = null;

   /** font typeface */
   protected String typeface = null;

   /** toggle the drawing of text on or off */
   private boolean enable = true;

   /**
    * flag to indicate whether X justification has been set
    */
   private boolean xJustSet = false;

   /**
    * flag to indicate whether absolute X and Y positions
    * have been selected by calling
    * {@link Text#setPosition(double x, double y)}
    */
   private boolean xySet = false;

   /**
    * flag to indicate whether Y justification has been set
    */
   private boolean yJustSet = false;

   /** text rotation in degrees */
   private double textDegreesRotation = Double.NaN;

   /** text expansion coefficient */
   private double textExpansion = 1.0;

   /** absolute x position in user coordinates */
   private double x;

   /** justification in the x direction */
   private double xJustification = 0.5;

   /** absolute y position in user coordinates */
   private double y;

   /** justification in the y direction */
   private double yJustification = 0.5;

   /**
    * set text color
    *
    * @param arg text color
    * @pre arg != null
    ******************************************************/
   public void setColor(java.awt.Color arg)
   {
      this.color = arg;
   }

   /**
    * retrieve text color
    *
    * @return text color
    ******************************************************/
   public java.awt.Color getColor()
   {
      return color;
   }

   /**
    * set drawing enabled flag
    *<ul>
    *<li>true - draw object
    *<li>false - do not draw object
    *</ul>
    *
    * @param enable flag
    ********************************************************/
   public void setEnable(boolean enable)
   {
      this.enable = enable;
   }

   /**
    * retrieve drawing enabled flag
    *
    * @return enable flag
    ********************************************************/
   public boolean getEnable()
   {
      return enable;
   }

   /**
    * set region
    * <pre>
    *
    *    RIGHT_HAND_MARGIN;
    *    LEFT_HAND_MARGIN;
    *    TOP_HAND_MARGIN;
    *    BOTTOM_HAND_MARGIN;
    *    PLOT_REGION;
    *
    * </pre>
    * set position string
    * <pre>
    *
    *    "NW" | "N" | "NE"
    *    -----------------
    *     "W" | "C" | "E"
    *    -----------------
    *    "SW" | "S" | "SE"
    *
    * position should be one of the Strings from the above grid
    * </pre>
    *
    * @param region position string
    * @param position position string
    * @param xJust the X justification
    * @param yJust the Y justification
    * @pre position != null
    ******************************************************/
   public void setPosition(String region, String position, double xJust, 
      double yJust)
   {
      this.region = region;
      this.position = position;
      this.xJustification = xJust;
      this.yJustification = yJust;
      xJustSet = true;
      yJustSet = true;
   }

   /**
    * set position string
    * <pre>
    *
    *    "NW" | "N" | "NE"
    *    -----------------
    *     "W" | "C" | "E"
    *    -----------------
    *    "SW" | "S" | "SE"
    *
    * position should be one of the Strings from the above grid
    * </pre>
    *
    * @param position position string
    * @param xJust the X justification
    * @param yJust the Y justification
    * @pre position != null
    ******************************************************/
   public void setPosition(String position, double xJust, double yJust)
   {
      this.position = position;
      this.xJustification = xJust;
      this.yJustification = yJust;
      xJustSet = true;
      yJustSet = true;
      xySet = false;
   }

   /**
    * set the absolute position of the legend in user coordinates
    *
    * @param x X position in user coordinates
    * @param y Y position in user coordinates
    ******************************************************/
   public void setPosition(double x, double y)
   {
      this.x = x;
      this.y = y;
      xySet = true;
   }

   /**
    * set position string
    * <pre>
    *
    *    "NW" | "N" | "NE"
    *    -----------------
    *     "W" | "C" | "E"
    *    -----------------
    *    "SW" | "S" | "SE"
    *
    * position should be one of the Strings from the above grid
    * </pre>
    *
    * @param arg position string
    * @pre arg != null
    * @deprecated deprecated_text
    ******************************************************/
   public void setPosition(java.lang.String arg)
   {
      this.position = arg;
   }

   /**
    * retrieve the position
    * <pre>
    *
    *    "NW" | "N" | "NE"
    *    -----------------
    *     "W" | "C" | "E"
    *    -----------------
    *    "SW" | "S" | "SE"
    *
    * position should be one of the Strings from the above grid
    * </pre>
    *
    * @return position string
    ******************************************************/
   public java.lang.String getPosition()
   {
      return position;
   }

   /**
    * retrieve the region in which draw
    *
    * @return region in which draw
    ********************************************************/
   public java.lang.String getRegion()
   {
      return region;
   }

   /**
    * set font style
    *
    * @param arg font style
    * @pre arg != null
    ******************************************************/
   public void setStyle(java.lang.String arg)
   {
      this.style = arg;
   }

   /**
    * retrieve font style
    *
    * @return font style
    ******************************************************/
   public java.lang.String getStyle()
   {
      return style;
   }

   /**
    * set the text rotation in degrees
    *
    * @param arg text rotation in degrees
    ******************************************************/
   public void setTextDegreesRotation(double arg)
   {
      this.textDegreesRotation = arg;
   }

   /**
    * retrieve the text rotation in degrees
    *
    * @return text rotation in degrees
    ******************************************************/
   public double getTextDegreesRotation()
   {
      return textDegreesRotation;
   }

   /**
    * set the text expansion coefficient
    *
    * @param arg the text expansion coefficient
    * @pre arg > 0.0
    ******************************************************/
   public void setTextExpansion(double arg)
   {
      this.textExpansion = arg;
   }

   /**
    * retrieve the text expansion coefficient
    *
    * @return the text expansion coefficient
    ******************************************************/
   public double getTextExpansion()
   {
      return textExpansion;
   }

   /**
    * set text string
    *
    * @param arg text string
    * @pre arg != null
    ******************************************************/
   public void setTextString(java.lang.String arg)
   {
      this.textString = arg;
   }

   /**
    * retrieve text string
    *
    * @return text string
    ******************************************************/
   public java.lang.String getTextString()
   {
      return textString;
   }

   /**
    * set text string
    *
    * @param arg time format string
    * @pre arg != null
    ******************************************************/
   public void setTimeFormat(java.lang.String arg)
   {
      this.timeFormat = arg;
   }

   /**
    * retrieve text string
    *
    * @return time format string
    ******************************************************/
   public java.lang.String getTimeFormat()
   {
      return timeFormat;
   }

   /**
    * set font typeface
    *
    * @param arg font typeface
    * @pre arg != null
    ******************************************************/
   public void setTypeface(java.lang.String arg)
   {
      this.typeface = arg;
   }

   /**
    * retrieve font typeface
    *
    * @return font typeface
    ******************************************************/
   public java.lang.String getTypeface()
   {
      return typeface;
   }

   /**
    * set the absolute X position in user coordinates
    *
    * @param arg X position in user coordinates
    * @deprecated deprecated_text
    ******************************************************/
   public void setX(double arg)
   {
      this.x = arg;
   }

   /**
    * retrieve the absolute X position in user coordinates
    *
    * @return X position in user coordinates
    ******************************************************/
   public double getX()
   {
      return x;
   }

   /**
    * set the X justification
    * <pre>
    * o range of values = [0,1]
    * o 0.0 = left justified
    * o 0.5 = centered (DEFAULT)
    * o 1.0 = right justified
    * </pre>
    *
    * @param arg the X justification
    * @deprecated deprecated_text
    ******************************************************/
   public void setXJustification(double arg)
   {
      this.xJustification = arg;
      xJustSet = true;
   }

   /**
    * retrieve the X justification set in setXJustification(double arg)
    * <pre>
    * o range of values = [0,1]
    * o 0.0 = left justified
    * o 0.5 = centered (DEFAULT)
    * o 1.0 = right justified
    * </pre>
    *
    * @return X justification
    ******************************************************/
   public double getXJustification()
   {
      return xJustification;
   }

   /**
    * retrieve flag which indicates if absolute X and Y positions
    * have been selected by calling
    * {@link Text#setPosition(double x, double y)}
    *
    * @return flag xySet
    ********************************************************/
   public boolean getXYset()
   {
      return xySet;
   }

   /**
    * CONSIDER REMOVING
    *
    * @return CONSIDER REMOVING
    * @deprecated deprecated_text
    ********************************************************/
   public boolean getXjustSet()
   {
      return xJustSet;
   }

   /**
    * set the absolute Y position in user coordinates
    *
    * @param arg Y position in user coordinates
    * @deprecated deprecated_text
    ******************************************************/
   public void setY(double arg)
   {
      this.y = arg;
   }

   /**
    * retrieve the absolute Y position in user coordinates
    *
    * @return the absolute Y position
    ******************************************************/
   public double getY()
   {
      return y;
   }

   /**
    * set the Y justification
    * <pre>
    * o range of values = [0,1]
    * o 0.0 = bottom justified
    * o 0.5 = centered (DEFAULT)
    * o 1.0 = top justified
    * </pre>
    * @param arg Y justification
    * @deprecated deprecated_text
    ******************************************************/
   public void setYJustification(double arg)
   {
      this.yJustification = arg;
      yJustSet = true;
   }

   /**
    * retrieve the Y justification set in setYJustification(double arg)
    * <pre>
    * o range of values = [0,1]
    * o 0.0 = bottom justified
    * o 0.5 = centered (DEFAULT)
    * o 1.0 = top justified
    * </pre>
    * @return Y justification
    ******************************************************/
   public double getYJustification()
   {
      return yJustification;
   }

   /**
    * CONSIDER REMOVING
    *
    * @return CONSIDER REMOVING
    * @deprecated deprecated_text
    ********************************************************/
   public boolean getYjustSet()
   {
      return yJustSet;
   }

   /**
    * Creates and returns a copy of this object
    *
    * @return a copy of this object
    ******************************************************/
   public Object clone()
   {
      try
      {
         return super.clone();
      }
      catch (CloneNotSupportedException e)
      {
         return null;
      }
   }

   /**
    * Compares this object to the specified object.
    *
    * @param o the object to compare this object against
    *
    * @return true if the objects are equal; false otherwise
    ********************************************************/
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
         Text other = (Text) o;

         rtrn = ((position == null)
                 ? (other.position == null)
                 : (position.equals(other.position)))
                && (xJustification == other.xJustification)
                && (yJustification == other.yJustification) && (x == other.x)
                && (y == other.y) && (xySet == other.xySet)
                && (xJustSet == other.xJustSet)
                && (yJustSet == other.yJustSet) && (enable == other.enable)
                && (textExpansion == other.textExpansion)
                && (textDegreesRotation == other.textDegreesRotation)
                && ((region == null)
                    ? (other.region == null)
                    : (region.equals(other.region)))
                && ((typeface == null)
                    ? (other.typeface == null)
                    : (typeface.equals(other.typeface)))
                && ((style == null)
                    ? (other.style == null)
                    : (style.equals(other.style)))
                && ((textString == null)
                    ? (other.textString == null)
                    : (textString.equals(other.textString)))
                && ((timeFormat == null)
                    ? (other.timeFormat == null)
                    : (timeFormat.equals(other.timeFormat)))
                && ((color == null)
                    ? (other.color == null)
                    : (color.equals(other.color)));
      }

      return rtrn;
   }

   /**
    * print object to string
    *
    *
    * @return String describing object
    ******************************************************/
   public String toString()
   {
      return textString;
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
      b.append("setColor(" + color + ")");
      b.append(separator);
      b.append("setEnable(" + enable + ")");
      b.append(separator);
      b.append("setStyle(" + style + ")");
      b.append(separator);
      b.append("setTextExpansion(" + textExpansion + ")");
      b.append(separator);
      b.append("setTypeface(" + typeface + ")");
      b.append(separator);
      b.append("setTextString(" + textString + ")");
      b.append(separator);

      return b.toString();
   }
}
