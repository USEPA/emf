package gov.epa.emissions.googleearth.kml.generator;

public enum BinningAlgorithmType {

	EQUAL_COUNT("Equal Count"), //
	EQUAL_WIDTH("Equal Width"), //
	LOGARITHMIC("Logarithmic"); //

	private String displayName;

	private BinningAlgorithmType(String displayName) {

		this.displayName = displayName;
	}

	@Override
	public String toString() {
		return this.displayName;
	}

	public static BinningAlgorithmType getByDisplayName(String displayName) {

		BinningAlgorithmType retVal = null;

		if (EQUAL_WIDTH.displayName.equals(displayName)) {
			retVal = EQUAL_WIDTH;
		} else if (EQUAL_COUNT.displayName.equals(displayName)) {
			retVal = EQUAL_COUNT;
		} else if (LOGARITHMIC.displayName.equals(displayName)) {
			retVal = LOGARITHMIC;
		}

		return retVal;
	}
}
