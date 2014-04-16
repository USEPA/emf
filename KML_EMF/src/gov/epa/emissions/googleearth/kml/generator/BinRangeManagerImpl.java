package gov.epa.emissions.googleearth.kml.generator;

import gov.epa.emissions.googleearth.kml.bin.Range;

import java.util.List;

public class BinRangeManagerImpl implements BinRangeManager {

	private List<Range> binRanges;
	private double minRange; 

	public BinRangeManagerImpl(List<Range> binRanges) {
		this.binRanges = binRanges;
		minRange = Double.MAX_VALUE;
		for (Range r : binRanges)
		{
			double net = r.getMax() - r.getMin();
			if ( net<0) net = 0-net;
			if ( this.minRange>net) this.minRange = net;
		}
	}

	@Override
	public int getBinNumber(double value) {

		int binNumber = -1;
		for (binNumber = 0; binNumber < this.binRanges.size(); binNumber++) {
			if (this.binRanges.get(binNumber).isInRangeInclusive(value)) {
				break;
			}
		}

		if (binNumber == this.binRanges.size()) {
			binNumber--;
		}

		return binNumber;
	}

	@Override
	public int getRangeCount() {
		return this.binRanges.size();
	}

	@Override
	public Range getRange(int index) {
		return this.binRanges.get(index);
	}
	
	@Override
	public double getMinRange() {
		return this.minRange;
	}	
}
