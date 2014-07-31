package gov.epa.emissions.framework.client.data.viewer;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.TableMetadata;
import gov.epa.emissions.framework.client.ManagedView;

public interface DataView extends ManagedView {
    void display(Version version, String table, TableMetadata tableMetadata);

//    void populate();
    
    void observe(DataViewPresenter presenter);

    void populate(TableMetadata tableMetadata);
}
