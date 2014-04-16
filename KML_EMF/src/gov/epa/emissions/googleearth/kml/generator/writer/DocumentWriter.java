package gov.epa.emissions.googleearth.kml.generator.writer;

import gov.epa.emissions.googleearth.kml.KMZGeneratorException;
import gov.epa.emissions.googleearth.kml.generator.BinnedMultiGeometrySourceGenerator;
import gov.epa.emissions.googleearth.kml.generator.BinnedPointSourceGenerator;

import java.io.File;
import java.io.IOException;
import java.io.Writer;

public interface DocumentWriter {

	void writeDocument(Writer writer, File inputFile,
			BinnedPointSourceGenerator generator) throws IOException,
			KMZGeneratorException;
	
	void writeDocument(Writer writer, File inputFile,
			BinnedMultiGeometrySourceGenerator generator) throws IOException,
			KMZGeneratorException;
}
