/*

 * SpecialTableColumn.java

 *

 * Created on December 2, 2003, 2:46 PM

 */



package gov.epa.mims.analysisengine.gui;



import java.awt.datatransfer.*;

import java.awt.Toolkit;

import javax.swing.JTable;

import javax.swing.table.*;

import gov.epa.mims.analysisengine.gui.DefaultUserInteractor;

import gov.epa.mims.analysisengine.gui.UserInteractor;







public abstract class SpecialTableColumn extends TableColumn

{

   /** Set the data flavor*/

   protected DataFlavor dataFlavor;

   

   /** Set the class type */

   protected Class type;

   

   /** save the elements from the copy operation*/

   protected Transferable contents;

   

   /** tracks the calls to get default choices*/

   protected int count = 0;

   

   /** indicates whether default value will be used in insertRow in a table */

   protected boolean insertDefaultValue = false;

   

   /** column header tool tip*/

   protected String columnHeaderToolTip = null;

   

   /** Creates a new instance of SpecialTableColumn */

   public SpecialTableColumn(int modelIndex, String name)

   {

      super(modelIndex);

      setupCellEditor();

      this.setHeaderValue(name);

      

   }

   

   /**

    * saves the obj in the contents variable

    * @param obj Object actual type will be of subclass type

    */

   protected abstract void copySelectedCell(Object obj);

   

   /** set up the custom cell editor for the column*/

   protected abstract void setupCellEditor();

   

   /**

    * get default value for the column

    * @return Object actual type of the default value determined by subclass

    */

   public abstract Object getDefaultValue();

   

   /** set default value for the column

    * @param obj Object actual type will be of subclass type

    */

   public abstract void setDefaultValue(Object obj) throws Exception;

   

   public abstract Object nextChoice();

   

   

   /**

    * Paste what is on the contents to a particular cell defined by row and column

    * @table JTable on which paste operation is to be performed

    * @row int row no on the table

    * @column int column no on the table

    *

    */

   protected void pasteSelectedCells(JTable table, int row, int column)

   {

      // if contents is null paste nothing

      if(contents ==null)

      {

         return;

      }

      

      if (contents.isDataFlavorSupported(dataFlavor))

      {

         try

         {

            Object o = contents.getTransferData(dataFlavor);

            table.setValueAt(o, row, column);

         }

         catch (Exception e)

         {

            DefaultUserInteractor.get().notify(table,"Pasting Error",

            "The item that you are trying to paste cannot be pasted into this table.",

            UserInteractor.ERROR);

         }

      }

   }

   

   

   /** Getter column type.

    * @return Class column type

    */

   public Class getType()

   {

      return type;

   }

   

   /** Getter for property insertDefaultValue.

    * @return Value of property insertDefaultValue.

    *

    */

   public boolean isInsertDefaultValue()

   {

      return insertDefaultValue;

   }

   

   /** Setter for property insertDefaultValue.

    * @param insertDefaultValue New value of property insertDefaultValue.

    *

    */

   public void setInsertDefaultValue(boolean insertDefaultValue)

   {

      this.insertDefaultValue = insertDefaultValue;

   }

   

   /**

    * set the column header tool tip

    * @param tip tooltip string

    */

   public void setColumnHeaderTooltip(String tip)

   {

      columnHeaderToolTip = tip;

   }

   

   /**

    * getter for column header tool tip

    */

   public String getColumnHeaderTooltip()

   {

      return columnHeaderToolTip;

   }

}



