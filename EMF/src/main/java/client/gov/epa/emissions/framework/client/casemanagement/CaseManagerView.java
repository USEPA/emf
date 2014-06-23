package gov.epa.emissions.framework.client.casemanagement;

import gov.epa.emissions.framework.client.ManagedView;
import gov.epa.emissions.framework.client.casemanagement.editor.CaseEditor;
import gov.epa.emissions.framework.client.casemanagement.editor.CaseViewer;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseCategory;

public interface CaseManagerView extends ManagedView {

    void display(CaseCategory[] catetories);

    void observe(CaseManagerPresenter presenter);

    void refresh(Case[] cases);
    
    void refreshWithLastCategory() throws EmfException;

    void addNewCaseToTableData(Case newCase);
    
    CaseCategory getSelectedCategory();
    String getNameFilter();
    
    void setSelectedCategory();
    
    void setMessage(String message);

    void displayCaseComparisonResult(String string, String absolutePath);
    
    Case[] getSCases();
    CaseViewer getCViewer();
    CaseEditor getCEditor();
 
}
