package gov.epa.emissions.googleearth.kml.bin;

import gov.epa.emissions.googleearth.kml.generator.BinRangeManager;
import gov.epa.emissions.googleearth.kml.generator.BinRangeManagerImpl;
import gov.epa.emissions.googleearth.kml.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class EqualCountBinStrategy implements BinStrategy {

	private List<Double> values;
	private int binCount;

	public EqualCountBinStrategy(int binCount, List<Double> values) {

		this.binCount = binCount;
		this.values = values;
	}

	@Override
	public BinRangeManager createBins() {

		Collections.sort(this.values, new Comparator<Double>() {

			@Override
			public int compare(Double d1, Double d2) {
				return d1.compareTo(d2);

//				int retVal = 0;
//
//				if (d2 != d1) {
//
//					if (d1 < d2) {
//						retVal = -1;
//					} else {
//						retVal = 1;
//					}
//				}
//
//				return retVal;
			}

		});

		List<Range> ranges = new ArrayList<Range>();

		int count = this.values.size();
		int binSize = count / this.binCount;

		double min = 0;
		double max = 0;
		for (int i = 0; i < this.binCount; i++) {

			min = this.values.get(binSize * i);

			if (i == this.binCount - 1) {
			    max = this.values.get(count-1);
			} else {
				max = this.values.get(binSize * (i + 1) - 1);
			}
			// max = this.values.get(binSize * (i + 1));
			// }

			//if (min >= 1) {
			//	min = Math.floor(min);
			//} else {
			//	min = Utils.roundDigits(min, 4);
			//}

			//if (max >= 1) {
			//	max = Math.floor(max);
			//} else {
			//	max = Utils.roundDigits(max, 4);
			//}

			Range range = new Range();
			range.setMin(min);
			range.setMax(max);

			ranges.add(range);
		}

		return new BinRangeManagerImpl(ranges);
	}
}
