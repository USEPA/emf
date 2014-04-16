package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.commons.Record;
import gov.epa.emissions.commons.io.importer.CommaDelimitedTokenizer;
import gov.epa.emissions.commons.io.importer.DelimitedFileReader;
import gov.epa.emissions.commons.io.importer.FileVerifier;
import gov.epa.emissions.commons.io.importer.Reader;
import gov.epa.emissions.framework.services.cost.controlmeasure.Scc;
import gov.epa.emissions.framework.services.cost.controlmeasure.Sccs;

import java.io.File;
import java.util.List;

public class SccFileReader {

    private Reader reader;

    private Sccs sccs;

    private String fileName;

    public SccFileReader(String fileName, Sccs sccs) throws Exception {
        this.fileName = fileName;
        reader = reader(fileName);
        this.sccs = sccs;
    }

    public void read() throws Exception {
        reader.read();// disregard the first line-column header
        Record record =reader.read();
        while (!record.isEnd()) {
            sccs.addScc(scc(record));
            record = reader.read();
        }
    }

    private Scc scc(Record record) throws Exception {
        List list = record.tokens();
        validate(list);
        String[] values = (String[]) list.toArray(new String[0]);

        return new Scc(values[0], values[14], values[1], values[2], values[3], values[4], values[5], values[6], values[7], values[8], values[9], values[10], values[11], values[12], values[13]);
    }

    private void validate(List list) throws Exception {
        if (list.size() > 15 || list.size() == 0) {
            throw new Exception("The SCC file name '" + fileName
                    + "' has more than fifteen tokens in line " + reader.lineNumber());
        }
        
        if (list.size() == 1)
            list.add(""); // list.size()==1

    }

    private Reader reader(String fileName) throws Exception {
        File file = new File(fileName);
        verifyFile(file);

        return new DelimitedFileReader(new File(fileName), new CommaDelimitedTokenizer());
    }

    private void verifyFile(File file) throws Exception {
        new FileVerifier().shouldExist(file);
    }

}
