package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.framework.services.basic.FilterField;
import gov.epa.emissions.framework.services.module.SearchFilterFields;

public class ControlStrategyProgramFilter extends SearchFilterFields {

    public ControlStrategyProgramFilter() {
        addFilterField("Name", new FilterField("cp.name", String.class));
        addFilterField("Type", new FilterField("cpt.name", String.class));
        addFilterField("Description", new FilterField("cp.description", String.class));
        addFilterField("Dataset Name", new FilterField("dataset.name", String.class));
    }
}
