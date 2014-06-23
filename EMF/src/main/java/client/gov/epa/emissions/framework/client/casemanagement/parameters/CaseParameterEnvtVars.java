package gov.epa.emissions.framework.client.casemanagement.parameters;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.CaseService;
import gov.epa.emissions.framework.services.casemanagement.parameters.ParameterEnvVar;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CaseParameterEnvtVars {

    private List<ParameterEnvVar> list;

    private EmfSession session;

    public CaseParameterEnvtVars(EmfSession session, ParameterEnvVar[] paramEnvtVars) {
        this.session = session;
        this.list = Arrays.asList(paramEnvtVars);
        Collections.sort(list);
    }

    public ParameterEnvVar get(Object selected) throws EmfException {
        if (selected instanceof String) {
            return editParamEnvtVarType(selected);

        }
        
        if (selected == null)
            return null;
        
        int index = list.indexOf(selected);
        return list.get(index);
    }

    public ParameterEnvVar[] getAll() {
        return list.toArray(new ParameterEnvVar[0]);
    }

    private ParameterEnvVar editParamEnvtVarType(Object selected) throws EmfException {
        String newParamEnvtVar = ((String) selected).trim();
        if (newParamEnvtVar.length() == 0)
            return null;

        ParameterEnvVar name = new ParameterEnvVar(newParamEnvtVar);
        int index = list.indexOf(name);
        if (index == -1) {// new input name
            ParameterEnvVar persistName = persistName(name);
            list.add(persistName);
            Collections.sort(list);
            return persistName;
        }
        return list.get(index);
    }

    private ParameterEnvVar persistName(ParameterEnvVar name) throws EmfException {
        CaseService service = session.caseService();
        return service.addParameterEnvVar(name);
    }
}
