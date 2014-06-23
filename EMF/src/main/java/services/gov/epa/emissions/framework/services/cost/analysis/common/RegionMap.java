package gov.epa.emissions.framework.services.cost.analysis.common;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegionMap {

    private Map<String,Map> map;

    public RegionMap() {
        map = new HashMap<String,Map>();
    }

    public void add(String datasetIdVersion, List<String> fips) {
        Map fipsMap = map.get(datasetIdVersion);
        if (fipsMap == null) {
            Map<String,String> newFipsMap = new HashMap<String,String>();
            for (int i = 0; i < fips.size(); i++) {
                newFipsMap.put(fips.get(i), "");
            }
            map.put(datasetIdVersion, newFipsMap);
        }
    }

    public boolean hasFips(String datasetIdVersion, String fips) {
        Map fipsMap = map.get(datasetIdVersion);
        if (fipsMap == null)
            return false;
        if (fipsMap.get(fips) == null)
            return false;
        return true;
    }

    public boolean exists(String datasetIdVersion) {
        Map fipsMap = map.get(datasetIdVersion);
        if (fipsMap == null)
            return false;
        return true;
    }

    public int size() {
        return map.size();
    }
}
