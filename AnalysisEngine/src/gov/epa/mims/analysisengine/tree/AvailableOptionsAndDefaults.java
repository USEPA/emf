package gov.epa.mims.analysisengine.tree;

import gov.epa.mims.analysisengine.gui.OptionInfo;

import java.io.Serializable;

import java.util.HashMap;

/**
 * availableOptionsAndDefaults class
 * 
 * @version $Revision: 1.4 $
 * @author Alison Eyth , Tommy E. Cathey
 */
public class AvailableOptionsAndDefaults implements Serializable {
	/** serial version UID */
	static final long serialVersionUID = 1;

	/**
	 * global default values
	 */
	private static HashMap globalDefaults = new HashMap(10);

	/** hashmap of default {@link AnalysisOptions} */
	private HashMap defValues = new HashMap(10);

	/** array of available {@link AnalysisOptions} */
	private String[] allKeywords = null;

	/*******************************************************************************************************************
	 * Creates a new AvailableOptionsAndDefaults object.
	 * 
	 * @param allKeywords
	 *            array of available {@link AnalysisOptions}
	 ******************************************************************************************************************/
	public AvailableOptionsAndDefaults(String[] allKeywords) {
		this.allKeywords = (String[]) allKeywords.clone();
	}

	/*******************************************************************************************************************
	 * returns array of available {@link AnalysisOptions} keywords
	 * 
	 * @return array of available {@link AnalysisOptions} keywords
	 ******************************************************************************************************************/
	public String[] getAllKeywords() {
		return (allKeywords == null) ? null : (String[]) allKeywords.clone();
	}

	/*******************************************************************************************************************
	 * retrieve {@link AnalysisOptions} default values
	 * 
	 * @param keywords
	 *            String[] keyword to look up default for
	 * @return Object[] default values for keywords (items null if not available)
	 ******************************************************************************************************************/
	public AnalysisOption[] getDefaultValues(String[] keywords) throws Exception, CloneNotSupportedException {
		AnalysisOption[] defaults = new AnalysisOption[keywords.length];

		for (int i = 0; i < keywords.length; i++) {
			defaults[i] = (AnalysisOption) defValues.get(keywords[i]);

			if (defaults[i] == null) {
				AnalysisOption tempOption = (AnalysisOption) globalDefaults.get(keywords[i]);
				if (tempOption == null) {
					throw new Exception("There is no global default value for keyword '" + keywords[i] + "'.");
				}
				defaults[i] = (AnalysisOption) tempOption.clone();
			} else {
				defaults[i] = (AnalysisOption) defaults[i].clone();
			}
		}

		return defaults;
	}

	/**
	 * 
	 * @param keyword
	 *            String to add to global defaults
	 * @return Object default value to add to global defaults - be sure to clone before using!!
	 */
	public static Object getGlobalDefaultValue(String keyword) {
		AnalysisOption defaultValue = (AnalysisOption) globalDefaults.get(keyword);

		if (defaultValue != null) {
			return defaultValue;
		}

		return defaultValue;
	}

	/*******************************************************************************************************************
	 * add default AnalysisOptions
	 * 
	 * @param keywords
	 *            array of {@link AnalysisOptions} keywords
	 * @param values
	 *            default values associated with the {@link AnalysisOptions} keywords
	 * 
	 * @return keywords.length
	 * @throws Exception
	 *             if keyword is not available for class
	 ******************************************************************************************************************/
	public int addDefaultValues(String[] keywords, Object[] values) throws Exception {
		if (keywords.length != values.length) {
			String s1 = "In addDefaultValues, the number of keywords does not ";
			String s2 = "equal the number of values";
			throw new IllegalArgumentException(s1 + s2);
		}

		if (defValues == null) {
			defValues = new HashMap();
		}

		for (int i = 0; i < keywords.length; ++i) {
			if (containsKeyword(keywords[i])) {
				defValues.put(keywords[i], values[i]);
			} else {
				StringBuffer b = new StringBuffer(300);
				b.append("Cannot set default value for keyword ");
				b.append(keywords[i]);
				b.append(" because the keyword is not available for class ");
				b.append(getClass().getName());
				throw new Exception(b.toString());
			}
		}

		return keywords.length;
	}

	/**
	 * 
	 * @param keyword
	 *            String to add to global defaults
	 * @param defaultValue
	 *            Object default value to add to global defaults
	 * @return globalDefaults
	 */
	public static Object addGlobalDefaultValue(String keyword, Object defaultValue) {
		return globalDefaults.put(keyword, defaultValue);
	}

	/*******************************************************************************************************************
	 * Creates and returns a copy of this object
	 * 
	 * @return a copy of this object
	 ******************************************************************************************************************/
	public Object clone() {
		try {
			AvailableOptionsAndDefaults clone;
			clone = (AvailableOptionsAndDefaults) super.clone();

			clone.allKeywords = (allKeywords == null) ? null : (String[]) allKeywords.clone();
			clone.defValues = (defValues == null) ? null : (HashMap) defValues.clone();

			return clone;
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

	/**
	 * 
	 * @param keyword
	 *            String keyword to look for
	 * @return boolean whether the keyword exists in allKeywords
	 */
	public boolean containsKeyword(String keyword) {
		for (int i = 0; i < allKeywords.length; i++) {
			if (allKeywords[i].equals(keyword)) {
				return true;
			}
		}

		return false;
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
			AvailableOptionsAndDefaults other = (AvailableOptionsAndDefaults) o;

			rtrn = Util.equals(allKeywords, other.allKeywords) && Util.equals(defValues, other.defValues);
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
}
