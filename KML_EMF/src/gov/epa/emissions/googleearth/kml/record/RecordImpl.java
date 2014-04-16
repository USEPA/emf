package gov.epa.emissions.googleearth.kml.record;

import gov.epa.emissions.googleearth.kml.KMZGeneratorException;
import gov.epa.emissions.googleearth.kml.PropertiesManager;
import gov.epa.emissions.googleearth.kml.PropertiesManager.PropertyKey;
import gov.epa.emissions.googleearth.kml.utils.Utils;

public class RecordImpl implements Record {

	private String[] values;

	private static boolean[] isReal;
	private static boolean[] isInt;

	private int latLonDecimalPlaces;
	private int decimalPlaces;
	private static String[] keys;
	private static String[] displayKeys;
	private static int latIndex = -1;
	private static int lonIndex = -1;

	public RecordImpl(String[] values) {

		this.values = values;
		this.latLonDecimalPlaces = PropertiesManager.getInstance()
				.getValueAsInt(PropertyKey.DECIMAL_PLACES_LATLON);
		this.decimalPlaces = PropertiesManager.getInstance().getValueAsInt(
				PropertyKey.DECIMAL_PLACES);

		checkIsNumeric(values);
	}

	public static void setKeys(String[] keys) throws KMZGeneratorException {

		latIndex = -1;
		lonIndex = -1;

		displayKeys = new String[keys.length];
		for (int i = 0; i < keys.length; i++) {

			keys[i] = keys[i].trim();
			String key = keys[i];
			if (key.startsWith("fips")) {
				displayKeys[i] = key.toUpperCase();
			} else {
				displayKeys[i] = Utils.capitalize(key.replace('_', ' '));
			}
		}

		RecordImpl.keys = keys;

		for (int i = 0; i < keys.length; i++) {

			String key = keys[i];
			if (key.startsWith("lat") || key.startsWith("yloc")) {
				latIndex = i;
			} else if (key.startsWith("lon") || key.startsWith("xloc")) {
				lonIndex = i;
			}
		}

		if (latIndex == -1 || lonIndex == -1) {
			throw new KMZGeneratorException(
					KMZGeneratorException.ERROR_CODE_MISSING_KEY,
					"Column keys must contain lat and lon (or xloc and yloc)");
		}

		initNumericCheck();
	}

	private static void initNumericCheck() {

		isReal = new boolean[keys.length];
		isInt = new boolean[keys.length];
		for (int i = 0; i < keys.length; i++) {

			isReal[i] = true;
			isInt[i] = true;
		}
	}

	public static void checkIsNumeric(String[] values) {

		for (int i = 0; i < values.length; i++) {

			String value = values[i];
			if (value == null || value.length() == 0) {
				//System.out.println("Skipping check of value '" + value + "'");
			} else {

				if (isInt[i]) {

					try {
						Integer.parseInt(value);
					} catch (NumberFormatException e) {

						isInt[i] = false;
						try {
							Double.parseDouble(value);
						} catch (NumberFormatException e2) {
							isReal[i] = false;
						}
					}
				} else if (isReal[i]) {

					try {
						Double.parseDouble(value);
					} catch (NumberFormatException e2) {
						isReal[i] = false;
					}
				}
			}
		}
	}

	@Override
	public int getColumnCount() {
		return values.length;
	}

	@Override
	public String getKey(int index) {
		return RecordImpl.keys[index];
	}

	@Override
	public String getDisplayKey(int index) {
		return RecordImpl.displayKeys[index];
	}

	@Override
	public String getHorizontal() {

		String horizontalStr = this.values[latIndex];

		if (this.latLonDecimalPlaces >= 0 && horizontalStr != null
				&& !horizontalStr.isEmpty()) {

			Double horizontal = Double.valueOf(horizontalStr);
			horizontal = Utils.roundDecimalPaces(horizontal,
					this.latLonDecimalPlaces);
			horizontalStr = Double.toString(horizontal);
		}

		return horizontalStr;
	}

	@Override
	public String getVertical() {

		String verticalStr = this.values[lonIndex];

		if (this.latLonDecimalPlaces >= 0 && verticalStr != null
				&& !verticalStr.isEmpty()) {

			Double vertical = Double.valueOf(verticalStr);
			vertical = Utils.roundDecimalPaces(vertical,
					this.latLonDecimalPlaces);
			verticalStr = Double.toString(vertical);
		}

		return verticalStr;
	}

	@Override
	public String getValue(int index) {

		String stringValue = this.values[index];

		if (stringValue == null || stringValue.length() == 0) {
			//System.out
			//		.println("Skipping format of value '" + stringValue + "'");
		} else {

			if (isReal[index] && !isInt[index]) {
				try {
					double doubleValue = Double.parseDouble(stringValue);
					doubleValue = Utils.roundDecimalPaces(doubleValue,
							this.decimalPlaces);
					stringValue = Double.toString(doubleValue);
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
			}
		}

		return stringValue;
	}

	@Override
	public void setValue(int index, String value) {
		this.values[index] = value;
	}

	@Override
	public int getIndex(String key) {

		int retVal = -1;

		for (int i = 0; i < keys.length; i++) {

			if (keys[i].equalsIgnoreCase(key)) {
				retVal = i;
				break;
			}
		}

		return retVal;
	}

	@Override
	public String toString() {

		StringBuilder sb = new StringBuilder();

		sb.append("(");
		for (int i = 0; i < this.values.length; i++) {

			sb.append(this.values[i]);

			if (i < this.values.length - 1) {
				sb.append(", ");
			}
		}
		sb.append(")");

		return sb.toString();
	}

	@Override
	public String toStringKeys() {
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < RecordImpl.keys.length; i++) {

			sb.append(RecordImpl.keys[i]);

			if (i < RecordImpl.keys.length - 1) {
				sb.append(", ");
			}
		}

		return sb.toString();
	}
}
