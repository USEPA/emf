package gov.epa.emissions.googleearth.kml.gui.table;

import gov.epa.emissions.googleearth.kml.PropertiesManager;

import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

@SuppressWarnings("serial")
public class PropertiesTable extends JTable {

	public PropertiesTable() {
		this.initTable();
	}

	public PropertiesTable(PropertiesManager propertiesManager) {

		this.initTable();

		PropertiesTableModel model = new PropertiesTableModel(propertiesManager);
		this.setModel(model);

		this.initColumns(model);
	}

	private void initTable() {

		this.setAutoCreateRowSorter(true);
		this.setRowHeight(20);
		this.setCellSelectionEnabled(false);
		this.setColumnSelectionAllowed(false);

		this.setFillsViewportHeight(true);
	}

	private void initColumns(TableModel model) {

		TableColumn column = this.getColumnModel().getColumn(0);
		column.setCellRenderer(new KeyTableCellRenderer());

		column = this.getColumnModel().getColumn(1);
		column.setCellEditor(new PropertiesTableCellEditor(model));
		column.setCellRenderer(new PropertiesTableCellRenderer());
	}

	public void updateModel(TableModel dataModel) {

		this.setModel(dataModel);
		this.initColumns(dataModel);
	}

	public PropertiesTableModel getPropertiesTableModel() {
		return (PropertiesTableModel) this.getModel();
	}

	public PropertiesManager getPropertiesManager() {
		return this.getPropertiesTableModel().getPropertiesManager();
	}

	public static void main(String[] args) {
		//
		// JFrame frame = new JFrame();
		// frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		//
		// List<Object> keys = Arrays.asList(new Object[] { "InputFile",
		// "StringKey" });
		// List<Object> values = Arrays.asList(new Object[] { new File(""),
		// "StringValue" });
		//
		// PropertiesTable propertiesTable = new PropertiesTable(keys, values);
		// propertiesTable.setPreferredSize(new Dimension(500, 300));
		//
		// // Create the scroll pane and add the table to it.
		// JScrollPane scrollPane = new JScrollPane(propertiesTable);
		//
		// frame.setContentPane(scrollPane);
		//
		// frame.pack();
		// frame.setVisible(true);
	}
}
