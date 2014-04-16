package gov.epa.emissions.googleearth;

import gov.epa.emissions.googleearth.kml.KMZGeneratorException;
import gov.epa.emissions.googleearth.kml.record.RecordImpl;
import gov.epa.emissions.googleearth.kml.record.Record;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class BaseCSVRecordReader implements RecordProducer {

	private BufferedReader reader;
	private int columnCount;

	public BaseCSVRecordReader(File inFile) throws FileNotFoundException {

		this.reader = new BufferedReader(new FileReader(inFile));
		this.columnCount = getColumnCount();
	}

	private int getColumnCount() {

		String[] tokens = null;
		try {
			String line = this.reader.readLine();
			if (line != null) {
				tokens = line.split(",");
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return tokens.length;
	}

	@Override
	public Record nextRecord() {

		Record retVal = null;
		try {
			String line = this.reader.readLine();
			if (line != null) {

				retVal = new RecordImpl(line.split(","));

				if (retVal.getColumnCount() != this.columnCount) {

					System.out.println("Throwing out: " + line);
					retVal = this.nextRecord();
				}
			}

			if (retVal != null) {

				for (int i = 0; i < retVal.getColumnCount(); i++) {

					if (retVal.getValue(i).startsWith("\"")) {
						retVal.setValue(i, retVal.getValue(i).substring(1,
								retVal.getValue(i).length() - 1));
					}
				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return retVal;
	}

	public static void main(String[] args) throws FileNotFoundException {

		long t1;
		long t2;
		try {
			CSVRecordReader recordReader = new CSVRecordReader(new File(
					"D:\\cep\\GoogleEarth\\src\\data\\plant_summary.csv"));

			t1 = System.currentTimeMillis();
			Record record = null;
			while ((record = recordReader.nextRecord()) != null) {
				System.out.println(record.toString());
			}
			t2 = System.currentTimeMillis();
			System.out.println("Time: " + (t2 - t1) + "millis");
		} catch (KMZGeneratorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public String[] getColumnHeader() {
		// TODO Auto-generated method stub
		return null;
	}
}