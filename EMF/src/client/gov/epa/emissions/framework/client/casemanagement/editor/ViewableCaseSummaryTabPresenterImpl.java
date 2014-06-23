package gov.epa.emissions.framework.client.casemanagement.editor;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;

public class ViewableCaseSummaryTabPresenterImpl extends  CaseSummaryTabPresenterImpl {
    
    private ViewableCaseSummaryTab view;
    private EmfSession session;
   
    public ViewableCaseSummaryTabPresenterImpl(EmfSession session, Case caseObj, ViewableCaseSummaryTab view) {
        super(session, caseObj);
        this.view = view;
        this.session = session;
    }

    @Override
    public Object[] refreshProcessData() throws EmfException {
        refreshObjectManager();
        this.caseObj = session.caseService().reloadCase(caseObj.getId());
        refreshObjectManager();
        return new Case[]{caseObj};
    }

    @Override
    public void refreshDisplay(Object[] objs) throws EmfException {
        view.refresh(caseObj);    
    }

    @Override
    public Object[] swProcessData() throws EmfException {
        // NOTE Auto-generated method stub
        return null;
    }

    @Override
    public void swDisplay(Object[] objs) throws EmfException {
        // NOTE Auto-generated method stub
        
    }

    @Override
    public Object[] saveProcessData() throws EmfException {
        // NOTE Auto-generated method stub
        return null;
    }

    @Override
    public void saveData(Object[] objs) throws EmfException {
        // NOTE Auto-generated method stub
        
    }

}
