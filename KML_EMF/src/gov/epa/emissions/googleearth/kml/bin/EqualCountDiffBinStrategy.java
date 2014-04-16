package gov.epa.emissions.googleearth.kml.bin;

import gov.epa.emissions.googleearth.kml.generator.BinRangeManager;
import gov.epa.emissions.googleearth.kml.generator.DiffPlotBinRangeManager;
import gov.epa.emissions.googleearth.kml.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class EqualCountDiffBinStrategy implements BinStrategy {

	private List<Double> values;
	private int binCount;
	private double zero;

	public EqualCountDiffBinStrategy(int binCount, List<Double> values,
			double zero) {

		this.binCount = binCount;
		this.values = values;
		this.zero = zero;
	}

	@Override
	public BinRangeManager createBins() {

		Collections.sort(this.values, new Comparator<Double>() {

			@Override
			public int compare(Double d1, Double d2) {

				int retVal = 0;

				if (d2 != d1) {

					if (d2 > d1) {
						retVal = -1;
					} else {
						retVal = 1;
					}
				}

				return retVal;
			}

		});

		List<Double> negValues = new ArrayList<Double>();
		List<Double> posValues = new ArrayList<Double>();
		List<Double> zeroValues = new ArrayList<Double>();
		for (Double value : this.values) {

			if (value > this.zero) {
				posValues.add(value);
			} else if (value < -this.zero) {
				negValues.add(value);
			} else {
				zeroValues.add(value);
			}
		}

		List<Range> ranges = new ArrayList<Range>();

		/*
		 * handle neg first
		 */
		int count = negValues.size();
		int binSize = count / (this.binCount / 2);

		double min = -this.zero;
		double max = -this.zero;
		int i = 0;
		for (; i < this.binCount / 2; i++) {

			if (!negValues.isEmpty()) {

				min = negValues.get(binSize * i);
				max = negValues.get(Math.min(binSize * (i + 1), negValues
						.size() - 1));
			}

			if (min >= 1) {
				min = Math.floor(min);
			} else {
				min = Utils.roundDigits(min, 4);
			}

			if (max >= 1) {
				max = Math.floor(max);
			} else {
				max = Utils.roundDigits(max, 4);
			}

			Range range = new Range();
			range.setMin(min);
			range.setMax(max);

			ranges.add(range);
		}

		/*
		 * be sure that the max neg is -zeroRange
		 */
		ranges.get(i - 1).setMax(-this.zero);

		Range zeroRange = new Range();
		zeroRange.setMin(-this.zero);
		zeroRange.setMax(this.zero);

		ranges.add(zeroRange);
		i++;

		/*
		 * handle pos next
		 */
		count = posValues.size();
		binSize = count / (this.binCount / 2);

		boolean firstPos = true;
		int resetI = i;
		for (; i < this.binCount; i++) {

			if (firstPos) {

				min = this.zero;
				firstPos = false;
			} else {
				min = posValues.get(binSize * (i - resetI));
			}

			if (i == this.binCount - 1) {
				max = posValues.get(binSize * ((i - resetI) + 1) - 1);
			} else {
				max = posValues.get(binSize * ((i - resetI) + 1));
			}

			if (min >= 10) {
				min = Math.floor(min);
			} else {
				min = Utils.roundDigits(min, 4);
			}

			if (max >= 10) {
				max = Math.floor(max);
			} else {
				max = Utils.roundDigits(max, 4);
			}

			Range range = new Range();
			range.setMin(min);
			range.setMax(max);

			ranges.add(range);
		}

		return new DiffPlotBinRangeManager(ranges);
	}
}
