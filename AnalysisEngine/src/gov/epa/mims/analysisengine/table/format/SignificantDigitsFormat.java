package gov.epa.mims.analysisengine.table.format;

import gov.epa.mims.analysisengine.UserPreferences;

import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.NumberFormat;

/**
 * This class extends the decimal format and attempts add the significant digits feature
 * 
 * @author Prashant Pai, CEP UNC
 * @version $Id: SignificantDigitsFormat.java,v 1.2 2010/02/11 18:30:25 rross67 Exp $
 */

public class SignificantDigitsFormat extends DecimalFormat {
	/** serial version UID */
	static final long serialVersionUID = 1;

	public static final String NaN_FORMAT = " ";

	// constants.. maybe a better place to move them later

	/** the standard number format notation e.g. 22 * */
	public static final String STANDARD_FORMAT = "Standard_Notation";

	/** the scientific notation format e.g. 2.2E01 * */
	public static final String SCIENTIFIC_FORMAT = "Scientific_Notation";

	/** the currency format $22.00 * */
	public static final String CURRENCY_FORMAT = "Dollars";

	/** the percentage format 22.0% * */
	public static final String PERCENTAGE_FORMAT = "Percentage";

	/** the custom number format * */
	public static final String CUSTOM_FORMAT = "Custom";

	/** a complete list of available format options * */
	public static final String[] FORMAT_OPTIONS = { STANDARD_FORMAT, SCIENTIFIC_FORMAT, CURRENCY_FORMAT,
			PERCENTAGE_FORMAT, CUSTOM_FORMAT };

	/** the number of significant digits should be > 1 * */
	private int numSigDigits = 3;

	/** the number of decimal places.. should be > 1 * */
	private int numDecimalPlaces = 0;

	/** local decimal formatter * */
	private DecimalFormat sigDigitsFormat = new DecimalFormat();

	/** the selected number format * */
	private String selectedOption = this.SCIENTIFIC_FORMAT;

	/**
	 * default empty constructor
	 */
	public SignificantDigitsFormat() {
		super();
		setUserDefaults();
		// applyPattern(toPattern());
	}// SignificantDigitsFormat()

	/**
	 * default constructor with pattern
	 * 
	 * @pre pattern != null
	 */
	public SignificantDigitsFormat(String pattern) {
		super(pattern);
		if (pattern == null) {
			throw new IllegalArgumentException("The pattern specified cannot be null");
		}
		setUserDefaults();
		setFromPattern(pattern);
		// applyPattern(toPattern());
	}// SignificantDigitsFormat()

	private void setUserDefaults() {
		UserPreferences pref = UserPreferences.USER_PREFERENCES;
		String prefOption = pref.getProperty(UserPreferences.FORMAT_OPTION);
		String prefNumSigDigits = pref.getProperty(UserPreferences.FORMAT_DOUBLE_SIGNIFICANT_DIGITS);
		String prefNumDecimalPlaces = pref.getProperty(UserPreferences.FORMAT_DOUBLE_DECIMAL_PLACES);
		String prefGrouping = pref.getProperty(UserPreferences.FORMAT_GROUPING);
		if (prefOption == null) {
			prefOption = "";
		}
		if (prefNumSigDigits == null) {
			prefNumSigDigits = "";
		}
		if (prefNumDecimalPlaces == null) {
			prefNumDecimalPlaces = "";
		}
		boolean foundOption = false;
		for (int i = 0; i < FORMAT_OPTIONS.length; i++) {
			if (prefOption.equalsIgnoreCase(FORMAT_OPTIONS[i])) {
				foundOption = true;
				break;
			}
		}// for
		if (foundOption) {
			setSelectedOption(prefOption);
		} else {
			setSelectedOption(SCIENTIFIC_FORMAT);
		}
		int numOFSigDigits = 4;
		int numOFDecimalPlaces = 2;
		try {
			numOFSigDigits = Integer.parseInt(prefNumSigDigits);
		} catch (NumberFormatException e) {
			// do nothing
		}
		try {
			numOFDecimalPlaces = Integer.parseInt(prefNumDecimalPlaces);
		} catch (NumberFormatException e) {
			// do nothing
		}
		setNumberOfSignificantDigits(numOFSigDigits);
		setNumberOfDecimalPlaces(numOFDecimalPlaces);
		// setNumberOfSignificantDigits(4);
		// setNumberOfDecimalPlaces(1);

		/*
		 * grouping==true is the default
		 */
		boolean grouping = true;
		try {
			if (prefGrouping != null && prefGrouping.trim().length() > 0) {
				grouping = Boolean.parseBoolean(prefGrouping);
			}
		} catch (Exception e) {
			/*
			 * no-op
			 */
		}

		this.setGroupingUsed(grouping);
	}

