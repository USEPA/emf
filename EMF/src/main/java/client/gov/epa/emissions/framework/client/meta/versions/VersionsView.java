package gov.epa.emissions.framework.client.meta.versions;

import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.db.version.Version;

public interface VersionsView {

    void observe(VersionsViewPresenter presenter);

    void add(Version version);

    void reload(Version[] versions);

    void display(Version[] versions, InternalSource[] sources);
}
