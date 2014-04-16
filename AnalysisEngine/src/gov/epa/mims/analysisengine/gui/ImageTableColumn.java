/*
 * ImageTableColumn.java
 *
 * Created on December 2, 2003, 5:35 PM
 */

package gov.epa.mims.analysisengine.gui;


import java.awt.Toolkit;
import java.awt.datatransfer.*;
import java.util.EventObject;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import gov.epa.mims.analysisengine.gui.DefaultUserInteractor;
import gov.epa.mims.analysisengine.gui.UserInteractor;

public class ImageTableColumn extends SpecialTableColumn
{
   
    /** The list of valid choices for the user to choose from. This will 
    * be null if the user can enter any value. */
   protected ImageIcon [] validChoices = null;
   
   /** The default value that will be place in newly added rows. */
   protected ImageIcon defaultValue =
   createImageIcon("/gov/epa/mims/analysisengine/gui/icons/lineStyles/blank.jpg");
   
   /** Creates a new instance of ImageTableColumn */
   public ImageTableColumn(int modelIndex,String name)
   {
      super(modelIndex, name);
      dataFlavor = ImageSelection.imageFlavor;
      type = ImageIcon.class;
   }
   
   
   /**
    * To create a image icon object given a image
    * @path A String denotes the location of the image icon file
    */
   protected ImageIcon createImageIcon(String path)
   {
      java.net.URL imgURL = EditableTablePanel.class.getResource(path);
      if (imgURL != null)
      {
         return new ImageIcon(imgURL);
      }
      else
      {
         System.err.println("Could not find file: " + path);
         return null;
      }
   }
   
   /**
    * Copy the currently selected row.
    * Note: This works only if one row is selected.
    *
    * @author Daniel Gatti
    */
   protected void copySelectedCell(Object obj)
   {
     ImageIcon c = (ImageIcon)obj;
      contents = new ImageSelection(c);
   }
   
   /**
    * getter for the default value
    * @return Object actual type will be ImageIcon
    */
   public Object getDefaultValue()
   {
      return this.defaultValue;
   }
   

   /**
    * setter for the default value
    * @param obj actual type should be of type ImageIcon
    */
   public void setDefaultValue(Object obj) throws Exception
   {
      if (obj instanceof ImageIcon)
      {
         defaultValue = (ImageIcon)obj;
      }
      else
      {
         throw new Exception("Expected a Double value in " + 
            "DoubleEditableTablePanel.setDefaultValue()");
      }
   }
   
   /**
    * Set a list of valid choices that will be the only ones that the user can enter into the table.
    *
    * @author Daniel Gatti
    * @param choices Object[] that is a list of valid choices to be displayed in a combo box
    *    when the user is editing data in the table.
    * @see gov.epa.mims.analysisengine.gui.EditableTablePanel#setValidChoices(java.lang.Object[])
    */
   public void setValidChoices(Object[] choices)
   {
      if (choices == null || choices.length == 0)
         return;
      
      if (!(choices[0] instanceof ImageIcon))
         return;
      
      validChoices = new ImageIcon[choices.length];
      System.arraycopy(choices, 0, validChoices, 0, choices.length);
      
      setUpComboBoxEditor();
   }
   
   
   /**
    * Create a combo box with the valid choices passed in by the user and tell the
    * table to use it for Integers.
    * NOTE: I assume that you have checked that the validChoices array is not null.
    *
    *@author Daniel Gatti
    */
   protected void setUpComboBoxEditor()
   {
      // Create a JComboBox and place the images in it.
      JComboBox comboBox = new JComboBox((ImageIcon[])validChoices);
      comboBox.setSelectedIndex(0);
     
      DefaultCellEditor editor = new DefaultCellEditor(comboBox);
      editor.setClickCountToStart(2);
      cellEditor = editor;
   }
   
   protected void setupCellEditor()
   {
      //code need to be filled for create Image icon from a file
      
   }
   
  
   /** 
    * return the next object in the valid choices
    */
   public Object nextChoice()
   {
      if(validChoices != null)
      {
         if(count >= validChoices.length)
         {
            count = 0; 
         }
         return validChoices[this.count++];
      }
      else
      {
         //error message
      }
      return this.defaultValue;
   }   
   
 //  setUpComboBoxEditor()
}

