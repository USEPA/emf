package gov.epa.emissions.framework.services.cost.controlmeasure.io;

import gov.epa.emissions.framework.services.cost.EquationType;

import java.util.HashMap;
import java.util.Map;

public class EquationTypeMap {
   
    private Map<String,EquationType> map;
    
    public EquationTypeMap(EquationType[] equationTypes) {
        this.map = new HashMap<String,EquationType>();
        buildMap(equationTypes);
    }
    
    private void buildMap(EquationType[] equationTypes) {
        for (int i = 0; i < equationTypes.length; i++) {
            map.put(equationTypes[i].getName(), equationTypes[i]);
        }
    }

    public EquationType getEquationType(String name) {
        return map.get(name);
    }
}