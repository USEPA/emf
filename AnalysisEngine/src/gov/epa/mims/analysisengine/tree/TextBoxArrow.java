package gov.epa.mims.analysisengine.tree;

import java.awt.Color;

import java.io.Serializable;


/**
 * @author Tommy E. Cathey
 * @version $Id: TextBoxArrow.java,v 1.2 2005/09/19 14:50:10 rhavaldar Exp $
 *
 * class to hold parameters related to displaying text box arrows
 *
 * <p>Elided Code Example:
 * <pre>
 *    :
 *    :
 * TextBox textBox = new TextBox();
 *    :
 *    :
 * TextBoxArrow arrow1 = new TextBoxArrow();
 * TextBoxArrow arrow2 = new TextBoxArrow();
 *
 * arrow1.setLength(0.25);
 * arrow1.setColor(java.awt.Color.blue);
 * arrow1.setEnable(true);
 * arrow1.setPosition(3.9845, 3.6618,TextBoxArrow.USER_UNITS);
 * arrow1.setBoxContactPt(TextBoxArrow.WEST);
 * arrow1.setAngle(30.0);
 * arrow1.setCode(1);
 * arrow1.setLty(TextBoxArrow.SOLID);
 * arrow1.setWidth(3.0);
 * arrow1.setBackoff(1.0);
 *
 * arrow2.setLength(0.75);
 * arrow2.setColor(java.awt.Color.red);
 * arrow2.setEnable(true);
 * arrow2.setPosition(0.6945,4.9186,TextBoxArrow.USER_UNITS);
 * arrow2.setBoxContactPt(TextBoxArrow.WEST);
 * arrow2.setAngle(8.0);
 * arrow2.setCode(3);
 * arrow2.setLty(TextBoxArrow.SOLID);
 * arrow2.setWidth(1.0);
 * arrow2.setBackoff(0.0);
 *
 * textBox.addArrow(arrow1);
 * textBox.addArrow(arrow2);
 *    :
 *    :
 * </pre>
 * @version $Revision: 1.2 $
 * @author Tommy E. Cathey
 ********************************************************/
