package gov.epa.emissions.framework.client.casemanagement.sensitivity;

import gov.epa.emissions.framework.client.ManagedView;
import gov.epa.emissions.framework.client.casemanagement.CaseManagerPresenter;
import gov.epa.emissions.framework.services.casemanagement.Case;

public interface SensitivityView extends ManagedView {

    void observe(SensitivityPresenter presenter, CaseManagerPresenter parentPresenter);

    void display(Case case1);

}