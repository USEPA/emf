package gov.epa.emissions.framework.services.cost.controlmeasure.io;

import gov.epa.emissions.commons.Record;
import gov.epa.emissions.commons.data.Reference;
import gov.epa.emissions.commons.io.importer.ImporterException;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.util.Map;

public class CMReferenceRecordReader {

    private CMReferenceFileFormat fileFormat;

    private CMAddImportStatus status;

    private int errorCount = 0;

    private int errorLimit = 100;

    public CMReferenceRecordReader(CMReferenceFileFormat fileFormat, User user, HibernateSessionFactory sessionFactory) {

        this.fileFormat = fileFormat;
        this.status = new CMAddImportStatus(user, sessionFactory);
    }

    public void parse(Map<Integer, Reference> references, Record record, int lineNo) throws ImporterException {

        StringBuffer sb = new StringBuffer();
        String[] tokens = modify(record, sb, lineNo);
        if (tokens == null) {
            return;
        }

        int id = Integer.valueOf(tokens[0]);        
        String description = tokens[1];

        Reference reference = new Reference(id, description);        
        references.put(reference.getId(), reference);

        if (sb.length() > 0) {
            errorCount++;
            status.addStatus(lineNo, sb);
        }

        if (errorCount >= errorLimit) {
            throw new ImporterException("The maximum allowable error limit (" + errorLimit
                    + ") has been reached while parsing the control measure reference records.");
        }
    }

    private String[] modify(Record record, StringBuffer sb, int lineNo) throws ImporterException {

        String[] tokens = record.getTokens();

        int ffLength = fileFormat.cols().length;
        int tLength = tokens.length;
        int sizeDiff = ffLength - tLength;

        if (sizeDiff == 0) {
            return tokens;
        }

        if (sizeDiff > 0) {

            for (int i = 0; i < sizeDiff; i++) {
                record.add("");
            }

            return tokens;
        }

        throw new ImporterException("The new reference record has extra tokens");
    }

    public int getErrorCount() {
        return errorCount;
    }
}
