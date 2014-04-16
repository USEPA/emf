package gov.epa.mims.analysisengine.tree;

import java.io.Serializable;

import java.util.ArrayList;


/**
 *
 * @author Tommy E. Cathey
 * @version $Id: AxisNumeric.java,v 1.2 2005/09/19 14:50:10 rhavaldar Exp $
 *
 **/
public class AxisNumeric
   extends AxisContinuous
   implements Serializable,
              Cloneable
{
   /** serial version UID */
   static final long serialVersionUID = 1;

   /** a List of reference Lines to draw */
   private ArrayList referenceLines = null;

   /** axis range [min,max] */
   private Double[] axisRange = null;

   /** enable/disable log scale */
   private boolean logScale = false;

   /** final pt to allow grid to be drawn */
   private double finalPoint = Double.NaN;

   /** increment for grid spacing NOTE: intervalCount and
    *  increment are mutually exclusive
    */
   private double increment = Double.NaN;

   /** initial pt to start grid */
   private double initialPoint = Double.NaN;

   /** number of intervals in grid NOTE: intervalCount and
    *  increment are mutually exclusive
    */
   private int intervalCount = 0;

   /**
    * set the axis range
    *
    * @param min min X value
    * @param max max X value
    ********************************************************/
   public void setAxisRange(Object min, Object max)
   {
      this.axisRange = (((Double) min).isNaN() || ((Double) max).isNaN())
                       ? null
                       : (new Double[] 
      {
         (Double) min, 
         (Double) max
      });
   }

   /**
    * retrieve the axis range
    *
    * @return Double[] = [min,max]
    ********************************************************/
   public Object[] getAxisRange()
   {
      return this.axisRange;
   }

   /**
    * retrieve the last point of grid
    *
    * @return last point of grid
    ********************************************************/
   public double getFinalPoint()
   {
      return finalPoint;
   }

   /**
    * Creates a new default AxisGrid object. The grid lines line up with
    * the tick marks.
    * <p><A HREF="doc-files/ExampleAxisGrid01.html"><B>Example</B></A>
    ********************************************************/
   public void setGrid()
   {
      this.initialPoint = Double.NaN;
      this.increment = Double.NaN;
      this.finalPoint = Double.NaN;
   }

   /**
    * Creates a new AxisGrid object. The grid lines start at
    * "initialPoint". Additional lines are drawn at increments of
    * "increment".
    * <p><A HREF="doc-files/ExampleAxisGrid02.html"><B>Example</B></A>
    *
    * @param initialPoint initial pt to start grid
    * @param increment grid spacing
    ********************************************************/
   public void setGrid(double initialPoint, double increment)
   {
      String cName = getClass().getName();

      if (Double.isNaN(initialPoint))
      {
         throw new IllegalArgumentException(cName + " initialPoint==NaN");
      }

      if (Double.isNaN(increment))
      {
         throw new IllegalArgumentException(cName + " increment==NaN");
      }

      this.initialPoint = initialPoint;
      this.increment = increment;
      this.finalPoint = Double.NaN;
   }

   /**
    * Creates a new AxisGrid object. The grid lines start at
    * "initialPoint". Additional lines are drawn at increments of
    * "increment". Grid lines are not drawn past the "finalPoint".
    * <p><A HREF="doc-files/ExampleAxisGrid03.html"><B>Example</B></A>
    *
    * @param initialPoint initial pt to start grid
    * @param increment grid spacing
    * @param finalPoint last pt to allow grid to be drawn
    ********************************************************/
   public void setGrid(double initialPoint, double increment, 
      double finalPoint)
   {
      String cName = getClass().getName();

      if (Double.isNaN(initialPoint))
      {
         throw new IllegalArgumentException(cName + " initialPoint==NaN");
      }

      if (Double.isNaN(increment))
      {
         throw new IllegalArgumentException(cName + " increment==NaN");
      }

      if (Double.isNaN(finalPoint))
      {
         throw new IllegalArgumentException(cName + " finalPoint==NaN");
      }

      this.initialPoint = initialPoint;
      this.increment = increment;
      this.finalPoint = finalPoint;
   }

   /**
    * set the grid options
    *
    * @param initialPoint initial grid point
    * @param intervalCount interval count
    * @param finalPoint final grid point
    ******************************************************/
   public void setGrid(double initialPoint, int intervalCount, 
      double finalPoint)
   {
      String cName = getClass().getName();

      if (Double.isNaN(initialPoint))
      {
         throw new IllegalArgumentException(cName + " initialPoint==NaN");
      }

      if (intervalCount <= 0)
      {
         throw new IllegalArgumentException(cName + " intervalCount= "
                                            + intervalCount);
      }

      if (Double.isNaN(finalPoint))
      {
         throw new IllegalArgumentException(cName + " finalPoint==NaN");
      }

      this.initialPoint = initialPoint;
      this.intervalCount = intervalCount;
      this.finalPoint = finalPoint;
   }

   /**
    * retrieve increment for grid spacing NOTE: intervalCount and
    *  increment are mutually exclusive
    *
    * @return increment for grid spacing
    ********************************************************/
   public double getIncrement()
   {
      return increment;
   }

   /**
    * retrieve the initial point to start drawing the grid
    *
    * @return initial point to start drawing the grid
    ********************************************************/
   public double getInitialPoint()
   {
      return initialPoint;
   }

   /**
    * number of intervals in grid NOTE: intervalCount and
    *  increment are mutually exclusive
    *
    * @return intervals in grid
    ********************************************************/
   public int getIntervalCount()
   {
      return intervalCount;
   }

   /**
   * set the log scale flag
   *
   * <p><A HREF="doc-files/ExampleAxisLog01.html"><B>Example</B></A>
   * <p><A HREF="doc-files/ExampleAxisLog02.html"><B>Example</B></A>
   * @param flg true->log scale; false->linear scale
   ******************************************************/
   public void setLogScale(boolean flg)
   {
      this.logScale = flg;
   }

   /**
   * get the log scale flag
   *
   * @return  true->log scale; false->linear scale
   ******************************************************/
   public boolean getLogScale()
   {
      return this.logScale;
   }

   /**
    * get the number of Reference Lines
    *
    * @return number of Reference Lines
    ********************************************************/
   public int getNumReferenceLines()
   {
      int num = 0;

      if (referenceLines != null)
      {
         num = referenceLines.size();
      }

      return num;
   }

   /**
    * retrieve the i-th ReferenceLine
    *
    * @param i index of a ReferenceLine
    *
    * @return the i-th ReferenceLine
    * @pre ((i < 0) || (i >= referenceLines.size()))
    ********************************************************/
   public ReferenceLine getReferenceLine(int i)
   {
      if ((i < 0) || (i >= referenceLines.size()))
      {
         throw new IllegalArgumentException(getClass().getName() + "i= " + i
                                            + " is out of range");
      }

      return (ReferenceLine) referenceLines.get(i);
   }

   /**
    * add a ReferenceLine
    *
    * @param refLine ReferenceLine to
    * @pre refLine != null
    ********************************************************/
   public void setReferenceLineAdd(ReferenceLine refLine)
   {
      if (refLine == null)
      {
         throw new IllegalArgumentException(getClass().getName()
                                            + " refLine == null");
      }

      if (referenceLines == null)
      {
         referenceLines = new ArrayList();
      }

      referenceLines.add(refLine);
   }

   /**
    * add a set of ReferenceLines
    * null refLines clears the list.
    *
    * @param refLines ArrayList of ReferencLines to add.
    ********************************************************/
   public void setReferenceLines(ArrayList refLines)
   {
      if (referenceLines == null)
      {
         referenceLines = new ArrayList();
      }
      else
      {
         referenceLines.clear();
      }

      if (refLines != null)
      {
         for (int i = 0; i < refLines.size(); i++)
         {
            referenceLines.add(refLines.get(i));
         }
      }
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
         AxisNumeric clone = (AxisNumeric) super.clone();

         clone.referenceLines = (clone.referenceLines == null)
                                ? null
                                : (ArrayList) referenceLines.clone();

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
         AxisNumeric other = (AxisNumeric) o;

         rtrn = ((Util.equals(axisRange, other.axisRange))
                && ((referenceLines == null)
                    ? (other.referenceLines == null)
                    : (Util.equals(referenceLines, other.referenceLines)))
                && (logScale == other.logScale)
                && Util.equals(initialPoint, other.initialPoint)
                && Util.equals(finalPoint, other.finalPoint)
                && Util.equals(increment, other.increment)
                && Util.equals(intervalCount, other.intervalCount));
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