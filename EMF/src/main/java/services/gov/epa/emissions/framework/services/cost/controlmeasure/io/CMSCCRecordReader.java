package gov.epa.emissions.framework.services.cost.controlmeasure.io;

import gov.epa.emissions.commons.Record;
import gov.epa.emissions.commons.io.importer.ImporterException;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.controlmeasure.Scc;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CMSCCRecordReader {

    private CMSCCsFileFormat fileFormat;

    private CMAddImportStatus status;
//    private HashSet sccSet;
    private List sccList;
    
    private int errorCount = 0;

    private int errorLimit = 100;

    public CMSCCRecordReader(CMSCCsFileFormat fileFormat, User user, HibernateSessionFactory sessionFactory) {
        this.fileFormat = fileFormat;
        this.status = new CMAddImportStatus(user, sessionFactory);
 //       this.sccSet = new HashSet();
        this.sccList=new ArrayList();
    }

    public void parse(Map controlMeasures, Record record, int lineNo) throws ImporterException {
        StringBuffer sb = new StringBuffer();
        String[] tokens = modify(record, sb, lineNo);
        ControlMeasure cm = controlMeasure(tokens[0], controlMeasures, sb, lineNo);
        if (tokens == null || cm == null)
            return;
        if (constraintCheck(cm, tokens, sb)) {
            Scc scc = new Scc();
            scc.setCode(tokens[1]);
            scc.setStatus(tokens[2]);
            cm.addScc(scc);
        }
        if (sb.length() > 0) {
            errorCount++;
            status.addStatus(lineNo, sb);
        }
        if (errorCount >= errorLimit) throw new ImporterException("The maximum allowable error limit (" + errorLimit + ") has been reached while parsing the control measure SCC records.");
    }

    private boolean constraintCheck(ControlMeasure cm, String[] tokens, StringBuffer sb) {
//        Scc scct = new Scc();
//        scct.setControlMeasureId(cm.getId());
//        scct.setCode(tokens[1]);
//        scct.setStatus(tokens[2]);    
//        if (sccSet.contains(scct)){ 
//            sb.append(format(" SCC already in the file: "+ tokens[0]));
//            return false;
//        }
//        sccSet.add(scct);
        String sccString=tokens[0].toLowerCase()+tokens[1].toLowerCase();
        if (sccList.contains(sccString)){ 
            sb.append(format(" SCC already in the file: Abbreviation = " + tokens[0] + " SCC = " + tokens[1]));
            return false;
        }
        sccList.add(sccString);
        return true;
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
