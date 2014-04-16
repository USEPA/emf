package gov.epa.emissions.googleearth.kml.generator;

import gov.epa.emissions.googleearth.kml.bin.Range;

public interface BinRangeManager {

	int getBinNumber(double value);

	int getRangeCount();
	
	Range getRange(int index);
	double getMinRange();
}