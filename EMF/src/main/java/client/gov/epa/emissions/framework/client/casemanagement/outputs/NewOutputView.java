package gov.epa.emissions.framework.client.casemanagement.outputs;

import gov.epa.emissions.framework.services.casemanagement.outputs.CaseOutput;

public interface NewOutputView {
    void display(int caseId, CaseOutput newOutput);

 //   boolean shouldCreate();

//    CaseOutput Output();
    
    void observe(Object presenter);
}
