package gov.epa.mims.analysisengine.tree;

import gov.epa.mims.analysisengine.AnalysisEngineConstants;
import gov.epa.mims.analysisengine.AnalysisException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * 
 * RankOrderPlot is Leaf Node which signals the generation of a Rank Order Plot.
 * 
 * 
 * <p>
 * Elided Code Example:
 * 
 * <pre>
 *       :
 *       :
 *    //====================================================================
 *    //
 *    // initialize tree components
 *    //
 *    //====================================================================
 *    AnalysisOptions optionsGlobal = initAnalysisOptions();
 *    Page page = new Page();
 *    RankOrderPlot rankOrderPlot = new RankOrderPlot();
 *    dataSets = initDataSets(rankOrderPlot);
 *   
 *   
 *   
 *    //====================================================================
 *    //
 *    // build tree
 *    //
 *    //====================================================================
 *    //
 *    //
 *    //                     dataSets
 *    //                         |
 *    //                   optionsGlobal
 *    //                         |
 *    //                       page
 *    //                         |
 *    //                   rankOrderPlot
 *    //
 *    dataSets.add(optionsGlobal);
 *    optionsGlobal.add(page);
 *    page.add(rankOrderPlot);
 *       :
 *       :
 *    private DataSets initDataSets(RankOrderPlot p)
 *    {
 *       DataSets dataSets = new DataSets();
 *   
 *       // store data; use data sets unique ID as an unique key name
 *       String key1 = &quot;key1&quot;;
 *       String key2 = &quot;key2&quot;;
 *       String key3 = &quot;key3&quot;;
 *       String key4 = &quot;key4&quot;;
 *       dataSets.add(initData(&quot;My data set 1&quot;, 6), key1);
 *       dataSets.add(initData(&quot;My data set 2&quot;, 6), key2);
 *       dataSets.add(initData(&quot;My data set 3&quot;, 6), key3);
 *       dataSets.add(initData(&quot;My data set 4&quot;, 6), key4);
 *   
 *       p.setDataSetKeys(
 *             new Object[]
 *       {
 *          new String[]
 *          {
 *             key1,
 *             key2,
 *             key3,
 *             key4
 *          }
 *       });
 *       return dataSets;
 *    }
 *   
 *    private DoubleSeries initData(String seriesName, int count)
 *    {
 *       DoubleSeries ds = new DoubleSeries();
 *       ds.setName(seriesName);
 *   
 *       for (int i = 0; i &lt; count; ++i)
 *       {
 *          double value = Math.random() * 10.0;
 *          ds.addData(value);
 *       }
 *   
 *       return ds;
 *    }
 *   
 *   
 * </pre>
 * 
 * <br>
 * <A HREF="doc-files/ExampleRankOrder01.html"><B>View Example</B></A> <br>
 * <A HREF="doc-files/ExampleRankOrder02.html"><B>View Example</B></A> <br>
 * <A HREF="doc-files/ExampleRankOrder03.html"><B>View Example</B></A> <br>
 * <A HREF="doc-files/ExampleRankOrder04.html"><B>View Example</B></A> <br>
 * <A HREF="doc-files/ExampleRankOrder05.html"><B>View Example</B></A> <br>
 * <A HREF="doc-files/ExampleRankOrder06.html"><B>View Example</B></A> <br>
 * <A HREF="doc-files/ExampleRankOrder07.html"><B>View Example</B></A> <br>
 * <A HREF="doc-files/ExampleRankOrder08.html"><B>View Example</B></A>
 * 
 * @author Tommy E. Cathey
 * @version $Id: RankOrderPlot.java,v 1.4 2006/12/11 22:16:39 parthee Exp $
 * 
 */
public class RankOrderPlot extends Plot implements Serializable, Cloneable, AnalysisOptionConstantsIfc {
	/** serial version UID */
	static final long serialVersionUID = 1;

	/** the PlotInfo for this Plot */
	private static PlotInfo plotInfo = null;

	/**
	 * List of all data keys which were set in {@link RankOrderPlot#setDataSetKeys(Object[])}
	 */
	private ArrayList keyList = new ArrayList();

