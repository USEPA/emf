package gov.epa.emissions.framework.services.cost.controlStrategy.io;

import gov.epa.emissions.commons.Record;
import gov.epa.emissions.commons.io.importer.ImporterException;

import java.io.File;
import java.util.ArrayList;

public class CSCountyImporter {

    private File file;

    private CSCountyRecordReader csCountyRecord;
    
    private ArrayList fips;
    
    public CSCountyImporter(File file, CSCountyFileFormat fileFormat) {
        this.file = file;
        this.fips = new ArrayList();
        csCountyRecord = new CSCountyRecordReader(fileFormat);
    }

    public String[] run() throws ImporterException {
        CSCSVFileReader reader = new CSCSVFileReader(file);
        String[] headers = reader.getCols();
        int index = 0;
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].equalsIgnoreCase("FIPs")) index = i;
        }
        csCountyRecord.setFIPsColIndex(index);
        for (Record record = reader.read(); !record.isEnd(); record = reader.read()) {
            fips.add(csCountyRecord.parse(record, reader.lineNumber()));
        }
        return (String[]) fips.toArray(new String[0]);
    }
}