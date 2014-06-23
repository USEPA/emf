package gov.epa.emissions.framework.client.casemanagement;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.MeteorlogicalYear;
import gov.epa.emissions.framework.services.cost.controlmeasure.YearValidation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class MeteorlogicalYears {

    private List list;

    public MeteorlogicalYears(MeteorlogicalYear[] array) {
        this.list = new ArrayList(Arrays.asList(array));
    }

    public MeteorlogicalYear get(String name) throws EmfException {
        if(name == null || name.trim().length() == 0)
            return null;

        name = name.trim();
        for (int i = 0; i < list.size(); i++) {
            MeteorlogicalYear item = ((MeteorlogicalYear) list.get(i));
            if (item.getName().equalsIgnoreCase(name))
                return item;
        }
        validateYear(name);
        return new MeteorlogicalYear(name);
    }

    private void validateYear(String name) throws EmfException {
        new YearValidation("Meteorological Year").value(name);
    }

    public String[] names() {
        List names = new ArrayList();
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            MeteorlogicalYear element = (MeteorlogicalYear) iter.next();
            names.add(element.getName());
        }

        return (String[]) names.toArray(new String[0]);
    }
}
