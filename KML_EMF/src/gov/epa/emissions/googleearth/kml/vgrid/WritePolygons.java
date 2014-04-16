package gov.epa.emissions.googleearth.kml.vgrid;

import gov.epa.emissions.googleearth.kml.utils.KMLUtils;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import ucar.ma2.Array;
import ucar.nc2.Variable;

public class WritePolygons {

	private float minValue = 0;

	private float maxValue = 0;

	private int numOfRow = 0;

	private int numOfCol = 0;

	private String variableFile;

	private NetcdfDataLoader dataLoader;

	private StringBuilder indent = new StringBuilder();

	private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

	private Date currentDate;
	
	private int currentTime;

	private File legend;
	
	private File titleFile;
	
	private File noteFile;

	private List<String> varNames;

	private String varUnit;

	private String title;
	
	private String note;

	private static final String COLOR_MAP0 = "clr_map0";

	private static final String COLOR_MAP1 = "clr_map1";

	private static final String COLOR_MAP2 = "clr_map2";

	private static final String COLOR_MAP3 = "clr_map3";

	private static final String COLOR_MAP4 = "clr_map4";

	private static final String COLOR_MAP5 = "clr_map5";

	private static final String COLOR_MAP6 = "clr_map6";

	private static final String POLYGON_COLOR0 = "poly_clr0";

	private static final String POLYGON_COLOR1 = "poly_clr1";

	private static final String POLYGON_COLOR2 = "poly_clr2";

	private static final String POLYGON_COLOR3 = "poly_clr3";

	private static final String POLYGON_COLOR4 = "poly_clr4";

	private static final String POLYGON_COLOR5 = "poly_clr5";

	private static final String POLYGON_COLOR6 = "poly_clr6";

	private static final String IMAGES_DIR = "images";

	public WritePolygons(String varFile) throws Exception {
		variableFile = varFile;
		dataLoader = new NetcdfDataLoader(varFile);
		readVarInfo();
	}

	public void printVariablesInfo() throws Exception {
		dataLoader.printVariablesInfo();
	}

	private void readVarInfo() throws Exception {
		dataLoader.load();
		varNames = dataLoader.getVariablesInfo();
		dataLoader.close();
	}

	public List<String> getVarNames() {
		return varNames;
	}

	public Integer[] getLayers() {
		return dataLoader.getLayers();
	}

	public void writePolygons(File polygonFile, File outputFile,
			String variable, int layer, float[] bins, int lineWidth,
			boolean showGrids, String userTitle, float convEff, boolean multiply, boolean division) throws Exception {
		title = userTitle;

		if (userTitle == null || userTitle.trim().isEmpty())
			title = variable + ":  Layer " + (layer + 1) + "; "
					+ dataLoader.getSdate();
		
		String[] grids = readGrids(polygonFile);

		dataLoader.load();

		Variable slice = dataLoader.getSlice(variable, "LAY", layer);
		Array array = slice.read();
		float[] origData = dataLoader.getFloatValues(array);
		varUnit = slice.findAttribute("units").getStringValue();
		dataLoader.close();
		float[] data = convertUnits(origData, convEff, multiply, division);

		float binWidth = (maxValue - minValue) / 7;

		if (binWidth == 0)
			throw new Exception(
					"The current layer has minimum and maximum data both at 0.");

		if (grids == null || grids.length == 0)
			throw new Exception("No data found for grids.");

		if (data == null || data.length == 0)
			throw new Exception("No data found for variable: " + variable);

		legend = File.createTempFile("legend", ".png");
		titleFile = File.createTempFile("title", ".png");
		noteFile = File.createTempFile("note", ".png");
		File tempFile = File.createTempFile("kml", ".kml");
		FileOutputStream fos = new FileOutputStream(tempFile);
		OutputStreamWriter osw = new OutputStreamWriter(fos);
		BufferedWriter writer = new BufferedWriter(osw);

		writeKmlHeader(new File(variableFile), variable, layer, lineWidth,
				"ff", writer);

		if (!showGrids)
			writeBaseGrids(grids, writer);

		writeGrids(grids, data, bins, binWidth, writer, showGrids);
		writeKmlClosure(writer);

		writer.close();

		zipResults(tempFile, outputFile, legend, titleFile, noteFile);
		titleFile.deleteOnExit();
		noteFile.deleteOnExit();
		legend.deleteOnExit();
		tempFile.deleteOnExit();
	}

