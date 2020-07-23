package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.framework.services.basic.FilterField;
import gov.epa.emissions.framework.services.module.SearchFilterFields;

import java.util.Date;

public class ControlStrategyProgramFilter extends SearchFilterFields {

    public ControlStrategyProgramFilter() {
        addFilterField("Name", new FilterField("cp.name", String.class));
        addFilterField("Type", new FilterField("cpt.name", String.class));
        addFilterField("Start", new FilterField("cp.startDate", Date.class));
        addFilterField("Last Modified", new FilterField("cp.lastModifiedDate", Date.class));
        addFilterField("End", new FilterField("cp.endDate", Date.class));
        addFilterField("Description", new FilterField("cp.description", String.class));
        addFilterField("Dataset", new FilterField("dataset.name", String.class));
        addFilterField("Version", new FilterField("cp.datasetVersion", Integer.class));
    }
}
