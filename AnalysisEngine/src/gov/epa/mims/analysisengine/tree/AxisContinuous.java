package gov.epa.mims.analysisengine.tree;

import java.awt.Color;

import java.io.Serializable;


/**
 *
 * @author Tommy E. Cathey
 * @version $Id: AxisContinuous.java,v 1.2 2005/09/19 14:50:10 rhavaldar Exp $
 *
 **/
public abstract class AxisContinuous
   extends Axis
   implements Serializable,
              Cloneable
{
   /** serial version UID */
   static final long serialVersionUID = 1;

   /** grid color */
   private Color gridColor = null;

   /**
    * line style set by call to
    * {@link #setGridlineStyle(java.lang.String)}
    */
   private String gridlineStyle = SOLID;

   /** enable or disable drawing of the grid */
   private boolean gridEnable = false;

   /** enable/disable drawing of grid tickmarks */
   private boolean gridTickmarkEnable = true;

   /** grid tick mark length */
   private double gridTickmarkLength = Double.NaN;

   /** line width to use when drawing the grid */
   private double gridlineWidth = Double.NaN;

   /**
    * Creates a new default AxisGrid object. The grid lines line up with
    * the tick marks.
    ********************************************************/
   public abstract void setGrid();

   /**
    * set color to use when drawing the grid
    * <p><A HREF="doc-files/ExampleAxisGrid08.html"><B>Example</B></A>
    *
    * @param arg desired color of grid
    ******************************************************/
   public void setGridColor(java.awt.Color arg)
   {
      this.gridColor = arg;
   }

   /**
    * retrieve color to use when drawing the grid
    *
    * @return desired color of grid
    ******************************************************/
   public java.awt.Color getGridColor()
   {
      return gridColor;
   }

   /**
    * set enable and disable drawing of grid flag
    * <p><A HREF="doc-files/ExampleAxisGrid07.html"><B>Example</B></A>
    *
    * @param arg true-> draw grid; false-> do not draw grid
    ******************************************************/
   public void setGridEnable(boolean arg)
   {
      this.gridEnable = arg;
   }

   /**
    * retrive enable and disable drawing of grid flag
    *
    * @return true-> draw grid; false-> do not draw grid
    ******************************************************/
   public boolean getGridEnable()
   {
      return gridEnable;
   }

   /**
    * set enable and disable drawing of grid tickmark flag
    *
    * @param arg true-> draw grid ticks; false-> do not draw grid ticks
    ******************************************************/
   public void setGridTickmarkEnable(boolean arg)
   {
      this.gridTickmarkEnable = arg;
   }

   /**
    * retrive enable and disable drawing of grid tickmarks
    *
    * @return true-> draw grid; false-> do not draw grid tickmarks
    ******************************************************/
   public boolean getGridTickmarkEnable()
   {
      return gridTickmarkEnable;
   }

   /**
    * set grid tick mark length
    *
    * @param arg grid tick mark length
    ******************************************************/
   public void setGridTickmarkLength(double arg)
   {
      this.gridTickmarkLength = arg;
   }

   /**
    * retrieve grid tick mark length
    *
    * @return grid tick mark length
    ******************************************************/
   public double getGridTickmarkLength()
   {
      return gridTickmarkLength;
   }

   /**
    * set the grid line style
    * <p>Valid values are of following two types
    * <ol>
    * <li>Predefined styles
    * <ul>
    * <li>{@link Axis#SOLID}
    * <li>{@link Axis#DASHED}
    * <li>{@link Axis#DOTTED}
    * <li>{@link Axis#DOTDASH}
    * <li>{@link Axis#LONGDASH}
    * <li>{@link Axis#TWODASH}
    * <li>{@link Axis#BLANK}
    * </ul>
    * <li>User defined styles
    * <ul>
    * <li>a string of up to 8 characters
    * (from 0:9, "A":"F") may be given, giving the length of line segments
    * which are alternatively drawn and skipped.
    * </ul>
    * </ol>
    * <p><A HREF="doc-files/ExampleAxisGrid05.html"><B>Example</B></A>
    *
    * @param arg linetype used to draw grid
    ******************************************************/
   public void setGridlineStyle(java.lang.String arg)
   {
      this.gridlineStyle = arg;
   }

   /**
    * retrieve the grid line style
    *
    * @return linetype used to draw grid
    ******************************************************/
   public java.lang.String getGridlineStyle()
   {
      return gridlineStyle;
   }

   /**
    * set line width used to draw grid
    * <p><A HREF="doc-files/ExampleAxisGrid06.html"><B>Example</B></A>
    *
    * @param arg line width
    ******************************************************/
   public void setGridlineWidth(double arg)
   {
      this.gridlineWidth = arg;
   }

   /**
    * retrieve line width used to draw grid
    *
    * @return line width
    ******************************************************/
   public double getGridlineWidth()
   {
      return gridlineWidth;
   }

   /**
    * Creates and returns a copy of this object
    *
    * @return a copy of this object
    * @throws CloneNotSupportedException is cloning not supported
    ******************************************************/
   public Object clone()
                throws CloneNotSupportedException
   {
      AxisContinuous clone = (AxisContinuous) super.clone();

      clone.gridEnable = gridEnable;

      return clone;
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

      if (!super.equals(o))
      {
         rtrn = false;
      }
      else if (o == null)
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
         AxisContinuous other = (AxisContinuous) o;

         rtrn = ((Util.equals(gridlineStyle, other.gridlineStyle))
                && (Util.equals(gridlineWidth, other.gridlineWidth))
                && (Util.equals(gridEnable, other.gridEnable))
                && (Util.equals(gridTickmarkEnable, other.gridTickmarkEnable))
                && (Util.equals(gridTickmarkLength, other.gridTickmarkLength))
                && (Util.equals(gridColor, other.gridColor)));
      }

      return rtrn;
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