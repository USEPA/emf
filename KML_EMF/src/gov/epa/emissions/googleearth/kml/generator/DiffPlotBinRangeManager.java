package gov.epa.emissions.googleearth.kml.generator;

import gov.epa.emissions.googleearth.kml.bin.Range;

import java.util.List;

public class DiffPlotBinRangeManager extends BinRangeManagerImpl {

	public DiffPlotBinRangeManager(List<Range> binRanges) {
		super(binRanges);
	}

	@Override
	public int getBinNumber(double value) {

		int binNumber = -1;
		int zeroRangeIndex = this.getZeroRangeIndex();
		if (this.getZeroRange().isInRangeInclusive(value)) {
			binNumber = zeroRangeIndex;
		} else {

			for (binNumber = 0; binNumber < this.getRangeCount(); binNumber++) {
				if (binNumber != zeroRangeIndex
						&& this.getRange(binNumber).isInRangeInclusive(value)) {
					break;
				}
			}
		}

		if (binNumber == this.getRangeCount()) {
			binNumber--;
		}

		return binNumber;
	}

	private Range getZeroRange() {
		return this.getRange(getZeroRangeIndex());
	}

	private int getZeroRangeIndex() {
		return this.getRangeCount() / 2;
	}
}
