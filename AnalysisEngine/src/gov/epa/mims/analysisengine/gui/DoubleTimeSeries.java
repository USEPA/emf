package gov.epa.mims.analysisengine.gui;

import gov.epa.mims.analysisengine.rcommunicator.RCommandGenerator;
import gov.epa.mims.analysisengine.rcommunicator.RCommunicator;
import gov.epa.mims.analysisengine.rcommunicator.RGenerator;
import gov.epa.mims.analysisengine.rcommunicator.Util;
import gov.epa.mims.analysisengine.tree.DateDataSetIfc;
import gov.epa.mims.analysisengine.tree.LabeledDataSetIfc;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.NoSuchElementException;


/**
 * Copied from the gov.epa.mims.analysisengine.rcommunicator.test package.
 * I tried to implement the LabeledDataSetIfc, but the time series plot
 * blows up when I try this. We wanted to try and implement LabeledDataSetIfc
 * to be able to use these series in bar charts. DMG
 *
 * @version $Revision: 1.2 $
 * @author Tommy E. Cathey
 */
public class DoubleTimeSeries
      extends DoubleSeries
      implements DateDataSetIfc/*, LabeledDataSetIfc*/, Serializable, Cloneable
{
   /** serial version UID */
   static final long serialVersionUID = 1;

   /** list of timestamps */
   private ArrayList date = new ArrayList();

   /** The formatter to use for dates. */
   SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");

   /**
    * DOCUMENT_ME
    *
    * @param o DOCUMENT_ME
    *
    * @return DOCUMENT_ME
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
         DoubleTimeSeries other = (DoubleTimeSeries) o;
         rtrn = gov.epa.mims.analysisengine.tree.Util.equals(date, other.date);
      }

      return rtrn;
   }

   /**
    * cloning method
    *
    * @return clone of this object
    ******************************************************/
   public Object clone()
   {
      DoubleTimeSeries clone = (DoubleTimeSeries) super.clone();

      if (clone != null)
      {
         clone.date = (ArrayList) date.clone();
      }

      return clone;
   }

   /**
    * retrieve the i-th timestamp
    *
    * @param indx index into ArrayList date
    * @return date for the data point at location indx
    * @throws java.util.NoSuchElementException if i is out of range
    * @throws java.lang.Exception if data series is not open
    ******************************************************/
   public Date getDate(int indx) throws java.util.NoSuchElementException,
                                        java.lang.Exception
   {
      if (super.getNumUnmatchedOpens() <= 0)
      {
         throw new Exception("DoubleSeries DataSet is not open");
      }

      if ((indx > (date.size() - 1)) || (indx < 0))
      {
         throw new NoSuchElementException();
      }

      return (Date) date.get(indx);
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
      // Error handling is in getDate().
      return dateFormat.format(getDate(i));
   }

   /**
    * add date to the list of timestamps
    *
    * @param date timestamp to add to data list
    * @param val the Y value
    * @pre date != null
    ******************************************************/
   public void addTimeStamp(java.util.Date date, double val)
   {
      this.date.add(date);
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

