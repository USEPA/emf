package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.data.Project;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class Projects {

    private List<Project> list;

    public Projects(Project[] array) {
        this.list = new ArrayList<Project>(Arrays.asList(array));
    }

    public Project get(String name) {
        if(name == null || name.trim().length() == 0)
            return null;
        
        name = name.trim();
        for (int i = 0; i < list.size(); i++) {
            Project item = list.get(i);
            if (item.getName().equalsIgnoreCase(name))
                return item;
        }
        return new Project(name);
    }

    public String[] names() {
        List<String> names = new ArrayList<String>();
        for (Iterator<Project> iter = list.iterator(); iter.hasNext();) {
            Project element = iter.next();
            names.add(element.getName());
        }
        
        return names.toArray(new String[0]);
    }
}
