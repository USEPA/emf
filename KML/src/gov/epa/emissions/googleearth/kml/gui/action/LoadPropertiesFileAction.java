package gov.epa.emissions.googleearth.kml.gui.action;

import gov.epa.emissions.googleearth.kml.ConfigurationManager;
import gov.epa.emissions.googleearth.kml.KMZGeneratorException;
import gov.epa.emissions.googleearth.kml.PropertiesManager;
import gov.epa.emissions.googleearth.kml.gui.InputFileBrowser;
import gov.epa.emissions.googleearth.kml.gui.KMLGeneratorView;
import gov.epa.emissions.googleearth.kml.gui.table.PropertiesTable;
import gov.epa.emissions.googleearth.kml.gui.table.PropertiesTableModel;
import gov.epa.emissions.googleearth.kml.utils.Utils;

import java.awt.event.ActionEvent;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

@SuppressWarnings("serial")
public class LoadPropertiesFileAction extends LoadFileAction {

	private PropertiesTable propertiesTable;
	private KMLGeneratorView frame;

	public LoadPropertiesFileAction(JTextField textField,
			PropertiesTable propertiesTable) {

		super(textField, new FileNameExtensionFilter("Properties Files",
				"properties", "props", "dat", "csv", "txt"));
		this.propertiesTable = propertiesTable;
		this.setToolTipText("Load properties file to populate table.");
	}

	public LoadPropertiesFileAction(JTextField textField,
			KMLGeneratorView frame) {

		super(textField, new FileNameExtensionFilter("Properties Files",
				"properties", "props"));

		this.frame = frame;
		this.setToolTipText("Load properties file to populate table.");
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {

		InputFileBrowser localBrowser = this.getInputFileBrowser();
		if (localBrowser == null) {

			localBrowser = new InputFileBrowser();
			this.setInputFileBrowser(localBrowser);
			localBrowser.setFileFilter(this.getFileFilter());
		}

		int returnVal = localBrowser.showOpenDialog(this.getTextField());
		if (returnVal == JFileChooser.APPROVE_OPTION) {

			String inputFile = localBrowser.getSelectedFile().getAbsolutePath();
			this.getTextField().setText(inputFile);

			try {
				PropertiesManager propertiesManager = PropertiesManager
						.getInstance();
				propertiesManager.initProperties(inputFile);

				if (this.propertiesTable != null) {
					this.propertiesTable.updateModel(new PropertiesTableModel(
							propertiesManager));
				} else if (this.frame != null) {
					this.frame.updatePropertiesFields(propertiesManager);
				}

			} catch (KMZGeneratorException e) {

				String message = e.getLocalizedMessage();
				if (ConfigurationManager.getInstance().getValueAsBoolean(
						ConfigurationManager.PropertyKey.SHOW_OUTPUT.getKey())) {
					System.err.println(message);
				}

				JOptionPane
						.showMessageDialog(this.getTextField()
								.getTopLevelAncestor(), Utils.wrapLine(message,
								80), "Properties File Load Error",
								JOptionPane.ERROR_MESSAGE);
			}
		}
	}
}
