package gov.epa.emissions.framework.client.moduletype;

import java.util.Map;

import gov.epa.emissions.framework.services.module.ModuleTypeVersion;

public interface ModuleTypeVersionNewRevisionView {

    void display();

    void observe(ModuleTypeVersionNewRevisionPresenter presenter);
}