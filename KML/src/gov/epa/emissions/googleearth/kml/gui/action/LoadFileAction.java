package gov.epa.emissions.googleearth.kml.gui.action;

import gov.epa.emissions.googleearth.kml.gui.InputFileBrowser;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

@SuppressWarnings("serial")
public class LoadFileAction extends AbstractAction {

	private InputFileBrowser inputFileBrowser;
	private JTextField textField;
	private FileFilter fileFilter;
	private String toolTipText;
	
	public LoadFileAction(JTextField textField, FileFilter fileFilter) {

		super("Load");
		this.textField = textField;
		this.fileFilter = fileFilter;
	}
	
	public LoadFileAction(String label, JTextField textField, FileFilter fileFilter) {

		super(label);
		this.textField = textField;
		this.fileFilter = fileFilter;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {

		if (this.inputFileBrowser == null) {

			this.inputFileBrowser = new InputFileBrowser();
			inputFileBrowser.setFileFilter(this.fileFilter);
		}

		int returnVal = inputFileBrowser.showOpenDialog(this.textField);
		if (returnVal == JFileChooser.APPROVE_OPTION) {

			String inputFile = inputFileBrowser.getSelectedFile()
					.getAbsolutePath();
			this.textField.setText(inputFile);
			inputFileBrowser.setCurrentDirectory(inputFileBrowser.getSelectedFile().getParentFile());
		}
	}

	public void setInputFileBrowser(InputFileBrowser inputFileBrowser) {
		this.inputFileBrowser = inputFileBrowser;
	}

	public void setTextField(JTextField textField) {
		this.textField = textField;
	}

	public InputFileBrowser getInputFileBrowser() {
		return inputFileBrowser;
	}

	public JTextField getTextField() {
		return textField;
	}

	public FileFilter getFileFilter() {
		return fileFilter;
	}
	
	
	public void setToolTipText(String toolTipText) {
		this.toolTipText = toolTipText;
	}

	public String getToolTipText() {
		return this.toolTipText;
	}

}
