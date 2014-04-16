package gov.epa.emissions.framework.client.meta.summary;

import gov.epa.emissions.commons.data.Project;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.client.meta.PropertiesEditorTabPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;

public interface EditableSummaryTabPresenter extends PropertiesEditorTabPresenter {
    EmfDataset reloadDataset() throws EmfException;
    Version[] getVersions() throws EmfException;
    Project[] getProjects();
}