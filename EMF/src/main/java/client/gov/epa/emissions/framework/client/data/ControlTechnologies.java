package gov.epa.emissions.framework.client.data;


import gov.epa.emissions.framework.services.cost.data.ControlTechnology;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class ControlTechnologies {

    private List list;

    public ControlTechnologies(ControlTechnology[] array) {
        this.list = new ArrayList(Arrays.asList(array));
    }

    public ControlTechnology get(String name) {
        if(name == null || name.trim().length() == 0)
            return null;
        
        name = name.trim();
        for (int i = 0; i < list.size(); i++) {
            ControlTechnology item = ((ControlTechnology) list.get(i));
            if (item.getName().equalsIgnoreCase(name))
                return item;
        }
        ControlTechnology p = new ControlTechnology();
        p.setName(name);
        return p;
    }

    public String[] names() {
        List names = new ArrayList();
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            ControlTechnology element = (ControlTechnology) iter.next();
            names.add(element.getName());
        }
        
        return (String[]) names.toArray(new String[0]);
    }
}
