package gov.epa.emissions.googleearth.kml.gui;

import gov.epa.emissions.googleearth.kml.PropertiesManager;

public interface PropertyField {

	void updateProperty(PropertiesManager propertiesManager);

	void setEnabled(boolean enabled);
}
