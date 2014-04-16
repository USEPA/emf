package gov.epa.mims.analysisengine.tree;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.List;

/***********************************************************************************************************************
 * abstract base class for all plots
 * 
 * @author Tommy E. Cathey
 * @version $Id: Plot.java,v 1.6 2007/05/31 14:29:31 qunhe Exp $
 * 
 **********************************************************************************************************************/
public abstract class Plot extends Leaf implements Serializable, Cloneable {
	/** serial version UID */
	static final long serialVersionUID = 1;

	/*******************************************************************************************************************
	 * get a data series
	 * 
	 * @param searchKey
	 *            Object which serves as look up key
	 * @return the object corresponding to given key null if not found
	 * @pre searchKey != null
	 ******************************************************************************************************************/
	public DataSetIfc getDataSet(Object searchKey) {
		DataSetIfc dataSeries;
		ArrayList dataSetsAncestors = findAncestors(DataSets.class);

		for (int i = 0; i < dataSetsAncestors.size(); i++) {
			dataSeries = ((DataSets) dataSetsAncestors.get(i)).getDataSet(searchKey);

			if (dataSeries != null) {
				return dataSeries;
			}
		}

		return null;
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
	public abstract void setDataSetKeys(Object[] keys) throws IllegalArgumentException;

	/*******************************************************************************************************************
	 * abstract method for retrieving the plot name as defined in gov.epa.mims.analysisengine.AnalysisEngineConstants
	 * 
	 * @return plot name
	 ******************************************************************************************************************/
	public abstract String getName();

	/*******************************************************************************************************************
	 * Creates and returns a copy of this object
	 * 
	 * @return a copy of this object
	 * @throws java.lang.CloneNotSupportedException
	 *             if not clonable
	 ******************************************************************************************************************/
	public Object clone() throws java.lang.CloneNotSupportedException {
		Plot clone = (Plot) super.clone();

		return clone;
	}

	/*******************************************************************************************************************
	 * abstract method to create data set keys
	 * 
	 * @param dataSets
	 *            data sets
	 * 
	 * @throws Exception
	 *             if not successful
	 ******************************************************************************************************************/
	// public abstract void createDataSetKeys(DataSets[] dataSets)
	// throws Exception;
	public abstract void createDataSetKeys(ArrayList dataSets) throws Exception;
	
	
	public abstract List getDataKeyList();
	
	public abstract String[] getKeys(int i);
	
	/****************************************************
	 * Added to check whether the plot is newly created
	 * @Date 5/30/2007
	 * @author Qun He
	 * @return a boolean value
	 ****************************************************/
	
	public boolean keysInitialized() {
		List keysList =  null;
		
		try {
			keysList = getDataKeyList();
		} catch (Exception e) {
			return false;
		}
		
		if (keysList == null || keysList.size() == 0)
			return false;
		
		return true;
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
}