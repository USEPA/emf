package gov.epa.emissions.googleearth.kml.generator;

import gov.epa.emissions.googleearth.RecordProducer;
import gov.epa.emissions.googleearth.kml.ConfigurationManager;
import gov.epa.emissions.googleearth.kml.KMZGeneratorException;
import gov.epa.emissions.googleearth.kml.PropertiesManager;
import gov.epa.emissions.googleearth.kml.PropertiesManager.PropertyKey;
import gov.epa.emissions.googleearth.kml.record.Record;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PreProcessor {

	private RecordProducer recordProducer;
	private PropertiesManager propertiesManager;

	private List<Record> records;
	private ArrayList<Double> values;

	private ArrayList<String> pollutants;
	private String labelColumnName;
	private String filterColumnName;
	private String dataColumnName;
	private String filterDisplayName;
	private String dataDisplayName;

	private int filterColumnIndex;
	private int dataColumnIndex;
	private int labelColumnIndex;

	private String filterValue;
	private double dataMinimum;

	private double absMinValue = Double.MAX_VALUE;
	private double absMaxValue = 0;

	private Double maxValue;
	private Double minValue;

	private int totalCount;
	private int processsedCount;

	public PreProcessor(RecordProducer recordProducer,
			PropertiesManager propertiesManager) {

		this.recordProducer = recordProducer;
		this.propertiesManager = propertiesManager;
	}

	public void preProcessRecords() throws KMZGeneratorException {

		this.labelColumnName = propertiesManager
				.getValue(PropertyKey.LABEL_COLUMNNAME.getKey());
		this.filterColumnName = propertiesManager
				.getValue(PropertyKey.FILTER_COLUMNNAME.getKey());
		this.filterValue = propertiesManager.getValue(PropertyKey.FILTER_VALUE
				.getKey());
		this.dataColumnName = propertiesManager
				.getValue(PropertyKey.DATA_COLUMNNAME.getKey());

		String key = PropertyKey.DATA_MINIMUM.getKey();
		String value = propertiesManager.getValue(key);
		try {
			this.dataMinimum = Double.parseDouble(value);
		} catch (NumberFormatException e) {
			throw new KMZGeneratorException(
					KMZGeneratorException.ERROR_CODE_INCORRECT_VALUE_TYPE,
					"Value of '" + key + "' is '" + value
							+ "'. It must be numeric.");
		}

		long t1 = System.currentTimeMillis();
		this.records = new ArrayList<Record>();
		this.pollutants = new ArrayList<String>();
		this.values = new ArrayList<Double>();
		Record nextRecord = null;

		boolean first = true;
		while ((nextRecord = recordProducer.nextRecord()) != null) {

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

				Double dataValue = new Double(nextRecord
						.getValue(this.dataColumnIndex));
				if (dataValue > this.absMaxValue) {
					this.absMaxValue = dataValue;
				} else if (dataValue < this.absMinValue) {
					this.absMinValue = dataValue;
				}

				this.totalCount++;
				if ((this.filterValue != null && this.filterValue
						.equalsIgnoreCase(pollutant))
						&& dataValue >= this.dataMinimum) {

					this.processsedCount++;
					this.records.add(nextRecord);
					this.getValues().add(dataValue);

					if (dataValue > this.maxValue) {
						this.maxValue = dataValue;
					} else if (dataValue < this.minValue) {
						this.minValue = dataValue;
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
					KMZGeneratorException.ERROR_CODE_INCORRECT_VALUE, "Filter "
							+ this.filterValue + " must be one of "
							+ sb.toString());
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

			System.out.println(this.processsedCount
					+ " records processed out of " + this.totalCount);
		}

		if (ConfigurationManager.getInstance().getValueAsBoolean(
				ConfigurationManager.PropertyKey.SHOW_OUTPUT.getKey())) {
			System.out.println("Time to preprocess records: " + (t2 - t1)
					+ "ms");
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

	public Double getMaxValue() {
		return maxValue;
	}
}
