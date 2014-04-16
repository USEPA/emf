package gov.epa.emissions.googleearth.kml.bin;

import gov.epa.emissions.googleearth.kml.ConfigurationManager;

public class LogarithmicBinStrategy implements BinStrategy {

	private double min;
	private double max;
	private int binCount;

	public LogarithmicBinStrategy(double min, double max, int binCount) {

		this.min = min;
		this.max = max;
		this.binCount = binCount;
	}

	@Override
	public double[] createBins() {

		double[] retVal = new double[this.binCount];

		double logOfMin = 0;
		if (this.min > 0) {
			logOfMin = Math.log10(this.min);
		}

		double logOfMax = Math.log10(this.max);
		double diffOfLogs = logOfMax - logOfMin;
		double binSize = diffOfLogs / (this.binCount-1);

		retVal[0] = 0;
		retVal[1] = logOfMin;
		for (int i = 2; i < retVal.length; i++) {
			retVal[i] = retVal[i - 1] + binSize;
		}

		Boolean showOutput = ConfigurationManager.getInstance()
				.getValueAsBoolean(
						ConfigurationManager.PropertyKey.SHOW_OUTPUT.getKey());
		if (showOutput) {
			for (int i = 0; i < retVal.length; i++) {
				System.out.println("log of bin[" + i + "] starts at "
						+ retVal[i]);
			}
		}

		retVal[0] = this.min;
		retVal[1] = this.min;
		for (int i = 2; i < retVal.length; i++) {
			retVal[i] = Math.pow(10, retVal[i]);
		}

		if (showOutput) {
			for (int i = 0; i < retVal.length; i++) {
				System.out.println("bin[" + i + "] starts at " + retVal[i]);
			}
		}

		return retVal;
	}
}
