package gov.epa.mims.analysisengine.tree;

import gov.epa.mims.analysisengine.AnalysisEngineConstants;

import java.util.Hashtable;

/**
 * <p>
 * This class traveres the tree and keeps track of how many pages and plots there are as well as the number of each type
 * of plot. It provides access methods to get this information once the tree has been traversed.
 * </p>
 * 
 * NOTE: YOU MUST ADD A NEW visit() METHOD FOR EACH PLOT THAT YOU ADD.
 * <p>
 * Copyright: Copyright (c) 2003
 * </p>
 * <p>
 * Company: UNC-CH, Carolina Environmental Program
 * </p>
 * 
 * @author Daniel Gatti
 * @version $Id: TreeSummary.java,v 1.4 2006/12/11 22:16:39 parthee Exp $
 */
public class TreeSummary extends Visitor {
	/** The number of pages in the current tree. */
	protected int numPages = 0;

	/** The number of each type of plot in the current tree. */
	protected int[] numPlots = null;

	/** The types of plots that we have. */
	protected String[] plotTypes = null;

	/** A hash table for fast lookup of plot information. */
	protected Hashtable plotCounts = new Hashtable();

	/**
	 * Given a non-null tree, traverse it and count the number of pages and the number of each type of plot.
	 * 
	 * @param tree
	 *            Branch that is the root of a tree. (Cannot be null)
	 */
	public TreeSummary(Branch tree) {
		plotTypes = AnalysisEngineConstants.PLOT_NAMES;
		numPlots = new int[plotTypes.length];
		tree.accept(this);

		// Populate the plot count hashtable with the data we found.
		for (int i = 0; i < numPlots.length; i++) {
			plotCounts.put(plotTypes[i], new Integer(numPlots[i]));
		}
	}

	/**
	 * Return the number of pages in the tree.
	 * 
	 * @return int that is the number of pages in the tree.
	 */
	public int getNumPages() {
		return numPages;
	}

	/**
	 * Get the number of plots of a specific type.
	 * 
	 * @param plotName
	 *            String that <b>must</b> be one of the plot types in
	 * @return int that is the number of plots for the given type.
	 */
	public int getCountForPlot(String plotName) {
		Integer count = (Integer) plotCounts.get(plotName);
		if (count == null)
			throw new IllegalArgumentException("The plot name " + plotName + "is not a recognized plot type in "
					+ getClass().toString() + ".getPlotCountForPlot()");

		return count.intValue();
	}

	/**
	 * Return the total number of plots in the tree.
	 * 
	 * @return int that is the total number of plots in the tree.
	 */
	public int getTotalNumPlots() {
		int sum = 0;
		for (int i = 0; i < numPlots.length; i++)
			sum += numPlots[i];

		return sum;
	}

	public Object visit(BarPlot plot) {
		return visitPlot(plot);
	}

	public Object visit(DiscreteCategoryPlot plot) {
		return visitPlot(plot);
	}

	public Object visit(HistogramPlot plot) {
		return visitPlot(plot);
	}

	public Object visit(RankOrderPlot plot) {
		return visitPlot(plot);
	}

	public Object visit(ScatterPlot plot) {
		return visitPlot(plot);
	}

	public Object visit(TimeSeries plot) {
		return visitPlot(plot);
	}

	public Object visitPlot(Plot plot) {
		System.out.println("visit(Plot)");
		String plotName = plot.getName();
		int i = 0;
		for (i = 0; i < plotTypes.length; i++) {
			System.out.println(plotTypes[i] + " " + plotName);
			if (plotTypes[i].equalsIgnoreCase(plotName)) {
				numPlots[i]++;
				break;
			}
		}

		if (i == plotTypes.length)
			throw new IllegalArgumentException("Unknown plot type " + plotName + " in " + getClass().toString()
					+ ".visit(Plot)!");

		return null;
	}

	public Object visit(Page page) {
		System.out.println("visit(Page)");
		numPages++;
		for (int c = 0; c < page.getChildCount(); c++)
			((Node) page).getChild(c).accept(this);
		return null;
	}

	public Object visit(AnalysisOptions analyisOptions) {
		System.out.println("visit(AnalysisOptions)");
		for (int c = 0; c < analyisOptions.getChildCount(); c++)
			((Node) analyisOptions).getChild(c).accept(this);
		return null;
	}

	public Object visit(DataSets dataSets) {
		System.out.println("visit(DataSets)");
		for (int c = 0; c < dataSets.getChildCount(); c++)
			((Node) dataSets).getChild(c).accept(this);
		return null;
	}
}
