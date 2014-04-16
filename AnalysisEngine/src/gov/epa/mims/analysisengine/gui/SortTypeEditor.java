package gov.epa.mims.analysisengine.gui;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import gov.epa.mims.analysisengine.tree.SortType;

/**
 * The editor for the SortType.
 * Provide options for what to do with missing data, which direction to sort in
 * and whether sorting is enabled.
 * We chose to use Quicksort as the default plotting algorithm and do not
 * currently give the user a choice.
 * This is used in the RankOrderPlot.
 *
 * @author Daniel Gatti
 * @see gov.epa.mims.analysisengine.tree.SortType.java
 * @see gov.epa.mims.analysisengine.tree.RankOrderPlot.java
 * @version $Id: SortTypeEditor.java,v 1.3 2005/09/21 14:19:48 parthee Exp $
 */
public class SortTypeEditor
   extends OptionDialog
{
   /** The SortType that this GUI will be editing. */
   protected SortType sortType = null;

   /** Checkbox to enable/disable the sorting. */
   protected BooleanValuePanel enablePnl = null;

   /** Radio buttons to sort in ascending or descending order.
    *  Note: true = descending, false = ascending. */
   protected BooleanValuePanel sortOrderPnl = null;

   /** A Combobox to select what to do with missing data. */
   protected StringChooserPanel missingDataPnl = null;


   /**
    * Null constructor need for class.newInstance.
    *
    * @authoe Daniel Gatti
    */
   public SortTypeEditor()
   {
        this(null);
   }



   /**
    * Constructor.
    *
    * @author Daniel Gatti
    * @param sortType SortType that this GUI will represent.
    */
   protected SortTypeEditor(SortType sortType)
   {
      super();
      this.sortType = sortType;
      this.setDataSource(sortType, "");
      initialize();
      setLocation(ScreenUtils.getPointToCenter(this));
   }



   /**
    * Build the GUI.
    *
    * @author Daniel Gatti
    */
   private void initialize()
   {
      enablePnl = new BooleanValuePanel("Sort values?");
      enablePnl.addActionListener(new ActionListener()
         {
            public void actionPerformed(ActionEvent e)
            {
               boolean enabled = enablePnl.getValue();
               sortOrderPnl.setEnabled(enabled);
               missingDataPnl.setEnabled(enabled);
            }
         }
      );
      // Note: true = descending, false = ascending
      sortOrderPnl = new BooleanValuePanel("Ascending", "Descending", true);
      sortOrderPnl.setBorder(BorderFactory.createEtchedBorder());
      JPanel sortPanel = new JPanel();
      sortPanel.setLayout(new BoxLayout(sortPanel, BoxLayout.X_AXIS));
      sortPanel.setBorder(BorderFactory.createTitledBorder(
         BorderFactory.createLoweredBevelBorder(),
         "Sorting", TitledBorder.LEFT, TitledBorder.TOP));
      sortPanel.add(enablePnl);
      sortPanel.add(sortOrderPnl);

      // DMG - 12/19/2003- Removing the SortType.REMOVE option until we can get
      // it to work.
      missingDataPnl = new StringChooserPanel(""/*"What would you like to do with missing data?"*/,
         false, new String[] {/*SortType.REMOVE, */SortType.BEGINNING, SortType.ENDING});
      missingDataPnl.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(
         BorderFactory.createLoweredBevelBorder(),
         "Missing Data", TitledBorder.LEFT, TitledBorder.TOP),
         BorderFactory.createEmptyBorder(0, 10, 10, 10)));

      Container contentPane = getContentPane();
      contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
      contentPane.add(sortPanel);
      contentPane.add(missingDataPnl);
      contentPane.add(getButtonPanel());
      this.setModal(true);
   } // initialize()


   /**
    * Take the values from the underlying SortType and populate the GUI with those
    * values.
    *
    * @author Daniel Gatti
    * @see gov.epa.mims.analysisengine.gui.OptionDialog#initGUIFromModel()
    */
   protected void initGUIFromModel()
   {
      enablePnl.setValue(sortType.getEnable());
      sortOrderPnl.setValue(!sortType.getAscending());
      missingDataPnl.setValue(sortType.getMissingData());

      // Set the enabled state for the sorting panels.
      boolean enabled = enablePnl.getValue();
      sortOrderPnl.setEnabled(enabled);
      missingDataPnl.setEnabled(enabled);
   }


   /**
    * Take the values in the GUI and set them in the underlying SortType.
    *
    * @author Daniel Gati
    * @see gov.epa.mims.analysisengine.gui.OptionDialog#saveGUIValuesToModel()
    */
   protected void saveGUIValuesToModel() throws Exception
   {
      // We must use Shell sort because quicksort only sorts in ascedning order.
      sortType.setSortMethod(SortType.SHELL);
      sortType.setEnable(enablePnl.getValue());
      //   Note: true = descending, false = ascending
      sortType.setAscending(!sortOrderPnl.getValue());
      sortType.setMissingData(missingDataPnl.getValue());
   }


   /**
    * Set the date source for this GUI.
    *
    * @author Daniel Gatti
    * @param dataSource Object that is a SortType.
    * @param optionName String
    */
   public void setDataSource(Object dataSource, String optionName)
     {
       sortType = (SortType)dataSource;
       super.setDataSource(dataSource, optionName);
       if (sortType != null)
         initGUIFromModel();

       pack();
       repaint();
      }
} // class SortTypeEditor

