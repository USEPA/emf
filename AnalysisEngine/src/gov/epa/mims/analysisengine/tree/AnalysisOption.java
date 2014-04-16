package gov.epa.mims.analysisengine.tree;

import java.io.Serializable;

/**
 * An AnalysisOption
 * 
 * @author Tommy E. Cathey
 * @created July 30, 2004
 * @version $Id: AnalysisOption.java,v 1.3 2006/10/30 21:43:50 parthee Exp $
 */
public class AnalysisOption implements Serializable, Cloneable {
	/** Use serialVersionUID for interoperability. */
	private final static long serialVersionUID = -5617339133039014122L;

	/**
	 * Creates and returns a copy of this object
	 * 
	 * @return a copy of this object
	 * @throws CloneNotSupportedException
	 *             if clone on supported
	 */
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	/**
	 * Compares this object to the specified object.
	 * 
	 * @param o
	 *            the object to compare this object against
	 * @return true if the objects are equal; false otherwise
	 */
	public boolean equals(Object o) {
		boolean rtrn = true;

		if (o == null) {
			rtrn = false;
		} else if (o == this) {
			rtrn = true;
		} else if (o.getClass() != getClass()) {
			rtrn = false;
		}
		return rtrn;
	}
}
