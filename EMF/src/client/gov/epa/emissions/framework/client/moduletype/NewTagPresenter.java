package gov.epa.emissions.framework.client.moduletype;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.module.Tag;

public class NewTagPresenter {

    private EmfSession session;

    private NewTagView view;
    
    public NewTagPresenter(NewTagView view, EmfSession session) {
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
    
    public void addTag(String name, String description) throws EmfException {
        session.moduleService().addTag(new Tag(name, description));
    }
}