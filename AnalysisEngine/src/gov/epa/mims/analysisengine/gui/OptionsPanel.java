package gov.epa.mims.analysisengine.gui;

import gov.epa.mims.analysisengine.tree.AnalysisOption;
import gov.epa.mims.analysisengine.tree.AnalysisOptions;
import gov.epa.mims.analysisengine.tree.AvailableOptionsAndDefaults;
import gov.epa.mims.analysisengine.tree.AnalysisOptionConstantsIfc;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;

/**
 * A panel to display, edit and save options.  An OptionsTable containing name-
 * value pairings is included, in addition to a toolbar for copy, paste, and
 * clear actions.
 *
 * @author Alison Eyth
 * @version $Id: OptionsPanel.java,v 1.2 2005/09/19 14:50:03 rhavaldar Exp $
 *
 **/

public class OptionsPanel
  extends JPanel
{
   /** a tool bar for the operations on the table */
   private JToolBar toolbar = new JToolBar();

   /** button to copy */
   private JButton copyButton = new JButton("Copy");

   /** button to paste */
   private JButton pasteButton = new JButton("Paste");

   /** button to clear */
   private JButton clearButton = new JButton("Clear");

   /** the table to display and edit the options */
   private OptionsTable optionsTable;

   /** the options shown in the table */
   AnalysisOptions analysisOptions;

   /** the list of all possible keywords */
   String [] allKeywords;

   /** the default values for options */
   AnalysisOption [] defaultValues;

   /** A String that describesthe plot type. */
   protected String plotType = null;

   /**
    * Constructor for the case that analysis options are already defined.
    * The table will show currently set values from options, but will also
    * show the entire list of keywords.  The table model will need to handle
    * this.
    *
    * @param options AnalysisOptions (could be null) that are already set
    * @param allKeywords String [] the list of all possible keywords
    * @param defaultValues Object [] default values for all options
    * @param plotType String that is the name of the plot (as on of the
    *    constants in AnalysisEngineConstants.)
    */
   public OptionsPanel(AnalysisOptions options,
     AvailableOptionsAndDefaults optAndDef, String plotType)
   {
      this.analysisOptions = options;  // could be null - TBD: maybe don't allow null
      this.plotType = plotType;
      setKeywordsAndDefaults(optAndDef);
      initialize(plotType);
   }


   private void initialize(String plotType)
   {
      this.setLayout(new BorderLayout());

      toolbar.add(clearButton);
      clearButton.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent ae)
        {
           optionsTable.clearValues();
        }
      });

      this.add(toolbar, BorderLayout.NORTH);

      // TBD: someday add a "Promote" button to promote options to higher levels

      // set the actions for the copy, paste, and clear buttons
      // the copy button ask the optionsTable to copy its selected rows to a
      //    clipboard
      // the paste button should ask the optionsTable to paste the values on
      //    the clipboard to the selected rows
      // the clear button should ask the optionsTable to clear out values for
      //   the selected rows of the table

      // TBD: this will result in options w/o mandatory keywords, this may
      //      not be advisable
      if (analysisOptions == null)
      {
         analysisOptions = new AnalysisOptions();
      }

      optionsTable = new OptionsTable(analysisOptions, allKeywords,
         defaultValues, plotType);
      JTableHeader header = optionsTable.getTableHeader();
      this.add(optionsTable, BorderLayout.SOUTH);
      this.setBorder(BorderFactory.createEtchedBorder());
   }//OptionsPanel()


   /**
    * @return AnalysisOptions with the currently set values
    */
   public AnalysisOptions getOptions()
   {
      return optionsTable.getAnalysisOptions();
   }


   /**
    * Reset the options in the table based on the new ones passed in.
    * @param newOptions AnalysisOptions that should be used to repopulate
    * the table.
    * @param newDefaults AvailableOptionsAndDefaults
    * @param plotType String
    */
    public void resetOptions(AnalysisOptions newOptions,
                             AvailableOptionsAndDefaults newDefaults,
                             String plotType)
    {
       this.analysisOptions = newOptions;
       setKeywordsAndDefaults(newDefaults);
       this.remove(optionsTable);
       optionsTable = new OptionsTable(analysisOptions, allKeywords,
                                       defaultValues, plotType);
       this.add(optionsTable, BorderLayout.SOUTH);
    }


    /**
     * Set the keywords and default values for this plot.
     * @param optAndDef
     */
    protected void setKeywordsAndDefaults(AvailableOptionsAndDefaults optAndDef)
    {
       this.allKeywords = optAndDef.getAllKeywords();
       try
       {
          this.defaultValues = optAndDef.getDefaultValues(allKeywords);
       }
       catch(CloneNotSupportedException e)
       {
          DefaultUserInteractor.get().notify(this,"Cloning Error",
                "Cloning is not supported for some of the objects",
                UserInteractor.ERROR);
       }
       catch(Exception e)
       {
          DefaultUserInteractor.get().notify(this,"Error",
                e.getMessage(), UserInteractor.ERROR);
       }
    }

}

