package gov.epa.emissions.googleearth.kml.gui.action;

import gov.epa.emissions.googleearth.kml.ConfigurationManager;
import gov.epa.emissions.googleearth.kml.PropertiesManager;
import gov.epa.emissions.googleearth.kml.utils.Utils;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

@SuppressWarnings("serial")
public class SavePropertiesFileAction extends AbstractAction {

	private PropertiesManager propertiesManager;
	private JFileChooser fileChooser;
	private Component parentComponent;

	public SavePropertiesFileAction(Component parentComponent,
			PropertiesManager propertiesManager) {

		super("Save");
		this.propertiesManager = propertiesManager;
		this.fileChooser = new JFileChooser(".");
		this.fileChooser.setFileFilter(new FileNameExtensionFilter(
				"Properties Files", "properties", "props"));
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {

		int returnVal = this.fileChooser.showSaveDialog(this.parentComponent);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			try {

				File selectedFile = this.fileChooser.getSelectedFile();

				boolean shouldOverwrite = !selectedFile.exists();

				while (selectedFile.exists() && !shouldOverwrite
						&& returnVal == JFileChooser.APPROVE_OPTION) {

					if (!shouldOverwrite) {

						int result = JOptionPane.showConfirmDialog(
								this.parentComponent,
								"Properties file exists. Overwrite?",
								"Save File", JOptionPane.YES_NO_OPTION,
								JOptionPane.QUESTION_MESSAGE);
						shouldOverwrite = result == JOptionPane.YES_OPTION;
					}

					if (!shouldOverwrite) {

						returnVal = this.fileChooser
								.showSaveDialog(this.parentComponent);

						if (returnVal == JFileChooser.APPROVE_OPTION) {

							selectedFile = this.fileChooser.getSelectedFile();
							shouldOverwrite = !selectedFile.exists();
						}
					}
				}

				if (shouldOverwrite) {
					this.writeFile(selectedFile);
				}

			} catch (IOException e) {

				String message = e.getLocalizedMessage();
				if (ConfigurationManager.getInstance().getValueAsBoolean(
						ConfigurationManager.PropertyKey.SHOW_OUTPUT.getKey())) {
					System.err.println(message);
				}

				JOptionPane.showMessageDialog(parentComponent, Utils.wrapLine(
						message, 80), "Properties File Save Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}

	}

	private void writeFile(File outputFile) throws IOException {
		this.propertiesManager.storeProperties(outputFile);
	}

	public String getToolTipText() {
		return "Save current properties as a properties file.";
	}
}
