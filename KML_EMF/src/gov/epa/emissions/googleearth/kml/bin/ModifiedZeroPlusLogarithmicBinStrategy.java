package gov.epa.emissions.googleearth.kml.bin;

import gov.epa.emissions.googleearth.kml.generator.BinRangeManager;

import java.util.ArrayList;
import java.util.List;

public class ModifiedZeroPlusLogarithmicBinStrategy implements BinStrategy {

	private double min;
	private double max;
	private int binCount;
	private double factor;

	public ModifiedZeroPlusLogarithmicBinStrategy(double min, double max,
			int binCount, double factor) {

		this.min = min;
		this.max = max;
		this.binCount = binCount;
		this.factor = factor;
	}

	@Override
	public BinRangeManager createBins() {

		List<Range> ranges = new ArrayList<Range>();

		throw new RuntimeException("This strategy (" + this.getClass()
				+ ") has not been refactored to new codebase.");

		// double[] retVal = new double[this.binCount];
		//
		// double logOfMin = 0;
		// if (this.min > 0) {
		// logOfMin = Math.log10(this.min);
		// }
		//
		// double logOfMax = Math.log10(this.max);
		// double diffOfLogs = logOfMax - logOfMin;
		// double binSize = diffOfLogs / (this.binCount - 1);
		//
		// retVal[0] = 0;
		// retVal[1] = logOfMin;
		// for (int i = 2; i < retVal.length; i++) {
		// retVal[i] = retVal[i - 1] + binSize * i / this.factor;
		// }
		//
		// Boolean showOutput = ConfigurationManager.getInstance()
		// .getValueAsBoolean(
		// ConfigurationManager.PropertyKey.SHOW_OUTPUT.getKey());
		// if (showOutput) {
		// for (int i = 0; i < retVal.length; i++) {
		// System.out.println("log of bin[" + i + "] starts at "
		// + retVal[i]);
		// }
		// }
		//
		// retVal[0] = this.min;
		// retVal[1] = this.min;
		// for (int i = 2; i < retVal.length; i++) {
		// retVal[i] = Math.pow(10, retVal[i]) - 1;
		// }

		// if (ConfigurationManager.getInstance().getValueAsBoolean(
		// ConfigurationManager.PropertyKey.SHOW_OUTPUT.getKey())) {
		// for (int i = 0; i < ranges.size(); i++) {
		// System.out.println("bin[" + i + "] starts at " + ranges.get(i));
		// }
		// }
		//
		// return new BinRangeManagerImpl(ranges);
	}
}
