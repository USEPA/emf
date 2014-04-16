package gov.epa.emissions.googleearth.kml.bin;

import java.util.List;

public abstract class AbstractEqualWidthBinStrategy implements BinStrategy {

	private List<Double> values;
	private int binCount;
	private double minCutoff = MIN_MIN_CUTOFF;
	private double maxCutoff = MAX_MAX_CUTOFF;
	public static final double MAX_MAX_CUTOFF = Double.MAX_VALUE;
	public static final double MIN_MIN_CUTOFF = -Double.MAX_VALUE;

	public AbstractEqualWidthBinStrategy(int binCount, List<Double> values) {

		this.values = values;
		this.binCount = binCount;
	}

	public void setMinCutoff(double minCutoff) {
		this.minCutoff = minCutoff;
	}

	public void setMaxCutoff(double maxCutoff) {
		this.maxCutoff = maxCutoff;
	}

	protected List<Double> getValues() {
		return values;
	}

	public int getBinCount() {
		return binCount;
	}

	public double getMinCutoff() {
		return minCutoff;
	}

	public double getMaxCutoff() {
		return maxCutoff;
	}
}