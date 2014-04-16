package gov.epa.mims.analysisengine.tree;

import java.io.Serializable;

import java.util.Date;
import java.util.TimeZone;


/**
 *
 * @author Tommy E. Cathey
 * @version $Id: AxisTime.java,v 1.2 2005/09/19 14:50:10 rhavaldar Exp $
 *
 **/
public class AxisTime
   extends AxisContinuous
   implements Serializable,
              Cloneable
{
   /** serial version UID */
   static final long serialVersionUID = 1;

   /** final pt to allow grid to be drawn */
   private Date finalPoint = null;

   /** desired location of first tick mark */
   private Date firstTickMark;

   /** initial pt to start grid */
   private Date initialPoint = null;

   /** number of intervals in grid NOTE: intervalCount and
    *  increment are mutually exclusive
    */
   private Integer intervalCount = null;

   /** increment for grid spacing NOTE: intervalCount and
    *  increment are mutually exclusive
    */
   private Long gridIncrement = null;

   /** tick label format */
   private String tickLabelFormat = null;

   /** format used to display the constant time label */
   private Text constantTimeLabelFormat = null;

   /** time zone */
   private TimeZone timeZone = null;

   /** axis range [min,max] */
   private Date[] axisRange = null;

   /** separation between tick marks in milliseconds */
   private long tickIncrement;

   /**
    * set the axis range
    *
    * @param min min X value
    * @param max max X value
    ********************************************************/
   public void setAxisRange(Object min, Object max)
   {
      if ((min != null) & (max != null))
      {
         this.axisRange = (new Date[] 
         {
            (Date) min, 
            (Date) max
         });
      }
      else
      {
         this.axisRange = null;
      }
   }

   /**
    * retrieve the axis range
    *
    * @return Date[] = [min,max]
    ********************************************************/
   public Object[] getAxisRange()
   {
      return (axisRange == null)
             ? null
             : (Date[])this.axisRange.clone();
   }

   /**
    * set formatting for constant part of timestamp
    *
    * @param arg Text describing the format of constant timestamp
    * @pre arg != null
    * @post this.constantTimeLabelFormat != null
    ******************************************************/
   public void setConstantTimeLabelFormat(Text arg)
   {
      this.constantTimeLabelFormat = arg;
   }

   /**
    * retrieve formatting for constant part of timestamp
    *
    * @return format string
    ******************************************************/
   public Text getConstantTimeLabelFormat()
   {
      return (constantTimeLabelFormat == null)
             ? null
             : (Text) constantTimeLabelFormat.clone();
   }

   /**
    * retrieve the final axis Date
    *
    * @return final axis Date
    ******************************************************/
   public java.util.Date getFinalPoint()
   {
      return finalPoint;
   }

   /**
    * set the desired first tick mark location
    *
    * @param arg the location to place the first tick mark
    * @pre arg != null
    * @post this.firstTickMark != null
    ******************************************************/
   public void setFirstTickMark(java.util.Date arg)
   {
      this.firstTickMark = arg;
   }

   /**
    * retrieve the desired first tick mark location
    *
    * @return the location to place the first tick mark
    ******************************************************/
   public java.util.Date getFirstTickMark()
   {
      return firstTickMark;
   }

   /**
    * Creates a new default AxisGrid object. The grid lines line up with
    * the tick marks.
    * <p><A HREF="doc-files/ExampleAxisGrid01.html"><B>Example</B></A>
    ********************************************************/
   public void setGrid()
   {
      this.initialPoint = null;
      this.gridIncrement = null;
      this.finalPoint = null;
   }

   /**
    * Creates a new TimeSeriesAxisGrid object. The grid lines start at
    * "initialPoint". Additional lines are drawn at increments of
    * "increment".
    * <p><A HREF="doc-files/ExampleTimeSeriesAxisGrid02.html"><B>Example</B></A>
    *
    * @param initialPoint initial pt to start grid
    * @param gridIncrement grid spacing
    ********************************************************/
   public void setGrid(Date initialPoint, Long gridIncrement)
   {
      String cName = getClass().getName();

      if (initialPoint == null)
      {
         throw new IllegalArgumentException(cName + " initialPoint==null");
      }

      if (gridIncrement == null)
      {
         throw new IllegalArgumentException(cName + " gridIncrement==null");
      }

      if (gridIncrement.longValue() <= 0)
      {
         throw new IllegalArgumentException(cName
                                            + " gridIncrement.longValue()="
                                            + gridIncrement.longValue());
      }

      this.initialPoint = initialPoint;
      this.gridIncrement = gridIncrement;
      this.finalPoint = null;
   }

   /**
    * Creates a new TimeSeriesAxisGrid object. The grid lines start at
    * "initialPoint". Additional lines are drawn at increments of
    * "increment". Grid lines are not drawn past the "finalPoint".
    * <p><A HREF="doc-files/ExampleTimeSeriesAxisGrid03.html"><B>Example</B></A>
    *
    * @param initialPoint initial pt to start grid
    * @param gridIncrement grid spacing
    * @param finalPoint last pt to allow grid to be drawn
    ********************************************************/
   public void setGrid(Date initialPoint, Long gridIncrement, Date finalPoint)
   {
      String cName = getClass().getName();

      if (initialPoint == null)
      {
         throw new IllegalArgumentException(cName + " initialPoint==null");
      }

      if (gridIncrement == null)
      {
         throw new IllegalArgumentException(cName + " gridIncrement==null");
      }

      if (gridIncrement.longValue() <= 0)
      {
         throw new IllegalArgumentException(cName
                                            + " gridIncrement.longValue()="
                                            + gridIncrement.longValue());
      }

      if (finalPoint == null)
      {
         throw new IllegalArgumentException(cName + " finalPoint==null");
      }

      this.initialPoint = initialPoint;
      this.gridIncrement = gridIncrement;
      this.finalPoint = finalPoint;
   }

   /**
    * Creates a new TimeSeriesAxisGrid object. The grid lines start at
    * "initialPoint" and end at "finalPoint". There are "intervalCount"
    * grid spaces between the end points.
    * <p><A HREF="doc-files/ExampleTimeSeriesAxisGrid04.html"><B>Example</B></A>
    *
    * @param initialPoint initial pt to start grid
    * @param intervalCount number of intervals in grid
    * @param finalPoint last pt to allow grid to be drawn
    ********************************************************/
   public void setGrid(Date initialPoint, Integer intervalCount, 
      Date finalPoint)
   {
      String cName = getClass().getName();

      if (initialPoint == null)
      {
         throw new IllegalArgumentException(cName + " initialPoint==null");
      }

      if (intervalCount == null)
      {
         throw new IllegalArgumentException(cName + " intervalCount==null");
      }

      if (finalPoint == null)
      {
         throw new IllegalArgumentException(cName + " finalPoint==null");
      }

      this.initialPoint = initialPoint;
      this.intervalCount = intervalCount;
      this.finalPoint = finalPoint;
   }

   /**
    * retrieve desired grid increment
    *
    * @return desired grid increment
    ******************************************************/
   public Long getGridIncrement()
   {
      return gridIncrement;
   }

   /**
    * retrieve the initial axis Date
    *
    * @return initial axis Date
    ******************************************************/
   public java.util.Date getInitialPoint()
   {
      return initialPoint;
   }

   /**
    * retieve the interval count
    *
    * @return interval count
    ******************************************************/
   public java.lang.Integer getIntervalCount()
   {
      return intervalCount;
   }

   /**
    * set desired tick increment in milliseconds
    *
    * @param arg desired tick increment in milliseconds
    * @pre arg > 0
    * @post this.tickIncrement == arg
    ******************************************************/
   public void setTickIncrement(long arg)
   {
      this.tickIncrement = arg;
   }

   /**
    * retrieve desired tick increment in milliseconds
    *
    * @return desired tick increment in milliseconds
    ******************************************************/
   public long getTickIncrement()
   {
      return tickIncrement;
   }

   /**
    * set formatting for tick label portion of timestamps
    *
    * @param arg format string
    * @pre arg != null
    * @post this.tickLabelFormat != null
    ******************************************************/
   public void setTickLabelFormat(java.lang.String arg)
   {
      this.tickLabelFormat = arg;
   }

   /**
    * retrieve formatting for tick label portion of timestamps
    *
    * @return format string
    ******************************************************/
   public java.lang.String getTickLabelFormat()
   {
      return tickLabelFormat;
   }

   /**
    * set the time zone
    *
    * @param arg TimeZone object
    * @pre arg != null
    * @post this.timeZone != null
    ******************************************************/
   public void setTimeZone(java.util.TimeZone arg)
   {
      if (arg != null)
      {
         this.timeZone = (TimeZone) arg.clone();
      }
      else
      {
         this.timeZone = null;
      }
   }

   /**
    * retrieve the time zone object
    *
    * @return time zone string
    ******************************************************/
   public java.util.TimeZone getTimeZone()
   {
      return (timeZone != null)
             ? (TimeZone) timeZone.clone()
             : null;
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
         AxisTime clone = (AxisTime) super.clone();

         clone.constantTimeLabelFormat = this.getConstantTimeLabelFormat();

         if (axisRange != null)
         {
            clone.axisRange = (Date[]) this.axisRange.clone();
         }
         else
         {
            clone.axisRange = null;
         }

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
         AxisTime other = (AxisTime) o;

         rtrn = ((Util.equals(axisRange, other.axisRange))
                && (Util.equals(initialPoint, other.initialPoint))
                && (Util.equals(finalPoint, other.finalPoint))
                && (Util.equals(gridIncrement, other.gridIncrement))
                && (Util.equals(intervalCount, other.intervalCount))
                && (Util.equals(firstTickMark, other.firstTickMark))
                && (Util.equals(timeZone, other.timeZone))
                && (Util.equals(tickLabelFormat, other.tickLabelFormat))
                && (Util.equals(constantTimeLabelFormat, 
                                other.constantTimeLabelFormat))
                && (Util.equals(tickIncrement, other.tickIncrement)));
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
