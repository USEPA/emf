package gov.epa.mims.analysisengine.tree;

import java.io.Serializable;

import java.util.ArrayList;

/***********************************************************************************************************************
 * a required tree node which signals the start of a plot page
 * <p>
 * Elided Code Example:
 * 
 * <pre>
 *      :
 *      :
 *   //====================================================================
 *   //
 *   // initialize tree components
 *   //
 *   //====================================================================
 *   AnalysisOptions optionsGlobal = initAnalysisOptions();
 *   Page page = new Page();
 *   ScatterPlot scatterPlot = new ScatterPlot();
 *   dataSets = initDataSets(scatterPlot);
 *  
 *  
 *   //====================================================================
 *   //
 *   // build tree
 *   //
 *   //====================================================================
 *   //
 *   //
 *   //                     dataSets
 *   //                         |
 *   //                   optionsGlobal
 *   //                         |
 *   //                       page
 *   //                         |
 *   //                    scatterPlot
 *   //
 *   dataSets.add(optionsGlobal);
 *   optionsGlobal.add(page);
 *   page.add(scatterPlot);
 *  
 * </pre>
 * 
 * @author Tommy E. Cathey
 * @version $Id: Page.java,v 1.3 2006/12/08 22:46:52 parthee Exp $
 * 
 **********************************************************************************************************************/
public class Page extends Branch implements Serializable, Cloneable, AnalysisOptionConstantsIfc, PageConstantsIfc {
	/** serial version UID */
	static final long serialVersionUID = 1;

	/** reference to PageInfo object */
	private static PageInfo pageInfo = null;

	/**
	 * array of all valid file formats which can be passed to setForm(String) of a PageType object
	 */
	private static String[] fileFormat;

	// static initialization block
	{
		// statically initialize the fileFormat array
		fileFormat = new String[] { SCREEN, X11, PNG_BITMAP, JPEG, PDF, POSTSCRIPT, LATEX_PICTEX_GRAPHICS };
	}

	/*******************************************************************************************************************
	 * retrieve valid file formats for page output; these are the valid arguments for setForm(String) of PageType
	 * objects
	 * 
	 * @see gov.epa.mims.analysisengine.tree.PageType#setForm(String)
	 * @return array of file format Strings
	 ******************************************************************************************************************/
	public static String[] getFileFormats() {
		return (String[]) fileFormat.clone();
	}

	/*******************************************************************************************************************
	 * retrieve page infomation
	 * 
	 * @return returns PageInfo
	 ******************************************************************************************************************/
	public static PageInfo getPageInfo() {
		if (pageInfo == null) {
			initPageInfo();
		}

		return pageInfo;
	}

	/*******************************************************************************************************************
	 * accept a Node visitor
	 * 
	 * @param v
	 *            visitor class
	 ******************************************************************************************************************/
	public void accept(VisitorIfc v) {
		v.visit(this);
	}

	/*******************************************************************************************************************
	 * Creates and returns a copy of this object
	 * 
	 * @return a copy of this object
	 ******************************************************************************************************************/
	public Object clone() {
		try {
			Page clone = (Page) super.clone();

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
		return super.equals(o);
	}

	/*******************************************************************************************************************
	 * describe object in a String
	 * 
	 * @return String describing object
	 ******************************************************************************************************************/
	public String toString() {
		return Util.toString(this);
	}

	/*******************************************************************************************************************
	 * create PageInfo Object and save a reference to it
	 ******************************************************************************************************************/
	private static void initPageInfo() {
		// add any new AnalysisOptions keywords to the
		// ArrayList allKeywordsList
		ArrayList allKeywordsList = new ArrayList();
		allKeywordsList.add(PAGE_TYPE);

		// convert ArrayList allKeywordsList to a String[]
		String[] allKeywords = new String[allKeywordsList.size()];
		allKeywordsList.toArray(allKeywords);

		// create the PageInfo object, save it for future
		// references (create once refence many times)
		pageInfo = new PageInfo(allKeywords);
	}
}