	private String[] readGrids(File polygonFile) throws IOException {
		FileInputStream fis = new FileInputStream(polygonFile);
		InputStreamReader isr = new InputStreamReader(fis);
		BufferedReader reader = new BufferedReader(isr);

		String line = reader.readLine(); // to skip the first grid id line
		List<String> grids = new ArrayList<String>();
		StringBuilder sb = new StringBuilder();
		int gridSides = 4; // Assuming reading grids

		while ((line = reader.readLine()) != null) {
			if (line.trim().toUpperCase().startsWith("END")) {
				reader.readLine(); // to skip the grid id line
				grids.add(sb.toString());
				sb = new StringBuilder(); // NOTE: clear string buffer
				gridSides = 4; // NOTE: reset the grid sides number
				continue;
			}

			if (gridSides > 0)
				sb.append(line + " ");

			--gridSides;
		}

		reader.close();

		return grids.toArray(new String[0]);
	}
	
	private float[] convertUnits(float[] floatValues, float convEff,
			boolean multiply, boolean division) {
		if (floatValues == null || floatValues.length == 0)
			return floatValues;
		
		if (convEff <= 0)
			return floatValues;
		
		if (!multiply && !division)
			return floatValues;
		
		int len = floatValues.length;
		float[] values = new float[len];
		
		if (division)
			convEff = 1.0f / convEff;
		
		for (int i = 0; i < len; i++)
			values[i] = floatValues[i] * convEff;
		
		this.maxValue = this.maxValue * convEff;
		this.minValue = this.minValue * convEff;
		
		return values;
	}

	private String getColorMapUrl(float value, float[] bins) {
		if (value < bins[0])
			return COLOR_MAP0;

		if (value < bins[1])
			return COLOR_MAP0;

		if (value < bins[2])
			return COLOR_MAP1;

		if (value < bins[3])
			return COLOR_MAP2;

		if (value < bins[4])
			return COLOR_MAP3;

		if (value < bins[5])
			return COLOR_MAP4;

		if (value < bins[6])
			return COLOR_MAP5;

		return COLOR_MAP6;
	}

