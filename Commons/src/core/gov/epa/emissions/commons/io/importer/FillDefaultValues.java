package gov.epa.emissions.commons.io.importer;

import gov.epa.emissions.commons.io.FileFormatWithOptionalCols;

import java.util.List;

public interface FillDefaultValues {

    void fill(FileFormatWithOptionalCols format, List data, long datasetId);

}