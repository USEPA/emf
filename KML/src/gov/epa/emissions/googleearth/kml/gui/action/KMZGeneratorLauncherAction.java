package gov.epa.emissions.googleearth.kml.gui.action;

import gov.epa.emissions.googleearth.kml.ConfigurationManager;
import gov.epa.emissions.googleearth.kml.gui.DataFileProducer;
import gov.epa.emissions.googleearth.kml.gui.PointSourceGeneratorFrame;
import gov.epa.emissions.googleearth.kml.utils.Utils;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

@SuppressWarnings("serial")
public class KMZGeneratorLauncherAction extends AbstractAction {

	private DataFileProducer dataFileProducer;
	private String frameTitle;
	private Component parentComponent;

	public KMZGeneratorLauncherAction(DataFileProducer dataFileProducer,
			String label) {
		this(null, dataFileProducer, label, null);
	}

	public KMZGeneratorLauncherAction(String label) {
		this(null, null, label, null);
	}

	public KMZGeneratorLauncherAction(String label, String frameTitle) {
		this(null, null, label, frameTitle);
	}

	public KMZGeneratorLauncherAction(DataFileProducer dataFileProducer,
			String label, String frameTitle) {
		this(null, dataFileProducer, label, frameTitle);
	}

	public KMZGeneratorLauncherAction(Component parentComponent,
			DataFileProducer dataFileProducer, String label) {
		this(parentComponent, dataFileProducer, label, null);
	}

	public KMZGeneratorLauncherAction(Component parentComponent, String label) {
		this(parentComponent, null, label, null);
	}

	public KMZGeneratorLauncherAction(Component parentComponent, String label,
			String frameTitle) {
		this(parentComponent, null, label, frameTitle);
	}

	public KMZGeneratorLauncherAction(Component parentComponent,
			DataFileProducer dataFileProducer, String label, String frameTitle) {

		super(label);

		this.parentComponent = parentComponent;
		this.dataFileProducer = dataFileProducer;
		this.frameTitle = frameTitle;
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		try {

			PointSourceGeneratorFrame frame = null;
			if (this.dataFileProducer == null) {

				if (this.frameTitle != null) {
					frame = new PointSourceGeneratorFrame(this.frameTitle);
				} else {
					frame = new PointSourceGeneratorFrame();
				}
			} else {
				if (this.frameTitle != null) {
					frame = new PointSourceGeneratorFrame(this.dataFileProducer
							.getDataFile(), this.frameTitle);
				} else {
					frame = new PointSourceGeneratorFrame(this.dataFileProducer
							.getDataFile());
				}
			}

			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.pack();
			frame.setVisible(true);

		} catch (Exception ex) {

			String message = ex.getLocalizedMessage();
			if (ConfigurationManager.getInstance().getValueAsBoolean(
					ConfigurationManager.PropertyKey.SHOW_OUTPUT.getKey())) {
				System.err.println(message);
			}

			JOptionPane.showMessageDialog(this.parentComponent, Utils.wrapLine(
					message, 80), "Application Launch Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	/*
	 * Do this PointSourceGeneratorFrame frame = new PointSourceGeneratorFrame(<File>);
	 * frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //
	 * frame.centerFrame(); frame.pack(); frame.setVisible(true);
	 */

	/*
	 * Or this new JButton(new KMZGeneratorLauncherAction(<DataFileProducer>,
	 * "Launch"))
	 */

	public static void main(String[] args) {

		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		/*
		 * Creating a button like this is the hook needed to launch the GUI
		 */
		frame.setContentPane(new JButton(new KMZGeneratorLauncherAction(frame,
				"Launch", "File Generator")));

		frame.pack();
		frame.setVisible(true);

	}
}