	// Overrides
	/**
	 * Formats a double to produce a string.
	 * 
	 * @param number
	 *            The double to format
	 * @param result
	 *            where the text is to be appended
	 * @param fieldPosition
	 *            On input: an alignment field, if desired. On output: the offsets of the alignment field.
	 * @return The formatted number string
	 * @see java.text.FieldPosition
	 */
	public StringBuffer format(double number, StringBuffer result, FieldPosition fieldPosition) {
		// get a formatted string according to the significant digits decimal
		// format
		// System.out.println(sigDigitsFormat.toPattern());
		if (Double.isNaN(number)) {
			// return new StringBuffer(new Double("NaN").toString());
			return new StringBuffer(NaN_FORMAT);
		}
		String formattedNumber = "" + number;
		if (sigDigitsFormat != null && numSigDigits > 0) {
			formattedNumber = sigDigitsFormat.format(number);
		}// if (sigDigitsFormat != null)

		// now that the number is truncated to the required format
		// apply the parent format
		return super.format(Double.parseDouble(formattedNumber), result, fieldPosition);
	}// format(double, StringBuffer, FieldPosition)

	/**
	 * sets the number of significant digits
	 * 
	 * @param numSigDigits
	 * @throws IllegalArgumentException
	 */
	public void setNumberOfSignificantDigits(int numSigDigits)
	// throws IllegalArgumentException RP: no need to throw an IllegalArgumentException since
	// it's a runtime exception
	{
		if (numSigDigits < 0) {
			throw new IllegalArgumentException("The number of significant digits " + "cannot be negative");
		}
		this.numSigDigits = numSigDigits;
		// if the number of significant digits is not specified or is 0, then do
		// nothing
		if (numSigDigits < 1)
			return;

		StringBuffer sb = new StringBuffer();

		// e.g if the numSigDigits is 2, then the pattern should be 0.0E0
		// if the numSigDigits is 4, then it should be 0.000E0
		sb.append('0');
		sb.append('.');

		for (int i = 0; i < numSigDigits - 1; i++) {
			sb.append('0');
		}
		// add E0 to the pattern.. need to check whether this is enough or we need
		// to do E##0 to be on safe side
		sb.append("E0");

		sigDigitsFormat.applyPattern(sb.toString());

		// also if the selected option is scientific then set the number of
		// decimal places to be one less than that of significant digits
		if (selectedOption.equals(SCIENTIFIC_FORMAT)) {
			setNumberOfDecimalPlaces(numSigDigits - 1);
		}
	}// setNumberOfSignificantDigits(int)

	/**
	 * return the number of significant digits set for this format
	 * 
	 * @return int
	 */
	public int getNumberOfSignificantDigits() {
		return numSigDigits;
	}// getNumberOfSignificantDigits()

	/**
	 * sets the number of decimal places
	 * 
	 * @param int the number of decimal places required
	 * @throws IllegalArgumentException
	 */
	public void setNumberOfDecimalPlaces(int numDecimalPlaces) throws IllegalArgumentException {
		if (numDecimalPlaces < 0) {
			throw new IllegalArgumentException("The number of decimal places " + "cannot be negative");
		}
		this.numDecimalPlaces = numDecimalPlaces;
	}// setNumberOfSignificantDigits(int)

