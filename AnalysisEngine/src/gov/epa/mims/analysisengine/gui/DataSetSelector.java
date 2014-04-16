package gov.epa.mims.analysisengine.gui;

import gov.epa.mims.analysisengine.tree.DataSetIfc;
import gov.epa.mims.analysisengine.tree.DataSetInfo;
import gov.epa.mims.analysisengine.tree.DataSetsAdapter;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.BevelBorder;

/**
 * Select the data sets to use in a plot from the list of available datasets
 * 
 * @author Alison Eyth, CEP UNC
 * @version $Id: DataSetSelector.java,v 1.7 2007/05/22 20:57:27 qunhe Exp $
 */
public class DataSetSelector extends JDialog {

	/*******************************************************************************************************************
	 * 
	 * fields
	 * 
	 ******************************************************************************************************************/

	/** the string array to edit * */
	private String[] stringArray = null;

	/** a list to include the selected values * */
	private JList selectedList = null;

	/** a list to show all the values * */
	private JList overallList = null;

	/** the arraylist holding the objects to be displayed * */
	private ArrayList objectList = null;

	private Vector selectedDataSetWithKeys = new Vector();

	private Vector selectedKeys = new Vector();

	/** the DataSetsAdapter to choose data sets from */
	private DataSetsAdapter dataSetsAdapter = null;

	private Vector allDataSets = null;

	private DataSetInfo dataSetInfo = null;

	private Class dataSetType = null;

	/** whether the user selected OK to close the window - false if they did cancel */
	private boolean didOK = false;

	private Vector initialSelection;

	private TreeMap allDSMap = null;

	/*******************************************************************************************************************
	 * 
	 * methods
	 * 
	 ******************************************************************************************************************/

	public DataSetSelector(DataSetsAdapter dataSetsAdapter, Vector initialSelection, DataSetInfo dsInfo) {
		this.dataSetsAdapter = dataSetsAdapter;
		this.dataSetInfo = dsInfo;
		this.dataSetType = dsInfo.getClassType();
		this.initialSelection = initialSelection;
		setTitle("Select Data Sets");
		// set up list of all data sets and selected data sets
		initializeDataSets(initialSelection);
		initialize();

	}// ArrayEditorDialog(double[])

	private void initializeDataSets(Vector initialSelection) {
		// get iterator over all data sets
		Iterator allDSIt = dataSetsAdapter.getDataSetKeys(dataSetType, this.getContentPane()).iterator();

		// make the tree map so the available data sets are sorted alphabetically
		allDSMap = new TreeMap();
		while (allDSIt.hasNext()) {
			Object key = allDSIt.next();
			DataSetIfc dataSet = dataSetsAdapter.getDataSet(key);
			DataSetWithKey dswk = new DataSetWithKey(key, dataSet);
			allDSMap.put(key, dswk);
		}
		allDataSets = new Vector(allDSMap.values());

		selectedDataSetWithKeys = new Vector();
		if (initialSelection != null) {
			// select all data sets in initial selection
			Iterator selDSIt = initialSelection.iterator();
			while (selDSIt.hasNext()) {
				DataSetWithKey dswk = (DataSetWithKey) selDSIt.next();
				Object key = dswk.key;
				DataSetIfc existingDataSet = dataSetsAdapter.getDataSet(key);
				// make sure the data set in the initial selection exists in the adapter
				if (existingDataSet == null) {
					throw new IllegalArgumentException("Data set " + dswk.getName()
							+ " not found in available data sets");
				} else if (!existingDataSet.equals(dswk.dataSet)) {
					throw new IllegalArgumentException("Initial data set " + existingDataSet.toString()
							+ " was not found in data set list");
				}
				
				try {
					addDataSetToSelection(dswk, selectedDataSetWithKeys);
				} catch (Exception exc) {
					throw new IllegalArgumentException(exc.getMessage());
				}
			}
		}
	}

