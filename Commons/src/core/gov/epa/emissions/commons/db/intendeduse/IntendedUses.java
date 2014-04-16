package gov.epa.emissions.commons.db.intendeduse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IntendedUses {

    private List list;

    public IntendedUses(IntendedUse[] array) {
        this.list = new ArrayList(Arrays.asList(array));
    }

    public IntendedUse get(String name) {
        name = name.trim();
        for (int i = 0; i < list.size(); i++) {
            IntendedUse item = ((IntendedUse) list.get(i));
            if (item.getName().equalsIgnoreCase(name))
                return item;
        }
        return new IntendedUse(name);
    }

}
