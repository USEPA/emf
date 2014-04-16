package gov.epa.emissions.framework.client.casemanagement.jobs;

import gov.epa.emissions.framework.services.casemanagement.jobs.Executable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Executables {

    private List<Executable> list;

    public Executables(Executable[] exes) {
        this.list = new ArrayList<Executable>(Arrays.asList(exes));
    }

    public Executable get(Object selected) {
        if (selected == null)
            return null;
        
        int index = list.indexOf(selected);
        return list.get(index);
    }

    public Executable[] getAll() {
        return list.toArray(new Executable[0]);
    }

}
