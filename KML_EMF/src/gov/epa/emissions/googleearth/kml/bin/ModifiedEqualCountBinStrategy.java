package gov.epa.emissions.googleearth.kml.bin;

import gov.epa.emissions.googleearth.kml.generator.BinRangeManager;
import gov.epa.emissions.googleearth.kml.generator.BinRangeManagerImpl;
import gov.epa.emissions.googleearth.kml.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ModifiedEqualCountBinStrategy implements BinStrategy {

	private List<Double> values;
	private int binCount;

	public ModifiedEqualCountBinStrategy(int binCount, List<Double> values) {

		this.binCount = binCount;
		this.values = values;
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

		List<Range> ranges = new ArrayList<Range>();

		double firstValue = this.values.get(0);
		int sameCount = 1;
		for (; sameCount < this.values.size(); sameCount++) {

			Double nextValue = this.values.get(sameCount);
			if (firstValue != nextValue) {
				break;
			}
		}

		if (sameCount > this.values.size() / this.binCount) {

			int count = this.values.size() - sameCount;
			int binSize = count / (this.binCount - 1);

			double min = this.values.get(0);
			double max = this.values.get(0);
			Range sameRange = new Range();
			sameRange.setMin(min);
			sameRange.setMax(max);

			ranges.add(sameRange);

			for (int i = 1; i < this.binCount; i++) {
				min = this.values.get(binSize * i + sameCount);
				max = this.values.get(binSize * (i + 1) + sameCount);

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

		} else {

			int count = this.values.size();
			int binSize = count / this.binCount;

			for (int i = 0; i < this.binCount; i++) {

				double min = this.values.get(binSize * i + sameCount);
				double max = this.values.get(binSize * (i + 1) + sameCount);

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
		}

		return new BinRangeManagerImpl(ranges);
	}
}
