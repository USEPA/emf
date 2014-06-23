package gov.epa.emissions.framework.client.meta.revisions;

import gov.epa.emissions.framework.services.editor.Revision;

public interface RevisionsTabView {

    void display(Revision[] revisions, RevisionsTabPresenter presenter);
    
    void refreshMSG();

}
