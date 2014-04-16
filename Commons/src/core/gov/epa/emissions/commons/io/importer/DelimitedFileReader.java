package gov.epa.emissions.commons.io.importer;

import gov.epa.emissions.commons.Record;
import gov.epa.emissions.commons.io.CustomCharSetInputStreamReader;
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
import java.util.regex.Pattern;

public class DelimitedFileReader implements Reader {

    protected BufferedReader fileReader;

    private List<String> comments;

    private Tokenizer tokenizer;

    private int lineNumber;

    private String line;

    private String[] inLineComments;

    public DelimitedFileReader(File file, Tokenizer tokenizer) throws FileNotFoundException {
        this(file, new String[] { "#" }, tokenizer);
    }

    public DelimitedFileReader(File file, String[] inLineComments, Tokenizer tokenizer) throws FileNotFoundException {
        // fileReader = new BufferedReader(new FileReader(file));
        try {
            fileReader = new BufferedReader(new CustomCharSetInputStreamReader(new FileInputStream(file)));
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("File not found: " + e.getMessage());
        } catch (UnsupportedEncodingException e) {
            throw new FileNotFoundException("Unsupported char set encoding: " + e.getMessage());
        }

        comments = new ArrayList<String>();
        this.tokenizer = tokenizer;
        this.inLineComments = inLineComments;
        this.lineNumber = 0;
    }

    public void close() throws IOException {
        fileReader.close();
    }

    public Record readOneLine() throws Exception {
        return doRead(fileReader.readLine());
    }

    public Record read() throws IOException, ImporterException {
        String line = fileReader.readLine();

        while (line != null) {
            lineNumber++;
            this.line = line;
            if (isExportInfo(line)) {
                line = fileReader.readLine();
                continue;
            }
            if (isData(line))
                return doRead(line);
            if (isComment(line))
                comments.add(StringTools.escapeBackSlash(line));

            line = fileReader.readLine();
        }

        return new TerminatorRecord();
    }

    private boolean isData(String line) {
        return !(line.trim().length() == 0) && (!isComment(line));
    }

    private boolean isExportInfo(String line) {
        return line == null ? false : (line.trim().startsWith("#EXPORT_"));// || line.startsWith("#EMF_"));
    }

    private Record doRead(String line) throws ImporterException {
        Record record = new Record();
        if (line.indexOf('!') == -1) {
            String[] tokens = tokenizer.tokens(line);
            record.add(Arrays.asList(tokens));

            return record;
        }

        int inlineCommentPosition = getInlineCommentPosition('!', line);
        String[] tokens = tokenizer.tokens(line.substring(0, inlineCommentPosition));
        record.add(Arrays.asList(tokens));

        if (inlineCommentPosition < line.length())
            record.add(processInlineComment(line.substring(inlineCommentPosition)));

        return record;

    }

    private int getInlineCommentPosition(char commentChar, String line) {
        int position = 0, index = 0, theEnd = line.length();
        String temp = line;

        while ((index = temp.indexOf(commentChar)) != -1) {
            position += index;

            if (!hasEvenNumOfQuotes(line.substring(0, position))) {
                temp = temp.substring(++index);
                position++;
            } else
                return position;
        }

        return theEnd;
    }

    private boolean hasEvenNumOfQuotes(String token) {
        int doubleQuotesCount = getQuotesCount("\"", token);

        return (doubleQuotesCount % 2 == 0); // && (singleQuotesCount % 2 == 0);
    }

    private int getQuotesCount(String quote, String token) {
        int index;
        int count = 0;
        String temp = token;

        while ((index = temp.indexOf(quote)) != -1) {
            ++count;
            temp = temp.substring(++index);
        }

        return count;
    }

    private String processInlineComment(String comment) {
        comment = StringTools.escapeBackSlash4jdbc(comment);
        
        return comment.startsWith("!") ? comment : "!" + comment;
    }

    private boolean isComment(String line) {
        for (int i = 0; i < inLineComments.length; i++) {
            if (line.startsWith(inLineComments[i]))
                return true;
        }
        return false;
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

    public String delimiter() {
        return tokenizer.delimiter();
    }

    // Added to remove header lines
    public String[] readHeader(int numLines) throws IOException {
        List<String> header = new ArrayList<String>();
        for (int i = 0; i < numLines; i++) {
            header.add(StringTools.escapeBackSlash(fileReader.readLine()));
        }

        return header.toArray(new String[0]);
    }

    // Added to remove header lines, especially for smk report files
    // Very specific function, make sure it fits into your file format!!!
    public String[] readHeader(String regex) throws IOException {
        List<String> header = new ArrayList<String>();
        String line = fileReader.readLine();
        Pattern pattern = Pattern.compile(regex);

        line = readHeaderLines(header, line, pattern);
        header.add(line); // Table header - Column Names or units
        header.add(fileReader.readLine()); // column names or units

        if (line.startsWith("#"))
            return header.toArray(new String[0]);

        line = fileReader.readLine();

        if (line.startsWith("-----")) {
            header.add(line); // one more line of table border
        } else {
            fileReader.mark(1);
            fileReader.reset(); // file reader goes back 1 line
        }

        return header.toArray(new String[0]);
    }

    private String readHeaderLines(List<String> header, String line, Pattern pattern) throws IOException {
        while (pattern.split(line).length < 3) {
            if (!isExportInfo(line))
                header.add(StringTools.escapeBackSlash(line));
            
            line = fileReader.readLine();
        }
        return line;
    }

}
