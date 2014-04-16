package gov.epa.emissions.commons.io.ida;

import gov.epa.emissions.commons.io.importer.ImporterException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class IDAHeaderReader {

    private BufferedReader reader;

    private IDAPollutantParser parser;

    public IDAHeaderReader(File file) throws ImporterException {
        reader = reader(file);
        parser = new IDAPollutantParser();

    }

    private BufferedReader reader(File file) throws ImporterException {
        try {
            return  new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            throw new ImporterException("File not found -" + e.getMessage());
        }
    }

    public String[] polluntants() {
        return parser.pollutants();
    }

    public void read() throws ImporterException {
        List comments = new ArrayList();
        String line = "";
        try {
            line = reader.readLine();
            while (line != null) {
                line = line.trim();
                if (isComment(line))
                    comments.add(line);
                else if (isData(line))
                    break;
                line = reader.readLine();
            }
            parser.processComments((String[]) comments.toArray(new String[0]));

        } catch (IOException e) {
            throw new ImporterException("Could not read the header: " + line);
        }
    }

    private boolean isComment(String line) {
        return line.length() != 0 && line.startsWith("#");
    }

    private boolean isData(String line) {
        return line.length() != 0 && !line.startsWith("#");
    }

    public List comments() {
        return parser.comments();

    }

    public void close() throws ImporterException {
        try {
            reader.close();
        } catch (IOException e) {
            throw new ImporterException("Error in closing IDA header reader", e);
        }
    }

}
