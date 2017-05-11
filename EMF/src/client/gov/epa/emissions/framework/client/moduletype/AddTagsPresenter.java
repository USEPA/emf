package gov.epa.emissions.framework.client.moduletype;

import java.util.HashSet;
import java.util.Set;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.module.Tag;

public class AddTagsPresenter {

    private EmfSession session;

    private AddTagsView view;
    
    public AddTagsPresenter(AddTagsView view, EmfSession session) {
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
    
    public Set<Tag> getAllTags() {
        Set<Tag> retval = new HashSet<Tag>(); 
        try {
            Tag[] tags = session.moduleService().getTags();
            for(Tag tag : tags) {
                retval.add(tag);
            }
            
        } catch (EmfException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }
        return retval;
    }

    public void displayNewTagView(NewTagView view) throws Exception {
        NewTagPresenter presenter = new NewTagPresenter(view, session);
        presenter.display();
    }
}