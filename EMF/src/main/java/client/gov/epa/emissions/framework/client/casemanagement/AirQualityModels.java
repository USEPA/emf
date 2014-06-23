package gov.epa.emissions.framework.client.casemanagement;

import gov.epa.emissions.framework.services.casemanagement.AirQualityModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class AirQualityModels {

    private List<AirQualityModel> list;

    public AirQualityModels(AirQualityModel[] array) {
        this.list = new ArrayList<AirQualityModel>(Arrays.asList(array));
    }

    public AirQualityModel get(String name) {
        if(name == null || name.trim().length() == 0)
            return null;

        name = name.trim();
        for (int i = 0; i < list.size(); i++) {
            AirQualityModel item = list.get(i);
            if (item.getName().equalsIgnoreCase(name))
                return item;
        }
        return new AirQualityModel(name);
    }

    public String[] names() {
        List<String> names = new ArrayList<String>();
        for (Iterator<AirQualityModel> iter = list.iterator(); iter.hasNext();) {
            AirQualityModel element = iter.next();
            names.add(element.getName());
        }

        return names.toArray(new String[0]);
    }

}
