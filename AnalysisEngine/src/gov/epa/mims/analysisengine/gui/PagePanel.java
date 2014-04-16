package gov.epa.mims.analysisengine.gui;

import gov.epa.mims.analysisengine.tree.AnalysisOptions;
import gov.epa.mims.analysisengine.tree.DataSetsAdapter;
import gov.epa.mims.analysisengine.tree.DisplaySizeType;
import gov.epa.mims.analysisengine.tree.Page;
import gov.epa.mims.analysisengine.tree.PageInfo;
import gov.epa.mims.analysisengine.tree.PageType;
import gov.epa.mims.analysisengine.tree.Plot;
import javax.swing.JPanel;
import java.awt.*;

/**
 * A panel for setting up a page. This will eventually let the user select
 * the number of rows and columns desired, will display the plot type in each
 * matrix cell (or let the user choose one from a list).
 * Currently it contains a table for editing page options.
 *
 * If a particular plot is selected on the PagePanel, it will show an
 * OptionsPanel for that plot in the lower part of the panel.
 *
 * @author Alison Eyth
 * @version $Id: PagePanel.java,v 1.3 2005/09/21 14:19:48 parthee Exp $
 *
 **/

public class PagePanel
   extends JPanel
   implements gov.epa.mims.analysisengine.tree.AnalysisOptionConstantsIfc,
     gov.epa.mims.analysisengine.tree.PageConstantsIfc
{
   /** the panel to display / edit the page's options */
   PageOptionsPanel pageOptionsPanel;

   /** the panel to display / edit the plot's options and other attributes */
   PlotPanel plotPanel;

   /** The AnalysisOptions node for this page */
   private AnalysisOptions pageOptions;

   /** The AnalysisOptions node for the currently active plot */
   private AnalysisOptions plotOptions;

   /** The currently active plot */
   private Plot currentPlot;

   /** The page */
   private Page page;

   /** data set adapter to choose data sets from */
   private DataSetsAdapter dataSetsAdapter;

   /** The parent TreeDialog. */
   private TreeDialog parent = null;

   /**
    * constructor with parameters for preexisting page and plot options
    *
    * @param pageOptions AnalysisOptions for the page (could be null)
    * @param plotOptions AnalysisOptions for the plot  (could be null)
    * @param plot an existing plot (could be null)
    * @param dataSetsAdapter DataSetsAdapter to select data from
    */
   public PagePanel(AnalysisOptions pageOptions, AnalysisOptions plotOptions,
      Plot plot, DataSetsAdapter dataSetsAdapter, TreeDialog parent)
   {
      // TBD: the form of this constructor will need to change when the form
      //    of our tree changes
      // TBD: don't use PageInfo - get rid of it once Page returns info
      // about itself
      this.pageOptions = pageOptions;  // could be NULL
      this.dataSetsAdapter = dataSetsAdapter;
      this.parent = parent;
      plotPanel = new PlotPanel(plotOptions, plot, dataSetsAdapter);
      initialize();
   }//PagePanel(pageOptions, plotOptions)

   /**
    * constructor to create a particular type of plot and no preexisting options
    *
    * @param plotType String with information about the type of plot
    * @param dataSetsAdapter DataSetsAdapter to select data from
    */
   public PagePanel(String plotType, DataSetsAdapter dataSetsAdapter,
                    TreeDialog parent)
   {
      // add a new OptionsPanel for the page options
      pageOptions = null;
      this.dataSetsAdapter = dataSetsAdapter;
      this.parent = parent;
      plotPanel = new PlotPanel(plotType, dataSetsAdapter);
      initialize();
   }//PagePanel(pageOptions, plotOptions)


   /**
    * method to initialize the dialog
    */
   private void initialize()
   {
      PageInfo pageInfo = new PageInfo(new String[]{});
      page = new Page();  // TBD: do we need to do anything else w/ page?
      pageOptionsPanel = new PageOptionsPanel(pageOptions, parent);

      // eventually, add a control for setting up multiple plots and choosing
      // one to work on.  For now, there is just one plot.

      // at implementation, use a layout to make this look nice.  Page options
      // on top, plot panel (w/ options) on the bottom.
      setLayout(new BorderLayout());
      add(pageOptionsPanel, BorderLayout.SOUTH);
      add(plotPanel, BorderLayout.CENTER);

      plotOptions = plotPanel.getPlotOptions();
      pageOptions = pageOptionsPanel.getOptions();
      //this.currentPlot = plotPanel.getPlot();
   }//initialize()

   /**
    * @return the current page options
    */
   protected AnalysisOptions getPageOptions()
   {
      pageOptions = pageOptionsPanel.getOptions();
      plotOptions = plotPanel.getPlotOptions();
      DisplaySizeType displayType =(DisplaySizeType)plotOptions.getOption(DISPLAY_SIZE_TYPE);
      if(displayType.getEnable())
      {
         pageOptions.addOption(DISPLAY_SIZE_TYPE, displayType);
      }
      else
      {
         pageOptions.addOption(DISPLAY_SIZE_TYPE, new DisplaySizeType());
      } 
      return pageOptions;
   }

   /**
    * @return the current plot options
    */
    protected AnalysisOptions getPlotOptions()
   {
      plotOptions = plotPanel.getPlotOptions();
      return plotOptions;
   }

   /**
    * @return the page
    */
   protected Page getPage()
   {
      return page;
   }

   /**
    * @return the current plot
    */
   protected Plot getPlot()
   {
      return plotPanel.getPlot();
   }

   /**
    * Set new PageOptions and update the GUI accordingly.
    *
    * @param newPage Page to use as the new data model.
    * @param newPageOptions PageOptions to use as the new data model and
    *        to populate the GUI.
    */
   protected void setDataModel(AnalysisOptions newPageOptions,
       AnalysisOptions newPlotOptions, Plot newPlot, DataSetsAdapter newDataSetsAdapter)
  {
      this.pageOptions = newPageOptions;
      this.plotOptions = newPlotOptions;
      this.dataSetsAdapter = newDataSetsAdapter;
      plotPanel.setDataModel(newPlotOptions, newPlot, newDataSetsAdapter);

      updateGUIFromModel();
   }


   /**
    * take any actions needed when the user hits create plots or save
    * @param onScreen boolean that is true if the plot is being drawn on screen.
    * @throws Exception
    */
   protected void storeGUIValues(boolean onScreen) throws Exception
   {
      pageOptionsPanel.storeGUIValues(onScreen);
      plotPanel.storeGUIValues(onScreen);
   }


   /**
    * Update the GUI based on the state of the model.
    */
   protected void updateGUIFromModel()
   {
      PageType pageType = (PageType)pageOptions.getOption(PAGE_TYPE);
      pageType.setForm(SCREEN);

      pageOptionsPanel.setDataModel(pageType, pageOptions);
      pageOptionsPanel.initGUIFromModel();
      pageOptionsPanel.repaint();
   }
}
