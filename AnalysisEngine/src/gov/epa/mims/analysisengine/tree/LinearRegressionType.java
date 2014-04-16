package gov.epa.mims.analysisengine.tree;

import java.io.Serializable;
import java.util.ArrayList;


/**
 * @author    Tommy E. Cathey
 * @created   October 7, 2004
 * @version   $Id: LinearRegressionType.java,v 1.3 2006/10/30 21:43:50 parthee Exp $
 */

public class LinearRegressionType
    extends AnalysisOption
    implements Serializable,
   Cloneable
{
   /** serial version UID */
   final static long serialVersionUID = 1;

   /** Description of the Field */
   private ArrayList linearRegressions = new ArrayList();

   /** Description of the Field */
   private boolean enable = true;


   /**
    * Sets the enable attribute of the LinearRegressionType object
    *
    * @param enable  The new enable value
    */
   public void setEnable(boolean enable)
   {
      this.enable = enable;
   }


   /**
    * Gets the enable attribute of the LinearRegressionType object
    *
    * @return   The enable value
    */
   public boolean getEnable()
   {
      return enable;
   }


   /**
    * Adds a feature to the LinearRegression attribute of the
    * LinearRegressionType object
    *
    * @param linearRegression  The feature to be added to the LinearRegression
    *      attribute
    */
   public void add(LinearRegression linearRegression)
   {
      linearRegressions.add(linearRegression);
   }


   /**
    * Gets the linearRegression attribute of the LinearRegressionType object
    *
    * @param i  Description of the Parameter
    * @return   The linearRegression value
    */
   public LinearRegression getLinearRegression(int i)
   {
      return (LinearRegression) linearRegressions.get(i);
   }


   /**
    * Description of the Method
    *
    * @param i  Description of the Parameter
    */
   public void removeLinearRegression(int i)
   {
      linearRegressions.remove(i);
   }


   /** Description of the Method */
   public void clearLinearRegression()
   {
      linearRegressions.clear();
   }


   /**
    * Gets the linearRegressionSize attribute of the LinearRegressionType object
    *
    * @return   The linearRegressionSize value
    */
   public int getLinearRegressionSize()
   {
      return linearRegressions.size();
   }


   /**
    * Creates and returns a copy of this object
    *
    * @return                             a copy of this object
    * @throws CloneNotSupportedException  is not cloneable
    */
   public Object clone()
      throws CloneNotSupportedException
   {
      LinearRegressionType clone = (LinearRegressionType) super.clone();

      if (clone != null)
      {
         clone.linearRegressions = (ArrayList) linearRegressions.clone();
      }
      else
      {
         throw new CloneNotSupportedException();
      }

      return clone;
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
         LinearRegressionType other = (LinearRegressionType) o;

         rtrn = Util.equals(linearRegressions, other.linearRegressions);
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

