package gov.epa.emissions.framework.client.casemanagement.inputs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.CaseService;
import gov.epa.emissions.framework.services.casemanagement.InputName;

public class CaseInputNames {

    private List list;

    private EmfSession session;

    public CaseInputNames(EmfSession session, InputName[] inputNames) {
        this.session = session;
        this.list = new ArrayList(Arrays.asList(inputNames));
    }

    public InputName get(Object selected) throws EmfException {
        if (selected == null)
            throw new EmfException("Input name field can not be empty");
        
        if (selected instanceof String) {
            return editInputNameType(selected);

        }
        int index = list.indexOf(selected);
        return (InputName) list.get(index);
    }

    public InputName[] getAll() {
        return (InputName[]) list.toArray(new InputName[0]);
    }

    private InputName editInputNameType(Object selected) throws EmfException {
        String newInputName = ((String) selected).trim();
        if (newInputName.length() == 0)
            throw new EmfException("Input name field can not be empty");

        InputName name = new InputName(newInputName);
        int index = list.indexOf(name);
        if (index == -1) {// new input name
            InputName persistName = persistName(name);
            list.add(persistName);
            return persistName;
        }
        return (InputName) list.get(index);
    }

    private InputName persistName(InputName name) throws EmfException {
        CaseService service = session.caseService();
        return service.addCaseInputName(name);
    }
}
