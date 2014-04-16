package gov.epa.mims.analysisengine.tree;

import gov.epa.mims.analysisengine.tree.DataSetIfc;

import java.awt.Component;

import java.util.Set;
import java.util.Vector;

/**
 * An interface that contains the available data sets in the DataSets node if one was provided. If no DataSets node was
 * provided or it was null, then the DataSetsAdapter would get dummy data sets and display them.
 * 
 * @author Prashant Pai
 * @version $Id: DataSetsAdapter.java,v 1.3 2006/12/08 22:46:52 parthee Exp $
 * 
 */
public interface DataSetsAdapter {
	/*******************************************************************************************************************
	 * retreive a data set using its key
	 * 
	 * @param key
	 *            the key corresponding to the desired data set
	 * @return data set corresponding to the key or null if not found
	 * @pre key != null
	 * 
	 ******************************************************************************************************************/
	public DataSetIfc getDataSet(Object key);

	/**
	 * @param type
	 *            Class that specifies the type of data set. If = null, return all data set keys.
	 * @param owner
	 *            Component window that owns this adapter (to set owner of dialog used in calling application, if any
	 *            are needed)
	 * @return Vector of Objects that are keys to datasets with the specified type
	 */
	public Vector getDataSetKeys(Class type, Component owner);

	/**
	 * @param type
	 *            Class that specifies the type of data set. If = null, return all data sets.
	 * @param owner
	 *            Component window that owns this adapter (to set owner of dialog used in calling application, if any
	 *            are needed)
	 * @return Vector of DataSetIfcs with the specified type
	 */
	public Vector getDataSets(Class type, Component owner);

	/**
	 * The description would be what the data sets have in common, and the default value for the text on the X axis.
	 * (e.g. "Person", "Chemical", "Year")
	 * 
	 * @return String a description of these data sets
	 */
	public String getDataSetsDescription();

	/*******************************************************************************************************************
	 * get all keys
	 * 
	 * @return set containing all the keys
	 ******************************************************************************************************************/
	public Set getKeys();

	/**
	 * Add a single data set to the adapter
	 * 
	 * @param dataSet
	 *            the DataSetIfc to add
	 * @param key
	 *            the Object representing the key for the data set
	 * @throws IllegalArgumentException
	 *             if something went wrong
	 */
	public void add(DataSetIfc dataSet, Object key) throws IllegalArgumentException;

	/**
	 * Add data sets to the current set of data sets known by the adapter.
	 * 
	 * @param dataSets
	 *            the DataSetIfc [] to add
	 * @param key
	 *            the Object [] representing the keys for the data set
	 * @throws IllegalArgumentException
	 *             if something went wrong
	 */
	public void addDataSets(DataSetIfc[] datasets, Object[] keys) throws IllegalArgumentException;
}