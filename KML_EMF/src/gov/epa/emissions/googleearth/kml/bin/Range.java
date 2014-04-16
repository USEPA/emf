package gov.epa.emissions.googleearth.kml.bin;

public class Range implements Comparable<Range> {

	private double min;
	private double max;

	public double getMin() {
		return min;
	}

	public void setMin(double min) {
		this.min = min;
	}

	public double getMax() {
		return max;
	}

	public void setMax(double max) {
		this.max = max;
	}

	public boolean isInRangeInclusive(double value) {
		return (this.min == this.max && value == this.max)
				|| (value >= this.min && value <= this.max);
	}

	public boolean isInRangeExclusive(double value) {
		return value > this.min && value < this.max;
	}

	@Override
	public String toString() {
		return "(" + this.min + ", " + this.max + ")";
	}

	@Override
	public int compareTo(Range that) {

		int retVal = 0;

		if (this.min < that.min) {
			retVal = -1;
		} else if (this.min > that.min) {
			retVal = 1;
		}

		return retVal;
	}

}
