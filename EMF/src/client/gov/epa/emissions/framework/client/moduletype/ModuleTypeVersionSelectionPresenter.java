package gov.epa.emissions.framework.client.moduletype;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DataCommonsService;
import gov.epa.emissions.framework.services.module.ModuleType;

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
    
    public ModuleType[] getModuleTypes() {
        try {
            return session.moduleService().getModuleTypes();
        } catch (EmfException ex) {
            return new ModuleType[]{};
        }
    }
    
}