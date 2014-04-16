package gov.epa.emissions.framework.client.casemanagement;

import gov.epa.emissions.framework.services.casemanagement.CaseCategory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class CaseCategories {

    private List list;

    public CaseCategories(CaseCategory[] array) {
        this.list = new ArrayList(Arrays.asList(array));
    }

    public CaseCategory get(String name) {
        if(name == null || name.trim().length() == 0)
            return null;

        name = name.trim();
        for (int i = 0; i < list.size(); i++) {
            CaseCategory item = ((CaseCategory) list.get(i));
            if (item.getName().equalsIgnoreCase(name))
                return item;
        }
        return new CaseCategory(name);
    }

    public String[] names() {
        List names = new ArrayList();
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            CaseCategory element = (CaseCategory) iter.next();
            names.add(element.getName());
        }

        return (String[]) names.toArray(new String[0]);
    }
}
