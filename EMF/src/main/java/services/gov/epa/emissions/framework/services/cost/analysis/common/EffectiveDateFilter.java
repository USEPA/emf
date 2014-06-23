package gov.epa.emissions.framework.services.cost.analysis.common;

import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EffectiveDateFilter {

    private EfficiencyRecord[] efficiencyRecords;

    private int inventoryYear;

    public EffectiveDateFilter(EfficiencyRecord[] efficiencyRecords, int inventoryYear) {
        this.efficiencyRecords = efficiencyRecords;
        this.inventoryYear = inventoryYear;
    }

    public EfficiencyRecord[] filter() {
        return effectiveDateFilter(efficiencyRecords, inventoryYear);
    }

    private EfficiencyRecord[] effectiveDateFilter(EfficiencyRecord[] efficiencyRecords, int inventoryYear) {
        List records = new ArrayList();
        List nullList = new ArrayList();
        int minDiff = Integer.MAX_VALUE;
        for (int i = 0; i < efficiencyRecords.length; i++) {
            Date effectiveDate = efficiencyRecords[i].getEffectiveDate();
            if (effectiveDate == null)
                nullList.add(efficiencyRecords[i]);
            else
                minDiff = dateFilter(records, efficiencyRecords[i], effectiveDate, inventoryYear, minDiff);
        }

        if (!records.isEmpty())
            return (EfficiencyRecord[]) records.toArray(new EfficiencyRecord[0]);

        return (EfficiencyRecord[]) nullList.toArray(new EfficiencyRecord[0]);
    }

    private int dateFilter(List records, EfficiencyRecord record, Date effectiveDate, int inventoryYear, int minDiff) {
        int recordYear = Integer.parseInt(CustomDateFormat.format_YYYY(effectiveDate));
        int diff = inventoryYear - recordYear;
        if (diff >= 0) {
            if (diff == minDiff) {
                records.add(record);
                return minDiff;
            }
            if (diff < minDiff) {
                records.clear();
                records.add(record);
                return diff;
            }
            // (diff>minDiff) not close enough
        }
        return minDiff;

    }

}
