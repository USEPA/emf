package gov.epa.emissions.googleearth;

import gov.epa.emissions.googleearth.kml.KMZGeneratorException;
import gov.epa.emissions.googleearth.kml.record.CssiRecord;
import gov.epa.emissions.googleearth.kml.record.Record;

import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class CSSIRecordReader implements RecordProducer {

	private BufferedReader reader;

	public CSSIRecordReader(File inFile) throws FileNotFoundException {
		this.reader = new BufferedReader(new FileReader(inFile));
	}

	@Override
	public Record nextRecord() {

		Record retVal = null;
		try {
			String line = this.reader.readLine();
			if (line != null) {

				if (line.startsWith("*")) {
					retVal = this.nextRecord();
				} else {

					String[] split = line.split("[\\s]+");
					List<String> values = new ArrayList<String>();
					for (String string : split) {
						if (string.length() > 0) {
							values.add(string);
						}
					}

					retVal = new CssiRecord(values.toArray(new String[0]));
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

	@Override
	public String[] getColumnHeader() {
		// TODO Auto-generated method stub
		return null;
	}

	public static void main(String[] args) throws KMZGeneratorException,
			IOException {

		System.out.println();
		if (args != null && args.length == 3) {

			try {

				String pathname = args[0];
				double originLat = Double.valueOf(args[1]);
				double originLon = Double.valueOf(args[2]);

				System.out
						.println("Generating kml generator compatible file based on the following input:");
				System.out
						.println("----------------------------------------------------------------------");
				System.out.println("  file: " + pathname);
				System.out.println("  center lat: " + originLat);
				System.out.println("  center lon: " + originLon);
				System.out
				.println("----------------------------------------------------------------------");

				CSSIRecordReader recordReader = new CSSIRecordReader(new File(
						pathname));

				CSSIToLatLonConverter converter = new CSSIToLatLonConverter(
						originLat, originLon);

				CssiRecord.setKeys(new String[] { "X", "Y", "AVERAGE_CONC",
						"ZELEV", "ZHILL", "ZFLAG", "AVE", "GRP", "DATE" });

				long t1 = System.currentTimeMillis();

				File tempFile = File.createTempFile("aermod", ".csv");
				System.out.println();
				System.out.println("Output filename: "
						+ tempFile.getAbsolutePath());
				System.out.println();

				FileOutputStream fos = new FileOutputStream(tempFile);
				OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
				BufferedWriter bw = new BufferedWriter(osw);

				System.out.println("Writing file: '"
						+ tempFile.getAbsolutePath() + "'");

				bw
						.write("\"point_number\", \"lat\" ,\"lon\", \"average_concentration\", \"z_elevation\", \"z_hill\", \"z_flag\", \"average\", \"group\", \"date\"\n");

				Record record = null;
				int counter = 0;
				while ((record = recordReader.nextRecord()) != null) {

					Point2D latLon = converter.convert(Double.valueOf(record
							.getHorizontal()), Double.valueOf(record
							.getVertical()));
					double lon = latLon.getY();
					double lat = latLon.getX();
					String averageConc = record.getValue(
							record.getIndex("AVERAGE_CONC")).trim();
					String zElev = record.getValue(record.getIndex("ZELEV"))
							.trim();
					String zHill = record.getValue(record.getIndex("ZHILL"))
							.trim();
					String zFlag = record.getValue(record.getIndex("ZFLAG"))
							.trim();
					String ave = record.getValue(record.getIndex("AVE")).trim();
					String grp = record.getValue(record.getIndex("GRP")).trim();
					String date = record.getValue(record.getIndex("DATE"))
							.trim();

					String line = ++counter + "," + lat + "," + lon + ","
							+ averageConc + "," + zElev + "," + zHill + ","
							+ zFlag + "," + ave + "," + grp + "," + date;
					bw.write(line);
					bw.write("\n");
				}

				System.out.println("Finished writing file: '"
						+ tempFile.getAbsolutePath() + "'");

				long t2 = System.currentTimeMillis();
				System.out.println();
				System.out.println("Ellapsed time: " + (t2 - t1) + "ms");

				fos.close();

				System.exit(0);
			} catch (Exception e) {

				System.out.println("Error occurred while generating file:");
				e.printStackTrace();
				System.exit(1);
			}
		} else {

			System.out.println("Usage: <path> <center_lat> <center_lon>");
			System.exit(1);
		}

	}
}