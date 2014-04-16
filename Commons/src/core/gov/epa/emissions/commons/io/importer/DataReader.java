package gov.epa.emissions.commons.io.importer;

import gov.epa.emissions.commons.Record;
import gov.epa.emissions.commons.util.StringTools;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DataReader implements Reader {

    private BufferedReader fileReader;

    private List<String> comments;

    private Parser parser;
    
    private int lineNumber;

    private String currentLine;

    public DataReader(BufferedReader reader, int lineNumber, Parser parser) {
        fileReader = reader;
        this.lineNumber = lineNumber;
        this.parser = parser;
        comments = new ArrayList<String>();
    }

    public Record read() throws IOException {
        for (String line = fileReader.readLine(); !isEnd(line); line = fileReader.readLine()) {
            this.currentLine = line.trim();
            this.lineNumber++;
            
            if (isComment(currentLine)) {
                if (isExportInfo(currentLine))
                    continue; // rip off the export info lines
                
                comments.add(StringTools.escapeBackSlash(currentLine));
                continue;
            }
            
            if (currentLine.length() != 0)
                return parser.parse(currentLine);
        }

        return new TerminatorRecord();
    }

    private boolean isExportInfo(String line) {
        return line == null ? false : (line.trim().startsWith("#EXPORT_")); // || line.startsWith("#EMF_"));
    }
    
    private boolean isEnd(String line) {
        return line == null;
    }

    private boolean isComment(String line) {
        return line.trim().startsWith("#");
    }

    public List<String> comments() {
        return comments;
    }

    public void close() throws IOException {
       fileReader.close();
    }
    
    public int lineNumber() {
        return lineNumber;
    }

    public String line() {
        return currentLine;
    }

}