	private void writeKmlHeader(File varFile, String var, int layer,
			int lineWidth, String transparency, BufferedWriter writer)
			throws IOException {
		String fileName = varFile.getName();
		fileName += "_" + var + "_" + layer;

		StringBuilder sb = new StringBuilder();
		sb.append(KMLUtils.createXMLElement());
		sb.append(KMLUtils.openKMLElement(this.indent));
		sb.append(KMLUtils.openDocumentElement(this.indent));
		sb.append(KMLUtils.createNameElement(fileName, this.indent));
		sb.append(KMLUtils.createVisibilityElement(1 + "", this.indent));
		sb.append(KMLUtils.createOpenElement("1", this.indent));
		
		//NOTE: writing title
		sb.append(KMLUtils.openScreenOverlayElement(this.indent));
		sb.append(KMLUtils.createNameElement("Title", this.indent));
		sb.append(KMLUtils.createIconElement(IMAGES_DIR + "/"
				+ this.titleFile.getName(), this.indent));
		sb.append(KMLUtils.createOverlayXYElement("0.1", "1", this.indent));
		sb.append(KMLUtils.createScreenXYElement("0.1", "1", this.indent));
		sb.append(KMLUtils.createRotationXYElement("0", "0", this.indent));
		sb.append(KMLUtils.createSizeElement("0", "0", this.indent));
		sb.append(KMLUtils.closeScreenOverlayElement(this.indent));
		
		//NOTE: writing note
		sb.append(KMLUtils.openScreenOverlayElement(this.indent));
		sb.append(KMLUtils.createNameElement("Foot note", this.indent));
		sb.append(KMLUtils.createIconElement(IMAGES_DIR + "/"
				+ this.noteFile.getName(), this.indent));
		sb.append(KMLUtils.createOverlayXYElement("0.1", "0.05", this.indent));
		sb.append(KMLUtils.createScreenXYElement("0.1", "0.05", this.indent));
		sb.append(KMLUtils.createRotationXYElement("0", "0", this.indent));
		sb.append(KMLUtils.createSizeElement("0", "0", this.indent));
		sb.append(KMLUtils.closeScreenOverlayElement(this.indent));

		//NOTE: writing legend
		sb.append(KMLUtils.openScreenOverlayElement(this.indent));
		sb.append(KMLUtils.createNameElement("Legend", this.indent));
		sb.append(KMLUtils.createIconElement(IMAGES_DIR + "/"
				+ this.legend.getName(), this.indent));
		sb.append(KMLUtils.createOverlayXYElement("1", "0.1", this.indent));
		sb.append(KMLUtils.createScreenXYElement("1", "0.1", this.indent));
		sb.append(KMLUtils.createRotationXYElement("0", "0", this.indent));
		sb.append(KMLUtils.createSizeElement("0", "0", this.indent));
		sb.append(KMLUtils.closeScreenOverlayElement(this.indent));

		sb.append(KMLUtils.createStyleElement(POLYGON_COLOR0, "ff"
				+ KMLUtils.WHITE, lineWidth, this.indent));
		sb.append(KMLUtils.createStyleMapElement(COLOR_MAP0, POLYGON_COLOR0,
				this.indent));

		sb.append(KMLUtils.createStyleElement(POLYGON_COLOR1, transparency
				+ KMLUtils.BLUE, lineWidth, this.indent));
		sb.append(KMLUtils.createStyleMapElement(COLOR_MAP1, POLYGON_COLOR1,
				this.indent));

		sb.append(KMLUtils.createStyleElement(POLYGON_COLOR2, transparency
				+ KMLUtils.CYAN, lineWidth, this.indent));
		sb.append(KMLUtils.createStyleMapElement(COLOR_MAP2, POLYGON_COLOR2,
				this.indent));

		sb.append(KMLUtils.createStyleElement(POLYGON_COLOR3, transparency
				+ KMLUtils.GREEN, lineWidth, this.indent));
		sb.append(KMLUtils.createStyleMapElement(COLOR_MAP3, POLYGON_COLOR3,
				this.indent));

		sb.append(KMLUtils.createStyleElement(POLYGON_COLOR4, transparency
				+ KMLUtils.YELLOW, lineWidth, this.indent));
		sb.append(KMLUtils.createStyleMapElement(COLOR_MAP4, POLYGON_COLOR4,
				this.indent));

		sb.append(KMLUtils.createStyleElement(POLYGON_COLOR5, transparency
				+ KMLUtils.ORANGE, lineWidth, this.indent));
		sb.append(KMLUtils.createStyleMapElement(COLOR_MAP5, POLYGON_COLOR5,
				this.indent));

		sb.append(KMLUtils.createStyleElement(POLYGON_COLOR6, transparency
				+ KMLUtils.RED, lineWidth, this.indent));
		sb.append(KMLUtils.createStyleMapElement(COLOR_MAP6, POLYGON_COLOR6,
				this.indent));

		writer.write(sb.toString());
	}

	private void writeBaseGrids(String[] grids, BufferedWriter writer)
			throws IOException {
		numOfCol = dataLoader.getNumOfCols();
		numOfRow = dataLoader.getNumOfRows();

		int first = 0;
		int second = numOfCol * (numOfRow - 1);

		writer.write(KMLUtils.opendFolderWithName("VGid domain", 0 + "",
				this.indent));

		float adjLat = 0.25f;
		float adjLon = 0.3f;
		String baseGrid = getLeftBoader(grids[first], grids[second], adjLon);
		writer.write(KMLUtils.createPlaceMark(COLOR_MAP0, baseGrid,
				this.indent));
		
		writer.write(KMLUtils.createPlaceMark(COLOR_MAP0, adjustLowerLeftGrid(grids[1],adjLon,adjLat),
				this.indent));
		writer.write(KMLUtils.createPlaceMark(COLOR_MAP0, adjustUpperLeftGrid(grids[second + 1],adjLon,adjLat),
				this.indent));
		
		for (int i = 2; i < numOfCol - 2; i++) {
			writer.write(KMLUtils.createPlaceMark(COLOR_MAP0, adjustGridTop(grids[first + i],adjLat),
					this.indent));
			writer.write(KMLUtils.createPlaceMark(COLOR_MAP0, adjustGridBottom(grids[second + i],adjLat),
					this.indent));
		}
		
		writer.write(KMLUtils.createPlaceMark(COLOR_MAP0, adjustLowerRightGrid(grids[numOfCol - 2],adjLon,adjLat),
				this.indent));
		writer.write(KMLUtils.createPlaceMark(COLOR_MAP0, adjustUpperRightGrid(grids[grids.length - 2],adjLon,adjLat),
				this.indent));
		
		baseGrid = getRightBoader(grids[numOfCol -1], grids[grids.length - 1], adjLon);
		writer.write(KMLUtils.createPlaceMark(COLOR_MAP0, baseGrid,
				this.indent));

		writer.write(KMLUtils.closeFolderWithName(this.indent));
	}

