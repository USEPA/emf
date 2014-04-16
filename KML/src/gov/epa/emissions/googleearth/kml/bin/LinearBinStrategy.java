package gov.epa.emissions.googleearth.kml.bin;

import gov.epa.emissions.googleearth.kml.ConfigurationManager;

public class LinearBinStrategy implements BinStrategy {

	private double min;
	private double max;
	private int binCount;

	public LinearBinStrategy(double min, double max, int binCount) {

		this.min = min;
		this.max = max;
		this.binCount = binCount;
	}

	@Override
	public double[] createBins() {

		double[] retVal = new double[this.binCount];
		double diff = this.max - this.min;
		double binSize = diff / this.binCount;

		retVal[0] = this.min;
		for (int i = 1; i < retVal.length; i++) {
			retVal[i] = retVal[i - 1] + binSize;
		}

		if (ConfigurationManager.getInstance().getValueAsBoolean(
				ConfigurationManager.PropertyKey.SHOW_OUTPUT.getKey())) {
			for (int i = 0; i < retVal.length; i++) {
				System.out.println("bin[" + i + "] starts at " + retVal[i]);
			}
		}

		return retVal;
	}

}
