package gov.epa.mims.analysisengine.tree;

import gov.epa.mims.analysisengine.AnalysisEngineConstants;
import gov.epa.mims.analysisengine.AnalysisException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * BarPlot is Leaf Node which signals the generation of a Bar Plot.
 * 
 * 
 * <p>
 * Elided Code Example:
 * 
 * <pre>
 *        :
 *        :
 *     //====================================================================
 *     //
 *     // initialize tree components
 *     //
 *     //====================================================================
 *     AnalysisOptions optionsGlobal = initAnalysisOptions();
 *     Page page = new Page();
 *     BarPlot barPlot = new BarPlot();
 *     dataSets = initDataSets(barPlot);
 *    
 *    
 *     //====================================================================
 *     //
 *     // build tree
 *     //
 *     //====================================================================
 *     //
 *     //
 *     //                     dataSets
 *     //                         |
 *     //                   optionsGlobal
 *     //                         |
 *     //                       page
 *     //                         |
 *     //                      barPlot
 *     //
 *     dataSets.add(optionsGlobal);
 *     optionsGlobal.add(page);
 *     page.add(barPlot);
 *        :
 *        :
 *        :
 *      **
 *      * initialize DataSets
 *      *
 *      * @param p BarPlot to associate data keys with
 *      *
 *      * @return initialized DataSet
 *      ********************************************************
 *     private DataSets initDataSets(BarPlot p)
 *     {
 *        DataSets dataSets = new DataSets();
 *    
 *        // store data; use data sets unique ID as an unique key name
 *        String key1 = &quot;key1&quot;;
 *        dataSets.add(initData(&quot;My data set 1&quot;,&quot;lds1&quot;, 6), key1);
 *    
 *        p.setDataSetKeys(
 *              new Object[]
 *        {
 *           new String[]
 *           {
 *              key1
 *           }
 *        });
 *    
 *        return dataSets;
 *     }
 *        :
 *        :
 *        :
 *      **
 *      * create and initialize a LabeledDoubleSeries
 *      *
 *      * @param seriesName name of the data set
 *      * @param labelPrefix prefix to use in label generation
 *      * The label name is generated as:
 *      * String labelName = labelPrefix + i;
 *      * @param count number of elements to generate
 *      *
 *      * @return initialized LabeledDoubleSeries
 *      ********************************************************
 *     private LabeledDoubleSeries initData(String seriesName, String labelPrefix,
 *        int count)
 *     {
 *        LabeledDoubleSeries lds = new LabeledDoubleSeries();
 *        lds.setName(seriesName);
 *    
 *        for (int i = 0; i &lt; count; ++i)
 *        {
 *           String labelName = labelPrefix + i;
 *           double value = Math.random() * 10.0;
 *           lds.addData(value, labelName);
 *        }
 *    
 *        return lds;
 *     }
 *    
 * </pre>
 * 
 * <p>
 * <A HREF="doc-files/ExampleBarPlot01.html"><B>ExampleBarPlot01.html</B></A>
 * <p>
 * <A HREF="doc-files/ExampleBarPlot02.html"><B>ExampleBarPlot02.html</B></A>
 * <p>
 * <A HREF="doc-files/ExampleBarPlot03.html"><B>ExampleBarPlot03.html</B></A>
 * 
 * 
 * @author Tommy E. Cathey
 * @version $Id: BarPlot.java,v 1.5 2006/12/11 22:16:39 parthee Exp $
 * 
 */
public class BarPlot extends Plot implements Serializable, Cloneable, AnalysisOptionConstantsIfc {
	/** serial version UID */
	static final long serialVersionUID = 1;

	/** the PlotInfo for this Plot */
	private static PlotInfo plotInfo = null;

	/**
	 * List of all data keys which were set in {@link BarPlot#setDataSetKeys(Object[])}
	 */
	private ArrayList keyList = new ArrayList();

	/** data keys associated with this Plot */
	private Object[] keys = null;

	/**
	 * maximum no of elements allowed per dataset in a barplot
	 */
	public static final int MAX_ELEMENTS = 200;

