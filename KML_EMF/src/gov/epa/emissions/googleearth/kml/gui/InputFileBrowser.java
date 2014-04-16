package gov.epa.emissions.googleearth.kml.gui;

import java.io.File;

import javax.swing.JFileChooser;

@SuppressWarnings("serial")
public class InputFileBrowser extends JFileChooser {

	private static String defaultFolder = ".";
	
	public InputFileBrowser() {
		super(".");
	}
	
	public void setCurrentDirectory(File dir) {
		defaultFolder = dir.getAbsolutePath();
		super.setCurrentDirectory(dir);
	}
}