	/** data keys associated with this Plot */
	private Object[] keys;

	/*******************************************************************************************************************
	 * Creates a new RankOrderPlot object.
	 ******************************************************************************************************************/
	public RankOrderPlot() {
		if (plotInfo == null) {
			initPlotInfo();
		}
	}

	/*******************************************************************************************************************
	 * retrieve a List of all data keys which were set in {@link RankOrderPlot#setDataSetKeys(Object[])}
	 * 
	 * @return List of all data keys
	 ******************************************************************************************************************/
	public List getDataKeyList() {
		return (ArrayList) keyList.clone();
	}

	/*******************************************************************************************************************
	 * Set the data set keys for this plot.
	 * 
	 * @param keys
	 *            Object[] the length of this array should equal the number of DataSetInfos for this plot Each element
	 *            in the array could have one of three types.
	 *            <ol>
	 *            <li> null - no data set was specified (valid if minimum number is 0)
	 *            <li> String - one data set was specified, this is its key
	 *            <li> String[] - multiple data sets were specified. This is an array of their keys
	 *            </ol>
	 * 
	 * @exception IllegalArgumentException
	 *                thrown if the wrong number of data sets were specified
	 ******************************************************************************************************************/
	public void setDataSetKeys(Object[] keys) throws IllegalArgumentException {
		if (keys.length != plotInfo.getNumDataSetInfo()) {
			StringBuffer b = new StringBuffer(300);
			b.append("This plot requires " + plotInfo.getNumDataSetInfo());
			b.append(" types of data for creation, but " + keys.length);
			b.append(" were provided");
			throw new IllegalArgumentException(b.toString());
		}

		DataSetInfo[] dsInfos = plotInfo.getDataSetInfo();

		for (int i = 0; i < keys.length; ++i) {
			Object obj = keys[i];

			try {
				if (obj == null) {
					dsInfos[i].validateNumber(0);
				}

				if (obj instanceof String[]) {
					keys[i] = (String[]) ((String[]) obj).clone();

					// verify that the correct number of each type of data set
					// was provided
					dsInfos[i].validateNumber(((String[]) keys[i]).length);

					// throw exception if bad #
				}
			} catch (Exception exc) {
				throw new IllegalArgumentException(exc.getMessage());
			}
		}

		this.keys = (Object[]) keys.clone();
		generateKeyList();
	}

	/*******************************************************************************************************************
	 * retrieve the i-th data keys which were set in {@link RankOrderPlot#setDataSetKeys(Object[])}
	 * 
	 * @param i
	 *            index into the Object[] pasted to {@link RankOrderPlot#setDataSetKeys(Object[])}
	 * 
	 * @return array of data keys which are associated with i-th index
	 ******************************************************************************************************************/
	public String[] getKeys(int i) {
		if ((i < 0) || (i >= keys.length)) {
			throw new IllegalArgumentException("i is out of range");
		}

		String[] rtrn = null;
		Object obj = keys[i];

		if ((obj instanceof String)) {
			//
			// wrap it in a String array before returning it
			//
			rtrn = new String[] { (String) obj };
		} else if (obj instanceof String[]) {
			//
			// return a clone
			//
			rtrn = (String[]) ((String[]) obj).clone();
		}

		return rtrn;
	}

	/*******************************************************************************************************************
	 * retrieve the PlotInfo obj for this plot
	 * 
	 * @return the PlotInfo obj for this plot
	 ******************************************************************************************************************/
	public static PlotInfo getPlotInfo() {
		if (plotInfo == null) {
			initPlotInfo();
		}

		return plotInfo;
	}

