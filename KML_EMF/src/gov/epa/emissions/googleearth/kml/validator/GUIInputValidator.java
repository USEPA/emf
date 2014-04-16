package gov.epa.emissions.googleearth.kml.validator;

import gov.epa.emissions.googleearth.kml.KMZGeneratorException;

public class GUIInputValidator implements InputValidator {

	private static InputValidator instance = new GUIInputValidator();

	private GUIInputValidator() {
	}

	public static synchronized InputValidator getInstance() {
		return GUIInputValidator.instance;
	}

	@Override
	public boolean validateInput(String[] input) throws KMZGeneratorException {
		// FIXME This is doing no validation right now
		return true;
	}

	@Override
	public String getUsage() {
		// FIXME Is this relevant?
		return "";
	}
}
