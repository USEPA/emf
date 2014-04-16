package gov.epa.emissions.framework.services.cost.controlmeasure.io;

import gov.epa.emissions.commons.Record;
import gov.epa.emissions.commons.io.importer.ImporterException;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.ControlMeasureProperty;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.util.Map;

public class CMPropertyRecordReader {

    private CMPropertyFileFormat fileFormat;

    private CMAddImportStatus status;
    
    private int errorCount = 0;
    
    private int errorLimit = 100;
    
//    private HibernateSessionFactory sessionFactory;

    private ControlMeasurePropertyCategories categories;

    public CMPropertyRecordReader(CMPropertyFileFormat fileFormat, User user, HibernateSessionFactory sessionFactory) {
        this.fileFormat = fileFormat;
        this.status = new CMAddImportStatus(user, sessionFactory);
//        this.sessionFactory = sessionFactory;
        this.categories = new ControlMeasurePropertyCategories(sessionFactory);
    }

    public void parse(Map controlMeasures, Record record, int lineNo) throws ImporterException {
        StringBuffer sb = new StringBuffer();
        String[] tokens = modify(record, sb, lineNo);
        ControlMeasure cm = controlMeasure(tokens[0], controlMeasures, sb, lineNo);
        if (tokens == null || cm == null)
            return;

        ControlMeasureProperty property = new ControlMeasureProperty();
        property.setName(tokens[1]);
        try {
            property.setCategory(categories.getCategory(tokens[2]));
        } catch (EmfException e) {
            sb.append(format(e.getMessage()));
        }
        property.setUnits(tokens[3]);
        property.setDataType(tokens[4]);
        property.setDbFieldName(tokens[5]);
        property.setValue(tokens[6]);
        cm.addProperty(property);
        if (sb.length() > 0) {
            errorCount++;
            status.addStatus(lineNo, sb);
        }
        if (errorCount >= errorLimit) throw new ImporterException("The maximum allowable error limit (" + errorLimit + ") has been reached while parsing the control measure equation records.");
    }

    private ControlMeasure controlMeasure(String token, Map controlMeasures, StringBuffer sb, int lineNo) {
        ControlMeasure cm = (ControlMeasure) controlMeasures.get(token);
        if (cm == null) {
            sb.append(format("abbreviation '" + token + "' is not in the control measure summary file"));
        }
        return cm;
    }

    private String[] modify(Record record, StringBuffer sb, int lineNo) throws ImporterException {
        int sizeDiff = fileFormat.cols().length - record.getTokens().length;
        if (sizeDiff == 0)
            return record.getTokens();

        if (sizeDiff > 0) {
            for (int i = 0; i < sizeDiff; i++) {
                record.add("");
            }
            return record.getTokens();
        }

        throw new ImporterException("The new record has extra tokens");
    }

    private String format(String text) {
        return status.format(text);
    }

    public int getErrorCount() {
        return errorCount;
    }
}
