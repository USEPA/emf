package gov.epa.emissions.commons.io.importer;

import gov.epa.emissions.commons.Record;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.FileFormat;
import gov.epa.emissions.commons.util.StringTools;

public class FixedWidthParser implements Parser {

    protected FileFormat fileFormat;

    public FixedWidthParser(FileFormat fileFormat) {
        this.fileFormat = fileFormat;
    }

    public Record parse(String line) {
        Record record = new Record();
        addTokens(line, record, fileFormat.cols());

        return record;
    }

    private void addTokens(String inLine, Record record, Column[] columns) {
        int numCols = columns.length, i;
        String[] bipart = splitLineByInlineComment(inLine);
        String left = bipart[0];

        for (i = 0; i < numCols; i++) {
            if (left.length() < columns[i].width()) {
                String data = left.substring(0);
                record.add(data);
                break;
            }

            String data = left.substring(0, columns[i].width());
            left = left.substring(columns[i].width());

            record.add(data);
        }

        // If line ends without specified column values, we need to put blank
        // string there
        while (i < numCols - 1) {
            record.add("");
            i++;
        }

        if (bipart[1].length() > 0) // add inline comment if there is one
            record.add(StringTools.escapeBackSlash4jdbc(bipart[1]));
    }

    private String[] splitLineByInlineComment(String line) {
        String[] bipart = { line, "" };
        int bang = line.indexOf('!');

        if (bang >= 0) {
            bipart[0] = line.substring(0, bang);
            bipart[1] = line.substring(bang);
        }

        return bipart;
    }

}
