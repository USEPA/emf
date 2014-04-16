
package gov.epa.mims.analysisengine.gui;

import gov.epa.mims.analysisengine.tree.LinearRegressionType;
import gov.epa.mims.analysisengine.tree.LinearRegression;
import gov.epa.mims.analysisengine.tree.TextBorder;
import java.awt.Container;
import java.awt.BorderLayout;

import javax.swing.*;
//import javax.swing.border.*;
import javax.swing.table.TableColumn;

/**
 * Editor for LinearRegressionTypeEditor
 * @see LinearRegressionTypeEditor.java
 * @see LinearRegression.java
 * @see LinearRegressionStatistics.java
 * @author Parthee Partheepan UNC
 * @version $Id: LinearRegressionTypeEditor.java,v 1.2 2005/09/19 14:50:03 rhavaldar Exp $
 */
public class LinearRegressionTypeEditor extends OptionDialog
{

   /** a linear regression type  that represent container for the linear regression */
   private LinearRegressionType linearRegressionType;

   /** check box for enable the linear regression */
   private BooleanValuePanel enableLRPanel;

   /** a panel to edit different linear regression line settings **/
   private MultipleEditableTablePanel tablePanel = null;

   /** Indicates the linear regression columnn number */
   private static final int LINEAR_REGRESSION_COLUMN = 1;

   /** Indicates the Boolean columnn number which use to determiner whether
    to apply the linear regression or not
    */
   private static final int APPLY_COLUMN = 2;

   private static int lrCounter = 0;

   /** store the tab clicked last time */
   //public static int rememberTabNo = LinearRegressionEditor.L_L_TAB;

   /** Creates a new instance of LinerRegressionTypeEditor */
   public LinearRegressionTypeEditor(LinearRegressionType aLinearRegressionType)
   {
      super();
      setTitle("LR Type Editor");
      initialize();
      setDataSource(aLinearRegressionType, "");
   }//LinerRegressionTypeEditor()


   /**
   * constructor need for class.newInstance
   */
   public LinearRegressionTypeEditor()
   {
      this(null);
   }//LinerRegressionTypeEditor()

    /**
    * Set the data source for the editor: tree.LinearRegressionType
    * @param dataSource source of the data of type tree.LinearRegressionType
    * @param optionName String title for the dialog
    */
   public void setDataSource(Object dataSource, String optionName)
   {
      this.linearRegressionType = (LinearRegressionType)dataSource;

      super.setDataSource(dataSource, optionName);
      if (linearRegressionType != null)
      {
         initGUIFromModel();
      }
      pack();
      setLocation(ScreenUtils.getPointToCenter(this));
      this.repaint();
   }//setDataSource()

   private void initialize()
   {
      this.setModal(true);

      enableLRPanel = new BooleanValuePanel("Enable Linear Regression",false);
      JPanel enablePanel = new JPanel();
      enablePanel.add(enableLRPanel);
      LinearRegressionTableColumn lrColumn = new LinearRegressionTableColumn(
         LINEAR_REGRESSION_COLUMN," Linear Regression ");
      lrColumn.setPreferredWidth(200);

      BooleanTableColumn booleanColumn = new BooleanTableColumn(APPLY_COLUMN
         , "Apply?");
      try
      {
         booleanColumn.setDefaultValue(Boolean.TRUE);
      }
      catch(Exception e)
      {
         e.printStackTrace();
      }
      booleanColumn.setPreferredWidth(50);
       //creating a table consist of above columns
      TableColumn [] tableColumns = {lrColumn, booleanColumn};
      tablePanel = new MultipleEditableTablePanel(tableColumns)
      {
//       protected  void insertRow(boolean above)
         public  void insertRow(boolean above)
         {
            super.insertRow(above);
            lrCounter ++;
         }
         public void removeRow(int rowNo)
         {
            super.removeRow(rowNo);
            lrCounter --;
         }

      };
      //tablePanel.setPreferredSize(new Dimension(350, 250));
      tablePanel.setRowHeight(20);
      tablePanel.setBorder(BorderFactory.createLoweredBevelBorder());
      JPanel mainPanel = new JPanel(new BorderLayout());
      mainPanel.add(enablePanel,BorderLayout.NORTH);
      mainPanel.add(tablePanel,BorderLayout.CENTER);
      Container container = getContentPane();
      container.setLayout(new BorderLayout());
      container.add(mainPanel);
      container.add(getButtonPanel(),BorderLayout.SOUTH);
   }

   /** initialize the values from the model */
   protected void initGUIFromModel()
   {
      enableLRPanel.setValue(linearRegressionType.getEnable());
      int rowCount = tablePanel.getRowCount();
      for (int i=0; i< rowCount; i++)
      {
         tablePanel.removeRow(0);
      }

      if(linearRegressionType != null)
      {
         int size =linearRegressionType.getLinearRegressionSize();
         for(int i=0; i< size; i++)
         {
             LinearRegression aLR = linearRegressionType.getLinearRegression(i);
             tablePanel.insertRow(false);
             tablePanel.setValueAt(aLR,i,LINEAR_REGRESSION_COLUMN);
             Boolean apply = new Boolean(aLR.getEnable());
             tablePanel.setValueAt(apply,i, APPLY_COLUMN);
         }
      }//if(linearRegressionType != null)
   }//initGUIFromModel()

   /** save the gui to the model */
   protected void saveGUIValuesToModel() throws Exception
   {
        linearRegressionType.setEnable(enableLRPanel.getValue());
        linearRegressionType.clearLinearRegression();
        int rowCount = tablePanel.getRowCount();
        for(int i=0; i< rowCount; i++)
        {
            LinearRegression aLR = (LinearRegression)tablePanel.getValueAt(
               i,LINEAR_REGRESSION_COLUMN);
            Boolean value = (Boolean)tablePanel.getValueAt(i, APPLY_COLUMN);
            aLR.setEnable(value.booleanValue());
            linearRegressionType.add(aLR);
            //check
            TextBorder txtBorder = (TextBorder)aLR.getLabel();
            if(txtBorder.getRegion() == null)
            {
               txtBorder.setPosition(TextBorder.REFERENCE_LINE,TextBorder.CENTER,0.5,0.5);
            }
            if(txtBorder.getTextString() != null)
            {
               txtBorder.setTextString(null);
            }
        }//for(i)
   }//saveGUIValuesToModel()

   public static String getRegressionString()
   {
      return "Linear Regression";
   }
   /**
    * @param args the command line arguments
    */
   public static void main(String[] args)
   {
      DefaultUserInteractor.set(new GUIUserInteractor());
      try
      {
         LinearRegressionType aLRType = new LinearRegressionType();
         LinearRegressionTypeEditor editor = new LinearRegressionTypeEditor(aLRType);
         editor.setVisible(true);
      }
      catch(Exception e)
      {
         e.printStackTrace();
      }
   }

}
