package gov.epa.emissions.framework.services.cost.controlmeasure.io;

import gov.epa.emissions.commons.Record;
import gov.epa.emissions.commons.io.importer.ImporterException;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.ControlMeasureDAO;
import gov.epa.emissions.framework.services.cost.ControlMeasureEquation;
import gov.epa.emissions.framework.services.cost.EquationType;
import gov.epa.emissions.framework.services.cost.EquationTypeVariable;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;

public class CMEquationRecordReader {

    private CMEquationFileFormat fileFormat;

    private CMAddImportStatus status;
    
    //Add EqTypVar map -- contains a map between eq type no to list of variables. 
    private EquationTypeMap equationTypeMap;

    private int errorCount = 0;
    
    private List equList;

    private int errorLimit = 100;
    
    private HibernateSessionFactory sessionFactory;

    private Pollutants pollutants;

    public CMEquationRecordReader(CMEquationFileFormat fileFormat, User user, HibernateSessionFactory sessionFactory) throws EmfException {
        this.fileFormat = fileFormat;
        this.status = new CMAddImportStatus(user, sessionFactory);
        this.sessionFactory = sessionFactory;
        this.equationTypeMap = new EquationTypeMap(getEquationTypes());
        this.equList= new ArrayList();
        this.pollutants = new Pollutants(sessionFactory);
    }

    public void parse(Map controlMeasures, Record record, int lineNo) throws ImporterException {
        StringBuffer sb = new StringBuffer();
        String[] tokens = modify(record, sb, lineNo);
        ControlMeasure cm = controlMeasure(tokens[0], controlMeasures, sb, lineNo);
        if (tokens == null || cm == null)
            return;

        //first lets get the equation type using the map, the seconds columns should
        //contain the equation type name.
        EquationType equationType = equationTypeMap.getEquationType(tokens[1]);
        if (equationType != null) {
            EquationTypeVariable[] equationTypeVariables = equationType.getEquationTypeVariables();
            //get rid of original settings
            cm.setEquations(new ControlMeasureEquation[] {});
            //set equation cost year
            ControlMeasureEquation equation = new ControlMeasureEquation(equationType);
            try {
                equation.setPollutant(pollutants.getPollutant(tokens[2]));
            } catch (CMImporterException e) {
                sb.append(format(e.getMessage()));
            }
            equation.setCostYear(Integer.parseInt(tokens[3]));
            //now add equation settings...
            if (constraintCheck(tokens[0], equationType, sb)){
                for (int i = 0; i < equationTypeVariables.length; i++) {

                    try {
                        Double value = null;
                        if (tokens[equationTypeVariables[i].getFileColPosition() + 3].length() > 0)
                            value = Double.valueOf(tokens[equationTypeVariables[i].getFileColPosition() + 3]);
                        if (equationTypeVariables[i].getFileColPosition() == 1) { 
                            equation.setValue1(value);
                        } else if (equationTypeVariables[i].getFileColPosition() == 2) { 
                            equation.setValue2(value);
                        } else if (equationTypeVariables[i].getFileColPosition() == 3) { 
                            equation.setValue3(value);
                        } else if (equationTypeVariables[i].getFileColPosition() == 4) { 
                            equation.setValue4(value);
                        } else if (equationTypeVariables[i].getFileColPosition() == 5) { 
                            equation.setValue5(value);
                        } else if (equationTypeVariables[i].getFileColPosition() == 6) { 
                            equation.setValue6(value);
                        } else if (equationTypeVariables[i].getFileColPosition() == 7) { 
                            equation.setValue7(value);
                        } else if (equationTypeVariables[i].getFileColPosition() == 8) { 
                            equation.setValue8(value);
                        } else if (equationTypeVariables[i].getFileColPosition() == 9) { 
                            equation.setValue9(value);
                        } else if (equationTypeVariables[i].getFileColPosition() == 10) { 
                            equation.setValue10(value);
                        } else if (equationTypeVariables[i].getFileColPosition() == 11) { 
                            equation.setValue11(value);
                        }
                    } catch (NumberFormatException e) {
                        sb.append(format("variable value must be a number, column position = " + (equationTypeVariables[i].getFileColPosition() + 3) + ", value = " + tokens[equationTypeVariables[i].getFileColPosition() + 3]));
                        break;
                    }
                }
                cm.addEquation(equation);
            }

        } else {
            sb.append(format("unknown equation type '" + tokens[1] + "'"));
        }
        if (sb.length() > 0) {
            errorCount++;
            status.addStatus(lineNo, sb);
        }
        if (errorCount >= errorLimit) throw new ImporterException("The maximum allowable error limit (" + errorLimit + ") has been reached while parsing the control measure equation records.");
    }

    private boolean constraintCheck(String abbre, EquationType equationType, StringBuffer sb) {
        String equString=abbre + equationType.getName();
        if (equList.contains(equString)){ 
            sb.append(format("Equation already in the file: "+ equString));
            return false;
        }
        equList.add(equString);
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
    
    private EquationType[] getEquationTypes() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List<EquationType> all = new ControlMeasureDAO().getEquationTypes(session);
            return all.toArray(new EquationType[0]);
        } catch (RuntimeException e) {
            throw new EmfException("Could not retrieve control measures Equation Types.");
        } finally {
            session.close();
        }
    }
}
