package gov.epa.emissions.commons.io.importer;

import gov.epa.emissions.commons.Record;
import gov.epa.emissions.commons.io.FileFormat;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

public class FixedWidthPacketReader implements PacketReader {

    private PacketReaderImpl delegate;

    public FixedWidthPacketReader(BufferedReader reader, String headerLine, FileFormat fileFormat, int lineNumber) {
        delegate = new PacketReaderImpl(reader, headerLine, new FixedWidthParser(fileFormat), lineNumber);
    }

    public Record read() throws IOException {
        return delegate.read();
    }

    public List<String> comments() {
        return delegate.comments();
    }

    public String identify() {
        return delegate.identify();
    }

    public void close() {
        delegate.close();
    }

    public int lineNumber() {
        return delegate.lineNumber();
    }

    public String line() {
        return delegate.line();
    }
}
