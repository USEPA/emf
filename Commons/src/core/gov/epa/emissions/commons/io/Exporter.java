package gov.epa.emissions.commons.io;

import java.io.File;

public interface Exporter {
    void export(File file) throws ExporterException;
    
    long getExportedLinesCount();
}
