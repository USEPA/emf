
package gov.epa.mims.analysisengine.gui;

import gov.epa.mims.analysisengine.tree.TextBoxArrow;
import java.awt.Container;
import java.util.ArrayList;
import java.util.EventObject;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.border.*;
import javax.swing.table.TableColumn;

/**
 * An editor for Arrows
 * @author Parthee R Partheepan
 * @version $Id: ArrowsEditor.java,v 1.2 2005/09/19 14:50:03 rhavaldar Exp $
 */

public class ArrowsEditor extends JPanel
{
   /** The list of arrows that this GUi will edit. */
   protected ArrayList arrows = null;

   /** a panel to edit arrows **/
   private MultipleEditableTablePanel tablePanel = null;

   /** Arrow column number*/
   private final int ARROW = 1;

   /** Show arrow column number*/
   private final int SHOW_ARROW= 2;


   /** Creates a new instance of ArrowEditor */
   public ArrowsEditor(ArrayList arrows)
   {
      super();
      initialize();
      this.arrows = arrows;
      setDataSource(arrows, "");
   }

   public void setDataSource(Object dataSource, String optionName)
   {
      initGUIFromModel();

   }


   private void initialize()
   {

      TextBoxArrowTableColumn arrowColumn = new TextBoxArrowTableColumn(ARROW, " Arrow ");
      arrowColumn.setPreferredWidth(100);

      BooleanTableColumn showColumn = new BooleanTableColumn(SHOW_ARROW, "Show?");
      try
      {
         showColumn.setDefaultValue(Boolean.TRUE);
      }
      catch(Exception e)
      {
         DefaultUserInteractor.get().notify(this,"Unexpected Object Type",
            e.getMessage(),UserInteractor.ERROR);
      }

      TableColumn[] tableColumns = {arrowColumn,showColumn};
      tablePanel = new MultipleEditableTablePanel(tableColumns);
      tablePanel.setRowHeight(20);
      tablePanel.setBorder(BorderFactory.createLoweredBevelBorder());

      this.setLayout(new BorderLayout());
      this.add(tablePanel,BorderLayout.CENTER);


   }//initialize()


   protected void initGUIFromModel()
   {
      int rowCount = tablePanel.getRowCount();
      for (int i=0; i< rowCount; i++)
      {
         tablePanel.removeRow(0);
      }

      // if we have set of arrows //
      if(arrows !=null && arrows.size() !=0)
      {
         for(int i=0; i< arrows.size(); i++)
         {
            TextBoxArrow arrow = (TextBoxArrow)arrows.get(i);
            tablePanel.insertRow(false);
            tablePanel.setValueAt(arrow, i,ARROW);
            Boolean enabled = Boolean.valueOf(arrow.getEnable());
            tablePanel.setValueAt(enabled, i,SHOW_ARROW);
         }//for(i)
      }//if
   }

   protected void saveGUIValuesToModel() throws Exception
   {
      int rowCount = tablePanel.getRowCount();
      arrows = new ArrayList();
      for(int i=0; i<rowCount; i++)
      {
        TextBoxArrow arrow = (TextBoxArrow)tablePanel.getValueAt(i,ARROW);
         Boolean enabled = (Boolean)tablePanel.getValueAt(i,SHOW_ARROW);
         arrow.setEnable(enabled.booleanValue());
         arrows.add(arrow);
      }//for(i)
   }//saveGUIValuesToModel

   /** setter for the arrows
    *@param arrows ArrayList of TextBoxArrows
    */
   public void setTextBoxArrows(ArrayList arrows)
   {
      this.arrows = arrows;
      initGUIFromModel();
   }//setTextBoxArrows()

   /** getter for the arrows
    *@return ArrayList arrows of TextBoxArrows
    */
   public ArrayList getTextBoxArrows()
   {
      try
      {
         saveGUIValuesToModel();
      }
      catch(Exception e)
      {
         DefaultUserInteractor.get().notify(this,"Error",
         "Exception is thrown when saving the arrows. ", UserInteractor.ERROR);
         e.printStackTrace();
      }//Exception
      return this.arrows;
   }//getTextBoxArrows()

   /**
    * @param args the command line arguments
    */
   public static void main(String[] args)
   {
      ArrayList arrows = new ArrayList();
      TextBoxArrow arrow = new TextBoxArrow();
      arrows.add(arrow);

      DefaultUserInteractor.set(new GUIUserInteractor());
      try
      {
         ArrowsEditor editor = new ArrowsEditor(arrows);
         JFrame f = new JFrame();
         f.getContentPane().add(editor);
         f.pack();
         f.setLocation(ScreenUtils.getPointToCenter(f));
         f.setVisible(true);
      }
      catch(Exception e)
      {
         e.printStackTrace();
      }
   }//main()



}
