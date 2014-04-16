
package gov.epa.mims.analysisengine.gui;

import gov.epa.mims.analysisengine.tree.TextBoxesType;
import gov.epa.mims.analysisengine.tree.TextBox;
import gov.epa.mims.analysisengine.tree.TextBorder;
import gov.epa.mims.analysisengine.tree.TextBoxArrow;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.TableColumn;


/**
 * Editor for TextBoxesType
 * @see TextBoxesType.java
 * @see TextBox.java
 * @see TextBoxArrow.java
 * @author Parthee Partheepan UNC
 * @version $Id: TextBoxesTypeEditor.java,v 1.2 2005/09/19 14:50:03 rhavaldar Exp $
 */

public class TextBoxesTypeEditor extends OptionDialog
{
   /** a text boxes type that represent container for the text boxes */
   private TextBoxesType textBoxesType;

   /** check box for enable the text boxes */
   private JCheckBox enableAllCheckBox;

   /** a panel to edit different text boxes **/
   private MultipleEditableTablePanel tablePanel = null;

   /** Indicates the text columnn number */
   private static final int TEXTBOX_COLUMN = 1;

   /** Indicates the Boolean columnn number which use to determiner whether
    to show the text boxes or not
    */
   private static final int SHOW_COLUMN = 2;

   /** store the tab clicked last time */
   public static int rememberTabNo = TextBoxEditor.TEXTBORDER_TAB;

   /** Creates a new instance of TextBoxesTypeEditor */
   public TextBoxesTypeEditor(TextBoxesType aTextBoxesType)
   {
      super();
      initialize();
      setDataSource(aTextBoxesType, "");
   }//TextBoxesTypeEditor()

   /**
   * constructor need for class.newInstance
   */
   public TextBoxesTypeEditor()
   {
      this(null);
   }//TextBoxesTypeEditor()

    /**
    * Set the data source for the editor: tree.TextBoxesType
    * @param dataSource source of the data of type tree.TextBoxesType
    * @param optionName String title for the dialog
    */
   public void setDataSource(Object dataSource, String optionName)
   {
      this.textBoxesType = (TextBoxesType)dataSource;

      super.setDataSource(dataSource, optionName);
      if (textBoxesType != null)
      {
         initGUIFromModel();
      }
      pack();
      setLocation(ScreenUtils.getPointToCenter(this));
      this.repaint();
   }//setDataSource()

   /** initilize the gui components */
   private void initialize()
   {
      this.setModal(true);

//      JPanel enableAllPanel = new JPanel();
//      enableAllCheckBox = new JCheckBox("Enable Text Boxes? ",true);
//      enableAllPanel.add(enableAllCheckBox);

      TextBoxTableColumn boxColumn = new TextBoxTableColumn(TEXTBOX_COLUMN,
         " Box ");
      boxColumn.setPreferredWidth(200);
      //setup the third column
      BooleanTableColumn booleanColumn = new BooleanTableColumn(SHOW_COLUMN
         , "Show?");
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
      TableColumn [] tableColumns = {boxColumn, booleanColumn};
      tablePanel = new MultipleEditableTablePanel(tableColumns);
      //tablePanel.setPreferredSize(new Dimension(350, 250));
      tablePanel.setRowHeight(20);
      tablePanel.setBorder(BorderFactory.createLoweredBevelBorder());

      Container container = getContentPane();
      container.setLayout(new BorderLayout());
//      container.add(enableAllPanel,BorderLayout.NORTH);
      container.add(tablePanel,BorderLayout.CENTER);
      container.add(getButtonPanel(),BorderLayout.SOUTH);


   }//initialize()

   /** initialize the values from the model */
   protected void initGUIFromModel()
   {
      int rowCount = tablePanel.getRowCount();
      for (int i=0; i< rowCount; i++)
      {
         tablePanel.removeRow(0);
      }

      if(textBoxesType != null)
      {
         int numTextBoxes = textBoxesType.getNumTextBoxes();
         for(int i=0; i< numTextBoxes; i++)
         {
            TextBox aTextBox = textBoxesType.getTextBox(i);
            tablePanel.insertRow(false);
            tablePanel.setValueAt(aTextBox,i,TEXTBOX_COLUMN);
            tablePanel.setValueAt(new Boolean(aTextBox.getEnable()),i, SHOW_COLUMN);
         }
      }//if(textBoxesType != null)

   }//initGUIFromModel()

   /** save the gui to the model */
   protected void saveGUIValuesToModel() throws Exception
   {
      textBoxesType.clearTextBox();
      int rowCount = tablePanel.getRowCount();
      for(int i=0; i< rowCount; i++)
      {
         TextBox textBox = (TextBox)tablePanel.getValueAt(
            i,TEXTBOX_COLUMN);
//System.out.println("sector=" + textBox.getRegion());
//System.out.println("position=" + textBox.getPosition());
         Boolean value = (Boolean)tablePanel.getValueAt(i, SHOW_COLUMN);
         textBox.setEnable(value.booleanValue());
         textBoxesType.addTextBox(textBox);
      }//for(i)

   }//saveGUIValuesToModel()


//   /** helper method to transer the setting from one textBox to another TextBox object
//    */
//   private void transferValues(TextBorder textBorder, TextBox textBox)
//   {
//
//      textBox.setTextString(textBorder.getTextString());
////System.out.println("textBox.getTextString="+textBox.getTextString());
//      textBox.setStyle(textBorder.getStyle());
//      textBox.setTypeface(textBorder.getTypeface());
//      textBox.setColor(textBorder.getColor());
//      textBox.setTextExpansion(textBorder.getTextExpansion());
////System.out.println("textBox.getTextExpansion="+textBox.getTextExpansion());
//      textBox.setTextDegreesRotation(textBorder.getTextDegreesRotation()); // not necessary
//      //warnig only grid positioning is allowed
//     textBox.setPosition(textBorder.getPosition(),textBorder.getXJustification(),
//             textBorder.getYJustification());
////System.out.println("textBox.="+textBox.);
//      textBox.setDrawBorder(textBorder.getDrawBorder());
////System.out.println("textBox.="+textBox.);
//      textBox.setBorderLinestyle(textBorder.getBorderLinestyle());
////System.out.println("textBox.="+textBox.);
//      textBox.setBorderLinewidth(textBorder.getBorderLinewidth());
////System.out.println("textBox.="+textBox.);
//      textBox.setBorderColor(textBorder.getBorderColor());
////System.out.println("textBox.="+textBox.);
//      textBox.setBackgroundColor(textBorder.getBackgroundColor());
////System.out.println("textBox.="+textBox.);
//      textBox.setPadBottom(textBorder.getPadBottom());
////System.out.println("textBox.="+textBox.);
//      textBox.setPadLeft(textBorder.getPadLeft());
////System.out.println("textBox.="+textBox.);
//      textBox.setPadTop(textBorder.getPadTop());
////System.out.println("textBox.="+textBox.);
//      textBox.setPadRight(textBorder.getPadRight());
////System.out.println("textBox.="+textBox.);
//   }//transerValues()
//

    /**
    * @param args the command line arguments
    */
   public static void main(String[] args)
   {
      DefaultUserInteractor.set(new GUIUserInteractor());
      try
      {
         TextBoxesType aTBsType = new TextBoxesType();
         TextBoxesTypeEditor editor = new TextBoxesTypeEditor(aTBsType);
         editor.setVisible(true);
      }
      catch(Exception e)
      {
         e.printStackTrace();
      }
   }//main()


}//End
