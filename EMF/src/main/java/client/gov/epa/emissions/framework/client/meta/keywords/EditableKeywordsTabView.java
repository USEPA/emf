package gov.epa.emissions.framework.client.meta.keywords;

import gov.epa.emissions.commons.data.KeyVal;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.Keywords;

public interface EditableKeywordsTabView {
    void display(EmfDataset dataset, Keywords masterKeywords);
    
    EmfDataset getDataset();

    KeyVal[] updates() throws EmfException;
    
    void observe(EditableKeywordsTabPresenter presenter);
}
