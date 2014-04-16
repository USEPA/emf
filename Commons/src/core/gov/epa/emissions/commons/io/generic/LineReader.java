package gov.epa.emissions.commons.io.generic;

import gov.epa.emissions.commons.Record;
import gov.epa.emissions.commons.io.CustomCharSetInputStreamReader;
import gov.epa.emissions.commons.io.importer.Reader;
import gov.epa.emissions.commons.io.importer.TerminatorRecord;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class LineReader implements Reader {

    private BufferedReader fileReader;

    private List<String> comments;

    private String line;

    private int lineNumber;

    public LineReader(File file) throws FileNotFoundException {
        //fileReader = new BufferedReader(new FileReader(file));
        try {
            fileReader = new BufferedReader(new CustomCharSetInputStreamReader(new FileInputStream(file)));
        } catch (UnsupportedEncodingException e) {
            throw new FileNotFoundException("Encoding char set not supported.");
        }
        comments = new ArrayList<String>();
    }

    public void close() throws IOException {
        fileReader.close();
    }

    public Record read() throws IOException {
        String line = fileReader.readLine();

        while (isExportInfo(line)) {
            line = fileReader.readLine(); // rip off the export info lines
            lineNumber++;
        }
        
        if (line != null) {
            this.line = line;
            Record record = new Record();
            lineNumber++;
            record.add("" + lineNumber);
            record.add(checkBackSlash(line));
            return record;
        }
        return new TerminatorRecord();
    }

    public List<String> comments() {
        return comments;
    }

    private boolean isExportInfo(String line) {
        return line == null ? false : (line.trim().startsWith("#EXPORT_"));// || line.startsWith("#EMF_"));
    }
    
    public int lineNumber() {
        return lineNumber;
    }

    public String line() {
        return line;
    }
    
    private String checkBackSlash(String col) {
        return col.replaceAll("\\\\", "\\\\\\\\");
    }
}
