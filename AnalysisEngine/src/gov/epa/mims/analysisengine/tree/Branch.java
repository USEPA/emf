package gov.epa.mims.analysisengine.tree;

import java.io.Serializable;

import java.util.LinkedList;
import java.util.List;

/**
 * an abstract extension of Node which can hold children
 * 
 * @author Tommy E. Cathey
 * @version $Id: Branch.java,v 1.3 2006/10/30 21:43:50 parthee Exp $
 * 
 */
public abstract class Branch extends Node implements Serializable, Cloneable {
	/*******************************************************************************************************************
	 * 
	 * fields
	 * 
	 ******************************************************************************************************************/
	static final long serialVersionUID = 1;

	/** a linked list of children nodes */
	private LinkedList children = new LinkedList();

	/*******************************************************************************************************************
	 * get a child
	 * 
	 * @param i
	 *            the index of child node to get
	 * @return ith child of this branch
	 * @pre i >= 0
	 ******************************************************************************************************************/
	public Node getChild(int i) {
		return (Node) children.get(i);
	}

	/*******************************************************************************************************************
	 * get the number of children
	 * 
	 * @return the number of children
	 ******************************************************************************************************************/
	public int getChildCount() {
		return children.size();
	}

	/*******************************************************************************************************************
	 * get the Leaf nodes below this branch node
	 * 
	 * @param leafList
	 *            a List to add found Leaves to
	 * @leafList != null
	 ******************************************************************************************************************/
	public void getLeaves(List leafList) {
		for (int i = 0; i < getChildCount(); i++) {
			Node child = getChild(i);

			if (child instanceof Branch) {
				((Branch) child).getLeaves(leafList);
			} else {
				leafList.add(child);
			}
		}
	}

	/*******************************************************************************************************************
	 * get the Page nodes below this branch node
	 * 
	 * @param pageList
	 *            a List to add found Pages to
	 * @pageList != null
	 ******************************************************************************************************************/
	public void getPages(List pageList) {
		for (int i = 0; i < getChildCount(); i++) {
			Node child = getChild(i);

			if (child instanceof Page) {
				pageList.add(child);
			} else {
				((Branch) child).getPages(pageList);
			}
		}
	}

	/*******************************************************************************************************************
	 * add a child node
	 * 
	 * @param node
	 *            child node to add
	 * @pre node != null
	 ******************************************************************************************************************/
	public void add(Node node) {
		children.add(node);
		node.setParent(this);
	}

	/*******************************************************************************************************************
	 * recursively remove all children from this branch
	 * 
	 ******************************************************************************************************************/
	public void clear() {
		for (int i = 0; i < getChildCount(); i++) {
			Node child = getChild(i);

			if (child instanceof Branch) {
				((Branch) child).clear();
			}
		}

		children.clear();
	}

	/*******************************************************************************************************************
	 * Creates and returns a copy of this object
	 * 
	 * @return a copy of this object
	 * @throws java.lang.CloneNotSupportedException
	 *             if not clonable
	 ******************************************************************************************************************/
	public Object clone() throws java.lang.CloneNotSupportedException {
		Branch clone = (Branch) super.clone();
		clone.children = new LinkedList();

		for (int i = 0; i < children.size(); ++i) {
			Node n = (Node) ((Node) children.get(i)).clone();
			clone.children.add(n);
		}

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
		} else {
			Branch other = (Branch) o;

			if (getChildCount() != other.getChildCount()) {
				rtrn = false;
			} else {
				for (int i = 0; i < getChildCount(); ++i) {
					if (!(getChild(i).equals(other.getChild(i)))) {
						rtrn = false;
					}
				}
			}
		}

		return rtrn;
	}

	/*******************************************************************************************************************
	 * remove a child
	 * 
	 * @param node
	 *            child node to remove
	 * @pre node != null
	 ******************************************************************************************************************/
	public void remove(Node node) {
		children.remove(node);
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