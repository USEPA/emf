package gov.epa.emissions.framework.client.casemanagement.jobs;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.CaseService;
import gov.epa.emissions.framework.services.casemanagement.jobs.Host;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Hosts {

    private List<Host> list;

    private EmfSession session;

    public Hosts(EmfSession session, Host[] hosts) {
        this.session = session;
        this.list = Arrays.asList(hosts);
        Collections.sort(list);
    }

    public Host get(Object selected) throws EmfException {
        if (selected instanceof String) {
            return editHostType(selected);
        }
        
        if (selected == null)
            return null;
        
        int index = list.indexOf(selected);
        return list.get(index);
    }

    public Host[] getAll() {
        return list.toArray(new Host[0]);
    }

    private Host editHostType(Object selected) throws EmfException {
        String newhost = ((String) selected).trim();
        if (newhost.length() == 0)
            return null;

        Host name = new Host(newhost);
        int index = list.indexOf(name);
        if (index == -1) {// new input name
            Host persistName = persistName(name);
            list.add(persistName);
            Collections.sort(list);
            return persistName;
        }
        return list.get(index);
    }

    private Host persistName(Host host) throws EmfException {
        CaseService service = session.caseService();
        return service.addHost(host);
    }
}
