package gov.epa.emissions.framework.services.module;

import gov.epa.emissions.commons.data.Module;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;

public interface ModuleService {
    void runModules(Module[] modules, User user) throws EmfException;
}
