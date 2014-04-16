package gov.epa.emissions.googleearth.kml.generator.preprocessor;

import gov.epa.emissions.googleearth.kml.ConfigurationManager;
import gov.epa.emissions.googleearth.kml.KMZGeneratorException;
import gov.epa.emissions.googleearth.kml.PropertiesManager;
import gov.epa.emissions.googleearth.kml.PropertiesManager.PropertyKey;
import gov.epa.emissions.googleearth.kml.generator.BinnedMultiGeometrySourceGenerator;
import gov.epa.emissions.googleearth.kml.generator.BinnedPointSourceGenerator;
import gov.epa.emissions.googleearth.kml.record.Record;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MultiGeometryPreProcessorImpl implements PreProcessor {

	private List<Record> records;
	private ArrayList<Double> values;

	private ArrayList<String> pollutants;
	private String labelColumnName;
	private String filterColumnName;
	private String dataColumnName;
	private String filterDisplayName;
	private String dataDisplayName;

	private int filterColumnIndex = -1;
	private int dataColumnIndex = -1;
	private int labelColumnIndex = -1;

	private String filterValue;
	private double dataMinimum;
	private double dataMaximum;

	private double absMinValue = Double.MAX_VALUE;
	private double absMaxValue = -Double.MAX_VALUE;

	private double maxValue;
	private double minValue;
	private double meanValue;

	private int totalCount;
	private int processsedCount;

	public MultiGeometryPreProcessorImpl() {
		this.records = new ArrayList<Record>();
	}

	public void preProcessRecords(BinnedPointSourceGenerator generator)
			throws KMZGeneratorException {

		this.processPropertiesManager(generator);

		long t1 = System.currentTimeMillis();
		this.pollutants = new ArrayList<String>();
		this.values = new ArrayList<Double>();
		Record nextRecord = null;

		boolean first = true;
		while ((nextRecord = generator.getRecordProducer().nextRecord()) != null) {

			boolean discard = false;
			if (nextRecord.getHorizontal() == null
					|| nextRecord.getHorizontal().isEmpty()) {

				if (ConfigurationManager.getInstance().getValueAsBoolean(
						ConfigurationManager.PropertyKey.SHOW_OUTPUT.getKey())) {

					System.out.println("Latitude blank for record "
							+ nextRecord);
					System.out.println("Discarding.");
				}

				discard = true;
			}

			if (nextRecord.getVertical() == null
					|| nextRecord.getVertical().isEmpty()) {

				if (ConfigurationManager.getInstance().getValueAsBoolean(
						ConfigurationManager.PropertyKey.SHOW_OUTPUT.getKey())) {

					System.out.println("Longitude blank for record "
							+ nextRecord);
					System.out.println("Discarding.");
					discard = true;
				}
			}

			if (discard) {
				continue;
			}

			if (first) {

				this.validateColumnNames(nextRecord);

				if (this.filterColumnIndex == -1) {
					this.filterColumnIndex = nextRecord
							.getIndex(this.filterColumnName);
				}

				if (this.filterDisplayName == null) {
					this.filterDisplayName = nextRecord
							.getDisplayKey(this.filterColumnIndex);
				}

				if (this.dataColumnIndex == -1) {
					this.dataColumnIndex = nextRecord
							.getIndex(this.dataColumnName);
				}

				if (this.dataDisplayName == null) {
					this.dataDisplayName = nextRecord
							.getDisplayKey(this.dataColumnIndex);
				}

				first = false;
			}

			String pollutant = nextRecord.getValue(this.filterColumnIndex);
			if (!this.pollutants.contains(pollutant)) {
				this.pollutants.add(pollutant);
			}

			try {

				String stringValue = nextRecord.getValue(this.dataColumnIndex);
				if ((stringValue == null || stringValue.length() == 0)
						&& !PropertiesManager.getInstance().getValueAsBoolean(
								PropertyKey.DATA_DISCARD_BLANK)) {
					stringValue = "0";
				}

				double value = Double.valueOf(stringValue);
				if (value > this.absMaxValue) {
					this.absMaxValue = value;
				} else if (value < this.absMinValue) {
					this.absMinValue = value;
				}

				if (value < this.dataMinimum || value > this.dataMaximum) {
					System.out.println("Filtering " + value);
				}

				this.totalCount++;

				if ((this.filterValue == null || this.filterValue.isEmpty() || this.filterValue
						.equalsIgnoreCase(pollutant))
						&& (value >= this.dataMinimum && value <= this.dataMaximum)) {

					this.processsedCount++;
					this.records.add(nextRecord);
					this.values.add(value);

					if (value > this.maxValue) {
						this.maxValue = value;
					} else if (value < this.minValue) {
						this.minValue = value;
					}
				}
			} catch (NumberFormatException e) {
				if (ConfigurationManager.getInstance().getValueAsBoolean(
						ConfigurationManager.PropertyKey.SHOW_OUTPUT.getKey())) {
					System.out.println("Unable to process record "
							+ nextRecord.toString()
							+ " due to number formatting problem.");
				}
			}

		}

		if (this.filterValue != null && !this.filterValue.isEmpty()) {

			boolean containsFilterValue = false;
			for (String pollutant : this.pollutants) {

				if (pollutant.equalsIgnoreCase(this.filterValue)) {

					containsFilterValue = true;
					break;
				}
			}

			if (!containsFilterValue) {

				StringBuilder sb = new StringBuilder();
				sb.append(" (");
				for (String string : this.pollutants) {
					sb.append(string).append(", ");
				}
				sb.deleteCharAt(sb.length() - 1);
				sb.deleteCharAt(sb.length() - 1);
				sb.append(")");
				throw new KMZGeneratorException(
						KMZGeneratorException.ERROR_CODE_INCORRECT_VALUE,
						"Filter " + this.filterValue + " must be one of "
								+ sb.toString());
			}
		}

		if (ConfigurationManager.getInstance().getValueAsBoolean(
				ConfigurationManager.PropertyKey.SHOW_OUTPUT.getKey())) {

			System.out.println("Pollutants:");
			for (String string : this.pollutants) {
				System.out.println("  " + string);
			}
		}

		if (this.absMaxValue < this.dataMinimum) {

			throw new KMZGeneratorException(
					KMZGeneratorException.ERROR_CODE_INCORRECT_VALUE,
					"Data minimum cutoff " + this.dataMinimum
							+ " must be less than maximum value "
							+ this.absMaxValue);
		}

		if (this.absMinValue > this.dataMaximum) {

			throw new KMZGeneratorException(
					KMZGeneratorException.ERROR_CODE_INCORRECT_VALUE,
					"Data maximum cutoff " + this.dataMaximum
							+ " must be greater than minimum value "
							+ this.absMinValue);
		}

		if (ConfigurationManager.getInstance().getValueAsBoolean(
				ConfigurationManager.PropertyKey.SHOW_OUTPUT.getKey())) {
			System.out.println("min: " + this.minValue + ", max: "
					+ this.maxValue);
		}

		Collections.sort(this.records, new Comparator<Record>() {

			@Override
			public int compare(Record record1, Record record2) {

				if (labelColumnIndex == -1) {
					labelColumnIndex = record1.getIndex(labelColumnName);
				}

				return record1.getValue(labelColumnIndex).compareTo(
						record2.getValue(labelColumnIndex));
			}

		});

		long t2 = System.currentTimeMillis();

		if (ConfigurationManager.getInstance().getValueAsBoolean(
				ConfigurationManager.PropertyKey.SHOW_OUTPUT.getKey())) {
			System.out.println("Time to preprocess records: " + (t2 - t1)
					+ "ms");
		}

		this.meanValue = this.calculateMean(values);
	}

	private double calculateMean(List<Double> values) {

		double mean = 0;

		if (!values.isEmpty()) {

			double sum = 0;
			for (Double double1 : values) {
				sum += double1;
			}

			mean = sum / values.size();
		}

		return mean;
	}

	private void processPropertiesManager(BinnedPointSourceGenerator generator)
			throws KMZGeneratorException {

		PropertiesManager propertiesManager = PropertiesManager.getInstance();
		this.labelColumnName = propertiesManager
				.getValue(PropertyKey.LABEL_COLUMNNAME);
		this.filterColumnName = propertiesManager
				.getValue(PropertyKey.FILTER_COLUMNNAME);
		this.filterValue = propertiesManager.getValue(PropertyKey.FILTER_VALUE);
		this.dataColumnName = propertiesManager
				.getValue(PropertyKey.DATA_COLUMNNAME);

		PropertyKey propertyKey = PropertyKey.DATA_MINIMUM_CUTOFF;
		String value = propertiesManager.getValue(propertyKey);
		try {

			if (value == null || value.isEmpty()) {
				this.dataMinimum = -Double.MAX_VALUE;
			} else {
				this.dataMinimum = Double.parseDouble(value);
			}
		} catch (NumberFormatException e) {
			throw new KMZGeneratorException(
					KMZGeneratorException.ERROR_CODE_INCORRECT_VALUE_TYPE,
					"Value of '" + propertyKey.getDisplayName() + "' is '"
							+ value + "'. It must be numeric.");
		} catch (NullPointerException e) {
			throw new KMZGeneratorException(
					KMZGeneratorException.ERROR_CODE_INCORRECT_VALUE_TYPE,
					"Value of '" + propertyKey.getDisplayName()
							+ "' is empty. It must be numeric.");
		}

		propertyKey = PropertyKey.DATA_MAXIMUM_CUTOFF;
		value = propertiesManager.getValue(propertyKey);
		try {

			if (value == null || value.isEmpty()) {
				this.dataMaximum = Double.MAX_VALUE;
			} else {
				this.dataMaximum = Double.parseDouble(value);
			}
		} catch (NumberFormatException e) {
			throw new KMZGeneratorException(
					KMZGeneratorException.ERROR_CODE_INCORRECT_VALUE_TYPE,
					"Value of '" + propertyKey.getDisplayName() + "' is '"
							+ value + "'. It must be numeric.");
		} catch (NullPointerException e) {
			throw new KMZGeneratorException(
					KMZGeneratorException.ERROR_CODE_INCORRECT_VALUE_TYPE,
					"Value of '" + propertyKey.getDisplayName()
							+ "' is empty. It must be numeric.");
		}

		if (this.dataMaximum <= this.dataMinimum) {
			throw new KMZGeneratorException(
					KMZGeneratorException.ERROR_CODE_INCORRECT_VALUE,
					"Value of '"
							+ PropertyKey.DATA_MAXIMUM_CUTOFF.getDisplayName()
							+ "' (" + this.dataMaximum
							+ ") must be greater than the value of '"
							+ PropertyKey.DATA_MINIMUM_CUTOFF.getDisplayName()
							+ "' (" + this.dataMinimum + ").");
		}

		if (generator.isDiffPlot()) {

			propertyKey = PropertyKey.DATA_ZERO_CUTOFF;
			value = propertiesManager.getValue(propertyKey);
			try {
				double zeroCutoff = Double.parseDouble(value);

				if (!(this.dataMaximum > zeroCutoff && -zeroCutoff > this.dataMinimum)) {

					throw new KMZGeneratorException(
							KMZGeneratorException.ERROR_CODE_INCORRECT_VALUE,
							"Value of '"
									+ PropertyKey.DATA_MAXIMUM_CUTOFF
											.getDisplayName()
									+ "' ("
									+ this.dataMaximum
									+ ") must be greater than the value of '"
									+ PropertyKey.DATA_ZERO_CUTOFF
											.getDisplayName()
									+ "' ("
									+ zeroCutoff
									+ ") and value of '-"
									+ PropertyKey.DATA_ZERO_CUTOFF
											.getDisplayName()
									+ "' ("
									+ -zeroCutoff
									+ ") must be greater than the value of '"
									+ PropertyKey.DATA_MINIMUM_CUTOFF
											.getDisplayName() + "' ("
									+ this.dataMinimum + ").");
				}

			} catch (NumberFormatException e) {
				throw new KMZGeneratorException(
						KMZGeneratorException.ERROR_CODE_INCORRECT_VALUE_TYPE,
						"Value of '" + propertyKey.getDisplayName() + "' is '"
								+ value + "'. It must be numeric.");
			} catch (NullPointerException e) {
				throw new KMZGeneratorException(
						KMZGeneratorException.ERROR_CODE_INCORRECT_VALUE_TYPE,
						"Value of '" + propertyKey.getDisplayName()
								+ "' is empty. It must be numeric.");
			}
		}

		try {
			propertiesManager.getValueAsInt(PropertyKey.DECIMAL_PLACES);
		} catch (NumberFormatException e) {
			throw new KMZGeneratorException(
					KMZGeneratorException.ERROR_CODE_INCORRECT_VALUE_TYPE,
					"Value of '"
							+ PropertyKey.DECIMAL_PLACES.getDisplayName()
							+ "' is '"
							+ propertiesManager
									.getValue(PropertyKey.DECIMAL_PLACES)
							+ "'. It must be an integer value.");
		}
	}

	protected void validateColumnNames(Record record)
			throws KMZGeneratorException {

		if (record.getIndex(this.dataColumnName) < 0) {
			throw new KMZGeneratorException(
					KMZGeneratorException.ERROR_CODE_INCORRECT_KEY_VALUE, "'"
							+ PropertyKey.DATA_COLUMNNAME.getDisplayName()
							+ "' has value of '" + this.dataColumnName
							+ "'. It must be one of the following: "
							+ record.toStringKeys() + "\nPlease check input.");
		}

		if (record.getIndex(this.filterColumnName) < 0) {
			throw new KMZGeneratorException(
					KMZGeneratorException.ERROR_CODE_INCORRECT_KEY_VALUE, "'"
							+ PropertyKey.FILTER_COLUMNNAME.getDisplayName()
							+ "' has value of '" + this.filterColumnName
							+ "'. It must be one of the following: "
							+ record.toStringKeys() + "\nPlease check input.");
		}

		if (record.getIndex(this.labelColumnName) < 0) {
			throw new KMZGeneratorException(
					KMZGeneratorException.ERROR_CODE_INCORRECT_KEY_VALUE, "'"
							+ PropertyKey.LABEL_COLUMNNAME.getDisplayName()
							+ "' has value of '" + this.labelColumnName
							+ "'. It must be one of the following: "
							+ record.toStringKeys() + "\nPlease check input.");
		}

	}

	public List<Record> getRecords() {
		return records;
	}

	public ArrayList<Double> getValues() {
		return values;
	}

	public double getMaxValue() {
		return maxValue;
	}

	public double getMinValue() {
		return minValue;
	}

	public double getMeanValue() {
		return meanValue;
	}

	@Override
	public void preProcessRecords(BinnedMultiGeometrySourceGenerator generator)
			throws KMZGeneratorException {
		// TODO Auto-generated method stub
		
	}
}
