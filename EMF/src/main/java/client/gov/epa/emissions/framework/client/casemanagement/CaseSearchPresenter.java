package gov.epa.emissions.framework.client.casemanagement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseCategory;

public class CaseSearchPresenter {

    private EmfSession session;

    private CaseSearchView view;
    
    private static CaseCategory lastCaseCategory = null;
    
    private static String lastNameContains = null;
    
    private static Case[] lastCases = null;

    public CaseSearchPresenter(CaseSearchView view, EmfSession session) {
        this.session = session;
        this.view = view;
    }
    
    public void display(CaseCategory defaultCategory, boolean selectSingle) throws Exception {
        view.observe(this);

        //get data...
        List<CaseCategory> categories = new ArrayList<CaseCategory>();;
//        CaseCategory[] caseCategories = new CaseCategory[] {};
        categories.add(new CaseCategory("All"));
        categories.addAll(Arrays.asList(session.caseService().getCaseCategories())); 

        view.display(categories.toArray(new CaseCategory[0]), defaultCategory, selectSingle);
    }

    public void refreshCases(CaseCategory caseCategory, String nameContaining) throws EmfException {
        if ((lastCases!=null) && (lastCaseCategory!=null) && caseCategory.getName().equals(lastCaseCategory.getName()) && (nameContaining.equals(lastNameContains)))
        {
            // nothing has changed since last time, so just refresh with the previously retrieved list
            view.refreshCases(lastCases);
        }    
        else 
        {
            lastCases = session.caseService().getCases(caseCategory, nameContaining);
            List<Case> caseArrayList = new ArrayList<Case>();;
//          CaseCategory[] caseCategories = new CaseCategory[] {};
            caseArrayList.add(new Case("All"));
            caseArrayList.addAll(Arrays.asList(lastCases)); 
            view.refreshCases(caseArrayList.toArray(new Case[0]));      
        }
        lastCaseCategory = caseCategory;
        lastNameContains = nameContaining;        
    }
    
    public Case[] getCases() {
        return view.getCases();
    }
    
    public EmfSession getSession(){
        return session; 
    }

}
