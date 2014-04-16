package gov.epa.mims.analysisengine.tree;

import java.io.Serializable;


/**
 * controls the sorting of data for the Ranked Order Plot
 * <br>It is used as an argument in
 * {@link AnalysisOptions#addOption(java.lang.String key,
 *  java.lang.Object obj) }
 * <p>Elided Code Example:
 * <pre>
 *
 *       :
 *       :
 *    String aSORTTYPE = SORT_TYPE;
 *    AnalysisOptions options = new AnalysisOptions();
 *    options.addOption(aSORTTYPE, initSortType());
 *       :
 *       :
 * private SortType initSortType()
 * {
 *    SortType sortType = new SortType();
 *
 *    sortType.setMissingData(SortType.ENDING);
 *    sortType.setSortMethod(SortType.SHELL);
 *    sortType.setAscending(true);
 *    sortType.setEnable(true);
 *
 *    return sortType;
 * }
 *
 * </pre>
 *
 *
 * @author Tommy E. Cathey
 * @version $Id: SortType.java,v 1.2 2005/09/19 14:50:10 rhavaldar Exp $
 *
 **/
public class SortType
   extends AnalysisOption
   implements Serializable,
              Cloneable,
              AnalysisOptionConstantsIfc
{
   /******************************************************
    *
    * fields
    *
    *****************************************************/
   static final long serialVersionUID = 1;

   /**
    * where to place missing data when sorting
    * <p>Use as argument in 
    * {@link SortType#setMissingData(java.lang.String arg)}
    */
   public static final String BEGINNING = "BEGINNING";

   /**
    * where to place missing data when sorting
    * <p>Use as argument in 
    * {@link SortType#setMissingData(java.lang.String arg)}
    */
   public static final String ENDING = "ENDING";

   /**
    * where to place missing data when sorting
    * <p>Use as argument in 
    * {@link SortType#setMissingData(java.lang.String arg)}
    */
   public static final String REMOVE = "REMOVE";

   /**
    * type of sort algorithm to use when sorting
    * <p>Use as argument in 
    * {@link SortType#setSortMethod(java.lang.String arg)}
    */
   public static final String QUICK = "QUICK";

   /**
    * type of sort algorithm to use when sorting
    * <p>Use as argument in 
    * {@link SortType#setSortMethod(java.lang.String arg)}
    */
   public static final String SHELL = "SHELL";

   /** 
    * handling of missing data
    * <p>set in {@link SortType#setMissingData(String)}
    */
   private String missingData = null;

   /** 
    * R sort method to use
    * <p>set in {@link SortType#setSortMethod(String)}
    */
   private String sortMethod = null;

   /** 
    * sort order
    * <p>set in {@link SortType#setAscending(boolean)}
    */
   private boolean ascending = true;

   /** 
    * enable sorting
    * <p>set in {@link SortType#setEnable(boolean)}
    */
   private boolean enable = true;

   /**
    * how to sort data (ascending or descending)
    * <p>
    * <ul>
    * <li>true -> ascending
    * <p>if {@link SortType#setSortMethod(String)} is set to
    * {@link SortType#QUICK} then data must be sorted in an
    * ascending order
    * <p>
    * <li>false -> descending
    * </ul>
    * <br><A HREF="doc-files/ExampleRankOrder02.html"><B>View Example</B></A>
    * <br><A HREF="doc-files/ExampleRankOrder03.html"><B>View Example</B></A>
    * <br><A HREF="doc-files/ExampleRankOrder04.html"><B>View Example</B></A>
    * <br><A HREF="doc-files/ExampleRankOrder05.html"><B>View Example</B></A>
    * <br><A HREF="doc-files/ExampleRankOrder06.html"><B>View Example</B></A>
    * <br><A HREF="doc-files/ExampleRankOrder07.html"><B>View Example</B></A>
    * <br><A HREF="doc-files/ExampleRankOrder08.html"><B>View Example</B></A>
    *
    * @param arg true=ascending
    ********************************************************/
   public void setAscending(boolean arg)
   {
      this.ascending = arg;
   }

   /**
    * retrieve ascending sort flag
    *
    * @return ascending sort flag
    ********************************************************/
   public boolean getAscending()
   {
      return ascending;
   }

   /**
    * enable/disable sorting of data
    *
    * <br><A HREF="doc-files/ExampleRankOrder02.html"><B>View Example</B></A>
    * <br><A HREF="doc-files/ExampleRankOrder03.html"><B>View Example</B></A>
    * <br><A HREF="doc-files/ExampleRankOrder04.html"><B>View Example</B></A>
    * <br><A HREF="doc-files/ExampleRankOrder05.html"><B>View Example</B></A>
    * <br><A HREF="doc-files/ExampleRankOrder06.html"><B>View Example</B></A>
    * <br><A HREF="doc-files/ExampleRankOrder07.html"><B>View Example</B></A>
    * <br><A HREF="doc-files/ExampleRankOrder08.html"><B>View Example</B></A>
    * @param arg true=enable sorting
    ********************************************************/
   public void setEnable(boolean arg)
   {
      this.enable = arg;
   }

   /**
    * retrieve enable/disable flag
    *
    * @return enable/disable flag
    ********************************************************/
   public boolean getEnable()
   {
      return this.enable;
   }

   /**
    * set the handling of missing data when sorting
    * <p>where to place missing data values:
    * <ul>
    * <li> {@link SortType#BEGINNING}
    * <li> {@link SortType#ENDING}
    * <li> {@link SortType#REMOVE}
    * </ul>
    * <br><A HREF="doc-files/ExampleRankOrder02.html"><B>View Example</B></A>
    * <br><A HREF="doc-files/ExampleRankOrder03.html"><B>View Example</B></A>
    * <br><A HREF="doc-files/ExampleRankOrder04.html"><B>View Example</B></A>
    * <br><A HREF="doc-files/ExampleRankOrder05.html"><B>View Example</B></A>
    * <br><A HREF="doc-files/ExampleRankOrder06.html"><B>View Example</B></A>
    * <br><A HREF="doc-files/ExampleRankOrder07.html"><B>View Example</B></A>
    * <br><A HREF="doc-files/ExampleRankOrder08.html"><B>View Example</B></A>
    *
    * @param arg how to handle missing data
    * @pre ((arg==SortType.BEGINNING)||(arg==SortType.ENDING)
    * ||(arg==SortType.REMOVE))
    ********************************************************/
   public void setMissingData(java.lang.String arg)
   {
      this.missingData = arg;
   }

   /**
    * retrieve how to handle missing data option
    *
    * @return where to place missing data values
    ********************************************************/
   public java.lang.String getMissingData()
   {
      return missingData;
   }

   /**
    * set the sort method to use when sorting
    * <p>valid values are:
    * <ul>
    * <li> {@link SortType#QUICK}
    * <br> {@link SortType#setAscending(boolean)} must be set to 
    * true when using the {@link SortType#QUICK} method
    * <li> {@link SortType#SHELL}
    * </ul>
    * <br><A HREF="doc-files/ExampleRankOrder02.html"><B>View Example</B></A>
    * <br><A HREF="doc-files/ExampleRankOrder03.html"><B>View Example</B></A>
    * <br><A HREF="doc-files/ExampleRankOrder04.html"><B>View Example</B></A>
    * <br><A HREF="doc-files/ExampleRankOrder05.html"><B>View Example</B></A>
    * <br><A HREF="doc-files/ExampleRankOrder06.html"><B>View Example</B></A>
    * <br><A HREF="doc-files/ExampleRankOrder07.html"><B>View Example</B></A>
    * <br><A HREF="doc-files/ExampleRankOrder08.html"><B>View Example</B></A>
    *
    * @param arg sort method to use when sorting
    * @pre ((arg==SortType.QUICK)||(arg==SortType.SHELL))
    ********************************************************/
   public void setSortMethod(String arg)
   {
      this.sortMethod = arg;
   }

   /**
    * retrieve the sort method to use
    *
    * @return sort method
    ********************************************************/
   public java.lang.String getSortMethod()
   {
      return sortMethod;
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
         SortType clone = (SortType) super.clone();

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
         SortType other = (SortType) o;

         rtrn = ((missingData == null)
                 ? (other.missingData == null)
                 : (missingData.equals(other.missingData)))
                && ((sortMethod == null)
                    ? (other.sortMethod == null)
                    : (sortMethod.equals(other.sortMethod)))
                && (ascending == other.ascending) && (enable == other.enable);
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