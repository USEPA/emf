package gov.epa.emissions.googleearth.kml;

@SuppressWarnings("serial")
public class KMZGeneratorException extends Throwable {

	private static int errorCodeGenerator = 1000;
	public static final int ERROR_CODE_GENERAL = KMZGeneratorException
			.generateErrorCode();
	public static final int ERROR_CODE_NULL_INPUT = KMZGeneratorException
			.generateErrorCode();
	public static final int ERROR_CODE_ARGS_COUNT = KMZGeneratorException
			.generateErrorCode();
	public static final int ERROR_CODE_PROPERTIES_FILE = KMZGeneratorException
			.generateErrorCode();
	public static final int ERROR_CODE_PROCESSING_PROPERTIES_FILE = KMZGeneratorException
			.generateErrorCode();
	public static final int ERROR_CODE_PROPERTIES_FILE_DOESNT_EXIST = KMZGeneratorException
			.generateErrorCode();
	public static final int ERROR_CODE_DATA_FILE = KMZGeneratorException
			.generateErrorCode();
	public static final int ERROR_CODE_PROCESSING_DATA_FILE = KMZGeneratorException
			.generateErrorCode();
	public static final int ERROR_CODE_DATA_FILE_DOESNT_EXIST = KMZGeneratorException
			.generateErrorCode();
	public static final int ERROR_CODE_MISSING_KEY = KMZGeneratorException
			.generateErrorCode();
	public static final int ERROR_CODE_KEY_MISSING_HYPHEN = KMZGeneratorException
			.generateErrorCode();
	public static final int ERROR_CODE_INCORRECT_KEY_VALUE = KMZGeneratorException
			.generateErrorCode();
	public static final int ERROR_CODE_VALUE_STARTS_WITH_HYPHEN = KMZGeneratorException
			.generateErrorCode();
	public static final int ERROR_CODE_MISSING_VALUE = KMZGeneratorException
			.generateErrorCode();
	public static final int ERROR_CODE_INCORRECT_VALUE_TYPE = KMZGeneratorException
			.generateErrorCode();
	public static final int ERROR_CODE_INCORRECT_VALUE = KMZGeneratorException
			.generateErrorCode();
	public static final int ERROR_CODE_UNABLE_TO_WRITE_KML_DOCUMENT = KMZGeneratorException
			.generateErrorCode();
	public static final int ERROR_CODE_READING_DATA_FILE = KMZGeneratorException.generateErrorCode();

	private int errorCode;

	public static int generateErrorCode() {
		return KMZGeneratorException.errorCodeGenerator++;
	}

	public KMZGeneratorException(int errorCode, String errorMessage) {

		super("ERROR[" + errorCode + "] " + errorMessage);
		this.errorCode = errorCode;
	}

	public int getErrorCode() {
		return errorCode;
	}
}
