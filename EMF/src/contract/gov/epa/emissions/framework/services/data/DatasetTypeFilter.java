package gov.epa.emissions.framework.services.data;

import gov.epa.emissions.framework.services.basic.FilterField;
import gov.epa.emissions.framework.services.module.SearchFilterFields;

import java.util.Date;

public class DatasetTypeFilter extends SearchFilterFields {

    public DatasetTypeFilter() {
        addFilterField("Name", new FilterField("name", String.class));
        addFilterField("# Keywords", new FilterField("keyVals", Integer.class, true));
        addFilterField("# QA Step Templates", new FilterField("qaStepTemplates", Integer.class, true));
        addFilterField("Min Files", new FilterField("minFiles", Integer.class));
        addFilterField("Max Files", new FilterField("maxFiles", Integer.class));
        addFilterField("Description", new FilterField("description", String.class));
    }
}
