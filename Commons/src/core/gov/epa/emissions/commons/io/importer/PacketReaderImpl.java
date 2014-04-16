package gov.epa.emissions.commons.io.importer;

import gov.epa.emissions.commons.Record;
import gov.epa.emissions.commons.util.StringTools;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PacketReaderImpl implements PacketReader {

    private BufferedReader fileReader;

    private String header;

    private List<String> comments;

    private Parser parser;

    private int lineNumber;

    private String line;

    public PacketReaderImpl(BufferedReader reader, String headerLine, Parser parser, int lineNumber) {
        fileReader = reader;
        header = parseHeader(headerLine);
        this.parser = parser;
        this.lineNumber = lineNumber;
        comments = new ArrayList<String>();
        line = null;
    }

    private String parseHeader(String header) {
        Pattern p = Pattern.compile("/[a-zA-Z\\s]+/");
        Matcher m = p.matcher(header);
        if (m.find()) {
            return header.substring(m.start() + 1, m.end() - 1);
        }

        return null;
    }

    public String identify() {
        return header;
    }

    public Record read() throws IOException {
        for (String line = fileReader.readLine(); !isEnd(line); line = fileReader.readLine()) {
            this.line = line;
            if (isExportInfo(line))
                continue;          // rip off the export info lines
            if (isData(line))
                return parser.parse(line);
            if (isComment(line))
                comments.add(StringTools.escapeBackSlash(line));
        }

        return new TerminatorRecord();
    }

    private boolean isEnd(String line) {
        return (line == null) || (line.trim().equals("/END/"));
    }

    private boolean isData(String line) {
        return !(line.trim().length() == 0) && (!isComment(line));
    }

    private boolean isComment(String line) {
        return line == null ? false : line.trim().startsWith("#");
    }

    public List<String> comments() {
        return comments;
    }
    
    private boolean isExportInfo(String line) {
        return (line.trim().startsWith("#EXPORT_"));// || line.startsWith("#EMF_"));
    }

    public void close() {
        // no op
    }

    public int lineNumber() {
        return lineNumber;
    }

    public String line() {
        return line;
    }

}
