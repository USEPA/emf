package gov.epa.mims.analysisengine.table.persist;

import gov.epa.mims.analysisengine.gui.TreeDialog;
import gov.epa.mims.analysisengine.tree.AnalysisOptions;
import gov.epa.mims.analysisengine.tree.Branch;
import gov.epa.mims.analysisengine.tree.DataSets;
import gov.epa.mims.analysisengine.tree.PageConstantsIfc;
import gov.epa.mims.analysisengine.tree.PageType;

import java.awt.Component;
import java.io.File;
import java.util.Arrays;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.table.DefaultTableModel;

public class SaveConfigModel extends DefaultTableModel {

	/** confignames to be displayed on the ConfigList of SaveConfigGUI */
	private String[] configNames;

	/** column names to be displayed on the top of the ConfigTable */
	private static String[] columnNames = { "Configurations", "Type", "Save?" };

	/** AnalysisConfiguration view and manipulated by this Class */
	private AnalysisConfiguration aconfig;

	public SaveConfigModel(AnalysisConfiguration config) throws Exception {
		super();
		if (config == null) {
			throw new Exception("There are no configurations available");
		}
		aconfig = config;
		configNames = aconfig.getConfigNames();
		if (configNames.length == 0) {
			throw new Exception("There are no configurations available");
		}
		createColumnAndData();
	}

	private void createColumnAndData() {
		Vector toSaveOrNot = new Vector();
		Vector plotTypes = new Vector();

		for (int i = 0; i < configNames.length; i++) {
			plotTypes.add(getPlotType(configNames[i]));
		}

		for (int i = 0; i < configNames.length; i++) {
			toSaveOrNot.add(Boolean.valueOf("true"));
		}
		addColumn(columnNames[0], configNames);
		addColumn(columnNames[1], plotTypes);
		addColumn(columnNames[2], toSaveOrNot);
	}

	public String getPlotType(String configName) {
		Data dat = aconfig.getConfig(configName);
		if (dat != null && dat.info != null)
			return dat.info.getPlotType();
		return "Table Configuration";
	}

	public Class getColumnClass(int index) {
		if (index == 2) {
			return Boolean.class;
		}
		return String.class;
	}

	public void remove(int rowIndex) {
		aconfig.removeKey(getConfigNames()[rowIndex]);
	}

	/*****************************************
	 * Added to remove configuration directly
	 * from config name in case the row index
	 * is a wrong one. Qun He 6/1/2007
	 * @param configName
	 *****************************************/
	public void remove(String configName) {
		aconfig.removeKey(configName);
	}

	public void renameConfig(int row, String newName) throws Exception {
		String[] configNames = getConfigNames();
		if (newName.equals(configNames[row])) {
			return;
		}
		if (aconfig.getConfig(newName) != null) {
			throw new Exception(newName + " already exists");
		}

		Data dat = aconfig.getConfig(configNames[row]);
		if (dat == null || dat.configType == Data.TABLE_TYPE) {
			throw new Exception("You cannot rename a table configuration");
		}
		aconfig.removeKey(configNames[row]);
		aconfig.storePlotConfig(newName, dat, true);

	}

	public String[] getConfigNames() {
		return aconfig.getConfigNames();
	}

	public void showTree(String configname) throws Exception {
		Data dat = aconfig.getConfig(configname);
		if (dat.configType == Data.TABLE_TYPE)
			throw new Exception("You cannot edit a Table Configuration");
		JFrame newFrame = new JFrame();
		dat = TreeDialog.showTreeDialog(newFrame, dat, aconfig.getDataSets(dat.info, newFrame), null, null);
		aconfig.storePlotConfig(configname, dat, true);
	}

	public void showPlot(int row, Component parent) throws Exception {
		String pageType = null;
		AnalysisOptions options;
		Data dat = aconfig.getConfig((String) getValueAt(row, 0));

		if (dat != null) {
			if (dat.configType == Data.TABLE_TYPE)
				return;
			DataSets dset = aconfig.getDataSets(dat.info, parent);
			DataSets tempTree = (DataSets) (dat.tree);

			if (dset != null) {
				options = (AnalysisOptions) tempTree.getChild(0);
				pageType = setToDefaultScreenPageType(options);
				dset.add(tempTree.getChild(0));
			} else {
				throw new Exception("Empty Data Set");
			}

			removeColumnsNotAvailableInNewDatasets(dset, tempTree);

			TreeDialog.createPlotWithoutGUI(dset, dset, null, null);

			if (pageType != null) {
				PageType pt = (PageType) options.getOption("PAGE_TYPE");
				pt.setTextString(pageType);
				options.addOption("PAGE_TYPE", pt);
			}
		}
	}

	private void removeColumnsNotAvailableInNewDatasets(DataSets newDataset, Branch tree) throws Exception {
		UpdateDataColumns update = new UpdateDataColumns(newDataset, tree);
		update.update();
	}

	/**
	 * Sets the plot output to SCREEN
	 */
	protected String setToDefaultScreenPageType(AnalysisOptions options) {
		PageType pt = (PageType) options.getOption("PAGE_TYPE");
		String retval = null;

		if (pt != null) {
			if (!((pt.getForm()).equals(PageConstantsIfc.SCREEN))) {
				retval = pt.getTextString();
				pt.setForm(PageConstantsIfc.SCREEN);
				options.addOption("PAGE_TYPE", pt);
			}
		}
		return retval;
	}

	public void reorderColumns() {
		Vector oldconfigNames = new Vector();
		Vector oldTypes = new Vector();
		Vector oldValues = new Vector();
		for (int i = 0; i < getRowCount(); i++) {
			oldconfigNames.add(getValueAt(i, 0));
			oldTypes.add(getValueAt(i, 1));
			oldValues.add(getValueAt(i, 2));
		}
		String[] configNames = aconfig.getConfigNames();
		Vector data = new Vector();
		for (int i = 0; i < configNames.length; i++) {
			Vector entry = new Vector();
			entry.add(configNames[i]);
			entry.add(oldTypes.remove(oldconfigNames.indexOf(configNames[i])));
			entry.add(oldValues.remove(oldconfigNames.indexOf(configNames[i])));
			oldconfigNames.remove(configNames[i]);
			data.add(entry);
		}
		setDataVector(data, new Vector(Arrays.asList(columnNames)));
	}

	public String getConfigName(int row) {
		String[] names = aconfig.getConfigNames();
		if (row >= 0 && row < names.length) {
			return names[row];
		}
		return null;
	}

	public Vector getSelectedValues() {
		Vector result = new Vector();
		for (int i = 0; i < getRowCount(); i++)
			if (((Boolean) getValueAt(i, 2)).booleanValue() == true)
				result.add(getValueAt(i, 0));
		return result;
	}

	public void saveConfiguration(boolean binaryFormat, File file) throws java.io.IOException {
		file.createNewFile();
		aconfig.saveConfiguration(binaryFormat, file, getSelectedValues());
	}

	public boolean isCellEditable(int row, int column) {
		if (column == 0) {
			String value = (String) getValueAt(row, column);
			if (value.equals(AnalysisConfiguration.TABLE_FILTER_CRITERIA)
					|| value.equals(AnalysisConfiguration.TABLE_FORMAT)
					|| value.equals(AnalysisConfiguration.TABLE_SORT_CRITERIA)) {
				return false;
			}
			return true;
		} else if (column == 1) {
			return false;
		}
		return true;
	}

	public void setConfig(AnalysisConfiguration config) {
		this.aconfig = config;
	}

}
