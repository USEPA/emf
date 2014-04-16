package gov.epa.emissions.commons.io.importer;

import gov.epa.emissions.commons.Record;

public interface Parser {

    Record parse(String line);

}