	public BarPlot() {
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
	 *        :
	 *        :
	 *     private DataSets initDataSets(BarPlot p)
	 *     {
	 *        DataSets dataSets = new DataSets();
	 *    
	 *         // store data; use data sets unique ID as an unique key name
	 *         String key1 = &quot;key1&quot;;
	 *        String key2 = &quot;key2&quot;;
	 *        String key3 = &quot;key3&quot;;
	 *        String key4 = &quot;key4&quot;;
	 *        dataSets.add(initData(&quot;My data set 1&quot;,&quot;lds1&quot;, 3), key1);
	 *        dataSets.add(initData(&quot;My data set 2&quot;,&quot;lds1&quot;, 3), key2);
	 *        dataSets.add(initData(&quot;My data set 3&quot;,&quot;lds1&quot;, 3), key3);
	 *    
	 *        p.setDataSetKeys(
	 *              new Object[]
	 *        {
	 *           new String[]
	 *           {
	 *              key1,
	 *              key2,
	 *              key3
	 *           }
	 *        });
	 *    
	 *        return dataSets;
	 *     }
	 *        :
	 *        :
	 *     private LabeledDoubleSeries initData(String seriesName, String labelPrefix,
	 *        int count)
	 *     {
	 *        LabeledDoubleSeries lds = new LabeledDoubleSeries();
	 *        lds.setName(seriesName);
	 *    
	 *           for (int i = 0; i &lt; count; ++i)
	 *           {
	 *              String labelName = labelPrefix + i;
	 *              double value = Math.random() * 10.0;
	 *              lds.addData(value, labelName);
	 *           }
	 *    
	 *           return lds;
	 *     }
	 *        :
	 *        :
	 * </pre>
	 * 
	 * @exception IllegalArgumentException
	 *                thrown if the wrong number of data sets were specified
	 */
	public void setDataSetKeys(Object[] keys) throws IllegalArgumentException {
		if (keys.length != plotInfo.getNumDataSetInfo()) {
			StringBuffer b = new StringBuffer(300);
			b.append("A Bar Plot requires " + plotInfo.getNumDataSetInfo());
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
		return AnalysisEngineConstants.BAR_PLOT;
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
			BarPlot clone = (BarPlot) super.clone();
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
	public void createDataSetKeys(ArrayList dataSets) throws Exception {
		// System.out.println("the datasets size ="+dataSets.size());
		if (dataSets.size() > 1) {
			throw new Exception("Only one set of data sets should be provided to the Bar Plot, but " + dataSets.size()
					+ " were provided");
		}
		/*
		 * System.out.println("In createDataSetKeys() class ="+ dataSets.get(0).getClass()); Vector tmpDataSet =
		 * (Vector)dataSets.get(0); System.out.println("the tmpDataSet class = "+tmpDataSet.get(0).getClass()); Vector
		 * data = ((DataSets)tmpDataSet.get(0)).getDataSets(null, null); for(int i=0; i< data.size(); i++) {
		 * 
		 * LabeledDataSetIfc dateSetIfc = (LabeledDataSetIfc)data.get(i); dateSetIfc.open(); int length =
		 * dateSetIfc.getNumElements(); if(length > MAX_ELEMENTS) { throw new Exception("There were "+length+ " data
		 * points in one of the data sets passed to Bar Plot, but only "+ MAX_ELEMENTS + "can be plotted."); } }
		 */
		Vector dsKeys = (Vector) dataSets.get(0);
		String[] keyList = new String[dsKeys.size()];
		Iterator keyIt = dsKeys.iterator();
		int i = 0;
		while (keyIt.hasNext()) {
			String key = keyIt.next().toString();
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
			BarPlot other = (BarPlot) o;

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

		// allKeywordsList.add(WORLD_COORDINATES);
		allKeywordsList.add(BAR_TYPE);
		allKeywordsList.add(LEGEND);
		allKeywordsList.add(CATEGORY_AXIS);
		allKeywordsList.add(NUMERIC_AXIS);
		allKeywordsList.add(OUTLINE_TYPE);
		allKeywordsList.add(DISPLAY_SIZE_TYPE);
		allKeywordsList.add(TEXT_BOXES);

		// page type is part of the page, not the plot
		// allKeywordsList.add(PAGE_TYPE);
		String[] allKeywords = new String[allKeywordsList.size()];
		allKeywordsList.toArray(allKeywords);

		DataSetInfo[] dataSetInfos = new DataSetInfo[1];
		dataSetInfos[0] = new DataSetInfo("Bar Data Series", DataSetIfc.class, 1, -1);
		plotInfo = new PlotInfo(AnalysisEngineConstants.BAR_PLOT, BarPlot.class, allKeywords, dataSetInfos);
	}
}
