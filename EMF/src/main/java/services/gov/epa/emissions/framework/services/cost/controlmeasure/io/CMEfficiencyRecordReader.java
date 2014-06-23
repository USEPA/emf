package gov.epa.emissions.framework.services.cost.controlmeasure.io;

import gov.epa.emissions.commons.Record;
import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.ControlMeasureEquation;
import gov.epa.emissions.framework.services.cost.controlStrategy.CostYearTable;
import gov.epa.emissions.framework.services.cost.controlmeasure.EfficiencyRecordValidation;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CMEfficiencyRecordReader {

    private CMFileFormat fileFormat;

    private int colCount;
    
    private Pollutants pollutants;

    private CMAddImportStatus status;

    private EfficiencyRecordValidation validation;
    
    private User user;

    private CostYearTable costYearTable;
    
    private int errorCount = 0;

    private int errorLimit = 1000;

    private boolean warning = false;

    private Map<String, String> compositeKeyMap;
    
    public CMEfficiencyRecordReader(CMFileFormat fileFormat, User user, 
            HibernateSessionFactory sessionFactory, CostYearTable costYearTable) {
        this.fileFormat = fileFormat;
        this.colCount = fileFormat.cols().length;
        this.status = new CMAddImportStatus(user, sessionFactory);
        this.user = user;
        this.pollutants = new Pollutants(sessionFactory);
        this.validation = new EfficiencyRecordValidation();
        this.costYearTable = costYearTable;
        this.compositeKeyMap = new HashMap<String,String>();
    }

//    public void parse(Map controlMeasures, Record record, int lineNo) {
//        StringBuffer sb = new StringBuffer();
//        String[] tokens = modify(record, sb, lineNo);
//
//        ControlMeasure cm = controlMeasure(tokens[0], controlMeasures, sb, lineNo);
//        if (tokens == null || cm == null || !checkForConstraints(tokens, sb, lineNo))
//            return;
//
//        try {
//            EfficiencyRecord efficiencyRecord = new EfficiencyRecord();
//            efficiencyRecord.setLastModifiedTime(new Date());
//            efficiencyRecord.setLastModifiedBy(user.getName());
//            pollutant(efficiencyRecord, tokens[1], sb);
//            locale(efficiencyRecord, tokens[2], sb);
//            effectiveDate(efficiencyRecord, tokens[3], sb);
//            existingMeasureAbbrev(efficiencyRecord, tokens[4]);
//            controlEfficiency(efficiencyRecord, tokens[6], sb);
//            costYear(efficiencyRecord, tokens[7], sb);
//            costPerTon(efficiencyRecord, tokens[8], sb);
//            ruleEffectiveness(efficiencyRecord, tokens[9], sb);
//            rulePenetration(efficiencyRecord, tokens[10], sb);
//            equationType(efficiencyRecord, tokens[11]);
//            capitalRecoveryFactor(efficiencyRecord, tokens[12], sb);
//            discountFactor(efficiencyRecord, tokens[13], sb);
//            details(efficiencyRecord, tokens[14]);
//
//            cm.addEfficiencyRecord(efficiencyRecord);
//        } catch (EmfException e) {
//            // don't add the efficiency record if the validation fails
//        }
//        status.addStatus(lineNo, sb);
//    }

    public EfficiencyRecord parseEfficiencyRecord(Map controlMeasures, Record record, int lineNo) throws EmfException {
        StringBuffer sb = new StringBuffer();
        String[] tokens = modify(record, sb, lineNo);
        EfficiencyRecord efficiencyRecord = null;
        warning = false;
        
//        control_measures_id, pollutant_id, locale, existing_measure_abbr, effective_date, min_emis, max_emis
        //tokens[0] + "_" + pollutants.getPollutant(tokens[1]) + "_" + tokens[2] + "_" + tokens[4] + "_" + validation.existingDevCode(tokens[5]) + "_" + validation.effectiveDate(tokens[3]) + "_" + validation.minEmis(tokens[6]) + "_" + validation.minEmis(tokens[7])  
        try {
            ControlMeasure cm = controlMeasure(tokens[0], controlMeasures, sb, lineNo);
            if (tokens == null || cm == null || !checkForConstraints(tokens, sb, lineNo)) {
                errorCount++;
                return null;
            }
            efficiencyRecord = new EfficiencyRecord();
            efficiencyRecord.setControlMeasureId(cm.getId());
            efficiencyRecord.setLastModifiedTime(new Date());
            efficiencyRecord.setLastModifiedBy(user.getName());
            pollutant(efficiencyRecord, tokens[1], sb);
            locale(efficiencyRecord, tokens[2], sb);
            effectiveDate(efficiencyRecord, tokens[3], sb);
            existingMeasureAbbrev(efficiencyRecord, tokens[4]);
            existingDevCode(efficiencyRecord, tokens[5], sb);
            //original file format
            if (this.colCount == 16) {
                controlEfficiency(efficiencyRecord, tokens[6], sb);
                costYear(efficiencyRecord, tokens[7], sb);
                costPerTon(efficiencyRecord, tokens[0], controlMeasures, 
                        tokens[8], tokens[7], 
                        sb);
//                costYear(efficiencyRecord, tokens[7], sb);
//                costPerTon(efficiencyRecord, tokens[8], sb);
                refYrCostPerTon(efficiencyRecord, tokens[7], tokens[8]);
                ruleEffectiveness(efficiencyRecord, tokens[9], sb);
                rulePenetration(efficiencyRecord, tokens[10], sb);
                equationType(efficiencyRecord, tokens[11]);
                capitalRecoveryFactor(efficiencyRecord, tokens[12], sb);
                discountFactor(efficiencyRecord, tokens[13], sb);
                details(efficiencyRecord, tokens[14]);
            //v2 file format
            } else if (this.colCount == 18) {
                minEmis(efficiencyRecord, tokens[6], sb);
                maxEmis(efficiencyRecord, tokens[7], sb);
                controlEfficiency(efficiencyRecord, tokens[8], sb);
                costYear(efficiencyRecord, tokens[9], sb);
                costPerTon(efficiencyRecord, tokens[0], controlMeasures,
                        tokens[10], tokens[9], 
                        sb);
                refYrCostPerTon(efficiencyRecord, tokens[9], tokens[10]);
                ruleEffectiveness(efficiencyRecord, tokens[11], sb);
                rulePenetration(efficiencyRecord, tokens[12], sb);
                equationType(efficiencyRecord, tokens[13]);
                capitalRecoveryFactor(efficiencyRecord, tokens[14], sb);
                discountFactor(efficiencyRecord, tokens[15], sb);
                details(efficiencyRecord, tokens[16]);
            }
            //v3 file format
            else if (this.colCount == 20){
                minEmis(efficiencyRecord, tokens[6], sb);
                maxEmis(efficiencyRecord, tokens[7], sb);
                controlEfficiency(efficiencyRecord, tokens[8], sb);
                costYear(efficiencyRecord, tokens[9], sb);
                costPerTon(efficiencyRecord, tokens[0], controlMeasures,
                        tokens[10], tokens[9], 
                        sb);
                refYrCostPerTon(efficiencyRecord, tokens[9], tokens[10]);
                ruleEffectiveness(efficiencyRecord, tokens[11], sb);
                rulePenetration(efficiencyRecord, tokens[12], sb);
                equationType(efficiencyRecord, tokens[13]);
                capitalRecoveryFactor(efficiencyRecord, tokens[14], sb);
                discountFactor(efficiencyRecord, tokens[15], sb);
                capitalAnnulizdRatio(efficiencyRecord, tokens[16],sb);
                incrementalCTP(efficiencyRecord, tokens[17],sb);
                details(efficiencyRecord, tokens[18]);  
            }

        } catch (CMImporterException e) {
            // don't add the efficiency record if the validation fails
            efficiencyRecord = null;
        } catch (EmfException e) {
            // don't add the efficiency record if the validation fails
            efficiencyRecord = null;
        }
        if (sb.length() > 0) {
            if (!warning) errorCount++;
            status.setStatus("Line "+lineNo + ": Measure, " + tokens[0] + ", Efficiency Record Issue for pollutant, " + tokens[1] + ": " + sb.toString());
//            status.addStatus(lineNo, sb);
        }

        if (errorCount >= errorLimit) throw new EmfException("The maximum allowable error limit (" + errorLimit + ") has been reached while parsing the control measure efficiency records.");
        return efficiencyRecord;
    }

    private void incrementalCTP(EfficiencyRecord efficiencyRecord, String incCPT, StringBuffer sb) {
        try {
            efficiencyRecord.setIncrementalCostPerTon(validation.increCPT(incCPT));
        } catch (EmfException e) {
            sb.append(format(e.getMessage()));
        } 
    }

    private void capitalAnnulizdRatio(EfficiencyRecord efficiencyRecord, String capAnnRatio, StringBuffer sb) {
        try {
            efficiencyRecord.setCapitalAnnualizedRatio(validation.capAnnRatio(capAnnRatio));
        } catch (EmfException e) {
            sb.append(format(e.getMessage()));
        } 
    }

    private ControlMeasure controlMeasure(String token, Map controlMeasures, StringBuffer sb, int lineNo) {
        ControlMeasure cm = (ControlMeasure) controlMeasures.get(token);
        if (cm == null) {
            sb.append(format("Abbreviation '" + token + "' is not in the control measure summary file"));
            status.addStatus(lineNo, sb);
        }
        return cm;
    }

    private boolean checkForConstraints(String[] tokens, StringBuffer sb, int lineNo) throws EmfException, CMImporterException {
        String uniqueCompositeKey = tokens[0] + "_" + pollutants.getPollutant(tokens[1]).getName() + "_" + tokens[2] + "_" + tokens[4] + "_" + validation.existingDevCode(tokens[5]) + "_" + validation.effectiveDate(tokens[3]) + "_" + (this.colCount != 15 ? validation.minEmis(tokens[6]) + "_" + validation.minEmis(tokens[7]) : "_");
        if (this.compositeKeyMap.containsKey(uniqueCompositeKey)) {
            sb.append(format("Efficiency record is a duplicate, remove the duplicate, abbrv = " + tokens[0] + ", poll = " + tokens[1]));
            status.addStatus(lineNo, sb);
            return false;
        }
        //add new composite key
        this.compositeKeyMap.put(uniqueCompositeKey, "");
//control_measures_id, pollutant_id, locale, existing_measure_abbr, effective_date, min_emis, max_emis
        if (tokens[0].length() == 0) {
            sb.append("pollutant should not be empty.");
            status.addStatus(lineNo, sb);
            return false;
        }

        return true;
    }

    private void pollutant(EfficiencyRecord efficiencyRecord, String name, StringBuffer sb) {
        try {
            Pollutant pollutant = pollutants.getPollutant(name);
            efficiencyRecord.setPollutant(pollutant);
        } catch (CMImporterException e) {
            sb.append(format(e.getMessage()));
        }
    }

    private void locale(EfficiencyRecord efficiencyRecord, String locale, StringBuffer sb) {
        try {
            efficiencyRecord.setLocale(validation.locale(locale));
        } catch (EmfException e) {
            sb.append(format(e.getMessage()));
        }
    }

    private void effectiveDate(EfficiencyRecord efficiencyRecord, String effectiveDate, StringBuffer sb) {
        try {
            efficiencyRecord.setEffectiveDate(validation.effectiveDate(effectiveDate));
        } catch (EmfException e) {
            sb.append(format(e.getMessage()));
        }
    }

    private void existingMeasureAbbrev(EfficiencyRecord efficiencyRecord, String existMeasureAbbrev) {
        efficiencyRecord.setExistingMeasureAbbr(existMeasureAbbrev);
    }

    private void existingDevCode(EfficiencyRecord efficiencyRecord, String existDevCode, StringBuffer sb) {
        try {
            efficiencyRecord.setExistingDevCode(validation.existingDevCode(existDevCode));
        } catch (EmfException e) {
            sb.append(format(e.getMessage()));
        }
    }

    private void controlEfficiency(EfficiencyRecord efficiencyRecord, String ce, StringBuffer sb) throws EmfException {
        String efficiency = ((ce.indexOf('%') != -1) ? ce.split("%")[0] : ce).trim();

        try {
            if (efficiency.length() > 0) {
                Double value = validation.efficiency(efficiency);
                if (value == 0) {
                    warning = true;
                    sb.append(format("The Control Efficiency is zero, 0%, this efficiency record will be dropped."));
                    throw new EmfException("The Control Efficiency is zero, 0%, this efficiency record will be dropped.");            
                }
                efficiencyRecord.setEfficiency(value);
            } else {
                efficiencyRecord.setEfficiency(null);
            }
        } catch (EmfException e) {
            sb.append(format(e.getMessage()));
            // If control Efficiency is not valid, we want the validation process to stop
            // so let the exception go up a level
            throw e;
        }
    }

    private void costYear(EfficiencyRecord efficiencyRecord, String year, StringBuffer sb) {
        try {
            efficiencyRecord.setCostYear(validation.costYear(year));
        } catch (EmfException e) {
            sb.append(format(e.getMessage()));
        }

    }

    private void costPerTon(EfficiencyRecord efficiencyRecord, String abbreviation, 
            Map controlMeasures, String costValue, 
            String year, StringBuffer sb) {
        try {
            Double costPerTon = validation.costPerTon(costValue, year);
            //check if has equation, if so, make sure there is a default cpt
            if (costPerTon == null) {
                ControlMeasure measure = (ControlMeasure) controlMeasures.get(abbreviation);
                ControlMeasureEquation[] equations = measure.getEquations();
                if (equations.length > 0) {
                    for (ControlMeasureEquation equation : equations) {
                        if (equation.getPollutant().equals(efficiencyRecord.getPollutant())) {
                            warning = true;
                            sb.append(format("Warning: Missing default cost per ton when an equation uses the same pollutant.  A default CPT is not required."));
                        }
                    }
                }
            }
                
            efficiencyRecord.setCostPerTon(costPerTon);
        } catch (EmfException e) {
            sb.append(format(e.getMessage()));
        }
    }

    private void minEmis(EfficiencyRecord efficiencyRecord, String minEmisValue, StringBuffer sb) {
        try {
            efficiencyRecord.setMinEmis(validation.minEmis(minEmisValue));
        } catch (EmfException e) {
            sb.append(format(e.getMessage()));
        }
    }

    private void maxEmis(EfficiencyRecord efficiencyRecord, String maxEmisValue, StringBuffer sb) {
        try {
            efficiencyRecord.setMaxEmis(validation.maxEmis(maxEmisValue));
        } catch (EmfException e) {
            sb.append(format(e.getMessage()));
        }
    }

    private void ruleEffectiveness(EfficiencyRecord efficiencyRecord, String ruleEffectiveness, StringBuffer sb) {
        try {
            efficiencyRecord.setRuleEffectiveness(validation.ruleEffectiveness(ruleEffectiveness));
        } catch (EmfException e) {
            sb.append(format(e.getMessage()));
        }
    }

    private void rulePenetration(EfficiencyRecord efficiencyRecord, String rulePenetration, StringBuffer sb) {
        try {
            efficiencyRecord.setRulePenetration(validation.rulePenetration(rulePenetration));
        } catch (EmfException e) {
            sb.append(format(e.getMessage()));
        }
    }

    private void equationType(EfficiencyRecord efficiencyRecord, String eqType) {
        efficiencyRecord.setEquationType(eqType);
    }

    private void capitalRecoveryFactor(EfficiencyRecord efficiencyRecord, String factor, StringBuffer sb) {
        try {
            efficiencyRecord.setCapRecFactor(validation.capRecFactor(factor));
        } catch (EmfException e) {
            sb.append(format(e.getMessage()));
        }
    }

    private void discountFactor(EfficiencyRecord efficiencyRecord, String factor, StringBuffer sb) {
        String ds = (factor.indexOf('%') != -1) ? factor.split("%")[0] : factor;
        try {
            efficiencyRecord.setDiscountRate(validation.discountRate(ds));
        } catch (EmfException e) {
            sb.append(format(e.getMessage()));
        }
    }

    private void details(EfficiencyRecord efficiencyRecord, String details) {
        efficiencyRecord.setDetail(details);
    }

    private void refYrCostPerTon(EfficiencyRecord efficiencyRecord, String year, String costValue) {
        try {
            Double costPerTon = validation.costPerTon(costValue, year);
            if (costPerTon != null)
                efficiencyRecord.setRefYrCostPerTon(costPerTon * new Float(costYearTable.factor(validation.costYear(year))));
            else
                efficiencyRecord.setRefYrCostPerTon(null);
        } catch (EmfException e) {
            //don't propagate exception, these would have been taken care of from the cost per ton and cost year validation
        }
    }

    private String[] modify(Record record, StringBuffer sb, int lineNo) {
        int sizeDiff = fileFormat.cols().length - record.getTokens().length;
        if (sizeDiff == 0)
            return record.getTokens();

        if (sizeDiff > 0) {
            for (int i = 0; i < sizeDiff; i++) {
                record.add("");
            }
            return record.getTokens();
        }

        sb.append(format("The new record has extra tokens"));
//        status.addStatus(lineNo, sb);
        return null;
    }

    private String format(String text) {
        return status.format(text);
    }

    public int getErrorCount() {
        return errorCount;
    }
}
