package gov.epa.emissions.framework.services.module;

import gov.epa.emissions.framework.services.basic.FilterField;

import java.util.Date;

public class ModuleFilter extends SearchFilterFields {

    public ModuleFilter() {
        addFilterField("Module Name", new FilterField("lm.name", String.class));
        addFilterField("Composite?", new FilterField("liteModuleType.isComposite", Boolean.class));
        addFilterField("Final?", new FilterField("lm.isFinal", Boolean.class));
        addFilterField("Tags", new FilterField("tag.name", String.class));
        addFilterField("Project", new FilterField("project.name", String.class));
        addFilterField("Module Type", new FilterField("liteModuleType.name", String.class));
        addFilterField("Version", new FilterField("liteModuleTypeVersion.version", Integer.class));
        addFilterField("Creator", new FilterField("creator.name", String.class));
        addFilterField("Date (YYYY/MM/DD)", new FilterField("lm.creationDate", Date.class));
        addFilterField("Lock Owner", new FilterField("lm.lockOwner", String.class));
        addFilterField("Lock Date (YYYY/MM/DD)", new FilterField("lm.lockDate", Date.class));
        addFilterField("Description", new FilterField("lm.description", String.class));
    }
}
