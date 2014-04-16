package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.data.Pollutant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class Pollutants {

    private List list;

    public Pollutants(Pollutant[] array) {
        this.list = new ArrayList(Arrays.asList(array));
    }

    public Pollutant get(String name) {
        if(name == null || name.trim().length() == 0)
            return null;
        
        name = name.trim();
        for (int i = 0; i < list.size(); i++) {
            Pollutant item = ((Pollutant) list.get(i));
            if (item.getName().equalsIgnoreCase(name))
                return item;
        }
        Pollutant p = new Pollutant();
        p.setName(name);
        return p;
    }

    public String[] names() {
        List names = new ArrayList();
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            Pollutant element = (Pollutant) iter.next();
            names.add(element.getName());
        }
        
        return (String[]) names.toArray(new String[0]);
    }
}
