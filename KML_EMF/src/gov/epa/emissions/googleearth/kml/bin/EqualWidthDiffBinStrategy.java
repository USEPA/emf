package gov.epa.emissions.googleearth.kml.bin;

import gov.epa.emissions.googleearth.kml.generator.BinRangeManager;
import gov.epa.emissions.googleearth.kml.generator.DiffPlotBinRangeManager;
import gov.epa.emissions.googleearth.kml.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class EqualWidthDiffBinStrategy extends AbstractEqualWidthBinStrategy {

	private double zero;

	public EqualWidthDiffBinStrategy(int binCount, List<Double> values,
			double zero) {
		super(binCount, values);
		this.zero = zero;
	}

	@Override
	public BinRangeManager createBins() {

		List<Double> localValues = this.getValues();
		Collections.sort(localValues, new Comparator<Double>() {

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
		for (Double value : localValues) {

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
		double absMin = this.zero;
		if (!negValues.isEmpty()) {
			absMin = negValues.get(0);
		}

		if (this.getMinCutoff() != MIN_MIN_CUTOFF) {
			absMin = this.getMinCutoff();
		}

		double diff = absMin + this.zero;

		double binWidth = Math.abs(diff / (this.getBinCount() / 2));

		int i = 0;
		for (; i < this.getBinCount() / 2; i++) {

			double min = binWidth * i + absMin;
			double max = binWidth * (i + 1) + absMin;

			min = Utils.roundDigits(min, 4);
			max = Utils.roundDigits(max, 4);

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
		double absMax = this.zero;
		if (!posValues.isEmpty()) {
			absMax = posValues.get(posValues.size() - 1);
		}

		if (this.getMaxCutoff() != MAX_MAX_CUTOFF) {
			absMax = this.getMaxCutoff();
		}

		diff = absMax - this.zero;

		binWidth = diff / (this.getBinCount() / 2);

		int resetI = i;
		for (; i < this.getBinCount(); i++) {

			double min = binWidth * (i - resetI) + this.zero;
			double max = binWidth * (i - resetI + 1) + this.zero;

			min = Utils.roundDigits(min, 4);
			max = Utils.roundDigits(max, 4);

			Range range = new Range();
			range.setMin(min);
			range.setMax(max);

			ranges.add(range);
		}

		return new DiffPlotBinRangeManager(ranges);
	}
}
