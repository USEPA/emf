package gov.epa.mims.analysisengine.table.filter;

import gov.epa.mims.analysisengine.gui.OptionDialog;

import java.awt.BorderLayout;
import java.awt.Container;

import javax.swing.JFrame;

/**
 * A Window that allows the user to enter information about how to filter
 * data.
 *
 * @author  Daniel Gatti
 * @version $Id: FilterRowGUI.java,v 1.1 2006/11/01 15:33:39 parthee Exp $
 */

public class FilterRowGUI extends OptionDialog
{
   /** The FilterPanel that handles data display and editing. */
   private FilterRowPanel filterRowPanel;
   
   /**
    * Constructor.
    *
    * @param columnNames String[] with the names of the column in the table
    * that is being filtered. (Not the table in this GUI).
    * @param columnClasses Class[]  with the classes in the table.
    * @param columnFormats Format[] with the formats in the table.
    * @param filterCriteria FilterCriteria[] that this GUI should represent.
    *       Could be null.
    */
   public FilterRowGUI(JFrame parent, String[] columnNames, Class[] columnClasses,
                      FilterCriteria filterCriteria)
   {
      super(parent);
      filterRowPanel = new FilterRowPanel(columnNames,columnClasses, filterCriteria);
      this.setDataSource(filterCriteria, "");
      initialize();
      setModal(true);
      setTitle("Filter Rows");
      //initGUIFromModel();
      pack();
   } // FilterRowGUI()

   /**
    * Return the filter criteria based on this GUI.
    * @return FilterCriteria that reflects the information in the GUI.
    */
   public FilterCriteria getFilterCriteria()
   {
      //saveGUIValuesToModel(); ?? check whether this is necessary?? RP
      return filterRowPanel.getFilterCriteria();
   } // getFilterCriteria()

   /**
    * Build the GUI.
    */
   public void initialize()
   {
      Container contentPane = getContentPane();
      contentPane.add(filterRowPanel, BorderLayout.CENTER);
      contentPane.add(getButtonPanel(), BorderLayout.SOUTH);
   } // initialize()

   /**
    * Initialize the GUI based onthe FilterCriteria passed in.
    */
   public void initGUIFromModel()
   {
      filterRowPanel.initGUIFromModel();
   } // initGUIFrommodel()

   /**
    * Save the data from the GUI to a FilterCriteria.
    */
   protected void saveGUIValuesToModel()
   {
      boolean sucess = filterRowPanel.saveGUIValuesToModel();
      shouldContinueClosing  = sucess;
   } // saveGUIValuesToModel()

} // class FilterRowGUI
