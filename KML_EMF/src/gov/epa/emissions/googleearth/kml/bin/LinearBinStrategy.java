package gov.epa.emissions.googleearth.kml.bin;

import gov.epa.emissions.googleearth.kml.generator.BinRangeManager;
import gov.epa.emissions.googleearth.kml.generator.BinRangeManagerImpl;

import java.util.ArrayList;
import java.util.List;

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
	public BinRangeManager createBins() {

		List<Range> ranges = new ArrayList<Range>();
		double diff = this.max - this.min;
		double binSize = diff / this.binCount;

		for (int i = 0; i < this.binCount; i++) {

			double min = this.min + i * binSize;
			double max = this.min + (i + 1) * binSize;

			Range range = new Range();
			range.setMin(min);
			range.setMax(max);

			ranges.add(range);
		}

		return new BinRangeManagerImpl(ranges);
	}

}
