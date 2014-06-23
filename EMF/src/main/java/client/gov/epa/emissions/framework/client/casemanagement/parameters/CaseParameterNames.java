package gov.epa.emissions.framework.client.casemanagement.parameters;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.CaseService;
import gov.epa.emissions.framework.services.casemanagement.parameters.ParameterName;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CaseParameterNames {

    private List<ParameterName> list;

    private EmfSession session;

    public CaseParameterNames(EmfSession session, ParameterName[] paramNames) {
        this.session = session;
        this.list = Arrays.asList(paramNames);
        Collections.sort(this.list);
    }

    public ParameterName get(Object selected) throws EmfException {
        if (selected == null)
            throw new EmfException("Parameter name field can not be empty");
        
        if (selected instanceof String) {
            return editParameterNameType(selected);

        }
        int index = list.indexOf(selected);
        return list.get(index);
    }

    public ParameterName[] getAll() {
        return list.toArray(new ParameterName[0]);
    }

    private ParameterName editParameterNameType(Object selected) throws EmfException {
        String newParamName = ((String) selected).trim();
        if (newParamName.length() == 0)
            throw new EmfException("Parameter name field can not be empty");

        ParameterName name = new ParameterName(newParamName);
        int index = list.indexOf(name);
        if (index == -1) {// new input name
            ParameterName persistName = persistName(name);
            list = Arrays.asList(session.caseService().getParameterNames());
            Collections.sort(list);
            return persistName;
        }
        return list.get(index);
    }

    private ParameterName persistName(ParameterName name) throws EmfException {
        CaseService service = session.caseService();
        return service.addParameterName(name);
    }
}
