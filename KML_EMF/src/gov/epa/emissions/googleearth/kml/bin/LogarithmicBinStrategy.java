package gov.epa.emissions.googleearth.kml.bin;

import gov.epa.emissions.googleearth.kml.ConfigurationManager;
import gov.epa.emissions.googleearth.kml.generator.BinRangeManager;
import gov.epa.emissions.googleearth.kml.generator.BinRangeManagerImpl;

import java.util.ArrayList;
import java.util.List;

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
	public BinRangeManager createBins() {

		List<Range> ranges = new ArrayList<Range>(this.binCount);

		double logOfMin = 0;

		if (this.min > 0) {
			logOfMin = Math.log10(this.min);
		}

		double logOfMax = Math.log10(this.max);
		double diffOfLogs = logOfMax - logOfMin;
		double binSize = diffOfLogs / this.binCount;

		double m = logOfMin;
		for (int i = 0; i < this.binCount; i++) {

			Range range = new Range();
			range.setMin(m);
			m += binSize;
			range.setMax(m);

			ranges.add(range);
		}

		Boolean showOutput = ConfigurationManager.getInstance()
				.getValueAsBoolean(
						ConfigurationManager.PropertyKey.SHOW_OUTPUT.getKey());
		if (showOutput) {
			for (int i = 0; i < ranges.size(); i++) {
				System.out.println("log of bin[" + i + "]: " + ranges.get(i));
			}
		}

		for (Range range : ranges) {

			double rangeMin = Math.pow(10, range.getMin()) - 1;
			double rangeMax = Math.pow(10, range.getMax()) - 1;
			range.setMin(rangeMin);
			range.setMax(rangeMax);
		}

		if (!ranges.isEmpty()) {
			ranges.get(ranges.size() - 1).setMax(this.max);
		}

		return new BinRangeManagerImpl(ranges);
	}
}
