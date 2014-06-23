package gov.epa.emissions.framework.client.casemanagement;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseCategory;
import gov.epa.emissions.framework.services.casemanagement.CaseService;

import java.util.Date;

public class NewCasePresenter {
    private NewCaseView view;

    private EmfSession session;

    private CaseManagerPresenter managerPresenter;

    public NewCasePresenter(EmfSession session, NewCaseView view, CaseManagerPresenter managerPresenter) {
        this.session = session;
        this.view = view;
        this.managerPresenter = managerPresenter;
    }

    public void doDisplay() {
        view.observe(this);
        view.display();
    }

    public void doClose() {
        closeView();
    }

    private void closeView() {
        view.disposeView();
    }

    public void doSave(Case newCase) throws EmfException {
        if (isDuplicate(newCase))
            throw new EmfException("A Case named '" + newCase.getName() + "' already exists.");

        newCase.setCreator(session.user());
        newCase.setLastModifiedBy(session.user());
        newCase.setLastModifiedDate(new Date());
        
        Case loaded = service().addCase(session.user(), newCase);
        closeView();
        managerPresenter.addNewCaseToTableData(loaded);
    }

    private boolean isDuplicate(Case newCase) throws EmfException {
        Case[] cases = service().getCases();
        for (int i = 0; i < cases.length; i++) {
            if (cases[i].getName().equals(newCase.getName()))
                return true;
        }

        return false;
    }

    private CaseService service() {
        return session.caseService();
    }
    
    public CaseCategory[] getCaseCategories() throws EmfException {
        return managerPresenter.getCategories();
    }
    
    public CaseCategory getSelectedCategory() {
        return managerPresenter.getSelectedCategory();
    }

}
