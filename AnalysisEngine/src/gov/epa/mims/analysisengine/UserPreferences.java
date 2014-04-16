package gov.epa.mims.analysisengine;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

public class UserPreferences extends Properties {

	public static UserPreferences USER_PREFERENCES = null;

	/**
	 * formatting double
	 */
	// keys
	public static final String FORMAT_DOUBLE_SIGNIFICANT_DIGITS = "format.double.significant_digits";

	public static final String FORMAT_DOUBLE_DECIMAL_PLACES = "format.double.decimal_places";

	public static final String FORMAT_OPTION = "format.double.option";

	/*
	 * pref for determining whether to group digits in large numbers
	 */
	public static final String FORMAT_GROUPING = "format.grouping";

	static {
		USER_PREFERENCES = new UserPreferences();
		try {
			String fileName = System.getProperty("USER_PREFERENCES");
			if (fileName != null && fileName.trim().length() >= 0) {
				fileName = fileName.trim();
				File file = new File(fileName);
				if (file.exists() && file.isFile()) {

					FileInputStream inStream = new FileInputStream(file);

					USER_PREFERENCES.load(inStream);
				}
			}
		} catch (Exception e) {
			// do nothing
		}
	}
}
