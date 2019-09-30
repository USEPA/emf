package gov.epa.emissions.framework.client.moduletype;

import gov.epa.emissions.framework.client.EmfSession;

public class ModuleTypeVersionNewRevisionPresenter {

    private EmfSession session;

    private ModuleTypeVersionNewRevisionView view;
    
    public ModuleTypeVersionNewRevisionPresenter(ModuleTypeVersionNewRevisionView view, EmfSession session) {
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