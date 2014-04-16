package gov.epa.emissions.googleearth.kml.record;

import gov.epa.emissions.googleearth.kml.KMZGeneratorException;
import gov.epa.emissions.googleearth.kml.PropertiesManager;
import gov.epa.emissions.googleearth.kml.PropertiesManager.PropertyKey;
import gov.epa.emissions.googleearth.kml.utils.Utils;

public class StreetRecordImpl implements StreetRecord {

	private String[] values;
	
	private StreetRecordHeader headers;

	private int decimalPlaces;

	public StreetRecordImpl(StreetRecordHeader headers, String[] values) throws KMZGeneratorException {
		this.headers = headers;
		this.values = values;
		this.decimalPlaces = PropertiesManager.getInstance().getValueAsInt(
				PropertyKey.DECIMAL_PLACES);
		checkValues(values);
	}

	public void checkValues(String[] values) throws KMZGeneratorException {
		
		if (null == values) {
			throw new KMZGeneratorException(KMZGeneratorException.ERROR_CODE_GENERAL,"Street record values is null!");
		}
		
		if (headers.getColCount() != values.length) {
			throw new KMZGeneratorException(KMZGeneratorException.ERROR_CODE_GENERAL,"Street record # columns does not match # columns of headers!");
		}
		
		try {
			Integer.parseInt(values[headers.getLinkIDIndex()]);
		} catch (NumberFormatException e) {
			throw new KMZGeneratorException(KMZGeneratorException.ERROR_CODE_GENERAL,"Street record LinkID is not an int!");
		}
		
		try {
			Integer.parseInt(values[headers.getLinkBufferIndex()]);
		} catch (NumberFormatException e) {
			throw new KMZGeneratorException(KMZGeneratorException.ERROR_CODE_GENERAL,"Street record Buffer is not an int!");
		}
		
		try {
			Double.parseDouble(values[headers.getCOTotalIndex()]);
		} catch (NumberFormatException e) {
			throw new KMZGeneratorException(KMZGeneratorException.ERROR_CODE_GENERAL,"Street record CO total is not a number!");
		}
		
		try {
			Double.parseDouble(values[headers.getNOXTotalIndex()]);
		} catch (NumberFormatException e) {
			throw new KMZGeneratorException(KMZGeneratorException.ERROR_CODE_GENERAL,"Street record NOX total is not a number!");
		}
		
		try {
			Double.parseDouble(values[headers.getPM25TotalIndex()]);
		} catch (NumberFormatException e) {
			throw new KMZGeneratorException(KMZGeneratorException.ERROR_CODE_GENERAL,"Street record PM25 total is not a number!");
		}
		
		try {
			Double.parseDouble(values[headers.getEC25TotalIndex()]);
		} catch (NumberFormatException e) {
			throw new KMZGeneratorException(KMZGeneratorException.ERROR_CODE_GENERAL,"Street record EC25 total is not a number!");
		}
		
		try {
			Double.parseDouble(values[headers.getOC25TotalIndex()]);
		} catch (NumberFormatException e) {
			throw new KMZGeneratorException(KMZGeneratorException.ERROR_CODE_GENERAL,"Street record OC25 total is not a number!");
		}
		
		try {
			Double.parseDouble(values[headers.getBENZTotalIndex()]);
		} catch (NumberFormatException e) {
			throw new KMZGeneratorException(KMZGeneratorException.ERROR_CODE_GENERAL,"Street record BENZ total is not a number!");
		}
	}

	@Override
	public int getColumnCount() {
		return values.length;
	}

	@Override
	public String getKey(int index) {
		return headers.getHeaders()[index];
	}

	@Override
	public String getDisplayKey(int index) {
		return headers.getHeaders()[index];
	}

	@Override
	public String getValue(int index) {

		String stringValue = this.values[index];

		if (stringValue == null || stringValue.length() == 0) {
			//System.out
			//		.println("Skipping format of value '" + stringValue + "'");
		} else {

			if (this.headers.isDouble(index) && !this.headers.isDouble(index)) {
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

		for (int i = 0; i < this.headers.getHeaders().length; i++) {

			if (this.headers.getHeaders()[i].equalsIgnoreCase(key)) {
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

		for (int i = 0; i < this.headers.getHeaders().length; i++) {

			sb.append(this.headers.getHeaders()[i]);

			if (i < this.headers.getHeaders().length - 1) {
				sb.append(", ");
			}
		}

		return sb.toString();
	}

	@Override
	public String getPlaceMarkGeometryXml() {
		return this.values[this.headers.getPlacemarkXmlIndex()];
	}
}
