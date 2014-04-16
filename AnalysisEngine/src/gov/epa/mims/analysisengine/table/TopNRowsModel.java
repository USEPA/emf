
package gov.epa.mims.analysisengine.table;

/**
 * <p>Description: Get an integer from the user to use for showing the
 *  top/bottom N rows in the table. </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author Parthee Partheepan - UNC - CEP
 * @version $Id: TopNRowsModel.java,v 1.2 2005/09/19 14:50:03 rhavaldar Exp $
 */

public class TopNRowsModel
{
   /** a model for the currently viewed table */
   private OverallTableModel model;
   
   /** indicate wheter this model shows the topN( if true) or bottom N rows */
   private boolean topNRows = true;
   
   /** indicate whether number of rows button is selected or percentage button is 
    * selected
    */
   private boolean rows = true;
   
   /** indicate the topN/bottomN rows number */
   private static int noOfRows = 0;
   
   /** indicate the topN/bottomN rows number in percentage */
   private static int percentage = 100;
   
   /** to save the selected column name  */
   private String selectedColName;
   
   /** Creates a new instance of TopNRowsModel */
   public TopNRowsModel(boolean topNRows, OverallTableModel model)
   {
      this.topNRows = topNRows;
      this.model = model;
   }
   
   /** Getter for property rows.
    * @return Value of property rows.
    *
    */
   public boolean isRows()
   {
      return rows;
   }
   
   /** Getter for property noOfRows.
    * @return Value of property noOfRows.
    *
    */
   public int getNoOfRows()
   {
      return noOfRows;
   }
   
   /** Getter for the model for the currently viewed table
    * @return OverallTableModel model
    */
   public OverallTableModel getOverallTableModel()
   {
      return model;
   }
   
   /** Getter for property topNRows.
    * @return Value of property topNRows.
    *
    */
   public boolean isTopNRows()
   {
      return topNRows;
   }   

   /** Getter for property percentage.
    * @return Value of property percentage.
    *
    */
   public int getPercentage()
   {
      return percentage;
   }
   
   /** Getter for property selectedColName.
    * @return Value of property selectedColName.
    *
    */
   public String getSelectedColName()
   {
      return selectedColName;
   }   

      
   /** Setter for property noOfRows.
    * @param noOfRows New value of property noOfRows.
    *
    */
   public void setNoOfRows(int noOfRows)
   {
      this.noOfRows = noOfRows;
   }
   
   /** Setter for the model for the currently viewed table
    * @param model OverallTableModel 
    */
   public void setOverallTableModel(OverallTableModel model)
   {
      this.model = model;
   }
   
   /** Setter for property percentage.
    * @param percentage New value of property percentage.
    *
    */
   public void setPercentage(int percentage)
   {
      this.percentage = percentage;
   }
   
   /** Setter for property rows.
    * @param rows New value of property rows.
    *
    */
   public void setRows(boolean rows)
   {
      this.rows = rows;
   }

   /** Setter for property selectedColName.
    * @param selectedColName New value of property selectedColName.
    *
    */
   public void setSelectedColName(String selectedColName)
   {
      this.selectedColName = selectedColName;
   }   

   
}

