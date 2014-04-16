package gov.epa.emissions.framework.services.sms;

import gov.epa.emissions.commons.data.DatasetType;

import java.util.HashMap;
import java.util.Map;

public class SectorScenarioInputToOutputDatasetTypeMap {
    private Map<String,String> map = new HashMap<String,String>();
    
    public SectorScenarioInputToOutputDatasetTypeMap() {
        map.put(DatasetType.ORL_POINT_NATA, DatasetType.ORL_POINT_NATA_SECTOR_ANNOTATED);
        map.put(DatasetType.ORL_POINT_NATA_SECTOR_ANNOTATED, DatasetType.ORL_POINT_NATA_SECTOR_ANNOTATED);
//        map.put(DatasetType.ORL_NONPOINT_NATA, DatasetType.ORL_NONPOINT_NATA_SECTOR_ANNOTATED);
//        map.put(DatasetType.ORL_NONPOINT_NATA_SECTOR_ANNOTATED, DatasetType.ORL_NONPOINT_NATA_SECTOR_ANNOTATED);
        map.put(DatasetType.NOF_POINT, DatasetType.NOF_POINT);
        map.put(DatasetType.NOF_NONPOINT, DatasetType.NOF_NONPOINT);
    }

    public String getOutputDatasetType(String inputDatasetType) {
        String outputDatasetType = map.get(inputDatasetType);
        return outputDatasetType;
    }
    
    public static void main(String args[]) {
        SectorScenarioInputToOutputDatasetTypeMap map = new SectorScenarioInputToOutputDatasetTypeMap();
        System.out.println(map.getOutputDatasetType(DatasetType.ORL_POINT_NATA));
        System.out.println(map.getOutputDatasetType(DatasetType.ORL_POINT_NATA_SECTOR_ANNOTATED));
//        System.out.println(map.getOutputDatasetType(DatasetType.ORL_NONPOINT_NATA));
//        System.out.println(map.getOutputDatasetType(DatasetType.ORL_NONPOINT_NATA_SECTOR_ANNOTATED));
        System.out.println(map.getOutputDatasetType("SDSADSADASDSADSAD"));
    }
}
