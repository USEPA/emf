package gov.epa.emissions.googleearth.kml.generator;

import gov.epa.emissions.googleearth.CSVRecordReader;
import gov.epa.emissions.googleearth.RecordProducer;
import gov.epa.emissions.googleearth.kml.ConfigurationManager;
import gov.epa.emissions.googleearth.kml.KMZGeneratorException;
import gov.epa.emissions.googleearth.kml.PropertiesManager;
import gov.epa.emissions.googleearth.kml.PropertiesManager.PropertyKey;
import gov.epa.emissions.googleearth.kml.bin.AbstractEqualWidthBinStrategy;
import gov.epa.emissions.googleearth.kml.bin.EqualCountBinStrategy;
import gov.epa.emissions.googleearth.kml.bin.EqualCountDiffBinStrategy;
import gov.epa.emissions.googleearth.kml.bin.EqualWidthDiffBinStrategy;
import gov.epa.emissions.googleearth.kml.bin.EqualWidthStrategy;
import gov.epa.emissions.googleearth.kml.bin.LogarithmicBinStrategy;
import gov.epa.emissions.googleearth.kml.bin.ModifiedLogarithmicBinStrategy;
import gov.epa.emissions.googleearth.kml.generator.color.ColorPalette;
import gov.epa.emissions.googleearth.kml.generator.color.ColorPaletteGenerator;
import gov.epa.emissions.googleearth.kml.generator.color.ColorPaletteGeneratorImpl;
import gov.epa.emissions.googleearth.kml.generator.image.ImageGenerator;
import gov.epa.emissions.googleearth.kml.generator.image.ImageGeneratorImpl;
import gov.epa.emissions.googleearth.kml.generator.preprocessor.PreProcessor;
import gov.epa.emissions.googleearth.kml.generator.preprocessor.PreProcessorImpl;
import gov.epa.emissions.googleearth.kml.generator.writer.DocumentWriter;
import gov.epa.emissions.googleearth.kml.generator.writer.DocumentWriterImpl;
import gov.epa.emissions.googleearth.kml.record.Record;
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
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class BinnedPointSourceGenerator {

	private PropertiesManager propertiesManager;
	private RecordProducer recordProducer;
	private PreProcessor preProcessor;
	private ImageGenerator imageGenerator;
	private DocumentWriter documentWriter;
	private ColorPaletteGenerator colorPaletteGenerator;

	private File inputFile;
	private File outputFile;
	private List<List<Record>> binnedRecords = new ArrayList<List<Record>>();

	private List<Integer> rgbs;

	private BinRangeManager binRangeManager;

	private boolean showStats;

	private boolean diffPlot;
	private double zeroCutoff = .01;

	public static final String IMAGES_DIR = "images";

	public BinnedPointSourceGenerator(RecordProducer recordProducer,
			File inputFile, File outputFile,
			PropertiesManager propertiesManager, PreProcessor preProcessor,
			ImageGenerator imageGenerator, DocumentWriter documentWriter,
			ColorPaletteGenerator colorPaletteGenerator) {

		this.recordProducer = recordProducer;
		this.propertiesManager = propertiesManager;
		this.inputFile = inputFile;
		this.outputFile = outputFile;
		this.preProcessor = preProcessor;
		this.imageGenerator = imageGenerator;
		this.documentWriter = documentWriter;
		this.colorPaletteGenerator = colorPaletteGenerator;

		this.diffPlot = this.propertiesManager
				.getValueAsBoolean(PropertyKey.PLOT_DIFF);

		this.initRGBs();
	}

	private void initRGBs() {

		List<Integer> rgbs = new ArrayList<Integer>();

		String selectedPalette = PropertiesManager.getInstance().getValue(
				PropertyKey.COLOR_PALETTE);

		ColorPalette colorPalette = null;
		if (this.diffPlot) {
			colorPalette = this.colorPaletteGenerator
					.getDiffColorPalette(selectedPalette);
		} else {
			colorPalette = this.colorPaletteGenerator
					.getRegularColorPalette(selectedPalette);
		}

		if (ConfigurationManager.getInstance().getValueAsBoolean(
				ConfigurationManager.PropertyKey.SHOW_OUTPUT.getKey())) {
			System.out.println("Coilor palette: " + colorPalette.getName());
		}

		Color[] colors = colorPalette.getColors();

		for (Color color : colors) {
			rgbs.add(color.getRGB());
		}

		this.setImageColors(rgbs);
	}

	public void setImageColors(List<Integer> rgbs) {
		this.rgbs = rgbs;
	}

	public void generate() throws KMZGeneratorException {

		this.preProcessor.preProcessRecords(this);

		List<Double> values = this.preProcessor.getValues();
		if (values.isEmpty()) {
			throw new KMZGeneratorException(
					KMZGeneratorException.ERROR_CODE_UNABLE_TO_WRITE_KML_DOCUMENT,
					"No data remained after processing. Please check input.");
		} else {

			this.binRangeManager = this.createBinRanges(values);

			this.imageGenerator.generateImages(this);

			this.binRecords(this.preProcessor.getRecords());

			try {

				FileOutputStream fos = new FileOutputStream(outputFile);
				OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
				BufferedWriter bw = new BufferedWriter(osw);

				this.documentWriter.writeDocument(bw, this.inputFile, this);

				bw.flush();
				bw.close();
			} catch (IOException e) {
				throw new KMZGeneratorException(
						KMZGeneratorException.ERROR_CODE_UNABLE_TO_WRITE_KML_DOCUMENT,
						e.getLocalizedMessage());
			}
		}
	}

	private BinRangeManager createBinRanges(List<Double> values)
			throws KMZGeneratorException {

		BinRangeManager binRangeManager = null;

		String binningAlgorithmDisplayName = PropertiesManager.getInstance()
				.getValue(PropertyKey.BINNING_ALGORITHM);

		BinningAlgorithmType binningAlgorithmType = BinningAlgorithmType
				.getByDisplayName(binningAlgorithmDisplayName);

		if (binningAlgorithmType == null) {
			binningAlgorithmType = BinningAlgorithmType.EQUAL_COUNT;
		}

		PropertyKey propertyKey = PropertyKey.DATA_ZERO_CUTOFF;
		String value = propertiesManager.getValue(propertyKey);
		try {
			this.zeroCutoff = Double.parseDouble(value);
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

		double minValue = this.preProcessor.getMinValue();
		if (BinningAlgorithmType.LOGARITHMIC.equals(binningAlgorithmType)
				&& minValue < 0) {

			String dataColumnName = propertiesManager
					.getValue(PropertyKey.DATA_COLUMNNAME);

			throw new KMZGeneratorException(
					KMZGeneratorException.ERROR_CODE_READING_DATA_FILE,
					"Negative values in data column '" + dataColumnName
							+ "' not allowed for '"
							+ BinningAlgorithmType.LOGARITHMIC
							+ "' binning algorithm.");
		}

		if (this.diffPlot) {

			if (BinningAlgorithmType.LOGARITHMIC.equals(binningAlgorithmType)) {

				LogarithmicBinStrategy binStrategy = new LogarithmicBinStrategy(
						minValue, this.preProcessor.getMaxValue(), this.rgbs
								.size());
				binRangeManager = binStrategy.createBins();
			} else if (BinningAlgorithmType.EQUAL_COUNT
					.equals(binningAlgorithmType)) {

				EqualCountDiffBinStrategy binStrategy = new EqualCountDiffBinStrategy(
						this.rgbs.size(), values, this.zeroCutoff);
				binRangeManager = binStrategy.createBins();
			} else {

				double minCutoff = -Double.MAX_VALUE;
				double maxCutoff = Double.MAX_VALUE;

				propertyKey = PropertyKey.DATA_MINIMUM_CUTOFF;
				value = propertiesManager.getValue(propertyKey);
				try {

					if (value != null && !value.isEmpty()) {
						minCutoff = Double.parseDouble(value);
					}
				} catch (NumberFormatException e) {
					throw new KMZGeneratorException(
							KMZGeneratorException.ERROR_CODE_INCORRECT_VALUE_TYPE,
							"Value of '" + propertyKey.getDisplayName()
									+ "' is '" + value
									+ "'. It must be numeric.");
				} catch (NullPointerException e) {
					throw new KMZGeneratorException(
							KMZGeneratorException.ERROR_CODE_INCORRECT_VALUE_TYPE,
							"Value of '" + propertyKey.getDisplayName()
									+ "' is empty. It must be numeric.");
				}

				propertyKey = PropertyKey.DATA_MAXIMUM_CUTOFF;
				value = propertiesManager.getValue(propertyKey);
				try {

					if (value != null && !value.isEmpty()) {
						maxCutoff = Double.parseDouble(value);
					}
				} catch (NumberFormatException e) {
					throw new KMZGeneratorException(
							KMZGeneratorException.ERROR_CODE_INCORRECT_VALUE_TYPE,
							"Value of '" + propertyKey.getDisplayName()
									+ "' is '" + value
									+ "'. It must be numeric.");
				} catch (NullPointerException e) {
					throw new KMZGeneratorException(
							KMZGeneratorException.ERROR_CODE_INCORRECT_VALUE_TYPE,
							"Value of '" + propertyKey.getDisplayName()
									+ "' is empty. It must be numeric.");
				}

				AbstractEqualWidthBinStrategy binStrategy = new EqualWidthDiffBinStrategy(
						this.rgbs.size(), values, this.zeroCutoff);

				binStrategy.setMinCutoff(minCutoff);
				binStrategy.setMaxCutoff(maxCutoff);

				binRangeManager = binStrategy.createBins();
			}
		} else {

			if (BinningAlgorithmType.LOGARITHMIC.equals(binningAlgorithmType)) {

				ModifiedLogarithmicBinStrategy binStrategy = new ModifiedLogarithmicBinStrategy(
						minValue, this.preProcessor.getMaxValue(), this.rgbs
								.size());
				binRangeManager = binStrategy.createBins();
			} else if (BinningAlgorithmType.EQUAL_COUNT
					.equals(binningAlgorithmType)) {

				EqualCountBinStrategy binStrategy = new EqualCountBinStrategy(
						this.rgbs.size(), values);
				binRangeManager = binStrategy.createBins();
			} else {

				double minCutoff = -Double.MAX_VALUE;
				double maxCutoff = Double.MAX_VALUE;

				propertyKey = PropertyKey.DATA_MINIMUM_CUTOFF;
				value = propertiesManager.getValue(propertyKey);
				try {

					if (value != null && !value.isEmpty()) {
						minCutoff = Double.parseDouble(value);
					}
				} catch (NumberFormatException e) {
					throw new KMZGeneratorException(
							KMZGeneratorException.ERROR_CODE_INCORRECT_VALUE_TYPE,
							"Value of '" + propertyKey.getDisplayName()
									+ "' is '" + value
									+ "'. It must be numeric.");
				} catch (NullPointerException e) {
					throw new KMZGeneratorException(
							KMZGeneratorException.ERROR_CODE_INCORRECT_VALUE_TYPE,
							"Value of '" + propertyKey.getDisplayName()
									+ "' is empty. It must be numeric.");
				}

				propertyKey = PropertyKey.DATA_MAXIMUM_CUTOFF;
				value = propertiesManager.getValue(propertyKey);
				try {

					if (value != null && !value.isEmpty()) {
						maxCutoff = Double.parseDouble(value);
					}
				} catch (NumberFormatException e) {
					throw new KMZGeneratorException(
							KMZGeneratorException.ERROR_CODE_INCORRECT_VALUE_TYPE,
							"Value of '" + propertyKey.getDisplayName()
									+ "' is '" + value
									+ "'. It must be numeric.");
				} catch (NullPointerException e) {
					throw new KMZGeneratorException(
							KMZGeneratorException.ERROR_CODE_INCORRECT_VALUE_TYPE,
							"Value of '" + propertyKey.getDisplayName()
									+ "' is empty. It must be numeric.");
				}

				EqualWidthStrategy binStrategy = new EqualWidthStrategy(
						this.rgbs.size(), values);

				binStrategy.setMinCutoff(minCutoff);
				binStrategy.setMaxCutoff(maxCutoff);

				binRangeManager = binStrategy.createBins();
			}
		}

		if (ConfigurationManager.getInstance().getValueAsBoolean(
				ConfigurationManager.PropertyKey.SHOW_OUTPUT.getKey())) {

			for (int i = 0; i < binRangeManager.getRangeCount(); i++) {
				System.out.println("bin[" + i + "]: "
						+ binRangeManager.getRange(i));
			}
		}

		return binRangeManager;
	}

	public static String createDefaultFileName(
			PropertiesManager propertiesManager, String inputFileName) {

		StringBuilder sb = new StringBuilder();

		String label = propertiesManager
				.getValue(PropertiesManager.PropertyKey.LABEL_COLUMNNAME);
		String filterColumnName = propertiesManager
				.getValue(PropertiesManager.PropertyKey.FILTER_COLUMNNAME);
		String filterValue = propertiesManager
				.getValue(PropertiesManager.PropertyKey.FILTER_VALUE);
		String dataColumnName = propertiesManager
				.getValue(PropertiesManager.PropertyKey.DATA_COLUMNNAME);
		String dataMinimum = propertiesManager
				.getValue(PropertiesManager.PropertyKey.DATA_MINIMUM_CUTOFF);
		String dataMaximum = propertiesManager
				.getValue(PropertiesManager.PropertyKey.DATA_MAXIMUM_CUTOFF);

		sb.append(getBeginingOfFileName(inputFileName, 12)).append("_").append(
				label).append("_").append(filterColumnName).append("_").append(
				filterValue).append("_").append(dataColumnName).append("_")
				.append(dataMinimum).append("_to_").append(dataMaximum).append(
						".kmz");
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

	protected void binRecords(List<Record> records) {

		this.binnedRecords.clear();

		for (int i = 0; i < this.rgbs.size(); i++) {
			this.binnedRecords.add(new ArrayList<Record>());
		}

		int dataColumnIndex = -1;
		String dataDisplayName = null;
		for (Record record : records) {

			if (dataColumnIndex == -1) {

				String dataColumnName = propertiesManager
						.getValue(PropertyKey.DATA_COLUMNNAME);

				dataColumnIndex = record.getIndex(dataColumnName);

				if (dataDisplayName == null) {
					dataDisplayName = record.getDisplayKey(dataColumnIndex);
				}
			}

			String stringValue = record.getValue(dataColumnIndex);
			if ((stringValue == null || stringValue.length() == 0)
					&& !PropertiesManager.getInstance().getValueAsBoolean(
							PropertyKey.DATA_DISCARD_BLANK)) {
				stringValue = "0";
			}

			double value = Double.valueOf(stringValue);
			int binNumber = this.binRangeManager.getBinNumber(value);

			this.binnedRecords.get(binNumber).add(record);
		}
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
		return this.imageGenerator.getLegend();
	}

	public File getTitleLegend() {
		return this.imageGenerator.getTitleLegend();
	}

	public File getStatsLegend() {
		return this.imageGenerator.getStatsLegend();
	}

	public List<File> getImages() {
		return this.imageGenerator.getImages();
	}

	public boolean shouldDrawTitleLegend() {
		return this.imageGenerator.shouldDrawTitleLegend();
	}

	public List<Integer> getRGBs() {
		return this.rgbs;
	}

	public BinRangeManager getBinRangeManager() {
		return this.binRangeManager;
	}

	public boolean isShowStats() {
		return this.showStats;
	}

	public boolean isDiffPlot() {
		return this.diffPlot;
	}

	public PreProcessor getPreProcessor() {
		return this.preProcessor;
	}

	public ImageGenerator getImageGenerator() {
		return this.imageGenerator;
	}

	public RecordProducer getRecordProducer() {
		return this.recordProducer;
	}

	public List<List<Record>> getBinnedRecords() {
		return this.binnedRecords;
	}

	public void zipResults(File tempFile, File outputFile, List<File> images,
			File legend, File titleLegend, File statsLegend) throws IOException {

		outputFile.getParentFile().mkdir();

		List<String> fileNames = new ArrayList<String>();
		fileNames.add(tempFile.getAbsolutePath());
		fileNames.add(legend.getAbsolutePath());
		fileNames.add(titleLegend.getAbsolutePath());

		if (showStats) {
			fileNames.add(statsLegend.getAbsolutePath());
		}

		for (File file : images) {
			fileNames.add(file.getAbsolutePath());
		}

		List<String> fakeNames = new ArrayList<String>();
		fakeNames.add(tempFile.getName());
		fakeNames.add(IMAGES_DIR + "/" + legend.getName());
		fakeNames.add(IMAGES_DIR + "/" + titleLegend.getName());

		if (showStats) {
			fakeNames.add(IMAGES_DIR + "/" + statsLegend.getName());
		}

		for (File file : images) {
			fakeNames.add(IMAGES_DIR + "/" + file.getName());
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

				PropertiesManager propertiesManager = PropertiesManager
						.getInstance();
				PropertiesManager.getInstance().initProperties(args);

				File tempFile = File.createTempFile("kml", ".kml");

				File inputFile = new File(BinnedPointSourceGenerator
						.getInputFileName(args));
				String outputFileName = BinnedPointSourceGenerator
						.createDefaultFileName(propertiesManager, inputFile
								.getName());

				BinnedPointSourceGenerator generator = new BinnedPointSourceGenerator(
						new CSVRecordReader(inputFile), inputFile, tempFile,
						propertiesManager, new PreProcessorImpl(),
						new ImageGeneratorImpl(), new DocumentWriterImpl(),
						new ColorPaletteGeneratorImpl());
				generator.generate();

				File outputFile = new File(inputFile.getParentFile()
						.getAbsolutePath()
						+ "/" + outputFileName);

				List<File> images = generator.getImages();
				File legend = generator.getLegend();
				File titleLegend = generator.getTitleLegend();
				File statsLegend = generator.getStatsLegend();

				generator.zipResults(tempFile, outputFile, images, legend,
						titleLegend, statsLegend);

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
