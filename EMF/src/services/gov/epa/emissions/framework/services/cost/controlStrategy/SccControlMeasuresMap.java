package gov.epa.emissions.framework.services.cost.controlStrategy;

import gov.epa.emissions.framework.services.cost.ControlMeasure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SccControlMeasuresMap {

    private Map map;

    private Map measuresMap;

    public SccControlMeasuresMap() {
        map = new HashMap();
        measuresMap = new HashMap();
    }

    public void add(String scc, ControlMeasure measure) {
        List list = (List) map.get(scc);
        if (list == null) {
            list = new ArrayList();
            map.put(scc, list);
        }
        list.add(Integer.valueOf(measure.getId()));

        measuresMap.put(Integer.valueOf(measure.getId()), measure);
    }

    public ControlMeasure[] getControlMeasures(String scc) {
        List list = (List) map.get(scc);
        if (list == null)
            list = Collections.EMPTY_LIST;
        return measures(list);
    }

    private ControlMeasure[] measures(List list) {
        ControlMeasure[] cms = new ControlMeasure[list.size()];
        for (int i = 0; i < list.size(); i++) {
            cms[i] = (ControlMeasure) measuresMap.get(list.get(i));
        }
        return cms;
    }

    public int size() {
        return map.size();
    }
}
