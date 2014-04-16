package gov.epa.mims.analysisengine.tree;

import gov.epa.mims.analysisengine.AnalysisEngineConstants;
import gov.epa.mims.analysisengine.AnalysisException;
import gov.epa.mims.analysisengine.tree.DateDataSetIfc;

import java.io.Serializable;
import java.util.*;

/**
 * Time Series Plot Leaf Node
 * 
 * @author Tommy E. Cathey
 * @version $Id: TimeSeries.java,v 1.3 2006/12/11 22:16:39 parthee Exp $
 * 
 * <br>
 * <A HREF="doc-files/ExampleTimeSeriesAxisGrid15.html"><B>View Example</B></A> <br>
 * <A HREF="doc-files/ExampleTimeSeriesAxisGrid16.html"><B>View Example</B></A> <br>
 * <A HREF="doc-files/ExampleTimeSeriesAxisGrid17.html"><B>View Example</B></A> <br>
 * <A HREF="doc-files/ExampleTimeSeriesAxisGrid18.html"><B>View Example</B></A> <br>
 * <A HREF="doc-files/ExampleTimeSeriesAxisGrid11.html"><B>View Example</B></A> <br>
 * <A HREF="doc-files/ExampleTimeSeriesAxisGrid12.html"><B>View Example</B></A> <br>
 * <A HREF="doc-files/ExampleTimeSeriesAxisGrid13.html"><B>View Example</B></A> <br>
 * <A HREF="doc-files/ExampleTimeSeriesAxisGrid14.html"><B>View Example</B></A> <br>
 * <A HREF="doc-files/ExampleTimeSeriesAxisGrid06.html"><B>View Example</B></A> <br>
 * <A HREF="doc-files/ExampleTimeSeriesAxisGrid07.html"><B>View Example</B></A> <br>
 * <A HREF="doc-files/ExampleTimeSeriesAxisGrid08.html"><B>View Example</B></A> <br>
 * <A HREF="doc-files/ExampleTimeSeriesAxisGrid09.html"><B>View Example</B></A> <br>
 * <A HREF="doc-files/ExampleTimeSeriesAxisGrid10.html"><B>View Example</B></A> <br>
 * <A HREF="doc-files/ExampleTimeSeriesAxisGrid01.html"><B>View Example</B></A> <br>
 * <A HREF="doc-files/ExampleTimeSeriesAxisGrid05.html"><B>View Example</B></A>
 */
public class TimeSeries extends Plot implements Serializable, Cloneable, AnalysisOptionConstantsIfc {
	/*******************************************************************************************************************
	 * 
	 * fields
	 * 
	 ******************************************************************************************************************/
	static final long serialVersionUID = 1;

	/** the PlotInfo for this Plot */
	private static PlotInfo plotInfo = null;

	/**
	 * List of all data keys which were set in {@link TimeSeries#setDataSetKeys(Object[])}
	 */
	private ArrayList keyList = new ArrayList();

	/** data keys associated with this Plot */
	private Object[] keys;

	// static initialization block
	{
	}

	/*******************************************************************************************************************
	 * Creates a new TimeSeries object.
	 ******************************************************************************************************************/
	public TimeSeries() {
		if (plotInfo == null) {
			initPlotInfo();
		}
	}

