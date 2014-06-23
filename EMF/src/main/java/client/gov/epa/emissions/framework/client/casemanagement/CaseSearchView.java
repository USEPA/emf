package gov.epa.emissions.framework.client.casemanagement;

import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseCategory;
import gov.epa.emissions.framework.services.data.EmfDataset;

public interface CaseSearchView {

    void display(CaseCategory[] categories);

    void display(CaseCategory[] categories, CaseCategory defaultCategory, boolean selectSingle);

    void observe(CaseSearchPresenter presenter);

    void refreshCases(Case[] cases);

    Case[] getCases();

    boolean shouldCreate();
    
    void clearMessage();
}