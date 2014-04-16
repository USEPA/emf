package gov.epa.emissions.googleearth;

import gov.epa.emissions.googleearth.kml.KMZGeneratorException;
import gov.epa.emissions.googleearth.kml.record.StreetRecord;

public interface StreetRecordProducer {
	StreetRecord nextRecord() throws KMZGeneratorException;

	String[] getColumnHeader();
}
