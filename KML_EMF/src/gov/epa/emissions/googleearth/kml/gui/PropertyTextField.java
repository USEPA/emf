package gov.epa.emissions.googleearth.kml.gui;

import gov.epa.emissions.googleearth.kml.PropertiesManager;
import gov.epa.emissions.googleearth.kml.PropertiesManager.PropertyKey;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

@SuppressWarnings("serial")
public class PropertyTextField extends JTextField implements PropertyField {

	private PropertiesManager.PropertyKey propertyKey;
	private PropertiesManager propertiesManager;

	public PropertyTextField(final PropertyKey propertyKey,
			final PropertiesManager propertiesManager) {

		this.propertiesManager = propertiesManager;
		this.propertyKey = propertyKey;
		this.updateProperty(this.propertiesManager);

		this.setToolTipText(propertyKey.getDescription());

		this.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void changedUpdate(DocumentEvent e) {
				this.change(e);
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				this.change(e);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				this.change(e);
			}

			private void change(DocumentEvent e) {
				propertiesManager.setValue(propertyKey.getKey(),
						PropertyTextField.this.getText());
			}
		});

		this.addFocusListener(new FocusAdapter() {

			@Override
			public void focusGained(FocusEvent e) {
				PropertyTextField.this.selectAll();
			}
		});
	}

	@Override
	public void updateProperty(PropertiesManager propertiesManager) {
		this.setText(this.propertiesManager.getValue(this.propertyKey));
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
