package gov.epa.emissions.framework.client.casemanagement.editor;

import gov.epa.emissions.framework.services.casemanagement.Case;

public class ViewableCaseSummaryTabPresenterImpl implements ViewableCaseSummaryTabPresenter {

    private Case caseObj;

    public ViewableCaseSummaryTabPresenterImpl(Case caseObj, ViewableCaseSummaryTab view) {
        this.caseObj = caseObj;
    }

    public Case getCaseObj() {
        return this.caseObj;
    }

}
