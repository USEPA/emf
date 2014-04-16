package gov.epa.emissions.googleearth;

import gov.epa.emissions.googleearth.kml.KMZGeneratorException;
import gov.epa.emissions.googleearth.kml.record.Record;

public interface RecordProducer {

	/**
	 * Gets the next record, returning null if no more records.
	 * 
	 * @return the next record, or null
	 */
	Record nextRecord() throws KMZGeneratorException;

	String[] getColumnHeader();
}
