package gov.epa.mims.analysisengine.tree;

/**
 * Helper class to store information about data sets that a plot can accept.
 * 
 * @author Alison Eyth , Tommy E. Cathey
 * @version $Id: DataSetInfo.java,v 1.5 2007/05/31 14:29:31 qunhe Exp $
 * 
 */
public class DataSetInfo {
	/** Class of required data set */
	private final Class type;

	/** name of data set "Category" - e.g. X, Y, ... */
	private final String name;

	/** maximum number of data sets needed of this type or 0 if no maximum */
	private final int maxNumber;

	/** minimum number of data sets needed of this type */
	private final int minNumber;

	/** information about this object */
	private String infoString = null;

	/*******************************************************************************************************************
	 * Creates a new DataSetInfo object.
	 * 
	 * @param name
	 *            name of data set "Category" - e.g. X, Y, ...
	 * @param type
	 *            Class of required data set
	 * @param minNumber
	 *            minimum number of data sets needed of this type
	 * @param maxNumber
	 *            maximum number of data sets needed of this type or -1 if no maximum The following Ranges are possible:
	 *            maxNumber >= 0 --> [minNumber,maxNumber] maxNumber == -1 --> [minNumber,no limit)
	 * @pre name != null
	 * @pre type != null
	 * @pre minNumber >= 0
	 * @pre (maxNumber >= minNumber)||(maxNumber==-1)
	 * @post this.name != null
	 * @post this.type != null
	 * @post this.minNumber >= 0
	 * @post (this.maxNumber >= this.minNumber)||(this.maxNumber==-1)
	 ******************************************************************************************************************/
	public DataSetInfo(String name, Class type, int minNumber, int maxNumber) {
		this.name = name;
		this.type = type;
		this.minNumber = minNumber;
		this.maxNumber = maxNumber;
	}

	/*******************************************************************************************************************
	 * retrieve Class of required data set
	 * 
	 * @return Class of required data set
	 ******************************************************************************************************************/
	public Class getClassType() {
		return type;
	}

	/*******************************************************************************************************************
	 * info about this object
	 * 
	 * @return String describing object
	 ******************************************************************************************************************/
	public String getInfo() {
		if (infoString == null) {
			setInfoString();
		}

		return infoString;
	}

	/*******************************************************************************************************************
	 * retrieve maximum number of data sets needed of this type or -1 if no maximum
	 * 
	 * @return maximum number of data sets needed of this type or -1 if no maximum
	 ******************************************************************************************************************/
	public int getMaxNumber() {
		return maxNumber;
	}

	/*******************************************************************************************************************
	 * retrieve minimum number of data sets needed of this type
	 * 
	 * @return minimum number of data sets needed of this type
	 ******************************************************************************************************************/
	public int getMinNumber() {
		return minNumber;
	}

	/*******************************************************************************************************************
	 * retrieve name of data set "Category" - e.g. X, Y, ...
	 * 
	 * @return name of data set "Category" - e.g. X, Y, ...
	 ******************************************************************************************************************/
	public String getName() {
		return name;
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
			DataSetInfo other = (DataSetInfo) o;
			rtrn = ((name == null) ? (other.name == null) : (name.equals(other.name)))
					&& ((type == null) ? (other.type == null) : (type.equals(other.type)))
					&& (minNumber == other.minNumber) && (maxNumber == other.maxNumber);
		}

		return rtrn;
	}

	/*******************************************************************************************************************
	 * describe object in a String
	 * 
	 * @return String describing object
	 ******************************************************************************************************************/
	public String toString() {
		return getName();
	}

	/*******************************************************************************************************************
	 * validate the number of data sets provided
	 * 
	 * @param numberProvided
	 *            number of data sets provided
	 * 
	 * @return true if number is OK
	 * 
	 * @throws Exception
	 *             if number is NOT OK
	 ******************************************************************************************************************/
	public boolean validateNumber(int numberProvided) throws Exception {
		if (numberProvided < minNumber) {
			throw new Exception(minNumber + " data sets of type " + name + " are required for the plot, but "
					+ numberProvided + " were provided");
		}

		if ((maxNumber != -1) && (numberProvided > maxNumber)) {
			throw new Exception(maxNumber + " data sets of type " + name + " are allowed for the plot, but "
					+ numberProvided + " were provided");
		}

		return true;
	}

	/*******************************************************************************************************************
	 * set the information string, infoString
	 ******************************************************************************************************************/
	private void setInfoString() {
		if (maxNumber == -1) {
			infoString = Integer.toString(minNumber) + " to N needed";
		} else if (minNumber == maxNumber) {
			infoString = Integer.toString(minNumber) + " needed";
		} else {
			infoString = Integer.toString(minNumber) + " to " + Integer.toString(maxNumber) + " needed";
		}
	}
}