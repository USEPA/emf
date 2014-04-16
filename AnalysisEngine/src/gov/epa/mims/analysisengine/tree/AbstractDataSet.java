package gov.epa.mims.analysisengine.tree;

import java.io.Serializable;

/**
 * abstract base class for all data sets
 * 
 * @author Tommy E. Cathey
 * 
 * @created July 30, 2004
 * 
 * @version $Id: AbstractDataSet.java,v 1.3 2007/05/22 20:57:25 qunhe Exp $
 * 
 */

public abstract class AbstractDataSet

implements DataSetIfc,

Serializable,

Cloneable

{

	/** serial version UID */

	final static long serialVersionUID = 1;

	/** description of contents, e.g. for axis label */

	private String contentDescription = "";

	/** name of data set */

	private String name;

	/** data units */

	private String units = "";

	/** reference count for the number of opens */

	private transient int numUnmatchedOpens = 0;

	/**
	 * 
	 * The content description is the default value for what would appear on the
	 * 
	 * Y axis, followed by the units. e.g. "Average exposure"
	 * 
	 * 
	 * 
	 * @param contentDescription
	 *            String a description of the contents of the
	 * 
	 * data set.
	 * 
	 */

	public void setContentDescription(String contentDescription)

	{

		this.contentDescription = contentDescription;

	}

	/**
	 * 
	 * The content description is the default value for what would appear on the
	 * 
	 * Y axis, followed by the units. e.g. "Average exposure"
	 * 
	 * 
	 * 
	 * @return String a description of the contents of the data set.
	 * 
	 */

	public String getContentDescription()

	{

		return contentDescription;

	}

	/**
	 * 
	 * set name for this data set
	 * 
	 * 
	 * 
	 * @param arg
	 *            the name to use for this data set
	 * 
	 * @pre arg != null
	 * 
	 */

	public void setName(java.lang.String arg)

	{

		this.name = arg;

	}

	/**
	 * 
	 * retrieve name of this data set
	 * 
	 * 
	 * 
	 * @return the user seleceted name for this data set
	 * 
	 * @pre name != null
	 * 
	 */

	public java.lang.String getName()

	{

		return name;

	}

	/**
	 * 
	 * retrieve the number of unmatched opens
	 * 
	 * 
	 * 
	 * @return the number of unmatched opens
	 * 
	 */

	public int getNumUnmatchedOpens()

	{

		return numUnmatchedOpens;

	}

	/**
	 * 
	 * set the units of this data set
	 * 
	 * 
	 * 
	 * @param arg
	 *            data units for this data
	 * 
	 * @pre arg != null
	 * 
	 */

	public void setUnits(java.lang.String arg)

	{

		this.units = arg;

	}

	/**
	 * 
	 * get the units of this data set
	 * 
	 * 
	 * 
	 * @return the user set data units
	 * 
	 */

	public java.lang.String getUnits()

	{

		return units;

	}

	/**
	 * 
	 * Creates and returns a copy of this object
	 * 
	 * 
	 * 
	 * @return a copy of this object
	 * 
	 * @throws java.lang.CloneNotSupportedException
	 *             if super class is not
	 * 
	 * clonable
	 * 
	 */

	public Object clone()

	throws java.lang.CloneNotSupportedException

	{

		AbstractDataSet clone = (AbstractDataSet) super.clone();

		clone.numUnmatchedOpens = 0;

		return clone;

	}

	/**
	 * 
	 * close the data series
	 * 
	 * 
	 * 
	 * @pre numUnmatchedOpens >= 0
	 * 
	 * @throws java.lang.Exception
	 *             if data series is not open
	 * 
	 */

	public void close()

	throws java.lang.Exception

	{

		if (numUnmatchedOpens == 0)

		{

			throw new Exception("data series is not open");

		}

		numUnmatchedOpens--;

	}

	/**
	 * 
	 * Compares this object to the specified object.
	 * 
	 * 
	 * 
	 * @param o
	 *            the object to compare this object against
	 * 
	 * @return true if the objects are equal; false otherwise
	 * 
	 */

	public boolean equals(Object o)

	{

		boolean rtrn = true;

		if (o == null)

		{

			rtrn = false;

		}

		else if (o == this)

		{

			rtrn = true;

		}

		else if (o.getClass() != getClass())

		{

			rtrn = false;

		} else {
			AbstractDataSet other = (AbstractDataSet) o;

			if (name == null)
				rtrn = other.name == null && units.equals(other.units)
						&& (numUnmatchedOpens == other.numUnmatchedOpens);
			else
				rtrn = name.equals(other.name) && units.equals(other.units)
						&& (numUnmatchedOpens == other.numUnmatchedOpens);
		}

		return rtrn;
	}

	/**
	 * 
	 * open the data series
	 * 
	 * 
	 * 
	 * @pre numUnmatchedOpens >= 0
	 * 
	 * @throws java.lang.Exception
	 *             never
	 * 
	 */

	public void open()

	throws java.lang.Exception

	{

		numUnmatchedOpens++;

	}

	/**
	 * 
	 * describe object in a String
	 * 
	 * 
	 * 
	 * @return String describing object
	 * 
	 */

	public String toString()

	{

		return Util.toString(this);

	}

}