	/*
	 * if (initialSelection != null) { // select all data sets in initial selection Iterator selDSIt =
	 * initialSelection.getDataSetKeys(dataSetType, this.getContentPane()).iterator(); while (selDSIt.hasNext()) {
	 * 
	 * Object key = selDSIt.next(); DataSetIfc dataSet = initialSelection.getDataSet(key); DataSetWithKey dswk = new
	 * DataSetWithKey(key, dataSet); Object existingDataSet = dataSetsAdapter.getDataSet(key); // make sure the data set
	 * in the initial selection exists in the adapter if (existingDataSet == null) { throw new IllegalArgumentException(
	 * "No data set found for in data set list for key "+key.toString()); } else if (existingDataSet != dataSet) { throw
	 * new IllegalArgumentException( "Initial data set "+existingDataSet.toString()+ " was not found in data set list"); }
	 * try { addDataSetToSelection(dswk, selectedDataSetWithKeys); } catch (Exception exc) { throw new
	 * IllegalArgumentException(exc.getMessage()); } }
	 */

	private void initialize() {
		Container contentPane = this.getContentPane();
		contentPane.setLayout(new BorderLayout());
		JPanel listPanel = new JPanel();
		listPanel.setLayout(new GridLayout(1, 2, 5, 2));
		JPanel selectedPanel = new JPanel(new BorderLayout());
		JPanel availablePanel = new JPanel(new BorderLayout());
		JLabel selectedLabel = new JLabel("Selected Data Sets");
		JLabel availableLabel = new JLabel("Available Data Sets");

		selectedList = new JList();
		overallList = new JList();
		selectedPanel.add(selectedLabel, BorderLayout.NORTH);
		selectedPanel.add(new JScrollPane(selectedList), BorderLayout.CENTER);
		availablePanel.add(availableLabel, BorderLayout.NORTH);
		availablePanel.add(new JScrollPane(overallList));
		listPanel.add(selectedPanel);
		listPanel.add(availablePanel);
		overallList.setListData(allDataSets);
		// Add the selected data set to the selected list on a double click.
		overallList.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() >= 2) {
					Object obj = overallList.getSelectedValue();
					if (obj instanceof DataSetWithKey) {
						try {
							addDataSetToSelection((DataSetWithKey) obj, selectedDataSetWithKeys);
							selectedList.setListData(selectedDataSetWithKeys);
						} catch (Exception exp) {
							DefaultUserInteractor.get().notifyOfException(DataSetSelector.this,
									"Error adding data set", exp, UserInteractor.ERROR);
						}
					} // if
					else {
						DefaultUserInteractor.get().notify(
								DataSetSelector.this,
								"Incorrect Object Type",
								"Expected an object of type DataSetWithKey in " + "overallList, but found "
										+ obj.getClass() + "instead.", UserInteractor.ERROR);
					} // else
				}
				// TBD: would like to toggle the selection, but it doesn't leave it
				// selected at all with this code
				/*
				 * else if (e.getClickCount() == 1) { int idx = overallList.getSelectedIndex(); if
				 * (overallList.isSelectedIndex(idx)); overallList.removeSelectionInterval(idx,idx); }
				 */// if
			} // mouseClicked()
		} // MouseAdapter()
				);
		selectedList.setListData(selectedDataSetWithKeys);
		selectedList.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() >= 2) {
					removeSelectedDataSet();
				} // if
			} // mouseClicked()
		} // MouseAdapter()
				);

		contentPane.add(listPanel, BorderLayout.CENTER);

		// a panel for the add, remove and ok and cancel buttons
		JPanel buttonPanel = new JPanel(new GridLayout(2, 1));
		contentPane.add(buttonPanel, BorderLayout.SOUTH);
		JButton addButton = new JButton("Add to Selected");
		addButton.setToolTipText("Add the highlighted Available Data Sets to the Selected Data Sets");
		JButton removeButton = new JButton("Remove");
		removeButton.setToolTipText("Remove the highlighted data sets from the Selected Data Sets");
		// This needs to be final because it is referenced in the ListSelectionListener
		// method for the overallList below.
		final JButton viewButton = new JButton("View Data Sets");
		viewButton.setToolTipText("View the highlighted Available Data Sets");
		// Set this disabled initially because there are no items in the list for the user to view.
		JPanel button1Panel = new JPanel();
		button1Panel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		button1Panel.add(removeButton);
		button1Panel.add(addButton);
		button1Panel.add(viewButton);
		buttonPanel.add(button1Panel);
		OKCancelPanel ocPanel = new OKCancelPanel(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				// this automatically adds the selected data sets when you hit OK
				// only do this if it's the first time - otherwise if the person
				// has viewed a data set it will be added
				if (selectedKeys.size() == 0) {
					addSelectedDataSets();
				}
				didOK = true;
				dispose();
			}
		}, new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				// when cancel is selected, we need to restore the original state of the list
				objectList = null;
				dispose();
			}
		}, getRootPane());
		buttonPanel.add(ocPanel);
		objectList = new ArrayList();
		ActionListener editListener = null;

		// Select all items in the overall list by default, but only if nothing
		// is already selected
		if (selectedList.getModel().getSize() == 0) {
			overallList.setSelectionInterval(0, overallList.getModel().getSize() - 1);
		}

		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				addSelectedDataSets();
			}
		});

		// if (type.equals(STRINGS))
		{
			if (stringArray != null)
				for (int i = 0; i < stringArray.length; i++)
					objectList.add(stringArray[i]);

			addButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					addSelectedDataSets();
				}// actionPeformed()
			});// addActionListener()

			removeButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					removeSelectedDataSet();
				}// actionPeformed()
			});
			editListener = new ActionListener() {
				// Get the selected DataSets in the "Available Data Sets" list and present them to the user in a Dialog.
				public void actionPerformed(ActionEvent ae) {
					if (overallList.isSelectionEmpty())
						return;
					try {
						Object[] selObj = overallList.getSelectedValues();
						if (selObj == null || selObj.length == 0)
							return;

						// Exract the DataSetIfc's from the list of DataSetWithKeys.
						// This is needed because the DataSetViewer requires a list of DataSetIfc's.
						DataSetIfc[] selectedDataSets = new DataSetIfc[selObj.length];
						for (int i = selObj.length - 1; i >= 0; --i) {
							if (selObj[i] instanceof DataSetWithKey)
								selectedDataSets[i] = ((DataSetWithKey) selObj[i]).dataSet;
							else
								throw new Exception(
										"Selected objects in DataSetSelector list were not instances of DataSetWithKey as "
												+ "expected.\n Instead they were instances of " + selObj[0].getClass());
						}

						JDialog dataSetViewerDlg = new JDialog(DataSetSelector.this, "Data Set Viewer", true);
						DataSetViewer dataSetViewer = new DataSetViewer(dataSetViewerDlg, selectedDataSets);
						dataSetViewerDlg.getContentPane().add(dataSetViewer);
						dataSetViewerDlg.pack();
						dataSetViewerDlg.setVisible(true);
					} catch (Exception e) {
						(new GUIUserInteractor()).notify(DataSetSelector.this, "Error evaluating entry",
								e.getMessage(), UserInteractor.ERROR);
						return;
					}
				}
			};
			viewButton.addActionListener(editListener);

		}

		setModal(true);
		pack();
		// make the dialog come up in the center of the screen
		this.setLocation(30, 30);
		setVisible(true);
	}// initialize()

	public boolean getDidOK() {
		return didOK;
	}

	/**
	 * Add the datasets selected in the "Available Data Sets" window if there are currently no datasets selected.
	 */
	private void addSelectedDataSets() {
		try {
			// insert the items in a sorted fashion
			Object[] setsToAdd = overallList.getSelectedValues();
			if (setsToAdd.length == allDataSets.size()) {
				if (setsToAdd.length > 1) {
					if (dataSetInfo.getMaxNumber() == 1) {
						throw new Exception("Only one data set of this type can be used on this plot");
					}
				}
				// all data sets are selected, so add them all
				selectedDataSetWithKeys.clear();
				selectedDataSetWithKeys.addAll(allDSMap.values());
			} else {
				for (int i = 0; i < setsToAdd.length; i++) {
					DataSetWithKey dswk = (DataSetWithKey) setsToAdd[i];
					addDataSetToSelection(dswk, selectedDataSetWithKeys);
				}
			}
			selectedList.setListData(selectedDataSetWithKeys);
		} catch (Exception e) {
			DefaultUserInteractor.get().notify(this, "Error adding data sets", e.getMessage(), UserInteractor.ERROR);
			return;
		} finally {
			overallList.clearSelection();
		}
	}

	/**
	 * Adds data set to selection in sorted order based on name
	 * 
	 * @param setToAdd
	 *            array of DataSetWithKeys
	 * @param alreadySelected
	 *            Vector of already selected datasets
	 */
	private void addDataSetToSelection(DataSetWithKey setToAdd, Vector selected) throws Exception {
		if ((setToAdd == null) || (selected == null))
			return;
		// if only one data set is being selected, replace the selection
		if (dataSetInfo.getMaxNumber() == 1) {
			selected.clear();
			selected.add(setToAdd);
			return;
		}
		boolean foundPlace = false;
		int j = 0;
		String selectedValStr = setToAdd.getName();
		while (!foundPlace && (j < selected.size())) {
			int result = selectedValStr.compareToIgnoreCase(((DataSetWithKey) selected.elementAt(j)).getName());
			if (result == 0) {
				foundPlace = true;
			}
			j++;
		}
		if (!foundPlace && okToAddDataSet(selected.size())) {
			selected.add(setToAdd);
		}
	}

	private boolean okToAddDataSet(int currentSize) throws Exception {
		if ((dataSetInfo.getMaxNumber() > 0) && (1 + currentSize > dataSetInfo.getMaxNumber())) {
			if (dataSetInfo.getMaxNumber() == 1) {
				throw new Exception("Only one data set of this type can be used on this plot");
			}
			throw new Exception("Only " + dataSetInfo.getMaxNumber()
					+ " data sets of this type can be used on this plot");
		}
		return true;
	}

	public Vector getSelection() {
		if (didOK) {
			/*
			 * DataSets ds = new DataSets(); //for (int i = selectedDataSetWithKeys.size()-1; i >= 0; i--) for (int i =
			 * 0; i < selectedDataSetWithKeys.size(); i++) { DataSetWithKey dswk =
			 * (DataSetWithKey)selectedDataSetWithKeys.get(i); System.out.println("key "+i+" = "+dswk.key);
			 * ds.add(dswk.dataSet, dswk.key); } return ds;
			 */
			return new Vector(selectedDataSetWithKeys);
		}
		// the selection didn't change so just return it
		return initialSelection;
	}

	public String[] getStringValues() {
		if (objectList == null)
			return stringArray;
		stringArray = new String[objectList.size()];
		objectList.toArray(stringArray);
		return stringArray;
	}// getStringValues()

	public void invoke(Object[] objects) {
		if (objectList != null) {
			setVisible(true);
			return;
		}
		objectList = new ArrayList();
		for (int i = 0; i < objects.length; i++)
			objectList.add(objects[i]);
		setVisible(true);
	}

	/**
	 * Remove the given data set from the list of selected data sets.
	 * 
	 * @author Daniel Gatti
	 * @param
	 */
	public void removeSelectedDataSet() {
		if (selectedList.isSelectionEmpty())
			return;
		Object[] selectedVals = selectedList.getSelectedValues();
		for (int i = 0; i < selectedVals.length; i++) {
			selectedDataSetWithKeys.remove(selectedVals[i]);
		}
		selectedList.setListData(selectedDataSetWithKeys);
		if (selectedList.getModel().getSize() > 0)
			selectedList.setSelectedIndex(0);
	}

	public static Vector pickDataSets(DataSetsAdapter dataSetsAdapter, Vector alreadySelected, DataSetInfo dsInfo) {
		DataSetSelector selector = new DataSetSelector(dataSetsAdapter, alreadySelected, dsInfo);
		return selector.getSelection();
	}
}
