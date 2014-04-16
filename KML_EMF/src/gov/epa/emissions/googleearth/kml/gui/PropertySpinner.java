package gov.epa.emissions.googleearth.kml.gui;

import gov.epa.emissions.googleearth.kml.PropertiesManager;
import gov.epa.emissions.googleearth.kml.PropertiesManager.PropertyKey;

import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JFrame;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

@SuppressWarnings("serial")
public class PropertySpinner extends JSpinner implements PropertyField {

	private PropertiesManager.PropertyKey propertyKey;

	public PropertiesManager.PropertyKey getPropertyKey() {
		return propertyKey;
	}

	private PropertiesManager propertiesManager;

	public PropertySpinner(final PropertyKey propertyKey,
			final PropertiesManager propertiesManager, int min) {

		SpinnerNumberModel numberModel = (SpinnerNumberModel) this.getModel();
		numberModel.setMinimum(min);

		this.propertiesManager = propertiesManager;
		this.propertyKey = propertyKey;
		this.updateProperty(this.propertiesManager);

		this.setToolTipText(propertyKey.getDescription());

		this.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				change();
			}

		});
	}

	private void change() {
		propertiesManager.setValue(propertyKey.getKey(), PropertySpinner.this
				.getValue().toString());
	}

	@Override
	public void updateProperty(PropertiesManager propertiesManager) {
		this.setValue(this.propertiesManager.getValueAsInt(this.propertyKey));
	}

	public static void main(String[] args) {

		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		PropertyTextField field = new PropertyTextField(
				PropertyKey.DATA_COLUMNNAME, PropertiesManager.getInstance());
		frame.setContentPane(field);

		frame.pack();
		frame.setVisible(true);
	}
}
