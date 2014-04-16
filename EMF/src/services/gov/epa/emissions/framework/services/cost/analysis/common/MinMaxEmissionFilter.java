package gov.epa.emissions.framework.services.cost.analysis.common;

import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;

import java.util.ArrayList;
import java.util.List;

public class MinMaxEmissionFilter {

    private EfficiencyRecord[] efficiencyRecords;

    private double invenAnnualEmission;

    public MinMaxEmissionFilter(EfficiencyRecord[] efficiencyRecords, double invenAnnualEmission) {
        this.efficiencyRecords = efficiencyRecords;
        this.invenAnnualEmission = invenAnnualEmission;
    }

    public EfficiencyRecord[] filter() {
        return minMaxEmissionFilter(efficiencyRecords, invenAnnualEmission);
    }

    private EfficiencyRecord[] minMaxEmissionFilter(EfficiencyRecord[] efficiencyRecords, double invenAnnualEmission) {
        List records = new ArrayList();
        Double minEmis;
        Double maxEmis;
        for (int i = 0; i < efficiencyRecords.length; i++) {
            minEmis = efficiencyRecords[i].getMinEmis();
            maxEmis = efficiencyRecords[i].getMaxEmis();
            //if no min or max is specified, then include it in the arraylist...
            if (minEmis == null && maxEmis == null) {
                records.add(efficiencyRecords[i]);
            //assume minEmis is zero in this case...
            } else if (minEmis == null && maxEmis != null)  {
                if (invenAnnualEmission >= 0 && invenAnnualEmission <= maxEmis)
                    records.add(efficiencyRecords[i]);
            } else if (minEmis != null && maxEmis != null)  {
                if (invenAnnualEmission >= minEmis && invenAnnualEmission <= maxEmis)
                    records.add(efficiencyRecords[i]);
            //assume maxEmis is infinitely big in this case...
            } else if (minEmis != null && maxEmis == null)  {
                if (invenAnnualEmission >= minEmis)
                    records.add(efficiencyRecords[i]);
            }
        }
        return (EfficiencyRecord[]) records.toArray(new EfficiencyRecord[0]);
    }
}
