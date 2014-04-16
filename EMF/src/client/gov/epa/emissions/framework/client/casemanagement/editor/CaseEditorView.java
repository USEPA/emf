package gov.epa.emissions.framework.client.casemanagement.editor;

import java.awt.Cursor;

import gov.epa.emissions.framework.client.ManagedView;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;

public interface CaseEditorView extends ManagedView {

    void observe(CaseEditorPresenter presenter);

    void display(Case caseObj, String msg) throws EmfException;

    void notifyLockFailure(Case caseObj);

    void setCursor(Cursor cursor);

    void showRemindingMessage(String msg);
    
    void showLockingMsg(String msg);
    
}