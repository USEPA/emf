package gov.epa.emissions.framework.client.casemanagement.editor;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseService;

public class RelatedCasePresenter {
    private EmfSession session;
    //private int caseId;
    private RelatedCaseView view;
    
    public RelatedCasePresenter(RelatedCaseView view, EmfSession session) {
        this.session = session;
        this.view = view;
    }
    
    public void doDisplay(Case[] inputCases, Case[] outputCases) {
        view.observe(this);
        view.display(inputCases, outputCases);
    }
    
    private CaseService service() {
        return session.caseService();
    }
    
    public Case getCaseFromName(String caseName) throws EmfException{
        return service().getCaseFromName(caseName);
    }
    
    public void doView(CaseViewerView caseView, Case caseObj) throws EmfException {
        CaseViewerPresenter presenter = new CaseViewerPresenterImpl(caseObj, session, caseView);
        presenter.doDisplay();
    }
    
    

}
