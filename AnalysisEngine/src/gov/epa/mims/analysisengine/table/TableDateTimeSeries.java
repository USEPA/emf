
package gov.epa.mims.analysisengine.table;

import gov.epa.mims.analysisengine.tree.DateDataSetIfc;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.NoSuchElementException;

/**
 * <p>Description: It provides a wrapper around </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author Daniel Gatti
 * @version $Id: TableDateTimeSeries.java,v 1.3 2006/10/30 21:43:50 parthee Exp $
 */
public class TableDateTimeSeries extends TableDataSeries implements DateDataSetIfc
{
   private String pattern;
   
   private SimpleDateFormat dateFormat;
   
  /**
   *
   * @param model that is currently being displayed in the table the topmost level
   * tablemodel
   * @pre model != null
   * @pre valueCol < plottingInfo.getValueColumns().length
   */
  public TableDateTimeSeries(OverallTableModel model, int valueCol,
   int [] dateColumns, String pattern)
  {
    super(model,valueCol,dateColumns,' ');
    this.pattern = pattern;
    dateFormat = new SimpleDateFormat(pattern);
  }//TableDataSeries()
  
  
    /**
    * retrieve the i-th timestamp
    *
    * @param indx index into ArrayList date
    * @return date for the data point at location indx
    * @throws java.util.NoSuchElementException if i is out of range
    * @throws java.lang.Exception if data series is not open
    ******************************************************/
   public Date getDate(int index) throws NoSuchElementException, Exception
   {
      if ((index > (tableModel.getRowCount() - 1)) || (index < 0))
      {
         throw new NoSuchElementException();
      }
      String dateString = "";
      
      if(labelColumns.length ==1)
      {
         dateString = tableModel.getFormattedValueAt(index,labelColumns[0]+1);
      }
      else if(labelColumns.length ==2)
      {
         String string1 = tableModel.getFormattedValueAt(index,labelColumns[0]+1);
         String string2 = tableModel.getFormattedValueAt(index,labelColumns[1]+1);
         dateString = string2 + ' '+ string1  ;
      }
      Date date = null;
      try
      {
         date = dateFormat.parse(dateString);
      }
      catch(ParseException p)
      {
         throw new Exception("The pattern \""+pattern+ "\" cannot be applied to"
            + " the date columns you selected. \n Please check the column " + 
            "selection for the date and time");
      }
      return date;
//System.out.println("dateColumn="+(dateColumn+1));      
//System.out.println("Class Type="+tableModel.getValueAt(index,dateColumn+1).getClass());
//System.out.println("value " + tableModel.getValueAt(index,dateColumn+1).toString());
   }//getDate()
   
     /**
   * get data label
   *
   * @param i index into data series
   * @return element label
   * @throws java.lang.Exception if series is not open
   * @throws java.util.NoSuchElementException if i is out of range
   **************************************************/
  public String getLabel(int i) throws Exception, java.util.NoSuchElementException
  {
    String label = "";
    if(labelColumns.length == 1)
    {
       //adding one to skip the first col in the overall table model: it's
       // the row header
       label = tableModel.getFormattedValueAt(i,labelColumns[0]+1);
       return label;
    }
    else if(labelColumns.length ==2)
    {
       label = tableModel.getFormattedValueAt(i,labelColumns[0]+1);
       label = label + separator+
         tableModel.getFormattedValueAt(i, labelColumns[1]+1);
       return label;
    }
    else 
    {
      return "unspecified label" +i;
    }
  }//getLabel(int)
}