	private void writeGrids(String[] grids, float[] data, float[] userBins,
			float binWidth, BufferedWriter writer, boolean showGrids)
			throws Exception {
		float minimum = minValue;
		float first = minimum + binWidth;
		float second = first + binWidth;
		float third = second + binWidth;
		float fourth = third + binWidth;
		float fifth = fourth + binWidth;
		float sixth = fifth + binWidth;
		float[] bins = { first, second, third, fourth, fifth, sixth };

		List<Float> values = getBinValues(bins, userBins);

		List<Color> colors = new ArrayList<Color>();
		colors.add(Color.RED);
		colors.add(Color.ORANGE);
		colors.add(Color.YELLOW);
		colors.add(Color.GREEN);
		colors.add(Color.CYAN);
		colors.add(Color.BLUE);
		colors.add(Color.LIGHT_GRAY);

		int len = data.length;
		int gridLen = grids.length;

		currentDate = dataLoader.getSdate();
		Date startDate = new Date();
		startDate.setTime(currentDate.getTime());
		String date = formatter.format(currentDate);

		currentTime = dataLoader.getStimeValue();
		String time = getTime(currentTime, 0);
		int timeStep = dataLoader.getTstepValue();
		
		note = "Min = " + minValue + "; Max = " + maxValue + "; " + currentDate;
		createLegend(values, colors, legend, titleFile, noteFile);

		StringBuilder sb = new StringBuilder();
		sb.append(KMLUtils.opendFolderWithName("VGid time: " + time, 0 + "",
				this.indent));

		float[] correctBins = getFloatValues(values);

		for (int i = 0; i < len; i++) {
//			System.out.println("Current date: " + currentDate.toString() + "(" + currentDate.getTime() + ")" +
//					" Start date: " + startDate.toString() + " (" + startDate.getTime() + ")");
			if ((currentDate.getTime() - startDate.getTime()) > 86400000)
				return;
			
			String mapUrl = getColorMapUrl(data[i], correctBins);

			if (showGrids)
				sb.append(KMLUtils.createPlaceMark(mapUrl, grids[i % gridLen],
						date, time, false, this.indent));

			else if (!mapUrl.equals(COLOR_MAP0))
				sb.append(KMLUtils.createPlaceMark(mapUrl, addAltitute(grids[i
						% gridLen], 1), date, time, true, this.indent));

			if ((i + 1) % gridLen == 0) {
				time = getTime(currentTime, timeStep);
				date = formatter.format(currentDate);
				sb.append(KMLUtils.closeFolderWithName(this.indent));
				writer.write(sb.toString());
				sb = new StringBuilder(); // NOTE: clear string buffer
				sb.append(KMLUtils.opendFolderWithName("VGid time: " + time,
						0 + "", this.indent));
			}
		}
	}

	private String addAltitute(String grid, int alt) {
		String[] latLons = grid.split(" ");
		String point1 = latLons[0] + "," + alt;
		String point2 = latLons[1] + "," + alt;
		String point3 = latLons[2] + "," + alt;
		String point4 = latLons[3] + "," + alt;

		return point1 + " " + point2 + " " + point3 + " " + point4;
	}

	private float[] getFloatValues(List<Float> values) {
		int len = values.size();
		float[] floatValues = new float[len];

		for (int i = 0; i < len; i++)
			// NOTE: values from big to small
			floatValues[i] = values.get(len - i - 1).floatValue();

		return floatValues;
	}

