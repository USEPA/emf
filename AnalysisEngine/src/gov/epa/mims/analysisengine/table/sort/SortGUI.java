package gov.epa.mims.analysisengine.table.sort;

import gov.epa.mims.analysisengine.gui.OptionDialog;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.border.TitledBorder;

/**
 * A GUI that allows the user to specify sorting information for a table.
 * 
 * @author Daniel Gatti
 * @version $Id: SortGUI.java,v 1.1 2006/11/01 15:33:40 parthee Exp $
 */
public class SortGUI extends OptionDialog {
	/** The SotCritria that this GUI represents. */
	protected SortCriteria sortCriteria = null;

	/** The list of SortPanels that we have added to the GUI. */
	protected Vector sortPanels = new Vector();

	/** The column names that are available to sort with. */
	protected String[] columnNames = null;

	/** Scrollpane on which the sort panels ride. */
	protected JScrollPane scrollPane = null;

	/** The panel that sort panels are added to in the scroll pane. */
	protected JPanel sortContentPanel = new JPanel();

	/** Button to add a new sort column. */
	protected JButton addBtn = new JButton("Add");

	/** Button to clear all of the sort criteria. */
	protected JButton clearBtn = new JButton("Clear");

	public static final int SORT_COLUMN = 0;

	public static final int ORDER_COLUMN = 1;

	public static final int ASCENDING_COLUMN = 2;

	public static final int NAME_COLUMN = 3;

	public static final int NUM_PANELS_VISIBLE = 3;

	/**
	 * Consructor.
	 */
	public SortGUI(JFrame parent, String[] columnNames, SortCriteria sortCriteria) {
		super(parent);

		this.columnNames = columnNames;
		this.sortCriteria = sortCriteria;

		JToolBar toolBar = new JToolBar();
		toolBar.add(addBtn);
		toolBar.add(clearBtn);

		addBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addNewSortPanel(sortPanels.isEmpty());
			}
		});
		addBtn.setMnemonic('A');

		clearBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				clearSortPanel();
			}
		});
		clearBtn.setMnemonic('C');

		sortContentPanel.setLayout(new BoxLayout(sortContentPanel, BoxLayout.Y_AXIS));

		scrollPane = new JScrollPane(sortContentPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		initGUIFromModel();

		Container contentPane = getContentPane();
		contentPane.add(toolBar, BorderLayout.NORTH);
		contentPane.add(scrollPane, BorderLayout.CENTER);
		contentPane.add(getButtonPanel(), BorderLayout.SOUTH);
		pack();
		setModal(true);
		setTitle("Sort Columns");
	} // SortGUI()

	/**
	 * Add a new empty SortPanel.
	 * 
	 * @param isFirst
	 *            boolean that is true if this is the first panel in the list. This is used to set the "Sort by", "Then
	 *            sort by" labels.
	 */
	protected void addNewSortPanel(boolean isFirst) {
		SortPanel sortPanel = new SortPanel(columnNames, isFirst);
		sortPanel.setAscending(true);
		sortContentPanel.add(sortPanel);
		sortPanels.add(sortPanel);

		// Set the size to be fit at most 3 panels in the scrollpane.
		int numPanelsVisible = NUM_PANELS_VISIBLE;
		if (sortPanels.size() < 3) {
			numPanelsVisible = sortPanels.size();
		}

		// Get the dimension of one sort panel.
		Dimension onePanelDimension = sortPanel.getPreferredSize();
		// Set the preferred size of the viewport to the panel hieght * the
		// number of panels or 3, whichever is smaller.
		scrollPane.getViewport().setPreferredSize(
				new Dimension(onePanelDimension.width, onePanelDimension.height * numPanelsVisible));
		pack();

		// Move the scroll bar to the bottom to show the new panel.
		JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
		verticalScrollBar.setValue(verticalScrollBar.getMaximum());
	} // addNewSortPanel()

	/**
	 * Clear all of the sort criteria.
	 */
	protected void clearSortPanel() {
		sortContentPanel.removeAll();
		sortPanels.clear();
		addNewSortPanel(true);
	} // clearSortPanel()

	/**
	 * Initialize the GUI based on the SortCriteria passed in.
	 */
	public void initGUIFromModel() {
		if (sortCriteria == null) {
			addNewSortPanel(true);
		} else {
			String[] cols = sortCriteria.getColumnNames();
			boolean[] asc = sortCriteria.getAscending();
			boolean[] cas = sortCriteria.getCaseSensitive();
			for (int i = 0; i < cols.length; i++) {
				SortPanel sortPanel = new SortPanel(columnNames, (i == 0));
				sortPanel.setColumnName(cols[i]);
				sortPanel.setAscending(asc[i]);
				sortPanel.setCaseSensitive(cas[i]);
				sortContentPanel.add(sortPanel);
				sortPanels.add(sortPanel);
				pack();
			}
		}
	} // initGUIFromModel()

	/**
	 * Return the SortCriteria that was collected from the GUI. Could be null.
	 */
	public SortCriteria getSortCriteria() {
		return sortCriteria;
	} // getSortCriteria()

	protected void saveGUIValuesToModel() {
		sortCriteria = null;
		String[] cols = new String[sortPanels.size()];
		boolean[] asc = new boolean[sortPanels.size()];
		boolean[] cas = new boolean[sortPanels.size()];

		for (int i = 0; i < sortPanels.size(); i++) {
			SortPanel sp = (SortPanel) sortPanels.get(i);
			cols[i] = sp.getColumnName();
			asc[i] = sp.getAscending();
			cas[i] = sp.getCaseSensitive();
		}

		sortCriteria = new SortCriteria(cols, asc, cas);
	} // saveGUIDataToModel()

	/**
	 * A private class that contains a panel with sorting information for one column.
	 */
	class SortPanel extends JPanel {
		private JComboBox columnBox = new JComboBox();

		private JCheckBox ascendingBtn = new JCheckBox("Ascending?");

		private JCheckBox caseSensitiveBtn = new JCheckBox("Case Sensitive?");

		public SortPanel(String[] columnNames, boolean isFirst) {
			DefaultComboBoxModel model = new DefaultComboBoxModel(columnNames);
			columnBox.setModel(model);
			columnBox.setEditable(false);
			JPanel comboPanel = new JPanel();
			comboPanel.add(columnBox);

			JPanel orderPanel = new JPanel();
			orderPanel.setLayout(new BoxLayout(orderPanel, BoxLayout.Y_AXIS));
			orderPanel.add(ascendingBtn);
			orderPanel.add(caseSensitiveBtn);

			String borderTitle = "Then Sort By";
			if (isFirst) {
				borderTitle = "Sort By";
			}

			setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), borderTitle,
					TitledBorder.LEFT, TitledBorder.TOP));

			setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
			add(comboPanel);
			add(orderPanel);
		}

		public boolean getAscending() {
			return ascendingBtn.isSelected();
		}

		public boolean getCaseSensitive() {
			return caseSensitiveBtn.isSelected();
		}

		public int getColumnIndex() {
			return columnBox.getSelectedIndex();
		}

		public String getColumnName() {
			return (String) columnBox.getSelectedItem();
		}

		public void setColumnName(String columnName) {
			columnBox.setSelectedItem(columnName);
		}

		public void setAscending(boolean ascending) {
			ascendingBtn.setSelected(ascending);
		}

		public void setCaseSensitive(boolean caseSensitive) {
			caseSensitiveBtn.setSelected(caseSensitive);
		}
	} // class SortPanel

} // class SortGUI

