package gov.epa.emissions.framework.client.casemanagement;

import gov.epa.emissions.framework.client.casemanagement.editor.EditableCaseSummaryTabView;
import gov.epa.emissions.framework.client.casemanagement.editor.ViewableCaseSummaryTab;
import gov.epa.emissions.framework.client.meta.qa.EditQAArgumentsView;
import gov.epa.emissions.framework.client.meta.qa.EditQAStepView;

public class CaseDescriptionPresenter { // BUG3621
    private CaseDescriptionView view;
    private EditableCaseSummaryTabView editableSummaryView;
    private ViewableCaseSummaryTab     viewableSummaryView;
    
    public CaseDescriptionPresenter(CaseDescriptionView view, EditableCaseSummaryTabView summaryView) {
        this.view = view;
        this.editableSummaryView = summaryView;
    }
    public CaseDescriptionPresenter(CaseDescriptionView view, ViewableCaseSummaryTab summaryView) {
        this.view = view;
        this.viewableSummaryView = summaryView;
    }

    public void display() {
        view.observe(this);
        view.display();
    }
    
    public void refreshDescription(String descText) {
        editableSummaryView.updateDescriptionTextArea(descText);
    }
}
