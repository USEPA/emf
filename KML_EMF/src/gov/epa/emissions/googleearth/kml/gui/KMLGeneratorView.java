package gov.epa.emissions.googleearth.kml.gui;

import gov.epa.emissions.googleearth.kml.KMZGeneratorException;
import gov.epa.emissions.googleearth.kml.PropertiesManager;

import java.io.File;

public interface KMLGeneratorView {

	void handleDataFile(File datafile) throws KMZGeneratorException;
	
	void updatePropertiesFields(PropertiesManager propertiesManager) throws KMZGeneratorException;
}
