package gov.epa.mims.analysisengine.tree;

/**
 * Node visitor interface functions
 *
 * @author Tommy E. Cathey
 * @version $Id: NodeVisitorIfc.java,v 1.2 2005/09/19 14:50:10 rhavaldar Exp $
 *
 ***************************************************************/
public interface NodeVisitorIfc
{
   /**
    * visit an AnalysisOptions node
    *
    * @param p AnalysisOptions to be visited
    * @pre p != null
    ***************************************************************/
   void visitAnalysisOptions(AnalysisOptions p);

   /**
    * visit a BarPlot node
    *
    * @param p BarPlot node to visit
    * @pre p != null
    ***************************************************************/
   void visitBarPlot(BarPlot p);

   /**
    * visit a DataSets node
    *
    * @param p Data Sets node to visit
    * @pre p != null
    ***************************************************************/
   void visitDataSets(DataSets p);

   /**
    * visit a DiscreteCategoryPlot node
    *
    * @param p DiscreteCategoryPlot node to visit
    * @pre p != null
    ***************************************************************/
   void visitDiscreteCategoryPlot(DiscreteCategoryPlot p);

   /**
    * visit a HistogramPlot node
    *
    * @param p HistogramPlot node to visit
    * @pre p != null
    ***************************************************************/
   void visitHistogramPlot(HistogramPlot p);

   /**
    * visit a Page node
    *
    * @param p Page to be visited
    * @pre p != null
    ***************************************************************/
   void visitPage(Page p);

   /**
    * visit a RankOrderPlot node
    *
    * @param p RankOrderPlot node to visit
    * @pre p != null
    ***************************************************************/
   void visitRankOrderPlot(RankOrderPlot p);

   /**
    * visit a ScatterPlot node
    *
    * @param p ScatterPlot node to visit
    * @pre p != null
    ***************************************************************/
   void visitScatterPlot(ScatterPlot p);

   /**
    * visit TimeSeries node
    *
    * @param p TimeSeries node to visit
    * @pre p != null
    ***************************************************************/
   void visitTimeSeriesPlot(TimeSeries p);
}