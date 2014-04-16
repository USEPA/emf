package gov.epa.emissions.googleearth.kml.gui;

import gov.epa.emissions.googleearth.kml.PropertiesManager;
import gov.epa.emissions.googleearth.kml.PropertiesManager.PropertyKey;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JFrame;

@SuppressWarnings("serial")
public class PropertyComboBox extends JComboBox implements PropertyField {

	private PropertiesManager.PropertyKey propertyKey;
	public PropertiesManager.PropertyKey getPropertyKey() {
		return propertyKey;
	}

	private PropertiesManager propertiesManager;

	public PropertyComboBox(final PropertyKey propertyKey,
			final PropertiesManager propertiesManager) {

		this.propertiesManager = propertiesManager;
		this.propertyKey = propertyKey;
		this.updateProperty(this.propertiesManager);

		this.setToolTipText(propertyKey.getDescription());

		this.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				this.change();
			}

			private void change() {
				propertiesManager.setValue(propertyKey.getKey(),
						PropertyComboBox.this.getSelectedItem().toString());
			}
		});
	}

	@Override
	public void updateProperty(PropertiesManager propertiesManager) {
		this.setSelectedItem(this.propertiesManager.getValue(this.propertyKey));
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
