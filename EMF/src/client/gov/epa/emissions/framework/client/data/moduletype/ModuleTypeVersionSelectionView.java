package gov.epa.emissions.framework.client.data.moduletype;

import java.util.Map;

import gov.epa.emissions.commons.data.ModuleTypeVersion;

public interface ModuleTypeVersionSelectionView {

    void display();

    void observe(ModuleTypeVersionSelectionPresenter presenter);

    void refreshModuleTypeVersions(Map<Integer, ModuleTypeVersion> moduleTypeVersions);

    ModuleTypeVersion getSelectedModuleTypeVersion();
}