package gov.epa.emissions.framework.client.casemanagement.inputs;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.CaseProgram;
import gov.epa.emissions.framework.services.casemanagement.CaseService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Programs {

    private List<CaseProgram> list;

    private EmfSession session;

    public Programs(EmfSession session, CaseProgram[] pgrograms) {
        this.session = session;
        this.list = Arrays.asList(pgrograms);
        Collections.sort(list);
    }

    public CaseProgram get(Object selected) throws EmfException {
        if (selected instanceof String) {
            return addNewProgram(selected);
        }
        
        if (selected == null)
            return null;
        
        int index = list.indexOf(selected);
        return list.get(index);
    }

    public CaseProgram[] getAll() {
        return list.toArray(new CaseProgram[0]);
    }

    private CaseProgram addNewProgram(Object selected) throws EmfException {
        String newProgram = ((String) selected).trim();
        if (newProgram.length() == 0)
            return null;

        CaseProgram name = new CaseProgram(newProgram);
        int index = list.indexOf(name);
        if (index == -1) {// new input name
            CaseProgram persistName = persistName(name);
            list.add(persistName);
            Collections.sort(list);
            return persistName;
        }
        return list.get(index);
    }

    private CaseProgram persistName(CaseProgram name) throws EmfException {
        CaseService service = session.caseService();
        return service.addProgram(name);
    }
}
