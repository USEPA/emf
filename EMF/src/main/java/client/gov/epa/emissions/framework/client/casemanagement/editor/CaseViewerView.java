package gov.epa.emissions.framework.client.casemanagement.editor;

import java.awt.Cursor;

import gov.epa.emissions.framework.client.ManagedView;
import gov.epa.emissions.framework.services.casemanagement.Case;

public interface CaseViewerView extends ManagedView {

    void observe(CaseViewerPresenter presenter, Case caseObj);

    void display(String caseJobSummaryMsg);

    //void setCursor(Cursor cursor);
    
    //void showMsg(String msg);

}