package gov.epa.mims.analysisengine.tree;

import java.io.Serializable;

/***********************************************************************************************************************
 * Data Set interface functions
 * 
 * @author Tommy E. Cathey
 * @version $Id: DataSetIfc.java,v 1.4 2007/05/22 20:57:26 qunhe Exp $
 * 
 **********************************************************************************************************************/
public interface DataSetIfc extends Serializable {
	/**
	 * The content description is the default value for what would appear on the Y axis, followed by the units. e.g.
	 * "Average exposure"
	 * 
	 * @return String a description of the contents of the data set.
	 */
	String getContentDescription();

	/*******************************************************************************************************************
	 * get data element as a double
	 * 
	 * @param i
	 *            index into data series
	 * @return element as a double
	 * @throws java.lang.Exception
	 *             if series is not open
	 * @throws java.util.NoSuchElementException
	 *             if i is out of range
	 ******************************************************************************************************************/
	double getElement(int i) throws java.lang.Exception, java.util.NoSuchElementException;

	/*******************************************************************************************************************
	 * get the name of the data set
	 * 
	 * @return String containing the name of the data set
	 ******************************************************************************************************************/
	String getName();

	/*******************************************************************************************************************
	 * get number of elements in this data set
	 * 
	 * @return number of elements
	 * @throws java.lang.Exception
	 *             if series is not open
	 ******************************************************************************************************************/
	int getNumElements() throws java.lang.Exception;

	/*******************************************************************************************************************
	 * get the units of the data set
	 * 
	 * @return String containing the units of the data set
	 ******************************************************************************************************************/
	String getUnits();

	/*******************************************************************************************************************
	 * close data series
	 * 
	 * @throws Exception
	 *             if close fails
	 ******************************************************************************************************************/
	void close() throws Exception;

	/*******************************************************************************************************************
	 * open data series
	 * 
	 * @throws Exception
	 *             if open fails
	 ******************************************************************************************************************/
	void open() throws Exception;
	
	boolean equals(Object other);
}