package gov.epa.emissions.framework.client.meta.versions;

import gov.epa.emissions.commons.db.version.Version;

public interface EditVersionsView {

    void observe(EditVersionsPresenter presenter);

    void add(Version version);

    void reload();
    
    void refresh(); 

    void display(Version[] versions);

    void notifyLockFailure(Version version);

}