	private List<Float> getBinValues(float[] bins, float[] userBins) {
		List<Float> values = new ArrayList<Float>();
		int uBinsLen = (userBins == null) ? 0 : userBins.length;

		if (uBinsLen == 0) {
			values.add(maxValue);
			values.add(bins[5]);
			values.add(bins[4]);
			values.add(bins[3]);
			values.add(bins[2]);
			values.add(bins[1]);
			values.add(bins[0]);
			values.add(minValue);
			return values;
		}

		if (uBinsLen == 1) {
			float span = (maxValue - userBins[0]) / 6;
			values.add(maxValue);
			values.add(userBins[0] + 5 * span);
			values.add(userBins[0] + 4 * span);
			values.add(userBins[0] + 3 * span);
			values.add(userBins[0] + 2 * span);
			values.add(userBins[0] + span);
			values.add(userBins[0]);
			values.add(minValue);
			return values;
		}

		if (uBinsLen == 2) {
			float span = (maxValue - userBins[1]) / 5;
			values.add(maxValue);
			values.add(userBins[1] + 4 * span);
			values.add(userBins[1] + 3 * span);
			values.add(userBins[1] + 2 * span);
			values.add(userBins[1] + span);
			values.add(userBins[1]);
			values.add(userBins[0]);
			values.add(minValue);
			return values;
		}

		if (uBinsLen == 3) {
			float span = (maxValue - userBins[2]) / 4;
			values.add(maxValue);
			values.add(userBins[2] + 3 * span);
			values.add(userBins[2] + 2 * span);
			values.add(userBins[2] + span);
			values.add(userBins[2]);
			values.add(userBins[1]);
			values.add(userBins[0]);
			values.add(minValue);
			return values;
		}

		if (uBinsLen == 4) {
			float span = (maxValue - userBins[3]) / 3;
			values.add(maxValue);
			values.add(userBins[3] + 2 * span);
			values.add(userBins[3] + span);
			values.add(userBins[3]);
			values.add(userBins[2]);
			values.add(userBins[1]);
			values.add(userBins[0]);
			values.add(minValue);
			return values;
		}

		if (uBinsLen == 5) {
			float span = (maxValue - userBins[4]) / 2;
			values.add(maxValue);
			values.add(userBins[4] + span);
			values.add(userBins[4]);
			values.add(userBins[3]);
			values.add(userBins[2]);
			values.add(userBins[1]);
			values.add(userBins[0]);
			values.add(minValue);
			return values;
		}

		if (uBinsLen == 6) {
			values.add(maxValue);
			values.add(userBins[5]);
			values.add(userBins[4]);
			values.add(userBins[3]);
			values.add(userBins[2]);
			values.add(userBins[1]);
			values.add(userBins[0]);
			values.add(minValue);
			return values;
		}

		if (uBinsLen == 7) {
			values.add(maxValue);
			values.add(userBins[6]);
			values.add(userBins[5]);
			values.add(userBins[4]);
			values.add(userBins[3]);
			values.add(userBins[2]);
			values.add(userBins[1]);
			values.add(userBins[0]);

			return values;
		}

		if (uBinsLen == 8) {
			values.add(userBins[7]);
			values.add(userBins[6]);
			values.add(userBins[5]);
			values.add(userBins[4]);
			values.add(userBins[3]);
			values.add(userBins[2]);
			values.add(userBins[1]);
			values.add(userBins[0]);

			return values;
		}

		if (uBinsLen > 8) {
			values.add(userBins[uBinsLen - 1]);
			values.add(bins[uBinsLen - 2]);
			values.add(bins[uBinsLen - 3]);
			values.add(bins[uBinsLen - 4]);
			values.add(bins[uBinsLen - 5]);
			values.add(userBins[uBinsLen - 6]);
			values.add(userBins[uBinsLen - 7]);
			values.add(userBins[uBinsLen - 8]);

			return values;
		}

		return values;
	}

