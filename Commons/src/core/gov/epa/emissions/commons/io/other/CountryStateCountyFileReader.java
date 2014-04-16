package gov.epa.emissions.commons.io.other;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gov.epa.emissions.commons.Record;
import gov.epa.emissions.commons.io.importer.PacketReader;
import gov.epa.emissions.commons.io.importer.Parser;
import gov.epa.emissions.commons.io.importer.TerminatorRecord;
import gov.epa.emissions.commons.util.StringTools;

public class CountryStateCountyFileReader implements PacketReader {
    private BufferedReader fileReader;

    private String header;

    private List<String> comments;

    private Parser parser;

    private int lineNumber;

    private String line;

    public CountryStateCountyFileReader(BufferedReader reader, int lineNumber, String headerLine, Parser parser) {
        fileReader = reader;
        this.lineNumber = lineNumber;
        header = parseHeader(headerLine);
        this.parser = parser;
        comments = new ArrayList<String>();
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
        for (String line = fileReader.readLine(); !isEnd(line, fileReader); line = fileReader.readLine()) {
            this.line = line;
            if (isExportInfo(line))
                continue;
            if (isData(line)) {
                // Country State County data file has less than 255 characters
                // in a line
                fileReader.mark(255);
                return parser.parse(line);
            }
            if (isComment(line))
                comments.add(StringTools.escapeBackSlash(line));
        }

        return new TerminatorRecord();
    }

    private boolean isEnd(String line, BufferedReader reader) throws IOException {
        if (line == null)
            return true;

        if (line.trim().startsWith("/")) {
            // Country State County data file is a special packet
            // file that it dosen't have ending structure like "/END/"
            reader.reset();
            return true;
        }

        return false;
    }

    private boolean isData(String line) {
        return !(line.trim().length() == 0) && (!isComment(line));
    }
    
    private boolean isExportInfo(String line) {
        return line == null ? false : (line.trim().startsWith("#EXPORT_"));// || line.startsWith("#EMF_"));
    }

    private boolean isComment(String line) {
        return line.trim().startsWith("#");
    }

    public List<String> comments() {
        return comments;
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
