package gov.epa.mims.analysisengine.gui;

import gov.epa.mims.analysisengine.rcommunicator.Util;
import gov.epa.mims.analysisengine.tree.LabeledDataSetIfc;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.NoSuchElementException;


/**
 * class to hold Labeled Double series data
 *
 * @author Tommy E. Cathey
 * @version $Id: LabeledDoubleSeries.java,v 1.2 2005/09/19 14:50:03 rhavaldar Exp $
 *
 **/
public class LabeledDoubleSeries extends DoubleSeries
   implements LabeledDataSetIfc, Serializable, Cloneable
{
   /** serial version UID*/
   static final long serialVersionUID = 1;

   /** list of labels */
   private ArrayList label = new ArrayList();

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
      else
      {
         LabeledDoubleSeries other = (LabeledDoubleSeries) o;
         rtrn = gov.epa.mims.analysisengine.tree.Util.equals(label, 
                                                             other.label);
      }

      return rtrn;
   }

   /**
    * Creates and returns a copy of this object
    *
    * @return a copy of this object
    ******************************************************/
   public Object clone()
   {
      LabeledDoubleSeries clone = (LabeledDoubleSeries) super.clone();

      if (clone != null)
      {
         clone.label = (ArrayList) label.clone();
      }

      return clone;
   }

   /**
    * retrieve label at index i
    *
    * @param i index into ArrayList label
    *
    * @throws java.lang.Exception if Series is not open
    * @throws java.util.NoSuchElementException if i is out of range
    * @return label at index i
    */
   public java.lang.String getLabel(int i)
                             throws java.util.NoSuchElementException, 
                                    java.lang.Exception
   {
      if (super.getNumUnmatchedOpens() <= 0)
      {
         throw new Exception("LabeledDoubleSeries DataSet is not open");
      }

      if ((i < 0) || (i >= label.size()))
      {
         throw new NoSuchElementException("" + i + " is invalid index");
      }

      return (String) label.get(i);
   }

   /**
    * add data point along with it's label
    *
    * @param val y value
    * @param label label for this value
    */
   public void addData(double val, java.lang.String label)
   {
      this.label.add(label);
      super.addData(val);
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

