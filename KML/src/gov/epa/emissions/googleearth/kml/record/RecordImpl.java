package gov.epa.emissions.googleearth.kml.record;

import gov.epa.emissions.googleearth.kml.KMZGeneratorException;
import gov.epa.emissions.googleearth.kml.utils.Utils;

public class RecordImpl implements Record {

	private String[] values;

	private static String[] keys;
	private static String[] displayKeys;
	private static int latIndex = -1;
	private static int lonIndex = -1;

	public RecordImpl(String[] values) {
		this.values = values;
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
			if (key.startsWith("lat")) {
				latIndex = i;
			} else if (key.startsWith("lon")) {
				lonIndex = i;
			}
		}

		if (latIndex == -1 || lonIndex == -1) {
			throw new KMZGeneratorException(
					KMZGeneratorException.ERROR_CODE_MISSING_KEY,
					"Column keys must contain lat and lon");
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
		return this.values[latIndex];
	}

	@Override
	public String getVertical() {
		return this.values[lonIndex];
	}

	@Override
	public String getValue(int index) {
		return this.values[index];
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
