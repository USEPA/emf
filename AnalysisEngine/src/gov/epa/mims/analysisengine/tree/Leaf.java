package gov.epa.mims.analysisengine.tree;

import java.io.Serializable;

/**
 * abstract class which extends Node and implements leaf behavior
 * 
 * @author Tommy E. Cathey
 * @version $Id: Leaf.java,v 1.3 2006/12/08 22:46:52 parthee Exp $
 * 
 */
public abstract class Leaf extends Node implements Serializable, Cloneable {
	/** serial version UID */
	static final long serialVersionUID = 1;

	/**
	 * get i-th child of this Node
	 * 
	 * @param i
	 *            index of child Node
	 * @throws java.lang.IllegalArgumentException
	 *             thrown if this method is called on a Leaf
	 * @return i-th child of this Node
	 */
	public Node getChild(int i) throws java.lang.IllegalArgumentException {
		throw new IllegalArgumentException("A Leaf cannot have children");
	}

	/**
	 * get number of children for this Node
	 * 
	 * @return always return 0 (Leaves cannot have children)
	 * 
	 */
	public int getChildCount() {
		return 0;
	}

	/*******************************************************************************************************************
	 * Creates and returns a copy of this object
	 * 
	 * @return a copy of this object
	 * @throws java.lang.CloneNotSupportedException
	 *             if not clonable
	 ******************************************************************************************************************/
	public Object clone() throws java.lang.CloneNotSupportedException {
		Leaf clone = (Leaf) super.clone();

		return clone;
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
}