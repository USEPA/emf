package gov.epa.mims.analysisengine.table.persist;

import gov.epa.mims.analysisengine.gui.GUIUserInteractor;
import gov.epa.mims.analysisengine.gui.ScreenUtils;
import gov.epa.mims.analysisengine.gui.TreeDialog;
import gov.epa.mims.analysisengine.gui.UserInteractor;
import gov.epa.mims.analysisengine.table.CurrentDirectory;
import gov.epa.mims.analysisengine.table.OverallTableModel;
import gov.epa.mims.analysisengine.table.SaveToDialog;
import gov.epa.mims.analysisengine.table.filter.FilterCriteria;
import gov.epa.mims.analysisengine.table.format.ColumnFormatInfo;
import gov.epa.mims.analysisengine.table.format.FormattedCellRenderer;
import gov.epa.mims.analysisengine.table.sort.SortCriteria;
import gov.epa.mims.analysisengine.tree.AnalysisOptions;
import gov.epa.mims.analysisengine.tree.Branch;
import gov.epa.mims.analysisengine.tree.DataSets;
import gov.epa.mims.analysisengine.tree.PageConstantsIfc;
import gov.epa.mims.analysisengine.tree.PageType;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EtchedBorder;
import javax.swing.table.TableColumn;

/**
 * Class LoadConfigurationGUI - Dialog to preview the configuration in a file before using it in current table's
 * configuration; It is not an editor for the file configuration. This dialog pops up when the user wants to load an
 * existing configuration file to apply on the table currently being examined. It has several features: View to view the
 * plot based on the current table data View All to view all the plots in the configuration based on the current table
 * data Import - import selected or all of the configurations into the current configuration of the table. Save - to
 * save the configured plots based on the current table data to a specific directory and in specific file format. If the
 * plot is already configured to be saved as a particular file and format it will be saved under that filename in the
 * chosen directory.
 * 
 * @author Krithiga Thangavelu, CEP, UNC CHAPEL HILL.
 * @version $Id: LoadConfigurationGUI.java,v 1.12 2007/06/11 03:31:19 eyth Exp $
 */
public class LoadConfigurationGUI extends javax.swing.JDialog {

	private JButton closeButton;

	private JButton savePlotsButton;

	private JButton importButton;

	private JButton viewButton;

	private JList configList;

	/** The Configuration object from the configuration file being loaded */
	private AnalysisConfiguration input;

	/** The configuration object of the current table */
	private AnalysisConfiguration dest;

	/** The Table containing the model in the sort filter table panel */
	private JTable table;

	private CurrentDirectory currentDirectory;

	public LoadConfigurationGUI(AnalysisConfiguration input, AnalysisConfiguration dest, JTable table, JFrame parent,
			CurrentDirectory currentDirectory) {
		super(parent);
		this.input = input;
		this.dest = dest;
		this.table = table;
		this.currentDirectory = currentDirectory;
		initGUI();
		setLocation(ScreenUtils.getPointToCenter(this));
	}

	public void initGUI() {
		try {
			JPanel overallPanel = new JPanel();
			JScrollPane scrollPane = new JScrollPane();
			String[] configNames = input.getConfigNames();
			configList = new JList(configNames);
			// select all the values
			int[] selectIndex = new int[configNames.length];
			for (int i = 0; i < selectIndex.length; i++) {
				selectIndex[i] = i;
			}
			configList.setSelectedIndices(selectIndex);

			JPanel buttonPanel = buttonPanel();

			this.setResizable(true);
			this.setTitle("Load Configuration Preview");
			this.setSize(new Dimension(375, 230));
			this.getContentPane().add(overallPanel);
			BoxLayout thisLayout = new BoxLayout(overallPanel, 0);

			overallPanel.setLayout(thisLayout);
			overallPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory
					.createEtchedBorder(EtchedBorder.RAISED), BorderFactory.createEmptyBorder(5, 5, 5, 5)));

