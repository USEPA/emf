package gov.epa.emissions.framework.services.cost.analysis.maxreduction;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.analysis.common.BestMeasureEffRecord;
import gov.epa.emissions.framework.services.cost.analysis.common.RetrieveBestEffRecord;
import gov.epa.emissions.framework.services.cost.controlStrategy.CostYearTable;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MaxEmsRedControlMeasureMap {

    private Map map;

    private CostYearTable costYearTable;
    
    private RetrieveBestEffRecord retrieveBestEffRecord;

    public MaxEmsRedControlMeasureMap(CostYearTable costYearTable) {
        this.costYearTable = costYearTable;
        this.retrieveBestEffRecord = new RetrieveBestEffRecord(costYearTable);
        this.map = new HashMap();
    }

    public void add(ControlMeasure measure, EfficiencyRecord record) {
        map.put(record, measure);
    }

    public BestMeasureEffRecord findBestMeasure() throws EmfException {
        if (map.size() == 0)
            return null;// FIXME: do we have to warn or error
        
        Iterator iterator = map.entrySet().iterator();

        Map.Entry entry =  (Map.Entry)iterator.next();
        EfficiencyRecord bestRecord = (EfficiencyRecord) entry.getKey();
        ControlMeasure bestMeasure = (ControlMeasure) entry.getValue();

        while (iterator.hasNext()) {
            entry =  (Map.Entry)iterator.next();
            EfficiencyRecord record = (EfficiencyRecord) entry.getKey();
            ControlMeasure measure = (ControlMeasure) entry.getValue();
            bestRecord = retrieveBestEffRecord.findBestEfficiencyRecord(measure, record, 
                    bestMeasure, bestRecord);
            bestMeasure = (ControlMeasure)map.get(bestRecord);
        }

        ControlMeasure controlMeasure = (ControlMeasure) map.get(bestRecord);
        BestMeasureEffRecord maxMeasure = new BestMeasureEffRecord(controlMeasure, bestRecord, costYearTable);
        return maxMeasure;

    }
}