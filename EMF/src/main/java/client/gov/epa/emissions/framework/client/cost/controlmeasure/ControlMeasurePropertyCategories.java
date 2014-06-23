package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasurePropertyCategory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ControlMeasurePropertyCategories {

    private List<ControlMeasurePropertyCategory> categories;

    private EmfSession session;
    
    public ControlMeasurePropertyCategories(ControlMeasurePropertyCategory[] categories, EmfSession session) {
        this.categories = new ArrayList<ControlMeasurePropertyCategory>(Arrays.asList(categories));
        this.session = session;
    }

    public ControlMeasurePropertyCategory get(String name) throws EmfException {
        name = name.trim();
        for (int i = 0; i < categories.size(); i++) {
            ControlMeasurePropertyCategory category = categories.get(i);
            if (category.getName().equalsIgnoreCase(name))
                return category;
        }
        //couldn't find it in the list, lets create a new one and add it the list for future use
        return session.controlMeasureService().getPropertyCategory(name);
    }
    
    public boolean remove(ControlMeasurePropertyCategory category){
        return categories.remove(category);
    }

    public ControlMeasurePropertyCategory[] all() {
        return categories.toArray(new ControlMeasurePropertyCategory[0]);
    }

    public boolean contains(String name) {
        for (int i = 0; i < categories.size(); i++) {
            if (categories.get(i).getName().equalsIgnoreCase(name))
                return true;
        }
        return false;
    }
}