	private String getTime(int start, int timeStep) {
		// NOTE: needs to modify to consider the minute and sencond

		this.currentTime = start + timeStep;

		if (currentTime >= 240000) {
			this.currentTime -= 240000;
			this.currentDate.setTime(currentDate.getTime() + 86400000);
		}

		String time = "" + currentTime;

		if (time.length() < 6)
			time = pad(time, 6);

//		return time.substring(0, 2) + ":"
//				+ pad("" + (Math.abs(new Random().nextInt())) % 60, 2) + ":"
//				+ pad("" + (Math.abs(new Random().nextInt())) % 60, 2);
		
		return time.substring(0, 2) + ":"
		+ time.substring(2, 4) + ":"
		+ time.substring(4);
	}

	private String pad(String time, int length) {
		int len = time.length();
		int spaces = length - len;

		for (int i = 0; i < spaces; i++)
			time = "0" + time;

		return time;
	}

	private void writeKmlClosure(BufferedWriter writer) throws IOException {
		writer.write(KMLUtils.closeDocumentElement(this.indent));
		writer.write(KMLUtils.closeKMLElement(this.indent));
	}

	private void zipResults(File tempFile, File outputFile, File legend,
			File titleFile, File noteFile) throws IOException {

		outputFile.getParentFile().mkdirs();

		List<String> fileNames = new ArrayList<String>();
		fileNames.add(tempFile.getAbsolutePath());
		fileNames.add(legend.getAbsolutePath());
		fileNames.add(titleFile.getAbsolutePath());
		fileNames.add(noteFile.getAbsolutePath());

		List<String> fakeNames = new ArrayList<String>();
		fakeNames.add(tempFile.getName());
		fakeNames.add(IMAGES_DIR + "/" + legend.getName());
		fakeNames.add(IMAGES_DIR + "/" + titleFile.getName());
		fakeNames.add(IMAGES_DIR + "/" + noteFile.getName());

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
	}

	public void readMinMaxValues(String varName, int layer) throws Exception {
		dataLoader.load();
		Variable slice = dataLoader.getSlice(varName, "LAY", layer);
		varUnit = slice.findAttribute("units").getStringValue();
		Array array = slice.read();
		float[] data = dataLoader.getFloatValues(array);
		getMinMaxValues(data);
		dataLoader.close();
	}

	private void getMinMaxValues(float[] data) {
		int size = data.length;
		Arrays.sort(data);
		this.minValue = data[0];
		this.maxValue = data[size - 1];
	}

	public float getMinValue() {
		return minValue;
	}

	public float getMaxValue() {
		return maxValue;
	}
	
	public String getVarUnit() {
		return varUnit;
	}

	private void createLegend(List<Float> values, List<Color> colors,
			File imageFile, File titleFile, File noteFile) throws Exception {
		imageFile.getParentFile().mkdirs();
		RectangularLegend legend = new RectangularLegend();
		legend.drawImage(20, colors, values, this.varUnit);
		legend.writeImage(imageFile);
		legend.drawTitleImage(20, 2, this.title);
		legend.writeImage(titleFile);
		legend.drawTitleImage(10, 5, this.note);
		legend.writeImage(noteFile);
	}

	public float[] parseUserSpecifiedBins(String binsString) throws Exception {
		if (binsString == null || binsString.trim().length() == 0)
			return null;

		binsString = binsString.trim();

		if (!Character.isDigit(binsString.charAt(0)))
			return new float[0];

		String[] bins = binsString.split(",");
		float[] binValues = new float[bins.length];

		for (int i = 0; i < bins.length; i++)
			binValues[i] = Float.parseFloat(bins[i]);

		return binValues;
	}

	private String getLeftBoader(String grid1, String grid2, float adjLon) {
		String point1 = grid1.split(" ")[0] + ",0";
		String point2 = grid2.split(" ")[1] + ",0";
		String[] temp3 = grid2.split(" ")[2].split(",");
		String point3 = (Float.parseFloat(temp3[0]) - adjLon)+ "," + temp3[1] + ",0";
		String[] temp4 = grid1.split(" ")[3].split(",");
		String point4 = (Float.parseFloat(temp4[0]) - adjLon) + "," + temp4[1] + ",0";

		return point1 + " " + point2 + " " + point3 + " " + point4;
	}
	
