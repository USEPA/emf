package gov.epa.mims.analysisengine.tree;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/***********************************************************************************************************************
 * abstract class Node for the construction of composite plot description trees
 * 
 * @author Tommy E. Cathey
 * @version $Id: Node.java,v 1.3 2006/10/30 21:43:50 parthee Exp $
 * 
 **********************************************************************************************************************/
public abstract class Node implements Serializable, Cloneable, VisitableIfc {
	/** serial version UID */
	static final long serialVersionUID = 1;

	/** instance counter */
	private static int instances;

	/** parent node to this node */
	private Node parent = null;

	/** name of node */
	private String name;

	/** instance id for this node */
	private int id;

	/**
	 * Creates a new Node object.
	 */
	public Node() {
		id = instances;
		instances++;
	}

	/*******************************************************************************************************************
	 * retrieve the i-th child
	 * 
	 * @param i
	 *            index into the list of children for this node
	 * 
	 * @return the i-th child for this node
	 ******************************************************************************************************************/
	public abstract Node getChild(int i);

	/*******************************************************************************************************************
	 * retrieve the number of children of this node
	 * 
	 * @return the number of children of this node
	 ******************************************************************************************************************/
	public abstract int getChildCount();

	/*******************************************************************************************************************
	 * retreive the ID for this Node
	 * 
	 * @return id
	 ******************************************************************************************************************/
	public int getID() {
		return id;
	}

	/*******************************************************************************************************************
	 * get an option
	 * 
	 * @param searchKey
	 *            String which serves as look up searchKey
	 * @return the object corresponding to given searchKey null if searchKey is not in OptionsHash
	 * @pre searchKey != null
	 ******************************************************************************************************************/
	public Object getOption(Object searchKey) {
		HashMap optionsForThisNode = new HashMap();
		ArrayList analysisOptionsAncestors = findAncestors(AnalysisOptions.class);

		for (int i = 0; i < analysisOptionsAncestors.size(); i++) {
			AnalysisOptions opts;
			opts = (AnalysisOptions) analysisOptionsAncestors.get(i);

			Set keys = opts.getKeys();
			Iterator iter = keys.iterator();

			while (iter.hasNext()) {
				String key = (String) iter.next();
				Object opt = opts.getOption(key);
				optionsForThisNode.put(key, opt);
			}
		}

		if (optionsForThisNode.containsKey(searchKey)) {
			return optionsForThisNode.get(searchKey);
		}
		return null;
	}

	/*******************************************************************************************************************
	 * return parent Node
	 * 
	 * @return parent node
	 ******************************************************************************************************************/
	public Node getParent() {
		return parent;
	}

	/*******************************************************************************************************************
	 * Creates and returns a copy of this object
	 * 
	 * @return a copy of this object
	 * @throws java.lang.CloneNotSupportedException
	 *             if not clonable
	 ******************************************************************************************************************/
	public Object clone() throws java.lang.CloneNotSupportedException {
		Node clone = (Node) super.clone();

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

		if (o == null) {
			rtrn = false;
		} else if (o == this) {
			rtrn = true;
		} else if (o.getClass() != getClass()) {
			rtrn = false;
		} else {
			Node other = (Node) o;

			rtrn = ((name == null) ? (other.name == null) : (name.equals(other.name)));
		}

		return rtrn;
	}

	/*******************************************************************************************************************
	 * find Class c ancestors of a Node
	 * 
	 * @param c
	 *            the Class of ancestor to find
	 * @return list of all ancestors of Class type c
	 * 
	 ******************************************************************************************************************/
	public ArrayList findAncestors(Class c) {
		ArrayList ancestorList;

		if (parent != null) {
			ancestorList = parent.findAncestors(c);
		} else {
			ancestorList = new ArrayList();
		}

		if (c.isInstance(this)) {
			ancestorList.add(this);
		}

		return ancestorList;
	}

	/*******************************************************************************************************************
	 * describe object in a String
	 * 
	 * @return String describing object
	 ******************************************************************************************************************/

	// public String toString()
	// {
	// return Util.toString(this);
	// }
	/*******************************************************************************************************************
	 * set the parent for this Node
	 * 
	 * @param parent
	 *            Node which serves as parent to this Node
	 * 
	 ******************************************************************************************************************/
	void setParent(Node parent) {
		this.parent = parent;
	}
}