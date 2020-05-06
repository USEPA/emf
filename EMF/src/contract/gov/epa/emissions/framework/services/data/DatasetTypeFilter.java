package gov.epa.emissions.framework.services.data;

import gov.epa.emissions.framework.services.basic.FilterField;
import gov.epa.emissions.framework.services.module.SearchFilterFields;

import java.util.Date;

public class DatasetTypeFilter extends SearchFilterFields {

    public DatasetTypeFilter() {
        addFilterField("Name", new FilterField("dt.name", String.class));
        addFilterField("# Keywords", new FilterField("dt.keyVals", Integer.class, true));
        addFilterField("# QA Step Templates", new FilterField("dt.qaStepTemplates", Integer.class, true));
        addFilterField("Min Files", new FilterField("dt.minFiles", Integer.class));
        addFilterField("Max Files", new FilterField("dt.maxFiles", Integer.class));
        addFilterField("Description", new FilterField("dt.description", String.class));
    }
}
