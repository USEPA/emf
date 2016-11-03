package gov.epa.emissions.framework.client.moduletype;

import gov.epa.emissions.framework.services.module.ModuleTypeVersion;

public interface ModuleTypeVersionObserver {
    void closedChildWindow(ModuleTypeVersion moduleTypeVersion);
}
