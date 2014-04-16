package gov.epa.emissions.googleearth.kml.bin;

import gov.epa.emissions.googleearth.kml.ConfigurationManager;
import gov.epa.emissions.googleearth.kml.utils.Utils;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class EqualBinStrategy implements BinStrategy {

	private List<Double> values;
	private int binCount;

	public EqualBinStrategy(int binCount, List<Double> values) {

		this.binCount = binCount;
		this.values = values;
	}

	@Override
	public double[] createBins() {

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

		double[] retVal = new double[this.binCount];

		int count = this.values.size();
		int binSize = count / this.binCount;

		retVal[0] = this.values.get(0);
		for (int i = 1; i < retVal.length; i++) {
			retVal[i] = this.values.get(binSize * i);
		}

		for (int i = 0; i < retVal.length; i++) {

			if (retVal[i] >= 1) {
				retVal[i] = Math.floor(retVal[i]);
			} else {
				retVal[i] = Utils.roundDigits(retVal[i], 4);
			}
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
