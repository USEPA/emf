package gov.epa.emissions.framework.services.cost.controlmeasure.io;

import gov.epa.emissions.commons.Record;
import gov.epa.emissions.commons.io.CustomCharSetInputStreamReader;
import gov.epa.emissions.commons.io.importer.CommaDelimitedTokenizer;
import gov.epa.emissions.commons.io.importer.ImporterException;
import gov.epa.emissions.commons.io.importer.Reader;
import gov.epa.emissions.commons.io.importer.TerminatorRecord;
import gov.epa.emissions.commons.io.importer.Tokenizer;
import gov.epa.emissions.commons.util.StringTools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CMCSVFileReader implements Reader {

    private static Log log = LogFactory.getLog(CMCSVFileReader.class);

    private BufferedReader fileReader;

    private List<String> comments;

    private Tokenizer tokenizer;

    private int lineNumber;

    private String line;

    private String[] cols;

    private List<String> header;

    public CMCSVFileReader(File file) throws ImporterException {
        try {
//            fileReader = new BufferedReader(new FileReader(file));
            fileReader = new BufferedReader(new CustomCharSetInputStreamReader(new FileInputStream(file)));
            tokenizer = new CommaDelimitedTokenizer();
            comments = new ArrayList<String>();
            this.lineNumber = 0;
            cols = read().getTokens();
        } catch (FileNotFoundException e) {
            log.error("Importer failure: File not found" + "\n" + e);
            throw new ImporterException("Importer failure: File not found");
        } catch (UnsupportedEncodingException e) {
            log.error("Importer failure: encoding char set not supported" + "\n" + e);
            throw new ImporterException("Importer failure: encoding char set not supported");
        }
    }

    public void close() throws IOException {
        fileReader.close();
    }

    public Record read() throws ImporterException {
        try {
            String line = fileReader.readLine();

            while (line != null) {
                lineNumber++;
                this.line = line;
                if (isData(line)) {
                    //next lets see if the line continues on to the next line, could have new line in the column text
//look for what is in between the last comma in the line and hte end of the line.
                    //  should be:
                    //  ,
                    //  ,""
                    //if quote count is one or more than two
                    int count = line.replaceAll("[^\"]","").length();
                    if (count != 0 && count % 2 != 0) {
                        String nextLine = fileReader.readLine();
                        while (nextLine != null) {
                            line += "\n" + nextLine;
                            count = nextLine.replaceAll("[^\"]","").length();
                            if (count == 0 && count % 2 == 0) {
                                nextLine = fileReader.readLine();
                            } else
                                nextLine = null;
                        }
                    }
                    return doRead(line);
                }
                if (isComment(line))
                    comments.add(StringTools.escapeBackSlash(line));
    
                line = fileReader.readLine();
            }

        } catch (IOException e) {
            log.error("Importer failure: Error reading file" + "\n" + e);
            throw new ImporterException("Importer failure: Error reading file");
        }
        return new TerminatorRecord();
    }

    private boolean isData(String line) {
        return !(line.trim().length() == 0) && (!isComment(line));
    }

    private Record doRead(String line) throws ImporterException {
        Record record = new Record();
        String[] tokens = tokenizer.tokens(line);
//        List<String> stringList = new ArrayList<String>();
        for (int i = 0; i < tokens.length - 1; i++) {
//            stringList.add(tokens[i]);
            
            tokens[i] = tokens[i].replace("\"\"", "\"");
        }
        record.add(Arrays.asList(tokens));
        record.token(1);
        return record;
    }

    private boolean isComment(String line) {
        return line.startsWith("#");
    }

    public List<String> comments() {
        return comments;
    }

    public int lineNumber() {
        return lineNumber;
    }

    public String line() {
        return line;
    }

    public List<String> getHeader() {
        return header;
    }

    public String[] getCols() {
        return cols;
    }

}
