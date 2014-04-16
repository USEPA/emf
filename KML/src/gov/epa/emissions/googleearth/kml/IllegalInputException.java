package gov.epa.emissions.googleearth.kml;

@SuppressWarnings("serial")
public class IllegalInputException extends Throwable {

	private static int errorCodeGenerator = 1000;
	public static final int ERROR_CODE_GENERAL = IllegalInputException
			.generateErrorCode();
	public static final int ERROR_CODE_NULL_INPUT = IllegalInputException
			.generateErrorCode();
	public static final int ERROR_CODE_INPUT_FILE = IllegalInputException
			.generateErrorCode();
	public static final int ERROR_CODE_PROCESSING_INPUT_FILE = IllegalInputException
			.generateErrorCode();
	public static final int ERROR_CODE_FIRST_PARAMETER_KEY = IllegalInputException
			.generateErrorCode();
	public static final int ERROR_CODE_PROPERTIES_FILE_DOESNT_EXIST = IllegalInputException
			.generateErrorCode();
	public static final int ERROR_CODE_KEY_MISSING_HYPHEN = IllegalInputException
			.generateErrorCode();
	public static final int ERROR_CODE_INCORRECT_KEY_VALUE = IllegalInputException
			.generateErrorCode();
	public static final int ERROR_CODE_VALUE_STARTS_WITH_HYPHEN = IllegalInputException
			.generateErrorCode();
	public static final int ERROR_CODE_MISSING_VALUE = IllegalInputException
			.generateErrorCode();
	public static final int ERROR_CODE_INCORRECT_VALUE_TYPE = IllegalInputException
			.generateErrorCode();
	public static final int ERROR_CODE_INCORRECT_VALUE = IllegalInputException
			.generateErrorCode();

	private int errorCode;

	public static int generateErrorCode() {
		return IllegalInputException.errorCodeGenerator++;
	}

	public IllegalInputException(int errorCode, String errorMessage) {

		super("ERROR[" + errorCode + "] " + errorMessage);
		this.errorCode = errorCode;
	}

	public int getErrorCode() {
		return errorCode;
	}
}
