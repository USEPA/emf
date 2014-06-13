package gov.epa.emissions.framework.client.casemanagement.jobs;

import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;

public interface JobsTabView {
    void refresh(CaseJob[] jobs);
}
