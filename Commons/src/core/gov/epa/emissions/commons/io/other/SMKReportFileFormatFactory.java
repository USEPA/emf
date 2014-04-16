package gov.epa.emissions.commons.io.other;

import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.FileFormat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Pattern;

public class SMKReportFileFormatFactory {

    private BufferedReader reader;

    private SqlDataTypes types;

    private String[] cols;

    private String delimiter;

    public SMKReportFileFormatFactory(File file, SqlDataTypes types) throws IOException {
        this.reader = new BufferedReader(new FileReader(file));
        this.types = types;
        this.delimiter = readHeader();
    }

    public FileFormat getFormat() throws Exception {
        return new SMKReportFileFormat(cols, types);
    }

    public String getDelimiter() {
        return delimiter;
    }

    private String readHeader() throws IOException {
        String comma = ",";
        String semicolon = ";";
        String pipe = "|";
        Pattern bar = Pattern.compile("[|]");
        String localDelimiter = null;

        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
            if (line.split(comma).length >= 3) {
                localDelimiter = comma;
            } else if (line.split(semicolon).length >= 3) {
                localDelimiter = semicolon;
            } else if (bar.split(line).length >= 3) {
                localDelimiter = pipe;
            }
            
            if (line.trim().startsWith("#") && localDelimiter != null) {
                line = reader.readLine();
                
                if (line != null) {
                    line = line.trim().substring(1); // get rid of leading # sign
                    fillColNames(localDelimiter, line);
                    reader.close();
                    
                    return localDelimiter;
                }
            }

            if (localDelimiter != null) {
                fillColNames(localDelimiter, line);
                reader.close();
                return localDelimiter;
            }
        }

        reader.close();
        return localDelimiter;
    }
    
    private void fillColNames(String delimiter, String line) {
        if (delimiter.equals("|")) {
            Pattern bar = Pattern.compile("[|]");
            cols = bar.split(line);
            return;
        }
        
        cols = line.split(delimiter);
    }
    
}
