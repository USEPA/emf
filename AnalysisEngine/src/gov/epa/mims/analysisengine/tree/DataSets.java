package gov.epa.mims.analysisengine.tree;

import java.awt.Component;

import java.io.Serializable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

/***********************************************************************************************************************
 * class_description
 * 
 * @author Tommy E. Cathey
 * @version $Id: DataSets.java,v 1.3 2006/12/07 20:41:53 parthee Exp $
 * 
 **********************************************************************************************************************/
public class DataSets extends Branch implements Serializable, Cloneable, DataSetsAdapter {
	/** serial version UID */
	static final long serialVersionUID = 1;

	/** hash map of DataSet's and their keys */
	private HashMap dataSetHash = new HashMap();

	/**
	 * A description of what these DataSets are (e.g. "Model Output", "Year")
	 */
	private String description = "";

	/*******************************************************************************************************************
	 * retreive a data set
	 * 
	 * @param key
	 *            the key corresponding to the desired data set
	 * @return data set corresponding to the key or null if not found
	 * @pre key != null
	 * 
	 ******************************************************************************************************************/
	public DataSetIfc getDataSet(Object key) {
		if (dataSetHash.containsKey(key)) {
			return (DataSetIfc) dataSetHash.get(key);
		}

		return null;
	}

	/**
	 * @param type
	 *            Class that specifies the type of data set. If = null, return all data sets.
	 * @param owner
	 *            Component window that owns this adapter (to set owner of dialog used in calling application, if any
	 *            are needed)
	 * @return Vector of Objects that are keys to datasets with the specified type
	 */
	public Vector getDataSetKeys(Class type, Component owner) {
		Collection allKeys = dataSetHash.keySet();
		Iterator it = allKeys.iterator();
		Vector matchingKeys = new Vector();

		while (it.hasNext()) {
			Object currentKey = it.next();
			Object currentObject = dataSetHash.get(currentKey);

			if ((type == null) || type.isInstance(currentObject)) {
				// this will reverse the items and return them in the same order
				// as they were put in
				matchingKeys.add(0, currentKey);
			}
		}

		return matchingKeys;
	}

	/**
	 * @param type
	 *            Class that specifies the type of data set. If = null, return all data sets.
	 * @param owner
	 *            Component window that owns this adapter (to set owner of dialog used in calling application, if any
	 *            are needed)
	 * @return Vector of DataSetIfc's with the specified type
	 */
	public Vector getDataSets(Class type, Component owner) {
		Collection allDataSets = dataSetHash.values();
		Iterator it = allDataSets.iterator();
		Vector matchingDataSets = new Vector();

		while (it.hasNext()) {
			Object current = it.next();

			if ((type == null) || !type.isInstance(current)) {
				// this will reverse the items and return them in the same order
				// as they were put in
				matchingDataSets.add(0, current);
			}
		}

		return matchingDataSets;
	}

	/**
	 * The description would be what the data sets have in common, and the default value for the text on the X axis.
	 * (e.g. "Person", "Chemical", "Year")
	 * 
	 * @param description
	 *            String a description of these data sets
	 */
	public void setDataSetsDescription(String description) {
		this.description = description;
	}

	/**
	 * The description would be what the data sets have in common, and the default value for the text on the X axis.
	 * (e.g. "Person", "Chemical", "Year")
	 * 
	 * @return String a description of these data sets
	 */
	public String getDataSetsDescription() {
		return description;
	}

	/*******************************************************************************************************************
	 * get all keys
	 * 
	 * @return set containing all the keys
	 ******************************************************************************************************************/
	public Set getKeys() {
		Set keys = dataSetHash.keySet();

		return keys;
	}

	/*******************************************************************************************************************
	 * accept a Node visitor
	 * 
	 * @param v
	 *            visitor
	 * @pre v != null
	 ******************************************************************************************************************/
	public void accept(VisitorIfc v) {
		v.visit(this);
	}

	/*******************************************************************************************************************
	 * add a DataSet to dataSetHash map
	 * 
	 * @param dataSet
	 *            the data set object to store
	 * @param key
	 *            an unique key corresponding to the given data set
	 * @pre dataSet != null
	 * @pre key != null
	 * @throws IllegalArgumentException
	 *             if key is a duplicate
	 ******************************************************************************************************************/
	public void add(DataSetIfc dataSet, Object key) throws IllegalArgumentException {
		if (dataSetHash.get(key) != null) {
			throw new IllegalArgumentException(key + " is a duplicate key.");
		}

		dataSetHash.put(key, dataSet);
	}

	/**
	 * Add data sets to the current set of data sets known by the adapter. Typically you'd use the other add method that
	 * takes the key as an argument
	 * 
	 * @param datasets
	 *            DataSetIfc [] Datasets to add.
	 * @param keys
	 *            Object [] keys for each data set.
	 */
	public void addDataSets(DataSetIfc[] datasets, Object[] keys) {
		for (int i = 0; i < datasets.length; i++) {
			add(datasets[i], keys[i]);
		}
	}

	/**
	 * Remove all of the DataSets from this collection.
	 */
	public void clearDataSets() {
		if (dataSetHash != null) {
			dataSetHash.clear();
		}
	}

	/*******************************************************************************************************************
	 * Creates and returns a copy of this object
	 * 
	 * @return a copy of this object
	 ******************************************************************************************************************/
	public Object clone() {
		try {
			DataSets clone = (DataSets) super.clone();

			clone.dataSetHash = (clone.dataSetHash == null) ? null : (HashMap) dataSetHash.clone();

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
			DataSets other = (DataSets) o;

			rtrn = Util.equals(dataSetHash, other.dataSetHash);
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

	/**
	 * During serialization, remove the datasets temporarily and restore them after writing out the object.
	 * 
	 * @param out
	 *            ObjectOutputStream that is serializing the Object.
	 * @throws java.io.IOException
	 */
	private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
		HashMap tmpHash = (HashMap) dataSetHash.clone();
		clearDataSets();
		out.defaultWriteObject();
		dataSetHash = tmpHash;
	}
}