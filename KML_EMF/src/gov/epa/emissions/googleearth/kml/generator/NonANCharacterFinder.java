package gov.epa.emissions.googleearth.kml.generator;

import gov.epa.emissions.googleearth.CSVRecordReader;
import gov.epa.emissions.googleearth.RecordProducer;
import gov.epa.emissions.googleearth.kml.ConfigurationManager;
import gov.epa.emissions.googleearth.kml.KMZGeneratorException;
import gov.epa.emissions.googleearth.kml.record.Record;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class NonANCharacterFinder {

	public NonANCharacterFinder(RecordProducer recordProducer, File outputFile)
			throws KMZGeneratorException {

		List<Character> illegalChars = new ArrayList<Character>();
		List<String> illegalStrings = new ArrayList<String>();
		try {
			FileOutputStream fos = new FileOutputStream(outputFile);
			OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
			BufferedWriter bw = new BufferedWriter(osw);

			Record nextRecord = null;
			while ((nextRecord = recordProducer.nextRecord()) != null) {

				for (int i = 0; i < 4; i++) {

					String string = nextRecord.getValue(i);

					if (!string.matches("[A-Za-z0-9]")) {

						for (int j = 0; j < string.length(); j++) {

							char ch = string.charAt(j);
							if (!new String("" + ch).matches("[A-Za-z0-9]")
									&& !illegalChars.contains(ch)) {
								illegalChars.add(ch);

								if (!illegalStrings.contains(string)) {
									illegalStrings.add(string);
								}
							}
						}
					}
				}
			}

			bw.flush();
			bw.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		if (ConfigurationManager.getInstance().getValueAsBoolean(
				ConfigurationManager.PropertyKey.SHOW_OUTPUT.getKey())) {

			for (Character character : illegalChars) {
				System.out.println(character + " " + (int) character);
			}
			
			for (String string : illegalStrings) {
				System.out.println(string);
			}
		}
	}

	public static void main(String[] args) throws KMZGeneratorException {

		String pollutant = "NH3";
		double cutoff = 5;
		try {
			new NonANCharacterFinder(new CSVRecordReader(new File(
					"D:\\cep\\GoogleEarth\\src\\data\\plant_summary.csv")),
					new File("D:\\cep\\GoogleEarth\\temp\\epaBinned_"
							+ pollutant + "_" + cutoff + ".kml"));

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
