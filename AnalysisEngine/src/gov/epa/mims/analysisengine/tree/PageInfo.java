package gov.epa.mims.analysisengine.tree;

import java.io.Serializable;


/**
 * class for maintaining Page Options and Defaults
 *
 * @version $Revision: 1.2 $
 * @author Tommy E. Cathey
 ********************************************************/
public class PageInfo
   extends AvailableOptionsAndDefaults
   implements Serializable
{
   /** serial version UID */
   static final long serialVersionUID = 1;

   /**
    * Creates a new PageInfo object.
    *
    * @param allKeywords allowed AnlysisOption keywords for this plot type
    ********************************************************/
   public PageInfo(String[] allKeywords)
   {
      super(allKeywords);
   }

   /**
    * clone this object
    *
    * @return clone of this object
    ******************************************************/
   public Object clone()
   {
      return super.clone();
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
      return super.equals(o);
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