package gov.epa.emissions.googleearth.kml.vgrid;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ucar.ma2.Array;
import ucar.nc2.Dimension;
import ucar.nc2.Group;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.util.CancelTask;

public class NetcdfDataLoader {

	private String filePath;

	private NetcdfFile dataFile = null;

	private List<String> variableNames;

	private Date sdate;

	private int stimeValue = 0;

	private int tstepValue = 0;

	private int numOfLayers = 0;

	private int numOfVariables = 0;
	
	private int numOfRows = 0;
	
	private int numOfCols = 0;

	private String fileDesc;

	private String gridName;

	private List<String> variables;
	
	private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

	public NetcdfDataLoader(String file) {
		this.filePath = file;
	}

	public void load() throws Exception {
		URL url = new File(filePath).toURI().toURL();

		try {
			String urlString = url.toExternalForm();

			if (url.getProtocol().equals("file")) {
				urlString = new URI(urlString).getPath();
			}

			dataFile = NetcdfFile.open(urlString, new CancelTask() {
				public boolean isCancel() {
					return false;
				}

				public void setError(String msg) {
				}
			});
			
			variables = getVariablesInfo();
		} catch (URISyntaxException e) {
			throw new Exception("Error reading netcdf file", e);
		} catch (IOException e) {
			throw new Exception("Error reading netcdf file", e);
		}
	}

	public List<String> getVariablesInfo() throws Exception {
		if (dataFile == null || dataFile.isClosed())
			throw new Exception("Data file not opened or is closed.");
		
		List<Variable> vars = (List<Variable>) dataFile.getVariables();
		
		variableNames = new ArrayList<String>();
		Variable varHasLayer = null;

		for (Iterator<Variable> iter = vars.iterator(); iter.hasNext();) {
			Variable var = iter.next();
			variableNames.add(var.getName());
			
			if (var.findDimensionIndex("LAY") > 0)
				varHasLayer = var;
		}

		loadGroupInfo(varHasLayer);
		
		return variableNames;
	}

	public Map<String, Integer> getDimensionsLengths(String varName)
			throws Exception {
		Map<String, Integer> map = new HashMap<String, Integer>();
		Variable var = getVariable(varName);
		List<Dimension> dims = var.getDimensions();

		for (Iterator<Dimension> iter = dims.iterator(); iter.hasNext();) {
			Dimension dim = iter.next();
			map.put(dim.getName().toUpperCase(), new Integer(dim.getLength()));
		}

		return map;
	}

	public Variable getVariable(String name) throws Exception {
		List<Variable> vars = (List<Variable>) dataFile.getVariables();

		if (vars == null || vars.size() == 0)
			throw new Exception("No variables found in data file.");

		for (Iterator<Variable> iter = vars.iterator(); iter.hasNext();) {
			Variable var = iter.next();

			if (var.getName().toLowerCase().equals(name.toLowerCase()))
				return var;
		}

		return null;
	}
	
	public Variable getSlice(String variableName, String dimension, int index) throws Exception {
		Variable var = getVariable(variableName);
		int dim = var.findDimensionIndex(dimension);
		
		if (var.getDimension(dim).getLength() == 1)
			return var;
		
		return var.slice(dim, index);
	}
	
	public float[] getFloatValues(Array array) throws Exception {
		return (float[]) array.get1DJavaArray(Float.class);
	}
	
	public double[] getDoubleValues(Array array) throws Exception {
		return (double[]) array.get1DJavaArray(Double.class);
	}

	public int[] getIntValues(Array array) throws Exception {
		return (int[]) array.get1DJavaArray(Integer.class);
	}
	
	private void loadGroupInfo(Variable var) throws Exception {
		Group grp = var.getParentGroup();
		String sdateValue = grp.findAttribute("SDATE").getNumericValue().toString();
		stimeValue = grp.findAttribute("STIME").getNumericValue().intValue();
		tstepValue = grp.findAttribute("TSTEP").getNumericValue().intValue();
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.DAY_OF_YEAR, Integer.parseInt(sdateValue.substring(4)));
		String temp = formatter.format(calendar.getTime());
		temp = sdateValue.substring(0,4) + temp.substring(4);
		sdate = formatter.parse(temp);
		
		numOfLayers = grp.findAttribute("NLAYS").getNumericValue().intValue();
		numOfVariables = grp.findAttribute("NVARS").getNumericValue().intValue();
		numOfRows = grp.findAttribute("NROWS").getNumericValue().intValue();
		numOfCols = grp.findAttribute("NCOLS").getNumericValue().intValue();
		fileDesc = grp.findAttribute("FILEDESC").getStringValue();
		gridName = grp.findAttribute("GDNAM").getStringValue();
	}
	
	public int getStimeValue() {
		return stimeValue;
	}
	
	public int getTstepValue() {
		return tstepValue;
	}
	
	public int getNumOfRows() {
		return numOfRows;
	}
	
	public int getNumOfCols() {
		return numOfCols;
	}
	
	public Date getSdate() {
		return sdate;
	}
	
	public Integer[] getLayers() {
		if (numOfLayers <= 0)
			return new Integer[0];
		
		Integer[] layers = new Integer[this.numOfLayers];
		
		for(int i = 0; i < numOfLayers; i++)
			layers[i] = new Integer(i+1);
		
		return layers;
	}
	
	public void printVariablesInfo() throws Exception {
		load();
		close();
		
		StringBuilder sb = new StringBuilder();
		sb.append("List of variables (total " + numOfVariables + "):\n");

		for (int i = 0; i < variables.size() - 1; i++)
			sb.append(variables.get(i) + ",");
		
		sb.append(variables.get(variables.size() - 1) + "\n\n");
		sb.append("Start date: " + formatter.format(getSdate()) + "; ");
		sb.append("Start time: " + getStimeValue() + "; ");
		sb.append("Time step: " + getTstepValue() + "\n\n");
		sb.append("Number of layers: " + numOfLayers + "\n\n");
		sb.append("Grid name: " + gridName + "\n\n");
		sb.append("File discription: " + fileDesc.substring(0, 128) + "\n\n");
		
		System.err.println(sb.toString());
	}

	public void close() throws Exception {
		this.dataFile.close();
	}


}
