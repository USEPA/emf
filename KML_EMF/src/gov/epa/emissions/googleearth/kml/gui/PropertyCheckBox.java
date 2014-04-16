package gov.epa.emissions.googleearth.kml.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import gov.epa.emissions.googleearth.kml.PropertiesManager;
import gov.epa.emissions.googleearth.kml.PropertiesManager.PropertyKey;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

@SuppressWarnings("serial")
public class PropertyCheckBox extends JCheckBox implements PropertyField {

	private PropertiesManager.PropertyKey propertyKey;
	private PropertiesManager propertiesManager;

	public PropertyCheckBox(final PropertyKey propertyKey,
			final PropertiesManager propertiesManager) {

		this.propertiesManager = propertiesManager;
		this.propertyKey = propertyKey;
		this.updateProperty(this.propertiesManager);

		this.setToolTipText(propertyKey.getDescription());

		this.addActionListener(new ActionListener() {

			
			@Override
			public void actionPerformed(ActionEvent e) {
				this.change(e);
			}

			private void change(ActionEvent e) {
				propertiesManager.setValue(propertyKey.getKey(), Boolean
						.toString(PropertyCheckBox.this.isSelected()));
			}
		});
	}

	@Override
	public void updateProperty(PropertiesManager propertiesManager) {
		this.setSelected(this.propertiesManager
				.getValueAsBoolean(this.propertyKey));
	}

	public static void main(String[] args) {

		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		PropertyCheckBox field = new PropertyCheckBox(
				PropertyKey.PLOT_DIFF, PropertiesManager.getInstance());
		frame.setContentPane(field);

		frame.pack();
		frame.setVisible(true);
	}
}
