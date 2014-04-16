package gov.epa.mims.analysisengine.table.persist;

import gov.epa.mims.analysisengine.tree.Branch;
import gov.epa.mims.analysisengine.tree.DataSets;
import gov.epa.mims.analysisengine.tree.Node;
import gov.epa.mims.analysisengine.tree.Plot;
import gov.epa.mims.analysisengine.tree.ScatterPlot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UpdateDataColumns {

	private DataSets newDataset;

	private Branch tree;

	public UpdateDataColumns(DataSets newDataset, Branch tree) {
		this.newDataset = newDataset;
		this.tree = tree;
	}

	public void update() throws Exception {
		Plot[] plots = plotsInConfig(tree);
		Set keysSet = newDataset.getKeys();

		for (int i = 0; i < plots.length; i++) {
			Plot plot = plots[i];

			if (!(plot instanceof ScatterPlot)) {
				Object[] retVal = new Object[1];
				retVal[0] = stripNonCurrentKeys(plot, keysSet);
				plots[i].setDataSetKeys(retVal);
			} else {
				updateForScatterPlotsWithDataForXAxis(plot, keysSet);
			}
		}
	}

	/****************************************************************
	 * Added to keep in plot only those column names available in
	 * current data set.
	 * 
	 *  Qun He 5/31/2007
	 *****************************************************************/
	
	private String[] stripNonCurrentKeys(Plot plot, Set keys) {
		if (!plot.keysInitialized())
			return (String[])keys.toArray(new String[0]);
		
		List keysList = plot.getDataKeyList();
		List existedKeys = new ArrayList();
		
		for (int i = 0; i < keysList.size(); i++) {
			Object key = keysList.get(i);
		
			if (keys.contains(key))
				existedKeys.add(key);
		}
		
		return (String[])existedKeys.toArray(new String[0]);
	}

	private void updateForScatterPlotsWithDataForXAxis(Plot plot, Set allNewKeys) throws Exception {

		String[] xKeys = plot.getKeys(0);
		// x axis dataset for scatter plot is optional and if key exist the length always 1
		String[] newYKeys = null;
		if (xKeys.length > 0) {
			checkForXKey(allNewKeys, xKeys[0]);
			newYKeys = stripNonCurrentKeys(plot, removeXKey(allNewKeys, xKeys[0]));
		} else {
			newYKeys = stripNonCurrentKeys(plot, allNewKeys);
		}

		Object[] retVal = new Object[2];
		retVal[0] = xKeys;
		retVal[1] = newYKeys;
		plot.setDataSetKeys(retVal);
	}

	private void checkForXKey(Set allKeys, String xKey) throws Exception {
		if (!allKeys.contains(xKey))
			throw new Exception("The data column '" + xKey + "' for x axis is not in the table");
	}

	private Set removeXKey(Set allKeys, String xKey) {
		List keys = new ArrayList();
		keys.addAll(allKeys);
		Set keysSet = new HashSet();
		
		if (allKeys.contains(xKey))
			keys.remove(xKey);
		
		keysSet.addAll(keys);
		
		return keysSet;
	}

	private Plot[] plotsInConfig(Branch tree) {
		List plots = new ArrayList();
		getPlotsFromTree(plots, tree);
		return (Plot[]) plots.toArray(new Plot[0]);
	}

	private void getPlotsFromTree(List plots, Node tree) {
		int childCount = tree.getChildCount();
		for (int i = 0; i < childCount; i++) {
			Node child = tree.getChild(i);
			if (child instanceof Plot) {
				plots.add(child);
				return;
			}
			getPlotsFromTree(plots, child);
		}
	}

}
