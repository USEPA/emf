package gov.epa.emissions.framework.client.moduletype;

import gov.epa.emissions.framework.client.EmfSession;

public class RemoveTagsPresenter {

    private EmfSession session;

    private RemoveTagsView view;
    
    public RemoveTagsPresenter(RemoveTagsView view, EmfSession session) {
        this.session = session;
        this.view = view;
    }

    public void display() throws Exception {
        view.observe(this);
        view.display();
    }
    
    public EmfSession getSession(){
        return session; 
    }
}