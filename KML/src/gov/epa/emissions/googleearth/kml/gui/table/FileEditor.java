package gov.epa.emissions.googleearth.kml.gui.table;

import gov.epa.emissions.googleearth.kml.gui.InputFileBrowser;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableModel;

@SuppressWarnings("serial")
public class FileEditor extends AbstractCellEditor implements TableCellEditor {

	private File currentFile = new File(".");
	private JLabel label;
	private InputFileBrowser fileBrowser;
	private TableModel model;
	private int row = -1;
	private int col = -1;

	public FileEditor(TableModel model) {

		this.model = model;

		this.label = new JLabel();
		this.label.setOpaque(false);
		this.label.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
		this.label.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {

				if (e.getClickCount() == 2) {

					FileEditor.this.fileBrowser
							.setCurrentDirectory(FileEditor.this.currentFile);

					// if (FileEditor.this.model
					// .getValueAt(FileEditor.this.row, 0).equals(
					// PropertyKey.OUTPUT_DIRECTORY.getKey())) {
					// FileEditor.this.fileBrowser
					// .setFileSelectionMode(InputFileBrowser.DIRECTORIES_ONLY);
					// } else {
					FileEditor.this.fileBrowser
							.setFileSelectionMode(InputFileBrowser.FILES_ONLY);
					// }

					int returnVal = FileEditor.this.fileBrowser
							.showOpenDialog(null);
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						FileEditor.this.currentFile = FileEditor.this.fileBrowser
								.getSelectedFile();
						FileEditor.this.label
								.setText(FileEditor.this.currentFile
										.getAbsolutePath());
					}

					FileEditor.this.model.setValueAt(
							FileEditor.this.currentFile, FileEditor.this.row,
							FileEditor.this.col);
				}
			}
		});

		this.fileBrowser = new InputFileBrowser();
		this.fileBrowser.setCurrentDirectory(this.currentFile);
	}

	public Object getCellEditorValue() {
		return this.currentFile;
	}

	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {

		this.currentFile = (File) value;
		this.row = row;
		this.col = column;

		this.label.setText(this.currentFile.getAbsolutePath());

		return this.label;
	}
}
