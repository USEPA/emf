package gov.epa.emissions.googleearth.kml.validator;

import gov.epa.emissions.googleearth.kml.ConfigurationManager;
import gov.epa.emissions.googleearth.kml.KMZGeneratorException;
import gov.epa.emissions.googleearth.kml.PropertiesManager.PropertyKey;
import gov.epa.emissions.googleearth.kml.version.Version;

import java.io.File;

public class CLIInputValidator implements InputValidator {

	public static final String DATA_FILE_KEY = "-data.file";
	public static final String PROPERTIES_FILE_KEY = "-properties.file";
	public static final String VERSION_KEY = "-version";

	private static InputValidator instance = new CLIInputValidator();

	private CLIInputValidator() {
	}

	public static synchronized InputValidator getInstance() {
		return CLIInputValidator.instance;
	}

	@Override
	public boolean validateInput(String[] input) throws KMZGeneratorException {

		if (input == null) {
			throw new KMZGeneratorException(
					KMZGeneratorException.ERROR_CODE_NULL_INPUT,
					"Input parameters cannot be null.\n\n" + getUsage());
		}

		if (ConfigurationManager.getInstance().getValueAsBoolean(
				ConfigurationManager.PropertyKey.SHOW_OUTPUT.getKey())) {
			for (String string : input) {
				System.out.println(string);
			}
		}

		if (input.length == 1 && VERSION_KEY.equals(input[0])) {

			System.out.println("Build Version: " + new Version().getVersion());
			return false;
		} else if (input.length >= 4 && input.length % 2 == 0) {

			boolean foundInputFile = false;
			boolean foundPropertiesFile = false;

			for (int i = 0; i < input.length; i += 2) {

				if (i % 2 == 0) {

					if (!input[i].startsWith("-")) {
						throw new KMZGeneratorException(
								KMZGeneratorException.ERROR_CODE_KEY_MISSING_HYPHEN,
								"Overriding parameter key " + input[i]
										+ " does not begin with '-'.\n\n"
										+ getUsage());
					}

					if (input[i + 1].startsWith("-")) {
						throw new KMZGeneratorException(
								KMZGeneratorException.ERROR_CODE_VALUE_STARTS_WITH_HYPHEN,
								"Overriding parameter value " + input[i + 1]
										+ " begins with '-'.\n\n" + getUsage());
					}
				}

				if (PROPERTIES_FILE_KEY.equals(input[i])) {

					File propertiesFile = null;
					try {

						propertiesFile = new File(input[i + 1]);

						if (!propertiesFile.exists()) {
							throw new KMZGeneratorException(
									KMZGeneratorException.ERROR_CODE_PROPERTIES_FILE_DOESNT_EXIST,
									"Input file '" + input[1]
											+ "' does not exist.\n\n"
											+ getUsage());
						}
					} catch (Exception e) {
						throw new KMZGeneratorException(
								KMZGeneratorException.ERROR_CODE_PROPERTIES_FILE_DOESNT_EXIST,
								e.getLocalizedMessage() + "\n\n" + getUsage());
					}

					foundPropertiesFile = true;
				} else if (DATA_FILE_KEY.equals(input[i])) {

					File dataFile = null;
					try {
						dataFile = new File(input[i + 1]);

						if (!dataFile.exists()) {
							throw new KMZGeneratorException(
									KMZGeneratorException.ERROR_CODE_DATA_FILE_DOESNT_EXIST,
									"Data file '" + input[1]
											+ "' does not exist.\n\n"
											+ getUsage());
						}
					} catch (Exception e) {
						throw new KMZGeneratorException(
								KMZGeneratorException.ERROR_CODE_DATA_FILE, e
										.getLocalizedMessage()
										+ "\n\n" + getUsage());
					}

					foundInputFile = true;
				}
			}

			if (!foundInputFile) {

				throw new KMZGeneratorException(
						KMZGeneratorException.ERROR_CODE_MISSING_VALUE,
						"Parameter key " + DATA_FILE_KEY + " not found.");
			} else if (!foundPropertiesFile) {

				throw new KMZGeneratorException(
						KMZGeneratorException.ERROR_CODE_MISSING_VALUE,
						"Parameter key " + PROPERTIES_FILE_KEY + " not found.");
			}

		} else {
			throw new KMZGeneratorException(
					KMZGeneratorException.ERROR_CODE_GENERAL, getUsage());
		}

		return true;
	}

	@Override
	public String getUsage() {
		return "Usage: {" + VERSION_KEY + " | " + PROPERTIES_FILE_KEY
				+ " <properties_file> " + DATA_FILE_KEY
				+ " <data_file> [-<overriding_key> <overriding_value>]\n\n"
				+ this.getPropertiesFileFormat() + "}";
	}

	protected String getPropertiesFileFormat() {

		StringBuilder sb = new StringBuilder();

		sb.append("  Properties File Format:\n\n");

		PropertyKey[] values = PropertyKey.values();
		for (PropertyKey propertyKey : values) {
			sb.append("    " + propertyKey.getKey() + "=<"
					+ propertyKey.getDescription() + ">\n");
		}

		return sb.toString();
	}

	public static void main(String[] args) {
		System.out.println(CLIInputValidator.getInstance().getUsage());
	}
}
