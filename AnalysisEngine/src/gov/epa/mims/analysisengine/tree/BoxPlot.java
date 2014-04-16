package gov.epa.mims.analysisengine.tree;

import gov.epa.mims.analysisengine.AnalysisEngineConstants;
import gov.epa.mims.analysisengine.AnalysisException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * BoxPlot is Leaf Node which signals the generation of a Box Plot.
 * 
 * 
 * <p>
 * Elided Code Example:
 * 
 * <pre>
 *     :
 *     :
 * </pre>
 * 
 * 
 * @author Tommy E. Cathey
 * @version $Id: BoxPlot.java,v 1.3 2006/10/30 21:43:50 parthee Exp $
 * 
 */
public class BoxPlot extends Plot implements Serializable, Cloneable, AnalysisOptionConstantsIfc {
	/** serial version UID */
	static final long serialVersionUID = 1;

	/** the PlotInfo for this Plot */
	private static PlotInfo plotInfo = null;

	/**
	 * List of all data keys which were set in {@link BoxPlot#setDataSetKeys(Object[])}
	 */
	private ArrayList keyList = new ArrayList();

	/** data keys associated with this Plot */
	private Object[] keys = null;

	/*******************************************************************************************************************
	 * Creates a new BoxPlot object.
	 ******************************************************************************************************************/
	public BoxPlot() {
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

	/*******************************************************************************************************************
	 * DOCUMENT_ME
	 * 
	 * @param keys
	 *            DOCUMENT_ME
	 * 
	 * @throws IllegalArgumentException
	 *             DOCUMENT_ME
	 ******************************************************************************************************************/
	public void setDataSetKeys(Object[] keys) throws IllegalArgumentException {
		if (keys.length != plotInfo.getNumDataSetInfo()) {
			StringBuffer b = new StringBuffer(300);
			b.append("A Box Plot requires " + plotInfo.getNumDataSetInfo());
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
		return AnalysisEngineConstants.BOX_PLOT;
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
			BoxPlot clone = (BoxPlot) super.clone();
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
			BoxPlot other = (BoxPlot) o;

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
		allKeywordsList.add(BOX_TYPE);
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
		dataSetInfos[0] = new DataSetInfo("Box Data Series", DataSetIfc.class, 1, -1);
		plotInfo = new PlotInfo(AnalysisEngineConstants.BOX_PLOT, BoxPlot.class, allKeywords, dataSetInfos);
	}
}