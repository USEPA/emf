package gov.epa.emissions.googleearth.kml.gui.action;

import gov.epa.emissions.googleearth.kml.ConfigurationManager;
import gov.epa.emissions.googleearth.kml.KMZGeneratorException;
import gov.epa.emissions.googleearth.kml.gui.InputFileBrowser;
import gov.epa.emissions.googleearth.kml.gui.KMLGeneratorView;
import gov.epa.emissions.googleearth.kml.utils.Utils;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

@SuppressWarnings("serial")
public class LoadDataFileAction extends LoadFileAction {

	private KMLGeneratorView frame;

	public LoadDataFileAction(JTextField textField,
			KMLGeneratorView frame) {

		super(textField, new FileNameExtensionFilter("Data Files", "csv", "ncf"));
		this.frame = frame;
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

			String dataFile = localBrowser.getSelectedFile().getAbsolutePath();
			this.getTextField().setText(dataFile);

			try {
				this.frame.handleDataFile(new File(dataFile));
			} catch (KMZGeneratorException e) {

				String message = e.getLocalizedMessage();
				if (ConfigurationManager.getInstance().getValueAsBoolean(
						ConfigurationManager.PropertyKey.SHOW_OUTPUT.getKey())) {
					System.err.println(message);
				}

				JOptionPane
						.showMessageDialog(this.getTextField()
								.getTopLevelAncestor(), Utils.wrapLine(message,
								80), "Data File Load Error",
								JOptionPane.ERROR_MESSAGE);
			}
		}
	}
}
