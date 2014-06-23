package gov.epa.emissions.framework.client.casemanagement.inputs;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.CaseService;
import gov.epa.emissions.framework.services.casemanagement.InputEnvtVar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CaseInputEnvtVars {

    private List list;

    private EmfSession session;

    public CaseInputEnvtVars(EmfSession session, InputEnvtVar[] inputEnvtVars) {
        this.session = session;
        this.list = new ArrayList(Arrays.asList(inputEnvtVars));
    }

    public InputEnvtVar get(Object selected) throws EmfException {
        if (selected instanceof String) {
            return editInputEnvtVarType(selected);

        }
        
        if (selected == null)
            return null;
        
        int index = list.indexOf(selected);
        return (InputEnvtVar) list.get(index);
    }

    public InputEnvtVar[] getAll() {
        return (InputEnvtVar[]) list.toArray(new InputEnvtVar[0]);
    }

    private InputEnvtVar editInputEnvtVarType(Object selected) throws EmfException {
        String newInputEnvtVar = ((String) selected).trim();
        if (newInputEnvtVar.length() == 0)
            return null;

        InputEnvtVar name = new InputEnvtVar(newInputEnvtVar);
        int index = list.indexOf(name);
        if (index == -1) {// new input name
            InputEnvtVar persistName = persistName(name);
            list.add(persistName);
            return persistName;
        }
        return (InputEnvtVar) list.get(index);
    }

    private InputEnvtVar persistName(InputEnvtVar name) throws EmfException {
        CaseService service = session.caseService();
        return service.addInputEnvtVar(name);
    }
}
