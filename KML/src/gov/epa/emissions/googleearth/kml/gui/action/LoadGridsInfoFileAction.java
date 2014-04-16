package gov.epa.emissions.googleearth.kml.gui.action;

import gov.epa.emissions.googleearth.kml.gui.InputFileBrowser;
import gov.epa.emissions.googleearth.kml.gui.KMLGeneratorView;

import java.awt.event.ActionEvent;

import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

@SuppressWarnings("serial")
public class LoadGridsInfoFileAction extends LoadFileAction {

	public LoadGridsInfoFileAction(JTextField textField,
			KMLGeneratorView frame) {

		super(textField, new FileNameExtensionFilter("Grids Info Files",
				"dat", "txt", "csv"));
		this.setToolTipText("Load grids info file.");
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
		}
	}
}
