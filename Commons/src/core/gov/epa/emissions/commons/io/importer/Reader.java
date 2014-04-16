package gov.epa.emissions.commons.io.importer;

import gov.epa.emissions.commons.Record;

import java.io.IOException;
import java.util.List;

public interface Reader {

    Record read() throws IOException, ImporterException;

    List<String> comments();

    void close() throws IOException;

    int lineNumber();
    
    String line();
    
}