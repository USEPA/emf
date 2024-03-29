package gov.epa.emissions.framework.client.meta.keywords;

import gov.epa.emissions.framework.client.meta.PropertiesEditorTabPresenter;
import gov.epa.emissions.framework.client.swingworker.LightSwingWorkerPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.Keywords;

public interface EditableKeywordsTabPresenter extends PropertiesEditorTabPresenter, LightSwingWorkerPresenter {

    void display(Keywords masterKeywords);
    
    //void refreshView();
    
    //void refresh() throws EmfException;

}