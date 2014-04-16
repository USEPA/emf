package gov.epa.mims.analysisengine.table.persist;

import gov.epa.mims.analysisengine.gui.DefaultUserInteractor;
import gov.epa.mims.analysisengine.gui.GUIUserInteractor;
import gov.epa.mims.analysisengine.gui.TreeDialog;
import gov.epa.mims.analysisengine.gui.UserInteractor;
import gov.epa.mims.analysisengine.table.OverallTableModel;
import gov.epa.mims.analysisengine.table.filter.FilterCriteria;
import gov.epa.mims.analysisengine.table.format.ColumnFormatInfo;
import gov.epa.mims.analysisengine.table.plot.PlottingInfo;
import gov.epa.mims.analysisengine.table.sort.SortCriteria;
import gov.epa.mims.analysisengine.tree.AnalysisOptions;
import gov.epa.mims.analysisengine.tree.Branch;
import gov.epa.mims.analysisengine.tree.DataSets;
import gov.epa.mims.analysisengine.tree.PageType;

import java.awt.Component;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.event.TableModelEvent;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class AnalysisConfiguration implements Serializable {

	static final long serialVersionUID = 1;
	
	/** a sorted mapping between plot name and the <tree, plottinginfo> */
	private TreeMap configs;

	/**
	 * An array of names to keep track of the inserted order of configurations
	 */
	private Vector insertedOrder;

	private OverallTableModel model;

	public final static String TABLE_FORMAT = "Table Format";

	public final static String TABLE_SORT_CRITERIA = "Table Sort Criteria";

	public final static String TABLE_FILTER_CRITERIA = "Table Filter Criteria";

	public AnalysisConfiguration(OverallTableModel model) {
		super();
		configs = new TreeMap();
		insertedOrder = new Vector();
		this.model = model;
		configs.put(TABLE_FILTER_CRITERIA, null); // placeholder
		configs.put(TABLE_SORT_CRITERIA, null); // placeholder
		configs.put(TABLE_FORMAT, null); // placeholder
		insertedOrder.add(TABLE_FILTER_CRITERIA);
		insertedOrder.add(TABLE_SORT_CRITERIA);
		insertedOrder.add(TABLE_FORMAT);
	}

	public void refreshTableConfig() {
		Data dat = new Data();
		dat.configType = Data.TABLE_TYPE;
		dat.criteria = model.getFilterCriteria();
		configs.put(TABLE_FILTER_CRITERIA, dat);

		dat = new Data();
		dat.configType = Data.TABLE_TYPE;
		dat.criteria = model.getSortCriteria();
		configs.put(TABLE_SORT_CRITERIA, dat);

		dat = new Data();
		dat.configType = Data.TABLE_TYPE;
		dat.criteria = new Hashtable();
		((Hashtable) dat.criteria).putAll(model.getColumnFormatInfo());
		configs.put(TABLE_FORMAT, dat);
	}

	/**
	 * loads configuration from the File into the object
	 * 
	 * @param binaryFormat
	 */
	public void loadConfiguration(File configFile, boolean applyTableConfig, boolean binaryFormat,
			 Component parent) throws Exception {
		deserialize(configFile, binaryFormat);

		Data data = (Data) configs.get(TABLE_FORMAT);
		if (data != null && data.criteria != null) {
			Hashtable formats = checkCompatibility((Hashtable) data.criteria);
			data.criteria = formats;
			if (formats != null) {
				if (applyTableConfig) {
					Set keys = formats.keySet();
					Iterator iter = keys.iterator();
					while (iter.hasNext()) {
						String key = (String) iter.next();
						model.setColumnFormatInfo(key, (ColumnFormatInfo) formats.get(key));
					}// while(iter.hasNext())
				}// if(applyTableConfig)s
			}// if(formats != null)s
		}// if(data != null)
		data = (Data) configs.get(TABLE_FILTER_CRITERIA);
		if (data != null && data.criteria != null) {
			try {
				data.criteria = ((FilterCriteria) data.criteria).checkCompatibility(model, parent);
				if (applyTableConfig) {
					((FilterCriteria) data.criteria).setTableModel(model);
					model.filterRows((FilterCriteria) data.criteria);
					model.filterColumns((FilterCriteria) data.criteria);
				}// if(applyTableConfig)
			}// try
			catch (Exception e) {
				data.criteria = null;
				DefaultUserInteractor.get().notify(null, "FilterCriteria", e.getMessage(), UserInteractor.WARNING);

			}// catch
		}// if(data != null && data.criteria != null)
		data = (Data) configs.get(TABLE_SORT_CRITERIA);
		if (data != null && data.criteria != null) {
			try {
				data.criteria = ((SortCriteria) data.criteria).checkCompatibility(model, parent);
				if (applyTableConfig) {
					model.sort((SortCriteria) data.criteria);
				}
			} catch (Exception e) {
				data.criteria = null;
				DefaultUserInteractor.get().notify(null, "Sort Criteria", e.getMessage(), UserInteractor.WARNING);
			}

		}

		insertedOrder.clear();
		insertedOrder.addAll(configs.keySet());
		if (applyTableConfig) {
			model.tableChanged(new TableModelEvent(model));
		}
	}

	private void deserialize(File configFile, boolean binaryFormat) throws Exception {
		ObjectInputStream ois = null;
		try {
			if (binaryFormat)
				ois = new ObjectInputStream(new FileInputStream(configFile));
			else
				ois = new XStream(new DomDriver()).createObjectInputStream(new FileReader(configFile));
			configs = (TreeMap) ois.readObject();
		} catch (FileNotFoundException e) {
			throw new Exception("Could not find the file '" + configFile.getName() + "'");
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("Please check the file format");
		} finally {
			if (ois != null)
				ois.close();
		}
	}

	/**
	 * checks the availability of column names in the TableModel in the loaded configuration
	 */
	private Hashtable checkCompatibility(Hashtable formats) {
		boolean showError = true;
		String[] colNames = model.getColumnNames();
		Vector allColNames = new Vector();
		for (int i = 0; i < colNames.length; i++) {
			allColNames.add(colNames[i]);
		}// for(i)

		Set configColNames = formats.keySet();
		Hashtable newFormats = new Hashtable();
		String missingColNames = "";
		Iterator iterator = configColNames.iterator();
		int count = 0;
		// for(int i=0; i < colCount ; i++)
		while (iterator.hasNext()) {
			String colName = (String) iterator.next();
			if (allColNames.contains(colName)) {
				newFormats.put(colName, formats.get(colName));
				count++;
			} else {
				if (!colName.equalsIgnoreCase("Row")) {
					missingColNames = missingColNames + colName + ", ";
				}
				// System.out.println("missing:"+colName);
			}
		}// while

		//NOTE: need to investigate if this is the place where multiple message boxes poping up ?
		if (count == 0 && showError) {
			DefaultUserInteractor.get().notify(null, "Error",
					"The table does not have " + "any column names exist on the configuration file",
					UserInteractor.ERROR);
			showError = false;
			newFormats = null;
			return null;
		} else if (count < configColNames.size() - 1 && showError) // deducting 1 for the first col
		{
			missingColNames = missingColNames.substring(0, missingColNames.length() - 2);
			DefaultUserInteractor.get()
					.notify(
							null,
							"Warning",
							"The table does not have " + missingColNames
									+ " column names that exist in the configuration file", UserInteractor.WARNING);
			showError = false;
			return newFormats;
		} else // count == configColNames.size()-1
		{
			newFormats = null;
			return formats;
		}
	}

	/**
	 * saves plots based on configuration and table information to a specific directory with filenames defaulting to
	 * Plot Name and with default File Type
	 */

	public void showOrSaveConfiguredPlots(String path, int fileTypes, String[] plotNames, Component parent) throws Exception {
		String extensions[] = { PageType.JPG_EXT, PageType.PS_EXT, PageType.PDF_EXT, PageType.PNG_EXT, PageType.PTX_EXT };
		StringBuffer error = new StringBuffer();
		for (int i = 0; i < plotNames.length; i++) {
			try {
				Data value = (Data) configs.get(plotNames[i]);
				if (value.configType == Data.TABLE_TYPE)
					continue; // ignore Table Configurations
				DataSets dset = getDataSets(value.info, parent);
				Branch tree = (value.tree); // .clone();
				removeColumnsNotAvailableInNewDatasets(dset, tree);
				dset.add(tree.getChild(0));

				PageType pt = (PageType) (((AnalysisOptions) (tree.getChild(0))).getOption("PAGE_TYPE"));
				HashMap textvalues = new HashMap();
				String fullname;
				if (pt.getForm().equals("SCREEN")) {
					fullname = path + File.separator + plotNames[i] + "." + extensions[fileTypes];
				} else {
					String filename = pt.getFilename();
					filename = filename.substring(filename.lastIndexOf(File.separatorChar));
					fullname = path + File.separator + filename;
				}

				textvalues.put("PAGE_TYPE", fullname);
				TreeDialog.createPlotWithoutGUI(dset, dset, null, textvalues);
			} catch (Exception e) {
				e.printStackTrace();
				error.append("Error saving " + plotNames[i] + "\n. Exception :" + e.getMessage());
			}
		}

		if (error.length() > 1) {
			throw new Exception(error.toString());
		}
	}

	private void removeColumnsNotAvailableInNewDatasets(DataSets newDataset, Branch tree) throws Exception {
		UpdateDataColumns update = new UpdateDataColumns(newDataset, tree);
		update.update();
	}

	public String[] getConfigNames() {
		return (String[]) insertedOrder.toArray(new String[0]);
	}

	/**
	 * stores plot configuration in the AnalysisConfiguration object
	 */
	public void storePlotConfig(String plotName, Data data, boolean overWrite) {
		if (plotName == null || plotName.trim().length() == 0) {
			new GUIUserInteractor().notify(null, "Warning", "Invalid plot name "
					+ "specified: plot configuration was not saved", UserInteractor.ERROR);
			return;
		}

		int write = UserInteractor.YES;

		if ((plotName.equals("Table Sort Criteria") || plotName.equals("Table Filter Criteria") || plotName
				.equals("Table Format"))) {
			new GUIUserInteractor().notify(null, "Error", plotName + " is reserved for " + "table configuration",
					UserInteractor.ERROR);
		}

		if (!overWrite && insertedOrder.contains(plotName)) {
			write = new GUIUserInteractor().selectOption(null, "Error", "A plot with the name " + plotName
					+ " already exists.\nDo you want to overwrite?", UserInteractor.YES_NO, UserInteractor.YES);
		}

		if (write == UserInteractor.YES) {
			if (configs.containsKey(plotName))
				configs.remove(plotName);
			
			if (insertedOrder.contains(plotName))
				insertedOrder.remove(plotName);
			
			configs.put(plotName, data);
			insertedOrder.add(plotName);
		}
	}

	/**
	 * stores plot configuration in the AnalysisConfiguration object
	 */
	public void storePlotConfig(String plotName, Branch tree, PlottingInfo info, boolean overWrite) {
		Data dat = new Data();
		dat.info = info;
		dat.tree = tree;
		dat.configType = Data.PLOT_TYPE;
		storePlotConfig(plotName, dat, overWrite);
	}

	/**
	 * returns the sorted keySet of plots HashMap
	 */
	public String[] getKeys() {
		Set result = configs.keySet();
		if (result != null) {
			String[] a = {};
			return (String[]) result.toArray(a);
		}
		return null;
	}

	/**
	 * returns number of Configuration items in the AnalysisConfiguration
	 */
	public int getCount() {
		return configs.size();
	}

	/**
	 * remove entry corresponding to key
	 */
	protected void removeKey(String key) {
		insertedOrder.remove(key);
		configs.remove(key);
	}

	/**
	 * getter method for configuration based on key
	 */

	public Data getConfig(String key) {
		return (Data) configs.get(key);
	}

	/**
	 * getter method for DataSets based on plottingInfo and Table
	 */

	public DataSets getDataSets(PlottingInfo info, Component parent) throws Exception {
		if (info == null) {
			return null;
		}
		info.setOverallTableModel(model, parent);
		return info.createDataSets();
	}

	public void setModel(OverallTableModel model) {
		this.model = model;
	}

	public OverallTableModel getModel() {
		return model;
	}

	public void saveConfiguration(boolean binaryFormat, File filename, Vector selectedValues) throws IOException {
		TreeMap temp = new TreeMap();
		refreshTableConfig();
		for (int i = 0; i < selectedValues.size(); i++) {
			String value = (String) selectedValues.get(i);
			temp.put(value, configs.get(value));
		}
		ObjectOutputStream oos = null;
		if (binaryFormat)
			oos = new ObjectOutputStream(new FileOutputStream(filename));
		else {
			XStream xstream = new XStream();
			oos = xstream.createObjectOutputStream(new FileWriter(filename));
		}
		oos.writeObject(temp);
		oos.close();

	}
}
