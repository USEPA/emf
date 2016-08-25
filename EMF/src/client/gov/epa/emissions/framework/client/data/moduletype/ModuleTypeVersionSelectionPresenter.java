package gov.epa.emissions.framework.client.data.moduletype;

import gov.epa.emissions.commons.data.ModuleType;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DataCommonsService;

public class ModuleTypeVersionSelectionPresenter {

    private EmfSession session;

    private ModuleTypeVersionSelectionView view;
    
    public ModuleTypeVersionSelectionPresenter(ModuleTypeVersionSelectionView view, EmfSession session) {
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
    
    public DataCommonsService service() {
        return session.dataCommonsService();
    }

    public ModuleType[] getModuleTypes() {
        try {
            return service().getModuleTypes();
        } catch (EmfException ex) {
            return new ModuleType[]{};
        }
    }
    
}