	/**
	 * returns the number of decimal places set in this format
	 * 
	 * @return
	 */
	public int getNumberOfDecimalPlaces() {
		return numDecimalPlaces;
	}// getNumberOfSignificantDigits()

	/**
	 * sets the selected option
	 * 
	 * @param option
	 */
	public void setSelectedOption(String option) {
		this.selectedOption = option;
	}// setSelectedOption(String)

	/**
	 * returns the currently set formatting option
	 * 
	 * @return String
	 */
	public String getSelectedOption() {
		return selectedOption;
	}// getSelectedOption()

	/**
	 * resolves the paramters set into this formatter into a string equivalent REMEMBER that the significant digits will
	 * not be part of this string It is applied in a different fashion
	 * 
	 * @return String the inherent pattern
	 */
	public String toPattern() {

		// if the selectedOption is not set use the super class toPattern
		if (selectedOption == null)
			return super.toPattern();

		StringBuffer sb = new StringBuffer();

		// Place dollars first if selected.
		if (selectedOption.equals(CURRENCY_FORMAT)) {
			sb.append('$');
		}

		/*
		 * Always put a zero in front of the decimal point. This is 
         * needed if the user chooses 0 decimal places and no special options.
		 * Also if grouping==true add delimiters, for readability.
		 */
		if (this.isGroupingUsed()) {
			sb.append("#,##0");
		} else {
			sb.append("0");
		}

		// Set the number of decimal places.
		if (numDecimalPlaces > 0) {
			sb.append('.');
			for (int i = 0; i < numDecimalPlaces; i++) {
				sb.append('0');
			}
		}

		// Set scientific notation if selected.
		if (selectedOption.equals(SCIENTIFIC_FORMAT)) {
			// Dan has set two digits at least after the E sign
			sb.append("E00");
		}

		// Set the percent last. This multiplies the value in the field by 100.
		if (selectedOption.equals(PERCENTAGE_FORMAT)) {
			sb.append('%');
		}

		return sb.toString();
	}// toPattern()

	public void setFromPattern(String formatString) {
		// Look for an 'E' or and 'e' in th format string.
		// If we find one, check the scientific check box.
		selectedOption = this.STANDARD_FORMAT;
		if ((formatString.indexOf('e') > 0) || (formatString.indexOf('E') > 0)) {
			selectedOption = this.SCIENTIFIC_FORMAT;
		} else if (formatString.indexOf('$') > -1) {
			selectedOption = this.CURRENCY_FORMAT;
		} else if (formatString.indexOf('%') > 0) {
			selectedOption = this.PERCENTAGE_FORMAT;
		}
		// If we have a decimal place, then count the number

		int dot = formatString.indexOf('.');

		if (dot >= 0) {
			if (selectedOption.equals(SCIENTIFIC_FORMAT)) {
				numDecimalPlaces = formatString.length() - 3 - dot - 1;
			}// if (selectedOption.equals(SCIENTIFIC_FORMAT))
			else {
				numDecimalPlaces = formatString.length() - dot - 1;
			}// else
		}// if (dot >= 0)
	}// setFromPattern()

	/**
	 * a factory method that returns the
	 * 
	 * @return
	 */
	public static final NumberFormat getSignificantDigitsInstance() {
		return new SignificantDigitsFormat();
	}// getSignificantDigitsInstance()

	public static void main(String[] args) {
		SignificantDigitsFormat significantDigitsFormat = new SignificantDigitsFormat("#0");
		significantDigitsFormat.setNumberOfSignificantDigits(5);
		double number = 3163.3;
		String formatted = significantDigitsFormat.sigDigitsFormat.format(number);
		System.out.println(formatted);
	}
}
