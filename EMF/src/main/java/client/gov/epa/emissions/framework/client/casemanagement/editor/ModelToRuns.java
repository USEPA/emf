package gov.epa.emissions.framework.client.casemanagement.editor;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.CaseService;
import gov.epa.emissions.framework.services.casemanagement.ModelToRun;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ModelToRuns {
    private List list;

    private EmfSession session;

    public ModelToRuns(EmfSession session, ModelToRun[] models) {
        this.session = session;
        this.list = new ArrayList(Arrays.asList(models));
    }

    public ModelToRun get(Object selected) throws EmfException {
        if (selected instanceof String) {
            return editModelRoRunType(selected);
        }
        
        if (selected == null)
            return null;
        
        int index = list.indexOf(selected);
        return (ModelToRun) list.get(index);
    }

    public ModelToRun[] getAll() {
        return (ModelToRun[]) list.toArray(new ModelToRun[0]);
    }

    private ModelToRun editModelRoRunType(Object selected) throws EmfException {
        String newModelName = ((String) selected).trim();
        if (newModelName.length() == 0)
            return null;

        ModelToRun name = new ModelToRun(newModelName);
        int index = list.indexOf(name);
        if (index == -1) {// new input name
            ModelToRun persistName = persistName(name);
            list.add(persistName);
            return persistName;
        }
        return (ModelToRun) list.get(index);
    }

    private ModelToRun persistName(ModelToRun model) throws EmfException {
        CaseService service = session.caseService();
        return service.addModelToRun(model);
    }
}
