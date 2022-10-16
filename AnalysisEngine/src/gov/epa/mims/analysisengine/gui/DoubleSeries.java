package gov.epa.mims.analysisengine.gui;

import gov.epa.mims.analysisengine.tree.AbstractDataSet;
import gov.epa.mims.analysisengine.tree.DataSetIfc;
import gov.epa.mims.analysisengine.tree.Util;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.NoSuchElementException;

/***********************************************************************************************************************
 * class to hold Double series data
 * 
 * @author Tommy E. Cathey
 * @version $Id: DoubleSeries.java,v 1.3 2006/12/08 22:46:52 parthee Exp $
 * 
 **********************************************************************************************************************/
public class DoubleSeries extends AbstractDataSet implements DataSetIfc, Serializable, Cloneable {
	/** DOCUMENT_ME */
	static final long serialVersionUID = 1;

	/** double series data */
	private ArrayList dataArrayList = new ArrayList();

	/*******************************************************************************************************************
	 * DOCUMENT_ME
	 * 
	 * @param o
	 *            DOCUMENT_ME
	 * 
	 * @return DOCUMENT_ME
	 ******************************************************************************************************************/
	public boolean equals(Object o) {
		boolean rtrn = true;

		if (!super.equals(o)) {
			rtrn = false;
		} else {
			DoubleSeries other = (DoubleSeries) o;
			rtrn = Util.equals(dataArrayList, other.dataArrayList);
		}

		return rtrn;
	}

	/*******************************************************************************************************************
	 * cloning method
	 * 
	 * @return clone of this object
	 ******************************************************************************************************************/
	public Object clone() {
		try {
			DoubleSeries clone = (DoubleSeries) super.clone();
			clone.dataArrayList = (ArrayList) dataArrayList.clone();

			return clone;
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

	/*******************************************************************************************************************
	 * store the data
	 * 
	 * @param data
	 *            the actual data for this data series
	 * @data != null
	 ******************************************************************************************************************/
	public void setData(double[] data) {
		for (int i = 0; i < data.length; i++) {
			dataArrayList.add(Double.valueOf(data[i]));
		}
	}

	/*******************************************************************************************************************
	 * add a data point to list of data
	 * 
	 * @param data
	 *            point
	 ******************************************************************************************************************/
	public void addData(double data) {
		dataArrayList.add(Double.valueOf(data));
	}

	/*******************************************************************************************************************
	 * retreive the data in this data series
	 * 
	 * @return the data
	 ******************************************************************************************************************/
	public double[] getData() {
		double[] temp = new double[dataArrayList.size()];

		for (int i = 0; i < temp.length; i++) {
			temp[i] = ((Double) dataArrayList.get(i)).doubleValue();
		}

		return temp;
	}

	/*******************************************************************************************************************
	 * retreive the i-th data element in this data series
	 * 
	 * @param i
	 *            index of requested element
	 * @throws java.lang.Exception
	 *             if DoubleSeries is not open
	 * @throws java.util.NoSuchElementException
	 *             if i is out of range
	 * @return the i-th data element
	 ******************************************************************************************************************/
	public double getElement(int i) throws java.util.NoSuchElementException, java.lang.Exception {
		if (super.getNumUnmatchedOpens() <= 0) {
			throw new Exception("DoubleSeries DataSet is not open");
		}

		if ((i < 0) || (i >= dataArrayList.size())) {
			throw new NoSuchElementException("" + i + " is an invalid index");
		}

		return ((Double) dataArrayList.get(i)).doubleValue();
	}

	/*******************************************************************************************************************
	 * get number of elements
	 * 
	 * @throws java.lang.Exception
	 *             if DoubleSeries is not open
	 * @return the number of data elements
	 ******************************************************************************************************************/
	public int getNumElements() throws java.lang.Exception {
		if (super.getNumUnmatchedOpens() <= 0) {
			throw new java.lang.Exception("DoubleSeries DataSet is not open");
		}

		return dataArrayList.size();
	}

	/*******************************************************************************************************************
	 * describe object in a String
	 * 
	 * @return String describing object
	 ******************************************************************************************************************/
	public String toString() {
		return this.getName();
	}
}