	private String getRightBoader(String grid1, String grid2, float adjust) {
		String[] temp1 = grid1.split(" ")[0].split(",");
		String point1 = (Float.parseFloat(temp1[0]) + adjust) + "," + temp1[1] + ",0";
		String[] temp2 = grid2.split(" ")[1].split(",");
		String point2 = (Float.parseFloat(temp2[0]) + adjust) + "," + temp2[1] + ",0";
		String[] temp3 = grid2.split(" ")[2].split(",");
		String point3 = temp3[0] + "," + temp2[1] + ",0"; //NOTE: to get a square corner
		String[] temp4 = grid1.split(" ")[3].split(",");
		String point4 =  temp4[0] + "," + temp1[1]+ ",0"; //NOTE: to get a aqure corner

		return point1 + " " + point2 + " " + point3 + " " + point4;
	}
	
	private String adjustUpperLeftGrid(String grid, float adjLon, float adjLat) {
		String[] points = grid.split(" ");
		String[] temp2 = points[1].split(",");
		String point2 = (Float.parseFloat(temp2[0]) - adjLon) + "," + temp2[1] + ",0";
		String[] temp1 = points[0].split(",");
		String point1 = (Float.parseFloat(temp2[0]) - adjLon) + "," + (Float.parseFloat(temp1[1]) + adjLat) + ",0";
		String[] temp4 = points[3].split(",");
		String point4 = temp4[0] + "," + (Float.parseFloat(temp4[1]) + adjLat) + ",0";
		
		return point1 + " " + point2 + " " + points[2] + " " + point4;
	}
	
	private String adjustUpperRightGrid(String grid, float adjLon, float adjLat) {
		String[] points = grid.split(" ");
		String[] temp1 = points[0].split(",");
		String point1 = temp1[0] + "," + (Float.parseFloat(temp1[1]) + adjLat) + ",0";
		String[] temp3 = points[2].split(",");
		String point3 = (Float.parseFloat(temp3[0]) + adjLon) + "," + temp3[1] + ",0";
		String[] temp4 = points[3].split(",");
		String point4 = (Float.parseFloat(temp3[0]) + adjLon) + "," + (Float.parseFloat(temp4[1]) + adjLat) + ",0";
		
		return point1 + " " + points[1] + " " + point3 + " " + point4;
	}
	
	private String adjustLowerLeftGrid(String grid, float adjLon, float adjLat) {
		String[] points = grid.split(" ");
		String[] temp1 = points[0].split(",");
		String point1 = (Float.parseFloat(temp1[0]) - adjLon) + "," + temp1[1] + ",0";
		String[] temp2 = points[1].split(",");
		String point2 = (Float.parseFloat(temp2[0]) - adjLon) + "," + (Float.parseFloat(temp2[1]) - adjLat) + ",0";
		String[] temp3 = points[2].split(",");
		String point3 = temp3[0] + "," + (Float.parseFloat(temp3[1]) - adjLat) + ",0";
		
		return point1 + " " + point2 + " " + point3 + " " + points[3];
	}
	
	private String adjustLowerRightGrid(String grid, float adjLon, float adjLat) {
		String[] points = grid.split(" ");
		String[] temp2 = points[1].split(",");
		String point2 = temp2[0] + "," + (Float.parseFloat(temp2[1]) - adjLat) + ",0";
		String[] temp3 = points[2].split(",");
		String point3 = (Float.parseFloat(temp3[0]) + adjLon) + "," + (Float.parseFloat(temp3[1]) - adjLat) + ",0";
		String[] temp4 = points[3].split(",");
		String point4 = (Float.parseFloat(temp4[0]) + adjLon) + "," + temp4[1] + ",0";
		
		return points[0] + " " + point2 + " " + point3 + " " + point4;
	}
	
	private String adjustGridTop(String grid, float adjLat) {
		String[] points = grid.split(" ");
		String[] temp2 = points[1].split(",");
		String point2 = temp2[0] + "," + (Float.parseFloat(temp2[1]) - adjLat) + ",0";
		String[] temp3 = points[2].split(",");
		String point3 = temp3[0] + "," + (Float.parseFloat(temp3[1]) - adjLat) + ",0";
		
		return points[0] + " " + point2 + " " + point3 + " " + points[3];
	}
	
