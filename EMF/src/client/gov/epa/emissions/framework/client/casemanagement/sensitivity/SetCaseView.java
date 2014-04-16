package gov.epa.emissions.framework.client.casemanagement.sensitivity;

import gov.epa.emissions.framework.client.ManagedView;
import gov.epa.emissions.framework.client.casemanagement.CaseManagerPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;

public interface SetCaseView extends ManagedView {
    void display(Case caseObj) throws EmfException;
    
    void observe(SetCasePresenter presenter, CaseManagerPresenter managerPresenter);

    void notifyLockFailure(Case caseObj);
}