	/*******************************************************************************************************************
	 * retrieve plot name as a String
	 * 
	 * @return plot name
	 ******************************************************************************************************************/
	public String getName() {
		return AnalysisEngineConstants.RANK_ORDER_PLOT;
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
			RankOrderPlot clone = (RankOrderPlot) super.clone();
			clone.keys = (Object[]) keys.clone();
			clone.keyList = (ArrayList) keyList.clone();

			return clone;
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

	/**
	 * create the data set keys from an array of dataSets then call setDataSetKeys
	 * 
	 * @param dataSets
	 *            an ordered list of DataSets to create the keys from
	 * @throws Exception
	 *             if((xkey.size() != 1) && (xkey.size() != 0))
	 */
	public void createDataSetKeys(ArrayList dataSets) throws Exception {
		Vector dsKeys = (Vector) dataSets.get(0);
		String[] keyList = new String[dsKeys.size()];
		Iterator keyIt = dsKeys.iterator();
		int i = 0;
		while (keyIt.hasNext()) {
			String key = keyIt.next().toString();

			// reverse the order so they appear in the plot in the correct order
			// keyList[dsKeys.size() - 1 - i] = key;
			keyList[i] = key;
			i++;
		}

		Object[] retVal = new Object[1];
		retVal[0] = keyList;
		setDataSetKeys(retVal);
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
			RankOrderPlot other = (RankOrderPlot) o;

			rtrn = ((plotInfo == null) ? (other.plotInfo == null) : (plotInfo.equals(other.plotInfo)));

			if ((keys == null) && (other.keys != null)) {
				rtrn = false;
			} else if ((keys != null) && (other.keys == null)) {
				rtrn = false;
			} else if ((keys != null) && (other.keys != null)) {
				if (keys.length != other.keys.length) {
					rtrn = false;
				} else {
					for (int i = 0; i < keys.length; ++i) {
						if (!(keys[i].equals(other.keys[i]))) {
							rtrn = false;
						}
					}
				}
			}

			rtrn = rtrn && Util.equals(keyList, other.keyList);
		}

		return rtrn;
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
	 * convert all the keys in keys[] into an single List called keyList
	 ******************************************************************************************************************/
	private void generateKeyList() {
		keyList.clear();

		for (int i = 0; i < keys.length; ++i) {
			Object obj = keys[i];

			if (obj == null) {
				continue;
			} else if (obj instanceof String) {
				keyList.add(obj);
			} else if (obj instanceof String[]) {
				for (int j = 0; j < ((String[]) obj).length; ++j) {
					keyList.add(((String[]) obj)[j]);
				}
			} else {
				throw new AnalysisException("Unknown key Entry");
			}
		}
	}

	/*******************************************************************************************************************
	 * initialize the PlotInfo obj for this plot
	 ******************************************************************************************************************/
	private static void initPlotInfo() {
		//
		// all valid keywords for this plot
		//
		ArrayList allKeywordsList = new ArrayList();
		allKeywordsList.add(PLOT_TITLE);
		allKeywordsList.add(PLOT_SUBTITLE);
		allKeywordsList.add(PLOT_FOOTER);

		// allKeywordsList.add(WORLD_COORDINATES);
		allKeywordsList.add(LEGEND);
		allKeywordsList.add(SORT_TYPE);
		allKeywordsList.add(X_NUMERIC_AXIS);
		allKeywordsList.add(Y_NUMERIC_AXIS);
		allKeywordsList.add(LINE_TYPE);
		allKeywordsList.add(OUTLINE_TYPE);
		allKeywordsList.add(DISPLAY_SIZE_TYPE);
		allKeywordsList.add(TEXT_BOXES);
		//
		// convert keywords List into an array
		//
		String[] allKeywords = new String[allKeywordsList.size()];
		allKeywordsList.toArray(allKeywords);

		//
		// initialize the DataSetInfo object
		// NO x values
		//
		// y values have
		// minimum of 1 and a maximum of "no limit" thus -1 --> no limit
		//
		DataSetInfo[] dataSetInfos = new DataSetInfo[1];
		dataSetInfos[0] = new DataSetInfo("Y data series", DataSetIfc.class, 1, -1);

		//
		// initialize the PlotInfo object
		//
		plotInfo = new PlotInfo(AnalysisEngineConstants.RANK_ORDER_PLOT, RankOrderPlot.class, allKeywords, dataSetInfos);
	}

}