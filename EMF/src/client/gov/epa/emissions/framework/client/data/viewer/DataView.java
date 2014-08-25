package gov.epa.emissions.framework.client.data.viewer;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.client.ManagedView;

public interface DataView extends ManagedView {
    void display(Version version, String table);

//    void populate();
    
    void observe(DataViewPresenter presenter);

    void populate(String table);
}
