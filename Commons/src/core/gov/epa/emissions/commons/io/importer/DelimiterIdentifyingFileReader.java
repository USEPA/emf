package gov.epa.emissions.commons.io.importer;

import gov.epa.emissions.commons.Record;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

public class DelimiterIdentifyingFileReader implements Reader {

    private DelimitedFileReader reader;
    
    public DelimiterIdentifyingFileReader(File file, int minTokens) throws FileNotFoundException {
        DelimiterIdentifyingTokenizer tokenizer = new DelimiterIdentifyingTokenizer(minTokens);
        reader = new DelimitedFileReader(file, tokenizer);
    }

    public DelimiterIdentifyingFileReader(File file, String[] inlineComments, int minTokens)
            throws FileNotFoundException {
        DelimiterIdentifyingTokenizer tokenizer = new DelimiterIdentifyingTokenizer(minTokens);
        reader = new DelimitedFileReader(file, inlineComments, tokenizer);
    }

    public void close() throws IOException {
        reader.close();
    }

    public Record read() throws IOException, ImporterException {
        return reader.read();
    }

    public List<String> comments() {
        return reader.comments();
    }

    public int lineNumber() {
        return reader.lineNumber();
    }

    public String line() {
        return reader.line();
    }
    
    public String delimiter() {
        return reader.delimiter();
    }

}
