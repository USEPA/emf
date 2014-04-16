package gov.epa.mims.analysisengine.table.stats;

import gov.epa.mims.analysisengine.gui.GUIUserInteractor;
import gov.epa.mims.analysisengine.gui.UserInteractor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import weka.core.Instances;
import weka.gui.explorer.Explorer;

public class WekaExplorer extends Explorer {

	public WekaExplorer(Component parent, Instances data) {
		m_PreprocessPanel.setInstances(data);
	}

	public void showGUI() {
		try {
			final JFrame jf = new JFrame("Weka Explorer");
			jf.getContentPane().setLayout(new BorderLayout());
			jf.getContentPane().add(this, BorderLayout.CENTER);
			jf.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					jf.dispose();
				}
			});
			jf.pack();
			jf.setSize(800, 600);
			jf.setVisible(true);
		} catch (Exception ie) {
			new GUIUserInteractor().notify(this, "Error", "Error occured while running " + "Weka Explorer. "
					+ ie.getMessage(), UserInteractor.ERROR);
			return;
		}
	}

}
