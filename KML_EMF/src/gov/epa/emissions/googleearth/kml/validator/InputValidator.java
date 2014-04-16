package gov.epa.emissions.googleearth.kml.validator;

import gov.epa.emissions.googleearth.kml.KMZGeneratorException;

public interface InputValidator {

	boolean validateInput(String[] input) throws KMZGeneratorException;

	String getUsage();
}
