package gov.epa.emissions.framework.client.casemanagement.parameters;

import gov.epa.emissions.framework.services.casemanagement.parameters.CaseParameter;

public interface NewCaseParameterView {
    void display(int caseId, CaseParameter newParam);

    boolean shouldCreate();

    CaseParameter parameter();
    
    void register(Object presenter);
}
