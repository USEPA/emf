package gov.epa.emissions.framework.client.module;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.BasicSearchFilter;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.module.*;

import java.util.concurrent.ConcurrentSkipListMap;

public class ModulesManagerPresenter {

    private ModulesManagerView view;

    private EmfSession session;

    public ModulesManagerPresenter(EmfSession session, ModulesManagerView view) {
        this.session = session;
        this.view = view;
    }

    public void doDisplay() {
        view.observe(this);
        view.display();
        view.populate();
    }

    private ModuleService service() {
        return session.moduleService();
    }

    public void doClose() {
        view.disposeView();
    }

    public void displayNewModuleView(ModulePropertiesView view) {
        ModulePropertiesPresenter presenter = new ModulePropertiesPresenter(session, view);
        presenter.doDisplay();
    }

    public void displayModuleComparisonView(ModuleComparisonView view) {
        ModuleComparisonPresenter presenter = new ModuleComparisonPresenter(session, view);
        presenter.doDisplay();
    }

    public int[] doRemove(int[] moduleIds, boolean deleteOutputs) throws EmfException {
        
        int[] lockedModuleIds = lockModules(moduleIds);
        if (lockedModuleIds == null)
            return new int[0];

        try {
            return service().deleteModules(session.user(), lockedModuleIds, deleteOutputs);
        } catch (EmfException e) {
            throw new EmfException(e.getMessage());
        } finally {
            unlockModules(lockedModuleIds);
        }
    }

    public void runModules(int[] moduleIds) throws EmfException {

        int[] lockedModuleIds = lockModules(moduleIds);
        if (lockedModuleIds == null)
            return;

        try {
            session.moduleService().runModules(lockedModuleIds, session.user());
        } catch (EmfException e) {
            throw new EmfException(e.getMessage());
        } finally {
            unlockModules(lockedModuleIds);
        }
    }

    private int[] lockModules(int[] moduleIds) throws EmfException{
        int[] lockedModuleIds = service().lockModules(session.user(), moduleIds);
        return lockedModuleIds;
    }

    private int[] unlockModules(int[] moduleIds) throws EmfException{
        int[] lockedModuleIds = service().unlockModules(session.user(), moduleIds);
        return lockedModuleIds;
    }

    public ConcurrentSkipListMap<Integer, LiteModule> getLiteModules() {
        return session.getFreshLiteModules();
    }

    public LiteModule[] getLiteModules(BasicSearchFilter searchFilter) throws EmfException {
        return service().getLiteModules(searchFilter);
    }

    public Module getModule(int id) throws EmfException {
        return service().getModule(id);
    }

    public Module getModule(String name) throws EmfException {
        return service().getModule(name);
    }

    public Module addModule(Module module) throws EmfException {
        return session.moduleService().addModule(module);
    }

    public ModuleType getModuleType(String name) throws EmfException {
        return service().getModuleType(name);
    }

    public EmfDataset getDataset(String name) throws EmfException {
        return session.dataService().getDataset(name);
    }

    public Version getVersion(EmfDataset dataset, int version) throws EmfException {
        return session.dataViewService().getVersion(dataset.getId(), version);
    }
}
