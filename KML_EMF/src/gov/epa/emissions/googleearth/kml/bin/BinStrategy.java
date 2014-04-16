package gov.epa.emissions.googleearth.kml.bin;

import gov.epa.emissions.googleearth.kml.generator.BinRangeManager;

public interface BinStrategy {
	BinRangeManager createBins();
}
