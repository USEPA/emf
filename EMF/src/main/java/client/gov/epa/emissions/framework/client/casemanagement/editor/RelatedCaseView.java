package gov.epa.emissions.framework.client.casemanagement.editor;

import gov.epa.emissions.framework.services.casemanagement.Case;


public interface RelatedCaseView {

    void display(Case[] inputCases, Case[] outputCases);

    void observe(RelatedCasePresenter presenter);    
    
    
}
