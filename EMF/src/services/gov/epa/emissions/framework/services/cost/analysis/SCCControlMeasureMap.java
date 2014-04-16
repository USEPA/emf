package gov.epa.emissions.framework.services.cost.analysis;

import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.controlmeasure.Scc;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//FIXME: Rename ???
public class SCCControlMeasureMap {
   
    private Map map;
    
    private String pollutant;

    private int costYear;
    
    public SCCControlMeasureMap(String[] sccs, ControlMeasure[] measures, String pollutant, int costYear) {
        this.map = new HashMap();
        this.pollutant = pollutant;
        this.costYear  = costYear;
        setMaps(sccs, measures);
    }
    
    private void setMaps(String[] sccs, ControlMeasure[] measures) {
        for (int i = 0; i < sccs.length; i++) {
            List measuresList = getControlMeasuresList(sccs[i], measures);
            ControlMeasure seed = calculateMaxMeasure(measuresList);
            ControlMeasure maxRedMeasure = calculateTargetMeasure(seed, measuresList);
            map.put(sccs[i], maxRedMeasure);
        }
    }

    private List getControlMeasuresList(String scc, ControlMeasure[] measures) {
        List measuresList = new ArrayList();
        for (int i = 0; i < measures.length; i++) {
            if (contains(measures[i], scc))
                measuresList.add(measures[i]);
        }
        
        return measuresList;
    }

    private boolean contains(ControlMeasure measure, String scc) {
        Scc[] sccs = measure.getSccs();
        
        for (int i = 0; i < sccs.length; i++)
            if (scc.equalsIgnoreCase((sccs[i]).getCode()))
                return true;
        
        return false;
    }
    
    public ControlMeasure getMaxRedControlMeasure(String scc) {
        return (ControlMeasure) map.get(scc);
    }
    
    private ControlMeasure calculateTargetMeasure(ControlMeasure seed, List measures) {
        ControlMeasure previous = seed;
        if (previous == null)
            return previous;
        measures.remove(previous);

        ControlMeasure next = calculateMaxMeasure(measures);
        if (next == null)
            return previous;
        measures.remove(next);

        // Recursively check if there are two control measures that have the same control
        // efficiency
        Double previousEff = getEfficiency(previous);
        Double nextEff = getEfficiency(next);
        
        if ( previousEff > nextEff)
            return previous;
        
        if (previousEff == nextEff)
            seed = getMeasureWithSmallerCost(previous, next); 
        
        if (previousEff < nextEff)
            seed = next;
        
        return calculateTargetMeasure(seed, measures);
    }

    private ControlMeasure getMeasureWithSmallerCost(ControlMeasure previous, ControlMeasure next) {
        return getCostPerTon(previous) <= getCostPerTon(next) ? previous : next;
    }
    
    private double getCostPerTon(ControlMeasure measure) {
        EfficiencyRecord [] records = measure.getEfficiencyRecords();
        
        for (int i = 0; i < records.length; i++) {
            String controlMeasurePollutant = records[i].getPollutant().getName();//FIXME: don't use the name to compare use pollutants
            if (pollutant.equalsIgnoreCase(controlMeasurePollutant) && costYear == records[i].getCostYear())
                return records[i].getCostPerTon();
        }

        return 0; // assume cost per ton >= 0;
    }

    private ControlMeasure calculateMaxMeasure(List measureList) {
        if (measureList.size() == 0)
            return null;

        ControlMeasure temp = (ControlMeasure) measureList.get(0);

        for (int i = 1; i < measureList.size(); i++) {
            if (getEfficiency(temp) < getEfficiency((ControlMeasure) measureList.get(i)))
                temp = (ControlMeasure) measureList.get(i);
        }
        
        return temp;
    }

    private Double getEfficiency(ControlMeasure measure) {
        EfficiencyRecord[] records = measure.getEfficiencyRecords();

        for (int i = 0; i < records.length; i++) {
            String pollutant = records[i].getPollutant().getName();
            if (pollutant.equalsIgnoreCase(this.pollutant))
                return records[i].getEfficiency();
        }

        return 0D; // assume efficiency >= 0;
    }
    
}
