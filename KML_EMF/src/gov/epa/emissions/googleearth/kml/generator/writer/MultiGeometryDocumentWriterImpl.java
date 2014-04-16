package gov.epa.emissions.googleearth.kml.generator.writer;

import gov.epa.emissions.googleearth.kml.ConfigurationManager;
import gov.epa.emissions.googleearth.kml.KMZGeneratorException;
import gov.epa.emissions.googleearth.kml.PropertiesManager;
import gov.epa.emissions.googleearth.kml.PropertiesManager.PropertyKey;
import gov.epa.emissions.googleearth.kml.bin.Range;
import gov.epa.emissions.googleearth.kml.generator.BinRangeManager;
import gov.epa.emissions.googleearth.kml.generator.BinnedMultiGeometrySourceGenerator;
import gov.epa.emissions.googleearth.kml.generator.BinnedPointSourceGenerator;
import gov.epa.emissions.googleearth.kml.generator.OverlayPosition;
import gov.epa.emissions.googleearth.kml.generator.image.ImageGenerator;
import gov.epa.emissions.googleearth.kml.generator.preprocessor.PreProcessor;
import gov.epa.emissions.googleearth.kml.record.Record;
import gov.epa.emissions.googleearth.kml.utils.KMLUtils;
import gov.epa.emissions.googleearth.kml.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class MultiGeometryDocumentWriterImpl implements DocumentWriter {

	private StringBuilder indent;

	private int dataColumnIndex = -1;
	private String dataColumnName;
	private String dataDisplayName;

	private double dataMinimum;
	private double dataMaximum;
	private String filterValue;

	private String filterDisplayName;
	private String labelColumnName;
	private String filterColumnName;

	private double iconScaleMin;
	private double iconScaleMax;

	private int labelColumnIndex = -1;
	private Object labelDisplayName;

	@Override
	public void writeDocument(Writer writer, File inputFile,
			BinnedPointSourceGenerator generator) throws IOException,
			KMZGeneratorException {

		List<List<Record>> binnedRecords = generator.getBinnedRecords();
		BinRangeManager binRangeManager = generator.getBinRangeManager();
		ImageGenerator imageGenerator = generator.getImageGenerator();

		this.processPropertiesManager();
		this.initializeDisplayNames(binnedRecords);

		this.indent = new StringBuilder();

		String fileName = inputFile.getName();
		fileName = Utils.stripExtension(fileName);

		StringBuilder sb = new StringBuilder();
		sb.append(KMLUtils.createXMLElement());
		sb.append(KMLUtils.openKMLElement(this.indent));
		sb.append(KMLUtils.openDocumentElement(this.indent));

		PreProcessor preProcessor = generator.getPreProcessor();

		if (ConfigurationManager.getInstance().getValueAsBoolean(
				ConfigurationManager.PropertyKey.SHOW_OUTPUT.getKey())) {

			System.out.println("Max: " + preProcessor.getMaxValue());
			System.out.println("Min: " + preProcessor.getMinValue());
			System.out.println("Mean: " + preProcessor.getMeanValue());
		}

		if (generator.getImageGenerator().shouldDrawTitleLegend()) {

			String positionDisplayName = PropertiesManager.getInstance()
					.getValue(PropertyKey.TITLE_POSITION);

			OverlayPosition titlePosition = OverlayPosition
					.getByDisplayName(positionDisplayName);

			if (titlePosition == null) {
				titlePosition = OverlayPosition.TOP_LEFT;
			}

			sb.append(KMLUtils.openScreenOverlayElement(this.indent));
			sb.append(KMLUtils.createNameElement("Title", this.indent));
			sb.append(KMLUtils.createVisibilityElement("1", this.indent));
			sb.append(KMLUtils.createIconElement(
					BinnedPointSourceGenerator.IMAGES_DIR + "/"
							+ imageGenerator.getTitleLegend().getName(),
					this.indent));
			sb.append(KMLUtils.createOverlayXYElement(titlePosition
					.getOverlayX()
					+ "", titlePosition.getOverlayY() + "", this.indent));
			sb.append(KMLUtils.createScreenXYElement("0", "1", this.indent));
			sb.append(KMLUtils.createRotationXYElement("0", "0", this.indent));
			sb.append(KMLUtils.createSizeElement("0", "0", this.indent));
			sb.append(KMLUtils.closeScreenOverlayElement(this.indent));
		}

		String positionDisplayName = PropertiesManager.getInstance().getValue(
				PropertyKey.LEGEND_POSITION);

		OverlayPosition legendPosition = OverlayPosition
				.getByDisplayName(positionDisplayName);

		if (legendPosition == null) {
			legendPosition = OverlayPosition.BOTTOM_RIGHT;
		}

		sb.append(KMLUtils.openScreenOverlayElement(this.indent));
		sb.append(KMLUtils.createNameElement("Legend", this.indent));
		sb.append(KMLUtils.createVisibilityElement("1", this.indent));
		sb.append(KMLUtils.createIconElement(
				BinnedPointSourceGenerator.IMAGES_DIR + "/"
						+ imageGenerator.getLegend().getName(), this.indent));
		sb.append(KMLUtils.createOverlayXYElement(legendPosition.getOverlayX()
				+ "", legendPosition.getOverlayY() + "", this.indent));
		sb.append(KMLUtils.createScreenXYElement(legendPosition.getScreenX()
				+ "", legendPosition.getScreenY() + "", this.indent));
		sb.append(KMLUtils.createRotationXYElement("0", "0", this.indent));
		sb.append(KMLUtils.createSizeElement("0", "0", this.indent));
		sb.append(KMLUtils.closeScreenOverlayElement(this.indent));

		if (generator.isShowStats()) {

			sb.append(KMLUtils.openScreenOverlayElement(this.indent));
			sb.append(KMLUtils.createNameElement("Stats", this.indent));
			sb.append(KMLUtils.createVisibilityElement("1", this.indent));

			sb.append(KMLUtils.createIconElement(
					BinnedPointSourceGenerator.IMAGES_DIR + "/"
							+ imageGenerator.getStatsLegend().getName(),
					this.indent));

			sb.append(KMLUtils.createOverlayXYElement("0", "-1", this.indent));
			sb.append(KMLUtils.createScreenXYElement("0", ".1", this.indent));
			sb.append(KMLUtils.createRotationXYElement("0", "0", this.indent));
			sb.append(KMLUtils.createSizeElement("0", "0", this.indent));
			sb.append(KMLUtils.closeScreenOverlayElement(this.indent));
		}

		sb.append(KMLUtils.createNameElement(fileName, this.indent));
		sb.append(KMLUtils.createVisibilityElement("1", this.indent));
		sb.append(KMLUtils.createOpenElement("1", this.indent));
		sb.append(KMLUtils.openSnippetElement(2, this.indent));

		if (this.filterValue != null) {

			sb.append(KMLUtils.addValue("This is a view of the "
					+ this.dataDisplayName.toLowerCase() + " for "
					+ this.filterDisplayName.toLowerCase() + " "
					+ this.filterValue.toUpperCase() + " with values between "
					+ this.dataMinimum + " and " + this.dataMaximum + ".",
					this.indent));
			sb.append(KMLUtils.closeSnippetElement(this.indent));
			sb.append(KMLUtils.createDescriptionElement(
					"This is a view of the "
							+ this.dataDisplayName.toLowerCase() + " for "
							+ this.filterDisplayName.toLowerCase() + " "
							+ this.filterValue.toUpperCase()
							+ " with values between " + this.dataMinimum
							+ " and " + this.dataMaximum + ".\n"
							+ "(Source file: " + fileName + ")", this.indent));
		} else {

			sb.append(KMLUtils.addValue("This is a view of the "
					+ this.dataDisplayName.toLowerCase()
					+ " with values between " + this.dataMinimum + " and "
					+ this.dataMaximum + ".", this.indent));
			sb.append(KMLUtils.closeSnippetElement(this.indent));
			sb.append(KMLUtils.createDescriptionElement(
					"This is a view of the "
							+ this.dataDisplayName.toLowerCase()
							+ " with values between " + this.dataMinimum
							+ " and " + this.dataMaximum + ".\n"
							+ "(Source file: " + fileName + ")", this.indent));
		}

		writer.write(sb.toString());

		writer.write(KMLUtils.createLookAt(Double.toString(-94), Double
				.toString(38.5), Double.toString(4000000), Double
				.toString(4000000), Double.toString(0), Double.toString(0),
				this.indent));

		ArrayList<File> images = imageGenerator.getImages();
		int binNumber = 0;
		for (File imageFile : images) {
			this.writeStyleMap(writer, binNumber, images.size(),
					BinnedPointSourceGenerator.IMAGES_DIR + "/"
							+ imageFile.getName());
			binNumber++;
		}

		for (binNumber = binnedRecords.size() - 1; binNumber >= 0; binNumber--) {
			this.writeBinFolder(writer, binnedRecords, binRangeManager,
					binNumber, generator.isDiffPlot());
		}

		writer.write(KMLUtils.closeDocumentElement(this.indent));
		writer.write(KMLUtils.closeKMLElement(this.indent));
	}

	private void initializeDisplayNames(List<List<Record>> binnedRecords) {

		if (!binnedRecords.isEmpty()) {

			for (List<Record> list : binnedRecords) {

				if (!list.isEmpty()) {

					Record record = list.get(0);

					String dataColumnName = PropertiesManager.getInstance()
							.getValue(PropertyKey.DATA_COLUMNNAME);
					this.dataColumnIndex = record.getIndex(dataColumnName);
					if (this.dataDisplayName == null) {

						this.dataDisplayName = record
								.getDisplayKey(this.dataColumnIndex);
					}

					this.filterColumnName = PropertiesManager.getInstance()
							.getValue(PropertyKey.FILTER_COLUMNNAME);
					int filterColumnIndex = record
							.getIndex(this.filterColumnName);
					if (this.filterDisplayName == null) {

						this.filterDisplayName = record
								.getDisplayKey(filterColumnIndex);
					}

					break;
				}
			}

		}
	}

	protected void writeStyleMap(Writer writer, int binNumber,
			int numberOfBins, String imagePath) throws IOException {

		double iconScale = this.iconScaleMin
				+ (this.iconScaleMax - this.iconScaleMin) / numberOfBins
				* binNumber;
		String styleMapName = "bin" + binNumber;
		StringBuilder sb = new StringBuilder();

		sb
				.append(KMLUtils.openStyleElement(styleMapName + "List",
						this.indent));
		sb.append(KMLUtils.createListStyleElement(imagePath, this.indent));
		sb.append(KMLUtils.closeStyleElement(this.indent));

		sb.append(KMLUtils.openStyleElement(styleMapName + "_normal",
				this.indent));
		sb.append(KMLUtils.createIconStyleElement(Double.toString(iconScale),
				imagePath, this.indent));
		sb.append(KMLUtils
				.createLabelStyleElement("0", "00000000", this.indent));
		sb.append(KMLUtils.closeStyleElement(this.indent));

		sb.append(KMLUtils.openStyleElement(styleMapName + "_highlighted",
				this.indent));
		sb.append(KMLUtils.createIconStyleElement(Double.toString(iconScale),
				imagePath, this.indent));
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

	protected void writeBinFolder(Writer writer,
			List<List<Record>> binnedRecords, BinRangeManager binRangeManager,
			int binNumber, boolean diffPlot) throws IOException {

		StringBuilder sb = new StringBuilder();

		// double max = this.maxValue;
		// if (binNumber == this.rgbs.size()) {
		// if (max >= 1) {
		// max = Math.ceil(this.maxValue);
		// } else {
		// max = Utils.ceilDecimalPaces(max, 4);
		// }
		// } else {
		// max = this.bins[binNumber];
		// }
		//
		// double min = this.bins[binNumber - 1];

		Range range = binRangeManager.getRange(binNumber);
		double min = range.getMin();
		double max = range.getMax();

		if (min < 0 && max < 0) {

			double temp = max;
			max = min;
			min = temp;
		}

		double minRange = binRangeManager.getMinRange();
		
		//String minStr = Utils.format(min, Utils.getFormat(min));
		//String maxStr = Utils.format(max, Utils.getFormat(max));
		String minStr = Utils.format(min, minRange); //Utils.format(min, Utils.getFormat(minRange));
		String maxStr = Utils.format(max, minRange);//Utils.format(max, Utils.getFormat(minRange));		
		

		sb.append(KMLUtils.openFolderElement(this.indent));

		if (min == max) {
			sb.append(KMLUtils.createNameElement("Value: " + minStr,
					this.indent));
		} else {
			sb.append(KMLUtils.createNameElement("Range: " + minStr + " to "
					+ maxStr, this.indent));
		}

		sb.append(KMLUtils.createVisibilityElement("1", this.indent));

		int numRecords = binnedRecords.get(binNumber).size();
		if (this.filterValue != null) {

			if (min == max) {
				sb.append(KMLUtils.createDescriptionElement("This bin has "
						+ numRecords + " sites whose "
						+ this.dataDisplayName.toLowerCase() + " for "
						+ this.filterDisplayName.toLowerCase() + " "
						+ this.filterValue.toUpperCase() + " are equal to "
						+ minStr, this.indent));
			} else {
				sb.append(KMLUtils.createDescriptionElement("This bin has "
						+ numRecords + " sites whose "
						+ this.dataDisplayName.toLowerCase() + " for "
						+ this.filterDisplayName.toLowerCase() + " "
						+ this.filterValue.toUpperCase() + " are between "
						+ minStr + " and " + maxStr, this.indent));
			}

		} else {

			if (min == max) {
				sb.append(KMLUtils.createDescriptionElement("This bin has "
						+ numRecords + " sites whose "
						+ this.dataDisplayName.toLowerCase() + " are equal to "
						+ minStr, this.indent));
			} else {
				sb.append(KMLUtils.createDescriptionElement("This bin has "
						+ numRecords + " sites whose "
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

		for (Record record : binnedRecords.get(binNumber)) {
			this.writePlacemark(writer, record, binRangeManager, diffPlot);
		}

		writer.write(KMLUtils.closeFolderElement(this.indent));
	}

	protected void writePlacemark(Writer writer, Record record,
			BinRangeManager binRangeManager, boolean diffPlot)
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

		sb.append(KMLUtils.openPlacemarkElement(this.indent));

		String stringValue = record.getValue(this.dataColumnIndex);
		if ((stringValue == null || stringValue.length() == 0)
				&& !PropertiesManager.getInstance().getValueAsBoolean(
						PropertyKey.DATA_DISCARD_BLANK)) {
			stringValue = "0";
		}

		sb.append(KMLUtils.createNameElement(record
				.getValue(this.labelColumnIndex)
				+ ":  " + stringValue, this.indent));
		sb.append(KMLUtils.openSnippetElement(1, this.indent));
		// sb.append(KMLUtils.addValue(record.getDisplayKey(0) + ": "
		// + record.getValue(0) + this.indent + ", "
		// + record.getDisplayKey(1) + ": " + record.getValue(1),
		// this.indent));
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

			String displayKey = record.getDisplayKey(i);
			if (displayKey != null
					&& (displayKey.toLowerCase().startsWith("lon") || displayKey
							.toLowerCase().startsWith("yloc"))) {
				value = record.getVertical();
			} else if (displayKey != null
					&& (displayKey.toLowerCase().startsWith("lat") || displayKey
							.toLowerCase().startsWith("xloc"))) {

				value = record.getHorizontal();
			}

			sb.append(KMLUtils.addValue(displayKey + ": " + value + "<br/>",
					this.indent));
		}

		sb.append(KMLUtils.addValue("<p/>", this.indent));
		sb.append(KMLUtils.addValue("<table border='0'>", this.indent));
		sb.append(KMLUtils.addValue("]]>", this.indent));
		sb.append(KMLUtils.closeDescriptionElement(this.indent));

		double value = Double.valueOf(stringValue);
		int binNumber = binRangeManager.getBinNumber(value);

		sb.append(KMLUtils
				.createStyleUrlElement("bin" + binNumber, this.indent));

		writer.write(sb.toString());

		writer.write(KMLUtils.createLookAt(record.getVertical(), record
				.getHorizontal(), Double.toString(500000), Double
				.toString(500000), Double.toString(0), Double.toString(0),
				this.indent));

		writer.write(KMLUtils.createPoint(record.getVertical(), record
				.getHorizontal(), "0", this.indent));
		writer.write(KMLUtils.closePlacemarkElement(this.indent));
	}

	private void processPropertiesManager() throws KMZGeneratorException {

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

		propertyKey = PropertyKey.ICON_SCALE;
		value = propertiesManager.getValue(propertyKey);

		String formatInstruction = "It must be either numeric, between 0 and 1, or of the form '#-#' where each '#' must be between 0 and 1.";
		String errorMessage = "Value of '" + propertyKey.getDisplayName()
				+ "' is '" + value + "'. " + formatInstruction;
		String emptyErrorMessage = "Value of '" + propertyKey.getDisplayName()
				+ "' is empty. " + formatInstruction;

		if (value.contains("-")) {

			try {

				String[] split = value.split("-");
				this.iconScaleMin = Double.parseDouble(split[0].trim());
				this.iconScaleMax = Double.parseDouble(split[1].trim());

				if (this.iconScaleMin <= 0 || this.iconScaleMin > 1
						|| this.iconScaleMax <= 0 || this.iconScaleMax > 1) {
					throw new KMZGeneratorException(
							KMZGeneratorException.ERROR_CODE_INCORRECT_VALUE_TYPE,
							errorMessage);
				}
			} catch (NumberFormatException e) {
				throw new KMZGeneratorException(
						KMZGeneratorException.ERROR_CODE_INCORRECT_VALUE_TYPE,
						errorMessage);
			}
		} else {

			try {

				this.iconScaleMax = this.iconScaleMin = Double
						.parseDouble(value);
				if (this.iconScaleMin <= 0 || this.iconScaleMin > 1) {
					throw new KMZGeneratorException(
							KMZGeneratorException.ERROR_CODE_INCORRECT_VALUE_TYPE,
							errorMessage);
				}
			} catch (NumberFormatException e) {
				throw new KMZGeneratorException(
						KMZGeneratorException.ERROR_CODE_INCORRECT_VALUE_TYPE,
						errorMessage);
			} catch (NullPointerException e) {
				throw new KMZGeneratorException(
						KMZGeneratorException.ERROR_CODE_INCORRECT_VALUE_TYPE,
						emptyErrorMessage);
			}
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

	@Override
	public void writeDocument(Writer writer, File inputFile,
			BinnedMultiGeometrySourceGenerator generator) throws IOException,
			KMZGeneratorException {
		// TODO Auto-generated method stub
		
	}

}
