package gov.epa.emissions.googleearth.kml.generator;

import gov.epa.emissions.googleearth.CSVRecordReader;
import gov.epa.emissions.googleearth.RecordProducer;
import gov.epa.emissions.googleearth.kml.ConfigurationManager;
import gov.epa.emissions.googleearth.kml.KMZGeneratorException;
import gov.epa.emissions.googleearth.kml.PropertiesManager;
import gov.epa.emissions.googleearth.kml.PropertiesManager.PropertyKey;
import gov.epa.emissions.googleearth.kml.bin.ModifiedEqualBinStrategy;
import gov.epa.emissions.googleearth.kml.image.ImageWriter;
import gov.epa.emissions.googleearth.kml.image.LegendImageWriter;
import gov.epa.emissions.googleearth.kml.image.LegendStatsWriter;
import gov.epa.emissions.googleearth.kml.image.LegendTitleWriter;
import gov.epa.emissions.googleearth.kml.record.Record;
import gov.epa.emissions.googleearth.kml.utils.KMLUtils;
import gov.epa.emissions.googleearth.kml.utils.Utils;
import gov.epa.emissions.googleearth.kml.validator.CLIInputValidator;
import gov.epa.emissions.googleearth.kml.validator.GUIInputValidator;
import gov.epa.emissions.googleearth.kml.version.Version;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class BinnedPointSourceGenerator {

	private PropertiesManager propertiesManager;
	private RecordProducer recordProducer;

	private File inputFile;
	private File outputFile;
	private int processsedCount = 0;
	private int totalCount = 0;
	private List<Record> records = new ArrayList<Record>();
	private List<List<Record>> binnedRecords = new ArrayList<List<Record>>();

	private List<Integer> rgbs;
	private List<File> images;
	private File legend;
	private File titleLegend;
	private File statsLegend;
	private boolean drawTitleLegend;

	private double minValue = Double.MAX_VALUE;
	private double maxValue = 0;
	private double meanValue = 0;

	private double[] bins = new double[0];
	private int[] binCount = new int[0];

	private List<String> pollutants;
	private List<Double> values;
	private double absMinValue = Double.MAX_VALUE;
	private double absMaxValue = 0;

	private String labelColumnName;
	private String filterColumnName;
	private String filterValue;
	private String dataColumnName;
	private double dataMinimum;
	private double iconScale;

	private String labelDisplayName;
	private String filterDisplayName;
	private String dataDisplayName;

	private int labelColumnIndex = -1;
	private int filterColumnIndex = -1;
	private int dataColumnIndex = -1;

	private StringBuilder indent = new StringBuilder();

	private static final String IMAGES_DIR = "images";

	public BinnedPointSourceGenerator(RecordProducer recordProducer,
			File inputFile, File outputFile, PropertiesManager propertiesManager) {

		this.recordProducer = recordProducer;
		this.propertiesManager = propertiesManager;
		this.inputFile = inputFile;
		this.outputFile = outputFile;

		this.initRGBs();
	}

	private void initRGBs() {

		List<Integer> rgbs = new ArrayList<Integer>();
		rgbs.add(Color.BLUE.getRGB());
		rgbs.add(Color.CYAN.getRGB());
		rgbs.add(Color.GREEN.getRGB());
		rgbs.add(Color.YELLOW.getRGB());
		rgbs.add(Color.ORANGE.getRGB());
		rgbs.add(Color.RED.getRGB());
		this.setImageColors(rgbs);
	}

	public void setImageColors(List<Integer> rgbs) {

		this.rgbs = rgbs;
		int numberOfBins = this.rgbs.size();
		this.bins = new double[numberOfBins];
		this.binCount = new int[numberOfBins];
	}

	private void generateImages() {

		this.images = new ArrayList<File>();

		ImageWriter imageWriter = new ImageWriter(16);

		try {

			List<Color> colors = new ArrayList<Color>();
			for (Integer rgb : this.rgbs) {
				colors.add(new Color(rgb));
			}

			for (Color color : colors) {
				imageWriter.drawImage(color);
				File file = File.createTempFile("icon", ".png");
				this.images.add(file);
				imageWriter.writeImage(file);
			}

			List<String> ranges = new ArrayList<String>();
			for (int i = 0; i < this.rgbs.size(); i++) {

				String minStr = Utils.format(this.bins[i], Utils
						.getFormat(this.bins[i]));
				ranges.add(minStr);
			}

			LegendImageWriter legendImageWriter = new LegendImageWriter();
			legendImageWriter.drawImage(colors, ranges);
			this.legend = File.createTempFile("legend", ".png");
			legendImageWriter.writeImage(this.legend);

			String title = PropertiesManager.getInstance().getValue(
					PropertyKey.PLOT_TITLE.getKey());
			String subtitle = PropertiesManager.getInstance().getValue(
					PropertyKey.PLOT_SUBTITLE.getKey());

			LegendTitleWriter legendTitleWriter = new LegendTitleWriter();
			legendTitleWriter.drawImage(title, subtitle);

			this.drawTitleLegend = !legendTitleWriter.isEmpty();
			this.titleLegend = File.createTempFile("titleLegend", ".png");
			legendTitleWriter.writeImage(this.titleLegend);

			LegendStatsWriter legendStatsWriter = new LegendStatsWriter();
			legendStatsWriter.drawImage(this.minValue, this.maxValue,
					this.meanValue);

			this.statsLegend = File.createTempFile("statsLegend", ".png");
			legendStatsWriter.writeImage(this.statsLegend);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void generate() throws KMZGeneratorException {

		this.labelColumnName = propertiesManager
				.getValue(PropertyKey.LABEL_COLUMNNAME.getKey());
		this.filterColumnName = propertiesManager
				.getValue(PropertyKey.FILTER_COLUMNNAME.getKey());
		this.filterValue = propertiesManager.getValue(PropertyKey.FILTER_VALUE
				.getKey());
		this.dataColumnName = propertiesManager
				.getValue(PropertyKey.DATA_COLUMNNAME.getKey());

		PropertyKey propertyKey = PropertyKey.DATA_MINIMUM;
		String value = propertiesManager.getValue(propertyKey.getKey());
		try {
			this.dataMinimum = Double.parseDouble(value);
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

		propertyKey = PropertyKey.ICON_SCALE;
		value = propertiesManager.getValue(propertyKey.getKey());
		try {
			this.iconScale = Double.parseDouble(value);
			if (this.iconScale <= 0 || this.iconScale > 1) {
				throw new KMZGeneratorException(
						KMZGeneratorException.ERROR_CODE_INCORRECT_VALUE_TYPE,
						"Value of '" + propertyKey.getDisplayName() + "' is '"
								+ value
								+ "'. It must be numeric, between 0 and 1.");
			}

		} catch (NumberFormatException e) {
			throw new KMZGeneratorException(
					KMZGeneratorException.ERROR_CODE_INCORRECT_VALUE_TYPE,
					"Value of '" + propertyKey.getDisplayName() + "' is '"
							+ value + "'. It must be numeric, between 0 and 1.");
		} catch (NullPointerException e) {
			throw new KMZGeneratorException(
					KMZGeneratorException.ERROR_CODE_INCORRECT_VALUE_TYPE,
					"Value of '"
							+ propertyKey.getDisplayName()
							+ "' is empty. It must be numeric, between 0 and 1.");
		}

		this.preProcessRecords(this.recordProducer);

		if (this.values.isEmpty()) {

			throw new KMZGeneratorException(
					KMZGeneratorException.ERROR_CODE_UNABLE_TO_WRITE_KML_DOCUMENT,
					"No data remained after processing. Please check input.");
		} else {

			// this.bins = new EqualBinStrategy(this.rgbs.size(), this.values)
			// .createBins();
			this.bins = new ModifiedEqualBinStrategy(this.rgbs.size(),
					this.values).createBins();

			this.generateImages();

			this.binRecords(this.records);

			try {

				FileOutputStream fos = new FileOutputStream(outputFile);
				OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
				BufferedWriter bw = new BufferedWriter(osw);

				this.writeDocument(bw);

				bw.flush();
				bw.close();

				if (ConfigurationManager.getInstance().getValueAsBoolean(
						ConfigurationManager.PropertyKey.SHOW_OUTPUT.getKey())) {

					System.out.println(this.processsedCount
							+ " records processed out of " + this.totalCount);

					for (int i = 0; i < this.binCount.length; i++) {
						System.out.println("bin[" + i + "] contains "
								+ this.binCount[i]);
					}
				}

			} catch (IOException e) {
				throw new KMZGeneratorException(
						KMZGeneratorException.ERROR_CODE_UNABLE_TO_WRITE_KML_DOCUMENT,
						e.getLocalizedMessage());
			}
		}
	}

	public static String createDefaultFileName(
			PropertiesManager propertiesManager, String inputFileName) {

		StringBuilder sb = new StringBuilder();

		String label = propertiesManager
				.getValue(PropertiesManager.PropertyKey.LABEL_COLUMNNAME
						.getKey());
		String filterColumnName = propertiesManager
				.getValue(PropertiesManager.PropertyKey.FILTER_COLUMNNAME
						.getKey());
		String filterValue = propertiesManager
				.getValue(PropertiesManager.PropertyKey.FILTER_VALUE.getKey());
		String dataColumnName = propertiesManager
				.getValue(PropertiesManager.PropertyKey.DATA_COLUMNNAME
						.getKey());
		String dataMinimum = propertiesManager
				.getValue(PropertiesManager.PropertyKey.DATA_MINIMUM.getKey());

		sb.append(getBeginingOfFileName(inputFileName, 12)).append("_").append(
				label).append("_").append(filterColumnName).append("_").append(
				filterValue).append("_").append(dataColumnName).append("_")
				.append(dataMinimum).append(".kmz");
		return sb.toString();
	}

	private static String getBeginingOfFileName(String fileName) {

		String retVal = null;

		String temp = fileName;
		if (fileName.contains("\\")) {

			String[] split = fileName.split("\\\\");
			temp = split[split.length - 1];
		} else if (fileName.contains("/")) {

			String[] split = fileName.split("/");
			temp = split[split.length - 1];
		}

		while (temp.length() > 0) {

			if (temp.startsWith("_") || temp.startsWith(".")) {
				temp = temp.substring(1);
			} else {
				break;
			}
		}

		String[] split = temp.split("_");
		if (split.length == 1) {

			split = temp.split(".");
			retVal = split[0];
		} else {
			retVal = split[0];
		}

		return retVal;
	}

	private static String getBeginingOfFileName(String fileName, int length) {

		String retVal = fileName;

		if (retVal.length() >= length) {
			retVal = retVal.substring(0, length);
		}

		return retVal;
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

	protected void preProcessRecords(RecordProducer recordProducer)
			throws KMZGeneratorException {

		long t1 = System.currentTimeMillis();
		this.pollutants = new ArrayList<String>();
		this.values = new ArrayList<Double>();
		Record nextRecord = null;

		boolean first = true;
		while ((nextRecord = recordProducer.nextRecord()) != null) {

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

				double value = Double.valueOf(nextRecord
						.getValue(this.dataColumnIndex));
				if (value > this.absMaxValue) {
					this.absMaxValue = value;
				} else if (value < this.absMinValue) {
					this.absMinValue = value;
				}

				this.totalCount++;
				if ((this.filterValue == null || this.filterValue.isEmpty() || this.filterValue
						.equalsIgnoreCase(pollutant))
						&& value >= this.dataMinimum) {

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

	protected void binRecords(List<Record> records) {

		for (int i = 0; i < this.rgbs.size(); i++) {
			this.binnedRecords.add(new ArrayList<Record>());
		}

		for (Record record : records) {

			int binNumber = 0;
			double value = Double
					.valueOf(record.getValue(this.dataColumnIndex));

			if (value > 0) {
				for (binNumber = 0; binNumber < this.bins.length; binNumber++) {

					if (this.dataColumnIndex == -1) {
						this.dataColumnIndex = record
								.getIndex(this.dataColumnName);
					}

					if (this.dataDisplayName == null) {
						this.dataDisplayName = record
								.getDisplayKey(this.dataColumnIndex);
					}

					if (value < this.bins[binNumber]) {
						break;
					}
				}
			}

			if (binNumber != 0) {
				binNumber--;
			}

			this.binnedRecords.get(binNumber).add(record);
		}
	}

	protected void writeDocument(Writer writer) throws IOException {

		String fileName = this.inputFile.getName();
		fileName = Utils.stripExtension(fileName);

		StringBuilder sb = new StringBuilder();
		sb.append(KMLUtils.createXMLElement());
		sb.append(KMLUtils.openKMLElement(this.indent));
		sb.append(KMLUtils.openDocumentElement(this.indent));

		System.out.println("Max: " + this.maxValue);
		System.out.println("Min: " + this.minValue);
		System.out.println("Mean: " + this.meanValue);

		if (this.drawTitleLegend) {

			sb.append(KMLUtils.openScreenOverlayElement(this.indent));
			sb.append(KMLUtils.createNameElement("Title", this.indent));
			sb.append(KMLUtils.createVisibilityElement("1", this.indent));
			sb.append(KMLUtils.createIconElement(IMAGES_DIR + "/"
					+ this.titleLegend.getName(), this.indent));
			sb.append(KMLUtils.createOverlayXYElement("0", "1", this.indent));
			sb.append(KMLUtils.createScreenXYElement("0", "1", this.indent));
			sb.append(KMLUtils.createRotationXYElement("0", "0", this.indent));
			sb.append(KMLUtils.createSizeElement("0", "0", this.indent));
			sb.append(KMLUtils.closeScreenOverlayElement(this.indent));
		}

		sb.append(KMLUtils.openScreenOverlayElement(this.indent));
		sb.append(KMLUtils.createNameElement("Legend", this.indent));
		sb.append(KMLUtils.createVisibilityElement("1", this.indent));
		sb.append(KMLUtils.createIconElement(IMAGES_DIR + "/"
				+ this.legend.getName(), this.indent));
		sb.append(KMLUtils.createOverlayXYElement("1", "0.2", this.indent));
		sb.append(KMLUtils.createScreenXYElement("1", "0.2", this.indent));
		sb.append(KMLUtils.createRotationXYElement("0", "0", this.indent));
		sb.append(KMLUtils.createSizeElement("0", "0", this.indent));
		sb.append(KMLUtils.closeScreenOverlayElement(this.indent));

		sb.append(KMLUtils.openScreenOverlayElement(this.indent));
		sb.append(KMLUtils.createNameElement("Stats", this.indent));
		sb.append(KMLUtils.createVisibilityElement("1", this.indent));
		sb.append(KMLUtils.createIconElement(IMAGES_DIR + "/"
				+ this.statsLegend.getName(), this.indent));
		sb.append(KMLUtils.createOverlayXYElement("0", "-1", this.indent));
		sb.append(KMLUtils.createScreenXYElement("0", ".1", this.indent));
		sb.append(KMLUtils.createRotationXYElement("0", "0", this.indent));
		sb.append(KMLUtils.createSizeElement("0", "0", this.indent));
		sb.append(KMLUtils.closeScreenOverlayElement(this.indent));

		sb.append(KMLUtils.createNameElement(fileName, this.indent));
		sb.append(KMLUtils.createVisibilityElement("1", this.indent));
		sb.append(KMLUtils.createOpenElement("1", this.indent));
		sb.append(KMLUtils.openSnippetElement(2, this.indent));

		if (this.filterValue != null) {

			sb.append(KMLUtils.addValue("This is a view of the "
					+ this.dataDisplayName.toLowerCase() + " for "
					+ this.filterDisplayName.toLowerCase() + " "
					+ this.filterValue.toUpperCase() + " with min cutoff of "
					+ this.dataMinimum + ".", this.indent));
			sb.append(KMLUtils.closeSnippetElement(this.indent));
			sb.append(KMLUtils.createDescriptionElement(
					"This is a view of the "
							+ this.dataDisplayName.toLowerCase() + " for "
							+ this.filterDisplayName.toLowerCase() + " "
							+ this.filterValue.toUpperCase()
							+ " with min cutoff of " + this.dataMinimum + ".\n"
							+ "(Source file: " + fileName + ")", this.indent));
		} else {

			sb.append(KMLUtils.addValue("This is a view of the "
					+ this.dataDisplayName.toLowerCase()
					+ " with min cutoff of " + this.dataMinimum + ".",
					this.indent));
			sb.append(KMLUtils.closeSnippetElement(this.indent));
			sb.append(KMLUtils.createDescriptionElement(
					"This is a view of the "
							+ this.dataDisplayName.toLowerCase()
							+ " with min cutoff of " + this.dataMinimum + ".\n"
							+ "(Source file: " + fileName + ")", this.indent));
		}

		writer.write(sb.toString());

		writer.write(KMLUtils.createLookAt(Double.toString(-94), Double
				.toString(38.5), Double.toString(4000000), Double
				.toString(4000000), Double.toString(0), Double.toString(0),
				this.indent));

		int binNumber = 1;
		for (Integer rgb : this.rgbs) {
			this.writeStyleMap(writer, "bin" + binNumber, IMAGES_DIR + "/"
					+ binNumber + ".png");
			binNumber++;
		}
		int count = 1;
		for (List<Record> bin : this.binnedRecords) {
			this.writeBinFolder(writer, bin, count++);
		}

		writer.write(KMLUtils.closeDocumentElement(this.indent));
		writer.write(KMLUtils.closeKMLElement(this.indent));
	}

	protected void writeStyleMap(Writer writer, String styleMapName,
			String imagePath) throws IOException {

		StringBuilder sb = new StringBuilder();

		sb
				.append(KMLUtils.openStyleElement(styleMapName + "List",
						this.indent));
		sb.append(KMLUtils.createListStyleElement(imagePath, this.indent));
		sb.append(KMLUtils.closeStyleElement(this.indent));

		sb.append(KMLUtils.openStyleElement(styleMapName + "_normal",
				this.indent));
		sb.append(KMLUtils.createIconStyleElement(Double
				.toString(this.iconScale), imagePath, this.indent));
		sb.append(KMLUtils
				.createLabelStyleElement("0", "00000000", this.indent));
		sb.append(KMLUtils.closeStyleElement(this.indent));

		sb.append(KMLUtils.openStyleElement(styleMapName + "_highlighted",
				this.indent));
		sb.append(KMLUtils.createIconStyleElement(Double
				.toString(this.iconScale), imagePath, this.indent));
		sb.append(KMLUtils
				.createLabelStyleElement("1", "ffffffff", this.indent));
		sb.append(KMLUtils.closeStyleElement(this.indent));

		sb.append(KMLUtils.openStyleMapElement(styleMapName, this.indent));
		sb.append(KMLUtils.createNormalPairElement(styleMapName + "_normal",
				this.indent));
		sb.append(KMLUtils.createHighlightPairElement(styleMapName
				+ "_highlighted", this.indent));
		sb.append(KMLUtils.closeStyleMapElement(this.indent));

		writer.write(sb.toString());
	}

	protected void writeBinFolder(Writer writer, List<Record> records,
			int binNumber) throws IOException {

		StringBuilder sb = new StringBuilder();

		double max = this.maxValue;
		if (binNumber == this.rgbs.size()) {
			if (max >= 1) {
				max = Math.ceil(this.maxValue);
			} else {
				max = Utils.ceilDecimalPaces(max, 4);
			}
		} else {
			max = this.bins[binNumber];
		}

		double min = this.bins[binNumber - 1];
		String minStr = Utils.format(min, Utils.getFormat(min));
		String maxStr = Utils.format(max, Utils.getFormat(max));

		sb.append(KMLUtils.openFolderElement(this.indent));

		if (min == max) {
			sb.append(KMLUtils.createNameElement("Value: " + minStr,
					this.indent));
		} else {
			sb.append(KMLUtils.createNameElement("Range: " + minStr + " to "
					+ maxStr, this.indent));
		}

		sb.append(KMLUtils.createVisibilityElement("1", this.indent));

		if (this.filterValue != null) {

			if (min == max) {
				sb.append(KMLUtils.createDescriptionElement("This bin has "
						+ records.size() + " sites whose "
						+ this.dataDisplayName.toLowerCase() + " for "
						+ this.filterDisplayName.toLowerCase() + " "
						+ this.filterValue.toUpperCase() + " are equal to "
						+ minStr, this.indent));
			} else {
				sb.append(KMLUtils.createDescriptionElement("This bin has "
						+ records.size() + " sites whose "
						+ this.dataDisplayName.toLowerCase() + " for "
						+ this.filterDisplayName.toLowerCase() + " "
						+ this.filterValue.toUpperCase() + " are between "
						+ minStr + " and " + maxStr, this.indent));
			}

		} else {

			if (min == max) {
				sb.append(KMLUtils.createDescriptionElement("This bin has "
						+ records.size() + " sites whose "
						+ this.dataDisplayName.toLowerCase() + " are equal to "
						+ minStr, this.indent));
			} else {
				sb.append(KMLUtils.createDescriptionElement("This bin has "
						+ records.size() + " sites whose "
						+ this.dataDisplayName.toLowerCase() + " are between "
						+ minStr + " and " + maxStr, this.indent));
			}
		}

		sb.append(KMLUtils.createStyleUrlElement("bin" + binNumber + "List",
				this.indent));

		writer.write(sb.toString());

		writer.write(KMLUtils.createLookAt(Double.toString(-94), Double
				.toString(38.5), Double.toString(4000000), Double
				.toString(4000000), Double.toString(0), Double.toString(0),
				this.indent));

		for (Record record : records) {
			this.writePlacemark(writer, record);
		}

		writer.write(KMLUtils.closeFolderElement(this.indent));
	}

	protected void writePlacemark(Writer writer, Record record)
			throws IOException {

		if (this.labelColumnIndex == -1) {
			this.labelColumnIndex = record.getIndex(this.labelColumnName);
		}

		if (this.labelDisplayName == null) {
			this.labelDisplayName = record.getDisplayKey(this.labelColumnIndex);
		}

		if (this.dataColumnIndex == -1) {
			this.dataColumnIndex = record.getIndex(this.dataColumnName);
		}

		if (this.dataDisplayName == null) {
			this.dataDisplayName = record.getDisplayKey(this.dataColumnIndex);
		}

		StringBuilder sb = new StringBuilder();

		String dataValue = record.getValue(this.dataColumnIndex);
		Double numericDataValue = null;
		try {
			numericDataValue = Double.valueOf(dataValue);
			dataValue = Utils.format(numericDataValue, Utils
					.getFormat(numericDataValue));
		} catch (NumberFormatException e) {
		}

		sb.append(KMLUtils.openPlacemarkElement(this.indent));
		sb.append(KMLUtils.createNameElement(record
				.getValue(this.labelColumnIndex)
				+ ":  " + dataValue, this.indent));
		sb.append(KMLUtils.openSnippetElement(4, this.indent));
		sb.append(KMLUtils.addValue(record.getDisplayKey(0) + ": "
				+ record.getValue(0) + this.indent + ", "
				+ record.getDisplayKey(1) + ": " + record.getValue(1),
				this.indent));
		sb.append(KMLUtils.addValue("Location: (" + record.getHorizontal()
				+ ", " + record.getVertical() + ")", this.indent));
		sb.append(KMLUtils.closeSnippetElement(this.indent));
		sb.append(KMLUtils.openDescriptionElement(this.indent));
		sb.append(KMLUtils.addValue("<![CDATA[\n", this.indent));

		for (int i = 0; i < record.getColumnCount(); i++) {

			String value = record.getValue(i);
			if (value == null || value.isEmpty()) {
				value = "N/A";
			}

			sb.append(KMLUtils.addValue(record.getDisplayKey(i) + ": " + value
					+ "<br/>", this.indent));
		}

		sb.append(KMLUtils.addValue("<p/>", this.indent));
		sb.append(KMLUtils.addValue("<table border='0'>", this.indent));
		sb.append(KMLUtils.addValue("]]>", this.indent));
		sb.append(KMLUtils.closeDescriptionElement(this.indent));

		double value = Double.valueOf(record.getValue(this.dataColumnIndex));
		int binNumber = 0;
		if (value > 0) {
			for (; binNumber < this.bins.length; binNumber++) {
				if (value < this.bins[binNumber]) {
					break;
				}
			}
		}

		if (binNumber == 0) {
			binNumber = 1;
		}

		sb.append(KMLUtils
				.createStyleUrlElement("bin" + binNumber, this.indent));

		writer.write(sb.toString());

		if (binNumber != 0) {
			this.binCount[binNumber - 1]++;
		} else {
			this.binCount[binNumber]++;
		}

		writer.write(KMLUtils.createLookAt(record.getVertical(), record
				.getHorizontal(), Double.toString(500000), Double
				.toString(500000), Double.toString(0), Double.toString(0),
				this.indent));

		writer.write(KMLUtils.createPoint(record.getVertical(), record
				.getHorizontal(), "0", this.indent));
		writer.write(KMLUtils.closePlacemarkElement(this.indent));
	}

	public static boolean validateCLIInput(String[] args)
			throws KMZGeneratorException {
		return CLIInputValidator.getInstance().validateInput(args);
	}

	public static boolean validateGUIInput(String[] args)
			throws KMZGeneratorException {
		return GUIInputValidator.getInstance().validateInput(args);
	}

	public static String getInputFileName(String[] args) {

		String retVal = null;

		for (int i = 0; i < args.length; i++) {

			if (CLIInputValidator.DATA_FILE_KEY.equals(args[i])
					&& i < args.length - 1) {

				retVal = args[i + 1];
				break;
			}
		}

		return retVal;
	}

	public File getLegend() {
		return legend;
	}

	public File getTitleLegend() {
		return this.titleLegend;
	}

	public File getStatsLegend() {
		return this.statsLegend;
	}

	public List<File> getImages() {
		return images;
	}

	public static void zipResults(File tempFile, File outputFile,
			List<File> images, File legend, File titleLegend, File statsLegend)
			throws IOException {

		outputFile.getParentFile().mkdir();

		List<String> fileNames = new ArrayList<String>();
		fileNames.add(tempFile.getAbsolutePath());
		fileNames.add(legend.getAbsolutePath());
		fileNames.add(titleLegend.getAbsolutePath());
		fileNames.add(statsLegend.getAbsolutePath());
		for (File file : images) {
			fileNames.add(file.getAbsolutePath());
		}

		List<String> fakeNames = new ArrayList<String>();
		fakeNames.add(tempFile.getName());
		fakeNames.add(IMAGES_DIR + "/" + legend.getName());
		fakeNames.add(IMAGES_DIR + "/" + titleLegend.getName());
		fakeNames.add(IMAGES_DIR + "/" + statsLegend.getName());
		int binNumber = 1;
		for (File file : images) {
			fakeNames.add(IMAGES_DIR + "/" + binNumber++ + ".png");
		}

		// Create a buffer for reading the files
		byte[] buf = new byte[1024];

		// Create the ZIP file
		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(
				outputFile));

		// Compress the files
		for (int i = 0; i < fileNames.size(); i++) {

			String filename = fileNames.get(i);
			InputStream in = new FileInputStream(filename);

			// Add ZIP entry to output stream.
			out.putNextEntry(new ZipEntry(fakeNames.get(i)));

			// Transfer bytes from the file to the ZIP file
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}

			// Complete the entry
			out.closeEntry();
			in.close();
		}

		// Complete the ZIP file
		out.close();

		if (ConfigurationManager.getInstance().getValueAsBoolean(
				ConfigurationManager.PropertyKey.SHOW_OUTPUT.getKey())) {
			System.out.println("Successfully created file '"
					+ outputFile.getAbsolutePath() + "'");
		}
	}

	public static void main(String[] args) throws Exception {

		ConfigurationManager configManager = ConfigurationManager.getInstance();
		configManager.setValue(ConfigurationManager.PropertyKey.SHOW_OUTPUT
				.getKey(), Boolean.TRUE.toString());

		boolean showOutput = ConfigurationManager.getInstance()
				.getValueAsBoolean(
						ConfigurationManager.PropertyKey.SHOW_OUTPUT.getKey());

		try {

			boolean shouldRun = BinnedPointSourceGenerator
					.validateCLIInput(args);

			if (shouldRun) {

				if (showOutput) {
					System.out.println(new Version().getVersion());
				}

				PropertiesManager.getInstance().initProperties(args);

				File tempFile = File.createTempFile("kml", ".kml");

				File inputFile = new File(BinnedPointSourceGenerator
						.getInputFileName(args));
				String outputFileName = BinnedPointSourceGenerator
						.createDefaultFileName(PropertiesManager.getInstance(),
								inputFile.getName());

				BinnedPointSourceGenerator generator = new BinnedPointSourceGenerator(
						new CSVRecordReader(inputFile), inputFile, tempFile,
						PropertiesManager.getInstance());
				generator.generate();

				File outputFile = new File(inputFile.getParentFile()
						.getAbsolutePath()
						+ "/" + outputFileName);

				List<File> images = generator.getImages();
				File legend = generator.getLegend();
				File titleLegend = generator.getTitleLegend();
				File statsLegend = generator.getStatsLegend();

				BinnedPointSourceGenerator.zipResults(tempFile, outputFile,
						images, legend, titleLegend, statsLegend);

				/*
				 * delete temporary files
				 */
				tempFile.delete();

				for (File file : images) {
					file.delete();
				}

				legend.delete();
			}

		} catch (IOException e) {

			if (showOutput) {
				System.err.println("Error while generating kmz file: "
						+ e.getLocalizedMessage() + "\nExiting.");
			}
			System.exit(-1);
		} catch (KMZGeneratorException e) {

			if (showOutput) {
				System.err.println("Illegal input: " + e.getLocalizedMessage()
						+ "\nExiting.");
			}
			System.exit(-1);
		} catch (Exception e) {

			e.printStackTrace();
			if (showOutput) {
				System.err.println("Error while generating kmz file: "
						+ e.getLocalizedMessage() + "\nExiting.");
			}
			System.exit(-1);
		}
	}
}