	/*******************************************************************************************************************
	 * retrieve a list of data keys
	 * 
	 * @return list of data keys
	 ******************************************************************************************************************/
	public List getDataKeyList() {
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
	 * 
	 * @exception IllegalArgumentException
	 *                thrown if the wrong number of data sets were specified
	 */
	public void setDataSetKeys(Object[] keys) throws IllegalArgumentException {
		if (keys.length != plotInfo.getNumDataSetInfo()) {
			StringBuffer b = new StringBuffer(300);
			b.append("A Bar Plot requires ");
			b.append(plotInfo.getNumDataSetInfo());
			b.append(" types of data for creation, but ");
			b.append(keys.length + " were provided");
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

					// verify that the correct number of each type of
					// data set was provided
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
	 * retrieve the i-th data keys which were set in {@link TimeSeries#setDataSetKeys(Object[])}
	 * 
	 * @param i
	 *            index into the Object[] pasted to {@link TimeSeries#setDataSetKeys(Object[])}
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
			rtrn = new String[] { (String) obj };
		} else if (obj instanceof String[]) {
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
		return AnalysisEngineConstants.TIME_SERIES_PLOT;
	}

	/**
	 * accept a Node visitor
	 * 
	 * @param v
	 *            visitor class
	 */
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
			TimeSeries clone = (TimeSeries) super.clone();
			clone.keys = (keys == null) ? null : (Object[]) keys.clone();
			clone.keyList = (keyList == null) ? null : (ArrayList) keyList.clone();

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
		// The length of the data set should be equal
		// The data set labels should be unique and matching accross the dataset
		// and in an asceding order

		if (dataSets.size() > 1) {
			throw new Exception("No of DataSets(each DataSets contains set of Double Series) > 1 ");
		}

		/**
		 * Vector data = ((DataSets)dataSets.get(0)).getDataSets(null,null); //checking the length int currLength = -1;
		 * Date prevDate = null; Date dateK = new Date((long)-1e14); //just set it to a very low value(Thu Feb 26
		 * 09:13:20 EST 1200) Date dateI = null; boolean checkLength = true; DateDataSetIfc dateSetIfc0 =
		 * (DateDataSetIfc)data.get(0); dateSetIfc0.open(); int length0 = dateSetIfc0.getNumElements();
		 * 
		 * for(int k=0; k< length0; k++) { prevDate = dateK; dateK = dateSetIfc0.getDate(k);
		 * if(!prevDate.before(dateK)) { throw new Exception("The dates in a date dataset should be in an " + "ascending
		 * order" ); }//if(!prevDate.before(dateK))
		 * 
		 * for(int m=1; m< data.size(); m++) { DateDataSetIfc dateSetIfc = (DateDataSetIfc)data.get(m);
		 * dateSetIfc.open(); if(checkLength) { currLength = dateSetIfc.getNumElements(); if(currLength != length0) {
		 * throw new Exception("The number of elements in datasets, \"" + dateSetIfc.getName() +"\",
		 * \""+dateSetIfc0.getName()+ "\" are not equal." ); }//if(currLength != prevLength) }//if(checkLength) dateI =
		 * dateSetIfc.getDate(k); if(!dateK.equals(dateI)) { throw new Exception("The date labels for the selected
		 * datasets are not " + "same across the datasets"); } dateSetIfc.close(); }//for(m) checkLength = false;
		 * }//for(k) dateSetIfc0.close();
		 */
		Vector dsKeys = (Vector) dataSets.get(0);
		String[] keyList = new String[dsKeys.size()];
		Iterator keyIt = dsKeys.iterator();
		int i = 0;
		int numKeys = dsKeys.size();
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
			TimeSeries other = (TimeSeries) o;

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
	 * generate a List of all data keys
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
		ArrayList allKeywordsList = new ArrayList();
		allKeywordsList.add(PLOT_TITLE);
		allKeywordsList.add(PLOT_SUBTITLE);
		allKeywordsList.add(PLOT_FOOTER);

		// allKeywordsList.add(WORLD_COORDINATES);
		allKeywordsList.add(LEGEND);
		allKeywordsList.add(X_TIME_AXIS);
		allKeywordsList.add(Y_NUMERIC_AXIS);
		allKeywordsList.add(LINE_TYPE);
		allKeywordsList.add(OUTLINE_TYPE);
		allKeywordsList.add(DISPLAY_SIZE_TYPE);
		allKeywordsList.add(TEXT_BOXES);

		// Page type belongs with the Page, not the Plot
		// allKeywordsList.add(PAGE_TYPE);
		String[] allKeywords = new String[allKeywordsList.size()];
		allKeywordsList.toArray(allKeywords);

		DataSetInfo[] dataSetInfos = new DataSetInfo[1];
		dataSetInfos[0] = new DataSetInfo("Time Series Data", DateDataSetIfc.class, 1, -1);
		plotInfo = new PlotInfo(AnalysisEngineConstants.TIME_SERIES_PLOT, TimeSeries.class, allKeywords, dataSetInfos);
	}
}