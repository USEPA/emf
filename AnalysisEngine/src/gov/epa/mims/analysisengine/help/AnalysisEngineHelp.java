package gov.epa.mims.analysisengine.help;

import gov.epa.mims.analysisengine.gui.DefaultUserInteractor;
import gov.epa.mims.analysisengine.gui.ScreenUtils;
import gov.epa.mims.analysisengine.gui.UserInteractor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javax.help.HelpSet;
import javax.help.HelpSetException;
import javax.help.JHelp;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class AnalysisEngineHelp extends JFrame {

	public static final String ANALYSIS_ENGINE_TARGET = "sensitivity_intro";

	private JHelp helpViewer = null;

	private static AnalysisEngineHelp emisviewHelp = null;
	
	
	private AnalysisEngineHelp() {
		setTitle("Analysis Engine Users Guide");
		helpViewer = createHelpViewer();
		JPanel mainPanel = new JPanel(new BorderLayout());
		Dimension size = calculatePrefDimension();
		mainPanel.setPreferredSize(size);
		mainPanel.add(helpViewer);
		getContentPane().add(mainPanel);
		pack();
		setLocation(ScreenUtils.getPointToCenter(this));
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
	}
	private Dimension calculatePrefDimension() {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int width = 900;
		int height= 800;
		if(width > screenSize.width-50){
			width = screenSize.width-50;
		}
		if(height > screenSize.height-100){
			height = screenSize.height-100;
		}
		return new Dimension(width,height);
	}

	private JHelp createHelpViewer() {
		String value = System.getProperty("HOME_DIR");
		if(value==null){
			throw new RuntimeException("Please specify the HOME_DIR system variable");
		}
		char sep = File.separatorChar;
		
		value += (sep+"docs"+sep+"help"+sep+ "analysisengine"+sep+ "ae_help.hs");
		if(!new File(value).isFile() ||  !value.endsWith(".hs")){
			throw new RuntimeException("The '"+value +" is not a correct analysis engine helpset file"+
					"\nPlease check whether file exist");
		}
		ClassLoader cl = AnalysisEngineHelp.class.getClassLoader();
		URL url = null;
		try {
			url = new URL("file:///"+value);
		} catch (MalformedURLException e1) {
			DefaultUserInteractor.get().notify(this, "Error",
					"The help set file does not exist in the location '"+value+"'", UserInteractor.ERROR);
			e1.printStackTrace();
		}
		try {
			return new JHelp(new HelpSet(cl, url));
		} catch (HelpSetException e) {
			DefaultUserInteractor.get().notify(this, "Error",
					"Could not find the help set file", UserInteractor.ERROR);
			e.printStackTrace();
			return null;
		}
	}
	

	private void setCurrentTarget(String target) {
		helpViewer.setCurrentID(target);
	}

	public static void showHelp(String currentTarget) {
		if (emisviewHelp == null) {
			emisviewHelp = new AnalysisEngineHelp();
		}
		emisviewHelp.setCurrentTarget(currentTarget);
		emisviewHelp.setVisible(true);
	}

	public static void main(String[] arg) {
		AnalysisEngineHelp.showHelp(AnalysisEngineHelp.ANALYSIS_ENGINE_TARGET);
	}
}
