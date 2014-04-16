package gov.epa.mims.analysisengine.tree;

import java.io.Serializable;


/**
 *
 * @author Tommy E. Cathey
 * @version $Id: AxisCategory.java,v 1.2 2005/09/19 14:50:10 rhavaldar Exp $
 *
 **/
public class AxisCategory
   extends Axis
   implements Serializable,
              Cloneable
{
   /** serial version UID */
   static final long serialVersionUID = 1;

   /** axis range [min,max] */
   private Double[] axisRange = null;

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
    * Creates and returns a copy of this object
    *
    * @return a copy of this object
    ******************************************************/
   public Object clone()
   {
      try
      {
         AxisCategory clone = (AxisCategory) super.clone();

         clone.axisRange = (clone.axisRange == null)
                           ? null
                           : (Double[]) axisRange.clone();

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
         AxisCategory other = (AxisCategory) o;

         rtrn = (Util.equals(axisRange, other.axisRange));
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