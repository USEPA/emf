package gov.epa.emissions.framework.client.moduletype;

import java.util.Map;

import gov.epa.emissions.framework.services.module.ModuleTypeVersion;

public interface ModuleTypeVersionSelectionView {

    void display();

    void observe(ModuleTypeVersionSelectionPresenter presenter);

    void refreshModuleTypeVersions(Map<Integer, ModuleTypeVersion> moduleTypeVersions);

    ModuleTypeVersion getSelectedModuleTypeVersion();
}