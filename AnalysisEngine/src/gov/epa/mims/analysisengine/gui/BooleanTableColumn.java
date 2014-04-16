/*

 * BooleanTableColumn.java

 *

 * Created on December 8, 2003, 1:58 PM

 */



package gov.epa.mims.analysisengine.gui;



import javax.swing.*;

import javax.swing.table.*;

import java.awt.datatransfer.DataFlavor;

import java.awt.datatransfer.StringSelection;



public class BooleanTableColumn extends SpecialTableColumn

{

   

   /** The default value that will be place in newly added rows. */

   protected Boolean defaultValue = Boolean.FALSE;

   

   /** Creates a new instance of BooleanTableColumn */

   public BooleanTableColumn(int modelIndex, String name)

   {

      super(modelIndex, name);

      dataFlavor = BooleanSelection.booleanFlavor;

      type = Boolean.class;

   }

   

   protected void copySelectedCell(Object obj)

   {

      Boolean b = (Boolean)obj;

      contents = new BooleanSelection(b);

 

   }

   

 /**

    * getter for the default value

    * @param Object default value

    */

   public Object getDefaultValue()

   {

      return defaultValue;

   }

   

  /** 

    * return the next object in the valid choices

    */

   public Object nextChoice()

   {

      return this.defaultValue;

   }   

    /**
    * Set a default value that will be used when a new row is added.
    *
    * @author Daniel Gatti
    * @param value Object that is the default value to place in a new row.
    */
   public void setDefaultValue(Object value) throws Exception
   {
      if (defaultValue instanceof Boolean)
      {
            defaultValue = (Boolean)value;
      }
      else
      {
         throw new IllegalArgumentException("Expected an Boolean value");
      }
   }

   

   protected void setupCellEditor()
   {

   

   }

}



