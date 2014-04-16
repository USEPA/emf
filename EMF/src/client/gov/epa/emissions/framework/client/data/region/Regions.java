package gov.epa.emissions.framework.client.data.region;

import gov.epa.emissions.commons.data.Region;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class Regions {

    private List list;

    public Regions(Region[] array) {
        this.list = new ArrayList(Arrays.asList(array));
    }

    public Region get(String name) {
        if(name == null || name.trim().length() == 0)
            return null;

        name = name.trim();
        for (int i = 0; i < list.size(); i++) {
            Region item = ((Region) list.get(i));
            if (item.getName().equalsIgnoreCase(name))
                return item;
        }
        return new Region(name);
    }

    public String[] names() {
        List names = new ArrayList();
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            Region element = (Region) iter.next();
            names.add(element.getName());
        }

        return (String[]) names.toArray(new String[0]);
    }
}