			scrollPane.setPreferredSize(new Dimension(177, 200));
			JPanel configPanel = new JPanel();

			configPanel.setLayout(new BoxLayout(configPanel, 1));
			configPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createCompoundBorder(BorderFactory
					.createEtchedBorder(EtchedBorder.LOWERED), BorderFactory.createEmptyBorder(10, 10, 10, 10)),
					"Configurations"));

			configPanel.add(scrollPane);
			configList.setToolTipText(" Configurations in the file");
			scrollPane.add(configList);
			scrollPane.setViewportView(configList);
			overallPanel.add(configPanel);

			buttonPanel.setPreferredSize(new Dimension(120, 210));
			overallPanel.add(buttonPanel);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private JPanel buttonPanel() {
		viewButton = new JButton();
		importButton = new JButton();
		savePlotsButton = new JButton();
		closeButton = new JButton();
		viewButton.setText("    View    ");
		viewButton.setToolTipText("View all or the selected plots");
		viewButton.setPreferredSize(new Dimension(100, 30));
		viewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				viewConfiguation();
			}
		});

		importButton.setText("  Import   ");
		importButton.setToolTipText("Import all or the selected configurations into the table");
		importButton.setPreferredSize(new Dimension(100, 30));
		importButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				doImport();
			}
		});

		savePlotsButton.setText("Save Plots");
		savePlotsButton.setToolTipText("Save all or the selected plots to a directory");
		savePlotsButton.setPreferredSize(new Dimension(100, 30));
		savePlotsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				doSavePlots();
			}
		});

		closeButton.setText("   Close    ");
		closeButton.setToolTipText("Close this window");
		closeButton.setPreferredSize(new Dimension(100, 30));
		closeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				dispose();
				return;
			}
		});

		JPanel buttonPanel = new JPanel();
		FlowLayout flowLayout = new FlowLayout(FlowLayout.CENTER, 3, 6);
		flowLayout.layoutContainer(buttonPanel);
		buttonPanel.setLayout(flowLayout);
		buttonPanel.add(viewButton);
		buttonPanel.add(importButton);
		buttonPanel.add(savePlotsButton);
		buttonPanel.add(closeButton);
		return buttonPanel;
	}

	protected void viewConfiguation() {
		Object[] values = configList.getSelectedValues();
		if (values.length == 0) {
			values = input.getConfigNames();
			// rearrange the array values
			values = reorder((String[]) values);
		}
		StringBuffer error = new StringBuffer();
		for (int i = 0; i < values.length; i++) {
			try {
				viewConfig((String) values[i]);
			} catch (Exception e) {
				error.append(values[i] + " not applicable to current table\n Error:" + e.getMessage() + "\n");
				e.printStackTrace();
			}
		}
		if (error.length() > 1) {
			new GUIUserInteractor().notify(this, "Configuration Error", error.toString(), UserInteractor.ERROR);
		}
	}

	private String[] reorder(String[] values) {
		Vector list = new Vector();
		for (int i = 0; i < values.length; i++) {
			list.add(values[i]);
		}

		boolean format = list.contains(AnalysisConfiguration.TABLE_FORMAT);
		boolean filter = list.contains(AnalysisConfiguration.TABLE_FILTER_CRITERIA);
		boolean sort = list.contains(AnalysisConfiguration.TABLE_SORT_CRITERIA);
		int count = 0;
		if (format) {
			list.remove(AnalysisConfiguration.TABLE_FORMAT);
			list.add(count++, AnalysisConfiguration.TABLE_FORMAT);
		}
		if (filter) {
			list.remove(AnalysisConfiguration.TABLE_FILTER_CRITERIA);
			list.add(count++, AnalysisConfiguration.TABLE_FILTER_CRITERIA);
		}
		if (sort) {
			list.remove(AnalysisConfiguration.TABLE_SORT_CRITERIA);
			list.add(count++, AnalysisConfiguration.TABLE_SORT_CRITERIA);
		}
		return (String[]) list.toArray(values);
	}// reorder()

	protected void viewConfig(String configName) throws Exception {
		String pageType = null;
		Data dat = input.getConfig(configName);
		if (dat != null && dat.configType == Data.PLOT_TYPE) {
		    dat.info.setPlotName(configName);
			viewPlot(pageType, dat);
		}// if (dat != null && dat.configType == Data.PLOT_TYPE)
		else {
			if (dat != null && dat.configType == Data.TABLE_TYPE) {
				if (dat.criteria == null) {
					new GUIUserInteractor().notify(this, "Table Configuration", configName + " is empty",
							UserInteractor.NOTE);
					return;
				}
				previewTable(dat);
			}
		}// else
	}

	private void viewPlot(String pageType, Data dat) throws Exception {
		AnalysisOptions options;
		DataSets dset = input.getDataSets(dat.info, this);
		Branch tempTree = dat.tree; // .clone());
		
		if (dset != null) {
			options = (AnalysisOptions) tempTree.getChild(0);
			pageType = setToDefaultScreenPageType(options);
			dset.add(tempTree.getChild(0)); //this is to set the page options
		} else {
			throw new Exception("Empty Data Set");
		}
		
		removeColumnsNotAvailableInNewDatasets(dset, tempTree);
		TreeDialog.createPlotWithoutGUI(tempTree, dset, null, null);
		
		if (pageType != null) {
			PageType pt = (PageType) options.getOption("PAGE_TYPE");
			pt.setTextString(pageType);
			options.addOption("PAGE_TYPE", pt);
		}
	}

	private void removeColumnsNotAvailableInNewDatasets(DataSets newDataset, Branch tree) throws Exception {
		UpdateDataColumns update = new UpdateDataColumns(newDataset, tree);
		update.update();
	}

	protected void previewTable(Data dat) {
		OverallTableModel model = input.getModel();
		try {
			Hashtable formats = null;
			SortCriteria sCriteria = null;
			FilterCriteria fCriteria = null;
			formats = (Hashtable) (model.getColumnFormatInfo()).clone();
			sCriteria = model.getSortCriteria();
			fCriteria = model.getFilterCriteria();
			if (dat.criteria instanceof Hashtable) {
				formatTable((Hashtable) dat.criteria);
			} else {
				importIntoTable(dat.criteria);
			}
			this.setVisible(false);
			int result = new GUIUserInteractor().selectOption(this, "Table " + " Configuration Preview ",
					"View the table now. Do you want to keep " + "this configuration?", UserInteractor.YES_NO,
					UserInteractor.NO);

			if (result == UserInteractor.NO) {
				if (dat.criteria instanceof Hashtable)
					formatTable(formats);
				else {
					model.reset();
					importIntoTable(formats);
					importIntoTable(fCriteria);
					importIntoTable(sCriteria);
				}
			}
			this.setVisible(true);
		} catch (Exception e) {
			new GUIUserInteractor().notify(this, "Configuration Mismatch", "The "
					+ "imported configuration is not applicable to the current table. " + e.getMessage(),
					UserInteractor.ERROR);
		}
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

	protected void doImport() {
		Object[] values = configList.getSelectedValues();
		if (values.length == 0) {
			values = input.getConfigNames();
			values = reorder((String[]) values);
		}
		List errorMsgs = new ArrayList();
		if (values.length > 0) {
			for (int i = 0; i < values.length; i++) {
				try {
					Data dat = input.getConfig((String) values[i]);
					dat.info.setPlotName((String)values[i]);
					DataSets dset = input.getDataSets(dat.info, this);
					removeColumnsNotAvailableInNewDatasets(dset, dat.tree);
					
					if (dat.configType == Data.TABLE_TYPE) {
						importIntoTable(dat.criteria);
					} else {
						dest.storePlotConfig((String) values[i], dat, false);
					}
				} catch (Exception e) {
					errorMsgs.add("Configuration " + values[i] + " not applicable to the current" + " table. "
							+ e.getMessage());
				}
			}
		}
		showErrorMessages(errorMsgs);
	}

	private void showErrorMessages(List errorMsgs) {
		if (errorMsgs.isEmpty()) {
			dispose();
			new GUIUserInteractor().notify(this, "Import Completion", "Import process completed.", UserInteractor.NOTE);
			return;
		}

		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < errorMsgs.size(); i++) {
			sb.append(errorMsgs.get(i) + "\n");
		}
		new GUIUserInteractor().notify(this, "Configuration Mismatch", sb.toString(), UserInteractor.ERROR);
	}

	protected void importIntoTable(Object criteria) throws Exception {

		OverallTableModel model = input.getModel();
		if (criteria == null)
			return;
		if (criteria instanceof Hashtable) {
			formatTable((Hashtable) criteria);

		} else {
			if (criteria instanceof SortCriteria) {
				model.sort((SortCriteria) criteria);
			} else {
				if (criteria instanceof FilterCriteria) {
					((FilterCriteria) criteria).setTableModel(model);
					model.filterRows((FilterCriteria) criteria);
					model.filterColumns((FilterCriteria) criteria);
					formatTable(model.getColumnFormatInfo());
				} else {
					throw new Exception("Not an instance of Table Criteria ");
				}
			}
		}
	}

	protected void formatTable(Hashtable formats) throws Exception {
		Set keys = formats.keySet();
		Iterator iter = keys.iterator();
		while (iter.hasNext()) {
			String key = (String) iter.next();
			int colIndex = input.getModel().getColumnNameIndex(key);
			// System.out.println("key=" + key + " colIndex = "+colIndex);
			ColumnFormatInfo formatInfo = (ColumnFormatInfo) formats.get(key);
			if (colIndex != -1 && formatInfo != null) {
				TableColumn column = table.getColumnModel().getColumn(colIndex + 1);
				column.setPreferredWidth(formatInfo.width);
				FormattedCellRenderer formattedRenderer = new FormattedCellRenderer(formatInfo.getFormat(),
						formatInfo.alignment);
				formattedRenderer.setFont(formatInfo.font);
				formattedRenderer.setForeground(formatInfo.foreground);
				formattedRenderer.setBackground(formatInfo.background);
				// Now set the new renderer to be used in the column.
				column.setCellRenderer(formattedRenderer);
				dest.getModel().setColumnFormatInfo(key, new ColumnFormatInfo(column));
			}
		}
		table.repaint();
	}

	protected void doSavePlots() {
		Object[] values = configList.getSelectedValues();
		if (values.length == 0) {
			values = input.getConfigNames();
			values = reorder((String[]) values);
		}
		SaveToDialog dialog = new SaveToDialog(this, currentDirectory);
		dialog.setVisible(true);
		int returnVal = dialog.getRetVal();
		try {
			if (returnVal == SaveToDialog.CANCEL) {
				dialog.dispose();
				return;
			}
			if (returnVal == SaveToDialog.APPROVE) {
				String[] stringValues = new String[values.length];
				for (int i = 0; i < values.length; i++) {
					stringValues[i] = (String) values[i];
					Data dat = input.getConfig(stringValues[i]);
					if (dat.configType == Data.TABLE_TYPE) {
						importIntoTable(dat.criteria);
					}
				}
				input.showOrSaveConfiguredPlots(dialog.getAbsolutePath(), dialog.getFileType(), stringValues, this);
				new GUIUserInteractor().notify(this, "Saving Plots", "Selected Plots " + " were saved successfully!!",
						UserInteractor.NOTE);
			}
		}

		catch (Exception ex) {
			ex.printStackTrace();
			new GUIUserInteractor().notify(this, "Error saving plots", ex.getMessage(), UserInteractor.ERROR);
		}
		dialog.dispose();
		return;
	}

}
