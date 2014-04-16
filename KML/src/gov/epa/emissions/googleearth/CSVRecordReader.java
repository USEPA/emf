package gov.epa.emissions.googleearth;

import gov.epa.emissions.googleearth.kml.ConfigurationManager;
import gov.epa.emissions.googleearth.kml.KMZGeneratorException;
import gov.epa.emissions.googleearth.kml.record.RecordImpl;
import gov.epa.emissions.googleearth.kml.record.Record;
import gov.epa.emissions.googleearth.kml.utils.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class CSVRecordReader implements RecordProducer {

	private File inFile;
	private BufferedReader reader;
	private boolean headerRead = false;
	private int columnCount;
	private String[] columnHeader;

	public CSVRecordReader(File inFile) throws FileNotFoundException,
			KMZGeneratorException {

		this.inFile = inFile;
		this.initReader();
	}

	public void initReader() throws KMZGeneratorException,
			FileNotFoundException {

		this.headerRead = false;
		this.reader = new BufferedReader(new FileReader(this.inFile));
		this.columnHeader = this.readColumnHeader();
		RecordImpl.setKeys(this.columnHeader);
	}

	private String[] readColumnHeader() throws KMZGeneratorException {

		assert !this.headerRead;

		String[] tokens = null;
		try {

			String line = this.getNextLine();
			line = line.replace("\"", "");
			if (line != null) {
				tokens = line.split(",");
			}

		} catch (IOException e) {
			throw new KMZGeneratorException(
					KMZGeneratorException.ERROR_CODE_PROCESSING_DATA_FILE,
					"Error while reading data file column header");
		}

		this.columnCount = tokens.length;
		this.headerRead = true;

		return tokens;
	}

	private String getNextLine() throws IOException {

		String line = this.reader.readLine();
		while (line != null
				&& (line.trim().length() == 0 || line.trim().startsWith("#"))) {

			if (ConfigurationManager.getInstance().getValueAsBoolean(
					ConfigurationManager.PropertyKey.SHOW_OUTPUT.getKey())) {

				if (line.trim().length() == 0) {
					System.out.println("Ignoring blank line");
				} else if (line.startsWith("#")) {
					System.out.println("Ignoring line: " + line);
				}
			}

			line = this.reader.readLine();
		}

		return line;
	}

	@Override
	public Record nextRecord() throws KMZGeneratorException {

		Record retVal = null;
		try {
			String line = this.getNextLine();
			if (line != null && line.trim().length() > 0) {

				line = line.replaceAll("&", "&amp;");
				line = line.replaceAll("#", "&#35;");
				line = line.replaceAll("%", "&#37;");
				line = line.replaceAll("'", "&#39;");
				line = line.replaceAll("@", "&#64;");
				line = line.replaceAll("`", "&#90;");

				retVal = new RecordImpl(Utils.parseLine(line, ","));

				if (retVal.getColumnCount() != this.columnCount) {

					if (ConfigurationManager.getInstance().getValueAsBoolean(
							ConfigurationManager.PropertyKey.SHOW_OUTPUT
									.getKey())) {
						System.err
								.println("Throwing out line based on column count: "
										+ line);
					}

					retVal = this.nextRecord();
				}
			}

			if (retVal != null) {

				for (int i = 0; i < retVal.getColumnCount(); i++) {

					if (retVal.getValue(i).startsWith("\"")) {
						retVal.setValue(i, retVal.getValue(i).substring(1,
								retVal.getValue(i).length() - 1));
						retVal.setValue(i, retVal.getValue(i).replaceAll("-",
								" "));
					}
				}
			}

		} catch (IOException e) {
			throw new KMZGeneratorException(
					KMZGeneratorException.ERROR_CODE_PROCESSING_DATA_FILE,
					"Error while reading data file record");
		}

		return retVal;
	}

	public static void main(String[] args) throws FileNotFoundException,
			KMZGeneratorException {

		CSVRecordReader recordReader = new CSVRecordReader(new File(
				"D:\\cep\\GoogleEarth\\src\\data\\plant_summary.csv"));

		long t1 = System.currentTimeMillis();
		Record record = null;
		while ((record = recordReader.nextRecord()) != null) {
			System.out.println(record.toString());
		}
		long t2 = System.currentTimeMillis();

		System.out.println("Time: " + (t2 - t1) + "millis");
	}

	public String[] getColumnHeader() {
		return this.columnHeader;
	}
}