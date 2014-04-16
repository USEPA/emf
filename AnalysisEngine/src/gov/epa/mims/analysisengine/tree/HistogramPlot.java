package gov.epa.mims.analysisengine.tree;

import gov.epa.mims.analysisengine.AnalysisEngineConstants;
import gov.epa.mims.analysisengine.AnalysisException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * HistogramPlot is Leaf Node which signals the generation of a Histogram Plot.
 * 
 * 
 * <p>
 * Elided Code Example:
 * 
 * <pre>
 *     :
 *     :
 *  //====================================================================
 *  //
 *  // initialize tree components
 *  //
 *  //====================================================================
 *  AnalysisOptions optionsGlobal = initAnalysisOptions();
 *  Page page = new Page();
 *  HistogramPlot histogramPlot = new HistogramPlot();
 *  dataSets = initDataSets(barPlot);
 * 
 * 
 *  //====================================================================
 *  //
 *  // build tree
 *  //
 *  //====================================================================
 *  //
 *  //
 *  //                     dataSets
 *  //                         |
 *  //                   optionsGlobal
 *  //                         |
 *  //                       page
 *  //                         |
 *  //                  histogramPlot
 *  //
 *  dataSets.add(optionsGlobal);
 *  optionsGlobal.add(page);
 *  page.add(histogramPlot);
 *     :
 *     :
 *     :
 *   **
 *   * initialize DataSets
 *   *
 *   * @param p HistogramPlot to associate data keys with
 *   *
 *   * @return initialized DataSet
 *   ********************************************************
 *  private DataSets initDataSets(HistogramPlot p)
 *  {
 *     DataSets dataSets = new DataSets();
 * 
 *     // store data; use data sets unique ID as an unique key name
 *     String key1 = &quot;key1&quot;;
 *     dataSets.add(initData(&quot;My data set 1&quot;,&quot;lds1&quot;, 6), key1);
 * 
 *     p.setDataSetKeys(
 *           new Object[]
 *     {
 *        new String[]
 *        {
 *           key1
 *        }
 *     });
 * 
 *     return dataSets;
 *  }
 *     :
 *     :
 *     :
 *   **
 *   * create and initialize a LabeledDoubleSeries
 *   *
 *   * @param seriesName name of the data set
 *   * @param labelPrefix prefix to use in label generation
 *   * The label name is generated as:
 *   * String labelName = labelPrefix + i;
 *   * @param count number of elements to generate
 *   *
 *   * @return initialized LabeledDoubleSeries
 *   ********************************************************
 *  private LabeledDoubleSeries initData(String seriesName, String labelPrefix,
 *     int count)
 *  {
 *     LabeledDoubleSeries lds = new LabeledDoubleSeries();
 *     lds.setName(seriesName);
 * 
 *     for (int i = 0; i &lt; count; ++i)
 *     {
 *        String labelName = labelPrefix + i;
 *        double value = Math.random() * 10.0;
 *        lds.addData(value, labelName);
 *     }
 * 
 *     return lds;
 *  }
 * 
 * </pre>
 * 
 * <p>
 * <A HREF="doc-files/ExampleHistogramPlot01.html"> <B>ExampleHistogramPlot01.html</B></A>
 * 
 * 
 * @author Tommy E. Cathey
 * @version $Id: HistogramPlot.java,v 1.3 2006/10/30 21:43:50 parthee Exp $
 * 
 */
public class HistogramPlot extends Plot implements Serializable, Cloneable, AnalysisOptionConstantsIfc {
	/** serial version UID */
	static final long serialVersionUID = 1;

	/** the PlotInfo for this Plot */
	private static PlotInfo plotInfo = null;

	/**
	 * List of all data keys which were set in {@link HistogramPlot#setDataSetKeys(Object[])}
	 */
	private ArrayList keyList = new ArrayList();

	/** data keys associated with this Plot */
	private Object[] keys = null;

	/*******************************************************************************************************************
	 * Creates a new HistogramPlot object.
	 ******************************************************************************************************************/
	public HistogramPlot() {
		if (plotInfo == null) {
			initPlotInfo();
		}
	}

	/*******************************************************************************************************************
	 * retrieve a List of data keys
	 * 
	 * @return List of data keys
	 ******************************************************************************************************************/
	public List getDataKeyList() {
		if (keys == null) {
			throw new AnalysisException("keys not initialized");
		}

		return (ArrayList) keyList.clone();
	}