public class TextBoxArrow
   implements Serializable,
              Cloneable,
              CompassConstantsIfc,
              UnitsConstantsIfc,
              LineTypeConstantsIfc
{
   /** serial version UID */
   static final long serialVersionUID = 1;

   /** arrow type used in {@link #setCode(int)} */
   public static final int ARROW_POINTS_TO_BOX = 1;

   /** arrow type used in {@link #setCode(int)} */
   public static final int ARROW_POINTS_FROM_BOX = 2;

   /** arrow type used in {@link #setCode(int)} */
   public static final int ARROW_IS_DOUBLE_HEADED = 3;

   /** color of the arrow */
   private Color color = Color.blue;

   /** string position - should be set to one of the values in 
     * {@link CompassConstantsIfc} except CENTER
     */
   private String boxContactPt = EAST;

   /** toggle the drawing of the arrow on or off */
   private boolean enable = true;

   /** x position in {@link #unitsXY} coordinates of arrow's end */
   private double x;

   /** y position in {@link #unitsXY} coordinates of arrow's end */
   private double y;

   /** x & y units 
    * @see UnitsConstantsIfc
    */
   private int unitsXY = FIGURE_UNITS;

   /** length of arrow head as % of character height */
   private double length = 0.5;

   /** angle of arrow head */
   private double angle = 30.0;

   /** type of arrow valid values are {@link #ARROW_POINTS_TO_BOX},
     * {@link #ARROW_POINTS_FROM_BOX} or {@link #ARROW_IS_DOUBLE_HEADED}
     */
   private int code = 2;

   /** line style - should be set to one of the values in 
     * {@link LineTypeConstantsIfc} 
     */
   private String lty = LineTypeConstantsIfc.SOLID;

   /** arrow line width */
   private double width = 1.0;

   /** backoff from the arrow head from pt ({@link #x},{@link #y})
     * as % of character height 
     */
   private double backoff = 0.0;

   /**
    * set the arrow color
    * <br><A HREF="doc-files/ExampleTextBoxArrow01.html"><B>View Example</B></A>
    *
    * @param arg arrow color
    * @pre arg != null
    ******************************************************/
   public void setColor(java.awt.Color arg)
   {
      this.color = arg;
   }

   /**
    * retrieve arrow color
    *
    * @return arrow color
    ******************************************************/
   public java.awt.Color getColor()
   {
      return color;
   }

   /**
    * set drawing enabled flag
    * <br><A HREF="doc-files/ExampleTextBoxArrow01.html"><B>View Example</B></A>
    * <br><A HREF="doc-files/ExampleTextBoxArrow05.html"><B>View Example</B></A>
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
    * set the position of the arrow head in {@link #unitsXY} coordinates
    * <br><A HREF="doc-files/ExampleTextBoxArrow01.html"><B>View Example</B></A>
    *
    * @param x X position in {@link #unitsXY} coordinates
    * @param y Y position in {@link #unitsXY} coordinates
    * @param unitsXY the unit coordinates to use - should be
    * one of the values from {@link #UnitsConstantsIfc}
    * @pre unitsXY == USER_UNITS || unitsXY == FIGURE_UNITS || 
    * unitsXY == DEVICE_UNITS
    ******************************************************/
   public void setPosition(double x, double y, int unitsXY)
   {
      this.x = x;
      this.y = y;
      this.unitsXY = unitsXY;
   }

   /**
    * retrieve unitsXY
    *
    * @return unitsXY
    ********************************************************/
   public int getUnitsXY()
   {
      return this.unitsXY;
   }

   /**
    * retrieve the X position in {@link #unitsXY} coordinates
    *
    * @return the X position
    ******************************************************/
   public double getX()
   {
      return x;
   }

   /**
    * retrieve the Y position in {@link #unitsXY} coordinates
    *
    * @return the absolute Y position
    ******************************************************/
   public double getY()
   {
      return y;
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
         TextBoxArrow clone = (TextBoxArrow) super.clone();

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
         TextBoxArrow other = (TextBoxArrow) o;

         rtrn = Util.equals(color, other.color)
                && Util.equals(boxContactPt, other.boxContactPt)
                && Util.equals(enable, other.enable)
                && Util.equals(x, other.x) && Util.equals(y, other.y)
                && Util.equals(unitsXY, other.unitsXY)
                && Util.equals(length, other.length)
                && Util.equals(angle, other.angle)
                && Util.equals(code, other.code)
                && Util.equals(lty, other.lty)
                && Util.equals(width, other.width)
                && Util.equals(backoff, other.backoff);
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
      return Util.toString(this);
   }

   /**
    * retrieve the arrow's box contact point
    *
    * @return boxContactPt
    ********************************************************/
   public java.lang.String getBoxContactPt()
   {
      return boxContactPt;
   }

   /**
    * set the arrow's box contact point
    * <br><A HREF="doc-files/ExampleTextBoxArrow01.html"><B>View Example</B></A>
    *
    * @param arg arrow's box contact point - should be one of
    * the values from {@link CompassConstantsIfc} except CENTER
    * @pre arg == NORTHWEST || arg == NORTH || arg == NORTHEAST
    * || arg == WEST || arg == EAST || arg == SOUTHWEST
    * || arg == SOUTH || arg == SOUTHEAST
    ********************************************************/
   public void setBoxContactPt(java.lang.String arg)
   {
      this.boxContactPt = arg;
   }

   /**
    * retrieve the length of the arrow head
    *
    * @return length as a % for character height
    ********************************************************/
   public double getLength()
   {
      return length;
   }

   /**
    * set length of the arrow head as % for character height
    * <br><A HREF="doc-files/ExampleTextBoxArrow01.html"><B>View Example</B></A>
    *
    * @param arg length of the arrow head as % for character height
    ********************************************************/
   public void setLength(double arg)
   {
      this.length = arg;
   }

   /**
    * retrieve the arrow head angle
    *
    * @return arrow head angle
    ********************************************************/
   public double getAngle()
   {
      return angle;
   }

   /**
    * set the arrow head angle
    * <br><A HREF="doc-files/ExampleTextBoxArrow01.html"><B>View Example</B></A>
    *
    * @param arg arrow head angle
    ********************************************************/
   public void setAngle(double arg)
   {
      this.angle = arg;
   }

   /**
    * set the type of arrow
    *
    * @return type of arrow
    ********************************************************/
   public int getCode()
   {
      return code;
   }

   /**
    * set the type of arrow
    * <br><A HREF="doc-files/ExampleTextBoxArrow01.html"><B>View Example</B></A>
    * <br><A HREF="doc-files/ExampleTextBoxArrow06.html"><B>View Example</B></A>
    *
    * @param arg type of arrow - should be one the values
    * {@link #ARROW_POINTS_TO_BOX} , {@link #ARROW_POINTS_FROM_BOX}
    * or {@link #ARROW_IS_DOUBLE_HEADED}
    * @pre arg == ARROW_POINTS_TO_BOX || arg == ARROW_POINTS_FROM_BOX
    * || arg == ARROW_IS_DOUBLE_HEADED
    ********************************************************/
   public void setCode(int arg)
   {
      this.code = arg;
   }

   /**
    * set the line type of the arrow
    *
    * @return line type of the arrow
    ********************************************************/
   public java.lang.String getLty()
   {
      return lty;
   }

   /**
    * set the line type of the arrow
    * <br><A HREF="doc-files/ExampleTextBoxArrow01.html"><B>View Example</B></A>
    * <br><A HREF="doc-files/ExampleTextBoxArrow02.html"><B>View Example</B></A>
    * <br><A HREF="doc-files/ExampleTextBoxArrow03.html"><B>View Example</B></A>
    * <br><A HREF="doc-files/ExampleTextBoxArrow04.html"><B>View Example</B></A>
    *
    * @param arg line type of the arrow - should be one the values
    * from {@link LineTypeConstantsIfc}
    ********************************************************/
   public void setLty(java.lang.String arg)
   {
      this.lty = arg;
   }

   /**
    * retrieve the width of the arrow's line
    *
    * @return width of the arrow's line
    ********************************************************/
   public double getWidth()
   {
      return width;
   }

   /**
    * set the width of the arrow's line
    * <br><A HREF="doc-files/ExampleTextBoxArrow01.html"><B>View Example</B></A>
    *
    * @param arg width of the arrow's line
    ********************************************************/
   public void setWidth(double arg)
   {
      this.width = arg;
   }

   /**
    * retrieve the arrow tip backoff value as a % of char height
    *
    * @return arrow tip backoff value as a % of char height
    ********************************************************/
   public double getBackoff()
   {
      return backoff;
   }

   /**
    * retrieve the arrow tip backoff value as a % of char height
    * <br><A HREF="doc-files/ExampleTextBoxArrow01.html"><B>View Example</B></A>
    *
    * @param arg arrow tip backoff value as a % of char height
    ********************************************************/
   public void setBackoff(double arg)
   {
      this.backoff = arg;
   }

   /**
    * print class as a pretty String
    *
    * @param separator separator to separate the fields "\n" works well
    *
    * @return class as a pretty String
    ********************************************************/
   public String toStringPretty(String separator)
   {
      StringBuffer b = new StringBuffer();
      b.append("setPosition(" + x + "," + y + "," + unitsXY + ")");
      b.append(separator);
      b.append("setLength(" + length + ")");
      b.append(separator);
      b.append("setColor(" + color + ")");
      b.append(separator);
      b.append("setEnable(" + enable + ")");
      b.append(separator);
      b.append("setBoxContactPt(" + boxContactPt + ")");
      b.append(separator);
      b.append("setAngle(" + angle + ")");
      b.append(separator);
      b.append("setCode(" + code + ")");
      b.append(separator);
      b.append("setLty(" + lty + ")");
      b.append(separator);
      b.append("setWidth(" + width + ")");
      b.append(separator);
      b.append("setBackoff(" + backoff + ")");
      b.append(separator);

      return b.toString();
   }
}