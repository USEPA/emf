package gov.epa.emissions.framework.client.casemanagement;

import gov.epa.emissions.framework.services.casemanagement.Speciation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class Speciations {

    private List list;

    public Speciations(Speciation[] array) {
        this.list = new ArrayList(Arrays.asList(array));
    }

    public Speciation get(String name) {
        if(name == null || name.trim().length() == 0)
            return null;

        name = name.trim();
        for (int i = 0; i < list.size(); i++) {
            Speciation item = ((Speciation) list.get(i));
            if (item.getName().equalsIgnoreCase(name))
                return item;
        }
        return new Speciation(name);
    }

    public String[] all() {
        List names = new ArrayList();
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            Speciation element = (Speciation) iter.next();
            names.add(element.getName());
        }

        return (String[]) names.toArray(new String[0]);
    }
}
