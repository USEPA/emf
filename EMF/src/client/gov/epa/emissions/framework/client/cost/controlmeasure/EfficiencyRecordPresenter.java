package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;

import java.util.Date;

public class EfficiencyRecordPresenter {

    protected ControlMeasureEfficiencyTabView parentView;

    public EfficiencyRecordPresenter(ControlMeasureEfficiencyTabView parentView) {
        this.parentView = parentView;
    }

    public void checkForDuplicate(EfficiencyRecord record) throws EmfException {
        EfficiencyRecord[] records = parentView.records();
        for (int i = 0; i < records.length; i++) {
            if (record.getRecordId() != records[i].getRecordId()) {
                if (same(record, records[i])) {
                    throw new EmfException("Duplicate Record:" + duplicateRecordMsg());
                }
            }
        }
    }

    private String duplicateRecordMsg() {
        return "The combination of 'Pollutant', 'Locale', 'Effective Date', 'Existing Measure', 'Existing Dev Code', 'Min Emission' and 'Max Emission' should be unique";
    }

    private boolean same(EfficiencyRecord record1, EfficiencyRecord record2) {
        return record1.getPollutant().equals(record2.getPollutant()) && record1.getLocale().equals(record2.getLocale())
                && sameEffectiveDate(record1, record2)
                && record1.getExistingMeasureAbbr().equals(record2.getExistingMeasureAbbr())
                && record1.getExistingDevCode() == record2.getExistingDevCode()
                && sameMinMaxEmission(record1, record2);
    
    }

    private boolean sameEffectiveDate(EfficiencyRecord record1, EfficiencyRecord record2) {
        Date effectiveDate1 = record1.getEffectiveDate();
        Date effectiveDate2 = record2.getEffectiveDate();
        // if both are null mean user didn't enter a effective date=>equal
        if (effectiveDate1 == null && effectiveDate2 == null)
            return true;
        // if either one is null =>not equal
        if (effectiveDate1 == null || effectiveDate2 == null)
            return false;
    
        return effectiveDate1.equals(effectiveDate2);
    }

    private boolean sameMinMaxEmission(EfficiencyRecord record1, EfficiencyRecord record2) {
        Double minEmis1 = record1.getMinEmis();
        Double minEmis2 = record2.getMinEmis();
        Double maxEmis1 = record1.getMaxEmis();
        Double maxEmis2 = record2.getMaxEmis();
        if (minEmis1 == null && minEmis2 == null
                && maxEmis1 == null && maxEmis2 == null)
            return true;
        if (minEmis1 != null && minEmis2 != null && minEmis1.equals(minEmis2) 
                && maxEmis1 != null && maxEmis2 != null && maxEmis1.equals(maxEmis2))
            return true;
        if (minEmis1 != null && minEmis2 != null && minEmis1.equals(minEmis2) 
                && maxEmis1 == null && maxEmis2 == null)
            return true;
        return false;
    }

}
