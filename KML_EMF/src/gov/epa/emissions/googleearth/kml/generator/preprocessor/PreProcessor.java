package gov.epa.emissions.googleearth.kml.generator.preprocessor;

import gov.epa.emissions.googleearth.kml.KMZGeneratorException;
import gov.epa.emissions.googleearth.kml.generator.BinnedMultiGeometrySourceGenerator;
import gov.epa.emissions.googleearth.kml.generator.BinnedPointSourceGenerator;
import gov.epa.emissions.googleearth.kml.record.Record;

import java.util.List;

public interface PreProcessor {

	void preProcessRecords(BinnedPointSourceGenerator generator)
			throws KMZGeneratorException;

	void preProcessRecords(BinnedMultiGeometrySourceGenerator generator)
			throws KMZGeneratorException;
	
	List<Record> getRecords();

	List<Double> getValues();

	double getMaxValue();

	double getMinValue();

	double getMeanValue();
}