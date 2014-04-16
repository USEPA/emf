package gov.epa.emissions.commons.io.other;

import gov.epa.emissions.commons.Record;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.FileFormat;
import gov.epa.emissions.commons.io.importer.FixedWidthParser;

public class COSTCYParser extends FixedWidthParser {

    private String formatId;

    private int numOfCols;

    private Column[] columns;

    public COSTCYParser(FileFormat fileFormat) {
        super(fileFormat);
        this.formatId = fileFormat.identify().toUpperCase();
        this.columns = fileFormat.cols();
        this.numOfCols = columns.length;
    }

    public Record parse(String line) {
        return fillBlanksForCountryStateCode(super.parse(line));
    }

    private Record fillBlanksForCountryStateCode(Record record) {
        String[] tokens = record.getTokens();

        for (int i = 0; i < numOfCols; i++) {
            if (isBlankCountryStateCode(tokens[i], i))
                tokens[i] = "" + 0;
        }

        record.setTokens(tokens);

        return record;
    }

    private boolean isBlankCountryStateCode(String token, int col) {

        if (token != null && !token.trim().isEmpty())
            return false;

        // NOTE: Slower method but more robust
        // String colName = columns[col].name();
        //
        // if (colName.equalsIgnoreCase("CODE") || colName.equalsIgnoreCase("COUNTRYCODE")
        // || colName.equalsIgnoreCase("STATECODE"))
        // return true;

        // NOTE: Faster but more hard-wired, needs to modify if fileformats change
        if ((formatId.equals("COUNTRY") && col == 0) || (formatId.equals("STATE") && (col == 0 || col == 1))
                || (formatId.equals("COUNTY") && (col == 5 || col == 6)))
            return true;

        return false;
    }
}
