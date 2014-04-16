package gov.epa.emissions.googleearth.kml.bin;

import gov.epa.emissions.googleearth.kml.generator.BinRangeManager;
import gov.epa.emissions.googleearth.kml.generator.BinRangeManagerImpl;
import gov.epa.emissions.googleearth.kml.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class EqualWidthStrategy extends AbstractEqualWidthBinStrategy {

	public EqualWidthStrategy(int binCount, List<Double> values) {
		super(binCount, values);
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

		List<Range> ranges = new ArrayList<Range>();

		double absMin = 0;
		if (!localValues.isEmpty()) {
			absMin = localValues.get(0);
		}

		if (this.getMinCutoff() != MIN_MIN_CUTOFF) {
			absMin = this.getMinCutoff();
		}

		double absMax = 0;
		if (!localValues.isEmpty()) {
			absMax = localValues.get(localValues.size() - 1);
		}

		if (this.getMaxCutoff() != MAX_MAX_CUTOFF) {
			absMax = this.getMaxCutoff();
		}

		double diff = absMax - absMin;

		double binWidth = diff / this.getBinCount();

		for (int i = 0; i < this.getBinCount(); i++) {

			double min = binWidth * i + absMin;
			double max = binWidth * (i + 1) + absMin;

			min = Utils.roundDigits(min, 4);
			max = Utils.roundDigits(max, 4);

			Range range = new Range();
			range.setMin(min);
			range.setMax(max);

			ranges.add(range);
		}

		return new BinRangeManagerImpl(ranges);
	}
}
