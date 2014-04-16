package gov.epa.mims.analysisengine.tree;

import java.io.Serializable;

import java.util.HashMap;
import java.util.Set;

/**
 * node that holds analysis options
 * <p>
 * Elided Code Example:
 * 
 * <pre>
 *     :
 *  //====================================================================
 *  //
 *  // initialize tree components
 *  //
 *  //====================================================================
 *  AnalysisOptions optionsGlobal = initAnalysisOptions();
 *  Page page = new Page();
 *  ScatterPlot scatterPlot = new ScatterPlot();
 *  dataSets = initDataSets(scatterPlot);
 * 
 *  //====================================================================
 *  //
 *  // build tree
 *  //
 *  //====================================================================
 *  //
 *  //                dataSets
 *  //                    |
 *  //              AnalysisOptions(Options Global)
 *  //                    |
 *  //                  page
 *  //                    |
 *  //               scatterPlot
 *  //
 *  dataSets.add(optionsGlobal);
 *  optionsGlobal.add(page);
 *  page.add(scatterPlot);
 * 
 *     :
 *     :
 *  private AnalysisOptions initAnalysisOptions()
 *  {
 *     String aOUTLINETYPE = OUTLINE_TYPE;
 *     String aPAGETYPE = PAGE_TYPE;
 *     String aLINETYPE = LINE_TYPE;
 *     String aXAXIS = XAXIS;
 *     String aYAXIS = YAXIS;
 *     String aPLOTTITLE = PLOT_TITLE;
 *     String aPLOTFOOTER = PLOT_FOOTER;
 *     String aLEGEND = LEGEND;
 * 
 *     AnalysisOptions options = new AnalysisOptions();
 *     options.addOption(aPAGETYPE, initPageType());
 *     options.addOption(aLINETYPE, initLineType());
 *     options.addOption(aOUTLINETYPE, initOutlineType());
 *     options.addOption(aXAXIS, initXAxis());
 *     options.addOption(aYAXIS, initYAxis());
 *     options.addOption(aPLOTTITLE, initPlotTitle());
 *     options.addOption(aPLOTFOOTER, initPlotFooter());
 *     options.addOption(aLEGEND, initLegend());
 * 
 *     return options;
 *  }
 * 
 *     :
 *     :
 * 
 * </pre>
 * 
 * <p>
 * Processing of AnalysisOptions when more than one AnalysisOptions component exists: <br>
 * Consider the following complex tree
 * 
 * <pre>
 * //
 * //
 * //                     dataSets
 * //                         |
 * //                   optionsGlobal
 * //                         |
 * //        ------------------------------------
 * //        |                |                 |
 * //      page1   			page2     	      page3	
 * //        |                |                 |
 * //     optionsLocal1    optionsLocal2   optionsLocal3
 * //        |                |                 |
 * //     barPlot1         barPlot2         barPlot3
 * //
 * </pre>
 * 
 * optionsGlobal, optionsLocal1,optionsLocal2, and optionsLocal3 are all AnalysisOptions components each containing
 * disparate options.
 * <p>
 * How does a plot find its AnalysisOptions?
 * <p>
 * When searching for an AnalysisOption such as XAXIS the tree is searched in a bottom up fashion starting with the plot
 * component and ending with the top level node, dataSets in this example. For example, barPlot1 options are located in
 * optionsLocal1 and optionsGlobal.
 * <p>
 * In general a traversal of a tree branch may contain several AnalysisOptions components. Even though a given option,
 * say XAXIS, may occur only once in a given AnalysisOptions component, it be present in more than one AnalysisOptions
 * component along the path from the plot to the top level node. If this is the case the option appearing in the
 * AnalysisOptions component closest to the plot component takes precedence. In the present example options in
 * optionsLocal will over ride those in optionsGlobal.
 * 
 * 
 * @author Tommy E. Cathey
 * @version $Id: AnalysisOptions.java,v 1.4 2006/12/11 22:16:39 parthee Exp $
 * 
 */
public class AnalysisOptions extends Branch implements Serializable, Cloneable, AnalysisOptionConstantsIfc {
	/** serial version UID */
	static final long serialVersionUID = 1;

	/** hash map of options */
	private HashMap optionsHash = new HashMap();

	/*******************************************************************************************************************
	 * get keys
	 * 
	 * @return a set containing all option keys in this class
	 ******************************************************************************************************************/
	public Set getKeys() {
		return optionsHash.keySet();
	}

	/*******************************************************************************************************************
	 * get an option
	 * 
	 * @param key
	 *            keyword String which serves as look up key
	 * @return AnalysisOption object corresponding to given key null if key is not in optionsHash
	 * @pre key !=null
	 ******************************************************************************************************************/
	public Object getOption(String key) {
		return (optionsHash.containsKey(key)) ? (optionsHash.get(key)) : null;
	}

	/*******************************************************************************************************************
	 * accept a Node visitor
	 * 
	 * @param v
	 *            VisitorIfc to accept
	 * @pre v != null
	 ******************************************************************************************************************/
	public void accept(VisitorIfc v) {
		v.visit(this);
	}

	/*******************************************************************************************************************
	 * add an option
	 * 
	 * @param key
	 *            keyword String which serves as look up key
	 * @param obj
	 *            the AnalysisOption to store
	 * @pre key !=null
	 * @pre obj !=null
	 ******************************************************************************************************************/
	public void addOption(String key, Object obj) {
		optionsHash.put(key, obj);
	}

	/*******************************************************************************************************************
	 * Creates and returns a copy of this object
	 * 
	 * @return a copy of this object
	 ******************************************************************************************************************/
	public Object clone() {
		try {
			AnalysisOptions clone = (AnalysisOptions) super.clone();
			clone.optionsHash = (optionsHash == null) ? null : (HashMap) optionsHash.clone();

			return clone;
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

	/*******************************************************************************************************************
	 * Compares this object to the specified object.
	 * 
	 * @param o
	 *            the object to compare this object against
	 * 
	 * @return true if the objects are equal; false otherwise
	 ******************************************************************************************************************/
	public boolean equals(Object o) {
		boolean rtrn = true;

		if (!super.equals(o)) {
			rtrn = false;
		} else {
			AnalysisOptions other = (AnalysisOptions) o;

			rtrn = Util.equals(optionsHash, other.optionsHash);
		}

		return rtrn;
	}

	/*******************************************************************************************************************
	 * remove an option
	 * 
	 * @param key
	 *            keyword String which serves as look up key
	 * @pre key !=null
	 * @return Object the object that was removed (or null if not found)
	 ******************************************************************************************************************/
	public Object removeOption(String key) {
		return optionsHash.remove(key);
	}

	/*******************************************************************************************************************
	 * describe object in a String
	 * 
	 * @return String describing object
	 ******************************************************************************************************************/
	public String toString() {
		return Util.toString(this);
	}
}