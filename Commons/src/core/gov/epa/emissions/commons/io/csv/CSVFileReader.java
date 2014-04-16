package gov.epa.emissions.commons.io.csv;

import gov.epa.emissions.commons.Record;
import gov.epa.emissions.commons.db.PostgreSQLKeyWords;
import gov.epa.emissions.commons.io.CustomCharSetInputStreamReader;
import gov.epa.emissions.commons.io.importer.CommaDelimitedTokenizer;
import gov.epa.emissions.commons.io.importer.ImporterException;
import gov.epa.emissions.commons.io.importer.Reader;
import gov.epa.emissions.commons.io.importer.TerminatorRecord;
import gov.epa.emissions.commons.io.importer.Tokenizer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CSVFileReader implements Reader {

    private static Log log = LogFactory.getLog(CSVFileReader.class);

    private BufferedReader fileReader;

    private List<String> comments;

    private Tokenizer tokenizer;

    private int lineNumber;

    private String currentLine;

    private String[] cols = new String[] {};
    
    private String[] colTypes;

    private List<String> header;

    private File file;

    private String[] existedCols = { "Record_Id", "Dataset_Id", "Version", "Delete_Versions", "Comments", "DESC" };

    private String inlineCommentDelimiter = null;

    private String[] systemDefinedColNames = null;
    
    public CSVFileReader(File file) throws ImporterException {
        this(file, null, null);
    }

    public CSVFileReader(File file, String inlineCommentDelimiter, String[] systemDefinedColNames) throws ImporterException {
        try {
            fileReader = new BufferedReader(new CustomCharSetInputStreamReader(new FileInputStream(file)));
            comments = new ArrayList<String>();
            header = new ArrayList<String>();
            this.file = file;
            this.lineNumber = 0;
            this.inlineCommentDelimiter = inlineCommentDelimiter;
            this.systemDefinedColNames  = systemDefinedColNames;
            //override this setting with system defined col names
            this.cols = systemDefinedColNames;
            detectDelimiter();
        } catch (FileNotFoundException e) {
            log.error("Importer failure: File not found" + "\n" + e);
            throw new ImporterException("Importer failure: File not found");
        } catch (UnsupportedEncodingException e) {
            log.error("Importer failure: character set encoding not supported" + "\n" + e);
            throw new ImporterException("Importer failure: character set encoding not supported");
        }
    }
    
    public void close() throws IOException {
        fileReader.close();
    }

    public Record read() throws ImporterException {
        try {
            String line = null;

            while ((line = fileReader.readLine()) != null) {
                lineNumber++;
                this.currentLine = line.trim();
                
                if (isComment(currentLine)) {
                    if (isExportInfo(currentLine))
                        continue; // rip off the export info lines
                    
                    comments.add(checkBackSlash(currentLine));
                    continue;
                }

                if (currentLine.length() != 0)
                    return doRead(line);
            }

        } catch (IOException e) {
            log.error("Importer failure: Error reading file" + "\n" + e);
            throw new ImporterException("Importer failure: Error reading file");
        }
        return new TerminatorRecord();
    }

    private boolean isExportInfo(String line) {
        return line == null ? false : (line.trim().startsWith("#EXPORT_")); // || line.startsWith("#EMF_"));
    }
    
    private boolean isColTypes(String line) {
        return line == null ? false : (line.trim().startsWith("#COLUMN_TYPES"));
    }

    private int getInlineCommentPosition(String commentChar, String line) {
        int position = 0, index = 0, theEnd = line.length();
        
        //don't look if there is no commentDelimiter specified
        if (commentChar == null || commentChar.isEmpty())
            return theEnd;
        
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

    private Record doRead(String line) throws ImporterException {
        Record record = new Record();
        String[] tokens = null;
        String inlineComment = null;
        
        try {
            //look for comment, if exists lets strip it off
            int inlineCommentPosition = getInlineCommentPosition(inlineCommentDelimiter, line);
            if (inlineCommentPosition < line.length()) {
                inlineComment = line.substring(inlineCommentPosition + 1);
                tokens = tokenizer.tokens(line.substring(0, inlineCommentPosition));
            } else {
                tokens = tokenizer.tokens(line);
            }
        } catch (Exception e) {
            String err = e.getMessage();
            
            if (err != null && err.toLowerCase().contains("no match available"))
                throw new ImporterException("Line " + lineNumber + " has unmatched quotes.");
            
            throw new ImporterException("Line " + lineNumber + " has format errors." );
        }
        
        if (tokens.length < cols.length) {
            throw new ImporterException("Line " + lineNumber + " has too few tokens or unbalanced quotes.");
        }
        
        if (tokens.length > cols.length) {
            throw new ImporterException("Line " + lineNumber + " has too many tokens.");
        }
        
        for (int i = 0; i < tokens.length; i++) {
            if (!isDoubleQuotesBalanced(tokens[i]))
                throw new ImporterException("Line " + lineNumber + " has unbalanced quotes.");
            
            tokens[i] = checkBackSlash(tokens[i]);
        }
        
        record.add(Arrays.asList(tokens));

        if (inlineComment != null)
            record.add(inlineComment);
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
        return currentLine;
    }

    public List<String> getHeader() {
        return header;
    }
    
    public String headerToString(){
        String lineFeeder = System.getProperty("line.separator");
        String output = "";
        if (header != null){
            for (int i=0; i<header.size(); i++)
                output += header.get(i) + lineFeeder;
        }
        return  output;  
    }

    public String[] getCols() {
        return cols;
    }
    
    public String colsToString(){
        String lineFeeder = ",";
        String output = "";
        if (cols != null){
            for (int i=0; i<cols.length; i++)
                output += cols[i] + lineFeeder;
        }
        return  output;  
    }
    
    public String colTypesToString(){
        String lineFeeder = ",";
        String output = "";
        if (colTypes != null){
            for (int i=0; i<colTypes.length; i++)
                output += colTypes[i] + lineFeeder;
        }
        return  output;  
    }
    
    public String[] getColTypes() {
        return colTypes;
    }

    private void detectDelimiter() throws ImporterException {
        if (file.length() == 0)
            throw new ImporterException("File: " + file.getAbsolutePath() + " is empty.");

            if (getTokenizer())
                return;
    }

    private boolean getTokenizer() throws ImporterException {
        //for system defined column names, assume a CommaDelimitedTokenizer for the file!
        //don't parse first line or it will be lost when parsing the data when there are no
        //column labels
        if (systemDefinedColNames != null && systemDefinedColNames.length > 0) {
            if (systemDefinedColNames.length < 2)
                throw new ImporterException("At least 2 columns are required.");

            tokenizer = new CommaDelimitedTokenizer();
            return true;
        }
        
        //At this point try and parse the delimiter and assume first normal file 
        //line is the column names.
        try {
            
            String lineRead = fileReader.readLine();
            
            for (; lineRead != null; lineRead = fileReader.readLine()) {
                lineNumber++;
                
                if (isExportInfo(lineRead))
                    continue;
                else if (isComment(lineRead)) {
                    header.add(checkBackSlash(lineRead));
                    comments.add(checkBackSlash(lineRead));
                    if (isColTypes(lineRead)){
                        setColTypes(lineRead);
                    }
                } else if (lineRead.split(",").length < 2) {
                    throw new ImporterException("At least 2 columns are required.");
                } else if (lineRead.split(",").length >= 2)
                    tokenizer = new CommaDelimitedTokenizer();

                if (tokenizer != null) {
                    cols = underScoreTheSpace(tokenizer.tokens(lineRead));
                    return true;
                }
            }
        } catch (IOException e) {
            log.error("Importer failure: Error reading file" + "\n" + e);
            throw new ImporterException("Importer failure: Error reading file");
        }

        return false;
    }
    
    private void setColTypes(String lineRead) throws ImporterException{
        List<String> columnTypes = new ArrayList<String>();
        int index = lineRead.indexOf("=");
        if (index < 0)
            throw new ImporterException(
                    "Column types line format is not correct. The correct format is: #TYPES=[type_1] [type_2] ... [type_n]");

        StringTokenizer st = new StringTokenizer(lineRead.substring(++index), "|");

        while (st.hasMoreTokens())
            columnTypes.add(st.nextToken());
    
        colTypes = columnTypes.toArray(new String[0]);
    }

    private String[] underScoreTheSpace(String[] cols) {
        for (int i = 0; i < cols.length; i++) {
            String temp = cols[i].replace(' ', '_');
            temp = (PostgreSQLKeyWords.reserved(temp.toUpperCase())) ? temp + "_" : temp;
            
            for (int j = 0; j < temp.length(); j++) {
                if (!Character.isLetterOrDigit(temp.charAt(j))) {
                    temp = temp.replace(temp.charAt(j), '_');
                }
            }
            
            cols[i] = checkExistCols(temp);
        }

        return cols;
    }

    private String checkExistCols(String col) {
        for (int i = 0; i < existedCols.length; i++)
            if (col.equalsIgnoreCase(existedCols[i]))
                col += "_XXX";
        return col;
    }

    private String checkBackSlash(String col) {
        return col.replaceAll("\\\\", "\\\\\\\\");
    }
    
    private boolean isDoubleQuotesBalanced(String token) {
        int count = 0;
        int len = token.length();
        int i = token.indexOf('"');
        
        if (i < 0)
            return true;
        
        for (; i < len;) {
            count++;
            i++;
            
            if (i == len)
                break;
            
            i = token.indexOf('"', i);
            
            if (i < 0)
                break;
        }
        
        return count % 2 == 0;
    }

}
