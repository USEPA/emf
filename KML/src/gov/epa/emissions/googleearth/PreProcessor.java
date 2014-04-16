package gov.epa.emissions.googleearth;

import gov.epa.emissions.googleearth.kml.record.Record;

import java.util.List;

public interface PreProcessor {

	double getMaxValue();

	double getMinValue();

	List<Record> getRecords();

	int getRecordCount();

	void process();
}