	private String adjustGridBottom(String grid, float adjLat) {
		String[] points = grid.split(" ");
		String[] temp1 = points[0].split(",");
		String point1 = temp1[0] + "," + (Float.parseFloat(temp1[1]) + adjLat) + ",0";
		String[] temp4 = points[3].split(",");
		String point4 = temp4[0] + "," + (Float.parseFloat(temp4[1]) + adjLat) + ",0";
		
		return point1 + " " + points[1] + " " + points[2] + " " + point4;
	}

	public void printVariablesMaxMinValues(String outputFile) throws Exception {
		this.dataLoader.load();
		List<String> vars = this.dataLoader.getVariablesInfo();

		FileOutputStream fos = new FileOutputStream(outputFile);
		OutputStreamWriter osw = new OutputStreamWriter(fos);
		BufferedWriter writer = new BufferedWriter(osw);

		for (Iterator<String> iter = vars.iterator(); iter.hasNext();) {
			String name = iter.next();
			Variable var = dataLoader.getVariable(name);
			Map<String, Integer> map = dataLoader.getDimensionsLengths(name);

			if (map.get("LAY") == null) {
				writer.append("Variable " + name
						+ " doesn't have layer info.\n\n");
				continue;
			}

			int layers = map.get("LAY");

			writer.append("Variable: " + name + " (Unit: "
					+ var.findAttribute("units").getStringValue() + ")\n");

			for (int i = 0; i < layers; i++) {
				Variable slice = dataLoader.getSlice(name, "LAY", i);
				Array array = slice.read();
				float[] data = dataLoader.getFloatValues(array);
				getMinMaxValues(data);

				writer.append("\tLayer " + i + ": min = " + this.minValue
						+ " max = " + this.maxValue + "\n");
			}
		}

		this.dataLoader.close();
		writer.close();
	}

	public List<Integer> setImageColors(List<Integer> rgbs) {
		// NOTE: to be implemented
		return rgbs;
	}

	public void closeDataFile() throws Exception {
		this.dataLoader.close();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String usage1 = "Usage:\n\tjava -jar kmlGenerator.jar [netcdf_file] [vgrid_file]\n"
				+ "\t[output_file] [variable_name] [layer] [show_grids?(true/false or yes/no)] [title]\n" 
				+ "[conversion efficient] [\"multiply/divide\"]";
		String usage2 = "Use the following to get variable info:\n\tjava -jar kmlGenerator.jar -listVar [netcdf_file]\n";
		List<String> arguments = Arrays.asList(args);
		WritePolygons polygonWriter = null;

		try {
			if (arguments.contains("-dumpMaxMin")) {
				polygonWriter = new WritePolygons(args[1]);
				polygonWriter.printVariablesMaxMinValues(args[2]);
				System.exit(0);
			}

			if (arguments.contains("?") || arguments.contains("-help")
					|| arguments.contains("-h")) {
				System.err.println(usage1);
				System.err.println();
				System.err.println(usage2);
				System.exit(0);
			}

			if (arguments.contains("-listVar") && arguments.size() > 1) {
				polygonWriter = new WritePolygons(args[1]);
				polygonWriter.printVariablesInfo();
				System.exit(0);
			}

			if (args.length < 6) {
				System.err.println("Use option -h or -help for usage.");
				System.exit(0);
			}

			float[] userBins = polygonWriter.parseUserSpecifiedBins(args[5]);
			String opf = args[2].lastIndexOf(".kmz") > 0 ? args[2] : args[2]
					+ ".kmz";
			float convEff = (args.length > 9) ? Float.parseFloat(args[9]) : 0; 
			boolean multiply = (args.length > 10) ? (args[10].equalsIgnoreCase("multiply")) : false;
			boolean division = false;
			
			if (convEff > 0) {
				if (multiply)
					division = false;
				else
					division = true;
			} 
			
			if (convEff <= 0) {
				multiply = false;
				division = false;
			}
			
			polygonWriter = new WritePolygons(args[0]);
			polygonWriter.writePolygons(new File(args[1]), new File(opf),
					args[3], Integer.parseInt(args[4]), userBins, Integer
							.parseInt(args[6]), (args[7]
							.equalsIgnoreCase("true")
							|| args[7].equalsIgnoreCase("yes") ? true : false),
					args[8], convEff, multiply, division);
			polygonWriter.closeDataFile();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Use option -h or -help for usage.");
			System.exit(1);
		}

	}

}
