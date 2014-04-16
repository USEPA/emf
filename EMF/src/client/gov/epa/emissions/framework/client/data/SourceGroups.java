package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.data.SourceGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class SourceGroups {

    private List list;

    public SourceGroups(SourceGroup[] array) {
        this.list = new ArrayList(Arrays.asList(array));
    }

    public SourceGroup get(String name) {
        if(name == null || name.trim().length() == 0)
            return null;
        
        name = name.trim();
        for (int i = 0; i < list.size(); i++) {
            SourceGroup item = ((SourceGroup) list.get(i));
            if (item.getName().equalsIgnoreCase(name))
                return item;
        }
        SourceGroup p = new SourceGroup();
        p.setName(name);
        return p;
    }

    public String[] names() {
        List names = new ArrayList();
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            SourceGroup element = (SourceGroup) iter.next();
            names.add(element.getName());
        }
        
        return (String[]) names.toArray(new String[0]);
    }
}
