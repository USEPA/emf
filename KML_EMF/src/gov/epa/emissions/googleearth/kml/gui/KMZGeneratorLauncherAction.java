package gov.epa.emissions.googleearth.kml.gui;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;

@SuppressWarnings("serial")
public class KMZGeneratorLauncherAction extends AbstractAction {

	private DataFileProducer dataFileProducer;

	public KMZGeneratorLauncherAction(DataFileProducer dataFileProducer,
			String label) {

		super(label);
		this.dataFileProducer = dataFileProducer;
	}

	public KMZGeneratorLauncherAction(String label) {
		super(label);
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		PointSourceGeneratorFrame frame = null;
		if (this.dataFileProducer == null) {
			frame = new PointSourceGeneratorFrame();
		} else {
			frame = new PointSourceGeneratorFrame(this.dataFileProducer
					.getDataFile());
		}

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// frame.centerFrame();
		frame.pack();
		frame.setVisible(true);
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
		frame.setContentPane(new JButton(new KMZGeneratorLauncherAction(
				"Launch")));

		frame.pack();
		frame.setVisible(true);

	}
}