	/**
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
	 *            <p>
	 *            Elided Code Example:
	 * 
	 * <pre>
	 *     :
	 *     :
	 *  private DataSets initDataSets(HistogramPlot p)
	 *  {
	 *     DataSets dataSets = new DataSets();
	 * 
	 *      // store data; use data sets unique ID as an unique key name
	 *      String key1 = &quot;key1&quot;;
	 *     String key2 = &quot;key2&quot;;
	 *     String key3 = &quot;key3&quot;;
	 *     String key4 = &quot;key4&quot;;
	 *     dataSets.add(initData(&quot;My data set 1&quot;,&quot;lds1&quot;, 3), key1);
	 *     dataSets.add(initData(&quot;My data set 2&quot;,&quot;lds1&quot;, 3), key2);
	 *     dataSets.add(initData(&quot;My data set 3&quot;,&quot;lds1&quot;, 3), key3);
	 * 
	 *     p.setDataSetKeys(
	 *           new Object[]
	 *     {
	 *        new String[]
	 *        {
	 *           key1,
	 *           key2,
	 *           key3
	 *        }
	 *     });
	 * 
	 *     return dataSets;
	 *  }
	 *     :
	 *     :
	 *  private LabeledDoubleSeries initData(String seriesName, String labelPrefix,
	 *     int count)
	 *  {
	 *     LabeledDoubleSeries lds = new LabeledDoubleSeries();
	 *     lds.setName(seriesName);
	 * 
	 *        for (int i = 0; i &lt; count; ++i)
	 *        {
	 *           String labelName = labelPrefix + i;
	 *           double value = Math.random() * 10.0;
	 *           lds.addData(value, labelName);
	 *        }
	 * 
	 *        return lds;
	 *  }
	 *     :
	 *     :
	 * </pre>
	 * 
	 * @exception IllegalArgumentException
	 *                thrown if the wrong number of data sets were specified
	 */
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

					// verify that the correct number of each type of data
					// set was provided
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
	 * retrieve a key by index
	 * 
	 * @param i
	 *            index of key to retrieve
	 * 
	 * @return key with index i
	 ******************************************************************************************************************/
	public String[] getKeys(int i) {
		if ((i < 0) || (i >= keys.length)) {
			throw new IllegalArgumentException("i is out of range");
		}

		String[] rtrn = null;
		Object obj = keys[i];

		if ((obj instanceof String)) {
			rtrn = new String[] { (String) obj };
		} else if (obj instanceof String[]) {
			rtrn = (String[]) ((String[]) obj).clone();
		}

		return rtrn;
	}

	/*******************************************************************************************************************
	 * retrieve plot info for this plot
	 * 
	 * @return plot info for this plot
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
		return AnalysisEngineConstants.HISTOGRAM_PLOT;
	}

	/*******************************************************************************************************************
	 * accept a Node visitor
	 * 
	 * @param v
	 *            visitor object
	 * @pre v != null
	 ******************************************************************************************************************/
	public void accept(VisitorIfc v) {
		v.visit(this);
	}

	/*******************************************************************************************************************
	 * cloning method
	 * 
	 * @return clone of this object
	 ******************************************************************************************************************/
	public Object clone() {
		try {
			HistogramPlot clone = (HistogramPlot) super.clone();
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
	 */
	public void createDataSetKeys(ArrayList dataSets) {
		Vector keys = (Vector) dataSets.get(0);
		String[] keyList = new String[keys.size()];
		Iterator keyIt = keys.iterator();
		int i = 0;
		while (keyIt.hasNext()) {
			String key = keyIt.next().toString();

			// reverse the order so they appear in the plot in the correct order
			// keyList[keys.size() - 1 - i] = key;
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
			HistogramPlot other = (HistogramPlot) o;

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
	 * generate a List container of keys
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
	 * initialize the PlotInfo object
	 ******************************************************************************************************************/
	private static void initPlotInfo() {
		ArrayList allKeywordsList = new ArrayList();
		allKeywordsList.add(PLOT_TITLE);
		allKeywordsList.add(PLOT_SUBTITLE);
		allKeywordsList.add(PLOT_FOOTER);
		allKeywordsList.add(HISTOGRAM_TYPE);

		allKeywordsList.add(X_NUMERIC_AXIS);
		allKeywordsList.add(Y_NUMERIC_AXIS);
		allKeywordsList.add(OUTLINE_TYPE);
		allKeywordsList.add(DISPLAY_SIZE_TYPE);
		allKeywordsList.add(TEXT_BOXES);
		// page type is part of the page, not the plot
		// allKeywordsList.add(PAGE_TYPE);
		String[] allKeywords = new String[allKeywordsList.size()];
		allKeywordsList.toArray(allKeywords);

		DataSetInfo[] dataSetInfos = new DataSetInfo[1];

		String s = "Histogram Data";
		dataSetInfos[0] = new DataSetInfo(s, DataSetIfc.class, 1, 1);
		plotInfo = new PlotInfo(AnalysisEngineConstants.HISTOGRAM_PLOT, HistogramPlot.class, allKeywords, dataSetInfos);
	}
}